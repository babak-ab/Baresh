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
    private int fileSize;
    DownloadInfoDialog mDownloadDialog;
    public Downloader(URL url,DownloadInfoDialog dilaog) {
        headerTask = new HttpAsyncTask(this);
        mDownloadTask = new HttpAsyncTask[8];
        this.url = url;
        this.mDownloadDialog = dilaog;
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

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
        mDownloadDialog.setFileSize(this.fileSize);
    }
}