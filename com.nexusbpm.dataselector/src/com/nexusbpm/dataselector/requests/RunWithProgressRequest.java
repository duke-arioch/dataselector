package com.nexusbpm.dataselector.requests;

import com.nexusbpm.multipage.bus.BusRequest;

public class RunWithProgressRequest implements BusRequest {
    private Runnable runnable;
    private String action;
    private String description;
    
    public RunWithProgressRequest(Runnable runnable, String action, String description) {
        this.runnable = runnable;
        this.action = action;
        this.description = description;
    }
    
    public Runnable getRunnable() {
        return runnable;
    }
    
    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + action + ":" + description + ":" + runnable + ")";
    }
}
