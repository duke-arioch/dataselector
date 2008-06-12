package com.nexusbpm.dataselector.drools.model;

import java.util.ArrayList;
import java.util.List;

import com.nexusbpm.dataselector.drools.Plugin;
import com.nexusbpm.dataselector.model.AbstractModelExtension;
import com.nexusbpm.dataselector.model.LSTree;

public class PropertyList extends AbstractModelExtension {
    public static final String PROPERTY_LIST_CHANGED = "droolsPropertyListChanged";
    public static final String ELEMENT_KEY = "droolsPropertyList";
    
    private List<String> values;
    
    public PropertyList(LSTree parent) {
        super(parent);
        values = new ArrayList<String>();
    }
    
    @Override
    public void markDirty() {
        super.markDirty();
    }
    
    public List<String> getValues() {
        return values;
    }
    
    public void fireValuesChanged() {
        firePropertyChange(PROPERTY_LIST_CHANGED, this, this);
    }
    
    public static void remove(LSTree element) {
        PropertyList list = get(element, false);
        if(list != null) {
            element.removeExtension(Plugin.PLUGIN_ID, ELEMENT_KEY);
        }
    }
    
    public static PropertyList get(LSTree element, boolean create) {
        PropertyList list = null;
        if(element != null) {
            list = (PropertyList) element.getExtension(Plugin.PLUGIN_ID, ELEMENT_KEY);
            if(list == null && create) {
                list = new PropertyList(element);
                element.setExtension(Plugin.PLUGIN_ID, ELEMENT_KEY, list);
                list.markDirty();
            }
        }
        return list;
    }
}
