package com.example.babak.baresh;

public interface DownloaderListener {
    void onDownloadFinish();
    void onFileSizeChanged(Integer size);
    void onFileTypeChanged(String fileType);
}
