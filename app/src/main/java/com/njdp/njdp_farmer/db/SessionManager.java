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

    private static final  String KEY_TOKEN="Token";

    private static final  String KEY_NAME="Name";

    private static final  String KEY_TELEPHONE="Telephone";

    private static final  String KEY_QQ="QQ";

    private static final  String KEY_WEIXIN="WeiXin";

    private static final  String KEY_ADDRESS="Address";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    //缓存登录状态信息
    public void setLogin(boolean isLoggedIn,boolean isDriver, String token) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        editor.putBoolean(KEY_IS_DRIVER, isDriver);
        editor.putString(KEY_TOKEN, token);
        // commit changes
        editor.commit();

        Log.d(TAG, "User Login session modified!");
    }

    //缓存用户信息
    public void setUserInfo(String name, String telephone, String qq, String weixin, String address){

        editor.putString(KEY_NAME, name);
        editor.putString(KEY_TELEPHONE, telephone);
        editor.putString(KEY_QQ, qq);
        editor.putString(KEY_WEIXIN, weixin);
        editor.putString(KEY_ADDRESS, address);
        // commit changes
        editor.commit();

        Log.d(TAG, "User Information session modified!");
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }

    public boolean isDriver(){
        return pref.getBoolean(KEY_IS_DRIVER, false);
    }

    public String getToken(){
        return pref.getString(KEY_TOKEN, "");
    }

    public String getName(){
        return pref.getString(KEY_NAME, "");
    }

    public String getTelephone(){
        return pref.getString(KEY_TELEPHONE, "");
    }

    public String getQQ(){
        return pref.getString(KEY_QQ, "");
    }

    public String getWeixin(){
        return pref.getString(KEY_WEIXIN, "");
    }

    public String getAddress(){
        return pref.getString(KEY_ADDRESS, "");
    }
}
