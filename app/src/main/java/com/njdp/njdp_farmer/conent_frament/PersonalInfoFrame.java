package com.njdp.njdp_farmer.conent_frament;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.njdp.njdp_farmer.FarmerLandList;
import com.njdp.njdp_farmer.PersonalSet;
import com.njdp.njdp_farmer.R;
import com.njdp.njdp_farmer.bean.FarmlandInfo;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.login;
import com.njdp.njdp_farmer.util.NetUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class PersonalInfoFrame extends Fragment implements View.OnClickListener {
    private final String TAG = "PersonalInfoFrame";
    //所有监听的控件
    static ImageView userImage;
    TextView userName, telephone, qq, weixin, address, myrelease;
    Button personalEdit;
    View view;
    private ProgressDialog pDialog;
    private NetUtil netutil = new NetUtil();
    private String token;
    List<FarmlandInfo> farmlandInfos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            Bundle bundle = getArguments();
            token = bundle.getString("token");
            if (token == null) {
                error_hint("参数传递错误！");
                return null;
            }
            farmlandInfos = new List<FarmlandInfo>() {
                @Override
                public void add(int location, FarmlandInfo object) {

                }

                @Override
                public boolean add(FarmlandInfo object) {
                    return false;
                }

                @Override
                public boolean addAll(int location, Collection<? extends FarmlandInfo> collection) {
                    return false;
                }

                @Override
                public boolean addAll(Collection<? extends FarmlandInfo> collection) {
                    return false;
                }

                @Override
                public void clear() {

                }

                @Override
                public boolean contains(Object object) {
                    return false;
                }

                @Override
                public boolean containsAll(Collection<?> collection) {
                    return false;
                }

                @Override
                public FarmlandInfo get(int location) {
                    return null;
                }

                @Override
                public int indexOf(Object object) {
                    return 0;
                }

                @Override
                public boolean isEmpty() {
                    return false;
                }

                @NonNull
                @Override
                public Iterator<FarmlandInfo> iterator() {
                    return null;
                }

                @Override
                public int lastIndexOf(Object object) {
                    return 0;
                }

                @Override
                public ListIterator<FarmlandInfo> listIterator() {
                    return null;
                }

                @NonNull
                @Override
                public ListIterator<FarmlandInfo> listIterator(int location) {
                    return null;
                }

                @Override
                public FarmlandInfo remove(int location) {
                    return null;
                }

                @Override
                public boolean remove(Object object) {
                    return false;
                }

                @Override
                public boolean removeAll(Collection<?> collection) {
                    return false;
                }

                @Override
                public boolean retainAll(Collection<?> collection) {
                    return false;
                }

                @Override
                public FarmlandInfo set(int location, FarmlandInfo object) {
                    return null;
                }

                @Override
                public int size() {
                    return 0;
                }

                @NonNull
                @Override
                public List<FarmlandInfo> subList(int start, int end) {
                    return null;
                }

                @NonNull
                @Override
                public Object[] toArray() {
                    return new Object[0];
                }

                @NonNull
                @Override
                public <T> T[] toArray(T[] array) {
                    return null;
                }
            };
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
        getFarmlandInfos();
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
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

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

    public void getFarmlandInfos() {

        String tag_string_req = "req_farmland_get";

        pDialog.setMessage("正在获取个人数据 ...");
        showDialog();

        if (netutil.checkNet(getActivity()) == false) {
            hideDialog();
            error_hint("网络连接错误");
            return;
        } else {
            //服务器请求
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_FARMLAND_GET, mSuccessListener, mErrorListener) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to url
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("token", token);
                    return params;
                }
            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }

    //响应服务器成功
    private Response.Listener<String> mSuccessListener = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            Log.i("tagconvertstr", "[" + response + "]");
            Log.d(TAG, "Release Response: " + response.toString());
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                int status = jObj.getInt("status");

                // Check for error node in json
                if (status == 0) {
                    // user successfully logged in
                    error_hint("发布成功！");

                } else if(status == 1){
                    //密匙失效
                    error_hint("用户登录过期，请重新登录！");
                    Intent intent = new Intent(getContext(), login.class);
                    startActivity(intent);
                    getActivity().finish();
                }
                else{
                    error_hint("其他未知错误！");
                }
            } catch (JSONException e) {
                empty_hint(R.string.connect_error);
                // JSON error
                e.printStackTrace();
                Log.e(TAG, "Json error：response错误！" + e.getMessage());
            }
        }
    };

    //响应服务器失败
    private Response.ErrorListener mErrorListener = new Response.ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "Release Error: " + error.getMessage());
            error_hint("服务器连接失败");
            hideDialog();
        }
    };

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
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
