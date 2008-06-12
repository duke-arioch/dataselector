package com.nexusbpm.multipage.bus;

/**
 * Marker interface for objects that need to know about the EventRequestBus. When
 * the EventRequestBus receives an object that implements this interface it will
 * set itself on the object.
 */
public interface BusAware {
    void setEventRequestBus(EventRequestBus bus);
    EventRequestBus getEventRequestBus();
}
