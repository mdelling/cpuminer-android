package com.mdelling.cpuminer;

import java.util.Locale;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

public class CPUMinerAppWidgetProvider extends AppWidgetProvider {

	private LogEntry lastEntry = null;

	@Override
	public void onEnabled(Context context) {
	    super.onEnabled(context);
	    setWidgetActive(context, true);
	}

	@Override
	public void onDisabled(Context context) {
	    setWidgetActive(context, false);
	    super.onDisabled(context);
	}

	private void setWidgetActive(Context context, boolean active){
	    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
	    SharedPreferences.Editor edit = prefs.edit();
	    String pref_widget = context.getResources().getString(R.string.pref_widget);
	    edit.putBoolean(pref_widget, active);
	    edit.commit();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras.containsKey("com.mdelling.cpuminer.logEntry")) {
			LogEntry entry = intent.getParcelableExtra("com.mdelling.cpuminer.logEntry");
			lastEntry = entry;
		}

		super.onReceive(context, intent);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		String hashrate_string = "Stopped";
		String accept_string = "0/0 accepted";

		// If this is a status update, grab the values
		if (lastEntry != null) {
			hashrate_string = String.format(Locale.getDefault(), "%.2f kh/s", lastEntry.getHashRate());
			accept_string = lastEntry.getBlocksAccepted() + "/" + lastEntry.getBlocksTotal() + " accepted";
		}

		// Get the server address
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String server = prefs.getString("pref_server", "");

		// Generate the intent for switching to the app
		Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.setComponent(new ComponentName("com.mdelling.cpuminer",
            "com.mdelling.cpuminer.MainActivity"));
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 0);

	    // Get all ids
	    ComponentName thisWidget = new ComponentName(context, CPUMinerAppWidgetProvider.class);
	    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
	    for (int widgetId : allWidgetIds) {
	        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
	        remoteViews.setOnClickPendingIntent(R.id.widget_hashrate, pendingIntent);
	        remoteViews.setTextViewText(R.id.widget_server, server);
	        remoteViews.setTextViewText(R.id.widget_hashrate, hashrate_string);
	        remoteViews.setTextViewText(R.id.widget_accepted, accept_string);
	        appWidgetManager.updateAppWidget(widgetId, remoteViews);
	    }
	}
}
