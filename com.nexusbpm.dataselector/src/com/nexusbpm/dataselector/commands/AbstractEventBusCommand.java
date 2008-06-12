package com.nexusbpm.dataselector.commands;

import org.eclipse.gef.commands.Command;

import com.nexusbpm.dataselector.events.ExceptionEvent;
import com.nexusbpm.multipage.bus.BusEvent;
import com.nexusbpm.multipage.bus.BusRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;
import com.nexusbpm.multipage.bus.UnhandledRequestException;

public abstract class AbstractEventBusCommand extends Command {
    private EventRequestBus eventBus;
    
    public AbstractEventBusCommand(String label, EventRequestBus eventBus) {
        super(label);
        this.eventBus = eventBus;
    }
    
    protected void sendEvent(BusEvent event) {
        if(eventBus == null) {
            System.err.println("Cannot handle event " + event + " because the event bus was not set!");
        }
        eventBus.handleEvent(event);
    }
    
    protected void sendRequest(BusRequest request) {
        if(eventBus == null) {
            System.err.println("Cannot handle request " + request + " because the event bus was not set!");
        }
        try {
            eventBus.handleRequest(request);
        } catch(UnhandledRequestException e) {
            eventBus.handleEvent(new ExceptionEvent("Invalid event bus configuration", e));
        }
    }
    
    @Override
    public boolean canUndo() {
        return false;
    }
}
