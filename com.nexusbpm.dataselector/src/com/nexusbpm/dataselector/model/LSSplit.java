package com.nexusbpm.dataselector.model;

public class LSSplit extends AbstractModelElement {
    public static final String PROPERTY_SPLIT_COLUMN = "splitColumn";
//    public static final String PROPERTY_SPLIT_TYPE = "splitType";
    
    private String column;
//    private String type;
    
    public LSSplit(LSNode parent) {
        super(parent);
    }
    
    public String getColumn() {
        return column;
    }
    
    public void setColumn(String column) {
        String oldColumn = this.column;
        this.column = column;
        firePropertyChange(PROPERTY_SPLIT_COLUMN, oldColumn, column);
    }
    
//    public String getType() {
//        return type;
//    }
//    
//    public void setType(String type) {
//        String oldType = this.type;
//        this.type = type;
//        firePropertyChange(PROPERTY_SPLIT_TYPE, oldType, type);
//    }
}
