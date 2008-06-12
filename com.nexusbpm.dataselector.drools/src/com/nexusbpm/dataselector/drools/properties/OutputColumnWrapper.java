package com.nexusbpm.dataselector.drools.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.nexusbpm.dataselector.drools.model.PropertyList;
import com.nexusbpm.dataselector.drools.model.PropertyMap;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSTree;

public class OutputColumnWrapper implements Comparable<OutputColumnWrapper> {
    private LSNode node;
    private String name;
    
    public OutputColumnWrapper(LSNode node, String name) {
        this.node = node;
        this.name = name;
    }
    
    public LSNode getNode() {
        return node;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        if(name == null || name.length() == 0 || name == this.name || name.equals(this.name)) {
            return;
        }
        LSTree tree = node.getTree();
        PropertyList list = PropertyList.get(tree, true);
        List<String> names = list.getValues();
        int index = names.indexOf(this.name);
        if(index >= 0) {
            names.add(index, name);
        } else {
            names.add(name);
        }
        names.remove(this.name);
        
        List<PropertyMap> maps = new ArrayList<PropertyMap>();
        
        for(LSNode n : tree.getNodes()) {
            PropertyMap map = PropertyMap.get(n, false);
            if(map != null) {
                // for all the nodes in the tree that had a mapping under the old name
                Map<String, String> m = map.getValues();
                if(m.containsKey(this.name)) {
                    // remove the old mapping and put in a new one for the new name
                    m.put(name, m.remove(this.name));
                    maps.add(map);
                }
            }
        }
        
        this.name = name;
        list.fireValuesChanged();
        for(PropertyMap map : maps) {
            map.fireValuesChanged();
        }
        list.markDirty();
    }
    
    public String getValue() {
        String value = null;
        PropertyMap map = PropertyMap.get(node, false);
        if(map != null) {
            value = map.getValues().get(name);
        }
        return value;
    }
    
    public void setValue(String value) {
        PropertyMap map = PropertyMap.get(node, value != null);
        if(map != null) {
            map.getValues().put(name, value);
            map.fireValuesChanged();
            map.markDirty();
        }
    }
    
    public int compareTo(OutputColumnWrapper o) {
        int value = node.compareTo(o.node);
        if(value == 0) {
            value = name.compareTo(o.name);
        }
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj instanceof OutputColumnWrapper) {
            OutputColumnWrapper other = (OutputColumnWrapper) obj;
            return node.equals(other.node) && name.equals(other.name);
        }
        return false;
    }
}
