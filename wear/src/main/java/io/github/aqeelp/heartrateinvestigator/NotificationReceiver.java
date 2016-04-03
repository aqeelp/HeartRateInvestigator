package io.github.aqeelp.heartrateinvestigator;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by aqeelp on 4/1/16.
 */
public class NotificationReceiver extends WearableListenerService {
    private static final String TAG = "HeartRateInvestigator";
    private static final String ACTIVITY_MESSAGE_PATH = "/heart_rate/activity";
    private static final String NOTIFICATION_MESSAGE_PATH = "/heart_rate/notification";
    private static boolean waiting;
    float preNotificationAverage, postNotificationAverage;
    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        waiting = false;

        Log.v(TAG, "NotificationReceiver created");
    }

    @Override // WearableListenerService
    public void onMessageReceived(MessageEvent messageEvent) {
        if (waiting) return;
        Log.v(TAG, "Messaged received!");
        if (messageEvent.getPath().equalsIgnoreCase(NOTIFICATION_MESSAGE_PATH)) {
            Log.v(TAG, "Message sent along notification message path");
            preNotificationAverage = BroadcastService.average();
            Log.v(TAG, "Got the first average!");
            try {
                waiting = true;
                Thread.sleep(5000);
                if (waiting) {
                    Log.v(TAG, "Getting the second average!");
                    postNotificationAverage = BroadcastService.average();
                    Log.v(TAG, "Ok gonna send now!");
                    sendNotification();
                    waiting = false;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendNotification() {
        Log.d(TAG, "Attempting to send notif message from wearable... ");
        DataMap dataMap = new DataMap();
        dataMap.putFloat("pre", preNotificationAverage);
        dataMap.putFloat("post", postNotificationAverage);
        dataMap.putString("timestamp", (new Date()).toString());
        final byte[] rawData = dataMap.toByteArray();

        if (mGoogleApiClient == null) return;

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
}
