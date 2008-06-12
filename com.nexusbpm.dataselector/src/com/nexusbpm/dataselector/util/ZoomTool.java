package com.nexusbpm.dataselector.util;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.tools.SimpleDragTracker;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Device;

import com.nexusbpm.dataselector.Plugin;

public class ZoomTool extends SimpleDragTracker {
    private Point start;
    private Cursor cursor;
    
    public ZoomTool(Device device) {
        cursor = new Cursor(device,
                Plugin.imageDescriptorFromPlugin(Plugin.PLUGIN_ID, "/icons/16x16/actions/viewmag.gif").getImageData(),
                5, 5);
    }
    
    @Override
    protected Cursor calculateCursor() {
        return cursor;
    }
    
    @Override
    protected boolean handleDragStarted() {
        boolean b = super.handleDragStarted();
        if(b) {
            start = getLocation();
        }
        return b;
    }
    
    @Override
    protected String getCommandName() {
        return null;
    }
    
    @Override
    protected boolean handleButtonUp(int button) {
        if(start != null) {
            Point end = getLocation();
            
            ScalableRootEditPart part = (ScalableRootEditPart) getCurrentViewer().getRootEditPart();
            ZoomManager manager = part.getZoomManager();
            
            long x = start.x - end.x;
            long y = start.y - end.y;
            double distance = Math.sqrt(x * x + y * y);
            long direction = (y == 0) ? 0 : y / Math.abs(y);
            
            manager.setZoom(manager.getZoom() * (1 + direction * (distance / 500)));
            start = null;
        }
        boolean b = super.handleButtonUp(button);
        reactivate();
        return b;
    }
}
