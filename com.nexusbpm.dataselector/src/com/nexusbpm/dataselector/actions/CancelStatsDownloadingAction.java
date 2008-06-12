package com.nexusbpm.dataselector.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.nexusbpm.dataselector.controller.NodeController;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.requests.CancelDownloadRequest;
import com.nexusbpm.dataselector.stats.StatsThread;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class CancelStatsDownloadingAction extends AbstractEventBusAction {
    private ISelectionProvider selectionProvider;
    
    public CancelStatsDownloadingAction(ISelectionProvider selectionProvider, EventRequestBus bus) {
        super("Cancel Stats Downloading", IAction.AS_PUSH_BUTTON, bus);
        this.selectionProvider = selectionProvider;
        setId(ActionRegistry.CANCEL_DOWNLOAD_ID);
    }
    
    @Override
    public void performRun() {
        Object[] selection = ((IStructuredSelection) selectionProvider.getSelection()).toArray();
        
        List<LSNode> nodes = new ArrayList<LSNode>();
        for(Object o : selection) {
            if(o instanceof NodeController) {
                nodes.add(((NodeController) o).getModel());
            } else if(o instanceof LSNode) {
                nodes.add((LSNode) o);
            }
        }
        
        // we need at least 2 nodes in order to combine nodes
        if(nodes.size() > 0) {
            sendRequest(new CancelDownloadRequest(nodes));
        }
    }
    
    @Override
    public boolean canPerform() {
        Object[] selection = ((IStructuredSelection) selectionProvider.getSelection()).toArray();
        
        // first make sure we've only selected nodes
        if(selection.length < 1) {
            return false;
        }
        
        for(Object o : selection) {
            LSNode node = null;
            if(o instanceof NodeController) {
                node = ((NodeController) o).getModel();
            } else if(o instanceof LSNode) {
                node = (LSNode) o;
            }
            if(node != null && node.getLockOwner() instanceof StatsThread) {
                return true;
            }
        }
        
        return false;
    }
}
