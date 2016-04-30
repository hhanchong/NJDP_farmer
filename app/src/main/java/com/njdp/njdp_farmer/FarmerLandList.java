package com.njdp.njdp_farmer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.njdp.njdp_farmer.adpter.MyAdapter;
import com.njdp.njdp_farmer.bean.FarmlandInfo;

import java.util.ArrayList;
import java.util.List;

public class FarmerLandList extends AppCompatActivity {
    private ExpandableListView listView;
    private List<String> group;
    private List<List<FarmlandInfo>> child;
    private ArrayList<FarmlandInfo> farmlandInfoList;
    private MyAdapter adapter;
    private ImageButton getback=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_land_list);

        farmlandInfoList = (ArrayList<FarmlandInfo>)getIntent().getSerializableExtra("farmlandInfos");
        if(farmlandInfoList == null)
        {
            error_hint("没有发布信息！");
            this.finish();
        }

        listView = (ExpandableListView) findViewById(R.id.expandableListView);
        /**
         * 初始化数据
         */
        initData();
        adapter = new MyAdapter(this,group,child);
        listView.setAdapter(adapter);
        listView.setGroupIndicator(null);  //不显示向下的箭头
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
        group = new ArrayList<String>();
        child = new ArrayList<List<FarmlandInfo>>();
        //addInfo("成功村-小麦-20亩-未收割",new FarmlandInfo[]{farmlandInfo});
        //addInfo("河北", new FarmlandInfo[]{farmlandInfo1});
        //addInfo("广东", new FarmlandInfo[]{farmlandInfo});
        for(FarmlandInfo f :farmlandInfoList){
            addInfo(f.getVillage() + "-" + f.getCrops_kind() + "-" + f.getArea() + "亩-" + (f.getStatus().equals("0")?"未收割":"已收割"), new FarmlandInfo[]{f});
        }
    }

    /**
     * 添加数据信息
     * @param g
     * @param c
     */
    private void addInfo(String g, FarmlandInfo[] c) {
        group.add(g);
        List<FarmlandInfo> list = new ArrayList<FarmlandInfo>();
        for (int i = 0; i < c.length; i++) {
            list.add(c[i]);
        }
        child.add(list);
    }

    //错误信息提示
    private void error_hint(String str) {
        Toast toast = Toast.makeText(FarmerLandList.this, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }
}
