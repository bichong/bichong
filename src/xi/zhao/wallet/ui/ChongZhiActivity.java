package xi.zhao.wallet.ui;

import xi.zhao.wallet.R;
import android.os.Bundle;

public class ChongZhiActivity extends AbstractBindServiceActivity{
	
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		 super.onCreate(savedInstanceState);
	       setContentView(R.layout.czsend_coins_content);
	       getWalletApplication().startBlockchainService(false);
	       
	     
		
	}
	
	
}
