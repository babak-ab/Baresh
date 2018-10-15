package com.example.babak.baresh;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private PopupWindow mPopupWindow;
    private LinearLayout mLinearLayout;
    private Button button;
    private Downloader download;
    private DBHelper mDb;

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
        String[] values = new String[] { "Ubuntu", "Android", "iPhone",
                "Windows", "Ubuntu", "Android", "iPhone", "Windows" };
        DownloadArrayAdapter adapter = new DownloadArrayAdapter(this,values);
        l.setAdapter(adapter);



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
//                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
//
//                // Inflate the custom layout/view
//                View customView = inflater.inflate(R.layout.add_layout,null);
//
//                // Initialize a new instance of popup window
//                mPopupWindow = new PopupWindow(
//                        customView,
//                        LayoutParams.WRAP_CONTENT,
//                        LayoutParams.WRAP_CONTENT
//                );
//
//                // Set an elevation value for popup window
//                // Call requires API level 21
//                if(Build.VERSION.SDK_INT>=21){
//                    mPopupWindow.setElevation(5.0f);
//                }
//                // Closes the popup window when touch outside.
//                mPopupWindow.setOutsideTouchable(true);
//                mPopupWindow.setFocusable(true);
//                // Removes default background.
//                mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                mPopupWindow.showAtLocation(mLinearLayout, Gravity.CENTER,0,0);

//                final Dialog dialog = new Dialog(getApplicationContext());
//                dialog.setContentView(R.layout.add_layout);
//                dialog.setTitle("Title...");
//
//                // set the custom dialog components - text, image and button
//                TextView text = (TextView) dialog.findViewById(R.id.text);
//                text.setText("Android custom dialog example!");
                //dialog.show();
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.add_layout);
                dialog.setTitle("Title...");
                final TextView text = (TextView) dialog.findViewById(R.id.editText_address);
                text.setText("http://ipv4.download.thinkbroadband.com/5MB.zip");
                Button dialogButton = (Button) dialog.findViewById(R.id.button_accept);
                // if button is clicked, close the custom dialog
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            download = new Downloader(new URL((String) text.getText()));
                            download.header();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                });
                dialog.show();
                break;
            default:
                break;
        }

        return true;
    }
}
