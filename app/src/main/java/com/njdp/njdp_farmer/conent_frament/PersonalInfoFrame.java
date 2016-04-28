package com.njdp.njdp_farmer.conent_frament;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.njdp.njdp_farmer.FarmerLandList;
import com.njdp.njdp_farmer.PersonalSet;
import com.njdp.njdp_farmer.R;

public class PersonalInfoFrame extends Fragment implements View.OnClickListener {

    //所有监听的控件
    static ImageView userImage;
    TextView userName, telephone, qq, weixin, address, myrelease;
    Button personalEdit;
    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            if (view == null) {
                view = inFlater(inflater);
            }
            return view;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public View inFlater(LayoutInflater inflater) {
        view = inflater.inflate(R.layout.activity_personal_info, null, false);
        initView(view);

        return view;
    }

    private void initView(View view) {
        userImage = (ImageView) view.findViewById(R.id.user_image);
        userName = (TextView) view.findViewById(R.id.tv_user_name);
        telephone = (TextView) view.findViewById(R.id.tv_phonenum);
        qq = (TextView) view.findViewById(R.id.tv_qq);
        weixin = (TextView) view.findViewById(R.id.tv_weixin);
        address = (TextView) view.findViewById(R.id.tv_address);
        myrelease = (TextView) view.findViewById(R.id.tv_my_release);
        personalEdit = (Button) view.findViewById(R.id.btn_edit);

        initOnClick();
    }

    private void initOnClick() {
        personalEdit.setOnClickListener(this);
        myrelease.setOnClickListener(this);
        //Thread.setOnClickListener(this);
        //myMessage.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // TODO: 2015/11/18 头像
            case R.id.btn_edit:
                Log.e("------------->", "点击修改用户信息");
                Intent intent1 = new Intent(getActivity(), PersonalSet.class);
                startActivity(intent1);
                break;
            case R.id.tv_my_release:
                Log.e("------------->", "查看我的发布信息");
                Intent intent2 = new Intent(getActivity(), FarmerLandList.class);
                startActivity(intent2);
                break;
        }
    }
    public static Handler handle=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case 1:
                    //userName.setText("立即登录");
                    //goldNumber.setVisibility(View.VISIBLE);
                    //jinbiCount.setVisibility(View.GONE);
                    //picture.setImageResource(R.mipmap.biz_tie_user_avater_default_common);
                    //flag=false;
                    break;
                case 2:
                    Bitmap bp= (Bitmap) msg.obj;
                    if (bp!=null){
                        //picture.setImageBitmap(bp);
                    }
                    break;
            }

        }
    };
}
