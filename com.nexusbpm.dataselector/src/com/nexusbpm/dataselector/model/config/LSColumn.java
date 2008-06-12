package com.nexusbpm.dataselector.model.config;

import com.nexusbpm.database.info.DBColumn;
import com.nexusbpm.dataselector.model.NamedModelElement;

public class LSColumn extends NamedModelElement implements Comparable<LSColumn>, DBColumn {
    public static final String PROPERTY_COLUMN_SQL_TYPE = "columnSQLType";
    public static final String PROPERTY_COLUMN_TYPE_NAME = "columnTypeName";
    public static final String PROPERTY_COLUMN_JAVA_TYPE = "columnJavaType";
    public static final String PROPERTY_COLUMN_ORDINAL = "columnOrdinal";
    public static final String PROPERTY_COLUMN_IS_PREDICTOR = "columnIsPredictor";
//    public static final String PROPERTY_COLUMN_TABLE_NAME = "columnTableName";
    
    private int sqlType;
    private String typeName;
    private String javaType;
//    private String tableName;
    private int ordinal;
    private boolean predictor;
    
    public LSColumn(LSConfig config) {
        super(config);
    }
    
    @Override
    public LSConfig getParent() {
        return (LSConfig) super.getParent();
    }
    
    public int getSQLType() {
        return sqlType;
    }
    
    public void setSQLType(int sqlType) {
        Integer oldType = Integer.valueOf(this.sqlType);
        this.sqlType = sqlType;
        firePropertyChange(PROPERTY_COLUMN_SQL_TYPE, oldType, Integer.valueOf(sqlType));
    }
    
    public String getTypeName() {
        return typeName;
    }
    
    public void setTypeName(String typeName) {
        String oldTypeName = this.typeName;
        this.typeName = typeName;
        firePropertyChange(PROPERTY_COLUMN_TYPE_NAME, oldTypeName, typeName);
    }
    
    public String getJavaTypeName() {
        return javaType;
    }
    
    public void setJavaTypeName(String javaType) {
        String oldJavaType = this.javaType;
        this.javaType = javaType;
        firePropertyChange(PROPERTY_COLUMN_JAVA_TYPE, oldJavaType, javaType);
    }
    
//    public String getTableName() {
//        return tableName;
//    }
    
//    public void setTableName(String tableName) {
//        String oldTableName = this.tableName;
//        this.tableName = tableName;
//        firePropertyChange(PROPERTY_COLUMN_TABLE_NAME, oldTableName, tableName);
//    }
    
    public int getOrdinal() {
        return ordinal;
    }
    
    public void setOrdinal(int ordinal) {
        Integer oldOrdinal = Integer.valueOf(this.ordinal);
        this.ordinal = ordinal;
        firePropertyChange(PROPERTY_COLUMN_ORDINAL, oldOrdinal, Integer.valueOf(ordinal));
    }
    
    public boolean isPredictor() {
        return predictor;
    }
    
    public void setPredictor(boolean predictor) {
        Boolean oldPredictor = Boolean.valueOf(this.predictor);
        this.predictor = predictor;
        firePropertyChange(PROPERTY_COLUMN_IS_PREDICTOR, oldPredictor, Boolean.valueOf(predictor));
    }
    
    public int compareTo(LSColumn o) {
        if(ordinal == o.ordinal) throw new IllegalStateException("Columns cannot have equal ordinals!");
        return ordinal - o.ordinal;
    }
}
