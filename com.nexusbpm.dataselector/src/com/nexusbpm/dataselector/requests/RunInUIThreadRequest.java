package com.nexusbpm.dataselector.requests;

import com.nexusbpm.dataselector.events.ExceptionEvent;
import com.nexusbpm.multipage.bus.BusAware;
import com.nexusbpm.multipage.bus.BusEvent;
import com.nexusbpm.multipage.bus.BusRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;
import com.nexusbpm.multipage.bus.UnhandledRequestException;

public class RunInUIThreadRequest implements BusRequest, BusAware {
    private Runnable runnable;
    private EventRequestBus bus;
    private Boolean runAsynchronously;
    
    public RunInUIThreadRequest(Runnable runnable) {
        this.runnable = runnable;
    }
    
    public RunInUIThreadRequest(Runnable runnable, Boolean runAsynchronously) {
        this.runnable = runnable;
        this.runAsynchronously = runAsynchronously;
    }
    
    public RunInUIThreadRequest(BusRequest request) {
        this.runnable = new RequestRunnable(request);
    }
    
    public RunInUIThreadRequest(BusRequest request, Boolean runAsynchronously) {
        this.runnable = new RequestRunnable(request);
        this.runAsynchronously = runAsynchronously;
    }
    
    public RunInUIThreadRequest(BusEvent event) {
        this.runnable = new EventRunnable(event);
    }
    
    public RunInUIThreadRequest(BusEvent event, Boolean runAsynchronously) {
        this.runnable = new EventRunnable(event);
        this.runAsynchronously = runAsynchronously;
    }
    
    public Runnable getRunnable() {
        return runnable;
    }
    
    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }
    
    public EventRequestBus getEventRequestBus() {
        return bus;
    }
    
    public void setEventRequestBus(EventRequestBus bus) {
        this.bus = bus;
    }
    
    public Boolean getRunAsynchronously() {
        return runAsynchronously;
    }
    
    /**
     * Set how the runnable should be run. If null, may or may not be run asynchronously
     * depending on what the current thread is. If <code>Boolean.TRUE</code> then the
     * runnable will be run asynchronously. If <code>Boolean.FALSE</code> then the
     * runnable will be run synchronously.
     */
    public void setRunAsynchronously(Boolean runAsynchronously) {
        this.runAsynchronously = runAsynchronously;
    }
    
    protected class RequestRunnable implements Runnable {
        private BusRequest request;
        public RequestRunnable(BusRequest request) {
            this.request = request;
        }
        public void run() {
            try {
                bus.handleRequest(request);
            } catch(UnhandledRequestException e) {
                bus.handleEvent(new ExceptionEvent("Event bus not configured", e));
            }
        }
    }
    
    protected class EventRunnable implements Runnable {
        private BusEvent event;
        public EventRunnable(BusEvent event) {
            this.event = event;
        }
        public void run() {
            bus.handleEvent(event);
        }
    }
}
