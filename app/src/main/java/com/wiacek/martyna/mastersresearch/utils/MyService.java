package com.wiacek.martyna.mastersresearch.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.wiacek.martyna.mastersresearch.R;
import com.wiacek.martyna.mastersresearch.tasks.SendUserLocationTask;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Martyna on 18.02.2017.
 */
public class MyService extends Service {

    public static final long NOTIFY_INTERVAL = 15 * 1000; // 15 seconds
    private static final double MIN_DISTANCE = 1;

    // run on another Thread to avoid crash
    private Handler mHandler;
    // timer handling
    private Timer mTimer = null;
    SendUserLocationTask task;

    long timeNow = 0;
    long time1, time2 = 0;
    ConnectivityManager connectivity;
    NetworkInfo[] info;

    private LocalBroadcastManager broadcaster;

    GPSTracker gps;
    DBHelper mDBHelper;
    SessionManager sessionManager;

    Intent intent;
    ArrayList<String> indices;
    ArrayList<String> data;

    Location LKL = null;
    Context context;

    static final public String NO_GPS_RESULT = "com.wiacek.martyna.mastersreaserch.NO_GPS";
    static final public String UPDATE_LATLANG = "com.wiacek.martyna.mastersreaserch.UPDATE_LATLANG";
    static final public String LATITUDE = "com.wiacek.martyna.mastersreaserch.LATITUDE";
    static final public String LONGITUDE = "com.wiacek.martyna.mastersreaserch.LONGITUDE";
    static final public String ALERT_DESCRIPTION = "com.wiacek.martyna.mastersresearch.ALERT_DESCRIPTION";
    static final public String MESSAGE_PL = "com.wiacek.martyna.mastersresearch.MESSAGE_PL";
    static final public String MESSAGE_EN = "com.wiacek.martyna.mastersresearch.MESSAGE_EN";

    String USERNAME;

    public MyService() {}

    public MyService (GPSTracker gpsTracker, Context context) {
        this.context = context;
        gps = gpsTracker;
        sessionManager = new SessionManager(context);
        USERNAME = sessionManager.getValueOfLogin();
        broadcaster = LocalBroadcastManager.getInstance(context);
        mDBHelper = new DBHelper(context);
        mHandler = new Handler();
        try {
                Thread.sleep(2000);
        }catch (InterruptedException e){
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void startTimer() {
        if(mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        } else {
            mTimer = new Timer();
        }

        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
        timeNow = System.currentTimeMillis();
        time1 = SystemClock.elapsedRealtime();
    }

    public void stopTimer() {

        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
    }

    @Override
    public void onCreate() {
        sessionManager = new SessionManager(context);
        USERNAME = sessionManager.getValueOfLogin();
        broadcaster = LocalBroadcastManager.getInstance(context);
        mDBHelper = new DBHelper(context);
        try {
            Thread.sleep(2000);
        }catch (InterruptedException e){
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    public void buildAlertMessageNoGps() {
        intent = new Intent(NO_GPS_RESULT);
        broadcaster.sendBroadcast(intent);
    }

    public void buildAlertDescription(int pl, int en) {
        intent = new Intent(ALERT_DESCRIPTION);
        intent.putExtra(MESSAGE_PL, pl);
        intent.putExtra(MESSAGE_EN, en);
        broadcaster.sendBroadcast(intent);
    }

    public void updateLatitudeLongitude(String latitude, String longitude) {
        intent = new Intent(UPDATE_LATLANG);
         if(latitude != null)
           intent.putExtra(LATITUDE, latitude);
        if (longitude != null)
            intent.putExtra(LONGITUDE, longitude);

        broadcaster.sendBroadcast(intent);
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (!gps.getManager().isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        buildAlertMessageNoGps();
                    } else if (gps.canGetLocation()){
                        updateLatitudeLongitude(Double.toString(gps.getLatitude()), Double.toString(gps.getLongitude()));
                        if ( (LKL == null ||
                                gps.checkDistance(LKL, gps.getLocation()) >= MIN_DISTANCE))  {
                            LKL = gps.getLocation();
                            updateLatitudeLongitude(Double.toString(LKL.getLatitude()), Double.toString(LKL.getLongitude()));
                            timeNow = System.currentTimeMillis();
                            if (!isConnectingToInternet()) {
                                buildAlertDescription(R.string.polishLocallyStatus, R.string.englishLocallyStatus);

                                mDBHelper.insertLocation(Double.toString(LKL.getLongitude()), Double.toString(LKL.getLatitude()), Long.toString(timeNow));
                            } else {

                                if (mDBHelper.numberOfRows() > 0) {
                                    synchronizeDataset();
                                }

                                buildAlertDescription(R.string.polishOnServerStatus, R.string.englishOnServerStatus);
                                task = new SendUserLocationTask(context, new String[]{USERNAME, Long.toString(timeNow), Double.toString(LKL.getLatitude()),
                                        Double.toString(LKL.getLongitude())});
                                task.runVolley();
                            }
                        } else {
                            buildAlertDescription(R.string.polishNoMove, R.string.englishNoMove);
                        }
                    } else {
                        buildAlertMessageNoGps();
                    }
                }
            });
        }


        private void synchronizeDataset() {
            indices = mDBHelper.getAllLocations();
            for (String s : indices) {
                data = mDBHelper.getData(Integer.parseInt(s));
                task = new SendUserLocationTask(context, new String[]{USERNAME, data.get(0),
                        data.get(1), data.get(2)});
                task.runVolley();
                mDBHelper.deleteLocation(Integer.parseInt(s));
            }
        }

        public boolean isConnectingToInternet(){
            connectivity = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                info = connectivity.getAllNetworkInfo();
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
