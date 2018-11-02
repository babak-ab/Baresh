package com.example.babak.baresh;

import java.net.URL;

public class DownloadModel {
    private String mUrl;
    private Long mDownloadId;
    private String mName;
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
}
