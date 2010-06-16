package org.chrisbailey.todo;

import java.util.LinkedList;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;

public class ToDoWidgetProvider extends AppWidgetProvider
{
    public static String LOG_TAG = "ToDoWidgetProvider";
    
//    @Override
//    public void onEnabled(Context  context)
//    {
//        Log.d(LOG_TAG, "onEnabled");
//        setIntent(context);
//    }
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) 
    {
        Log.i(LOG_TAG, "onUpdate");
        
        int N = appWidgetIds.length;
        
        Log.i(LOG_TAG, "have " + N + " widgets!");

        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
    
    public static void updateAppWidget(Context context, AppWidgetManager manager, int appWidgetId)
    {
        ToDoDatabase db = new ToDoDatabase(context.getApplicationContext());

        LinkedList<Note> notes = db.getAllNotes();
        
        db.close();
        db = null;
        
        
        StringBuffer s = new StringBuffer();

        for (Note n : notes)
        {
            if (n.status == Note.Status.FINISHED) s.append("<font color=\""+ToDoActivity.DONE_COLOR+"\">"+n.text+"</font>");
            else s.append("<font color=\"#000000\">"+n.text+"</font>");
            s.append("<br/>");
        }
        
        Log.d(LOG_TAG, s.toString());

        // Get the layout for the App Widget and attach an on-click listener to the button
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setTextViewText(R.id.textarea, Html.fromHtml((s.toString())));
        
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
