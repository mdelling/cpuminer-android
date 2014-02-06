package com.mdelling.cpuminer;

import android.app.Activity;
import android.app.Application;

public class CPUMinerApplication extends Application {

	private native int detectCPUHasNeon();
	private String textLog = "";
	private Thread worker = null;
	private Thread logger = null;
	private Activity currentActivity = null;

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

	public Thread getWorker()
	{
		return worker;
	}

	public void startWorker(Thread t)
	{
		worker = t;
		worker.start();
	}

	public void stopWorker()
	{
		if (worker == null)
			return;

		try {
			worker.join();
			worker = null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Thread getLogger()
	{
		return logger;
	}

	public void startLogger(Thread t)
	{
		logger = t;
		logger.start();
	}

	public void stopLogger()
	{
		if (logger == null)
			return;

		try {
			logger.join();
			logger = null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Activity getCurrentActivity()
	{
		return currentActivity;
	}

	public void setCurrentActivity(Activity activity)
	{
		currentActivity = activity;
	}
}
