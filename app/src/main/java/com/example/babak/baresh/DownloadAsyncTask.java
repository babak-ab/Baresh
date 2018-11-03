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
import java.net.URL;

public class DownloadAsyncTask extends AsyncTask<String, Integer, Integer> {

    private static final String TAG = "MyActivity";
    private Downloader mDownloader;
    private Long mStartByte;
    private Long mEndByte;
    private Long mLastByte;
    private Integer mTaskId;
    File rootDir = Environment.getExternalStorageDirectory();

    public DownloadAsyncTask(int taskId,Downloader downloader) {
        mDownloader = downloader;
        mTaskId = taskId;
    }
    public void setRange(Long startByte,Long endByte){
        mStartByte = startByte;
        mEndByte = endByte;
    }
    @Override
    protected Integer doInBackground(String... urls) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            urlConnection = (HttpURLConnection) new URL(urls[0]).openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Range","bytes="+mStartByte.toString()+"-"+mEndByte.toString()+"");
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
                    total += len1;
                    publishProgress(len1);
                    store.write(buffer,0,len1);
                    mStartByte = mStartByte + total;
                    if (isCancelled())
                        break;
                }
               store.close();
            }
        } catch (IOException e) {
            Log.e("PlaceholderFragment", "Error ", e);
            return -1;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return 0;
    }
    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        mDownloader.onDownloadFinished(mTaskId);
    }
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        mDownloader.onDownloadedSizeChanged(progress[0]);
    }

    protected void onCancelled() {
        super.onCancelled();
        Log.e("PlaceholderFragment", "onCancelled ");
    }
}
