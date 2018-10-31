package com.example.babak.baresh;

public interface DownloadInfoDialogListener {
    void onDownloadAccepted(Long downloadId);
    void onDownloadReject(Long downloadId);
}
