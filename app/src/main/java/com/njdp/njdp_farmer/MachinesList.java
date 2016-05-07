package com.njdp.njdp_farmer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.njdp.njdp_farmer.MyClass.MachineInfo;
import com.njdp.njdp_farmer.adpter.MachineAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MachinesList extends AppCompatActivity implements AdapterView.OnItemClickListener, Window.Callback {
    private ExpandableListView listView;
    private List<String[]> group;
    private List<List<MachineInfo>> child;
    private ArrayList<MachineInfo> machineInfos;
    private MachineAdapter adapter;
    private ImageButton getback = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machines_list);

        //初始化参数及控件
        machineInfos = new ArrayList<>();
        listView = (ExpandableListView) findViewById(R.id.expandableListView);

        //获取传递的参数
        machineInfos = (ArrayList<MachineInfo>) getIntent().getSerializableExtra("machineInfos");
        if (machineInfos == null) {
            error_hint("没有发布信息！");
        }

        /**
         * 初始化列表数据
         */
        initData();
        if (group.size() >= 0) {
            adapter = new MachineAdapter(MachinesList.this, group, child, mListener);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);
            listView.setGroupIndicator(null);  //不显示向下的箭头
        }

        getback = (ImageButton) super.findViewById(R.id.getback);
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
        for (MachineInfo f : machineInfos) {
            addInfo(new String[]{f.getImage(), f.getName(), f.getRange(), f.getTelephone()}, new MachineInfo[]{f});
        }
    }

    /**
     * 添加数据信息
     * @param g 标题信息
     * @param c 发布的内容
     */
    private void addInfo(String[] g, MachineInfo[] c) {
        group.add(g);
        List<MachineInfo> list = Arrays.asList(c);
        child.add(list);
    }

    /**
     * 响应ListView中item的点击事件
     */
    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
        Toast.makeText(this, "listview的item被点击了！，点击的位置是-->" + position,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * 实现类，响应按钮点击事件
     */
    private MachineAdapter.MyClickListener mListener = new MachineAdapter.MyClickListener() {
        @Override
        public void myOnClick(int position, View v) {
            //Toast.makeText(MachinesList.this, "listview的内部的按钮被点击了！，位置是-->" + position + ",内容是-->" + group.get(position)[3], Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + group.get(position)[3]));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (ActivityCompat.checkSelfPermission(MachinesList.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                error_hint("没有获得拨打电话的权限，请确认。");
                return;
            }
            startActivity(intent);
        }
    };

    //错误信息提示
    private void error_hint(String str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }
}
