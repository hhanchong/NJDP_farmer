package com.njdp.njdp_farmer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FarmerLandList extends AppCompatActivity {
    private final String TAG = "FarmLandList";
    private ExpandableListView listView;
    private List<String> group;
    private List<List<FarmlandInfo>> child;
    private ArrayList<FarmlandInfo> farmlandInfoList;
    private FarmAdapter adapter;
    private ImageButton getback=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_land_list);
        //初始化参数及控件
        farmlandInfoList = new ArrayList<>();
        listView = (ExpandableListView) findViewById(R.id.expandableListView);

        //获取传递的参数
        farmlandInfoList = (ArrayList<FarmlandInfo>)getIntent().getSerializableExtra("farmlandInfos");
        if(farmlandInfoList == null)
        {
            error_hint("没有发布信息！");
        }
        /**
         * 初始化数据
         */
        initData();
        if(group.size() >= 0) {
            adapter = new FarmAdapter(this, group, child);
            listView.setAdapter(adapter);
            listView.setGroupIndicator(null);  //不显示向下的箭头
        }
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
        for(FarmlandInfo f :farmlandInfoList){
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
        for (FarmlandInfo f : list) {
            list.add(f);
        }
        child.add(list);
    }

    //错误信息提示
    private void error_hint(String str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }
}
