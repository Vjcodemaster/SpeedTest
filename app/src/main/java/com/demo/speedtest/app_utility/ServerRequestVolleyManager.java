package com.demo.speedtest.app_utility;

import android.location.Location;

import androidx.lifecycle.ViewModel;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.demo.speedtest.speedtest_utils.URLInfo;
import com.demo.speedtest.ui.viewmodels.SpeedTestVM;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ServerRequestVolleyManager {
    int mStatusCode;
    ViewModel viewModel;
    HashMap<Integer, String> mapKey = new HashMap<>();
    HashMap<Integer, List<String>> mapValue = new HashMap<>();
    HashMap<String, URLInfo> mHMURLInfo = new HashMap<>();
    int count = 0;
    int countComparator = 0;
    String ip;
    double selfLat = 0.0;
    double selfLon = 0.0;
    String isp;
    double tmp = 19349458;

    String url;
    String lat;
    String lon;
    String name;
    String country;
    String cc;
    String sponsor;
    String host;

    public void volleyExecute(int nWhichTask, ViewModel viewModel) {
        this.viewModel = viewModel;
        Constants constants = Constants.values()[nWhichTask];
        switch (constants) {
            case SPEED_TEST_CONFIG:
                getSpeedTestConfig();
                break;
            case SPEED_TEST_SERVERS:
                getSpeedTestHostURLs();
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
                        //onCallSuccess(response);
                        /*String ip = response.split("client ip=\"")[1].split(" ")[0].replace("\"", "");
                        selfLat = Double.parseDouble(response.split("lat=\"")[1].split(" ")[0].replace("\"", ""));
                        selfLon = Double.parseDouble(response.split("lon=\"")[1].split(" ")[0].replace("\"", ""));
                        isp = response.split("isp=\"")[1].replace("\"", "");

                        HashMap<String, String> hmConfigData = new HashMap<>();
                        hmConfigData.put("ip", ip);
                        hmConfigData.put("lat", String.valueOf(selfLat));
                        hmConfigData.put("lon", String.valueOf(selfLon));
                        hmConfigData.put("isp", String.valueOf(isp));
                        SpeedTestVM speedTestVM = (SpeedTestVM) viewModel;
                        speedTestVM.postServerConfigData(hmConfigData);*/

                        HashMap<String, String> hmConfigData = new HashMap<>();
                        try {
                            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                            factory.setNamespaceAware(true);
                            XmlPullParser xpp = factory.newPullParser();

                            xpp.setInput(new StringReader(response));
                            int eventType = xpp.getEventType();
                            while (eventType != XmlPullParser.END_DOCUMENT) {
                                if (eventType == XmlPullParser.START_TAG) {
                                    for (int i = 0; i < xpp.getAttributeCount(); i++) {
                                        System.out.println("Attribute name: " + xpp.getAttributeName(i) + " - Attribute value: " + xpp.getAttributeValue(i));

                                        String sCase = xpp.getAttributeName(i);
                                        switch (sCase) {
                                            case "ip":
                                                hmConfigData.put("ip", xpp.getAttributeValue(i));
                                                break;
                                            case "lat":
                                                selfLat = Double.parseDouble(xpp.getAttributeValue(i));
                                                hmConfigData.put("lat", xpp.getAttributeValue(i));
                                                break;
                                            case "lon":
                                                selfLon = Double.parseDouble(xpp.getAttributeValue(i));
                                                hmConfigData.put("lon", xpp.getAttributeValue(i));
                                                break;
                                            case "isp":
                                                hmConfigData.put("isp", xpp.getAttributeValue(i));
                                                break;
                                        }
                                        if(hmConfigData.size()==4)
                                            break;
                                    }
                                }
                                if(hmConfigData.size()==4)
                                    break;
                                eventType = xpp.next();
                            }
                            SpeedTestVM speedTestVM = (SpeedTestVM) viewModel;
                            speedTestVM.postServerConfigData(hmConfigData);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mStatusCode == 401) {
                            // HTTP Status Code: 401 Unauthorized
                        }
                        //onCallFailed();
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

    private void getSpeedTestHostURLs() {
        StringRequest request = new StringRequest(
                Request.Method.GET, StaticReferenceClass.SPEED_TEST_SERVERS_URL, //BASE_URL + Endpoint.USER
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // Success
                        try {
                            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                            factory.setNamespaceAware(true);
                            XmlPullParser xpp = factory.newPullParser();

                            xpp.setInput(new StringReader(response));
                            int eventType = xpp.getEventType();
                            while (eventType != XmlPullParser.END_DOCUMENT) {
                                if (eventType == XmlPullParser.START_TAG) {
                                    for (int i = 0; i < xpp.getAttributeCount(); i++) {
                                        System.out.println("Attribute name: " + xpp.getAttributeName(i) + " - Attribute value: " + xpp.getAttributeValue(i));

                                        String sCase = xpp.getAttributeName(i);
                                        switch (sCase) {
                                            case "url":
                                                url = xpp.getAttributeValue(i);
                                                break;
                                            case "lat":
                                                lat = xpp.getAttributeValue(i);
                                                break;
                                            case "lon":
                                                lon = xpp.getAttributeValue(i);
                                                break;
                                            case "name":
                                                name = xpp.getAttributeValue(i);
                                                break;
                                            case "country":
                                                country = xpp.getAttributeValue(i);
                                                break;
                                            case "cc":
                                                cc = xpp.getAttributeValue(i);
                                                break;
                                            case "sponsor":
                                                sponsor = xpp.getAttributeValue(i);
                                                break;
                                            case "host":
                                                host = xpp.getAttributeValue(i);
                                                break;
                                        }
                                        if (i == xpp.getAttributeCount() - 1)
                                            count++;
                                    }
                                }
                                    /*if(count>mapKey.size()) {
                                        List<String> ls = Arrays.asList(lat, lon, name, country, cc, sponsor, host);
                                        mapKey.put(count, url);
                                        mapValue.put(count, ls);
                                        //Log.e("Size  of map", mapKey.size() + " " + mapValue.size());
                                    }*/
                                if (count > countComparator) {
                                    Location source = new Location("Source");
                                    source.setLatitude(selfLat);
                                    source.setLongitude(selfLon);

                                    Location dest = new Location("Dest");
                                    dest.setLatitude(Double.parseDouble(lat));
                                    dest.setLongitude(Double.parseDouble(lon));

                                    double distance = source.distanceTo(dest);
                                    if (distance < tmp) {
                                        tmp = distance;

                                        mHMURLInfo = new HashMap<>();
                                        URLInfo urlInfo = new URLInfo();
                                        urlInfo.setLat(Double.parseDouble(lat));
                                        urlInfo.setLon(Double.parseDouble(lon));
                                        urlInfo.setName(name);
                                        urlInfo.setCountry(country);
                                        urlInfo.setCC(cc);
                                        urlInfo.setSponsor(sponsor);
                                        urlInfo.setHost(host);
                                        mHMURLInfo.put(url, urlInfo);
                                        countComparator = count;
                                    }
                                }
                                eventType = xpp.next();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        SpeedTestVM speedTestVM = (SpeedTestVM) viewModel;
                        /*speedTestVM.postServerMapKey(mapKey);
                        speedTestVM.postServerMapValue(mapValue);*/
                        speedTestVM.postServerURLInfo(mHMURLInfo);
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mStatusCode == 401) {
                            // HTTP Status Code: 401 Unauthorized
                        }
                        //onCallFailed();
                    }
                }) {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                mStatusCode = response.statusCode;
                return super.parseNetworkResponse(response);
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        // add the request object to the queue to be executed
        ApplicationController.getInstance().addToRequestQueue(request);
    }
}
