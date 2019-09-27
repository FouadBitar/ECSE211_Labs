package ca.mcgill.ecse211.searcher;

/**
 * this class handles the color detection of the ring
 */
public class ColorDetector {

	/*
	 * Detect colors according to rgb input
	 * yellow 3 ->  7,8      5,6         0,1
     * blue   1 ->  1,2      4,5,6,7     2,3,4
     * orange 4 ->  3,4,5    1,2         0
     * green  2 ->  1,2,3    3,4,5,6,7   0,1 
	 */
	public static int detectColor(double[] rgb) {
		// yellow
		if(
				rgb[0] >= 6 && 
				rgb[0] <= 9 &&
				
				rgb[1] >= 4 && 
				rgb[1] <= 7 &&
				
				rgb[2] >= 0 && 
				rgb[2] <= 2
		)
			return 3;
		
		// blue
		if(
				rgb[0] >= 0 && 
				rgb[0] <= 3 &&
				
				rgb[1] >= 3 && 
				rgb[1] <= 8 &&
				
				rgb[2] >= 1 && 
				rgb[2] <= 5
		)
			return 1;
		
		// orange
		if(
				rgb[0] >= 2 && 
				rgb[0] <= 6 &&
				
				rgb[1] >= 0 && 
				rgb[1] <= 3 &&
				
				rgb[2] >= 0 && 
				rgb[2] <= 1
		)
			return 4;
		
		// green
		if(
				rgb[0] >= 0 && 
				rgb[0] <= 4 &&
				
				rgb[1] >= 2 && 
				rgb[1] <= 8 &&
				
				rgb[2] >= 0 && 
				rgb[2] <= 2
		)
			return 2;
		
		return 0;
	}
}
