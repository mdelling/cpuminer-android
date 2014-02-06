package com.mdelling.cpuminer;

import java.util.List;

import android.annotation.TargetApi;
import android.os.Build;
import android.preference.PreferenceActivity;

public class CPUMinerPreferences extends PreferenceActivity {
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences, target);
    }
}
