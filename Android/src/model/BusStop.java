package ca.ubc.cpsc210.waldo.model;

import java.util.Set;

import ca.ubc.cpsc210.waldo.exceptions.IllegalBusStopException;
import ca.ubc.cpsc210.waldo.util.LatLon;

/**
 * Represents a bus stop.
 * 
 * @author CPSC 210 Instructor
 */
public class BusStop {

	// The number of the stop
	private int number;

	// The name of the stop
	private String name;

	// The latitude and longitude of the stop
	private LatLon latlon;

	// The routes that pass through this stop
	private Set<BusRoute> routes;
	
	// The description to display for a trip
	private String descriptionToDisplay;

	/**
	 * Constructor
	 * 
	 * @param number
	 *            The number of the stop
	 * @param name
	 *            The name of the stop
	 * @param latlon
	 *            The latitude and longitude of the stop
	 * @param routes
	 *            All routes that pass through this stop
	 */
	public BusStop(int number, String name, LatLon latlon, Set<BusRoute> routes)  {
		if ( number <= 0 || name == null || name.length() == 0 || latlon.isIllegal())
			throw new IllegalBusStopException("Incorrect data to construct a bus stop.");
		this.number = number;
		this.name = name;
		this.latlon = latlon;
		this.routes = routes;
		this.descriptionToDisplay = "";
	}
	
	/**
	 * Construct a stop with just a number
	 * @param number Just the number of the stop
	 */
	public BusStop(int number) {
		this.number = number;
	}

	/**
	 * Return the number of the stop
	 * @return The number of the stop
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Return the name of the stop
	 * @return The name of the stop
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the lat/lon of the stop
	 * @return The lat/lon of the stop
	 */
	public LatLon getLatLon() {
		return latlon;
	}

	/**
	 * Return the routes that pass through the stop
	 * @return The routes that pass through the stop
	 */
	public Set<BusRoute> getRoutes() {
		return routes;
	}
	
	/**
	 * Set this description for the stop
	 * @param description A description for the stop
	 */
	public void setDescriptionToDisplay(String description) {
		this.descriptionToDisplay = description;
	}
	
	/**
	 * Retrieve the description of the stop
	 * @return A description for the stop
	 */
	public String getDescriptionToDisplay() {
		return descriptionToDisplay;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof BusStop) {
			BusStop otherAsBusStop = (BusStop) other;
			return otherAsBusStop.number == number;
		}
		return false;
	}

	@Override
	public int hashCode() {
		Integer hashCodeValue = new Integer(number);
		return hashCodeValue.hashCode();
	}

	@Override
	public String toString() {
		return number + ": " + "<" + name + ">, " + latlon + ", "
				+ routesAsString();
	}

	/**
	 * Return routes as a string to help in debugging
	 * @return The routes as space separated string
	 */
	private String routesAsString() {
		String result = new String();
		for (BusRoute r : routes)
			result = result + " " + r.getRouteNumber();
		return result;
	}
}
