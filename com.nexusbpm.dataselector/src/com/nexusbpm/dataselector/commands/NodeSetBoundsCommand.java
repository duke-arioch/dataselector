package com.nexusbpm.dataselector.commands;

import org.eclipse.draw2d.geometry.Rectangle;

import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.requests.SetDirtyRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class NodeSetBoundsCommand extends AbstractEventBusCommand /*implements PropertyChangeListener*/ {
    private LSNode node;
    private LSTree tree;
    private Rectangle rect;
    private Rectangle origRect;
    
    private String action = "Move/Resize ";
    
    public NodeSetBoundsCommand(EventRequestBus bus, LSNode node, Rectangle rect) {
        super("Move/Resize Node", bus);
        this.node = node;
        this.rect = rect;
        this.tree = node.getTree();
    }
    
//    @Override
//    public void propertyChange(PropertyChangeEvent evt) {
//        if(evt.getPropertyName().equals(LSNode.PROPERTY_ELEMENT_NAME)) {
//            refreshLabel();
//        }
//    }
    
    protected void refreshLabel() {
        if(rect != null && origRect != null) {
            int oldRight = origRect.x + origRect.width;
            int oldBottom = origRect.y + origRect.height;
            int newRight = rect.x + rect.width;
            int newBottom = rect.y + rect.height;
            if(rect.x == origRect.x || rect.y == origRect.y || newRight == oldRight || newBottom == oldBottom) {
                action = "Resize ";
            } else if(rect.width == origRect.width && rect.height == origRect.height) {
                action = "Move ";
            } else {
                action = "Move/Resize ";
            }
        }
        if(node != null && node.getName() != null && node.getName().length() > 0) {
            setLabel(action + "Node " + node.getName());
        } else {
            setLabel(action + "Node");
        }
    }
    
//    @Override
//    public void dispose() {
//        if(node != null) {
//            node.removePropertyChangeListener(this);
//        }
//        super.dispose();
//    }
    
    @Override
    public boolean canExecute() {
        return node != null && rect != null && tree != null;
    }
    
    @Override
    public void execute() {
        origRect = node.getBounds();
//        node.addPropertyChangeListener(this);
        refreshLabel();
        origRect = node.getBounds();
        redo();
    }
    
    @Override
    public void redo() {
        synchronized(tree) {
            node.setBounds(rect);
            sendRequest(new SetDirtyRequest(true));
        }
    }
    
    @Override
    public boolean canUndo() {
        return node != null && origRect != null;
    }
    
    @Override
    public void undo() {
        synchronized(tree) {
            node.setBounds(origRect);
            sendRequest(new SetDirtyRequest(true));
        }
    }
}
