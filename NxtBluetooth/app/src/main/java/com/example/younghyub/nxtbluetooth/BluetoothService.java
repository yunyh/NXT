package com.example.younghyub.nxtbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by YoungHyub on 2015-11-13.
 */
public class BluetoothService extends Handler {
    private BluetoothAdapter mBluetoothAdapter;
    private Activity mActivity;
    private Handler mHandler;

    //private ArrayList mDeviceNameList;
    private ArrayAdapter<String> mArrayAdapter;
    private ListView mListView;

    public BluetoothService() {

    }

    public BluetoothService(Activity activity, Handler handler) {
        mActivity = activity;
        mHandler = handler;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){

        }
    }

    public void BluetoothDeviceList() {
      //  mListView = (ListView) mActivity.findViewById(R.id.device_list);
        mArrayAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1);
        mListView.setAdapter(mArrayAdapter);
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
// If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add("Device Name : " + device.getName() + "\n"
                        + "Address : " + device.getAddress());

                //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInput;
        private final OutputStream mmOutput;

        public ConnectThread(BluetoothSocket socket){
            mmSocket = socket;

            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try{
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }catch (IOException e){

            }
            mmInput = tmpIn;
            mmOutput = tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while(true){
                try{
                    bytes = mmInput.read(buffer);
                    mHandler.obtainMessage(2, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void write(byte[] buffer){
            try{
                mmOutput.write(buffer);
            }catch (IOException e){

            }
        }

        public void cancel(){
            try{
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}