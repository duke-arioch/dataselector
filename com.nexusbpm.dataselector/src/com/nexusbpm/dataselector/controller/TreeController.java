package com.nexusbpm.dataselector.controller;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.swt.graphics.Color;

import com.nexusbpm.dataselector.commands.NodeSetBoundsCommand;
import com.nexusbpm.dataselector.events.ExtensionChangeEvent;
import com.nexusbpm.dataselector.model.AbstractModelElement;
import com.nexusbpm.dataselector.model.DisplayExtension;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.util.ColorCache;

public class TreeController extends AbstractController<LSTree> {
    private ColorCache colorCache;
    
    public TreeController(LSTree tree) {
        super(null);
        setModel(tree);
    }
    
    @Override
    protected void listenToModel() {
        getModel().addPropertyChangeListener(this);
        getModel().getConfig().addPropertyChangeListener(this);
        DisplayExtension extension = DisplayExtension.getDisplayExtension(
                getModel().getConfig(), "showGrid", false);
        if(extension != null) {
            extension.addPropertyChangeListener(this);
        }
    }
    
    @Override
    protected void stopListeningToModel() {
        getModel().removePropertyChangeListener(this);
        getModel().getConfig().removePropertyChangeListener(this);
        DisplayExtension extension = DisplayExtension.getDisplayExtension(
                getModel().getConfig(), "showGrid", false);
        if(extension != null) {
            extension.removePropertyChangeListener(this);
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if(event.getPropertyName().equals(LSTree.PROPERTY_EXTENSION_ADDED)) {
            ExtensionChangeEvent evt = (ExtensionChangeEvent) event;
            if(evt.getExtensionId().equals(DisplayExtension.ELEMENT_KEY + ".showGrid")) {
                evt.getNewValue().addPropertyChangeListener(this);
                queueRefreshVisuals();
            } else if(evt.getExtensionId().equals(DisplayExtension.ELEMENT_KEY + ".countsAsPercents")) {
                evt.getNewValue().addPropertyChangeListener(this);
                queueRefreshNodeVisuals();
            }
        } else if(event.getPropertyName().equals(LSTree.PROPERTY_EXTENSION_REMOVED)) {
            ExtensionChangeEvent evt = (ExtensionChangeEvent) event;
            if(evt.getExtensionId().equals(DisplayExtension.ELEMENT_KEY + ".showGrid")) {
                evt.getOldValue().removePropertyChangeListener(this);
                queueRefreshVisuals();
            } else if(evt.getExtensionId().equals(DisplayExtension.ELEMENT_KEY + ".countsAsPercents")) {
                evt.getOldValue().removePropertyChangeListener(this);
                queueRefreshNodeVisuals();
            }
        } else if(event.getPropertyName().equals(DisplayExtension.PROPERTY_EXTENSION_VALUE) &&
                DisplayExtension.getDisplayExtension(
                        getModel().getConfig(), "showGrid", false) == event.getSource()) {
            queueRefreshVisuals();
        } else if(event.getPropertyName().equals(DisplayExtension.PROPERTY_EXTENSION_VALUE) &&
                DisplayExtension.getDisplayExtension(
                        getModel().getConfig(), "countsAsPercents", false) == event.getSource()) {
            queueRefreshNodeVisuals();
        } else if(event.getPropertyName().equals(LSTree.PROPERTY_NODE_REMOVED)) {
            removeChild(getChild((LSNode) event.getOldValue()));
        } else if(event.getPropertyName().equals(LSTree.PROPERTY_NODE_ADDED)) {
            addChild(createChild(event.getNewValue()), getChildren().size());
        } else if(event.getSource() == getModel()) {
            super.propertyChange(event);
        }
    }
    
    protected void queueRefreshNodeVisuals() {
        for(Object o : getChildren()) {
            ((NodeController) o).queueRefreshVisuals();
        }
    }
    
    @Override
    protected List<AbstractModelElement> getModelChildren() {
        return new ArrayList<AbstractModelElement>(getModel().getNodes());
    }
    
    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new XYLayoutEditPolicy() {
            @Override
            protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
                if(child instanceof NodeController && constraint instanceof Rectangle) {
                    return new NodeSetBoundsCommand(
                            getModel().getEventRequestBus(),
                            ((NodeController) child).getModel(),
                            (Rectangle) constraint);
                }
                System.out.println("createChangeConstraint:" + child + ":" + constraint);
                return null;
            }
            @Override
            protected Command getCreateCommand(CreateRequest request) {
                System.out.println("getCreateCommand:" + request);
                return null;
            }
        });
        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class key) {
        if(SnapToHelper.class.equals(key)) {
            DisplayExtension ext = DisplayExtension.getDisplayExtension(
                    getModel().getConfig(), "showGrid", false);
            if(ext != null && ext.getBoolean(true)) {
                return new SnapToGrid(this);
            }
        }
        return super.getAdapter(key);
    }
    
    protected void updateGrid() {
        boolean grid = false;
        
        DisplayExtension ext = DisplayExtension.getDisplayExtension(
                getModel().getConfig(), "showGrid", false);
        if(ext != null && ext.getBoolean(true)) {
            grid = true;
        }
        if(colorCache == null) {
            colorCache = new ColorCache(null);
        }
        Color gridColor;
        if(grid) {
            gridColor = colorCache.getColor(230, 240, 250);
        } else {
            gridColor = colorCache.getColor(255, 255, 255);
        }
        ((LayerManager) getViewer().getEditPartRegistry().get(LayerManager.ID))
        .getLayer(LayerConstants.GRID_LAYER).setForegroundColor(gridColor);
        getViewer().setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE, Boolean.valueOf(grid));
        getViewer().setProperty(SnapToGrid.PROPERTY_GRID_ENABLED, Boolean.valueOf(grid));
    }
    
    protected IFigure createFigure() {
        Figure f = new FreeformLayer();
        f.setOpaque(false);
        f.setLayoutManager(new FreeformLayout());
        return f;
    }
    
    @Override
    public void activate() {
        super.activate();
        updateGrid();
    }
    
    @Override
    public void refreshVisuals() {
        updateGrid();
        super.refreshVisuals();
    }
    
    @Override
    public void deactivate() {
        super.deactivate();
        if(colorCache != null) {
            colorCache.dispose();
            colorCache = null;
        }
    }
}
