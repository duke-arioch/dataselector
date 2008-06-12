package com.nexusbpm.database.table;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public class ColumnComparator extends ViewerComparator {
    private int column;
    private int direction = 1;
    
    public ColumnComparator() {
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public int compare(Viewer viewer, Object e1, Object e2) {
        Object[] row1 = (Object[]) e1;
        Object[] row2 = (Object[]) e2;
        if(row1 == null && row2 == null) {
            return 0;
        } else if(row1 == null) {
            return 1;
        } else if(row2 == null) {
            return -1;
        } else {
            int value = 0;
            int column = getColumn();
            if(column >= row1.length || column >= row2.length) {
                column = Math.min(row1.length, row2.length) - 1;
            }
            
            while(value == 0 && column >= 0) {
                if(row1[column] == row2[column]) {
                    // do nothing (comparison value remains at zero)
                } else if(row1[column] == null) {
                    value = 1;
                } else if(row2[column] == null) {
                    value = -1;
                } else {
                    try {
                        value = ((Comparable)row1[column]).compareTo(row2[column]);
                    } catch(Exception e) {
                        value = row1[column].toString().compareTo(row2[column].toString());
                    }
                }
                column -= 1;
            }
            
            return value * getDirection();
        }
    }
    
    public int getColumn() {
        return column;
    }
    
    public void setColumn(int column) {
        this.column = column;
    }
    
    public int getDirection() {
        return direction;
    }
    
    public void setDirection(int direction) {
        this.direction = direction;
    }
    
    public SelectionListener createSelectionListener(int column, StructuredViewer viewer) {
        return new ColumnSelectionListener(column, viewer);
    }
    
    protected class ColumnSelectionListener implements SelectionListener {
        private StructuredViewer viewer;
        private int column;
        
        public ColumnSelectionListener(int column, StructuredViewer viewer) {
            this.column = column;
            this.viewer = viewer;
        }
        
        public void widgetDefaultSelected(SelectionEvent e) {
        }
        public void widgetSelected(SelectionEvent e) {
            if(getColumn() != column) {
                setColumn(column);
                setDirection(1);
                viewer.refresh();
            } else {
                setDirection(0 - getDirection());
                viewer.refresh();
            }
        }
    }
}
