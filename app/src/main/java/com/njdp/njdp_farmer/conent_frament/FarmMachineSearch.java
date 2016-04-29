package com.njdp.njdp_farmer.conent_frament;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.njdp.njdp_farmer.R;

public class FarmMachineSearch extends Fragment implements View.OnClickListener {
    private String token;
    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        try {
            if (view == null) {
                view = inFlater(inflater);
            }
            Bundle bundle = getArguments();
            token = bundle.getString("token");
            if (token == null) {
                error_hint("参数传递错误！");
                return null;
            }
            return view;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
        //return inflater.inflate(R.layout.frament_farm_machine, container, false);
    }

    public View inFlater(LayoutInflater inflater) {
        view = inflater.inflate(R.layout.activity_farm_machine_search, null, false);
        initView(view);

        return view;
    }

    private void initView(View view) {


        initOnClick();
    }

    private void initOnClick() {

        //Thread.setOnClickListener(this);
        //myMessage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //1.得到InputMethodManager对象
        //InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        //2.调用toggleSoftInput方法，实现切换显示软键盘的功能。
        //imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        switch (v.getId()) {
            // TODO: 2015/11/18 头像
            case R.id.btn_editFinish:
                Log.e("------------->", "点击发布农田信息");

                break;
            case R.id.address:

                break;
            case R.id.start_time:

                break;
            case R.id.end_time:

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
