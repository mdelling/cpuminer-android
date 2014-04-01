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
		String algorithm = prefs.getString("pref_algorithm", "");
		String protocol = prefs.getString("pref_protocol", "");
		String server = prefs.getString("pref_server", "");
		String port = prefs.getString("pref_port", "");
		String username = prefs.getString("pref_username", "");
		String password = prefs.getString("pref_password", "");

		// Start mining
		int retval = application.startMiner(7, "minerd -a " + algorithm + " -o " + protocol + server + ":" + port + " -O " + username + ":" + password);
		if (retval != 0)
			Log.e("CPUMinerRunnable", "Failed to start CPUMiner");

		// Release wakelock
		wakeLock.release();
	}
}
