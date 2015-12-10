import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.*;
import java.util.*;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Settings;
import lejos.nxt.TouchSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTCommConnector;
import lejos.nxt.comm.NXTConnection;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.NavigationListener;
import lejos.robotics.navigation.Navigator;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import lejos.robotics.objectdetection.Feature;
import lejos.robotics.objectdetection.FeatureDetector;
import lejos.robotics.objectdetection.FeatureListener;
import lejos.robotics.objectdetection.RangeFeatureDetector;
import lejos.robotics.objectdetection.TouchFeatureDetector;

public class NxtMain{
	
	private static final int DETECTING = 0;
	private static final int CONNECTED = 1;
	private static final int STOP = 2;
	private static final int DISCONNECTED = 3;
	private static final String PATROL = "Patrol";
	
	private int state;
	
	public static int MAX_DETECT = 100;
    public ConnectTheard mConnectTheard;
    static DataInputStream dis;
    static DataOutputStream dos;
	UltrasonicSensor us;
	TouchSensor ts1;
	TouchSensor ts2;
	RangeFeatureDetector fd;
	TouchFeatureDetector td1;
	TouchFeatureDetector td2;
	
	RegulatedMotor LeftMoter = Motor.B;
	RegulatedMotor RightMoter = Motor.C;
	RegulatedMotor Shooter = Motor.A;
    DifferentialPilot differentialPilot;
    Waypoint point1;
    Waypoint point2;
    Waypoint point3;
    Waypoint point4;
    Navigator navigator;
	
    public NxtMain() {
		// TODO Auto-generated constructor stub
    	mConnectTheard = new ConnectTheard();
    	mConnectTheard.start();
    	setState(CONNECTED);
	}
	public static void main (String[] args) throws Exception{
		
		NxtMain nxtMain = new NxtMain();
		nxtMain.doIt();
		
	}
	
	public void doIt() {
		us = new UltrasonicSensor(SensorPort.S4);
		ts1 = new TouchSensor(SensorPort.S2);
		ts2 = new TouchSensor(SensorPort.S1);
		td1 = new TouchFeatureDetector(ts1, 4.5, 10);
		td2 = new TouchFeatureDetector(ts2, 4.5 ,10);
		fd = new RangeFeatureDetector(us, MAX_DETECT, 300);
		
		differentialPilot = new DifferentialPilot(3.22f, 19.5f, LeftMoter, RightMoter);
		navigator = new Navigator(differentialPilot);
		LeftMoter.setSpeed(300);
		RightMoter.setSpeed(300);
		Shooter.setSpeed(500);
		point1 = new Waypoint(100,0);
		point2 = new Waypoint(100,100);
		point3 = new Waypoint(0,100);
		point4 = new Waypoint(0,0);
		
		setWaypoint();
		
		navigator.addNavigationListener(new NavigationListener() {
			
			@Override
			public void pathInterrupted(Waypoint waypoint, Pose pose, int sequence) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void pathComplete(Waypoint waypoint, Pose pose, int sequence) {
				// TODO Auto-generated method stub
				setWaypoint();
			}
			
			@Override
			public void atWaypoint(Waypoint waypoint, Pose pose, int sequence) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		LCD.drawString("Patrol...", 0, 1);
		
		fd.addListener(new FeatureListener() {
			@Override
			public void featureDetected(Feature feature, FeatureDetector detector) {
				// TODO Auto-generated method stub
				int range = (int)feature.getRangeReading().getRange();
				
			//	String alert = "alert";
//				if(range < 10 && (getState() == DETECTING || getState() == CONNECTED)){
				if(range < 20 || ts1.isPressed() || ts2.isPressed()){
					try {
						LCD.clear();
						SendAlert();
						//write(alert.getBytes());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						LCD.drawString("Not Connected Bluetooth", 0, 1);
					}
					System.out.println("Range:" + range);					
				}
			}
		});
		td1.addListener(new FeatureListener() {
			
			@Override
			public void featureDetected(Feature feature, FeatureDetector detector) {
				// TODO Auto-generated method stub
				LCD.drawString("Left Bumper... ", 0, 1);
				if(feature.getRangeReading().getRange() > 10){
					try {
						LCD.clear();
						SendAlert();
						//write(alert.getBytes());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						LCD.drawString("Not Connected Bluetooth", 0, 1);
					}
				}
			}
		});
		
		td1.addListener(new FeatureListener() {
			
			@Override
			public void featureDetected(Feature feature, FeatureDetector detector) {
				// TODO Auto-generated method stub
				LCD.drawString("Right Bumper... ", 0, 1);
				if(feature.getRangeReading().getRange() > 10){
					try {
						LCD.clear();
						SendAlert();
						//write(alert.getBytes());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						LCD.drawString("Not Connected Bluetooth", 0, 1);
					}
				}
			}
		});
		Button.ENTER.addButtonListener(new ButtonListener() {
			
			@Override
			public void buttonReleased(Button b) {
				// TODO Auto-generated method stub
				try {
					mConnectTheard.cancel();
					mConnectTheard = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			@Override
			public void buttonPressed(Button b) {
				// TODO Auto-generated method stub
				try {
					mConnectTheard.cancel();
					mConnectTheard = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		Button.ENTER.waitForPressAndRelease();

	}
	
	//state set/get
	public synchronized void setState(int state){
		this.state = state;
	}
	
	public synchronized int getState(){
		return state;
	}
	
	public void setWaypoint(){
		navigator.goTo(point1);
		navigator.addWaypoint(point2);
		navigator.addWaypoint(point3);
		navigator.addWaypoint(point4);
	}
	
	public void stopMove(){
		navigator.stop();
		LeftMoter.stop();
		RightMoter.stop();
	}
	
	public void shoot(){
		Shooter.rotate(360);
		
	}
	//정찰 시작 함수
	public void ReStartPatrol(){
		setState(DETECTING);
		
		fd.enableDetection(true);
		td1.enableDetection(true);
		setWaypoint();
		LCD.clear();
		LCD.drawString("Patrol...", 0, 1);
	}
	
	public void Shooting(){
		shoot();
		//LCD.clear();
		LCD.drawString("Fire!!!!", 0, 2);
	}
	public synchronized void SendAlert(){
		fd.enableDetection(false);
		td1.enableDetection(false);
		stopMove();
		String alert = "alert";
		LCD.clear();
		LCD.drawString("Alert...", 0, 1);
		setState(CONNECTED);
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
	    //BufferedReader bufferedReader;
	    
	    int bytes;
	    String message;
	    public void run(){
	    	while(true){
	    		try {				
					if(dis.available() > 0){
						byte[] buffer = new byte[128];
						bytes = dis.read(buffer);
						message = new String(buffer, 0, bytes);
						//bufferedReader = new BufferedReader(new InputStreamReader(dis));
						String s = new String();
						LCD.clear();
						//System.out.println(message);
						
						if(message.contentEquals("confirm")){
							ReStartPatrol();
						}
						else if(message.contentEquals("shoot")){
						    Shooting();
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
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