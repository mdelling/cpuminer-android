package com.mdelling.cpuminer;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

public class CPUMinerApplication extends Application {

	private native int detectCPUHasNeon();
	protected native int startMiner(int number, String parameters);
	private native void stopMiner();
	protected native long getAccepted();
	protected native long getRejected();
	protected native int getThreads();
	protected native double getHashRate(int cpu);

	private String textLog = "";
	private Thread worker = null;
	private Thread logger = null;
	private PreferenceReceiver preferenceReceiver = null;
	private BatteryReceiver batteryReceiver = null;

	static {
	    System.loadLibrary("curl");
	    System.loadLibrary("neondetect");
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
	    if (detectCPUHasNeon() == 0)
	    {
	    	System.loadLibrary("cpuminer");
	    	appendToTextLog("Loading CPUMiner without NEON support");
	    }
	    else
	    {
	    	System.loadLibrary("cpuminer-neon");
	    	appendToTextLog("Loading CPUMiner with NEON support");
	    }
	}

	public String getTextLog()
	{
		return textLog;
	}

	public void appendToTextLog(String value)
	{
		textLog = textLog + value + "\n";
	}

	public void clearTextLog()
	{
		textLog = "";
	}

	//=====================================================================
	// Mining state
	//=====================================================================

	protected void start()
	{
		// Register for battery status changes
		IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		this.batteryReceiver = new BatteryReceiver();
		this.registerReceiver(batteryReceiver, batteryIntentFilter);

		// Register for battery status changes
		IntentFilter preferenceIntentFilter = new IntentFilter("com.mdelling.cpuminer.preferenceChanged");
		this.preferenceReceiver = new PreferenceReceiver();
		LocalBroadcastManager.getInstance(this).registerReceiver(preferenceReceiver, preferenceIntentFilter);

		startWorker();
		startLogger();
	}

	protected void stop()
	{
		// Unregister for battery status changes
		this.unregisterReceiver(batteryReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(preferenceReceiver);

		new WaitForWorkers().execute();
		stopMiner();
		logger.interrupt();
		handleLogEntry(null);
	}

	protected boolean isRunning()
	{
		return hasWorker() || hasLogger();
	}

	protected boolean isStopping()
	{
		return !hasLogger() && hasWorker();
	}

	// Stop miner and logger asynchronously, updating the buttons after completion
	private class WaitForWorkers extends AsyncTask<Void, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			if (!isRunning() || isStopping())
				return false;

			waitForLogger();
			publishProgress(1);
			waitForWorker();
			publishProgress(2);
			return true;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			handleStateChange("Stopped worker " + values[0] + "/2");
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result)
				handleStateChange("Stopped miner");
		}
	}

	//=====================================================================
	// Worker management
	//=====================================================================

	private boolean hasWorker()
	{
		return worker != null && worker.getState() != Thread.State.TERMINATED;
	}

	private void startWorker()
	{
		if (!hasWorker()) {
			worker = new Thread(new CPUMinerRunnable(this));
			worker.start();
		}
	}

	private void waitForWorker()
	{
		if (!hasWorker())
			return;

		try {
			worker.join();
			worker = null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//=====================================================================
	// Logger management
	//=====================================================================

	private boolean hasLogger()
	{
		return logger != null && logger.getState() != Thread.State.TERMINATED;
	}

	private void startLogger()
	{
		if (!hasLogger()) {
			logger = new Thread(new LoggerRunnable(this));
			logger.start();
		}
	}

	private void waitForLogger()
	{
		if (!hasLogger())
			return;

		try {
			logger.join();
			logger = null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//=====================================================================
	// Application updates
	//=====================================================================

	private void handleStateChange(String message)
	{
        Intent i = new Intent("com.mdelling.cpuminer.stateMessage");
        i.putExtra("com.mdelling.cpuminer.stateEntry", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
	}

	protected void handleLogEntry(LogEntry entry)
	{
		// Tell the widget something has happened
		if (isWidgetActive()) {
			Intent intent = new Intent(this, CPUMinerAppWidgetProvider.class);
			intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
			int ids[] = AppWidgetManager.getInstance(this).getAppWidgetIds(new ComponentName(this, CPUMinerAppWidgetProvider.class));
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
			intent.putExtra("com.mdelling.cpuminer.logEntry", entry);
			sendBroadcast(intent);
		}

		// Update the activity
		if (entry != null) {
	        Intent i = new Intent("com.mdelling.cpuminer.logMessage");
	        i.putExtra("com.mdelling.cpuminer.logEntry", entry);
	        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
		}
	}

	private boolean isWidgetActive() {
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    String pref_widget = getString(R.string.pref_widget);
	    return prefs.getBoolean(pref_widget, false);
	}

	//=====================================================================
	// Battery management
	//=====================================================================

	private Intent batteryStatus()
	{
		// Check whether we should start given current power settings
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		return this.registerReceiver(null, ifilter);
	}

	protected boolean shouldRunOnBattery()
	{
		return shouldRunOnBattery(batteryStatus());
	}

	protected boolean shouldRunOnBattery(Intent batteryStatus)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean useBattery = prefs.getBoolean(getString(R.string.pref_battery_key), false);
		float useBatteryLevel = prefs.getFloat(getString(R.string.pref_battery_level_key), 0);
		int currentLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		float level = (float)currentLevel / scale;

		// If we can run on battery, stop at minimum battery level
		if (useBattery)
			return level >= useBatteryLevel;

		// Otherwise, only run if charging or full
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		return status == BatteryManager.BATTERY_STATUS_CHARGING ||
			   status == BatteryManager.BATTERY_STATUS_FULL;
	}

	// Stop mining if we just switched to battery and aren't supposed to use it
	protected void handleBatteryEvent()
	{
		handleBatteryEvent(batteryStatus());
	}

	protected void handleBatteryEvent(Intent batteryStatus)
	{
		if (!shouldRunOnBattery(batteryStatus) && hasLogger()) {
			stop();
		}
	}
}
