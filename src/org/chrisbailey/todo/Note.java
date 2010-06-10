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
    
//    public Note(String note)
//    {
//        this.id = -1;
//        this.text = note;
//        status = Status.CREATED;
//    }
    
    public Note()
    {
        this.id = -1;
        this.status = Status.CREATED;
        this.created = System.currentTimeMillis();
    }
    
    public boolean isNew() { return id == -1; }
}
