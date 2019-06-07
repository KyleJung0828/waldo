package ca.ubc.cpsc210.waldo.map;

import ca.ubc.cpsc210.waldo.R;
import ca.ubc.cpsc210.waldo.R.layout;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Deal with user preference settings like distance to stop.
 * 
 * @author CPSC 210 Instructor
 */

public class UserPreferences extends PreferenceActivity {
	
	/**
	 * onCreate - creates the preference setting. We use a deprecated method to work with older versions of Android.
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        addPreferencesFromResource(R.layout.settings);
 
    }
}