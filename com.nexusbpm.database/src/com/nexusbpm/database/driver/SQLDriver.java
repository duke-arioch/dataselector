package com.nexusbpm.database.driver;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLDriver {
    private static final List<SQLDriver> drivers = new ArrayList<SQLDriver>();
    private static final List<String> driverNames = new ArrayList<String>();
    
    private String name;
    private String sampleConnectString;
    private String driverClassName;
    
    static {
        addRow("Cloudscape","jdbc:cloudscape:<DB>","COM.cloudscape.core.JDBCDriver");
        addRow("Cloudscape RMI","jdbc:rmi://<HOST>:<PORT>/jdbc:cloudscape:<DB>","RmiJdbc.RJDriver");
        addRow("Firebird (JCA/JDBC Driver)","jdbc:firebirdsql:[//<HOST>[:<PORT>]/]<DB>","org.firebirdsql.jdbc.FBDriver");
        addRow("IDS Server","jdbc:ids://<HOST>:<PORT>/conn?dsn='<ODBC_DSN_NAME>'","ids.sql.IDSDriver");
        addRow("Informix Dynamic Server","jdbc:informix-sqli://<HOST>:<PORT>/<DB>:INFORMIXSERVER=<SERVER_NAME>","com.informix.jdbc.IfxDriver");
        addRow("InstantDB (v3.13 and earlier)","jdbc:idb:<DB>","jdbc.idbDriver");
        addRow("InstantDB (v3.14 and later)","jdbc:idb:<DB>","org.enhydra.instantdb.jdbc.idbDriver");
        addRow("Interbase (InterClient Driver)","jdbc:interbase://<HOST>/<DB>","interbase.interclient.Driver");
        addRow("IBM DB2","jdbc:db2://<HOST>:<PORT>/<DB>","COM.ibm.db2.jdbc.app.DB2Driver");
        addRow("JDBC-ODBC Bridge","jdbc:odbc:<DB>","sun.jdbc.odbc.JdbcOdbcDriver");
        addRow("Hypersonic SQL (v1.2 and earlier)","jdbc:HypersonicSQL:<DB>","hSql.hDriver");
        addRow("Hypersonic SQL (v1.3 and later)","jdbc:HypersonicSQL:<DB>","org.hsql.jdbcDriver");
        addRow("Microsoft SQL Server (JTurbo Driver)","jdbc:JTurbo://<HOST>:<PORT>/<DB>","com.ashna.jturbo.driver.Driver");
        addRow("Microsoft SQL Server (Sprinta Driver)","jdbc:inetdae:<HOST>:<PORT>?database=<DB>","com.inet.tds.TdsDriver");
        addRow("Microsoft SQL Server 2000 (Microsoft Driver)","jdbc:microsoft:sqlserver://<HOST>:<PORT>[;DatabaseName=<DB>]","com.microsoft.jdbc.sqlserver.SQLServerDriver");
        addRow("Microsoft SQL Server (Weblogic)","jdbc:weblogic:mssqlserver4:<DB>@<HOST>:<PORT>","weblogic.jdbc.mssqlserver4.Driver");
        addRow("MySQL (MM.MySQL Driver)","jdbc:mysql://<HOST>:<PORT>/<DB>","org.gjt.mm.mysql.Driver");
        addRow("Oracle OCI 8i","jdbc:oracle:oci8:@<SID>","oracle.jdbc.driver.OracleDriver");
        addRow("Oracle OCI 9i","jdbc:oracle:oci:@<SID>","oracle.jdbc.driver.OracleDriver");
        addRow("Oracle Thin","jdbc:oracle:thin:@<HOST>:<PORT>:<SID>","oracle.jdbc.driver.OracleDriver");
        addRow("PointBase Embedded Server","jdbc:pointbase://embedded[:<PORT>]/<DB>","com.pointbase.jdbc.jdbcUniversalDriver");
        addRow("PostgreSQL (v6.5 and earlier)","jdbc:postgresql://<HOST>:<PORT>/<DB>","postgresql.Driver");
        addRow("PostgreSQL (v7.0 and later)","jdbc:postgresql://<HOST>:<PORT>/<DB>","org.postgresql.Driver");
        addRow("SAS","jdbc:sharenet://<hostname>[:<portnumber>]","com.sas.net.sharenet.ShareNetDriver");
        addRow("Sybase (jConnect 4.2 and earlier)","jdbc:sybase:Tds:<HOST>:<PORT>","com.sybase.jdbc.SybDriver");
        addRow("Sybase (jConnect 5.2)","jdbc:sybase:Tds:<HOST>:<PORT>","com.sybase.jdbc2.jdbc.SybDriver"); 
    }
    
    private static void addRow(String desc, String sample, String className) {
        SQLDriver driver = new SQLDriver(desc, sample, className);
        drivers.add(driver);
        driverNames.add(desc);
    }
    
    public static List<SQLDriver> getDrivers() {
        return drivers;
    }
    
    public static List<String> getDriverNames() {
        return driverNames;
    }
    
    public static SQLDriver getDriverInstanceByName(String name) {
        for (int index = 0; index < drivers.size(); index++) {
            SQLDriver driver = drivers.get(index);
            if (driver.getName().equals(name)) return driver;
        }
        return null;
    }
    
    public static SQLDriver getDriverInstanceByClassName(String className) {
        for(int index = 0; index < drivers.size(); index++) {
            SQLDriver driver = drivers.get(index);
            if(driver.getDriverClassName().equals(className)) {
                return driver;
            }
        }
        return null;
    }
    
    public static SQLDriver getDriverInstanceByClassNameAndURI(String className, String uri) {
    	Pattern regex = Pattern.compile("jdbc:\\w*:{0,1}\\w*");
        for (int index = 0; index < drivers.size(); index++) {
        	SQLDriver driver = drivers.get(index);
        	String sample = driver.getSampleConnectString();
        	Matcher m = regex.matcher(sample);
        	m.find();
        	String split = m.group();
        	if (driver.getDriverClassName().equals(className)
    			&& uri.startsWith(split)
           	) { 
            	return driver;
        	}
        }
        return null;
    }
    
    public SQLDriver(String name, String sampleConnectString, String driverClassName) {
        this.name = name;
        this.sampleConnectString = sampleConnectString;
        this.driverClassName = driverClassName;
    }
    
    public String getDriverClassName() {
        return driverClassName;
    }
    
    public String getName() {
        return name;
    }
    
    public String getSampleConnectString() {
        return sampleConnectString;
    }
    
    public String toString() {
        return getDriverClassName();
    }
}
