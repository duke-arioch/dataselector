package com.nexusbpm.database.driver;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;

import com.nexusbpm.database.Activator;
import com.nexusbpm.database.driver.SQLDriver.SQLDriverCallback;

public class DriverProperties implements IPropertyChangeListener {
    private SQLDriverCallback driverCallback;
    
    private PropertyChangeSupport support;
    
    DriverProperties(SQLDriverCallback callback) {
        this.driverCallback = callback;
        this.support = new PropertyChangeSupport(SQLDriver.class);
        
        addBuiltIn("Cloudscape","jdbc:cloudscape:<DB>","COM.cloudscape.core.JDBCDriver");
        addBuiltIn("Cloudscape RMI","jdbc:rmi://<HOST>:<PORT>/jdbc:cloudscape:<DB>","RmiJdbc.RJDriver");
        addBuiltIn("Firebird (JCA/JDBC Driver)","jdbc:firebirdsql:[//<HOST>[:<PORT>]/]<DB>","org.firebirdsql.jdbc.FBDriver");
        addBuiltIn("IDS Server","jdbc:ids://<HOST>:<PORT>/conn?dsn='<ODBC_DSN_NAME>'","ids.sql.IDSDriver");
        addBuiltIn("Informix Dynamic Server","jdbc:informix-sqli://<HOST>:<PORT>/<DB>:INFORMIXSERVER=<SERVER_NAME>","com.informix.jdbc.IfxDriver");
        addBuiltIn("InstantDB (v3.13 and earlier)","jdbc:idb:<DB>","jdbc.idbDriver");
        addBuiltIn("InstantDB (v3.14 and later)","jdbc:idb:<DB>","org.enhydra.instantdb.jdbc.idbDriver");
        addBuiltIn("Interbase (InterClient Driver)","jdbc:interbase://<HOST>/<DB>","interbase.interclient.Driver");
        addBuiltIn("IBM DB2","jdbc:db2://<HOST>:<PORT>/<DB>","COM.ibm.db2.jdbc.app.DB2Driver");
        addBuiltIn("JDBC-ODBC Bridge","jdbc:odbc:<DB>","sun.jdbc.odbc.JdbcOdbcDriver");
        addBuiltIn("Hypersonic SQL (v1.2 and earlier)","jdbc:HypersonicSQL:<DB>","hSql.hDriver");
        addBuiltIn("Hypersonic SQL (v1.3 and later)","jdbc:HypersonicSQL:<DB>","org.hsql.jdbcDriver");
        addBuiltIn("Microsoft SQL Server (JTurbo Driver)","jdbc:JTurbo://<HOST>:<PORT>/<DB>","com.ashna.jturbo.driver.Driver", "stdev", false);
        addBuiltIn("Microsoft SQL Server (Sprinta Driver)","jdbc:inetdae:<HOST>:<PORT>?database=<DB>","com.inet.tds.TdsDriver", "stdev", false);
        addBuiltIn("Microsoft SQL Server 2000 (Microsoft Driver)","jdbc:microsoft:sqlserver://<HOST>:<PORT>[;DatabaseName=<DB>]","com.microsoft.jdbc.sqlserver.SQLServerDriver", "stdev", false);
        addBuiltIn("Microsoft SQL Server 2005 (Microsoft Driver)","jdbc:sqlserver://<HOST>:<PORT>[;databaseName=<DB>]","com.microsoft.sqlserver.jdbc.SQLServerDriver", "stdev", false);
        addBuiltIn("Microsoft SQL Server (Weblogic)","jdbc:weblogic:mssqlserver4:<DB>@<HOST>:<PORT>","weblogic.jdbc.mssqlserver4.Driver", "stdev", false);
        addBuiltIn("MySQL (MM.MySQL Driver)","jdbc:mysql://<HOST>:<PORT>/<DB>","org.gjt.mm.mysql.Driver");
        addBuiltIn("Oracle OCI 8i","jdbc:oracle:oci8:@<SID>","oracle.jdbc.driver.OracleDriver");
        addBuiltIn("Oracle OCI 9i","jdbc:oracle:oci:@<SID>","oracle.jdbc.driver.OracleDriver");
        addBuiltIn("Oracle Thin","jdbc:oracle:thin:@<HOST>:<PORT>:<SID>","oracle.jdbc.driver.OracleDriver");
        addBuiltIn("PointBase Embedded Server","jdbc:pointbase://embedded[:<PORT>]/<DB>","com.pointbase.jdbc.jdbcUniversalDriver");
        addBuiltIn("PostgreSQL (v6.5 and earlier)","jdbc:postgresql://<HOST>:<PORT>/<DB>","postgresql.Driver");
        addBuiltIn("PostgreSQL (v7.0 and later)","jdbc:postgresql://<HOST>:<PORT>/<DB>","org.postgresql.Driver");
        addBuiltIn("SAS","jdbc:sharenet://<hostname>[:<portnumber>]","com.sas.net.sharenet.ShareNetDriver", null);
        addBuiltIn("Sybase (jConnect 4.2 and earlier)","jdbc:sybase:Tds:<HOST>:<PORT>","com.sybase.jdbc.SybDriver");
        addBuiltIn("Sybase (jConnect 5.2)","jdbc:sybase:Tds:<HOST>:<PORT>","com.sybase.jdbc2.jdbc.SybDriver");
        addBuiltIn("Teradata driver (12.0.0.104 and later)", "jdbc:teradata://<HOST>[/DATABASE=<DB>,DBS_PORT=<PORT>,TMODE=ANSI]", "com.teradata.jdbc.TeraDriver");
        addBuiltIn("Teradata legacy driver", "jdbc:teradata//<GATEWAYHOST>:<PORT>/<DATABASEHOST>[,DATABASE=<DB>,DBS_PORT=<PORT>]", "com.ncr.teradata.TeraDriver");
        
        Preferences prefs = Activator.getDefault().getPluginPreferences();
        
        addDrivers(prefs.getString(SQLDriver.DRIVERS_PROPERTY_NAME));
        driverCallback.sortDrivers();
        // don't fire a change on initialization
        // there should be no listeners, since initialization is done before listeners can be added or removed
        prefs.addPropertyChangeListener(this);
    }
    
    protected void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    
    protected void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
    
    protected void addBuiltIn(String name, String sample, String className) {
        addBuiltIn(name, sample, className, "stddev", true);
    }
    
    protected void addBuiltIn(String name, String sample, String className, String stddevFunction) {
        addBuiltIn(name, sample, className, stddevFunction, true);
    }
    
    protected void addBuiltIn(String name, String sample, String className, boolean minMaxStrings) {
        addBuiltIn(name, sample, className, "stddev", minMaxStrings);
    }
    
    protected void addBuiltIn(String name,
                              String sample,
                              String className,
                              String stddevFunction,
                              boolean minMaxStrings) {
        driverCallback.addDriver(name, sample, className, stddevFunction, minMaxStrings, true);
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        if(event.getProperty().equals(SQLDriver.DRIVERS_PROPERTY_NAME)) {
            driverCallback.clear(false);
            
            addDrivers((String) event.getNewValue());
            driverCallback.sortDrivers();
            
            fireDriversChanged();
        }
    }
    
    protected void fireDriversChanged() {
        support.firePropertyChange(SQLDriver.DRIVERS_PROPERTY_NAME, null, SQLDriver.getDrivers());
    }
    
    protected void addDrivers(String driversString) {
        // format:
        // (|DRIVER_NAME|SAMPLE_CONNECT_STRING|CLASS_NAME|) (|NAME|SAMPLE|CLASS_NAME|) etc
        String[] driversArray = driversString.split("(\\(\\|)|(\\|\\))", -1);
        
        for(String driverString : driversArray) {
            driverString = driverString.trim();
            if(driverString.length() == 0) {
                continue;
            }
            String[] parts = driverString.split("\\|", -1);
            
            if(parts.length == 2) {
                driverCallback.addDriver(parts[0], "", parts[1], "stddev", true, false);
            } else if(parts.length == 3) {
                driverCallback.addDriver(parts[0], parts[1], parts[2], "stddev", true, false);
            } else if(parts.length == 5) {
                String stdDevFunction = null;
                if(parts[3].length() > 0) {
                    stdDevFunction = parts[3];
                }
                boolean minMaxStrings = Boolean.parseBoolean(parts[4]);
                driverCallback.addDriver(parts[0], parts[1], parts[2], stdDevFunction, minMaxStrings, false);
            }
        }
    }
    
    protected String getDriversString(List<SQLDriver> drivers) {
        StringBuilder buffer = new StringBuilder();
        
        for(SQLDriver driver : drivers) {
            if(driver.isBuiltIn()) {
                continue;
            }
            
            buffer.append("(|")
                .append(verifyString(driver.getName())).append('|')
                .append(verifyString(driver.getSampleConnectString())).append('|')
                .append(verifyString(driver.getDriverClassName())).append('|')
                .append(verifyStdDevString(driver.getStdDevFunction())).append('|')
                .append(verifyString(String.valueOf(driver.supportsMinMaxStrings()))).append("|)");
        }
        
        return buffer.toString();
    }
    
    protected String verifyString(String str) {
        str = str.replace("|", "");
        while(str.startsWith(")")) {
            str = str.substring(1);
        }
        while(str.endsWith("(")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }
    
    protected String verifyStdDevString(String str) {
        if(str == null) {
            str = "";
        }
        return verifyString(str);
    }
}
