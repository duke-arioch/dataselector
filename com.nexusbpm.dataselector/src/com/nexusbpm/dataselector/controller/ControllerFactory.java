package com.nexusbpm.dataselector.controller;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSNodeConnector;
import com.nexusbpm.dataselector.model.LSTree;

public class ControllerFactory implements EditPartFactory {
    public EditPart createEditPart(EditPart context, Object model) {
        if(model instanceof LSTree) {
            return new TreeController((LSTree) model);
        } else if(model instanceof LSNode) {
            return new NodeController(getTree(context), (LSNode) model);
        } else if(model instanceof LSNodeConnector) {
            return new NodeConnectorController(getTree(context), (LSNodeConnector) model);
        }
        throw new RuntimeException("Model " + model + " not accepted!");
    }
    
    protected TreeController getTree(EditPart context) {
        if(context instanceof AbstractController) {
            return ((AbstractController<?>) context).getTreeController();
        } else {
            return ((AbstractConnectionController<?>) context).getTreeController();
        }
    }
}
