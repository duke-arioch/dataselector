package com.nexusbpm.dataselector.model;

public class LSNodeConnector extends AbstractModelElement implements Comparable<LSNodeConnector> {
//    public static final String PROPERTY_NODE_CONNECTOR_SOURCE = "nodeConnectorSource";
//    public static final String PROPERTY_NODE_CONNECTOR_TARGET = "nodeConnectorTarget";
    
    private LSNode target;
    
//    public LSNodeConnector(LSNode source) {
//        super(source);
//    }
    
    public LSNodeConnector(LSNode source, LSNode target) {
        super(source);
        this.target = target;
    }
    
    public LSNode getParent() {
        return (LSNode) super.getParent();
    }
    
    public LSNode getSource() {
        return getParent();
    }
    
//    public void setSource(LSNode source) {
//        LSNode oldSource = this.source;
//        this.source = source;
//        firePropertyChange(PROPERTY_NODE_CONNECTOR_SOURCE, oldSource, source);
//    }
    
    public LSNode getTarget() {
        return target;
    }
    
//    public void setTarget(LSNode target) {
//        LSNode oldTarget = this.target;
//        this.target = target;
//        firePropertyChange(PROPERTY_NODE_CONNECTOR_TARGET, oldTarget, target);
//    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + hashCode() + ":" +
            (getSource() == null ? "NULL" : getSource().getName()) + "->" + (target == null ? "NULL" : target.getName());
    }
    
    public int compareTo(LSNodeConnector o) {
        return target.compareTo(o.target);
    }
}
