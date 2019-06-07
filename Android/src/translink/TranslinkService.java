package ca.ubc.cpsc210.waldo.translink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import ca.ubc.cpsc210.waldo.exceptions.IllegalBusException;
import ca.ubc.cpsc210.waldo.exceptions.IllegalBusStopException;
import ca.ubc.cpsc210.waldo.exceptions.WaldoException;
import ca.ubc.cpsc210.waldo.model.Bus;
import ca.ubc.cpsc210.waldo.model.BusRoute;
import ca.ubc.cpsc210.waldo.model.BusStop;
import ca.ubc.cpsc210.waldo.model.Trip;
import ca.ubc.cpsc210.waldo.util.LatLon;

/**
 * A wrapper for Translink information
 * 
 * @author CPSC210 Instructor
 */

public class TranslinkService {

	/**
	 * Translink API key - must be included with any request for data from
	 * Translink service
	 */
	// CPSC 210 STUDENTS: Complete with your key from developer.translink.ca
	private final static String APIKEY = "WFLaEs0PGMO80sZ7TqEx"; 

	// Routes
	private Set<BusRoute> routes;

	// Stops
	private Set<BusStop> stops;

	/**
	 * Constructor
	 */
	public TranslinkService() {
		// Initialize the routes and stops
		routes = new HashSet<BusRoute>();
		stops = new HashSet<BusStop>();
	}

	/**
	 * Get data from Translink about stops around a given location
	 * 
	 * @param location
	 *            The latitude and longitude of the location of interest
	 * @param radius
	 *            How far from location to look for stops
	 */
	public Set<BusStop> getBusStopsAround(LatLon location, int radius) {
		
		// Format the request string
		DecimalFormat sixDecimalRounding = new DecimalFormat("#.######");
		StringBuilder urlBuilder = new StringBuilder(
				"//api.translink.ca/RTTIAPI/V1/");
		urlBuilder.append("stops?lat="
				+ sixDecimalRounding.format(location.getLatitude()) + "&");
		urlBuilder.append("long="
				+ sixDecimalRounding.format(location.getLongitude()) + "&");
		urlBuilder.append("radius=" + radius + "&");
		urlBuilder.append("apikey=" + APIKEY);
		
		// Make the request
		InputStream in = null;
		try {
			String s = makeJSONQuery(urlBuilder);
			return parseBusStopsAroundFromJSON(s);
		} catch (Exception e) {
			// Return an empty set of bus stops but let developer know
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException ioe) {
				throw new IllegalBusStopException(
						"getBusStopsAround: Unable to open or read return from http request.");
			}
		}
		// Something went wrong so return an empty set
		return new HashSet<BusStop>();
	}

	/**
	 * Parse the JSON returned for a bus stop query
	 * 
	 * @param input The returned string from Translink
	 * @return A set of bus stops parsed
	 */
	private Set<BusStop> parseBusStopsAroundFromJSON(String input) {
		
		// Initialize
		Set<BusStop> stopsFound = new HashSet<BusStop>();
		JSONArray obj;
			
		try {
			// Parse each bus stop
			obj = (JSONArray) new JSONTokener(input).nextValue();
			if (obj != null) {
				for (int i = 0; i < obj.length(); i++) {
					
					// Retrieve the stop number, name, lat and lon
					JSONObject stop = obj.getJSONObject(i);
					int stopNumber = stop.getInt("StopNo");
					String stopName = stop.getString("Name").trim();
					double lat = stop.getDouble("Latitude");
					double lon = stop.getDouble("Longitude");
					
					// Deal with the routes. Parse them out and form into routes using translateRoutes
					String routesAsString = stop.getString("Routes");
					List<String> routesAsListOfStrings = new ArrayList<String>();
					StringTokenizer st = new StringTokenizer(routesAsString,
							",");
					while (st.hasMoreTokens()) {
						routesAsListOfStrings.add(st.nextToken().trim());
					}
					Set<BusRoute> routes = translateRoutes(routesAsListOfStrings);
					addToRoutes(routes);
					
					// Create the bus stop and remember it
					BusStop busStop = new BusStop(stopNumber, stopName,
							new LatLon(lat, lon), routes);
					addBusStop(busStop);
					stopsFound.add(busStop);

				}
			}
		} catch (JSONException e) {
			// Let the developer know but just return whatever is in stopsFound. Probably there was an
			// error in the JSON returned.
			e.printStackTrace();
		}
		return stopsFound;
	}

	/**
	 * Get data from Translink about when buses are going to arrive at a given
	 * stop
	 * 
	 * @param stop
	 *            The stop of interest
	 * @param input
	 *            Where to get the Translink data from
	 */
	public void getBusEstimatesForStop(BusStop stop) throws IllegalBusException {

		// Format query string
		StringBuilder urlBuilder = new StringBuilder(
				"//api.translink.ca/RTTIAPI/V1/stops/");
		urlBuilder.append(stop.getNumber() + "/estimates?");
		urlBuilder.append("apikey=" + APIKEY);
		urlBuilder.append("&count=3");
		urlBuilder.append("&timeframe=60");
		InputStream in = null;
		try {
			String s = makeJSONQuery(urlBuilder);
			parseBusEstimatesFromJSON(stop, s);
		} catch (Exception e) {
			// Just let developer know we encountered an error or something
			e.printStackTrace();
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					// Oh well, couldn't free resource. Don't worry about it for now
				}
		}

	}

	/**
	 * Parse the bus estimate information from the JSON returned from Translink
	 * 
	 * @param stop The stop to remember the bus estimates for
	 * @param input The JSON to parse
	 */
	private void parseBusEstimatesFromJSON(BusStop stop, String input) {
		// Initialize
		JSONArray obj;
		try {
			obj = (JSONArray) (new JSONTokener(input).nextValue());
			if (obj != null) {
				
				// For every bus estimate
				for (int i = 0; i < obj.length(); i++) {
					JSONObject route = obj.getJSONObject(i);
					String routeNumber = route.getString("RouteNo");
					String direction = route.getString("Direction");
					String routeMapLocation = route.getString("RouteMap");
					// Clean up routeMapLocation. Look for http and unescape /
					int httpStart = routeMapLocation.indexOf("http");
					routeMapLocation = routeMapLocation.substring(httpStart,
							routeMapLocation.length() - 1);
					routeMapLocation = routeMapLocation.replace("\\", "");
					routeMapLocation = routeMapLocation.replace("\"", "");
					BusRoute busRoute = lookupRoute(routeNumber);

					busRoute.setRouteMapLocation(routeMapLocation);

					busRoute.clearBuses();
					JSONArray schedules = (JSONArray) route
							.getJSONArray("Schedules");
					for (int j = 0; j < schedules.length(); j++) {
						JSONObject schedule = schedules.getJSONObject(j);
						int expectedCountdown = schedule
								.getInt("ExpectedCountdown");
						boolean cancelledTrip = schedule
								.getBoolean("CancelledTrip");
						boolean cancelledStop = schedule.getBoolean("CancelledStop");
						if (!cancelledTrip && !cancelledStop) {
							Bus nextBus = new Bus(busRoute, direction, stop,
									expectedCountdown);
							busRoute.addBus(nextBus);
						}
					}
				}
			}

		} catch (JSONException e) {
			// Probably was an error returned. Just let developer know stacktrace
			e.printStackTrace();
		}

	}

	/**
	 * Execute a given query 
	 * 
	 * @param urlBuilder The query with everything but http:
	 * @return The JSON returned from the query 
	 */
	private String makeJSONQuery(StringBuilder urlBuilder) {
		try {
			URL url = new URL("http:" + urlBuilder.toString());
			HttpURLConnection client = (HttpURLConnection) url.openConnection();
			client.setRequestProperty("accept", "application/json");
			InputStream in = client.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String returnString = br.readLine();
			client.disconnect();
			return returnString;
		} catch (Exception e) {
			throw new WaldoException("Unable to make JSON query: " + urlBuilder.toString());
		}
	}

	/**
	 * Get route information from KMZ file at given URL
	 * 
	 * @throws IOException
	 *             when an exception occurs obtaining or parsing data from
	 *             Translink service
	 */
	public void parseKMZ(BusRoute route) {

		try {
			URL kmzURL = new URL(route.getRouteMapLocation());

			URLConnection conn = kmzURL.openConnection();
			InputStream is = conn.getInputStream();
			ZipInputStream zis = new ZipInputStream(is);
			zis.getNextEntry(); // assuming only one entry in zip file
			InputSource src = new InputSource(zis);

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser parser = spf.newSAXParser();

			XMLReader reader = parser.getXMLReader();

			KMLParser kmlParser = new KMLParser(route);
			reader.setContentHandler(kmlParser);
			reader.parse(src);

		} catch (Exception e) {
			e.printStackTrace();
			throw new WaldoException("Unable to parse KML.");
		}
	}

	/**
	 * Remember a bus stop
	 * 
	 * @param stop
	 *            The stop to remember
	 */
	public void addBusStop(BusStop stop) {
		if (stop == null)
			throw new IllegalBusStopException("No stop available to remember in TranslinkService.");
		stops.add(stop);
	}

	/**
	 * Find a route
	 * 
	 * @param number
	 *            The number of the route
	 * @return The route
	 */
	public BusRoute lookupRoute(String number) {
		for (BusRoute r : routes)
			if (r.getRouteNumber().equals(number))
				return r;
		return null;
	}

	/**
	 * Return all the bus stops
	 * 
	 * @return A set of all bus stops
	 */
	public Set<BusStop> getBusStops() {
		return stops;
	}

	/**
	 * Remember the given routes
	 * 
	 * @param routes
	 *            Some routes to remember
	 */
	public void addToRoutes(Set<BusRoute> routes) {
		if (routes != null)
			this.routes.addAll(routes);
	}

	/**
	 * Take a list of routes as strings and remember them as BusRoute objects
	 * 
	 * @param routesAsParsed
	 *            The routes separated by commas
	 * @return A set of BusRoute objects
	 */
	public Set<BusRoute> translateRoutes(List<String> routesAsParsed) {
		Set<BusRoute> routes = new HashSet<BusRoute>();

		if (routesAsParsed != null) {
			for (String routeNumberAsString : routesAsParsed) {
				BusRoute r = lookupRoute(routeNumberAsString);
				if (r != null)
					routes.add(r);
				else {
					r = new BusRoute(routeNumberAsString);
					routes.add(r);
				}
			}
		}
		return routes;
	}
	
	/**
	 * Forget all Translink data we have seen to date.
	 */
	public void clearModel() {
		routes = new HashSet<BusRoute>();
		stops = new HashSet<BusStop>();
	}
	

}
