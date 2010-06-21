package org.chrisbailey.todo;

import java.lang.reflect.Field;
import java.util.LinkedList;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class ToDoWidgetProvider extends AppWidgetProvider
{
    public static final int MAX_NOTES = 10;
    public static String LOG_TAG = "ToDoWidgetProvider";

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
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

        LinkedList<Note> notes = db.getAllNotes(appWidgetId);
        
        db.close();
        db = null;
        
        StringBuffer s = new StringBuffer();

        for (int i=1; i<= MAX_NOTES; i++)
        {
            if (i >= notes.size()) break;
            Note n = notes.get(i);
            if (n.text != null)
            {
                Field f;
                try
                {
                    f = R.id.class.getDeclaredField("noteimage_"+i);
                    int imageView = f.getInt("noteimage_"+i);
                    int imageDrawable = R.drawable.tickbox;
                    if (n.status == Note.Status.FINISHED) imageDrawable = R.drawable.tick;
                    views.setImageViewResource(imageView, imageDrawable);
                    f = R.id.class.getDeclaredField("note_"+i);
                    int textView = f.getInt("note_"+i);
                    views.setTextViewText(textView, n.text);
                    if (n.status == Note.Status.FINISHED) views.setTextColor(textView, R.color.done_color);
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
//            if (n.status == Note.Status.FINISHED) s.append("<font color='#FF0000'>[]</font><img src='tick'/> <font color='"+ToDoActivity.DONE_COLOR+"'>"+n.text+"</font>");
//            else s.append("<font color='#0000FF'>[*]</font><img src='tickbox'/> <font color='#000000'>"+n.text+"</font>");
 //           s.append("<br/>");
            }
        }
        
        Log.d(LOG_TAG, s.toString());

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