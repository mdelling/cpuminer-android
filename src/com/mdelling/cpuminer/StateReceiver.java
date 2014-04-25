package com.mdelling.cpuminer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StateReceiver extends BroadcastReceiver {

	private MainActivity activity;

	public StateReceiver(MainActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		String state = arg1.getStringExtra("com.mdelling.cpuminer.stateEntry");
		activity.log(state);
		activity.updateButtons();
	}

}
