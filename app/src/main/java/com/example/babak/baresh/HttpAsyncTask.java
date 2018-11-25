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
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

@SuppressLint("NewApi")
public class HttpAsyncTask extends AsyncTask<Void, Integer, Boolean> {

    enum Error{
        ERROR_NO_ERROR,
        ERROR_TIMEOUT,
        ERROR_HOST_UNKNOWN
    }
    enum Authenticate{
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
    private Authenticate mAuthenticateType;
    private boolean mAuthenticateEnable;
    private String mLogin;
    private String mPassword;
    private Error mError;
    public HttpAsyncTask(HttpDownloadListener listener,String url,
                         boolean authenticateEnable,String username,String password) {
        mUrl = url;
        mSuccessful = false;
        mListener = listener;
        mResponseCode = 0;
        mAuthenticateEnable = authenticateEnable;
        mLogin = username;
        mPassword = password;
        mError = Error.ERROR_NO_ERROR;
    }
    @Override
    protected Boolean doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                urlConnection = (HttpURLConnection) new URL(mUrl).openConnection();
                urlConnection.setFollowRedirects(false);
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Connection","keep-alive");
                urlConnection.setRequestProperty("Content-Type", "some/type");
                urlConnection.setConnectTimeout(500);
                if(mAuthenticateEnable) {
                    DigestAuthentication auth = DigestAuthentication.fromResponse(urlConnection);
                    auth.username(mLogin).password(mPassword);
                    if (!auth.canRespond()) {
                        mSuccessful = false;
                    } else {
                        urlConnection = (HttpURLConnection) new URL(mUrl).openConnection();
                        urlConnection.setRequestProperty(DigestChallengeResponse.HTTP_HEADER_AUTHORIZATION,
                                auth.getAuthorizationForRequest("GET", urlConnection.getURL().getPath()));
                    }
                }
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
                    String www_Authenticate = urlConnection.getHeaderField("WWW-Authenticate");
                    if (www_Authenticate != null) {
                        if (www_Authenticate.contains("Basic")) {
                            mAuthenticateType = Authenticate.Authenticate_Basic;
                        }
                        if (www_Authenticate.contains("Digest")) {
                            mAuthenticateType = Authenticate.Authenticate_Digest;
                        }
                    }else{
                        mSuccessful = false;
                        mResponseMessage = "خطای ناشناخته";
                    }
                }
            }catch (SocketTimeoutException exception) {
                Log.e("PlaceholderFragment", "Error ", exception);
                mResponseMessage = "زمان طولانی برای اتصال";
                mSuccessful = false;
                mError = Error.ERROR_TIMEOUT;
                return false;
            }
            catch (UnknownHostException exception) {
                Log.e("PlaceholderFragment", "Error ", exception);
                mResponseMessage = "آدرس موجود نمی باشد";
                mSuccessful = false;
                mError = Error.ERROR_HOST_UNKNOWN;
                return false;
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                mResponseMessage = e.toString();
                mSuccessful = false;
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
        if(mListener != null)
            mListener.onHeadFinished(mResponseCode,result);
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