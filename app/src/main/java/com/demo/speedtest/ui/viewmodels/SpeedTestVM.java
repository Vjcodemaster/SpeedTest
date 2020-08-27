package com.demo.speedtest.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.demo.speedtest.app_utility.Constants;
import com.demo.speedtest.app_utility.StaticReferenceClass;
import com.demo.speedtest.speedtest_utils.URLInfo;

import java.util.HashMap;

public class SpeedTestVM extends ViewModel {
    public SingleLiveEvent<HashMap<String, String>> mHMServerConfigData = new SingleLiveEvent<>();
    public SingleLiveEvent<Boolean> isPingTestComplete = new SingleLiveEvent<>();
    public SingleLiveEvent<Boolean> isDownloadSpeedComplete = new SingleLiveEvent<>();
    public SingleLiveEvent<Boolean> isUploadSpeedComplete = new SingleLiveEvent<>();
    public MutableLiveData<String> ping = new MutableLiveData<>();
    public SingleLiveEvent<String> downloadSpeed = new SingleLiveEvent<>();
    public SingleLiveEvent<String> uploadSpeed = new SingleLiveEvent<>();

    public SingleLiveEvent<String> averageDownloadSpeed = new SingleLiveEvent<>();
    //public SingleLiveEvent<HashMap<Integer, String>> mHMServerMapKey = new SingleLiveEvent<>();
    //public SingleLiveEvent<HashMap<Integer, List<String>>> mHMServerMapValue = new SingleLiveEvent<>();
    public SingleLiveEvent<HashMap<String, URLInfo>> mHMURLInfo = new SingleLiveEvent<>();

    private int t1Value;
    private int t2Value;
    private int t3Value;
    private int NETWORK_TYPE;

    public void postServerConfigData(HashMap<String, String> hmData) {
        mHMServerConfigData.postValue(hmData);
    }

    /*public void postServerMapKey(HashMap<Integer, String> mHMServerMapKey) {
        this.mHMServerMapKey.postValue(mHMServerMapKey);
    }

    public void postServerMapValue(HashMap<Integer, List<String>> mHMServerMapValue) {
        this.mHMServerMapValue.postValue(mHMServerMapValue);
    }*/

    public void postServerURLInfo(HashMap<String, URLInfo> mHMURLInfo) {
        this.mHMURLInfo.postValue(mHMURLInfo);
    }

    public void postPingTest(boolean isPingTestComplete){
        this.isPingTestComplete.postValue(isPingTestComplete);
    }

    public void postDownloadTestResult(boolean isDownloadTestComplete, int fromWhere){
        if (NETWORK_TYPE== StaticReferenceClass.NETWORK_TYPE_MOBILE){
            if(fromWhere==2){
                this.isDownloadSpeedComplete.postValue(isDownloadTestComplete);
            }
        } else {
            if(fromWhere==3){
                this.isDownloadSpeedComplete.postValue(isDownloadTestComplete);
            }
        }
    }

    public void postUploadTestResult(boolean isUploadTestComplete){
        this.isUploadSpeedComplete.postValue(isUploadTestComplete);
    }

    public void postPingData(String ping){
        this.ping.postValue(ping);
    }

    public LiveData<String> getPing(){
        return ping;
    }

    public void postDownloadSpeed(String downloadSpeed, int fromWhere){
        this.downloadSpeed.postValue(downloadSpeed);
        Constants constants = Constants.values()[NETWORK_TYPE];
        switch (constants){
            case NETWORK_TYPE_MOBILE:
                if(fromWhere==1){
                    t1Value = Integer.parseInt(downloadSpeed);
                } else {
                    t2Value = Integer.parseInt(downloadSpeed);
                    int value = (t1Value + t2Value);
                    this.averageDownloadSpeed.postValue(String.valueOf(value));
                }
                break;
            case NETWORK_TYPE_WIFI:
                if(fromWhere==1){
                    t1Value = Integer.parseInt(downloadSpeed);
                } else if(fromWhere==2){
                    t2Value = Integer.parseInt(downloadSpeed);
                } else {
                    t3Value = Integer.parseInt(downloadSpeed);
                    int value = (t1Value + t2Value + t3Value);
                    this.averageDownloadSpeed.postValue(String.valueOf(value));
                }
                break;
        }

    }

    public void postUploadSpeed(String uploadSpeed){
        this.uploadSpeed.postValue(uploadSpeed);
    }
    public void setNetworkType(int NETWORK_TYPE){
        this.NETWORK_TYPE = NETWORK_TYPE;
    }
}
