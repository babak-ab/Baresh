package com.example.babak.baresh;


import android.content.Context;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.net.FileNameMap;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class Downloader{
    private static final String TAG = "MyActivity";
    private HttpAsyncTask headerTask;
    private DownloadAsyncTask[] mDownloadTask;
    private URL mUrl;
    private Long mId;
    private String fileType;
    private Boolean isPartialContent;
    private long mFileSize;
    private Context mContext;
    private String mFileName;
    private long mDownloadedSize;
    private DownloadManager mDownloadManager;
    //DownloadInfoDialog mDownloadDialog;
    public Downloader(Long id,URL url,DownloadManager downloadManager,Context context) {
        headerTask = new HttpAsyncTask(this);
        mDownloadTask = new DownloadAsyncTask[8];
        mDownloadedSize = 0;
        mDownloadManager = downloadManager;
        mUrl = url;
        mId = id;
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

        header();
    }
    public void header(){
        headerTask.execute(mUrl);
    }

    public URL getUrl(){
        return  mUrl;
    }

    public boolean isPartialContent() {
        return isPartialContent;
    }

    public void setPartialContent(boolean partialContent) {
        this.isPartialContent = partialContent;
    }

    public long getFileSize() {
        return mFileSize;
    }

    public void setFileSize(long fileSize) {
        mFileSize = fileSize;
        mDownloadManager.setFileSize(mFileSize);
    }
    public void setFileType(String fileType){
        this.fileType = fileType;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        this.mFileName = fileName;
    }

    public void setDownloadedSize(long downloadedSize){
        mDownloadManager.setDownloadedSize(downloadedSize);
    }

    public long getDownloadedSize() {
        return mDownloadedSize;
    }

    public Long getId() {
        return mId;
    }
}