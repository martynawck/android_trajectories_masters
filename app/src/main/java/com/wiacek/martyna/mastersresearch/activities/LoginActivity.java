package com.wiacek.martyna.mastersresearch.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.PowerManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.wiacek.martyna.mastersresearch.R;
import com.wiacek.martyna.mastersresearch.tasks.CheckIfUserExistsTask;
import com.wiacek.martyna.mastersresearch.utils.SessionManager;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "MainActivity";

    private GoogleApiClient mGoogleApiClient;
    private boolean mIntentInProgress;
    private boolean mSignInClicked;
    private ConnectionResult mConnectionResult;
    SessionManager sessionManager;

    private SignInButton btnSignIn;
    private TextView description;
    private ImageButton polish, english;
    private boolean isLanguagePolish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().requestProfile().requestId()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        sessionManager = new SessionManager(getApplicationContext());

        if (sessionManager.getValueOfIsLogged()) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();

        } else {
            btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
            description = (TextView) findViewById(R.id.description);
            polish = (ImageButton) findViewById(R.id.polishButton);
            english = (ImageButton) findViewById(R.id.englishButton);

            isLanguagePolish = true;

            changeLanguage("PL");

            btnSignIn.setSize(SignInButton.SIZE_STANDARD);
            btnSignIn.setScopes(gso.getScopeArray());
            btnSignIn.setOnClickListener(this);
            polish.setOnClickListener(this);
            english.setOnClickListener(this);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("INFORMACJA");
            builder.setMessage("Upewnij się, że przed każdym korzystaniem z aplikacji wyłączony został TRYB OSZCZĘDZAJĄCY BATERIĘ. " +
                    "W przeciwnym wypadku wyniki badania będą nieprawidłowe! By polepszyć precyzję pomiaru ustaw metodę pobierania lokalizacji na GPS, WiFi oraz sieci komórkowe.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in:
                signIn();
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
    /**
     * Method to resolve any signin errors
     * */
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    public String makeSHA1Hash(String input)
            throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.reset();
        byte[] buffer = input.getBytes("UTF-8");
        md.update(buffer);
        byte[] digest = md.digest();

        String hexStr = "";
        for (int i = 0; i < digest.length; i++) {
            hexStr +=  Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return hexStr;
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();

            try {
                CheckIfUserExistsTask task = new CheckIfUserExistsTask(this, new String[] {makeSHA1Hash(acct.getEmail())});
                task.runVolley();
            } catch (Exception e) {
                Toast.makeText(this, "Nie ma połączenia z internetem!", Toast.LENGTH_LONG).show();
            }
        } else if (isLanguagePolish == true) {
            Toast.makeText(this, "Nie udało się zalogować!", Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(this, "You could not be connected!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;
        Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
    }

    private void changeLanguage(String lang) {
        if (lang == "PL") {
            description.setText(getString(R.string.polishDescription));
        } else {
            description.setText(getString(R.string.englishDescription));
        }
    }


}
