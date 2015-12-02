package com.gcm.younghyup.gcm.MainManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gcm.younghyup.gcm.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    private BroadcastReceiver mBroadcastReceiver;
    private ProgressBar mProgressBar;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = (ProgressBar) findViewById(R.id.registrationProgressbar);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean alert = sharedPreferences.getBoolean(QuickstartPreferences.RECEIVE_ALERT_TO_SERVER, false);
                if(alert) {
                    mTextView.setText("Alert");
                    sharedPreferences.edit().putBoolean(QuickstartPreferences.RECEIVE_ALERT_TO_SERVER, false).apply();

                }
                else{
                    boolean sentToken = sharedPreferences.getBoolean(QuickstartPreferences.SEND_TOKEN_TO_SERVER, false);
                    if(sentToken){
                        mTextView.setText(getString(R.string.gcm_send_message));
                    }
                    else{
                        mTextView.setText(getString(R.string.token_error_message));
                    }
                }
            }
        };
        mTextView = (TextView) findViewById(R.id.informationTextView);

        if(checkPlayService()){
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        registerReceiver();
    }

    public void registerReceiver(){
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(QuickstartPreferences.REGISTRATION_COMPLETE);
        intentFilter.addAction((QuickstartPreferences.RECEIVE_ALERT_TO_SERVER));
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
               intentFilter);
    }
    @Override
    public void onPause(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();

    }

    private boolean checkPlayService(){
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(apiAvailability.isUserResolvableError(resultCode)){
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }else{
                Log.i(TAG, "This device is not support");
                finish();
            }
            return false;
        }
        return true;
    }

}
