package com.wiacek.martyna.mastersresearch.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.wiacek.martyna.mastersresearch.utils.GPSTracker;
import com.wiacek.martyna.mastersresearch.utils.MyService;
import com.wiacek.martyna.mastersresearch.R;
import com.wiacek.martyna.mastersresearch.utils.SessionManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
         GoogleApiClient.OnConnectionFailedListener{

    private static final int RC_SIGN_IN = 0;
    private static final int MY_PERMISSION_LOCATION = 123;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIntentInProgress;
    private boolean mSignInClicked;
    private ConnectionResult mConnectionResult;
    GPSTracker gps;
    MyService myService = null;
    private Button btnSignOut, btnStartPause;
    private ImageButton polish, english;
    private TextView description;
    private boolean isButtonStart;
    private boolean isResearchStarted;
    private boolean isLanguagePolish;
    private String username;
    private TextView statusDescription, userIdValue;
    private TextView latitudeShow, longitudeShow;
    private boolean isNetworkAvailable;

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainw);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().requestProfile().requestId()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        sessionManager = new SessionManager(getApplicationContext());
        username = sessionManager.getValueOfLogin();


        isLanguagePolish = true;
        if (sessionManager.getValueOfIsResearchRunning()) {
            isResearchStarted = true;
            isButtonStart = false;

        } else {
            isResearchStarted = false;
            isButtonStart = true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_LOCATION);
        }
        gps = new GPSTracker(MainActivity.this);

        userIdValue = (TextView) findViewById(R.id.userIdValue);
        description = (TextView) findViewById(R.id.description);
        btnSignOut = (Button) findViewById(R.id.btn_sign_out);
        btnStartPause = (Button) findViewById(R.id.btn_start_pause);
        polish = (ImageButton) findViewById(R.id.polishButton);
        english = (ImageButton) findViewById(R.id.englishButton);
        statusDescription = (TextView) findViewById(R.id.status_description);
        latitudeShow = (TextView) findViewById(R.id.latitude);
        longitudeShow = (TextView) findViewById(R.id.longitude);
        latitudeShow.setText(Long.toString(sessionManager.getLastLatitude()));
        longitudeShow.setText(Long.toString(sessionManager.geLastLongitude()));

        changeLanguage("PL");
        isNetworkAvailable = true;

        userIdValue.setText(username);
        btnStartPause.setOnClickListener(this);
        polish.setOnClickListener(this);
        english.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);
        btnStartPause.setVisibility(View.VISIBLE);
        btnSignOut.setVisibility(View.VISIBLE);

        if (sessionManager.getValueOfIsResearchRunning() == true)
            startSendingCurrentLocationTask();
    }

    /****** BUTTONS PRESSED *****/

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        // your code.
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver((receiver),
                new IntentFilter(MyService.NO_GPS_RESULT)
        );
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver((receiver2),
                new IntentFilter(MyService.UPDATE_LATLANG)
        );
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver((receiver3),
                new IntentFilter(MyService.ALERT_DESCRIPTION)
        );
    }

    private void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            sessionManager.destroySession();
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            stopSendingCurrentLocationTask();
                            finish();
                        }
                    });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_pause:
                runTrajectoryCalculations();
                if (isLanguagePolish)
                    changeLanguage("PL");
                else
                    changeLanguage("EN");
                break;
            case R.id.btn_sign_out:
                signOutFromGplus();
                break;
            case R.id.polishButton:
                changeLanguage("PL");
                isLanguagePolish = true;
                break;
            case R.id.englishButton:
                changeLanguage("EN");
                isLanguagePolish = false;
                break;
        }
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            stopSendingCurrentLocationTask();
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
                    0).show();
            return;
        }

        if (!mIntentInProgress) {
            mConnectionResult = result;

            if (mSignInClicked) {
                resolveSignInError();
            }
        }
    }

    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    /**** TRAJECTORY CALCULATION****/
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSION_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            gps = new GPSTracker(MainActivity.this);
        }
    }

    /**** START TRAJECTORY LOGGING ***/
    private void runTrajectoryCalculations() {
        gps.getLocation();

        if (isButtonStart) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("INFORMACJA");
            builder.setMessage("Upewnij się, że przed każdym korzystaniem z aplikacji wyłączony został TRYB OSZCZĘDZAJĄCY BATERIĘ. " +
                    "W przeciwnym wypadku wyniki badania będą nieprawidłowe! By polepszyć precyzję pomiaru ustaw metodę pobierania lokalizacji na GPS, WiFi oraz sieci komórkowe.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (gps.canGetLocation()) {
                        latitudeShow.setText(Double.toString(gps.getLatitude()));
                        longitudeShow.setText(Double.toString(gps.getLongitude()));
                        gps.stopUsingGPS();
                        startSendingCurrentLocationTask();
                        btnStartPause.setText("PAUZA");
                        isButtonStart = false;
                        isResearchStarted = true;
                    } else {
                        gps.showSettingsAlert();
                    }
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            stopSendingCurrentLocationTask();
            btnStartPause.setText("START");
            isResearchStarted = false;
            isButtonStart = true;
        }
    }

    /**** LANGUAGE ***/
    private void changeLanguage(String lang) {
        if (lang == "PL") {
            description.setText(getString(R.string.polishDescriptionMA2));
            btnSignOut.setText(getString(R.string.signOffPL));
            if (isResearchStarted) {
                btnStartPause.setText(getString(R.string.pauseButtonPL));
                if (isNetworkAvailable) {
                    statusDescription.setText(getString((R.string.polishOnServerStatus)));
                } else
                    statusDescription.setText(getString(R.string.polishLocallyStatus));
            }
            else {
                btnStartPause.setText(getString(R.string.startButtonPL));
                statusDescription.setText(getString(R.string.researchPausedStoppedPL));
            }
        } else {
            description.setText(getString(R.string.englishDescriptionMA2));
            btnSignOut.setText(getString(R.string.signOffEN));
            if (isResearchStarted) {
                btnStartPause.setText(getString(R.string.pauseButtonEN));
                if (isNetworkAvailable) {
                    statusDescription.setText(getString((R.string.englishOnServerStatus)));
                } else
                    statusDescription.setText(getString(R.string.englishLocallyStatus));
            }
            else {
                btnStartPause.setText(getString(R.string.startButtonEN));
                statusDescription.setText(getString(R.string.researchPausedStoppedEN));
            }
        }
    }

    boolean stopToSetGPS() {
            stopSendingCurrentLocationTask();
            btnStartPause.setText("START");
            isResearchStarted = false;
            isButtonStart = true;
            if (isLanguagePolish)
                changeLanguage("PL");
            else
                changeLanguage("EN");

            gps.showSettingsAlert();

        return true;
    }

        /**** RECEIVERS ******/
    BroadcastReceiver receiver = new BroadcastReceiver() {
            private boolean firstConnect = true;
            private long lastTime = 0;
            @Override
        public void onReceive(Context context, Intent intent) {

                long timeNow = System.currentTimeMillis();
                if ( timeNow - lastTime >= 15*1000) {
                    firstConnect = true;
                }

                if(firstConnect) {
                    firstConnect = false;
                    lastTime = timeNow;
                    stopToSetGPS();
                }
        }
    };

    BroadcastReceiver receiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            latitudeShow.setText(intent.getStringExtra(MyService.LATITUDE));
            longitudeShow.setText(intent.getStringExtra(MyService.LONGITUDE));

        }
    };

    BroadcastReceiver receiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isLanguagePolish)
                statusDescription.setText(getString(intent.getIntExtra(MyService.MESSAGE_PL,R.string.polishNoMove)));
            else
                statusDescription.setText(getString(intent.getIntExtra(MyService.MESSAGE_EN,R.string.englishNoMove)));
        }
    };

    /***** START SENDING LOCATION ****/
    void startSendingCurrentLocationTask() {
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver((receiver),
                new IntentFilter(MyService.NO_GPS_RESULT)
        );
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver((receiver2),
                new IntentFilter(MyService.UPDATE_LATLANG)
        );
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver((receiver3),
                new IntentFilter(MyService.ALERT_DESCRIPTION)
        );

        myService = new MyService(gps, getApplicationContext());
        myService.startTimer();
        sessionManager.setValueOfIsResearchRunning(true);
    }

    /****** STOP SENDING LOCATION *******/
    void stopSendingCurrentLocationTask() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver2);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver3);
        if (myService != null)
            myService.stopTimer();
        sessionManager.setValueOfIsResearchRunning(false);
    }
}
