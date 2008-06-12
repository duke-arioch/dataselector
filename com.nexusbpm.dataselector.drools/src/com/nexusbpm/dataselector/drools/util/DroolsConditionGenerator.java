package com.nexusbpm.dataselector.drools.util;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import com.nexusbpm.dataselector.conditions.AbstractDataConditionGenerator;
import com.nexusbpm.dataselector.model.LSWhere.Match;
import com.nexusbpm.dataselector.model.config.LSColumn;

public class DroolsConditionGenerator extends AbstractDataConditionGenerator {
    protected static final String DATE_FORMAT = "yyyy-MM-dd";
    protected static final String TIME_FORMAT = "HH:mm:ss.SSS";
    protected static final String TIMESTAMP_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;
    
    protected SimpleDateFormat dateFormat;
    protected SimpleDateFormat timeFormat;
    protected SimpleDateFormat timestampFormat;
    
    public DroolsConditionGenerator() {
        super(" &&\r\n\t\t\t", " ||\r\n\t\t\t");
        dateFormat = new SimpleDateFormat(DATE_FORMAT);
        timeFormat = new SimpleDateFormat(TIME_FORMAT);
        timestampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
    }
    public String getMatchString(Match match, LSColumn column, Object value) {
        return "ComparisonUtil." + match.toString().toLowerCase() + "(" +
            "input.get(\"" + column.getName() + "\"), " +
            getColumnValueString(column, value) + ", \"" +
            column.getJavaTypeName() + "\")";
    }
    public String getColumnValueString(LSColumn column, Object value) {
        if(value instanceof BigDecimal) {
            value = ((BigDecimal) value).toPlainString();
        } else if(value == null) {
            return "null";
        }
        String string;
        if(column.getJavaTypeName().equals(String.class.getName()) ||
                column.getJavaTypeName().equals(Character.class.getName())) {
            string = value.toString();
        } else if(column.getJavaTypeName().equals(java.sql.Timestamp.class.getName())) {
            string = toDateTimeString(timestampFormat, value);
        } else if(column.getJavaTypeName().equals(java.sql.Time.class.getName())) {
            string = toDateTimeString(timeFormat, value);
        } else if(column.getJavaTypeName().equals(java.sql.Date.class.getName())) {
            string = toDateTimeString(dateFormat, value);
        } else if(column.getJavaTypeName().equals(java.util.Date.class.getName())) {
            string = toDateTimeString(timestampFormat, value);
        } else {
            string = value.toString();
        }
        return "\"" + string + "\"";
    }
    protected String toDateTimeString(SimpleDateFormat format, Object value) {
        if(value instanceof java.util.Date) {
            return format.format((java.util.Date) value);
        } else {
            return value.toString();
        }
    }
    @Override
    protected String getNegatedCondition(String condition) {
        return "!(" + condition + ")";
    }
}
