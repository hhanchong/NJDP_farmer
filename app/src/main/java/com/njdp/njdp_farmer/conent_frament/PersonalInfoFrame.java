package com.njdp.njdp_farmer.conent_frament;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.njdp.njdp_farmer.FarmerLandList;
import com.njdp.njdp_farmer.PersonalSet;
import com.njdp.njdp_farmer.R;
import com.njdp.njdp_farmer.MyClass.Farmer;
import com.njdp.njdp_farmer.MyClass.FarmlandInfo;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.db.SQLiteHandler;
import com.njdp.njdp_farmer.login;
import com.njdp.njdp_farmer.mainpages;
import com.njdp.njdp_farmer.util.NetUtil;
import com.njdp.njdp_farmer.util.NormalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PersonalInfoFrame extends Fragment implements View.OnClickListener {
    private final String TAG = "PersonalInfoFrame";
    private static final int USEREDIT = 1;
    private SQLiteHandler db;
    private NormalUtil nutil=new NormalUtil();
    //所有监听的控件
    static ImageView userImage;
    TextView userName, telephone, qq, weixin, address;
    Button personalEdit;
    View view;
    private ProgressDialog pDialog;
    private NetUtil netutil = new NetUtil();
    private String token;
    private Farmer farmer;
    private String path;//用户头像路径
    private boolean imageexists = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            Bundle bundle = getArguments();
            token = bundle.getString("token");

            //判断参数传递是否正确
            if (token == null) {
                error_hint("参数传递错误！");
                return null;
            }
            // SQLite database handler
            db = new SQLiteHandler(getActivity().getApplicationContext());

            if (view == null) {
                view = inFlater(inflater);
            }
            pDialog = new ProgressDialog(getActivity());
            pDialog.setCancelable(false);
            pDialog.setMessage("正在获取用户信息 ...");
            showDialog();
            //获取用户信息
            getUserInfo();

            return view;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public View inFlater(LayoutInflater inflater) {
        view = inflater.inflate(R.layout.activity_personal_info, null, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        userImage = (ImageView) view.findViewById(R.id.user_image);
        HashMap<String, String> user = db.getUserDetails();
        //设置头像本地存储路径
        if(nutil.ExistSDCard()) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/NJDP/"+"njdp_" +user.get("telephone") + "_image.png";
        }else {
            path = getActivity().getCacheDir().getAbsolutePath()+"/NJDP/"+"njdp_" +user.get("telephone") + "_image.png";
        }
        if(new File(path).exists()) {
            userImage.setImageURI(Uri.parse(path));
        }else {
            imageexists = false;
        }

        userName = (TextView) view.findViewById(R.id.tv_user_name);
        telephone = (TextView) view.findViewById(R.id.tv_phonenum);
        qq = (TextView) view.findViewById(R.id.tv_qq);
        weixin = (TextView) view.findViewById(R.id.tv_weixin);
        address = (TextView) view.findViewById(R.id.tv_address);
        personalEdit = (Button) view.findViewById(R.id.btn_edit);
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        initOnClick();
    }

    private void initOnClick() {
        personalEdit.setOnClickListener(this);
        //Thread.setOnClickListener(this);
        //myMessage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // TODO: 2015/11/18 头像
            case R.id.btn_edit:
                Log.e("------------->", "点击修改用户信息");
                if(farmer != null) {
                    Intent intent1 = new Intent(getActivity(), PersonalSet.class);
                    intent1.putExtra("user", farmer);
                    startActivityForResult(intent1, USEREDIT);
                }
                break;
        }
    }

    public void updateView(){
        userName.setText(farmer.getName());
        telephone.setText(farmer.getTelephone());
        qq.setText(farmer.getQQ());
        weixin.setText(farmer.getWeixin());
        address.setText(farmer.getAddress());
    }
    //这是跳转到另一个布局页面返回来的操作
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        switch (requestCode) {
            case USEREDIT:
                farmer = (Farmer) data.getSerializableExtra("user");
                if(farmer == null){
                    error_hint("参数传递错误！");
                    getActivity().finish();
                }
                //userImage.setImageURI();
                userName.setText(farmer.getName());
                telephone.setText(farmer.getTelephone());
                qq.setText(farmer.getQQ());
                weixin.setText(farmer.getWeixin());
                address.setText(farmer.getAddress());
                break;
        }
    }

    //获取用户信息
    public void getUserInfo() {

        String tag_string_req = "req_farmerInfo_get";

        if (!netutil.checkNet(getActivity())) {
            hideDialog();
            error_hint("网络连接错误");
        } else {
            //服务器请求
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_GETUSERINFO, mSuccessListener, mErrorListener) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to url
                    Map<String, String> params = new HashMap<>();
                    params.put("token", token);
                    return params;
                }
            };
            strReq.setRetryPolicy(new DefaultRetryPolicy(2000,1,1.0f)); //请求超时时间2S，重复1次
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
            //获取头像

        }
    }

    //响应服务器成功
    private Response.Listener<String> mSuccessListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            Log.i("tagconvertstr", "[" + response + "]");
            Log.d(TAG, "Release Response: " + response);
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                int status = jObj.getInt("status");

                // Check for error node in json
                if (status == 0) {
                    //解析用户数据
                    farmer = new Farmer();
                    JSONObject user = jObj.getJSONObject("result");
                    farmer.setFm_token(token);
                    farmer.setId(user.getInt("fm_id"));
                    farmer.setName(user.getString("person_name"));
                    farmer.setTelephone(user.getString("person_phone"));
                    if(user.getString("person_photo").equals("null")){
                        farmer.setImageUrl("未设置");
                    }else {
                        farmer.setImageUrl(user.getString("person_photo"));
                    }
                    if(user.getString("person_qq").equals("null")){
                        farmer.setQQ("未设置");
                    }else {
                        farmer.setQQ(user.getString("person_qq"));
                    }
                    if(user.getString("person_weixin").equals("null")){
                        farmer.setWeixin("未设置");
                    }else {
                        farmer.setWeixin(user.getString("person_weixin"));
                    }
                    if(user.getString("person_address").equals("null")){
                        farmer.setAddress("未设置");
                    }else {
                        farmer.setAddress(user.getString("person_address"));
                    }
                    updateView();
                } else if(status == 1){
                    //密匙失效
                    error_hint("用户登录过期，请重新登录！");
                    Intent intent = new Intent(getContext(), login.class);
                    startActivity(intent);
                    getActivity().finish();
                }
                else{
                    error_hint("其他未知错误！");
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
    private Response.ErrorListener mErrorListener = new Response.ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "GetPersonInfo Error: " + error.getMessage());
            error_hint("服务器连接超时");
            hideDialog();
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

    //错误信息提示1
    private void error_hint(String str) {
        Toast toast = Toast.makeText(getActivity(), str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    //错误信息提示2
    private void empty_hint(int in) {
        Toast toast = Toast.makeText(getActivity(), getResources().getString(in), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

}
