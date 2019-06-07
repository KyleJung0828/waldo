package ca.ubc.cpsc210.waldo.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ca.ubc.cpsc210.waldo.util.BoundingBox;
import ca.ubc.cpsc210.waldo.util.Segment;

/**
 * Represents a bus route.
 * 
 * @author CPSC 210 Instructor
 */

public class BusRoute {

	// Each bus route has a number, which may start with a letter. We must store
	// it as a string.
	private String number;
	// The stops on the route
	private Set<BusStop> stops;
	// The buses on the route currently
	private Set<Bus> buses;
	// The route map location
	private String routeMapLocation;
	// Bounding box for route map
	private BoundingBox bounds;
	// The segments making up the route
	private List<Segment> segments;


	/**
	 * Constructor
	 * @param number
	 *            The number of the bus route
	 */
	public BusRoute(String number) {
		this.number = number;
		buses = new HashSet<Bus>();
		segments = new ArrayList<Segment>();
	}
	
	/**
	 * Remember a bus on this route
	 * @param nextBus A bus on the route
	 */
	public void addBus(Bus nextBus) {
		buses.add(nextBus);
	}
	
	/**
	 * Forget all buses on the route
	 */
	public void clearBuses() {
		buses = new HashSet<Bus>();	
	}
	
	/**
	 * Return the buses on the route
	 * @return The set of buses on the route
	 */
	public Set<Bus> getBuses() {
		return buses;
	}
	
	/**
	 * Return the number of this route
	 * @return The number of this route
	 */
	public String getRouteNumber() {
		return number;
	}
	
	/**
	 * Set the URL of the map for the route in KLM (compressed) format
	 * @param routeMapLocation URL for the route map
	 */
	public void setRouteMapLocation(String routeMapLocation) {
		this.routeMapLocation = routeMapLocation;
	}
	
	/**
	 * Return the URL of the map for the route in KLM (compressed) format
	 * @return The URL of the route map
	 */
	public String getRouteMapLocation() {
		return routeMapLocation;
	}
	
	/**
	 * Sets a bounding box for this bus route
	 * @param north  northern latitude of boundary
	 * @param south  southern latitude of boundary
	 * @param east   eastern longitude of boundary
	 * @param west   western longitude of boundary
	 */
	public void setBounds(String north, String south, String east, String west) {
		bounds = new BoundingBox(Double.parseDouble(north),
				Double.parseDouble(south), Double.parseDouble(east),
				Double.parseDouble(west));
	}
	
	/**
	 * Return The bounding box for the bus route
	 * @return The bounds of the route
	 */
	public BoundingBox getBounds() {
		return bounds;
	}

	/**
	 * Return a segment for the route 
	 * @param seg A series of points in a segment
	 */
	public void addSegment(Segment seg) {
		segments.add(seg);
	}

	/**
	 * Retrieve segments in a route
	 * @return Segments in a route
	 */
	public List<Segment> getSegments() {
		return segments;
	}


	@Override
	// Compare bus routes based on bus number
	public boolean equals(Object other) {
		if (other instanceof BusRoute) {
			BusRoute otherAsBusRoute = (BusRoute) other;
			return otherAsBusRoute.number.equals(number);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return number.hashCode();
	}

	@Override
	public String toString() {
		return "Route is <" + number + ">";
	}
}
