package com.mdelling.cpuminer;

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
		this.activity.log(arg1.getStringExtra("logMessage"));
	}

}
