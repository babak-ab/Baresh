package com.example.babak.baresh;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements ServiceConnection, DownloadManagerService.CallBack {

    private Context mContext;
    private DownloadAdapter mDownloadAdapter;
    private ArrayList<Downloader> dataModels;
    private DownloadManagerService mDownloadManagerService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        mContext = MainActivity.this;
        isStoragePermissionGranted();
        ListView listView = (ListView) findViewById(R.id.listView);
        dataModels = new ArrayList<>();
        mDownloadAdapter = new DownloadAdapter(dataModels,MainActivity.this);
        listView.setAdapter(mDownloadAdapter);
        listView.setItemsCanFocus(false);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
                Downloader data = (Downloader) parent.getItemAtPosition(position);
                Log.e("PlaceholderFragment", String.valueOf(position));
                Log.e("PlaceholderFragment", String.valueOf(data.getStatus()));
                if(mDownloadManagerService.getStatus(data.getDownloadId()) == Downloader.Status.PAUSE)
                    mDownloadManagerService.startDownload(data.getDownloadId());
                else if(mDownloadManagerService.getStatus(data.getDownloadId()) == Downloader.Status.STOP)
                    mDownloadManagerService.startDownload(data.getDownloadId());
                else
                    mDownloadManagerService.stopDownload(data.getDownloadId());
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int position, long id) {
                String[] animals = {"Delete Link","Delete Link And File"};
                final Downloader data = (Downloader) adapterView.getItemAtPosition(position);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Title")
                        .setItems(animals, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0:
                                        mDownloadManagerService.removeDownload(data.getDownloadId());
                                        break;
                                    case 1:
                                        Downloader data = (Downloader) adapterView.getItemAtPosition(position);
                                        mDownloadManagerService.removeDownload(data.getDownloadId());
                                        File file = data.getFile();
                                        if (file.exists()) {
                                            if (file.delete()) {
                                                Toast.makeText(MainActivity.this,"Link and file deleted",Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(MainActivity.this,"File not deleted",Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        break;
                                }
                            }
                        })
                        .create()
                        .show();
                return true;
            }
        });
        getApplicationContext().bindService(new Intent(getApplicationContext(), DownloadManagerService.class), this,BIND_AUTO_CREATE);

        Intent intent = getIntent();
        Uri data = intent.getData();
        if(data != null){
            showAddDialog(data.toString());
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    public void showAddDialog(String url){
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.add_dialog_layout);
        dialog.setTitle("Add link for download...");
        final TextView text = (TextView) dialog.findViewById(R.id.editText_address);
        // text.setText("http://techslides.com/demos/sample-videos/small.mp4");
        //text.setText("http://ipv4.download.thinkbroadband.com/10MB.zip");
        //text.setText("http://ipv4.download.thinkbroadband.com/1GB.zip");
        //text.setText("https://httpstat.us/303");
        //text.setText("https://jigsaw.w3.org/HTTP/Digest/");
        //text.setText("http://httpbin.org/basic-auth/path/path");
        //text.setText("http://det.jrl.police.ir/backend/uploads/701726543874abcd5515189a1ec68423b27f7d28.pdf");
        //if(url == "")
        //    text.setText("http://techslides.com/demos/sample-videos/small.mp4");
        text.setText(url);
        Button dialogButton = (Button) dialog.findViewById(R.id.button_accept);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDownloadManagerService.createDownload(text.getText().toString(),false,"","");
                dialog.cancel();
            }
        });
        dialog.show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                showAddDialog("");
                break;
            case R.id.action_setting:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mDownloadManagerService = ((DownloadManagerService.MyBinder)iBinder).getService();
        mDownloadManagerService.setCallBack(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    public void onNotifyDataSetChanged() {
        final ArrayList<Downloader> values= new ArrayList<>(mDownloadManagerService.getDownloaderHashMap().values());
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDownloadAdapter.setDataSet(values);
                }
            });

    }
    @Override
    public void onNetworkDisconnected(){
        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.activity_main_ll);
        final Snackbar snackbar = Snackbar
                .make(linearLayout, "شبکه قطع می باشد", Snackbar.LENGTH_INDEFINITE)
                .setAction("دوباره", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });

        snackbar.show();
    }
    @Override
    public void onInternetDisconnected(){
        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.activity_main_ll);
        final Snackbar snackbar = Snackbar
                .make(linearLayout, "اینترنت قطع می باشد", Snackbar.LENGTH_INDEFINITE)
                .setAction("دوباره", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });

        snackbar.show();
    }
    @Override
    public void onAuthenticationRequest(final long id) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.login_layout);
        dialog.setTitle("Add link for download...");
        final EditText editText_login = (EditText)dialog.findViewById(R.id.editText_username);
        final EditText editText_password = (EditText)dialog.findViewById(R.id.editText_password);
        Button button_accept = (Button)dialog.findViewById(R.id.button_authAccept);
        button_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDownloadManagerService.authenticationAccepted(id,editText_login.getText().toString(),
                        editText_password.getText().toString() );
                dialog.cancel();
            }
        });
        dialog.show();
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
            mDialog.dismiss();
        }
    }
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
//        if (mDownloadManagerService != null) {
//            mDownloadManagerService.setCallBack(null);
//            unbindService(this);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mDownloadManagerService != null) {
//            mDownloadManagerService.setCallBack(null);
//            unbindService(this);
//        }
    }
}
