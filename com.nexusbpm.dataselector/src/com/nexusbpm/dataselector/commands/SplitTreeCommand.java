package com.nexusbpm.dataselector.commands;

import java.util.Collection;
import java.util.Set;

import com.nexusbpm.dataselector.controller.GraphUpdateQueue;
import com.nexusbpm.dataselector.model.LSCondition;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSNodeConnector;
import com.nexusbpm.dataselector.model.LSSplit;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.model.LSWhere;
import com.nexusbpm.dataselector.model.config.LSColumn;
import com.nexusbpm.dataselector.requests.SetDirtyRequest;
import com.nexusbpm.dataselector.util.Range;
import com.nexusbpm.dataselector.util.TreeLayout;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class SplitTreeCommand extends AbstractGraphUpdateCommand {
    private LSTree tree;
    private LSNode node;
    private LSColumn column;
    private Collection<Set<Range>> rangeSets;
    
    public SplitTreeCommand(EventRequestBus bus, LSNode node, LSColumn column, Collection<Set<Range>> rangeSets) {
        super("Split Tree", bus);
        this.node = node;
        this.column = column;
        this.rangeSets = rangeSets;
        this.tree = node.getTree();
    }
    
    @Override
    public boolean canExecute() {
        return node != null && column != null && rangeSets != null && rangeSets.size() > 0 && tree != null;
    }
    
    @Override
    public void execute() {
        GraphUpdateQueue queue = getGraphUpdateQueue();
        synchronized(tree) {
            queue.startNonFlushingOperation();
            try {
                LSSplit split = new LSSplit(node);
                split.setColumn(column.getName());
                node.setSplit(split);
                
                for(Set<Range> rset : rangeSets) {
                    LSNode child = new LSNode(tree);
                    tree.addNode(child);
                    child.setDefaultBounds();
//                    child.setBounds(new Rectangle(0, 0, 180, 175));
                    
                    for(Range r : rset) {
                        LSCondition cond = new LSCondition(child);
                        if(r.getMinMatch() != null) {
                            LSWhere where = new LSWhere(cond);
                            where.setMatch(r.getMinMatch());
                            where.setValue(r.getMin());
                            cond.addWhereClause(where);
                        }
                        if(r.getMaxMatch() != null) {
                            LSWhere where = new LSWhere(cond);
                            where.setMatch(r.getMaxMatch());
                            where.setValue(r.getMax());
                            cond.addWhereClause(where);
                        }
                        child.addCondition(cond);
                    }
                    LSNodeConnector connector = new LSNodeConnector(node, child);
                    child.setConnector(connector);
                    node.addSubNode(child);
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
    public boolean canUndo() {
        return false;
    }
}
