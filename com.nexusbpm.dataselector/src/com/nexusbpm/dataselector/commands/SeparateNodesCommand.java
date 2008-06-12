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

public class SeparateNodesCommand extends AbstractGraphUpdateCommand {
    private List<LSNode> nodes;
    private LSTree tree;
    
    public SeparateNodesCommand(EventRequestBus bus, List<LSNode> nodes) {
        super("Separate Nodes", bus);
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
                    separate(node);
                }
                TreeLayout layout = new TreeLayout();
                layout.layout(tree);
                sendRequest(new SetDirtyRequest(true));
            } finally {
                queue.endNonFlushingOperation();
            }
        }
    }
    
    protected void separate(LSNode node) {
        if(node.getConditions().size() < 2) {
            // if the node doesn't have multiple conditions then we don't need to separate it
            return;
        }
        
        // first remove any children of the node
        if(node.getSubNodes().size() > 0) {
            node.clearSubNodes();
        }
        
        // now remove the node
        LSTree tree = node.getTree();
        LSNodeConnector oldConnector = node.getConnector();
        LSNode parent = oldConnector.getSource();
        node.setConnector(null);
        parent.removeSubNode(node);
        tree.removeNode(node);
        
        // now create new children to replace the old one
        for(LSCondition c : node.getConditions()) {
            LSNode child = new LSNode(tree);
            tree.addNode(child);
            child.setDefaultBounds();
            LSCondition cond = new LSCondition(child);
            for(LSWhere w : c.getWhereClauses()) {
                LSWhere where = new LSWhere(cond);
                where.setMatch(w.getMatch());
                where.setValue(w.getValue());
                cond.addWhereClause(where);
            }
            child.addCondition(cond);
            LSNodeConnector connector = new LSNodeConnector(parent, child);
            child.setConnector(connector);
            parent.addSubNode(child);
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
