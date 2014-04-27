package com.mdelling.cpuminer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;

public class CPUMinerRunnable implements Runnable {

	private CPUMinerApplication application;

	public CPUMinerRunnable(CPUMinerApplication application) {
		this.application = application;
	}

	@SuppressLint("Wakelock")
	@Override
	public void run() {
		// Don't stop mining when the screen sleeps
		PowerManager mgr = (PowerManager)application.getSystemService(Context.POWER_SERVICE);
		WakeLock wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
		wakeLock.acquire();

		// Get the current settings
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(application);
		String algorithm_key = application.getResources().getString(R.string.pref_algorithm_key);
		String algorithm = prefs.getString(algorithm_key, "");
		String protocol_key = application.getResources().getString(R.string.pref_protocol_key);
		String protocol = prefs.getString(protocol_key, "");
		String server_key = application.getResources().getString(R.string.pref_server_key);
		String server = prefs.getString(server_key, "");
		String port_key = application.getResources().getString(R.string.pref_port_key);
		String port = prefs.getString(port_key, "");
		String username_key = application.getResources().getString(R.string.pref_username_key);
		String username = prefs.getString(username_key, "");
		String password_key = application.getResources().getString(R.string.pref_password_key);
		String password = prefs.getString(password_key, "");

		// Start mining
		int retval = application.startMiner(7, "minerd -a " + algorithm + " -o " + protocol + server + ":" + port + " -O " + username + ":" + password);
		if (retval != 0)
			Log.e("CPUMinerRunnable", "Failed to start CPUMiner");

		// Release wakelock
		wakeLock.release();
	}
}
