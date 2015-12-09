package com.gcm.younghyup.gcm.MainManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gcm.younghyup.gcm.R;
import com.gcm.younghyup.gcm.RestRequest.Item;
import com.gcm.younghyup.gcm.RestRequest.RestRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int STATUS_RED = 0;
    private static final int STATUS_YELLOW = 1;
    private static final int STATUS_GREEN = 2;
    private static final String TAG = "MainActivity";

    private BroadcastReceiver mBroadcastReceiver;
    private ProgressBar mProgressBar;
    private TextView mTextView;
    private Button mConfirmButton;
    private ImageView mStatusImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = (ProgressBar) findViewById(R.id.registrationProgressbar);
        mConfirmButton = (Button) findViewById(R.id.confirmButton);
        mStatusImageView = (ImageView) findViewById(R.id.statusImage);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mProgressBar.setVisibility(ProgressBar.GONE);
                mConfirmButton.setVisibility(View.GONE);
                final SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean alert = sharedPreferences.getBoolean(QuickstartPreferences.RECEIVE_ALERT_TO_SERVER, false);
                if(alert) {
                    mConfirmButton.setVisibility(View.VISIBLE);
                    setStatusView(STATUS_RED);
                    mConfirmButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            RestRequest.APIService service = new RestRequest().getService();
                            Call<Item> itemCall = service.sendConfirm(true);
                            itemCall.enqueue(new Callback<Item>() {
                                @Override
                                public void onResponse(retrofit.Response response, Retrofit retrofit) {
                                    sharedPreferences.edit().putBoolean(QuickstartPreferences.RECEIVE_ALERT_TO_SERVER, false).apply();
                                    setStatusView(STATUS_GREEN);
                                }
                                @Override
                                public void onFailure(Throwable t) {
                                    setStatusView(STATUS_YELLOW);
                                }
                            });
                        }
                    });
                }
                else{
                    boolean sentToken = sharedPreferences.getBoolean(QuickstartPreferences.SEND_TOKEN_TO_SERVER, false);
                    if(sentToken){
                        setStatusView(STATUS_GREEN);
                    }
                    else{
                        mTextView.setText(getString(R.string.token_error_message));
                        setStatusView(STATUS_YELLOW);

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

    private void setStatusView(int status){
        switch (status){
            case STATUS_RED:
                mTextView.setText(R.string.alert);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mStatusImageView.setBackground(getDrawable(R.drawable.ic_red));
                }
                else{
                    mStatusImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_red));
                }
                break;
            case STATUS_YELLOW:
                mTextView.setText(R.string.token_error_message);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mStatusImageView.setBackground(getDrawable(R.drawable.ic_yellow));
                }
                else{
                    mStatusImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_yellow));
                }
                break;
            case STATUS_GREEN:
                mTextView.setText(getString(R.string.gcm_send_message));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mStatusImageView.setBackground(getDrawable(R.drawable.ic_green));
                }
                else{
                    mStatusImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_green));
                }
                break;
        }
    }

}
