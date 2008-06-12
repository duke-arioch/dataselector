package com.nexusbpm.database.table;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.kupzog.ktable.KTableCellEditor;
import de.kupzog.ktable.KTableCellRenderer;
import de.kupzog.ktable.KTableDefaultModel;
import de.kupzog.ktable.renderers.FixedCellRenderer;
import de.kupzog.ktable.renderers.TextCellRenderer;

public class ResultSetCachedTableModel extends KTableDefaultModel {
    private ResultSet rs;
    private int window = 25;
    private int maxRows = 50;
    private int CACHE_MAX = 1000;
    private boolean completelyRead = false;
    private List<ResultSetTableUpdateListener> listeners = new ArrayList<ResultSetTableUpdateListener>();
    private LinkedList<Integer> mru = new LinkedList<Integer>();
    private Map<Integer, List<Object>> cache = new HashMap<Integer, List<Object>>();
    private List<ResultSetTableExceptionListener> exceptionListeners = new ArrayList<ResultSetTableExceptionListener>();
    
    private final FixedCellRenderer headerRenderer =
        new FixedCellRenderer(FixedCellRenderer.STYLE_FLAT);
    
    private final TextCellRenderer cellRenderer =
        new TextCellRenderer(TextCellRenderer.INDICATION_FOCUS);
    
    public ResultSetCachedTableModel(ResultSet resultSet) throws SQLException {
        this.rs = resultSet;
        rs.next();
    }
    
    public KTableCellEditor doGetCellEditor(int col, int row) {
        return null;
    }
    
    public KTableCellRenderer doGetCellRenderer(int col, int row) {
        if(col == 0 || row == 0) {
            return headerRenderer;
        } else {
            return cellRenderer;
        }
    }
    
    public int doGetColumnCount() {
        int retval = 1;
        try {
            retval = rs.getMetaData().getColumnCount() + 1;
        } catch(SQLException e) {
            fireException(e);
        }
        return retval;
    }
    
    public Object doGetContentAt(int col, int row) {
        if(row == 0) {
            return getColumnHeader(col);
        } else if(col == 0) {
            return "" + row;
        } else {
            Object r = getValueAt(row, col);
            if(r != null) return r;
            else return "";
        }
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object r = "*";
            try {
                if (rowIndex > maxRows ) rowIndex = maxRows;
//                cache(rowIndex); // TODO using cache instead of the following IF statement
                // will cause the last few rows to get cut off
                if (!cache.containsKey(Integer.valueOf(rowIndex + 1))) {
                    rs.absolute(rowIndex);
                    ArrayList<Object> list = new ArrayList<Object>();
                    for (int i = 1; i < getColumnCount(); i++) {
                        Object o = rs.getObject(i);
                        list.add(o);
                    }
                    if (cache.size() > CACHE_MAX) {
                        Integer toGo = mru.removeLast();
                        cache.remove(toGo);
                    }
                    mru.addFirst(Integer.valueOf(rowIndex + 1));
                    cache.put(Integer.valueOf(rowIndex + 1), list);
                }
                r = cache.get(Integer.valueOf(rowIndex + 1)).get(columnIndex - 1);
                
                if (rowIndex + 1 + window > maxRows && !completelyRead) {
                    maxRows = Math.max(maxRows, rowIndex + 1 + window);
//                    sendNotice(new TableModelEvent(this, rowIndex + 1, maxRows, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
                }
            } catch (SQLException e) {
                completelyRead = true;
                maxRows = rowIndex;
                fireException(e);
                fireUpdate(maxRows);
                //exhausted result set - go no further!
            }
        return r;
    }
    
    protected void cache(int rowIndex) throws SQLException {
        for(int i = 0; i < 10 && rowIndex <= maxRows; i++) {
            if (!cache.containsKey(Integer.valueOf(rowIndex + 1))) {
                rs.absolute(rowIndex);
                ArrayList<Object> list = new ArrayList<Object>();
                for (int index = 1; index < getColumnCount(); index++) {
                    Object o = rs.getObject(index);
                    list.add(o);
                }
                if (cache.size() > CACHE_MAX) {
                    Integer toGo = mru.removeLast();
                    cache.remove(toGo);
                }
                mru.addFirst(Integer.valueOf(rowIndex + 1));
                cache.put(Integer.valueOf(rowIndex + 1), list);
            }
            rowIndex += 1;
        }
    }
    
    protected Object getColumnHeader(int col) {
        if(col == 0) {
            return "row";
        } else {
            try {
                return rs.getMetaData().getColumnName(col);
            } catch(SQLException e) {
                fireException(e);
                return "";
            }
        }
    }
    
    public int doGetRowCount() {
        return maxRows;
    }
    
    public String doGetTooltipAt(int col, int row) {
        Object value = null;
        List<Object> list = cache.get(Integer.valueOf(row + 1));
        if(list != null && list.size() >= col) {
            value = list.get(col - 1);
        }
        if(value != null) {
            return value.toString();
        } else {
            return null;
        }
    }
    
    public void doSetContentAt(int col, int row, Object value) {}
    
    public int getInitialColumnWidth(int column) {
        if(column == 0) return 35;
        return 100;
    }
    
    public int getInitialRowHeight(int row) {
        if (row==0) return 22;
        return 18;
    }

    public int getFixedHeaderColumnCount() {
        return 1;
    }

    public int getFixedHeaderRowCount() {
        return 1;
    }

    public int getFixedSelectableColumnCount() {
        return 0;
    }

    public int getFixedSelectableRowCount() {
        return 0;
    }

    public int getRowHeightMinimum() {
        return 18;
    }

    public boolean isColumnResizable(int col) {
        return true;
    }

    public boolean isRowResizable(int row) {
        return false;
    }
    
    public void addExceptionListener(ResultSetTableExceptionListener listener) {
        if(!exceptionListeners.contains(listener)) {
            exceptionListeners.add(listener);
        }
    }
    
    public void removeExceptionListener(ResultSetTableExceptionListener listener) {
        exceptionListeners.remove(listener);
    }
    
    private void fireException(Exception e) {
        Object[] listenerArray = exceptionListeners.toArray();
        for(int index = 0; index < listenerArray.length; index++) {
            ResultSetTableExceptionListener listener = (ResultSetTableExceptionListener) listenerArray[index];
            try {
                listener.handleException(e);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public void addUpdateListener(ResultSetTableUpdateListener listener) {
        if(!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeUpdateListener(ResultSetTableUpdateListener listener) {
        listeners.remove(listener);
    }
    
    private void fireUpdate(int rows) {
        Object[] listenerArray = listeners.toArray();
        for(int index = 0; index < listenerArray.length; index++) {
            ResultSetTableUpdateListener listener = (ResultSetTableUpdateListener) listenerArray[index];
            try {
                listener.handleUpdate(rows);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
