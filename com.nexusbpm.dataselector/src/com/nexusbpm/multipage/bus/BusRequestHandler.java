package com.nexusbpm.multipage.bus;

public interface BusRequestHandler {
    /**
     * @return <code>true</code> if this handler can handle the given request.
     */
    boolean canHandleRequest(BusRequest request);
    /**
     * Handle the given request and return a request-specific return value. Null is
     * a valid return value. A request may store results in the request object itself.
     */
    Object handleRequest(BusRequest request);
}
