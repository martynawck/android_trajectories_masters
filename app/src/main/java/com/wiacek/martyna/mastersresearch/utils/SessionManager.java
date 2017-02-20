package com.wiacek.martyna.mastersresearch.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Martyna on 19.02.2017.
*/

public class SessionManager {
    private final SharedPreferences _sharedPreferences;
    private final SharedPreferences.Editor _editor;
    private static final String SESSION_NAME = "MASTERS_RESEARCH_APP";

    private static final String KEY_IS_LOGIN = "IS_LOGGED";
    private static final String KEY_USERNAME = "USERNAME";
    private static final String KEY_IS_RESEACH_RUNNING = "IS_RESEARCH_RUNNING";

    private static final String LAST_LATITUDE = "LAST_LATITUDE";
    private static final String LAST_LONGITUDE = "LAST_LONGITUDE";
    private static final String LAST_NETWORK_STATUS = "LAST_NETWORK_STATUS";


    public SessionManager(Context context){
        Context _context = context;
        _sharedPreferences = _context.getSharedPreferences(SESSION_NAME, Context.MODE_PRIVATE);
        _editor =_sharedPreferences.edit();
    }

    public void createSession(String username) {
        _editor.putString(KEY_USERNAME,username);
        _editor.putBoolean(KEY_IS_LOGIN, true);
        _editor.putBoolean(KEY_IS_RESEACH_RUNNING, false);
        _editor.commit();
    }

    public String getValueOfLogin() {
        return _sharedPreferences.getString(KEY_USERNAME, "");
    }

    public void setValueOfIsLogged(boolean b) {
        _editor.putBoolean(KEY_IS_LOGIN,b );
        _editor.commit();
    }

    public void setValueOfIsResearchRunning(boolean b) {
        _editor.putBoolean(KEY_IS_RESEACH_RUNNING,b );
        _editor.commit();
    }

    public void setValueOfLastLongitude(long b) {
        _editor.putLong(LAST_LONGITUDE,b );
        _editor.commit();
    }

    public void setValueOfLastLatitude(long b) {
        _editor.putLong(LAST_LATITUDE,b );
        _editor.commit();
    }

    public void setValueOfLastNetworkState(String b) {
        _editor.putString(LAST_NETWORK_STATUS,b );
        _editor.commit();
    }

    public boolean getValueOfIsLogged() {
        return _sharedPreferences.getBoolean(KEY_IS_LOGIN, false);
    }

    public long getLastLatitude() {
        return _sharedPreferences.getLong(LAST_LATITUDE, 0);
    }

    public long geLastLongitude() {
        return _sharedPreferences.getLong(LAST_LONGITUDE, 0);
    }

    public String getLastNetworkStatus() {
        return _sharedPreferences.getString(LAST_NETWORK_STATUS, "");
    }

    public boolean getValueOfIsResearchRunning() {
        return _sharedPreferences.getBoolean(KEY_IS_RESEACH_RUNNING, false);
    }


    public boolean resumeSession(){
        return _sharedPreferences.contains(KEY_USERNAME);
    }

    public void destroySession() {
        _editor.clear();
        _editor.commit();
    }
}