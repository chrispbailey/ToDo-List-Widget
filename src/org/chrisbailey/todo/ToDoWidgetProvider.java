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
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) 
    {
        int N = appWidgetIds.length;
        
        Log.i(LOG_TAG, "updating " + N + " widgets");

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
        
        Log.i(LOG_TAG, "deleting " + N + " widgets");

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
        ToDoDatabase db = new ToDoDatabase(context.getApplicationContext());
        PreferenceManager pm = new PreferenceManager(context, db);
        RemoteViews views = new RemoteViews(context.getPackageName(), manager.getAppWidgetInfo(appWidgetId).initialLayout);

        views.setImageViewResource(R.id.widget_background, pm.getBackground());

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
        
        for (int i=0; i< MAX_NOTES; i++)
        {
            try
            {
                noteField = R.id.class.getDeclaredField("note_"+(i+1)).getInt(null);
                imageField = R.id.class.getDeclaredField("noteimage_"+(i+1)).getInt(null);
                views.setViewVisibility(noteField, View.INVISIBLE);
                views.setViewVisibility(imageField, View.INVISIBLE);
                
                views.setFloat(noteField, "setTextSize", pm.getSize());
                
                if (i >= notes.size()) { continue; }
                
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
        
        manager.updateAppWidget(appWidgetId, views);
    }
}