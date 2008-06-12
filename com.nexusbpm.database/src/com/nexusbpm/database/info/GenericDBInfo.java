package com.nexusbpm.database.info;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;

public class GenericDBInfo implements DBInfo {
    private boolean supportsStdDev;
    private boolean supportsMinMaxStrings;
    
    public GenericDBInfo() {
        this.supportsStdDev = true;
        this.supportsMinMaxStrings = true;
    }
    
    public GenericDBInfo(boolean supportsStdDev, boolean supportsMinMaxStrings) {
        this.supportsStdDev = supportsStdDev;
        this.supportsMinMaxStrings = supportsMinMaxStrings;
    }
    
    public boolean isOther(DBColumn column) {
        return column.getSQLType() == Types.OTHER;
    }
    
    public boolean isNumeric(DBColumn column) {
        if(isOther(column)) return false;
        String type = column.getJavaTypeName();
        return
            type.equals(Integer.class.getName()) ||
            type.equals(Long.class.getName()) ||
            type.equals(Double.class.getName()) ||
            type.equals(Float.class.getName()) ||
            type.equals(BigDecimal.class.getName()) ||
            type.equals(BigInteger.class.getName());
    }
    
    public boolean isBoolean(DBColumn column) {
        if(isOther(column)) return false;
        String type = column.getJavaTypeName();
        return type.equals(Boolean.class.getName());
    }
    
    public boolean isString(DBColumn column) {
        if(isOther(column)) return false;
        String type = column.getJavaTypeName();
        return type.equals(String.class.getName());
    }
    
    public boolean isDateTime(DBColumn column) {
        if(isOther(column)) return false;
        String type = column.getJavaTypeName();
        return
            type.equals(java.sql.Date.class.getName()) ||
            type.equals(java.sql.Time.class.getName()) ||
            type.equals(java.sql.Timestamp.class.getName());
    }
    
    public boolean isBinary(DBColumn column) {
        // assume some sort of binary/array type if not any of the other basic types
        return !(isOther(column) ||
                isNumeric(column) ||
                isBoolean(column) ||
                isString(column) ||
                isDateTime(column));
    }
    
    public boolean isContinuous(DBColumn column) {
        return isNumeric(column) || isString(column) || isDateTime(column);
    }
    
    public boolean supportsMin(DBColumn column) {
        return isNumeric(column) ||
            isDateTime(column) ||
            (supportsMinMaxStrings && isString(column));
    }
    
    public boolean supportsMax(DBColumn column) {
        return isNumeric(column) ||
            isDateTime(column) ||
            (supportsMinMaxStrings && isString(column));
    }
    
    public boolean supportsAverage(DBColumn column) {
        return isNumeric(column);
    }
    
    public boolean supportsStandardDeviation(DBColumn column) {
        return supportsStdDev && isNumeric(column);
    }
    
    public boolean supportsSum(DBColumn column) {
        return isNumeric(column);
    }
}
