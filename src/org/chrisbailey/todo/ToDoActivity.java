package org.chrisbailey.todo;

import java.util.LinkedList;

import org.chrisbailey.todo.Note.Status;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

public class ToDoActivity extends Activity
{
    // reference to the database
    public ToDoDatabase db;
    
    private static final String LOG_TAG = "ToDoActivity";
    
    public static enum FOCUS { GIVE_TO_LAST, GIVE_TO_LAST_WITH_KEYBOARD, NONE };
    
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        db = new ToDoDatabase(this.getApplicationContext());

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
            Log.d(LOG_TAG, "Invalid app id, finishing");
            finish();
        }
        
        EditText title = (EditText)findViewById(R.id.edittitle);
        title.setId(mAppWidgetId);
        title.setText(db.getTitle(mAppWidgetId));
        title.addTextChangedListener(new MyTitleTextWatcher(title));
        title.setBackgroundResource(R.drawable.input_background);
        redraw(this, FOCUS.GIVE_TO_LAST);
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

        Log.i(LOG_TAG, "Sending intent");
        
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        ToDoWidgetProvider.updateAppWidget(getApplicationContext(), appWidgetManager, mAppWidgetId);
        
        setConfigureResult(RESULT_OK);
        finish();
    }
    
    private TableRow addNote(Note n, ToDoActivity c)
    {
        TableRow row = createRow(c);
        
        ImageView toggle = createImage(c);
        int btn = R.drawable.tickbox;
        if (n.status == Status.FINISHED) btn = R.drawable.tick;
        toggle.setImageDrawable(getResources().getDrawable(btn));
        toggle.setId(n.id);
        toggle.setOnClickListener(new StatusClickListener(c));
        row.addView(toggle);
        
        EditText note = createInput(c);
        note.setPadding(0, 0, 0, 0);
        note.setBackgroundResource(R.drawable.input_background);
        note.setText(n.text);
        note.setId(n.id);
        if (n.status == Note.Status.FINISHED) note.setTextColor(getResources().getColor(R.color.done_color));
        else note.setTextColor(getResources().getColor(R.color.widget_item_color));
        note.addTextChangedListener(new MyTextWatcher(note));
        row.addView(note);

        ImageView delete = createImage(c);
        delete.setImageDrawable(getResources().getDrawable(R.drawable.delete));
        delete.setId(n.id);
        delete.setOnClickListener(new DeleteClickListener(c));
        row.addView(delete);
        
        return row;
    }
    
    public void redraw(ToDoActivity c, FOCUS focus)
    {
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
        row.setGravity(Gravity.CENTER_HORIZONTAL);
        row.setPadding(0, 0, 0, 0);
        return row;
    }

    
    /**
     * Utility function to create an image and set defaults
     * @param c
     * @return
     */
    private static ImageView createImage(Context c)
    {
        ImageView iv = new ImageView(c);
        iv.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        iv.setPadding(4, 0, 4, 0);
        return iv;
    }
    
    private static EditText createInput(Context c)
    {
        EditText et = new EditText(c);
        et.setWidth(200);
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
            
            int btn = R.drawable.tickbox;
            if (n.status == Status.FINISHED) btn = R.drawable.tick;
            b.setImageDrawable(getResources().getDrawable(btn));
            
            redraw(c, FOCUS.GIVE_TO_LAST);
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
            db.updateNote(n);
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
