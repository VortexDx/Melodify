package com.vortico.melodify;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaSession2Service;
import android.os.Build;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

public class Notification extends Application {
    public static final String CHANNEL_ID_1 = "channel_1";
    public static final String CHANNEL_ID_2 = "channel_2";
    public static final String PLAYPAUSE = "play/pause";
    public static final String PREVIOUS = "previous";
    public static final String NEXT = "next";

    public static final int NOTIFICATION_ID= 412;
    public static final String TAG = Notification.class.getSimpleName();
    public static final String CHANNEL_ID = "com.vortico.melodify.channel";
    public static final int REQ_CODE = 501;
    private final MediaSession2Service mService;
    private final NotificationCompat.Action mPlayAction;
    private  NotificationCompat.Action mPauseAction;
    private  NotificationManager mNotificationManager;

    public Notification(MediaSession2Service musicContext){
        mService = musicContext;
        mPlayAction = new NotificationCompat.Action(android.R.drawable.ic_media_play,"play", MediaButtonReceiver.buildMediaButtonPendingIntent(mService, PlaybackStateCompat.ACTION_PLAY));
        mPauseAction = new NotificationCompat.Action(android.R.drawable.ic_media_pause,"pause",MediaButtonReceiver.buildMediaButtonPendingIntent(mService,PlaybackStateCompat.ACTION_PAUSE));
        mNotificationManager.cancelAll();
    }
    public void onDestroy(){
        Log.d(TAG,"on Destroy");
    }
    public NotificationManager getNotificationManager(){
        return mNotificationManager;
    }
    public android.app.Notification getNotification(MediaMetadataCompat metadata, @NonNull PlaybackStateCompat state, MediaSessionCompat.Token token){
        boolean isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;
        MediaDescriptionCompat description = metadata.getDescription();
        NotificationCompat.Builder builder = buildNotification(state, token, isPlaying, description);
        return builder.build();
    }

    private NotificationCompat.Builder buildNotification(@NonNull PlaybackStateCompat state, MediaSessionCompat.Token token, boolean isPlaying, MediaDescriptionCompat description) {
        if(isAndroidOOrHigher()){
            createChannel();
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mService, CHANNEL_ID);
        builder.setStyle(
                new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(token)
                        .setShowActionsInCompactView(0)
                        // For backwards compatibility with Android L and earlier.
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        mService,
                                        PlaybackStateCompat.ACTION_STOP)))
                .setColor(ContextCompat.getColor(mService, R.color.av_dark_blue))
                .setSmallIcon(android.R.drawable.ic_media_play)
                // Pending intent that is fired when user clicks on notification.
                .setContentIntent(createContentIntent())
                // Title - Usually Song name.
                .setContentTitle(description.getTitle())
                // When notification is deleted (when playback is paused and notification can be
                // deleted) fire MediaButtonPendingIntent with ACTION_PAUSE.
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                        mService, PlaybackStateCompat.ACTION_PAUSE));


        builder.addAction(isPlaying ? mPauseAction : mPlayAction);

        return builder;
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            // The user-visible name of the channel.
            CharSequence name = "MediaSession";
            // The user-visible description of the channel.
            String description = "MediaSession and MediaPlayer";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(
                    new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
            Log.d(TAG, "createChannel: New channel created");
        } else {
            Log.d(TAG, "createChannel: Existing channel reused");
        }
    }

    private boolean isAndroidOOrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(mService, MainActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(
                mService, REQ_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
