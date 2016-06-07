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
    public boolean saveBitmap(Context context,String telephone,Bitmap mBitmap) {
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
            //未获取到根目录，返回错误
            return false;
        }
        try {
            tempFile=new File(file.getAbsolutePath(),"/NJDP/" + telephone + "/photo/");
            if(!tempFile.exists()){
                if(!createFilePath(tempFile)){
                    Toast.makeText(context, "文件目录创建失败！", Toast.LENGTH_SHORT).show();
                }
            }
            File file1=new File(tempFile,"userimage000.png");
            if(!file1.exists()){
                file1.createNewFile();
            }
            fOut = new FileOutputStream(file1);
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

    //创建文件目录
    public boolean createFilePath(File file){
        try{
            if(!file.getParentFile().exists()){
                //父节点创建失败
                if(!createFilePath(file.getParentFile())){
                    return false;
                }
            }
            if(!file.exists()){
                if (file.isFile())
                    file.createNewFile();
                else
                    file.mkdir();
            }
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

}