package io.github.aqeelp.heartrateinvestigator;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by aqeelp on 3/22/16.
 */
public class BroadcastService extends IntentService implements SensorEventListener {
    private static final String TAG = "HeartRateInvestigator";
    private static final String DATA_MESSAGE_PATH = "/heart_rate/data";

    private static final int SAMSUNG_HEARTRATE_TYPE = 65562;
    // private static final int MOTO_HEARTRATE_TYPE = 65538;
    private static final int MOTO_HEARTRATE_TYPE = Sensor.TYPE_HEART_RATE;

    /* !!! CHANGE THE FOLLOWING LINE FOR DIFFERENT DEVICES: */
    private static final int HEARTRATE_SENSOR = SAMSUNG_HEARTRATE_TYPE;
    /* !!! CHANGE THE FOLLOWING LINE FOR DIFFERENT SAMPLE PERIODS: */
    private static final int SECONDS = 10;

    private GoogleApiClient mGoogleApiClient;
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private ArrayList<Float> recentHeartRates;

    public BroadcastService() {
        super(null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "BroadCast service started - making STICKY.");

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        recentHeartRates = new ArrayList<>();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(HEARTRATE_SENSOR);
        mSensorManager.registerListener(this, this.mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);

        final Handler h = new Handler();
        final int delay = SECONDS * 1000; //milliseconds
        h.postDelayed(new Runnable() {
            public void run() {
                //do something
                sendRecentAverage();
                h.postDelayed(this, delay);
            }
        }, delay);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.values[0] > 0) {
            // String eventString = "sensor event: " + sensorEvent.sensor.getName()
                    // + ", accuracy: " + sensorEvent.accuracy + ", value: " + sensorEvent.values[0];
            recentHeartRates.add(sensorEvent.values[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "accuracy changed: " + accuracy);
    }

    private void sendMessage(DataMap dataMap) {
        Log.d(TAG, "Attempting to send message from wearable... " + dataMap);
        final byte[] rawData = dataMap.toByteArray();

        if (mGoogleApiClient == null) return;

        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mGoogleApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), DATA_MESSAGE_PATH, rawData).await();
                    if (result.getStatus().isSuccess())
                        Log.d(TAG, "Message sent successfully");
                    else
                        Log.d(TAG, "Message failed");
                }
            }
        }).start();
    }

    private void sendRecentAverage() {
        float sum = 0f;
        if (!recentHeartRates.isEmpty()) {
            for (float f : recentHeartRates) {
                sum += f;
            }
        }
        sum /= recentHeartRates.size();
        recentHeartRates.clear();

        DataMap dataMap = new DataMap();
        dataMap.putInt("heartRate", (int) sum);
        dataMap.putString("time", (new Date()).toString());
        sendMessage(dataMap);
    }
}
