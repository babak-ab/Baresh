package com.example.babak.baresh;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressLint("NewApi")
public class HttpAsyncTask extends AsyncTask<String, Integer, Integer> {

    private static final String TAG = "MyActivity";
    private Downloader mDownloader;
    private Boolean mPartialContent = false;
    private long mFileSize;
    private String mFileName;
    public HttpAsyncTask(Downloader downloader) {
        mDownloader = downloader;
    }
    @Override
    protected Integer doInBackground(String... urls) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                urlConnection = (HttpURLConnection) new URL(urls[0]).openConnection();
                urlConnection.setRequestMethod("HEAD");
                urlConnection.setRequestProperty("Content-Type", "some/type");
                urlConnection.setRequestProperty("Range","bytes=0-124");
                urlConnection.connect();
                int responseCode = urlConnection.getResponseCode();
                Log.e("DOWNLOAD2", String.valueOf(responseCode));
                if(responseCode == 206){
                    mPartialContent = true;
                    //String Content_Length = urlConnection.getHeaderField("Content-Length");
                    urlConnection = (HttpURLConnection) new URL(urls[0]).openConnection();
                    urlConnection.setRequestMethod("HEAD");
                    urlConnection.setRequestProperty("Content-Type", "some/type");
                    urlConnection.connect();
                    responseCode = urlConnection.getResponseCode();
                    if(responseCode == 200){
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
                    }
                }else if(responseCode == 200){
                    mPartialContent = false;
                    String Content_Length = urlConnection.getHeaderField("Content-Length");
                    mFileSize =  Long.parseLong(Content_Length);
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
                    mPartialContent = false;
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
        mDownloader.onHeadFinished(mFileName,mFileSize,mPartialContent);
//        mDownloader.setPartialContent(mPartialContent);
//        mDownloader.setFileSize(mFileSize);
//        mDownloader.setFileName(mFileName);
    }
}