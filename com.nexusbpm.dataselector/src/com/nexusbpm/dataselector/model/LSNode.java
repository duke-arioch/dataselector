package com.nexusbpm.dataselector.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

public class LSNode extends NamedModelElement implements Comparable<LSNode> {
    public static final String PROPERTY_ADD_CONDITION = "addCondition";
    public static final String PROPERTY_REMOVE_CONDITION = "removeCondition";
    public static final String PROPERTY_NODE_CONNECTOR = "nodeConnector";
    public static final String PROPERTY_NODE_STATS = "nodeStats";
    public static final String PROPERTY_NODE_SPLIT = "nodeSplit";
    public static final String PROPERTY_ADD_SUB_NODE = "addSubNode";
    public static final String PROPERTY_REMOVE_SUB_NODE = "removeSubNode";
    
    public static final String PROPERTY_NODE_BOUNDS = "nodeBounds";
    public static final String PROPERTY_NODE_BOUNDS_X = "nodeBoundsX";
    public static final String PROPERTY_NODE_BOUNDS_Y = "nodeBoundsY";
    public static final String PROPERTY_NODE_BOUNDS_WIDTH = "nodeBoundsWidth";
    public static final String PROPERTY_NODE_BOUNDS_HEIGHT = "nodeBoundsHeight";
    
    private static IPropertyDescriptor[] DESCRIPTORS;
    
    private TreeSet<LSCondition> conditions;
    private LSStats stats;
    private LSSplit split;
    private LSNodeConnector connector;
    private TreeSet<LSNode> subNodes;
    
    private Object lockOwner;
    
    private int x;
    private int y;
    private int width;
    private int height;
    
    public LSNode(LSTree tree) {
        super(tree);
        conditions = new TreeSet<LSCondition>();
        subNodes = new TreeSet<LSNode>();
    }
    
    public synchronized boolean isLocked() {
        return lockOwner != null;
    }
    
    public synchronized boolean lock(Object lockOwner) {
        if(this.lockOwner == null) {
            this.lockOwner = lockOwner;
            return true;
        }
        return false;
    }
    
    public synchronized boolean unlock(Object lockOwner) {
        if(this.lockOwner == lockOwner) {
            this.lockOwner = null;
            return true;
        }
        return false;
    }
    
    public Object getLockOwner() {
        return lockOwner;
    }
    
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        if(this.x != x) {
            Rectangle old = getBounds();
            this.x = x;
            firePropertyChange(PROPERTY_NODE_BOUNDS, old, getBounds());
        }
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        if(this.y != y) {
            Rectangle old = getBounds();
            this.y = y;
            firePropertyChange(PROPERTY_NODE_BOUNDS, old, getBounds());
        }
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        if(this.width != width) {
            Rectangle old = getBounds();
            this.width = width;
            firePropertyChange(PROPERTY_NODE_BOUNDS, old, getBounds());
        }
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        if(this.height != height) {
            Rectangle old = getBounds();
            this.height = height;
            firePropertyChange(PROPERTY_NODE_BOUNDS, old, getBounds());
        }
    }
    
    public void setDefaultBounds() {
        setBounds(getDefaultBounds());
    }
    
    protected Rectangle getDefaultBounds() {
        return new Rectangle(x, y, 140, 180);
    }
    
    public void setBounds(Rectangle r) {
        Rectangle old = getBounds();
        this.x = r.x;
        this.y = r.y;
        this.width = r.width;
        this.height = r.height;
        Rectangle bounds = getBounds();
        if(!old.equals(bounds)) {
            firePropertyChange(PROPERTY_NODE_BOUNDS, old, bounds);
        }
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    public boolean isRemainderNode() {
        return conditions.size() == 1 && conditions.first().isRemainderCondition();
    }
    
    public Set<LSCondition> getConditions() {
        return Collections.unmodifiableSet(conditions);
    }
    
    public void addCondition(LSCondition condition) {
        conditions.add(condition);
        firePropertyChange(PROPERTY_ADD_CONDITION, null, condition);
    }
    
    public void removeCondition(LSCondition condition) {
        conditions.remove(condition);
        firePropertyChange(PROPERTY_REMOVE_CONDITION, condition, null);
    }
    
    public LSStats getStats() {
        return stats;
    }
    
    public void setStats(LSStats stats) {
        LSStats oldStats = this.stats;
        this.stats = stats;
        firePropertyChange(PROPERTY_NODE_STATS, oldStats, stats);
    }
    
    public LSNodeConnector getConnector() {
        return connector;
    }
    
    public void setConnector(LSNodeConnector connector) {
        LSNodeConnector oldConnector = this.connector;
        this.connector = connector;
        firePropertyChange(PROPERTY_NODE_CONNECTOR, oldConnector, connector);
    }
    
    public LSSplit getSplit() {
        return split;
    }
    
    public void setSplit(LSSplit split) {
        LSSplit oldSplit = this.split;
        this.split = split;
        firePropertyChange(PROPERTY_NODE_SPLIT, oldSplit, split);
    }
    
    public void clearSubNodes() {
        if(subNodes == null || subNodes.size() == 0) {
            return;
        }
        for(LSNode node : new ArrayList<LSNode>(subNodes)) {
            // the order of these statements is important
            node.clearSubNodes();
            node.setConnector(null);
            removeSubNode(node);
            getTree().removeNode(node);
        }
        setSplit(null);
    }
    
    public Set<LSNode> getSubNodes() {
        return Collections.unmodifiableSet(subNodes);
    }
    
    public void addSubNode(LSNode node) {
        subNodes.add(node);
        firePropertyChange(PROPERTY_ADD_SUB_NODE, null, node);
    }
    
    public void removeSubNode(LSNode node) {
        subNodes.remove(node);
        firePropertyChange(PROPERTY_REMOVE_SUB_NODE, node, null);
    }
    
    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        if(DESCRIPTORS == null) {
            IPropertyDescriptor[] parentDescriptors = super.getPropertyDescriptors();
            int offset = parentDescriptors.length;
            DESCRIPTORS = new IPropertyDescriptor[offset + 4];
            for(int index = 0; index < parentDescriptors.length; index++) {
                DESCRIPTORS[index] = parentDescriptors[index];
            }
            String locCategory = "Location";
            String sizeCategory = "Size";
            ICellEditorValidator locValidator = new ICellEditorValidator() {
                public String isValid(Object value) {
                    try {
                        Integer.parseInt(String.valueOf(value));
                    } catch(NumberFormatException e) {
                        return "The value must be a valid number";
                    }
                    return null;
                }
            };
            ICellEditorValidator sizeValidator = new ICellEditorValidator() {
                public String isValid(Object value) {
                    try {
                        int v = Integer.parseInt(String.valueOf(value));
                        if(v <= 0) {
                            return "The value must be greater than zero";
                        }
                    } catch(NumberFormatException e) {
                        return "The value must be a valid number";
                    }
                    return null;
                }
            };
            DESCRIPTORS[offset] = createTextPropertyDescriptor(
                    PROPERTY_NODE_BOUNDS_X, "x",
                    "The x coordinate of the left side of the node",
                    locCategory, locValidator, true);
            DESCRIPTORS[offset + 1] = createTextPropertyDescriptor(
                    PROPERTY_NODE_BOUNDS_Y, "y",
                    "The y coordinate of the top side of the node",
                    locCategory, locValidator, true);
            DESCRIPTORS[offset + 2] = createTextPropertyDescriptor(
                    PROPERTY_NODE_BOUNDS_WIDTH, "width",
                    "The width of the node",
                    sizeCategory, sizeValidator, false);
            DESCRIPTORS[offset + 3] = createTextPropertyDescriptor(
                    PROPERTY_NODE_BOUNDS_HEIGHT, "height",
                    "The height of the node",
                    sizeCategory, sizeValidator, false);
        }
        return DESCRIPTORS;
    }
    
    @Override
    public Object getPropertyValue(Object id) {
        if(id.equals(PROPERTY_NODE_BOUNDS_X)) {
            return String.valueOf(getX());
        } else if(id.equals(PROPERTY_NODE_BOUNDS_Y)) {
            return String.valueOf(getY());
        } else if(id.equals(PROPERTY_NODE_BOUNDS_WIDTH)) {
            return String.valueOf(getWidth());
        } else if(id.equals(PROPERTY_NODE_BOUNDS_HEIGHT)) {
            return String.valueOf(getHeight());
        } else {
            return super.getPropertyValue(id);
        }
    }
    
    @Override
    public boolean isPropertySet(Object id) {
        if(id.equals(PROPERTY_NODE_BOUNDS_WIDTH)) {
            return getWidth() != getDefaultBounds().width;
        } else if(id.equals(PROPERTY_NODE_BOUNDS_HEIGHT)) {
            return getHeight() != getDefaultBounds().height;
        } else {
            return super.isPropertySet(id);
        }
    }
    
    @Override
    public void resetPropertyValue(Object id) {
        if(id.equals(PROPERTY_NODE_BOUNDS_WIDTH)) {
            setWidth(getDefaultBounds().width);
        } else if(id.equals(PROPERTY_NODE_BOUNDS_HEIGHT)) {
            setHeight(getDefaultBounds().height);
        } else {
            super.resetPropertyValue(id);
        }
    }
    
//    @Override
//    public void setPropertyValue(Object id, Object value) {
//        try {
//            if(id.equals(PROPERTY_NODE_BOUNDS_X)) {
//                setX(Integer.parseInt(String.valueOf(value)));
//            } else if(id.equals(PROPERTY_NODE_BOUNDS_Y)) {
//                setY(Integer.parseInt(String.valueOf(value)));
//            } else if(id.equals(PROPERTY_NODE_BOUNDS_WIDTH)) {
//                setWidth(Integer.parseInt(String.valueOf(value)));
//            } else if(id.equals(PROPERTY_NODE_BOUNDS_HEIGHT)) {
//                setHeight(Integer.parseInt(String.valueOf(value)));
//            } else {
//                super.setPropertyValue(id, value);
//            }
//        } catch(NumberFormatException e) {
//        }
//    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + hashCode() + ":" + getName();
    }
    
    public int compareTo(LSNode o) {
        if(this == o) return 0;
        LSNode myParent = null;
        LSNode otherParent = null;
        if(getConnector() != null) {
            myParent = getConnector().getSource();
        }
        if(o.getConnector() != null) {
            otherParent = o.getConnector().getSource();
        }
        // if the nodes are not all children of the same parent, use their node indices in the tree
        if(myParent == null || otherParent == null || myParent != otherParent) {
            LSTree tree = getTree();
            return tree.getNodeIndex(this) - tree.getNodeIndex(o);
        }
        // otherwise compare the nodes' conditions
        if(conditions.size() == 0) {
            return -1;
        } else if(o.conditions.size() == 0) {
            return 1;
        } else {
            return conditions.first().compareTo(o.conditions.first());
        }
    }
}
