package io.github.aqeelp.heartrateinvestigator;

import android.Manifest;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "HeartRateInvestigator";
    private static final int PERM_REQUEST_EX_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERM_REQUEST_EX_STORAGE);

        } else {

            initFile();
            initService();
            finish();

        }
    }

    private void initFile() {
        Log.d(TAG, "Making new file in " + Environment.getExternalStorageDirectory() + "/aqeelp_heartrate");
        String dir = Environment.getExternalStorageDirectory() + File.separator + "aqeelp_heartrate";
        try {
            File folder = new File(dir); //folder name
            folder.mkdir();

            File file = new File(dir, "heartRateData.json");
            file.createNewFile();

            Log.d(TAG, "Created.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initService() {
        Log.d(TAG, "Starting up Heart Rate receiver service...");

        Intent notificationServiceStarter = new Intent(this, HeartRateReceiver.class);
        startService(notificationServiceStarter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERM_REQUEST_EX_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    initFile();
                    initService();
                    finish();

                }
            }
            return;
        }
    }
}
