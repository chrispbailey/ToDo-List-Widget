package org.chrisbailey.todo;

import java.util.HashMap;
import java.util.LinkedList;

import org.chrisbailey.todo.Note.Status;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ToDoDatabase extends SQLiteOpenHelper
{
    // The name of the database file on the file system
    private static final String DATABASE_NAME = "ToDoWidget";

    // The version of the database that this class understands
    private static final int DATABASE_VERSION = 2;

    // static table name identifier
    public static final String VARIABLE_TABLE_NAME = "variables";
    public static final String NOTE_TABLE_NAME = "notes";

    // SQL create query
    private final static String CREATE_SQL = 
       "CREATE TABLE " + VARIABLE_TABLE_NAME + " (name TEXT PRIMARY KEY, value TEXT);\n" +
       "CREATE TABLE " + NOTE_TABLE_NAME + " (list INT, name TEXT, status INT, created INT);";

    private final static String LOG_TAG = "ToDoDatabase";
    
    private final static String TITLE_KEY = "title_";
    
    // create all required upgrade paths as a hashmap
    private final static HashMap<String,String> UPGRADE_SQL 
        = new HashMap<String,String>();
    
    static
    {
        UPGRADE_SQL.put("1-2", "CREATE TABLE " + VARIABLE_TABLE_NAME + " (name TEXT PRIMARY KEY, value TEXT);");
    }
    
    /**
     * Default constructor
     * 
     * @param context
     */
    public ToDoDatabase(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Adds a cross-session parameter to the database
     * 
     * @param name
     * @param value
     */
    private void setVariable(String name, String value)
    {
        String sql = "REPLACE INTO " + VARIABLE_TABLE_NAME
                + " (name,value) VALUES (?,?)";
        
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();

            db.beginTransaction();
            try
            {
                db.execSQL(sql, new Object[] { name, value });
                db.setTransactionSuccessful();
            }
            catch (Exception e)
            {
                Log.e(LOG_TAG, "Error writing variable info to database [" + name + "," + value + "]", e);
            }
            finally
            {
                db.endTransaction();
            }
        }
        catch (Exception e) 
        {
            Log.e(LOG_TAG, "Unable to open database for writing", e);
        }
    }

    
    /**
     * Reads the value of the parameter identified by <code>name</code>
     * 
     * @param name
     * @return String value of the parameter, <code>null</code> otherwise
     */
    private String getVariable(String name)
    {
        String[] cols = new String[] { "value" };
        String[] whereArgs = new String[] { name };

        Cursor c = null;

        String result = null;
        try
        {
            SQLiteDatabase db = this.getReadableDatabase();
            c = db.query(VARIABLE_TABLE_NAME, cols,
                    "name=?", whereArgs, null, null, null);
            boolean hasResult = c.moveToFirst();
            if (hasResult && !c.isNull(0))
            {
                result = c.getString(0);
            }
            c.close();
            db.close();
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG, "Error getting variable [" + name + "]", e);
        }
        finally
        {
            if (null != c)
            {
                try
                {
                    c.close();
                }
                catch (Exception e)
                {
                    Log.e(LOG_TAG,"Error closing cursor",e);
                }
            }
        }
        return result;
    }
    
    /**
     * Remove a cross-session parameter to the database
     * 
     * @param name
     */
    private void deleteVariable(String name)
    {
        String sql = "DELETE FROM " + VARIABLE_TABLE_NAME
                + " WHERE name=?";
        
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();

            db.beginTransaction();
            try
            {
                db.execSQL(sql, new Object[] { name });
                db.setTransactionSuccessful();
            }
            catch (Exception e)
            {
                Log.e(LOG_TAG, "Error removing variable from database [" + name + "]", e);
            }
            finally
            {
                db.endTransaction();
            }
        }
        catch (Exception e) 
        {
            Log.e(LOG_TAG, "Unable to open database for writing", e);
        }
    }

    public void setTitle(int id, String value)
    {
        setVariable(TITLE_KEY + id, value);
    }
    
    public String getTitle(int id)
    {
        String s = getVariable(TITLE_KEY + id);
        if (s == null) return "";
        return s;
    }
    
    public void deleteTitle(int id)
    {
        deleteVariable(TITLE_KEY + id);
    }
    
    /**
     * Adds a note to the database
     * 
     * @param name
     * @param value
     */
    public void addNote(Note n)
    {
        String sql = "REPLACE INTO " + NOTE_TABLE_NAME + " (list, name, status, created) VALUES (?, ?,?,?)";
        
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();

            db.beginTransaction();
            try
            {
                db.execSQL(sql, new Object[] { n.list, n.text, n.status, n.created });
                db.setTransactionSuccessful();
            }
            catch (Exception e)
            {
                Log.e(LOG_TAG, "Error writing note to database [" + n.text + "]", e);
            }
            finally
            {
                db.endTransaction();
            }
        }
        catch (Exception e) 
        {
            Log.e(LOG_TAG, "Unable to open database for writing", e);
        }
    }
    
    /**
     * Update a given note in the database 
     * @param n
     */
    public void updateNote(Note n)
    {
        if (n.isNew()) addNote(n);
        else
        {
            String sql = "REPLACE INTO " + NOTE_TABLE_NAME + " (rowid, list, name, status) VALUES (?,?,?,?)";
            
            try
            {
                SQLiteDatabase db = this.getWritableDatabase();
    
                db.beginTransaction();
                try
                {
                    Log.i(LOG_TAG,"Saving status of "+n.text+" to "+n.status);
                    db.execSQL(sql, new Object[] { n.id, n.list, n.text, n.status.getCode() });
                    db.setTransactionSuccessful();
                }
                catch (Exception e)
                {
                    Log.e(LOG_TAG, "Error writing note to database [" + n.text + "]", e);
                }
                finally
                {
                    db.endTransaction();
                }
            }
            catch (Exception e) 
            {
                Log.e(LOG_TAG, "Unable to open database for writing", e);
            }
        }
    }
    
    /**
     * Retrieve a note based on the note id
     * @param noteId
     * @return
     */
    public Note getNote(int noteId)
    {
        String[] cols = new String[] { "list", "name", "status", "created" };
        String[] whereArgs = new String[] { noteId+"" };

        Cursor c = null;

        try
        {
            SQLiteDatabase db = this.getReadableDatabase();
            c = db.query(NOTE_TABLE_NAME, cols,
                    "rowid=?", whereArgs, null, null, null);
            boolean hasResult = c.moveToFirst();
            if (hasResult && !c.isNull(0))
            {
                Note n = new Note(c.getInt(0));
                n.id = noteId;
                n.text = c.getString(1);
                n.status = Status.get(c.getInt(2));
                if (n.status == null) n.status = Status.CREATED;
                n.created = c.getLong(3);
                return n;
            }
            c.close();
            db.close();
        }
        catch (IllegalStateException e)
        {
            Log.e(LOG_TAG, "Hi - caught you", e);
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG, "Error getting note [" + noteId + "]", e);
        }
        finally
        {
            if (null != c)
            {
                try
                {
                    c.close();
                }
                catch (Exception e)
                {
                    Log.e(LOG_TAG,"Error closing cursor",e);
                }
            }
        }
        
        Log.w(LOG_TAG, "c: "+ c.isClosed() + " : " + c.toString());

        c = null;
        
        return null;
    }

    /**
     * Deletes a given note
     * @param n
     */
    public void deleteNote(Note n)
    {
        String sql = "DELETE FROM " + NOTE_TABLE_NAME + " WHERE rowid = ?";
        
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();

            db.beginTransaction();
            try
            {
                db.execSQL(sql, new Object[] { n.id });
                db.setTransactionSuccessful();
            }
            catch (Exception e)
            {
                Log.e(LOG_TAG, "Error removing note from database [" + n.text + "]", e);
            }
            finally
            {
                db.endTransaction();
            }
        }
        catch (Exception e) 
        {
            Log.e(LOG_TAG, "Unable to open database for writing", e);
        }
    }

    /**
     * Delete all notes associated with a given list
     * @param list
     */
    public void deleteAllNotes(int list)
    {
        String sql = "DELETE FROM " + NOTE_TABLE_NAME + " WHERE list = ?";
        Log.d(LOG_TAG, "deleteAllNotes for " + list);
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();

            db.beginTransaction();
            try
            {
                db.execSQL(sql, new Object[] { list });
                db.setTransactionSuccessful();
            }
            catch (Exception e)
            {
                Log.e(LOG_TAG, "Error removing all note from database for list " + list, e);
            }
            finally
            {
                db.endTransaction();
            }
        }
        catch (Exception e) 
        {
            Log.e(LOG_TAG, "Unable to open database for writing", e);
        }
    }
    
    /**
     * Get all notes for a given list id
     * @param list
     * @return
     */
    public LinkedList<Note> getAllNotes(int list)
    {
        LinkedList<Note> results = new LinkedList<Note>();
        
        String[] cols = new String[] { "rowid", "name", "status", "created" };
        String[] whereArgs = new String[] { list+"" };
        
        Cursor c = null;

        try
        {
            SQLiteDatabase db = this.getReadableDatabase();
            c = db.query(NOTE_TABLE_NAME, cols, "list=?", whereArgs, null, null, null);
            while (c.moveToNext())
            {
                Note n = new Note(list);
                n.id = c.getInt(0);
                n.text = c.getString(1);
                n.status = Note.Status.get(c.getInt(2));
                n.created = c.getLong(3);
                results.add(n);
            }
            c.close();
            db.close();
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG, "Error getting all notes", e);
        }
        finally
        {
            if (null != c)
            {
                try
                {
                    c.close();
                }
                catch (Exception e)
                {
                    Log.e(LOG_TAG,"Error closing cursor",e);
                }
            }
        }

        c = null;
        return results;
    }
    
    /**
     * Called when it is time to create the database
     * 
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String[] sql = CREATE_SQL.split("\n");
        boolean success = execMultipleSQL(db, sql);
        if (!success)
            Log.e(LOG_TAG, "Unable to create database");
    }

    /**
     * Executes the queries specified in the strings.xml file.<br/>
     * Called if the user's database does not match the one specified in the 
     * AndroidManifest.xml file.<br/>
     *  
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        String upgradeKey = oldVersion + "-" + newVersion;
        Log.i(LOG_TAG, "upgrading " + upgradeKey);

        boolean success = false;
        if (UPGRADE_SQL.containsKey(upgradeKey))
        {
            String[] sql = UPGRADE_SQL.get(upgradeKey).split("\n");
            success = execMultipleSQL(db, sql);
        }
        if (!success)
        {
            Log.e(LOG_TAG, "Unable to upgrade the database (" + upgradeKey + ")");
        }
    }

    /**
     * Execute all of the SQL statements in the supplied String array
     * 
     * @param db
     *            The database on which to execute the statements
     * @param sql
     *            An array of SQL statements to execute
     * @return boolean indicating success state
     */
    private boolean execMultipleSQL(SQLiteDatabase db, String[] sql)
    {
        boolean returnFlag = false;

        db.beginTransaction();
        try
        {
            for (String s : sql)
            {
                if (s.trim().length() > 0) db.execSQL(s);
            }
            db.setTransactionSuccessful();

            returnFlag = true;
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG, "Error with sql statement", e);
        }
        finally
        {
            db.endTransaction();
        }

        return returnFlag;
    }
    
    public void close()
    {
        Log.i(LOG_TAG, "Closing db");
        getWritableDatabase().close();
        super.close();
    }
}
