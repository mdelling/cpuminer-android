package com.mdelling.cpuminer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ServerPreferences extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Context context = this.getActivity().getApplicationContext();
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.server_preferences);
        loadPreferences(prefs);
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
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
		ListPreference protocol = (ListPreference)findPreference("pref_protocol");
		protocol.setSummary(prefs.getString("pref_protocol", ""));
		EditTextPreference server = (EditTextPreference)findPreference("pref_server");
		server.setSummary(prefs.getString("pref_server", ""));
		EditTextPreference port = (EditTextPreference)findPreference("pref_port");
		port.setSummary(prefs.getString("pref_port", ""));
		EditTextPreference username = (EditTextPreference)findPreference("pref_username");
		username.setSummary(prefs.getString("pref_username", ""));
		EditTextPreference password = (EditTextPreference)findPreference("pref_password");
		password.setSummary(prefs.getString("pref_password", ""));
		ListPreference algorithm = (ListPreference)findPreference("pref_algorithm");
		algorithm.setSummary(algorithm.getEntry());
	}
}
