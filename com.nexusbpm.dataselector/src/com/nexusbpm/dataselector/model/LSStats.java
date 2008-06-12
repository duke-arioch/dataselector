package com.nexusbpm.dataselector.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class LSStats extends AbstractModelElement {
    public static final String PROPERTY_STATS_ROW_COUNT ="statsRowCount";
    public static final String PROPERTY_ADD_COLUMN_STATS = "addColumnStats";
    public static final String PROPERTY_CLEAR_COLUMN_STATS = "clearColumnStats";
    public static final String PROPERTY_UPDATE_COLUMN_STATS = "updateColumnStats";
    
    private long rowCount = -1;
    private Set<LSColumnStats> columnStats;
    
    public LSStats(LSNode parent) {
        super(parent);
        columnStats = new TreeSet<LSColumnStats>();
    }
    
    public long getRowCount() {
        return rowCount;
    }
    
    public void setRowCount(long rowCount) {
        Long oldRowCount = Long.valueOf(this.rowCount);
        this.rowCount = rowCount;
        firePropertyChange(PROPERTY_STATS_ROW_COUNT, oldRowCount, Long.valueOf(rowCount));
    }
    
    public Set<LSColumnStats> getColumnStats() {
        return Collections.unmodifiableSet(columnStats);
    }
    
    public void addColumnStats(LSColumnStats stats) {
        columnStats.add(stats);
        firePropertyChange(PROPERTY_ADD_COLUMN_STATS, null, stats);
    }
    
    public void clearColumnStats() {
        Set<LSColumnStats> oldColumnStats = new LinkedHashSet<LSColumnStats>(columnStats);
        columnStats.clear();
        firePropertyChange(PROPERTY_CLEAR_COLUMN_STATS, oldColumnStats, null);
    }
    
    public void fireUpdateStats() {
        firePropertyChange(PROPERTY_UPDATE_COLUMN_STATS, null, null);
    }
}
