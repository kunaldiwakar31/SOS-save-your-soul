package com.application.saveyoursoul;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteOpenHelper {
    public static final String CONTACT_DATABASE = "CONTACT_DATABASE";
    public static final String COLUMN_NAME = "NAME";
    public static final String COLUMN_PHONENUMBER = "PHONENUMBER";

    public Database(@Nullable Context context) {
        super(context, "contact.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String table= "CREATE TABLE " + CONTACT_DATABASE + " (" + COLUMN_NAME + " TEXT, " + COLUMN_PHONENUMBER + " TEXT)";
        sqLiteDatabase.execSQL(table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public List<String> getContactList(){
        List<String> list=new ArrayList<>();
        SQLiteDatabase db= this.getReadableDatabase();

        String query="SELECT * FROM "+CONTACT_DATABASE;
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToNext()){
            do{
                String name=cursor.getString(0);
                String phone=cursor.getString(1);

                list.add(name+" \t "+phone);
            }while(cursor.moveToNext());
        }else{
            list.add("Contact List is empty!!");
        }
        return list;
    }

    public List<String> getContacts(){
        List<String> list=new ArrayList<>();
        SQLiteDatabase db= this.getReadableDatabase();

        String query="SELECT * FROM "+CONTACT_DATABASE;
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToNext()){
            do{
                String phone=cursor.getString(1);
                list.add(phone);
            }while(cursor.moveToNext());
        }
        return list;
    }

    public boolean addOne(String name,String phone){
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv= new ContentValues();

        cv.put(COLUMN_NAME,name);
        cv.put(COLUMN_PHONENUMBER,phone);

        long insert = db.insert(CONTACT_DATABASE, null, cv);
        return insert != -1;
    }
}
