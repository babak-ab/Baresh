package com.example.babak.baresh;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownloadManager extends ArrayAdapter<Downloader> {
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
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.download_row_layout, parent, false);
        final ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageResource(android.R.drawable.ic_media_pause);
            }
        });
        return rowView;
    }
}
