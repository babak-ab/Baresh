package com.example.babak.baresh;


import android.util.Log;

import java.net.URL;

public class Downloader implements DownloaderInterface {

    private static final String TAG = "MyActivity";
    private HttpAsyncTask headerTask;
    private HttpAsyncTask[] mDownloadTask;

    private URL url;
    private byte numberOfThread;
    private boolean resumable;
    private Long fileSize;
    public Downloader(URL url) {
        headerTask = new HttpAsyncTask(this);
        mDownloadTask = new HttpAsyncTask[8];
        this.url = url;
    }
    public void header(){
        headerTask.execute(url);
    }
    @Override
    public void onDownloadFinish() {
        Log.i(TAG,"headerFinished");
    }

    public boolean isResumable() {
        return resumable;
    }

    public void setResumable(boolean resumable) {
        this.resumable = resumable;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
}