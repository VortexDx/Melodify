package com.vortico.melodify;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

public class RenamePlaylistPopUp extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rename_playlist_pop_up);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*0.5),(int)(height*0.3));
        EditText newName;
        Button renameBT;
        newName = findViewById(R.id.playlist_rename_ET);
        renameBT = findViewById(R.id.rename_playlist_BT);
        Intent intent = getIntent();
        String oldName = intent.getStringExtra("oldname");
        renameBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = newName.getText().toString();
                DatabaseHelper dbh = new DatabaseHelper(RenamePlaylistPopUp.this);
                dbh.renameTable(oldName,name);
                dbh.close();
                finish();
            }
        });
    }
}
