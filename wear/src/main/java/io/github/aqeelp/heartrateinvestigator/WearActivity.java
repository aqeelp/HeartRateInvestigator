package io.github.aqeelp.heartrateinvestigator;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.Calendar;

public class WearActivity extends Activity {
    private static final String TAG = "HeartRateInvestigator";
    private static final int PERM_REQUEST_BODY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BODY_SENSORS},
                    PERM_REQUEST_BODY);
        } else {
            initService();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERM_REQUEST_BODY: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initService();
                    finish();
                }
            }
            return;
        }
    }

    private void initService() {
        Intent mServiceIntent = new Intent(this, BroadcastService.class);
        this.startService(mServiceIntent);
    }
}
