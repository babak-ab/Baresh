package com.example.babak.baresh;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressLint("NewApi")
public class HttpAsyncTask extends AsyncTask<URL, Integer, Integer> {

    private static final String TAG = "MyActivity";
    private Downloader mDownloader;
    private Boolean mResume = false;
    private int mFileSize;
    public HttpAsyncTask(Downloader downloader) {
        mDownloader = downloader;
    }
    @Override
    protected Integer doInBackground(URL... urls) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                urlConnection = (HttpURLConnection) urls[0].openConnection();
                urlConnection.setRequestMethod("HEAD");
                urlConnection.setRequestProperty("Content-Type", "some/type");
                urlConnection.setRequestProperty("Range","bytes=0-124");
                urlConnection.connect();
                int responseCode = urlConnection.getResponseCode();
                Log.e("DOWNLOAD2", String.valueOf(responseCode));
                if(responseCode == 206){
                    mResume = true;
                    //String Content_Length = urlConnection.getHeaderField("Content-Length");
                    urlConnection = (HttpURLConnection) urls[0].openConnection();
                    urlConnection.setRequestMethod("HEAD");
                    urlConnection.setRequestProperty("Content-Type", "some/type");
                    urlConnection.connect();
                    responseCode = urlConnection.getResponseCode();
                    if(responseCode == 200){
                        String Content_Length = urlConnection.getHeaderField("Content-Length");

                        mFileSize = Integer.parseInt(Content_Length);
                    }else{
                    }
                }else if(responseCode == 200){
                    mResume = false;
                    String Content_Length = urlConnection.getHeaderField("Content-Length");
                    mFileSize = Integer.parseInt(Content_Length);
                }else{
                    mResume = false;
                    mFileSize = 0;
                }
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        return 1;
    }
    @Override
    protected void onPostExecute(Integer integer) {
        Log.e("DOWNLOAD", String.valueOf(mFileSize));
        mDownloader.setResumable(mResume);
        mDownloader.setFileSize(mFileSize);
    }
}