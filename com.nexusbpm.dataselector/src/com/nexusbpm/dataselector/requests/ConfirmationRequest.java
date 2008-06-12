package com.nexusbpm.dataselector.requests;

public class ConfirmationRequest extends AbstractUserInputRequest {
    private String message;
    
    public ConfirmationRequest(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
