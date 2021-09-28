package com.vortico.melodify;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateNewPlaylistPopUp extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_new_playlist_pop_up);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*0.5),(int)(height*0.3));
        EditText playlistName;
        Button create;
        playlistName = findViewById(R.id.playlist_name_ET);
        create = findViewById(R.id.create_playlist_BT);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = playlistName.getText().toString();
                DatabaseHelper dbh = new DatabaseHelper(CreateNewPlaylistPopUp.this);
                dbh.createTable(name);
                dbh.close();
                Toast.makeText(CreateNewPlaylistPopUp.this, "Add Song now", Toast.LENGTH_SHORT).show();
                finish();

            }
        });

    }
}