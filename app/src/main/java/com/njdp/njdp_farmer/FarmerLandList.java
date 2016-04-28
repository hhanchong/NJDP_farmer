package com.njdp.njdp_farmer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageButton;

import com.njdp.njdp_farmer.adpter.MyAdapter;
import com.njdp.njdp_farmer.bean.FarmlandInfo;

import java.util.ArrayList;
import java.util.List;

public class FarmerLandList extends AppCompatActivity {
    private ExpandableListView listView;
    private List<String> group;
    private List<List<FarmlandInfo>> child;
    private MyAdapter adapter;
    private ImageButton getback=null;
    private FarmlandInfo farmlandInfo, farmlandInfo1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_land_list);

        farmlandInfo = new FarmlandInfo();
        farmlandInfo.setCrops_kind("小麦");
        farmlandInfo.setStatus("未收割");
        farmlandInfo.setArea(28);
        farmlandInfo.setUnit_price(86);
        farmlandInfo.setBlock_type("规则");
        farmlandInfo.setProvince("河北");
        farmlandInfo.setCity("邯郸");
        farmlandInfo.setCounty("武义县");
        farmlandInfo.setTown("某某乡");
        farmlandInfo.setVillage("某某村");
        farmlandInfo.setStart_time(farmlandInfo.StringFormatDate("2016-04-07"));
        farmlandInfo.setEnd_time(farmlandInfo.StringFormatDate("2016-04-30"));
        farmlandInfo.setRemark("无");
        farmlandInfo1 = new FarmlandInfo();
        farmlandInfo1.setCrops_kind("小麦");
        farmlandInfo1.setStatus("0");
        farmlandInfo1.setArea(28);
        farmlandInfo1.setUnit_price(86);
        farmlandInfo1.setBlock_type("规则");
        farmlandInfo1.setProvince("河北");
        farmlandInfo1.setCity("邯郸");
        farmlandInfo1.setCounty("武义县");
        farmlandInfo1.setTown("某某乡");
        farmlandInfo1.setVillage("某某村");
        farmlandInfo1.setStart_time(farmlandInfo.StringFormatDate("2016-04-07"));
        farmlandInfo1.setEnd_time(farmlandInfo.StringFormatDate("2016-04-30"));
        farmlandInfo1.setRemark("无");
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
        addInfo("北京",new FarmlandInfo[]{farmlandInfo});
        addInfo("河北", new FarmlandInfo[]{farmlandInfo1});
        //addInfo("广东", new FarmlandInfo[]{farmlandInfo});
    }

    /**
     * 添加数据信息
     * @param g
     * @param c
     */
    private void addInfo(String g,FarmlandInfo[] c) {
        group.add(g);
        List<FarmlandInfo> list = new ArrayList<FarmlandInfo>();
        for (int i = 0; i < c.length; i++) {
            list.add(c[i]);
        }
        child.add(list);
    }
}
