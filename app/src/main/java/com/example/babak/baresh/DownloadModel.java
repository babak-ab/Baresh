package com.example.babak.baresh;

import java.net.URL;

public class DownloadModel {
    private String mUrl;
    private Long mDownloadId;
    private String mName;
    private String mFile;
    private Long mFileSize;
    private Long mSize;
    private Long mDownloaded;
    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public Long getDownloadId() {
        return mDownloadId;
    }

    public void setDownloadId(Long mDownloadId) {
        this.mDownloadId = mDownloadId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getFile() {
        return mFile;
    }
    public void setFile(String mFile) {
        this.mFile = mFile;
    }

    public Long getFileSize() {
        return mFileSize;
    }

    public void setFileSize(Long mFileSize) {
        this.mFileSize = mFileSize;
    }

    public Long getSize() {
        return mSize;
    }

    public void setSize(Long mSize) {
        this.mSize = mSize;
    }

    public Long getDownloaded() {
        return mDownloaded;
    }

    public void setDownloaded(Long mDownloaded) {
        this.mDownloaded = mDownloaded;
    }
}
