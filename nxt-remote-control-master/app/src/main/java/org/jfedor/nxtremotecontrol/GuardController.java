package org.jfedor.nxtremotecontrol;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import org.jfedor.nxtremotecontrol.GcmManager.QuickstartPreferences;
import org.jfedor.nxtremotecontrol.RestRequest.Item;
import org.jfedor.nxtremotecontrol.RestRequest.RestRequest;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class GuardController extends Activity {
    
    private boolean NO_BT = false; 
    
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CONNECT_DEVICE = 2;
    private static final int REQUEST_SETTINGS = 3;
    
    public static final int MESSAGE_TOAST = 1;
    public static final int MESSAGE_STATE_CHANGE = 2;
    public static final int MESSAGE_ALERT = 3;
    
    public static final String TOAST = "toast";

    private BluetoothAdapter mBluetoothAdapter;

    private NXTTalker mNXTTalker;
    private BroadcastReceiver mReceiver;
    
    private int mState = NXTTalker.STATE_NONE;
    private int mSavedState = NXTTalker.STATE_NONE;
    private boolean mNewLaunch = true;
    private String mDeviceAddress = null;
    private TextView mStateDisplay;
    private TextView mLogTextView;
    private Button mConnectButton;
    private Button mDisconnectButton;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        mStateDisplay = (TextView) findViewById(R.id.state_display);
        mConnectButton = (Button) findViewById(R.id.connect_button);
        mConnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NO_BT) {
                    findBrick();
                } else {
                    mState = NXTTalker.STATE_CONNECTED;
                }
            }
        });

        mDisconnectButton = (Button) findViewById(R.id.disconnect_button);
        mDisconnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mNXTTalker.stop();
            }
        });
        mLogTextView = (TextView)findViewById(R.id.log_textview);
        mLogTextView.setMovementMethod(new ScrollingMovementMethod());

        if (!NO_BT) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        mNXTTalker = new NXTTalker(mHandler);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

                if(QuickstartPreferences.NOT_CONNECTED_NXT.equals(action)){
                    updateLog("Not connected");
                }

                if(QuickstartPreferences.RECEIVE_DEACTIVATION_TO_SERVER.equals(action)){
                    boolean set = sharedPreferences.getBoolean(QuickstartPreferences.SEND_TOKEN_TO_SERVER, false);
                    updateLog("Send token to server");
                    String state = sharedPreferences.getString(QuickstartPreferences.MESSAGE_STATE, null);
                    updateLog("Receive Message : " + state);
                    if(set){
                        if(state.equals("confirm")) {
                            sharedPreferences.edit().putBoolean(QuickstartPreferences.RECEIVE_DEACTIVATION_TO_SERVER, false).apply();
                            mNXTTalker.ReStartingPatrol(state);
                        }
                        else if(state.equals("shoot")){
                            mNXTTalker.ReStartingPatrol(state);
                        }
                        else {
                            updateLog("Error");
                        }
                    }
                    else{
                        updateLog("Already Detecting");
                    }
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Log.i("NXT", "NXTRemoteControl.onStart()");
        if (!NO_BT) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            } else {
                if (mSavedState == NXTTalker.STATE_CONNECTED) {
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
                    mNXTTalker.connect(device);
                } else {
                    if (mNewLaunch) {
                        mNewLaunch = false;
                        findBrick();
                    }
                }
            }
        }
    }

    private void findBrick() {
        Intent intent = new Intent(this, ChooseDeviceActivity.class);
        startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
    }
  
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case REQUEST_ENABLE_BT:
            if (resultCode == Activity.RESULT_OK) {
                findBrick();
            } else {
                Toast.makeText(this, "Bluetooth not enabled, exiting.", Toast.LENGTH_LONG).show();
                updateLog("Bluetooth not enabled, exiting.");
                finish();
            }
            break;
        case REQUEST_CONNECT_DEVICE:
            if (resultCode == Activity.RESULT_OK) {
                String address = data.getExtras().getString(ChooseDeviceActivity.EXTRA_DEVICE_ADDRESS);
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                mDeviceAddress = address;
                mNXTTalker.connect(device);
            }
            break;
        case REQUEST_SETTINGS:
            //XXX?
            break;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(QuickstartPreferences.RECEIVE_DEACTIVATION_TO_SERVER);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                intentFilter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mState == NXTTalker.STATE_CONNECTED) {
            outState.putString("device_address", mDeviceAddress);
        }
    }

    private void displayState() {
        String stateText = null;
        int color = 0;
        switch (mState){ 
        case NXTTalker.STATE_NONE:
            stateText = "Not connected";
            updateLog(stateText);
            color = 0xffff0000;
            mConnectButton.setVisibility(View.VISIBLE);
            mDisconnectButton.setVisibility(View.GONE);
            setProgressBarIndeterminateVisibility(false);
            break;
        case NXTTalker.STATE_CONNECTING:
            stateText = "Connecting...";
            updateLog(stateText);
            color = 0xffffff00;
            mConnectButton.setVisibility(View.GONE);
            mDisconnectButton.setVisibility(View.GONE);
            setProgressBarIndeterminateVisibility(true);
            break;
        case NXTTalker.STATE_CONNECTED:
            stateText = "Connected";
            updateLog(stateText);
            color = 0xff00ff00;
            mConnectButton.setVisibility(View.GONE);
            mDisconnectButton.setVisibility(View.VISIBLE);
            setProgressBarIndeterminateVisibility(false);
            break;
        }
        mStateDisplay.setText(stateText);
        mStateDisplay.setTextColor(color);
    }

    public void updateLog(final String log){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLogTextView.append(log + "\n");
                int lineTop =  mLogTextView.getLineCount();
                int scrollY = lineTop - mLogTextView.getHeight();
                Log.d("LineTop", Integer.toString(lineTop));
                Log.d("Scroll Y", Integer.toString(scrollY));
                Log.d("Textview Height", Integer.toString(mLogTextView.getHeight()));
                if (scrollY > 0) {
                    mLogTextView.scrollTo(0, scrollY);
                } else {
                    mLogTextView.scrollTo(0, 0);
                }
            }
        });
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_STATE_CHANGE:
                mState = msg.arg1;
                updateLog("State is " + Integer.toString(mState));
                displayState();
                break;
            case MESSAGE_ALERT:
                RestRequest.APIService service = new RestRequest().getService();
                Call<Item> itemCall = service.sendAlert(msg.toString());
                updateLog(msg.toString());
                itemCall.enqueue(new retrofit.Callback<Item>() {
                    @Override
                    public void onResponse(Response<Item> response, Retrofit retrofit) {
                        Log.d("Handler", response.message());
                    }

                    @Override
                    public void onFailure(Throwable t) {

                    }
                });
                break;
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        mSavedState = mState;
        updateLog("Stop");
        mNXTTalker.stop();
    }
}
