package com.nexusbpm.dataselector.stats;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.nexusbpm.dataselector.commands.PredictorChangeCommand;
import com.nexusbpm.dataselector.connection.ConnectionPool;
import com.nexusbpm.dataselector.events.ConfigurationChangeEvent;
import com.nexusbpm.dataselector.events.ExceptionEvent;
import com.nexusbpm.dataselector.events.UserMessageEvent;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.config.LSColumn;
import com.nexusbpm.dataselector.model.config.LSConfig;
import com.nexusbpm.dataselector.requests.CancelDownloadRequest;
import com.nexusbpm.dataselector.requests.ConfirmationRequest;
import com.nexusbpm.dataselector.requests.DownloadStatisticsRequest;
import com.nexusbpm.dataselector.requests.ExecuteCommandRequest;
import com.nexusbpm.dataselector.requests.GetConnectionPoolRequest;
import com.nexusbpm.dataselector.requests.PredictorChangeRequest;
import com.nexusbpm.multipage.bus.BusEvent;
import com.nexusbpm.multipage.bus.BusEventListener;
import com.nexusbpm.multipage.bus.BusRequest;
import com.nexusbpm.multipage.bus.BusRequestHandler;
import com.nexusbpm.multipage.bus.EventRequestBus;
import com.nexusbpm.multipage.bus.UnhandledRequestException;

public class StatsDownloadControl implements BusEventListener, BusRequestHandler {
    private NodeQueue queue;
    private ThreadGroup threadGroup;
    private List<StatsThread> threads;
    private ConnectionPool connectionPool;
    
    private EventRequestBus bus;
    
    private String treeName;
    private int nextId = 1;
    private int maxThreads = 6;
    
    private volatile boolean shutdown;
    
    public StatsDownloadControl(String treeName, EventRequestBus bus) {
        queue = new NodeQueue();
        threadGroup = new ThreadGroup(
                Thread.currentThread().getThreadGroup(), "StatsDownloadGroup:" + treeName);
        threads = new ArrayList<StatsThread>();
        this.treeName = treeName;
        this.bus = bus;
        bus.addEventListener(this);
        bus.addRequestHandler(this);
    }
    
    public void handleEvent(BusEvent event) {
        if(event instanceof ConfigurationChangeEvent) {
            ConfigurationChangeEvent cce = (ConfigurationChangeEvent) event;
            if(cce.isPreChange()) {
                // if the event was sent before the change, stop all the threads from downloading
                // TODO react based on the severity of the change, and alert the existing threads as needed
                switch(cce.getSeverity()) {
                    case ConfigurationChangeEvent.SEVERITY_USERNAME_PASSWORD:
                        break;
                    case ConfigurationChangeEvent.SEVERITY_DATABASE_INFO:
                        break;
                    case ConfigurationChangeEvent.SEVERITY_TABLE_INFO_PARTIAL:
                        break;
                    case ConfigurationChangeEvent.SEVERITY_TABLE_INFO_TOTAL:
                        // TODO destroy existing stats here when DB info changes?
                        break;
                }
                queue.clear(); // TODO should this be in or out of the synchronized block?
                /* give all threads the new connection pool. This will need to be updated to
                 * possibly interrupt the threads and tell them to restart downloading based
                 * on the severity of the change.
                 */
                synchronized(threads) {
                    ConnectionPool oldPool = this.connectionPool;
                    connectionPool = null;
                    System.out.println("changing connection pool " + oldPool + " -> " + connectionPool);
                    for(StatsThread thread : threads) {
                        thread.setConnectionPool(connectionPool);
                        thread.cancelWork();
                    }
                    if(oldPool != null && oldPool != connectionPool) {
                        oldPool.close();
                    }
                }
            } else {
                // if the change is finished then we can get the new connection pool
                synchronized(threads) {
                    connectionPool = cce.getConnectionPool();
                    for(StatsThread thread : threads) {
                        thread.setConnectionPool(connectionPool);
                    }
                }
            }
        }
    }
    
    public boolean canHandleRequest(BusRequest request) {
        return request instanceof DownloadStatisticsRequest ||
            request instanceof PredictorChangeRequest ||
            request instanceof CancelDownloadRequest;
    }
    
    public Object handleRequest(BusRequest request) {
        if(request instanceof DownloadStatisticsRequest) {
            // note: we never hand the work directly to a thread.
            // first make sure we're connected, then check how many threads exist
            // and are working to determine whether we should create a new one,
            // then add the node to the work queue
            if(ensureConnected()) {
                List<LSNode> nodes = ((DownloadStatisticsRequest) request).getNodes();
                createStatsThread(nodes.size());
                for(LSNode node : nodes) {
                    queue(node);
                }
            } else {
                if(shutdown) return null;
                bus.handleEvent(new UserMessageEvent(
                        "Error",
                        "Cannot download stats without a connection",
                        UserMessageEvent.STATUS_ERROR));
            }
        } else if(request instanceof PredictorChangeRequest) {
            handlePredictorChangeRequest((PredictorChangeRequest) request);
        } else if(request instanceof CancelDownloadRequest) {
            handleCancelDownloadRequest((CancelDownloadRequest) request);
        }
        return null;
    }
    
    protected void handleCancelDownloadRequest(CancelDownloadRequest request) {
        List<LSNode> nodes = request.getNodes();
        
        queue.removeAll(nodes);
        
        synchronized(threads) {
            for(StatsThread thread : threads) {
                LSNode node = thread.getCurrentNode();
                if(node != null && nodes.contains(node)) {
                    thread.cancelWork();
                }
            }
        }
    }
    
    protected void handlePredictorChangeRequest(PredictorChangeRequest request) {
        LSConfig config = request.getConfig();
        String target = request.getTarget();
        boolean[] predictors = request.getPredictors();
        List<LSColumn> added = new ArrayList<LSColumn>();
        List<LSColumn> removed = new ArrayList<LSColumn>();
        LSColumn targetColumn = null;
        for(LSColumn column : config.getColumns()) {
            if(targetColumn == null && target.equals(column.getName())) {
                targetColumn = column;
            }
            boolean predictor = predictors[column.getOrdinal() - 1] || targetColumn == column;
            if(predictor && !column.isPredictor()) {
                added.add(column);
            } else if(column.isPredictor() && !predictor) {
                removed.add(column);
            }
        }
        
        if(targetColumn == null) {
            Exception e = new Exception("Target column '" + target + "' is not a valid column name!");
            e.fillInStackTrace();
            bus.handleEvent(new ExceptionEvent("Invalid target column", e));
            return;
        }
        
        try {
            PredictorChangeCommand cmd = new PredictorChangeCommand(
                    bus,
                    config,
                    targetColumn,
                    added,
                    removed);
            
            boolean executeCommand = true;
            if(cmd.willPruneTree()) {
                ConfirmationRequest cr = new ConfirmationRequest(
                        "Some of the predictors being used in the tree have been removed. " +
                        "To continue, the tree will have to be pruned accordingly. Continue?");
                bus.handleRequest(cr);
                executeCommand = !cr.isCancelled();
            }
            
            if(executeCommand) {
                bus.handleRequest(new ExecuteCommandRequest(cmd));
            }
        } catch(UnhandledRequestException e) {
            bus.handleEvent(new ExceptionEvent("Bus not configured", e));
        }
    }
    
    protected boolean ensureConnected() {
        if(shutdown) return false;
        testConnection();
        if(connectionPool == null || connectionPool.isClosed()) {
            GetConnectionPoolRequest request = new GetConnectionPoolRequest(true);
            try {
                bus.handleRequest(request);
            } catch(UnhandledRequestException e) {
                bus.handleEvent(new ExceptionEvent("bus not configured", e));
            }
            connectionPool = request.getConnectionPool();
        }
        testConnection();
        return connectionPool != null && !connectionPool.isClosed();
    }
    
    protected void testConnection() {
        if(connectionPool != null) {
            try {
                connectionPool.releaseConnection(connectionPool.getConnection());
            } catch(SQLException e) {
                connectionPool.close();
                connectionPool = null;
            }
        }
    }
    
    /** Creates a stats thread if none are free and the current # of threads is below the limit. */
    protected void createStatsThread(int nodes) {
        if(shutdown) return;
        synchronized(threads) {
            int freeThreads = 0;
            for(StatsThread thread : threads) {
                freeThreads += thread.isWorking() ? 0 : 1;
            }
            if(nodes > freeThreads && threads.size() < maxThreads) {
                int count = nodes - freeThreads;
                if(threads.size() + count >= maxThreads) {
                    count = maxThreads - threads.size() - 1;
                }
                for(int i = 0; i < count; i++) {
                    StatsThread t = new StatsThread(threadGroup, bus, treeName, nextId, queue, this);
                    t.setConnectionPool(connectionPool);
                    t.start();
                    nextId += 1;
                    threads.add(t);
                }
            }
        }
    }
    
    protected boolean removeStatsThread(StatsThread thread) {
        synchronized(threads) {
            if(threads.size() > queue.size()) {
                threads.remove(thread);
                return true;
            } else {
                return false;
            }
        }
    }
    
    protected void queue(LSNode node) {
        if(shutdown) return;
        queue.add(node);
    }
    
    public void shutdown(boolean cancelCurrentWork) {
        shutdown = true;
        synchronized(threads) {
            Iterator<StatsThread> iter = threads.iterator();
            while(iter.hasNext()) {
                StatsThread thread = iter.next();
                thread.shutdown(cancelCurrentWork);
                iter.remove();
            }
        }
    }
}
