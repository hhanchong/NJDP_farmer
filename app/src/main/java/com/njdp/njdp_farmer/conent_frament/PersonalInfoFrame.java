package com.njdp.njdp_farmer.conent_frament;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.njdp.njdp_farmer.FarmerLandList;
import com.njdp.njdp_farmer.PersonalSet;
import com.njdp.njdp_farmer.R;
import com.njdp.njdp_farmer.bean.Farmer;
import com.njdp.njdp_farmer.bean.FarmlandInfo;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.login;
import com.njdp.njdp_farmer.util.NetUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

public class PersonalInfoFrame extends Fragment implements View.OnClickListener {
    private final String TAG = "PersonalInfoFrame";
    //所有监听的控件
    static ImageView userImage;
    TextView userName, telephone, qq, weixin, address, myrelease;
    Button personalEdit;
    View view;
    private ProgressDialog pDialog;
    private NetUtil netutil = new NetUtil();
    private String token;
    private Farmer farmer;
    ArrayList<FarmlandInfo> farmlandInfos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            Bundle bundle = getArguments();
            token = bundle.getString("token");
            farmer = (Farmer)bundle.getSerializable("farmer");
            //自测用户
            farmer = new Farmer();
            farmer.setName("李占伟");
            farmer.setImageUrl("@drawable/ic_launcher");
            farmer.setTelephone("18932659760");
            farmer.setQQ("842558891");
            farmer.setWeixin("ZhiHuiNongJi");
            farmer.setAddress("河北省保定市清苑县***乡***村");
            //判断参数传递是否正确
            if (token == null || farmer == null) {
                error_hint("参数传递错误！");
                return null;
            }
            if (view == null) {
                view = inFlater(inflater);
            }
            return view;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public View inFlater(LayoutInflater inflater) {
        view = inflater.inflate(R.layout.activity_personal_info, null, false);
        initView(view);
        farmlandInfos = new ArrayList<FarmlandInfo>();
        getFarmlandInfos();
        return view;
    }

    private void initView(View view) {
        userImage = (ImageView) view.findViewById(R.id.user_image);
        //userImage.setImageURI();
        userName = (TextView) view.findViewById(R.id.tv_user_name);
        userName.setText(farmer.getName());
        telephone = (TextView) view.findViewById(R.id.tv_phonenum);
        telephone.setText(farmer.getTelephone());
        qq = (TextView) view.findViewById(R.id.tv_qq);
        qq.setText(farmer.getQQ());
        weixin = (TextView) view.findViewById(R.id.tv_weixin);
        weixin.setText(farmer.getWeixin());
        address = (TextView) view.findViewById(R.id.tv_address);
        address.setText(farmer.getAddress());
        myrelease = (TextView) view.findViewById(R.id.tv_my_release);
        personalEdit = (Button) view.findViewById(R.id.btn_edit);
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        initOnClick();
    }

    private void initOnClick() {
        personalEdit.setOnClickListener(this);
        myrelease.setOnClickListener(this);
        //Thread.setOnClickListener(this);
        //myMessage.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // TODO: 2015/11/18 头像
            case R.id.btn_edit:
                Log.e("------------->", "点击修改用户信息");
                Intent intent1 = new Intent(getActivity(), PersonalSet.class);
                intent1.putExtra("user", farmer);
                startActivity(intent1);
                break;
            case R.id.tv_my_release:
                Log.e("------------->", "查看我的发布信息");
                Intent intent2 = new Intent(getActivity(), FarmerLandList.class);
                intent2.putExtra("farmlandInfos", farmlandInfos);
                startActivity(intent2);
                break;
        }
    }
    public static Handler handle=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case 1:
                    //userName.setText("立即登录");
                    //goldNumber.setVisibility(View.VISIBLE);
                    //jinbiCount.setVisibility(View.GONE);
                    //picture.setImageResource(R.mipmap.biz_tie_user_avater_default_common);
                    //flag=false;
                    break;
                case 2:
                    Bitmap bp= (Bitmap) msg.obj;
                    if (bp!=null){
                        //picture.setImageBitmap(bp);
                    }
                    break;
            }
        }
    };

    public void getFarmlandInfos() {

        String tag_string_req = "req_farmland_get";

        pDialog.setMessage("正在获取个人数据 ...");
        showDialog();

        if (netutil.checkNet(getActivity()) == false) {
            hideDialog();
            error_hint("网络连接错误");
            return;
        } else {
            //服务器请求
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_FARMLAND_GET, mSuccessListener, mErrorListener) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to url
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("token", token);
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
            Log.d(TAG, "Release Response: " + response.toString());
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                int status = jObj.getInt("status");

                // Check for error node in json
                if (status == 0) {
                    //此处引入JSON jar包
                    JSONArray jObjs = jObj.getJSONArray("result");
                    for(int i = 0; i < jObjs.length(); i++){
                        FarmlandInfo temp = new FarmlandInfo();
                        JSONObject object = (JSONObject)jObjs.opt(i);
                        temp.setId(object.getInt("id"));
                        temp.setCrops_kind(object.getString("Farmlands_crops_kind"));
                        temp.setArea(Float.parseFloat(object.getString("Farmlands_area")));
                        temp.setUnit_price(Float.parseFloat(object.getString("Farmlands_unit_price")));
                        temp.setBlock_type(object.getString("Farmlands_block_type"));
                        temp.setProvince(object.getString("Farmlands_province"));
                        temp.setCity(object.getString("Farmlands_city"));
                        temp.setCounty(object.getString("Farmlands_county"));
                        temp.setTown(object.getString("Farmlands_town"));
                        temp.setVillage(object.getString("Farmlands_village"));
                        temp.setLongitude(object.getString("Farmlands_longitude"));
                        temp.setLatitude(object.getString("Farmlands_Latitude"));
                        temp.setStreet_view(object.getString("Farmlands_street_view"));
                        temp.setStart_time(object.getString("Farmlands_start_time"));
                        temp.setEnd_time(object.getString("Farmlands_end_time"));
                        temp.setStatus(object.getString("Farmlands_status"));
                        temp.setRemark(object.getString("Farmlands_remark"));
                        temp.setCreatetime(object.getString("created_at"));
                        temp.setUpdatetime(object.getString("updated_at"));
                        farmlandInfos.add(temp);
                    }
                    myrelease.setText("共发布了" + jObjs.length() + "条信息，点击查看");

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
            Log.e(TAG, "Release Error: " + error.getMessage());
            error_hint("服务器连接失败");
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
