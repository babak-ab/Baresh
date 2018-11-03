package com.example.babak.baresh;


import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Downloader{
    private static final String TAG = "MyActivity";
    private HttpAsyncTask headerTask;
    private HashMap<Integer,DownloadAsyncTask> mDownloadTask;
    private Timer mTimerUpdate;
    private Timer mTimerStartDownload;

    private String mFileType;
    private Boolean mIsPartialContent;
    private long mFileSize;

   // private String mFileName;
    private long mDownloadedSize;
    private String mFilePath;
    private File mFile;
    private long mSpeed;
    private long mDurationTime;
    private boolean mDownloadHeadFinished;
    private boolean mDownloadAccepted;
    private boolean isPause;

    private DownloadModel mDownloadModel;
    private int mError;
    private Context mContext;
    private DownloadManager mDownloadManager;
    public Downloader(DownloadModel model,DownloadManager downloadManager,Context context) {
        isPause = false;
        mDownloadHeadFinished = false;
        mDownloadAccepted = false;
        mDownloadModel = model;
        headerTask = new HttpAsyncTask(this);
        File dir = new File(Environment.getExternalStorageDirectory() + "/myFolder");
        boolean succeed;
        if (!dir.exists()) {
            succeed = dir.mkdir();
            if (!succeed) {
                mError = 1;
            } else {
                mFilePath = dir.toString();
            }
        }else{
            mFilePath = dir.toString();
        }
        mDownloadTask = new HashMap<>();
        for(int i = 0;i < 4; i++){
            DownloadAsyncTask task = new DownloadAsyncTask(i,this);
            mDownloadTask.put(i,task);
        }
        mDownloadedSize = 0;
        mDownloadManager = downloadManager;
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mimeType = fileNameMap.getContentTypeFor(mDownloadModel.toString());
        if(mimeType != null){
            mFileType = mimeType;
        }else{
            mFileType = MimeTypeMap.getSingleton().
                    getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(mDownloadModel.toString()));
        }
        if(mFileType == null){
            mFileType = "Unknown";
        }
        mContext = context;
        mTimerUpdate = new Timer();
        mTimerStartDownload = new Timer();
        TimerTask downloadTask = new StartDownloadTask();
        mTimerStartDownload.scheduleAtFixedRate(downloadTask, 0, 100);
    }
    public void setDownloadAccepted(boolean accepted) {
        mDownloadAccepted = accepted;
        if(mDownloadAccepted == false)
            mTimerStartDownload.cancel();
    }
    public String getUrl(){
        return  mDownloadModel.getUrl();
    }
    public boolean isPartialContent() {
        return mIsPartialContent;
    }
    public long getFileSize() {
        return mFileSize;
    }
    public String getFileName() {
        return mDownloadModel.getName();
    }
    public long getDownloadedSize() {
        return mDownloadedSize;
    }
    public long getDownloadId() {
        return mDownloadModel.getDownloadId();
    }
    public long getSpeed() {
        return mSpeed;
    }
    public long getDurationTime() {
        return mDurationTime / 1000;
    }
    public long getRemindTime() {
        if(mSpeed > 0)
            return (mFileSize - mDownloadedSize) / mSpeed;
        else
            return 0;
    }
    public File getFile() {
        return mFile;
    }
    public void onDownloadedSizeChanged(long downloadedSize){
        mDownloadedSize += downloadedSize;
        ///mDownloadManager.onDownloadSizeChanged();

    }
    public void onHeadFinished(String fileName, long fileSize, boolean partialContent){
        mDownloadHeadFinished = true;
        mDownloadModel.setName(fileName);
        mFileSize = fileSize;
        mIsPartialContent = partialContent;
        mDownloadManager.onHeadFinished(mDownloadModel.getDownloadId());
    }
    public void onDownloadFinished(int taskId){
        mDownloadTask.remove(taskId);
        if(mDownloadTask.size() == 0) {
            mDownloadManager.onDownloadFinished(mDownloadModel.getDownloadId());
            mTimerUpdate.cancel();
        }
    }
    public void startHead() {
        headerTask.execute(mDownloadModel.getUrl());
    }
    private void stopDownload(){
        mDownloadTask.get(0).cancel(true);
        mDownloadTask.get(1).cancel(true);
        mDownloadTask.get(2).cancel(true);
        mDownloadTask.get(3).cancel(true);
    }
    private void startDownload() {
        mDownloadTask.get(0).execute(mDownloadModel.getUrl());
        mDownloadTask.get(1).execute(mDownloadModel.getUrl());
        mDownloadTask.get(2).execute(mDownloadModel.getUrl());
        mDownloadTask.get(3).execute(mDownloadModel.getUrl());

        mDurationTime = 0;
        TimerTask speedTask = new DownloadSpeedTask();
        mTimerUpdate.scheduleAtFixedRate(speedTask, 0, 100);
    }

    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        isPause = pause;
        Log.e("PlaceholderFragment", String.valueOf(pause));
        if(isPause == true){
            stopDownload();
        }else{
            startDownload();
        }
    }

    class StartDownloadTask extends TimerTask {
        public void run() {
            if(mDownloadAccepted && mDownloadHeadFinished){
                mTimerStartDownload.cancel();
                long tmp;
                long start;
                long end;
                if(mFileSize % 4 == 0){
                    tmp = (mFileSize / 4) - 1;
                    start = 0;
                    end = tmp;
                    for (int i = 0;i < 4;i++){
                        mDownloadTask.get(i).setRange(start,end);
                        start = end + 1;
                        end = start + tmp;
                    }
                }else{
                    tmp = (mFileSize / 3) - 1;
                    start = 0;
                    end = tmp;
                    for (int i = 0;i < 3;i++){
                        mDownloadTask.get(i).setRange(start,end);
                        start = end + 1;
                        end = start + tmp;
                    }
                    end = start + mFileSize % 4;
                    mDownloadTask.get(3).setRange(start,end);
                }

                mFile = new File(mFilePath +"/"+ mDownloadModel.getName());

                startDownload();
            }
        }
    }
    class DownloadSpeedTask extends TimerTask {
        private long prev = 0;
        public void run() {
            if((mDurationTime % 1000) == 0)
            {
                mSpeed =  (mDownloadedSize - prev);
                prev = mDownloadedSize;
                Log.e("DownloadSpeedTask", String.valueOf(mSpeed));
            }
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDownloadManager.onDownloadSizeChanged();
                }
            });

            mDurationTime += 100;
           // mDownloadManager.onDownloadSizeChanged();
        }
    }
}