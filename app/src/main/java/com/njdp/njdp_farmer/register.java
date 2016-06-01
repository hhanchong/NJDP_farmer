package com.njdp.njdp_farmer;

import android.app.Activity;
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
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.njdp.njdp_farmer.MyClass.Farmer;
import com.njdp.njdp_farmer.changeDefault.TimeCount;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.db.SQLiteHandler;
import com.njdp.njdp_farmer.db.SessionManager;
import com.njdp.njdp_farmer.util.NetUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class register extends AppCompatActivity {

    private EditText text_user_name=null;
    private EditText text_user_telephone=null;
    private EditText text_verification_code=null;
    private EditText text_user_password=null;
    private Button btn_verification_code=null;
    private Button btn_register_next=null;
    private ImageButton getback=null;
    private Farmer farmer;
    private AwesomeValidation verification_code_Validation=new AwesomeValidation(ValidationStyle.BASIC);
    private AwesomeValidation mValidation=new AwesomeValidation(ValidationStyle.BASIC);
    private static final String TAG = register.class.getSimpleName();
    private ProgressDialog pDialog;
    private SessionManager session;
    private String verify_code = "";

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
        setContentView(R.layout.activity_register);
        farmer = new Farmer();
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        //SQLiteHandler db = new SQLiteHandler(getApplicationContext());

        btn_verification_code = (Button) super.findViewById(R.id.register_get_verification_code);
        text_user_name = (EditText) super.findViewById(R.id.user_name);
        text_user_telephone = (EditText) super.findViewById(R.id.user_telephone);
        text_verification_code = (EditText) super.findViewById(R.id.verification_code);
        text_user_password = (EditText) super.findViewById(R.id.user_password);
        btn_register_next = (Button) this.findViewById(R.id.register_next_button);
        getback=(ImageButton) super.findViewById(R.id.getback);

        btn_register_next.setEnabled(false);
        btn_register_next.setClickable(false);

        editTextIsNull();
        verification_code_Validation.addValidation(register.this, R.id.user_telephone, "^1[3-9]\\d{9}+$", R.string.err_phone);
        form_verification(register.this);
        //返回上一界面
        getback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //点击按钮是时验证注册表单
        btn_register_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mValidation.validate()) {
                    farmer.setName(text_user_name.getText().toString().trim());
                    farmer.setTelephone(text_user_telephone.getText().toString().trim());
                    farmer.setPassword(text_user_password.getText().toString().trim());
                    String t_verify_code = text_verification_code.getText().toString().trim();

                    if (verify_code.equals(t_verify_code)) {
                        register_next();

                    } else {
                        error_hint("验证码错误！");
                    }
                }
            }
        });

        //点击获取验证码按钮,，没填手机号提示，填了以后，发送短信，按钮60s倒计时，禁用60s
        btn_verification_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isempty(R.id.user_telephone)) {
                    empty_hint(R.string.err_phone2);
                } else if (verification_code_Validation.validate()) {
                    get_verification_code();
                    //按钮60s倒计时，禁用60s
                    TimeCount time_CountDown = new TimeCount(register.this, 60000, 1000, btn_verification_code);
                    time_CountDown.start();
                    empty_hint(R.string.vertify_hint);
                }
            }
        });
    }


    //EditText输入是否为空
    private boolean isempty( int id) {
        EditText editText = (EditText) super.findViewById(id);
        assert editText != null;
        return TextUtils.isEmpty(editText.getText());
    }

    //信息未输入提示
    private void empty_hint(int in) {
        Toast toast = Toast.makeText(register.this, getResources().getString(in), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    //错误信息提示
    private void error_hint(String str) {
        Toast toast = Toast.makeText(register.this, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    //获取验证码
    public void get_verification_code() {

        String tag_string_req = "req_register_driver_VerifyCode";

        if(!NetUtil.checkNet(register.this)){
            error_hint("网络连接错误");
        } else {
            StringRequest strReq = new StringRequest(Request.Method.GET,
                    AppConfig.URL_REGISTER, vertifySuccessListener, mErrorListener) {
                @Override
                protected Map<String, String> getParams() {

                    Map<String, String> params = new HashMap<>();
                    params.put("telephone", text_user_telephone.getText().toString());
                    return params;
                }
            };
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }

    }

    private void register_next()
    {
        checkRegister(farmer);
        // 跳转设置头像页面
//        Intent intent = new Intent(register.this, register_image.class);
        Bundle farmer_bundle = new Bundle();
        farmer_bundle.putString("name", text_user_name.getText().toString());
        farmer_bundle.putString("password", text_user_password.getText().toString());
        farmer_bundle.putString("telephone", text_user_telephone.getText().toString());
        farmer_bundle.putBoolean("isDriver", false);
//        intent.putExtra("farmer_register", farmer_bundle);
//        startActivity(intent);
    }

    //验证码响应服务器成功
    private Response.Listener<String> vertifySuccessListener =new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            Log.d(TAG, "Register Response: " + response);
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");
                if (!error) {

                    //服务器返回的验证码
                    verify_code=jObj.getString("verify_code");
                } else {
                    empty_hint(R.string.register_error1);
                    // Error occurred in registration. Get the error
                    // message
                    String errorMsg = jObj.getString("error_msg");
                    Log.e(TAG, errorMsg);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                empty_hint(R.string.register_error2);
            }

        }
    };

    //checkLogin 验证帐号密码
    public void checkRegister(final Farmer farmer) {

        String tag_string_req = "req_register";

        pDialog.setMessage("正在注册 ...");
        showDialog();

        if (!NetUtil.checkNet(register.this)) {
            hideDialog();
            error_hint("网络连接错误");
        } else {

            //服务器请求
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_REGISTER, mSuccessListener, mErrorListener) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to url
                    Map<String, String> params = new HashMap<>();
                    params.put("telephone", farmer.getTelephone());
                    params.put("password", farmer.getPassword());
                    params.put("name", farmer.getName());
                    params.put("isDriver", "false");
                    return params;
                }
            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }
    //响应服务器成功
    private Response.Listener<String> mSuccessListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            Log.i("tagconvertstr", "[" + response + "]");
            Log.d(TAG, "Register Response: " + response);
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    // Now store the user in SQLite
                    String token = jObj.getString("result");

                    // Inserting row in users table
                    //db.addUser(farmer.getId(), farmer.getName(), farmer.getTelephone(), farmer.getPassword(), farmer.getImageUrl());

                    // user successfully logged in
                    // Create signin session
                    session.setLogin(true,false, token);
                    //Launch main activity
                    Intent intent = new Intent(register.this, MainLink.class);
                    intent.putExtra("farmer_token", token);
                    startActivity(intent);
                    finish();
                } else {
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
    private Response.ErrorListener mErrorListener= new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "RegisterError: " + error.getMessage());
            error_hint(error.getMessage());
            hideDialog();
        }
    };

    //ProgressDialog显示与隐藏
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }
    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    //注册表单验证
    private void form_verification(final Activity activity) {
        mValidation.addValidation(activity, R.id.user_name, "^[\\u4e00-\\u9fa5]+$", R.string.err_name);
        mValidation.addValidation(activity, R.id.user_telephone, "^1[3-9]\\d{9}+$", R.string.err_phone);
        mValidation.addValidation(activity, R.id.verification_code, "\\d{6}+$", R.string.err_verification_code);
        mValidation.addValidation(activity, R.id.user_password, "^[A-Za-z0-9_]{5,15}+$", R.string.err_password);
    }

    //输入是否为空，判断是否禁用按钮
    private void editTextIsNull(){

        text_user_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(text_user_telephone.getText()) && !TextUtils.isEmpty(text_user_password.getText())
                        && !TextUtils.isEmpty(text_verification_code.getText())) {
                    btn_register_next.setClickable(true);
                    btn_register_next.setEnabled(true);
                    //btn_register_next.setBackground(getResources().getDrawable(R.drawable.bule_button.xml));
                } else {
                    btn_register_next.setEnabled(false);
                    btn_register_next.setClickable(false);
                    //btn_register_next.setBackground(getResources().getDrawable(R.drawable.buleButtonUnable));
                }
            }
        });

        text_user_telephone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(text_user_name.getText()) && !TextUtils.isEmpty(text_user_password.getText())
                        && !TextUtils.isEmpty(text_verification_code.getText())) {
                    btn_register_next.setClickable(true);
                    btn_register_next.setEnabled(true);
                } else {
                    btn_register_next.setEnabled(false);
                    btn_register_next.setClickable(false);
                }
            }
        });

        text_user_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(text_user_password.getText()) && !TextUtils.isEmpty(text_user_name.getText())
                        && !TextUtils.isEmpty(text_verification_code.getText())) {
                    btn_register_next.setClickable(true);
                    btn_register_next.setEnabled(true);
                } else {
                    btn_register_next.setEnabled(false);
                    btn_register_next.setClickable(false);
                }
            }
        });

        text_verification_code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(text_user_password.getText()) && !TextUtils.isEmpty(text_user_name.getText())
                        && !TextUtils.isEmpty(text_user_telephone.getText())) {
                    btn_register_next.setClickable(true);
                    btn_register_next.setEnabled(true);
                } else {
                    btn_register_next.setEnabled(false);
                    btn_register_next.setClickable(false);
                }
            }
        });
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