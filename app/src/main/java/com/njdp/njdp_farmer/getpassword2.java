package com.njdp.njdp_farmer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.njdp.njdp_farmer.bean.Farmer;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.db.LruBitmapCache;
import com.njdp.njdp_farmer.db.SQLiteHandler;
import com.njdp.njdp_farmer.db.SessionManager;
import com.njdp.njdp_farmer.util.NetUtil;
import com.njdp.njdp_farmer.util.NormalUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class getpassword2 extends AppCompatActivity {

    private EditText text_password=null;
    private EditText text_password2=null;
    private ImageButton password_reveal=null;
    private ImageButton password_show = null;
    private ImageButton password_reveal2=null;
    private ImageButton password_show2=null;
    private ImageButton getback=null;
    private String telephone;
    private File tempFile;
    private AwesomeValidation password_Validation=new AwesomeValidation(ValidationStyle.BASIC);
    private AwesomeValidation password2_Validation=new AwesomeValidation(ValidationStyle.BASIC);
    private static final String TAG = getpassword2.class.getSimpleName();
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private NormalUtil nutil=new NormalUtil();
    private NetUtil netutil=new NetUtil();
    private LruBitmapCache lruBitmapCache=new LruBitmapCache();
    private String URL_GETPASSWORD2;//设置连接数据用户的URL，Driver Or Farmer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getpassword2);

        //设置头像本地存储路径
        if(nutil.ExistSDCard())
        {
            tempFile= Environment.getExternalStorageDirectory();
        }else
        {
            tempFile=getCacheDir();
        }

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        //获取forgetpassword输入的用户名、手机号.姓名
        telephone = getIntent().getExtras().getBundle("farmer_access").getString("telephone");
        if(telephone == null)
        {
            nutil.error_hint(getpassword2.this, "程序错误！请联系管理员！");
            finish();
        }

        password_Validation.addValidation(getpassword2.this, R.id.user_password, "^[A-Za-z0-9_]{5,15}+$", R.string.err_password);
        password2_Validation.addValidation(getpassword2.this, R.id.user_password2,"^[A-Za-z0-9_]{5,15}+$" , R.string.err_password);
        this.password_reveal=(ImageButton) super.findViewById(R.id.reveal_button);
        this.password_show=(ImageButton) super.findViewById(R.id.show_button);
        this.password_reveal2=(ImageButton) super.findViewById(R.id.reveal_button2);
        this.password_show2=(ImageButton) super.findViewById(R.id.show_button2);
        this.text_password=(EditText)super.findViewById(R.id.user_password);
        this.text_password2=(EditText)super.findViewById(R.id.user_password2);
        this.getback=(ImageButton) super.findViewById(R.id.getback);

        findViewById(R.id.finish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nutil.isempty(text_password)){
                    nutil.error_hint(getpassword2.this,"请输入新的密码");
                }else if(password_Validation.validate()==true){
                    if(nutil.isempty(text_password2)){
                        nutil.error_hint(getpassword2.this,"请再次输入密码");
                    }else if(password2_Validation.validate()==true){
                        if(text_password.getText().equals(text_password2.getText())){
                            String nPassword=text_password2.getText().toString().trim();
                            setNewPassword(nPassword);
                        }
                        else{
                            nutil.error_hint(getpassword2.this,"两次输入的密码不一致！");
                        }
                    }
                }
            }
        });
        //返回上一界面
        getback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //点击眼睛1按钮，设置密码显示或者隐藏
    public void showClick(View v) {
        password_reveal.setVisibility(View.VISIBLE);
        password_show.setVisibility(View.GONE);
        getpassword2.this.text_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }
    public void revealClick(View v) {
        password_reveal.setVisibility(View.GONE);
        password_show.setVisibility(View.VISIBLE);
        getpassword2.this.text_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
    }
    //点击眼睛2按钮，设置密码显示或者隐藏
    public void showClick2(View v) {
        password_reveal2.setVisibility(View.VISIBLE);
        password_show2.setVisibility(View.GONE);
        getpassword2.this.text_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }
    public void revealClick2(View v) {
        password_reveal2.setVisibility(View.GONE);
        password_show2.setVisibility(View.VISIBLE);
        getpassword2.this.text_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
    }

    //设置新的密码到数据库
    public void setNewPassword(final String password) {

        // Tag used to cancel the request
        String tag_string_req = "req_reset_password";

        pDialog.setMessage("正在重置密码 ...");
        showDialog();

        if (netutil.checkNet(getpassword2.this) == false) {
            nutil.error_hint(getpassword2.this, "网络连接错误");
            hideDialog();
            return;
        } else {
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    URL_GETPASSWORD2, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Register Response: " + response.toString());
                    hideDialog();

                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");
                        if (!error) {

                            JSONObject user = jObj.getJSONObject("Farmers");
                            boolean islogined = user.getBoolean("isLogined");
                            String imageurl=user.getString("imageurl");
                            Bitmap bitmap=lruBitmapCache.getBitmap(imageurl);
                            Bitmap zoobitmap=nutil.zoomBitmap(bitmap, 400, 400);
                            nutil.saveBitmap(getpassword2.this,zoobitmap);
                            //imageurl=tempFile.getAbsolutePath().toString()+"temp"+filename;
                            // Inserting row in users table
                            //farmer.setPassword(password);
                            //db.editUser(farmer.getId(), farmer.getName(), farmer.getPassword(), farmer.getTelephone(), farmer.getImageUrl());
                            nutil.error_hint(getpassword2.this, "重置密码成功");

                            // 重新登录
                            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        } else {

                            // Error occurred in registration. Get the error
                            // message
                            String errorMsg = jObj.getString("error_msg");
                            nutil.error_hint(getpassword2.this, errorMsg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Registration Error: " + error.getMessage());
                    nutil.error_hint(getpassword2.this, error.getMessage());
                    hideDialog();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("telephone", telephone);
                    params.put("password", password);
                    params.put("tag", "F");
                    return params;
                }

            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
