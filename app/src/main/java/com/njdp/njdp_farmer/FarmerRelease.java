package com.njdp.njdp_farmer;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
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
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.njdp.njdp_farmer.MyClass.MyDialog;
import com.njdp.njdp_farmer.address.AddressSelect;
import com.njdp.njdp_farmer.MyClass.FarmlandInfo;
import com.njdp.njdp_farmer.conent_frament.FarmlandManager;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.db.SessionManager;
import com.njdp.njdp_farmer.util.NetUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/25.
 * 农田信息发布
 */
public class FarmerRelease extends AppCompatActivity {
    //private static final int FARMLAND_EDIT = 1;
    private static final int ADDRESSEDIT = 2;
    private boolean isEdit = false;
    private String typeTitle;
    private final String[] crops = new String[]{"小麦", "玉米", "水稻", "谷物", "其他"};
    private final String[] crops1 = new String[]{"WH", "CO", "RC", "GR", "OT"};
    private final String[] cultivation = new String[]{"深松", "平地"};
    private final String[] cultivation1 = new String[]{"SS", "HA"};
    private final String[] blocks = new String[]{"规则", "不规则"};
    private String[] typeArray;
    private FarmlandInfo farmlandInfo;
    private String token;
    private ProgressDialog pDialog;
    private final String TAG = "FarmerRelease";
    private int dateFlag; //0为开始日期，1为结束日期
    //所有监听的控件
    private TextView tv1;
    private EditText croptype, area, price, blocktype, starttime, endtime, remark;
    private EditText address, addresspic;
    private RadioButton rbH, rbC, rbS;
    private Button releaseEditFinish;
    private ImageButton getback=null;
    private boolean firstSearchBaiduGPS;


    ////////////////根据地址的经纬度变量///////////////
    GeoCoder mSearch;
    MyOnGetGeoCoderResultListener myOnGetGeoCoderResultListener;

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
        setContentView(R.layout.activity_farmer_release);

        farmlandInfo = (FarmlandInfo)getIntent().getSerializableExtra("farmlandInfo");
        if (farmlandInfo == null) {
            farmlandInfo = new FarmlandInfo();
            isEdit = false;
        }else {
            isEdit = true;
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
        tv1=(TextView)this.findViewById(R.id.tv1);
        TextView top_title = (TextView) this.findViewById(R.id.tv_top_title);
        rbH=(RadioButton)this.findViewById(R.id.rbH);   //收割Harvest
        rbC=(RadioButton)this.findViewById(R.id.rbC);   //耕作Cultivation
        rbS=(RadioButton)this.findViewById(R.id.rbS);   //播种Seeding
        rbH.setChecked(true);
        croptype.setText("小麦");
        typeTitle = "选择作物类型";
        typeArray = crops;

        //如果是编辑的话，初始化数据
        if(isEdit){
            assert top_title != null;
            top_title.setText("修改需求信息");
            rbH.setClickable(false);
            rbC.setClickable(false);
            rbS.setClickable(false);
            croptype.setClickable(false);
            if(farmlandInfo.getCrops_kind().substring(0,1).equals("H")){
                rbH.setChecked(true);
                croptype.setText(crops[indexArry(crops1, farmlandInfo.getCrops_kind().substring(1,3))]);
            }else if(farmlandInfo.getCrops_kind().substring(0,1).equals("C")){
                tv1.setText("耕作类型");
                rbC.setChecked(true);
                croptype.setText(cultivation[indexArry(cultivation1, farmlandInfo.getCrops_kind().substring(1,3))]);
            } else {
                rbS.setChecked(true);
                croptype.setText(crops[indexArry(crops1, farmlandInfo.getCrops_kind().substring(1,3))]);
            }
            area.setText(String.valueOf(farmlandInfo.getArea()));
            price.setText(String.valueOf(farmlandInfo.getUnit_price()));
            blocktype.setText(farmlandInfo.getBlock_type());
            address.setText(farmlandInfo.getProvince() + "-" + farmlandInfo.getCity() + "-" + farmlandInfo.getCounty() + "-" +
                            farmlandInfo.getTown() + "-" + farmlandInfo.getVillage());
            starttime.setText(farmlandInfo.getStart_time_String());
            endtime.setText(farmlandInfo.getEnd_time_String());
            if(farmlandInfo.getStreet_view().equals("null")){
                addresspic.setText("");
            }else {
                addresspic.setText(farmlandInfo.getStreet_view());
            }
            remark.setText(farmlandInfo.getRemark());
            releaseEditFinish.setText("确认修改");
        }

        initOnClick();
    }

    private void initOnClick() {
        address.setOnClickListener(handler);
        addresspic.setOnClickListener(handler);
        starttime.setOnClickListener(handler);
        endtime.setOnClickListener(handler);
        releaseEditFinish.setOnClickListener(handler);
        getback.setOnClickListener(handler);
        blocktype.setOnClickListener(new RadioClickListener());
        if(!isEdit) {
            croptype.setOnClickListener(new RadioClickListener());
            rbH.setOnClickListener(new RadioClickListener());
            rbC.setOnClickListener(new RadioClickListener());
            rbS.setOnClickListener(new RadioClickListener());
        }

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
                // TODO: 根据点击进行不同的处理
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
                    // 选择收割日期操作
                    dateFlag = 0;
                    MyDialog dialogFragment = MyDialog.newInstance(
                            "选择开始日期", "农田发布");
                    dialogFragment.show(getFragmentManager(), "选择日期");
                    break;
                case R.id.end_time:
                    // 选择收割日期操作
                    dateFlag = 1;
                    MyDialog dialogFragment1 = MyDialog.newInstance(
                            "选择结束日期", "农田发布");
                    dialogFragment1.show(getFragmentManager(), "选择日期");
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
                    builder.setTitle(typeTitle);
                    int a = indexArry(typeArray, croptype.getText().toString());
                    builder.setSingleChoiceItems(typeArray, a, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int position) {
                            croptype.setText(typeArray[position]);
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
                case R.id.rbH:
                case R.id.rbS:
                    tv1.setText("作物类型");
                    typeTitle = "选择作物类型";
                    typeArray = crops;
                    croptype.setText("小麦");
                    croptype.setHint("请选择农作物种类");
                    break;
                case R.id.rbC:
                    tv1.setText("耕作类型");
                    typeTitle = "选择耕作类型";
                    typeArray = cultivation;
                    croptype.setText("深松");
                    croptype.setHint("请选择耕作类型");
                    break;
            }
        }
    }

    public void checkRelease() {

        String tag_string_req = "req_farmland_release";

        pDialog.setMessage("正在发布 ...");
        showDialog();
        Log.i("GGGG", farmlandInfo.getLongitude());
        if (!NetUtil.checkNet(this)) {
            hideDialog();
            error_hint("网络连接错误");
        } else {

            if (farmlandInfo.getLongitude().length() == 0 || farmlandInfo.getLatitude().length() == 0) {
                hideDialog();
                error_hint("发布失败，没有获取到有效的GPS位置信息！");
                return;
            }
            String ReqUrl; //需要连接的URL
            if(isEdit){
                ReqUrl = AppConfig.URL_FARMLAND_EDIT;
            }else {
                ReqUrl = AppConfig.URL_FARMLAND_RELEASE;
            }
            //服务器请求
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    ReqUrl, mSuccessListener, mErrorListener) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to url
                    Map<String, String> params = new HashMap<>();
                    params.put("token", token);
                    if(isEdit){
                        params.put("id", String.valueOf(farmlandInfo.getId()));
                    }
                    String crops_kind;
                    if(rbH.isChecked()){
                        crops_kind = "H";
                        crops_kind += crops1[indexArry(typeArray, croptype.getText().toString())];
                    }else if(rbS.isChecked()){
                        crops_kind = "S";
                        crops_kind += crops1[indexArry(typeArray, croptype.getText().toString())];
                    }else {
                        crops_kind = "C";
                        crops_kind += cultivation1[indexArry(typeArray, croptype.getText().toString())];
                    }
                    farmlandInfo.setCrops_kind(crops_kind);
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
                    params.put("Farmlands_remark", farmlandInfo.getRemark());
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
                    // user successfully logged in
                    if(isEdit)
                        error_hint("修改成功！");
                    else
                        error_hint("发布成功！");
                    //clean frament
                    setContentNUll();

                } else if(status == 3){
                    //密匙失效
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(FarmerRelease.this, login.class);
                    startActivity(intent);
                    finish();
                }
                else if(status == 4){
                    //密匙不存在
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(FarmerRelease.this, login.class);
                    startActivity(intent);
                    finish();
                }else if(status == 15){
                    error_hint("农田正在收割或已收割，不能进行修改！");
                } else{
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
        Toast toast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    //错误信息提示2
    private void empty_hint(int in) {
        Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(in), Toast.LENGTH_LONG);
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
        //返回结果
        Intent intent = new Intent(FarmerRelease.this, FarmlandManager.class);
        if(isEdit){
            intent = new Intent(FarmerRelease.this, FarmerLandList.class);
            intent.putExtra("farmlandInfo", farmlandInfo);
        }
        setResult(RESULT_OK, intent);
        //清空数据
        //rbH.setChecked(true);
        //typeTitle = "选择作物类型";
        //typeArray = crops;
        //croptype.setText("");
        //area.setText("");
        //price.setText("");
        //blocktype.setText("");
        //address.setText("");
        //starttime.setText("");
        //endtime.setText("");
        //remark.setText(""); //街景暂未考虑
        //remark.setText("");

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
                    firstSearchBaiduGPS = true;
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

        remark.addTextChangedListener(new TextWatcher() {
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
                farmlandInfo.setRemark(s.toString());
            }
        });
    }


    ////////////////////////根据地址获取经纬度代码////////////////////////////
    class MyOnGetGeoCoderResultListener implements OnGetGeoCoderResultListener{
        @Override
        public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
            if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                //有时因为村名包含“村”字而搜索不到位置，进行二次搜索
                if(firstSearchBaiduGPS){
                    firstSearchBaiduGPS = false;
                    mSearch.geocode(new GeoCodeOption().city(farmlandInfo.getCity()).address(farmlandInfo.getVillage().substring(0,farmlandInfo.getVillage().length()-1)));
                    return;
                }
                //未找到村庄位置，显示提示信息
                new AlertDialog.Builder(FarmerRelease.this)
                        .setTitle("系统提示")
                        .setMessage("未能找到村庄位置，请确认输入是否正确，否则将要定位本地位置。")
                        .setIcon(R.drawable.ic_dialog_info)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“确认”后的操作
                                //获取地址经纬度失败，获取本地GPS经纬度
                                Location location = getLocalGPS();
                                // 将GPS设备采集的原始GPS坐标转换成百度坐标
                                CoordinateConverter converter  = new CoordinateConverter();
                                converter.from(CoordinateConverter.CoordType.GPS);
                                // sourceLatLng待转换坐标
                                assert location != null;
                                converter.coord(new LatLng(location.getLatitude(), location.getLongitude()));
                                LatLng point = converter.convert();
                                farmlandInfo.setLatitude(String.valueOf(point.latitude));
                                farmlandInfo.setLongitude(String.valueOf(point.longitude));
                            }
                        })
                        .setNegativeButton("返回", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“返回”后的操作,这里返回到地址录入界面
                                Intent intent = new Intent(FarmerRelease.this, AddressSelect.class);
                                intent.putExtra("address", address.getText().toString());
                                startActivityForResult(intent, ADDRESSEDIT);
                            }
                        }).show();
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

    public void selectDate() {
        //---选择日期---
        Calendar c = Calendar.getInstance();
        if(dateFlag == 0){
            if (null == starttime) {
                starttime = (EditText) findViewById(R.id.start_time);
            }
            assert starttime != null;
            if(starttime.getText().toString().length() == 10){
                String[] temp = starttime.getText().toString().split("-");
                c.set(Integer.parseInt(temp[0]), Integer.parseInt(temp[1])-1, Integer.parseInt(temp[2]));
            }
        }else {
            if (null == endtime) {
                endtime = (EditText) findViewById(R.id.end_time);
            }
            assert endtime != null;
            if(endtime.getText().toString().length() == 10){
                String[] temp = endtime.getText().toString().split("-");
                c.set(Integer.parseInt(temp[0]), Integer.parseInt(temp[1])-1, Integer.parseInt(temp[2]));
            }
        }
        new DatePickerDialog(this, mDateSetListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)).show();
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
            if(dateFlag == 0) {
                if (null == starttime) {
                    starttime = (EditText) findViewById(R.id.start_time);
                }
                if (null != starttime){
                    starttime.setText(yyyy + "-" + mm + "-" + dd);
                }
            }else{
                if(null == endtime) {
                    endtime = (EditText) findViewById(R.id.end_time);
                }
                if(null != endtime){
                    endtime.setText(yyyy + "-" + mm + "-" + dd);
                }
            }
        }
    };

    //不跟随系统变化字体大小
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config=new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config,res.getDisplayMetrics() );
        return res;
    }

}
