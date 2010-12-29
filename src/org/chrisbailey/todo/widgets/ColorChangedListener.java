/*******************************************************************************
 * ToDo List Widget - Android homescreen note taking application
 * Copyright (C) 2011  Chris Bailey
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.chrisbailey.todo.widgets;

import android.view.View;

/**
 * Interface for notifications of position change of slider.
 * 
 * @author Peli
 */
public interface ColorChangedListener {

	/**
	 * This method is called when the user changed the color.
	 * 
	 * This works in touch mode, by dragging the along the 
	 * color circle with the finger.
	 */
	void onColorChanged(View view, int newColor);
	
	/**
	 * This method is called when the user clicks the center button.
	 * 
	 * @param colorcircle
	 * @param newColor
	 */
	void onColorPicked(View view, int newColor);
}
