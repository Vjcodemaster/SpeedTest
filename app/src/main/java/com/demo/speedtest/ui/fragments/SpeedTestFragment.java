package com.demo.speedtest.ui.fragments;

import android.animation.AnimatorSet;
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

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //startSpeedTest();
                checkConfigAndHostURL();
            }
        }, 1000);

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
                Map.Entry<String,URLInfo> entry = mHMURLInfo.entrySet().iterator().next();
                sURL = entry.getKey();
                URLInfo urlInfo = entry.getValue();

                speedTestFragmentBinding.tvHostName.setText(urlInfo.getSponsor());
                speedTestFragmentBinding.tvIPAddress.setText(hmConfigData.get("ip"));

                //String sFinalTestURL = sURL.replace("http://", "https://").replace(":8080", "");
                final PingTest pingTest = new PingTest(urlInfo.getHost().replace(":8080", ""), 3, mSpeedTestVM);
                pingTest.start();
            }
        });

        mSpeedTestVM.isPingTestComplete.observe(getActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isPingTestSuccess) {
                if(!isPingTestSuccess){
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
                position = getPositionByRate(Double.parseDouble(downloadSpeed));
                String sFinalDownloadSpeed = downloadSpeed + " Mbps";
                speedTestFragmentBinding.tvDownloadSpeed.setText(sFinalDownloadSpeed);
                if (downloadTest.isFinished()) {
                    //Log.e("Anim", "500");
                    rotate = new RotateAnimation(position, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    ANIM_DURATION = 500;
                    final String sFinalURL = sURL.replace("http://", "https://");
                    position = 0;
                    lastPosition = 0;
                    /*getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            uploadTest = new HttpUploadTest(sFinalURL, mSpeedTestVM);
                            uploadTest.uploadTest = uploadTest;
                            uploadTest.start();
                        }
                    });*/

                    Runnable task = new Runnable() {
                        public void run() {
                            uploadTest = new HttpUploadTest(sFinalURL, mSpeedTestVM);
                            uploadTest.uploadTest = uploadTest;
                            uploadTest.start();
                        }
                    };
                    worker.schedule(task, 1200, TimeUnit.MILLISECONDS);
                    //worker.uploadTest();
                    /*uploadTest = new HttpUploadTest(sFinalURL, mSpeedTestVM);
                    uploadTest.uploadTest = uploadTest;
                    uploadTest.start();*/
                } else {
                    //Log.e("Anim", "300");
                    rotate = new RotateAnimation(lastPosition, position, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    ANIM_DURATION = 300;
                }
                rotate.setInterpolator(new LinearInterpolator());
                rotate.setDuration(ANIM_DURATION);
                speedTestFragmentBinding.ivBar.startAnimation(rotate);
                lastPosition = position;
            }
        });

        mSpeedTestVM.uploadSpeed.observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String uploadSpeed) {
                position = getPositionByRate(Double.parseDouble(uploadSpeed));
                String sFinalUploadSpeed = uploadSpeed + " Mbps";
                speedTestFragmentBinding.tvUploadSpeed.setText(sFinalUploadSpeed);
                if (uploadTest.isFinished()) {
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
                lastPosition = position;
            }
        });

        return speedTestFragmentBinding.getRoot();
    }

    private static final ScheduledExecutorService worker =
            Executors.newSingleThreadScheduledExecutor();


    private void checkConfigAndHostURL(){
        /*ServerRequestManager serverRequestManager = new ServerRequestManager() {
            @Override
            public void addParameters() {

            }

            @Override
            public void onCallSuccess(String result) {
                String ip = result.split("client ip=\"")[1].split(" ")[0].replace("\"", "");
                double selfLat = Double.parseDouble(result.split("lat=\"")[1].split(" ")[0].replace("\"", ""));
                double selfLon = Double.parseDouble(result.split("lon=\"")[1].split(" ")[0].replace("\"", ""));

                hmConfigData.put("ip", ip);
                hmConfigData.put("lat", String.valueOf(selfLat));
                hmConfigData.put("lon", String.valueOf(selfLon));
            }

            @Override
            public void onCallFailed() {

            }
        };
        serverRequestManager.volleyExecute(StaticReferenceClass.SPEED_TEST_CONFIG);*/
        serverRequestVolleyManager.volleyExecute(SPEED_TEST_CONFIG, mSpeedTestVM);
    }

    private void getSpeedTestHostURL(){
        //ServerRequestVolleyManager serverRequestVolleyManager = new ServerRequestVolleyManager();
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
                        if(distance < tmp){
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
                            speedTestFragmentBinding.tvHostName.setText("There was a problem in getting Host Location. Try again later.");
                        }
                    });
                    return;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //speedTestFragmentBinding.tvHostName.setText(String.format("Host Location: %s [Distance: %s km]", info.get(2), new DecimalFormat("#.##").format(distance / 1000)));
                        speedTestFragmentBinding.tvIPAddress.setText(ip);
                        speedTestFragmentBinding.tvHostName.setText(info.get(5));
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
}