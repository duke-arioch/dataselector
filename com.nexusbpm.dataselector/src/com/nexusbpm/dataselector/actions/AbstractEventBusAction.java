package com.nexusbpm.dataselector.actions;

import com.nexusbpm.dataselector.events.ExceptionEvent;
import com.nexusbpm.multipage.bus.BusEvent;
import com.nexusbpm.multipage.bus.BusRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;
import com.nexusbpm.multipage.bus.UnhandledRequestException;

public abstract class AbstractEventBusAction extends ValidatedAction {
    private EventRequestBus bus;
    
    public AbstractEventBusAction(String text, int style, EventRequestBus bus) {
        super(text, style);
        this.bus = bus;
    }
    
    protected EventRequestBus getEventRequestBus() {
        return bus;
    }
    
    protected void setEventRequestBus(EventRequestBus bus) {
        this.bus = bus;
    }
    
    protected void sendEvent(BusEvent event) {
        bus.handleEvent(event);
    }
    
    protected void sendRequest(BusRequest request) {
        try {
            bus.handleRequest(request);
        } catch(UnhandledRequestException e) {
            sendEvent(new ExceptionEvent("Event bus not configured!", e));
        }
    }
}
