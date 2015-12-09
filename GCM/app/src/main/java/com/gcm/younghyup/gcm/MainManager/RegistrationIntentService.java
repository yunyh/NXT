package com.gcm.younghyup.gcm.MainManager;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.gcm.younghyup.gcm.R;
import com.gcm.younghyup.gcm.RestRequest.Item;
import com.gcm.younghyup.gcm.RestRequest.RestRequest;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Retrofit;

/**
 * Created by YoungHyub on 2015-12-02.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegistrationIntentService";
    private static final String TAG_2 = "ServerMessage";
    private static final String CLIENT = "Client";
    private static final String[] TOPICS = {"global"};

    public RegistrationIntentService() {
        super(TAG);
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.d(TAG, "GCM Registration Token : " + token);
            sendRegistrationToServer(token);
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SEND_TOKEN_TO_SERVER, true).apply();
        } catch (IOException e) {
            e.printStackTrace();
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SEND_TOKEN_TO_SERVER, false).apply();
        }
        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    public void sendRegistrationToServer(String token){
        //add server

        RestRequest.APIService service = new RestRequest().getService();
        Call<Item> itemCall = service.sendID(token, CLIENT);
        itemCall.enqueue(new Callback<Item>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(retrofit.Response response, Retrofit retrofit) {
                Log.d(TAG_2, response.message());
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    private void subscribeTopic(String token) throws IOException{
        GcmPubSub pubSub =GcmPubSub.getInstance(this);
        for(String topic : TOPICS){
            pubSub.subscribe(token, "/topic/" + topic, null);
        }
    }
}
