package org.chrisbailey.todo;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


public class Note
{
    enum Status { CREATED(0), FINISHED(1);

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
