package org.chrisbailey.todo.config;

import java.util.ArrayList;
import java.util.List;

import org.chrisbailey.todo.LargeToDoWidget;
import org.chrisbailey.todo.MediumToDoWidget;
import org.chrisbailey.todo.R;
import org.chrisbailey.todo.ToDoWidgetProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class WidgetConfiguration extends Activity implements View.OnClickListener {
	public static final String LOG_TAG = "WidgetConfiguration";
	public  int ID = 0;
	ImageButton saveButton;
	PackageManager pm;
	
	ArrayList <Widget> widgetList = new ArrayList<Widget>();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i(LOG_TAG,"onCreate");
        
        setContentView(R.layout.config);
        
        pm = this.getPackageManager(); 

        LinearLayout layout = (LinearLayout)findViewById(R.id.widgetListLayout);

        // add our widgets dynamically
        Widget w = new Widget(ToDoWidgetProvider.class, this, R.string.app_name_2x2);
        layout.addView(w.cb);
        widgetList.add(w);
        w = new Widget(MediumToDoWidget.class, this, R.string.app_name_2x3);
        layout.addView(w.cb);
        widgetList.add(w);
        w = new Widget(LargeToDoWidget.class, this, R.string.app_name_2x4);
        layout.addView(w.cb);
        widgetList.add(w);

        saveButton = (ImageButton)findViewById(R.id.config_save_button);
        saveButton.setOnClickListener(this);
        saveButton.setId(++ID);
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
    	Log.i(LOG_TAG, name.getClassName() + " has " + instanceWidgets.length);
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
				dialog.setTitle(getResources().getString(R.string.config_save_warning_title));
				dialog.setMessage(getResources().getString(R.string.config_save_warning_message));
				dialog.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   for (Widget w : widgetList)
				   			{
					       		Log.i(LOG_TAG, "Setting " + w.label + " to " + w.cb.isChecked());
					    		int state = w.cb.isChecked() ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :  PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
					    		toggleWidget(w.componentName, state);
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
						dialog.setMessage(getResources().getString(R.string.config_disable_warning_message));
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

	/*
	public void toggleCheckbox(Widget w)
	{
		w.cb.setEnabled(false);
	}
*/
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
		Class<AppWidgetProvider> widgetClass;
		ComponentName componentName;
		String label;
		int instances = 0;
		
		public Widget(Class c, WidgetConfiguration config, int res)
		{
			widgetClass = c;
			componentName = new ComponentName(getApplicationContext(), widgetClass);
			label = config.getResources().getString(res);
			state = config.amIEnabled(componentName);
			instances = config.getWidgetInstances(componentName);
			createCheckbox(config,res);
		}
		
		private void createCheckbox(WidgetConfiguration c, int res)
		{
			cb = new CheckBox(c);
			cb.setText(label);
			cb.setOnClickListener(c);
			cb.setId(++c.ID);
			cb.setChecked(state);
		}
	}
}