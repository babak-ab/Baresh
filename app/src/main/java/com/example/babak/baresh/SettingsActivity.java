package com.example.babak.baresh;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.app.FragmentManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import java.io.File;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.github.angads25.filepicker.view.FilePickerPreference;

public class SettingsActivity extends AppCompatActivity {
    private static final int PICKFILE_REQUEST_CODE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_setting);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment preferenceFragment = new SettingsFragment();
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.pref_container, preferenceFragment);
        ft.commit();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat  implements  Preference.OnPreferenceClickListener,
            Preference.OnPreferenceChangeListener, DialogSelectionListener {

        private Preference dialogPreference;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preferences);
            dialogPreference = (Preference) getPreferenceScreen().findPreference("dialog_preference");
            String value = dialogPreference.getSharedPreferences().getString(dialogPreference.getKey(),"/mnt");
            dialogPreference.setSummary(value);
            dialogPreference.setOnPreferenceClickListener(this);
//            dialogPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//                public boolean onPreferenceClick(Preference preference) {
//                    final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
//                            .newDirectoryName("DialogSample")
//                            .build();
//                    mDialog = DirectoryChooserFragment.newInstance(config);
//                    mDialog.setTargetFragment(getParentFragment(), 0);
//                    mDialog.show(getParentFragment().getFragmentManager(), "android.support.v7.preference" +
//                            ".PreferenceFragment.DIALOG");
//                    return true;
//                }
//            });
        }
        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            // Try if the preference is one of our custom Preferences
            DialogFragment dialogFragment = null;
            if (preference instanceof NumberPickerPreference) {
                dialogFragment = NumberPickerDialogFragmentCompat.newInstance(preference.getKey());
            }
            if (dialogFragment != null) {
                // The dialog was created (it was one of our custom Preferences), show the dialog for it
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(this.getFragmentManager(), "android.support.v7.preference" +
                        ".PreferenceFragment.DIALOG");
            }
            else {
                super.onDisplayPreferenceDialog(preference);
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            DialogProperties properties = new DialogProperties();
            properties.selection_mode = DialogConfigs.SINGLE_MODE;
            properties.selection_type = DialogConfigs.DIR_SELECT;
            properties.root = new File(DialogConfigs.DEFAULT_DIR);
            properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
            properties.offset = new File(DialogConfigs.DEFAULT_DIR);
            properties.extensions = null;
            FilePickerDialog dialog = new FilePickerDialog(getContext(),properties);
            dialog.setTitle("Select a File");
            dialog.show();
            dialog.setDialogSelectionListener(this);
            return true;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            return false;
        }

        @Override
        public void onSelectedFilePaths(String[] files) {
                if(files.length > 0) {
                    dialogPreference.setSummary(files[0]);
                    SharedPreferences.Editor editor = dialogPreference.getSharedPreferences().edit();
                    editor.putString(dialogPreference.getKey(),files[0]);
                    editor.commit();
                }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String Fpath = data.getDataString();
        //TODO handle your request here
        if (requestCode == PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

        }
    }
}