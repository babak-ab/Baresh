package com.example.babak.baresh;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

public class DownloadAsyncTask extends AsyncTask<String, Integer, Boolean> {

    enum Error{
        ERROR_NO_ERROR,
        ERROR_TIMEOUT,
        ERROR_HOST_UNKNOWN
    }
    private static final String TAG = "MyActivity";
    private Downloader mDownloader;
    private Long mStartByte;
    private Long mEndByte;
    private Long mLastByte;
    private long mTaskId;
    private Error mError;
    File rootDir = Environment.getExternalStorageDirectory();
    public DownloadAsyncTask(long taskId,long startByte,long endByte,Downloader downloader) {
        mDownloader = downloader;
        mTaskId = taskId;
        mStartByte = startByte;
        mEndByte = endByte;
        mError = Error.ERROR_NO_ERROR;
    }
    @Override
    protected Boolean doInBackground(String... urls) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            urlConnection = (HttpURLConnection) new URL(urls[0]).openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Range","bytes="+mStartByte.toString()+"-"+mEndByte.toString()+"");
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();
            if(responseCode == 200 || responseCode == 206){
                int lengthOfFile = urlConnection.getContentLength();
                InputStream in = urlConnection.getInputStream();
                RandomAccessFile store = new RandomAccessFile(mDownloader.getFile().getPath(), "rw");
                store.seek(mStartByte);
                byte[] buffer = new byte[1024];
                int len1 = 0;
                long total = 0;
                while ((len1 = in.read(buffer)) > 0) {
                    if (isCancelled())
                        break;
                    total += len1;
                    mStartByte = mStartByte + len1;
                    publishProgress(len1);
                    store.write(buffer,0,len1);
                }
               store.close();
            }
        } catch (SocketTimeoutException e) {
            mError = Error.ERROR_TIMEOUT;
            return false;
        }
        catch (UnknownHostException e) {
            return false;
        } catch (IOException e) {
            mError = Error.ERROR_HOST_UNKNOWN;
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        mError = Error.ERROR_NO_ERROR;
        return true;
    }
    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        mDownloader.onDownloadFinished(mTaskId);
    }
    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
       // Log.e("PlaceholderFragment", "onProgressUpdate " + progress[0]);
        mDownloader.onDownloadedSizeChanged(progress[0]);
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDownloader.onDownloadStarted(mTaskId,this);
    }
    @Override
    protected void onCancelled() {
        super.onCancelled();
        mDownloader.onDownloadCancel(mTaskId);
        //Log.e("PlaceholderFragment", "onCancelled " + mStartByte);
    }
    public void setStartByte(Long startByte) {
        mStartByte = mStartByte;
    }

    public void setEndByte(Long endByte){
        mEndByte = endByte;
    }

    public Long getStartByte() {
        return mStartByte;
    }

    public Long getEndByte(){
        return mEndByte;
    }
}
