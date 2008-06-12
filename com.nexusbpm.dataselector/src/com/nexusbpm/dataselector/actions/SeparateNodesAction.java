package com.nexusbpm.dataselector.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.nexusbpm.dataselector.commands.SeparateNodesCommand;
import com.nexusbpm.dataselector.controller.NodeController;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.requests.ConfirmationRequest;
import com.nexusbpm.dataselector.requests.ExecuteCommandRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class SeparateNodesAction extends AbstractEventBusAction {
    private ISelectionProvider selectionProvider;
    public SeparateNodesAction(ISelectionProvider selectionProvider, EventRequestBus bus) {
        super("Separate Node Conditions", IAction.AS_PUSH_BUTTON, bus);
        this.selectionProvider = selectionProvider;
        setId(ActionRegistry.SEPARATE_NODES_ID);
    }
    
    @Override
    public void performRun() {
        Object[] selection = ((IStructuredSelection) selectionProvider.getSelection()).toArray();
        
//        LSTree tree = null;
        boolean prune = false;
        for(Object o : selection) {
            if(o instanceof NodeController) {
                LSNode node = ((NodeController) o).getModel();
                if(canSeparate(node) && node.getSubNodes().size() > 0 && !prune) {
                    ConfirmationRequest request = new ConfirmationRequest(
                            "Separating the selected node(s) will prune the tree. Continue?");
                    sendRequest(request);
                    if(request.isCancelled()) {
                        return;
                    } else {
                        prune = true;
                    }
                }
            }
        }
        
        List<LSNode> nodes = new ArrayList<LSNode>();
        for(Object o : selection) {
            if(o instanceof NodeController) {
                LSNode node = ((NodeController) o).getModel();
                if(canSeparate(node)) {
                    nodes.add(node);
//                    if(tree == null) {
//                        tree = node.getTree();
//                    }
//                    separate(node);
                }
            }
        }
        sendRequest(new ExecuteCommandRequest(new SeparateNodesCommand(
                getEventRequestBus(), nodes)));
//        if(tree != null) {
//            TreeLayout layout = new TreeLayout();
//            layout.layout(tree);
//        }
    }
    
    protected boolean canSeparate(LSNode node) {
        return node.getConditions().size() > 1;
    }
    
//    protected void separate(LSNode node) {
//        // first remove any children of the node
//        if(node.getSubNodes().size() > 0) {
//            node.clearSubNodes();
//        }
//        
//        // now remove the node
//        LSTree tree = node.getTree();
//        LSNodeConnector oldConnector = node.getConnector();
//        LSNode parent = oldConnector.getSource();
//        node.setConnector(null);
//        parent.removeSubNode(node);
//        tree.removeNode(node);
//        
//        // now create new children to replace the old one
//        for(LSCondition c : node.getConditions()) {
//            LSNode child = new LSNode(tree);
//            tree.addNode(child);
//            child.setDefaultBounds();
//            LSCondition cond = new LSCondition(child);
//            for(LSWhere w : c.getWhereClauses()) {
//                LSWhere where = new LSWhere(cond);
//                where.setMatch(w.getMatch());
//                where.setValue(w.getValue());
//                cond.addWhereClause(where);
//            }
//            child.addCondition(cond);
//            LSNodeConnector connector = new LSNodeConnector(parent, child);
//            child.setConnector(connector);
//            parent.addSubNode(child);
//        }
//    }
    
    @Override
    public boolean canPerform() {
        Object[] selection = ((IStructuredSelection) selectionProvider.getSelection()).toArray();
        
        for(Object o : selection) {
            if(o instanceof NodeController) {
                LSNode node = ((NodeController) o).getModel();
                if(canSeparate(node)) {
                    // can separate if one of the selected nodes has multiple conditions
                    return true;
                }
            }
        }
        return false;
    }
}
