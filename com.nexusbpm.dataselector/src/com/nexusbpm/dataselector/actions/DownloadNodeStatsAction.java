package com.nexusbpm.dataselector.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.nexusbpm.dataselector.controller.NodeController;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.requests.DownloadStatisticsRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class DownloadNodeStatsAction extends AbstractEventBusAction {
    private GraphicalViewer viewer;
    
    public DownloadNodeStatsAction(GraphicalViewer viewer, EventRequestBus bus) {
        super("Download Statistics", IAction.AS_PUSH_BUTTON, bus);
        setId(ActionRegistry.DOWNLOAD_STATS_ID);
        this.viewer = viewer;
    }
    
    @Override
    public void performRun() {
        Object[] selection = ((IStructuredSelection) viewer.getSelection()).toArray();
        
        List<LSNode> nodes = new ArrayList<LSNode>();
        for(Object o : selection) {
            if(o instanceof NodeController) {
                nodes.add(((NodeController) o).getModel());
            }
        }
        if(nodes.size() > 0) {
            sendRequest(new DownloadStatisticsRequest(nodes));
        }
    }
    
    @Override
    public boolean canPerform() {
        Object[] selection = ((IStructuredSelection) viewer.getSelection()).toArray();
        
        // can try to download stats if any nodes are selected
        for(Object o : selection) {
            if(o instanceof NodeController) {
                return true;
            }
        }
        return false;
    }
}
