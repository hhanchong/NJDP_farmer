package com.njdp.njdp_farmer.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NormalUtil {

    private static final String TAG = NormalUtil.class.getSimpleName();
    //错误信息提示
    public  static void error_hint(Context context,String str){
        Toast toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, -50);
        toast.show();
    }

    //EditText输入是否为空
    public static boolean isempty(EditText editText)
    {
        boolean bl= TextUtils.isEmpty(editText.getText());
        return bl;
    }

    //是否存在Sd卡
    public boolean ExistSDCard() {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }

    //返回存储路径
    public File getPath(Context context) {
        File savefile;
        if(ExistSDCard())
        {
            savefile= Environment.getExternalStorageDirectory();
            return savefile;
        }else {
            savefile=context.getCacheDir();
            return savefile;
        }
    }

    public File writeToFileFromInput(File file,InputStream input){

        OutputStream output = null;
        try {
            output = new FileOutputStream(file);
            byte buffer[] = new byte[4 * 1024];
            while((input.read(buffer)) != -1){
                output.write(buffer);
            }
            //清缓存，将流中的数据保存到文件中
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            try {
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
}

    //ProgressDialog的显示与隐藏
    public void showDialog(ProgressDialog pDialog) {
        if (!pDialog.isShowing())
            pDialog.show();
    }
    public void hideDialog(ProgressDialog pDialog) {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    //Bitmap缩放
    public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        return newbmp;
    }

    //保存照片到本地
    public boolean saveBitmap(Context context,Bitmap mBitmap) {
        File file;
        File tempFile=null;
        if(ExistSDCard())
        {
            file= Environment.getExternalStorageDirectory();
        }else
        {
            file=context.getCacheDir();
        }
        Bitmap bitmap = zoomBitmap(mBitmap, 400, 400);
        FileOutputStream fOut;
        if (!file.exists()) {
            try {
                file.mkdirs();
                tempFile=new File(file.getAbsolutePath(),"/njdpTemp");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            tempFile=new File(file.getAbsolutePath(),"/njdpTemp");
        }
            try {
                File file1=new File(tempFile,"userimage"+".png");
                fOut = new FileOutputStream(tempFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 60, fOut);
                fOut.flush();
                Log.d(TAG, "path:" + file1.getAbsolutePath().toString());
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "头像保存失败！请重试", Toast.LENGTH_SHORT).show();
                return false;
        }
        return true;
    }

}