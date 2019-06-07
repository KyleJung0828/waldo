package ca.ubc.cpsc210.waldo.waldowebservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;
import ca.ubc.cpsc210.waldo.exceptions.IllegalBusStopException;
import ca.ubc.cpsc210.waldo.exceptions.WaldoException;
import ca.ubc.cpsc210.waldo.model.Bus;
import ca.ubc.cpsc210.waldo.model.BusRoute;
import ca.ubc.cpsc210.waldo.model.BusStop;
import ca.ubc.cpsc210.waldo.model.Waldo;
import ca.ubc.cpsc210.waldo.util.LatLon;

public class WaldoService {

	private final static String WALDO_WEB_SERVICE_URL = "http://kramer.nss.cs.ubc.ca:8080/";

	/**
	 * Constructor
	 */
	List<Waldo> listOfWaldos;
	String key;
	
	public WaldoService() {
		listOfWaldos = new ArrayList<Waldo>();
		key = "";
	}

	/**
	 * Initialize a session with the Waldo web service. The session can time out
	 * even while the app is active...
	 * 
	 * @param nameToUse
	 *            The name to go register, can be null if you want Waldo to
	 *            generate a name
	 * @return The name that Waldo gave you
	 * @throws JSONException 
	 */
	public String initSession(String nameToUse) {
		// CPSC 210 Students. You will need to complete this method
		StringBuilder urlBuilder = new StringBuilder("http://kramer.nss.cs.ubc.ca:8080/");
		if (nameToUse == null) {
			urlBuilder.append("initsession");
		} else {
			urlBuilder.append("initsession/" + nameToUse);
		}
    	String s = makeJSONQuery(urlBuilder);	
		try {
			JSONObject obj = (JSONObject) (new JSONTokener(s).nextValue());
			nameToUse = obj.getString("Name").trim();
			key = obj.getString("Key").trim();
			Log.i("whatever", nameToUse);
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		return nameToUse;
	}
	
	/**
	 * Get waldos from the Waldo web service.
	 * 
	 * @param numberToGenerate
	 *            The number of Waldos to try to retrieve
	 * @return Waldo objects based on information returned from the Waldo web
	 *         service
	 */
	public List<Waldo> getRandomWaldos(int numberToGenerate) {
		// CPSC 210 Students: You will need to complete this method
		StringBuilder urlBuilder = new StringBuilder(WALDO_WEB_SERVICE_URL);
		urlBuilder.append("getwaldos" + "/" + key + "/" + Integer.toString(numberToGenerate));
		String input = makeJSONQuery(urlBuilder);
		JSONArray obj;
		try {
			obj = (JSONArray) (new JSONTokener(input).nextValue());
			if (obj != null) {
				// For all Waldos generated
				for (int i = 0; i < obj.length(); i++) {
					JSONObject waldo = obj.getJSONObject(i);
					String waldoName = waldo.getString("Name").trim();
					JSONObject location = waldo.getJSONObject("Loc");
					double lat = location.getDouble("Lat");
					double lon = location.getDouble("Long");
					long date = location.getLong("Tstamp");
					LatLon lastLocation = new LatLon(Double.toString(lat), Double.toString(lon));
					Date lastUpdated = new Date(date);
					// Add new Waldos to the list
					Waldo w = new Waldo(waldoName, lastUpdated, lastLocation);
					listOfWaldos.add(w);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		return listOfWaldos;
	}

	/**
	 * Return the current list of Waldos that have been retrieved
	 * 
	 * @return The current Waldos
	 */
	public List<Waldo> getWaldos() {
		return listOfWaldos;
	}

	/**
	 * Retrieve messages available for the user from the Waldo web service
	 * 
	 * @return A list of messages
	 */
	

    public List<String> getMessages() {
    	// CPSC 210 Students: You will need to complete this method
    	StringBuilder urlBuilder = new StringBuilder("http://kramer.nss.cs.ubc.ca:8080/getmsgs/");
    	urlBuilder.append(key+"/");
    	String input = makeJSONQuery(urlBuilder);
    	JSONObject obj;
    	List<String> messages = new ArrayList<String>();
    	try {
    		obj = (JSONObject) (new JSONTokener(input).nextValue());
    		Log.i("messages", obj.toString());
    		JSONArray objArray = obj.getJSONArray("Messages");
    		if (objArray != null) {
    			// For all Waldos generated
    			for (int i = 0; i < objArray.length(); i++) {
    				JSONObject message = objArray.getJSONObject(i);
    				String msg = message.getString("Message").trim();
    				// Finds and adds all messages parsed
    				messages.add(msg);
    			}
    		}
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}
    	return messages;
    }


	
	private String makeJSONQuery(StringBuilder urlBuilder) {
		try {
			URL url = new URL(urlBuilder.toString());
			HttpURLConnection client = (HttpURLConnection) url.openConnection();
			client.setRequestProperty("accept", "application/json");
			InputStream in = client.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String returnString = br.readLine();
			client.disconnect();
			return returnString;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WaldoException("Unable to make JSON query: " + urlBuilder.toString());
		}
	}

	
}
