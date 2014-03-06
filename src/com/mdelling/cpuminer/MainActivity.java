package com.mdelling.cpuminer;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private native int startMiner(int number, String parameters);
	private native void stopMiner();
	private native long getAccepted();
	private native long getRejected();
	private native int getThreads();
	private native int getHashRate(int cpu);
	private Button startButton;
	private Button stopButton;
	private Button clearButton;
	private TextView logView;
    private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Update the activity reference
		CPUMinerApplication app = (CPUMinerApplication)getApplicationContext();
		app.setCurrentActivity(this);

		// Initialize the log view
		this.logView = (TextView)this.findViewById(R.id.textlog);
		this.logView.setMovementMethod(new ScrollingMovementMethod());
		logView.setText(app.getTextLog());

		// Initialize the start/stop buttons
		this.startButton = (Button)this.findViewById(R.id.start_button);
		this.stopButton = (Button)this.findViewById(R.id.stop_button);
		this.updateButtons();

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

		// Create handle for logging
		mHandler = new Handler();
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

	private void updateButtons()
	{
		CPUMinerApplication app = (CPUMinerApplication)getApplicationContext();
		// The logger thread should stop immediately when we hit stop
		if (app.getLogger() == null)
			this.stopButton.setEnabled(false);
		else
			this.stopButton.setEnabled(true);

		// The worker thread however, may take a while to exit
		// We don't want to allow the user to start until it is finished
		if (app.getWorker() == null)
		    this.startButton.setEnabled(true);
		else
		    this.startButton.setEnabled(false);
	}

	private void startMining() {
		CPUMinerApplication app = (CPUMinerApplication)getApplicationContext();

		// Create and start the worker thread
	    Thread worker = new Thread(new Runnable() {
	        @SuppressLint("Wakelock")
			@Override
			public void run() {
	        	// Don't stop mining when the screen sleeps
	        	Context context = getApplicationContext();
	    		PowerManager mgr = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
	    		WakeLock wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
	    		wakeLock.acquire();

	    		// Get the current settings
	        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	    		String algorithm = prefs.getString("pref_algorithm", "");
	    		String protocol = prefs.getString("pref_protocol", "");
	    		String server = prefs.getString("pref_server", "");
	    		String port = prefs.getString("pref_port", "");
	    		String username = prefs.getString("pref_username", "");
	    		String password = prefs.getString("pref_password", "");

	    		// Start mining
	    		log("Starting miner");
	    		int retval = startMiner(7, "minerd -a " + algorithm + " -o " + protocol + server + ":" + port + " -O " + username + ":" + password);
	    		if (retval != 0)
	    			log("Configuration error");

	    		// Release wakelock
	    		log("Stopped miner");
	    		wakeLock.release();
	    		updateButtons();
	        }
	    });
	    app.startWorker(worker);

	    // Create and start the log update thread
	    Thread logger = new Thread(new Runnable() {
	    	@Override
	    	public void run() {
	    		Format df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
	    		while (true) {
	    			try {
	    				// Sleep for a bit
	    				Thread.sleep(60000);

	    				// Get the date
	    				Date today = Calendar.getInstance().getTime();
	    				String reportDate = df.format(today);

	    				// Get the hash rate
	    				double hashRate = 0;
	    				for (int i = 0; i < getThreads(); i++)
	    					hashRate += ((double)getHashRate(i)) / 1000;
    					String hashRateString = String.format(Locale.getDefault(), "%.2f khash/s", hashRate);

	    				// Get the block statistics
	    				long accepted = getAccepted();
	    				long total = accepted + getRejected();
	    				log(reportDate + ": " + getThreads() + " threads - " + hashRateString + " (" + accepted + "/" + total + " Blocks accepted)");
	    			} catch (InterruptedException exp) {
	    				break;
	    			}
	    		}
	    	}
	    });
	    app.startLogger(logger);

	    // Update the buttons
	    this.updateButtons();
	}

	private void stopMining() {
		CPUMinerApplication app = (CPUMinerApplication)getApplicationContext();
		log("Stopping miner");

		// Stop the worker threads and wait asynchronously - this may take a bit
		stopMiner();
		new Thread(new Runnable() {
		    @Override
			public void run() {
				CPUMinerApplication app = (CPUMinerApplication)getApplicationContext();
		    	app.stopWorker();
		    	mHandler.post(new Runnable() {
		    		@Override
					public void run() {
		    			updateButtons();
		    		}
		    	});
		    }
		}).start();

		// Stop the logger thread and wait synchronously - this should be short
		app.getLogger().interrupt();
		app.stopLogger();
	    this.updateButtons();
	}

	private void log(String message)
	{
		final String m = message;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
            	// This gets executed on the UI thread so it can safely modify Views
            	CPUMinerApplication app = (CPUMinerApplication)getApplicationContext();
            	app.appendToTextLog(m);
            	if (logView.getLineCount() < 2)
            		logView.setText(app.getTextLog());
            	else
            		logView.append(m + "\n");

            	// Scroll to the bottom of the textview
                final Layout layout = logView.getLayout();
                if(layout != null){
                    int scrollDelta = layout.getLineBottom(logView.getLineCount() - 1)
                        - logView.getScrollY() - logView.getHeight();
                    if(scrollDelta > 0)
                        logView.scrollBy(0, scrollDelta);
                }
            }
        });
	}
}
