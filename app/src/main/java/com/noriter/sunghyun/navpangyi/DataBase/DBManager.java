package com.noriter.sunghyun.navpangyi.DataBase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

/**
 * Created by gugu on 2017-08-06.
 */

public class DBManager {

    private final String DB_NAME = "navpangi.db";
    private final int DB_VERSION = 1;

    private Context mContext = null;
    private SQLiteDatabase db = null;
    private OpenHelper helper = null;

    //싱글턴으로 쓰여진 코드임
    //객체가 하나만 생성되게끔
    private class OpenHelper extends SQLiteOpenHelper {
        public OpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        //생성된 db가 없을경우에 한번호출되는거임
        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL("create table information(phone varchar(20), name varchar(10));");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }

    public DBManager(Context context) {
        this.mContext = context;
        this.helper = new OpenHelper(mContext, DB_NAME, null, DB_VERSION);
    }

    public void dbOpen() {
        this.db = helper.getWritableDatabase();
    }

    public void dbClose() {
        this.db.close();
    }

    //sql받아서 insert하는 메소드
    public void insertDB(String sql) {
        this.db.execSQL(sql);
    }

    //sql받아서 select하는 메소드
    public Bundle selectDB(String sql) {
        Cursor cursor = this.db.rawQuery(sql, null);
        //sqlite에서 행(raw)를 참조하는 클래스 (Cursor)
        Bundle save = new Bundle();

        while(cursor.moveToNext()) {
            String phone = cursor.getString(cursor.getColumnIndex("phone"));
            String name = cursor.getString(cursor.getColumnIndex("name"));

            save.putString("phone", phone);
            save.putString("name", name);
        }

        return save;
    }

}
