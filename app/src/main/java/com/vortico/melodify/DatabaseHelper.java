package com.vortico.melodify;



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(@Nullable Context context) {
        super(context, "songsList.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE `All Songs` (sno INTEGER PRIMARY KEY AUTOINCREMENT, songname TEXT, songuri TEXT, liked BIT)";
        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public ArrayList<String> dropTableAndRefresh(String table_name){
        String query = "DROP TABLE `"+table_name+"`";
        String queryCreate = "CREATE TABLE `"+table_name+"` (sno INTEGER PRIMARY KEY AUTOINCREMENT, songname TEXT, songuri TEXT, liked BIT)";

        ArrayList<String> likedSongs = getAllLiked();

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.execSQL(query);
            Log.d("Drop Table", "Success");
        }
        catch (Exception e){
            Log.d("Drop Table", "Fail");
        }
        db.execSQL(queryCreate);
        db.close();
        return likedSongs;
    }

    public boolean add(DatabaseBlueprint song,int liked){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("songname",song.getSongName());
        cv.put("songuri",song.getSongUri());
        cv.put("liked",liked);

        String query = "SELECT * FROM `All Songs` WHERE songname IS \"" + song.getSongName()+ "\"";
        Cursor cursor = db.rawQuery(query,null);
        if(cursor.getCount()>0){
            Log.d("Database","already present");
            return false;
        }
        else {
            long insert = db.insert("`All Songs`", null, cv);
            db.close();
            cv.clear();
            if (insert < 0) {
                Log.d("Database", "Error in insertion");
                return false;
            } else {
                Log.d("Database", "Success in insertion");
                return true;
            }
        }
    }

    public void createTable(String tableName){
        String query = "CREATE TABLE IF NOT EXISTS`"+tableName+"` (sno INTEGER PRIMARY KEY AUTOINCREMENT,songname TEXT, songuri TEXT)";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void dropTable(String tableName){
        String query = "DROP TABLE `"+tableName+"`";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public boolean addToTable(String tableName, String songName){
        SQLiteDatabase db = this.getWritableDatabase();
        String uriString;
        String query2 = "SELECT songuri FROM `All Songs` WHERE songname IS \""+songName+"\"";
        Cursor cursor = db.rawQuery(query2,null);
        if(cursor.moveToFirst()){
            uriString = cursor.getString(0);
        }
        else{
            Log.d("addToTable Cursor", "Empty, Insertion Fail");
            cursor.close();
            db.close();
            return false;
        }
        // Check if already present......................................................... do with changing icon
        String query1 = "SELECT songname FROM `"+tableName+"` WHERE songname IS \""+songName+"\"";
        Cursor cursor1 = db.rawQuery(query1,null);
        if(cursor1.moveToFirst()){
            db.close();
            Log.d("add to table","fail already present");
            return false;
        }
        // check liked column of all songs for heart icon change

        ContentValues cv = new ContentValues();
        cv.put("songname",songName);
        cv.put("songuri",uriString);
        long insert = db.insert("`"+tableName+"`", null, cv);
        if(insert<0){
            Log.d("addToTable","Fail");
            cursor.close();
            db.close();
            return false;
        }
        else{
            Log.d("addToTable","Success");
            cursor.close();
            db.close();
            return true;
        }

    }


    public String geturi(String songName){
        String result = null;
        String query = "SELECT * From `All Songs` WHERE songname IS \""+songName+ "\"";
        SQLiteDatabase db =this.getReadableDatabase();
        Log.d("Query next","previous");
        Cursor cursor = db.rawQuery(query,null);
        Log.d("Query","next");
        if(cursor.moveToFirst()){
            result = cursor.getString(2);
            return result;
        }
        else{
            Log.d("Query","Fail");
        }
        cursor.close();
        db.close();
        return "";


    }
    public ArrayList<String> getAllPlaylistTables(){
        String query = "SELECT name FROM sqlite_master WHERE type IS \'table\' AND name!='android_metadata' AND name!='sqlite_sequence'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        ArrayList<String> result = new ArrayList<>();
        if(cursor.moveToFirst()){
            do{
                if(cursor.getString(0).equals("All Songs") || cursor.getString(0).equals("Liked Songs")){

                }
                else {
                    result.add(cursor.getString(0));
                }


            }   while(cursor.moveToNext());
            Log.d("total playlists = ",""+result.size());
        }
        else{
            Log.d("no of playlist tables","0");
        }
        db.close();
        cursor.close();
        Collections.sort(result);
        return result;
    }
    public boolean checkLiked(String songName){
        String query = "SELECT liked FROM `All Songs` WHERE songname IS \""+songName+"\"";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        if(cursor.moveToFirst()){
            if(cursor.getInt(0)==1){
                db.close();
                cursor.close();
                return true;
            }
            else{
                db.close();
                cursor.close();
                return false;
            }
        }
        else{
            Log.d("checkLiked","Error getting liked");
            return false;
        }
    }
    public void addToLiked(String songName){
        String query = "UPDATE `All Songs` SET liked = 1 WHERE songname IS \""+songName+"\"";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();

    }
    public void removeFromLiked(String songName){
        String query = "UPDATE `All Songs` SET liked = 0 WHERE songname IS \""+songName+"\"";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();

    }
    public ArrayList<String> getAllLiked(){
        ArrayList<String> result = new ArrayList<>();
        String query = "SELECT songname FROM `All Songs` WHERE liked = 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        if(cursor.moveToFirst()){
            do{
                result.add(cursor.getString(0));

            }   while(cursor.moveToNext());
        }
        else{
            Log.d("getAllLiked","Empty cursor");
        }
        db.close();
        cursor.close();
        Collections.sort(result);
        return result;
    }
    public ArrayList<String> getSongsInPlaylist(String tableName, Boolean likedPlaylist){
        ArrayList<String> result = new ArrayList<>();
        String query;
        if(likedPlaylist) {
            query = "SELECT songname FROM `"+tableName+"` WHERE liked = 1";
        }
        else{
            query = "SELECT songname FROM `"+tableName+"`";
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        if(cursor.moveToFirst()){
            do{
                result.add(cursor.getString(0));
            }   while(cursor.moveToNext());

        }
        else{
            Log.d("getSongsInPlaylist","cursor size 0");
        }
        cursor.close();
        db.close();
        Collections.sort(result);
        return result;
    }
    public void renameTable(String tableName, String newName){
        String query = "ALTER TABLE `"+tableName+"`RENAME TO `"+newName+"`";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }
}
