package com.example.babak.baresh;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadInfoDialog extends Dialog{

    private DownloadInfoDialogListener mListener;
    public DownloadInfoDialog(@NonNull Context context, String urlString) {
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
        editText.setText(urlString);
        //spinner.setAdapter(new GroupsArrayAdapter(MainActivity.this, categories.toArray(new String[0])));

        final Button button_start = (Button)this.findViewById(R.id.button_downloadInfoStart);
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(mListener != null)
                    mListener.onDownloadAccepted();
                //DownloadInfoDialog.this.dismiss();
            }
        });

        final Button button_cancel = (Button)this.findViewById(R.id.button_downloadInfoCancel);
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(mListener != null)
                    mListener.onDownloadAccepted();
                //DownloadInfoDialog.this.dismiss();
            }
        });
    }

    public void setListener(DownloadInfoDialogListener listener){
        mListener = listener;
    }

    public void setFileSizeChanged(Long size) {
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
        TextView textView = (TextView)this.findViewById(R.id.textView_downloadInfoSize);
        textView.setText(string);
    }

    public void setFileTypeChanged(String type) {
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