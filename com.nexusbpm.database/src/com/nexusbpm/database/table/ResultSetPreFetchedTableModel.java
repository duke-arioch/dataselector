package com.nexusbpm.database.table;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.kupzog.ktable.KTableCellEditor;
import de.kupzog.ktable.KTableCellRenderer;
import de.kupzog.ktable.KTableDefaultModel;
import de.kupzog.ktable.renderers.FixedCellRenderer;
import de.kupzog.ktable.renderers.TextCellRenderer;

public class ResultSetPreFetchedTableModel extends KTableDefaultModel {
    private List<String> headers;
    private Object[][] data;
    
    private final FixedCellRenderer headerRenderer =
        new FixedCellRenderer(FixedCellRenderer.STYLE_FLAT);
    
    private final TextCellRenderer cellRenderer =
        new TextCellRenderer(TextCellRenderer.INDICATION_FOCUS);
    
    public ResultSetPreFetchedTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        headers = new ArrayList<String>(md.getColumnCount());
        for(int index = 0; index < md.getColumnCount(); index++) {
            headers.add(md.getColumnName(index + 1));
        }
        
        List<Object[]> dataList = new ArrayList<Object[]>();
        
        while(rs.next()) {
            Object[] row = new Object[headers.size()];
            
            for(int index = 0; index < md.getColumnCount(); index++) {
                row[index] = rs.getObject(index + 1);
            }
            
            dataList.add(row);
        }
        
        data = dataList.toArray(new Object[dataList.size()][]);
    }
    
    @Override
    public KTableCellEditor doGetCellEditor(int arg0, int arg1) {
        return null;
    }
    
    @Override
    public KTableCellRenderer doGetCellRenderer(int col, int row) {
        if(col == 0 || row == 0) {
            return headerRenderer;
        } else {
            return cellRenderer;
        }
    }
    
    @Override
    public int doGetColumnCount() {
        return headers.size() + 1;
    }
    
    @Override
    public Object doGetContentAt(int column, int row) {
        if(row == 0 && column == 0) {
            return "";
        } else if(row == 0) {
            return headers.get(column - 1);
        } else if(column == 0) {
            return String.valueOf(row);
        } else {
            return String.valueOf(data[row - 1][column - 1]);
        }
    }
    
    @Override
    public int doGetRowCount() {
        return data.length + 1;
    }
    
    @Override
    public void doSetContentAt(int arg0, int arg1, Object arg2) {
    }
    
    @Override
    public int getInitialColumnWidth(int column) {
        if(column == 0) return 35;
        return 100;
    }
    
    @Override
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
    
    public boolean isColumnResizable(int column) {
        return column > 0;
    }
    
    public boolean isRowResizable(int row) {
        return row > 0;
    }
    
}
