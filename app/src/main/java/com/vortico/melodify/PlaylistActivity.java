package com.vortico.melodify;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

public class PlaylistActivity extends AppCompatActivity {
    ListView listView;
    ConstraintLayout allSongs, likedSongs, cl;
    ArrayList<String> playlistTables = new ArrayList<>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Playlist ActivityResult",""+requestCode+" "+resultCode);
        setResult(resultCode,data);
        if(resultCode==MainActivity.playlistAllSongsReqCode){
            Log.d("Playlist ActivityResult","All songs");
            finish();
        }
        else if(resultCode==MainActivity.playlistLikedSongsReqCode){
            Log.d("Playlist ActivityResult","Liked songs");
            finish();
        }
        else if(resultCode==3){
            Log.d("Playlist ActivityResult","Custom Playlist");
            finish();
        }

        else{
            Log.d("Playlist ActivityResult","Returned without song");
            ////////@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
            getPlaylists();
            cl.setAlpha(1f);


        }

    }

    void getPlaylists(){
        DatabaseHelper dbh = new DatabaseHelper(this);
        playlistTables= dbh.getAllPlaylistTables();
        dbh.close();
        CustomAdapterPlaylistListview ad = new CustomAdapterPlaylistListview(this,R.layout.playlist_tile,playlistTables);
        listView.setAdapter(ad);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        listView = findViewById(R.id.playlist_listview);
        allSongs = findViewById(R.id.cl_allSongs);
        likedSongs = findViewById(R.id.cl_likedSongs);
        cl = findViewById(R.id.constraintlayout_playlist_activity);

        getPlaylists();


        allSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlaylistActivity.this,SongListActivity.class);
                intent.putExtra("playlistName","All Songs");
                intent.putExtra("resultcode",MainActivity.playlistAllSongsReqCode);
                startActivityForResult(intent,MainActivity.playlistAllSongsReqCode);
            }
        });

        likedSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlaylistActivity.this,SongListActivity.class);
                intent.putExtra("playlistName","All Songs");
                intent.putExtra("resultcode",MainActivity.playlistLikedSongsReqCode);
                startActivityForResult(intent,MainActivity.playlistLikedSongsReqCode);
            }
        });
    }
}