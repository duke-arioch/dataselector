package com.nexusbpm.dataselector.conditions;

import java.util.List;

import com.nexusbpm.dataselector.model.LSCondition;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSWhere;
import com.nexusbpm.dataselector.model.config.LSColumn;

public abstract class AbstractDataConditionGenerator implements DataConditionGenerator {
    protected final String andConditionString;
    protected final String orConditionString;
    
    public AbstractDataConditionGenerator(String andConditionString, String orConditionString) {
        this.andConditionString = andConditionString;
        this.orConditionString = orConditionString;
    }
    
    protected String getAndConditionString() {
        return andConditionString;
    }
    protected String getOrConditionString() {
        return orConditionString;
    }
    
    public String getFullConditionString(LSNode node, List<LSNode> nodes) {
        String parentCondition = "";
        if(node.getConnector() != null) {
            parentCondition = getFullConditionString(node.getConnector().getSource(), nodes);
        }
        String condition = "";
        if(parentCondition.length() > 0) {
            condition = parentCondition + getAndConditionString();
        }
        condition += getNodeConditionString(node, nodes);
        return condition;
    }
    
    public String getNodeConditionString(LSNode node, List<LSNode> nodes) {
        if(node.getConnector() == null) {
            return "";
        }
        LSNode parent = node.getConnector().getSource();
        String columnName = parent.getSplit().getColumn();
        LSColumn column = null;
        for(LSColumn c : parent.getTree().getConfig().getColumns()) {
            if(c.getName().equals(columnName)) {
                column = c;
                break;
            }
        }
        String conditionString = "";
        if(node.isRemainderNode()) {
            for(LSNode sibling : parent.getSubNodes()) {
                if(sibling != node && (nodes == null || !nodes.contains(sibling))) {
                    if(conditionString.length() > 0) {
                        conditionString += getOrConditionString();
                    }
                    conditionString += getNodeConditionString(sibling, null);
                }
            }
            
            conditionString = getNegatedCondition(conditionString);
        } else {
            for(LSCondition cond : node.getConditions()) {
                if(conditionString.length() > 0) {
                    conditionString += getOrConditionString();
                }
                String whereString = "";
                for(LSWhere where : cond.getWhereClauses()) {
                    if(whereString.length() > 0) {
                        whereString += getAndConditionString();
                    }
                    whereString += getMatchString(where.getMatch(), column, where.getValue());
                }
                conditionString += whereString;
            }
            if(node.getConditions().size() > 1) {
                conditionString = "(" + conditionString + ")";
            }
        }
        return conditionString;
    }
    
    protected abstract String getNegatedCondition(String condition);
}
