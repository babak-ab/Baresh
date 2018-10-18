package com.example.babak.baresh;


import android.util.Log;

import java.net.FileNameMap;
import java.net.URL;
import java.net.URLConnection;

public class Downloader implements DownloaderInterface {

    private static final String TAG = "MyActivity";
    private HttpAsyncTask headerTask;
    private HttpAsyncTask[] mDownloadTask;

    private URL url;
    private byte numberOfThread;
    private boolean resumable;
    private int fileSize;
    private String fileType;
    DownloadInfoDialog mDownloadDialog;
    public Downloader(URL url,DownloadInfoDialog dilaog) {
        headerTask = new HttpAsyncTask(this);
        mDownloadTask = new HttpAsyncTask[8];
        this.url = url;
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mimeType = fileNameMap.getContentTypeFor(url.toString());
        fileType = mimeType;
        Log.e("DOWNLOAD3", mimeType);
        this.mDownloadDialog = dilaog;
        this.mDownloadDialog.setFileType(mimeType);
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
    public void setFileType(String fileType){
        this.fileType = fileType;
    }
}