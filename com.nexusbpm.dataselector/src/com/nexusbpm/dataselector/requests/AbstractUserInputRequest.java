package com.nexusbpm.dataselector.requests;

import com.nexusbpm.multipage.bus.BusRequest;

public abstract class AbstractUserInputRequest implements BusRequest {
    private boolean cancelled;
    
    public AbstractUserInputRequest() {
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
