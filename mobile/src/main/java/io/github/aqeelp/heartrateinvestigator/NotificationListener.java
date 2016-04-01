package io.github.aqeelp.heartrateinvestigator;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by aqeelp on 1/23/16.
 */
public class NotificationListener extends NotificationListenerService implements
        GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = "HeartRateInvestigator";
    private static final String NOTIFICATION_MESSAGE_PATH = "/heart_rate/notification";
    private GoogleApiClient mGoogleApiClient;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Notification Listener service started - making STICKY.");

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Notification Listener service created.");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(TAG, "Notification posted. Attempting to send message... ");

        long[] vibrate = sbn.getNotification().vibrate;
        int defaults = sbn.getNotification().defaults;
        Uri sound = sbn.getNotification().sound;

        if (vibrate != null || defaults == Notification.DEFAULT_VIBRATE || defaults == Notification.DEFAULT_SOUND
                || defaults == Notification.DEFAULT_ALL || sound != null) {
            // This is a really terrible way to do this, but I don't want to sort the logic out backwards.
        } else {
            Log.d(TAG, "No vibrate. Doing nothing.");
            return;
        }

        DataMap dataMap = new DataMap();
        dataMap.putString("timestamp", (new Date()).toString());
        final byte[] rawData = dataMap.toByteArray();

        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mGoogleApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), NOTIFICATION_MESSAGE_PATH, rawData).await();
                    if (result.getStatus().isSuccess())
                        Log.d(TAG, "Message sent successfully");
                    else
                        Log.d(TAG, "Message failed");
                }
            }
        }).start();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnected: " + bundle);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnectionSuspended: " + i);
        }
    }
}
