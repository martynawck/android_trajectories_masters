package com.wiacek.martyna.mastersresearch.tasks;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wiacek.martyna.mastersresearch.utils.ServerUrl;

/**
 * Created by Martyna on 2016-06-10.
 */
public class SendUserLocationTask {

    private final Context mContext;
    private String[] urls;

    public SendUserLocationTask ( Context context, String[] urls) {
        this.mContext = context;
        this.urls = urls;
    }

    public void runVolley() {

        RequestQueue queue = Volley.newRequestQueue(mContext);
        String url = ServerUrl.BASE_URL+"location?username="+urls[0]+"&client_timestamp="+urls[1] +
                "&latitude="+urls[2]+"&longitude="+urls[3];
        StringRequest dr = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.d("RESPONSE",response);
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

