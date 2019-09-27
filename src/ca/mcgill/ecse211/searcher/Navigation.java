package ca.mcgill.ecse211.searcher;

import ca.mcgill.ecse211.lab5.lab5;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.sensors.DataController;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * This class is used to navigate the robot on the demo floor.
 */
public class Navigation {
	
	private static final int FORWARD_SPEED = 100;
  	private static final int ROTATE_SPEED = 150;
  	private static final double TILE_SIZE = 30.48;
  	private EV3LargeRegulatedMotor leftMotor;
  	private EV3LargeRegulatedMotor rightMotor;
  	
  	private Odometer odo;
  	private DataController dataCont;
  	private static boolean isNavigating;

    /**
     * Class constructor
     * 
     * @param leftMotor
     * @param rightMotor
     * @throws OdometerExceptions 
     */
    public Navigation(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) throws OdometerExceptions {
    	this.odo = Odometer.getOdometer();
    	this.dataCont = DataController.getDataController();
    	this.leftMotor = leftMotor;
    	this.rightMotor = rightMotor;
    }
    
    /**
     * This drives the robot to a specified point on the grid
     * 
     * @param x
     * @param y
     */
	public void travelTo(double x, double y) {
		isNavigating = false;
		
		while(!hasArrived(x, y)) {
		    double distance = dataCont.getD();
			
			if(distance < 20 && isNotABorderWall()) {
				isNavigating = false;
				avoidRing();
			} else if(!isNavigating){
				isNavigating = true;
				travelToCoordinate(x, y);
			}
		}
	}
	
    public boolean isNotABorderWall() {
		// TODO Auto-generated method stub
		return false;
	}


	public void avoidRing() {
		// TODO Auto-generated method stub
		
	}


	/**
     * This drives the robot to a way point
     * @param x
     * @param y
     */
	public void travelToCoordinate(double x, double y) {
		double dX = x - odo.getXYT()[0];
		double dY = y - odo.getXYT()[1];
		double theta = Math.atan(dX/dY); 
		if (dY < 0 && theta < Math.PI) theta += Math.PI; 
		double distance = Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2));

		turnTo(Math.toDegrees(theta));
		
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);

		leftMotor.rotate(convertDistance(lab5.WHEEL_RAD, distance), true);
		rightMotor.rotate(convertDistance(lab5.WHEEL_RAD, distance), false);
	}
	
    /**
     * This turns the robot to face an angle in regards to the x y axis
     * 
     * @param theta
     */
	public void turnTo(double theta) {
		double dTheta = theta - odo.getXYT()[2];
		if(dTheta < 0) dTheta += 360;
		// turn right
		if (dTheta > 180) {
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);
			leftMotor.rotate(-convertAngle(lab5.WHEEL_RAD, lab5.TRACK,360 - dTheta), true);
			rightMotor.rotate(convertAngle(lab5.WHEEL_RAD, lab5.TRACK, 360 - dTheta), false);
		}
		// turn left
		else { 
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);
			leftMotor.rotate(convertAngle(lab5.WHEEL_RAD, lab5.TRACK, dTheta), true);
			rightMotor.rotate(-convertAngle(lab5.WHEEL_RAD, lab5.TRACK, dTheta), false);
		}
	}
	
	
	
    /**
     * Checks if robot reached its destination
     */
	public boolean hasArrived(double xDest, double yDest) {
		double xCurr = odo.getXYT()[0];
		double yCurr = odo.getXYT()[1];
		double cErr = Math.hypot(xCurr - xDest, yCurr - yDest);
		return cErr < 4;
	}
	
	/**
	 * this method changes coordinates to actual in cm
	 * @param
	 */
	public double[] convertCoordinates(int[] xy) {
		double[] realXY = new double[2];
		realXY[0] = xy[0] * TILE_SIZE;
		realXY[1] = xy[1] * TILE_SIZE;
		return realXY;
	}
	
	/**
	 * 
	 * @param leftMotor
	 * @param rightMotor
	 * @param distance : distance to travel
	 * @param forwards : if true then it goes forward direction
	 * @param continueRunning : if true then program does not wait for wheels to stop, false program waits  
	 */
	public static void moveStraight(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, 
			double distance, int speed, boolean forwards, boolean continueRunning) {
		int i = 1;
		if (!forwards) i = -1;
		leftMotor.setSpeed(speed);
	    rightMotor.setSpeed(speed);
	    leftMotor.rotate(convertDistance(lab5.WHEEL_RAD, i * distance), true);
	    rightMotor.rotate(convertDistance(lab5.WHEEL_RAD, i *distance), continueRunning);
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
			int degrees, int speed, boolean direction, boolean continueRunning) {
		int i = 1;
		if (!direction)
			i = -1;
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);
		leftMotor.rotate(i * convertAngle(lab5.WHEEL_RAD, lab5.TRACK, degrees), true);
		rightMotor.rotate(i * -convertAngle(lab5.WHEEL_RAD, lab5.TRACK, degrees), continueRunning);
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
	
	public static boolean isNavigating() {
		return isNavigating;
	}

	public boolean shimmy() {
		boolean ringAhead = false;
		turnRobot(leftMotor, rightMotor, 10, 40, true, false);
		if (dataCont.getD() < 13) {
			turnRobot(leftMotor, rightMotor, -10, 40, false, false);
			return true;
		}
		turnRobot(leftMotor, rightMotor, -20, 40, false, false);
		if (dataCont.getD() < 13) {
			ringAhead = true;
			turnRobot(leftMotor, rightMotor, 10, 40, true, false);
			return ringAhead;
		}
		turnRobot(leftMotor, rightMotor, 10, 40, true, false);
		return ringAhead;
		
		
	}

}