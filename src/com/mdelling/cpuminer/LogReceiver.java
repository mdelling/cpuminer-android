package com.mdelling.cpuminer;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LogReceiver extends BroadcastReceiver {

	private MainActivity activity;

	public LogReceiver(MainActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		LogEntry entry = arg1.getParcelableExtra("com.mdelling.cpuminer.logEntry");
		Format df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
		String timestamp = df.format(entry.getTimestamp());
		String hashRateString = String.format(Locale.getDefault(), "%.2f khash/s", entry.getHashRate());
		String blockString = String.format(Locale.getDefault(), "(%d/%d  Blocks accepted)", entry.getBlocksAccepted(), entry.getBlocksTotal());
		activity.log(timestamp + ": " + entry.getThreads() + " threads - " + hashRateString + " " + blockString);
	}

}
