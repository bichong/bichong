/*
* Copyright 2013-2014 the original author or authors.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package xi.zhao.wallet.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import DomXMLpraser.DomXmlPraser;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import android.database.ContentObserver;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;


import android.os.Bundle;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.TransactionConfidence;
import com.google.bitcoin.core.Utils;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.TransactionConfidence.ConfidenceType;
import com.google.bitcoin.core.Wallet.BalanceType;
import com.google.bitcoin.core.Wallet.SendRequest;
import com.google.bitcoin.core.WrongNetworkException;


import xi.zhao.wallet.AddressBookProvider;
import xi.zhao.wallet.Constants;
import xi.zhao.wallet.ExchangeRatesProvider;
import xi.zhao.wallet.R;
import xi.zhao.wallet.WalletApplication;
import xi.zhao.wallet.ExchangeRatesProvider.ExchangeRate;
import xi.zhao.wallet.util.WalletUtils;

public final class ChongZhiSendCoinsFragment extends SherlockFragment {

	private AbstractBindServiceActivity activity;
	private WalletApplication application;
	private Wallet wallet;
	private ContentResolver contentResolver;
	private LoaderManager loaderManager;
	
	private String hash;
	
	private String dindaninfo=null;
	private ProgressDialog progressDialog1;
	private ProgressDialog progressDialog2;
	private final Handler handler = new Handler() 
	{
		
		@Override  
	    public void handleMessage(Message msg) {  
        int ss=msg.arg1;
        //关闭ProgressDialog  
        if(ss==-1)
        { 	
        	progressDialog1.dismiss();  
        	new AlertDialog.Builder(activity).setTitle("通知")  
   		    .setMessage("发送比特币时发生了问题，请稍后重试！")  
   		    .setPositiveButton("确定", null)   
   		    .show(); 
        }
        //更新UI  
		}
	};
	private HandlerThread backgroundThread;
	private Handler backgroundHandler;
	//private TextView chongzhi_adress,chongzhi_xinxi;
	//private TextView chongzhi_blance;
	private Button viewCancel,viewGo;
	private static final String[] m={"10","20","30","50","100"};  
	    private AutoCompleteTextView edithaoma ;  
	    private Spinner spinner;  
	    private ArrayAdapter<String> adapter;  


	private BigInteger rate=null;//服务器返回的汇率

	private AddressAndLabel validatedAddress = null;
	private Address selectedAddress = null;

	private static final int ID_RATE_LOADER = 0;

	private LinearLayout lout;
	
	private State state = State.INPUT;
	private Transaction sentTransaction = null;
	private ExchangeRate exchangeRate=null;
	//private BigInteger localValue=null;
	private BigInteger btcValue=null;
	//private BigInteger balance=null;
	private static final Logger log = LoggerFactory.getLogger(ChongZhiSendCoinsFragment.class);

	private ArrayAdapter<String> adapter_history;
	
	private boolean tijiaoState=false;
	private enum State
	{
		INPUT, PREPARATION, SENDING, SENT, FAILED
	}
	/**
	 * 检查网络连接是否正常
	 * @return 
	 * 正常返回 true
	 * 否则返回false
	 */
	private boolean isOpenNetwork() {  
	    ConnectivityManager connManager = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);  
	    if(connManager.getActiveNetworkInfo() != null) {  
	        return connManager.getActiveNetworkInfo().isAvailable();  
	    }  
	  
	    return false;  
	}  
	
	private final TransactionConfidence.Listener sentTransactionConfidenceListener = new TransactionConfidence.Listener()
	{
		boolean flag=false;
		@Override
		public void onConfidenceChanged(final Transaction tx, final TransactionConfidence.Listener.ChangeReason reason)
		{
			activity.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					Log.e("reason",reason.toString());
					if (state == State.SENDING)
					{
						final TransactionConfidence confidence = sentTransaction.getConfidence();
						Log.e("confidence.getConfidenceType()", confidence.getConfidenceType().toString()+confidence.numBroadcastPeers());
						if (confidence.getConfidenceType() == ConfidenceType.DEAD)
							{
								state = State.FAILED;
								//chongzhi_xinxi.setText("发送比特币时出现了问题，请稍后重试！");
								progressDialog1.dismiss();
			 					new AlertDialog.Builder(activity).setTitle("通知")  
					   		    .setMessage("发送比特币时发生了问题，请稍后重试！")  
					   		    .setPositiveButton("确定", null)   
					   		    .show();  
							}
						else if (confidence.numBroadcastPeers() > 1 || confidence.getConfidenceType() == ConfidenceType.BUILDING)
						{	
//							if(tijiaoState==false)
//								{
//									hash=tx.getHashAsString();
//									tijiaoDingdan(dindaninfo+"&hash="+hash);
//									tijiaoState=true;
//									
//								}
							
						}
					
					}
					
				}
			});
		}
	};
	

	@Override
	public void onAttach(final Activity activity)
	{
		super.onAttach(activity);

		this.activity = (AbstractBindServiceActivity) activity;
		this.application = (WalletApplication) activity.getApplication();
		this.wallet = application.getWallet();
		this.contentResolver = activity.getContentResolver();
		this.loaderManager = getLoaderManager();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);


		selectedAddress = application.determineSelectedAddress(); //获得默认地址
		backgroundThread = new HandlerThread("backgroundThread", Process.THREAD_PRIORITY_BACKGROUND);
		backgroundThread.start();
		backgroundHandler = new Handler(backgroundThread.getLooper());

		//btcPrecision = Integer.parseInt(prefs.getString(Constants.PREFS_KEY_BTC_PRECISION, Constants.PREFS_DEFAULT_BTC_PRECISION));
	}
	
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		
		final View view = inflater.inflate(R.layout.chongzhiwindow1, container);
		edithaoma=(AutoCompleteTextView) view.findViewById(R.id.EditText_haoma);
		edithaoma.setThreshold(2);
		SharedPreferences sp = activity.getSharedPreferences("history_strs", 0);
	        String save_history = sp.getString("history", "");
	        String[] hisArrays = save_history.split(",");
	        adapter_history = new ArrayAdapter<String>(activity,android.R.layout.simple_dropdown_item_1line, hisArrays);
	        if (hisArrays.length > 50) {
	            String[] newArrays = new String[50];
	            System.arraycopy(hisArrays, 0, newArrays, 0, 50);
	            adapter_history = new ArrayAdapter<String>(activity,
	                    android.R.layout.simple_dropdown_item_1line, newArrays);
	        }
	    edithaoma.setAdapter(adapter_history);
	    spinner=(Spinner) view.findViewById(R.id.Spinner);
	    adapter = new ArrayAdapter<String>(activity,android.R.layout.simple_spinner_item,m);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
	    spinner.setAdapter(adapter);   
	    viewCancel=(Button) view.findViewById(R.id.cancel);
	    viewGo=(Button) view.findViewById(R.id.go);
	    //验证时否是测试网络
	    try {
			if(Address.getParametersFromAddress("1GhPwrGpe8PUyELBnKotK8woMfbBcah4L2").equals(Constants.NETWORK_PARAMETERS))
				Log.e("test?", Constants.NETWORK_PARAMETERS.toString());
			
		} catch (AddressFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   
		//提交按钮监听器   
		viewGo.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				tijiaoState=false;
				 Save();
				//防止多次快速的点击提交按钮
				if (zhao.util.Utils.isFastDoubleClick()) {  
			        return;  
			     }
				
				//Log.e("rate", exchangeRate.rate.toString());
				if(isOpenNetwork() == true) 
				{ 
					progressDialog1 = ProgressDialog.show(activity, "", "请稍候", true, false);  
					chaXunTiJiao();
				}
				 else
				{
					 //activity.longToast("请检查网络连接，确保网络已打开！");
					 new AlertDialog.Builder(activity).setTitle("通知")  
			   		    .setMessage("请检查网络链接")  
			   		    .setPositiveButton("确定", null)   
			   		    .show();  
		    	}
			}
		});

		//取消按钮监听器
		viewCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				
				activity.finish();
			}
		});

		

		
		return view;
	}


	@Override
	public void onResume()
	{
		super.onResume();

		
	}

	@Override
	public void onPause()
	{

		loaderManager.destroyLoader(ID_RATE_LOADER);
		loaderManager.destroyLoader(1);


		//contentResolver.unregisterContentObserver(contentObserver);

		super.onPause();
	}

	@Override
	public void onDetach()
	{
		handler.removeCallbacksAndMessages(null);

		super.onDetach();
	}

	@Override
	public void onDestroy()
	{
		backgroundThread.getLooper().quit();

		if (sentTransaction != null)
			sentTransaction.getConfidence().removeEventListener(sentTransactionConfidenceListener);

		super.onDestroy();
	}

	
	@Override
	public void onSaveInstanceState(final Bundle outState)
	{
		super.onSaveInstanceState(outState);

		
	}

	/**
	 * 验证比特币余额是否充足
	 * @return
	 * 充足返回true
	 * 否则返回false
	 */
	private boolean validateAmounts()
	{
		boolean isValidAmounts = false;

		final BigInteger amount = btcValue;

		
		if (amount.signum() > 0)
		{
			final BigInteger estimated = wallet.getBalance(BalanceType.ESTIMATED);
			final BigInteger available = wallet.getBalance(BalanceType.AVAILABLE);
			//final BigInteger pending = estimated.subtract(available);
			// TODO subscribe to wallet changes

			final BigInteger availableAfterAmount = available.subtract(amount);
			final boolean enoughFundsForAmount = availableAfterAmount.signum() >= 0;

			if (enoughFundsForAmount)
			{
				// everything fine
				isValidAmounts = true;
			}
			else
			{
				// not enough funds for amount
				
			}
		}
		return isValidAmounts;
		
	}

	/**
	 * 生成交易单，并把交易发送出去
	 */
	private void handleGo()
	{
		state = State.PREPARATION;
		
		progressDialog1 = ProgressDialog.show(activity, "", "正在充值中，请稍候！不要关闭本窗口！", true, false);;
		// create spend
		final BigInteger amount = btcValue;
		final SendRequest sendRequest = SendRequest.to(validatedAddress.address, amount);
		sendRequest.changeAddress = WalletUtils.pickOldestKey(wallet).toAddress(Constants.NETWORK_PARAMETERS);
		sendRequest.emptyWallet = amount.equals(wallet.getBalance(BalanceType.AVAILABLE));

		backgroundHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				final Transaction transaction = wallet.sendCoinsOffline(sendRequest);
				handler.post(new Runnable()
				{
					@Override
					public void run()
					{
						if (transaction != null)
						{
							// play sound effect
							RingtoneManager.getRingtone(activity,
									Uri.parse("android.resource://" + activity.getPackageName() + "/" + R.raw.send_coins)).play();

							sentTransaction = transaction;
							
							state = State.SENDING;
							//dindaninfo=inint();
							
							sentTransaction.getConfidence().addEventListener(sentTransactionConfidenceListener);

							
							activity.getBlockchainService().broadcastTransaction(sentTransaction);
							
							
									hash=sentTransaction.getHashAsString();
									tijiaoDingdan(dindaninfo+"&hash="+hash);
									
								
							            

					        
							
						}
						else
						{
							state = State.FAILED;
							 Message msg = handler.obtainMessage();//同 new Message();  
					         msg.arg1 =-1;  
							 handler.sendMessage(msg);  
							//activity.longToast(R.string.send_coins_error_msg);
						}
					}
				});
			}
		});
	}
	//验证内嵌的地址是否为null以及比特币余额是否充足
	private boolean everythingValid()
	{
		return validatedAddress != null && validateAmounts();
	}


     /**
      * 生成订单信息。
      * @return
      * 订单信息的字符串形式：mobile=15********&value=0.***
      */
	  private String inint2()
     {
    	 StringBuilder sb = new StringBuilder();
			sb.append("mobile=");
			sb.append(edithaoma.getText().toString());
			sb.append("&value=");
			sb.append(spinner.getSelectedItem().toString());
			sb.append("&adress=");
			sb.append(selectedAddress.toString());
		 	sb.append("&btcvalue=");
		 	sb.append(btcValue.toString());
			//sb.append("&charset=utf-8");
			return sb.toString();
     }
	  /**
	   * 向服务器提交订单
	   * @param info
	   * 订单信息
	   */
	 private void tijiaoDingdan(String info) {
		 	
		 	Log.e("tiajio", "提交订单");	
		 	//Log.e("zong", dindaninfo2);
		 	//服务器的提交订单的接口
			new DownloadTextTask().execute("http://182.92.2.150:8473/com.zhao/tiJiaoServlet?"+info);
		}
	/**
	 * 查询服务器是否开启了充值功能	
	 */
	 private void chaXunTiJiao() {
		 	//Log.e("tiajio", "提交订单");	
			new DownloadTextTask2().execute("http://182.92.2.150:8473/com.zhao/YesorNo?");
	 }
		
	 /**
	  * 打开URL链接
	  * @param urlString
	  * url
	  * @return
	  * 服务器的响应
	  * @throws IOException
	  */
	 private InputStream OpenHttpConnection(String urlString) 
		     throws IOException
	 {
		         InputStream in = null;
		         int response = -1;
		                
		         URL url = new URL(urlString); 
		         URLConnection conn = url.openConnection();
		                  
		         if (!(conn instanceof HttpURLConnection))                     
		             throw new IOException("Not an HTTP connection");        
		         try{
		             HttpURLConnection httpConn = (HttpURLConnection) conn;
		             httpConn.setAllowUserInteraction(false);//allowUserInteraction 如果为 true，则在允许用户交互（例如弹出一个验证对话框）的上下文中对此 URL 进行检查。
		             httpConn.setInstanceFollowRedirects(true);//支持302自动自动跳转
		             httpConn.setRequestMethod("GET");
		             httpConn.connect();
		             response = httpConn.getResponseCode();                 
		             if (response == HttpURLConnection.HTTP_OK) {
		                 in = httpConn.getInputStream();                                 
		             }                     
		         }
		         catch (Exception ex)
		         {
		         	Log.d("Networking", ex.getLocalizedMessage());
		             throw new IOException("Error connecting");
		         }
		         return in;     
		}
		     
		    
		private String DownloadText(String URL)
	     {
	         InputStream in = null;
	         try {
	             in = OpenHttpConnection(URL);
	             if(in!=null)
	             {
	            	 Log.e("提交结果","有返回" );
	            	 InputStreamReader isr=new InputStreamReader(in);
	            	 int charRead;
	            	 String str="";
	            	 char[] inputBuffer=new char[100];
	            	 try{
	            		 while((charRead=isr.read(inputBuffer))>0){
	            			 String readString=String.copyValueOf(inputBuffer,0,charRead);
	            			 str+=readString;
	            			 inputBuffer=new char[100];
	            		 }
	            		 in.close();
	            	 }catch(IOException e){
	            	 return "";
	            	 }
	            	 return str;
	             }
	             else
	             {
	            	 return "";
	             }
	         } catch (IOException e) {
	         	Log.d("NetworkingActivity", e.getLocalizedMessage());
	             return "";
	         }
	     } 
	         
	     
		     
		     private String DownloadText2(String URL)
		     {
		         InputStream in = null;
		         try {
		             in = OpenHttpConnection(URL);
		             if(in!=null)
		            	 //Log.e("提交结果","有返回" );
		            	 return DomXmlPraser.readXML2(in);
		             else return "";
		         } catch (IOException e) {
		         	Log.d("NetworkingActivity", e.getLocalizedMessage());
		             return "";
		         }
		          
		     }    
		     
		 	private class DownloadTextTask extends AsyncTask<String, Void, String> {
		 		protected String doInBackground(String... urls) {
		 			return DownloadText(urls[0]);
		 		}

		 		@Override
		 		protected void onPostExecute(String result) {
		 			
		 			
		 				progressDialog1.dismiss();
		 				new AlertDialog.Builder(activity).setTitle(
								"努力充值中").setPositiveButton("确定",
								new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int whichButton)
									{
										
									}
								}).setMessage("请求已提交，请耐心等待网络确认，约十分钟后将为您充值！").show();
		 				//activity.finish();
		 			
		 		}

		 	}
			
			
			private class DownloadTextTask2 extends AsyncTask<String, Void, String> 
			{
		 		protected String doInBackground(String... urls) {
		 			return DownloadText2(urls[0]);
		 		}

		 		@Override
		 		protected void onPostExecute(String result) 
				{
		 			
		 			Log.e("yanzheng", result);
		 			if(result!="")
		 			{
		 				String[] strr=result.split(",");
		 				if(strr[0].equals("no"))
		 				{
		 					progressDialog1.dismiss();
		 					new AlertDialog.Builder(activity).setTitle("通知")  
				   		    .setMessage("很抱歉，暂停充值服务！")  
				   		    .setPositiveButton("确定", null)   
				   		    .show();  
							//chongzhi_xinxi.setText("服务器维护中，不能充值，请稍后重试！");
							//activity.longToast("服务器维护中，不能充值，请稍后重试！");
		 				}
						else if("yes".equals(strr[0])&&!strr[1].equals("")&&!strr[2].equals(""))
						{
							rate=BigDecimal.valueOf(Double.valueOf(strr[1])).multiply(BigDecimal.valueOf(100000000)).toBigInteger();
							Log.e("rate", rate.toString());
							try {
								validatedAddress = new AddressAndLabel(Constants.NETWORK_PARAMETERS, strr[2], "zhao");
							} catch (WrongNetworkException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (AddressFormatException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							String hua=spinner.getSelectedItem().toString().trim();
							double dd=0.0;
							try{
								Log.e("hua", hua);
								dd=Double.parseDouble(hua);
								Log.e("dd", dd+"");
							}catch(NumberFormatException e){
								e.printStackTrace();
							}
							 
							 BigInteger Bigmianzhi=Utils.toNanoCoins(String.valueOf(dd));
								   
							 //localValue = WalletUtils.localValue(balance, exchangeRate.rate);//比特币余额
							 //btcValue=WalletUtils.btcValue(Bigmianzhi, exchangeRate.rate);//实际比特币×100000000
							 btcValue=WalletUtils.btcValue(Bigmianzhi, rate);//实际比特币×100000000  使用服务器端的汇率
							 double btcv=new BigDecimal(btcValue).divide(new BigDecimal(100000000)).doubleValue();
							//Log.e("rate", exchangeRate.rate.toString());
							 Log.e("btcvalue", btcValue.toString());
							 //Log.e("localvalue", localValue.toString());
							 Log.e("Bigmianzhi", Bigmianzhi.toString());
							 String haoma =edithaoma.getText().toString();
							 Log.e("号码", haoma);
							 
							 
							if (haoma.length()==11&&everythingValid())
							{
								progressDialog1.dismiss();
								//生成订单信息
								dindaninfo=inint2();
								new AlertDialog.Builder(activity).setTitle(
										"是否充值").setPositiveButton("确定",
										new DialogInterface.OnClickListener()
										{
											public void onClick(DialogInterface dialog, int whichButton)
											{
												
												if (zhao.util.Utils.isFastDoubleClick()) {  
											        return;  
											     }
												//chaXunTiJiao();
												
												handleGo();
											}
										}).setNegativeButton("取消",
										new DialogInterface.OnClickListener()
										{
											public void onClick(DialogInterface dialog, int whichButton)
											{

												
											}
										}).setMessage("当前汇率是"+strr[1]+"！你选择"+hua+"元话费， 需要发送"+btcv+" Btc").show();
							 
							
									
							}
							else if (haoma.length()!=11)
							{
								progressDialog1.dismiss();
							   new AlertDialog.Builder(activity).setTitle("号码不正确")  
							   		    .setMessage("请检查号码")  
							   		    .setPositiveButton("确定", null)   
							   		    .show();  
							}
								
							else  
							{
							   			//activity.longToast("BTC余额不足！");
								progressDialog1.dismiss();
							   			if(hua.equals("10")){
							   				new AlertDialog.Builder(activity).setTitle("余额不足")  
								   		    .setMessage("您的比特币太少了")  
								   		    .setPositiveButton("确定", null)   
								   		    .show();  
							   			}
							   			else{
							   				new AlertDialog.Builder(activity).setTitle("比特币余额不足")  
							   				.setMessage("请选择更小的面值")  
							   				.setPositiveButton("确定", null)   
							   				.show();  
							   			}
							}
						
						    
							//handleGo();
						}
						else {
							progressDialog1.dismiss();
		 					new AlertDialog.Builder(activity).setTitle("通知")  
				   		    .setMessage("网络连接出现了问题，请稍后重试！")  
				   		    .setPositiveButton("确定", null)   
				   		    .show();  
						}
		 			}
		 			else 
		 			{
		 				progressDialog1.dismiss();
	 					new AlertDialog.Builder(activity).setTitle("通知")  
			   		    .setMessage("网络连接出现了问题，请稍后重试！")  
			   		    .setPositiveButton("确定", null)   
			   		    .show();  
		 				//chongzhi_xinxi.setText("网络连接出现了问题，请稍后重试！");
		 			}
		 			//activity.finish();
				}
			}

			 private void Save() {

			        String text = edithaoma.getText().toString();
			        SharedPreferences sp = activity.getSharedPreferences("history_strs", 0);
			        String save_Str = sp.getString("history", "");
			        String[] hisArrays = save_Str.split(",");
			        for(int i=0;i<hisArrays.length;i++)
			        {
			            if(hisArrays[i].equals(text))
			            {
			                return;
			            }
			        }
			        StringBuilder sb = new StringBuilder(save_Str);
			        sb.append(text + ",");
			        sp.edit().putString("history", sb.toString()).commit();
			        
			    }

}
