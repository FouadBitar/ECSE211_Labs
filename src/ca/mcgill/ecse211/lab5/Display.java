package ca.mcgill.ecse211.lab5;

import ca.mcgill.ecse211.searcher.Search;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;

public class Display {
	
	private static final TextLCD lcd = LocalEV3.get().getTextLCD();
	
	/**
	 * This is the constructor
	 * 
	 */
	public Display() {
	}
	/**
	 * This method is used to display the color of the ring detected
	 */
	public static void objectDetected(int color) {
		if (color == Search.TR) {
			Sound.beep();
		} else {
			Sound.beep();
			Sound.beep();
		}
		lcd.clear();
	}
	
}
