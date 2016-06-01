package com.njdp.njdp_farmer.conent_frament;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.njdp.njdp_farmer.FarmerLandList;
import com.njdp.njdp_farmer.FarmerRelease;
import com.njdp.njdp_farmer.MyClass.Farmer;
import com.njdp.njdp_farmer.MyClass.FarmlandInfo;
import com.njdp.njdp_farmer.R;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.db.SessionManager;
import com.njdp.njdp_farmer.login;
import com.njdp.njdp_farmer.mainpages;
import com.njdp.njdp_farmer.util.NetUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Administrator on 2016/5/6.
 */
public class FarmlandManager extends Fragment implements View.OnClickListener {
    private final String TAG = "FarmLandManager";
    private final int FARM_RELEASE = 1;
    private Button myrelease, newrelease;
    private View view;
    private String token;
    private ProgressDialog pDialog;
    private static ArrayList<FarmlandInfo> farmlandInfos;
    private boolean isFirst = true;
    private boolean isRefreshData = false;
    private Handler handler;
    private Runnable runnable;

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
            if (view == null) {
                view = inFlater(inflater);
            }
            //定时刷新任务
            //handler = new Handler();
            //runnable = new Runnable(){
            //    @Override
            //    public void run() {
                    // 在此处添加执行的代码
            //        getFarmlandInfos();
            //        handler.postDelayed(this, 30000);// 30s后执行this，即runable
            //    }
            //};
            //handler.postDelayed(runnable, 30000);// 打开定时器，30s后执行runnable操作
            return view;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public View inFlater(LayoutInflater inflater) {
        view = inflater.inflate(R.layout.activity_farmerland_manager, null, false);
        initView(view);
        farmlandInfos = new ArrayList<>();
        getFarmlandInfos();
        return view;
    }

    private void initView(View view) {
        myrelease = (Button) view.findViewById(R.id.bt_my_release);
        newrelease = (Button) view.findViewById(R.id.bt_new_release);
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        initOnClick();
    }

    private void initOnClick() {
        myrelease.setOnClickListener(this);
        newrelease.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_my_release:
                Log.e("------------->", "点击查看发布的信息");
                Intent intent1 = new Intent(getActivity(), FarmerLandList.class);
                intent1.putExtra("token", token);
                startActivity(intent1);
                isRefreshData = true;
                break;
            case R.id.bt_new_release:
                Log.e("------------->", "新建我的发布信息");
                Intent intent2 = new Intent(getActivity(), FarmerRelease.class);
                intent2.putExtra("token", token);
                startActivityForResult(intent2, FARM_RELEASE);
                break;
        }
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
            case FARM_RELEASE:
                getFarmlandInfos();
                break;
        }
    }

    //获取发布的农田信息
    public void getFarmlandInfos() {

        String tag_string_req = "req_farmland_get";

        if(isFirst) {
            pDialog.setMessage("正在获取发布农田数据 ...");
            showDialog();
            isFirst = false;
        }

        if (!NetUtil.checkNet(getActivity())) {
            hideDialog();
            error_hint("网络连接错误");
        } else {
            //服务器请求
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_FARMLAND_GET, mSuccessListener, mErrorListener) {

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
                    //清空旧数据
                    farmlandInfos.clear();
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
                    //myrelease.setText("共发布了" + jObjs.length() + "条信息，点击查看");
                    if(farmlandInfos.size() > 0){
                        returnLastReleaseUndo();
                    }

                } else if(status == 3){
                    //密匙失效
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getActivity().getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(getActivity(), login.class);
                    startActivity(intent);
                    getActivity().finish();
                }
                else if(status == 4){
                    //密匙不存在
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getActivity().getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(getActivity(), login.class);
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
            Log.e(TAG, "GetFarmLandInfo Error: " + error.getMessage());
            error_hint("服务器连接超时");
            hideDialog();
        }
    };

    //返回最后的未完成的发布信息
    private void returnLastReleaseUndo(){
        for(int i = farmlandInfos.size()-1; i >= 0; i--){
            if(farmlandInfos.get(i).getStatus().equals("0")){
                if(farmlandInfos.get(i).getEnd_time().getTime() >= System.currentTimeMillis()){
                    ((mainpages)getActivity()).setLastUndoFarmland(farmlandInfos.get(i));
                    return;
                }
            }
        }
        ((mainpages)getActivity()).setLastUndoFarmland(null);
    }

    //获取农田信息，与农田列表界面交互farmerLandList
    public static ArrayList<FarmlandInfo> getFarmlands() {
        return farmlandInfos;
    }

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

    @Override
    public void onResume() {
        if(isRefreshData) {
            Log.w(TAG, "重新获取农田数据");
            getFarmlandInfos();
            isRefreshData = false;
        }
        super.onResume();
    }

    @Override
    public void onDestroy(){
        //handler.removeCallbacks(runnable);// 关闭定时器处理
        super.onDestroy();
    }

}
