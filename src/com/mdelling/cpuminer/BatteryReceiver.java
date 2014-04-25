package com.mdelling.cpuminer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BatteryReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		((CPUMinerApplication) arg0).handleBatteryEvent(arg1);
	}
}
