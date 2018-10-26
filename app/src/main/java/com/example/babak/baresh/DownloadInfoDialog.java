package com.example.babak.baresh;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadInfoDialog extends Dialog implements DownloaderListener {


    public DownloadInfoDialog(@NonNull Context context, String url) {
        super(context);
        setContentView(R.layout.download_info_dialog_layout);
        setTitle("Add link for download...");
        List<String> categories = new ArrayList<String>();
        categories.add("General");
        categories.add("Archives");
        categories.add("Documents");
        categories.add("Musics");
        categories.add("Videos");
        categories.add("Programs");
        // String[] values = {"General","Archives","Documents","Musics","Videos","Programs"};

        final Spinner spinner = (Spinner) this.findViewById(R.id.spinner_downloadInfoGroups);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, categories);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        final EditText editText = (EditText)this.findViewById(R.id.editText_downloadInfoUrl);
        editText.setText(url);
        //spinner.setAdapter(new GroupsArrayAdapter(MainActivity.this, categories.toArray(new String[0])));
    }

    @Override
    public void onDownloadFinish() {

    }

    @Override
    public void onFileSizeChanged(Integer size) {
        String string = "0";
        double sValue;
        if(size < 1024){
            string = String.valueOf(size);
        }else if(size >= 1024 && size < 1024 * 1024){
            sValue = size / 1024.0;
            string = String.format ("%.2f", sValue)  + "KB";
        }else if(size > 1024 * 1024 && size < 1024 * 1024 * 1024){
            sValue = size / (1024.0 * 1024.0);
            string = String.format ("%.2f", sValue)  + "MB";
        }else if(size > 1024 * 1024 * 1024 && size < 1024 * 1024 * 1204 * 1024){
            sValue = size / (1024.0 * 1024.0 * 1024.0);
            string = String.format ("%.2f", sValue)  + "GB";
        }else{
            sValue = size / (1024.0 * 1024.0 * 1024.0 * 1024.0);
            string = String.format ("%.2f", sValue) + "TB";
        }
        TextView textView = (TextView)this.findViewById(R.id.textView_downloadInfoSize);
        textView.setText(string);
    }

    @Override
    public void onFileTypeChanged(String type) {
        final ImageView imageView = (ImageView)this.findViewById(R.id.imageView_downloadInfo);
        if(type.contains("video")){
            imageView.setImageResource(R.drawable.ic_local_movies_purple_900_48dp);
        }else if(type.contains("music") || type.contains("audio")){
            imageView.setImageResource(R.drawable.ic_music_note_red_900_48dp);
        }else if(type.contains("application") && type.contains("exe")){
            imageView.setImageResource(R.drawable.ic_settings_applications_teal_900_48dp);
        }else{
            imageView.setImageResource(R.drawable.ic_android_light_green_900_48dp);
        }
    }
}