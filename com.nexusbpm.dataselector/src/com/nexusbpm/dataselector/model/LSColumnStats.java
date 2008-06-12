package com.nexusbpm.dataselector.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class LSColumnStats extends AbstractModelElement implements Comparable<LSColumnStats> {
    public static final String PROPERTY_COLUMN_STATS_ORDINAL = "columnStatsOrdinal";
    public static final String PROPERTY_COLUMN_STATS_DISTINCT_COUNT = "columnStatsDistinctCount";
    public static final String PROPERTY_COLUMN_STATS_MIN = "columnStatsMin";
    public static final String PROPERTY_COLUMN_STATS_MAX = "columnStatsMax";
    public static final String PROPERTY_COLUMN_STATS_AVERAGE = "columnStatsAverage";
    public static final String PROPERTY_COLUMN_STATS_STD_DEV = "columnStatsStandardDeviation";
    public static final String PROPERTY_COLUMN_STATS_SUM = "columnStatsSum";
    public static final String PROPERTY_COLUMN_STATS_VALUES = "columnStatsValues";
    
    private int columnOrdinal;
    private long distinctCount = -1;
    private String min;
    private String max;
    private String average;
    private String standardDeviation;
    private String sum;
    private List<Object> values;
    
    public LSColumnStats(LSStats parent) {
        super(parent);
        values = new ArrayList<Object>();
    }

    public int getColumnOrdinal() {
        return columnOrdinal;
    }

    public void setColumnOrdinal(int columnOrdinal) {
        Integer oldColumnOrdinal = Integer.valueOf(this.columnOrdinal);
        this.columnOrdinal = columnOrdinal;
        firePropertyChange(PROPERTY_COLUMN_STATS_ORDINAL, oldColumnOrdinal, Integer.valueOf(columnOrdinal));
    }

    public long getDistinctCount() {
        return distinctCount;
    }

    public void setDistinctCount(long distinctCount) {
        Long oldDistinctCount = Long.valueOf(this.distinctCount);
        this.distinctCount = distinctCount;
        firePropertyChange(PROPERTY_COLUMN_STATS_DISTINCT_COUNT, oldDistinctCount, Long.valueOf(distinctCount));
    }
    
    public String getMin() {
        return min;
    }
    
    public void setMin(String min) {
        String oldMin = this.min;
        this.min = min;
        firePropertyChange(PROPERTY_COLUMN_STATS_MIN, oldMin, min);
    }
    
    public String getMax() {
        return max;
    }
    
    public void setMax(String max) {
        String oldMax = this.max;
        this.max = max;
        firePropertyChange(PROPERTY_COLUMN_STATS_MAX, oldMax, max);
    }
    
    public String getAverage() {
        return average;
    }
    
    public void setAverage(String average) {
        String oldAverage = this.average;
        this.average = average;
        firePropertyChange(PROPERTY_COLUMN_STATS_AVERAGE, oldAverage, average);
    }
    
    public String getStandardDeviation() {
        return standardDeviation;
    }
    
    public void setStandardDeviation(String standardDeviation) {
        String oldStdDev = this.standardDeviation;
        this.standardDeviation = standardDeviation;
        firePropertyChange(PROPERTY_COLUMN_STATS_STD_DEV, oldStdDev, standardDeviation);
    }
    
    public String getSum() {
        return sum;
    }
    
    public void setSum(String sum) {
        String oldSum = this.sum;
        this.sum = sum;
        firePropertyChange(PROPERTY_COLUMN_STATS_SUM, oldSum, sum);
    }
    
    public List<Object> getValues() {
        return Collections.unmodifiableList(values);
    }
    
    public void setValues(List<Object> values) {
        List<Object> oldValues = new ArrayList<Object>(this.values);
        this.values.clear();
        this.values.addAll(values);
        firePropertyChange(PROPERTY_COLUMN_STATS_VALUES, oldValues, values);
    }
    
    public int compareTo(LSColumnStats o) {
        if(columnOrdinal == o.columnOrdinal) throw new IllegalStateException("Columns cannot have equal ordinals!");
        return columnOrdinal - o.columnOrdinal;
    }
}
