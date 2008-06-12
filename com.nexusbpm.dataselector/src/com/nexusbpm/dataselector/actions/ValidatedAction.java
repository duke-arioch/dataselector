package com.nexusbpm.dataselector.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Event;

public abstract class ValidatedAction extends Action {
    public ValidatedAction(String text, int style) {
        super(text, style);
    }
    
    @Override
    public final void run() {
    }
    
    @Override
    public final void runWithEvent(Event event) {
        if(canPerform()) {
            performRunWithEvent(event);
        }
    }
    
    public void performRunWithEvent(Event event) {
        performRun();
    }
    
    public abstract void performRun();
    
    public abstract boolean canPerform();
}
