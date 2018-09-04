package com.segment.analytics.cordova;

import android.content.Context;
import android.util.Log;

import com.segment.analytics.Analytics;

import org.apache.cordova.CordovaPreferences;

import java.lang.ref.WeakReference;

public class AnalyticsPluginHelper implements Runnable {

    private volatile Analytics analytics;
    private CordovaPreferences preferences;
    private static final String TAG = "AnalyticsPlugin";
    private WeakReference<Context> contextWeakReference;
    private Boolean isPluginError = false;
    private String packageName;

    public AnalyticsPluginHelper(CordovaPreferences preferences, Context context, String packageName) {
        this.preferences = preferences;
        contextWeakReference = new WeakReference<>(context);
        this.packageName = packageName;
    }

    public Analytics getAnalytics() {
        while(analytics==null && !isPluginError) {
//            Uncomment below line if Android ever gets java 9 support
//            Thread.onSpinWait();
        }
        return analytics;
    }

    @Override
    public void run() {
        String writeKeyPreferenceName;
        Analytics.LogLevel logLevel;

        if (packageName.equals("com.shipt.groceries_staging")) {
            writeKeyPreferenceName = "shipt_analytics_android_debug_write_key";
            logLevel = Analytics.LogLevel.VERBOSE;
        } else if (packageName.equals("com.shipt.groceries")) {
            writeKeyPreferenceName = "shipt_analytics_android_write_key";
            logLevel = Analytics.LogLevel.NONE;
        }else if (packageName.equals("com.shipt.meijerstaging")) {
            writeKeyPreferenceName = "meijer_analytics_android_debug_write_key";
            logLevel = Analytics.LogLevel.VERBOSE;
        } else if (packageName.equals("com.shipt.meijer")) {
            writeKeyPreferenceName = "meijer_analytics_android_write_key";
            logLevel = Analytics.LogLevel.NONE;
        } else if (packageName.equals("com.shipt.shopper_staging")) {
            writeKeyPreferenceName = "shopper_analytics_android_debug_write_key";
            logLevel = Analytics.LogLevel.VERBOSE;
        } else if (packageName.equals("com.shipt.shopper-staging")) {
            writeKeyPreferenceName = "shopper_analytics_android_debug_write_key";
            logLevel = Analytics.LogLevel.VERBOSE;
        } else if (packageName.equals("com.shipt.shopper")) {
            writeKeyPreferenceName = "shopper_analytics_android_write_key";
            logLevel = Analytics.LogLevel.NONE;
        } else {
            writeKeyPreferenceName = "";
            logLevel = Analytics.LogLevel.VERBOSE;
        }

        String writeKey = preferences.getString(writeKeyPreferenceName, null);

        if (writeKey == null || "".equals(writeKey)) {
            analytics = null;
            Log.e(TAG, "Invalid write key: " + writeKey);
            isPluginError = true;
        } else {
            //trackApplicationLifecycleEvents() //-> Enable this to record certain application events automatically! -> which then used by Tune to map install attributions https://segment.com/docs/sources/mobile/android/quickstart/#step-2-initialize-the-client

            // trackApplicationLifecycleEvents - is not getting fired due to ` segment` initializing is getting done on onActivityStarted instead on onActivityCreated. Where segment logic of `trackApplicationLifecycleEvents` is handled with in `onActivityCreated` https://github.com/segmentio/analytics-android/blob/master/analytics/src/main/java/com/segment/analytics/Analytics.java#L291

            // analytics = new Analytics.Builder(
            //     cordova.getActivity().getApplicationContext(),
            //     writeKey
            // )
            // .logLevel(logLevel)
            // .collectDeviceId(true)
            // .trackApplicationLifecycleEvents()
            // .build();

            analytics = new Analytics.Builder(
                    contextWeakReference.get(),
                    writeKey
            )
                    .logLevel(logLevel)
                    .collectDeviceId(true)
                    .trackApplicationLifecycleEvents()
                    .build();

            Analytics.setSingletonInstance(analytics);
        }
    }
}
