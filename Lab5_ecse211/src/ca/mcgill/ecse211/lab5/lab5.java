package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.localizers.*;
import ca.mcgill.ecse211.odometer.*;
import ca.mcgill.ecse211.searcher.Navigation;
import ca.mcgill.ecse211.searcher.Search;
import ca.mcgill.ecse211.sensors.DataController;
import ca.mcgill.ecse211.sensors.LightColorPoller;
import ca.mcgill.ecse211.sensors.LightPoller;
import ca.mcgill.ecse211.sensors.UltrasonicPoller;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;


///////// TODO /////////
// - update ultrasonic localizer so that it takes the median of several readings
// - use us sensors on both sides to correct for odometry

//Design of robot
//	- light sensor front does not have to be so far out can have it close to us sensor
//	- should see how well us sensor detects any ring, if its close does it do really well?


public class lab5 {
	
	 // Motor objects and robot parameters
	private static final EV3LargeRegulatedMotor leftMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));

 
	public static final double WHEEL_RAD = 2.2;
	public static final double TRACK = 11.45;
	public static final double SQUARE_SIZE = 30.48;
	
	

	public static void main(String[] args) throws OdometerExceptions {
		
		
		Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD); 
		Navigation navigator = new Navigation(leftMotor, rightMotor);
		Search tracker = new Search(navigator, leftMotor, rightMotor);
		UltrasonicLocalizer uLocalizer = new UltrasonicLocalizer(leftMotor, rightMotor);
		LightLocalizer lLocalizer = new LightLocalizer(leftMotor, rightMotor, navigator);
		DataController dataCont = DataController.getDataController();
		 
		@SuppressWarnings("resource")
		SensorModes usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1")); 
		SampleProvider usDistance = usSensor.getMode("Distance");
		float[] usData = new float[usDistance.sampleSize()];
		
//		@SuppressWarnings("resource")
//		SensorModes lightSensor = new EV3ColorSensor(LocalEV3.get().getPort("S2"));
//		SampleProvider lightSample = lightSensor.getMode("Red");
//		float[] lightData = new float[lightSensor.sampleSize()];
//		
		@SuppressWarnings("resource")
		SensorModes ringSensor = new EV3ColorSensor(LocalEV3.get().getPort("S4"));
		SampleProvider ringSample = ringSensor.getMode("Red");
		float[] ringData = new float[ringSensor.sampleSize()];
				
		
		//Start odometer and sensor threads
		Thread odoThread = new Thread(odometer);
		odoThread.start();
		Thread usPoller = new UltrasonicPoller(usDistance, usData, dataCont);
		usPoller.start();
//	    Thread lightGPoller = new LightPoller(lightSample, lightData, dataCont);
//	    lightGPoller.start();
	    Thread lightRPoller = new LightColorPoller(ringSample, ringData, dataCont);
	    lightRPoller.start();
		
//		Navigation.moveStraight(leftMotor, rightMotor, 50, 100, true, true);
//		while (leftMotor.isMoving()) {
//			double d = dataCont.getD();
//			System.out.print("" + d + ", ");
//			try {
//				Thread.sleep(200);
//			} catch (Exception e) {
//			} // Poor man's timed sampling
//		}
		
		
	    //wait to initiate field trial
		Button.waitForAnyPress();
		
//		//Start ultrasonic localizer thread and wait for it to finish
//		uLocalizer.start();
//		try {
//			uLocalizer.join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		
//		//Start light localizer and wait for it to finish
//		lLocalizer.start();
//		try {
//			lLocalizer.join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		//start search thread
		tracker.start();
		
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);	 
		
		
	 }
}
