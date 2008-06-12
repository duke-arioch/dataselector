package com.nexusbpm.dataselector.connection;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConnectionPoolImpl implements ConnectionPool {
    // TODO add time-based dead connection cleaning?
    private Driver driver;
    private String uri;
    private String username;
    private String password;
    
    private List<Connection> connections;
    private boolean closed;
    
    private int maxExtraConnections = 10;
    
    public ConnectionPoolImpl(Driver driver, String uri, String username, String password) throws SQLException {
        this.driver = driver;
        this.uri = uri;
        this.username = username;
        this.password = password;
        this.closed = false;
        
        connections = new ArrayList<Connection>();
        
        Connection connection = createConnection();
        Statement s = connection.createStatement();
        s.close();
        connections.add(connection);
    }
    
    protected Connection createConnection() throws SQLException {
        if(closed) {
            throw new SQLException("Connection pool is closed!");
        }
        
        Properties properties = new Properties();
        properties.setProperty("user", username);
        properties.setProperty("password", password);
        Connection connection = driver.connect(uri, properties);
        
        if(connection == null) {
            throw new SQLException("Invalid URI for driver!");
        }
        
        return connection;
    }
    
    public synchronized void close() {
        closed = true;
        while(connections.size() > 0) {
            Connection connection = connections.remove(0);
            try {
                connection.close();
            } catch(SQLException e) {
            }
        }
    }
    
    public synchronized boolean isClosed() {
        return closed;
    }
    
    public synchronized Connection getConnection() throws SQLException {
        while(connections.size() > 0) {
            Connection connection = connections.remove(0);
            if(connection.isClosed()) {
                continue;
            }
            try {
//                try {
//                    if(connection.isValid(10)) {
//                        return connection;
//                    }
//                } catch(IncompatibleClassChangeError e) {
//                    // on some drivers the isValid method doesn't work, so try getMetaData
                    if(connection.getMetaData() != null) {
                        return connection;
                    }
//                }
            } catch(SQLException e) {
                e.printStackTrace();
            }
            close(connection);
        }
        
        return createConnection();
    }
    
    public synchronized void releaseConnection(Connection connection) {
        if(closed) {
            close(connection);
        }
        try {
            if(connection != null && !connection.isClosed()) {
                connections.add(connection);
                if(connections.size() > maxExtraConnections) {
                    close(connections.remove(0));
                }
            }
        } catch(SQLException e) {
        }
    }
    
    protected void close(Connection connection) {
        if(connection != null) {
            try {
                connection.close();
            } catch(SQLException e) {
            }
        }
        
    }
}
