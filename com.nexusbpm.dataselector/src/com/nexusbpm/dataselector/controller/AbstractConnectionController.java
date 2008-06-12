package com.nexusbpm.dataselector.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.gef.editparts.AbstractConnectionEditPart;

import com.nexusbpm.dataselector.model.AbstractModelElement;

public abstract class AbstractConnectionController<T extends AbstractModelElement>
        extends AbstractConnectionEditPart
        implements PropertyChangeListener, Controller {
    private TreeController tree;
    
    protected AbstractConnectionController(TreeController tree) {
        this.tree = tree;
    }
    
    @Override
    public void activate() {
        if(!isActive()) {
            super.activate();
            if(getModel() == null) throw new IllegalStateException("No model");
            getModel().addPropertyChangeListener(this);
        }
    }
    
    @Override
    public void deactivate() {
        if(isActive()) {
            if(getModel() == null) throw new IllegalStateException("No model");
            getModel().removePropertyChangeListener(this);
            super.deactivate();
        }
    }
    
    @SuppressWarnings(value={"unchecked"})
    public T getModel() {
        return (T) super.getModel();
    }
    
    protected TreeController getTreeController() {
        return tree;
    }
    
    public void propertyChange(PropertyChangeEvent event) {
//        System.out.println(getModel() + ":propertyChange:property=" + event.getPropertyName() + ":" +
//                event.getOldValue() + "->" + event.getNewValue());
        // TODO
        queueRefresh();
    }
    
    protected void queueRefresh() {
        ((GraphUpdateQueue) getViewer().getProperty("updateQueue")).queueRefresh(this);
    }
    
    protected void queueRefreshChildren() {
        ((GraphUpdateQueue) getViewer().getProperty("updateQueue")).queueRefreshChildren(this);
    }
    
    protected void queueRefreshSourceConnections() {
        ((GraphUpdateQueue) getViewer().getProperty("updateQueue")).queueRefreshSourceConnections(this);
    }
    
    protected void queueRefreshTargetConnections() {
        ((GraphUpdateQueue) getViewer().getProperty("updateQueue")).queueRefreshTargetConnections(this);
    }
    
    protected void queueRefreshVisuals() {
        ((GraphUpdateQueue) getViewer().getProperty("updateQueue")).queueRefreshVisuals(this);
    }
    
    @Override
    public void refreshChildren() {
        super.refreshChildren();
    }
    
    @Override
    public void refreshSourceConnections() {
        super.refreshSourceConnections();
    }
    
    @Override
    public void refreshTargetConnections() {
        super.refreshTargetConnections();
    }
    
    @Override
    public void refreshVisuals() {
        super.refreshVisuals();
    }
}
