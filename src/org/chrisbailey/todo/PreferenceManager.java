package org.chrisbailey.todo;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

public class PreferenceManager 
{
    private static final String BACKGROUND_DRAWABLE_PREFIX = "background";
    private static final String ACTIVE_DRAWABLE_PREFIX = "icon_active";
    private static final String FINISHED_DRAWABLE_PREFIX = "icon_finished";
    
    private static final String LOG_TAG = "ReferenceManager";

    private int currentBackground = -1;
    private int activeIcon = -1;
    private int finishedIcon = -1;
    private int currentActiveColor = -1;
    private int currentFinishedColor = -1;
    private int currentSize = -1;
    
    public PreferenceManager(Context c, ToDoDatabase db)
    {
        setBackground(db.getPrefBackground());

        setIcons(db.getPrefIcon());
        
        int i = db.getPrefColorActive();
        if ( i < 0) i = c.getResources().getColor(R.color.widget_item_color);
        setActiveColor(i);
        
        i = db.getPrefColorFinished();
        if ( i < 0) i = c.getResources().getColor(R.color.done_color);
        setFinishedColor(i);
        
        i = db.getPrefSize();
        if ( i < 0) i = (int) new TextView(c).getTextSize();
        setSize(i);
    }
    
    /**
     * Save state to database
     * @param db
     */
    public void save(ToDoDatabase db)
    {
    	db.setPrefBackground(currentBackground);
    	db.setPrefColorActive(currentActiveColor);
    	db.setPrefColorFinished(currentFinishedColor);
    	db.setPrefIcon(activeIcon);
    	db.setPrefSize(currentSize);
    }
    
    public void setSize(int i)
    {
    	currentSize = i;
    }
    
    public int getSize()
    {
    	return currentSize;
    }
    
    public int getActiveColor()
    {
    	return currentActiveColor;
    }
    
    public void setActiveColor(int i)
    {
    	currentActiveColor = i;
    }
    
    public int getFinishedColor()
    {
    	return currentFinishedColor;
    }
    
    public void setFinishedColor(int i)
    {
    	currentFinishedColor = i;
    }
    
    int[] getAllBackgrounds()
    {
    	return getAllDrawables(BACKGROUND_DRAWABLE_PREFIX);
    }
    
    public int[] getAllIcons()
    {
    	return getAllDrawables(ACTIVE_DRAWABLE_PREFIX);
    }
    
    private int[] getAllDrawables(String str)
    {
        ArrayList <Integer> drawables = new ArrayList<Integer>();

        try {
            Field f[] = R.drawable.class.getFields();

            for (int i = 0; i < f.length; i++)
            {
                if (f[i].getName().startsWith(str))
                {
                    drawables.add(f[i].getInt(null));
                }
            }
         }
         catch (Throwable e) {
            Log.e(LOG_TAG,"Error obtaining drawable",e);
         }
         
        int [] drawableReferences = new int[drawables.size()];
        int i = 0;
        for (Integer integer : drawables) drawableReferences[i++] = integer;
        
        return drawableReferences;
    }
    
    public void setBackground(int i)
    {
    	if (i < 0) i = 0;
    	currentBackground = getBackgroundId(i);
    }
    public void setIcons(int i)
    {
    	if (i < 0) i = 0;
    	activeIcon = getActiveIconId(i);
    	finishedIcon = getFinishedIconId(i);
    }

    public int getBackground()
    {
    	return currentBackground;
    }
    public int getActiveIcon()
    {
    	return activeIcon;
    }
    public int getFinishedIcon()
    {
    	return finishedIcon;
    }    
    private static int getActiveIconId(int i)
    {
        DrawableField f = new DrawableField(ACTIVE_DRAWABLE_PREFIX);
        return f.getDrawableField(i);
    }

    private static int getFinishedIconId(int i)
    {
        DrawableField f = new DrawableField(FINISHED_DRAWABLE_PREFIX);
        return f.getDrawableField(i);
    }
    private static int getBackgroundId(int i)
    {
        DrawableField f = new DrawableField(BACKGROUND_DRAWABLE_PREFIX);
        return f.getDrawableField(i);
    }
    
    public static class DrawableField
    {
        String field;
        
        public DrawableField(String s)
        {
            field = s;
        }
        
        public int getDrawableField(int i)
        {
            try 
            {
                i++;
                Log.i(LOG_TAG, "Looking up " + field+i);
                return R.drawable.class.getField(field+i).getInt(null);
            } catch (Exception e)
            {
                Log.e(LOG_TAG,"Error obtaining drawable",e);
            }
            return -1;
        }
    }
    
}
