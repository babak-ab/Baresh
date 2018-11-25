package com.example.babak.baresh;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Baresh.db";
    public static final String LINKS_TABLE_NAME = "links";
    public static final String LINKS_COLUMN_ID = "id";
    public static final String LINKS_COLUMN_NAME = "name";
    public static final String LINKS_COLUMN_URL = "url";
    public static final String LINKS_COLUMN_DURATION = "duration";
    //public static final String LINKS_COLUMN_FILE = "file";
    public static final String LINKS_COLUMN_SIZE = "size";
    public static final String LINKS_COLUMN_DOWNLOADED = "downloaded";
    public static final String LINKS_COLUMN_PARTIAL = "partial";
    public static final String TASKS_TABLE_NAME = "tasks";
    public static final String TASKS_COLUMN_ID = "id";
    public static final String TASKS_COLUMN_DOWNLOAD_FK = "downloadId_fk";
    public static final String TASKS_COLUMN_START = "startByte";
    public static final String TASKS_COLUMN_END = "endByte";

    private DownloadManagerService mDownloadManager;
    public DBHelper(Context context,DownloadManagerService downloadManager) {
        super(context, DATABASE_NAME , null, 1);
        mDownloadManager = downloadManager;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table links " +
                        "(id integer primary key AUTOINCREMENT, name text," +
                        "size integer,type integer,downloaded integer," +
                        "url text UNIQUE,file text,duration integer)");

        db.execSQL("create table tasks " +
                        "(id integer primary key AUTOINCREMENT," +
                        " downloadId_fk integer," +
                        " startByte integer,endByte integer," +
                "FOREIGN KEY(downloadId_fk) REFERENCES links(id))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS links");
        db.execSQL("DROP TABLE IF EXISTS tasks");
        onCreate(db);
    }

    public long insertLink (String name, String address,long size,long downloaded) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LINKS_COLUMN_NAME, name);
        contentValues.put(LINKS_COLUMN_URL, address);
        contentValues.put(LINKS_COLUMN_SIZE, size);
        contentValues.put(LINKS_COLUMN_DOWNLOADED, downloaded);
        long id = db.insert("links", null, contentValues);
        return id;
    }
    public long insertTask (Long downloadId, long start, long end) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TASKS_COLUMN_DOWNLOAD_FK, downloadId);
        contentValues.put(TASKS_COLUMN_START, start);
        contentValues.put(TASKS_COLUMN_END, end);
        long id = db.insert("tasks", null, contentValues);
        return id;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from links where id="+id+"", null );
        return res;
    }

//    public int numberOfRows(){
//        SQLiteDatabase db = this.getReadableDatabase();
//        int numRows = (int) DatabaseUtils.queryNumEntries(db, CONTACTS_TABLE_NAME);
//        return numRows;
//    }

    public boolean updateSizeLink(Long id, Long size) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LINKS_COLUMN_SIZE, size);
        db.update("links", contentValues, "id = ? ", new String[] { Long.toString(id) } );
        return true;
    }
    public boolean updateDownloadedLink(Long id, Long downloaded,Long duration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LINKS_COLUMN_DOWNLOADED, downloaded);
        contentValues.put(LINKS_COLUMN_DURATION, duration);
        db.update("links", contentValues, "id = ? ", new String[] { Long.toString(id) } );
        return true;
    }
    public boolean updateLink(Long id, String name, String address,String fileSize) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LINKS_COLUMN_NAME, name);
        contentValues.put(LINKS_COLUMN_URL, address);
        contentValues.put(LINKS_COLUMN_SIZE, fileSize);
        //contentValues.put(LINKS_COLUMN_FILE, path);
        db.update("links", contentValues, "id = ? ", new String[] { Long.toString(id) } );
        return true;
    }
    public boolean updateTask (Long id,  Long start, Long end) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TASKS_COLUMN_ID, id);
        contentValues.put(TASKS_COLUMN_START, start);
        contentValues.put(TASKS_COLUMN_END, end);
        db.update("tasks", contentValues, "id = ? ", new String[] { Long.toString(id) } );
        return true;
    }

    public Integer deleteLink (Long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("links",
                "id = ? ",
                new String[] {String.valueOf(id)});
    }
    public Integer deleteTask (Long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("tasks",
                "id = ? ",
                new String[] {String.valueOf(id)});
    }
    public HashMap<Long,Downloader> getAllLinks() {
        HashMap<Long,Downloader> array_list = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from links", null );
        res.moveToFirst();
        while(res.isAfterLast() == false){
            String name = res.getString(res.getColumnIndex(LINKS_COLUMN_NAME));
            String id = res.getString(res.getColumnIndex(LINKS_COLUMN_ID));
            String url = res.getString(res.getColumnIndex(LINKS_COLUMN_URL));
            String size = res.getString(res.getColumnIndex(LINKS_COLUMN_SIZE));
            String downloaded = res.getString(res.getColumnIndex(LINKS_COLUMN_DOWNLOADED));
            String duration = res.getString(res.getColumnIndex(LINKS_COLUMN_DURATION));
            Downloader downloader = new Downloader(Long.parseLong(id),url,name,mDownloadManager);
            downloader.setDownloadTask(getAllTasks(downloader.getDownloadId()));
            downloader.setFileSize(Long.parseLong(size));
            downloader.setDownloaded(Long.parseLong(downloaded));
            downloader.setDurationTime(Long.parseLong(duration));
            array_list.put(Long.parseLong(id),downloader);
            res.moveToNext();
        }
        return array_list;
    }
    public HashMap<Long,TaskModel> getAllTasks(long downloadId) {
        HashMap<Long,TaskModel> array_list = new HashMap<>(4);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from tasks where downloadId_fk = "+downloadId+"", null );
        res.moveToFirst();
        while(res.isAfterLast() == false){
            String taskId = res.getString(res.getColumnIndex(TASKS_COLUMN_ID));
            String start = res.getString(res.getColumnIndex(TASKS_COLUMN_START));
            String end = res.getString(res.getColumnIndex(TASKS_COLUMN_END));
            TaskModel task = new TaskModel();
            task.setTaskId(Long.valueOf(taskId));
            task.setDownloadId(downloadId);
            task.setStart(Long.valueOf(start));
            task.setEnd(Long.valueOf(end));
            array_list.put(Long.valueOf(taskId),task);
            res.moveToNext();
        }
        return array_list;
    }

}
