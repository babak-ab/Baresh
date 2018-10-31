package com.example.babak.baresh;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
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
    public void setDownloadedSize(long downloadedSize) {
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
        progress.setProgress((int)(dataModel.getDownloadedSize() / dataModel.getFileSize() * 100));
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageResource(android.R.drawable.ic_media_pause);
            }
        });
        return rowView;
    }
}