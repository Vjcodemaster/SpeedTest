package com.demo.speedtest.speedtest_utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class GetSpeedTestHostsHandler extends Thread {

    HashMap<Integer, String> mapKey = new HashMap<>();
    HashMap<Integer, List<String>> mapValue = new HashMap<>();
    double selfLat = 0.0;
    double selfLon = 0.0;
    //String isp = "";
    String ip = "";
    boolean finished = false;


    public HashMap<Integer, String> getMapKey() {
        return mapKey;
    }

    public HashMap<Integer, List<String>> getMapValue() {
        return mapValue;
    }

    public double getSelfLat() {
        return selfLat;
    }

    public double getSelfLon() {
        return selfLon;
    }

    public String getIp(){
        return ip;
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public void run() {
        //Get latitude, longitude
        try {
            URL url = new URL("https://www.speedtest.net/speedtest-config.php");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            int code = urlConnection.getResponseCode();

            if (code == 200) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                urlConnection.getInputStream()));

                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.contains("isp=")) {
                        continue;
                    }
                    ip = line.split("client ip=\"")[1].split(" ")[0].replace("\"", "");
                    selfLat = Double.parseDouble(line.split("lat=\"")[1].split(" ")[0].replace("\"", ""));
                    selfLon = Double.parseDouble(line.split("lon=\"")[1].split(" ")[0].replace("\"", ""));
                    //int index = line.split("isp=\"")[1].indexOf("=");
                    //isp = line.split("isp=\"")[1].substring(0, index);
                    break;
                }

                br.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        //getCityName(ip);

        String uploadAddress;
        String name;
        String country;
        String cc;
        String sponsor;
        String lat;
        String lon;
        String host;


        //Best server
        int count = 0;
        try {
            URL url = new URL("https://www.speedtest.net/speedtest-servers-static.php");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            int code = urlConnection.getResponseCode();

            if (code == 200) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                urlConnection.getInputStream()));

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("<server url")) {
                        uploadAddress = line.split("server url=\"")[1].split("\"")[0];
                        lat = line.split("lat=\"")[1].split("\"")[0];
                        lon = line.split("lon=\"")[1].split("\"")[0];
                        name = line.split("name=\"")[1].split("\"")[0];
                        country = line.split("country=\"")[1].split("\"")[0];
                        cc = line.split("cc=\"")[1].split("\"")[0];
                        sponsor = line.split("sponsor=\"")[1].split("\"")[0];
                        host = line.split("host=\"")[1].split("\"")[0];

                        List<String> ls = Arrays.asList(lat, lon, name, country, cc, sponsor, host);
                        mapKey.put(count, uploadAddress);
                        mapValue.put(count, ls);
                        count++;
                    }
                }

                br.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        finished = true;
    }

    private String getCityName(String ipAddress){
        //String ip = "2.51.255.200";
        String sCityName = null;
        try {
            URL url = new URL("https://freegeoip.net/json/" + ipAddress);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream is = connection.getInputStream();

            int status = connection.getResponseCode();
            if (status != 200) {
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            for (String line; (line = reader.readLine()) != null; ) {
                //this API call will return something like:
                //"2.51.255.200", "AE", "United Arab Emirates", "03", "Dubai", "Dubai", "", "x-coord", "y-coord", "", ""
                // you can extract whatever you want from it
                if(line.split(",").length>5)
                sCityName = line.split(",")[5];
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return sCityName;
    }
}
