import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.nxt.Settings;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTCommConnector;
import lejos.nxt.comm.NXTConnection;
import lejos.robotics.objectdetection.Feature;
import lejos.robotics.objectdetection.FeatureDetector;
import lejos.robotics.objectdetection.FeatureListener;
import lejos.robotics.objectdetection.RangeFeatureDetector;

public class NxtMain {
	
	private static final int DETECTING = 0;
	private static final int CONNECTED = 1;
	private static final int STOP = 2;
	private static final int DISCONNECTED = 3;
	private static final String PATROL = "Patrol";
	
	private int state;
	
	public static int MAX_DETECT = 40;
    public ConnectTheard mConnectTheard;
    static DataInputStream dis;
    static DataOutputStream dos;
	UltrasonicSensor us;
	RangeFeatureDetector fd;
    
    public NxtMain() {
		// TODO Auto-generated constructor stub
    	mConnectTheard = new ConnectTheard();
    	setState(CONNECTED);
	}
	public static void main (String[] args) throws Exception{
		
		NxtMain nxtMain = new NxtMain();
		nxtMain.doIt();
		
	}
	
	public void doIt(){
		us = new UltrasonicSensor(SensorPort.S4);
		fd = new RangeFeatureDetector(us, MAX_DETECT, 500);
		LCD.drawString("Patrol...", 0, 1);
		fd.addListener(new FeatureListener() {
			@Override
			public void featureDetected(Feature feature, FeatureDetector detector) {
				// TODO Auto-generated method stub
				int range = (int)feature.getRangeReading().getRange();
			//	String alert = "alert";
//				if(range < 10 && (getState() == DETECTING || getState() == CONNECTED)){
				if(range < 10){
					try {
						SendAlert();
						//write(alert.getBytes());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						LCD.clear();
						LCD.drawString("Not Connected Bluetooth", 0, 1);
					}
					System.out.println("Range:" + range);					
				}
			}
		});
		Button.ENTER.addButtonListener(new ButtonListener() {			
			@Override
			public void buttonReleased(Button b) {
				// TODO Auto-generated method stub

			}
			
			@Override
			public void buttonPressed(Button b) {
				// TODO Auto-generated method stub
				try {
					if(getState() == CONNECTED){
						setState(DISCONNECTED);
						mConnectTheard.cancel();
						mConnectTheard = null;
												
					}
					else if(getState() == DISCONNECTED){
				    	mConnectTheard = new ConnectTheard();
				    	setState(CONNECTED);
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
		});
	}
	
	//state set/get
	public synchronized void setState(int state){
		this.state = state;
	}
	
	public synchronized int getState(){
		return state;
	}
	
	//정찰 시작 함수
	public void ReStartPatrol(){
		setState(DETECTING);
		fd.enableDetection(true);
		LCD.clear();
		LCD.drawString("Patrol...", 0, 1);
	}
	
	public synchronized void SendAlert(){
		fd.enableDetection(false);
		String alert = "alert";
		write(alert.getBytes());
	}
	
	//블루투스 write 함수
    private void write(byte[] out) {
    	ConnectTheard r;
        synchronized (this) {
            if (state != CONNECTED) {
                return;
            }
            r = mConnectTheard;
        }
        r.write(out);
    }
	
	private class ConnectTheard extends Thread{
		private NXTConnection connection = Bluetooth.waitForConnection();
		DataInputStream dis = connection.openDataInputStream();
	    DataOutputStream dos = connection.openDataOutputStream();
	    BufferedReader bufferedReader;
	    public void run(){
	    	while(true){
	    		try {
					if(dis.available() > 0){
						bufferedReader = new BufferedReader(new InputStreamReader(dis));
						if(bufferedReader.toString().equals(PATROL)){
							System.out.println("Receive from Device");
							ReStartPatrol();
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    	/*for(int i=0;i<100;i++) {
	            int n;
				try {
					n = dis.readInt();
		            LCD.drawInt(n,7,0,1);
		            
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	} */
	    }
	    
	    public void write(byte[] buffer){
            try {
				dos.write(buffer);
	            dos.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    public void cancel() throws IOException{
	    	dos.close();
	    	dis.close();
	    	connection.close();
	    	LCD.clear();
	    	LCD.drawString("Disconnected", 0, 1);
	    }
	   
	}
}