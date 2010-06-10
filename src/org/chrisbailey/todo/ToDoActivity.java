package org.chrisbailey.todo;

import java.util.LinkedList;

import org.chrisbailey.todo.Note.Status;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TableLayout;
import android.widget.TableRow;

public class ToDoActivity extends Activity
{
    // reference to the database
    public static ToDoDatabase db;

    private static final String SAVED_INSTANCE_KEY = "android:savedDialogs";
    
    private static final String LOG_TAG = "ToDoActivity";
    
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    
    public ToDoActivity() {
        super();
    }

    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
//        if (savedInstanceState != null)
//        {
//            // remove and references to the dialog as re-creating this is difficult
//            if (savedInstanceState.containsKey(SAVED_INSTANCE_KEY))
//            {
//                savedInstanceState.remove(SAVED_INSTANCE_KEY);
//            }
//        }
//        
        super.onCreate(savedInstanceState);
        
        setResult(RESULT_CANCELED);

        setContentView(R.layout.main);
        
        db = new ToDoDatabase(this);
        
        redraw(this);
        
        Button addnotebutton = (Button)findViewById(R.id.addnotebutton);
        addnotebutton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                db.addNote(new Note());
                redraw(ToDoActivity.this);
            }
        });
        
        Button donebutton = (Button)findViewById(R.id.donebutton);
        donebutton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.i(LOG_TAG, "Sending intent");
                
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ToDoActivity.this);
                ToDoWidgetProvider.updateAppWidget(ToDoActivity.this, appWidgetManager, mAppWidgetId);

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget);
                appWidgetManager.updateAppWidget(mAppWidgetId, views);
                
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                
                setResult(RESULT_OK, resultValue);
//                finish();                
                finish();
            }
        });
        
        // request the widget get updated
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, 
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        
        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        if (db != null)
        {
            Log.d(LOG_TAG, "Closing db");
            db.close();
        }

//        Log.i(LOG_TAG, "Sending intent");
//        
//        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
//        ToDoWidgetProvider.updateAppWidget(this, appWidgetManager, mAppWidgetId);
//
//        RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget);
//        appWidgetManager.updateAppWidget(mAppWidgetId, views);
//        
//        Intent resultValue = new Intent();
//        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
//        
//        setResult(RESULT_OK, resultValue);
////        finish();
    }
    
    private TableRow addNote(Note n, ToDoActivity c)
    {
        TableRow row = createRow(c);
        
        EditText note = new EditText(c);
        note.setText(n.text);
        note.setId(n.id);
        if (n.status == Note.Status.FINISHED) note.setTextColor(Color.GRAY);
        
        note.setOnKeyListener(new View.OnKeyListener()
        {
            
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                EditText eb = ((EditText)v);
                Note n = db.getNote(eb.getId());
                n.text = eb.getText().toString();
                db.updateNote(n);
                return false;
            }
        });
        row.addView(note);

        Button b = createButton(c);
        b.setText("F");
        b.setId(n.id);
        b.setOnClickListener(new ButtonStatusClickListener(c));
        row.addView(b);
        
        b = createButton(c);
        b.setText("D");
        b.setId(n.id);
        b.setOnClickListener(new View.OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                new AlertDialog.Builder(v.getContext())
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        Log.i(LOG_TAG,"ID:"+id);
                   }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                   }
               })
               .show();
            }
        });
        row.addView(b);
        
        return row;
    }
    
    public void redraw(ToDoActivity c)
    {
        TableLayout table = (TableLayout) c.findViewById(R.id.table_layout);
        
        table.removeAllViews();

        LinkedList<Note> notes = db.getAllNotes();

        for (Note n : notes)
        {
            table.addView(addNote(n, c));
        }
    }
    
    /**
     * Utility function to create a TableRow and set defaults
     * @param c
     * @return
     */
    private static TableRow createRow(Context c)
    {
        TableRow row = new TableRow(c);
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        row.setPadding(5, 0, 0, 5);
        return row;
    }
    /**
     * Utility function to create a Button and set defaults
     * @param c
     * @return
     */
    private static Button createButton(Context c)
    {
        Button b = new Button(c);
        b.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        b.setPadding(5, 0, 0, 5);
        return b;
    }
    
    class ButtonStatusClickListener implements View.OnClickListener
    {
        ToDoActivity c;
        
        public ButtonStatusClickListener(ToDoActivity c)
        {
            this.c = c;
        }
        
        public void onClick(View v)
        {
            Button b = ((Button)v);
            Note n = db.getNote(b.getId());
            
            n.status = (n.status == Status.FINISHED) ? Status.CREATED : Status.FINISHED;
            Log.i(LOG_TAG, "Setting status of " +n.text+ " to " +n.status+ " ");
           
            db.updateNote(n);
            
            redraw(c);
        }
    }
}
