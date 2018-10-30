package com.example.babak.baresh;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownloadManager extends ArrayAdapter<Downloader> implements DownloadInfoDialogListener {
    private ArrayList<Downloader> dataSet;
    Context mContext;
    private static class ViewHolder {
        TextView txtName;
        TextView txtType;
        TextView txtVersion;
        ImageView info;
    }
    public DownloadManager(ArrayList<Downloader> data,Context context){
        super(context,R.layout.download_row_layout, data);
        this.dataSet = data;
        this.mContext=context;
    }
    public void CreateDownload(URL url){
        Downloader downloader = new Downloader(mContext,url);
        final DownloadInfoDialog dialog = new DownloadInfoDialog(mContext,downloader,url.toString());
        dialog.setListener(this);
        downloader.addListener(dialog);
        dialog.show();

    }
    public void onDownloadAccepted(Downloader downloader){
        dataSet.add(downloader);
        notifyDataSetChanged();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.download_row_layout, parent, false);
        final ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        Downloader dataModel = getItem(position);
        final TextView fileName = (TextView) rowView.findViewById(R.id.fileName_textView) ;
        fileName.setText(dataModel.getFileName());


        final ProgressBar progress = (ProgressBar)rowView.findViewById(R.id.download_progressBar);
        progress.setProgress((int)dataModel.getDownloadedSize() / dataModel.getFileSize() * 100);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageResource(android.R.drawable.ic_media_pause);
            }
        });
        return rowView;
    }
}
