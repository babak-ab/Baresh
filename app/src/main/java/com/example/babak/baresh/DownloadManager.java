package com.example.babak.baresh;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DownloadManager extends BaseAdapter implements DownloadInfoDialogListener {
    private HashMap<Long,Downloader> mDataSet;
    //private HashMap<Long,HttpAsyncTask> mHeadAsyncTasks;
    private ArrayList<DownloadModel> mDownloadModel;
    private ArrayList<Long> mKeys;
    private DBHelper mdb;
    private DownloadInfoDialog dialog;
    private Context mContext;
    private Integer mNumberOnThread;
    private HttpAsyncTask mHeadAsyncTask;
    private boolean mDownloadAccepted;
    private boolean mDownloadRejected;
    private boolean mHeadFinished;
    private Long mCreateDownloadId;
    private static class ViewHolder {
        TextView txtName;
        TextView txtType;
        TextView txtVersion;
        ImageView info;
    }
    public DownloadManager(HashMap<Long,Downloader>  data,Context context){
        super();
        mDataSet = data;
        mContext = context;
        mNumberOnThread = 4;
        mKeys = new ArrayList<>();
        mdb = new DBHelper(context);
        mDownloadModel = mdb.getAllLinks();
        mDownloadAccepted = false;
        mDownloadRejected = false;
        mHeadFinished = false;

        Iterator itr = mDownloadModel.iterator();
        while(itr.hasNext()) {
            DownloadModel model = (DownloadModel) itr.next();
            Downloader downloader = new Downloader(model,mdb.getAllTasks(model.getDownloadId()),
                    this,mContext);
            mDataSet.put(model.getDownloadId(),downloader);
            mKeys.add(model.getDownloadId());
        }
        notifyDataSetChanged();
    }
    public boolean createDownload(String url){
        mCreateDownloadId = mdb.insertLink("",url,"",0,0);
        mHeadAsyncTask = new HttpAsyncTask(this,mCreateDownloadId);
        mHeadAsyncTask.execute(url);

        mDownloadAccepted = false;
        mDownloadRejected = false;
        mHeadFinished = false;

        dialog = new DownloadInfoDialog(mContext,mCreateDownloadId,url);
        dialog.setListener(this);
        dialog.show();

        return true;
    }
    public void removeDownload(long downloadId){
        mdb.deleteLink(downloadId);
        mHeadAsyncTask = null;
        mdb.deleteLink(mCreateDownloadId);
        Downloader downloader = mDataSet.remove(downloadId);
        downloader = null;
        notifyDataSetChanged();
    }
    public void onDownloadAccepted(Long downloadId){
        if(dialog != null)
            dialog.dismiss();
        //mDataSet.get(downloadId).setDownloadAccepted(true);
        //notifyDataSetChanged();
        mDownloadAccepted = true;
        createLinkComelete();
    }

    @Override
    public void onDownloadReject(Long downloadId) {
        if(dialog != null)
            dialog.dismiss();
        mDownloadRejected = true;
        for (Map.Entry<Long,TaskModel> entry : mDataSet.get(downloadId).getTasksModel().entrySet()) {
            mdb.deleteTask(entry.getKey());
        }
        mdb.deleteLink(mCreateDownloadId);
        mDataSet.remove(downloadId);
    }
    public void onHeadFinished(long downloadId) {
        mHeadFinished = true;
        if(dialog != null){
            if(dialog.isShowing()){
                dialog.setFileSizeChanged(mHeadAsyncTask.getFileSize());
            }
        }
        createLinkComelete();
    }
    public void onDownloadSizeChanged() {
        notifyDataSetChanged();
    }
    public void onDownloadFinished(Long downloadId) {
        notifyDataSetChanged();
    }
    public void onDownloadPause(Long downloadId) {
        mdb.updateDownloadedLink(downloadId,mDataSet.get(downloadId).getDownloadedSize());
        HashMap<Long,TaskModel> tasks = mDataSet.get(downloadId).getTasksModel();
        for (Map.Entry<Long,TaskModel> entry : tasks.entrySet()) {
            mdb.updateTask(entry.getKey(),
                    entry.getValue().getStart(),entry.getValue().getEnd());
        }
//        for(int i = 0;i < tasks.size(); i++){
//            mdb.updateTask(tasks.get(i).getTaskId(),
//                    tasks.get(i).getStart(),tasks.get(i).getEnd());
//        }
        notifyDataSetChanged();
    }
    public void onDownloadStart(Long downloadId) {
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return mDataSet.size();
    }

    @Override
    public Object getItem(int i) {
        return mDataSet.get(mKeys.get(i));
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Downloader dataModel = (Downloader)getItem(position);

        View rowView = inflater.inflate(R.layout.download_row_layout, parent, false);

        final ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        if(dataModel.getStatus() == Downloader.Status.PAUSE){
            imageView.setImageResource(android.R.drawable.ic_media_pause);
        }else if(dataModel.getStatus() == Downloader.Status.RUNNING){
            imageView.setImageResource(android.R.drawable.ic_media_play);
        }
        final TextView fileName = (TextView) rowView.findViewById(R.id.fileName_textView) ;
        fileName.setText(dataModel.getFileName());

        final ProgressBar progress = (ProgressBar)rowView.findViewById(R.id.download_progressBar);
        if(dataModel.getFileSize() > 0)
            progress.setProgress((int)(dataModel.getDownloadedSize() / (float)dataModel.getFileSize() * 100.0));

        String speedStr = getSizeToString(dataModel.getSpeed()) +"/s";
        final TextView speedTextView = (TextView)rowView.findViewById(R.id.speed_textView);
        speedTextView.setText(speedStr);

        final TextView downloadSize = (TextView)rowView.findViewById(R.id.downloadSize_textView);
        downloadSize.setText(getSizeToString(dataModel.getDownloadedSize())+"/"+
                getSizeToString(dataModel.getFileSize()));


        String duStr;
        long time = dataModel.getRemindTime();
        if(time == 0){
             duStr = "--:--:--";
        }else{
             duStr = getTimeToString(time);
        }
        String tiStr =  getTimeToString(dataModel.getDurationTime());

        final TextView duration = (TextView)rowView.findViewById(R.id.duration_textView);
        duration.setText(tiStr + "/" + duStr);

//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                imageView.setImageResource(android.R.drawable.ic_media_pause);
//            }
//        });
        return rowView;
    }
    public String getSizeToString(long size){
        String string = "0";
        double sValue;
        Long TB = (long)1024 * 1024 * 1024 * 1024;
        Long GB = (long)1024 * 1024 * 1024;
        Long MB = (long)1024 * 1024;
        Long KB = (long)1024;
        if(size < 1024){
            string = String.valueOf(size);
        }else if(size >= KB && size < MB){
            sValue = size / 1024.0;
            string = String.format ("%.2f", sValue)  + "KB";
        }else if(size >= MB && size < GB){
            sValue = size / (1024.0 * 1024.0);
            string = String.format ("%.2f", sValue)  + "MB";
        }else if(size >= GB && size < TB){
            sValue = size / (1024.0 * 1024.0 * 1024.0);
            string = String.format ("%.2f", sValue)  + "GB";
        }else{
            sValue = size / (1024.0 * 1024.0 * 1024.0 * 1024.0);
            string = String.format ("%.2f", sValue) + "TB";
        }
        return string;
    }
    public String getTimeToString(long second){
        String str;
        long minute = TimeUnit.SECONDS.toMinutes(second);
        long hour = TimeUnit.SECONDS.toHours(second);
        long day = TimeUnit.SECONDS.toDays(second);
        second -= TimeUnit.MINUTES.toSeconds(minute);
        if(day > 1){
             str =  String.format("%d days", day);
        }else if(day > 0){
             str =  String.format("%d day", day);
        }else{
            if(hour > 0){
                str = String.format ("%02d:%02d:%02d", hour,minute,second);
            }else{
                str = String.format ("%02d:%02d", minute,second);
            }
        }
        return str;
    }
    private void createLinkComelete(){
        if (mDownloadAccepted && mHeadFinished) {
            DownloadModel model = new DownloadModel();
            model.setDownloadId(mCreateDownloadId);
            model.setUrl(mHeadAsyncTask.getUrl());
            model.setName(mHeadAsyncTask.getFileName());
            model.setFileSize(mHeadAsyncTask.getFileSize());
            model.setDownloaded(0);
            HashMap<Long,TaskModel> task_list = new HashMap<>(4);
            long tmp;
            long start;
            long end;
            if (model.getFileSize() % 4 == 0) {
                tmp = (model.getFileSize() / 4) - 1;
                start = 0;
                end = tmp;
                for (int i = 0; i < 4; i++) {
                    long taskId = mdb.insertTask(mCreateDownloadId,start,end);
                    TaskModel task = new TaskModel();
                    task.setStart(start);
                    task.setEnd(end);
                    task_list.put(taskId,task);
                    start = end + 1;
                    end = start + tmp;
                }
            } else {
                tmp = (model.getFileSize() / 3) - 1;
                start = 0;
                end = tmp;
                for (int i = 0; i < 3; i++) {
                    long taskId = mdb.insertTask(mCreateDownloadId,start,end);
                    TaskModel task = new TaskModel();
                    task.setStart(start);
                    task.setEnd(end);
                    task_list.put(taskId,task);
                    start = end + 1;
                    end = start + tmp;
                }
                end = start + model.getFileSize() % 4;
                //task_list.get(3).setStart(start);
                //task_list.get(3).setEnd(end);
                long taskId = mdb.insertTask(mCreateDownloadId,start,end);
                TaskModel task = new TaskModel();
                task.setStart(start);
                task.setEnd(end);
                task_list.put(taskId,task);
            }
            Downloader downloader = new Downloader(model, task_list, this, mContext);
            mDataSet.put(mCreateDownloadId, downloader);
            mKeys.add(mCreateDownloadId);
            mdb.updateSizeLink(mCreateDownloadId, mHeadAsyncTask.getFileSize());
            mdb.updateLink(mCreateDownloadId, mDataSet.get(mCreateDownloadId).getFileName(),
                    mDataSet.get(mCreateDownloadId).getUrl(), mDataSet.get(mCreateDownloadId).getFile().getAbsolutePath());
            mHeadAsyncTask = null;
            //mdb.deleteLink(mCreateDownloadId);
            notifyDataSetChanged();
        }
    }

}
