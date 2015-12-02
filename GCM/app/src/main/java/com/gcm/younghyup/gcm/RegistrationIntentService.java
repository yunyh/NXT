package com.gcm.younghyup.gcm;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.gcm.younghyup.gcm.RestRequest.RestRequestHelper;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.JsonObject;

import java.io.IOException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.Field;

/**
 * Created by YoungHyub on 2015-12-02.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegistrationIntentService";
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
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SEND_TOKEN_TO_SERVER,false).apply();
        }
        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    public void sendRegistrationToServer(String token){
        //add server

        RestRequestHelper.RestRequest service = new RestRequestHelper().getService();
        Call<RestRequestHelper.User> userCall = service.sendID(token);
        userCall.enqueue(new Callback<RestRequestHelper.User>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(Response<RestRequestHelper.User> response, Retrofit retrofit) {
                Log.d(TAG, response.body().getRet().toString());
            }

            @Override
            public void onFailure(Throwable t) {

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
