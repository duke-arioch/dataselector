package com.nexusbpm.dataselector.stats;

import java.util.ArrayList;
import java.util.List;

import com.nexusbpm.dataselector.controller.GraphUpdateQueue;
import com.nexusbpm.dataselector.events.ExceptionEvent;
import com.nexusbpm.dataselector.model.LSColumnStats;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSStats;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.requests.DownloadStatisticsRequest;
import com.nexusbpm.dataselector.requests.GetGraphUpdateQueueRequest;
import com.nexusbpm.dataselector.requests.SetDirtyRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;
import com.nexusbpm.multipage.bus.UnhandledRequestException;

public class SaveStatsRunnable implements Runnable {
    private List<Object[]> columnStatsList;
    private List<Object[]> columnValuesList;
    private Long rowCount;
    
    private LSNode node;
    private EventRequestBus bus;
    
    public SaveStatsRunnable(LSNode node, EventRequestBus bus) {
        this.node = node;
        this.bus = bus;
        columnStatsList = new ArrayList<Object[]>();
        columnValuesList = new ArrayList<Object[]>();
    }
    
    public void setRowCount(long rowCount) {
        this.rowCount = Long.valueOf(rowCount);
    }
    
    public void setRowCount(Long rowCount) {
        this.rowCount = rowCount;
    }
    
    public void addColumnStats(
            int columnOrdinal,
            long distinctCount,
            String min,
            String max,
            String average,
            String standardDeviation,
            String sum) {
        if(distinctCount == 0) {
            min = max = average = standardDeviation = sum = "n/a";
        }
        Object[] stats = new Object[] {
            Integer.valueOf(columnOrdinal),
            Long.valueOf(distinctCount),
            min,
            max,
            average,
            standardDeviation,
            sum
        };
        columnStatsList.add(stats);
    }
    
    public void addColumnValues(int columnOrdinal, Object[] values) {
        Object[] array = new Object[values.length + 1];
        array[0] = Integer.valueOf(columnOrdinal);
        System.arraycopy(values, 0, array, 1, values.length);
        columnValuesList.add(array);
    }
    
    public void run() {
        try {
            LSTree tree = node.getTree();
            GetGraphUpdateQueueRequest request = new GetGraphUpdateQueueRequest();
            bus.handleRequest(request);
            GraphUpdateQueue graphQueue = request.getQueue();
            if(tree == null || graphQueue == null) {
                return;
            }
            synchronized(tree) {
                graphQueue.startNonFlushingOperation();
                try {
                    LSStats stats = node.getStats();
                    if(stats == null) {
                        stats = new LSStats(node);
                        node.setStats(stats);
                    }
                    if(rowCount != null) {
                        stats.setRowCount(rowCount.longValue());
                    }
                    for(Object[] columnStats : columnStatsList) {
                        int columnOrdinal = ((Integer) columnStats[0]).intValue();
                        long distinctCount = ((Long) columnStats[1]).longValue();
                        String min = (String) columnStats[2];
                        String max = (String) columnStats[3];
                        String average = (String) columnStats[4];
                        String standardDeviation = (String) columnStats[5];
                        String sum = (String) columnStats[6];
                        LSColumnStats cstats = getColumnStats(stats, columnOrdinal);
                        cstats.setDistinctCount(distinctCount);
                        cstats.setMin(min);
                        cstats.setMax(max);
                        cstats.setAverage(average);
                        cstats.setStandardDeviation(standardDeviation);
                        cstats.setSum(sum);
                        if(cstats.getValues().size() > 0) {
                            cstats.setValues(new ArrayList<Object>());
                        }
                    }
                    for(Object[] columnValues : columnValuesList) {
                        int columnOrdinal = ((Integer) columnValues[0]).intValue();
                        LSColumnStats cstats = getColumnStats(stats, columnOrdinal);
                        List<Object> valuesList = new ArrayList<Object>();
                        for(int index = 1; index < columnValues.length; index++) {
                            valuesList.add(columnValues[index]);
                        }
                        cstats.setValues(valuesList);
                    }
                    if(rowCount != null || columnStatsList.size() > 0 || columnValuesList.size() > 0) {
                        stats.fireUpdateStats();
                        try {
                            bus.handleRequest(new SetDirtyRequest(true));
                            bus.handleRequest(new DownloadStatisticsRequest(node));
                        } catch(UnhandledRequestException e) {
                            bus.handleEvent(new ExceptionEvent("bus not configured", e));
                        }
                    }
                    List<LSNode> queue = new ArrayList<LSNode>(node.getSubNodes());
                    while(queue.size() > 0) {
                        LSNode n = queue.remove(0);
                        n.firePropertyChange(LSStats.PROPERTY_UPDATE_COLUMN_STATS, null, null);
                        queue.addAll(n.getSubNodes());
                    }
                } finally {
                    graphQueue.endNonFlushingOperation();
                }
            }
        } catch(Exception e) {
            bus.handleEvent(new ExceptionEvent("Error saving stats", e));
        }
    }
    
    protected LSColumnStats getColumnStats(LSStats stats, int columnOrdinal) {
        for(LSColumnStats cstats : stats.getColumnStats()) {
            if(cstats.getColumnOrdinal() == columnOrdinal) {
                return cstats;
            }
        }
        LSColumnStats cstats = new LSColumnStats(stats);
        cstats.setColumnOrdinal(columnOrdinal);
        stats.addColumnStats(cstats);
        return cstats;
    }
}
