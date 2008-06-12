package com.nexusbpm.dataselector.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

import com.nexusbpm.dataselector.model.AbstractModelElement;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSStats;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.model.config.LSColumn;

public class StatisticsContentLabelProvider implements IStructuredContentProvider, ITableLabelProvider, PropertyChangeListener {
    private ObjectConverter converter;
    private StructuredViewer viewer;
    private LSNode node;
    
    public StatisticsContentLabelProvider(StructuredViewer viewer) {
        this.viewer = viewer;
        converter = new ObjectConverter();
    }
    
    protected void disconnectListeners() {
        if(node == null) return;
        node.removePropertyChangeListener(this);
        LSStats stats = node.getStats();
        if(stats != null) {
            stats.removePropertyChangeListener(this);
        }
    }
    
    protected void connectListeners() {
        if(node == null) return;
        node.addPropertyChangeListener(this);
        LSStats stats = node.getStats();
        if(stats != null) {
            stats.addPropertyChangeListener(this);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(LSNode.PROPERTY_NODE_STATS)) {
            LSStats oldStats = (LSStats) evt.getOldValue();
            LSStats newStats = (LSStats) evt.getNewValue();
            if(oldStats != null) {
                oldStats.removePropertyChangeListener(this);
            }
            if(newStats != null) {
                newStats.addPropertyChangeListener(this);
            }
            refreshTable();
        } else if(evt.getPropertyName().equals(LSStats.PROPERTY_ADD_COLUMN_STATS) ||
                evt.getPropertyName().equals(LSStats.PROPERTY_CLEAR_COLUMN_STATS) ||
                evt.getPropertyName().equals(LSStats.PROPERTY_STATS_ROW_COUNT) ||
                evt.getPropertyName().equals(LSStats.PROPERTY_UPDATE_COLUMN_STATS)) {
            refreshTable();
        }
    }
    
    protected void refreshTable() {
        viewer.update(node.getTree().getConfig().getColumns().toArray(), new String[] {"p"});
    }
    
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if(this.viewer != viewer) {
            throw new IllegalStateException(getClass().getSimpleName() +
                    " cannot be used for multiple viewers!");
        }
        disconnectListeners();
        if(newInput != null && !(newInput instanceof LSNode)) {
            throw new IllegalArgumentException(getClass().getSimpleName() +
                    " cannot handle input of type " + newInput.getClass().getName());
        }
        node = (LSNode) newInput;
        connectListeners();
    }
    public Object[] getElements(Object inputElement) {
        if(inputElement instanceof AbstractModelElement) {
            LSTree tree = ((AbstractModelElement) inputElement).getTree();
            return tree.getConfig().getColumns().toArray();
        }
        return new Object[0];
    }
    
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }
    public String getColumnText(Object element, int columnIndex) {
        String value = "";
        if(element instanceof LSColumn) {
            value = ColumnTableUtil.getColumnText(node, (LSColumn) element, columnIndex);
        }
        try {
            if(value.startsWith("<=")) {
                value = "<=" + converter.reformatForDisplay(value.substring(2));
            } else {
                value = converter.reformatForDisplay(value);
            }
        } catch(Exception e) {
            // ignore
        }
        return value;
    }
    
    public boolean isLabelProperty(Object element, String property) {
        return element instanceof LSColumn;
    }
    public void addListener(ILabelProviderListener listener) {
    }
    public void removeListener(ILabelProviderListener listener) {
    }
    
    public void dispose() {
    }
}
