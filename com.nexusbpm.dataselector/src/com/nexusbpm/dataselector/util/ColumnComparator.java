package com.nexusbpm.dataselector.util;

import java.math.BigDecimal;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.config.LSColumn;

public class ColumnComparator extends ViewerComparator {
    private int column;
    private int direction = 1;
    
    public ColumnComparator() {
    }
    
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        LSColumn c1 = (LSColumn) e1;
        LSColumn c2 = (LSColumn) e2;
        if(c1 == null && c2 == null) {
            return 0;
        } else if(c1 == null) {
            return 1;
        } else if(c2 == null) {
            return -1;
        } else {
            int value = 0;
            int column = getColumn();
            
            while(value == 0 && column >= 0) {
                switch(column) {
                    case 0:
                        value = c1.getOrdinal() - c2.getOrdinal();
                        break;
                    default:
                        LSNode node = null;
                        if(viewer.getInput() instanceof LSNode) {
                            node = (LSNode) viewer.getInput();
                        }
                        String v1 = ColumnTableUtil.getColumnText(node, c1, column);
                        String v2 = ColumnTableUtil.getColumnText(node, c2, column);
                        
                        try {
                            BigDecimal d1 = new BigDecimal(v1);
                            BigDecimal d2 = new BigDecimal(v2);
                            value = d1.compareTo(d2);
                        } catch(Exception e) {
                            value = v1.compareTo(v2);
                        }
                        
                        break;
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
}
