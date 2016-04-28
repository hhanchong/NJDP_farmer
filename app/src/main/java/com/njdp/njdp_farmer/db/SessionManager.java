package com.njdp.njdp_farmer.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
/**
 * Created by USER-PC on 2016/4/13.
 */
public class SessionManager {
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    SharedPreferences.Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "AndroidLogin";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";

    private static final String KEY_IS_DRIVER = "isDriver";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    //缓存登录状态信息
    public void setLogin(boolean isLoggedIn,boolean isDriver) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        editor.putBoolean(KEY_IS_DRIVER, isDriver);

        // commit changes
        editor.commit();

        Log.d(TAG, "User Login session modified!");
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }

    public boolean isDriver(){
        return pref.getBoolean(KEY_IS_DRIVER, false);
    }
}
