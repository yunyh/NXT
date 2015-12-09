package com.gcm.younghyup.gcm.MainManager;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ShareActionProvider;

import com.gcm.younghyup.gcm.R;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.gcm.GcmReceiver;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

/**
 * Created by YoungHyub on 2015-12-02.
 */
public class MyGcmListenerService extends GcmListenerService{

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]

    @Override
    public void onMessageReceived(String from, Bundle data) {

        String message = data.getString("message");
        Log.d(TAG, "From : " + from);
        Log.d(TAG, "Data : " + data);
        if (from.startsWith("/topics/")) {

        } else {

        }
        //UPDATE UI
        sendNotification(message);
        super.onMessageReceived(from, data);

        Intent intent = new Intent(QuickstartPreferences.RECEIVE_ALERT_TO_SERVER);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putBoolean(QuickstartPreferences.RECEIVE_ALERT_TO_SERVER, true).apply();
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
}
    /**
     *
     * @param message
     */
    private void sendNotification(String message){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle("NXT Notification")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}
