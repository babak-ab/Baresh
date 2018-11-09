package com.example.babak.baresh;


import android.app.Activity;
import android.app.DownloadManager;
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
    private String mUrl;
    private Long mDownloadId;
    private File mFile;
    private String mFileName;
    private Long mFileSize;
    private Long mDownloaded;

    private HashMap<Long,TaskModel> mTaskModel;
    private HashMap<Long,DownloadAsyncTask> mDownloadTask;
    private Timer mTimerUpdate;
    private TimerTask mSpeedTask;
    private String mFileType;
    private Boolean mIsPartialContent;

   // private long mDownloadedSize;
    private String mFilePath;
    private long mSpeed;
    private long mDurationTime;
    private boolean mDownloadHeadFinished;
    private boolean mDownloadAccepted;

    private int mNumTaskShouldBeStart;
    private int mError;
    private Status mStatus;
    private Context mContext;
    private DownloadManagerService mDownloadManager;
    public Downloader(long downloadId,String url,String fileName,DownloadManagerService downloadManager) {
        mDownloadHeadFinished = false;
        mDownloadAccepted = false;
        mStatus = Status.STOP;
        mUrl = url;
        mDownloadId = downloadId;
        mFileName = fileName;
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
        String mimeType = fileNameMap.getContentTypeFor(mUrl);
        if(mimeType != null){
            mFileType = mimeType;
        }else{
            mFileType = MimeTypeMap.getSingleton().
                    getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(mUrl));
        }
        if(mFileType == null){
            mFileType = "Unknown";
        }

        mFile = new File(mFilePath +"/"+ mFileName);

        mTimerUpdate = new Timer();

    }
    public HashMap<Long,TaskModel> getTasksModel(){
        return mTaskModel;
    }

    public void setDownloadTask(HashMap<Long, TaskModel> taskModel) {
        this.mTaskModel = taskModel;
    }
    public Status getStatus() {
        return mStatus;
    }
    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public void setDownloadId(Long mDownloadId) {
        this.mDownloadId = mDownloadId;
    }

    public void setFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    public void setFileSize(Long mFileSize) {
        this.mFileSize = mFileSize;
    }
    public void setDownloaded(long mDownloaded) {
        this.mDownloaded = mDownloaded;
    }

    public String getUrl(){
        return  mUrl;
    }
    public boolean isPartialContent() {
        return mIsPartialContent;
    }
    public long getFileSize() {
        return mFileSize;
    }
    public String getFileName() {
        return mFileName;
    }
    public long getDownloadedSize() {
        return mDownloaded;
    }
    public long getDownloadId() {
        return mDownloadId;
    }
    public long getSpeed() {
        return mSpeed;
    }
    public long getDurationTime() {
        return mDurationTime / 1000;
    }
    public long getRemindTime() {
        if(mSpeed > 0) {
            return (mFileSize - mDownloaded) / mSpeed;
        }
        else {
            return -1;
        }
    }
    public File getFile() {
        return mFile;
    }
    public void onDownloadCancel(long taskId){
        mTaskModel.get(taskId).setStart(mDownloadTask.get(taskId).getStartByte());
        mDownloadTask.remove(taskId);
        Log.d(TAG,"onDownloadCancel " + taskId);
        if(mDownloadTask.size() == 0) {
            mSpeedTask.cancel();
            mStatus = Status.PAUSE;
            mDownloadManager.onDownloadPause(mDownloadId);
        }
    }
    public void onDownloadStarted(long taskId,DownloadAsyncTask task){
        mDownloadTask.put(taskId,task);
        Log.d(TAG,"onDownloadStarted " + taskId + "," + mNumTaskShouldBeStart);
          if(mDownloadTask.size() == mNumTaskShouldBeStart){
              mStatus = Status.RUNNING;
              mDownloadManager.onDownloadStart(mDownloadId);
          }
    }
    public void onDownloadedSizeChanged(long downloadedSize){
        mDownloaded += downloadedSize;
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
        Log.d(TAG,"onDownloadFinished " + taskId);
        mTaskModel.get(taskId).setStart(mDownloadTask.get(taskId).getStartByte());
        mDownloadTask.remove(taskId);
        if(mDownloadTask.size() == 0) {
            mStatus = Status.FINISH;
            mDownloadManager.onDownloadFinished(mDownloadId);
            mSpeedTask.cancel();
        }
    }
    public void stopDownload(){
        for (Map.Entry<Long,DownloadAsyncTask> entry : mDownloadTask.entrySet()) {
            entry.getValue().cancel(true);
        }
    }
    public void startDownload() {
        Log.d(TAG,"startDownload");
        mNumTaskShouldBeStart = 0;
        for (Map.Entry<Long,TaskModel> entry : mTaskModel.entrySet()) {
//            Log.d(TAG,"onDownloadCancelAَ " + "," + entry.getKey()+ "," +entry.getValue().getStart() + "," +
//                    entry.getValue().getEnd());
            if(entry.getValue().getStart() < entry.getValue().getEnd()) {
                DownloadAsyncTask task = new DownloadAsyncTask(entry.getKey(), entry.getValue().getStart(),
                        entry.getValue().getEnd(), this);
                mNumTaskShouldBeStart++;
                Log.d(TAG,"startDownload "+mNumTaskShouldBeStart);;
                task.execute(mUrl);
            }
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
            }
//            ((Activity) mContext).runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            });
            mDownloadManager.onDownloadSizeChanged();
            mDurationTime += 100;
        }
    }
}