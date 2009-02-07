package com.nexusbpm.dataselector.database;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nexusbpm.database.info.DBInfo;
import com.nexusbpm.database.info.DBInfoFactory;
import com.nexusbpm.dataselector.conditions.AbstractDataConditionGenerator;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSWhere.Match;
import com.nexusbpm.dataselector.model.config.LSColumn;
import com.nexusbpm.dataselector.model.config.LSConfig;
import com.nexusbpm.dataselector.util.ObjectConverter;

public class GenericSQLGenerator extends AbstractDataConditionGenerator implements SQLGenerator {
    protected static final String NA = "'n/a'";
    protected static final String SEPARATOR = ", ";
    
    protected static final String DATE_FORMAT = "yyyy-MM-dd";
    protected static final String TIME_FORMAT = "HH:mm:ss.SSS";
    protected static final String TIMESTAMP_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;
    
    protected SimpleDateFormat dateFormat;
    protected SimpleDateFormat timeFormat;
    protected SimpleDateFormat timestampFormat;
    
    protected ObjectConverter converter;
    protected DBInfoFactory infoFactory;
    protected Pattern queryPattern;
    
    public GenericSQLGenerator() {
        this(" and ", " or ");
    }
    
    public GenericSQLGenerator(String andConditionString, String orConditionString) {
        super(andConditionString, orConditionString);
        converter = new ObjectConverter();
        dateFormat = new SimpleDateFormat(DATE_FORMAT);
        timeFormat = new SimpleDateFormat(TIME_FORMAT);
        timestampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
    }
    
    public String getOutputQuery(
            List<LSNode> nodes, LSConfig config,
            boolean useNodeNames, boolean useOnlyPredictors) {
        if(!useNodeNames && !useOnlyPredictors && nodes.size() == 1 && nodes.get(0).getConnector() == null) {
            return config.getQuery();
        }
        String select = getOutputSelectClause(config, nodes, useNodeNames, useOnlyPredictors);
        String query = select + " " + getFromClause(nodes.get(0));
        String where = "";
        for(LSNode node : nodes) {
            // first check if a selected sibling node is a remainder node
            boolean remainder = remainder(node, nodes);
            if(remainder && !node.isRemainderNode()) {
                // when a sibling remainder node is selected,
                // only add to the where clause if this node is the remainder node
                continue;
            }
            
            if(where.length() > 0) {
                where += " or ";
            }
            if(remainder) {
//                where += getSimpleWhereClause(node, nodes);
                where += getFullConditionString(node, nodes);
            } else {
//                where += getSimpleWhereClause(node);
                where += getFullConditionString(node, null);
            }
        }
        if(where.length() > 0) {
            where = " where " + where;
        }
        return query + where;
    }
    
    protected boolean remainder(LSNode node, List<LSNode> nodes) {
        LSNode remainderNode = null;
        if(node.getConnector() != null) {
            LSNode parent = node.getConnector().getSource();
            
            for(LSNode child : parent.getSubNodes()) {
                if(child.isRemainderNode() && nodes.contains(child)) {
                    remainderNode = child;
                    break;
                }
            }
        }
        return remainderNode != null;
    }
    
    public String getOutputSelectClause(
            LSConfig config, Collection<LSNode> nodes,
            boolean useNodeNames, boolean useOnlyPredictors) {
        if(!useNodeNames && !useOnlyPredictors) {
            return "select *";
        }
        
        TreeSet<LSNode> nodeSet = new TreeSet<LSNode>(); // set of named nodes
        
        boolean allDataNamed = true;
        if(useNodeNames) {
            for(LSNode node : nodes) {
                if(!addNamedNodes(node, nodeSet)) {
                    allDataNamed = false;
                }
            }
        }
        
        if(nodeSet.size() == 0 && !useOnlyPredictors) {
            return "select *";
        }
        
        StringBuilder buffer = new StringBuilder();
        
        buffer.append("select ");
        
        Set<String> columnNames = new HashSet<String>();
        for(LSColumn column : config.getColumns()) {
            if(column.isPredictor() || !useOnlyPredictors) {
                columnNames.add(column.getName());
                buffer.append(column.getName());
                buffer.append(", ");
            }
        }
        
        if(nodeSet.size() == 0) {
            buffer.delete(buffer.length() - 2, buffer.length());
        } else if(nodeSet.size() == 1 && allDataNamed) {
            buffer.append("'");
            buffer.append(nodeSet.first().getName());
            buffer.append("' as ");
        } else {
            buffer.append("case");
            
            List<LSNode> queue = new ArrayList<LSNode>(nodeSet);
            while(queue.size() > 0) {
                LSNode node = queue.remove(queue.size() - 1);
                if(node.getConnector() == null) {
                    buffer.append(" else '");
                    buffer.append(node.getName());
                    buffer.append("'");
                } else {
                    buffer.append(" when ");
//                    buffer.append(getSimpleWhereClause(node));
                    buffer.append(getFullConditionString(node, null));
                    buffer.append(" then '");
                    buffer.append(node.getName());
                    buffer.append("'");
                }
            }
            
            if(!allDataNamed) {
                buffer.append(" else null");
            }
            buffer.append(" end as ");
        }
        
        if(nodeSet.size() > 0) {
            String name = "data_selector_segment_name";
            for(int number = 2; columnNames.contains(name); number++) {
                name = "data_selector_segment_name" + number;
            }
            
            buffer.append(name);
        }
        
        return buffer.toString();
    }
    
    /**
     * Returns true if all data for the given node has a name applied to it
     */
    protected boolean addNamedNodes(LSNode node, Set<LSNode> nodes) {
        boolean allDataNamed = false;
        if(node.getName() != null && node.getName().length() > 0) {
            nodes.add(node);
            allDataNamed = true;
        }
        boolean allChildrenNamed = node.getSubNodes().size() > 0;
        for(LSNode child : node.getSubNodes()) {
            if(!addNamedNodes(child, nodes)) {
                allChildrenNamed = false;
            }
        }
        if(allChildrenNamed) {
            allDataNamed = true;
        }
        if(!allDataNamed) {
            LSNode parent = node;
            while(parent != null) {
                if(parent.getName() != null && parent.getName().length() > 0) {
                    nodes.add(parent);
                    allDataNamed = true;
                    parent = null;
                } else if(parent.getConnector() != null) {
                    parent = parent.getConnector().getSource();
                } else {
                    parent = null;
                }
            }
        }
        return allDataNamed;
    }
    
    public String getStatsSelectClause(LSColumn column) {
        String columnName = column.getName();
        
        DBInfo info = getDBInfo(column.getParent());
        
        String distinct;
        if(info.isOther(column)) {
            distinct = "1";
        } else {
            distinct = "count(distinct " + columnName + ")";
        }
        
        String min;
        if(info.supportsMin(column)) {
            min = "min(" + columnName + ")";
        } else {
            min = NA;
        }
        
        String max;
        if(info.supportsMax(column)) {
            max = "max(" + columnName + ")";
        } else {
            max = NA;
        }
        
        String average;
        if(info.supportsAverage(column)) {
            average = "avg(" + columnName + ")";
        } else {
            average = NA;
        }
        
        String stddev;
        if(info.supportsStandardDeviation(column)) {
            stddev = info.getStandardDeviationFunction(column) + "(" + columnName + ")";
        } else {
            stddev = NA;
        }
        
        String sum;
        if(info.supportsSum(column)) {
            sum = "sum(" + columnName + ")";
        } else {
            sum = NA;
        }
        
        return
            distinct + SEPARATOR +
            min + SEPARATOR +
            max + SEPARATOR +
            average + SEPARATOR +
            stddev + SEPARATOR +
            sum;
    }
    
    protected DBInfo getDBInfo(LSConfig config) {
        if(infoFactory == null) {
            infoFactory = DBInfoFactory.getInstance();
        }
        return infoFactory.getDBInfo(config.getDriver().getName());
    }
    
    /**
     * Gets the appropriate from clause for the given node. Will not return null
     * or an empty string. Examples:
     * <pre>
     * from tablename
     * from (select * from ...)
     * </pre>
     */
    public String getFromClause(LSNode node) {
        return "from " + getSimpleFromClause(node);
    }
    
    /**
     * Gets the from clause for the particular node minus the "from " part. Will
     * not return null or an empty string.
     */
    public String getSimpleFromClause(LSNode node) {
        String query = node.getTree().getConfig().getQuery();
        if(query.contains(";")) {
            query = query.substring(0, query.indexOf(";"));
        }
        if(queryPattern == null) {
            queryPattern = Pattern.compile("(\\s*)select(\\s+)([^']+)(\\s+)from(\\s+)(\\w+)(\\s*)");
        }
        Matcher matcher = queryPattern.matcher(query.toLowerCase());
        if(matcher.matches() && !matcher.group(3).toLowerCase().contains("case")) {
            return matcher.group(6);
        }
        return "(" + query + ") lssubquery";
    }
    
    /**
     * Gets the appropriate where clause for the given node. Will not return null
     * but may return an empty string if no where clause is needed.
     * Examples:
     * <pre>
     * where (col1 > 10 and col1 < 20) and (col2 == 'GOLD' or col2 == 'PLATINUM')
     * </pre>
     */
    public String getWhereClause(LSNode node) {
//        String where = getSimpleWhereClause(node);
        String where = getFullConditionString(node, null);
        if(where.length() > 0) {
            where = "where " + where;
        }
        return where;
    }
    
//    /**
//     * Gets the where clause for the particular node minus the "where " part. Will
//     * not return null but may return an empty string if no where clause is needed.
//     */
//    public String getSimpleWhereClause(LSNode node) {
//        return getSimpleWhereClause(node, null);
//    }
//    
//    /**
//     * Gets the where clause for the particular node minus the "where " part. If
//     * the node is a remainder node the where clause is generated assuming that
//     * the given list of nodes are also contained in the data set.
//     */
//    protected String getSimpleWhereClause(LSNode node, List<LSNode> nodes) {
//        String parentWhere = "";
//        if(node.getConnector() != null) {
//            parentWhere = getSimpleWhereClause(node.getConnector().getSource());
//        }
//        String where = "";
//        if(parentWhere.length() > 0) {
//            where = parentWhere + " and ";
//        }
//        where += getNodeConditionString(node, nodes);
//        return where;
//    }
    
//    /**
//     * Gets the part of the where clause for the given node (and not its ancestors).
//     * For example, if the full where clause for the node is:
//     * <pre>
//     * where (col1 > 10 and col1 < 20) and (col2 == 'GOLD or col2 == 'PLATINUM')
//     * </pre>
//     * and the given node's conditions are for col2 equal to "GOLD" or "PLATINUM"
//     * then the following is returned:
//     * <pre>
//     * col2 == 'GOLD' or col2 == 'PLATINUM'
//     * </pre>
//     * Returns the empty string for the root node.
//     */
//    public String getNodeConditionString(LSNode node) {
//        return getNodeConditionString(node, null);
//    }
    
//    /**
//     * @see #getNodeConditionString(LSNode)
//     * @see #getSimpleWhereClause(LSNode, List)
//     */
//    protected String getNodeConditionString(LSNode node, List<LSNode> nodes) {
//        if(node.getConnector() == null) {
//            return "";
//        }
//        LSNode parent = node.getConnector().getSource();
//        String columnName = parent.getSplit().getColumn();
//        LSColumn column = null;
//        for(LSColumn c : parent.getTree().getConfig().getColumns()) {
//            if(c.getName().equals(columnName)) {
//                column = c;
//                break;
//            }
//        }
//        String conditionString = "";
//        if(node.isRemainderNode()) {
//            for(LSNode sibling : parent.getSubNodes()) {
//                if(sibling != node && (nodes == null || !nodes.contains(sibling))) {
//                    if(conditionString.length() > 0) {
//                        conditionString += " or ";
//                    }
//                    conditionString += getNodeConditionString(sibling, null);
//                }
//            }
//            
//            conditionString = "not (" + conditionString + ")";
//        } else {
//            for(LSCondition cond : node.getConditions()) {
//                if(conditionString.length() > 0) {
//                    conditionString += " or ";
//                }
//                String whereString = "";
//                for(LSWhere where : cond.getWhereClauses()) {
//                    if(whereString.length() > 0) {
//                        whereString += " and ";
//                    }
//                    whereString += getMatchString(where.getMatch(), column, where.getValue());
//                }
//                conditionString += whereString;
//            }
//            if(node.getConditions().size() > 1) {
//                conditionString = "(" + conditionString + ")";
//            }
//        }
//        return conditionString;
//    }
    
    public String getMatchString(Match match, LSColumn column, Object value) {
        // TODO may need to handle additional cases here
//        if(match == Match.EXACT) {
//            match = Match.EQ;
//        }
        String matchString = null;
        if(value == null) {
            if(match == Match.EQ) {
                matchString = " is ";
            } else /*if(match == Match.NEQ)*/ {
                matchString = " is not ";
            }
        } else {
            matchString = " " + match.getDisplayString() + " ";
        }
        String valueString = getColumnValueString(column, value);
        
        // some databases don't like "col = ''" so we detect that case and use "col not like '_%'"
        if(column.getJavaTypeName().equals(String.class.getName()) && valueString.equals("''")) {
            return column.getName() + " not like '_%'";
        } else {
            return column.getName() + matchString + valueString;
        }
    }
    
    /**
     * Returns an appropriate string to insert into a SQL statement as a value
     * for the given value. In other words, the following code generates part
     * of a valid SQL statement (assuming the value is not null):
     * <pre>
     * "where " + columnName + " = " + getColumnValueString(column, value)
     * </pre>
     * The value will be converted into the appropriate textual representation
     * and surrounded by single quote characters if necessary.
     */
    public String getColumnValueString(LSColumn column, Object value) {
        if(value instanceof BigDecimal) {
            value = ((BigDecimal) value).toPlainString();
        } else if(value == null) {
            return "null";
        }
        String string;
        if(column.getJavaTypeName().equals(String.class.getName()) ||
                column.getJavaTypeName().equals(Character.class.getName())) {
            string = "'" + converter.format(value) + "'";
        } else if(column.getJavaTypeName().equals(java.sql.Timestamp.class.getName())) {
            string = "TIMESTAMP '" + toDateTimeString(timestampFormat, value) + "'";
        } else if(column.getJavaTypeName().equals(java.sql.Time.class.getName())) {
            string = "TIME '" + toDateTimeString(timeFormat, value) + "'";
        } else if(column.getJavaTypeName().equals(java.sql.Date.class.getName())) {
            string = "DATE '" + toDateTimeString(dateFormat, value) + "'";
        } else {
            string = converter.format(value);
        }
        return string;
    }
    
    protected String toDateTimeString(SimpleDateFormat format, Object value) {
        if(value instanceof java.util.Date) {
            return format.format((java.util.Date) value);
        } else {
            return (String) value;
        }
    }
    
    @Override
    protected final String getNegatedCondition(String condition) {
        return "not (" + condition + ")";
    }
}
