package com.nexusbpm.dataselector.model;

import com.nexusbpm.dataselector.requests.SetDirtyRequest;

public abstract class AbstractModelExtension extends NamedModelElement {
    public AbstractModelExtension(AbstractModelElement parent) {
        super(parent);
    }
    
    protected void markDirty() {
        try {
            if(getTree().getEventRequestBus() != null) {
                getTree().getEventRequestBus().handleRequest(new SetDirtyRequest(true));
            }
        } catch(Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
