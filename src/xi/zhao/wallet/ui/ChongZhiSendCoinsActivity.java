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

import java.math.BigInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import xi.zhao.wallet.R;

public class ChongZhiSendCoinsActivity extends AbstractBindServiceActivity{
	public static final String INTENT_EXTRA_ADDRESS = "address";

	public static final String INTENT_EXTRA_AMOUNT = "amount";
	

	public static void start(final Context context, @Nonnull final String address, 
			@Nullable final BigInteger amount)
	{
		final Intent intent = new Intent(context, ChongZhiSendCoinsActivity.class);
		intent.putExtra(INTENT_EXTRA_ADDRESS, address);
		
		intent.putExtra(INTENT_EXTRA_AMOUNT, amount);
		
		((ChongZhiActivity) context).startActivityForResult(intent,200);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.czsend_coins_content);

		getWalletApplication().startBlockchainService(false);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu)
	{
		getSupportMenuInflater().inflate(R.menu.send_coins_activity_options, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				finish();
				return true;

			case R.id.send_coins_options_help:
				HelpDialogFragment.page(getSupportFragmentManager(), "help_send_coins");
				return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
