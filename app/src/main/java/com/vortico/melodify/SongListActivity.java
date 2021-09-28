package com.vortico.melodify;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class SongListActivity extends AppCompatActivity implements CustomAdapterSongList.songNameInterface {
    int resultcode;
    String tableName;
    CustomAdapterSongList ad;
    ConstraintLayout cl;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        cl.setAlpha(1f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);


        ListView lv;
        ImageButton playBT;
        ImageButton shuffleBT;
        AutoCompleteTextView search;
        ImageView playlist_thumb;
        lv = findViewById(R.id.songsList_LV);
        playBT = findViewById(R.id.songlist_play);
        shuffleBT = findViewById(R.id.songlist_shuffle);
        search = findViewById(R.id.autoCompleteTextView);
        playlist_thumb = findViewById(R.id.playlist_thumb_open_IV);
        cl = findViewById(R.id.constraintlayout_songlist_activity);


        Intent intent = getIntent();
        tableName = intent.getStringExtra("playlistName");
        resultcode = intent.getIntExtra("resultcode",-99);
        ArrayList<String> songs;
        DatabaseHelper dbh = new DatabaseHelper(this);
        if(resultcode==MainActivity.playlistAllSongsReqCode) {
            songs = dbh.getSongsInPlaylist(tableName,false);
            //Collections.sort(songs);
            Log.d("get all frm table", "" + songs.size());
            ad = new CustomAdapterSongList(this, R.layout.songlist_custom_adapter, songs, tableName);
            lv.setAdapter(ad);
        }
        else if(resultcode==MainActivity.playlistLikedSongsReqCode){
            songs = dbh.getAllLiked();
            //Collections.sort(songs);
            Log.d("get all frm table", "" + songs.size());
            ad = new CustomAdapterSongList(this, R.layout.songlist_custom_adapter, songs, tableName);
            lv.setAdapter(ad);
        }
        else{
            songs = dbh.getSongsInPlaylist(tableName,false);
            Log.d("get all frm playlist",""+songs.size());
            ad = new CustomAdapterSongList(this,R.layout.songlist_custom_adapter,songs,tableName);
            lv.setAdapter(ad);
        }
        dbh.close();

        // set thumb of playlist
        if(songs!=null && songs.size()>0) {
            MediaMetadataRetriever mdr = new MediaMetadataRetriever();
            String uriString = dbh.geturi(songs.get(0));
            Uri uri = Uri.parse(uriString);
            mdr.setDataSource(uri.getPath());
            try {
                byte[] art = mdr.getEmbeddedPicture();
                Bitmap image = BitmapFactory.decodeByteArray(art, 0, art.length);
                playlist_thumb.setImageBitmap(image);
            } catch (Exception e) {
                Log.d("SongListActivity", "No thumb");
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,songs);
        search.setThreshold(2);
        search.setAdapter(adapter);
        search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String song = parent.getItemAtPosition(position).toString();
                Intent backIntent = new Intent();
                backIntent.putExtra("songname",song);
                songNameInterface(backIntent);
            }
        });

        playBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent backIntent = new Intent();
                backIntent.putExtra("songname",songs.get(0));
                songNameInterface(backIntent);
            }
        });
        shuffleBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent();
                backIntent.putExtra("songname",songs.get(0));
                backIntent.putExtra("shuffle",true);
                songNameInterface(backIntent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(ad==null){
            Log.d("adapter","nullllllllllllllll");
        }
        ad.hasFocus=false;
        finish();
    }

    @Override
    public void songNameInterface(Intent intent) {
        //intent from custom adapter
        intent.putExtra("tablename",tableName);
        setResult(resultcode,intent);
        finish();
    }
}