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
import com.njdp.njdp_farmer.changeDefault.TimeCount;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.util.NetUtil;
import com.njdp.njdp_farmer.util.NormalUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class getpassword extends AppCompatActivity {

    private String verify_code = "";
    //private String name;
    private Button getPassword_next = null;
    private AwesomeValidation verification_code_Validation = new AwesomeValidation(ValidationStyle.BASIC);
    private Button btn_VerificationCcode = null;
    private ImageButton getback=null;
    private EditText text_telephone = null;
    //private EditText text_user_name = null;
    private EditText text_VerificationCcode = null;
    private static final String TAG = getpassword.class.getSimpleName();
    private ProgressDialog pDialog;

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
        setContentView(R.layout.activity_getpassword);

        verification_code_Validation.addValidation(getpassword.this, R.id.user_telephone, "^1[3-9]\\d{9}+$", R.string.err_phone);
        this.getPassword_next = (Button) super.findViewById(R.id.btn_getPassword_next);
        this.btn_VerificationCcode = (Button) super.findViewById(R.id.btn_get_verificationCode);
        getback=(ImageButton) super.findViewById(R.id.getback);
        getPassword_next.setEnabled(false);
        getPassword_next.setClickable(false);

        this.text_telephone = (EditText) super.findViewById(R.id.user_telephone);
        //this.text_user_name = (EditText) super.findViewById(R.id.user_name);
        this.text_VerificationCcode = (EditText) super.findViewById(R.id.user_VerifyCode);

        editTextIsNull();

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //点击获取验证码按钮,，没填手机号提示，填了以后，发送短信，按钮60s倒计时，禁用60s
        btn_VerificationCcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NormalUtil.isempty(text_telephone)) {
                    NormalUtil.error_hint(getpassword.this, "手机号不能为空");
                } else if (verification_code_Validation.validate()) {

                    //String name = text_user_name.getText().toString().trim();
                    get_VerifyCode(text_telephone.getText().toString().trim());

                    //按钮60s倒计时，禁用60s
                    TimeCount time_CountDown = new TimeCount(getpassword.this, 60000, 1000, btn_VerificationCcode);
                    time_CountDown.start();
                    empty_hint(R.string.vertify_hint);
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

    //跳转到设置新密码页面
    public void getPassword_next(View v) {
        String t_verify_code = text_VerificationCcode.getText().toString().trim();
        if (verify_code.equals(t_verify_code)) {
            Intent intent = new Intent(getpassword.this, getpassword2.class);
            Bundle get_farmer_bundle = new Bundle();
            //get_farmer_bundle.putString("name", text_user_name.getText().toString());
            get_farmer_bundle.putString("telephone", text_telephone.getText().toString());
            get_farmer_bundle.putBoolean("isDriver", false);
            intent.putExtra("farmer_access", get_farmer_bundle);
            startActivity(intent);
        } else {
            NormalUtil.error_hint(getpassword.this, "验证码错误！");
        }
    }

    //获取验证码
    private void get_VerifyCode(final String telephone){

        String tag_string_req = "req_farmer_VerifyCode";

        if(!NetUtil.checkNet(getpassword.this)){
            NormalUtil.error_hint(getpassword.this, "网络连接错误");
        } else {
            StringRequest strReq = new StringRequest(Request.Method.GET,
                    AppConfig.URL_REGISTER, vertifySuccessListener, mErrorListener) {
                @Override
                protected Map<String, String> getParams() {

                    Map<String, String> params = new HashMap<>();
                    //params.put("name", name);
                    params.put("telephone", telephone);
                    return params;
                }
            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }

    //响应服务器失败
    private Response.ErrorListener mErrorListener = new Response.ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Register Error: " + error.getMessage());
            NormalUtil.error_hint(getpassword.this, "服务器连接失败");
            hideDialog();
        }
    };

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
                    empty_hint(R.string.vertify_error1);
                    // Error occurred in registration. Get the error
                    // message
                    String errorMsg = jObj.getString("error_msg");
                    Log.e(TAG, errorMsg);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                empty_hint(R.string.vertify_error2);
            }

        }
    };

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    //信息未输入提示
    private void empty_hint(int in){
        Toast toast = Toast.makeText(getpassword.this, getResources().getString(in), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    //输入是否为空，判断是否禁用按钮
    private void editTextIsNull(){

        text_telephone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(text_telephone.getText()) && !TextUtils.isEmpty(text_VerificationCcode.getText())) {
                    getPassword_next.setClickable(true);
                    getPassword_next.setEnabled(true);
                } else {
                    getPassword_next.setEnabled(false);
                    getPassword_next.setClickable(false);
                }
            }
        });

        text_VerificationCcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(text_telephone.getText())) {
                    getPassword_next.setClickable(true);
                    getPassword_next.setEnabled(true);
                } else {
                    getPassword_next.setEnabled(false);
                    getPassword_next.setClickable(false);
                }
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        View view = findViewById(R.id.top_layout);
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
