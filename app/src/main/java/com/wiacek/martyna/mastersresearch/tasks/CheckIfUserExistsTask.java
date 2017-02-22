package com.wiacek.martyna.mastersresearch.tasks;

import android.app.Activity;
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
import com.wiacek.martyna.mastersresearch.utils.SessionManager;

import java.net.HttpURLConnection;

/**
 * Created by Martyna on 2016-06-10.
 */
public class CheckIfUserExistsTask {

    private final Context mContext;
   // SessionManager sessionManager;
    private String[] urls;
    SessionManager sessionManager;

    public CheckIfUserExistsTask(Context context, String[] urls) {
        this.mContext = context;
     //   sessionManager = new SessionManager(mContext);
        this.urls = urls;
    }

    public void runVolley() {

        RequestQueue queue = Volley.newRequestQueue(mContext);
        final String url = ServerUrl.BASE_URL+"exist"+"?username="+urls[0];
        StringRequest dr = new StringRequest(Request.Method.GET, url,
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
        )
        {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                int mStatusCode = response.statusCode;


                if (mStatusCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    Intent intent = new Intent(mContext, PersonalInfoActivity.class);
                    Bundle b = new Bundle();
                    b.putString("username", urls[0]);
                    intent.putExtra("bundle",b);
                    ((Activity)mContext).finish();
                    mContext.startActivity(intent);
                }

                if (mStatusCode == HttpURLConnection.HTTP_OK){
                    Intent intent = new Intent(mContext, MainActivity.class);
                    sessionManager = new SessionManager(mContext);
                    sessionManager.createSession(urls[0]);
                    ((Activity)mContext).finish();
                    mContext.startActivity(intent);
                }

                return super.parseNetworkResponse(response);
            }
        };
        dr.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(dr);
    }
}

