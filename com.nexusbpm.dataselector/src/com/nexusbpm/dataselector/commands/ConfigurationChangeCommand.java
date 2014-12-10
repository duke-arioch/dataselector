package com.nexusbpm.dataselector.commands;

import java.util.List;

import org.eclipse.core.internal.runtime.Log;

import com.nexusbpm.database.driver.SQLDriver;
import com.nexusbpm.dataselector.connection.ConnectionPool;
import com.nexusbpm.dataselector.controller.GraphUpdateQueue;
import com.nexusbpm.dataselector.events.ConfigurationChangeEvent;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.model.config.LSColumn;
import com.nexusbpm.dataselector.model.config.LSConfig;
import com.nexusbpm.dataselector.model.config.LSConnection;
import com.nexusbpm.dataselector.model.config.LSDriver;
import com.nexusbpm.dataselector.requests.SetDirtyRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class ConfigurationChangeCommand extends AbstractGraphUpdateCommand {
    private SQLDriver sqlDriver;
    private String uri;
    private String username;
    private String password;
    private boolean savePassword;
    private String query;
    private List<LSColumn> columns;
    private String targetColumn;
    
    private ConnectionPool connectionPool;
    
    private LSConfig config;
    private LSTree tree;
    
    private ConfigurationChangeEvent event;
    
    public ConfigurationChangeCommand(
            EventRequestBus eventBus,
            LSConfig config,
            SQLDriver sqlDriver,
            String uri,
            String username,
            String password,
            boolean savePassword,
            String query,
            List<LSColumn> columns,
            ConnectionPool connectionPool) {
        super("Change Configuration", eventBus);
        this.config = config;
        this.sqlDriver = sqlDriver;
        this.uri = uri;
        this.username = username;
        this.password = password;
        this.savePassword = savePassword;
        this.query = query;
        this.columns = columns;
        this.connectionPool = connectionPool;
        this.targetColumn = columns.get(0).getName(); // TODO choose target column more intelligently
        this.tree = config.getTree();
    }
    
    @Override
    public boolean canExecute() {
        return tree != null;
    }
    
    protected ConfigurationChangeEvent getChangeEvent() {
        if(event == null) {
            // TODO calculate the difference (called before performExecute)
            // we will also need to remember exactly which columns have changed in order to know
            // which columns have old stats
            event = new ConfigurationChangeEvent(
                    ConfigurationChangeEvent.SEVERITY_TABLE_INFO_TOTAL,
                    connectionPool);
        }
        return event;
    }
    
    @Override
    public void execute() {
        GraphUpdateQueue queue = getGraphUpdateQueue();
        synchronized(tree) {
            queue.startNonFlushingOperation();
            try {
                ConfigurationChangeEvent event = getChangeEvent();
                
                // send an event first to make sure stats downloading is stopped, then make the changes
                ConfigurationChangeEvent preEvent = new ConfigurationChangeEvent(event.getSeverity(), true);
                sendEvent(preEvent);
                
                config.setQuery(query);
                config.setTargetColumn(targetColumn);
                
                LSConnection conn = config.getConnection();
                conn.setURI(uri);
                conn.setUsername(username);
                conn.setPassword(password);
                conn.setSavePassword(savePassword);
                
                LSDriver driver = config.getDriver();
                driver.setDriverClass(sqlDriver.getDriverClassName());
                driver.setName(sqlDriver.getName());
                
                config.clearColumns();
                
                for(LSColumn column : columns) {
                    column.setParent(config);
                    config.addColumn(column);
                }
                
                if((getChangeEvent().getSeverity() == ConfigurationChangeEvent.SEVERITY_TABLE_INFO_PARTIAL ||
                        getChangeEvent().getSeverity() == ConfigurationChangeEvent.SEVERITY_TABLE_INFO_TOTAL) &&
                        config.getTree().getRoot() != null) {
                    config.getTree().getRoot().clearSubNodes();
                    config.getTree().getRoot().setStats(null);
                }
                
                sendRequest(new SetDirtyRequest(true));
                sendEvent(event);
            } finally {
                queue.endNonFlushingOperation();
            }
        }
    }
}
