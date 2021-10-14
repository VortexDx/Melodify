package com.vortico.melodify;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.gauravk.audiovisualizer.visualizer.CircleLineVisualizer;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    public static final String intentKey = "com.vortico.melodify.intentKey.toSongList";
    public ArrayList<File> allSongs= new ArrayList<>();
    //public boolean firstLaunch=true;
    public boolean songAssigned=false;
    public static final int playlistAllSongsReqCode = 1000;
    public static final int playlistLikedSongsReqCode = 2000;
    ArrayList<String> permissions = new ArrayList<>();
    public static MediaPlayer mp = new MediaPlayer();
    MediaMetadataRetriever metadataRetriever;
    public static String currentSongName = null;
    String currentTable=null;
    ArrayList<String> songsInPlaylist;
    int currentSongIndex;
    Handler handler = new Handler();
    boolean isShuffle = false;
    int repeatState = 0;
    int dominantColor;
    boolean flag_get_sd_card_directory= false;      // //////////////////////////////  use shared preference and req only once
    public static final int sd_card_directory_reqCode = 9;
    File sdCard = null;
    Boolean hasFocus=false;
    AudioManager audioManager;
    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int i) {
            if(i == AudioManager.AUDIOFOCUS_LOSS || i == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ){

                if(mp!=null && mp.isPlaying()){
                    playBT.performClick();
                }
                hasFocus =false;

            }
            if(i == AudioManager.AUDIOFOCUS_GAIN){
                Log.d("call back:","has focus");
                hasFocus =true;
                if(mp!=null && !mp.isPlaying()){
                    playBT.performClick();
                }
            }

        }
    };
//    private static final String TAG =MainActivity.class.getSimpleName();
//    MediaSessionCompat mMediaSession;
//    PlaybackStateCompat.Builder mStateBuilder;



    SeekBar seekBar;
    ImageButton previousBT;
    ImageButton playBT;
    ImageButton nextBT;
    ImageButton songList;
    ImageButton refreshBT;
    ImageButton likedBT;
    ImageButton shuffle;
    ImageButton repeat;
    ConstraintLayout parent;
    TextView textView1;
    TextView textView2;
    TextView tv_songName;
    CircularImageView circularImageView;
    CircleLineVisualizer visualizer;
    ImageButton notification_next, notification_previous, notification_playpause, notification_liked;
    SeekBar notification_seekbar;
    ImageView notification_thumb;
    TextView notification_songname, notification_currenttime, notification_fulltime;


    public static void disableButton(ImageButton button){
        button.setEnabled(false);
    }
    public static void enableButton(ImageButton button){
        button.setEnabled(true);
    }



    public void fetchSongs(File file){

        Log.d("where>",file.getPath());
        //Log.d("is directory?",""+file.isDirectory());
        File[] songs = file.listFiles();

        if(songs != null) {
            Log.d("sizeeeee", "" + songs.length);
            for (int i = 0; i < songs.length; i++) {
                File myFile = songs[i];
                Log.d(myFile.getName(), "" + myFile.isDirectory() + myFile.isHidden());
                if (!myFile.isHidden() && myFile.isDirectory()) {
                    fetchSongs(myFile);
                } else {
                    if (myFile.getName().endsWith(".mp3") && !myFile.getName().startsWith(".")) {
                        Log.d("error statement", myFile.getName());
                        allSongs.add(myFile);

                    }
                }
            }
        }
    }

    void playSong(int index){
        Uri uri=null;
        if(mp.isPlaying()){
            mp.stop();
            mp.release();
            songAssigned=false;
            if(visualizer!=null){
                visualizer.release();
            }
            //audioManager.abandonAudioFocusRequest(afChangeListener);

        }
        mp = new MediaPlayer();

        try {
            DatabaseHelper dbh = new DatabaseHelper(MainActivity.this);
            String uriString = dbh.geturi(songsInPlaylist.get(index));
            uri = Uri.parse(uriString);
            mp.setDataSource(this,uri);
//            File temp = new File(uri.getPath());
            currentSongName = songsInPlaylist.get(index);
            if(dbh.checkLiked(songsInPlaylist.get(index))){
                likedBT.setImageResource(R.drawable.ic_liked);
                //notification_liked.setImageResource(R.drawable.ic_liked);
            }
            else{
                likedBT.setImageResource(R.drawable.ic_not_liked);
                //notification_liked.setImageResource(R.drawable.ic_not_liked);
            }
            tv_songName.setText(currentSongName);
            songAssigned=true;
        } catch (IOException e) {
            Log.d("hola","set datasource error");
            e.printStackTrace();
        }



        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Toast.makeText(MainActivity.this, songsInPlaylist.get(index), Toast.LENGTH_SHORT).show();
                seekBar.setMax(mp.getDuration());///
                int milisec = mp.getDuration();
                int minutes,seconds;
                minutes = milisec/(60*1000);
                seconds = milisec%(60*1000);
                String temp = ""+seconds;
                if(temp.length()==4){
                    temp = '0'+temp;
                }
                else if(temp.length()<4){
                    temp = "00"+temp;
                }
                String time= ""+minutes+":"+temp.charAt(0)+temp.charAt(1);
                textView2.setText(time);
                if(!hasFocus){
                    // request audio focus
                    if(audioManager.requestAudioFocus(afChangeListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                        hasFocus=true;
                    }

                }
                if(hasFocus)
                    mp.start();
//                mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,mp.getCurrentPosition(),1f);
            }
        });
        mp.prepareAsync();

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nextBT.performClick();
            }
        });

        // Set thumbnail
        metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(uri.getPath());
        try{
            byte[] art = metadataRetriever.getEmbeddedPicture();
            Bitmap songImage = BitmapFactory.decodeByteArray(art,0,art.length);
            circularImageView.setImageBitmap(songImage);
            //notification_thumb.setImageBitmap(songImage);
            ///////////////////////////////////////////////////////
            dominantColor = songImage.getPixel(0,0);
            int[] colors = new int[3];
            colors[0]= Color.parseColor("#162A5A");
            colors[1]=Color.parseColor("#8982EE");
            colors[2]=dominantColor;
            GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,colors);
            gd.setGradientType(GradientDrawable.LINEAR_GRADIENT);
            parent.setBackground(gd);
            //////////////////////////////////////////////////////
            circularImageView.setBorderColor(dominantColor);

        }
        catch(Exception e){
            Log.d("thumbnail","fail");
            //thumbnail.setImageResource(R.drawable.nothumb);
            circularImageView.setImageResource(R.drawable.nothumb);
            //notification_thumb.setImageResource(R.drawable.nothumb);
        }
        // set visualizer
        int audioSessionId = mp.getAudioSessionId();
        Log.d("audiosessionID",""+audioSessionId);
        if(audioSessionId != -1){
            visualizer.setAudioSessionId(mp.getAudioSessionId());
            visualizer.setColor(dominantColor);

        }


    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MainActivity resultcode",""+requestCode+" "+resultCode);
        if(resultCode == MainActivity.playlistAllSongsReqCode){
            String tableName = data.getStringExtra("tablename");
            currentTable = tableName;
            String songName = data.getStringExtra("songname");
            Boolean booleanExtra = data.getBooleanExtra("shuffle",false);
            Log.d("result",tableName+" "+songName);
            DatabaseHelper dbh = new DatabaseHelper(this);
            songsInPlaylist = dbh.getSongsInPlaylist(tableName,false);
            dbh.close();

            ////////////////////////////////////////////////////////
            isShuffle = false;
            shuffle.setImageResource(R.drawable.ic_shuffle);
            if(booleanExtra){

                shuffle.performClick();
            }
            currentSongIndex = songsInPlaylist.indexOf(songName);
            playSong(currentSongIndex);
            playBT.setImageResource(R.drawable.ic_pause_circle);
            //notification_playpause.setImageResource(android.R.drawable.ic_media_pause);
        }
        else if(resultCode == MainActivity.playlistLikedSongsReqCode){
            String tableName = data.getStringExtra("tablename");
            currentTable = tableName;
            String songName = data.getStringExtra("songname");
            Boolean booleanExtra = data.getBooleanExtra("shuffle",false);
            Log.d("result",tableName+" "+songName);
            DatabaseHelper dbh = new DatabaseHelper(this);
            songsInPlaylist = dbh.getSongsInPlaylist(tableName,true);
            dbh.close();
            isShuffle = false;
            shuffle.setImageResource(R.drawable.ic_shuffle);
            ////////////////////////////////////////////////////////
            if(booleanExtra){

                shuffle.performClick();
            }
            currentSongIndex = songsInPlaylist.indexOf(songName);
            playSong(currentSongIndex);
            playBT.setImageResource(R.drawable.ic_pause_circle);
            //notification_playpause.setImageResource(android.R.drawable.ic_media_pause);

        }
        else if(resultCode==3){
            String tableName = data.getStringExtra("tablename");
            currentTable = tableName;
            String songName = data.getStringExtra("songname");
            Boolean booleanExtra = data.getBooleanExtra("shuffle",false);
            Log.d("result",tableName+" "+songName);
            DatabaseHelper dbh = new DatabaseHelper(this);
            songsInPlaylist = dbh.getSongsInPlaylist(tableName,false);
            dbh.close();
            isShuffle = false;
            shuffle.setImageResource(R.drawable.ic_shuffle);
            ////////////////////////////////////////////////////////
            if(booleanExtra){

                shuffle.performClick();
            }
            currentSongIndex = songsInPlaylist.indexOf(songName);
            playSong(currentSongIndex);
            playBT.setImageResource(R.drawable.ic_pause_circle);
            //notification_playpause.setImageResource(android.R.drawable.ic_media_pause);

        }
        else if(requestCode== sd_card_directory_reqCode){
            if(resultCode==Activity.RESULT_OK) {
                if (data != null) {
                    Uri uri = data.getData();
                    File file = new File(uri.getPath());
                    Log.d("file name", file.getName());
                    String path = "/storage/" + file.getName().replace(":", "");
                    SharedPreferences sp = getSharedPreferences("sharedpreferences",MODE_PRIVATE);
                    SharedPreferences.Editor ed = sp.edit();
                    ed.putString("sdcardpath",path);
                    sdCard = new File(path);///////////////////////////////////////////////////
                    fetchSongs(sdCard);
                    flag_get_sd_card_directory = true;
                    ed.putBoolean("sdcard",true);
                    ed.apply();
                    final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                    ContentResolver resolver = getContentResolver();
                    resolver.takePersistableUriPermission(uri,takeFlags);

                }
                // sp

                refreshBT.performClick();
            }
            else{
                Toast.makeText(MainActivity.this, "Only Internal Storage songs is visible!", Toast.LENGTH_SHORT).show();
                SharedPreferences sp = getSharedPreferences("sharedpreferences",MODE_PRIVATE);
                SharedPreferences.Editor ed = sp.edit();
                ed.putBoolean("sdcard",false);
                ed.apply();
                refreshBT.performClick();
            }

        }
        else{
            Log.d("MainActivity", "Returned without song");
        }

    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parent = findViewById(R.id.parentLayout);
        seekBar = findViewById(R.id.seekBar);
        previousBT = findViewById(R.id.imageButton3);
        nextBT = findViewById(R.id.imageButton2);
        playBT = findViewById(R.id.imageButton);
        songList = findViewById(R.id.Playlists);
        refreshBT = findViewById(R.id.ib_refresh);
        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        tv_songName = findViewById(R.id.textView_songName);
        tv_songName.setSelected(true);
        circularImageView = findViewById(R.id.circular_IV);
        circularImageView.setImageResource(R.drawable.nothumb);
        likedBT = findViewById(R.id.mainactivity_liked_IB);
        shuffle = findViewById(R.id.mainactivity_shuffle_IB);
        repeat = findViewById(R.id.mainactivity_repeat_IB);
        visualizer = findViewById(R.id.visualiser);

//        View view = LayoutInflater.from(this).inflate(R.layout.notification_layout,null);
//        notification_seekbar = view.findViewById(R.id.notification_seekbar);
//        notification_playpause = view.findViewById(R.id.notification_playpause_BT);
//        notification_previous = view.findViewById(R.id.notification_previous_BT);
//        notification_next = view.findViewById(R.id.notification_next_BT);
//        notification_liked = view.findViewById(R.id.notification_liked_BT);
//        notification_currenttime = view.findViewById(R.id.notification_currenttime_TV);
//        notification_fulltime = view.findViewById(R.id.notification_fulltime_TV);
//        notification_songname = view.findViewById(R.id.notification_songname_TV);
//        notification_thumb = view.findViewById(R.id.notification_thumb_IV);
//        notification_thumb.setImageResource(R.drawable.nothumb);
//        // in play and mp.play also
//        notification_playpause.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                playBT.performClick();
//            }
//        });
//        notification_previous.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                previousBT.performClick();
//            }
//        });
//        notification_next.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                nextBT.performClick();
//            }
//        });
//        notification_liked.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                likedBT.performClick();
//            }
//        });




//        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(this,"tag");
//        initializeMediaSession();
        audioManager = (AudioManager)this.getSystemService(this.AUDIO_SERVICE);






        SharedPreferences sp = getSharedPreferences("sharedpreferences",MODE_PRIVATE);
        flag_get_sd_card_directory=sp.getBoolean("sdcard",false);
        if(sp.getString("sdcardpath",null)!=null) {
            sdCard = new File(sp.getString("sdcardpath", null));
            fetchSongs(sdCard);
        }
        Log.d("SharesPreferenceReturn",""+flag_get_sd_card_directory);








        Runnable r = new Runnable() {
            @Override
            public void run() {
                if(mp.isPlaying()){
                    seekBar.setProgress(mp.getCurrentPosition());
                    Log.d("Thread",""+mp.getCurrentPosition());
                    int milisec = mp.getCurrentPosition();
                    int minutes,seconds;
                    minutes = milisec/(60*1000);
                    seconds = milisec%(60*1000);

                    String temp = ""+seconds;
                    if(temp.length()==4){
                        temp = '0'+temp;
                    }
                    else if(temp.length()<4){
                        temp = "00"+temp;
                    }
                    Log.d("Thread temp",temp);
                    String time= ""+minutes+":"+temp.charAt(0)+temp.charAt(1);
                    textView1.setText(time);
                }
                handler.postDelayed(this,500);
            }
        };
        handler.postDelayed(r,500);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    try {
                        mp.seekTo(progress);
                        // change time here too
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        refreshBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Animation animation = (Animation) AnimationUtils.loadAnimation(MainActivity.this,R.anim.rotate);


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                disableButton(songList);
                                disableButton(nextBT);
                                disableButton(previousBT);
                                disableButton(refreshBT);
                                disableButton(likedBT);
                                // add animation of loading
                                refreshBT.startAnimation(animation);

                            }
                        });
                        // Delete previous table

                        DatabaseHelper dbhh = new DatabaseHelper(MainActivity.this);
                        ArrayList<String> likedSongs = dbhh.dropTableAndRefresh("All Songs");
                        dbhh.close();

                        //Fetch new Data
                        //allSongs = fetchSongs(Environment.getExternalStorageDirectory());
                        File file;
                        fetchSongs(Environment.getExternalStorageDirectory());
                        if(sdCard!=null){
                            fetchSongs(sdCard);
                        }

                        //Toast.makeText(MainActivity.this, "No of Songs: "+allSongs.size(), Toast.LENGTH_SHORT).show();
                        for(int i=0;i<allSongs.size();i++)
                        {
                            Uri uri = Uri.fromFile(allSongs.get(i));
                            DatabaseBlueprint dbb = new DatabaseBlueprint(allSongs.get(i).getName(),uri.toString());
                            DatabaseHelper dbh = new DatabaseHelper(MainActivity.this);
                            if(likedSongs.contains(allSongs.get(i).getName())) {
                                boolean insert = dbh.add(dbb,1);
                                if (insert) {
                                    Log.d("MainActivity insert", "Success");
                                } else {
                                    Log.d("MainActivity insert", "Fail");
                                }
                            }
                            else{
                                boolean insert = dbh.add(dbb,0);
                                if (insert) {
                                    Log.d("MainActivity insert", "Success");
                                } else {
                                    Log.d("MainActivity insert", "Fail");
                                }
                            }
//                            likedSongs.clear();
                            dbh.close();
                        }
                        //songList.setEnabled(true);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                enableButton(songList);
                                enableButton(nextBT);
                                enableButton(previousBT);
                                enableButton(refreshBT);
                                enableButton(likedBT);
                                Toast.makeText(MainActivity.this, "All Songs Loaded!", Toast.LENGTH_SHORT).show();
                                refreshBT.clearAnimation();
                            }
                        });

                    }
                }).start();

            }
        });
        // update playlists for deleted Songs
//        if(flag_get_sd_card_directory)
//            refreshBT.performClick();
        /////////////////////////////////////

        songList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(firstLaunch){
//                    Toast.makeText(MainActivity.this, "Search First Once", Toast.LENGTH_SHORT).show();
//                }
//                else {
                    Intent intent = new Intent(MainActivity.this,PlaylistActivity.class);
                    startActivityForResult(intent,0);
//                }

            }
        });
        playBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!hasFocus){
                    // request audio focus
                    if(audioManager.requestAudioFocus(afChangeListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                        hasFocus=true;
                    }

                }
                if(mp.isPlaying()){
                    mp.pause();
//                    mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, mp.getCurrentPosition(), 1f);
                    playBT.setImageResource(R.drawable.ic_play_circle);
                    //notification_playpause.setImageResource(android.R.drawable.ic_media_play);
                }
                else if(songAssigned && hasFocus){
                    mp.start();
                    playBT.setImageResource(R.drawable.ic_pause_circle);
                    //notification_playpause.setImageResource(android.R.drawable.ic_media_pause);
                }
                else if(!songAssigned){
                    Toast.makeText(MainActivity.this, "Select song from list", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }

            }
        });
        nextBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper dbh = new DatabaseHelper(MainActivity.this);
                if(currentTable==null){
                    Toast.makeText(MainActivity.this, "Select Song From List", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(repeatState==0) {
                        if (currentSongIndex + 1 >= songsInPlaylist.size()) {
                            Toast.makeText(MainActivity.this, "Reached End", Toast.LENGTH_SHORT).show();

                        } else {
                            currentSongIndex++;
                            playSong(currentSongIndex);
                            playBT.setImageResource(R.drawable.ic_pause_circle);
                            //notification_playpause.setImageResource(android.R.drawable.ic_media_pause);
                        }
                    }
                    else if(repeatState==1){
                        currentSongIndex= (currentSongIndex+1)%songsInPlaylist.size();
                        playSong(currentSongIndex);
                        playBT.setImageResource(R.drawable.ic_pause_circle);
                        //notification_playpause.setImageResource(android.R.drawable.ic_media_pause);

                    }
                    else{
                        // repeat one song
                        playSong(currentSongIndex);
                        playBT.setImageResource(R.drawable.ic_pause_circle);
                        //notification_playpause.setImageResource(android.R.drawable.ic_media_pause);

                    }
                }
            }
        });
        previousBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mp.getCurrentPosition() <= 4000) {

                    if (currentSongIndex - 1 < 0) {
                        Toast.makeText(MainActivity.this, "Reached Starting", Toast.LENGTH_SHORT).show();
                    } else {
                        currentSongIndex--;
                        playSong(currentSongIndex);
                        playBT.setImageResource(R.drawable.ic_pause_circle);
                        //notification_playpause.setImageResource(android.R.drawable.ic_media_pause);
                    }
                } else {
                    mp.seekTo(0);
                    seekBar.setProgress(0);
                    textView1.setText("0:00");
                }

                ////////////////////////////////////////////////////////////////////////////////////////////////
            }
        });
        likedBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentSongName==null || currentSongName==""){
                    return;
                }
                DatabaseHelper dbh = new DatabaseHelper(MainActivity.this);
                if(dbh.checkLiked(currentSongName)){
                    // dislike it
                    dbh.removeFromLiked(currentSongName);
                    likedBT.setImageResource(R.drawable.ic_not_liked);
                    //notification_liked.setImageResource(R.drawable.ic_not_liked);
                }
                else{
                    // like it
                    dbh.addToLiked(currentSongName);
                    likedBT.setImageResource(R.drawable.ic_liked);
                    //notification_liked.setImageResource(R.drawable.ic_liked);
                }
                dbh.close();
            }
        });
        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(songsInPlaylist==null || songsInPlaylist.size()==0){
                    Toast.makeText(MainActivity.this, "No songs in queue", Toast.LENGTH_SHORT).show();
                    return;
                }
                ArrayList<String> temp = new ArrayList<>();
                temp.addAll(songsInPlaylist);
                Collections.sort(temp);
                if(!isShuffle){
                    Collections.shuffle(songsInPlaylist);
                    currentSongIndex = songsInPlaylist.indexOf(currentSongName);
                    isShuffle=true;
                    shuffle.setImageResource(R.drawable.ic_shuffle_true);
                }
                else{
                    songsInPlaylist.clear();
                    songsInPlaylist = new ArrayList<>(temp);
                    temp.clear();
                    isShuffle= false;
                    currentSongIndex = songsInPlaylist.indexOf(currentSongName);
                    shuffle.setImageResource(R.drawable.ic_shuffle);
                }
            }
        });
        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatState==0){
                    repeatState = 1;
                    repeat.setImageResource(R.drawable.ic_repeat_playlist);
                }
                else if(repeatState==1){
                    repeatState = 2;
                    repeat.setImageResource(R.drawable.ic_repeat_one);
                }
                else{
                    repeatState = 0;
                    repeat.setImageResource(R.drawable.ic_repeat);
                }
            }
        });

        permissions.add(Manifest.permission.RECORD_AUDIO);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        //permissions.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        ArrayList<Integer> reqCodes = new ArrayList<Integer>();
        reqCodes.add(2);
        reqCodes.add(1);
        //reqCodes.add(4);
        checkPermission(permissions,reqCodes);

    }

    private void checkPermission(ArrayList<String> permissions, ArrayList<Integer> requestCodes) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permissions.get(0)) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(MainActivity.this, permissions.get(1)) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permissions.get(0),permissions.get(1)}, 3);
            }

            else {
                Log.d("zRe","refresh");
                refreshBT.performClick();
            }
    }
    @Override
    public void onRequestPermissionsResult(int reqCode, @NonNull String[] permissions, @NonNull int[] grantResult) {
        super.onRequestPermissionsResult(reqCode, permissions, grantResult);
        if(reqCode == 3){
            if(grantResult.length>0 && grantResult[0]==PackageManager.PERMISSION_GRANTED && grantResult[1]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                SharedPreferences sp = getSharedPreferences("sharedpreferences",MODE_PRIVATE);
                if(!sp.getBoolean("sdcard",false)){
                    Toast.makeText(MainActivity.this, "Select SD Card storage for SD Card songs else go back", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(intent, sd_card_directory_reqCode);

                }
                //refreshBT.performClick();

            }
            else{
                finish();
            }
        }

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacksAndMessages(null);

    }

//    public void initializeMediaSession() {
//        mMediaSession = new MediaSessionCompat(this,TAG);
//        mMediaSession.setFlags(
//                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
//                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
//        mMediaSession.setMediaButtonReceiver(null);
//        mStateBuilder = new PlaybackStateCompat.Builder()
//                .setActions(
//                        PlaybackStateCompat.ACTION_PLAY |
//                                PlaybackStateCompat.ACTION_PAUSE |
//                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
//                                PlaybackStateCompat.ACTION_PLAY_PAUSE|
//                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
//
//        mMediaSession.setPlaybackState(mStateBuilder.build());
//        mMediaSession.setCallback(new MySessionCallback());
//        mMediaSession.setActive(true);
//
//    }
//
//    private class MySessionCallback extends MediaSessionCompat.Callback {
//        @Override
//        public void onPlay() {
//            MainActivity.playBT.performClick();
//        }
//
//        @Override
//        public void onPause() {
//            MainActivity.playBT.performClick();
//        }
//
//        @Override
//        public void onSkipToNext() {
//            MainActivity.nextBT.performClick();
//        }
//
//        @Override
//        public void onSkipToPrevious() {
//            MainActivity.previousBT.performClick();
//        }
//    }
//    private void showNotification(PlaybackStateCompat state) {
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//
//        int icon;
//        String play_pause;
//        if(state.getState() == PlaybackStateCompat.STATE_PLAYING){
//            icon = R.drawable.ic_pause_circle;
//            play_pause = "pause";
//        } else {
//            icon = R.drawable.ic_play_circle;
//            play_pause = "play";
//        }
//
//
//        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
//                icon, play_pause,
//                MediaButtonReceiver.buildMediaButtonPendingIntent(this,
//                        PlaybackStateCompat.ACTION_PLAY_PAUSE));
//
//        NotificationCompat.Action restartAction = new android.support.v4.app..Action(R.drawable.exo_controls_previous, getString(R.string.restart),
//                MediaButtonReceiver.buildMediaButtonPendingIntent
//                        (this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
//
//        PendingIntent contentPendingIntent = PendingIntent.getActivity
//                (this, 0, new Intent(this, QuizActivity.class), 0);
//
//        builder.setContentTitle(getString(R.string.guess))
//                .setContentText(getString(R.string.notification_text))
//                .setContentIntent(contentPendingIntent)
//                .setSmallIcon(R.drawable.ic_music_note)
//                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                .addAction(restartAction)
//                .addAction(playPauseAction)
//                .setStyle(new NotificationCompat.MediaStyle()
//                        .setMediaSession(mMediaSession.getSessionToken())
//                        .setShowActionsInCompactView(0,1));
//
//
//        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        mNotificationManager.notify(0, builder.build());
//    }

}

