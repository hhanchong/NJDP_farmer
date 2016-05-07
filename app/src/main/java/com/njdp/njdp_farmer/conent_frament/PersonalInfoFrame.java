package com.njdp.njdp_farmer.conent_frament;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.njdp.njdp_farmer.FarmerLandList;
import com.njdp.njdp_farmer.PersonalSet;
import com.njdp.njdp_farmer.R;
import com.njdp.njdp_farmer.MyClass.Farmer;
import com.njdp.njdp_farmer.MyClass.FarmlandInfo;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.login;
import com.njdp.njdp_farmer.mainpages;
import com.njdp.njdp_farmer.util.NetUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PersonalInfoFrame extends Fragment implements View.OnClickListener {
    private final String TAG = "PersonalInfoFrame";
    //所有监听的控件
    static ImageView userImage;
    TextView userName, telephone, qq, weixin, address;
    Button personalEdit;
    View view;
    private ProgressDialog pDialog;
    private String token;
    private Farmer farmer;
    ArrayList<FarmlandInfo> farmlandInfos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            Bundle bundle = getArguments();
            token = bundle.getString("token");
            farmer = (Farmer)bundle.getSerializable("farmer");
            //自测用户
            farmer = new Farmer();
            farmer.setFm_token(token);
            farmer.setName("李占伟");
            farmer.setImageUrl("@drawable/ic_launcher");
            farmer.setTelephone("18932659760");
            farmer.setQQ("842558891");
            farmer.setWeixin("ZhiHuiNongJi");
            farmer.setAddress("河北省保定市清苑县***乡***村");
            //判断参数传递是否正确
            if (token == null || farmer == null) {
                error_hint("参数传递错误！");
                return null;
            }
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
        //userImage.setImageURI();
        userName = (TextView) view.findViewById(R.id.tv_user_name);
        userName.setText(farmer.getName());
        telephone = (TextView) view.findViewById(R.id.tv_phonenum);
        telephone.setText(farmer.getTelephone());
        qq = (TextView) view.findViewById(R.id.tv_qq);
        qq.setText(farmer.getQQ());
        weixin = (TextView) view.findViewById(R.id.tv_weixin);
        weixin.setText(farmer.getWeixin());
        address = (TextView) view.findViewById(R.id.tv_address);
        address.setText(farmer.getAddress());
        personalEdit = (Button) view.findViewById(R.id.btn_edit);
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        initOnClick();
    }

    private void initOnClick() {
        personalEdit.setOnClickListener(this);
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
                intent1.putExtra("user", farmer);
                startActivity(intent1);
                break;
        }
    }

    //错误信息提示1
    private void error_hint(String str) {
        Toast toast = Toast.makeText(getActivity(), str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    //错误信息提示2
    private void empty_hint(int in) {
        Toast toast = Toast.makeText(getActivity(), getResources().getString(in), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

}
