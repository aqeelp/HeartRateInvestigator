package io.github.aqeelp.heartrateinvestigator;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.PowerManager;
import android.util.JsonWriter;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by aqeelp on 1/29/16.
 */
public class HeartRateReceiver extends WearableListenerService {
    private static final String TAG = "HeartRateInvestigator";
    private static final String ACTIVITY_MESSAGE_PATH = "/heart_rate/activity";
    private static final String NOTIFICATION_MESSAGE_PATH = "/heart_rate/notification";
    private static final String ACTIVITY_FILE_PATH = Environment.getExternalStorageDirectory()
            + File.separator + "aqeelp_heartrate" + File.separator + "activityData.json";
    private static final String NOTIFICATION_FILE_PATH = Environment.getExternalStorageDirectory()
            + File.separator + "aqeelp_heartrate" + File.separator + "notificationData.json";
    private static int records;

    @Override
    public void onCreate() {
        super.onCreate();
        records = 0;

        Log.d(TAG, "Mobile-side data receiver created.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Data receiver service started - making STICKY.");

        return START_STICKY;
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        byte[] rawData = messageEvent.getData();
        DataMap dataMap = DataMap.fromByteArray(rawData);

        if (messageEvent.getPath().equalsIgnoreCase(ACTIVITY_MESSAGE_PATH)) {
            Log.d(TAG, "Message with Activity data received!");
            int heartRate = dataMap.getInt("heartRate");
            String time = dataMap.getString("time");
            String activity = getCurrentPackage();
            // TODO: What about a background process like listening to music?

            try {
                if (activity != null && heartRate != 0) {
                    Log.d(TAG, "Writing DataMap: " + dataMap.toString());
                    writeActivityData(heartRate, time, activity);
                } else {
                    Log.d(TAG, "Invalid process or heart rate. DataMap: " + dataMap.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (messageEvent.getPath().equalsIgnoreCase(NOTIFICATION_MESSAGE_PATH)) {
            Log.d(TAG, "Notif data received: " + dataMap.toString());
        }
    }

    private String getCurrentPackage() {
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        if (!pm.isInteractive()) {
            return null;
        }

        UsageStats currentApp = null;
        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
        if (appList != null && appList.size() > 0) {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
            for (UsageStats usageStats : appList) {
                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (mySortedMap != null && !mySortedMap.isEmpty()) {
                currentApp = mySortedMap.get(mySortedMap.lastKey());
            }
        }

        if (currentApp != null) {
            Log.v(TAG, "Current app: " + currentApp.getPackageName()
                    + ", last used at " + currentApp.getLastTimeUsed());
            return currentApp.getPackageName();
        } else {
            return null;
        }
    }

    private void writeActivityData(int heartRate, String time, String packageName) throws IOException {
        File file = new File(ACTIVITY_FILE_PATH);
        BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));

        if (records > 0) {
            bw.write(',');
            bw.write('\n');
        }

        JsonWriter writer = new JsonWriter(bw);
        writer.setIndent("  ");

        writer.beginObject();
        writer.name("heart_rate");
        writer.value(heartRate);
        writer.name("time");
        writer.value(time);
        writer.name("package_name");
        writer.value(packageName);
        writer.endObject();
        writer.close();

        bw.flush();
        bw.close();

        records++;
    }
}
