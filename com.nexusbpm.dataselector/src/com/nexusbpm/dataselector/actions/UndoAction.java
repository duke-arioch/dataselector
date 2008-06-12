package com.nexusbpm.dataselector.actions;

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

public class UndoAction extends Action implements CommandStackEventListener {
    private CommandStack stack;
    
    public UndoAction(EditDomain editDomain) {
        super("Undo", IAction.AS_PUSH_BUTTON);
        stack = editDomain.getCommandStack();
        stack.addCommandStackEventListener(this);
        updateEnablement();
    }
    
    protected void updateEnablement() {
        Command cmd = stack.getUndoCommand();
        setEnabled(cmd != null && cmd.canUndo());
        if(cmd != null && cmd.getLabel() != null) {
            setText("Undo " + cmd.getLabel());
        } else {
            setText("Undo");
        }
    }
    
    public void stackChanged(CommandStackEvent event) {
        updateEnablement();
    }
    
    @Override
    public void run() {
        stack.undo();
    }
}
