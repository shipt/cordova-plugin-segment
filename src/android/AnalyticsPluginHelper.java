package com.segment.analytics.cordova;

import android.content.Context;
import android.util.Log;

import com.segment.analytics.Analytics;

import org.apache.cordova.CordovaPreferences;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

public class AnalyticsPluginHelper implements Runnable {

    private volatile Analytics analytics;
    private CordovaPreferences preferences;
    private static final String TAG = "AnalyticsPlugin";
    private WeakReference<Context> contextWeakReference;
    private String packageName;
    private String segmentKey;
    private CountDownLatch latch = new CountDownLatch(1);

    public AnalyticsPluginHelper(CordovaPreferences preferences, Context context, String packageName) {
        this.preferences = preferences;
        contextWeakReference = new WeakReference<>(context);
        this.packageName = packageName;
        this.segmentKey = context.getString(context.getResources().getIdentifier( "segmentAnalyticsKey", "string", packageName));
    }

    public Analytics getAnalytics() {
        if(analytics==null) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return analytics;
    }

    @Override
    public void run() {
        String writeKeyPreferenceName;
        Analytics.LogLevel logLevel;
        logLevel = Analytics.LogLevel.VERBOSE;
        String writeKey = segmentKey;

        if (writeKey == null || "".equals(writeKey)) {
            analytics = null;
            Log.e(TAG, "Invalid write key: " + writeKey);
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
        latch.countDown();
    }
}
