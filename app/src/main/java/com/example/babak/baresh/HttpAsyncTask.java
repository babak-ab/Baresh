package com.example.babak.baresh;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressLint("NewApi")
public class HttpAsyncTask extends AsyncTask<String, Integer, Integer> {


    enum Authenticate{
        Authenticate_Failed,
        Authenticate_NotValid,
        Authenticate_Basic,
        Authenticate_Bearer,
        Authenticate_Digest,
        Authenticate_HOBA,
        Authenticate_Mutual,
        Authenticate_Negotiate,
        Authenticate_OAuth,
        Authenticate_SCRAM_SHA_1,
        Authenticate_SCRAM_SHA_256,
        Authenticate_vapid
    }
    private static final String TAG = "MyActivity";
    private HttpDownloadListener mListener;
    //private Boolean mPartialContent;
    private long mFileSize;
    private String mFileName;
    private String mUrl;
    private boolean mSuccessful;
    private int mResponseCode;
    private String mLocation;
   // private Authenticate mAuthenticateType;
    private boolean mAuthenticateEnable;
    private String mLogin;
    private String mPassword;
    public HttpAsyncTask(HttpDownloadListener listener,boolean authenticateEnable,String username,String password) {
        mSuccessful = false;
        mListener = listener;
        mResponseCode = 0;
        mAuthenticateEnable = authenticateEnable;
        mLogin = username;
        mPassword = password;
    }
    @Override
    protected Integer doInBackground(String... urls) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                mUrl = urls[0];
                urlConnection = (HttpURLConnection) new URL(urls[0]).openConnection();
                urlConnection.setFollowRedirects(false);
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Connection","keep-alive");
                urlConnection.setRequestProperty("Content-Type", "some/type");
                //urlConnection.setRequestProperty("Range","bytes=0-124");
//                if(mAuthenticateEnable){
//                    urlConnection.setRequestProperty("Authorization", "Basic " + Base64.encodeToString((mLogin+":"+mPassword).getBytes(), Base64.NO_WRAP));
//                }
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
                }else if(mResponseCode == 301 || mResponseCode == 302 || mResponseCode == 303){
                    mLocation = urlConnection.getHeaderField("Location");
                    mSuccessful = false;
                }else if(mResponseCode == 401){
                    if(mAuthenticateEnable) {
                        String www_Authenticate = urlConnection.getHeaderField("WWW-Authenticate");
                        if (www_Authenticate != null) {
                            if (www_Authenticate.contains("Basic")) {
                                urlConnection = (HttpURLConnection) new URL(urls[0]).openConnection();
                                urlConnection.setFollowRedirects(false);
                                urlConnection.setInstanceFollowRedirects(false);
                                urlConnection.setRequestMethod("GET");
                                urlConnection.setRequestProperty("Connection","keep-alive");
                                urlConnection.setRequestProperty("Content-Type", "some/type");
                                urlConnection.setRequestProperty("Authorization", "Basic " + Base64.encodeToString((mLogin+":"+mPassword).getBytes(), Base64.NO_WRAP));
                                urlConnection.connect();
                                mResponseCode = urlConnection.getResponseCode();
                                Log.e("DOWNLOAD2", String.valueOf(mResponseCode));
                                if(mResponseCode == 200){
                                    mSuccessful = true;
                                }else{
                                    mSuccessful = false;
                                }
                            }
                            if (www_Authenticate.contains("Digest")) {
                            }
                        }
                    }else{
                        mSuccessful = false;
                    }

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

    public long getFileSize() { return mFileSize; }

    public String getFileName() { return mFileName; }

    public String getUrl() { return mUrl; }

    public boolean isSuccessful() { return mSuccessful; }

    public int responseCode() { return  mResponseCode; }

    public String getLocation() { return mLocation; }

    public boolean isAuthenticateEnable() { return mAuthenticateEnable; }

    public void setAuthenticateEnable(boolean authenticateEnable) { mAuthenticateEnable = authenticateEnable; }

    public String getLogin() { return mLogin; }

    public void setLogin(String login) { mLogin = login; }

    public String getPassword() { return mPassword; }

    public void setPassword(String password) { mPassword = password; }

}