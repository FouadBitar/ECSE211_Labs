package nothing;



public class hello {

	
	public static final double SQUARE_SIZE = 30.48;
	public static void main(String args[]) {
		double[][] waypoints = createWayPoints(2, 2, 5, 5);
		int x = waypoints.length - 1;
		int i = 0;
		while(x > -1) {
			System.out.print(waypoints[i][0]);
			System.out.print(", ");
			System.out.println(waypoints[i][1]);
			i++;
			x--;
		}
	}
	
	public static double[][] createWayPoints(int lx, int ly, int ux, int uy) {
		int numberOfWaypoints = (ux - lx + 1) * (uy - ly + 1);
		double[][] waypoints = new double[numberOfWaypoints][2];
		boolean east = true;
		int j = 0;
		for(int y = ly; y < uy + 1; y++) {
			if (east) {
				for(int x = lx; x < ux + 1; x++, j++) {
					waypoints[j][0] = x - 0.5;
					waypoints[j][1] = y;
				}
				east = false;
			} else {
				for(int x = ux; x > lx - 1; x--, j++) {
					waypoints[j][0] = x + 0.5;
					waypoints[j][1] = y;
				}
				east = true;
			}
		}
		return waypoints;
	}
}
