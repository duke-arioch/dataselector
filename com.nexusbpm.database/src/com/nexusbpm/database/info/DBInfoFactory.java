package com.nexusbpm.database.info;

import java.util.HashMap;
import java.util.Map;

import com.nexusbpm.database.driver.SQLDriver;

public class DBInfoFactory {
    private static DBInfoFactory instance;
    
    protected Map<String, DBInfo> cache;
    
    public DBInfoFactory() {
        cache = new HashMap<String, DBInfo>();
    }
    
    public static synchronized DBInfoFactory getInstance() {
        if(instance == null) {
            instance = new DBInfoFactory();
        }
        return instance;
    }
    
    public DBInfo getDBInfo(SQLDriver driver) {
        return getDBInfo(driver.getName());
    }
    
    public DBInfo getDBInfo(String driverName) {
        boolean stddev = true;
        boolean minMaxStrings = true;
        if(driverName == null) {
            stddev = false;
            minMaxStrings = false;
        } else if(driverName.contains("Microsoft SQL Server")) {
            minMaxStrings = false;
        } else if(driverName.contains("SAS")) {
            stddev = false;
        }
        return getDBInfo(stddev, minMaxStrings);
    }
    
    protected synchronized DBInfo getDBInfo(boolean stddev, boolean minMaxStrings) {
        String key = stddev + "," + minMaxStrings;
        if(cache.get(key) == null) {
            cache.put(key, new GenericDBInfo(stddev, minMaxStrings));
        }
        return cache.get(key);
    }
}
