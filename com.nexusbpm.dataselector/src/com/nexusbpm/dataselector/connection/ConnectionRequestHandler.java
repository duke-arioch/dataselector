package com.nexusbpm.dataselector.connection;

import com.nexusbpm.dataselector.requests.ConfigurationChangeRequest;
import com.nexusbpm.dataselector.requests.GetConnectionPoolRequest;
import com.nexusbpm.dataselector.requests.TestConnectionRequest;
import com.nexusbpm.multipage.bus.BusRequest;
import com.nexusbpm.multipage.bus.BusRequestHandler;

public class ConnectionRequestHandler implements BusRequestHandler {
    private ConnectionControl connectionControl;
    
    public ConnectionRequestHandler(ConnectionControl connectionControl) {
        this.connectionControl = connectionControl;
    }
    
    public boolean canHandleRequest(BusRequest request) {
        return
            request instanceof GetConnectionPoolRequest ||
            request instanceof TestConnectionRequest ||
            request instanceof ConfigurationChangeRequest;
    }
    
    public Object handleRequest(BusRequest request) {
        if(request instanceof GetConnectionPoolRequest) {
            handleGetConnectionPoolRequest((GetConnectionPoolRequest) request);
        } else if(request instanceof TestConnectionRequest) {
            handleTestConnectionRequest((TestConnectionRequest) request);
        } else if(request instanceof ConfigurationChangeRequest) {
            handleConfigurationChangeRequest((ConfigurationChangeRequest) request);
        }
        return null;
    }
    
    protected void handleGetConnectionPoolRequest(GetConnectionPoolRequest request) {
        request.setConnectionPool(connectionControl.getCurrentConnectionPool(request.getShouldConnect()));
    }
    
    protected void handleTestConnectionRequest(TestConnectionRequest request) {
        if(request.useCurrentConfig()) {
            connectionControl.testConnection();
        } else {
            connectionControl.testConnection(
                    request.getSQLDriver(),
                    request.getUri(),
                    request.getUsername(),
                    request.getPassword());
        }
    }
    
    protected void handleConfigurationChangeRequest(ConfigurationChangeRequest request) {
        connectionControl.changeConfiguration(
                request.getSQLDriver(),
                request.getUri(),
                request.getUsername(),
                request.getPassword(),
                request.isSavePassword(),
                request.getQuery());
    }
}
