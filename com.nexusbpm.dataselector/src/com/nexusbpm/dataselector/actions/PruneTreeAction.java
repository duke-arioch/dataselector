package com.nexusbpm.dataselector.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.nexusbpm.dataselector.commands.PruneTreeCommand;
import com.nexusbpm.dataselector.controller.NodeController;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.requests.ExecuteCommandRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class PruneTreeAction extends AbstractEventBusAction {
    private ISelectionProvider selectionProvider;
    public PruneTreeAction(ISelectionProvider selectionProvider, EventRequestBus bus) {
        super("Prune Children", IAction.AS_PUSH_BUTTON, bus);
        this.selectionProvider = selectionProvider;
        setId(ActionRegistry.PRUNE_TREE_ID);
    }
    
    @Override
    public void performRun() {
        List<LSNode> nodes = new ArrayList<LSNode>();
        Object[] selection = ((IStructuredSelection) selectionProvider.getSelection()).toArray();
//        LSTree tree = null;
        for(Object o : selection) {
            if(o instanceof NodeController) {
                LSNode node = ((NodeController) o).getModel();
                if(node.getSplit() != null) {
                    nodes.add(node);
//                    node.clearSubNodes();
//                    if(tree == null) {
//                        tree = node.getTree();
//                    }
                }
            }
        }
        if(nodes.size() > 0) {
            sendRequest(new ExecuteCommandRequest(new PruneTreeCommand(
                    getEventRequestBus(), nodes)));
        }
//        if(tree != null) { // only layout the tree if we actually pruned
//            TreeLayout layout = new TreeLayout();
//            layout.layout(tree);
//        }
        // TODO after pruning the tree we should stop downloading statistics?
    }
    
    @Override
    public boolean canPerform() {
        Object[] selection = ((IStructuredSelection) selectionProvider.getSelection()).toArray();
        
        // can prune if any of the selected nodes has a split
        for(Object o : selection) {
            if(o instanceof NodeController) {
                NodeController controller = (NodeController) o;
                if(controller.getModel().getSplit() != null) {
                    return true;
                }
            }
        }
        return false;
    }
}
