package com.njdp.njdp_farmer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.njdp.njdp_farmer.address.AddressSelect;
import com.njdp.njdp_farmer.bean.Farmer;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.db.SQLiteHandler;
import com.njdp.njdp_farmer.util.NetUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PersonalSet extends AppCompatActivity implements View.OnClickListener{
    private static final int PHONEEDIT = 1;
    private static final int ADDRESSEDIT = 2;
    private static final String TAG = PersonalSet.class.getSimpleName();
    private AwesomeValidation mValidation=new AwesomeValidation(ValidationStyle.BASIC);
    private ProgressDialog pDialog;
    Farmer farmer = new Farmer();
    RelativeLayout setPhoneNum;
    RelativeLayout setUserPic;
    RelativeLayout setAddress;
    ImageButton getback;
    Button editFinish;
    TextView tv_phone;
    TextView tv_address;
    EditText et_name;
    EditText et_QQ;
    EditText et_weixin;
    private NetUtil netutil;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_set);
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        farmer = (Farmer)getIntent().getSerializableExtra("farmer");
        if(farmer == null){
            error_hint("参数传输错误！");
            finish();
        }
        setPhoneNum = (RelativeLayout)super.findViewById(R.id.rl_setPhonenum);
        setAddress = (RelativeLayout)super.findViewById(R.id.rl_setAddress);
        setUserPic = (RelativeLayout)super.findViewById(R.id.rl_set_user_image);
        getback = (ImageButton)super.findViewById(R.id.getback);
        editFinish = (Button)super.findViewById(R.id.btn_editFinish);
        tv_phone = (TextView)super.findViewById(R.id.phonenum);
        tv_address = (TextView)super.findViewById(R.id.address);
        et_name = (EditText)super.findViewById(R.id.user_name);
        et_QQ = (EditText)super.findViewById(R.id.qq);
        et_weixin = (EditText)super.findViewById(R.id.weixin);

        listenerEvent();
    }

    //监听按钮点击事件
    private void listenerEvent()
    {
        setPhoneNum.setOnClickListener(this);
        setUserPic.setOnClickListener(this);
        getback.setOnClickListener(this);
        editFinish.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_setPhonenum:
                Intent intent = new Intent(this, SetPhoneNum.class);
                startActivityForResult(intent, PHONEEDIT);
                break;
            case R.id.rl_set_user_image:
                Intent intent2 = new Intent(this, register_image.class);
                startActivity(intent2);

                break;
            case R.id.rl_setAddress:
                Intent intent3 = new Intent(this, AddressSelect.class);
                intent3.putExtra("address", tv_address.getText().toString());
                startActivityForResult(intent3, ADDRESSEDIT);
                break;
            case R.id.getback:
                finish();
                break;
            case R.id.btn_editFinish:
                mValidation.addValidation(PersonalSet.this, R.id.user_name, "^[\\u4e00-\\u9fa5]+$", R.string.err_name);
                if((mValidation.validate() == true) && (et_QQ.getText().length() > 5 || TextUtils.isEmpty(et_QQ.getText()))){
                    farmer.setName(et_name.getText().toString());
                    farmer.setQQ(et_QQ.getText().toString());
                    farmer.setWeixin(et_weixin.getText().toString());
                    checkEdit(farmer);
                }
                else {
                    error_hint("请输入正确的QQ号！");
                }
                break;

            case 0:

                break;
        }
    }

    //这是跳转到另一个布局页面返回来的操作
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK){
            return;
        }
        switch (requestCode) {
            case PHONEEDIT:
                //User user = (User) data.getSerializableExtra("user");
                //Log.i("main", "注册信息是："+user);
                //Toast.makeText(this,"注册信息是："+user, 50000).show();
                farmer.setTelephone((String)data.getSerializableExtra("phonenum"));
                tv_phone.setText(farmer.getTelephone());
                break;
            case ADDRESSEDIT:
                farmer.setAddress(data.getStringExtra("address"));
                tv_address.setText(farmer.getAddress());
                break;
        }
    }

    //验证帐号密码
    public void checkEdit(final Farmer farmer) {

        String tag_string_req = "req_user_edit";

        pDialog.setMessage("正在提交 ...");
        showDialog();

        if (netutil.checkNet(PersonalSet.this) == false) {
            hideDialog();
            error_hint("网络连接错误");
            return;
        } else {

            //服务器请求
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_REGISTER, mSuccessListener, mErrorListener) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to url
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("id", String.valueOf(farmer.getId()));
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
            Log.d(TAG, "EditUser Response: " + response.toString());
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {

                    // Now store the user in SQLite
                    JSONObject farmers = jObj.getJSONObject("Farmers");
                    farmer.setId(farmers.getInt("Id"));
                    farmer.setName(farmers.getString("Name"));
                    farmer.setImageUrl(farmers.getString("ImageUrl"));

                    // Inserting row in users table
                    db.editUser(farmer.getId(), farmer.getName(), farmer.getTelephone(), farmer.getPassword(), farmer.getImageUrl());

                    //Launch main activity
                    Intent intent = new Intent(PersonalSet.this, mainpages.class);
                    intent.putExtra("farmer_edit", farmer);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    error_hint("服务器响应错误！");
                    // Error in signin Get the error message
                    String errorMsg = jObj.getString("error_msg");
                    Log.e(TAG, errorMsg);
                }
            } catch (JSONException e) {
                error_hint(getResources().getString(R.string.connect_error));
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

    //错误信息提示
    private void error_hint(String str) {
        Toast toast = Toast.makeText(PersonalSet.this, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    //ProgressDialog显示与隐藏
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }
    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
