package com.mdelling.cpuminer;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Button startButton;
	private Button stopButton;
	private Button clearButton;
	private TextView logView;
	private LogReceiver logReceiver;
	private BatteryReceiver batteryReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Get a reference to the application
		CPUMinerApplication app = (CPUMinerApplication)getApplicationContext();

		// Initialize the log view
		this.logView = (TextView)this.findViewById(R.id.textlog);
		this.logView.setMovementMethod(new ScrollingMovementMethod());
		logView.setText(app.getTextLog());

		// Initialize the start/stop buttons
		this.startButton = (Button)this.findViewById(R.id.start_button);
		this.stopButton = (Button)this.findViewById(R.id.stop_button);

		this.startButton.setOnClickListener(new OnClickListener() {
			@Override
		    public void onClick(View v) {
				startMining();
		    }
		  });
		this.stopButton.setOnClickListener(new OnClickListener() {
			@Override
		    public void onClick(View v) {
				stopMining();
		    }
		  });

		// Initialize the clear button
		this.clearButton = (Button)this.findViewById(R.id.clear_button);
		this.clearButton.setOnClickListener(new OnClickListener() {
			@Override
		    public void onClick(View v) {
				CPUMinerApplication app = (CPUMinerApplication)getApplicationContext();
				app.clearTextLog();
				logView.setText("");
				logView.scrollTo(0, 0);
		    }
		  });

		// Register for logging messages
		IntentFilter logIntentFilter = new IntentFilter("com.mdelling.cpuminer.logMessage");
		this.logReceiver = new LogReceiver(this);
		LocalBroadcastManager.getInstance(this).registerReceiver(logReceiver, logIntentFilter);

		// Register for battery status changes
		IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		this.batteryReceiver = new BatteryReceiver();
		this.registerReceiver(batteryReceiver, batteryIntentFilter);

		// This may log, so we can't do it before we create the handler
		this.updateButtons();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem preferences = menu.findItem(R.id.action_settings);
		Intent prefsIntent = new Intent(this.getApplicationContext(), CPUMinerPreferences.class);
		preferences.setIntent(prefsIntent);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.action_settings:
	        this.startActivity(item.getIntent());
	        break;
	    }
	    return true;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus)
			this.updateButtons();
	}

	private void updateButtons()
	{
		CPUMinerApplication app = (CPUMinerApplication)getApplicationContext();
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String server = prefs.getString("pref_server", "");
		String port = prefs.getString("pref_port", "");
		String username = prefs.getString("pref_username", "");
		String password = prefs.getString("pref_password", "");

		// The logger thread should stop immediately when we hit stop
		if (app.hasLogger())
			this.stopButton.setEnabled(true);
		else
			this.stopButton.setEnabled(false);

		// Don't attempt to start if we're missing configurations
		// The worker thread however, may take a while to exit
		// We don't want to allow the user to restart until it is finished
		if (app.hasWorker())
			this.startButton.setEnabled(false);
		else if (server.length() > 0 && port.length() > 0 && username.length() > 0 && password.length() > 0)
			this.startButton.setEnabled(true);
		else {
			if (this.startButton.isEnabled())
				log("Configuration required");
		    this.startButton.setEnabled(false);
		}
	}

	// Start the worker and logger threads
	private void startMining() {
		// Check whether we should start given current power settings
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = this.registerReceiver(null, ifilter);
		if (!shouldRunOnBattery(batteryStatus)) {
			log("Currently running on battery");
			return;
		}

		CPUMinerApplication app = (CPUMinerApplication)getApplicationContext();
		app.startWorker();
		app.startLogger();
		this.updateButtons();
		log("Started miner");
	}

	private void stopMining() {
		new StopWorkers().execute();
	}

	private boolean shouldRunOnBattery(Intent batteryStatus)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean useBattery = prefs.getBoolean("pref_battery", false);
		if (useBattery)
			return true;

		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		return status == BatteryManager.BATTERY_STATUS_CHARGING ||
			   status == BatteryManager.BATTERY_STATUS_FULL;
	}

	protected void log(String message)
	{
		// This gets executed on the UI thread so it can safely modify Views
		CPUMinerApplication app = (CPUMinerApplication)getApplicationContext();
		app.appendToTextLog(message);
		if (logView.getLineCount() < 2)
			logView.setText(app.getTextLog());
		else
			logView.append(message + "\n");

		// Scroll to the bottom of the textview
		final Layout layout = logView.getLayout();
		if (layout != null) {
			int scrollDelta = layout.getLineBottom(logView.getLineCount() - 1)
					- logView.getScrollY() - logView.getHeight();
			if(scrollDelta > 0)
				logView.scrollBy(0, scrollDelta);
		}

		// Tell the widget to update
		updateWidget();
	}

	protected void handleBatteryEvent(Intent batteryStatus)
	{
		CPUMinerApplication app = (CPUMinerApplication)getApplicationContext();

		// Stop mining if we just switched to battery and aren't supposed to use it
		if (!shouldRunOnBattery(batteryStatus) && app.hasLogger()) {
			this.stopMining();
		}
	}

	private void updateWidget()
	{
		Intent intent = new Intent(this, CPUMinerAppWidgetProvider.class);
		intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
		int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), CPUMinerAppWidgetProvider.class));
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
		sendBroadcast(intent);
	}

	@Override
	protected void onDestroy() {
		// Unregister receivers since the activity is about to be closed.
		LocalBroadcastManager.getInstance(this).unregisterReceiver(logReceiver);
		this.unregisterReceiver(batteryReceiver);
		super.onDestroy();
	}

	// Stop miner and logger asynchronously, updating the buttons after completion
	private class StopWorkers extends AsyncTask<Void, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			CPUMinerApplication app = (CPUMinerApplication)getApplicationContext();
			if (!app.hasLogger())
				return false;

			app.stopLogger();
			publishProgress(1);
			app.stopWorker();
			publishProgress(2);
			return true;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			log("Stopped worker " + values[0] + "/2");
			updateButtons();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result)
				log("Stopped miner");
		}
	}
}
