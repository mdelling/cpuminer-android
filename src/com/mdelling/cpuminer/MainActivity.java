package com.mdelling.cpuminer;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

	private CPUMinerApplication application;
	private Button startButton;
	private Button stopButton;
	private Button clearButton;
	private TextView logView;
	private StateReceiver stateReceiver;
	private LogReceiver logReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Get a reference to the application
		application = (CPUMinerApplication)getApplication();

		// Initialize the log view
		this.logView = (TextView)this.findViewById(R.id.textlog);
		this.logView.setMovementMethod(new ScrollingMovementMethod());
		logView.setText(application.getTextLog());

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
				CPUMinerApplication app = (CPUMinerApplication)getApplication();
				app.clearTextLog();
				logView.setText("");
				logView.scrollTo(0, 0);
		    }
		  });

		// Register for logging messages
		IntentFilter logIntentFilter = new IntentFilter("com.mdelling.cpuminer.logMessage");
		this.logReceiver = new LogReceiver(this);
		LocalBroadcastManager.getInstance(this).registerReceiver(logReceiver, logIntentFilter);

		// Register for logging messages
		IntentFilter stateIntentFilter = new IntentFilter("com.mdelling.cpuminer.stateMessage");
		this.stateReceiver = new StateReceiver(this);
		LocalBroadcastManager.getInstance(this).registerReceiver(stateReceiver, stateIntentFilter);

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

	protected void updateButtons()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String server = prefs.getString(getString(R.string.pref_server_key), "");
		String port = prefs.getString(getString(R.string.pref_port_key), "");
		String username = prefs.getString(getString(R.string.pref_username_key), "");
		String password = prefs.getString(getString(R.string.pref_password_key), "");

		// Stop button should only be available if we're running
		if (application.isRunning() && !application.isStopping())
			this.stopButton.setEnabled(true);
		else
			this.stopButton.setEnabled(false);

		// Don't attempt to start if we're missing configurations
		// The worker thread however, may take a while to exit
		// We don't want to allow the user to restart until it is finished
		if (application.isRunning() || application.isStopping())
			this.startButton.setEnabled(false);
		else if (server.length() > 0 && port.length() > 0 && username.length() > 0 && password.length() > 0)
			this.startButton.setEnabled(true);
		else {
			if (this.startButton.isEnabled())
				log("Configuration required");
		    this.startButton.setEnabled(false);
		}
	}

	// Start mining
	private void startMining() {
		if (!application.shouldRunOnBattery()) {
			log("Currently running on battery");
			return;
		}

		application.start();
		this.updateButtons();
		log("Started miner");
	}

	// Stop mining
	private void stopMining() {
		application.stop();
	}

	protected void log(String message)
	{
		// This gets executed on the UI thread so it can safely modify Views
		application.appendToTextLog(message);
		if (logView.getLineCount() < 2)
			logView.setText(application.getTextLog());
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
	}

	@Override
	protected void onDestroy() {
		// Unregister receivers since the activity is about to be closed.
		LocalBroadcastManager.getInstance(this).unregisterReceiver(logReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(stateReceiver);
		super.onDestroy();
	}
}
