package org.chrisbailey.todo.widgets;

import org.chrisbailey.todo.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class ColorPickerDialog extends Dialog implements ColorChangedListener
{
    
    private OnColorChangedListener mListener;
    ColorCircle mColorCircle;
    ColorSlider mSaturation;
    ColorSlider mValue;

    int defaultColor = Color.BLACK;

    public interface OnColorChangedListener 
    {
        void onColorChanged(int color);
    }

    public ColorPickerDialog(Context context, OnColorChangedListener listener, int initialColor)
    {
        super(context);
        
        mListener = listener;
        defaultColor = initialColor;
    }
    
    public void setColor(int color)
    {
        defaultColor = color;
        mColorCircle.setColor(defaultColor);
        mSaturation.setColors(defaultColor, Color.BLACK);        
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.colorpicker);
        
        mColorCircle = (ColorCircle) findViewById(R.id.colorcircle);
        mColorCircle.setOnColorChangedListener(this);
        mColorCircle.setColor(defaultColor);
    
        mSaturation = (ColorSlider) findViewById(R.id.saturation);
        mSaturation.setOnColorChangedListener(this);
        mSaturation.setColors(defaultColor, Color.BLACK);
    
        mValue = (ColorSlider) findViewById(R.id.value);
        mValue.setOnColorChangedListener(this);
        mValue.setColors(Color.WHITE, defaultColor);
    }
    
    class ColorPickerState {
        int mColor;
    }

    public int toGray(int color) 
    {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int gray = (r + g + b) / 3;
        return Color.argb(a, gray, gray, gray);
    }
    
    
    public void onColorChanged(View view, int newColor)
    {
        if (view == mColorCircle) {
                mValue.setColors(0xFFFFFFFF, newColor);
        mSaturation.setColors(newColor, 0xff000000);
        } else if (view == mSaturation) {
                mColorCircle.setColor(newColor);
                mValue.setColors(0xFFFFFFFF, newColor);
        } else if (view == mValue) {
                mColorCircle.setColor(newColor);
        }
    }

    
    public void onColorPicked(View view, int newColor) 
    {
        // We can return result
        mListener.onColorChanged(newColor);
        dismiss();
    }
}
