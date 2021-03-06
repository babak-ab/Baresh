package com.example.babak.baresh;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DownloadManagerService extends Service implements HttpDownloadListener{
    static final int NOTIFICATION_ID = 543;
    public static boolean isServiceRunning = false;
    private HttpAsyncTask mHeadAsyncTask;
    private HashMap<Long,Downloader> mDownloaderHashMap;
    private DBHelper mdb;
    private long mCreateDownloadId;
    private MyBinder mLocalBinder = new MyBinder();
    private CallBack mCallBack;
    public DownloadManagerService() {

    }
    public HashMap<Long,Downloader> getDownloaderHashMap(){
        return  mDownloaderHashMap;
    }
    public void startDownload(long downloadId){
        mDownloaderHashMap.get(downloadId).startDownload();
    }
    public void stopDownload(long downloadId){
        mDownloaderHashMap.get(downloadId).stopDownload();
    }
    public Downloader.Status getStatus(long downloadId){
        return mDownloaderHashMap.get(downloadId).getStatus();
    }
    public void removeDownload(long downloadId){
        for (Map.Entry<Long,TaskModel> entry : mDownloaderHashMap.get(downloadId).getTasksModel().entrySet()) {
            mdb.deleteTask(entry.getKey());
        }
        mdb.deleteLink(downloadId);
        Downloader downloader = mDownloaderHashMap.remove(downloadId);
        downloader = null;
        if(mCallBack != null){
            mCallBack.onNotifyDataSetChanged();
        }
    }
    public void onDownloadStart(Long downloadId) {
        if(mCallBack != null){
            mCallBack.onNotifyDataSetChanged();
        }
    }
    public void onDownloadReject(Long downloadId) {
        for (Map.Entry<Long,TaskModel> entry : mDownloaderHashMap.get(downloadId).getTasksModel().entrySet()) {
            mdb.deleteTask(entry.getKey());
        }
        mdb.deleteLink(mCreateDownloadId);
        mDownloaderHashMap.remove(downloadId);
    }
    public void onDownloadSizeChanged() {
        if(mCallBack != null){
            mCallBack.onNotifyDataSetChanged();
        }
    }
    public void onDownloadFinished(Long downloadId) {
        vibration(200);
        mdb.updateDownloadedLink(downloadId, mDownloaderHashMap.get(downloadId).getDownloadedSize(),
                mDownloaderHashMap.get(downloadId).getDurationTime());
        HashMap<Long,TaskModel> tasks = mDownloaderHashMap.get(downloadId).getTasksModel();
        for (Map.Entry<Long,TaskModel> entry : tasks.entrySet()) {
            mdb.updateTask(entry.getKey(),
                    entry.getValue().getStart(),entry.getValue().getEnd());
        }
        if(mCallBack != null){
            mCallBack.onNotifyDataSetChanged();
        }
    }
    public void onDownloadPause(Long downloadId) {
        mdb.updateDownloadedLink(downloadId, mDownloaderHashMap.get(downloadId).getDownloadedSize(),
                mDownloaderHashMap.get(downloadId).getDurationTime());
        HashMap<Long,TaskModel> tasks = mDownloaderHashMap.get(downloadId).getTasksModel();
        for (Map.Entry<Long,TaskModel> entry : tasks.entrySet()) {
            mdb.updateTask(entry.getKey(),
                    entry.getValue().getStart(),entry.getValue().getEnd());
        }
        if(mCallBack != null){
            mCallBack.onNotifyDataSetChanged();
        }
    }
    private void updateDownload(long downloadId,Long fileSize,String fileName) {
        Downloader downloader = mDownloaderHashMap.get(mCreateDownloadId);
        downloader.setStatus(Downloader.Status.DOWNLOADING);
        downloader.setFileSize(fileSize);
        downloader.setFileName(fileName);
        downloader.setDownloaded(0);
        mdb.updateLink(downloadId,fileName,downloader.getUrl(), String.valueOf(fileSize));
        HashMap<Long, TaskModel> task_list = new HashMap<>(4);
        long tmp;
        long start;
        long end;
        if (downloader.getFileSize() % 4 == 0) {
            tmp = (downloader.getFileSize() / 4) - 1;
            start = 0;
            end = tmp;
            for (int i = 0; i < 4; i++) {
                long taskId = mdb.insertTask(mCreateDownloadId, start, end);
                TaskModel task = new TaskModel();
                task.setTaskId(taskId);
                task.setDownloadId(mCreateDownloadId);
                task.setStart(start);
                task.setEnd(end);
                task_list.put(taskId, task);
                start = end + 1;
                end = start + tmp;
            }
        } else {
            tmp = (downloader.getFileSize() / 3) - 1;
            start = 0;
            end = tmp;
            for (int i = 0; i < 3; i++) {
                long taskId = mdb.insertTask(mCreateDownloadId, start, end);
                TaskModel task = new TaskModel();
                task.setTaskId(taskId);
                task.setDownloadId(mCreateDownloadId);
                task.setStart(start);
                task.setEnd(end);
                task_list.put(taskId, task);
                start = end + 1;
                end = start + tmp;
            }
            end = start + downloader.getFileSize() % 4;
            //task_list.get(3).setStart(start);
            //task_list.get(3).setEnd(end);
            long taskId = mdb.insertTask(mCreateDownloadId, start, end);
            //DownloadAsyncTask task = new DownloadAsyncTask(taskId,start,end,downloader);
            TaskModel task = new TaskModel();
            task.setTaskId(taskId);
            task.setDownloadId(mCreateDownloadId);
            task.setStart(start);
            task.setEnd(end);
            task_list.put(taskId, task);
        }
        downloader.setDownloadTask(task_list);
        mdb.updateSizeLink(mCreateDownloadId, fileSize);
        downloader.startDownload();
    }
    public void createDownload(String url,boolean isAuthentication,String username,String password) {
        String filename = "Default.html";
        if(url.contains("/")){
            filename = url.substring(url.lastIndexOf("/") + 1,
                    url.length());
        }else{
            filename = url + ".html";
        }
        if(filename.isEmpty()){
            String [] list = url.split("/");
            if(list.length > 0) {
                filename = list[list.length - 1] + ".html";
            }else{
                filename = "Default.html";
            }
        }
        mCreateDownloadId = mdb.insertLink(filename,url,0,0);
        Downloader downloader = new Downloader(mCreateDownloadId,url,filename,this);
        downloader.setStatus(Downloader.Status.CONNECTING);
        mDownloaderHashMap.put(mCreateDownloadId, downloader);
//        mdb.updateLink(mCreateDownloadId, mDownloaderHashMap.get(mCreateDownloadId).getFileName(),
//                mDownloaderHashMap.get(mCreateDownloadId).getUrl());
        if (mCallBack != null) {
            mCallBack.onNotifyDataSetChanged();
        }
        mHeadAsyncTask = new HttpAsyncTask(this,url,isAuthentication,username,password);
        mHeadAsyncTask.execute();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mdb = new DBHelper(getApplicationContext(),this);
        mDownloaderHashMap = mdb.getAllLinks();
        startServiceWithNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction().equals("START")) {
            startServiceWithNotification();
        }
        else stopMyService();
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        isServiceRunning = false;
        super.onDestroy();
    }

    void startServiceWithNotification() {
        if (isServiceRunning) return;
        isServiceRunning = true;
         // NO_CLEAR makes the notification stay when the user performs a "delete all" command
        startForeground(NOTIFICATION_ID, getMyActivityNotification(""));
    }

    private Notification getMyActivityNotification(String text){
        // The PendingIntent to launch our activity if the user selects
        // this notification
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setAction("MAIN");  // A string containing the action name
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(text)
                //.setTicker(getResources().getString(R.string.app_name))
                //.setContentText(getResources().getString(R.string.my_string))
                .setSmallIcon(R.drawable.ic_stat_14dp)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(contentPendingIntent)
                .setOngoing(true)
                .build();
        notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;
        return  notification;
    }
    void stopMyService() {
        stopForeground(true);
        stopSelf();
        isServiceRunning = false;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    @Override
    public void onHeadFinished(int response,boolean result) {
        vibration(150);
        if(result){
            switch (response){
                case 200:
                    updateDownload(mCreateDownloadId,mHeadAsyncTask.getFileSize(),mHeadAsyncTask.getFileName());
                    break;
                case 401:
                    if (mCallBack != null) {
                        mCallBack.onAuthenticationRequest(mCreateDownloadId);
                    }
                    break;
                default:
                    Downloader downloader = mDownloaderHashMap.get(mCreateDownloadId);
                    downloader.setStatus(Downloader.Status.ERROR);
                    downloader.setError(response);
                    downloader.setErrorString(mHeadAsyncTask.getResponseMessage());
                    if (mCallBack != null) {
                        mCallBack.onNotifyDataSetChanged();
                    }
                    break;
            }
        }else{
//         if(!isNetworkAvailable()){
//             String text = "شبکه قطع می باشد";
//             Notification notification = getMyActivityNotification(text);
//             NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//             mNotificationManager.notify(NOTIFICATION_ID, notification);
//             Downloader downloader = mDownloaderHashMap.get(mCreateDownloadId);
//             downloader.setStatus(Downloader.Status.ERROR);
//             downloader.setError(response);
//             downloader.setErrorString(mHeadAsyncTask.getResponseMessage());
//             if (mCallBack != null) {
//                 mCallBack.onNotifyDataSetChanged();
//                 mCallBack.onNetworkDisconnected();
//             }
//         }else{
//             if(!isInternetAvailable()){
//                 String text = "اینترنت قطع می باشد";
//                 Notification notification = getMyActivityNotification(text);
//                 NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                 mNotificationManager.notify(NOTIFICATION_ID, notification);
//                 Downloader downloader = mDownloaderHashMap.get(mCreateDownloadId);
//                 downloader.setStatus(Downloader.Status.ERROR);
//                 downloader.setError(response);
//                 downloader.setErrorString(mHeadAsyncTask.getResponseMessage());
//                 if (mCallBack != null) {
//                     mCallBack.onNotifyDataSetChanged();
//                     mCallBack.onInternetDisconnected();
//                 }
//             }
//         }
        }
    }
    public void authenticationAccepted(long id,String username,String password){
        mHeadAsyncTask = new HttpAsyncTask(DownloadManagerService.this,mDownloaderHashMap.get(id).getUrl(),
                true,username,password);
        mHeadAsyncTask.execute();
    }
//    public boolean isNetworkAvailable() {
//        final ConnectivityManager connectivityManager = ((ConnectivityManager) DownloadManagerService.this.getSystemService(Context.CONNECTIVITY_SERVICE));
//        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
//    }
//    public boolean isInternetAvailable() {
//        try {
//            InetAddress address = InetAddress.getByName("www.google.com");
//            return !address.equals("");
//        } catch (IOException e) {
//
//        }
//        return false;
//    }
    public void vibration(int millisecond){
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(millisecond, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(millisecond);
        }
    }
    public interface CallBack {
        void onNotifyDataSetChanged();
        void onAuthenticationRequest(long id);
        void onNetworkDisconnected();
        void onInternetDisconnected();
    }
    public void setCallBack(CallBack callBack) {
        mCallBack = callBack;
        if(mCallBack != null)
            mCallBack.onNotifyDataSetChanged();
    }
    public class MyBinder extends Binder {
        public DownloadManagerService getService() {
            return DownloadManagerService.this;
        }
    }
}
