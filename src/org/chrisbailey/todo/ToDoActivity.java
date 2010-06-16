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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

public class ToDoActivity extends Activity
{
    // reference to the database
    public ToDoDatabase db;
    
    private static final String LOG_TAG = "ToDoActivity";
    
    public static final String DONE_COLOR = "#777777";
    
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    
    public ToDoActivity() {
        super();
    }

    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOG_TAG, "onCreate");
        
        super.onCreate(savedInstanceState);
        
        setConfigureResult(RESULT_CANCELED);

        setContentView(R.layout.main);
        
        db = new ToDoDatabase(this.getApplicationContext());
        
        redraw(this);

        Button addnotebutton = (Button)findViewById(R.id.addnotebutton);
        addnotebutton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                db.addNote(new Note());
                redraw(ToDoActivity.this);
            }
        });
        
        Button donebutton = (Button)findViewById(R.id.donebutton);
        donebutton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                done();
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
            Log.d(LOG_TAG, "Invalid app id, finishing");
            finish();
        }
    }
    
    
    @Override
    public void onPause()
    {
        Log.d(LOG_TAG, "onPause");
        super.onPause();
    }
    
    @Override
    public void onStop()
    {
        Log.d(LOG_TAG, "onStop");
        super.onPause();
    }
    
    @Override
    public void onDestroy()
    {
        Log.d(LOG_TAG, "onDestroy");
        super.onPause();
    }
    
    private void done()
    {
        if (db != null)
        {
            Log.d(LOG_TAG, "Closing db");
            db.close();
            db = null;
        }

        Log.i(LOG_TAG, "Sending intent");
        
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        ToDoWidgetProvider.updateAppWidget(getApplicationContext(), appWidgetManager, mAppWidgetId);
        
        setConfigureResult(RESULT_OK);
        finish();
    }
    
    private TableRow addNote(Note n, ToDoActivity c)
    {
        TableRow row = createRow(c);
        
        EditText note = new EditText(c);

        note.setText(n.text);
        note.setId(n.id);
        if (n.status == Note.Status.FINISHED) note.setTextColor(Color.parseColor(DONE_COLOR));
        note.addTextChangedListener(new MyTextWatcher(note));        

        row.addView(note);

        Button b = createButton(c);
        b.setText("F");
        b.setId(n.id);
        b.setOnClickListener(new ButtonStatusClickListener(c));
        row.addView(b);
        
        b = createButton(c);
        b.setText("D");
        b.setId(n.id);
        b.setOnClickListener(new ButtonDeleteClickListener(c));
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
    
    /**
     * Convenience method to always include {@link #mAppWidgetId} when setting
     * the result {@link Intent}.
     */
    public void setConfigureResult(int resultCode) {
        final Intent data = new Intent();
        data.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(resultCode, data);
    }
    
    class ButtonDeleteClickListener implements View.OnClickListener
    {
        ToDoActivity c;
        int noteId = -1;
        
        public ButtonDeleteClickListener(ToDoActivity c)
        {
            this.c = c;
        }
        
        public void onClick(View v)
        {
            Button b = ((Button)v);
            Note n = db.getNote(b.getId());
            
            noteId = n.id;
            new AlertDialog.Builder(v.getContext())
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                    Log.i(LOG_TAG,"ID:"+noteId);
                    Note n = db.getNote(noteId);
                    db.deleteNote(n);
                    redraw(c);
               }
           })
           .setNegativeButton("No", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
               }
           })
           .show();
        }
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
    
    public class MyTextWatcher implements TextWatcher
    {
        EditText et;
        
        public MyTextWatcher(EditText et)
        {
            this.et = et;
        }

        public void afterTextChanged(Editable s)
        {
            Note n = db.getNote(et.getId());
            n.text = et.getText().toString();
            Log.i(LOG_TAG,"Saving tag");
            db.updateNote(n);
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                int after)
        {}

        public void onTextChanged(CharSequence s, int start, int before,
                int count)
        { }
    }
}
