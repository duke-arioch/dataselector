package com.nexusbpm.dataselector.connection;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.nexusbpm.database.driver.SQLDriver;
import com.nexusbpm.dataselector.commands.ConfigurationChangeCommand;
import com.nexusbpm.dataselector.events.ExceptionEvent;
import com.nexusbpm.dataselector.events.UserMessageEvent;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.model.config.LSColumn;
import com.nexusbpm.dataselector.model.config.LSConfig;
import com.nexusbpm.dataselector.model.config.LSConnection;
import com.nexusbpm.dataselector.model.config.LSDriver;
import com.nexusbpm.dataselector.requests.ExecuteCommandRequest;
import com.nexusbpm.dataselector.requests.LoginRequest;
import com.nexusbpm.dataselector.requests.RetryRequest;
import com.nexusbpm.dataselector.requests.RunWithProgressRequest;
import com.nexusbpm.dataselector.requests.SetDirtyRequest;
import com.nexusbpm.multipage.bus.BusEvent;
import com.nexusbpm.multipage.bus.BusRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;
import com.nexusbpm.multipage.bus.UnhandledRequestException;

public class ConnectionControl {
    private ConnectionFactory connectionFactory;
    private EventRequestBus bus;
    private ConnectionPool currentConnectionPool;
    
    private LSTree tree;
    
    public ConnectionControl(EventRequestBus bus, LSTree tree) {
        this.bus = bus;
        this.tree = tree;
        bus.addRequestHandler(new ConnectionRequestHandler(this));
    }
    
    protected ConnectionFactory getConnectionFactory() {
        if(connectionFactory == null) {
            connectionFactory = new ConnectionFactory();
        }
        return connectionFactory;
    }
    
    protected void sendEvent(BusEvent event) {
        bus.handleEvent(event);
    }
    
    protected void sendRequest(BusRequest request) {
        try {
            bus.handleRequest(request);
        } catch(UnhandledRequestException e) {
            bus.handleEvent(new ExceptionEvent("bus not configured", e));
        }
    }
    
    protected LSConfig getLSConfig() {
        return tree.getConfig();
    }
    
    protected LSConnection getLSConnection() {
        return tree.getConfig().getConnection();
    }
    
    protected LSDriver getLSDriver() {
        return tree.getConfig().getDriver();
    }
    
    protected List<LSColumn> getColumnMetaData(ConnectionPool pool, String query, int maxRetryCount) {
        int retries = 0;
        
        while(retries < maxRetryCount) {
            try {
                return getColumnMetaData(pool, query);
            } catch(OperationCancelledException e) {
                return null;
            } catch(SQLException e) {
                sendEvent(new ExceptionEvent("Error while retrieving column info", e));
                boolean retry = retries == 0;
                RetryRequest request = new RetryRequest("Could not retrieve column meta data. Retry?");
                sendRequest(request);
                retry = !request.isCancelled();
                if(!retry) {
                    return null;
                }
            }
            
            retries += 1;
        }
        SQLException e = new SQLException("Retry count exceeded.");
        e.fillInStackTrace();
        sendEvent(new ExceptionEvent("Could not retrieve column meta data", e));
        return null;
    }
    
    protected List<LSColumn> getColumnMetaData(ConnectionPool pool, String query) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = pool.getConnection();
            statement = connection.createStatement();
            statement.setMaxRows(1);
            statement.execute(query);
                        
            ResultSetMetaData metadata = statement.getResultSet().getMetaData();
            
            List<LSColumn> columns = new ArrayList<LSColumn>();
            
            for(int index = 1; index <= metadata.getColumnCount(); index++) {
                LSColumn c = new LSColumn(null);
                c.setOrdinal(index);
                c.setName(metadata.getColumnName(index));
//                c.setTableName(metadata.getTableName(index));
                c.setSQLType(metadata.getColumnType(index));
                c.setTypeName(metadata.getColumnTypeName(index));
                if(c.getSQLType() == Types.DATE) {
                    c.setJavaTypeName(java.sql.Date.class.getName());
                } else if(c.getSQLType() == Types.TIME) {
                    c.setJavaTypeName(java.sql.Time.class.getName());
                } else if(c.getSQLType() == Types.TIMESTAMP) {
                    c.setJavaTypeName(java.sql.Timestamp.class.getName());
                } else {
                    c.setJavaTypeName(metadata.getColumnClassName(index));
                }
                columns.add(c);
            }
            
            if(columns.size() == 0) {
                throw new SQLException("Query returned no columns!");
            }
            
            return columns;
        } finally {
            close(statement);
            if(connection != null) {
                pool.releaseConnection(connection);
            }
        }
    }
    
    protected synchronized ConnectionPool getCurrentConnectionPool(boolean shouldConnect) {
        if(currentConnectionPool != null && !currentConnectionPool.isClosed()) {
            try {
                currentConnectionPool.releaseConnection(currentConnectionPool.getConnection());
                return currentConnectionPool;
            } catch(SQLException e) {
                currentConnectionPool.close();
            }
        }
        currentConnectionPool = null;
        if(shouldConnect) {
            LSConfig config = tree.getConfig();
            LSConnection connection = config.getConnection();
            SQLDriver sqlDriver = SQLDriver.getDriverInstanceByName(config.getDriver().getName());
            String uri = connection.getURI();
            String username = connection.getUsername();
            String password = connection.getPassword();
            boolean savePassword = connection.isSavePassword();
            
            Object[] con = null;
            try {
                con = getConnectionPool(sqlDriver, uri, username, password, savePassword);
            } catch(OperationCancelledException e) {
                return null;
            } catch(SQLException e) {
                sendEvent(new ExceptionEvent("Error while connecting", e));
                return null;
            }
            currentConnectionPool = (ConnectionPool) con[0];
            if(con.length > 1) {
                connection.setUsername((String) con[1]);
                connection.setPassword((String) con[2]);
                connection.setSavePassword(((Boolean) con[3]).booleanValue());
                sendRequest(new SetDirtyRequest(true));
            }
        }
        return currentConnectionPool;
    }
    
    /**
     * Tests the current configuration's connection settings. Uses the event/request
     * bus to report errors and request a username/password if needed. If the
     * user enters a new username and/or password and a connection is successfully
     * made using the new values, then the new values are set in the configuration.
     * <p>
     * This method is synchronized (compare to {@link #testConnection(SQLDriver, String, String, String)}
     * which isn't) because it accesses and possibly modifies the current configuration.
     * @return <code>true</code> if the connection is successfully established.
     */
    protected synchronized boolean testConnection() {
        // TODO if already connected, what should we do? If not, should we set this as the connection?
        LSDriver driver = getLSDriver();
        LSConnection connection = getLSConnection();
        SQLDriver sqlDriver = SQLDriver.getDriverInstanceByName(driver.getName());
        
        try {
            Object[] values = getConnectionPool(
                    sqlDriver,
                    connection.getURI(),
                    connection.getUsername(),
                    connection.getPassword(),
                    connection.isSavePassword());
            
            ((ConnectionPool) values[0]).close();
            
            if(values.length > 0) {
                connection.setUsername((String) values[1]);
                connection.setPassword((String) values[2]);
                connection.setSavePassword(((Boolean) values[3]).booleanValue());
            }
            
            sendEvent(new UserMessageEvent("Success", "The connection was successful"));
            return true;
        } catch(OperationCancelledException e) {
            return false;
        } catch(SQLException e) {
            sendEvent(new ExceptionEvent("Could not connect", e));
            return false;
        }
    }
    
    /** Not synchronized because it does not access or change the current configuration. */
    protected boolean testConnection(
            SQLDriver sqlDriver,
            String uri,
            String username,
            String password) {
        try {
            ConnectionPool pool = getConnectionPool(sqlDriver, uri, username, password);
            pool.close();
            sendEvent(new UserMessageEvent("Success", "The connection was successful"));
            return true;
        } catch(OperationCancelledException e) {
            return false;
        } catch(SQLException e) {
            sendEvent(new ExceptionEvent("Could not connect", e));
            return false;
        }
    }
    
    protected synchronized boolean changeConfiguration(
            SQLDriver sqlDriver,
            String uri,
            String username,
            String password,
            boolean savePassword,
            String query) {
        Object[] con = null;
        try {
            con = getConnectionPool(sqlDriver, uri, username, password, savePassword);
        } catch(OperationCancelledException e) {
            return false;
        } catch(SQLException e) {
            sendEvent(new ExceptionEvent("Error while connecting", e));
            return false;
        }
        ConnectionPool connection = (ConnectionPool) con[0];
        if(con.length > 1) {
            username = (String) con[1];
            password = (String) con[2];
            savePassword = ((Boolean) con[3]).booleanValue();
        }
        
        System.out.println("getting column meta data");
        
        List<LSColumn> columns = getColumnMetaData(connection, query, 10);
        if(columns == null) {
            return false;
        }
        
        System.out.println("done getting column meta data");
        
        currentConnectionPool = connection;
        
        sendRequest(new ExecuteCommandRequest(new ConfigurationChangeCommand(
                bus,
                getLSConfig(),
                sqlDriver,
                uri,
                username,
                password,
                savePassword,
                query,
                columns,
                connection)));
        
        return true;
    }
    
    /**
     * Attempts to get a connection based on the specified parameters.
     * Throws a SQLException if any error occurs.
     */
    protected ConnectionPool getConnectionPool(
            SQLDriver sqlDriver,
            String uri,
            String username,
            String password) throws SQLException {
        if(sqlDriver == null) {
            throw new SQLException("No driver specified!");
        } else if(uri == null || uri.length() == 0) {
            throw new SQLException("No connection URI was specified!");
        } else if(username == null || username.length() == 0 ||
                password == null || password.length() == 0) {
            throw new SQLException("Username and password must be provided!");
        }
        return getConnectionFactory().getConnectionPool(sqlDriver, uri, username, password);
    }
    
    /**
     * Attempts to connect using the given parameters and will use the event/request
     * bus to report status and request a new username/password as needed. Returns
     * a valid connection pool on success, throws an OperationCancelledException if
     * the user cancels the connection, or will throw a SQLException if the
     * connection cannot be established.
     * 
     * If the connection is made successfully then the first object returned is the
     * connection pool. If the user had to enter a new username and/or password, then
     * the second and third objects returned are the username and password. The array
     * will only contain the Connection unless the user had to enter a new username
     * and/or password. If the user entered a new username and/or password then the
     * fourth element of the array will be a Boolean indicating whether the password
     * should be saved.
     */
    protected Object[] getConnectionPool(
            SQLDriver sqlDriver,
            String uri,
            String username,
            String password,
            boolean savePassword) throws SQLException {
        String newUsername = username;
        String newPassword = password;
        boolean newSavePassword = savePassword;
        ConnectionPool connectionPool = null;
        int retries = 0;
        while(connectionPool == null && retries < 20) {
            retries += 1;
            GetConnectionRunnable runnable =
                new GetConnectionRunnable(sqlDriver, uri, newUsername, newPassword);
            
            sendRequest(new RunWithProgressRequest(
                    runnable, "Connecting", "Connecting to " + uri));
            
            connectionPool = runnable.getConnectionPool();
            if(runnable.getException() != null) {
                SQLException e = runnable.getException();
                sendEvent(new ExceptionEvent("Error while connecting", e));
                LoginRequest request = new LoginRequest(newUsername, newPassword, newSavePassword);
                sendRequest(request);
                if(!request.isCancelled()) {
                    newUsername = request.getUsername();
                    newPassword = request.getPassword();
                    newSavePassword = request.isSavePassword();
                } else {
                    throw OperationCancelledException.create(e);
                }
            }
        }
        if(connectionPool == null) {
            if(retries >= 100) {
                throw new SQLException("Maximum retry count exceeded!");
            } else {
                throw new SQLException("Connection could not be established");
            }
        }
        if(newUsername.equals(username) &&
                newPassword.equals(password) &&
                newSavePassword == savePassword) {
            return new Object[] {connectionPool};
        } else {
            return new Object[] {connectionPool, newUsername, newPassword, Boolean.valueOf(newSavePassword)};
        }
    }
    
    private class GetConnectionRunnable implements Runnable {
        private ConnectionPool connectionPool;
        private SQLException exception;
        
        private SQLDriver sqlDriver;
        private String uri;
        private String username;
        private String password;
        
        public GetConnectionRunnable(
                SQLDriver sqlDriver,
                String uri,
                String username,
                String password) {
            this.sqlDriver = sqlDriver;
            this.uri = uri;
            this.username = username;
            this.password = password;
        }
        
        public void run() {
            try {
                this.connectionPool = ConnectionControl.this.getConnectionPool(
                        sqlDriver, uri, username, password);
            } catch(SQLException e) {
                exception = e;
            }
        }
        
        public ConnectionPool getConnectionPool() {
            return connectionPool;
        }
        
        public SQLException getException() {
            return exception;
        }
    }
    
    protected void close(Statement statement) {
        if(statement == null) return;
        try {
            statement.close();
        } catch(SQLException e) {
            // ignore
        }
    }
    
    public void shutdown() {
        // TODO
    }
}
