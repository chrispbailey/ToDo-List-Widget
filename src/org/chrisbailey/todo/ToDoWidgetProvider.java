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
package org.chrisbailey.todo;

import java.util.LinkedList;

import org.chrisbailey.todo.activities.ToDoActivity;
import org.chrisbailey.todo.db.ToDoDatabase;
import org.chrisbailey.todo.utils.Note;
import org.chrisbailey.todo.utils.PreferenceManager;
import org.chrisbailey.todo.utils.Note.Status;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class ToDoWidgetProvider extends AppWidgetProvider
{
    public static final int MAX_NOTES = 20;
    public static String LOG_TAG = "ToDoWidgetProvider";
    
    public static final String BUTTON_UP = "org.chrisbailey.todo.btn.up";
    public static final String BUTTON_DOWN = "org.chrisbailey.todo.btn.down";
    public static final String TOGGLE = "org.chrisbailey.todo.toggle_";
    
    public static enum MOVE { UP, DOWN, NONE };
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) 
    {
        int N = appWidgetIds.length;
        
        if (ToDoActivity.debug) Log.i(LOG_TAG, "updating " + N + " widgets");

        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId, MOVE.NONE);
        }
        
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        int N = appWidgetIds.length;
        
        if (ToDoActivity.debug) Log.i(LOG_TAG, "deleting " + N + " widgets");

        ToDoDatabase db = new ToDoDatabase(context.getApplicationContext());
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            db.deleteTitle(appWidgetId);
            db.deleteAllNotes(appWidgetId);
        }
        db.close();
        db = null;
        
        super.onDeleted(context, appWidgetIds);
    }
    
    @Override 
    public void onReceive(Context context, Intent intent) { 
        final String action = intent.getAction();
        Bundle extras = intent.getExtras();
        if (ToDoActivity.debug) Log.i(LOG_TAG,"Action:"+action);

        if (BUTTON_UP.equals(action))
        {
            int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId, MOVE.UP);
        }
        if (BUTTON_DOWN.equals(action))
        {
            int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId, MOVE.DOWN);
        }
        if (action.startsWith(TOGGLE))
        {
            int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            int noteid = Integer.parseInt(action.substring(TOGGLE.length()));
            toggleNote(context, noteid);
            updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId, MOVE.NONE);
        }
        if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) { 
            int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) { 
                this.onDeleted(context, new int[] { appWidgetId }); 
            } 
        } else { 
            super.onReceive(context, intent); 
        } 
    } 

    public static void toggleNote(Context context, int noteId)
    {
    	try
    	{
    		// create a database connection
	        ToDoDatabase db = new ToDoDatabase(context.getApplicationContext());
	        Note n = db.getNote(noteId);
	        n.status = n.status == Status.CREATED ? Status.FINISHED : Status.CREATED;
	        db.updateNote(n);
	        db.close();
    	}
    	catch (NullPointerException npe) { /* do nothing */ }
    }
    
    public static void updateAppWidget(Context context, AppWidgetManager manager, int appWidgetId, MOVE move)
    {
        try
        {
            if (ToDoActivity.debug)  Log.i(LOG_TAG, "updating widget #" + appWidgetId);
            
            // create a database connection
            ToDoDatabase db = new ToDoDatabase(context.getApplicationContext());
            
            // read all required info from db
            PreferenceManager pm = new PreferenceManager(context, db);
            String title = db.getTitle(appWidgetId);
            
            boolean showScrollButtons = pm.getScrollButtons();
            
            int offset = db.getOffset(appWidgetId);
            
            // get all notes
            LinkedList<Note> notes = db.getAllNotes(appWidgetId);

            int maxCurrNotes = notes.size();
            
            // update the offset
            if (move == MOVE.UP)
            {
            	offset--;
            	
            	// stop moving into negative numbers
            	if (offset <= 0) offset = 0;
            }
            if (move == MOVE.DOWN)
            {
            	offset++;

            	// don't scroll past last item
                if (offset >= maxCurrNotes && maxCurrNotes > 0) offset = maxCurrNotes-1;
            }
            if (move != MOVE.NONE)
            {
            	db.setOffset(appWidgetId, offset);
            }
            
        	// now close the db
            db.close();
            db = null;
            
            // update the ui
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

            views.setImageViewResource(R.id.widget_background, pm.getBackground());

            // set top padding
            views.setViewVisibility(R.id.padding1, View.GONE);
            views.setViewVisibility(R.id.padding2, View.GONE);
            int padding = pm.getTopPadding();
            if (padding == 1) views.setViewVisibility(R.id.padding1, View.VISIBLE);
            if (padding == 2)
            {
                views.setViewVisibility(R.id.padding1, View.VISIBLE);
                views.setViewVisibility(R.id.padding2, View.VISIBLE);
            }
            
            // set the note title
            views.setTextViewText(R.id.notetitle, Html.fromHtml("<b><u>"+title.trim()+"</u></b>"));
            views.setViewVisibility(R.id.notetitle, View.VISIBLE);
            views.setTextColor(R.id.notetitle, pm.getActiveColor());
            views.setFloat(R.id.notetitle, "setTextSize", pm.getTitleSize());
            
            if (title.length() == 0)
            {
                views.setViewVisibility(R.id.notetitle, View.GONE);
            }
                
            int noteField;
            int imageField;
            int j = 0;
            
            if (showScrollButtons)
            {
            	views.setViewVisibility(R.id.widget_scroll_up, View.VISIBLE);
            	views.setViewVisibility(R.id.widget_scroll_down, View.VISIBLE);
                views.setImageViewResource(R.id.widget_scroll_up, R.drawable.background_99_0);
                views.setImageViewResource(R.id.widget_scroll_down, R.drawable.background_99_0);
            	
            	// set the scrolling button visibility
            	if (offset > 0) views.setImageViewResource(R.id.widget_scroll_up, R.drawable.action_scroll_up);
            	if (maxCurrNotes > 1 && offset < maxCurrNotes-1) views.setImageViewResource(R.id.widget_scroll_down, R.drawable.action_scroll_down);
            }
            else
            {
            	views.setViewVisibility(R.id.widget_scroll_up, View.GONE);
            	views.setViewVisibility(R.id.widget_scroll_down, View.GONE);
            }

//            Log.i(LOG_TAG,"MAX_NOTES:" +MAX_NOTES);
//            Log.i(LOG_TAG,"offset:" +offset);
//            Log.i(LOG_TAG,"showScrollButtons:"+ (showScrollButtons?"on":"off"));
            for (int i=offset; j< MAX_NOTES; i++)
            {
                try
                {
                    j++;
                    noteField = R.id.class.getDeclaredField("note_"+(j)).getInt(null);
                    imageField = R.id.class.getDeclaredField("noteimage_"+(j)).getInt(null);
                    views.setViewVisibility(noteField, View.INVISIBLE);
                    views.setViewVisibility(imageField, View.INVISIBLE);
                    
                    views.setFloat(noteField, "setTextSize", pm.getSize());
                    
                    if (i >= maxCurrNotes) { continue; }
                    
                    Note n = notes.get(i);
                    if (n.text != null && !n.text.equals(""))
                    {
                        views.setViewVisibility(noteField, View.VISIBLE);
                        views.setViewVisibility(imageField, View.VISIBLE);
                        if (pm.isEmptyIcon()) views.setViewVisibility(imageField, View.GONE);
                        int imageDrawable = pm.getActiveIcon();
                        if (n.status == Note.Status.FINISHED) imageDrawable = pm.getFinishedIcon();
                        views.setImageViewResource(imageField, imageDrawable);
                        Intent intent = new Intent(context, ToDoWidgetProvider.class);
                        intent.setAction(TOGGLE+n.id);
                        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                        intent.putExtra(TOGGLE+n.id, 1);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
                        views.setOnClickPendingIntent(imageField, pendingIntent);

                        int textColor = pm.getActiveColor();
                        if (n.status == Note.Status.FINISHED) textColor = pm.getFinishedColor();
                        views.setTextViewText(noteField, n.text);
                        views.setTextColor(noteField, textColor);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
    
            // Tell the AppWidgetManager to perform an update on the current App Widget
            // Create an Intent to launch ToDoActivity
            Intent intent = new Intent(context, ToDoActivity.class);
            intent.setAction(appWidgetId+"");
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
    
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, 0);
            
            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
            
            if (showScrollButtons)
            {
	            intent = new Intent(context, ToDoWidgetProvider.class);
	            intent.setAction(BUTTON_UP);
	            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
	            intent.putExtra(BUTTON_UP, 1);
	            pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
	            views.setOnClickPendingIntent(R.id.widget_scroll_up, pendingIntent);
	
	            intent = new Intent(context, ToDoWidgetProvider.class);
	            intent.setAction(BUTTON_DOWN);
	            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
	            intent.putExtra(BUTTON_DOWN, 1);
	            pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
	            views.setOnClickPendingIntent(R.id.widget_scroll_down, pendingIntent);
            }
            
            manager.updateAppWidget(appWidgetId, views);
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
}
