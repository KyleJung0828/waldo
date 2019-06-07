package ca.ubc.cpsc210.waldo.util;

/** 
 * Represent a bounding box in terms of latitude and longitude
 * @author CPSC 210 Instructor
 *
 */

public class BoundingBox {
	private double north;
	private double south;
	private double east;
	private double west;

	/**
	 * Constructor
	 */
	public BoundingBox() {
		north = 90.0;
		south = -90.0;
		east = 180.0;
		west = -180.0;
	}

	
	/** 
	 * Constructor
	 * @param n North value
	 * @param s South value
	 * @param e East value
	 * @param w West value
	 */
	public BoundingBox(double n, double s, double e, double w) {
		north = n;
		south = s;
		east = e;
		west = w;
	}

	public double getNorth() {
		return north;
	}

	public double getSouth() {
		return south;
	}

	public double getEast() {
		return east;
	}

	public double getWest() {
		return west;
	}
}
