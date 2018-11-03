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
import java.util.concurrent.TimeUnit;

public class DownloadManager extends BaseAdapter implements DownloadInfoDialogListener {
    private HashMap<Long,Downloader> mDataSet;
    private ArrayList<DownloadModel> mDownloadModel;
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
        super();
        mDataSet = data;
        mContext = context;
        mKeys = new ArrayList<>();
        mdb = new DBHelper(context);
        mDownloadModel = mdb.getAllLinks();
        Iterator itr = mDownloadModel.iterator();
        while(itr.hasNext()) {
            DownloadModel model = (DownloadModel) itr.next();
            Downloader downloader = new Downloader(model,this,mContext);
            mDataSet.put(model.getDownloadId(),downloader);
            mKeys.add(model.getDownloadId());
        }
        notifyDataSetChanged();
    }
    public boolean createDownload(String url){
        Iterator itr = mDownloadModel.iterator();
        while(itr.hasNext()) {
            DownloadModel model = (DownloadModel) itr.next();
            if(url.equals(model.getUrl())){
                return false;
            }
        }
        long id = mdb.insertLink("",url,"",false);
        DownloadModel model = new DownloadModel();
        model.setDownloadId(id);
        model.setUrl(url);
        Downloader downloader = new Downloader(model,this,mContext);
        downloader.startHead();
        mDataSet.put(id,downloader);
        mKeys.add(id);
        dialog = new DownloadInfoDialog(mContext,id,url);
        dialog.setListener(this);
        dialog.show();
        return true;
    }
    public void onDownloadAccepted(Long downloadId){
        if(dialog != null)
            dialog.dismiss();
        mDataSet.get(downloadId).setDownloadAccepted(true);
        notifyDataSetChanged();
    }

    @Override
    public void onDownloadReject(Long downloadId) {
        dialog.dismiss();
        mDataSet.remove(downloadId);
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
    public void onHeadFinished(long downloadId) {
        if(dialog != null){
            if(dialog.isShowing()){
                dialog.setFileSizeChanged(mDataSet.get(downloadId).getFileSize());
            }
        }
        mdb.updateContact(downloadId,mDataSet.get(downloadId).getFileName(),
                mDataSet.get(downloadId).getUrl(),"",
                mDataSet.get(downloadId).isPartialContent());
        notifyDataSetChanged();
    }
    public void onDownloadSizeChanged() {
        notifyDataSetChanged();
    }
    public void onDownloadFinished(Long downloadId) {
        notifyDataSetChanged();
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Downloader dataModel = (Downloader)getItem(position);

        View rowView = inflater.inflate(R.layout.download_row_layout, parent, false);

        final ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);

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
             duStr = "--:--";
        }else{
             duStr = getTimeToString(time);
        }
        String tiStr =  getTimeToString(dataModel.getDurationTime());

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
