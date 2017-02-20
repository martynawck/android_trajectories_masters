package com.wiacek.martyna.mastersresearch.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.wiacek.martyna.mastersresearch.R;
import com.wiacek.martyna.mastersresearch.activities.MainActivity;
import com.wiacek.martyna.mastersresearch.tasks.SendUserLocationTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Martyna on 18.02.2017.
 */
public class MyService extends Service {

    public static final long NOTIFY_INTERVAL = 15 * 1000; // 10 seconds
    private static final double MIN_DISTANCE = 1;

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;

    private LocalBroadcastManager broadcaster;

    GPSTracker gps;
    DBHelper mDBHelper;
    SessionManager sessionManager;

    Location LKL = null;

    static final public String NO_GPS_RESULT = "com.wiacek.martyna.mastersreaserch.NO_GPS";
    static final public String UPDATE_LATLANG = "com.wiacek.martyna.mastersreaserch.UPDATE_LATLANG";
    static final public String LATITUDE = "com.wiacek.martyna.mastersreaserch.LATITUDE";
    static final public String LONGITUDE = "com.wiacek.martyna.mastersreaserch.LONGITUDE";
    static final public String ALERT_DESCRIPTION = "com.wiacek.martyna.mastersresearch.ALERT_DESCRIPTION";

    static final public String MESSAGE_PL = "com.wiacek.martyna.mastersresearch.MESSAGE_PL";
    static final public String MESSAGE_EN = "com.wiacek.martyna.mastersresearch.MESSAGE_EN";

    String USERNAME;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        sessionManager = new SessionManager(getApplicationContext());
        USERNAME = sessionManager.getValueOfLogin();
        broadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
        mDBHelper = new DBHelper(getApplicationContext());
        gps = new GPSTracker(getApplicationContext());
        return START_STICKY;
    }

    @Override
    public void onCreate() {

        if(mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }
        // schedule task
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
    }
    @Override
    public void onDestroy()
    {
        mTimer.cancel();
        super.onDestroy();
    }

    public void buildAlertMessageNoGps() {
        Intent intent = new Intent(NO_GPS_RESULT);
        broadcaster.sendBroadcast(intent);
    }

    public void buildAlertDescription(int pl, int en) {
        Intent intent = new Intent(ALERT_DESCRIPTION);
        intent.putExtra(MESSAGE_PL, pl);
        intent.putExtra(MESSAGE_EN, en);
        broadcaster.sendBroadcast(intent);
    }

    public void updateLatitudeLongitude(String latitude, String longitude) {
        Intent intent = new Intent(UPDATE_LATLANG);
         if(latitude != null)
           intent.putExtra(LATITUDE, latitude);
        if (longitude != null)
            intent.putExtra(LONGITUDE, longitude);
        broadcaster.sendBroadcast(intent);
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
                    if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                        buildAlertMessageNoGps();
                    } else if (gps.canGetLocation()){
                        updateLatitudeLongitude(Double.toString(gps.getLatitude()), Double.toString(gps.getLongitude()));
                        if ( (LKL == null ||
                                gps.checkDistance(LKL, gps.getLocation()) >= MIN_DISTANCE))  {
                            LKL = gps.getLocation();
                            updateLatitudeLongitude(Double.toString(LKL.getLatitude()), Double.toString(LKL.getLongitude()));

                            long timeNow = System.currentTimeMillis();
                            if (!isConnectingToInternet()) {
                                Log.d("LOG","SAVING POINT IN LOCAL DB");
                                buildAlertDescription(R.string.polishLocallyStatus, R.string.englishLocallyStatus);

                                mDBHelper.insertLocation(Double.toString(LKL.getLongitude()), Double.toString(LKL.getLatitude()), Long.toString(timeNow));
                            } else {

                                if (mDBHelper.numberOfRows() > 0) {
                                    synchronizeDataset();
                                }

                                buildAlertDescription(R.string.polishOnServerStatus, R.string.englishOnServerStatus);
                                Log.d("LOG","SAVING POINT ON SERVER");
                                SendUserLocationTask task = new SendUserLocationTask(getApplicationContext(), new String[]{USERNAME, Long.toString(timeNow), Double.toString(LKL.getLatitude()),
                                        Double.toString(LKL.getLongitude())});
                                task.runVolley();
                            }
                        } else {
                            buildAlertDescription(R.string.polishNoMove, R.string.englishNoMove);
                        }
                    } else {
                        gps.showSettingsAlert();
                    }
                }

            });
        }

        private void synchronizeDataset() {
            ArrayList<String> indices = mDBHelper.getAllLocations();
            for (String s : indices) {
                Log.d("LOG","UPDATING");
                ArrayList<String> data = mDBHelper.getData(Integer.parseInt(s));
                SendUserLocationTask task = new SendUserLocationTask(getApplicationContext(), new String[]{USERNAME, data.get(0),
                        data.get(1), data.get(2)});
                task.runVolley();
                mDBHelper.deleteLocation(Integer.parseInt(s));
            }
        }

        public boolean isConnectingToInternet(){
            ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null)
                    for (int i = 0; i < info.length; i++)
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
            }
            return false;
        }
    }
}
