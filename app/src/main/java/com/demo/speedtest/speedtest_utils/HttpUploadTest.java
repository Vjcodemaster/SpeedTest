package com.demo.speedtest.speedtest_utils;


import com.demo.speedtest.ui.viewmodels.SpeedTestVM;

import java.io.DataOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

import static com.demo.speedtest.speedtest_utils.HttpUploadTest.uploadedKByte;

/**
 * @author erdigurbuz
 */
public class HttpUploadTest extends Thread {

    public String fileURL = "";
    static int uploadedKByte = 0;
    double uploadElapsedTime = 0;
    boolean finished = false;
    double elapsedTime = 0;
    double finalUploadRate = 0.0;
    long startTime;
    public SpeedTestVM speedTestVM;
    double instantUploadRate = 0.0;
    public HttpUploadTest uploadTest;
    public int count = 0;

    public HttpUploadTest(String fileURL) {
        this.fileURL = fileURL;
    }

    public HttpUploadTest(String fileURL, SpeedTestVM speedTestVM) {
        this.fileURL = fileURL;
        this.speedTestVM = speedTestVM;
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd;
        try {
            bd = new BigDecimal(value);
        } catch (Exception ex) {
            return 0.0;
        }
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public boolean isFinished() {
        return finished;
    }

    public double getInstantUploadRate() {
        /*try {
            BigDecimal bd = new BigDecimal(uploadedKByte);
        } catch (Exception ex) {
            return 0.0;
        }*/

        if (uploadedKByte >= 0) {
            long now = System.currentTimeMillis();
            elapsedTime = (now - startTime) / 1000.0;
            return round((Double) (((uploadedKByte / 1000.0) * 8) / elapsedTime), 2);
        } else {
            return 0.0;
        }
    }

    public void setInstantUploadRate(int uploadedKByte, double elapsedTime) {

        if (uploadedKByte >= 0) {
            instantUploadRate = round((Double) (((uploadedKByte / 1000.0) * 8) / elapsedTime), 2);
            speedTestVM.postUploadSpeed(String.valueOf(instantUploadRate));
        } else {
            instantUploadRate = 0.0;
        }
    }

    public double getFinalUploadRate() {
        return round(finalUploadRate, 2);
    }

    @Override
    public void run() {
        try {
            URL url = new URL(fileURL);
            uploadedKByte = 0;
            startTime = System.currentTimeMillis();

            ExecutorService executor = Executors.newFixedThreadPool(4);
            for (int i = 0; i < 4; i++) {
                executor.execute(new HandlerUpload(url, speedTestVM, uploadTest));
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            long now = System.currentTimeMillis();
            uploadElapsedTime = (now - startTime) / 1000.0;
            finalUploadRate = (Double) (((uploadedKByte / 1000.0) * 8) / uploadElapsedTime);
            //speedTestVM.postUploadSpeed(String.valueOf(finalUploadRate));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finished = true;
    }
}

class HandlerUpload extends Thread {

    URL url;
    //double instantUploadRate = 0;
    SpeedTestVM speedTestVM;
    HttpUploadTest uploadTest;

    public HandlerUpload(URL url, SpeedTestVM speedTestVM, HttpUploadTest uploadTest) {
        this.url = url;
        this.speedTestVM = speedTestVM;
        this.uploadTest = uploadTest;
    }

    public void run() {
        byte[] buffer = new byte[150 * 1024];
        long startTime = System.currentTimeMillis();
        int timeout = 8;

        while (true) {

            try {
                HttpsURLConnection conn = null;
                conn = (HttpsURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
                conn.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                conn.connect();
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());


                dos.write(buffer, 0, buffer.length);
                dos.flush();

                conn.getResponseCode();

                uploadedKByte += buffer.length / 1024.0;
                long endTime = System.currentTimeMillis();
                double uploadElapsedTime = (endTime - startTime) / 1000.0;
                uploadTest.setInstantUploadRate(uploadedKByte, uploadElapsedTime);
                //speedTestVM.postUploadSpeed(String.valueOf(round((Double) (((uploadedKByte / 1000.0) * 8) / uploadElapsedTime), 2)));
                //setInstantUploadRate(uploadedKByte, uploadElapsedTime);
                //getInstantUploadRate(uploadElapsedTime, startTime);
                //speedTestVM.postUploadSpeed(String.valueOf(getInstantUploadRate(uploadElapsedTime, startTime)));
                if (uploadElapsedTime >= timeout) {
                    uploadTest.count++;
                    if(uploadTest.count==4)
                        uploadTest.finished = true;
                    //Log.e("Breakout time", uploadElapsedTime + "");
                    break;
                }

                dos.close();
                conn.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
            //uploadTest.finished = true;
        }
    }
}
