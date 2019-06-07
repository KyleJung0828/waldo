package ca.ubc.cpsc210.waldo.map;

import ca.ubc.cpsc210.waldo.R;
import ca.ubc.cpsc210.waldo.R.drawable;
import ca.ubc.cpsc210.waldo.R.id;
import ca.ubc.cpsc210.waldo.R.layout;
import ca.ubc.cpsc210.waldo.R.menu;
import ca.ubc.cpsc210.waldo.R.string;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Activity that controls the map display.
 * 
 * @author CPSC 210 Instructor
 */
public class MapDisplayActivity extends FragmentActivity {
	
	// The Android fragment representing the main map screen
	private MapDisplayFragment fragment;
	
	// Left slide menu
	private DrawerLayout sideDrawerMenu;
	private ListView sideDrawerMenuList;
	private ActionBarDrawerToggle sideDrawerToggle;
	private String[] menuItems = { "Find Waldos", "Clear Waldos", 
			"Settings" };

	/**
	 * onCreate is a constructor for the activity. It is called on startup.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.side_drawer_menu);

		fragment = (MapDisplayFragment) getSupportFragmentManager()
				.findFragmentById(R.id.content_frame);
		sideDrawerMenu = (DrawerLayout) findViewById(R.id.drawer_layout);
		sideDrawerMenuList = (ListView) findViewById(R.id.left_drawer);
		
		// Set the adapter for the list view
		sideDrawerMenuList.setAdapter(SideDrawerItemAdapter);

		// Set the list's click listener
		sideDrawerMenuList
				.setOnItemClickListener(new DrawerItemClickListener());

		sideDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		sideDrawerMenu, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer icon to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description */
		R.string.drawer_close /* "close drawer" description */) {
		};
		sideDrawerToggle.syncState();

		// Set the drawer toggle as the DrawerListener
		sideDrawerMenu.setDrawerListener(sideDrawerToggle);

		// Create the map fragment if needed
		if (fragment == null) {
			fragment = new MapDisplayFragment();

			getSupportFragmentManager().beginTransaction()
					.add(R.id.content_frame, fragment).commit();
		}

		// Provide easier access to the menu at the top of the screen
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayShowHomeEnabled(true);
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setSubtitle("Find your way to a chosen Waldo!");
		}
	}

	/**
	 * onOptionsItemSelected - called when a user selects an item from the options menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			sideDrawerMenu.openDrawer(Gravity.LEFT);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * onCreateOptionsMenu - initialize the content of the activity's standard options menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actionbar_menu, menu);
		return true;
	}

	/**
	 * onConfigurationChanged - called if configuration changed when activity running
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		sideDrawerToggle.onConfigurationChanged(newConfig);
	}

	/**
	 * This private class helps handle when an item is clicked in the drawer menu. A private
	 * class is used because only the MapDisplayActivity class needs the functionality.
	 */
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	/**
	 * selectItem - if a drawer item is called, swaps the main view.
	 */
	private void selectItem(int position) {
		switch (position) {
		case 0:
			// Find Waldo
			fragment.findWaldos();
			break;
		case 1:
			// Clear Waldos
			fragment.clearWaldos();
			break;
		case 2:
			// Settings
			Intent i = new Intent(this, UserPreferences.class);
			startActivityForResult(i, 1);
			break;
		}

		// Highlight the selected item and close the drawer
		sideDrawerMenuList.setItemChecked(position, true);
		sideDrawerMenu.closeDrawer(sideDrawerMenuList);
	}

	/**
	 * A private class for handling the side drawer
	 */
	private BaseAdapter SideDrawerItemAdapter = new BaseAdapter() {

		@Override
		public int getCount() {
			return menuItems.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View retval = LayoutInflater.from(parent.getContext()).inflate(
					R.layout.side_drawer_item, null);

			TextView title = (TextView) retval
					.findViewById(R.id.drawer_item_content);
			ImageView icon = (ImageView) retval
					.findViewById(R.id.drawer_item_icon);

			String menuItem = menuItems[position];
			title.setText(menuItem);

			switch (position) {
			case 0:
				icon.setImageResource(R.drawable.ic_map);
				break;
			case 1:
				icon.setImageResource(R.drawable.ic_upload);
				break;
			case 2:
				icon.setImageResource(R.drawable.ic_settings);
			}

			return retval;
		}

	};
}
