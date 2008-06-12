package com.nexusbpm.dataselector.drools.model;

import java.util.HashMap;
import java.util.Map;

import com.nexusbpm.dataselector.drools.Plugin;
import com.nexusbpm.dataselector.model.AbstractModelExtension;
import com.nexusbpm.dataselector.model.LSNode;

public class PropertyMap extends AbstractModelExtension {
    public static final String PROPERTY_MAP_CHANGED = "droolsPropertyMapChanged";
    public static final String ELEMENT_KEY = "droolsPropertyMap";
    
    private Map<String, String> values;
    
    public PropertyMap(LSNode parent) {
        super(parent);
        values = new HashMap<String, String>();
    }
    
    @Override
    public void markDirty() {
        super.markDirty();
    }
    
    public Map<String, String> getValues() {
        return values;
    }
    
    public void fireValuesChanged() {
        firePropertyChange(PROPERTY_MAP_CHANGED, this, this);
    }
    
    public static void remove(LSNode element) {
        PropertyMap map = get(element, false);
        if(map != null) {
            element.removeExtension(Plugin.PLUGIN_ID, ELEMENT_KEY);
        }
    }
    
    public static PropertyMap get(LSNode element, boolean create) {
        PropertyMap map = null;
        if(element != null) {
            map = (PropertyMap) element.getExtension(Plugin.PLUGIN_ID, ELEMENT_KEY);
            if(map == null && create) {
                map = new PropertyMap(element);
                element.setExtension(Plugin.PLUGIN_ID, ELEMENT_KEY, map);
                map.markDirty();
            }
        }
        return map;
    }
}
