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
import android.widget.NumberPicker;

@SuppressLint("ValidFragment")
class NumberPickerDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    private NumberPicker mNumberPicker;

    public static NumberPickerDialogFragmentCompat newInstance(String key) {
        final NumberPickerDialogFragmentCompat
                fragment = new NumberPickerDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mNumberPicker = (NumberPicker) view.findViewById(R.id.numberPicker);
        // Exception: There is no TimePicker with the id 'edit' in the dialog.
        if (mNumberPicker == null) {
            throw new IllegalStateException("Dialog view must contain a TimePicker with id 'edit'");
        }else {
            mNumberPicker.setMaxValue(8);
            mNumberPicker.setMinValue(1);
            mNumberPicker.setWrapSelectorWheel(true);

            DialogPreference preference = getPreference();
            Integer value = null;
            if (preference instanceof NumberPickerPreference) {
                value = ((NumberPickerPreference) preference).getValue();
            }

            // Set the time to the TimePicker
            if (value != null) {
                mNumberPicker.setValue(value);
            }
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // Get the current values from the TimePicker
            int value;
            value = mNumberPicker.getValue();
            // Save the value
            DialogPreference preference = getPreference();
            if (preference instanceof NumberPickerPreference) {
                NumberPickerPreference timePreference = ((NumberPickerPreference) preference);
                // This allows the client to ignore the user value.
                if (timePreference.callChangeListener(value)) {
                    // Save the value
                    timePreference.setValue(value);
                }
            }
        }
    }
}
