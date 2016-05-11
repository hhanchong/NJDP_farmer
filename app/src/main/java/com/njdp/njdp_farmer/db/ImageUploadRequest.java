package com.njdp.njdp_farmer.db;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by USER-PC on 2016/4/13.
 */
public class ImageUploadRequest extends Request<String> {

    private MultipartEntityBuilder mBuilder = MultipartEntityBuilder.create();
    private final Response.Listener mListener;
    protected Map<String, String> params;

    public ImageUploadRequest(String url, File imageFile, String token, Response.Listener listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        mListener = listener;
        params = new HashMap<>();
        params.put("token", token);

        mBuilder.addBinaryBody("record_image", imageFile, ContentType.create("image/jpeg"), imageFile.getName());
        mBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        mBuilder.setLaxMode().setBoundary("xx").setCharset(Charset.forName("UTF-8"));
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            mBuilder.build().writeTo(bos);
        } catch (IOException e){
            VolleyLog.e("IOException writing to ByteArrayOutputStream bos, building the multipart request.");
        }
        return bos.toByteArray();
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }

    @Override
    public String getBodyContentType() {
        return mBuilder.build().getContentType().getValue();
    }

    @Override
    protected Response parseNetworkResponse(NetworkResponse response) {
        return Response.success(new String(response.data), HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    public void deliverError(VolleyError error) {
        super.deliverError(error);
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}
