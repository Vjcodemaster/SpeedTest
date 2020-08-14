package com.demo.speedtest.ui.fragments;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.demo.speedtest.R;
import com.demo.speedtest.app_utility.ServerRequestVolleyManager;
import com.demo.speedtest.databinding.SpeedTestFragmentBinding;
import com.demo.speedtest.speedtest_utils.GetSpeedTestHostsHandler;
import com.demo.speedtest.speedtest_utils.HttpDownloadTest;
import com.demo.speedtest.speedtest_utils.HttpUploadTest;
import com.demo.speedtest.speedtest_utils.PingTest;
import com.demo.speedtest.speedtest_utils.URLInfo;
import com.demo.speedtest.ui.viewmodels.SpeedTestVM;
import com.github.anastr.speedviewlib.Gauge;
import com.github.anastr.speedviewlib.components.Section;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.demo.speedtest.app_utility.StaticReferenceClass.SPEED_TEST_CONFIG;
import static com.demo.speedtest.app_utility.StaticReferenceClass.SPEED_TEST_SERVERS;

public class SpeedTestFragment extends Fragment {
    GetSpeedTestHostsHandler getSpeedTestHostsHandler = null;

    private SpeedTestVM mSpeedTestVM;
    private SpeedTestFragmentBinding speedTestFragmentBinding;

    boolean uploadTestFinished = false;
    boolean downloadTestFinished = false;
    boolean uploadTestStarted = false;
    int ANIM_DURATION = 0;
    final DecimalFormat dec = new DecimalFormat("#.##");

    static int position = 0;
    static int lastPosition = 0;

    RotateAnimation rotate;
    private HashMap<String, String> hmConfigData = new HashMap<>();
    private HashMap<String, URLInfo> mHMURLInfo = new HashMap<>();
    String sURL;
    ServerRequestVolleyManager serverRequestVolleyManager;
    HttpDownloadTest downloadTest;
    HttpUploadTest uploadTest;
    Runnable task;

    public static SpeedTestFragment newInstance() {
        return new SpeedTestFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpeedTestVM = new ViewModelProvider(this).get(SpeedTestVM.class);
        getSpeedTestHostsHandler = new GetSpeedTestHostsHandler();
        getSpeedTestHostsHandler.start();

        serverRequestVolleyManager = new ServerRequestVolleyManager();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        speedTestFragmentBinding = DataBindingUtil.inflate(
                inflater, R.layout.speed_test_fragment, container, false);
        speedTestFragmentBinding.setSpeedTestVM(mSpeedTestVM);
        speedTestFragmentBinding.setLifecycleOwner(getActivity());

        /*final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //startSpeedTest();
                checkConfigAndHostURL();
            }
        }, 1000);*/

        /*speedTestFragmentBinding.pointerSpeedometer.speedTo(1000, 600);
        speedTestFragmentBinding.pointerSpeedometer.speedTo(0, 600);*/
        //float value = (float)getGaugePositionByInternetSpeed(10);
        //speedTestFragmentBinding.pointerSpeedometer.speedTo(value, 500);
        //speedTestFragmentBinding.pointerSpeedometer.speedTo(20, 500);

        float percentage = getGaugePercentageByInternetSpeed(200);
        speedTestFragmentBinding.pointerSpeedometer.speedPercentTo((int) percentage, 500);
        //speedTestFragmentBinding.pointerSpeedometer.speedPercentTo(85, 500);
        speedTestFragmentBinding.tvGaugeSpeed.setText(String.valueOf(speedTestFragmentBinding.pointerSpeedometer.getSpeed()));

        speedTestFragmentBinding.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speedTestFragmentBinding.btnStart.setVisibility(View.INVISIBLE);
                restart();
                checkConfigAndHostURL();
            }
        });

        mSpeedTestVM.mHMServerConfigData.observe(getActivity(), new Observer<HashMap<String, String>>() {
            @Override
            public void onChanged(HashMap<String, String> Observer) {
                hmConfigData = Observer;
                getSpeedTestHostURL();
            }
        });

        mSpeedTestVM.mHMURLInfo.observe(getActivity(), new Observer<HashMap<String, URLInfo>>() {
            @Override
            public void onChanged(HashMap<String, URLInfo> stringURLInfoHashMap) {
                mHMURLInfo = stringURLInfoHashMap;
                Map.Entry<String, URLInfo> entry = mHMURLInfo.entrySet().iterator().next();
                sURL = entry.getKey();
                URLInfo urlInfo = entry.getValue();

                speedTestFragmentBinding.tvHostServerName.setText(urlInfo.getSponsor());
                speedTestFragmentBinding.tvInternetProviderName.setText(hmConfigData.get("isp"));
                //speedTestFragmentBinding.tvIPAddress.setText(hmConfigData.get("ip"));

                //String sFinalTestURL = sURL.replace("http://", "https://").replace(":8080", "");
                final PingTest pingTest = new PingTest(urlInfo.getHost().replace(":8080", ""), 3, mSpeedTestVM);
                pingTest.start();
            }
        });

        mSpeedTestVM.isPingTestComplete.observe(getActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isPingTestSuccess) {
                if (!isPingTestSuccess) {
                    Toast.makeText(getActivity(), "Unable to ping", Toast.LENGTH_SHORT).show();
                }
                String sFinalURL = sURL.replace("http://", "https://");
                downloadTest = new HttpDownloadTest(sFinalURL.replace(sURL.split("/")
                        [sURL.split("/").length - 1], ""), mSpeedTestVM);
                downloadTest.start();
            }
        });

        mSpeedTestVM.downloadSpeed.observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String downloadSpeed) {
                //position = getPositionByRate(Double.parseDouble(downloadSpeed));
                //String sFinalDownloadSpeed = downloadSpeed + " Mbps";
                float gaugePosition = (float) getGaugePositionByInternetSpeed(Double.parseDouble(downloadSpeed));
                speedTestFragmentBinding.pointerSpeedometer.setAccelerate(1);
                speedTestFragmentBinding.pointerSpeedometer.realSpeedTo(gaugePosition);
                //speedTestFragmentBinding.pointerSpeedometer.speedTo(gaugePosition);
                String sSpeed = downloadSpeed + " " + getResources().getString(R.string.mbps);

                speedTestFragmentBinding.tvGaugeSpeed.setText(sSpeed);
                speedTestFragmentBinding.tvDownloadSpeed.setText(downloadSpeed);
                speedTestFragmentBinding.pointerSpeedometer.setSpeedometerColor(
                        getActivity().getResources().getColor(R.color.colorDarkBlue));
                /*if (downloadTest.isFinished()) {
                    //Log.e("Anim", "500");
                    rotate = new RotateAnimation(position, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    ANIM_DURATION = 500;
                    final String sFinalURL = sURL.replace("http://", "https://");
                    position = 0;
                    lastPosition = 0;
                    *//*getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            uploadTest = new HttpUploadTest(sFinalURL, mSpeedTestVM);
                            uploadTest.uploadTest = uploadTest;
                            uploadTest.start();
                        }
                    });*//*

                    Runnable task = new Runnable() {
                        public void run() {
                            uploadTest = new HttpUploadTest(sFinalURL, mSpeedTestVM);
                            uploadTest.uploadTest = uploadTest;
                            uploadTest.start();
                        }
                    };
                    worker.schedule(task, 1200, TimeUnit.MILLISECONDS);
                    //worker.uploadTest();
                    *//*uploadTest = new HttpUploadTest(sFinalURL, mSpeedTestVM);
                    uploadTest.uploadTest = uploadTest;
                    uploadTest.start();*//*
                } else {
                    //Log.e("Anim", "300");
                    rotate = new RotateAnimation(lastPosition, position, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    ANIM_DURATION = 300;
                }
                rotate.setInterpolator(new LinearInterpolator());
                rotate.setDuration(ANIM_DURATION);
                speedTestFragmentBinding.ivBar.startAnimation(rotate);
                lastPosition = position;*/
            }
        });

        mSpeedTestVM.isDownloadSpeedComplete.observe(getActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDownloadTestComplete) {
                String sGaugeSpeed = getResources().getString(R.string.zero_digit) + " " +
                        getResources().getString(R.string.mbps);
                speedTestFragmentBinding.tvGaugeSpeed.setText(sGaugeSpeed);
                speedTestFragmentBinding.pointerSpeedometer.speedTo(0, 500);
                if (isDownloadTestComplete) {
                    final String sFinalURL = sURL.replace("http://", "https://");
                    task = new Runnable() {
                        public void run() {
                            speedTestFragmentBinding.pointerSpeedometer.setSpeedometerColor(
                                    getActivity().getResources().getColor(R.color.colorAmber));
                            speedTestFragmentBinding.tvGaugeSpeed.setTextColor(
                                    getActivity().getResources().getColor(R.color.colorAmber));

                            uploadTest = new HttpUploadTest(sFinalURL, mSpeedTestVM);
                            uploadTest.uploadTest = uploadTest;
                            uploadTest.start();
                        }
                    };
                    worker.schedule(task, 1500, TimeUnit.MILLISECONDS);
                    //reset();
                }
            }
        });

        mSpeedTestVM.uploadSpeed.observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String uploadSpeed) {
                //position = getPositionByRate(Double.parseDouble(uploadSpeed));
                //String sFinalUploadSpeed = uploadSpeed + " Mbps";
                /*getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d("UI thread", "I am the UI thread");
                    }
                });*/
                float gaugePosition = (float) getGaugePositionByInternetSpeed(Double.parseDouble(uploadSpeed));
                speedTestFragmentBinding.pointerSpeedometer.setAccelerate(1);
                //speedTestFragmentBinding.pointerSpeedometer.speedTo(gaugePosition);
                speedTestFragmentBinding.pointerSpeedometer.realSpeedTo(gaugePosition);
                String sSpeed = uploadSpeed + " " + getResources().getString(R.string.mbps);
                speedTestFragmentBinding.tvGaugeSpeed.setText(sSpeed);
                speedTestFragmentBinding.tvUploadSpeed.setText(uploadSpeed);
                /*if (uploadTest.isFinished()) {
                    rotate = new RotateAnimation(position, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    ANIM_DURATION = 500;
                    Log.e("Last anim", "500");
                } else {
                    rotate = new RotateAnimation(lastPosition, position, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    ANIM_DURATION = 300;
                    Log.e("normal anim", "300" + uploadTest.isFinished());
                }
                rotate.setInterpolator(new LinearInterpolator());
                rotate.setDuration(ANIM_DURATION);
                speedTestFragmentBinding.ivBar.startAnimation(rotate);
                lastPosition = position;*/
            }
        });

        mSpeedTestVM.isUploadSpeedComplete.observe(getActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isUploadSpeedComplete) {
                if (isUploadSpeedComplete) {
                    reset();
                }
            }
        });

        return speedTestFragmentBinding.getRoot();
    }


    private void reset() {
        speedTestFragmentBinding.pointerSpeedometer.speedTo(0, 500);

        Handler handler = new Handler();
        task = new Runnable() {
            public void run() {
                speedTestFragmentBinding.btnStart.setVisibility(View.VISIBLE);
                speedTestFragmentBinding.pointerSpeedometer.setSpeedometerColor(
                        getActivity().getResources().getColor(R.color.colorFadedWhite)
                );
                String sGaugeSpeed = getResources().getString(R.string.zero_digit) + " " +
                        getResources().getString(R.string.mbps);
                speedTestFragmentBinding.tvGaugeSpeed.setText(sGaugeSpeed);
                speedTestFragmentBinding.tvGaugeSpeed.setTextColor(
                        getActivity().getResources().getColor(R.color.colorDarkBlue));

            }
        };
        handler.postDelayed(task, 700);
    }

    private void restart() {
        speedTestFragmentBinding.tvInternetProviderName.setText(getResources().getString(R.string.finding));
        speedTestFragmentBinding.tvHostServerName.setText(getResources().getString(R.string.finding));
        speedTestFragmentBinding.tvDownloadSpeed.setText(getResources().getString(R.string.zero_digit));
        speedTestFragmentBinding.tvUploadSpeed.setText(getResources().getString(R.string.zero_digit));
    }

    private static final ScheduledExecutorService worker =
            Executors.newSingleThreadScheduledExecutor();


    private void checkConfigAndHostURL() {
        serverRequestVolleyManager.volleyExecute(SPEED_TEST_CONFIG, mSpeedTestVM);
    }

    private void getSpeedTestHostURL() {
        serverRequestVolleyManager.volleyExecute(SPEED_TEST_SERVERS, mSpeedTestVM);
    }

    private void startSpeedTest() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (getSpeedTestHostsHandler == null) {
                    getSpeedTestHostsHandler = new GetSpeedTestHostsHandler();
                    getSpeedTestHostsHandler.start();
                }
                //Find closest server
                HashMap<Integer, String> mapKey = getSpeedTestHostsHandler.getMapKey();
                HashMap<Integer, List<String>> mapValue = getSpeedTestHostsHandler.getMapValue();
                double selfLat = getSpeedTestHostsHandler.getSelfLat();
                double selfLon = getSpeedTestHostsHandler.getSelfLon();
                final String ip = getSpeedTestHostsHandler.getIp();
                double tmp = 19349458;
                //double dist = 0.0;
                int findServerIndex = 0;
                if (mapKey != null && getSpeedTestHostsHandler.isFinished())
                    for (int index : mapKey.keySet()) {
                    /*if (tempBlackList.contains(mapValue.get(index).get(5))) {
                        continue;
                    }*/

                        Location source = new Location("Source");
                        source.setLatitude(selfLat);
                        source.setLongitude(selfLon);

                        List<String> ls = mapValue.get(index);
                        Location dest = new Location("Dest");
                        dest.setLatitude(Double.parseDouble(ls.get(0)));
                        dest.setLongitude(Double.parseDouble(ls.get(1)));

                        double distance = source.distanceTo(dest);
                        if (distance < tmp) {
                            tmp = distance;
                            findServerIndex = index;
                        }
                        //Log.e(tmp + "", ls.get(0) + " " + ls.get(1));
                        /*if (tmp > distance) {
                            tmp = distance;
                            //dist = distance;
                            findServerIndex = index;
                        }*/
                    }
                else {
                    startSpeedTest();
                }
                String testAddr = mapKey.get(findServerIndex).replace("http://", "https://");
                final List<String> info = mapValue.get(findServerIndex);
                //final double distance = dist;

                if (info == null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //speedTestFragmentBinding.tvHostName.setTextSize(12);
                            //speedTestFragmentBinding.tvHostName.setText("There was a problem in getting Host Location. Try again later.");
                        }
                    });
                    return;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //speedTestFragmentBinding.tvHostName.setText(String.format("Host Location: %s [Distance: %s km]", info.get(2), new DecimalFormat("#.##").format(distance / 1000)));
                        //speedTestFragmentBinding.tvIPAddress.setText(ip);
                        //speedTestFragmentBinding.tvHostName.setText(info.get(5));
                    }
                });
                final List<Double> pingRateList = new ArrayList<>();
                final List<Double> downloadRateList = new ArrayList<>();
                final List<Double> uploadRateList = new ArrayList<>();
                boolean pingTestStarted = false;
                boolean pingTestFinished = false;
                boolean downloadTestStarted = false;
                //Boolean downloadTestFinished = false;
                //Boolean uploadTestStarted = false;
                //Boolean uploadTestFinished = false;

                //Init Test
                final PingTest pingTest = new PingTest(info.get(6).replace(":8080", ""), 3);
                final HttpDownloadTest downloadTest = new HttpDownloadTest(testAddr.replace(testAddr.split("/")[testAddr.split("/").length - 1], ""));
                final HttpUploadTest uploadTest = new HttpUploadTest(testAddr);

                //Tests
                while (true) {
                    if (!pingTestStarted) {
                        pingTest.start();
                        pingTestStarted = true;
                    }
                    if (pingTestFinished && !downloadTestStarted) {
                        downloadTest.start();
                        downloadTestStarted = true;
                    }

                    if (downloadTestFinished && !uploadTestStarted) {
                        try {
                            Thread.sleep(1200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        uploadTest.start();
                        uploadTestStarted = true;
                    }

                    //Ping Test
                    if (pingTestFinished) {
                        //Failure
                        if (pingTest.getAvgRtt() == 0) {
                            System.out.println("Ping error...");
                        } else {
                            //Success
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    speedTestFragmentBinding.tvPing.setText(dec.format(pingTest.getAvgRtt()) + " ms");
                                }
                            });
                        }
                    } else {
                        pingRateList.add(pingTest.getInstantRtt());

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                speedTestFragmentBinding.tvPing.setText(dec.format(pingTest.getInstantRtt()) + " ms");
                            }
                        });
                    }

                    //Download Test
                    if (pingTestFinished) {
                        if (downloadTestFinished) {
                            //Failure
                            if (downloadTest.getFinalDownloadRate() == 0) {
                                System.out.println("Download error...");
                            } else {
                                //Success
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        speedTestFragmentBinding.tvDownloadSpeed.setText(dec.format(downloadTest.getFinalDownloadRate()) + " Mbps");
                                    }
                                });
                            }
                        } else {
                            //Calc position
                            double downloadRate = downloadTest.getInstantDownloadRate();
                            downloadRateList.add(downloadRate);
                            Log.e("Download Rate", String.valueOf(downloadRate));
                            position = getPositionByRate(downloadRate);

                            getActivity().runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                            /*rotate = new RotateAnimation(lastPosition, position, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                            rotate.setInterpolator(new LinearInterpolator());
                                            rotate.setDuration(350);
                                            barImageView.startAnimation(rotate);
                                            downloadTextView.setText(dec.format(downloadTest.getInstantDownloadRate()) + " Mbps");*/

                                    if (downloadTest.isFinished()) {
                                        rotate = new RotateAnimation(position, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                        ANIM_DURATION = 500;
                                    } else {
                                        rotate = new RotateAnimation(lastPosition, position, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                                /*if(downloadRateList.size()<=1){
                                                    ANIM_DURATION = 2000;
                                                } else*/
                                        ANIM_DURATION = 250;
                                    }
                                    rotate.setInterpolator(new LinearInterpolator());
                                    rotate.setDuration(ANIM_DURATION);
                                    speedTestFragmentBinding.ivBar.startAnimation(rotate);
                                    speedTestFragmentBinding.tvDownloadSpeed.setText(dec.format(downloadTest.getInstantDownloadRate()) + " Mbps");
                                }

                            });
                            lastPosition = position;
                        }
                    }

                    //Upload Test
                    if (downloadTestFinished) {
                        if (uploadTestFinished) {
                            //Failure
                            if (uploadTest.getFinalUploadRate() == 0) {
                                System.out.println("Upload error...");
                            } else {
                                //Success
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        speedTestFragmentBinding.tvUploadSpeed.setText(dec.format(uploadTest.getFinalUploadRate()) + " Mbps");
                                    }
                                });
                            }
                        } else {
                            //Calc position
                            double uploadRate = uploadTest.getInstantUploadRate();
                            uploadRateList.add(uploadRate);
                            position = getPositionByRate(uploadRate);

                            getActivity().runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                            /*rotate = new RotateAnimation(lastPosition, position, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                            rotate.setInterpolator(new AccelerateDecelerateInterpolator());
                                            rotate.setDuration(500);
                                            barImageView.startAnimation(rotate);
                                            uploadTextView.setText(dec.format(uploadTest.getInstantUploadRate()) + " Mbps");*/
                                    //Log.e("runOnUiThread", uploadRateList.size() + " size");
                                    if (uploadTest.isFinished()) {
                                        rotate = new RotateAnimation(position, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                        ANIM_DURATION = 500;
                                    } else {
                                        rotate = new RotateAnimation(lastPosition, position, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                        ANIM_DURATION = 250;
                                    }
                                    rotate.setInterpolator(new LinearInterpolator());
                                    rotate.setDuration(ANIM_DURATION);
                                    speedTestFragmentBinding.ivBar.startAnimation(rotate);
                                    speedTestFragmentBinding.tvUploadSpeed.setText(dec.format(uploadTest.getInstantUploadRate()) + " Mbps");
                                }

                            });
                            lastPosition = position;
                            //Log.e("lastPosition", uploadRateList.size() + "size");
                        }
                    }

                    //Test bitti
                    if (pingTestFinished && downloadTestFinished && uploadTest.isFinished()) {
                        break;
                    }

                    if (pingTest.isFinished()) {
                        pingTestFinished = true;
                    }
                    if (downloadTest.isFinished()) {
                        downloadTestFinished = true;
                    }
                    if (uploadTest.isFinished()) {
                        uploadTestFinished = true;
                    }

                    if (pingTestStarted && !pingTestFinished) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //Thread bitiminde button yeniden aktif ediliyor
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startButton.setEnabled(true);
                        startButton.setTextSize(16);
                        startButton.setText("Restart Test");
                    }
                });*/
            }
        }).start();
    }

    public int getPositionByRate(double rate) {
        if (rate <= 1) {
            return (int) (rate * 30);
        } else if (rate <= 10) {
            return (int) (rate * 6) + 30;
        } else if (rate <= 30) {
            return (int) ((rate - 10) * 3) + 90;
        } else if (rate <= 50) {
            return (int) ((rate - 30) * 1.5) + 150;
        } else if (rate <= 100) {
            return (int) ((rate - 50) * 1.2) + 180;
        }
        return 0;
    }

    public double getGaugePositionByInternetSpeed(double rate) {
        //5 15 30 25 25 100 300 500 - this is the difference between each ticks
        //int speed = (int) rate;
        //double ss =  (rate * 9.5) + (rate / 2);

        if (rate <= 5) {
            return ((rate * 22 - rate) + 45);
        } else if (rate <= 20) {
            return ((rate * 10) - rate);
        } else if (rate <= 50) {
            return ((rate * 1.5) + 275);
        } else if (rate <= 75) {
            return ((rate * 3.5) + 237.5);
        } else if (rate <= 100) {
            return ((rate * 4) + 235);
        } else if (rate <= 200) {
            return ((rate * 2.5) + 250);
        } else if (rate <= 500) {
            return ((rate * 2) - 140);
        } else if (rate <= 1000) {
            return ((rate * 1.2) - 200);
        }
        return 0;
    }

    private float getGaugePercentageByInternetSpeed(float nSpeed) {
        //5 15 30 25 25 100 300 500 - this is the difference between each ticks mbps

        //14% = 5mbps, 25% = 20mbps, 35% = 50mbps, 50% = 75mbps, 65% = 100mbps, 75% = 200mbps, 85% = 500, 100% = 1000mbps
        //so here we should calculate 25-14 = 11 and then 35-25 = 10, 50-35 = 15, 65-50 = 15, 75-65 = 10
        //ex: the number between 5 to 20mbps is below, we multiple the input by 0.733 and update the percentage
        //6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 //11/15 = 0.733

        int speed = (int) nSpeed;
        if (speed <= 5) {
            return ((speed * 20 / 14f) * 2);
        } else if (speed <= 20) {
            return ((0.733f * speed) + 11);
        } else if (speed <= 50) {
            return ((0.344f * speed) + 29 - 10);
        } else if (speed <= 75) {
            float d = (0.625f * speed); //50% of gauge
            return ((d / 15) + d); //d / 15 is 3.125
        } else if (speed <= 100) {
            float d = (0.625f * speed); //65% of gauge
            return ((d / 24) + d); //24 here is total numbers between 75-100. i,e from previous % to this %
        } else if (speed <= 200) {
            float d = (0.101010101010101f * speed); //75% of gauge
            return (d + 65-10); //here 65 is nothing but the previous % of gauge, 10 is 75-65=10
        } else if (speed <= 500) {
            return ((speed * 2) - 140);
        } else if (speed <= 1000) {
            return ((speed * 1.2f) - 200);
        }
        return 0f;
    }
}