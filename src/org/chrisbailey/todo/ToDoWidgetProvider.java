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
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) 
    {
        Log.d(LOG_TAG, "onUpdate");
        // For each widget that needs an update, get the text that we should display:
        //   - Create a RemoteViews object for it
        //   - Set the text in the RemoteViews object
        //   - Tell the AppWidgetManager to show that views object for the widget.
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    
    public static void updateAppWidget(Context context, AppWidgetManager manager, int appWidgetId)
    {
        ToDoDatabase db = new ToDoDatabase(context);

        LinkedList<Note> notes = db.getAllNotes();
        
        db.close();

        StringBuffer s = new StringBuffer();

        for (Note n : notes)
        {
            if (n.status == Note.Status.FINISHED) s.append("<ul>"+n.text+"</ul>");
            else s.append(n.text);
            s.append("<br/>");
        }
        
        // Create an Intent to launch ToDoActivity
        Intent intent = new Intent(context, ToDoActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        
        // Get the layout for the App Widget and attach an on-click listener to the button
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setTextViewText(R.id.textarea, Html.fromHtml((s.toString())));
        views.setOnClickPendingIntent(R.id.textarea, pendingIntent);
        
        // Tell the AppWidgetManager to perform an update on the current App Widget
        manager.updateAppWidget(appWidgetId, views);
    }
}
