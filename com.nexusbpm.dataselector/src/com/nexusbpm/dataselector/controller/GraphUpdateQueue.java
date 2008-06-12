package com.nexusbpm.dataselector.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GraphUpdateQueue {
    private boolean shutdown;
    private int autoFlushCount;
    
    private TreeController tree;
    private Set<Integer> treeUpdateSet;
    
    private Map<Controller, Set<Integer>> updateSets;
    
    private static final Integer UPDATE_REFRESH = Integer.valueOf(0);
    private static final Integer UPDATE_REFRESH_VISUALS = Integer.valueOf(1);
    private static final Integer UPDATE_REFRESH_CHILDREN = Integer.valueOf(2);
    private static final Integer UPDATE_REFRESH_SOURCE_CONNETIONS = Integer.valueOf(3);
    private static final Integer UPDATE_REFRESH_TARGET_CONNETIONS = Integer.valueOf(4);
    
    public GraphUpdateQueue() {
        updateSets = new HashMap<Controller, Set<Integer>>();
        treeUpdateSet = new HashSet<Integer>();
    }
    
    public void queueRefresh(Controller controller) {
        if(!shutdown) {
            if(autoFlushCount == 0) {
                controller.refresh();
            } else {
                queueUpdate(controller, UPDATE_REFRESH);
            }
        }
    }
    
    public void queueRefreshVisuals(Controller controller) {
        if(!shutdown) {
            if(autoFlushCount == 0) {
                controller.refreshVisuals();
            } else {
                queueUpdate(controller, UPDATE_REFRESH_VISUALS);
            }
        }
    }
    
    public void queueRefreshChildren(Controller controller) {
        if(!shutdown) {
            if(autoFlushCount == 0) {
                controller.refreshChildren();
            } else {
                queueUpdate(controller, UPDATE_REFRESH_CHILDREN);
            }
        }
    }
    
    public void queueRefreshSourceConnections(Controller controller) {
        if(!shutdown) {
            if(autoFlushCount == 0) {
                System.out.println("refreshing source connections now");
                controller.refreshSourceConnections();
            } else {
                System.out.println("queueing refresh source connections");
                queueUpdate(controller, UPDATE_REFRESH_SOURCE_CONNETIONS);
            }
        }
    }
    
    public void queueRefreshTargetConnections(Controller controller) {
        if(!shutdown) {
            if(autoFlushCount == 0) {
                controller.refreshTargetConnections();
            } else {
                queueUpdate(controller, UPDATE_REFRESH_TARGET_CONNETIONS);
            }
        }
    }
    
    protected void queueUpdate(Controller controller, Integer update) {
        if(controller instanceof TreeController) {
            tree = (TreeController) controller;
            treeUpdateSet.add(update);
        } else {
            getUpdateSet(controller).add(update);
        }
    }
    
    protected Set<Integer> getUpdateSet(Controller controller) {
        if(updateSets.get(controller) == null) {
            updateSets.put(controller, new HashSet<Integer>());
        }
        return updateSets.get(controller);
    }
    
    protected void flush() {
        for(Map.Entry<Controller, Set<Integer>> entry : updateSets.entrySet()) {
            Controller controller = entry.getKey();
            if(controller.isActive()) {
                Set<Integer> updates = entry.getValue();
                if(updates.contains(UPDATE_REFRESH)) {
                    controller.refresh();
                } else {
                    if(updates.contains(UPDATE_REFRESH_CHILDREN)) {
                        controller.refreshChildren();
                    }
                    if(updates.contains(UPDATE_REFRESH_SOURCE_CONNETIONS)) {
                        controller.refreshSourceConnections();
                    }
                    if(updates.contains(UPDATE_REFRESH_TARGET_CONNETIONS)) {
                        controller.refreshTargetConnections();
                    }
                    if(updates.contains(UPDATE_REFRESH_VISUALS)) {
                        controller.refreshVisuals();
                    }
                }
            }
        }
        updateSets.clear();
        if(tree != null) {
            if(treeUpdateSet.contains(UPDATE_REFRESH)) {
                tree.refresh();
            } else {
                if(treeUpdateSet.contains(UPDATE_REFRESH_CHILDREN)) {
                    tree.refreshChildren();
                }
                if(treeUpdateSet.contains(UPDATE_REFRESH_SOURCE_CONNETIONS)) {
                    tree.refreshSourceConnections();
                }
                if(treeUpdateSet.contains(UPDATE_REFRESH_TARGET_CONNETIONS)) {
                    tree.refreshTargetConnections();
                }
                if(treeUpdateSet.contains(UPDATE_REFRESH_VISUALS)) {
                    tree.refreshVisuals();
                }
            }
        }
        treeUpdateSet.clear();
    }
    
    public void startNonFlushingOperation() {
        autoFlushCount += 1;
    }
    
    public void endNonFlushingOperation() {
        if(autoFlushCount == 0) {
            throw new IllegalStateException("No non-flushing operation was started!");
        }
        autoFlushCount -= 1;
        if(autoFlushCount == 0) {
            flush();
        }
    }
    
    public void shutdown() {
        shutdown = true;
    }
}
