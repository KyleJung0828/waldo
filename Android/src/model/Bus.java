package ca.ubc.cpsc210.waldo.model;

/**
 * Represents a particular bus on a particular route going in a particular direction
 * leaving a stop in a certain number of minutes
 * 
 * @author CPSC210 Instructor
 */

public class Bus {
	
	// The route this bus is currently serving
	private BusRoute route;
	// The direction the bus is currently travelling
	private String direction;
	// The stop the departure time is for
	private BusStop stop;
	// The time to departure from the given stop
	private int minutesToDeparture;

	/**
	 * Constructor
	 * @param route The route the bus is on
	 * @param direction The direction in which the bus is travelling
	 * @param stop The stop the bus is approaching
	 * @param minutesToDeparture The time at which the bus will next leave the stop
	 */
	public Bus(BusRoute route, String direction, BusStop stop, int minutesToDeparture) {
		this.route = route;
		this.direction = direction;
		this.stop = stop;
		this.minutesToDeparture = minutesToDeparture;
	}
	
	/**
	 * The direction the bus is travelling in
	 * @return The direction as a string from the set {EAST, WEST, NORTH, SOUTH}
	 */
	public String getDirection() {
		return direction;
	}
	
	/**
	 * The next stop the bus will arrive at
	 * @return The next stop
	 */
	public BusStop getStop() {
		return stop;
	}
	
	/**
	 * The minutes till the bus departs the stop
	 * @return The minutes to departure
	 */
	public int getMinutesToDeparture() {
		return minutesToDeparture;
	}
	
	/**
	 * The route this bus is currently serving
	 */
	public BusRoute getRoute() {
		return route;
	}

}
