package com.example.a13522.xiazai;

/**
 * Created by 13522 on 2017/7/19.
 */

public interface DownloadListener {
    void onProgress(int progress);
    void onSuccess();
    void onFailed();
    void onPause();
    void onCanceled();
}
