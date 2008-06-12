package com.nexusbpm.dataselector.drools.properties;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

public class DroolsLabelProvider extends CellLabelProvider implements ITableLabelProvider {
    private int columnIndex;
    
    public DroolsLabelProvider() {
    }
    
    public DroolsLabelProvider(int columnIndex) {
        this.columnIndex = columnIndex;
    }
    
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }
    
    public String getColumnText(Object element, int columnIndex) {
        String value = "";
        if(element instanceof OutputColumnWrapper) {
            OutputColumnWrapper column = (OutputColumnWrapper) element;
            if(columnIndex == 0) {
                value = column.getName();
            } else if(columnIndex == 1) {
                value = column.getValue();
            }
        }
        if(value == null) {
            value = "";
        }
//        if(element instanceof Entry) {
//            if(columnIndex == 0) {
//                value = (String) ((Entry<?, ?>) element).getKey();
//            } else if(columnIndex == 1) {
//                value = (String) ((Entry<?, ?>) element).getValue();
//            }
//        } else if(element instanceof String && columnIndex == 0) {
//            value = (String) element;
//        }
        return value;
    }
    
    @Override
    public void update(ViewerCell cell) {
        cell.setText(getColumnText(cell.getElement(), columnIndex));
    }
    
    public boolean isLabelProperty(Object element, String property) {
        return true; // TODO
    }
    
    public void addListener(ILabelProviderListener listener) {
    }
    public void removeListener(ILabelProviderListener listener) {
    }
    
    public void dispose() {
    }
}
