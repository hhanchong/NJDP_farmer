package com.njdp.njdp_farmer;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.njdp.njdp_farmer.CostomProgressDialog.CustomProgressDialog;
import com.njdp.njdp_farmer.MyClass.AgentApplication;
import com.njdp.njdp_farmer.MyClass.FarmlandInfo;
import com.njdp.njdp_farmer.conent_frament.*;
import com.njdp.njdp_farmer.viewpage.ContentViewPager;

import java.util.ArrayList;
import java.util.List;

public class mainpages extends AppCompatActivity {
    private String token;
    private FarmlandInfo lastUndoFarmland;
    private CustomProgressDialog progressDialog;
    private ContentViewPager contentViewPager;

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
        setContentView(R.layout.activity_mainpage);
        AgentApplication.addActivity(this);

        if(progressDialog != null) {
            progressDialog.cancel();
        }
        progressDialog = new CustomProgressDialog(this,"数据正在请求中...", R.anim.donghua_frame);
        progressDialog.show();

        //checkNetState();//检查网络状态
        //获取参数
        token = getIntent().getStringExtra("TOKEN");
        int openModule = getIntent().getIntExtra("openModule", 0);
        //填充数据
        initdata();
        if(openModule == 1 || openModule == 2 || openModule == 3){
            initview(openModule);//填充布局
        }
        else{
            error_hint("参数传输错误！");
            finish();
        }
        progressDialog.dismiss();
    }

    private List<Fragment> content_list = null;

    private void initdata() {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        //要传递的参数
        Bundle bundle1 = new Bundle();
        bundle1.putString("token", token);
        if(content_list == null)
            content_list = new ArrayList<>();
        else
            content_list.clear();
        //农户发布界面
        FarmlandManager farmlandManager = new FarmlandManager();
        farmlandManager.setArguments(bundle1);
        content_list.add(farmlandManager);
        //农机查询界面
        progressDialog.setContent("正在准备农机信息！");
        FarmMachineSearch farmMachineSearch = new FarmMachineSearch();
        farmMachineSearch.setArguments(bundle1);
        content_list.add(farmMachineSearch);
        //个人信息界面，需要用到农田发布的数据，先加载
        progressDialog.setContent("正在准备个人数据！");
        PersonalInfoFrame personalInfoFrame = new PersonalInfoFrame();
        personalInfoFrame.setArguments(bundle1);
        content_list.add(personalInfoFrame);
        transaction.commit();
    }

    private void initview(int page) {
        if (content_list == null) {
            return;
        }
        contentViewPager = (ContentViewPager) findViewById(R.id.content_viewpager);
        RadioGroup contentradiogroup = (RadioGroup) findViewById(R.id.content_radiogroup);
        //预加载一页
        contentViewPager.setOffscreenPageLimit(5);
        contentViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return content_list.get(i);
            }

            @Override
            public int getCount() {
                return content_list.size();
            }

        });
        assert contentradiogroup != null;
        contentradiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rb_release:
                        contentViewPager.setCurrentItem(0);
                        break;
                    case R.id.rb_search:
                        contentViewPager.setCurrentItem(1);
                        break;
                    case R.id.rb_userInfo:
                        contentViewPager.setCurrentItem(2);
                        break;
                }
            }
        });
        if(page == 1) {
            contentradiogroup.check(R.id.rb_release);
        }else if(page == 2){
            contentradiogroup.check(R.id.rb_search);
        }else {
            contentradiogroup.check(R.id.rb_userInfo);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        content_list.clear();
        AgentApplication.removeActivity(this);
    }

    //获取和设置最后没有完成的发布任务，与个人信息Frame交互
    public FarmlandInfo getLastUndoFarmland() {
        return lastUndoFarmland;
    }
    public void setLastUndoFarmland(FarmlandInfo farmland) {
        this.lastUndoFarmland = farmland;
    }

    //错误信息提示
    private void error_hint(String str) {
        Toast toast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
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
}
