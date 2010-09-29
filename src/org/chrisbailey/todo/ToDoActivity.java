package org.chrisbailey.todo;

import java.util.LinkedList;

import org.chrisbailey.todo.Note.Status;
import org.chrisbailey.todo.ToDoWidgetProvider.MOVE;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.ImageView.ScaleType;

public class ToDoActivity extends Activity
{
    // reference to the database
    public ToDoDatabase db;
    
    private static final String LOG_TAG = "ToDoActivity";
    static final boolean debug = false;
    private static float scale;
    private PreferenceManager pm;
    
    EditText title;
    
    public static enum FOCUS { GIVE_TO_LAST, GIVE_TO_LAST_WITH_KEYBOARD, NONE };
    
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    
    public ToDoActivity() {
        super();
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        if (debug) Log.d(LOG_TAG, "onCreate");
        
        super.onCreate(savedInstanceState);
        
        scale = getResources().getDisplayMetrics().density;

        setConfigureResult(RESULT_CANCELED);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity);
        
        db = new ToDoDatabase(this.getApplicationContext());

        pm = new PreferenceManager(this, db);
        
        ImageView addnote = (ImageView)findViewById(R.id.addnotebutton);
        addnote.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Note n = new Note(mAppWidgetId);
                db.addNote(n);
                redraw(ToDoActivity.this, FOCUS.GIVE_TO_LAST_WITH_KEYBOARD);
            }
        });

        ImageView done = (ImageView)findViewById(R.id.donebutton);
        done.setOnClickListener(new View.OnClickListener()
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
            if (debug) Log.d(LOG_TAG, "Invalid app id, finishing");
            finish();
        }
        
        title = (EditText)findViewById(R.id.edittitle);
        title.setId(mAppWidgetId);
        title.setText(db.getTitle(mAppWidgetId));
        title.addTextChangedListener(new MyTitleTextWatcher(title));
        title.setBackgroundResource(R.drawable.input_background);
        
        redraw(this, FOCUS.GIVE_TO_LAST);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editormenu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_configure:
                Intent intent = new Intent(ToDoActivity.this, PreferencesActivity.class);
                ToDoActivity.this.startActivityForResult(intent, 0);
                return true;
        }
        
        return false;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            done();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private void done()
    {
        if (db != null)
        {
            db.close();
            db = null;
        }

        if (debug) Log.i(LOG_TAG, "Sending intents to all widgets");
        
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        ComponentName THIS_APPWIDGET = new ComponentName(getApplicationContext(), ToDoWidgetProvider.class);
        int [] ids = appWidgetManager.getAppWidgetIds(THIS_APPWIDGET);
        for (int i : ids)
        {
            ToDoWidgetProvider.updateAppWidget(getApplicationContext(), appWidgetManager, i, MOVE.NONE);
        }
        THIS_APPWIDGET = new ComponentName(getApplicationContext(), MediumToDoWidget.class);
        ids = appWidgetManager.getAppWidgetIds(THIS_APPWIDGET);
        for (int i : ids)
        {
            ToDoWidgetProvider.updateAppWidget(getApplicationContext(), appWidgetManager, i, MOVE.NONE);
        }
        THIS_APPWIDGET = new ComponentName(getApplicationContext(), LargeToDoWidget.class);
        ids = appWidgetManager.getAppWidgetIds(THIS_APPWIDGET);
        for (int i : ids)
        {
            ToDoWidgetProvider.updateAppWidget(getApplicationContext(), appWidgetManager, i, MOVE.NONE);
        }
        
        setConfigureResult(RESULT_OK);
        finish();
    }
    
    private TableRow addNote(Note n, ToDoActivity c)
    {
        TableRow row = createRow(c);
        
        int btn = pm.getActiveIcon();
        ImageView toggle = createImage(c, getResources().getDrawable(btn));
        toggle.setId(n.id);
        toggle.setOnClickListener(new StatusClickListener(c));
        
        row.addView(toggle);
        
        EditText note = createInput(c);
        note.setPadding(0, 0, 0, 0);
        note.setBackgroundResource(R.drawable.input_background);
        note.setText(n.text);
        note.setId(n.id);
        note.addTextChangedListener(new MyTextWatcher(note));
        row.addView(note);

        ImageView delete = createImage(c, getResources().getDrawable(R.drawable.action_delete));
        delete.setId(n.id);
        delete.setOnClickListener(new DeleteClickListener(c));
        row.addView(delete);
        
        toggleRow(row, n.status);
        
        return row;
    }
    
    public void toggleRow(TableRow row, Status status)
    {
        int btn = pm.getActiveIcon();
        if (status == Status.FINISHED) btn = pm.getFinishedIcon();
        
        ImageView toggle = (ImageView) row.getChildAt(0);
        toggle.setImageDrawable(getResources().getDrawable(btn));
        
        EditText note = (EditText) row.getChildAt(1);
        if (status == Note.Status.FINISHED) note.setTextColor(pm.getFinishedColor());
        else note.setTextColor(pm.getActiveColor());
    }
    
    public void redraw(ToDoActivity c, FOCUS focus)
    {
        title.setTextColor(pm.getActiveColor());
        
        TableLayout table = (TableLayout) c.findViewById(R.id.table_layout);
        
        table.removeAllViews();

        LinkedList<Note> notes = db.getAllNotes(mAppWidgetId);
        if (notes.size() == 0)
        {
            db.addNote(new Note(mAppWidgetId));
            notes = db.getAllNotes(mAppWidgetId);
            focus = FOCUS.GIVE_TO_LAST_WITH_KEYBOARD;
        }

        for (int i = 0; i < notes.size(); i++)
        {
            Note n = notes.get(i);
            TableRow row = addNote(n, c);
            table.addView(row);

            if (i == notes.size() - 1)
            {
                EditText et = (EditText) row.getChildAt(1);
                if (focus != FOCUS.NONE) et.requestFocus();

                // give new note focus
                if (focus == FOCUS.GIVE_TO_LAST_WITH_KEYBOARD) et.postDelayed(new ShowKeyboardRunnable(et), 200);
                
            }
        }
        
        if (focus == FOCUS.NONE && c.getCurrentFocus() != null) c.getCurrentFocus().clearFocus();
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
        row.setVerticalGravity(Gravity.TOP);
        row.setPadding(0, 0, 0, 0);
        return row;
    }

    
    /**
     * Utility function to create an image and set defaults
     * @param c
     * @return
     */
    private static ImageView createImage(Context c, Drawable drawable)
    {
        ImageView iv = new ImageView(c);
        iv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT));
        iv.setImageDrawable(drawable);
        iv.setScaleType(ScaleType.CENTER);
        iv.setPadding(0, 0, 0, 0);
        return iv;
    }
    
    private static EditText createInput(Context c)
    {
        EditText et = new EditText(c);
        et.setWidth((int)(scale * 200));
        return et;
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

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent result)
    {
        // we don't care about the result, just refresh
        if (debug) Log.i(LOG_TAG,"onActivityResult");
        
        // reload settings
        pm = new PreferenceManager(this, db);
        
        redraw(ToDoActivity.this, FOCUS.GIVE_TO_LAST);
    }
    
    class DeleteClickListener implements View.OnClickListener
    {
        ToDoActivity c;
        int noteId = -1;
        
        public DeleteClickListener(ToDoActivity c)
        {
            this.c = c;
        }
        
        public void onClick(View v)
        {
            ImageView b = ((ImageView)v);
            Note n = db.getNote(b.getId());
            String name = n.text;
            if (name == null) name = "";
            if (name.length() > 5) name = name.substring(0,5)+"...";
            if (name.length() > 0) name = "(" + name + ")";
            String message = getString(R.string.delete_confirm);
            message = message.replace("[note]", name);
            
            String confirm = getString(android.R.string.ok);
            String cancel = getString(android.R.string.cancel);
            
            noteId = n.id;

            new AlertDialog.Builder(v.getContext())
            .setMessage(message)
            .setPositiveButton(confirm, new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                    Note n = db.getNote(noteId);
                    db.deleteNote(n);
                    redraw(c, FOCUS.NONE);
               }
           })
           .setNegativeButton(cancel, new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
               }
           })
           .show();
        }
    }
    
    class StatusClickListener implements View.OnClickListener
    {
        ToDoActivity c;
        
        public StatusClickListener(ToDoActivity c)
        {
            this.c = c;
        }
        
        public void onClick(View v)
        {
            ImageView b = ((ImageView)v);
            Note n = db.getNote(b.getId());
            
            n.status = (n.status == Status.FINISHED) ? Status.CREATED : Status.FINISHED;
           
            db.updateNote(n);
            
            toggleRow((TableRow)(v.getParent()), n.status);
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
        	try
        	{
	            Note n = db.getNote(et.getId());
	            n.text = et.getText().toString();
	            db.updateNote(n);
        	}
        	catch (NullPointerException npe) { /* do nothing */ }
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                int after)
        {}

        public void onTextChanged(CharSequence s, int start, int before,
                int count)
        { }
    }

    public class MyTitleTextWatcher extends MyTextWatcher
    {
        public MyTitleTextWatcher(EditText et)
        {
            super(et);
        }

        @Override
        public void afterTextChanged(Editable s)
        {
            int id= et.getId();
            String str = et.getText().toString();
            db.setTitle(id, str);
        }
    }
    
    public class ShowKeyboardRunnable implements Runnable
    {
        EditText et;
        
        public ShowKeyboardRunnable(EditText et)
        {
            this.et = et;
        }

        public void run()
        {
            InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(et, 0);
        }
    }
}
