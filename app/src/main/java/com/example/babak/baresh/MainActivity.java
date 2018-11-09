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
import android.os.Build;
import android.os.IBinder;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements
        DownloadInfoDialogListener,HttpDownloadListener,ServiceConnection, DownloadManagerService.CallBack {

    private Context mContext;
    private PopupWindow mPopupWindow;
    private LinearLayout mLinearLayout;
    private Button button;
    private Downloader download;
    private DownloadAdapter mDownloadAdapter;
    private HttpAsyncTask mHeadAsyncTask;
    private ArrayList<Downloader> dataModels;
    private DownloadManagerService mDownloadManagerService;
    private DownloadInfoDialog mInfoDialog;
    private boolean mHeadFinished;
    private boolean mDownloadAccepted;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        mContext = MainActivity.this;
        mLinearLayout = (LinearLayout) findViewById(R.id.activity_main_ll);
        isStoragePermissionGranted();
        ListView listView = (ListView) findViewById(R.id.listView);
        dataModels = new ArrayList<>();
        mHeadFinished = false;
        mDownloadAccepted = false;
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
                //Log.e("PlaceholderFragment","HOLD");
                String[] animals = {"Delete Link","Delete Link And File"};
                final Downloader data = (Downloader) adapterView.getItemAtPosition(position);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Title")
                        .setItems(animals, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0: // horse
                                        //mDownloadManager.removeDownload(data.getDownloadId());
                                        mDownloadManagerService.removeDownload(data.getDownloadId());
                                        break;
                                    case 1:
                                        Downloader data = (Downloader) adapterView.getItemAtPosition(position);
                                        //mDownloadManager.removeDownload(data.getDownloadId());
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
                // IF false -> call onItemClick in Item Click Release
                return true;
            }
        });

        Intent startIntent = new Intent(getApplicationContext(), DownloadManagerService.class);
        bindService(startIntent, this, BIND_AUTO_CREATE);
       // startIntent.setAction("START");
        //startService(startIntent);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.add_dialog_layout);
                dialog.setTitle("Add link for download...");
                final TextView text = (TextView) dialog.findViewById(R.id.editText_address);
               // text.setText("http://techslides.com/demos/sample-videos/small.mp4");
                text.setText("http://ipv4.download.thinkbroadband.com/10MB.zip");
                //text.setText("http://ipv4.download.thinkbroadband.com/1GB.zip");
                //text.setText("https://speed.hetzner.de/10GB.bin");
                //https://speed.hetzner.de/10GB.bin
                //text.setText("http://dl.hastidl.me/data/Friends.S01.E03.Hastidl.mkv");
                //text.setText("https://host2.rjmusicmedia.com/media/podcast/mp3-192/Abo-Atash-109.mp3");
                //text.setText("http://ftp2.nluug.nl/languages/qt/official_releases/qt-installer-framework/3.0.4/QtInstallerFramework-win-x86.exe")   ;
                Button dialogButton = (Button) dialog.findViewById(R.id.button_accept);
                // if button is clicked, close the custom dialog
                //dialogButton.setOnClickListener(new AddDialogButtonClicked(dialog,(String)text.getText().toString()));
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        mHeadFinished = false;
                        mDownloadAccepted = false;
                        mHeadAsyncTask = new HttpAsyncTask(MainActivity.this);
                        mHeadAsyncTask.execute((String)text.getText().toString());
                        mInfoDialog = new DownloadInfoDialog(mContext,(String)text.getText().toString());
                        mInfoDialog.setListener(MainActivity.this);
                        mInfoDialog.show();
                        dialog.cancel();
                    }
                });
                dialog.show();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onDownloadAccepted() {
        //createDownload();
        mDownloadAccepted = true;
        mInfoDialog.cancel();
        if(mHeadFinished && mDownloadAccepted) {
            mHeadFinished = false;
            mDownloadAccepted = false;
            mDownloadManagerService.createDownload(mHeadAsyncTask.getUrl(),mHeadAsyncTask.getFileName(),
                    mHeadAsyncTask.getFileSize());
        }
    }

    @Override
    public void onDownloadReject() {
        mInfoDialog.cancel();
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
    public void onHeadFinished() {
        //createDownload();
        mHeadFinished = true;
        if(mInfoDialog != null){
            if(mInfoDialog.isShowing()){
                mInfoDialog.setFileSizeChanged(mHeadAsyncTask.getFileSize());
            }
        }
        if(mHeadFinished && mDownloadAccepted) {
            mHeadFinished = false;
            mDownloadAccepted = false;
            mDownloadManagerService.createDownload(mHeadAsyncTask.getUrl(),mHeadAsyncTask.getFileName(),
                    mHeadAsyncTask.getFileSize());
        }
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
           // boolean result = mDownloadManagerService.createDownload(this.mText);
            //if(result == false)
            //   Toast.makeText(MainActivity.this,"Link is duplicate",Toast.LENGTH_LONG).show();
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
        if (mDownloadManagerService != null) {
            mDownloadManagerService.setCallBack(null);
            unbindService(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDownloadManagerService != null) {
            mDownloadManagerService.setCallBack(null);
            unbindService(this);
        }
    }
}
