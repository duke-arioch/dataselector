package com.nexusbpm.dataselector.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.nexusbpm.dataselector.controller.NodeController;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSStats;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.requests.DownloadStatisticsRequest;
import com.nexusbpm.dataselector.requests.RefreshTreeRequest;
import com.nexusbpm.dataselector.requests.SetDirtyRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class ClearStatsAction extends AbstractEventBusAction {
    private ISelectionProvider selectionProvider;
    
    public ClearStatsAction(ISelectionProvider selectionProvider, EventRequestBus bus) {
        super("Clear Statistics", IAction.AS_PUSH_BUTTON, bus);
        this.selectionProvider = selectionProvider;
        setId(ActionRegistry.CLEAR_STATS_ID);
    }
    
    @Override
    public boolean canPerform() {
        ISelection selection = selectionProvider.getSelection();
        if(selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
            return false;
        }
        for(Object o : ((IStructuredSelection) selection).toArray()) {
            LSNode node = null;
            if(o instanceof LSNode) {
                node = (LSNode) o;
            } else if(o instanceof NodeController) {
                node = ((NodeController) o).getModel();
            }
            if(node != null && hasStatistics(node)) {
                return true;
            }
        }
        return false;
    }
    
    protected boolean hasStatistics(LSNode node) {
        if(node.getStats() == null) {
            return false;
        }
        LSStats stats = node.getStats();
        return stats.getRowCount() >= 0 || stats.getColumnStats().size() > 0;
    }
    
    @Override
    public void performRun() {
        List<LSNode> nodes = new ArrayList<LSNode>();
        LSTree tree = null;
        for(Object o : ((IStructuredSelection) selectionProvider.getSelection()).toArray()) {
            LSNode node = null;
            if(o instanceof LSNode) {
                node = (LSNode) o;
            } else if(o instanceof NodeController) {
                node = ((NodeController) o).getModel();
            }
            if(node != null && hasStatistics(node)) {
                if(tree == null) {
                    tree = node.getTree();
                }
                nodes.add(node);
            }
        }
        if(nodes.size() == 0 || tree == null) {
            return;
        }
        synchronized(tree) {
            for(LSNode node : nodes) {
                node.getStats().clearColumnStats();
                node.setStats(null);
            }
            sendRequest(new SetDirtyRequest(true));
            sendRequest(new RefreshTreeRequest());
            if(tree.getConfig().isAutoDownloadStats()) {
                sendRequest(new DownloadStatisticsRequest(nodes));
            }
        }
    }
}
