package com.nexusbpm.dataselector.model;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class LSCondition extends AbstractModelElement implements Comparable<LSCondition>{
    public static final String PROPERTY_ADD_WHERE = "addWhere";
    public static final String PROPERTY_REMOVE_WHERE = "removeWhere";
    
    private TreeSet<LSWhere> whereClauses;
    
    public LSCondition(AbstractModelElement parent) {
        super(parent);
        whereClauses = new TreeSet<LSWhere>();
    }
    
    public boolean isRemainderCondition() {
        return whereClauses.size() == 0;
    }
    
    public Set<LSWhere> getWhereClauses() {
        return Collections.unmodifiableSet(whereClauses);
    }
    
    public void addWhereClause(LSWhere where) {
        whereClauses.add(where);
        firePropertyChange(PROPERTY_ADD_WHERE, null, where);
    }
    
    public void removeWhereClause(LSWhere where) {
        whereClauses.remove(where);
        firePropertyChange(PROPERTY_REMOVE_WHERE, where, null);
    }
    
    public int compareTo(LSCondition o) {
        if(whereClauses.size() == 0 && o.whereClauses.size() == 0) {
            return 0;
        } else if(whereClauses.size() == 0) {
            return 1;
        } else if(o.whereClauses.size() == 0) {
            return -1;
        } else {
            return whereClauses.first().compareTo(o.whereClauses.first());
        }
    }
}

