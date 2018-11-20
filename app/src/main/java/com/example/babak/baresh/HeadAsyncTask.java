package com.example.babak.baresh;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


@SuppressLint("NewApi")
public class HeadAsyncTask extends AsyncTask<String, Integer, Integer> {

    private static final String TAG = "MyActivity";
    private HttpDownloadListener mListener;
    //private Boolean mPartialContent;
    private long mFileSize;
    private String mFileName;
    private String mUrl;
    private boolean mSuccessful;
    private int mResponseCode;
    public HeadAsyncTask(HttpDownloadListener listener) {
        //mPartialContent = false;
        mSuccessful = false;
        mListener = listener;
        mResponseCode = 0;
    }
    @Override
    protected Integer doInBackground(String... urls) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            mUrl = urls[0];
            urlConnection = (HttpURLConnection) new URL(urls[0]).openConnection();
            urlConnection.setRequestMethod("HEAD");
            urlConnection.setRequestProperty("Content-Type", "some/type");
            //urlConnection.setRequestProperty("Range","bytes=0-124");
            urlConnection.connect();
            mResponseCode = urlConnection.getResponseCode();
            Log.e("DOWNLOAD2", String.valueOf(mResponseCode));
            if(mResponseCode == 200){
                String Content_Length = urlConnection.getHeaderField("Content-Length");
                mFileSize = Long.parseLong(Content_Length);
                String disposition = urlConnection.getHeaderField("Content-Disposition");
                //String contentType = httpConn.getContentType();
                //int contentLength = httpConn.getContentLength();
                if (disposition != null) {
                    // extracts file name from header field
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        mFileName = disposition.substring(index + 10,
                                disposition.length() - 1);
                    }
                } else {
                    // extracts file name from URL
                    String str = urls[0].toString();
                    mFileName = str.substring(str.lastIndexOf("/") + 1,
                            str.length());
                }
            }else{
                mSuccessful = false;
            }
        } catch (IOException e) {
            Log.e("PlaceholderFragment", "Error ", e);
            mSuccessful = false;
            return mResponseCode;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return mResponseCode;
    }
    @Override
    protected void onPostExecute(Integer integer) {
        if(mListener != null)
            mListener.onHeadFinished(mResponseCode);
    }

    public long getFileSize() {
        return mFileSize;
    }

    public String getFileName() {
        return mFileName;
    }

    public String getUrl() {
        return mUrl;
    }

    public boolean isSuccessful() {
        return mSuccessful;
    }
}