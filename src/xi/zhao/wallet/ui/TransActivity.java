package xi.zhao.wallet.ui;

import xi.zhao.wallet.R;
import android.os.Bundle;

public class TransActivity extends AbstractWalletActivity{
	@Override
	protected void onCreate(final Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		setContentView(R.layout.transaction);
	}

}
