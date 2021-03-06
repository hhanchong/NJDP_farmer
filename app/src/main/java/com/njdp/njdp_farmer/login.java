package com.njdp.njdp_farmer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.njdp.njdp_farmer.MyClass.AgentApplication;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.db.SQLiteHandler;
import com.njdp.njdp_farmer.db.SessionManager;
import com.njdp.njdp_farmer.MyClass.Farmer;
import com.njdp.njdp_farmer.util.NetUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class login extends AppCompatActivity {

    private EditText text_username=null;
    private EditText text_password=null;
    private ImageButton password_reveal=null;
    private ImageButton password_show=null;
    private Button login_check=null;
    private static final String TAG = login.class.getSimpleName();
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private Farmer farmer;
    private boolean historyLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置沉浸模式
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_login);
        AgentApplication.addActivity(this);

        farmer = new Farmer();
        if (session!=null)
        {
            //检测是否缓存了登录信息
            if (session.isLoggedIn() && !session.getToken().equals("")) {
                // User is already logged in. Take him to main activity
                Intent intent = new Intent(login.this, MainLink.class);
                intent.putExtra("TOKEN", session.getToken());
                startActivity(intent);
                finish();
            }
        }

        this.text_username=(EditText) super.findViewById(R.id.user_username);
        this.text_password=(EditText) super.findViewById(R.id.user_password);
        this.password_reveal=(ImageButton) super.findViewById(R.id.reveal_button);
        this.password_show=(ImageButton) super.findViewById(R.id.show_button);
        this.login_check=(Button) super.findViewById(R.id.loginin_button);

        //根据输入框是否为空判断是否禁用按钮
        assert login_check != null;
        login_check.setEnabled(false);
        login_check.setClickable(false);
        editTextIsNull();
        //编辑框的提示
        editTextHintVisible();
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn() && !session.getToken().equals("")) {
            Intent intent = new Intent(login.this, MainLink.class);
            intent.putExtra("TOKEN", session.getToken());
            startActivity(intent);
            finish();
        }else{
            HashMap<String, String> user = db.getUserDetails();
            if(user.size() > 0) {
                text_username.setText(user.get("telephone"));
                text_password.setText(user.get("password"));
                farmer.setTelephone(text_username.getText().toString());
                farmer.setPassword(text_password.getText().toString());
                if (user.get("password").length() > 0) {
                    historyLogin = true;
                }
            }
        }


        login_check.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                farmer.setTelephone (text_username.getText().toString());
                farmer.setPassword(text_password.getText().toString());
                checkLogin(farmer);
            }
        });
    }

    //点击眼睛按钮，设置密码显示或者隐藏
    public void showClick(View v) {
        password_reveal.setVisibility(View.VISIBLE);
        password_show.setVisibility(View.GONE);
        login.this.text_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }
    public void revealClick(View v) {
        password_reveal.setVisibility(View.GONE);
        password_show.setVisibility(View.VISIBLE);
        login.this.text_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
    }

    // register_user 跳转到注册界面
    public void register_user(View v){
        Intent intent = new Intent(this, register.class);
        startActivity(intent);
    }

    //找回密码
    public void get_password(View v){
        Intent intent = new Intent(this, getpassword.class);
        startActivity(intent);
    }

    //checkLogin 验证帐号密码
    public void checkLogin(final Farmer farmer) {

        String tag_string_req = "req_login";

        pDialog.setMessage("正在登录 ...");
        showDialog();

        if (!NetUtil.checkNet(login.this)) {
            hideDialog();
            error_hint("网络连接错误");
        } else {

            //服务器请求
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_LOGIN, mSuccessListener, mErrorListener) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to url
                    Map<String, String> params = new HashMap<>();
                    params.put("fm_username", farmer.getTelephone());
                    params.put("fm_password", farmer.getPassword());
                    params.put("fm_tag", "F");
                    return params;
                }
            };

            strReq.setRetryPolicy(new DefaultRetryPolicy(1000,3,1.0f)); //请求超时时间1S，重复3次
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

    //响应服务器成功
    private Response.Listener<String> mSuccessListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            Log.i("tagconvertstr", "[" + response + "]");
            Log.d(TAG, "Login Response: " + response);
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                int status = jObj.getInt("status");

                // Check for error node in json
                if (status == 0) {
                    // Now store the user in SQLite
                    String token = jObj.getString("result");
                    // user successfully logged in
                    // Create signin session
                    session.setLogin(true,false,token);
                    // Inserting row in users table
                    db.addUser(farmer.getId(), farmer.getName(), farmer.getTelephone(), farmer.getPassword(), farmer.getImageUrl());
                    //Launch main activity
                    Intent intent = new Intent(login.this, MainLink.class);
                    intent.putExtra("TOKEN", token);
                    startActivity(intent);
                    finish();

                } else if(status == 1) {
                    error_hint("无此用户！");
                }else if(status == 2){
                    error_hint("密码错误！");
                }else{
                    empty_hint(R.string.login_error);
                    // Error in signin Get the error message
                    String errorMsg = jObj.getString("error_msg");
                    Log.e(TAG, errorMsg);
                }
            } catch (JSONException e) {
                empty_hint(R.string.connect_error);
                // JSON error
                e.printStackTrace();
                Log.e(TAG, "Json error：response错误！" + e.getMessage());
            }
        }
    };

    //响应服务器失败
    private Response.ErrorListener mErrorListener = new Response.ErrorListener()  {

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Login Error: " + error.getMessage());
            error_hint("服务器连接失败");
            hideDialog();
        }
    };

    //错误信息提示1
    private void error_hint(String str){
        Toast toast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,-50);
        toast.show();
    }

    //错误信息提示2
    private void empty_hint(int in){
        Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(in), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    //输入是否为空，判断是否禁用按钮
    private void editTextIsNull(){

        text_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //当记录的历史用户修改后，密码置空
                if (historyLogin) {
                    historyLogin = false;
                    text_password.setText("");
                    db.editUser(farmer.getId(), farmer.getName(), farmer.getTelephone(), "", farmer.getImageUrl());
                }
                if ((s.length() > 0) && !TextUtils.isEmpty(text_password.getText())) {
                    login_check.setClickable(true);
                    login_check.setEnabled(true);
                } else {
                    login_check.setEnabled(false);
                    login_check.setClickable(false);
                }
            }
        });

        text_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(text_username.getText())) {
                    login_check.setClickable(true);
                    login_check.setEnabled(true);
                } else {
                    login_check.setEnabled(false);
                    login_check.setClickable(false);
                }
            }
        });
    }

    private void editTextHintVisible()
    {
        text_username.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (hasFocus) { //如果组件获得焦点
                    text_username.setHint(null);
                } else {
                    if (text_username.getText().length() > 0) {
                        text_username.setHint(null);
                    } else {
                        text_username.setHint("请输入注册手机号");
                    }
                }
            }
        });

        text_password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (hasFocus) { //如果组件获得焦点
                    text_password.setHint(null);
                } else {
                    if (text_password.getText().length() > 0) {
                        text_password.setHint(null);
                    } else {
                        text_password.setHint("请输入密码");
                    }
                }
            }
        });
    }

    private long timeMillis;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - timeMillis) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                timeMillis = System.currentTimeMillis();
            } else {
                AgentApplication.ExitApp();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        View view = findViewById(R.id.top_layout);
        assert view != null;
        view.setBackgroundResource(0); //释放背景图片
    }

    //不跟随系统变化字体大小
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config=new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }
}

