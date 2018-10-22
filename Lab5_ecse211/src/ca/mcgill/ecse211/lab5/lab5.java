package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.lab5.UltrasonicLocalizer.LocalizationType;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;

/**
 * Class with main method to start the UI
 * @author Huzaifa, Jake
 *
 */
public class Lab5 {

	// Instantiate relevant variables 
	public static final TextLCD lcd = LocalEV3.get().getTextLCD();
	public static final double WHEEL_RAD = 2.2;
	public static final double SQUARE_SIZE = 30.48;
	public static final double TRACK = 14.35;
	public static boolean isUSLocalizing = false;
	public static boolean isLightLocalizing = false;
	public static boolean isLightLocalizingTurn = false;
	public static boolean isGoingToLL = false;

	static Odometer odometer = null;

	public static final int LLx = 2;
	public static final int LLy = 2;
	public static final int URx = 7;
	public static final int URy = 7;
	public static final int SC = 0;
	public static final int TR = 1;


	//Motors and sensor initialization
	static final Port usPort = LocalEV3.get().getPort("S1");
	static final Port portColor = LocalEV3.get().getPort("S2");
	static final Port portGyro = LocalEV3.get().getPort("S3");
	static final Port portRing = LocalEV3.get().getPort("S4");


	public static final EV3LargeRegulatedMotor leftMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor rightMotor =
			new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));


	public static void main(String[] args) throws OdometerExceptions {
		int buttonChoice;

		do {
			lcd.clear();   		// clear the display

			lcd.drawString("<      >", 0, 0);
			lcd.drawString("Falling ", 0, 1);
			lcd.drawString(" Edge   ", 0, 2);
			lcd.drawString("        ", 0, 3);
			lcd.drawString("<      >", 0, 4);

			buttonChoice = Button.waitForAnyPress();      // Record choice (left or right press)

			// Until button pressed
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT); 

		// Set odometer and start thread
		try {
			odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
		} catch (OdometerExceptions e) {
		}
		Thread odoThread = new Thread(odometer);
		odoThread.start();
		Navigation nav = new Navigation(leftMotor, rightMotor, odometer);


		// Based on edge selection, call the corresponding edge method on the Ultrasonic Localizer object
		isUSLocalizing = true;
		UltrasonicLocalizer usLocalizer = new UltrasonicLocalizer(LocalizationType.FALLING_EDGE, odometer, nav);
		usLocalizer.fallingEdge();
		isUSLocalizing = false;
		((EV3GyroSensor) Odometer.myGyro).reset();

		// Upon any input, instantiate light localizer
		isLightLocalizing = true;
		LightLocalizer lightLocalizer  = new LightLocalizer(odometer, nav);    
		lightLocalizer.start();
		try {
			lightLocalizer.join();
		} catch (InterruptedException e) {

		}
		isLightLocalizing = false;
		isLightLocalizingTurn = false;
		
		odometer.setXYT(0, 0, 0);
		((EV3GyroSensor) Odometer.myGyro).reset();

		isGoingToLL = true;
		Navigation.travelTo(0, LLy);
		Navigation.travelTo(LLx, LLy);



	}

}