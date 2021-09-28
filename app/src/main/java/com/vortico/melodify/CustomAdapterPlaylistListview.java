package com.vortico.melodify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;


public class CustomAdapterPlaylistListview extends ArrayAdapter {

    ArrayList<String> tableNames = new ArrayList<>();
    Context context;

    public CustomAdapterPlaylistListview(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {

        super(context, resource, objects);
        tableNames = objects;
        this.context = context;
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return tableNames.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.playlist_tile,parent,false);
        // perform actions
        TextView textView = convertView.findViewById(R.id.playlistname);
        textView.setText(tableNames.get(position));
        ImageButton menuBT = convertView.findViewById(R.id.playlist_menuBT);
        ConstraintLayout playlistTile = convertView.findViewById(R.id.playlist_tile_constraintLayout);
        playlistTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,SongListActivity.class);
                intent.putExtra("playlistName",tableNames.get(position));
                intent.putExtra("resultcode",3);
                //context.startActivityForResult(intent,MainActivity.playlistAllSongsReqCode);
                ((PlaylistActivity)context).startActivityForResult(intent,3);
            }
        });
        menuBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu(menuBT,position);
            }
        });

        return convertView;
    }

    private void showMenu(ImageButton menuBT,int position){
        PopupMenu popupMenu = new PopupMenu(context,menuBT);
        popupMenu.getMenuInflater().inflate(R.menu.playlist_menu,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String action = item.getTitle().toString();
                if(action.equals("Delete")){
                    // drop table with name of playlist clicked
                    DatabaseHelper dbh = new DatabaseHelper(context);
                    dbh.dropTable(tableNames.get(position));
                    dbh.close();

                    ////////////////////@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
                    //call getPlaylists fn
                    ((PlaylistActivity)context).getPlaylists();


                }
                else if(action.equals("Rename")){
                    ConstraintLayout cl = ((Activity)context).findViewById(R.id.constraintlayout_playlist_activity);
                    cl.setAlpha(0.5f);
                    Intent intent = new Intent(context,RenamePlaylistPopUp.class);
                    intent.putExtra("oldname",tableNames.get(position));
                    ((Activity)context).startActivityForResult(intent,4);
                }
                return true;
            }
        });
        popupMenu.show();
    }
}
