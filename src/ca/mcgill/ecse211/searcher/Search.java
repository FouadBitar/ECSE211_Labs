package ca.mcgill.ecse211.searcher;

import java.util.ArrayList;
import ca.mcgill.ecse211.lab5.lab5;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.sensors.DataController;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * This class uses Navigation to move to region
 * then search for the rings
 */
public class Search extends Thread {

	// class variables
	public static final int LL_X = 3;
	public static final int LL_Y = 3;
	public static final int UR_X = 7;
	public static final int UR_Y = 7;
	public static final int TR = 2; // 1-blue ; 2-green ; 3-yellow ; 4-orange
	public static final int SC = 0;

	private static int waypoints[][];
	
	private static final int RING_INBOUND = 6;
	private static final int LARGEST_RADIUS = 6;
	private static final int CENTER_TO_SENSOR = 6; //distance from center of wheels to ring light sensor,  
													//should be slightly higher than actual value
	private static final int US_TO_LIGHT = 5; //distance from us sensor to ring light sensor
	private static final int SLOW_SPEED = 40;
	private static final int ULTRA_SLOW_SPEED = 20;
	private static final int RING_WIDTH = 3;
	
	private Navigation navigator;
	private Odometer odo;
	private DataController dataCont;
  	private EV3LargeRegulatedMotor leftMotor;
  	private EV3LargeRegulatedMotor rightMotor;
	

	/**
	 * Constructor
	 */
	public Search(Navigation navigator, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) 
			throws OdometerExceptions {
		this.navigator = navigator;
		this.odo = Odometer.getOdometer();
		this.dataCont = DataController.getDataController();
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
	}
	
	
	@SuppressWarnings("unused")
	public void run() {
		//moveToSearchRegion();	
		
		//use sensors on both sides to correct for straight passing
		//	can turn on this thread only when correcting
		//	can either have correction thread running algorithm or correct every so and so
		
		waypoints = createWayPoints(LL_X, LL_Y, UR_X, UR_Y);	// create waypoints to travel to
		
		int[] nextXY = new int[2];
		double d;
		boolean isNotConsecutiveRing = true;
		
		//change starting values
		odo.setXYT(LL_X * lab5.SQUARE_SIZE, LL_Y * lab5.SQUARE_SIZE, 90);
		
		//move to next point
		for (int i = 1; i < waypoints.length; i++) {
			nextXY = waypoints[i];
			// Convert X & Y coordinates to actual length (cm)
			int x = (int) ((int) nextXY[0] * lab5.SQUARE_SIZE);
			int y = (int) ((int) nextXY[1] * lab5.SQUARE_SIZE);
			double theta = odo.getXYT()[2];
			if(theta < 180 && theta > 0) {
				x = x - 10;
			} else {
				x = x + 10;
			}
			navigator.travelToCoordinate(x, y); 
			while(true) {
				d = dataCont.getD();
				if (d < RING_INBOUND) { //ring for sure ahead
					detectRingColor();//detect color
					break;
				} else if (d > 13) {
					if (navigator.shimmy()) { //double check if there is ring
						detectRingColor();//get in position
					} else {
						break;//go to next point
					}
				}
			}
			
			
		}
		//if there is a ring detect and analyze, then avoid according to the position i.e.
		//		if this is not a second time in a row avoidance, then turn left and right 45 deg to see which has bigger object
		//			so that avoiding is easier with that route
		//		if the ring is at the end of a row then avoid using different technique to set up for next row
		//if there is another ring detect and analyze, then avoid again
	}
	



	/**
	 * this method is to stop the robot just before the 
	 * start of the largest ring to avoid detecting the floor
	 * @param xy target x and y coordinates as real values in cm
	 */
	private void getInPosition(double[] xy) {
		double d;
		slowDownMotors(leftMotor, rightMotor, SLOW_SPEED); //slow motors down
		while(true) {
			d = dataCont.getD();
			if(d < US_TO_LIGHT || isWithinRange(xy[0], xy[1])) { 
				stopMotors(leftMotor, rightMotor);
				break;
			}
		}
	}

	/** 
	 * @return ring color code
	 */
	private int detectRingColor() {
		navigator.turnRobot(leftMotor, rightMotor, 30, 30, true, true);
		ArrayList<Integer> samples = new ArrayList<Integer>();
		int colorCode = 0;
		while(leftMotor.isMoving()) {
			colorCode = ColorDetector.detectColor(dataCont.getRGB()); //gets sensor data and passes to colordetector class
			samples.add(colorCode);
		}
		navigator.turnRobot(leftMotor, rightMotor, 30, 30, false, true);
		colorCode = findMode(samples);
//		Display.objectDetected(colorCode);
		System.out.println("" + colorCode);
		return colorCode;
	}
	
	private int findMode(ArrayList<Integer> samples) {
		int [] data = new int[4];
		for(Integer sample : samples)
			data[sample-1]++;
		int mode = 0, temp = data[0];
		for(int i = 1; i < 4; i++){
			if(data[i] > temp){
				mode = i;
				temp = data[i];
			}
		}
		return mode + 1;
	}
	/**
	 * This method takes the waypoints the robot is moving to
	 * and checks to see if its current position of sensor is over
	 * the largest ring
	 * @param x
	 * @param y
	 */
	private boolean isWithinRange(double x, double y) {
		if(Math.abs(odo.getXYT()[0] - x) < (CENTER_TO_SENSOR + LARGEST_RADIUS) 
				|| Math.abs(odo.getXYT()[2] - y) < (CENTER_TO_SENSOR + LARGEST_RADIUS))
			return true;
		else 
			return false;
	}
	
	// this method is for avoiding objects
	// do standard avoidance by curving around ring touching
	// on the center of the tile you are crossing and a bit towards the ring
	// because you can slightly nudge it 
	public void avoidRing() {
		
	}
	
	private void slowDownMotors(EV3LargeRegulatedMotor leftMotor2, EV3LargeRegulatedMotor rightMotor2, int speed) {
		leftMotor.setSpeed(speed);
	    rightMotor.setSpeed(speed);
	}

	/**
	 * stops the motors 
	 */
	public void stopMotors(EV3LargeRegulatedMotor left, EV3LargeRegulatedMotor right) {
		left.flt();
		right.flt();
		left.setAcceleration(1000);
		right.setAcceleration(1000);
	}
	
	/**
	 * This method moves the robot to the start of the search region
	 */
	private void moveToSearchRegion() {
		navigator.travelToCoordinate(LL_X, LL_Y);
		while(!navigator.hasArrived(LL_X, LL_Y)) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {}
		}
		Sound.beep();
	}
	
	/**
	 * This method takes in the coordinates of search region and 
	 * creates the points the robot will travel to, in the shape of a zig zag
	 * so we loop through the points and go to each of them
	 * @param lx
	 * @param ly
	 * @param ux
	 * @param uy
	 */
	public static int[][] createWayPoints(int lx, int ly, int ux, int uy) {
		int numberOfWaypoints = (ux - lx + 1) * (uy - ly + 1);
		int[][] waypoints = new int[numberOfWaypoints][2];
		boolean east = true;
		int j = 0;
		for(int y = ly; y < uy + 1; y++) {
			if (east) {
				for(int x = lx; x < ux + 1; x++, j++) {
					waypoints[j][0] = x;
					waypoints[j][1] = y;
				}
				east = false;
			} else {
				for(int x = ux; x > lx - 1; x--, j++) {
					waypoints[j][0] = x;
					waypoints[j][1] = y;
				}
				east = true;
			}
		}
		return waypoints;
	}
	
	
}
