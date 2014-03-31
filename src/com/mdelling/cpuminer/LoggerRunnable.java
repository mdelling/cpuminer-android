package com.mdelling.cpuminer;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class LoggerRunnable implements Runnable {

	private CPUMinerApplication application;

	public LoggerRunnable(CPUMinerApplication application) {
		this.application = application;
	}

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
				for (int i = 0; i < application.getThreads(); i++)
					hashRate += application.getHashRate(i) / 1000;
				String hashRateString = String.format(Locale.getDefault(), "%.2f khash/s", hashRate);

				// Get the block statistics
				long accepted = application.getAccepted();
				long total = accepted + application.getRejected();
				log(reportDate + ": " + application.getThreads() + " threads - " + hashRateString + " (" + accepted + "/" + total + " Blocks accepted)");
			} catch (InterruptedException exp) {
				break;
			}
		}
	}

	private void log(String message)
	{
        Intent i = new Intent("com.mdelling.cpuminer.logMessage");
        i.putExtra("logMessage", message);
        LocalBroadcastManager.getInstance(application).sendBroadcast(i);
    }
}
