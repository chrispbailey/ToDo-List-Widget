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
package org.chrisbailey.todo.utils;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


public class Note
{
		public enum Status { CREATED(0), FINISHED(1);

        private static final Map<Integer,Status> lookup = new HashMap<Integer,Status>();
    
        static {
            for(Status s : EnumSet.allOf(Status.class))
            {
                lookup.put(s.getCode(), s);
            }
        }
        
        private int code;
        
        Status(int code) {
            this.code = code;
        }
    
        public int getCode() { return code; }
        
        public static Status get(int code) { 
            return lookup.get(code); 
        }
    }

    public int id;
    public String text;
    public Status status;
    public Long created;
    public int list;
    private static final int EMPTYNOTE = -1;
    
    public Note(int list)
    {
        this.id = EMPTYNOTE;
        this.list = list;
        this.status = Status.CREATED;
        this.created = System.currentTimeMillis();
    }
    
    public boolean isNew() { return id == EMPTYNOTE; }
}
