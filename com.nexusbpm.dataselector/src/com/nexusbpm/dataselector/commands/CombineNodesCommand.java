package com.nexusbpm.dataselector.commands;

import java.util.List;

import com.nexusbpm.dataselector.controller.GraphUpdateQueue;
import com.nexusbpm.dataselector.model.LSCondition;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSNodeConnector;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.model.LSWhere;
import com.nexusbpm.dataselector.requests.SetDirtyRequest;
import com.nexusbpm.dataselector.util.TreeLayout;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class CombineNodesCommand extends AbstractGraphUpdateCommand {
    private List<LSNode> nodes;
    private LSTree tree;
    
    public CombineNodesCommand(EventRequestBus bus, List<LSNode> nodes) {
        super("Combine nodes", bus);
        this.nodes = nodes;
        if(nodes.size() > 0) {
            tree = nodes.get(0).getTree();
        }
    }
    
    @Override
    public boolean canExecute() {
        if(nodes != null && nodes.size() > 1 && tree != null) {
            for(LSNode node : nodes) {
                if(node.isRemainderNode()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    @Override
    public void execute() {
        GraphUpdateQueue queue = getGraphUpdateQueue();
        synchronized(tree) {
            queue.startNonFlushingOperation();
            try {
                combine();
                TreeLayout layout = new TreeLayout();
                layout.layout(tree);
                sendRequest(new SetDirtyRequest(true));
            } finally {
                queue.endNonFlushingOperation();
            }
        }
    }
    
    @Override
    public boolean canUndo() {
        return false;
    }
    
    protected void combine() {
        // first remove any children of the nodes
        for(LSNode node : nodes) {
            if(node.getSubNodes().size() > 0) {
                node.clearSubNodes();
            }
        }
        
        LSTree tree = nodes.get(0).getTree();
        LSNode parent = nodes.get(0).getConnector().getSource();
        
        // now remove the nodes
        for(LSNode node : nodes) {
            node.setConnector(null);
            parent.removeSubNode(node);
            tree.removeNode(node);
        }
        
        // now create a new child node to replace the old ones
        LSNode child = new LSNode(tree);
        tree.addNode(child);
        child.setDefaultBounds();
        for(LSNode node : nodes) {
            for(LSCondition c : node.getConditions()) {
                LSCondition cond = new LSCondition(child);
                for(LSWhere w : c.getWhereClauses()) {
                    LSWhere where = new LSWhere(cond);
                    where.setMatch(w.getMatch());
                    where.setValue(w.getValue());
                    cond.addWhereClause(where);
                }
                child.addCondition(cond);
            }
        }
        LSNodeConnector connector = new LSNodeConnector(parent, child);
        child.setConnector(connector);
        parent.addSubNode(child);
    }
}
