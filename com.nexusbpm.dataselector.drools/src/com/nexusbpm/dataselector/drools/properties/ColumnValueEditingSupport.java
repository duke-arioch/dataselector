package com.nexusbpm.dataselector.drools.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.nexusbpm.dataselector.model.config.LSColumn;

public class ColumnValueEditingSupport extends TextBoxEditingSupport {
    private OutputColumnWrapper column;
    
    public ColumnValueEditingSupport(ColumnViewer viewer, FormToolkit toolkit) {
        super(viewer, toolkit);
    }
    
    @Override
    protected String[] getNames() {
        if(column == null || column.getNode() == null) {
            return null;
        }
        List<String> names = new ArrayList<String>();
        for(LSColumn c : column.getNode().getTree().getConfig().getColumns()) {
            names.add(c.getName());
        }
        return names.toArray(new String[names.size()]);
    }
    
    @Override
    protected CellEditor getCellEditor(Object element) {
        column = (OutputColumnWrapper) element;
        return super.getCellEditor(element);
    }
    
    @Override
    protected Object getValue(Object element) {
        return ((OutputColumnWrapper) element).getValue();
    }
    
    @Override
    protected void setValue(Object element, Object value) {
        ((OutputColumnWrapper) element).setValue((String) value);
        getViewer().refresh();
    }
}
