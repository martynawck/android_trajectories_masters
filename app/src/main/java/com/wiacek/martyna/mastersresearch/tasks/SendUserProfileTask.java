package com.wiacek.martyna.mastersresearch.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wiacek.martyna.mastersresearch.activities.MainActivity;
import com.wiacek.martyna.mastersresearch.activities.PersonalInfoActivity;
import com.wiacek.martyna.mastersresearch.utils.ServerUrl;

import java.net.HttpURLConnection;

/**
 * Created by Martyna on 2016-06-11.
 */
public class SendUserProfileTask {

    private final Context mContext;
    private String[] urls;

    public SendUserProfileTask ( Context context, String[] urls) {
        this.mContext = context;
        this.urls = urls;
    }

    public void runVolley() {

        RequestQueue queue = Volley.newRequestQueue(mContext);

        String url = ServerUrl.BASE_URL+"user?username="+urls[0]+"&workStatus="+urls[5]+
                "&sex="+urls[1]+"&birthYear="+urls[2]+"&education="+urls[4]+"&isFromWarsaw="+urls[3]+"&relationship="+urls[6]+"&transportation="+urls[7];
        StringRequest dr = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        );

        dr.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(dr);
    }
}
