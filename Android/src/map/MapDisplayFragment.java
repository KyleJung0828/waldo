package ca.ubc.cpsc210.waldo.map;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.SimpleLocationOverlay;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import ca.ubc.cpsc210.waldo.R;
import ca.ubc.cpsc210.waldo.model.Bus;
import ca.ubc.cpsc210.waldo.model.BusRoute;
import ca.ubc.cpsc210.waldo.model.BusStop;
import ca.ubc.cpsc210.waldo.model.Trip;
import ca.ubc.cpsc210.waldo.model.Waldo;
import ca.ubc.cpsc210.waldo.translink.TranslinkService;
import ca.ubc.cpsc210.waldo.util.LatLon;
import ca.ubc.cpsc210.waldo.util.Segment;
import ca.ubc.cpsc210.waldo.waldowebservice.WaldoService;
	
/**
 * Fragment holding the map in the UI.
 * 
 * @author CPSC 210 Instructor
 */
public class MapDisplayFragment extends Fragment {

	/**
	 * Log tag for LogCat messages
	 */
	private final static String LOG_TAG = "MapDisplayFragment";

	/**
	 * Location of some points in lat/lon for testing and for centering the map
	 */
	private final static GeoPoint ICICS = new GeoPoint(49.261182, -123.2488201);
	private final static GeoPoint CENTERMAP = ICICS;

	/**
	 * Preference manager to access user preferences
	 */
	private SharedPreferences sharedPreferences;

	/**
	 * View that shows the map
	 */
	private MapView mapView;

	/**
	 * Map controller for zooming in/out, centering
	 */
	private MapController mapController;

	// **************** Overlay fields **********************

	/**
	 * Overlay for the device user's current location.
	 */
	private SimpleLocationOverlay userLocationOverlay;

	/**
	 * Overlay for bus stop to board at
	 */
	private ItemizedIconOverlay<OverlayItem> busStopToBoardOverlay;

	/**
	 * Overlay for bus stop to disembark
	 */
	private ItemizedIconOverlay<OverlayItem> busStopToDisembarkOverlay;

	/**
	 * Overlay for Waldo
	 */
	private ItemizedIconOverlay<OverlayItem> waldosOverlay;

	/**
	 * Overlay for displaying bus routes
	 */
	private List<PathOverlay> routeOverlays;

	/**
	 * Selected bus stop on map
	 */
	private OverlayItem selectedStopOnMap;

	/**
	 * Bus selected by user
	 */
	private OverlayItem selectedBus;

	// ******************* Application-specific *****************

	/**
	 * Wraps Translink web service
	 */
	private TranslinkService translinkService;

	/**
	 * Wraps Waldo web service
	 */
	private WaldoService waldoService;

	/**
	 * Waldo selected by user
	 */
	private Waldo selectedWaldo;

	/*
	 * The name the user goes by
	 */
	private String userName;
	
	/**
	 * Instantiates LocationManager
	 */
	LocationManager locationManager;
	
	/**
	 * Instantiates LocationListener
	 */
	LocationListener locationListener;

	// ***************** Android hooks *********************

	/**
	 * Help initialize the state of the fragment
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);

		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());

		initializeWaldo();

		waldoService = new WaldoService();
		translinkService = new TranslinkService();
		routeOverlays = new ArrayList<PathOverlay>();
		
		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		locationListener = new SubLocationListener();
	}

	/**
	 * Initialize the Waldo web service
	 */
	private void initializeWaldo() {
		String s = null;
		new InitWaldo().execute(s);
	}

	/**
	 * Set up map view with overlays for buses, selected bus stop, bus route and
	 * current location.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (mapView == null) {
			mapView = new MapView(getActivity(), null);

			mapView.setTileSource(TileSourceFactory.MAPNIK);
			mapView.setClickable(true);
			mapView.setBuiltInZoomControls(true);

			mapController = mapView.getController();
			mapController.setZoom(mapView.getMaxZoomLevel() - 4);
			mapController.setCenter(CENTERMAP);

			userLocationOverlay = createLocationOverlay();
			busStopToBoardOverlay = createBusStopToBoardOverlay();
			busStopToDisembarkOverlay = createBusStopToDisembarkOverlay();
			waldosOverlay = createWaldosOverlay();

			// Order matters: overlays added later are displayed on top of
			// overlays added earlier.
			mapView.getOverlays().add(waldosOverlay);
			mapView.getOverlays().add(busStopToBoardOverlay);
			mapView.getOverlays().add(busStopToDisembarkOverlay);
			mapView.getOverlays().add(userLocationOverlay);
		}

		return mapView;
	}

	/**
	 * Helper to reset overlays
	 */
	private void resetOverlays() {
		OverlayManager om = mapView.getOverlayManager();
		om.clear();
		om.addAll(routeOverlays);
		om.add(busStopToBoardOverlay);
		om.add(busStopToDisembarkOverlay);
		om.add(userLocationOverlay);
		om.add(waldosOverlay);
	}

	/**
	 * Helper to clear overlays
	 */
	private void clearOverlays() {
		waldosOverlay.removeAllItems();
		clearAllOverlaysButWaldo();
		OverlayManager om = mapView.getOverlayManager();
		om.add(waldosOverlay);
	}

	/**
	 * Helper to clear overlays, but leave Waldo overlay untouched
	 */
	private void clearAllOverlaysButWaldo() {
		if (routeOverlays != null) {
			routeOverlays.clear();
			busStopToBoardOverlay.removeAllItems();
			busStopToDisembarkOverlay.removeAllItems();

			OverlayManager om = mapView.getOverlayManager();
			om.clear();
			om.addAll(routeOverlays);
			om.add(busStopToBoardOverlay);
			om.add(busStopToDisembarkOverlay);
			om.add(userLocationOverlay);
		}
	}

	/**
	 * When view is destroyed, remove map view from its parent so that it can be
	 * added again when view is re-created.
	 */
	@Override
	public void onDestroyView() {
		((ViewGroup) mapView.getParent()).removeView(mapView);
		super.onDestroyView();
	}

	/**
	 * Shut down the various services
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Update the overlay with user's current location. Request location
	 * updates.
	 */
	@Override
	public void onResume() {
		// CPSC 210 students, you'll need to handle parts of location updates
		// here...
		initializeWaldo();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		super.onResume();
	}

	/**
	 * Cancel location updates.
	 */
	@Override
	public void onPause() {
		// CPSC 210 students, you'll need to do some work with location updates
		// here...
		super.onPause();
		locationManager.removeUpdates(locationListener);
	}

	/**
	 * Update the marker for the user's location and repaint.
	 */
	public void updateLocation(Location location) {
		// CPSC 210 Students: Implement this method. mapView.invalidate is
		// needed to redraw
		// the map and should come at the end of the method.
		double lat = location.getLatitude();
		double lon = location.getLongitude();
		GeoPoint gp = new GeoPoint((int) (lat*1000000.00), (int) (lon*1000000.00));
		userLocationOverlay.setLocation(gp);
		mapView.invalidate();
	}

	/**
	 * Save map's zoom level and centre.
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mapView != null) {
			outState.putInt("zoomLevel", mapView.getZoomLevel());
			IGeoPoint cntr = mapView.getMapCenter();
			outState.putInt("latE6", cntr.getLatitudeE6());
			outState.putInt("lonE6", cntr.getLongitudeE6());
		}
	}

	/**
	 * Retrieve Waldos from the Waldo web service
	 */
	public void findWaldos() {
		clearOverlays();
		// Find out from the settings how many waldos to retrieve, default is 1
		String numberOfWaldosAsString = sharedPreferences.getString(
				"numberOfWaldos", "1");
		int numberOfWaldos = Integer.valueOf(numberOfWaldosAsString);
		new GetWaldoLocations().execute(numberOfWaldos);
		mapView.invalidate();
	}

	/**
	 * Clear waldos from view
	 */
	public void clearWaldos() {
		clearOverlays();
		mapView.invalidate();

	}

	// ******************** Overlay Creation ********************

	/**
	 * Create the overlay for bus stop to board at marker.
	 */
	private ItemizedIconOverlay<OverlayItem> createBusStopToBoardOverlay() {
		ResourceProxy rp = new DefaultResourceProxyImpl(getActivity());

		OnItemGestureListener<OverlayItem> gestureListener = new OnItemGestureListener<OverlayItem>() {

			/**
			 * Display bus stop description in dialog box when user taps stop.
			 * 
			 * @param index
			 *            index of item tapped
			 * @param oi
			 *            the OverlayItem that was tapped
			 * @return true to indicate that tap event has been handled
			 */
			@Override
			public boolean onItemSingleTapUp(int index, OverlayItem oi) {

				new AlertDialog.Builder(getActivity())
						.setPositiveButton(R.string.ok, new OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								if (selectedStopOnMap != null) {
									selectedStopOnMap.setMarker(getResources()
											.getDrawable(R.drawable.pin_blue));

									mapView.invalidate();
								}
							}
						}).setTitle(oi.getTitle()).setMessage(oi.getSnippet())
						.show();

				oi.setMarker(getResources().getDrawable(R.drawable.pin_blue));
				selectedStopOnMap = oi;
				mapView.invalidate();
				return true;
			}

			@Override
			public boolean onItemLongPress(int index, OverlayItem oi) {
				// do nothing
				return false;
			}
		};

		return new ItemizedIconOverlay<OverlayItem>(
				new ArrayList<OverlayItem>(), getResources().getDrawable(
						R.drawable.pin_blue), gestureListener, rp);
	}

	/**
	 * Create the overlay for bus stop to disembark at marker.
	 */
	private ItemizedIconOverlay<OverlayItem> createBusStopToDisembarkOverlay() {
		ResourceProxy rp = new DefaultResourceProxyImpl(getActivity());

		OnItemGestureListener<OverlayItem> gestureListener = new OnItemGestureListener<OverlayItem>() {

			/**
			 * Display bus stop description in dialog box when user taps stop.
			 * 
			 * @param index
			 *            index of item tapped
			 * @param oi
			 *            the OverlayItem that was tapped
			 * @return true to indicate that tap event has been handled
			 */
			@Override
			public boolean onItemSingleTapUp(int index, OverlayItem oi) {

				new AlertDialog.Builder(getActivity())
						.setPositiveButton(R.string.ok, new OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								if (selectedStopOnMap != null) {
									selectedStopOnMap.setMarker(getResources()
											.getDrawable(R.drawable.pin_blue));

									mapView.invalidate();
								}
							}
						}).setTitle(oi.getTitle()).setMessage(oi.getSnippet())
						.show();

				oi.setMarker(getResources().getDrawable(R.drawable.pin_blue));
				selectedStopOnMap = oi;
				mapView.invalidate();
				return true;
			}

			@Override
			public boolean onItemLongPress(int index, OverlayItem oi) {
				// do nothing
				return false;
			}
		};

		return new ItemizedIconOverlay<OverlayItem>(
				new ArrayList<OverlayItem>(), getResources().getDrawable(
						R.drawable.pin_blue), gestureListener, rp);
	}

	/**
	 * Create the overlay for Waldo markers.
	 */
	private ItemizedIconOverlay<OverlayItem> createWaldosOverlay() {
		ResourceProxy rp = new DefaultResourceProxyImpl(getActivity());
		OnItemGestureListener<OverlayItem> gestureListener = new OnItemGestureListener<OverlayItem>() {

			/**
			 * Display Waldo point description in dialog box when user taps
			 * icon.
			 * 
			 * @param index
			 *            index of item tapped
			 * @param oi
			 *            the OverlayItem that was tapped
			 * @return true to indicate that tap event has been handled
			 */
			@Override
			public boolean onItemSingleTapUp(int index, OverlayItem oi) {

				selectedWaldo = waldoService.getWaldos().get(index);
				Date lastSeen = selectedWaldo.getLastUpdated();
				SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
						"MMM dd, hh:mmaa", Locale.CANADA);

				new AlertDialog.Builder(getActivity())
						.setPositiveButton(R.string.get_route,
								new OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {

										// CPSC 210 STUDENTS. You must set
										// currCoord to
										// the user's current location.
										
										LatLon currCoord = new LatLon((locationManager
												.getLastKnownLocation(LocationManager.GPS_PROVIDER))
												.getLatitude(),
												(locationManager
														.getLastKnownLocation(LocationManager.GPS_PROVIDER)
														.getLongitude()));

										// CPSC 210 Students: Set currCoord...
										
										LatLon destCoord = selectedWaldo
												.getLastLocation();

										new GetRouteTask().execute(currCoord,
												destCoord);

									}
								})
						.setNegativeButton(R.string.ok, null)
						.setTitle(selectedWaldo.getName())
						.setMessage(
								"Last seen  " + dateTimeFormat.format(lastSeen))
						.show();

				mapView.invalidate();
				return true;
			}

			@Override
			public boolean onItemLongPress(int index, OverlayItem oi) {
				// do nothing
				return false;
			}
		};

		return new ItemizedIconOverlay<OverlayItem>(
				new ArrayList<OverlayItem>(), getResources().getDrawable(
						R.drawable.map_pin_thumb_blue), gestureListener, rp);
	}

	/**
	 * Create overlay for a bus route.
	 */
	private PathOverlay createPathOverlay() {
		PathOverlay po = new PathOverlay(Color.parseColor("#cf0c7f"),
				getActivity());
		Paint pathPaint = new Paint();
		pathPaint.setColor(Color.parseColor("#cf0c7f"));
		pathPaint.setStrokeWidth(4.0f);
		pathPaint.setStyle(Style.STROKE);
		po.setPaint(pathPaint);
		return po;
	}

	/**
	 * Create the overlay for the user's current location.
	 */
	private SimpleLocationOverlay createLocationOverlay() {
		ResourceProxy rp = new DefaultResourceProxyImpl(getActivity());

		return new SimpleLocationOverlay(getActivity(), rp) {
			@Override
			public boolean onLongPress(MotionEvent e, MapView mapView) {
				new GetMessagesFromWaldo().execute();
				return true;
			}

		};
	}

	/**
	 * Plot endpoints
	 */
	private void plotEndPoints(Trip trip) {
		GeoPoint pointStart = new GeoPoint(trip.getStart().getLatLon()
				.getLatitude(), trip.getStart().getLatLon().getLongitude());

		OverlayItem overlayItemStart = new OverlayItem(Integer.valueOf(
				trip.getStart().getNumber()).toString(), trip.getStart()
				.getDescriptionToDisplay(), pointStart);
		GeoPoint pointEnd = new GeoPoint(trip.getEnd().getLatLon()
				.getLatitude(), trip.getEnd().getLatLon().getLongitude());
		OverlayItem overlayItemEnd = new OverlayItem(Integer.valueOf(
				trip.getEnd().getNumber()).toString(), trip.getEnd()
				.getDescriptionToDisplay(), pointEnd);
		busStopToBoardOverlay.removeAllItems();
		busStopToDisembarkOverlay.removeAllItems();

		busStopToBoardOverlay.addItem(overlayItemStart);
		busStopToDisembarkOverlay.addItem(overlayItemEnd);
	}

	/**
	 * Plot bus route onto route overlays
	 * 
	 * @param rte
	 *            : the bus route
	 * @param start
	 *            : location where the trip starts
	 * @param end
	 *            : location where the trip ends
	 */
	private void plotRoute(Trip trip) {

		// Put up the end points
		plotEndPoints(trip);
		
		// CPSC 210 STUDENTS: Complete the implementation of this method
		List<Segment> segmentsOfRoute = trip.getRoute().getSegments();
		Log.i("segments", segmentsOfRoute.toString());
		for(Segment s : segmentsOfRoute) {
			PathOverlay po = createPathOverlay();
			Iterator<LatLon> itr = s.iterator();
			while(itr.hasNext()) {
				LatLon next = itr.next();
				if(LatLon.inbetween(next, trip.getStart().getLatLon(), trip.getEnd().getLatLon())) {
					Log.i("next", next.toString());
					po.addPoint(new GeoPoint(next.getLatitude(), next.getLongitude()));
					routeOverlays.add(po);
				}
			}
			Log.i("pathoverlays", po.toString());
		}
		
		translinkService.getBusEstimatesForStop(trip.getStart());
		int time = -1;
		
		for (Bus b : trip.getRoute().getBuses()) {
			if (b.getMinutesToDeparture() < time || time < 0) {
				time = b.getMinutesToDeparture(); 
				}
			trip.getStart().setDescriptionToDisplay("Get on the " + trip.getRoute().getRouteNumber() + "at \n" + trip.getStart().getName() + "\n" + " it's leaving in " + time + " minutes");
			trip.getEnd().setDescriptionToDisplay("Disembark at: " + trip.getEnd().getName());
		}
		// This should be the last method call in this method to redraw the map
		Log.i("overlays", routeOverlays.toString());
		mapView.getOverlays().addAll(routeOverlays);
		plotEndPoints(trip);
		mapView.invalidate();	
	}

	/**
	 * Plot a Waldo point on the specified overlay.
	 */
	private void plotWaldos(List<Waldo> waldos) {
		// CPSC 210 STUDENTS: Complete the implementation of this method
		waldosOverlay.removeAllItems();
		for (Waldo w : waldos) {
			String name = w.getName();
			GeoPoint waldoGeoPoint = new GeoPoint(((int) ((w.getLastLocation().getLatitude())*1000000.00)), 
					((int) ((w.getLastLocation().getLongitude())*1000000.00)));
			OverlayItem overlayWaldo = new OverlayItem(name, "", waldoGeoPoint);
			waldosOverlay.addItem(overlayWaldo);
		}
		// This should be the last method call in this method to redraw the map
		mapView.invalidate();
	}

	/**
	 * Helper to create simple alert dialog to display message
	 * 
	 * @param msg
	 *            message to display in alert dialog
	 * @return the alert dialog
	 */
	private AlertDialog createSimpleDialog(String msg) {
		AlertDialog.Builder dialogBldr = new AlertDialog.Builder(getActivity());
		dialogBldr.setMessage(msg);
		dialogBldr.setNeutralButton(R.string.ok, null);
		return dialogBldr.create();
	}

	/**
	 * Asynchronous task to get a route between two endpoints. Displays progress
	 * dialog while running in background.
	 */
	private class GetRouteTask extends AsyncTask<LatLon, Void, Trip> {
		private ProgressDialog dialog = new ProgressDialog(getActivity());
		private LatLon startPoint;
		private LatLon endPoint;

		@Override
		protected void onPreExecute() {
			translinkService.clearModel();
			dialog.setMessage("Retrieving route...");
			dialog.show();
		}

		@Override
		protected Trip doInBackground(LatLon... routeEndPoints) {

			// THe start and end point for the route
			startPoint = routeEndPoints[0];
			endPoint = routeEndPoints[1];
			
			// CPSC 210 Students: Complete this method. It must return a trip.

			Trip trip;
			
			startPoint = new LatLon((locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER))
					.getLatitude(),
					(locationManager
							.getLastKnownLocation(LocationManager.GPS_PROVIDER)
							.getLongitude()));
			
			Log.i("startPoint LatLon is: ", startPoint.toString());
			
			String distance = sharedPreferences.getString("stopDistance", "500");
			Set<BusStop> busStopsNearStartPoint = translinkService.getBusStopsAround(startPoint, Integer.parseInt(distance));
			Set<BusStop> busStopsNearEndPoint = translinkService.getBusStopsAround(endPoint, Integer.parseInt(distance));
			
			// LogCat debug msg 1
			Log.i("parsing buses", "bus stops at the beginning:" 
			+ busStopsNearStartPoint.toString() 
			+ "and bus stops near the end" 
			+ busStopsNearEndPoint.toString());
			//
			
			// Create set of intersecting bus tops
			Set<BusStop> intersectingBusStops = new HashSet<BusStop>();
			
			// Create set of same routes
			Set<BusRoute> routesOfIntersectingBusStops = new HashSet<BusRoute>();
			
			// Create sets of BusRoute for all bus stops parsed
			Set<BusRoute> busRoutesParsedAtStart = new HashSet<BusRoute>();
			Set<BusRoute> busRoutesParsedAtEnd = new HashSet<BusRoute>();
			
			for (BusStop stop : busStopsNearStartPoint) {
				for (BusRoute route : stop.getRoutes()) {
					busRoutesParsedAtStart.add(route);
				}
			}
			
			Log.i("busRoutesParsedAtStart is: ", busRoutesParsedAtStart.toString());
			
			for (BusStop stop : busStopsNearEndPoint) {
				for (BusRoute route : stop.getRoutes()) {
					busRoutesParsedAtEnd.add(route);
				}
			}
			
			Log.i("busRoutesParsedAtEnd is: ", busRoutesParsedAtEnd.toString());
				
			// Find all the bus stops that are within the radius
			Set<BusStop> stopsWithinWalkingDistance = new HashSet<BusStop>();
			
			for (BusStop stop : busStopsNearStartPoint) {
				if (busStopsNearEndPoint.contains(stop)){
				stopsWithinWalkingDistance.add(stop);	
				}
			}
			
			// Logging stopsWithinWalkingDistance
			Log.i("stopsWithinWalkingDistance is: ", stopsWithinWalkingDistance.toString());
			
			// Find all routes that are the same
			for (BusRoute route : busRoutesParsedAtStart) {
				if (busRoutesParsedAtEnd.contains(route)) {
					routesOfIntersectingBusStops.add(route);
				}
			}
			
			// Add all bus stops with the route numbers we are using near the start point
			for (BusStop stop : busStopsNearStartPoint) {
				for (BusRoute route : stop.getRoutes()) {
					if (routesOfIntersectingBusStops.contains(route)) {
						intersectingBusStops.add(stop);
					}
				}
			}
			
			// Add all bus stops with the route numbers we are using near the end point
			for (BusStop stop : busStopsNearEndPoint) {
				for (BusRoute route : stop.getRoutes()) {
					if (routesOfIntersectingBusStops.contains(route)) {
						intersectingBusStops.add(stop);
					}
				}
			}
			
			Log.i("routesOfIntersectingBusStops is: ", routesOfIntersectingBusStops.toString());
			Log.i("intersectingBusStops sorted is: ", intersectingBusStops.toString());
			
			if (stopsWithinWalkingDistance.size() > 0) {
				if (LatLon.distanceBetweenTwoLatLon(startPoint, endPoint) <= (double) Integer.parseInt(distance)) {
					trip = new Trip(null, null, null, null, true); 
					Log.i("Log message", "we didn't get there! #1");
					return trip;
				} else {
					trip = null;
					Log.i("Log message", "we didn't get there! #2, trip is null");
					return trip;
				}
			} else {
				Log.i("Log message", "we got here!");
				Set<BusStop> stopsToUseNearStart = new HashSet<BusStop>();
				Set<BusStop> stopsToUseNearEnd = new HashSet<BusStop>();
				
				Set<BusRoute> sameRoutes = new HashSet<BusRoute>(routesOfIntersectingBusStops);
				
				for (BusRoute route : sameRoutes) {
					for (BusStop busStop : busStopsNearStartPoint) {
						if (busStop.getRoutes().contains(route)) {
							stopsToUseNearStart.add(busStop);
						}
					}
				}
				
				// Log the stops we parsed to use near the start point
				Log.i("stopsToUseNearStart is: ", stopsToUseNearStart.toString());
				
				for (BusRoute route : sameRoutes) {
					for (BusStop busStop : busStopsNearEndPoint) {
						if (busStop.getRoutes().contains(route)) {
							stopsToUseNearEnd.add(busStop);
						}
					}
				}
				
				// Log the stops we parsed to use near the end point
				Log.i("stopsToUseNearEnd is: ", stopsToUseNearEnd.toString());
				
				String routingType = sharedPreferences.getString("routingOptions", "closest_stop_me");
				
				BusStop stopClosestToStart = null;
				BusStop stopClosestToEnd = null;
				
				String directionToGoIn = null;
				Set<Bus> busesInRightDirection = new HashSet<Bus>();
				List<String> ourDirections = findDirections(startPoint, endPoint);
				
				Log.i("ourDirections is: ", ourDirections.toString());
				
				for (BusStop stop : stopsToUseNearStart) {
					translinkService.getBusEstimatesForStop(stop);
					for (BusRoute route : stop.getRoutes()) {
						Log.i("**Log Message Debug #1: ", route.getBuses().toString());
						for (Bus bus : route.getBuses()) {
							Log.i("**Log Message Debug #2: ", "we got to the second degree nested for!");
							if (ourDirections.contains(bus.getDirection().trim())) {
								Log.i("Log Message Debug in BusStop stopsToUseNearStart for loop: ", "...");
								directionToGoIn = bus.getDirection();
								Bus goodBus = new Bus(bus.getRoute(), bus.getDirection(), bus.getStop(), bus.getMinutesToDeparture());
								busesInRightDirection.add(goodBus);
							}
						}
					}
				}
				
				// Finding which stop to use, either closest to start or closest to end
				Bus busClosestToStart = null;
				Bus busClosestToEnd = null;
				List<Double> distancesFromStart = new ArrayList<Double>();
				List<Double> distancesFromEnd = new ArrayList<Double>();
				
				for (Bus bus : busesInRightDirection) {
					double dist = LatLon.distanceBetweenTwoLatLon(bus.getStop().getLatLon(), startPoint);
					if (!distancesFromStart.contains(dist)) {
						distancesFromStart.add(dist);
					}
				}
				
				// Finding the bus closest to the start
				Collections.sort(distancesFromStart);
				for (Bus bus : busesInRightDirection) {
					double dist = LatLon.distanceBetweenTwoLatLon(bus.getStop().getLatLon(), startPoint);
					if (dist == distancesFromStart.get(0)) {
						busClosestToStart = bus;
					}
				}
				
				for (Bus bus : busesInRightDirection) {
					double dist = LatLon.distanceBetweenTwoLatLon(bus.getStop().getLatLon(), endPoint);
					if (!distancesFromEnd.contains(dist)) {
						distancesFromEnd.add(dist);
					}
				}
				
				// Finding the bus closest to the end
				Collections.sort(distancesFromEnd);
				for (Bus bus : busesInRightDirection) {
					double dist = LatLon.distanceBetweenTwoLatLon(bus.getStop().getLatLon(), endPoint);
					if (dist == distancesFromEnd.get(0)) {
						busClosestToEnd = bus;
					}
				}
				
				Log.i("the buses in the right direction are: ", busesInRightDirection.toString());
				
				if (routingType.equals("closest_stop_me")) {
					
					if ((busClosestToStart != null) && (busClosestToEnd != null)) {
					
						BusRoute routeClosestToMe = busClosestToStart.getRoute();
					
						// Log for the selected route closest to me
						Log.i("routeClosestToMe: ", routeClosestToMe.toString());
						
						for (BusStop stop : stopsToUseNearStart) {
							for (BusRoute route : stop.getRoutes()) {
								if (routeClosestToMe.equals(route)) {
									stopClosestToStart = stop;
								}
							}
						}
					
						for (BusStop stop : stopsToUseNearEnd) {
							for (BusRoute route : stop.getRoutes()) {
								if (routeClosestToMe.equals(route)) {
									stopClosestToEnd = stop;
								}
							}
						}
						
						routeClosestToMe.setRouteMapLocation("http://nb.translink.ca/geodata/" 
								+ routeClosestToMe.getRouteNumber() 
								+ ".kmz");
						translinkService.parseKMZ(routeClosestToMe);
					
						trip = new Trip(stopClosestToStart, stopClosestToEnd, directionToGoIn, routeClosestToMe, false);
						return trip;
						
					} else {
						Log.i("trip is null", "");
						trip = null;
						return trip;
					}
					
				} else if (routingType.equals("closest_stop_dest")) { 
					
					if ((busClosestToStart != null) && (busClosestToEnd != null)) {
					
						BusRoute routeClosestToWaldo = busClosestToEnd.getRoute();
					
						// Log for the selected route closest to Waldo
						Log.i("routeClosestToWaldo: ", routeClosestToWaldo.toString());
						
						for (BusStop stop : stopsToUseNearStart) {
							for (BusRoute route : stop.getRoutes()) {
								if (routeClosestToWaldo.equals(route)) {
									stopClosestToStart = stop;
								}
							}
						}
					
						for (BusStop stop : stopsToUseNearEnd) {
							for (BusRoute route : stop.getRoutes()) {
								if (routeClosestToWaldo.equals(route)) {
									stopClosestToEnd = stop;
								}
							}
						}
					
						routeClosestToWaldo.setRouteMapLocation("http://nb.translink.ca/geodata/" 
								+ routeClosestToWaldo.getRouteNumber() 
								+ ".kmz");
						translinkService.parseKMZ(routeClosestToWaldo);
						
						trip = new Trip(stopClosestToStart, stopClosestToEnd, directionToGoIn, routeClosestToWaldo, false);
						return trip;
					
					} else {
						Log.i("trip is null", "");
						trip = null;
						return trip;
					}
				
				} else {
					Log.i("trip is null", "");
					trip = null;
					return trip;
				}
			}
		}
		
		public List<String> findDirections(LatLon start, LatLon end) {
			List<String> directions = new ArrayList<String>();
			if (start.getLongitude() < end.getLongitude()) {
				String eastWest = "EAST";
				directions.add(eastWest);
			} else if (start.getLongitude() >= end.getLongitude()) {
				String eastWest = "WEST";
				directions.add(eastWest);
			} if (start.getLatitude() < end.getLatitude()) {
				String northSouth = "NORTH";
				directions.add(northSouth);
			} else if (start.getLatitude() >= end.getLatitude()) {
				String northSouth = "SOUTH";
				directions.add(northSouth);
			}
			return directions;
		}
		
		@Override
		protected void onPostExecute(Trip trip) {
			dialog.dismiss();

			if (trip != null && !trip.inWalkingDistance()) {
				// Remove previous start/end stops
				busStopToBoardOverlay.removeAllItems();
				busStopToDisembarkOverlay.removeAllItems();

				// Removes all but the selected Waldo
				waldosOverlay.removeAllItems();
				List<Waldo> waldos = new ArrayList<Waldo>();
				waldos.add(selectedWaldo);
				plotWaldos(waldos);

				// Plot the route
				plotRoute(trip);

				// Move map to the starting location
				LatLon startPointLatLon = trip.getStart().getLatLon();
				mapController.setCenter(new GeoPoint(startPointLatLon
						.getLatitude(), startPointLatLon.getLongitude()));
				mapView.invalidate();
			} else if (trip != null && trip.inWalkingDistance()) {
				AlertDialog dialog = createSimpleDialog("You are in walking distance!");
				dialog.show();
			} else {
				AlertDialog dialog = createSimpleDialog("Unable to retrieve bus location info...");
				dialog.show();
			}
		}
	}

	/**
	 * Asynchronous task to initialize or re-initialize access to the Waldo web
	 * service.
	 */
	private class InitWaldo extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... arg0) {

			// Initialize the service passing the name of the Waldo to use. If
			// you have
			// passed an argument to this task, then it will be used as the
			// name, otherwise
			// nameToUse will be null
			String nameToUse = arg0[0];
			userName = waldoService.initSession(nameToUse);

			return null;
		}

	}

	/**
	 * Asynchronous task to get Waldo points from Waldo web service. Displays
	 * progress dialog while running in background.
	 */
	private class GetWaldoLocations extends
			AsyncTask<Integer, Void, List<Waldo>> {
		private ProgressDialog dialog = new ProgressDialog(getActivity());

		@Override
		protected void onPreExecute() {
			dialog.setMessage("Retrieving locations of waldos...");
			dialog.show();
		}

		@Override
		protected List<Waldo> doInBackground(Integer... i) {
			Integer numberOfWaldos = i[0];
			return waldoService.getRandomWaldos(numberOfWaldos);
		}

		@Override
		protected void onPostExecute(List<Waldo> waldos) {
			dialog.dismiss();
			if (waldos != null) {
				plotWaldos(waldos);
			}
		}
	}

	/**
	 * Asynchronous task to get messages from Waldo web service. Displays
	 * progress dialog while running in background.
	 */
	private class GetMessagesFromWaldo extends
			AsyncTask<Void, Void, List<String>> {

		private ProgressDialog dialog = new ProgressDialog(getActivity());

		@Override
		protected void onPreExecute() {
			dialog.setMessage("Retrieving messages...");
			dialog.show();
		}

		@Override
		protected List<String> doInBackground(Void... params) {
			return waldoService.getMessages();
		}

		@Override
	    protected void onPostExecute(List<String> messages) {
			// CPSC 210 Students: Complete this method
			dialog.dismiss();
			StringBuffer stringBuffer = new StringBuffer();
			if(messages == null || messages.isEmpty()) {
				stringBuffer.append("No messages!");
			} else {
				for(String s : messages) {
					stringBuffer.append("Your messages:\n" + s + " "+ "\n");
				}
			}
			AlertDialog alertDialog = createSimpleDialog(stringBuffer.toString());
			alertDialog.show();
		}
		
	}	

	private class SubLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location arg0) {
		updateLocation(arg0);
		}

		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
		
		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			
		}
	
		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
		}
	}
}