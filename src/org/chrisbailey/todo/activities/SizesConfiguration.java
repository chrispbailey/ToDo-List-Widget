/*******************************************************************************
 * ToDo List Widget - Android homescreen note taking application
 * Copyright (C) 2011  Chris Bailey
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.chrisbailey.todo.activities;

import java.util.ArrayList;
import java.util.List;

import org.chrisbailey.todo.LargeToDoWidget;
import org.chrisbailey.todo.MediumToDoWidget;
import org.chrisbailey.todo.R;
import org.chrisbailey.todo.SmallToDoWidget;
import org.chrisbailey.todo.ToDoWidget1x1;
import org.chrisbailey.todo.ToDoWidget1x2;
import org.chrisbailey.todo.ToDoWidget1x3;
import org.chrisbailey.todo.ToDoWidget1x4;
import org.chrisbailey.todo.ToDoWidget3x1;
import org.chrisbailey.todo.ToDoWidget3x2;
import org.chrisbailey.todo.ToDoWidget3x3;
import org.chrisbailey.todo.ToDoWidget3x4;
import org.chrisbailey.todo.ToDoWidget4x1;
import org.chrisbailey.todo.ToDoWidget4x2;
import org.chrisbailey.todo.ToDoWidget4x3;
import org.chrisbailey.todo.ToDoWidget4x4;
import org.chrisbailey.todo.ToDoWidgetProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class SizesConfiguration extends Activity implements View.OnClickListener {
	public static final String LOG_TAG = "WidgetConfiguration";
	public  int ID = 0;
	ImageView saveButton;
	ImageView cancelButton;
	PackageManager pm;
	
	ArrayList <Widget> widgetList = new ArrayList<Widget>();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.sizes_configuration);
        
        pm = this.getPackageManager(); 

        LinearLayout layout = (LinearLayout)findViewById(R.id.widgetListLayout);

        // add our widgets dynamically
        addWidget(layout, ToDoWidget1x1.class, R.string.app_name_1x1);
        addWidget(layout, ToDoWidget1x2.class, R.string.app_name_1x2);
        addWidget(layout, ToDoWidget1x3.class, R.string.app_name_1x3);
        addWidget(layout, ToDoWidget1x4.class, R.string.app_name_1x4);
        addWidget(layout, SmallToDoWidget.class, R.string.app_name_2x1);
        addWidget(layout, ToDoWidgetProvider.class, R.string.app_name_2x2);
        addWidget(layout, MediumToDoWidget.class, R.string.app_name_2x3);
        addWidget(layout, LargeToDoWidget.class, R.string.app_name_2x4);
        addWidget(layout, ToDoWidget3x1.class, R.string.app_name_3x1);
        addWidget(layout, ToDoWidget3x2.class, R.string.app_name_3x2);
        addWidget(layout, ToDoWidget3x3.class, R.string.app_name_3x3);
        addWidget(layout, ToDoWidget3x4.class, R.string.app_name_3x4);
        addWidget(layout, ToDoWidget4x1.class, R.string.app_name_4x1);
        addWidget(layout, ToDoWidget4x2.class, R.string.app_name_4x2);
        addWidget(layout, ToDoWidget4x3.class, R.string.app_name_4x3);
        addWidget(layout, ToDoWidget4x4.class, R.string.app_name_4x4);

        // add click listener to save button
        
        saveButton = (ImageView)findViewById(R.id.config_save_button);
        saveButton.setOnClickListener(this);
        saveButton.setId(++ID);
        
        cancelButton = (ImageView)findViewById(R.id.config_cancel_button);
        cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
    }
    
    private void addWidget(LinearLayout layout, Class<? extends AppWidgetProvider> c, int res)
    {
    	Widget w = new Widget(c, this, res);
        layout.addView(w.cb);
        widgetList.add(w);
    }
    
    public boolean amIEnabled(ComponentName name)
    {
	        AppWidgetManager mgnr = AppWidgetManager.getInstance(this.getApplicationContext());

		    List<AppWidgetProviderInfo> list = mgnr.getInstalledProviders();
		    int state  = pm.getComponentEnabledSetting(name);
		    if (state != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
		    {
		    	return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
		    }
	        for(AppWidgetProviderInfo i : list)
	        {
	        	if (i.toString().contains(name.getClassName())) return true;
	        }

    	return false;
    }
    
    public int getWidgetInstances(ComponentName name)
    {
    	AppWidgetManager mgnr = AppWidgetManager.getInstance(this.getApplicationContext());
    	int [] instanceWidgets = mgnr.getAppWidgetIds(name);
    	if (ToDoActivity.debug) Log.i(LOG_TAG, name.getClassName() + " has " + instanceWidgets.length);
		return instanceWidgets.length;
    }
    
	public void onClick(View v) {
		if (v.getId() == saveButton.getId())
		{
			boolean changed = false;
			for (Widget w : widgetList)
			{
				if (w.state != w.cb.isChecked()) changed = true;
			}
			if (changed)
			{
				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
				dialog.setTitle(getResources().getString(android.R.string.dialog_alert_title));
				dialog.setMessage(getResources().getString(R.string.config_save_warning_message));
				dialog.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   for (Widget w : widgetList)
				   			{
			        		   if (w.state != w.cb.isChecked())
			        		   {
			        			   if (ToDoActivity.debug) Log.i(LOG_TAG, "Setting " + w.label + " to " + w.cb.isChecked());
						    		int state = w.cb.isChecked() ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :  PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
						    		toggleWidget(w.componentName, state);
			        		   }
				   			}
			        	   finish();
			           }
			       });
				dialog.setIcon(android.R.drawable.ic_dialog_alert);
				dialog.show();
			}
			else
			{
				finish();
			}
		}
		else
		{
			for (Widget w : widgetList)
			{
				if (v.getId() == w.cb.getId())
				{
					// check existing widgets
					if (w.instances > 0 && !w.cb.isChecked())
					{
						AlertDialog.Builder dialog = new AlertDialog.Builder(this);
						dialog.setTitle(getResources().getString(R.string.config_disable_warning_title));
						int res = R.string.config_disable_warning_message_single;
						if (w.instances > 1) res = R.string.config_disable_warning_message_plural;
						String message = getResources().getString(res);
						message = message.replace("[num]", w.instances+"");
						dialog.setMessage(message);
						dialog.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					        	   dialog.dismiss();
					           }
					       });
						dialog.setIcon(android.R.drawable.ic_dialog_alert);
						dialog.show();
					}
				}
			}
		}
	}

	/**
	 * Enables or disables the widget 
	 */
	public void toggleWidget(ComponentName name, int state)
	{
		pm.setComponentEnabledSetting(name, state, PackageManager.DONT_KILL_APP);
	}
	
	class Widget
	{
		CheckBox cb;
		boolean state;
		Class<? extends AppWidgetProvider> widgetClass;
		ComponentName componentName;
		String label;
		int instances = 0;
		
		public Widget(Class<? extends AppWidgetProvider> c, SizesConfiguration config, int res)
		{
			widgetClass = c;
			componentName = new ComponentName(getApplicationContext(), widgetClass);
			label = config.getResources().getString(res);
			state = config.amIEnabled(componentName);
			instances = config.getWidgetInstances(componentName);
			createCheckbox(config,res);
		}
		
		private void createCheckbox(SizesConfiguration c, int res)
		{
			cb = new CheckBox(c);
			cb.setText(label);
			cb.setOnClickListener(c);
			cb.setId(++c.ID);
			cb.setChecked(state);
		}
	}
}
