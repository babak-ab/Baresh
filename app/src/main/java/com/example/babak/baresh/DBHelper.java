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
    public static final String CONTACTS_TABLE_NAME = "links";
    public static final String CONTACTS_COLUMN_ID = "id";
    public static final String CONTACTS_COLUMN_NAME = "name";
    public static final String CONTACTS_COLUMN_URL = "url";
    public static final String CONTACTS_COLUMN_PATH = "path";
    public static final String CONTACTS_COLUMN_RESUME = "resume";
    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table links " +
                        "(id integer primary key AUTOINCREMENT, name text,url text UNIQUE,path text,resume bool)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS links");
        onCreate(db);
    }

    public long insertLink (String name, String address, String path, Boolean resume) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTACTS_COLUMN_NAME, name);
        contentValues.put(CONTACTS_COLUMN_URL, address);
        contentValues.put(CONTACTS_COLUMN_PATH, path);
        contentValues.put(CONTACTS_COLUMN_RESUME, String.valueOf(resume));
        long id = db.insert("links", null, contentValues);
        return id;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from links where id="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, CONTACTS_TABLE_NAME);
        return numRows;
    }

    public boolean updateContact (Long id, String name, String address, String path, boolean resume) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTACTS_COLUMN_NAME, name);
        contentValues.put(CONTACTS_COLUMN_URL, address);
        contentValues.put(CONTACTS_COLUMN_PATH, path);
        contentValues.put(CONTACTS_COLUMN_RESUME, resume);
        db.update("links", contentValues, "id = ? ", new String[] { Long.toString(id) } );
        return true;
    }

    public Integer deleteContact (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("links",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<DownloadModel> getAllLinks() {
        ArrayList<DownloadModel> array_list = new ArrayList<DownloadModel>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from links", null );
        res.moveToFirst();
        while(res.isAfterLast() == false){
            String name = res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME));
            String id = res.getString(res.getColumnIndex(CONTACTS_COLUMN_ID));
            String path = res.getString(res.getColumnIndex(CONTACTS_COLUMN_ID));
            String url = res.getString(res.getColumnIndex(CONTACTS_COLUMN_URL));
            DownloadModel model = new DownloadModel();
            model.setDownloadId(Long.parseLong(id));
            model.setUrl(url);
            model.setName(name);
            array_list.add(model);
            res.moveToNext();
        }

        return array_list;
    }
}
