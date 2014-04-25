package com.mdelling.cpuminer;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
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

	public boolean hasWorker()
	{
		return worker != null;
	}

	public void startWorker()
	{
		if (worker == null) {
			worker = new Thread(new CPUMinerRunnable(this));
			worker.start();
		}
	}

	public void stopWorker()
	{
		if (worker == null)
			return;

		try {
			stopMiner();
			worker.join();
			worker = null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean hasLogger()
	{
		return logger != null;
	}

	public void startLogger()
	{
		if (logger == null) {
			logger = new Thread(new LoggerRunnable(this));
			logger.start();
		}
	}

	public void stopLogger()
	{
		if (logger == null)
			return;

		try {
			logger.interrupt();
			logger.join();
			logger = null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void updateApplication() {
		updateApplication(null);
	}

	protected void updateApplication(LogEntry entry)
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
	    String pref_widget = this.getResources().getString(R.string.pref_widget);
	    return prefs.getBoolean(pref_widget, false);
	}
}
