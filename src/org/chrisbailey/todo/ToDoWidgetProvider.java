package org.chrisbailey.todo;

import java.util.LinkedList;

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
    public int offSet = 0;
    
    public static final String BUTTON_UP = "btn.up";
    public static final String BUTTON_DOWN = "btn.down";
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) 
    {
        int N = appWidgetIds.length;
        
        if (ToDoActivity.debug) Log.i(LOG_TAG, "updating " + N + " widgets");

        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId);
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
        
        boolean refresh = false;
        if (BUTTON_UP.equals(action))
        {
            offSet--;
            if (offSet <= 0) offSet = 0;
            refresh = true;
        }
        if (BUTTON_DOWN.equals(action))
        {
            offSet++;
            refresh = true;
        }
        
        if (refresh)
        {
                int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID); 
        
                updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId);
        }
        
        if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) { 
            final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 
                                                  AppWidgetManager.INVALID_APPWIDGET_ID); 
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) { 
                this.onDeleted(context, new int[] { appWidgetId }); 
            } 
        } else { 
            super.onReceive(context, intent); 
        } 
    } 

    public static void updateAppWidget(Context context, AppWidgetManager manager, int appWidgetId)
    {
        try
        {
            if (ToDoActivity.debug)
            {
                Log.i(LOG_TAG, "updating widget #" + appWidgetId);
                Log.i(LOG_TAG, "offset is " + offSet);
            }
            
            ToDoDatabase db = new ToDoDatabase(context.getApplicationContext());
            PreferenceManager pm = new PreferenceManager(context, db);
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
            String title = db.getTitle(appWidgetId);
            title.trim();
            views.setTextViewText(R.id.notetitle, Html.fromHtml("<b><u>"+title+"</u></b>"));
            views.setViewVisibility(R.id.notetitle, View.VISIBLE);
            views.setTextColor(R.id.notetitle, pm.getActiveColor());
            views.setFloat(R.id.notetitle, "setTextSize", pm.getTitleSize());
            
            if (title.length() == 0)
            {
                views.setViewVisibility(R.id.notetitle, View.GONE);
            }
            
            // set the note items
            LinkedList<Note> notes = db.getAllNotes(appWidgetId);
            
            db.close();
            db = null;
    
            int noteField;
            int imageField;
            int j = 0;
            
            int maxCurrNotes = notes.size();
            // don't scroll past last item
            if (offSet >= maxCurrNotes && maxCurrNotes > 0) offSet = maxCurrNotes-1;
            
            for (int i=offSet; i< MAX_NOTES; i++)
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
            
            manager.updateAppWidget(appWidgetId, views);
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
}