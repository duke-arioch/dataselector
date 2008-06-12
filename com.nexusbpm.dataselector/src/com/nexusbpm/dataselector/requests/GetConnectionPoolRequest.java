package com.nexusbpm.dataselector.requests;

import com.nexusbpm.dataselector.connection.ConnectionPool;
import com.nexusbpm.multipage.bus.BusRequest;

public class GetConnectionPoolRequest implements BusRequest {
    private boolean shouldConnect;
    private ConnectionPool connectionPool;
    
    /**
     * If <code>shouldConnect</code> is <code>false</code> and there is no
     * current connection then no attempt is made to connect and no pool
     * is returned. If <code>true</code> and there is no current connection
     * then attempt to connect and only return <code>null</code> if no
     * connection can be made.
     */
    public GetConnectionPoolRequest(boolean shouldConnect) {
        this.shouldConnect = shouldConnect;
    }
    
    public boolean getShouldConnect() {
        return shouldConnect;
    }
    
    public void setShouldConnect(boolean shouldConnect) {
        this.shouldConnect = shouldConnect;
    }
    
    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }
    
    public void setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }
}
