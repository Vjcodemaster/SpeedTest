package com.demo.speedtest.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
    //public SingleLiveEvent<HashMap<Integer, String>> mHMServerMapKey = new SingleLiveEvent<>();
    //public SingleLiveEvent<HashMap<Integer, List<String>>> mHMServerMapValue = new SingleLiveEvent<>();
    public SingleLiveEvent<HashMap<String, URLInfo>> mHMURLInfo = new SingleLiveEvent<>();

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

    public void postDownloadTestResult(boolean isDownloadTestComplete){
        this.isDownloadSpeedComplete.postValue(isDownloadTestComplete);
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

    public void postDownloadSpeed(String downloadSpeed){
        this.downloadSpeed.postValue(downloadSpeed);
    }

    public void postUploadSpeed(String uploadSpeed){
        this.uploadSpeed.postValue(uploadSpeed);
    }
}
