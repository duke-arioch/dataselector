package com.nexusbpm.dataselector.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.IShellProvider;

public abstract class AbstractExtensionAction extends AbstractEventBusAction {
    private ISelectionProvider selectionProvider;
    private IShellProvider shellProvider;
    
    public AbstractExtensionAction(String text, int style) {
        super(text, style, null);
    }
    
    protected ISelectionProvider getSelectionProvider() {
        return selectionProvider;
    }
    
    protected void setSelectionProvider(ISelectionProvider selectionProvider) {
        this.selectionProvider = selectionProvider;
    }
    
    protected IShellProvider getShellProvider() {
        return shellProvider;
    }
    
    protected void setShellProvider(IShellProvider shellProvider) {
        this.shellProvider = shellProvider;
    }
}
