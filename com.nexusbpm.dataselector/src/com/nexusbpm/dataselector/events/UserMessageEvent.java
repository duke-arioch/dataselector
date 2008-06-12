package com.nexusbpm.dataselector.events;

import com.nexusbpm.multipage.bus.BusEvent;

public class UserMessageEvent implements BusEvent {
    public static final int STATUS_OK = 1;
    public static final int STATUS_WARNING = 2;
    public static final int STATUS_ERROR = 3;
    
    private String title;
    private String message;
    private int status;
    
    public UserMessageEvent(String title, String message) {
        this.title = title;
        this.message = message;
        this.status = STATUS_OK;
    }
    
    public UserMessageEvent(String title, String message, int status) {
        this.title = title;
        this.message = message;
        setStatus(status);
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        if(status != STATUS_WARNING && status != STATUS_ERROR) {
            this.status = STATUS_OK;
        } else {
            this.status = status;
        }
    }
}
