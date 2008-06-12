package com.nexusbpm.dataselector.drools.properties;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.nexusbpm.dataselector.drools.model.PropertyList;
import com.nexusbpm.dataselector.model.LSNode;

public class DroolsContentProvider implements IStructuredContentProvider {
    public DroolsContentProvider() {
    }
    
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
    
    public Object[] getElements(Object inputElement) {
        Object[] result = new Object[0];
        if(inputElement instanceof LSNode) {
            LSNode node = (LSNode) inputElement;
            PropertyList list = PropertyList.get(node.getTree(), false);
            if(list != null) {
                ArrayList<OutputColumnWrapper> resultList = new ArrayList<OutputColumnWrapper>();
                for(String s : list.getValues()) {
                    resultList.add(new OutputColumnWrapper(node, s));
                }
                result = resultList.toArray();
            }
        }
        return result;
    }
    
    public void dispose() {
    }
}
