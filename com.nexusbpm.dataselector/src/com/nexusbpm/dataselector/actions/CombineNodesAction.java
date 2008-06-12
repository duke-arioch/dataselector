package com.nexusbpm.dataselector.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.nexusbpm.dataselector.commands.CombineNodesCommand;
import com.nexusbpm.dataselector.controller.NodeController;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.requests.ConfirmationRequest;
import com.nexusbpm.dataselector.requests.ExecuteCommandRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class CombineNodesAction extends AbstractEventBusAction {
    private ISelectionProvider selectionProvider;
    public CombineNodesAction(ISelectionProvider selectionProvider, EventRequestBus bus) {
        super("Combine Selected Nodes", IAction.AS_PUSH_BUTTON, bus);
        this.selectionProvider = selectionProvider;
        setId(ActionRegistry.COMBINE_NODES_ID);
    }
    
    @Override
    public void performRun() {
        Object[] selection = ((IStructuredSelection) selectionProvider.getSelection()).toArray();
        
        List<LSNode> nodes = new ArrayList<LSNode>();
        LSTree tree = null;
        boolean prune = false;
        for(Object o : selection) {
            LSNode node = ((NodeController) o).getModel();
            nodes.add(node);
            if(tree == null) {
                tree = node.getTree();
            }
            if(node.getSubNodes().size() > 0 && !prune) {
                ConfirmationRequest request = new ConfirmationRequest(
                        "Combining the selected nodes will prune the tree. Continue?");
                sendRequest(request);
                if(request.isCancelled()) {
                    return;
                } else {
                    // keep track of the user's acceptance to prune the tree
                    prune = true;
                }
            }
        }
        
        // we need at least 2 nodes in order to combine nodes
        if(nodes.size() > 1) {
            sendRequest(new ExecuteCommandRequest(new CombineNodesCommand(
                    getEventRequestBus(), nodes)));
        }
    }
    
    @Override
    public boolean canPerform() {
        Object[] selection = ((IStructuredSelection) selectionProvider.getSelection()).toArray();
        
        // first make sure we've only selected nodes
        if(selection.length < 2 || !(selection[0] instanceof NodeController)) {
            return false;
        }
        // then get the parent of the first node
        LSNode parent = ((NodeController) selection[0]).getModel();
        if(parent.getConnector() == null || parent.getConnector().getSource() == null) {
            return false;
        }
        parent = parent.getConnector().getSource();
        
        // make sure the parent has more children than the number of selected nodes
        if(parent == null || parent.getSubNodes().size() <= selection.length) {
            return false;
        }
        
        // make sure all selected nodes have the same parent
        for(Object o : selection) {
            if(!(o instanceof NodeController)) {
                return false;
            }
            LSNode node = ((NodeController) o).getModel();
            if(node.getConnector() == null || node.getConnector().getSource() != parent) {
                return false;
            }
            // make sure the node is not a 'remainder' node
            if(node.isRemainderNode()) {
                return false;
            }
        }
        
        // if we get this far we can combine the selected nodes
        return true;
    }
}
