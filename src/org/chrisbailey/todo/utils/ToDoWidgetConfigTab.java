package org.chrisbailey.todo.utils;

import org.chrisbailey.todo.R;
import org.chrisbailey.todo.activities.PreferencesActivity;
import org.chrisbailey.todo.activities.WidgetConfiguration;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class ToDoWidgetConfigTab extends TabActivity {
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.tabs);

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, WidgetConfiguration.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("config").setIndicator("Sizes",
	                      res.getDrawable(android.R.drawable.btn_star_big_off))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, PreferencesActivity.class);
	    spec = tabHost.newTabSpec("preferences").setIndicator("Prefs",
	                      res.getDrawable(android.R.drawable.btn_star_big_off))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(1);
	}
}
