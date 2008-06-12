package com.nexusbpm.dataselector.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.draw2d.geometry.Rectangle;

import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.requests.SetDirtyRequest;
import com.nexusbpm.dataselector.util.TreeLayout;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class LayoutTreeCommand extends AbstractEventBusCommand {
    private LSTree tree;
    private Map<LSNode, Rectangle> boundsMap;
    
    public LayoutTreeCommand(EventRequestBus bus, LSTree tree) {
        super("Layout Tree", bus);
        this.tree = tree;
        boundsMap = new HashMap<LSNode, Rectangle>();
    }
    
    @Override
    public boolean canExecute() {
        return tree != null;
    }
    
    @Override
    public void execute() {
        for(LSNode node : tree.getNodes()) {
            boundsMap.put(node, node.getBounds());
        }
        redo();
    }
    
    @Override
    public boolean canUndo() {
        return true;
    }
    
    @Override
    public void undo() {
        synchronized(tree) {
            for(Entry<LSNode, Rectangle> entry : boundsMap.entrySet()) {
                entry.getKey().setBounds(entry.getValue());
            }
            sendRequest(new SetDirtyRequest(true));
        }
    }
    
    @Override
    public void redo() {
        synchronized(tree) {
            TreeLayout layout = new TreeLayout();
            layout.layout(tree);
            sendRequest(new SetDirtyRequest(true));
        }
    }
}
