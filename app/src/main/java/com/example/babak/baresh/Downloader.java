package com.example.babak.baresh;


import android.content.Context;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.net.FileNameMap;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Downloader{
    private static final String TAG = "MyActivity";
    private HttpAsyncTask headerTask;
    private HashMap<Integer,DownloadAsyncTask> mDownloadTask;
    private Timer mTimerSpeed;
    private Timer mTimerStartDownload;
    private URL mUrl;
    private Long mId;
    private String mFileType;
    private Boolean mIsPartialContent;
    private long mFileSize;
    private Context mContext;
    private String mFileName;
    private long mDownloadedSize;
    private String mFilePath;
    private File mFile;
    private int mSpeed;
    private int mDurationTime;
    private int mError;
    private boolean mDownloadHeadFinished;
    private boolean mDownloadAccepted;
    private DownloadManager mDownloadManager;
    public Downloader(Long id,URL url,DownloadManager downloadManager,Context context) {
        mDownloadHeadFinished = false;
        mDownloadAccepted = false;
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
        mUrl = url;
        mId = id;
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mimeType = fileNameMap.getContentTypeFor(url.toString());
        if(mimeType != null){
            mFileType = mimeType;
        }else{
            mFileType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url.toString()));
        }
        if(mFileType == null){
            mFileType = "Unknown";
        }
        mContext = context;
        mTimerSpeed = new Timer();
        mTimerStartDownload = new Timer();
        TimerTask downloadTask = new StartDownloadTask();
        mTimerStartDownload.scheduleAtFixedRate(downloadTask, 0, 100);
        headerTask.execute(mUrl);
    }
    public void setDownloadAccepted(boolean accepted) {
        mDownloadAccepted = accepted;
        if(mDownloadAccepted == false)
            mTimerStartDownload.cancel();
    }

    public URL getUrl(){
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
        return mDownloadedSize;
    }
    public Long getId() {
        return mId;
    }
    public int getSpeed() {
        return mSpeed;
    }
    public int getDurationTime() {
        return mDurationTime;
    }
    public int getDuration() {
        if(mSpeed > 0)
            return (int) (mFileSize - mDownloadedSize) / mSpeed;
        else
            return 0;
    }
    public File getFile() {
        return mFile;
    }
    public void onDownloadedSizeChanged(long downloadedSize){
        mDownloadedSize += downloadedSize;
        mDownloadManager.onDownloadSizeChanged();
    }
    public void onHeadFinished(String fileName, long fileSize, boolean partialContent){
        mDownloadHeadFinished = true;
        mFileName = fileName;
        mFileSize = fileSize;
        mIsPartialContent = partialContent;
        mDownloadManager.setFileSize(mFileSize);
    }
    public void onDownloadFinished(int taskId){
        mDownloadTask.remove(taskId);
        if(mDownloadTask.size() == 0)
            mTimerSpeed.cancel();
    }
    private void startDownload() {
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

        mFile = new File(mFilePath +"/"+ mFileName);

        mDownloadTask.get(0).execute(mUrl);
        mDownloadTask.get(1).execute(mUrl);
        mDownloadTask.get(2).execute(mUrl);
        mDownloadTask.get(3).execute(mUrl);

        mDurationTime = 0;
        TimerTask speedTask = new DownloadSpeedTask();
        mTimerSpeed.scheduleAtFixedRate(speedTask, 0, 1000);
    }
    class StartDownloadTask extends TimerTask {
        public void run() {
            if(mDownloadAccepted && mDownloadHeadFinished){
                mTimerStartDownload.cancel();
                startDownload();
            }
        }
    }
    class DownloadSpeedTask extends TimerTask {
        private long prev = 0;
        public void run() {
            mSpeed = (int)(mDownloadedSize - prev);
            prev = mDownloadedSize;
            mDurationTime++;
        }
    }
}