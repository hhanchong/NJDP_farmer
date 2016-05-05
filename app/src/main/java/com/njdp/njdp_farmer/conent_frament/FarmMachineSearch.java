package com.njdp.njdp_farmer.conent_frament;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.njdp.njdp_farmer.R;
import com.njdp.njdp_farmer.MyClass.FarmlandInfo;
import com.njdp.njdp_farmer.MyClass.MachineInfo;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.login;
import com.njdp.njdp_farmer.mainpages;
import com.njdp.njdp_farmer.util.NetUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FarmMachineSearch extends Fragment implements View.OnClickListener {
    private final String TAG = "MachineInfoFrame";
    private String token;
    private View view;
    private int width,height;
    private ProgressDialog pDialog;
    private NetUtil netutil = new NetUtil();
    private RelativeLayout test_pop_layout;
    private RadioButton rb5, rb10, rb20, rb30, rb50;//距离现则按钮
    private static FarmlandInfo farmlandInfo;         //农户最后发布的农田
    private ArrayList<MachineInfo> machineInfos;     //查询回来的农机
    private List<MachineInfo> machinesToShow;       //需要显示的农机
    private Thread thread;  //延时获取农田数据的线程
    private boolean isFirst = true;
    private Handler handler;
    private Runnable runnable;

    ////////////////////////地图变量//////////////////////////
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private boolean isFristLocation = true;
    /**
     * 当前定位的模式
     */
    private LocationService locationService;
    /**
     * 当前定位的模式
     */
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
    /**
     * 定位的客户端
     */
    private String permissionInfo;
    private final int SDK_PERMISSION_REQUEST = 127;
    private BDLocation curlocation ; //当前位置
    ////////////////////////地图变量//////////////////////////


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ////////////////////////地图代码////////////////////////////////////
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getActivity().getApplicationContext());
        ////////////////////////地图代码结束////////////////////////////////////

        //获取传递的参数
        Bundle bundle = getArguments();
        token = bundle.getString("token");
        if (token == null) {
            error_hint("参数传递错误！");
            return null;
        }
        // Inflate the layout for this fragment
        try {
            if (view == null) {
                machineInfos = new ArrayList<>();
                view = inFlater(inflater);
            }

            //////////////////////////地图代码////////////////////////////
            //获取地图控件引用
            mMapView = (MapView) getActivity().findViewById(R.id.bmapView);
            mMapView = (MapView) view.findViewById(R.id.bmapView);
            mMapView.showScaleControl(true);

            mBaiduMap = mMapView.getMap();

            // 改变地图状态
            MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);
            mBaiduMap.setMapStatus(msu);
            mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    mBaiduMap.hideInfoWindow();
                }

                @Override
                public boolean onMapPoiClick(MapPoi mapPoi) {
                    return false;
                }
            });

            //注册回到当前位置按钮监听事件
            //ImageButton locationBtn = (ImageButton)view.findViewById(R.id.my_location);
           // locationBtn.setOnClickListener(new goBackListener());

            //给服务器传递参数，启动该Activity获取50公里的维修站点经纬度
            //Log.i(TAG, "启动activity获取50公里农田经纬度");


            //定义Maker坐标点
            //西廉良村，河北大学，东站,保定站,植物园
            String[] names = new String[]{"保定站", "河北大学", "西廉良村", "植物园", "东站"};
            Double[][] numthree = new Double[][]{{38.86317366367406, 115.47990000000006}, {38.86858730724386, 115.51474000000007},{38.885335516312644, 115.44805233879083},
                    {38.914613417728475, 115.4850954388619}, {38.86430366154974, 115.60169999999994}};
            this.markMachine(numthree, names);

            // 开启图层定位
            // -----------location config ------------

            locationService = ((AppController) getActivity().getApplication()).locationService;
            //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的

            //注册监听
            locationService.registerListener(new mListener());
            locationService.setLocationOption(locationService.getOption());


            mBaiduMap.setMyLocationEnabled(true);
            locationService.start();// 定位SDK
            //添加覆盖物鼠标点击事件
            mBaiduMap.setOnMarkerClickListener(new markerClicklistener());
            /////////////////地图代码结束////////////////////////

            //延时获取农田信息
            thread=new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try {
                        Thread.sleep(2000);
                        //在这里添加调用接口获取数据的代码
                        farmlandInfo = ((mainpages)getActivity()).getLastUndoFarmland();
                        //获取农机数据
                        if(farmlandInfo != null) //如果传递过来的参数为空，则在mListener地图定位后，使用当前位置搜索农机
                            getMachineInfos();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

            //定时刷新任务
            handler = new Handler();
            runnable = new Runnable(){
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    // 在此处添加执行的代码
                    getMachineInfos();
                    handler.postDelayed(this, 50);// 50ms后执行this，即runable
                }
            };
            handler.postDelayed(runnable, 50);// 打开定时器，50ms后执行runnable操作
            return view;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
        //return inflater.inflate(R.layout.frament_farm_machine, container, false);
    }

    //初始化界面及数据
    public View inFlater(LayoutInflater inflater) {
        view = inflater.inflate(R.layout.activity_farm_machine_search, null, false);
        initView(view);
        // 获取屏幕的高度和宽度
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight();
        return view;
    }

    //初始化界面控件
    private void initView(View view) {
        test_pop_layout = (RelativeLayout)view.findViewById(R.id.test_top_layout);
        rb5 = (RadioButton)view.findViewById(R.id.rb5);
        rb10 = (RadioButton)view.findViewById(R.id.rb10);
        rb20 = (RadioButton)view.findViewById(R.id.rb20);
        rb30 = (RadioButton)view.findViewById(R.id.rb30);
        rb50 = (RadioButton)view.findViewById(R.id.rb50);
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);
        initOnClick();
    }

    //添加点击事件
    private void initOnClick() {
        rb5.setOnClickListener(this);
        rb10.setOnClickListener(this);
        rb20.setOnClickListener(this);
        rb30.setOnClickListener(this);
        rb50.setOnClickListener(this);
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
        Log.e("农机查询------------->", "点击选择查询范围");
        int index;
        switch (v.getId()) {
            case R.id.rb5:
                mBaiduMap.clear();
                index = IndexOfRange(5);
                if(index != -1){
                    machinesToShow = machineInfos.subList(0, index + 1);
                    ShowInMap(machinesToShow);
                }
                break;

            case R.id.rb10:
                mBaiduMap.clear();
                index = IndexOfRange(10);
                if(index != -1){
                    machinesToShow = machineInfos.subList(0, index + 1);
                    ShowInMap(machinesToShow);
                }
                break;

            case R.id.rb20:
                mBaiduMap.clear();
                index = IndexOfRange(20);
                if(index != -1){
                    machinesToShow = machineInfos.subList(0, index + 1);
                    ShowInMap(machinesToShow);
                }
                break;

            case R.id.rb30:
                mBaiduMap.clear();
                index = IndexOfRange(30);
                if(index != -1){
                    machinesToShow = machineInfos.subList(0, index + 1);
                    ShowInMap(machinesToShow);
                }
                break;

            case R.id.rb50:
                mBaiduMap.clear();
                index = IndexOfRange(50);
                if(index != -1){
                    machinesToShow = machineInfos.subList(0, index + 1);
                    ShowInMap(machinesToShow);
                }
                break;

        }
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

    ////////////////////////////地图代码开始//////////////////////////////////
    class mListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);
            // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
            BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_geo);
            MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, false, mCurrentMarker);
            mBaiduMap.setMyLocationConfigeration(config);
            Log.i("wwwwwwwwwwwwwwww", location.getLatitude() + "---" + location.getLongitude());
            // 当不需要定位图层时关闭定位图层

            // 第一次定位时，将地图位置移动到当前位置，这里有问题，先定位到河北农业大学
            if (isFristLocation) {
                isFristLocation = false;

                //保存当前location
                curlocation = location;

                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
                if(null == farmlandInfo){
                    farmlandInfo = new FarmlandInfo();
                    farmlandInfo.setCrops_kind("小麦");
                    farmlandInfo.setLongitude(String.valueOf(location.getLongitude()));
                    farmlandInfo.setLatitude(String.valueOf(location.getLatitude()));
                    getMachineInfos();
                }
            }
        }
    }

    //标记农田,参数经纬度
    private void markMachine(Double[][] numthree, String[] names) {
        //清楚覆盖物Marker,重新加载

        //Integer[] marks = new Integer[]{R.drawable.s1, R.drawable.s2, R.drawable.s3, R.drawable.s4, R.drawable.s5,
        //        R.drawable.s6, R.drawable.s7, R.drawable.s8, R.drawable.s9, R.drawable.s10,R.drawable.s11, R.drawable.s12,
        //        R.drawable.s13, R.drawable.s14, R.drawable.s15, R.drawable.s16, R.drawable.s17, R.drawable.s18, R.drawable.s19,
        //        R.drawable.s20, R.drawable.s21, R.drawable.s22, R.drawable.s23, R.drawable.s24, R.drawable.s25, R.drawable.s26,
        //        R.drawable.s27, R.drawable.s28, R.drawable.s29, R.drawable.s30};
        for (int i = 0; i < numthree.length; i++) {
        //    LatLng point = new LatLng(numthree[i][0], numthree[i][1]);

        //    int icon ;
        //    if(i<30){
        //        icon=marks[i];
        //    }else{
        //        icon=R.drawable.icon_gcoding;
        //    }

            //构建Marker图标
            //BitmapDescriptor bitmap = BitmapDescriptorFactory
            //        .fromResource(icon);
            //构建MarkerOption，用于在地图上添加Marker
            //OverlayOptions option = new MarkerOptions()
            //        .position(point)
            //        .icon(bitmap);
            //在地图上添加Marker，并显示
            //Marker marker = (Marker) mBaiduMap.addOverlay(option);

            MachineInfo machineInfo = new MachineInfo();
            machineInfo.setLatitude(numthree[i][0]);
            machineInfo.setLongitude(numthree[i][1]);
            machineInfo.setId(i);
            machineInfo.setName(names[i]);
            machineInfo.setTelephone("13483208987");
            machineInfo.setQq("123456789");
            machineInfo.setWeixin("zhihuinongjiweixun");
            machineInfo.setRange("" + (i * 10 + 3));
            machineInfo.setWork_time("" + 16);
            machineInfo.setRemark("无");
            machineInfos.add(machineInfo);

            //Bundle bundle = new Bundle();
            //bundle.putSerializable("machineInfo", machineInfo);
            //marker.setExtraInfo(bundle);

            //添加覆盖物鼠标点击事件
            //mBaiduMap.setOnMarkerClickListener(new markerClicklistener());
        }
        rb5.setChecked(true);
        int index = IndexOfRange(5);
        if(index != -1){
            machinesToShow = machineInfos.subList(0, index + 1);
            ShowInMap(machinesToShow);
        }
        //mMapView.refreshDrawableState();
    }

    private void ShowInMap(List<MachineInfo> machineInfos){
        //清楚覆盖物Marker,重新加载

        Integer[] marks = new Integer[]{R.drawable.s1, R.drawable.s2, R.drawable.s3, R.drawable.s4, R.drawable.s5,
                R.drawable.s6, R.drawable.s7, R.drawable.s8, R.drawable.s9, R.drawable.s10,R.drawable.s11, R.drawable.s12,
                R.drawable.s13, R.drawable.s14, R.drawable.s15, R.drawable.s16, R.drawable.s17, R.drawable.s18, R.drawable.s19,
                R.drawable.s20, R.drawable.s21, R.drawable.s22, R.drawable.s23, R.drawable.s24, R.drawable.s25, R.drawable.s26,
                R.drawable.s27, R.drawable.s28, R.drawable.s29, R.drawable.s30};
        for (int i = 0; i < machineInfos.size(); i++) {
            LatLng point = new LatLng(machineInfos.get(i).getLatitude(), machineInfos.get(i).getLongitude());

            int icon ;
            if(i<30){
                icon=marks[i];
            }else{
                icon=R.drawable.icon_gcoding;
            }

            //构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory
                    .fromResource(icon);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions()
                    .position(point)
                    .icon(bitmap);
            //在地图上添加Marker，并显示
            Marker marker = (Marker) mBaiduMap.addOverlay(option);
            Bundle bundle = new Bundle();
            bundle.putSerializable("machineInfo", machineInfos.get(i));
            marker.setExtraInfo(bundle);

        }

        mMapView.refreshDrawableState();
    }

    //地图图标点击事件监听类
    class markerClicklistener implements BaiduMap.OnMarkerClickListener {

        /**
         * 地图 Marker 覆盖物点击事件监听函数
         *
         * @param marker 被点击的 marker
         */
        @Override
        public boolean onMarkerClick(Marker marker) {
            final MachineInfo machineInfo = (MachineInfo) marker.getExtraInfo().get("machineInfo");
            InfoWindow infoWindow;

            // 显示自定义 popupWindow
            PopupWindow popupWindow = makePopupWindow(view.getContext(), machineInfo);
            if(phoneBtn != null) {
                phoneBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + telephone));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            }
            int[] xy = new int[2];
            test_pop_layout.getLocationOnScreen(xy);
            popupWindow.showAtLocation(test_pop_layout, Gravity.CENTER| Gravity.BOTTOM, 0, -height);

            //构造弹出layout
            /*LayoutInflater inflater = LayoutInflater.from(getActivity().getApplicationContext());
            View markerpopwindow = inflater.inflate(R.layout.markerpopwindow, null);

            TextView tv = (TextView) markerpopwindow.findViewById(R.id.markinfo);
            String markinfo = "电话:" + machineInfo.getTelephone() + "\n" + "经度:" + machineInfo.getLongitude() + "\n" + "维度:" + machineInfo.getLatitude();
            Log.i("markinfo", markinfo);
            tv.setText(markinfo);


            ImageButton tellBtn = (ImageButton) markerpopwindow.findViewById(R.id.markerphone);
            tellBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + machineInfo.getTelephone()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });

            LatLng ll = marker.getPosition();
            //将marker所在的经纬度的信息转化成屏幕上的坐标
            Point p = mBaiduMap.getProjection().toScreenLocation(ll);
            p.y -= 90;
            LatLng llInfo = mBaiduMap.getProjection().fromScreenLocation(p);
            //初始化infoWindow，最后那个参数表示显示的位置相对于覆盖物的竖直偏移量，这里也可以传入一个监听器
            infoWindow = new InfoWindow(markerpopwindow, llInfo, 1);
            mBaiduMap.showInfoWindow(infoWindow);//显示此infoWindow
            //让地图以备点击的覆盖物为中心
            MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.setMapStatus(status);*/
            return true;
        }
    }

    //回到当前位置按钮点击事件,将当前位置定位到屏幕中心
    class goBackListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            LatLng ll = new LatLng(curlocation.getLatitude(),
                    curlocation.getLongitude());
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(u);

        }
    }

    ////////////////////////////地图代码结束/////////////////////////////////

    // 显示机主信息
    private TextView driver_name, driver_phone, qq, weixin, range, work_time, remark;
    private Button phoneBtn;
    private String telephone;
    // 创建一个包含自定义view的PopupWindow
    private PopupWindow makePopupWindow(Context cx, final MachineInfo machineInfo)
    {
        final PopupWindow window;
        window = new PopupWindow(cx);

        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.machine_layout, null);
        window.setContentView(contentView);

        //加载控件
        driver_name = (TextView)contentView.findViewById(R.id.driver_name);
        driver_phone = (TextView)contentView.findViewById(R.id.driver_phone);
        range = (TextView)contentView.findViewById(R.id.range);
        qq = (TextView)contentView.findViewById(R.id.qq);
        weixin = (TextView)contentView.findViewById(R.id.weixin);
        work_time = (TextView)contentView.findViewById(R.id.work_time);
        remark = (TextView)contentView.findViewById(R.id.remark);
        phoneBtn = (Button)contentView.findViewById(R.id.phoneBtn);

        driver_name.setText("机主姓名：" + machineInfo.getName());
        driver_phone.setText("机主电话：" + machineInfo.getTelephone());
        range.setText("距离：" + machineInfo.getRange() + "km");
        qq.setText(" QQ ：" + machineInfo.getQq());
        weixin.setText("微信：" + machineInfo.getWeixin());
        work_time.setText("工作时间：" + machineInfo.getWork_time() + " 小时/天");
        remark.setText("补充说明：" + machineInfo.getRemark());
        telephone = machineInfo.getTelephone();

        window.setWidth(width);
        window.setHeight(height/2);

        // 设置PopupWindow外部区域是否可触摸
        window.setFocusable(true); //设置PopupWindow可获得焦点
        window.setTouchable(true); //设置PopupWindow可触摸
        window.setOutsideTouchable(true); //设置非PopupWindow区域可触摸
        return window;
    }

    //查找数据中满足条件的值
    private int IndexOfRange(int range){
        int i = -1;
        if(null == machineInfos || machineInfos.isEmpty()) return i;

        for (MachineInfo m:machineInfos) {
            if (Float.parseFloat(m.getRange()) > range)
                return i;
            else
                i++;
        }

        return i;
    }

    //查询农机
    public void getMachineInfos() {

        String tag_string_req = "req_machines_get";

        if(isFirst) {
            pDialog.setMessage("正在获取农机数据 ...");
            showDialog();
            rb5.setChecked(true);
            isFirst = false;
        }else{
            if(null != ((mainpages)getActivity()).getLastUndoFarmland()){
                farmlandInfo = ((mainpages)getActivity()).getLastUndoFarmland();
            }
        }

        if (!netutil.checkNet(getActivity())) {
            hideDialog();
            error_hint("网络连接错误");
        } else {
            //服务器请求
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_MACHINE_GET, mSuccessListener, mErrorListener) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to url
                    Map<String, String> params = new HashMap<>();
                    params.put("token", token);
                    params.put("Farmlands_longitude", farmlandInfo.getLongitude());
                    params.put("Farmlands_Latitude", farmlandInfo.getLatitude());
                    params.put("Farmlands_crops_kind", farmlandInfo.getCrops_kind());
                    //params.put("Machine_type", farmlandInfo.getOperation_kind());
                    params.put("Search_range", "50");
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
            Log.d(TAG, "Release Response: " + response);
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                int status = jObj.getInt("status");

                // Check for error node in json
                if (status == 0) {
                    //清空旧数据，因为暂时没有农机数据，所以使用模拟数据，不清空
                    //machineInfos.clear();
                    //此处引入JSON jar包
                    JSONArray jObjs = jObj.getJSONArray("result");
                    for(int i = 0; i < jObjs.length(); i++){
                        MachineInfo temp = new MachineInfo();
                        JSONObject object = (JSONObject)jObjs.opt(i);
                        //temp.setId(object.getInt("id"));
                        temp.setLatitude(object.getDouble("Machine_Latitude"));
                        temp.setLongitude(object.getDouble("Machine_longitude"));
                        temp.setName(object.getString("person_name"));
                        temp.setTelephone(object.getString("person_phone"));
                        temp.setQq(object.getString("person_qq"));
                        temp.setWeixin(object.getString("person_weixin"));
                        //temp.setState(object.getString("Machine_state"));
                        temp.setRange(object.getString("distance"));
                        //temp.setMachine_type(object.getString("Machine_type"));
                        temp.setWork_time(object.getString("Machine_worktime"));
                        temp.setRemark(object.getString("Machine_remark"));
                        machineInfos.add(temp);
                    }
                    //在地图上显示农机位置
                    int index;
                    if(rb5.isChecked()) {
                        index = IndexOfRange(5);
                    }else if(rb10.isChecked()){
                        index = IndexOfRange(10);
                    }else if(rb20.isChecked()){
                        index = IndexOfRange(20);
                    }else if(rb30.isChecked()){
                        index = IndexOfRange(30);
                    }else{
                        index = IndexOfRange(50);
                    }

                    if(index != -1) {
                        machinesToShow = machineInfos.subList(0, index + 1);
                        ShowInMap(machinesToShow);
                    }
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

    @Override
    public void onDestroy(){
        handler.removeCallbacks(runnable);// 关闭定时器处理
    }
}
