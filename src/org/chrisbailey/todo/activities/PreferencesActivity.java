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
import org.chrisbailey.todo.ToDoWidgetProvider.MOVE;
import org.chrisbailey.todo.db.ToDoDatabase;
import org.chrisbailey.todo.utils.PreferenceManager;
import org.chrisbailey.todo.widgets.ColorPickerDialog;
import org.chrisbailey.todo.widgets.NumberPicker;

import android.app.Activity;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;

public class PreferencesActivity extends Activity implements ColorPickerDialog.OnColorChangedListener, NumberPicker.OnNumberChangedListener, OnItemClickListener, View.OnClickListener {

    // static dialog indicators
    public static final int DIALOG_SELECT_COLOR = 1;
    
    public static final String LOG_TAG = "PreferencesActivity";
    
    View colorPickerActive;
    View colorPickerFinished;
    public static int[] imageBackgrounds;
    public static int[] imageIcons;
    
    /* References to preview items */
    private ImageView preview;
    private ImageView ivActiveIcon;
    private ImageView ivFinishedIcon;
    private ImageView ivScrollUp;
    private ImageView ivScrollDown;
    private static TextView tvTitle;
    private static ArrayList<TextView> tvNotes;
    private static ArrayList<ImageView> ivIcons;
    private TextView padding1;
    private TextView padding2;
    private LinearLayout scrollButtonLayout;
    private static final String NOTE_VIEW_PREFIX = "note_";
    private static final String ICON_VIEW_PREFIX = "noteimage_";
    
    private static PreferenceManager pm;
    
    private boolean activeColorChooser = true;

    int originalSize = 20;
    
    private int defaultScale;
    
    ToDoDatabase db;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);
        
        db = new ToDoDatabase(this);
        pm = new PreferenceManager(this, db);
        db.close();
        db = null;
        
        colorPickerActive = (View) findViewById(R.id.pick_color_active);
        colorPickerActive.setOnClickListener(new OnClickListener() 
        {
            public void onClick(View arg0)
            {
                activeColorChooser = true;
                PreferencesActivity.this.showDialog(PreferencesActivity.DIALOG_SELECT_COLOR);
            }
        });
        
        setBackgroundColor(colorPickerActive,pm.getActiveColor());
        
        colorPickerFinished = (View) findViewById(R.id.pick_color_finished);
        colorPickerFinished.setOnClickListener(new OnClickListener() 
        {
            public void onClick(View arg0)
            {
                activeColorChooser = false;
                PreferencesActivity.this.showDialog(PreferencesActivity.DIALOG_SELECT_COLOR);
            }
        });
        setBackgroundColor(colorPickerFinished,pm.getFinishedColor());
        
        try
        {
            NumberPicker fontSizeSelector = (NumberPicker) findViewById(R.id.font_size_selector);
            fontSizeSelector.setOnChangeListener(this);
            fontSizeSelector.setRange(10, 30);
            fontSizeSelector.setCurrent(pm.getSize());
        } 
        catch (ClassCastException issue6894)
        {
            // ignore
            // see http://code.google.com/p/android/issues/detail?id=6894
        }

        findViewById(R.id.ok_button).setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
            	saveAndClose();
            }
        });
        
        findViewById(R.id.cancel_button).setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                finish();
            }
        });
        
        scrollButtonLayout = (LinearLayout) findViewById(R.id.scroll_buttons_enable);
        scrollButtonLayout.setOnClickListener(this);
        ivScrollUp = (ImageView) findViewById(R.id.scroll_buttons_up);
        ivScrollDown = (ImageView) findViewById(R.id.scroll_buttons_down);
        // set default state
        setScrollButtonState(pm.getScrollButtons());
        
        // generate the list of drawable options (background & icons)
        initDrawableLists();
        
        // init the background selector
        Gallery backgroundSelector = (Gallery) findViewById(R.id.background_selector);
        backgroundSelector.setAdapter(new ImageAdapter(this, 150, 100, imageBackgrounds, PreferenceManager.BACKGROUND_DRAWABLE_PREFIX, ScaleType.FIT_CENTER));
        backgroundSelector.setOnItemClickListener(this);
        
        // set the currently selected background as the default
        int fieldId = pm.getBackgroundId();
        for (int i = 0; i < imageBackgrounds.length; i++)
        {
        	if (imageBackgrounds[i] == fieldId) backgroundSelector.setSelection(i);
        }
        
        // init the icon selector
        Gallery iconSelector = (Gallery) findViewById(R.id.icon_selector);
        iconSelector.setAdapter(new ImageAdapter(this, 70, 70, imageIcons, PreferenceManager.ACTIVE_DRAWABLE_PREFIX, ScaleType.CENTER));
        iconSelector.setOnItemClickListener(this);

        // set the currently selected active icon as the default
        fieldId = pm.getIconId();
        for (int i = 0; i < imageIcons.length; i++)
        {
        	if (imageIcons[i] == fieldId) iconSelector.setSelection(i);
        }
        
        // set default values
        defaultScale = (int) new TextView(this).getTextSize();
        
        // update
        updateIcons(2);
    }
    
    private void saveAndClose()
    {
        db = new ToDoDatabase(PreferencesActivity.this);
        pm.save(db);
        db.close();
        db = null;
        setResult(0, null);

        // refresh any visible widgets
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());        
        refreshWidgets(appWidgetManager, ToDoWidgetProvider.class);
        refreshWidgets(appWidgetManager, LargeToDoWidget.class);
        refreshWidgets(appWidgetManager, MediumToDoWidget.class);
        refreshWidgets(appWidgetManager, SmallToDoWidget.class);
        refreshWidgets(appWidgetManager, ToDoWidget1x1.class);
        refreshWidgets(appWidgetManager, ToDoWidget1x2.class);
        refreshWidgets(appWidgetManager, ToDoWidget1x3.class);
        refreshWidgets(appWidgetManager, ToDoWidget1x4.class);
        refreshWidgets(appWidgetManager, ToDoWidget3x1.class);
        refreshWidgets(appWidgetManager, ToDoWidget3x2.class);
        refreshWidgets(appWidgetManager, ToDoWidget3x3.class);
        refreshWidgets(appWidgetManager, ToDoWidget3x4.class);
        refreshWidgets(appWidgetManager, ToDoWidget4x1.class);
        refreshWidgets(appWidgetManager, ToDoWidget4x2.class);
        refreshWidgets(appWidgetManager, ToDoWidget4x3.class);
        refreshWidgets(appWidgetManager, ToDoWidget4x4.class);
        finish();
    }
    
    private void refreshWidgets(AppWidgetManager appWidgetManager, Class <? extends ToDoWidgetProvider> c)
    {
    	ComponentName component = new ComponentName(getApplicationContext(), c);
        int [] ids = appWidgetManager.getAppWidgetIds(component);
        for (int i : ids)
        {
        	if (ToDoActivity.debug) Log.i(LOG_TAG, "Sending intents to widget #" + ids);
            ToDoWidgetProvider.updateAppWidget(getApplicationContext(), appWidgetManager, i, MOVE.NONE);
        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id)
    {
        ColorPickerDialog dialog = null;

        switch (id)
        {
            case DIALOG_SELECT_COLOR:
                dialog = new ColorPickerDialog(this, this, pm.getActiveColor());
                break;
        }
        return dialog;
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog)
    {
        int color = pm.getActiveColor();
        if (!activeColorChooser) color = pm.getFinishedColor();
        if (ToDoActivity.debug) Log.i(LOG_TAG,"default color is " + color);
        
        ((ColorPickerDialog) dialog).setColor(color);
    }
    
    protected void initDrawableLists()
    {
        imageBackgrounds = pm.getAllBackgrounds();
        imageIcons = pm.getAllIcons();db = new ToDoDatabase(PreferencesActivity.this);
        pm.save(db);
        db.close();
        db = null;
    }
    
    /* Fired when scroll button option clicked */
	public void onClick(View v) {
		boolean b = !pm.getScrollButtons();
		setScrollButtonState(b);
		pm.setScrollButtons(b);
	}
	
	private void setScrollButtonState(boolean b)
	{
		if (b)
		{
			ivScrollUp.setVisibility(View.VISIBLE);
			ivScrollDown.setVisibility(View.VISIBLE);
			scrollButtonLayout.setBackgroundResource(R.drawable.border_on);
		}
		else
		{
			ivScrollUp.setVisibility(View.INVISIBLE);
			ivScrollDown.setVisibility(View.INVISIBLE);
			scrollButtonLayout.setBackgroundResource(R.drawable.border_off);
		}
	}
    
    public void onColorChanged(int color) 
    {
        if (ToDoActivity.debug) Log.i(LOG_TAG,"Color changed to " + color);
        if (activeColorChooser)
        {
            setBackgroundColor(colorPickerActive,color);
            pm.setActiveColor(color);
        }
        else
        {
            setBackgroundColor(colorPickerFinished,color);
            pm.setFinishedColor(color);

        }
        
        updateIcons(2);
    }

    private void setBackgroundColor(View v, int c)
    {
        if (ToDoActivity.debug) Log.i(LOG_TAG,v.getBackground().toString());
        GradientDrawable shape = (GradientDrawable) v.getBackground();
        shape.setColor(c);
        v.setBackgroundDrawable(shape);
    }
    
    public void onNumberChanged(NumberPicker picker, int oldVal, int newVal)
    {
        if (ToDoActivity.debug) Log.i(LOG_TAG,"Font size changed to " + newVal);
        pm.setSize(newVal);
        updateIcons(2);
    }
    
    @SuppressWarnings("unchecked")
    public void onItemClick(AdapterView parent, View v, int position, long id) 
    {
        if (parent.getId() == R.id.background_selector)
        {
            pm.setBackground(imageBackgrounds[position]);
        }
        else
        {
            pm.setIcons(imageIcons[position]);
        }
        updateIcons(2);
    }

    public void updateIcons(int max)
    {
        try
        {
            if (preview == null)
            {
                preview = (ImageView) findViewById(R.id.preview);
                tvTitle = (TextView) findViewById(R.id.notetitle);

                padding1 = (TextView) findViewById(R.id.padding1);
                padding2 = (TextView) findViewById(R.id.padding2);
                
                // bold & underline as per the real widget
                tvTitle.setText(Html.fromHtml("<b><u>"+tvTitle.getText().toString()+"</u></b>"));
                tvNotes = new ArrayList<TextView>();
                ivIcons = new ArrayList<ImageView>();

                ivActiveIcon = (ImageView) findViewById(R.id.active_color_icon);
                ivFinishedIcon = (ImageView) findViewById(R.id.finished_color_icon);
                for (int i = 0; i < max; i++)
                {
                    tvNotes.add((TextView) findViewById(getNoteId(i)));
                    ivIcons.add((ImageView) findViewById(getIconId(i)));
                }
            }
            pm.setBackground(pm.getBackgroundId());
            
            preview.setImageResource(pm.getBackground());
            
            // set top padding
            padding1.setVisibility(View.GONE);
            padding2.setVisibility(View.GONE);

            int padding = pm.getTopPadding();
            if (padding == 1) padding1.setVisibility(View.VISIBLE);
            if (padding == 2)
            {
                padding1.setVisibility(View.VISIBLE);
                padding2.setVisibility(View.VISIBLE);
            }
            
            tvTitle.setTextColor(pm.getActiveColor());
            tvTitle.setTextSize(pm.getTitleSize());

            tvNotes.get(0).setTextColor(pm.getActiveColor());
            tvNotes.get(0).setTextSize(pm.getSize());

            tvNotes.get(1).setTextColor(pm.getFinishedColor());
            tvNotes.get(1).setTextSize(pm.getSize());

            // set height of icon
            float scalingFactor = (float) pm.getSize() / (float) defaultScale;
            int newH = (int) (originalSize * scalingFactor);

            setIcon(ivIcons.get(0), pm.getActiveIcon(), originalSize, newH);
            setIcon(ivIcons.get(1), pm.getFinishedIcon(), originalSize, newH);

            ivActiveIcon.setBackgroundResource(pm.getActiveIcon());
            ivFinishedIcon.setBackgroundResource(pm.getFinishedIcon());

        }
        catch (Exception e)
        {
        }
    }
    
    private void setIcon(ImageView v, int ref, int w, int h)
    {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
        params.height = h;
        params.width = w;

        v.setLayoutParams(params);
        v.setImageResource(ref);
        
        v.setVisibility(View.VISIBLE);
        if (pm.isEmptyIcon()) v.setVisibility(View.GONE);
    }

    public static int getNoteId(int i)
    {
    	ViewField f = new ViewField(NOTE_VIEW_PREFIX);
        return f.getViewField(i);
    }
    
    public static int getIconId(int i)
    {
    	ViewField f = new ViewField(ICON_VIEW_PREFIX);
        return f.getViewField(i);
    }
    
    public static class ViewField
    {
        String field;
        
        public ViewField(String s)
        {
            field = s;
        }
        
        public int getViewField(int i)
        {
            try 
            {
                i++;
                return R.id.class.getField(field+i).getInt(null);
            } catch (Exception e)
            {
                Log.e(LOG_TAG,"Error obtaining drawable",e);
            }
            return -1;
        }
    }
    
    /*
     * Handles the population and selection of a Gallery widget
     */
    public class ImageAdapter extends BaseAdapter {
        int mGalleryItemBackground;
        int [] mDrawables;
        int mHeight = 150;
        int mWidth = 100;
        String mField;
        ScaleType mScaleType;
        
        /** The parent context */
        private Context mContext;

        /** Simple Constructor saving the 'parent' context. */
        public ImageAdapter(Context c, int width, int height, int drawables [], String field, ScaleType type)
        {
            mWidth = width;
            mHeight = height;
            mContext = c;
            mDrawables = drawables;
            mField = field;
            mScaleType = type;
            TypedArray a = obtainStyledAttributes(R.styleable.default_gallery);
            mGalleryItemBackground = a.getResourceId(R.styleable.default_gallery_android_galleryItemBackground, 0);
            a.recycle();
        }
        
        /** Returns the amount of images we have defined. */
        public int getCount() { return mDrawables.length; }

        /* Use the array-Positions as unique IDs */
        public Object getItem(int position) { return position; }
        public long getItemId(int position) { return position; }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            ImageView i = new ImageView(mContext);
            i.setImageResource(pm.getDrawableField(mDrawables[position], mField));
            i.setLayoutParams(new Gallery.LayoutParams(mWidth, mHeight));
            i.setScaleType(mScaleType);
            i.setBackgroundResource(mGalleryItemBackground);
            return i;
        }
    }
}
