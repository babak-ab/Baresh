package com.example.babak.baresh;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class PathPreference extends DialogPreference {

    // allowed range
    private int mValue;
    private int DEFAULT_VALUE;

    public PathPreference(Context context) {
        // Delegate to other constructor
        this(context, null);
    }

    public PathPreference(Context context, AttributeSet attrs) {
        super(context, attrs,R.attr.preferenceStyle);
        DEFAULT_VALUE = 4;
        setDialogLayoutResource(R.layout.pref_dialog_path);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
    }
    public PathPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        // Delegate to other constructor
        this(context, attrs, defStyleAttr, defStyleAttr);
    }
    public PathPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

}
