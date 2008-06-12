package com.nexusbpm.dataselector.actions;

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

public class RedoAction extends Action implements CommandStackEventListener {
    private CommandStack stack;
    
    public RedoAction(EditDomain editDomain) {
        super("Redo", IAction.AS_PUSH_BUTTON);
        stack = editDomain.getCommandStack();
        stack.addCommandStackEventListener(this);
        updateEnablement();
    }
    
    protected void updateEnablement() {
        Command cmd = stack.getRedoCommand();
        setEnabled(cmd != null && cmd.canExecute());
        if(cmd != null && cmd.getLabel() != null) {
            setText("Redo " + cmd.getLabel());
        } else {
            setText("Redo");
        }
    }
    
    public void stackChanged(CommandStackEvent event) {
        updateEnablement();
    }
    
    @Override
    public void run() {
        stack.redo();
    }
}
