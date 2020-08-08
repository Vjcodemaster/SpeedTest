package com.demo.speedtest.speedtest_utils;

import android.util.Log;

import com.demo.speedtest.ui.viewmodels.SpeedTestVM;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * @author erdigurbuz
 */
public class PingTest extends Thread {

    HashMap<String, Object> result = new HashMap<>();
    String server = "";
    int count;
    double instantRtt = 0;
    double avgRtt = 0.0;
    boolean finished = false;
    boolean started = false;
    SpeedTestVM speedTestVM;

    public PingTest(String serverIpAddress, int pingTryCount) {
        this.server = serverIpAddress;
        this.count = pingTryCount;
    }

    public PingTest(String serverIpAddress, int pingTryCount, SpeedTestVM speedTestVM) {
        this.server = serverIpAddress;
        this.count = pingTryCount;
        this.speedTestVM = speedTestVM;
    }

    public double getAvgRtt() {
        return avgRtt;
    }

    public double getInstantRtt() {
        return instantRtt;
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public void run() {
        try {
            ProcessBuilder ps = new ProcessBuilder("ping", "-c " + count, this.server);

            ps.redirectErrorStream(true);
            Process pr = ps.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.contains("icmp_seq")) {
                    instantRtt = Double.parseDouble(line.split(" ")[line.split(" ").length - 2].replace("time=", ""));
                    speedTestVM.postPingData(String.valueOf(instantRtt));
                }
                if (line.startsWith("rtt ")) {
                    avgRtt = Double.parseDouble(line.split("/")[4]);
                    break;
                }
                if (line.contains("Unreachable") || line.contains("Unknown") || line.contains("%100 packet loss")) {
                    speedTestVM.postPingTest(false);
                    return;
                }
            }
            pr.waitFor();
            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        //double value = getLatency(this.server);
        //get(this.server);
        finished = true;
        speedTestVM.postPingTest(true);
    }

    /*
Returns the latency to a given server in mili-seconds by issuing a ping command.
system will issue NUMBER_OF_PACKTETS ICMP Echo Request packet each having size of 56 bytes
every second, and returns the avg latency of them.
Returns 0 when there is no connection
 */
    public double getLatency(String ipAddress){
        String pingCommand = "/system/bin/ping -c " + 10 + " " + ipAddress;
        //String pingCommand = "/system/bin/ping" + " " + ipAddress;
        String inputLine = "";
        double avgRtt;

        try {
            // execute the command on the environment interface
            Process process = Runtime.getRuntime().exec(pingCommand);
            // gets the input stream to get the output of the executed command
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            inputLine = bufferedReader.readLine();
            while ((inputLine != null)) {
                if (inputLine.length() > 0 && inputLine.contains("avg")) {  // when we get to the last line of executed ping command
                    break;
                }
                inputLine = bufferedReader.readLine();
            }
        }
        catch (IOException e){
            Log.v("Latency", "getLatency: EXCEPTION");
            e.printStackTrace();
        }

        // Extracting the average round trip time from the inputLine string
        String afterEqual = inputLine.substring(inputLine.indexOf("="), inputLine.length()).trim();
        String afterFirstSlash = afterEqual.substring(afterEqual.indexOf('/') + 1, afterEqual.length()).trim();
        String strAvgRtt = afterFirstSlash.substring(0, afterFirstSlash.indexOf('/'));
        avgRtt = Double.parseDouble(strAvgRtt);

        return avgRtt;
    }

    /*private void get(String ip){
        //String ip="www.google.com";
        String pingResult="  ";
        String pingCmd="ping"+ip;

        try {
            Runtime r = Runtime.getRuntime();
            //Process p = r.exec(pingCmd);
            Process p=r.exec(new String[] {"ping", "-c 4", ip});

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String inputLine;
            //Toast.makeText(getApplicationContext(), "Going loop", 1).show();
            while ((inputLine = in.readLine()) != null) {
                pingResult = inputLine;
            }
            //Toast.makeText(getApplicationContext(), pingResult, 1).show();
            in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }*/

}
