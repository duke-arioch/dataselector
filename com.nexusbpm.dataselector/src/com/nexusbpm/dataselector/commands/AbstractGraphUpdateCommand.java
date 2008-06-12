package com.nexusbpm.dataselector.commands;

import com.nexusbpm.dataselector.controller.GraphUpdateQueue;
import com.nexusbpm.dataselector.requests.GetGraphUpdateQueueRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;

public abstract class AbstractGraphUpdateCommand extends AbstractEventBusCommand {
    public AbstractGraphUpdateCommand(String label, EventRequestBus eventBus) {
        super(label, eventBus);
    }
    
    protected GraphUpdateQueue getGraphUpdateQueue() {
        GetGraphUpdateQueueRequest request = new GetGraphUpdateQueueRequest();
        sendRequest(request);
        return request.getQueue();
    }
}
