package com.nexusbpm.dataselector.drools.properties;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class ColumnNameEditingSupport extends TextBoxEditingSupport {
    public ColumnNameEditingSupport(ColumnViewer viewer, FormToolkit toolkit) {
        super(viewer, toolkit);
    }
    
    @Override
    protected String[] getNames() {
        return null;
    }
    
    @Override
    protected Object getValue(Object element) {
        return ((OutputColumnWrapper) element).getName();
    }
    
    @Override
    protected void setValue(Object element, Object value) {
        ((OutputColumnWrapper) element).setName((String) value);
        getViewer().refresh();
    }
}
