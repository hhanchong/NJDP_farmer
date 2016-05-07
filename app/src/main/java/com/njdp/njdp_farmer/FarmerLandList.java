package com.njdp.njdp_farmer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
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
import com.njdp.njdp_farmer.adpter.FarmAdapter;
import com.njdp.njdp_farmer.MyClass.FarmlandInfo;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.util.NetUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FarmerLandList extends AppCompatActivity {
    private final String TAG = "FarmLandList";
    private ExpandableListView listView;
    private List<String> group;
    private List<List<FarmlandInfo>> child;
    private ArrayList<FarmlandInfo> farmlandInfoList;
    private ArrayList<FarmlandInfo> farmlandInfos; //根据年筛选后的数据
    private FarmAdapter adapter;
    private ImageButton getback=null;
    private Spinner spinner;
    List<String> Years = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_land_list);
        //初始化参数及控件
        farmlandInfoList = new ArrayList<>();
        farmlandInfos = new ArrayList<>();
        listView = (ExpandableListView) findViewById(R.id.expandableListView);
        spinner = (Spinner)findViewById(R.id.sp_year);

        //获取传递的参数
        farmlandInfoList = (ArrayList<FarmlandInfo>)getIntent().getSerializableExtra("farmlandInfos");
        if(farmlandInfoList == null)
        {
            error_hint("没有发布信息！");
        }
        /**
         * 初始化数据
         */
        //initData();
        //if(group.size() >= 0) {
        //   adapter = new FarmAdapter(this, group, child);
        //   listView.setAdapter(adapter);
        //   listView.setGroupIndicator(null);  //不显示向下的箭头
        //}
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
        //addInfo("成功村-小麦-20亩-未收割",new FarmlandInfo[]{farmlandInfo});
        //addInfo("河北", new FarmlandInfo[]{farmlandInfo1});
        //addInfo("广东", new FarmlandInfo[]{farmlandInfo});
        for(FarmlandInfo f :farmlandInfos){
            addInfo(f.getVillage() + "-" + f.getCrops_kind() + "-" + f.getArea() + "亩-" + (f.getStatus().equals("0")?"未收割":"已收割"), new FarmlandInfo[]{f});
        }

    }

    /**
     * 添加数据信息
     * @param g 标题信息
     * @param c 发布的内容
     */
    private void addInfo(String g, FarmlandInfo[] c) {
        group.add(g);
        List<FarmlandInfo> list = new ArrayList<>();
        for (FarmlandInfo f : c) {
            list.add(f);
        }
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

    //错误信息提示
    private void error_hint(String str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }
}
