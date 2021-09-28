package com.vortico.melodify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.Arrays;


public class CustomAdapterSongList extends ArrayAdapter<String> {

    ArrayList<String> songs;
    Bitmap[] images;
    Bitmap icon;
    Context context;
    private songNameInterface sni;
    String tableName;
    boolean hasFocus=true;




    public CustomAdapterSongList(@NonNull Context context, int resource, @NonNull ArrayList<String> objects, String tableName) {
        super(context, resource, objects);
        this.tableName = tableName;
        this.songs = objects;
        this.context = context;
        icon = BitmapFactory.decodeResource(context.getResources(),R.drawable.nothumb);
        images = new Bitmap[songs.size()];
        Arrays.fill(images,icon);
        MediaMetadataRetriever meta = new MediaMetadataRetriever();

        new Thread (new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<songs.size();i++){
                    DatabaseHelper dbh = new DatabaseHelper(context);
                    String name;
                    if(hasFocus){
                        name = songs.get(i);
                    }
                    else{
                        return;
                    }
                    String uriString = dbh.geturi(name);

                    try{
                        Uri uri = Uri.parse(uriString);
                        meta.setDataSource(uri.getPath());
                        try {
                            byte[] art = meta.getEmbeddedPicture();
                            images[i] = BitmapFactory.decodeByteArray(art, 0, art.length);
                        }
                        catch (Exception e){
                            Log.d("thumbnail","Fail");
                        }
                    }
                    catch (Exception e){
                        Log.d("songlist thread","table dropped?");
                        return;
                    }


                }

            }
        }).start();


    }

    @Nullable
    @Override
    public String getItem(int position) {
        return songs.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        convertView = LayoutInflater.from(getContext()).inflate(R.layout.songlist_custom_adapter,parent,false);

        TextView songName = convertView.findViewById(R.id.tv_songname);
        ImageButton addToPlaylist = convertView.findViewById(R.id.add_to_playlist_IB); //////////////////
        ImageButton share = convertView.findViewById(R.id.share_IB); //////////////////////////
        ImageButton addToLiked = convertView.findViewById(R.id.add_to_liked_IB);
        ImageView thumbnail = convertView.findViewById(R.id.thumbnail_IV);


        Log.d("images:-",""+images.length);
        thumbnail.setImageBitmap(images[position]);
        long start = System.currentTimeMillis();
        long end = start+20*1000;
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                thumbnail.setImageBitmap(images[position]);
                Log.d("Refreshing","Thumbnails");
                handler.postDelayed(this,1000);
                if(System.currentTimeMillis()>end){
                    handler.removeCallbacksAndMessages(null);
                }

            }
        };
        handler.postDelayed(runnable,500);

        DatabaseHelper helper = new DatabaseHelper(context);
        if(helper.checkLiked(songs.get(position))){
            addToLiked.setImageResource(R.drawable.ic_liked);
        }
        helper.close();

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT,songs.get(position));
                intent.setType("text/plain");
                if(intent.resolveActivity(context.getPackageManager())!=null)
                    context.startActivity(intent);
            }
        });

        addToLiked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper dbh = new DatabaseHelper(context);
                if(dbh.checkLiked(songs.get(position))){
                    // dislike it
                    dbh.removeFromLiked(songs.get(position));
                    addToLiked.setImageResource(R.drawable.ic_not_liked);
                }
                else{
                    // like it
                    dbh.addToLiked(songs.get(position));
                    addToLiked.setImageResource(R.drawable.ic_liked);
                }
                dbh.close();
            }
        });


        try{
            this.sni = ((songNameInterface)context);
        }
        catch (ClassCastException e){
            throw new ClassCastException(e.getMessage());
        }

        ConstraintLayout constraintLayout = convertView.findViewById(R.id.constraintLayoutSongList);
        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send back name of song to MainActivity
                Log.d("Click","Success");
                Intent intent = new Intent();
                intent.putExtra("songname",getItem(position));
                sni.songNameInterface(intent);
                //handler.removeCallbacksAndMessages(null);
                //images = null;
            }
        });

        addToPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context,addToPlaylist);
                popupMenu.getMenuInflater().inflate(R.menu.songlist_menu,popupMenu.getMenu());
                Log.d("popup","clicked");
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getTitle().toString().equals("Create New")){
                            // create new table and add the song in that table;
                            Intent intent = new Intent(context,CreateNewPlaylistPopUp.class);
                            ConstraintLayout cl = ((Activity)context).findViewById(R.id.constraintlayout_songlist_activity);
                            cl.setAlpha(0.5f);
                            ((Activity)context).startActivityForResult(intent,5);
                        }
                        else{
                            // add to table-> item.getTitle
                            DatabaseHelper dbh = new DatabaseHelper(context);
                            String playlist = item.getTitle().toString();
                            if(dbh.addToTable(playlist,songs.get(position))){
                                Log.d("popupmenu listner","Success");
                            }
                            else{
                                Log.d("popupmenu listner","Fail");

                            }
                        }
                        return true;
                    }
                });
                DatabaseHelper dbh = new DatabaseHelper(context);
                ArrayList<String> allPlaylists = dbh.getAllPlaylistTables();
                dbh.close();
                for(int i=0;i<allPlaylists.size();i++){
                    popupMenu.getMenu().add(allPlaylists.get(i));
                }
                popupMenu.show();
            }
        });

        /// highlight current playing song
        if(MainActivity.currentSongName!=null && MainActivity.currentSongName.equals(getItem(position))){
            Log.d("customadapterSongList","song equal to current song");
            songName.setTextColor(Color.parseColor("#7CFC00"));
            songName.setTypeface(null, Typeface.BOLD);
        }
        songName.setText(getItem(position));

        return convertView;
    }
    public interface songNameInterface{
         void songNameInterface(Intent intent);

    }
}
