package com.example.babak.baresh;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class DownloadInfoDialog extends Dialog {

    public DownloadInfoDialog(@NonNull Context context,String url) {
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
    public void setFileType(int type){

    }
    public void setFileSize(int size){
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
}