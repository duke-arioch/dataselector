package com.nexusbpm.dataselector.commands;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nexusbpm.dataselector.controller.GraphUpdateQueue;
import com.nexusbpm.dataselector.events.PredictorChangeEvent;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.model.config.LSColumn;
import com.nexusbpm.dataselector.model.config.LSConfig;
import com.nexusbpm.dataselector.requests.SetDirtyRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class PredictorChangeCommand extends AbstractGraphUpdateCommand {
    private LSConfig config;
    private LSTree tree;
    private LSColumn targetColumn;
    private List<LSColumn> added;
    private List<LSColumn> removed;
    
    private PredictorChangeEvent event;
    
    public PredictorChangeCommand(EventRequestBus eventBus, LSConfig config, LSColumn targetColumn, List<LSColumn> added, List<LSColumn> removed) {
        super("Change Predictors", eventBus);
        this.config = config;
        this.targetColumn = targetColumn;
        this.added = added;
        this.removed = removed;
        this.tree = config.getTree();
    }

    protected PredictorChangeEvent getChangeEvent() {
        if(event == null) {
            event = new PredictorChangeEvent(targetColumn, added, removed);
        }
        return event;
    }
    
    @Override
    public boolean canExecute() {
        return tree != null;
    }
    
    @Override
    public void execute() {
        GraphUpdateQueue queue = getGraphUpdateQueue();
        synchronized(tree) {
            queue.startNonFlushingOperation();
            try {
                PredictorChangeEvent event = getChangeEvent();
                for(LSColumn column : added) {
                    column.setPredictor(true);
                }
                for(LSColumn column : removed) {
                    column.setPredictor(false);
                }
                targetColumn.setPredictor(true);
                
                config.setTargetColumn(targetColumn.getName());
                
                if(config.getTree().getRoot() == null) {
                    LSTree tree = config.getTree();
                    LSNode root = new LSNode(tree);
                    root.setDefaultBounds();
                    
                    tree.addNode(root);
                    tree.setRoot(root);
                }
                
                if(willPruneTree()) {
                    pruneTree(config.getTree().getRoot(), getColumnNames(removed));
                }
                
                sendRequest(new SetDirtyRequest(true));
                sendEvent(event);
            } finally {
                queue.endNonFlushingOperation();
            }
        }
    }
    
    protected void pruneTree(LSNode node, Set<String> columns) {
        for(LSNode child : node.getSubNodes()) {
            pruneTree(child, columns);
        }
        if(node.getSplit() != null && columns.contains(node.getSplit().getColumn())) {
            node.clearSubNodes();
        }
    }
    
    protected Set<String> getColumnNames(Collection<LSColumn> columns) {
        Set<String> names = new HashSet<String>();
        for(LSColumn column : columns) {
            names.add(column.getName());
        }
        return names;
    }
    
    /**
     * This can be used before the command is executed to see if the tree will be pruned
     * from the operation (i.e.: to ask the user if they want to perform or cancel the
     * operation).
     */
    public boolean willPruneTree() {
        return tree.getRoot() != null &&
            nodeSplitsOnColumn(tree.getRoot(), getColumnNames(removed));
    }
    
    protected boolean nodeSplitsOnColumn(LSNode node, Set<String> columns) {
        if(node.getSplit() != null && columns.contains(node.getSplit().getColumn())) {
            return true;
        } else {
            for(LSNode child : node.getSubNodes()) {
                if(nodeSplitsOnColumn(child, columns)) {
                    return true;
                }
            }
        }
        return false;
    }
}
