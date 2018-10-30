package ca.mcgill.ecse211.lab4;

import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class LightLocalizer {

  	private static final int ROTATE_SPEED = 50;
  	private static final int FORWARD_SPEED = 60;
  	private static final int MAX_DISTANCE = 30;
  	private static final int RIGHT_ANGLE = 90; 
  	private static final int LIGHTSENSOR_TO_ROBOTCENTER = 4;
  	private static final double BLACK_LINE = 0.18;
  	
	private Odometer odo;
  	private EV3LargeRegulatedMotor leftMotor;
  	private EV3LargeRegulatedMotor rightMotor;
	private static final SensorModes colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("S2"));
	SampleProvider colorSample = colorSensor.getMode("Red");
	static float[] colorData = new float[colorSensor.sampleSize()];
	
	
	public LightLocalizer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) throws OdometerExceptions{
		this.odo = Odometer.getOdometer();
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
	}
	
	
	public void findOrigin() {
		double saveY = 0.0;
		//move forward till detect line 
		moveStraight(leftMotor, rightMotor, MAX_DISTANCE, true, true);
		while(true) {
			colorSample.fetchSample(colorData, 0);
			if (colorData[0] < BLACK_LINE) {
				Sound.beep();
				saveY = odo.getXYTD()[1];
				break;
			}
		}
		
		moveStraight(leftMotor, rightMotor, saveY, false, false); 	//move backward by y traveled
		turnRobot(leftMotor, rightMotor, RIGHT_ANGLE, true, false);	//turn right 90 degrees and straight
		moveStraight(leftMotor, rightMotor, MAX_DISTANCE, true, true); 	
		
		while(true) {
			colorSample.fetchSample(colorData, 0);
			if (colorData[0] < BLACK_LINE) {
				Sound.beep(); 
				break;
			}
		}
		// move distance of sensor-robotCenter
		moveStraight(leftMotor, rightMotor, LIGHTSENSOR_TO_ROBOTCENTER, true, false); 
		// turn left 90 degrees
		turnRobot(leftMotor, rightMotor, RIGHT_ANGLE, false, false); 	
		// move forward by saved y value + sensor-robotCenter
		moveStraight(leftMotor, rightMotor, (LIGHTSENSOR_TO_ROBOTCENTER + saveY), true, false); 
	}
	
	/**
	 * 
	 * @param leftMotor
	 * @param rightMotor
	 * @param distance : distance to travel
	 * @param forwards : if true then it goes forward direction
	 * @param continueRunning : if true then program does not wait for wheels to stop, false program waits  
	 */
	public void moveStraight(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, 
			double distance, boolean forwards, boolean continueRunning) {
		int i = 1;
		if (!forwards) i = -1;
		leftMotor.setSpeed(FORWARD_SPEED);
	    rightMotor.setSpeed(FORWARD_SPEED);
	    leftMotor.rotate(convertDistance(lab4.WHEEL_RAD, i * distance), true);
	    rightMotor.rotate(convertDistance(lab4.WHEEL_RAD, i *distance), continueRunning);
	}
	
	/**
	 * This method turns the robot to the right or left depending on direction boolean and turns the robot by the specified
	 * degrees amount.
	 * @param left : motor
	 * @param right : motor
	 * @param degrees : degrees to turn by
	 * @param direction : true means turn right, left otherwise
	 */
	public void turnRobot(EV3LargeRegulatedMotor left, EV3LargeRegulatedMotor right, 
			int degrees, boolean direction, boolean continueRunning) {
		int i = 1;
		if (!direction)
			i = -1;
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		leftMotor.rotate(i * convertAngle(lab4.WHEEL_RAD, lab4.TRACK, degrees), true);
		rightMotor.rotate(i * -convertAngle(lab4.WHEEL_RAD, lab4.TRACK, degrees), continueRunning);
	}
	
	public void stopMotors(EV3LargeRegulatedMotor left, EV3LargeRegulatedMotor right) {
		left.stop();
		right.stop();
		left.setAcceleration(1000);
		right.setAcceleration(1000);
	}
	
	/**
	 * This method allows the conversion of a distance to the total rotation of each wheel need to
	 * cover that distance.
	 * 
	 * @param radius
	 * @param distance
	 * @return
	 */
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

}
