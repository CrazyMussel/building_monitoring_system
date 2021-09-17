package com.unisalento.iotlab.deviceconfigurationservice.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SessionManager {

    private static String TAG = SessionManager.class.getSimpleName();

    SharedPreferences pref;

    Editor editor;
    Context _context;

    //shared pref mode
    int PRIVATE_MODE = 0;

    //shared preferences file name
    private static final String PREF_NAME = "DeviceConfigurationService";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    private static final String KEY_USER_INFO = "UserInfoCollected";
    private static final String KEY_BUILDINGS_INFO = "BuildingsInfoCollected";
    private static final String KEY_FLOORS_INFO = "FloorsInfoCollected";
    private static final String KEY_ROOMS_INFO = "RoomsInfoCollected";
    private static final String KEY_DEVICE_INFO = "DeviceInfoCollected";



    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }





    public void setLogin(boolean isLoggedIn) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);

        //commit changes
        editor.commit();

        Log.d(TAG, "User Login Session modified!");
    }




    public boolean isLoggedIn() {

        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }








}
