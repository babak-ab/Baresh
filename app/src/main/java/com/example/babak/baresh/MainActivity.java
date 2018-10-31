package com.example.babak.baresh;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private PopupWindow mPopupWindow;
    private LinearLayout mLinearLayout;
    private Button button;
    private Downloader download;
    private DownloadManager mDownloadManager;
    private HashMap<Long,Downloader> dataModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        mContext = getApplicationContext();
        mLinearLayout = (LinearLayout) findViewById(R.id.activity_main_ll);
//        Typeface fontAwesomeFont = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");
//        TextView fontAwesomeAndroidIcon = (TextView) findViewById(R.id.font_awesome_android_icon);
//        TextView fontAwesomeAreaChartIcon = (TextView) findViewById(R.id.font_awesome_area_chart_icon);
//        TextView fontAwesomeCubesIcon = (TextView) findViewById(R.id.font_awesome_cubes_icon);
//        TextView fontAwesomeMobilePhoneIcon = (TextView) findViewById(R.id.font_awesome_mobile_phone_icon);
//
//        fontAwesomeAndroidIcon.setTypeface(fontAwesomeFont);
//        fontAwesomeAreaChartIcon.setTypeface(fontAwesomeFont);
//        fontAwesomeCubesIcon.setTypeface(fontAwesomeFont);
//        fontAwesomeMobilePhoneIcon.setTypeface(fontAwesomeFont);

        // new HttpAsyncTask().execute();
        //HttpAsyncTask task = new HttpAsyncTask();
        //String[] params = new String[2];
        //task.execute();

       // mButton = (Button) findViewById(R.id.);


        ListView l = (ListView) findViewById(R.id.listView);
        dataModels = new HashMap<>();
        mDownloadManager = new DownloadManager(dataModels,this);
        l.setAdapter(mDownloadManager);



        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
            }
        });

    }
    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_add:
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.add_dialog_layout);
                dialog.setTitle("Add link for download...");
                final TextView text = (TextView) dialog.findViewById(R.id.editText_address);
                text.setText("http://dl.hastidl.me/data/Friends.S01.E02.Hastidl.mkv");
                //text.setText("https://host2.rjmusicmedia.com/media/podcast/mp3-192/Abo-Atash-109.mp3");
                //text.setText("http://ftp2.nluug.nl/languages/qt/official_releases/qt-installer-framework/3.0.4/QtInstallerFramework-win-x86.exe")   ;
                Button dialogButton = (Button) dialog.findViewById(R.id.button_accept);
                // if button is clicked, close the custom dialog
                dialogButton.setOnClickListener(new AddDialogButtonClicked(dialog,(String)text.getText().toString()));
                dialog.show();
                break;
            default:
                break;
        }

        return true;
    }

    class AddDialogButtonClicked  implements View.OnClickListener {
        private String mText;
        private Dialog mDialog;
        public AddDialogButtonClicked(Dialog dialog, String text) {
            this.mText = text;
            this.mDialog = dialog;
        }
        @Override
        public void onClick(View v) {
            try {
                mDialog.dismiss();
               mDownloadManager.createDownload(new URL(this.mText));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
