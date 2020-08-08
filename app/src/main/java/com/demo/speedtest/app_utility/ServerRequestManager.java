package com.demo.speedtest.app_utility;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;


public abstract class ServerRequestManager {
    int mStatusCode;

    /*public ServerRequestManager(int nWhichTask) {
        volleyExecute(nWhichTask);
    }*/

    public void volleyExecute(int nWhichTask) {
        Constants constants = Constants.values()[nWhichTask];
        switch (constants) {
            case SPEED_TEST_CONFIG:
                getSpeedTestConfig();
                break;
            case SPEED_TEST_SERVERS:

                break;
        }
    }

    private void getSpeedTestConfig() {
        StringRequest request = new StringRequest(
                Request.Method.GET, StaticReferenceClass.SPEED_TEST_CONFIG_URL, //BASE_URL + Endpoint.USER
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Success
                        onCallSuccess(response);

                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mStatusCode == 401) {
                            // HTTP Status Code: 401 Unauthorized
                        }
                        onCallFailed();
                    }
                }) {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                mStatusCode = response.statusCode;
                return super.parseNetworkResponse(response);
            }

            /*@Override
            public byte[] getBody() {
                return new JSONObject(params).toString().getBytes();
            }*/

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        // add the request object to the queue to be executed
        ApplicationController.getInstance().addToRequestQueue(request);
    }

    public void parseXml(String sResponse) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(sResponse)); // pass input whatever xml you have
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    Log.d("TAG", "Start document");
                } else if (eventType == XmlPullParser.START_TAG) {
                    Log.d("TAG", "Start tag " + xpp.getName());
                } else if (eventType == XmlPullParser.END_TAG) {
                    Log.d("TAG", "End tag " + xpp.getName());
                } else if (eventType == XmlPullParser.TEXT) {
                    Log.d("TAG", "Text " + xpp.getText()); // here you get the text from xml
                }
                eventType = xpp.next();
            }
            Log.d("TAG", "End document");

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void addParameters();

    public abstract void onCallSuccess(String sResponse);

    public abstract void onCallFailed();
}
