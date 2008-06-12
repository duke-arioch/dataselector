package com.nexusbpm.dataselector.commands;

import java.util.List;

import com.nexusbpm.dataselector.controller.GraphUpdateQueue;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.requests.SetDirtyRequest;
import com.nexusbpm.dataselector.util.TreeLayout;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class PruneTreeCommand extends AbstractGraphUpdateCommand {
    private List<LSNode> nodes;
    private LSTree tree;
    
    public PruneTreeCommand(EventRequestBus bus, List<LSNode> nodes) {
        super("Prune Tree", bus);
        this.nodes = nodes;
        if(nodes.size() > 0) {
            tree = nodes.get(0).getTree();
        }
    }
    
    @Override
    public void execute() {
        GraphUpdateQueue queue = getGraphUpdateQueue();
        synchronized(tree) {
            queue.startNonFlushingOperation();
            try {
                for(LSNode node : nodes) {
                    node.clearSubNodes();
                }
                TreeLayout layout = new TreeLayout();
                layout.layout(tree);
                sendRequest(new SetDirtyRequest(true));
            } finally {
                queue.endNonFlushingOperation();
            }
        }
    }
    
    @Override
    public boolean canExecute() {
        return nodes != null && nodes.size() > 0 && tree != null;
    }
    
    @Override
    public boolean canUndo() {
        return false;
    }
}
