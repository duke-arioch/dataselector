package com.nexusbpm.dataselector.util;

import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.nexusbpm.database.info.DBInfo;
import com.nexusbpm.database.info.DBInfoFactory;
import com.nexusbpm.dataselector.model.LSColumnStats;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSStats;
import com.nexusbpm.dataselector.model.config.LSColumn;

public class ColumnTableUtil {
    private ColumnTableUtil() {}
    
    public static void adapt(TableViewer viewer) {
        Table table = viewer.getTable();
        
        StatisticsContentLabelProvider provider = new StatisticsContentLabelProvider(viewer);
        ColumnComparator comparator = new ColumnComparator();
        
        viewer.setLabelProvider(provider);
        viewer.setComparator(comparator);
        viewer.setContentProvider(provider);
        viewer.addFilter(new PredictorFilter(viewer, true));
        viewer.addFilter(new InvalidColumnFilter());
        
        TableLayout tlayout = new ColumnTableLayout();
        tlayout.addColumnData(new ColumnWeightData(14, true));
        tlayout.addColumnData(new ColumnWeightData(35, true));
        tlayout.addColumnData(new ColumnWeightData(30, true));
        tlayout.addColumnData(new ColumnWeightData(25, true));
        tlayout.addColumnData(new ColumnWeightData(25, true));
        tlayout.addColumnData(new ColumnWeightData(25, true));
        tlayout.addColumnData(new ColumnWeightData(25, true));
        tlayout.addColumnData(new ColumnWeightData(25, true));
        tlayout.addColumnData(new ColumnWeightData(25, true));
        
        table.setLayout(tlayout);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        
        createTableColumn(viewer, SWT.CENTER, "Ordinal", 0, comparator);
        createTableColumn(viewer, SWT.LEFT, "Name", 1, comparator);
        createTableColumn(viewer, SWT.LEFT, "Type", 2, comparator);
        createTableColumn(viewer, SWT.CENTER, "Distinct", 3, comparator);
        createTableColumn(viewer, SWT.CENTER, "Min", 4, comparator);
        createTableColumn(viewer, SWT.CENTER, "Max", 5, comparator);
        createTableColumn(viewer, SWT.CENTER, "Avg", 6, comparator);
        createTableColumn(viewer, SWT.CENTER, "StdDev", 7, comparator);
        createTableColumn(viewer, SWT.CENTER, "Sum", 8, comparator);
    }
    
    protected static void createTableColumn(
            final TableViewer viewer,
            int alignment,
            String text,
            final int columnNumber,
            final ColumnComparator comparator) {
        TableColumn column = new TableColumn(viewer.getTable(), alignment);
        column.setText(text);
        if(columnNumber >= 0) {
            column.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                }
                public void widgetSelected(SelectionEvent e) {
                    if(comparator.getColumn() != columnNumber) {
                        comparator.setColumn(columnNumber);
                        comparator.setDirection(1);
                        viewer.refresh();
                    } else {
                        comparator.setDirection(0 - comparator.getDirection());
                        viewer.refresh();
                    }
                }
            });
        }
    }
    
    public static class InvalidColumnFilter extends ViewerFilter {
        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if(element instanceof LSColumn) {
                LSColumn column = (LSColumn) element;
                DBInfo info =
                    DBInfoFactory.getInstance().getDBInfo(column.getTree().getConfig().getDriver().getName());
                return !info.isOther(column);
            }
            return true;
        }
    }
    
    public static String getColumnText(LSNode node, LSColumn column, int index) {
        if(index > 8) return "?";
        switch(index) {
            case 0: return String.valueOf(column.getOrdinal());
            case 1: return column.getName();
            case 2: return column.getTypeName();
            default:
        }
        LSStats stats = (node != null) ? node.getStats() : null;
        LSColumnStats cstats = null;
        if(stats != null) {
            for(LSColumnStats c : stats.getColumnStats()) {
                if(c.getColumnOrdinal() == column.getOrdinal()) {
                    cstats = c;
                    break;
                }
            }
        }
        if(cstats != null) {
            switch(index) {
                case 3: return String.valueOf(cstats.getDistinctCount());
                case 4: return cstats.getMin();
                case 5: return cstats.getMax();
                case 6: return cstats.getAverage();
                case 7: return cstats.getStandardDeviation();
                case 8: return cstats.getSum();
                default:
            }
        }
        if(stats != null && index == 3 && stats.getRowCount() > -1) {
            return "<=" + stats.getRowCount();
        }
        if(node != null && index == 3) {
            // try to get an upper limit on the distinct count by going to a parent node
            LSNode n = node;
            while(n.getConnector() != null && n.getConnector().getSource() != null) {
                LSNode parent = n.getConnector().getSource();
                
                if(parent.getStats() != null && parent.getStats().getRowCount() >= 0) {
                    return "<=" + parent.getStats().getRowCount();
                }
                n = parent;
            }
        }
        return "?";
    }
}
