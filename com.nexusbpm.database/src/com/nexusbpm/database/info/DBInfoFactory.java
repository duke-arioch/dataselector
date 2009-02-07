package com.nexusbpm.database.info;

import com.nexusbpm.database.driver.SQLDriver;

public class DBInfoFactory {
//    private static DBInfoFactory instance;
    
//    protected Map<String, DBInfo> cache;
    
    public DBInfoFactory() {
//        cache = new HashMap<String, DBInfo>();
    }
    
    public static DBInfoFactory getInstance() {
//        if(instance == null) {
//            instance = new DBInfoFactory();
//        }
//        return instance;
        return new DBInfoFactory();
    }
    
    public DBInfo getDBInfo(String driverName) {
        return getDBInfo(SQLDriver.getDriverInstanceByName(driverName));
    }
    
    public DBInfo getDBInfo(SQLDriver driver) {
//        return getDBInfo(driver.getName());
//    }
//    
//    public DBInfo getDBInfo(String driverName) {
//        boolean stddev = true;
//        boolean minMaxStrings = true;
//        if(driverName == null) {
//            stddev = false;
//            minMaxStrings = false;
//        } else if(driverName.contains("Microsoft SQL Server")) {
//            minMaxStrings = false;
//        } else if(driverName.contains("SAS")) {
//            stddev = false;
//        }
//        return getDBInfo(stddev, minMaxStrings);
//    }
//    
//    protected synchronized DBInfo getDBInfo(boolean stddev, boolean minMaxStrings) {
//        String key = stddev + "," + minMaxStrings;
//        if(cache.get(key) == null) {
//            cache.put(key, new GenericDBInfo(stddev, minMaxStrings));
//        }
//        return cache.get(key);
        String stdDevFunction = null;
        boolean minMaxStrings = false;
        if(driver != null) {
            stdDevFunction = driver.getStdDevFunction();
            minMaxStrings = driver.supportsMinMaxStrings();
        }
        return new GenericDBInfo(stdDevFunction, minMaxStrings);
    }
}
