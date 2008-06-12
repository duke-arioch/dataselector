package com.nexusbpm.dataselector.events;

import com.nexusbpm.multipage.bus.BusEvent;

public class ExceptionEvent implements BusEvent {
    private Throwable throwable;
    private String message;
    
    public ExceptionEvent(String message, Throwable throwable) {
        setMessage(message);
        this.throwable = throwable;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
        if(this.message == null) {
            this.message = throwable.getLocalizedMessage();
        }
        if(this.message == null) {
            this.message = throwable.getClass().getName();
        }
    }
    
    public Throwable getThrowable() {
        return throwable;
    }
    
    public boolean isException() {
        return throwable instanceof Exception;
    }
    
    public Exception getException() {
        return (Exception) throwable;
    }
    
    public boolean isRuntimeException() {
        return throwable instanceof RuntimeException;
    }
    
    public RuntimeException getRuntimeException() {
        return (RuntimeException) throwable;
    }
    
    public boolean isError() {
        return throwable instanceof Error;
    }
    
    public Error getError() {
        return (Error) throwable;
    }
}
