package com.example.babak.baresh;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;

@SuppressLint("NewApi")
public class HttpAsyncTask extends AsyncTask<URL, Integer, Integer> {

    private static final String TAG = "MyActivity";
    private Downloader mDownloader;
    private Boolean mResumeable = false;
    private Long mFileSize;
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
                if(responseCode == 206){
                    mResumeable = true;
                    String Content_Length = urlConnection.getHeaderField("Content-Length");
                    mFileSize = Long.parseLong(Content_Length);
                }else if(responseCode == 200){
                    mResumeable = false;
                    String Content_Length = urlConnection.getHeaderField("Content-Length");
                    mFileSize = Long.parseLong(Content_Length);
                }else{
                    mResumeable = false;
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
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
        return 1;
    }
    @Override
    protected void onPostExecute(Integer integer) {
        mDownloader.setResumable(mResumeable);
        mDownloader.setFileSize(mFileSize);
        mDownloader.onDownloadFinish();
    }
}