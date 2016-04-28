package com.njdp.njdp_farmer;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.njdp.njdp_farmer.CostomProgressDialog.CustomProgressDialog;
import com.njdp.njdp_farmer.bean.Farmer;
import com.njdp.njdp_farmer.conent_frament.*;
import com.njdp.njdp_farmer.conent_frament.FarmerRelease;
import com.njdp.njdp_farmer.viewpage.ContentViewPager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class mainpages extends AppCompatActivity {

    private Farmer farmer;
    private CustomProgressDialog progressDialog;
    private ContentViewPager contentViewPager;
    private RadioGroup contentradiogroup;
    private EditText startTime, endTime; //发布界面的时间选择
    private int mYear, mMonth, mDay, mYear1, mMonth1, mDay1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainpage);

        farmer = (Farmer)getIntent().getSerializableExtra("farmer");

        //progressDialog = new CustomProgressDialog(this,"数据正在请求中...", R.anim.donghua_frame);
        //progressDialog.show();
        //progressDialog.dismiss();
        //checkNetState();//检查网络状态
        initdata();//填充数据
        initview();//填充布局
    }

    private List<Fragment> content_list = null;

    private void initdata() {
        content_list = new ArrayList<>();
        content_list.add(new FarmerRelease());
        content_list.add(new PersonalInfoFrame());
        content_list.add(new PersonalInfoFrame());
        //content_list.add(new YueDuFrament());
        //content_list.add(new SheZhiFrament());
    }

    private void initview() {
        if (content_list == null) {
            return;
        }
        contentViewPager = (ContentViewPager) findViewById(R.id.content_viewpager);
        contentradiogroup = (RadioGroup) findViewById(R.id.content_radiogroup);
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
        contentradiogroup.check(R.id.rb_release);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private long timeMillis;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - timeMillis) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                timeMillis = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        final Calendar c = Calendar.getInstance();

        switch (id) {
            case 0:
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
                        mDay);
            case 1:
                mYear = 0;
                mMonth = 0;
                mDay = 0;
                mYear1 = c.get(Calendar.YEAR);
                mMonth1 = c.get(Calendar.MONTH);
                mDay1 = c.get(Calendar.DAY_OF_MONTH);
                return new DatePickerDialog(this, mDateSetListener, mYear1, mMonth1,
                        mDay1);
        }
        return null;
    }

    private final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            String yyyy = String.valueOf(year);
            String mm;
            String dd;

            mm = String.valueOf(monthOfYear + 1);
            if (mm.length() < 2)
                mm = "0" + mm;

            dd = String.valueOf(dayOfMonth);
            if (dd.length() < 2)
                dd = "0" + dd;
            if(mYear > 0) {
                if (null == startTime) {
                    startTime = (EditText) findViewById(R.id.start_time);
                }
                if (null != startTime){
                    startTime.setText(yyyy + "-" + mm + "-" + dd);
                }
            }else{
                if(null == endTime) {
                    endTime = (EditText) findViewById(R.id.end_time);
                }
                if(null != endTime){
                    endTime.setText(yyyy + "-" + mm + "-" + dd);
                }
            }
        }
    };


}
