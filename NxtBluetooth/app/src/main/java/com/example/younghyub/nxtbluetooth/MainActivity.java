package com.example.younghyub.nxtbluetooth;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends Activity implements OnClickListener {

    private BluetoothService mBluetoothService;
    private Handler mHandler;

    Button circle;
    Button square;
    private Button draw;
    private Button mConnect;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        mBluetoothService = new BluetoothService(this, mHandler);
        setContentView(R.layout.activity_main);
        circle = (Button) findViewById(R.id.btnCircle);
        square = (Button) findViewById(R.id.btnSquare);
        draw = (Button) findViewById(R.id.btnDraw);
        mConnect = (Button) findViewById(R.id.btnConnect);
        mConnect.setOnClickListener(this);
        circle.setOnClickListener(this);
        square.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btnCircle:
                break;
            case R.id.btnSquare:
                break;
            case R.id.btnConnect:
                mBluetoothService.BluetoothDeviceList();
                break;
            case R.id.btnDraw:
                FragmentManager fm = getFragmentManager();
                Fragment fragment = new DrawFragment();
                fm.beginTransaction().replace(R.id.container, fragment);
                break;
        }
    }
}
