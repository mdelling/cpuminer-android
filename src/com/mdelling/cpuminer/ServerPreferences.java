package com.mdelling.cpuminer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ServerPreferences extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	final Context context = this.getActivity().getApplicationContext();
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.server_preferences);
        loadPreferences(prefs);

        // Validate that the port is a valid number
        EditTextPreference port = (EditTextPreference)findPreference(getString(R.string.pref_port_key));
        port.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
        	@Override
        	public boolean onPreferenceChange(Preference preference, Object newValue) {
        		Integer port = Integer.parseInt(((String)newValue).trim());
        		if (port >= 1 && port <= 65535)
        			return true;

        		Toast.makeText(context, "Port must be between 1 and 65535", Toast.LENGTH_LONG).show();
        		return false;
        	}
        });
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		final Context context = this.getActivity();
		if (context != null) {
			final Intent i = new Intent("com.mdelling.cpuminer.preferenceChanged");
			i.putExtra("com.mdelling.cpuminer.preferenceKey", key);
			LocalBroadcastManager.getInstance(context).sendBroadcast(i);
		}

		loadPreferences(sharedPreferences);
	}

	@Override
	public void onResume() {
	    super.onResume();
	    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	public void onPause() {
	    getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	    super.onPause();
	}

	public void loadPreferences(SharedPreferences prefs) {
		ListPreference protocol = (ListPreference)findPreference(getString(R.string.pref_protocol_key));
		protocol.setSummary(prefs.getString(getString(R.string.pref_protocol_key), ""));
		EditTextPreference server = (EditTextPreference)findPreference(getString(R.string.pref_server_key));
		server.setSummary(prefs.getString(getString(R.string.pref_server_key), ""));
		EditTextPreference port = (EditTextPreference)findPreference(getString(R.string.pref_port_key));
		port.setSummary(prefs.getString(getString(R.string.pref_port_key), ""));
		EditTextPreference username = (EditTextPreference)findPreference(getString(R.string.pref_username_key));
		username.setSummary(prefs.getString(getString(R.string.pref_username_key), ""));
		EditTextPreference password = (EditTextPreference)findPreference(getString(R.string.pref_password_key));
		password.setSummary(prefs.getString(getString(R.string.pref_password_key), ""));
		ListPreference algorithm = (ListPreference)findPreference(getString(R.string.pref_algorithm_key));
		algorithm.setSummary(algorithm.getEntry());
	}
}
