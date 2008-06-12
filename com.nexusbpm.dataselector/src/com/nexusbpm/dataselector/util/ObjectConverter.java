package com.nexusbpm.dataselector.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.nexusbpm.dataselector.model.config.LSColumn;

public class ObjectConverter {
    private static NumberFormat FORMAT = new DecimalFormat("###,###,###,###,###,###.###");
    private static FieldPosition POSITION = new FieldPosition(NumberFormat.INTEGER_FIELD);
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
    public static ObjectConverter getInstance() {
        return new ObjectConverter();
    }
    
    public String reformatForDisplay(String string) {
        try {
            return formatForDisplay(parse(string, BigDecimal.class.getName()));
        } catch(Exception e) {
            return string;
        }
    }
    
    public String formatForDisplay(Object object) {
        if(object instanceof Number) {
            StringBuffer buffer = new StringBuffer();
            FORMAT.format(object, buffer, POSITION);
            return buffer.toString();
        } else if(object instanceof java.util.Date &&
                !(object instanceof java.sql.Date) &&
                !(object instanceof java.sql.Time) &&
                !(object instanceof java.sql.Timestamp)) {
            return DATE_FORMAT.format((java.util.Date) object);
        } else {
            return String.valueOf(object);
        }
    }
    
    /**
     * Format the given object into a string that will be
     * understood by {@link #parse(String, LSColumn)}.
     */
    public String format(Object object) {
        if(object == null || object instanceof String) {
            return (String) object;
        } else if(object instanceof java.util.Date &&
                !(object instanceof java.sql.Date) &&
                !(object instanceof java.sql.Time) &&
                !(object instanceof java.sql.Timestamp)) {
            return DATE_FORMAT.format((java.util.Date) object);
        } else {
            return String.valueOf(object);
        }
    }
    
    public Object parse(String string, LSColumn column) {
        return parse(string, column.getJavaTypeName());
    }
    
    public Object parse(String string, String type) {
        if(type == null || string == null) {
            return null;
        } else if(type.equals(String.class.getName())) {
            return string;
        } else if(type.equals(Character.class.getName())) {
            if(/*string == null ||*/ string.length() == 0) {
                return null;
            } else {
                return Character.valueOf(string.charAt(0));
            }
        } else if(type.equals(BigDecimal.class.getName())) {
            return new BigDecimal(string);
        } else if(type.equals(BigInteger.class.getName())) {
            return new BigInteger(string);
        } else if(type.equals(Double.class.getName())) {
            return Double.valueOf(string);
        } else if(type.equals(Float.class.getName())) {
            return Float.valueOf(string);
        } else if(type.equals(Long.class.getName())) {
            return Long.valueOf(string);
        } else if(type.equals(Integer.class.getName())) {
            return Integer.valueOf(string);
        } else if(type.equals(Timestamp.class.getName())) {
            return Timestamp.valueOf(string);
        } else if(type.equals(java.sql.Date.class.getName())) {
            return java.sql.Date.valueOf(string);
        } else if(type.equals(java.sql.Time.class.getName())) {
            return java.sql.Time.valueOf(string);
        } else if(type.equals(java.util.Date.class.getName())) {
            try {
                return DATE_FORMAT.parse(string);
            } catch(ParseException e) {
                e.printStackTrace();
                return null;
            }
        } else if(type.equals(Boolean.class.getName())) {
            return Boolean.valueOf(string);
        }
        return null;
    }
    
    public String formatForDisplay(long number) {
        return FORMAT.format(number);
    }
    
    public String format(long number) {
        return String.valueOf(number);
    }
    
    public long parseLong(String number) {
        return Long.parseLong(number);
    }
    
    public int parseInt(String number) {
        return Integer.parseInt(number);
    }
    
    public String formatForDisplay(double number) {
        return FORMAT.format(number);
    }
    
    public String format(double number) {
        return String.valueOf(number);
    }
    
    public double parseDouble(String number) {
        return Double.parseDouble(number);
    }
    
    public float parseFloat(String number) {
        return Float.parseFloat(number);
    }
    
    public String formatForDisplay(boolean value) {
        return format(value);
    }
    
    public String format(boolean value) {
        return String.valueOf(value);
    }
    
    public boolean parseBoolean(String value) {
        return Boolean.parseBoolean(value);
    }
}
