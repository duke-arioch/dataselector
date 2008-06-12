package com.nexusbpm.dataselector.stats;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nexusbpm.database.info.DBInfo;
import com.nexusbpm.database.info.DBInfoFactory;
import com.nexusbpm.dataselector.connection.ConnectionPool;
import com.nexusbpm.dataselector.database.SQLGenerator;
import com.nexusbpm.dataselector.database.SQLGeneratorFactory;
import com.nexusbpm.dataselector.events.ExceptionEvent;
import com.nexusbpm.dataselector.model.LSColumnStats;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.model.config.LSColumn;
import com.nexusbpm.dataselector.requests.RunInUIThreadRequest;
import com.nexusbpm.dataselector.util.ObjectConverter;
import com.nexusbpm.multipage.bus.BusRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;
import com.nexusbpm.multipage.bus.UnhandledRequestException;

public class StatsThread extends Thread {
    private NodeQueue queue;
    private EventRequestBus bus;
    private ConnectionPool connectionPool;
    private StatsDownloadControl control;
    
    private LSNode currentNode;
    private Statement statement;
    private boolean cancelled;
    private boolean shutdown;
    
    private ObjectConverter converter;
    
    public StatsThread(
            ThreadGroup group,
            EventRequestBus bus,
            String treeName,
            int id,
            NodeQueue queue,
            StatsDownloadControl control) {
        super(group, "StatsDownloadThread(" + treeName + ":" + id + ")");
        setDaemon(true); // stats downloading shouldn't keep eclipse from closing
        this.bus = bus;
        this.queue = queue;
        this.control = control;
        this.converter = new ObjectConverter();
    }
    
    @Override
    public void run() {
        long lastWorked = System.currentTimeMillis();
        while(!shutdown) {
            LSNode node = queue.next();
            
            if(lock(node)) {
                try {
                    refreshNode(node);
                    downloadStats();
                    downloadValues();
                    lastWorked = System.currentTimeMillis();
                } catch(Exception e) {
                    if(!cancelled) {
                        bus.handleEvent(new ExceptionEvent("Exception downloading stats", e));
                    }
                } finally {
                    unlock();
                    refreshNode(node);
                }
            } else {
                if(node != null) {
                    queue.add(node);
                }
                sleep();
            }
            
            if(System.currentTimeMillis() > (lastWorked + 15000)) {
                // if this thread hasn't done any work in more than 15 seconds
                if(control.removeStatsThread(this)) {
                    shutdown(false);
                } else {
                    lastWorked = System.currentTimeMillis();
                }
            }
        }
    }
    
    protected void refreshNode(final LSNode node) {
        if(node == null) return;
        try {
            bus.handleRequest(new RunInUIThreadRequest(new Runnable() {
                public void run() {
                    node.firePropertyChange("refresh", null, null);
                }
            }));
        } catch(UnhandledRequestException e) {
            bus.handleEvent(new ExceptionEvent("bus not configured", e));
        }
    }
    
    public void downloadStats() {
        if(cancelled) return;
        boolean needsRowCount = false;
        List<LSColumn> neededColumns = new ArrayList<LSColumn>();
        
        LSTree tree = currentNode.getTree();
        if(tree == null) {
            // can't download the stats if we can't figure out which columns are predictors
            return;
        }
        
        SQLGenerator generator;
        
        // synchronize on the tree while we figure out which columns need stats
        synchronized(tree) {
            if(currentNode.getStats() == null || currentNode.getStats().getRowCount() < 0) {
                needsRowCount = true;
            }
            
            for(LSColumn column : tree.getConfig().getColumns()) {
                if(column.isPredictor()) {
                    LSColumnStats stats = getColumnStats(column);
                    if(stats == null) {
                        neededColumns.add(column);
                    }
                }
            }
            
            String driverName = tree.getConfig().getDriver().getName();
            generator = SQLGeneratorFactory.getInstance().getGenerator(driverName);
            
            System.out.println("Downloading row count for node " + currentNode + " (" + needsRowCount + ")");
            System.out.println("Downloading stats for " + neededColumns.size() + " rows");
        }
        
        while(needsRowCount || neededColumns.size() > 0) {
            if(cancelled) return;
            String select = "";
            String query = null;
            List<LSColumn> currentColumns = new ArrayList<LSColumn>();
            Map<LSColumn, Integer> columnOrdinals = new HashMap<LSColumn, Integer>();
            
            if(needsRowCount) {
                select = "count(*)";
            }
            
            // synchronize on the tree again to generate the SQL query
            synchronized(tree) {
                if(needsRowCount) {
                    for(LSColumn column : neededColumns) {
                        if(column.getName().equals(tree.getConfig().getTargetColumn())) {
                            currentColumns.add(column);
                            columnOrdinals.put(column, Integer.valueOf(column.getOrdinal()));
                            select += ", " + generator.getStatsSelectClause(column);
                            break;
                        }
                    }
                } else {
                    while(neededColumns.size() > 0 && currentColumns.size() < 40) {
                        LSColumn column = neededColumns.remove(0);
                        currentColumns.add(column);
                        columnOrdinals.put(column, Integer.valueOf(column.getOrdinal()));
                        
                        if(select.length() > 0) {
                            select += ", ";
                        }
                        select += generator.getStatsSelectClause(column);
                    }
                }
                
                query =
                    "select " + select + " " +
                    generator.getFromClause(currentNode) + " " +
                    generator.getWhereClause(currentNode);
            }
            
            System.out.println("query to execute:\n" + query + "\r\n");
            long starttime = System.currentTimeMillis();
            
            Connection connection = null;
            try {
                synchronized(this) {
                    if(cancelled) return;
                    connection = connectionPool.getConnection();
                    statement = connection.createStatement();
                }
                
                ResultSet rs = statement.executeQuery(query);
                rs.next();
                
                SaveStatsRunnable runnable = new SaveStatsRunnable(currentNode, bus);
                
                int offset = 0;
                
                if(needsRowCount) {
                    needsRowCount = false;
                    runnable.setRowCount(rs.getLong(1));
                    offset = 1;
                }
                
                for(int index = 0; index < currentColumns.size(); index++) {
                    LSColumn column = currentColumns.get(index);
                    int ordinal = columnOrdinals.get(column).intValue();
                    
                    runnable.addColumnStats(
                            ordinal,
                            rs.getLong(offset + index * 6 + 1),
                            converter.format(rs.getObject(offset + index * 6 + 2)),
                            converter.format(rs.getObject(offset + index * 6 + 3)),
                            converter.format(rs.getObject(offset + index * 6 + 4)),
                            converter.format(rs.getObject(offset + index * 6 + 5)),
                            converter.format(rs.getObject(offset + index * 6 + 6)));
                }
                long endtime = System.currentTimeMillis();
                System.out.println("stats downloaded in " + (endtime - starttime) + "ms");
                
                bus.handleRequest(new RunInUIThreadRequest(runnable));
                System.out.println("stats saved for node " + currentNode);
            } catch(UnhandledRequestException e) {
                sendRequest(new RunInUIThreadRequest(new ExceptionEvent("bus not configured", e)));
            } catch(SQLException e) {
                if(cancelled) {
                    System.out.println("cancelled query: " + query);
                } else {
                    sendRequest(new RunInUIThreadRequest(new ExceptionEvent("error downloading stats", e)));
                }
            } finally {
                needsRowCount = false;
                close(connection, statement);
                synchronized(this) {
                    statement = null;
                }
            }
        }
    }
    
    protected void downloadValues() {
        if(cancelled) return;
        LSTree tree = currentNode.getTree();
        if(tree == null) {
            return;
        }
        List<LSColumn> neededColumns = new ArrayList<LSColumn>();
        SQLGenerator generator;
        
        // synchronize on the tree while we figure out which columns to get values for
        synchronized(tree) {
            if(currentNode == null || currentNode.getStats() == null) {
                return;
            }
            if(!currentNode.getTree().getConfig().isAutoDownloadCategoricalSplits()) {
                return;
            }
            
            DBInfo info = DBInfoFactory.getInstance().getDBInfo(tree.getConfig().getDriver().getName());
            
            for(LSColumn column : tree.getConfig().getColumns()) {
                if(column.isPredictor() && !info.isOther(column)) {
                    LSColumnStats stats = getColumnStats(column);
                    if(stats != null && stats.getDistinctCount() > 0 && stats.getDistinctCount() < 9) {
                        LSColumnStats cstats = getColumnStats(column);
                        if(cstats.getValues().size() == 0) {
                            neededColumns.add(column);
                        }
                    }
                }
            }
            
            String driverName = tree.getConfig().getDriver().getName();
            generator = SQLGeneratorFactory.getInstance().getGenerator(driverName);
        }
        
        System.out.println("downloading values for " + neededColumns.size() + " columns");
        
        if(neededColumns.size() > 0) {
            Connection connection = null;
            statement = null;
            String query = "";
            try {
                synchronized(this) {
                    if(cancelled) return;
                    connection = connectionPool.getConnection();
                    statement = connection.createStatement();
                }
                
                while(neededColumns.size() > 0) {
                    LSColumn column = neededColumns.remove(0);
                    LSColumnStats stats;
                    int columnOrdinal;
                    String columnName;
                    String columnType;
                    
                    synchronized(tree) {
                        stats = getColumnStats(column);
                        columnOrdinal = stats.getColumnOrdinal();
                        columnName = column.getName();
                        columnType = column.getJavaTypeName();
                        
                        query = "select distinct " + column.getName() + " " +
                            generator.getFromClause(currentNode) + " " +
                            generator.getWhereClause(currentNode);
                    }
                    
                    ResultSet rs = statement.executeQuery(query);
                    
                    List<Object> values = ResultSetTranslator.getValues(columnType, rs);
                    
                    SaveStatsRunnable runnable = new SaveStatsRunnable(currentNode, bus);
                    
                    runnable.addColumnValues(columnOrdinal, values.toArray());
                    
                    bus.handleRequest(new RunInUIThreadRequest(runnable));
                    System.out.println("values saved for node " + currentNode +
                            " for column " + columnName);
                }
            } catch(SQLException e) {
                if(cancelled) {
                    System.out.println("cancelled query: " + query);
                } else {
                    sendRequest(new RunInUIThreadRequest(new ExceptionEvent("error downloading values", e)));
                }
            } catch(UnhandledRequestException e) {
                sendRequest(new RunInUIThreadRequest(new ExceptionEvent("bus not configured", e)));
            } finally {
                close(connection, statement);
                synchronized(this) {
                    statement = null;
                }
            }
        }
    }
    
    protected void sendRequest(BusRequest request) {
        try {
            bus.handleRequest(request);
        } catch(UnhandledRequestException e) {
            e.printStackTrace(); // can't send an ExceptionEvent here because we're on the wrong thread
        }
    }
    
    protected void close(Connection connection, Statement statement) {
        if(statement != null) {
            try {
                statement.close();
            } catch(Exception e) {
                // ignore
            }
        }
        connectionPool.releaseConnection(connection);
    }
    
    protected LSColumnStats getColumnStats(LSColumn column) {
        if(currentNode.getStats() != null) {
            for(LSColumnStats stats : currentNode.getStats().getColumnStats()) {
                if(stats.getColumnOrdinal() == column.getOrdinal()) {
                    return stats;
                }
            }
        }
        return null;
    }
    
    protected void sleep() {
        Thread.yield();
        try {
            Thread.sleep(300);
        } catch(InterruptedException e) {
            // ignore
        }
    }
    
    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }
    
    public void setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }
    
    protected LSNode getCurrentNode() {
        return currentNode;
    }
    
    protected synchronized boolean lock(LSNode node) {
        if(node != null && node.lock(this)) {
            currentNode = node;
            return true;
        }
        return false;
    }
    
    protected synchronized void unlock() {
        if(currentNode != null) {
            currentNode.unlock(this);
            currentNode = null;
            cancelled = false;
        }
    }
    
    public synchronized boolean isWorking() {
        return !shutdown && currentNode != null;
    }
    
    public synchronized void cancelWork() {
        Statement statement = this.statement;
        if(statement != null || currentNode != null) {
            cancelled = true;
        }
        if(statement != null) {
            try {
                statement.cancel();
            } catch(Exception e) {
                // ignore
            }
        }
    }
    
    public synchronized boolean isShutdown() {
        return shutdown;
    }
    
    public synchronized void shutdown(boolean cancelCurrentWork) {
        shutdown = true;
        if(cancelCurrentWork) {
            cancelWork();
        }
    }
}
