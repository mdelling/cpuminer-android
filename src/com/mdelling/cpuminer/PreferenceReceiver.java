package com.mdelling.cpuminer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PreferenceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		String key = arg1.getStringExtra("com.mdelling.cpuminer.preferenceKey");

		if (key.equals(arg0.getResources().getString(R.string.pref_battery)))
			((CPUMinerApplication) arg0).handleBatteryEvent();
	}

}
