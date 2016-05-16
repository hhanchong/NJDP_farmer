package com.njdp.njdp_farmer;

import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.njdp.njdp_farmer.adpter.FarmAdapter;
import com.njdp.njdp_farmer.MyClass.FarmlandInfo;
import com.njdp.njdp_farmer.conent_frament.FarmlandManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class FarmerLandList extends AppCompatActivity {
    private final String TAG = "FarmLandList";
    private final String[][] cropsType = new String[][]{{"H","收割"}, {"C", "耕作"}, {"S", "播种"},
            {"WH", "小麦"}, {"CO", "玉米"}, {"RC", "水稻"}, {"GR", "谷物"}, {"OT", "其他"}, {"SS", "深松"}, {"HA", "平地"}};
    private ExpandableListView listView;
    private List<String> group;
    private List<List<FarmlandInfo>> child;
    private ArrayList<FarmlandInfo> farmlandInfoList;
    private ArrayList<FarmlandInfo> farmlandInfos; //根据年筛选后的数据
    private FarmAdapter adapter;
    private ImageButton getback=null;
    private Spinner spinner;
    private View farmlandlist;
    List<String> Years = new ArrayList<>();

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
        //初始化参数及控件
        farmlandInfoList = new ArrayList<>();
        farmlandInfos = new ArrayList<>();
        listView = (ExpandableListView) findViewById(R.id.expandableListView);
        spinner = (Spinner)findViewById(R.id.sp_year);
        farmlandlist = findViewById(R.id.root_div);
        farmlandlist.getBackground().setAlpha(180);

        //获取农田数据
        farmlandInfoList = FarmlandManager.getFarmlands();
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
        spinner.setAdapter(adapter);
        //设置Spinner下拉列表的标题
        spinner.setPrompt("选择要查询的年份");
        //为spinner绑定监听器
        spinner.setOnItemSelectedListener(new SpinnerListener());
        spinner.setSelection(5);
        getback=(ImageButton) super.findViewById(R.id.getback);
        //返回上一界面
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
        for(FarmlandInfo f :farmlandInfos){
            f.setCrops_kind(ConvertToCHS(f.getCrops_kind()));
            addInfo(i+"."+f.getVillage() + "-" + f.getCrops_kind() + "-" + f.getArea() + "亩-" + (f.getStatus().equals("0") ? "未完成":"已完成"), new FarmlandInfo[]{f});
            i++;
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
            if(group.size() >= 0) {
                adapter = new FarmAdapter(FarmerLandList.this, group, child);
                listView.setAdapter(adapter);
                listView.setGroupIndicator(null);  //不显示向下的箭头
            }
        }
        //当用户不做选择时调用的该方法
        public void onNothingSelected(AdapterView<?> arg0) {

        }
    }

    //转换为
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

    //错误信息提示
    private void error_hint(String str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }
}
