package com.nexusbpm.multipage.bus;

public class UnhandledRequestException extends Exception {
    private static final long serialVersionUID = 1l;
    
    private BusRequest request;
    
    public UnhandledRequestException() {
    }
    public UnhandledRequestException(String message) {
        super(message);
    }
    public UnhandledRequestException(Throwable cause) {
        super(cause);
    }
    public UnhandledRequestException(String message, Throwable cause) {
        super(message, cause);
    }
    public UnhandledRequestException(BusRequest request) {
        super("No RequestHandler found for request: " + request.getClass() + " : " + request);
        setRequest(request);
    }
    
    protected void setRequest(BusRequest request) {
        this.request = request;
    }
    
    public BusRequest getRequest() {
        return request;
    }
}
