package com.nexusbpm.database.info;

public interface DBInfo {
    boolean isOther(DBColumn column);
    boolean isNumeric(DBColumn column);
    boolean isBoolean(DBColumn column);
    boolean isString(DBColumn column);
    boolean isDateTime(DBColumn column);
    boolean isBinary(DBColumn column);
    boolean supportsMin(DBColumn column);
    boolean supportsMax(DBColumn column);
    boolean supportsAverage(DBColumn column);
    boolean supportsStandardDeviation(DBColumn column);
    boolean supportsSum(DBColumn column);
    boolean isContinuous(DBColumn column);
}
