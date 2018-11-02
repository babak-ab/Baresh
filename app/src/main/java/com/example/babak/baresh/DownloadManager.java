package com.example.babak.baresh;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class DownloadManager extends BaseAdapter implements DownloadInfoDialogListener {
    private HashMap<Long,Downloader> dataSet;
    private ArrayList<Long> mKeys;
    private DBHelper mdb;
    private DownloadInfoDialog dialog;
    Context mContext;


    private static class ViewHolder {
        TextView txtName;
        TextView txtType;
        TextView txtVersion;
        ImageView info;
    }

    public DownloadManager(HashMap<Long,Downloader>  data,Context context){
        //super(context,R.layout.download_row_layout, data);
        super();
        this.dataSet = data;
        this.mContext = context;
        mdb = new DBHelper(context);
        mKeys = new ArrayList<>();
    }
    public void createDownload(URL url){
        long id = mdb.insertLink("",url.toString(),"",false);
        Downloader downloader = new Downloader(id,url,this,mContext);
        dataSet.put(id,downloader);
        mKeys.add(id);
        dialog = new DownloadInfoDialog(mContext,id,url.toString());
        dialog.setListener(this);
        dialog.show();
    }
    public void onDownloadAccepted(Long downloadId){
        if(dialog != null)
            dialog.dismiss();
        dataSet.get(downloadId).download();
        notifyDataSetChanged();
    }

    @Override
    public void onDownloadReject(Long downloadId) {
        dialog.dismiss();
        dataSet.remove(downloadId);
    }

    @Override
    public int getCount() {
        return dataSet.size();
    }

    @Override
    public Object getItem(int i) {
        return dataSet.get(mKeys.get(i));
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
    public void setFileSize(long mFileSize) {
        if(dialog != null){
            if(dialog.isShowing()){
                dialog.setFileSizeChanged(mFileSize);
            }
        }
    }
    public void onDownloadSizeChanged() {
        notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.download_row_layout, parent, false);
        final ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        Downloader dataModel = (Downloader)getItem(position);
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


        String duStr = getTimeToString(dataModel.getDuration());
        String tiStr =  getTimeToString(dataModel.getTime());

        final TextView duration = (TextView)rowView.findViewById(R.id.duration_textView);
        duration.setText(tiStr + "/" + duStr);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageResource(android.R.drawable.ic_media_pause);
            }
        });
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
}
