package com.njdp.njdp_farmer.conent_frament;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.njdp.njdp_farmer.R;
import com.njdp.njdp_farmer.bean.FarmlandInfo;
import com.njdp.njdp_farmer.bean.MachineInfo;
import com.njdp.njdp_farmer.db.AppController;

public class FarmMachineSearch extends Fragment implements View.OnClickListener {
    private String token;
    View view;


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
        // Inflate the layout for this fragment


        ////////////////////////地图代码////////////////////////////////////
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getActivity().getApplicationContext());
        ////////////////////////地图代码结束////////////////////////////////////


        try {
            if (view == null) {
                view = inFlater(inflater);
            }
            Bundle bundle = getArguments();
            token = bundle.getString("token");
            if (token == null) {
                error_hint("参数传递错误！");
                return null;
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
            Double[][] numthree = new Double[][]{{38.885335516312644, 115.44805233879083}, {38.86858730724386, 115.51474000000007}, {38.86430366154974, 115.60169999999994},
                    {38.86317366367406, 115.47990000000006}, {38.914613417728475, 115.4850954388619}};
            this.markMachine(numthree);

            // 开启图层定位
            // -----------location config ------------

            locationService = ((AppController) getActivity().getApplication()).locationService;
            //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的

            //注册监听
            locationService.registerListener(new mListener());
            locationService.setLocationOption(locationService.getOption());


            mBaiduMap.setMyLocationEnabled(true);
            locationService.start();// 定位SDK

            /////////////////地图代码结束////////////////////////



            return view;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
        //return inflater.inflate(R.layout.frament_farm_machine, container, false);
    }

    public View inFlater(LayoutInflater inflater) {
        view = inflater.inflate(R.layout.activity_farm_machine_search, null, false);
        initView(view);

        return view;
    }

    private void initView(View view) {


        initOnClick();
    }

    private void initOnClick() {

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

                break;
            case R.id.address:

                break;
            case R.id.start_time:

                break;
            case R.id.end_time:

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
            }
        }
    }

    //标记农田,参数经纬度
    private void markMachine(Double[][] numthree) {
        //清楚覆盖物Marker,重新加载

        Integer[] marks = new Integer[]{R.drawable.s1, R.drawable.s2, R.drawable.s3, R.drawable.s4, R.drawable.s5,
                R.drawable.s6, R.drawable.s7, R.drawable.s8, R.drawable.s9, R.drawable.s10,R.drawable.s11, R.drawable.s12,
                R.drawable.s13, R.drawable.s14, R.drawable.s15, R.drawable.s16, R.drawable.s17, R.drawable.s18, R.drawable.s19,
                R.drawable.s20, R.drawable.s21, R.drawable.s22, R.drawable.s23, R.drawable.s24, R.drawable.s25, R.drawable.s26,
                R.drawable.s27, R.drawable.s28, R.drawable.s29, R.drawable.s30};
        for (int i = 0; i < numthree.length; i++) {
            LatLng point = new LatLng(numthree[i][0], numthree[i][1]);

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

            MachineInfo machineInfo = new MachineInfo();
            machineInfo.setLatitude(numthree[i][0]);
            machineInfo.setLongitude(numthree[i][1]);
            machineInfo.setTelephone("13483208987");

            Bundle bundle = new Bundle();
            bundle.putSerializable("machineInfo", machineInfo);
            marker.setExtraInfo(bundle);

            //添加覆盖物鼠标点击事件
            mBaiduMap.setOnMarkerClickListener(new markerClicklistener());
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

            //构造弹出layout
            LayoutInflater inflater = LayoutInflater.from(getActivity().getApplicationContext());
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
            mBaiduMap.setMapStatus(status);
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
}
