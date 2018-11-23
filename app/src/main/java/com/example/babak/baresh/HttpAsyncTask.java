package com.example.babak.baresh;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.albroco.barebonesdigest.DigestAuthentication;
import com.albroco.barebonesdigest.DigestChallengeResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

@SuppressLint("NewApi")
public class HttpAsyncTask extends AsyncTask<Void, Integer, Integer> {


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
    private String mResponseMessage;
    private String mLocation;
   // private Authenticate mAuthenticateType;
    private boolean mAuthenticateEnable;
    private String mLogin;
    private String mPassword;
    public HttpAsyncTask(HttpDownloadListener listener,String url,
                         boolean authenticateEnable,String username,String password) {
        mUrl = url;
        mSuccessful = false;
        mListener = listener;
        mResponseCode = 0;
        mAuthenticateEnable = authenticateEnable;
        mLogin = username;
        mPassword = password;
    }
    @Override
    protected Integer doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                urlConnection = (HttpURLConnection) new URL(mUrl).openConnection();
                urlConnection.setFollowRedirects(false);
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Connection","keep-alive");
                urlConnection.setRequestProperty("Content-Type", "some/type");
                urlConnection.connect();
                mResponseCode = urlConnection.getResponseCode();
                mResponseMessage = urlConnection.getResponseMessage();
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
                        String str = mUrl.toString();
                        mFileName = str.substring(str.lastIndexOf("/") + 1,
                                str.length());
                    }
                    mSuccessful = true;
                }else if(mResponseCode == 301 || mResponseCode == 302 || mResponseCode == 303){
                    mLocation = urlConnection.getHeaderField("Location");
                    mUrl = mLocation;
                    urlConnection = (HttpURLConnection) new URL(mUrl).openConnection();
                    urlConnection.setFollowRedirects(false);
                    urlConnection.setInstanceFollowRedirects(false);
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Connection","keep-alive");
                    urlConnection.setRequestProperty("Content-Type", "some/type");
                    urlConnection.connect();
                    mResponseCode = urlConnection.getResponseCode();
                    mResponseMessage = urlConnection.getResponseMessage();
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
                            String str = mUrl.toString();
                            mFileName = str.substring(str.lastIndexOf("/") + 1,
                                    str.length());
                        }
                        mSuccessful = true;
                    }else{
                        mSuccessful = false;
                    }
                }else if(mResponseCode == 401){
                    if(mAuthenticateEnable) {
                        String www_Authenticate = urlConnection.getHeaderField("WWW-Authenticate");
                        if (www_Authenticate != null) {
                            if (www_Authenticate.contains("Basic")) {
                                urlConnection = (HttpURLConnection) new URL(mUrl).openConnection();
                                urlConnection.setFollowRedirects(false);
                                urlConnection.setInstanceFollowRedirects(false);
                                urlConnection.setRequestMethod("GET");
                                urlConnection.setRequestProperty("Connection","keep-alive");
                                urlConnection.setRequestProperty("Content-Type", "some/type");
                                urlConnection.setRequestProperty("Authorization", "Basic " + Base64.encodeToString((mLogin+":"+mPassword).getBytes(), Base64.NO_WRAP));
                                urlConnection.connect();
                                mResponseCode = urlConnection.getResponseCode();
                                mResponseMessage = urlConnection.getResponseMessage();
                                Log.e("DOWNLOAD2", String.valueOf(mResponseCode));
                                if(mResponseCode == 200){
                                    String Content_Length = urlConnection.getHeaderField("Content-Length");
                                    mFileSize = Long.parseLong(Content_Length);
                                    String disposition = urlConnection.getHeaderField("Content-Disposition");
                                    if (disposition != null) {
                                        int index = disposition.indexOf("filename=");
                                        if (index > 0) {
                                            mFileName = disposition.substring(index + 10,
                                                    disposition.length() - 1);
                                        }
                                    } else {
                                        String str = mUrl.toString();
                                        mFileName = str.substring(str.lastIndexOf("/") + 1,
                                                str.length());
                                    }
                                    mSuccessful = true;
                                }else{
                                    mSuccessful = false;
                                }
                            }
                            if (www_Authenticate.contains("Digest")) {
                                urlConnection = (HttpURLConnection) new URL(mUrl).openConnection();
                                urlConnection.setFollowRedirects(false);
                                urlConnection.setInstanceFollowRedirects(false);
                                urlConnection.setRequestMethod("GET");
                                urlConnection.setRequestProperty("Connection","keep-alive");
                                urlConnection.setRequestProperty("Content-Type", "some/type");
                                DigestAuthentication auth = DigestAuthentication.fromResponse(urlConnection);
                                auth.username(mLogin).password(mPassword);
                                if (!auth.canRespond()) {
                                    mSuccessful = false;
                                }else{
                                    urlConnection = (HttpURLConnection) new URL(mUrl).openConnection();
                                    urlConnection.setRequestProperty(DigestChallengeResponse.HTTP_HEADER_AUTHORIZATION,
                                            auth.getAuthorizationForRequest("GET", urlConnection.getURL().getPath()));
                                    urlConnection.connect();
                                    mResponseCode = urlConnection.getResponseCode();
                                    mResponseMessage = urlConnection.getResponseMessage();
                                    Log.e("DOWNLOAD2", String.valueOf(mResponseCode));
                                    if(mResponseCode == 200){
                                        mSuccessful = true;
                                    }else{
                                        mSuccessful = false;
                                    }
                                }
                            }
                        }
                    }else{
                        mSuccessful = false;
                    }
                }
            } catch (UnknownHostException exception) {
                Log.e("PlaceholderFragment", "Error ", exception);
                mResponseMessage = "آدرس موجود نمی باشد";
                mSuccessful = false;
                return 0;
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                mResponseMessage = e.toString();
                mSuccessful = false;
                return 0;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        return 1;
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

    public String getResponseMessage() { return mResponseMessage; }

}