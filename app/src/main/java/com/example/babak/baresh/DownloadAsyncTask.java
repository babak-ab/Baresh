package com.example.babak.baresh;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.logging.LogManager;

public class DownloadAsyncTask extends AsyncTask<URL, Integer, Integer> {

    private static final String TAG = "MyActivity";
    private Downloader mDownloader;
    private Integer mStartByte;
    private Integer mEndByte;
    private Integer mTaskId;
    File rootDir = Environment.getExternalStorageDirectory();

    public DownloadAsyncTask(int taskId,Downloader downloader) {
        mDownloader = downloader;
        mTaskId = taskId;
    }
    public void setRange(Integer startByte,Integer endByte){
        mStartByte = startByte;
        mEndByte = endByte;
    }
    @Override
    protected Integer doInBackground(URL... urls) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            File folder = new File(Environment.getExternalStorageDirectory() + "/myFolder");
            boolean succeed;
            if (!folder.exists()) {
                succeed = folder.mkdir();
                if (!succeed) {
                    Log.i("directory not created", "directory not created");
                } else {
                    Log.i("directory created", "directory created");
                }
            }
            urlConnection = (HttpURLConnection) urls[0].openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Range","bytes="+mStartByte.toString()+"-"+mEndByte.toString()+"");
            //urlConnection.setDoOutput(true);
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();
            if(responseCode == 200 || responseCode == 206){
                int lengthOfFile = urlConnection.getContentLength();

                //this is where the file will be seen after the download
                //FileOutputStream f = new FileOutputStream(new File(rootDir + “/my_downloads/”, fileName));
                //file input is from the url
                InputStream in = urlConnection.getInputStream();

               //File rootDir = Environment.getExternalStorageDirectory();
               OutputStream output = new FileOutputStream(new File("/sdcard/myFolder/"+String.valueOf(mTaskId)+".extension"));

                //here’s the download code
                byte[] buffer = new byte[mEndByte - mStartByte + 1];
                //byte[] buffer = new byte[1024];
                int len1 = 0;
                int total = 0;
                while ((len1 = in.read(buffer)) > 0) {
                    //total += len1; //total = total + len1
                    publishProgress(len1);
                    output.write(buffer, 0, len1);
                }
                output.close();
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
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        //mDownloader.setDownloadedSize(mEndByte - mStartByte + 1);
        mDownloader.onFinishDownload(mTaskId);
    }
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        //Log.e("DOWNLOAD123", String.valueOf(progress[0]));
        mDownloader.setDownloadedSize(progress[0]);
    }
}
