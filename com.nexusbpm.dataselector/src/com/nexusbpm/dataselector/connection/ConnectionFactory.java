package com.nexusbpm.dataselector.connection;

import java.sql.Driver;
import java.sql.SQLException;

import com.nexusbpm.database.driver.DriverClassloader;
import com.nexusbpm.database.driver.SQLDriver;

public class ConnectionFactory {
    public ConnectionFactory() {
    }
    
    public ConnectionPool getConnectionPool(
            SQLDriver sqlDriver,
            String uri,
            String username,
            String password) throws SQLException {
        try {
//            String driverClass = sqlDriver.getDriverClassName();
//            Driver driver = (Driver) Class.forName(driverClass, true, getClass().getClassLoader()).newInstance();
            Driver driver = DriverClassloader.getDriver(sqlDriver);
            
            return new ConnectionPoolImpl(driver, uri, username, password);
        } catch(InstantiationException e) {
            SQLException ex = new SQLException("Error accessing driver for database type: " + sqlDriver.getName());
            ex.initCause(e);
            throw ex;
        } catch(IllegalAccessException e) {
            SQLException ex = new SQLException("Error accessing driver for database type: " + sqlDriver.getName());
            ex.initCause(e);
            throw ex;
        } catch(ClassNotFoundException e) {
            SQLException ex = new SQLException("No driver found for database type: " + sqlDriver.getName());
            ex.initCause(e);
            throw ex;
        }
    }
}
