package com.example.babak.baresh;


import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Downloader{
    private static final String TAG = "MyActivity";
    enum Status{
        STOP,RUNNING,PAUSE,FINISH
    }
    private HashMap<Long,DownloadAsyncTask> mDownloadTask;
    private Timer mTimerUpdate;
    private TimerTask mSpeedTask;
    private String mFileType;
    private Boolean mIsPartialContent;

   // private long mDownloadedSize;
    private String mFilePath;
    private File mFile;
    private long mSpeed;
    private long mDurationTime;
    private boolean mDownloadHeadFinished;
    private boolean mDownloadAccepted;

    private DownloadModel mDownloadModel;
    private HashMap<Long,TaskModel> mTasksModel;
    private int mError;
    private Status mStatus;
    private Context mContext;
    private DownloadManager mDownloadManager;
    public Downloader(DownloadModel downloadModel, HashMap<Long,TaskModel> tasksModel,
                      DownloadManager downloadManager, Context context) {
        mDownloadHeadFinished = false;
        mDownloadAccepted = false;
        mContext = context;
        mDownloadModel = downloadModel;
        mTasksModel = tasksModel;
        mStatus = Status.STOP;
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

        //mDownloadedSize = 0;
        mDownloadManager = downloadManager;
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mimeType = fileNameMap.getContentTypeFor(mDownloadModel.getUrl());
        if(mimeType != null){
            mFileType = mimeType;
        }else{
            mFileType = MimeTypeMap.getSingleton().
                    getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(mDownloadModel.getUrl()));
        }
        if(mFileType == null){
            mFileType = "Unknown";
        }

        mFile = new File(mFilePath +"/"+ mDownloadModel.getName());

        mTimerUpdate = new Timer();

    }
    public HashMap<Long,TaskModel> getTasksModel(){
        return mTasksModel;
    }
    public Status getStatus() {
        return mStatus;
    }
    public String getUrl(){
        return  mDownloadModel.getUrl();
    }
    public boolean isPartialContent() {
        return mIsPartialContent;
    }
    public long getFileSize() {
        return mDownloadModel.getFileSize();
    }
    public String getFileName() {
        return mDownloadModel.getName();
    }
    public long getDownloadedSize() {
        return mDownloadModel.getDownloaded();
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
            return (mDownloadModel.getFileSize() - mDownloadModel.getDownloaded()) / mSpeed;
        else
            return 0;
    }
    public File getFile() {
        return mFile;
    }
    public void onDownloadCancel(long taskId){
        mTasksModel.get(taskId).setStart(mDownloadTask.get(taskId).getStartByte());
        mDownloadTask.remove(taskId);
        if(mDownloadTask.size() == 0) {
            mSpeedTask.cancel();
            mStatus = Status.PAUSE;
            mDownloadManager.onDownloadPause(mDownloadModel.getDownloadId());
        }
    }
    public void onDownloadStarted(long taskId,DownloadAsyncTask task){
          mDownloadTask.put(taskId,task);
          if(mDownloadTask.size() == 4){
              mStatus = Status.RUNNING;
              mDownloadManager.onDownloadStart(mDownloadModel.getDownloadId());
          }
    }
    public void onDownloadedSizeChanged(long downloadedSize){
        mDownloadModel.setDownloaded(mDownloadModel.getDownloaded() + downloadedSize);
        ///mDownloadManager.onDownloadSizeChanged();
    }
//    public void onHeadFinished(String fileName, long fileSize, boolean partialContent){
//        mDownloadHeadFinished = true;
//        mDownloadModel.setName(fileName);
//        mFileSize = fileSize;
//        mIsPartialContent = partialContent;
//        mFile = new File(mFilePath +"/"+ mDownloadModel.getName());
//        mDownloadManager.onHeadFinished(mDownloadModel.getDownloadId());
//    }
    public void onDownloadFinished(long taskId){
        mDownloadTask.remove(taskId);
        if(mDownloadTask.size() == 0) {
            mStatus = Status.FINISH;
            mDownloadManager.onDownloadFinished(mDownloadModel.getDownloadId());
            mSpeedTask.cancel();
        }
    }
    public void stopDownload(){
        for (Map.Entry<Long,DownloadAsyncTask> entry : mDownloadTask.entrySet()) {
            //Log.d(TAG,"onDownloadCancel2");
            entry.getValue().cancel(true);
        }
    }
    public void startDownload() {
        for (Map.Entry<Long,TaskModel> entry : mTasksModel.entrySet()) {
            DownloadAsyncTask task = new DownloadAsyncTask(entry.getKey(),entry.getValue().getStart(),
                    entry.getValue().getEnd(),this);
            task.execute(mDownloadModel.getUrl());
        }
        mDurationTime = 0;
        mSpeedTask = new DownloadSpeedTask();
        mTimerUpdate.scheduleAtFixedRate(mSpeedTask, 0, 100);
    }

    class DownloadSpeedTask extends TimerTask {
        private long prev = 0;
        public void run() {
            if((mDurationTime % 1000) == 0)
            {
                mSpeed =  (getDownloadedSize() - prev);
                prev = getDownloadedSize();
                Log.e("DownloadSpeedTask", String.valueOf(mSpeed));
            }
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDownloadManager.onDownloadSizeChanged();
                }
            });
            mDurationTime += 100;
        }
    }
}