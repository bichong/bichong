/*
 * Copyright 2011-2013 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package xi.zhao.wallet.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.bitcoin.core.VersionMessage;

import xi.zhao.wallet.Constants;
import xi.zhao.wallet.R;
import xi.zhao.wallet.WalletApplication;

/**
 * @author Andreas Schildbach
 */
public final class AboutActivity extends SherlockPreferenceActivity
{
	private static final String KEY_ABOUT_VERSION = "about_version";
	private static final String KEY_ABOUT_LICENSE = "about_license";
	private static final String KEY_ABOUT_BITCOIN_WALLET = "about_bitcoin_wallet";

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.about);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		findPreference(KEY_ABOUT_VERSION).setSummary(((WalletApplication) getApplication()).packageInfo().versionName);
		findPreference(KEY_ABOUT_LICENSE).setSummary(Constants.LICENSE_URL);
		findPreference(KEY_ABOUT_BITCOIN_WALLET).setSummary(Constants.BITCOIN_WALLET);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				finish();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen, final Preference preference)
	{
		final String key = preference.getKey();
		if (KEY_ABOUT_LICENSE.equals(key))
		{
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.LICENSE_URL)));
			finish();
		}
		else if (KEY_ABOUT_BITCOIN_WALLET.equals(key))
		{
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.BITCOIN_WALLET)));
			finish();
		}
		
		

		return false;
	}
}
