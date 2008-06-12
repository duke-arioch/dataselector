package com.nexusbpm.dataselector.actions;

import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.jface.action.IAction;

public class ResetZoomAction extends ValidatedAction {
    private ZoomManager zoomManager;
    
    public ResetZoomAction(ZoomManager zoomManager) {
        super("Reset Zoom", IAction.AS_PUSH_BUTTON);
        setId(ActionRegistry.RESET_ZOOM_ID);
        this.zoomManager = zoomManager;
    }
    
    @Override
    public boolean canPerform() {
        return zoomManager.getZoom() != 1.0;
    }
    
    @Override
    public void performRun() {
        zoomManager.setZoom(1.0);
    }
}
