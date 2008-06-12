package com.nexusbpm.dataselector.stats;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetTranslator {
    public static List<Object> getValues(String javaType, ResultSet rs) throws SQLException {
        int typeSwitch = 0;
        if(java.sql.Timestamp.class.getName().equals(javaType)) {
            typeSwitch = 1;
        } else if(java.sql.Time.class.getName().equals(javaType)) {
            typeSwitch = 2;
        } else if(java.sql.Date.class.getName().equals(javaType)) {
            typeSwitch = 3;
        }
        
        List<Object> values = new ArrayList<Object>();
        while(rs.next()) {
            // calling getObject will not return java.sql.Timestamp objects for columns
            // that declare their type returns a java.sql.Timestamp, so we have to
            // manually ensure we get the right kind of object here
            switch(typeSwitch) {
                case 0:
                    values.add(rs.getObject(1));
                    break;
                case 1:
                    values.add(rs.getTimestamp(1));
                    break;
                case 2:
                    values.add(rs.getTime(1));
                    break;
                case 3:
                    values.add(rs.getDate(1));
                    break;
            }
        }
        return values;
    }
}
