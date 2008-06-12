package com.nexusbpm.dataselector.stats;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.nexusbpm.dataselector.model.LSNode;

public class NodeQueue {
    private List<LSNode> queue;
    
    public NodeQueue() {
        queue = new LinkedList<LSNode>();
    }
    
    public void add(LSNode node) {
        synchronized(queue) {
            queue.add(node);
        }
    }
    
    public LSNode next() {
        synchronized(queue) {
            if(queue.size() > 0) {
                return queue.remove(0);
            } else {
                return null;
            }
        }
    }
    
    public int size() {
        synchronized(queue) {
            return queue.size();
        }
    }
    
    /** Removes every occurrence of the given node from the queue. */
    public void remove(LSNode node) {
        synchronized(queue) {
            Iterator<LSNode> iter = queue.iterator();
            while(iter.hasNext()) {
                if(iter.next() == node) {
                    iter.remove();
                }
            }
        }
    }
    
    /** Removes every occurrence of the given nodes from the queue. */
    public void removeAll(Collection<LSNode> nodes) {
        synchronized(queue) {
            Iterator<LSNode> iter = queue.iterator();
            while(iter.hasNext()) {
                if(nodes.contains(iter.next())) {
                    iter.remove();
                }
            }
        }
    }
    
    public void clear() {
        synchronized(queue) {
            queue.clear();
        }
    }
}
