package com.njdp.njdp_farmer.conent_frament;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.njdp.njdp_farmer.MainLink;
import com.njdp.njdp_farmer.PersonalSet;
import com.njdp.njdp_farmer.R;
import com.njdp.njdp_farmer.address.AddressSelect;
import com.njdp.njdp_farmer.bean.Farmer;
import com.njdp.njdp_farmer.bean.FarmlandInfo;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.mainpages;
import com.njdp.njdp_farmer.myDialog;
import com.njdp.njdp_farmer.util.NetUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/25.
 */
public class FarmerRelease extends Fragment implements View.OnClickListener {
    private static final int ADDRESSEDIT = 2;
    private final String[] crops = new String[]{"小麦", "玉米", "水稻", "棉花", "花生", "其他"};
    private final String[] blocks = new String[]{"规则", "不规则"};
    private int mYear, mMonth, mDay, mYear1, mMonth1, mDay1;
    private FarmlandInfo farmlandInfo;
    private ProgressDialog pDialog;
    private NetUtil netutil;
    private final String TAG = "FarmerRelease";
    //所有监听的控件
    EditText croptype, area, price, blocktype, starttime, endtime, remark;
    TextView address;
    Button releaseEditFinish;
    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            if(farmlandInfo == null){
                farmlandInfo = new FarmlandInfo();
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
        view = inflater.inflate(R.layout.activity_farmer_release, null, false);
        initView(view);

        return view;
    }

    private void initView(View view) {
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);
        netutil = new NetUtil();
        croptype = (EditText) view.findViewById(R.id.crops_kind);
        area = (EditText) view.findViewById(R.id.area);
        price = (EditText) view.findViewById(R.id.price);
        blocktype = (EditText) view.findViewById(R.id.block_type);
        address = (TextView) view.findViewById(R.id.address);
        starttime = (EditText) view.findViewById(R.id.start_time);
        endtime = (EditText) view.findViewById(R.id.end_time);
        remark = (EditText) view.findViewById(R.id.remark);
        releaseEditFinish = (Button) view.findViewById(R.id.btn_editFinish);
        if(remark != null){
            remark.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }

        initOnClick();
    }

    private void initOnClick() {
        address.setOnClickListener(this);
        starttime.setOnClickListener(this);
        endtime.setOnClickListener(this);
        releaseEditFinish.setOnClickListener(this);
        croptype.setOnClickListener(new RadioClickListener());
        blocktype.setOnClickListener(new RadioClickListener());

        releaseEditFinish.setEnabled(false);
        releaseEditFinish.setClickable(false);
        editTextIsNull();
        //Thread.setOnClickListener(this);
        //myMessage.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        //1.得到InputMethodManager对象
        //InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        //2.调用toggleSoftInput方法，实现切换显示软键盘的功能。
        //imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        switch (v.getId()) {
            // TODO: 2015/11/18 头像
            case R.id.btn_editFinish:
                Log.e("------------->", "点击发布农田信息");
                checkRelease();
                break;
            case R.id.address:
                Intent intent1 = new Intent(getActivity(), AddressSelect.class);
                intent1.putExtra("address", address.getText().toString());
                startActivityForResult(intent1, ADDRESSEDIT);
                break;
            case R.id.start_time:
                getActivity().showDialog(0);
                break;
            case R.id.end_time:
                getActivity().showDialog(1);
                break;
        }
    }

    //这是跳转到另一个布局页面返回来的操作
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != -1){
            return;
        }
        switch (requestCode) {
            case ADDRESSEDIT:
                //farmer.setAddress(data.getStringExtra("address"));
                address.setText(data.getStringExtra("address"));
                break;
        }
    }

    public static Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case 1:
                    //userName.setText("立即登录");
                    //goldNumber.setVisibility(View.VISIBLE);
                    //jinbiCount.setVisibility(View.GONE);
                    //picture.setImageResource(R.mipmap.biz_tie_user_avater_default_common);
                    //flag=false;
                    break;
                case 2:
                    Bitmap bp = (Bitmap) msg.obj;
                    if (bp != null) {
                        //picture.setImageBitmap(bp);
                    }
                    break;
            }
        }
    };

    /**
     * 单选弹出菜单窗口
     *
     * @author xmz
     */
    class RadioClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder;
            switch (v.getId())
            {
                case R.id.crops_kind:
                    builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("选择作物类型");
                    int a = indexArry(crops, croptype.getText().toString()) ;
                    builder.setSingleChoiceItems(crops, a, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int position) {
                            croptype.setText(crops[position]);
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                    break;

                case R.id.block_type:
                    builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("选择地块类型");
                    int b = indexArry(blocks, blocktype.getText().toString()) ;
                    builder.setSingleChoiceItems(blocks, b, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int position) {
                            blocktype.setText(blocks[position]);
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                    break;
            }
        }
    }

    public void checkRelease() {

        String tag_string_req = "req_farmland_release";

        pDialog.setMessage("正在发布 ...");
        showDialog();

        if (netutil.checkNet(getActivity()) == false) {
            hideDialog();
            error_hint("网络连接错误");
            return;
        } else {
            if(farmlandInfo.getLongitude().length() == 0 || farmlandInfo.getLatitude().length() == 0){
                hideDialog();
                error_hint("发布失败，没有获取到有效的GPS位置信息！");
                return;
            }
            //服务器请求
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_FARMLAND_RELEASE, mSuccessListener, mErrorListener) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to url
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("token", "eyJpdiI6IlVxZkZnMDdSTEJOR0JKaXVCMUN1UlE9PSIsInZhbHVlIjoiekFhMEY0UVwvMTZRWmJzMUc2cm51WkdpWHJ6emVZdVo3b24zbDNsTWtJQXM9IiwibWFjIjoiODRhNzExNjE2NjVkNjg5YzRmYjUyY2ZiNzE4Zjg3MjA2ZTU5OThiNDE3ODZlY2VkZWI3MmU4MTczYTkyNDU4NyJ9");
                    params.put("Farmlands_crops_kind", farmlandInfo.getCrops_kind());
                    params.put("Farmlands_area", String.valueOf(farmlandInfo.getArea()));
                    params.put("Farmlands_unit_price", String.valueOf(farmlandInfo.getUnit_price()));
                    params.put("Farmlands_block_type", farmlandInfo.getBlock_type());
                    params.put("Farmlands_province", farmlandInfo.getProvince());
                    params.put("Farmlands_city", farmlandInfo.getCity());
                    params.put("Farmlands_county", farmlandInfo.getCounty());
                    params.put("Farmlands_town", farmlandInfo.getTown());
                    params.put("Farmlands_village", farmlandInfo.getVillage());
                    params.put("Farmlands_longitude", farmlandInfo.getLongitude());
                    params.put("Farmlands_Latitude", farmlandInfo.getLatitude());
                    params.put("Farmlands_street_view", farmlandInfo.getStreet_view());
                    params.put("Farmlands_start_time", farmlandInfo.yyyymmdd_DateFormat.format(farmlandInfo.getStart_time()));
                    params.put("Farmlands_end_time", farmlandInfo.yyyymmdd_DateFormat.format(farmlandInfo.getEnd_time()));
                    //params.put("status", farmlandInfo.getStatus());
                    params.put("Farmlands_remark", farmlandInfo.getRemark());
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

    //响应服务器成功
    private Response.Listener<String> mSuccessListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            Log.i("tagconvertstr", "[" + response + "]");
            Log.d(TAG, "Release Response: " + response.toString());
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    // user successfully logged in

                    // Now store the user in SQLite
                    JSONObject FarmlandsInfo = jObj.getJSONObject("FarmlandsInfo");
                    farmlandInfo.setId(FarmlandsInfo.getInt("ID"));

                    //clean frament
                    setContentNUll();

                } else {
                    empty_hint(R.string.release_error);
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
            Log.e(TAG, "Release Error: " + error.getMessage());
            error_hint("服务器连接失败");
            hideDialog();
        }
    };

    //错误信息提示1
    private void error_hint(String str){
        Toast toast = Toast.makeText(getActivity(), str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,-50);
        toast.show();
    }

    //错误信息提示2
    private void empty_hint(int in){
        Toast toast = Toast.makeText(getActivity(), getResources().getString(in), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,-50);
        toast.show();
    }

    //查找数组中的位置
    private int indexArry(String[] source, String str){
        int i = -1;
        for(String s : source)
        {
            i++;
            if(str.equals(s)){
               return i;
            }
        }
        return -1;
    }

    //清空发布界面的录入信息
    private void setContentNUll(){
        croptype.setText("");
        area.setText("");
        price.setText("");
        blocktype.setText("");
        address.setText("");
        starttime.setText("");
        endtime.setText("");
        remark.setText("");
        remark.setText("");
    }

    private Location getLocalGPS(){
        //地理位置服务提供者
        String locationProvider = "";
        //获取地理位置管理器
        LocationManager locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        //获取所有可用的位置提供器
        List<String> providers = locationManager.getProviders(true);
        if(providers.contains(LocationManager.GPS_PROVIDER)){
            //如果是GPS
            locationProvider = LocationManager.GPS_PROVIDER;
        }else if(providers.contains(LocationManager.NETWORK_PROVIDER)){
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }else{
            error_hint("没有可用的位置提供器");
            return null;
        }
        //获取并返回Location
        return locationManager.getLastKnownLocation(locationProvider);
    }

    //输入是否为空，判断是否禁用按钮
    private void editTextIsNull(){

        croptype.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(area.getText()) && !TextUtils.isEmpty(price.getText()) && !TextUtils.isEmpty(blocktype.getText())
                        && !TextUtils.isEmpty(address.getText()) && !TextUtils.isEmpty(starttime.getText()) && !TextUtils.isEmpty(endtime.getText())) {
                    releaseEditFinish.setClickable(true);
                    releaseEditFinish.setEnabled(true);
                } else {
                    releaseEditFinish.setEnabled(false);
                    releaseEditFinish.setClickable(false);
                }
                farmlandInfo.setCrops_kind(s.toString());
            }
        });

        area.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(croptype.getText()) && !TextUtils.isEmpty(price.getText()) && !TextUtils.isEmpty(blocktype.getText())
                        && !TextUtils.isEmpty(address.getText()) && !TextUtils.isEmpty(starttime.getText()) && !TextUtils.isEmpty(endtime.getText())) {
                    releaseEditFinish.setClickable(true);
                    releaseEditFinish.setEnabled(true);
                } else {
                    releaseEditFinish.setEnabled(false);
                    releaseEditFinish.setClickable(false);
                }
                if(s.length() > 0) {
                    farmlandInfo.setArea(Float.valueOf(s.toString()));
                }else {
                    farmlandInfo.setArea(0);
                }
            }
        });

        price.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(area.getText()) && !TextUtils.isEmpty(croptype.getText()) && !TextUtils.isEmpty(blocktype.getText())
                        && !TextUtils.isEmpty(address.getText()) && !TextUtils.isEmpty(starttime.getText()) && !TextUtils.isEmpty(endtime.getText())) {
                    releaseEditFinish.setClickable(true);
                    releaseEditFinish.setEnabled(true);
                } else {
                    releaseEditFinish.setEnabled(false);
                    releaseEditFinish.setClickable(false);
                }
                if(s.length() > 0) {
                    farmlandInfo.setUnit_price(Float.parseFloat(s.toString()));
                }else{
                    farmlandInfo.setUnit_price(0);
                }
            }
        });

        blocktype.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(area.getText()) && !TextUtils.isEmpty(price.getText()) && !TextUtils.isEmpty(croptype.getText())
                        && !TextUtils.isEmpty(address.getText()) && !TextUtils.isEmpty(starttime.getText()) && !TextUtils.isEmpty(endtime.getText())) {
                    releaseEditFinish.setClickable(true);
                    releaseEditFinish.setEnabled(true);
                } else {
                    releaseEditFinish.setEnabled(false);
                    releaseEditFinish.setClickable(false);
                }
                farmlandInfo.setBlock_type(s.toString());
            }
        });

        address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(area.getText()) && !TextUtils.isEmpty(price.getText()) && !TextUtils.isEmpty(blocktype.getText())
                        && !TextUtils.isEmpty(croptype.getText()) && !TextUtils.isEmpty(starttime.getText()) && !TextUtils.isEmpty(endtime.getText())) {
                    releaseEditFinish.setClickable(true);
                    releaseEditFinish.setEnabled(true);
                } else {
                    releaseEditFinish.setEnabled(false);
                    releaseEditFinish.setClickable(false);
                }
                String temp[] = s.toString().split("-");
                farmlandInfo.setProvince(temp[0]);
                farmlandInfo.setCity(temp[1]);
                farmlandInfo.setCounty(temp[2]);
                farmlandInfo.setTown(temp[3]);
                farmlandInfo.setVillage(temp[4]);
                Location location = null;
                //location = getLocalGPSByNet(s.toString());
                if(location == null) {
                    location = getLocalGPS();
                    if (location != null) {
                        farmlandInfo.setLatitude(String.valueOf(location.getLatitude()));
                        farmlandInfo.setLongitude(String.valueOf(location.getLongitude()));
                    }
                    else {
                        error_hint("获取GPS位置失败！");
                    }
                }else{
                    farmlandInfo.setLatitude(String.valueOf(location.getLatitude()));
                    farmlandInfo.setLongitude(String.valueOf(location.getLongitude()));
                }
            }
        });

        starttime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(area.getText()) && !TextUtils.isEmpty(price.getText()) && !TextUtils.isEmpty(blocktype.getText())
                        && !TextUtils.isEmpty(address.getText()) && !TextUtils.isEmpty(croptype.getText()) && !TextUtils.isEmpty(endtime.getText())) {
                    releaseEditFinish.setClickable(true);
                    releaseEditFinish.setEnabled(true);
                } else {
                    releaseEditFinish.setEnabled(false);
                    releaseEditFinish.setClickable(false);
                }
                try {
                    farmlandInfo.setStart_time(farmlandInfo.yyyymmdd_DateFormat.parse(s.toString()));
                }catch (Exception ex){

                }
            }
        });

        endtime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((s.length() > 0) && !TextUtils.isEmpty(area.getText()) && !TextUtils.isEmpty(price.getText()) && !TextUtils.isEmpty(blocktype.getText())
                        && !TextUtils.isEmpty(address.getText()) && !TextUtils.isEmpty(starttime.getText()) && !TextUtils.isEmpty(croptype.getText())) {
                    releaseEditFinish.setClickable(true);
                    releaseEditFinish.setEnabled(true);
                } else {
                    releaseEditFinish.setEnabled(false);
                    releaseEditFinish.setClickable(false);
                }
                try {
                    farmlandInfo.setEnd_time(farmlandInfo.yyyymmdd_DateFormat.parse(s.toString()));
                }catch (Exception ex){

                }
            }
        });
    }

}
