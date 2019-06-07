package ca.ubc.cpsc210.waldo.util;

/**
 * A latitude/longitude coordinate pair
 * 
 * @author CPSC 210 Instructor
 */
public class LatLon {
	private double lat;
	private double lon;
	
	// We don't want to be strict on checking 'inbetweeness'. You can vary this constant to check strict betweeness
	// by setting it to 0 or allow some wiggle room.
	private static final double WIGGLE = .01;

	/**
	 * Constructor
	 * @param lat The latitude to remember, find a double in the given string
	 * @param lon The longitude to remember, find a double in the given string
	 */
	public LatLon(String lat, String lon) {
		this.lat = Double.parseDouble(lat);
		this.lon = Double.parseDouble(lon);
	}

	/** 
	 * Constructor
	 * @param lat The latitude to remember
	 * @param lon The longitude to remember
	 */
	public LatLon(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	/**
	 * Return the latitude
	 * @return Latitude
	 */
	public double getLatitude() {
		return lat;
	}

	/**
	 * Return the longitude
	 * @return The longitude
	 */
	public double getLongitude() {
		return lon;
	}
	
	/**
	 * Check if it is a valid lat/lon
	 */
	public boolean isIllegal() {
		if (lat < 0 || lat > 90 || lon < -180 || lon > 180)
			return true;
		return false;
	}
	
	/**
	 * Is the given pointOfInterest in between the other two points
	 * @param pointOfInterest The point to check
	 * @param point1 A point providing the bottom of a box
	 * @param point2 A point providing the top of a box
	 * @return true if the pointOfInterest is inbetween the given points, false otherwise
	 */
	public static boolean inbetween(LatLon pointOfInterest, LatLon point1, LatLon point2) {
		LatLon smallerLatitude = null;
		LatLon largerLatitude = null;
		LatLon smallerLongitude = null;
		LatLon largerLongitude = null;
		if (point1.getLatitude() < point2.getLatitude()) {
			smallerLatitude = point1;
			largerLatitude = point2;
		}
		else {
			smallerLatitude = point2;
			largerLatitude = point1;
		}
		if (point1.getLongitude() < point2.getLongitude()) {
			smallerLongitude = point1;
			largerLongitude = point2;
		}
		else {
			smallerLongitude = point2;
			largerLongitude = point1;
		}
		if ((pointOfInterest.getLatitude()-WIGGLE <= largerLatitude.getLatitude()) && 
				(pointOfInterest.getLatitude()+WIGGLE >= smallerLatitude.getLatitude()) &&
				(pointOfInterest.getLongitude()-WIGGLE <= largerLongitude.getLongitude()) && 
				(pointOfInterest.getLongitude()+WIGGLE >= smallerLongitude.getLongitude()))
						return true;
		return false;
				
	}

	@Override
	public String toString() {
		return lat + ", " + lon;
	}
	
	@Override
	public boolean equals(Object other) {
		LatLon otherAsLatLon;
		if (other instanceof LatLon) {
			otherAsLatLon = (LatLon) other;
			if (otherAsLatLon.lat == lat && otherAsLatLon.lon == lon)
				return true;
		}
		return false;
	}

	/**
	 * Determine the distance between two points
	 * @param point1 First point
	 * @param point2 Second point
	 * @return Distance
	 */
	public static double distanceBetweenTwoLatLon(LatLon point1, LatLon point2) {
		double d2r = Math.PI / 180;

		double dlong = (point2.getLongitude() - point1.getLongitude()) * d2r;
		double dlat = (point2.getLatitude() - point1.getLatitude()) * d2r;
		double a = Math.pow(Math.sin(dlat / 2.0), 2)
				+ Math.cos(point1.getLatitude() * d2r)
				* Math.cos(point2.getLatitude() * d2r)
				* Math.pow(Math.sin(dlong / 2.0), 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = 6367 * c;
		d = d * 1000;
		return d;
	}
}
