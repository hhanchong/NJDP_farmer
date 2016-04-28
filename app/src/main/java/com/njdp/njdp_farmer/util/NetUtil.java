package com.njdp.njdp_farmer.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 工具类，检查当前网络状态
 */
public class NetUtil {

    public static final String TAG = NetUtil.class.getSimpleName();
    private NormalUtil nutil;
    private boolean ISCONNECTED;
    private Context contexts;

    //判断是否连接网络
    public static boolean checkNet(Context context) {

        // 获取手机所以连接管理对象（包括wi-fi，net等连接的管理）
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conn != null) {
            // 网络管理连接对象
            NetworkInfo info = conn.getActiveNetworkInfo();

            if (info != null && info.isConnected()) {
                // 判断当前网络是否连接
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    //判断是否连接wifi
    public static boolean isWifi(Context context) {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conn.getActiveNetworkInfo();
        if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    //判断是否连接服务器
    public  boolean isConnected(Context context) {
        contexts=context;
        String tag_string_req = "req_connect";
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_IP,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.i("tagconvertstr", "[" + response + "]");
                        Log.d(TAG, "Login Response: " + response.toString());

                        try {
                            JSONObject jObj = new JSONObject(response);
                            boolean error = jObj.getBoolean("error");
                            if (!error) {
                                ISCONNECTED = true;
                            } else {
                                ISCONNECTED = false;
                                String errorMsg = jObj.getString("error_msg");
                                Log.e(TAG,errorMsg);
                                nutil.error_hint(contexts, "服务器连接失败");
                            }
                        } catch (JSONException e) {
                            ISCONNECTED = false;
                            e.printStackTrace();
                            Log.e(TAG, "Json error：response错误！" + e.getMessage());
                            nutil.error_hint(contexts, "服务器连接失败");
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Connect Error: " + error.getMessage());
                nutil.error_hint(contexts, "服务器连接失败");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login_driver url
                Map<String, String> params = new HashMap<String, String>();
                params.put("Connect_Test","C");
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        return ISCONNECTED;
    }
}
