package com.njdp.njdp_farmer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.njdp.njdp_farmer.changeDefault.NewClickableSpan;
import com.njdp.njdp_farmer.db.AppConfig;
import com.njdp.njdp_farmer.db.SQLiteHandler;
import com.njdp.njdp_farmer.util.NetUtil;
import com.njdp.njdp_farmer.util.NormalUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import okhttp3.Call;

public class register_image extends AppCompatActivity {

    private Button finish;
    private ImageButton getback;
    private TextView notice=null;
    private TextView setImage=null;
    private ImageView userImage=null;
    private String s1="服务条款";
    private String s2="隐私协议";
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private String path;//用户头像路径
    private File tempFile;
    private String imageName;
    private Uri imageUri;
    private String Url_Image;
    private String Url;
    private int crop = 300;// 裁剪大小
    private final int REQUEST_IMAGE=001;
    private final int CROP_PHOTO_CODE = 002;
    private ArrayList<String> defaultDataArray;
    public boolean IsSetImage=false;
    private NormalUtil nutil=new NormalUtil();
    private static final String TAG = register_image.class.getSimpleName();
    private NetUtil netutil=new NetUtil();
    private String token;
    private String telephone;

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
        setContentView(R.layout.activity_register_image);

        //修改
        Url=AppConfig.URL_REGISTER;
        Url_Image=AppConfig.URL_USERINFO_EDIT;
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        this.notice = (TextView) super.findViewById(R.id.regiser_termsOfService);
        this.setImage = (TextView) super.findViewById(R.id.set_user_image);
        this.getback=(ImageButton) super.findViewById(R.id.getback);
        this.finish=(Button) super.findViewById(R.id.btn_registerFinish);
        this.userImage = (ImageView) super.findViewById(R.id.user_image);

        token = getIntent().getStringExtra("token");
        telephone = getIntent().getStringExtra("telephone");
        IsSetImage = getIntent().getBooleanExtra("IsSetImage", false);
        if(token==null&&telephone==null) {
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

        imageName="njdp_" +telephone + "_image.png";
        //设置头像本地存储路径
        if(nutil.ExistSDCard()) {
            tempFile=Environment.getExternalStorageDirectory();
        }else {
            tempFile=getCacheDir();
        }
        path=tempFile.getAbsolutePath()+"/NJDP/"+imageName;

        //完成注册
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IsSetImage)
                {
                    //上传头像注册
                    register_uploadImage(Url_Image, path);
                }
                //默认头像注册
                //register_finish(Url);
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
        String tag_string_req = "req_edit_image";

        pDialog.setMessage("正在上传图片 ...");
        showDialog();

        if (!file.exists()) {
            hideDialog();
            Toast.makeText(register_image.this, "头像图片不存在!请重新选择！", Toast.LENGTH_SHORT).show();
        } else if (!netutil.checkNet(register_image.this)) {
            hideDialog();
            error_hint("网络连接错误");
        } else {
            OkHttpUtils.post()
                    .url(url)
                    .addParams("token", token)
                    .addFile("person_photo", imageName, file)
                    .addHeader("content-disposition","form-data")
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e) {
                            Log.e(TAG, "3 Connect Error: " + e.getMessage());
                        }

                        @Override
                        public void onResponse(String response) {
                            try {
                                Log.e(TAG, "UploadImage:" + response);
                                JSONObject jObj = new JSONObject(response);
                                int status = jObj.getInt("status");
                                if (status == 0) {
                                    String msg = jObj.getString("result");
                                    Log.e(TAG, "UploadImage response：" + msg);
                                } else {
                                    String errorMsg = jObj.getString("error_msg");
                                    Log.e(TAG, "1 Json error：response错误：" + errorMsg);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "2 Json error：response错误： " + e.getMessage());
                            }
                        }
                    });
        }
    }

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