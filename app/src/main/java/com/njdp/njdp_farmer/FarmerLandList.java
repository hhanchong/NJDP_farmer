package com.njdp.njdp_farmer;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.njdp.njdp_farmer.MyClass.AgentApplication;
import com.njdp.njdp_farmer.adpter.FarmAdapter;
import com.njdp.njdp_farmer.MyClass.FarmlandInfo;
import com.njdp.njdp_farmer.conent_frament.FarmlandManager;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.db.SessionManager;
import com.njdp.njdp_farmer.util.NetUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FarmerLandList extends AppCompatActivity {
    private static final int FARMLAND_EDIT = 1;
    private final String TAG = "FarmLandList";
    private final String[][] cropsType = new String[][]{{"H","收割"}, {"C", "耕作"}, {"S", "播种"},
            {"WH", "小麦"}, {"CO", "玉米"}, {"RC", "水稻"}, {"GR", "谷物"}, {"OT", "其他"}, {"SS", "深松"}, {"HA", "平地"}};
    private ExpandableListView listView;
    private List<String> group;
    private List<List<FarmlandInfo>> child;
    private ArrayList<FarmlandInfo> farmlandInfoList;
    private ArrayList<FarmlandInfo> farmlandInfos; //根据年筛选后的数据
    private ImageButton getback=null;
    List<String> Years = new ArrayList<>();
    private ProgressDialog pDialog;
    private String token;
    private int isEditNow=-1;

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
        setContentView(R.layout.activity_farmer_land_list);
        AgentApplication.addActivity(this);

        //初始化参数及控件
        farmlandInfoList = new ArrayList<>();
        farmlandInfos = new ArrayList<>();
        //获取扩展列表
        listView = (ExpandableListView) findViewById(R.id.expandableListView);
        listView.setOnItemLongClickListener(new OnItemLongClickListenerImpl()); // 长按事件
        this.registerForContextMenu(listView); // 为所有列表项注册上下文菜单
        //获取时间选择下拉窗
        Spinner spinner = (Spinner) findViewById(R.id.sp_year);
        //获取背景
        View farmlandlist = findViewById(R.id.root_div);
        assert farmlandlist != null;
        farmlandlist.getBackground().setAlpha(180);

        //获取农田数据
        farmlandInfoList = AgentApplication.farmlandInfos;
        token = getIntent().getStringExtra("token");
        //判断参数传递是否正确
        if (token == null) {
            error_hint("参数传递错误！");
            finish();
        }
        if(farmlandInfoList == null)
        {
            error_hint("没有发布信息！");
        }

        //下拉菜单的数据
        int year = Calendar.getInstance().get(Calendar.YEAR);
        Years.add((year-5)+"");
        Years.add((year-4)+"");
        Years.add((year-3)+"");
        Years.add((year-2)+"");
        Years.add((year-1)+"");
        Years.add(year+"");
        Years.add((year+1)+"");
        //绑定适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,Years);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //为spinner添加适配器
        assert spinner != null;
        spinner.setAdapter(adapter);
        //设置Spinner下拉列表的标题
        spinner.setPrompt("选择要查询的年份");
        //为spinner绑定监听器
        spinner.setOnItemSelectedListener(new SpinnerListener());
        spinner.setSelection(5);
        //进度条
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        //返回上一界面
        getback=(ImageButton) super.findViewById(R.id.getback);
        getback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void initData() {
        group = new ArrayList<>();
        child = new ArrayList<>();
        int i = 1;
        for(int j = farmlandInfos.size()-1; j > -1; j-- ){
            addInfo(i+"."+farmlandInfos.get(j).getVillage() + "-" + ConvertToCHS(farmlandInfos.get(j).getCrops_kind()) + "-" + farmlandInfos.get(j).getArea()
                    + "亩-" + (farmlandInfos.get(j).getStatus().equals("0") ? "未完成":"已完成"), new FarmlandInfo[]{farmlandInfos.get(j)});
            i++;
        }
        //刷新界面
        if(group.size() >= 0) {
            FarmAdapter adapter = new FarmAdapter(FarmerLandList.this, group, child);
            listView.setAdapter(adapter);
            listView.setGroupIndicator(null);  //不显示向下的箭头
        }
    }

    /**
     * 添加数据信息
     * @param g 标题信息
     * @param c 发布的内容
     */
    private void addInfo(String g, FarmlandInfo[] c) {
        group.add(g);
        List<FarmlandInfo> list = Arrays.asList(c);
        child.add(list);
    }

    //该监听器用于监听用户多spinner的操作
    class SpinnerListener implements AdapterView.OnItemSelectedListener {
        //当用户选择先拉列表中的选项时会调用这个方法
        /**
         *参数说明：
         *第一个：当前的下拉列表，也就是第三个参数的父view
         *第二个：当前选中的选项
         *第三个：所选选项的位置
         *第四个： 所选选项的id
         */
        public void onItemSelected(AdapterView<?> adapterView, View view, int position,
                                   long id) {
            //获取用户所选的选项内容
            String selected = adapterView.getItemAtPosition(position).toString();
            farmlandInfos.clear();
            for(FarmlandInfo f : farmlandInfoList){
                if(f.getStart_time_String().contains(selected)){
                    farmlandInfos.add(f);
                }
            }
            /**
             * 初始化列表数据
             */
            initData();
        }
        //当用户不做选择时调用的该方法
        public void onNothingSelected(AdapterView<?> arg0) {

        }
    }

    private class OnItemLongClickListenerImpl implements AdapterView.OnItemLongClickListener{
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if(view.getTag(R.id.flag)!=null){
                int groupPos = (Integer) view.getTag(R.id.flag); //参数值是在setTag时使用的对应资源id号
                Log.i("LongClickListener----", "触发长按事件，触发的是第" + groupPos + "项！");
            }else {
                return true; //返回TRUE不会弹出菜单选项
            }

            return false;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, view, menuInfo);
        ExpandableListView.ExpandableListContextMenuInfo info =(ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
        int type = ExpandableListView
                .getPackedPositionType(info.packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP )
        {
            if(!child.get(ExpandableListView.getPackedPositionGroup(info.packedPosition)).get(0).getStatus().equals("0")){
                error_hint("此项发布正在进行或已完成，不允许修改或删除！");
                return;
            }
            menu.add(0, 1, 0, "修改");
            menu.add(1, 2, 0, "删除" );
//            menu.add(1, 3, 0, "全部删除" );
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // 得到当前被选中的item信息
        //AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ExpandableListView.ExpandableListContextMenuInfo menuInfo = (ExpandableListView.ExpandableListContextMenuInfo)item.getMenuInfo();
        final int groupposion = ExpandableListView.getPackedPositionGroup(menuInfo.packedPosition);
        isEditNow = groupposion;

        switch(item.getItemId()) {
            case 1:
                // 修改
                Log.e("------------->", "修改我的发布信息");
                Intent intent = new Intent(FarmerLandList.this, FarmerRelease.class);
                intent.putExtra("token", token);
                intent.putExtra("farmlandInfo", child.get(groupposion).get(0));
                startActivityForResult(intent, FARMLAND_EDIT);
                break;
            case 2:
                // 删除
                new AlertDialog.Builder(FarmerLandList.this)
                        .setTitle("系统提示")
                        .setMessage("将要删除【" + group.get(groupposion) + "】，删除后将无法恢复，确定删除吗？")
                        .setIcon(R.drawable.ic_dialog_info)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“确认”后的操作，需要配合后台返回的结果执行下面的3行代码
                                DeleteFarmlandInfos(child.get(groupposion).get(0).getId());

                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“取消”后的操作
                            }
                        }).show();
                break;
            case 3:
                // 全部删除
                new AlertDialog.Builder(FarmerLandList.this)
                        .setTitle("系统提示")
                        .setMessage("将要删除发布的全部农田，删除后将无法恢复，确定删除吗？")
                        .setIcon(R.drawable.ic_dialog_info)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isEditNow = -1;
                                // 点击“确认”后的操作
                                DeleteFarmlandInfos(-1);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“取消”后的操作

                            }
                        }).show();
                break;

            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }

    //这是跳转到另一个布局页面返回来的操作
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != -1) {
            return;
        }
        switch (requestCode) {
            case FARMLAND_EDIT:
                if(isEditNow >= 0){
                    //更新原始数据
                    int index = farmlandInfoList.indexOf(child.get(isEditNow).get(0));
                    if(index != -1)
                        farmlandInfoList.set(index, (FarmlandInfo)data.getSerializableExtra("farmlandInfo"));
                    //更新筛选的数据
                    index = farmlandInfos.indexOf(child.get(isEditNow).get(0));
                    if(index != -1)
                        farmlandInfos.set(index, (FarmlandInfo) data.getSerializableExtra("farmlandInfo"));
                    //刷新显示
                    initData();
                    isEditNow = -1;
                }
                break;
        }
    }

    //删除发布的农田信息
    public void DeleteFarmlandInfos(final int id) {

        String tag_string_req = "req_farmland_Delete";

        pDialog.setMessage("正在更新农田数据 ...");
        showDialog();

        if (!NetUtil.checkNet(this)) {
            hideDialog();
            error_hint("网络连接错误");
        } else {
            //服务器请求
            StringRequest strReq;
            //id=-1删除全部，否则删除单条记录
            if(id >= 0){
                strReq = new StringRequest(Request.Method.POST,
                        AppConfig.URL_FARMLAND_DEL, mSuccessListener, mErrorListener) {

                    @Override
                    protected Map<String, String> getParams() {
                        // Posting parameters to url
                        Map<String, String> params = new HashMap<>();
                        params.put("token", token);
                        params.put("id", String.valueOf(id));
                        return params;
                    }
                };
            } else {
                strReq = new StringRequest(Request.Method.POST,
                        AppConfig.URL_FARMLAND_DEL_ALL, mSuccessListener, mErrorListener) {

                    @Override
                    protected Map<String, String> getParams() {
                        // Posting parameters to url
                        Map<String, String> params = new HashMap<>();
                        params.put("token", token);
                        return params;
                    }
                };
            }
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
                    //farmlandInfos.clear();
                    //此处引入JSON jar包
                    //String result = jObj.getString("result");
                    if(isEditNow == -1){
                        farmlandInfoList.clear();
                        farmlandInfos.clear();
                    }else {
                        farmlandInfoList.remove(child.get(isEditNow).get(0));
                        farmlandInfos.remove(child.get(isEditNow).get(0));
                    }
                    initData();

                } else if(status == 3){
                    //密匙失效
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(FarmerLandList.this, login.class);
                    startActivity(intent);
                    finish();
                }
                else if(status == 4){
                    //密匙不存在
                    error_hint("用户登录过期，请重新登录！");
                    SessionManager session=new SessionManager(getApplicationContext());
                    session.setLogin(false, false, "");
                    Intent intent = new Intent(FarmerLandList.this, login.class);
                    startActivity(intent);
                    finish();
                } else if(status == 15){
                    error_hint("农田正在收割，不能删除！");
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
            Log.e(TAG, "DeleteFarmLandInfo Error: " + error.getMessage());
            error_hint("服务器连接超时");
            hideDialog();
        }
    };

    //类型转换为中文
    private String ConvertToCHS(String s){
        String operation = "", crop = "";

        if(s.length() == 3){
            for(int i = 0; i < cropsType.length; i++){
                if(cropsType[i][0].equals(s.substring(0,1))){
                    operation = cropsType[i][1];
                }
                if(cropsType[i][0].equals(s.substring(1,3))){
                    crop = cropsType[i][1];
                }
            }
            if(operation.isEmpty() || crop.isEmpty()){
                operation = "未知";
                crop = "";
            }
        }else {
            operation = "未知";
        }
        return operation + crop;
    }

    //错误信息提示1
    private void error_hint(String str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    //错误信息提示2
    private void empty_hint(int in) {
        Toast toast = Toast.makeText(this, getResources().getString(in), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
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

    @Override
    public void onDestroy(){
        super.onDestroy();
        this.unregisterForContextMenu(listView);
        AgentApplication.removeActivity(this);
    }
}
