package com.example.babak.baresh;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.os.Environment;
@SuppressLint("ValidFragment")
class PathDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    private ListView dialogListView;
    private List<String> fileList = new ArrayList<String>();
    private File curFolder;
    private File root;

    public static PathDialogFragmentCompat newInstance(String key) {
        final PathDialogFragmentCompat
                fragment = new PathDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
       // mNumberPicker = (NumberPicker) view.findViewById(R.id.dialogList);
        dialogListView = (ListView)view.findViewById(R.id.dialogList);
        // Exception: There is no TimePicker with the id 'edit' in the dialog.
        if (dialogListView == null) {
            throw new IllegalStateException("Dialog view must contain a TimePicker with id 'edit'");
        }else {
            root = new File(Environment
                    .getExternalStorageDirectory()
                    .getAbsolutePath());

            curFolder = root;
            ListDir(curFolder);
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // Get the current values from the TimePicker

        }
    }

    void ListDir(File f) {
        curFolder = f;

        File[] files = f.listFiles();
        fileList.clear();
        for (File file : files) {
            fileList.add(file.getPath());
        }
        ArrayAdapter<String> directoryList
                = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, fileList);
        dialogListView.setAdapter(directoryList);
    }
}
