package org.chrisbailey.todo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;

public class ImageGetter implements Html.ImageGetter
{
    Context c;
    
    public ImageGetter(Context c)
    {
        this.c = c;
    }
    
    public Drawable getDrawable(String source) {
        Drawable d = null;
        int resID = c.getResources().getIdentifier(source, "drawable", c.getPackageName());

        Log.d(ToDoWidgetProvider.LOG_TAG,"RES:"+resID);
        
        d = c.getApplicationContext().getResources().getDrawable(resID);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());

        return d;
    }
}