package com.njdp.njdp_farmer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.njdp.njdp_farmer.bean.Farmer;

public class MainLink extends AppCompatActivity {
    private String token;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_link);

        token = getIntent().getStringExtra("TOKEN");
        if(token == null){
            error_hint("参数传输错误！");
            finish();
        }

        intent = new Intent(MainLink.this, mainpages.class);
        intent.putExtra("TOKEN", token);
    }

    //发布信息
    public void releaseDemandClick(View v){
        intent.putExtra("openModule", 1);
        startActivity(intent);
        finish();
    }

    //查询农机
    public void machineSearchClick(View v){
        intent.putExtra("openModule", 2);
        startActivity(intent);
        finish();
    }

    //个人信息
    public void peopleInfoClick(View v){
        intent.putExtra("openModule", 3);
        startActivity(intent);
        finish();
    }

    //错误信息提示
    private void error_hint(String str) {
        Toast toast = Toast.makeText(MainLink.this, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }
}
