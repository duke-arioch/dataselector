package com.nexusbpm.multipage.bus;

import java.util.ArrayList;
import java.util.List;

/**
 * A bus for sending both events and requests.
 */
public class EventRequestBus {
    private List<BusEventListener> listeners;
    private List<BusRequestHandler> handlers;
    
    public EventRequestBus() {
        listeners = new ArrayList<BusEventListener>();
        handlers = new ArrayList<BusRequestHandler>();
    }
    
    public synchronized void addEventListener(BusEventListener listener) {
        if(!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public synchronized void removeEventListener(BusEventListener listener) {
        listeners.remove(listener);
    }
    
    public synchronized void addRequestHandler(BusRequestHandler handler) {
        if(!handlers.contains(handler)) {
            handlers.add(handler);
        }
    }
    
    public synchronized void removeRequestHandler(BusRequestHandler handler) {
        handlers.remove(handler);
    }
    
    protected synchronized BusEventListener[] getListeners() {
        return listeners.toArray(new BusEventListener[listeners.size()]);
    }
    
    protected synchronized BusRequestHandler[] getHandlers() {
        return handlers.toArray(new BusRequestHandler[handlers.size()]);
    }
    
    public void handleEvent(BusEvent event) {
        handleBusAware(event);
        for(BusEventListener listener : getListeners()) {
            // TODO this try-catch is only here to aid development
            try {
                listener.handleEvent(event);
            } catch(Throwable t) {
                t.printStackTrace();
            }
        }
    }
    
    public Object handleRequest(BusRequest request) throws UnhandledRequestException {
        if(request == null) {
            throw new UnhandledRequestException("Cannot handle a null request!");
        }
        handleBusAware(request);
        for(BusRequestHandler handler : getHandlers()) {
            if(handler.canHandleRequest(request)) {
                // TODO this try-catch is only here to aid development
                try {
                    return handler.handleRequest(request);
                } catch(Throwable t) {
                    t.printStackTrace();
                    return null;
                }
            }
        }
        System.err.println("unhandled request:" + request); // TODO remove later
        throw new UnhandledRequestException(request);
    }
    
    protected void handleBusAware(Object object) {
        if(object instanceof BusAware) {
            ((BusAware) object).setEventRequestBus(this);
        }
    }
}
