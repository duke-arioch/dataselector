package com.nexusbpm.dataselector.database;

import com.nexusbpm.database.driver.SQLDriver;

public class SQLGeneratorFactory {
    private static SQLGeneratorFactory instance;
    
    public SQLGeneratorFactory() {
    }
    
    public static synchronized SQLGeneratorFactory getInstance() {
        if(instance == null) {
            instance = new SQLGeneratorFactory();
        }
        return instance;
    }
    
    public SQLGenerator getGenerator(SQLDriver driver) {
        return getGenerator(driver.getName());
    }
    
    public SQLGenerator getGenerator(String driverName) {
        // currently assume the generic generator is good enough, and modify as needed
        return new GenericSQLGenerator();
    }
}
