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
    private Timer mTimer;
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
    private int mTime;
    private int mError;
    private DownloadManager mDownloadManager;
    //DownloadInfoDialog mDownloadDialog;
    public Downloader(Long id,URL url,DownloadManager downloadManager,Context context) {
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
//        mDownloadTask.pu
//        mDownloadTask[0] = new DownloadAsyncTask(0,this);
//        mDownloadTask[1] = new DownloadAsyncTask(1,this);
//        mDownloadTask[2] = new DownloadAsyncTask(2,this);
//        mDownloadTask[3] = new DownloadAsyncTask(3,this);
      //  mDownloadTask[4] = new DownloadAsyncTask(this);
      //  mDownloadTask[5] = new DownloadAsyncTask(this);
      //  mDownloadTask[6] = new DownloadAsyncTask(this);
      //  mDownloadTask[7] = new DownloadAsyncTask(this);


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
        mTimer = new Timer();
        header();
    }
    public void header(){
        headerTask.execute(mUrl);
    }

    public void download() {

        long tmp;
        long start;
        long end;
        if(mFileSize % 4 == 0){
            tmp = (mFileSize / 4) - 1;
            start = 0;
            end = tmp;
            for (int i = 0;i < 4;i++){
                mDownloadTask.get(i).setRange((int)start,(int)end);
                start = end + 1;
                end = start + tmp;
            }
        }else{
            tmp = (mFileSize / 3) - 1;
            start = 0;
            end = tmp;
            for (int i = 0;i < 3;i++){
                mDownloadTask.get(i).setRange((int)start,(int)end);
                start = end + 1;
                end = start + tmp;
            }
            end = start + mFileSize % 4;
            mDownloadTask.get(3).setRange((int)start,(int)end);
        }

        mFile = new File(mFilePath +"/"+ mFileName);

        mDownloadTask.get(0).execute(mUrl);
        mDownloadTask.get(1).execute(mUrl);
        mDownloadTask.get(2).execute(mUrl);
        mDownloadTask.get(3).execute(mUrl);


        mTime = 0;
        TimerTask speedTask = new DownloadSpeedTask();
        mTimer.scheduleAtFixedRate(speedTask, 0, 1000);
        //mDownloadTask[4].execute(mUrl);
        //mDownloadTask[5].execute(mUrl);
        //mDownloadTask[6].execute(mUrl);
        //mDownloadTask[7].execute(mUrl);

    }


    public URL getUrl(){
        return  mUrl;
    }

    public boolean isPartialContent() {
        return mIsPartialContent;
    }

    public void setPartialContent(boolean partialContent) {
        this.mIsPartialContent = partialContent;
    }

    public long getFileSize() {
        return mFileSize;
    }

    public void setFileSize(long fileSize) {
        mFileSize = fileSize;
        mDownloadManager.setFileSize(mFileSize);
    }
    public void setFileType(String mFileType){
        this.mFileType = mFileType;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        this.mFileName = fileName;
    }

    public void setDownloadedSize(long downloadedSize){
        mDownloadedSize += downloadedSize;
        mDownloadManager.onDownloadSizeChanged();
    }
    public void onFinishDownload(int taskId){
        mDownloadTask.remove(taskId);
        if(mDownloadTask.size() == 0)
            mTimer.cancel();
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
    public int getTime() {
        return mTime;
    }
    public int getDuration() {

        if(mSpeed > 0)
            return (int)mFileSize / mSpeed;
        else
            return 0;
    }

    public File getFile() {
        return mFile;
    }

    class DownloadSpeedTask extends TimerTask {
        private long prev = 0;
        public void run() {
            mSpeed = (int)(mDownloadedSize - prev);
            prev = mDownloadedSize;
            mTime++;
        }
    }
}