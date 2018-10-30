package ca.mcgill.ecse211.lab4;

import ca.mcgill.ecse211.odometer.*;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * This class is used to localize the angle of the robot using the Ultrasonic sensor
 *
 */
public class UltrasonicLocalizer implements UltrasonicController {
	
	private static final int THRESHOLD = 39;
	private static final int ERROR_MARGIN = 6;
  	private static final int ROTATE_SPEED = 40;
  	private static final int TURN_ANGLE = 360;
	private Odometer odo;
  	private EV3LargeRegulatedMotor leftMotor;
  	private EV3LargeRegulatedMotor rightMotor;
	
	/**
	 * Constructor
	 * @param leftMotor
	 * @param rightMotor
	 */
	public UltrasonicLocalizer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) throws OdometerExceptions {
		this.odo = Odometer.getOdometer();
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
	}
	
	
	/**
	 * This method localizes the robot using the falling edge procedure
	 */
	public void fallingEdge() {
		
	    // reset motors
	    for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) {
	      motor.stop();
	      motor.setAcceleration(1000);
	    }

	    // sleep 2 seconds
	    try {
	      Thread.sleep(2000);
	    } catch (InterruptedException e) {
	    }
		
		double x1 = 1.0, x2 = 1.0, y1 = 1.0, y2 = 1.0, d;
		double backWall = 360.0, leftWall = 360.0, dTheta;
		

		turnRobot(leftMotor, rightMotor, TURN_ANGLE, true);
		while(true) {
			d = odo.getD();
			if (d < THRESHOLD + ERROR_MARGIN) {
				x1 = odo.getXYTD()[2];
				while(true) {
					d = odo.getD();
					if (d < THRESHOLD - ERROR_MARGIN) {
						x2 = odo.getXYTD()[2];
						break;
					}
				}
				break;
			}
		}
	
		turnRobot(leftMotor, rightMotor, TURN_ANGLE, false);
		
	    // sleep 2 seconds
	    try {
	      Thread.sleep(2000);
	    } catch (InterruptedException e) {
	    }
		
		while(true) {
			d = odo.getD();
			if (d < THRESHOLD + ERROR_MARGIN) {
				y1 = odo.getXYTD()[2];
				while(true) {
					d = odo.getD();
					if (d < THRESHOLD - ERROR_MARGIN) {
						y2 = odo.getXYTD()[2];
						break;
					}
				}
				break;
			}
		}
		
		stopMotors(leftMotor, rightMotor);	//reset motors
		backWall = (x1+x2)/2.0;
		leftWall = (y1+y2)/2.0;
		dTheta = dThetaFallingEdge(backWall, leftWall);
		correctAngle(dTheta);
	}
	
	public void risingEdge() {
	    // reset motors
	    for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) {
	      motor.stop();
	      motor.setAcceleration(1000);
	    }

	    // sleep 2 seconds
	    try {
	      Thread.sleep(2000);
	    } catch (InterruptedException e) {
	    }
		
		double x1 = 1.0, x2 = 1.0, y1 = 1.0, y2 = 1.0, d;
		double backWall = 360.0, leftWall = 360.0, dTheta;
		

		turnRobot(leftMotor, rightMotor, TURN_ANGLE, false);
		while(true) {
			d = odo.getD();
			if (d > THRESHOLD - ERROR_MARGIN) {
				x1 = odo.getXYTD()[2];
				while(true) {
					d = odo.getD();
					if (d > THRESHOLD + ERROR_MARGIN) {
						x2 = odo.getXYTD()[2];
						break;
					}
				}
				break;
			}
		}
	
		turnRobot(leftMotor, rightMotor, TURN_ANGLE, true);
		
	    // sleep 2 seconds
	    try {
	      Thread.sleep(2000);
	    } catch (InterruptedException e) {
	    }
		
		while(true) {
			d = odo.getD();
			if (d > THRESHOLD - ERROR_MARGIN) {
				y1 = odo.getXYTD()[2];
				while(true) {
					d = odo.getD();
					if (d > THRESHOLD + ERROR_MARGIN) {
						y2 = odo.getXYTD()[2];
						break;
					}
				}
				break;
			}
		}
		
		stopMotors(leftMotor, rightMotor);	//reset motors
		backWall = (x1+x2)/2.0;
		leftWall = (y1+y2)/2.0;
		dTheta = dThetaRisingEdge(backWall, leftWall);
		correctAngle(dTheta);
	}


	/**
	 * This method turns the robot to the right or left depending on direction boolean and turns the robot by the specified
	 * degrees amount.
	 * @param left
	 * @param right
	 * @param degrees
	 * @param direction
	 */
	public void turnRobot(EV3LargeRegulatedMotor left, EV3LargeRegulatedMotor right, int degrees, boolean direction) {
		int i = 1;
		if (!direction)
			i = -1;
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		leftMotor.rotate(i * convertAngle(lab4.WHEEL_RAD, lab4.TRACK, degrees), true);
		rightMotor.rotate(i * -convertAngle(lab4.WHEEL_RAD, lab4.TRACK, degrees), true);
	}
	
	public void stopMotors(EV3LargeRegulatedMotor left, EV3LargeRegulatedMotor right) {
		left.stop();
		left.setAcceleration(1000);
		right.stop();
		right.setAcceleration(1000);
	}
	
	public double dThetaFallingEdge(double backWall, double leftWall) {
		return 225.0 - (backWall+leftWall)/2.0;
	}
	
	private double dThetaRisingEdge(double backWall, double leftWall) {
		return 45.0 - (backWall+leftWall)/2.0;
	}
	
	public void correctAngle(double dTheta) {
		double newTheta = (odo.getXYTD()[2] + dTheta) % 360;
		odo.setTheta(newTheta);
		int turnAngle = (int) (360.0 - (newTheta));
		turnRobot(leftMotor, rightMotor, turnAngle, true);
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

	@Override
	public void processUSData(int distance) {
		odo.setD(distance);
	}

	@Override
	public int readUSDistance() {
		// TODO Auto-generated method stub
		return 0;
	}
	

}
