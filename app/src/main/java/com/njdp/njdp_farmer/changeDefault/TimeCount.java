package com.njdp.njdp_farmer.changeDefault;

import android.app.Activity;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.widget.Button;

public class TimeCount extends CountDownTimer {
    private Activity activity;
    private Button btn;

    // 在这个构造方法里需要传入4个参数，一个是Activity，一个总时间millisInFuture，一个间隔时间countDownInterval,一个按钮
    public TimeCount (Activity activity,long millisInFuture, long countDownInterval,Button btn) {
        super(millisInFuture, countDownInterval);
        this.activity = activity;
        this.btn =btn;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        btn.setEnabled(false);//设置不能点击
        btn.setText(millisUntilFinished / 1000 + "秒后可重新获取");//设置倒计时时间
        btn.setTextColor(Color.BLACK);//倒计时，文字显示为黑色

    }
    @Override
    public void onFinish() {
        btn.setText("重新获取\n验证码");
        btn.setTextColor(Color.WHITE);//倒计时结束，文字显示为白色
        btn.setEnabled(true);

    }
}
