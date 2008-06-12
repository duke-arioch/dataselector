package com.nexusbpm.database.table;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

public class ResultSetContentProvider implements IStructuredContentProvider, ITableLabelProvider {
    private Object[][] data;
    
    public ResultSetContentProvider(Object[][] data) {
        this.data = data;
    }
    
    public void dispose() {
    }
    
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
    
    public Object[] getElements(Object inputElement) {
        return data;
    }
    
    public void addListener(ILabelProviderListener listener) {
    }
    
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }
    
    public void removeListener(ILabelProviderListener listener) {
    }
    
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }
    
    public String getColumnText(Object element, int columnIndex) {
        return String.valueOf(((Object[]) element)[columnIndex]);
    }
}
