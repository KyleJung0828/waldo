package ca.ubc.cpsc210.waldo.model;

/**
 * Represents a trip that can be taken by a direct bus route
 * 
 * @author CPSC 210 Instructor
 */

public class Trip {
	
	// Invariant:
	// if walkingDistance is TRUE than startStop, endStop, direction and routeToUse are undefined
	// else startStop, endStop, direction and routeToUse are defined

	// The stop at the start of the trip
	private BusStop startStop;
	
	// The stop at the end of the trip
	private BusStop endStop;
	
	// The direction of the route to take
	private String direction;
	
	// The bus route to use
	private BusRoute routeToUse;
	
	// TRUE if a bus is not needed to get from start to end
	private boolean walkingDistance;

	/**
	 * Constructor
	 * @param start The starting stop of the trip, undefined if walkingDistance is true
	 * @param end The end stop of the trip, undefined if walkingDistance is true
	 * @param direction The direction as "SOUTH, NORTH, EAST, WEST" of the trip, undefined if walkingDistance is true
	 * @param route The bus route to use, undefined if walkingDistance is true
	 * @param walkingDistance The trip can be done by walking if true
	 */
	public Trip(BusStop start, BusStop end, String direction, BusRoute route,
			boolean walkingDistance) {
		if (!walkingDistance) {
			startStop = start;
			endStop = end;
			this.direction = direction;
			routeToUse = route;
			this.walkingDistance = false;
		} else
			this.walkingDistance = true;
	}
	
	/**
	 * Is the trip in walkingDistance?
	 * @return true if in walking distance, false otherwise
	 */
	public boolean inWalkingDistance() {
		return walkingDistance;
	}

	/**
	 * Retrieve the starting stop
	 * @return The starting stop (could be null if walkingDistance is true)
	 */
	public BusStop getStart() {
		return startStop;
	}

	/**
	 * Retrieve the end stop
	 * @return the end stop (could be null if walkingDistance is true)
	 */
	public BusStop getEnd() {
		return endStop;
	}

	/** 
	 * Retrieve the direction
	 * @return Returns NORTH, SOUTH, EAST or WEST (null if walkingDistance is true)
	 */
	public String getDirection() {
		return direction;
	}

	/**
	 * The route
	 * @return The route to use for the trip (null if walkingDistance is true)
	 */
	public BusRoute getRoute() {
		return routeToUse;
	}

}
