package com.nexusbpm.dataselector.requests;

import org.eclipse.gef.commands.Command;

import com.nexusbpm.multipage.bus.BusRequest;

public class ExecuteCommandRequest implements BusRequest {
    private Command command;
    
    public ExecuteCommandRequest(Command command) {
        this.command = command;
    }
    
    public Command getCommand() {
        return command;
    }
    
    public void setCommand(Command command) {
        this.command = command;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + command + ")";
    }
}
