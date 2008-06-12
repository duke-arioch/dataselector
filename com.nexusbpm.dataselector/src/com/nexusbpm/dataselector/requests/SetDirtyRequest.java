package com.nexusbpm.dataselector.requests;

import com.nexusbpm.multipage.bus.BusRequest;

public class SetDirtyRequest implements BusRequest {
    private boolean dirty;
    
    public SetDirtyRequest(boolean dirty) {
        this.dirty = dirty;
    }
    
    public boolean isDirty() {
        return dirty;
    }
    
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + dirty + ")";
    }
}
