package com.njdp.njdp_farmer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.njdp.njdp_farmer.db.SessionManager;

public class MainLink extends AppCompatActivity {
    private String token;
    Intent intent;
    private SessionManager session;

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
        setContentView(R.layout.activity_main_link);

        token = getIntent().getStringExtra("TOKEN");
        if(token == null){
            error_hint("参数传输错误！");
            finish();
        }

        // Session manager
        session = new SessionManager(getApplicationContext());

        intent = new Intent(MainLink.this, mainpages.class);
        intent.putExtra("TOKEN", token);
    }

    //退出登录
    public void logoutClick(View v){
        intent = new Intent(MainLink.this, login.class);
        startActivity(intent);
        session.setLogin(false,false,"");
        finish();
    }

    //发布信息
    public void releaseDemandClick(View v){
        intent.putExtra("openModule", 1);
        startActivity(intent);
    }

    //查询农机
    public void machineSearchClick(View v){
        intent.putExtra("openModule", 2);
        startActivity(intent);
    }

    //个人信息
    public void peopleInfoClick(View v){
        intent.putExtra("openModule", 3);
        startActivity(intent);
    }

    //错误信息提示
    private void error_hint(String str) {
        Toast toast = Toast.makeText(MainLink.this, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
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
}
