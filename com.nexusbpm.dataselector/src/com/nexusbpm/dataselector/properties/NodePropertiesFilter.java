package com.nexusbpm.dataselector.properties;

import org.eclipse.jface.viewers.IFilter;

import com.nexusbpm.dataselector.controller.NodeController;

public class NodePropertiesFilter implements IFilter {
    public boolean select(Object object) {
        if(object instanceof NodeController) {
            return true;
        }
//        if(object instanceof IPropertySource) {
//            return true;
//        } else if(object instanceof IAdaptable) {
//            return ((IAdaptable) object).getAdapter(IPropertySource.class) != null;
//        }
        return false;
    }
    
}
