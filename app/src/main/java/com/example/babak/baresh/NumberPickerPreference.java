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

public class NumberPickerPreference extends DialogPreference {

    // allowed range
    private int mDialogLayoutResId = R.layout.pref_dialog_num_thread;
    private int mValue;
    private int DEFAULT_VALUE;

    public NumberPickerPreference(Context context) {
        // Delegate to other constructor
        this(context, null);
    }

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs,R.attr.preferenceStyle);
        DEFAULT_VALUE = 4;
        setDialogLayoutResource(R.layout.pref_dialog_num_thread);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
    }
    public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        // Delegate to other constructor
        this(context, attrs, defStyleAttr, defStyleAttr);
    }
    public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setValue(int value) {
        mValue = value;
        persistInt(mValue);
        setSummary(String.valueOf(" حداکثر " + convertToEnglishDigits(String.valueOf(mValue)) + ""));
    }

    public int getValue() {
        return mValue;
    }
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            mValue = this.getPersistedInt(DEFAULT_VALUE);
        } else {
            // Set default state from the XML attribute
            mValue = (Integer) defaultValue;
            persistInt(mValue);
        }
        setSummary(String.valueOf(" حداکثر " + convertToEnglishDigits(String.valueOf(mValue)) + ""));
    }
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index,  DEFAULT_VALUE);
    }
    public static String convertToEnglishDigits(String value)
    {
        String newValue = value.replace("١", "1").replace("٢", "2").replace("٣", "3").replace("٤", "4").replace("٥", "5")
                .replace("٦", "6").replace("7", "٧").replace("٨", "8").replace("٩", "9").replace("٠", "0")
                .replace("۱", "1").replace("۲", "2").replace("۳", "3").replace("۴", "4").replace("۵", "5")
                .replace("۶", "6").replace("۷", "7").replace("۸", "8").replace("۹", "9").replace("۰", "0");

        return newValue;
    }
}
