package org.chrisbailey.todo;

import java.lang.reflect.Field;
import java.util.LinkedList;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class ToDoWidgetProvider extends AppWidgetProvider
{
    public static final int MAX_NOTES = 10;
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
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

        // set the note title
        
        String title = db.getTitle(appWidgetId);
        views.setTextViewText(R.id.notetitle, title);
        views.setViewVisibility(R.id.notetitle, View.VISIBLE);
        if (title.length() == 0)
        {
            views.setViewVisibility(R.id.notetitle, View.GONE);
        }
        
        // set the note items
        LinkedList<Note> notes = db.getAllNotes(appWidgetId);
        
        db.close();
        db = null;

        Field f;
        String fieldName;
        
        for (int i=0; i< MAX_NOTES; i++)
        {
            try
            {
                fieldName = "notelayout_"+(i+1);
                f = R.id.class.getDeclaredField(fieldName);
                views.setViewVisibility(f.getInt(fieldName), View.VISIBLE);
                
                if (i >= notes.size()) 
                {
                    views.setViewVisibility(f.getInt(fieldName), View.INVISIBLE);
                    continue;
                }
                
                Note n = notes.get(i);
                if (n.text != null && !n.text.equals(""))
                {
                    fieldName = "noteimage_"+(i+1);
                    f = R.id.class.getDeclaredField(fieldName);
                    int imageView = f.getInt(fieldName);
                    int imageDrawable = R.drawable.tickbox_widget;
                    if (n.status == Note.Status.FINISHED) imageDrawable = R.drawable.tick_widget;
                    views.setImageViewResource(imageView, imageDrawable);
                    fieldName = "note_"+(i+1);
                    f = R.id.class.getDeclaredField(fieldName);
                    int textView = f.getInt(fieldName);
                    int textColor = R.color.widget_item_color;
                    if (n.status == Note.Status.FINISHED) textColor = R.color.done_color;
                    views.setTextViewText(textView, n.text);
                    views.setTextColor(textView, context.getResources().getColor(textColor));
                }
                else
                {
                    views.setViewVisibility(f.getInt(fieldName), View.INVISIBLE);
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