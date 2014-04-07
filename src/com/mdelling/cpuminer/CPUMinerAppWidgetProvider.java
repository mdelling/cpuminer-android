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
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

public class CPUMinerAppWidgetProvider extends AppWidgetProvider {

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// Get the current hash rates
		CPUMinerApplication app = (CPUMinerApplication)context.getApplicationContext();
		String hashrate_string = "Not Running";
		if (app.hasLogger()) {
			double hashRate = 0;
			for (int i = 0; i < app.getThreads(); i++)
				hashRate += app.getHashRate(i) / 1000;
			hashrate_string = String.format(Locale.getDefault(), "%.2f kh/s", hashRate);
		}

		// Get the current accepted count
		long accepted = app.getAccepted();
		long total = accepted + app.getRejected();
		String accept_string = accepted + "/" + total + " accepted";

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
