package com.nexusbpm.dataselector.requests;

public class RetryRequest extends AbstractUserInputRequest {
    private String message;
    
    public RetryRequest(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + message + ")";
    }
}
