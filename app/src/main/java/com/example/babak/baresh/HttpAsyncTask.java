package com.example.babak.baresh;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;

@SuppressLint("NewApi")
public class HttpAsyncTask extends AsyncTask<URL, Integer, Integer> {

    private static final String TAG = "MyActivity";
    private Downloader mDownloader;
    private Boolean mResumeable = false;
    private int mFileSize;
    public HttpAsyncTask(Downloader downloader)
    {

        mDownloader = downloader;
    }
    @Override
    protected Integer doInBackground(URL... urls) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                //URL url = new URL("http://ipv4.download.thinkbroadband.com/5MB.zip");

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) urls[0].openConnection();
                urlConnection.setRequestMethod("HEAD");
                urlConnection.setRequestProperty("Content-Type", "some/type");
                urlConnection.setRequestProperty("Range","bytes=0-124");
                urlConnection.connect();
                int responseCode = urlConnection.getResponseCode();
                Log.e("DOWNLOAD2", String.valueOf(responseCode));
                if(responseCode == 206){
                    mResumeable = true;
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
                    mResumeable = false;
                    String Content_Length = urlConnection.getHeaderField("Content-Length");
                    mFileSize = Integer.parseInt(Content_Length);
                }else{
                    mResumeable = false;
                    mFileSize = 0;
                }
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
//                if (reader != null) {
//                    try {
//                        reader.close();
//                    } catch (final IOException e) {
//                        Log.e("PlaceholderFragment", "Error closing stream", e);
//                    }
//                }
            }
        return 1;
    }
    @Override
    protected void onPostExecute(Integer integer) {
        Log.e("DOWNLOAD", String.valueOf(mFileSize));
        mDownloader.setResumable(mResumeable);
        mDownloader.setFileSize(mFileSize);
        mDownloader.onDownloadFinish();
    }
}