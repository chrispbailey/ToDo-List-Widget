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
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class WidgetConfiguration extends Activity implements View.OnClickListener {
	public static final String LOG_TAG = "WidgetConfiguration";
	public  int ID = 0;
	ImageButton btn;
	PackageManager pm;
	boolean changed = false;
	
	ArrayList <Widget> widgetList = new ArrayList<Widget>();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i(LOG_TAG,"onCreate");
        
        setContentView(R.layout.config);
        changed = false;
        
        pm = this.getPackageManager(); 

        LinearLayout layout = (LinearLayout)findViewById(R.id.widgetListLayout);

        // add our widgets dynamically
        Widget w = new Widget(ToDoWidgetProvider.class);
        layout.addView(w.createCheckbox(this, R.string.app_name_2x2));
        widgetList.add(w);
        w = new Widget(MediumToDoWidget.class);
        layout.addView(w.createCheckbox(this, R.string.app_name_2x3));
        widgetList.add(w);
        w = new Widget(LargeToDoWidget.class);
        layout.addView(w.createCheckbox(this, R.string.app_name_2x4));
        widgetList.add(w);

        btn = (ImageButton)findViewById(R.id.config_save_button);
        btn.setOnClickListener(this);
        btn.setId(++ID);
    }

    public boolean amIEnabled(ComponentName name)
    {
	        AppWidgetManager mgnr = AppWidgetManager.getInstance(this.getApplicationContext());

		    List<AppWidgetProviderInfo> list = mgnr.getInstalledProviders();
		    
	        for(AppWidgetProviderInfo i : list)
	        {
	        	if (i.toString().contains(name.getClassName())) return true;
	        }

//	        PackageInfo info;
//	        info = pm.getPackageInfo(this.getPackageName(),PacdialogkageManager.GET_ACTIVITIES+
//						PackageManager.GET_DISABLED_COMPONENTS+
//						PackageManager.GET_SIGNATURES+
//						PackageManager.GET_CONFIGURATIONS);
//	        for(ActivityInfo i : info.activities)
//	        {
//	        	Log.i(LOG_TAG, i.toString() + " " + i.enabled + " " + i.name);
//	        }

    	return false;
    }
    
	public void onClick(View v) {
		Log.i(LOG_TAG, "onClick");
		if (v.getId() == btn.getId())
		{
			if (changed)
			{
				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
				dialog.setTitle(getResources().getString(R.string.config_save_warning_title));
				dialog.setMessage(getResources().getString(R.string.config_save_warning_message));
				dialog.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
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
		for (Widget w : widgetList)
		{
			if (v.getId() == w.cb.getId())
			{
				v.setEnabled(false);
				Log.i(LOG_TAG, "Setting " + w.label + " to " + w.cb.isChecked());
				int state = w.cb.isChecked() ?  PackageManager.COMPONENT_ENABLED_STATE_ENABLED :  PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
				toggleWidget(w.componentName, state);
				changed = true;
			}
		}
	}

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
		
		public Widget(Class c)
		{
			widgetClass = c;
			componentName = new ComponentName(getApplicationContext(), widgetClass);
		}
		
		public CheckBox createCheckbox(WidgetConfiguration c, int res)
		{
			label = c.getResources().getString(res);
			state = c.amIEnabled(componentName);
			cb = new CheckBox(c);
			cb.setText(label);
			cb.setOnClickListener(c);
			cb.setId(++c.ID);
			cb.setChecked(state);
			return cb;
		}
	}
}