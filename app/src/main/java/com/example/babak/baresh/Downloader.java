package com.example.babak.baresh;


import android.content.Context;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Downloader{

    private static final String TAG = "MyActivity";
    private HttpAsyncTask headerTask;
    private HttpAsyncTask[] mDownloadTask;
    private List<DownloaderListener> listeners = new ArrayList<DownloaderListener>();
    private URL url;
    private String fileType;
    private Boolean resume;
    private int fileSize;
    private Context mContext;
    //DownloadInfoDialog mDownloadDialog;
    public Downloader(Context context,URL url) {
        headerTask = new HttpAsyncTask(this);
        mDownloadTask = new HttpAsyncTask[8];
        this.url = url;
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mimeType = fileNameMap.getContentTypeFor(url.toString());
        if(mimeType != null){
            fileType = mimeType;
        }else{
            fileType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url.toString()));
        }
        if(fileType == null){
            fileType = "Unknown";
        }
        mContext = context;

        for (DownloaderListener hl : listeners)
            hl.onFileTypeChanged(fileType);
        header();
    }
    public void header(){
        headerTask.execute(url);
    }


    public void addListener(DownloaderListener toAdd) {
        listeners.add(toAdd);
    }
    public boolean isResumable() {
        return resume;
    }

    public void setResumable(boolean resumable) {
        this.resume = resumable;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
        for (DownloaderListener hl : listeners)
            hl.onFileSizeChanged(this.fileSize);
    }
    public void setFileType(String fileType){
        this.fileType = fileType;
    }
}