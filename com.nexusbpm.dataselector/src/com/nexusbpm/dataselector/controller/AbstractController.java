package com.nexusbpm.dataselector.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import com.nexusbpm.dataselector.model.AbstractModelElement;

public abstract class AbstractController<T extends AbstractModelElement>
        extends AbstractGraphicalEditPart
        implements PropertyChangeListener, Controller {
    private TreeController treeController;
    @SuppressWarnings("unchecked")
    private Map<AbstractModelElement, AbstractController> children;
    
    @SuppressWarnings("unchecked")
    protected AbstractController(TreeController tree) {
        children = new HashMap<AbstractModelElement, AbstractController>();
        if(tree == null) {
            this.treeController = (TreeController) this;
        } else {
            this.treeController = tree;
        }
    }
    
    @Override
    public void activate() {
        if(!isActive()) {
            super.activate();
            if(getModel() == null) throw new IllegalStateException("No model");
            listenToModel();
        }
    }
    
    protected void listenToModel() {
        getModel().addPropertyChangeListener(this);
    }
    
    @Override
    public void deactivate() {
        if(isActive()) {
            if(getModel() == null) throw new IllegalStateException("No model");
            stopListeningToModel();
            super.deactivate();
        }
    }
    
    protected void stopListeningToModel() {
        getModel().removePropertyChangeListener(this);
    }
    
    @SuppressWarnings(value={"unchecked"})
    public T getModel() {
        return (T) super.getModel();
    }
    
    public TreeController getTreeController() {
        return treeController;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class key) {
        Object adapter = super.getAdapter(key);
        if(adapter == null && getModel() instanceof IAdaptable) {
            adapter = ((IAdaptable) getModel()).getAdapter(key);
        }
        return adapter;
    }
    
    @SuppressWarnings("unchecked")
    protected <Y extends AbstractModelElement> AbstractController<Y> getChild(Y model) {
        return children.get(model);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void addChild(EditPart child, int index) {
        children.put((AbstractModelElement) child.getModel(), (AbstractController) child);
        super.addChild(child, index);
    }
    
    @Override
    protected void removeChild(EditPart child) {
        children.remove(child.getModel());
        super.removeChild(child);
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        System.out.println(getModel() + ":propertyChange:property=" + event.getPropertyName() + ":" +
                event.getOldValue() + "->" + event.getNewValue());
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
