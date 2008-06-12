package com.nexusbpm.dataselector.events;

import com.nexusbpm.dataselector.connection.ConnectionPool;
import com.nexusbpm.multipage.bus.BusEvent;

public class ConfigurationChangeEvent implements BusEvent {
    /**
     * Username and password changes only affect the UI and future connections.
     */
    public static final int SEVERITY_USERNAME_PASSWORD = 1;
    /**
     * Database info changes include the database type, SQL query, database URI,
     * username, etc. These changes require all connections to be reset.
     * Statistics may need to be redownloaded, but the tree does not need to change.
     * No table columns have changed.
     */
    public static final int SEVERITY_DATABASE_INFO = 2;
    /**
     * A partial table info change happens when the columns of the database have
     * changed, but the previously selected predictor columns and target column
     * still exist and still have the same type. Statistics saved for those columns
     * can be kept, but UI tables displaying all columns will need to be updated.
     */
    public static final int SEVERITY_TABLE_INFO_PARTIAL = 3;
    /**
     * A total table info change happens if any of the predictor or target columns
     * have been changed. The tree will need to be reset, all statistics discarded,
     * and all connections reset.
     */
    public static final int SEVERITY_TABLE_INFO_TOTAL = 4;
    
    /** Indicates whether this event is being sent before or after the change. */
    private boolean preChange;
    private ConnectionPool connectionPool;
    private int severity;
    
    // TODO see ConnectionConfiguration line 157 (send out this event after saving config changes)
    // TODO will also need to actually check how bad the change was.
    
    public ConfigurationChangeEvent() {
    }
    
    public ConfigurationChangeEvent(int severity, boolean preChange) {
        this.severity = severity;
        this.preChange = preChange;
    }
    
    public ConfigurationChangeEvent(int severity, ConnectionPool connectionPool) {
        this.severity = severity;
        this.connectionPool = connectionPool;
    }
    
    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }
    
    public void setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }
    
    public boolean isPreChange() {
        return preChange;
    }
    
    public void setPreChange(boolean preChange) {
        this.preChange = preChange;
    }
    
    public int getSeverity() {
        return severity;
    }
    
    public void setSeverity(int severity) {
        this.severity = severity;
    }
}
