package ca.mcgill.ecse211.lab4;

import ca.mcgill.ecse211.odometer.*;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;



public class lab4 {
	

	 // Motor Objects, and Robot related parameters
	 private static final Port usPort = LocalEV3.get().getPort("S1");
	 private static final EV3LargeRegulatedMotor leftMotor =
	     new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	 private static final EV3LargeRegulatedMotor rightMotor =
	     new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	 private static final TextLCD lcd = LocalEV3.get().getTextLCD();
	 
	 public static final double WHEEL_RAD = 2.2;
	 public static final double TRACK = 11.3;
	
	 
	public static void main(String[] args) throws OdometerExceptions {
		 
		 Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD); 
		 Display odometryDisplay = new Display(lcd);
		 final UltrasonicLocalizer uLocalizer = new UltrasonicLocalizer(leftMotor, rightMotor);
		 final LightLocalizer lLocalizer = new LightLocalizer(leftMotor, rightMotor);
		 
		 @SuppressWarnings("resource")
		 SensorModes usSensor = new EV3UltrasonicSensor(usPort); 
		 SampleProvider usDistance = usSensor.getMode("Distance");
		 float[] usData = new float[usDistance.sampleSize()];
		 
		 lcd.clear();
		 lcd.drawString("< Left | Right >", 0, 0);
		 lcd.drawString(" rising| falling", 0, 1);
		 lcd.drawString(" edge  | edge   ", 0, 2);

		 final int buttonChoice = Button.waitForAnyPress(); 
		 
		 Thread odoThread = new Thread(odometer);
		 odoThread.start();
		 
		 Thread odoDisplayThread = new Thread(odometryDisplay);
		 odoDisplayThread.start();
		 
		 Thread usPoller = new UltrasonicPoller(usDistance, usData, uLocalizer);
		 usPoller.start();
		 
		 (new Thread() {
			 public void run() {
				 if (buttonChoice == Button.ID_RIGHT)
					 uLocalizer.fallingEdge();
				 else 
					 uLocalizer.risingEdge();
			 }
		 }).start();
		 
		 while(Button.waitForAnyPress() != Button.ID_LEFT);
		 
		 (new Thread() {
			 public void run() {
				 lLocalizer.findOrigin();
			 }
		 }).start();
		 

		 while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		 System.exit(0);	 
	 }
}
