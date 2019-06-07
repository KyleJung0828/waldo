package ca.ubc.cpsc210.waldo.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A segment that forms part of a bus route
 * 
 * @author CPSC 210 Instructor
 */
public class Segment implements Iterable<LatLon> {
	
	// Points in the segment
	private List<LatLon> points;

	/**
	 * Constructor
	 */
	public Segment() {
		points = new ArrayList<LatLon>();
	}

	/**
	 * Add a point into the segment
	 * 
	 * @param pt The point to add
	 */
	public void addPoint(LatLon pt) {
		points.add(pt);
	}

	@Override
	public Iterator<LatLon> iterator() {
		return points.iterator();
	}
}
