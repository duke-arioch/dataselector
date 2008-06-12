package com.nexusbpm.dataselector.controller;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;

import com.nexusbpm.dataselector.model.LSNodeConnector;

public class NodeConnectorController extends AbstractConnectionController<LSNodeConnector> {
    public NodeConnectorController(TreeController tree, LSNodeConnector model) {
        super(tree);
        setModel(model);
    }
    
    @Override
    protected IFigure createFigure() {
        PolylineConnection connection = (PolylineConnection) super.createFigure();
//        PolygonDecoration decoration = new PolygonDecoration();
//        decoration.setFill(false);
        PolylineDecoration decoration = new PolylineDecoration();
        connection.setTargetDecoration(decoration);
        return connection;
    }
    
    @Override
    protected void createEditPolicies() {
        // TODO Auto-generated method stub
    }
}
