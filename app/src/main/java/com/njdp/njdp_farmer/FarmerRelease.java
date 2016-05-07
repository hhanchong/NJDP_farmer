package com.njdp.njdp_farmer;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.njdp.njdp_farmer.address.AddressSelect;
import com.njdp.njdp_farmer.MyClass.FarmlandInfo;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
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
public class FarmerRelease extends AppCompatActivity {
    private static final int ADDRESSEDIT = 2;
    private final String[] crops = new String[]{"小麦", "玉米", "水稻", "棉花", "花生", "其他"};
    private final String[] blocks = new String[]{"规则", "不规则"};
    private int mYear, mMonth, mDay, mYear1, mMonth1, mDay1;
    private FarmlandInfo farmlandInfo;
    private String token;
    private ProgressDialog pDialog;
    private NetUtil netutil;
    private final String TAG = "FarmerRelease";
    //所有监听的控件
    private EditText croptype, area, price, blocktype, starttime, endtime, remark;
    private EditText address, addresspic;
    private Button releaseEditFinish;
    private ImageButton getback=null;


    ////////////////根据地址的经纬度变量///////////////
    GeoCoder mSearch;
    MyOnGetGeoCoderResultListener myOnGetGeoCoderResultListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_release);

        if (farmlandInfo == null) {
            farmlandInfo = new FarmlandInfo();
        }

        token = getIntent().getStringExtra("token");
        if (token == null) {
            error_hint("参数传递错误！");
            finish();
        }
        initView();

        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        myOnGetGeoCoderResultListener = new MyOnGetGeoCoderResultListener();
        mSearch.setOnGetGeoCodeResultListener(new MyOnGetGeoCoderResultListener());
    }

    private void initView() {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        netutil = new NetUtil();
        croptype = (EditText) this.findViewById(R.id.crops_kind);
        area = (EditText) this.findViewById(R.id.area);
        price = (EditText) this.findViewById(R.id.price);
        blocktype = (EditText) this.findViewById(R.id.block_type);
        address = (EditText) this.findViewById(R.id.address);
        addresspic = (EditText) this.findViewById(R.id.addresspic);
        starttime = (EditText) this.findViewById(R.id.start_time);
        endtime = (EditText) this.findViewById(R.id.end_time);
        remark = (EditText) this.findViewById(R.id.remark);
        releaseEditFinish = (Button) this.findViewById(R.id.btn_editFinish);
        if (remark != null) {
            remark.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
        getback=(ImageButton) this.findViewById(R.id.getback);

        initOnClick();
    }

    private void initOnClick() {
        address.setOnClickListener(handler);
        addresspic.setOnClickListener(handler);
        starttime.setOnClickListener(handler);
        endtime.setOnClickListener(handler);
        releaseEditFinish.setOnClickListener(handler);
        getback.setOnClickListener(handler);
        croptype.setOnClickListener(new RadioClickListener());
        blocktype.setOnClickListener(new RadioClickListener());

        releaseEditFinish.setEnabled(false);
        releaseEditFinish.setClickable(false);
        editTextIsNull();
    }

    View.OnClickListener handler = new View.OnClickListener()
    {
        public void onClick (View v) {
            //1.得到InputMethodManager对象
            //InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            //2.调用toggleSoftInput方法，实现切换显示软键盘的功能。
            //imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            switch (v.getId()) {
                // TODO: 2015/11/18 头像
                case R.id.btn_editFinish:
                    Log.e("------------->", "点击发布农田信息");
                    checkRelease();
                    break;
                case R.id.address:
                    Intent intent1 = new Intent(FarmerRelease.this, AddressSelect.class);
                    intent1.putExtra("address", address.getText().toString());
                    startActivityForResult(intent1, ADDRESSEDIT);
                    break;
                case R.id.addresspic:

                    break;
                case R.id.start_time:
                    showDialog(0);
                    break;
                case R.id.end_time:
                    showDialog(1);
                    break;
                case R.id.getback:
                    finish();
                    break;
            }
        }
    };


    //这是跳转到另一个布局页面返回来的操作
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != -1) {
            return;
        }
        switch (requestCode) {
            case ADDRESSEDIT:
                address.setText(data.getStringExtra("address"));
                break;
        }
    }

    /**
     * 单选弹出菜单窗口
     *
     * @author xmz
     */
    class RadioClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder;
            switch (v.getId()) {
                case R.id.crops_kind:
                    builder = new AlertDialog.Builder(FarmerRelease.this);
                    builder.setTitle("选择作物类型");
                    int a = indexArry(crops, croptype.getText().toString());
                    builder.setSingleChoiceItems(crops, a, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int position) {
                            croptype.setText(crops[position]);
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                    break;

                case R.id.block_type:
                    builder = new AlertDialog.Builder(FarmerRelease.this);
                    builder.setTitle("选择地块类型");
                    int b = indexArry(blocks, blocktype.getText().toString());
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
        Log.i("GGGG", farmlandInfo.getLongitude());
        if (!netutil.checkNet(this)) {
            hideDialog();
            error_hint("网络连接错误");
        } else {

            if (farmlandInfo.getLongitude().length() == 0 || farmlandInfo.getLatitude().length() == 0) {
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
                    Map<String, String> params = new HashMap<>();
                    params.put("token", token);
                    params.put("Farmlands_crops_kind", farmlandInfo.getCrops_kind());
                    params.put("Farmlands_area", String.valueOf(farmlandInfo.getArea()));
                    params.put("Farmlands_unit_price", String.valueOf(farmlandInfo.getUnit_price()));
                    if(farmlandInfo.getBlock_type().length() == 0){
                        farmlandInfo.setBlock_type("规则");
                    }
                    params.put("Farmlands_block_type", farmlandInfo.getBlock_type());
                    params.put("Farmlands_province", farmlandInfo.getProvince());
                    params.put("Farmlands_city", farmlandInfo.getCity());
                    params.put("Farmlands_county", farmlandInfo.getCounty());
                    params.put("Farmlands_town", farmlandInfo.getTown());
                    params.put("Farmlands_village", farmlandInfo.getVillage());
                    params.put("Farmlands_longitude", farmlandInfo.getLongitude());
                    params.put("Farmlands_Latitude", farmlandInfo.getLatitude());
                    params.put("Farmlands_street_view", farmlandInfo.getStreet_view());
                    params.put("Farmlands_start_time", farmlandInfo.getStart_time_String());
                    params.put("Farmlands_end_time", farmlandInfo.getEnd_time_String());
                    //params.put("status", farmlandInfo.getStatus());
                    params.put("Farmlands_remark", remark.getText().toString());
                    return params;
                }
            };
            strReq.setRetryPolicy(new DefaultRetryPolicy(2000,1,1.0f)); //请求超时时间2S，重复1次
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
            Log.d(TAG, "Release Response: " + response);
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                int status = jObj.getInt("status");

                // Check for error node in json
                if (status == 0) {
                    // user successfully logged in
                    error_hint("发布成功！");
                    //clean frament
                    setContentNUll();

                } else if(status == 1){
                    //密匙失效
                    error_hint("用户登录过期，请重新登录！");
                    Intent intent = new Intent(FarmerRelease.this, login.class);
                    startActivity(intent);
                    finish();
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

    //错误信息提示1
    private void error_hint(String str) {
        Toast toast = Toast.makeText(FarmerRelease.this, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    //错误信息提示2
    private void empty_hint(int in) {
        Toast toast = Toast.makeText(FarmerRelease.this, getResources().getString(in), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    //查找数组中的位置
    private int indexArry(String[] source, String str) {
        int i = -1;
        for (String s : source) {
            i++;
            if (str.equals(s)) {
                return i;
            }
        }
        return -1;
    }

    //清空发布界面的录入信息
    private void setContentNUll() {
        croptype.setText("");
        area.setText("");
        price.setText("");
        blocktype.setText("");
        address.setText("");
        starttime.setText("");
        endtime.setText("");
        //remark.setText(""); //街景暂未考虑
        remark.setText("");
        finish();
    }

    private Location getLocalGPS() {
        //地理位置服务提供者
        String locationProvider;
        //获取地理位置管理器
        LocationManager locationManager = (LocationManager) FarmerRelease.this.getSystemService(Context.LOCATION_SERVICE);
        //获取所有可用的位置提供器
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            error_hint("没有可用的位置提供器，请检查GPS是否打开。");
            return null;
        }
        //获取并返回Location
        if (ActivityCompat.checkSelfPermission(FarmerRelease.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            error_hint("请开放GPS定位的权限。");
            return null;
        }
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
                if ((s.length() > 0) && !TextUtils.isEmpty(area.getText()) && !TextUtils.isEmpty(price.getText()) && !TextUtils.isEmpty(address.getText())
                        && !TextUtils.isEmpty(starttime.getText()) && !TextUtils.isEmpty(endtime.getText())) {
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
                if ((s.length() > 0) && !TextUtils.isEmpty(croptype.getText()) && !TextUtils.isEmpty(price.getText()) && !TextUtils.isEmpty(address.getText())
                         && !TextUtils.isEmpty(starttime.getText()) && !TextUtils.isEmpty(endtime.getText())) {
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
                if ((s.length() > 0) && !TextUtils.isEmpty(area.getText()) && !TextUtils.isEmpty(croptype.getText()) && !TextUtils.isEmpty(address.getText())
                         && !TextUtils.isEmpty(starttime.getText()) && !TextUtils.isEmpty(endtime.getText())) {
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
                //if ((s.length() > 0) && !TextUtils.isEmpty(area.getText()) && !TextUtils.isEmpty(price.getText()) && !TextUtils.isEmpty(croptype.getText())
                //        && !TextUtils.isEmpty(address.getText()) && !TextUtils.isEmpty(starttime.getText()) && !TextUtils.isEmpty(endtime.getText())) {
                //    releaseEditFinish.setClickable(true);
                //    releaseEditFinish.setEnabled(true);
                //} else {
                //    releaseEditFinish.setEnabled(false);
                //    releaseEditFinish.setClickable(false);
                //}
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
                if ((s.length() > 0) && !TextUtils.isEmpty(area.getText()) && !TextUtils.isEmpty(price.getText()) && !TextUtils.isEmpty(croptype.getText())
                         && !TextUtils.isEmpty(starttime.getText()) && !TextUtils.isEmpty(endtime.getText())) {
                    releaseEditFinish.setClickable(true);
                    releaseEditFinish.setEnabled(true);
                } else {
                    releaseEditFinish.setEnabled(false);
                    releaseEditFinish.setClickable(false);
                }
                String temp[] = s.toString().split("-");
                if(temp.length > 4) {
                    farmlandInfo.setProvince(temp[0]);
                    farmlandInfo.setCity(temp[1]);
                    farmlandInfo.setCounty(temp[2]);
                    farmlandInfo.setTown(temp[3]);
                    farmlandInfo.setVillage(temp[4]);

                    //通过村名查找坐标位置
                    mSearch.geocode(new GeoCodeOption().city(farmlandInfo.getCity()).address(farmlandInfo.getVillage()));
                }
            }
        });

        starttime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().length() == 0) {
                    return;
                }
                if(!TextUtils.isEmpty(endtime.getText())) {
                    if (farmlandInfo.StringFormatDate(s.toString()).getTime() > farmlandInfo.getEnd_time().getTime()) {
                        error_hint("开始时间不能晚于结束时间！");
                        if (farmlandInfo.getEnd_time().getTime() < farmlandInfo.getStart_time().getTime()) {
                            starttime.setText(farmlandInfo.getEnd_time_String());
                            farmlandInfo.setStart_time(farmlandInfo.getEnd_time());
                        } else {
                            if(farmlandInfo.getStart_time().getTime() == farmlandInfo.StringFormatDate("1900-01-01").getTime()){
                                starttime.setText(farmlandInfo.getEnd_time_String());
                                farmlandInfo.setStart_time(farmlandInfo.getEnd_time());
                            }else {
                                starttime.setText(farmlandInfo.getStart_time_String());
                            }
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if ((s.length() > 0) && !TextUtils.isEmpty(area.getText()) && !TextUtils.isEmpty(price.getText()) && !TextUtils.isEmpty(address.getText())
                         && !TextUtils.isEmpty(croptype.getText()) && !TextUtils.isEmpty(endtime.getText())) {
                    releaseEditFinish.setClickable(true);
                    releaseEditFinish.setEnabled(true);
                } else {
                    releaseEditFinish.setEnabled(false);
                    releaseEditFinish.setClickable(false);
                }
                if(s.toString().length() > 0) {
                    farmlandInfo.setStart_time(farmlandInfo.StringFormatDate(s.toString()));
                }
            }
        });

        endtime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().length() == 0) {
                    return;
                }
                if(!TextUtils.isEmpty(starttime.getText())){
                    if(farmlandInfo.StringFormatDate(s.toString()).getTime() < farmlandInfo.getStart_time().getTime()){
                        error_hint("结束时间不能早于开始时间！");
                        if(farmlandInfo.getEnd_time().getTime() < farmlandInfo.getStart_time().getTime()) {
                            endtime.setText(farmlandInfo.getStart_time_String());
                            farmlandInfo.setEnd_time(farmlandInfo.getStart_time());
                        } else {
                            endtime.setText(farmlandInfo.getEnd_time_String());
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                if ((s.length() > 0) && !TextUtils.isEmpty(area.getText()) && !TextUtils.isEmpty(price.getText())  && !TextUtils.isEmpty(address.getText())
                        && !TextUtils.isEmpty(starttime.getText()) && !TextUtils.isEmpty(croptype.getText())) {
                    releaseEditFinish.setClickable(true);
                    releaseEditFinish.setEnabled(true);
                } else {
                    releaseEditFinish.setEnabled(false);
                    releaseEditFinish.setClickable(false);
                    //releaseEditFinish.setBackground("#");
                }
                if(s.toString().length() > 0){
                    farmlandInfo.setEnd_time(farmlandInfo.StringFormatDate(s.toString()));
                }
            }
        });
    }


    ////////////////////////根据地址获取经纬度代码////////////////////////////
    class MyOnGetGeoCoderResultListener implements OnGetGeoCoderResultListener{
        @Override
        public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
            if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(FarmerRelease.this, "抱歉，未能找到村庄位置，将要获取本地位置！", Toast.LENGTH_LONG)
                        .show();
                //获取地址经纬度失败，获取本地GPS经纬度
                Location location = getLocalGPS();
                if (location != null) {
                    farmlandInfo.setLatitude(String.valueOf(location.getLatitude()));
                    farmlandInfo.setLongitude(String.valueOf(location.getLongitude()));
                }
                else {
                    error_hint("获取本地GPS位置失败！");
                }
                return;
            }
            farmlandInfo.setLatitude(String.valueOf(geoCodeResult.getLocation().latitude));
            farmlandInfo.setLongitude(String.valueOf(geoCodeResult.getLocation().longitude));
            Log.i("ccccccccccc","纬度"+String.valueOf(farmlandInfo.getLatitude()+"经度"+farmlandInfo.getLongitude()));
        }

        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        final Calendar c = Calendar.getInstance();

        switch (id) {
            case 0:
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
                        mDay);
            case 1:
                mYear = 0;
                mMonth = 0;
                mDay = 0;
                mYear1 = c.get(Calendar.YEAR);
                mMonth1 = c.get(Calendar.MONTH);
                mDay1 = c.get(Calendar.DAY_OF_MONTH);
                return new DatePickerDialog(this, mDateSetListener, mYear1, mMonth1,
                        mDay1);
        }
        return null;
    }

    private final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            String yyyy = String.valueOf(year);
            String mm;
            String dd;

            mm = String.valueOf(monthOfYear + 1);
            if (mm.length() < 2)
                mm = "0" + mm;

            dd = String.valueOf(dayOfMonth);
            if (dd.length() < 2)
                dd = "0" + dd;
            if(mYear > 0) {
                if (null == starttime) {
                    starttime = (EditText) findViewById(R.id.start_time);
                }
                if (null != starttime){
                    starttime.setText(yyyy + "-" + mm + "-" + dd);
                }
                removeDialog(0);
            }else{
                if(null == endtime) {
                    endtime = (EditText) findViewById(R.id.end_time);
                }
                if(null != endtime){
                    endtime.setText(yyyy + "-" + mm + "-" + dd);
                }
                removeDialog(1);
            }
        }
    };

}
