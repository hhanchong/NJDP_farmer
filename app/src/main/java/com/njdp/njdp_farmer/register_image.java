package com.njdp.njdp_farmer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.njdp.njdp_farmer.bean.Farmer;
import com.njdp.njdp_farmer.changeDefault.NewClickableSpan;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.AppController;
import com.njdp.njdp_farmer.db.ImageUploadRequest;
import com.njdp.njdp_farmer.db.SQLiteHandler;
import com.njdp.njdp_farmer.db.SessionManager;
import com.njdp.njdp_farmer.util.NetUtil;
import com.njdp.njdp_farmer.util.NormalUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class register_image extends AppCompatActivity {

    private Button finish;
    private ImageButton getback;
    private TextView notice=null;
    private TextView setImage=null;
    private ImageView userImage=null;
    private String name;
    private String password;
    private String telephone;
    private String imageurl;
    private String s1="服务条款";
    private String s2="隐私协议";
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private String path;//用户头像路径
    private File tempFile;
    private String imageName;
    private Uri imageUri;
    private String Url_Image;
    private String Url;
    private int crop = 300;// 裁剪大小
    private static final int REQUEST_IMAGE=001;
    private static final int CROP_PHOTO_CODE = 002;
    private ArrayList<String> defaultDataArray;
    public boolean IsSetImage=false;
    private NormalUtil nutil=new NormalUtil();
    private static final String TAG = register_image.class.getSimpleName();
    private NetUtil netutil=new NetUtil();
    private Farmer farmer = new Farmer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_image);

        //修改
        Url=AppConfig.URL_REGISTER;
        Url_Image=AppConfig.URL_GETPASSWORD2;
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        this.notice = (TextView) super.findViewById(R.id.regiser_termsOfService);
        this.setImage = (TextView) super.findViewById(R.id.set_user_image);
        this.getback=(ImageButton) super.findViewById(R.id.getback);
        this.finish=(Button) super.findViewById(R.id.btn_registerFinish);
        this.userImage = (ImageView) super.findViewById(R.id.user_image);

        Bundle farmer_bundle=this.getIntent().getBundleExtra("farmer_register");
        if(farmer_bundle!=null)
        {
            name = farmer_bundle.getString("name");
            password = farmer_bundle.getString("password");
            telephone = farmer_bundle.getString("telephone");
        }else
        {
            error_hint("程序错误！请联系管理员！");
        }

        //服务条款
        Intent intent = new Intent(this, register_TermsofService.class);
        SpannableString span1 = new SpannableString(s1);
        SpannableString span2 = new SpannableString(s2);
        ClickableSpan clickableSpan1 = new NewClickableSpan(ContextCompat.getColor(this, R.color.colorDefault), this, intent);
        ClickableSpan clickableSpan2 = new NewClickableSpan(ContextCompat.getColor(this, R.color.colorDefault), this, intent);
        span1.setSpan(clickableSpan1, 0, s1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span2.setSpan(clickableSpan2, 0, s2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        notice.setText("注册即意味着您同意");
        notice.append(span1);
        notice.append("和");
        notice.append(span2);
        notice.setMovementMethod(LinkMovementMethod.getInstance());
        //设置Textview超链接高亮背景色为透明色
        notice.setHighlightColor(00000000);

        imageName="njdp_user_image.png";
        //设置头像本地存储路径
        if(nutil.ExistSDCard())
        {
            tempFile=Environment.getExternalStorageDirectory();
        }else
        {
            tempFile=getCacheDir();
        }
        path=tempFile.getAbsolutePath()+"/temp/"+imageName;

        //完成注册
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IsSetImage==true)
                {
                    //上传头像注册
                    register_uploadImage(Url_Image, path);
                }
                //默认头像注册
                register_finish(Url);
            }
        });

        //返回上个界面
        getback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //弹出图片选择菜单
        setImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == 1)
            return;

        switch (requestCode) {
            case REQUEST_IMAGE:
                if(resultCode == RESULT_OK){
                    // 获取返回的图片列表
                    List<String> paths = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                    path=paths.get(0);
                    File file=new File(path);
                    imageUri=Uri.fromFile(file);
                    cropPhoto();
                }
                break;
            case CROP_PHOTO_CODE:
                if (null != data)
                {
                    setCropImg(data);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //选择照片
    private void selectImage()
    {
        Intent intent = new Intent(register_image.this, MultiImageSelectorActivity.class);
        // 是否显示调用相机拍照
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        // 设置模式 (支持 单选/MultiImageSelectorActivity.MODE_SINGLE 或者 多选/MultiImageSelectorActivity.MODE_MULTI)
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_SINGLE);
        // 默认选择图片,回填选项(支持String ArrayList)
        intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, defaultDataArray);
        startActivityForResult(intent, REQUEST_IMAGE);
    }
    //裁剪图片
    private void cropPhoto() {

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(imageUri, "image/*");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", crop);
        intent.putExtra("outputY", crop);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_PHOTO_CODE);
    }
    //保存裁剪的照片
    private void setCropImg(Intent picdata) {
        Bundle bundle = picdata.getExtras();
        if (null != bundle) {
            Bitmap mBitmap = bundle.getParcelable("data");
            boolean tag= nutil.saveBitmap(register_image.this, mBitmap);
            if(tag)
            {
                userImage.setImageBitmap(mBitmap);
                IsSetImage=true;
            }
        }
    }

    //上传头像
    private void register_uploadImage(String url,String path) {

        File file=new File(path);
        String tag_string_req = "req_register_image";

        pDialog.setMessage("即将完成注册 ...");
        showDialog();

        if (!file.exists()) {
            hideDialog();
            Toast.makeText(register_image.this, "头像图片不存在!请重新选择！", Toast.LENGTH_SHORT).show();
            return;
        } else if (netutil.checkNet(register_image.this) == false) {
            hideDialog();
            error_hint("网络连接错误");
            return;
        } else {
            ImageUploadRequest register_request = new ImageUploadRequest(url,file, mSuccessListener_image, mErrorListener);
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(register_request, tag_string_req);
        }
    }

    //完成注册
    private void register_finish(String url) {

        // Tag used to cancel the request
        String tag_string_req = "req_register_image";

        pDialog.setMessage("即将完成注册 ...");
        showDialog();

        if (!netutil.checkNet(register_image.this)) {
            error_hint("网络连接错误");
            return;
        }else {
            StringRequest strReq = new StringRequest(Request.Method.POST,
                    url,mSuccessListener,mErrorListener) {

                @Override
                protected Map<String, String> getParams() {

                    Map<String, String> params = new HashMap<String, String>();
                        params.put("name", name);
                        params.put("password", password);
                        params.put("telephone", telephone);
                        params.put("setImage", "NO");
                        params.put("tag", "F");
                    return params;
                }
            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }

    //上传头像响应服务器成功
    private Response.Listener<String> mSuccessListener_image =new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            Log.d(TAG, "Register Response: " + response.toString());
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                boolean imageError=jObj.getBoolean("imageError");
                if (imageError)
                {
                    empty_hint(R.string.register_error3);
                    return;
                } else {
                    //头像上传成功，完成注册
                    register_finish(Url);
                }
            } catch (JSONException e) {
                empty_hint(R.string.register_error2);
                e.printStackTrace();
                Log.e(TAG, "RegisterError: " + e.getMessage());
            }
        }
    };

    //注册响应服务器成功
    private Response.Listener<String> mSuccessListener =new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            Log.d(TAG, "Register Response: 0-" + response.toString());
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");
                if(!error) {
                    //session.setLogin(true, false);
                    // Now store the user in sqlite
                    // Inserting row in users table
                    JSONObject farmers = jObj.getJSONObject("Farmers");
                    farmer.setId(farmers.getInt("Id"));
                    farmer.setName(farmers.getString("Name"));
                    farmer.setPassword(farmers.getString("Password"));
                    farmer.setTelephone(farmers.getString("Telephone"));
                    farmer.setImageUrl(farmers.getString("ImageUrl"));
                    db.addUser(farmer.getId(), farmer.getName(), farmer.getPassword(),farmer.getTelephone(),farmer.getImageUrl());
                    empty_hint(R.string.register_success);

                    // 跳转到主页面
                    Intent intent = new Intent(register_image.this, mainpages.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Error occurred in registration. Get the error
                    // message
                    String errorMsg = jObj.getString("error_msg");
                    Log.d(TAG, "Register Response: 1-" + errorMsg);
                    empty_hint(R.string.register_error1);
                }
            } catch (JSONException e) {
                empty_hint(R.string.register_error2);
                e.printStackTrace();
                Log.e(TAG, "RegisterError: 2-" + e.getMessage());
            }
        }
    };

    //响应服务器失败
    private Response.ErrorListener mErrorListener= new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "RegisterError: 3-" + error.getMessage());
            empty_hint(R.string.register_error2);
            hideDialog();
        }
    };

    //ProgressDialog显示与隐藏
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }
    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    //信息未输入提示
    private void empty_hint(int in) {
        Toast toast = Toast.makeText(register_image.this, getResources().getString(in), Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    //错误信息提示
    private void error_hint(String str) {
        Toast toast = Toast.makeText(register_image.this, str, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

}