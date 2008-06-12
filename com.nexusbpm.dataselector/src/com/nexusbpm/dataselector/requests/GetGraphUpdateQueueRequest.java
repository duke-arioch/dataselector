package com.nexusbpm.dataselector.requests;

import com.nexusbpm.dataselector.controller.GraphUpdateQueue;
import com.nexusbpm.multipage.bus.BusRequest;

public class GetGraphUpdateQueueRequest implements BusRequest {
    private GraphUpdateQueue queue;
    
    public GetGraphUpdateQueueRequest() {
    }
    
    public GraphUpdateQueue getQueue() {
        return queue;
    }
    
    public void setQueue(GraphUpdateQueue queue) {
        this.queue = queue;
    }
}
