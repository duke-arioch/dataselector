package com.nexusbpm.dataselector.database;

import java.util.Collection;
import java.util.List;

import com.nexusbpm.dataselector.conditions.DataConditionGenerator;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.config.LSColumn;
import com.nexusbpm.dataselector.model.config.LSConfig;

public interface SQLGenerator extends DataConditionGenerator {
    String getOutputQuery(List<LSNode> nodes, LSConfig config,
            boolean useNodeNames, boolean useOnlyPredictors);
    String getOutputSelectClause(LSConfig config, Collection<LSNode> nodes,
            boolean useNodeNames, boolean useOnlyPredictors);
    
    String getStatsSelectClause(LSColumn column);
    
    /**
     * Gets the appropriate from clause for the given node. Will not return null
     * or an empty string. Examples:
     * <pre>
     * from tablename
     * from (select * from ...)
     * </pre>
     */
    String getFromClause(LSNode node);
    
    /**
     * Gets the from clause for the particular node minus the "from " part. Will
     * not return null or an empty string.
     */
    String getSimpleFromClause(LSNode node);
    
    /**
     * Gets the appropriate where clause for the given node. Will not return null
     * but may return an empty string if no where clause is needed.
     * Examples:
     * <pre>
     * where (col1 > 10 and col1 < 20) and (col2 == 'GOLD' or col2 == 'PLATINUM')
     * </pre>
     */
    String getWhereClause(LSNode node);
    
//    /**
//     * Gets the where clause for the particular node minus the "where " part. Will
//     * not return null but may return an empty string if no where clause is needed.
//     */
//    String getSimpleWhereClause(LSNode node);
//    
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
//    String getNodeConditionString(LSNode node);
    
//    String getMatchString(Match match, LSColumn column, Object value);
    
//    /**
//     * Returns an appropriate string to insert into a SQL statement as a value
//     * for the given value. In other words, the following code generates part
//     * of a valid SQL statement (assuming the value is not null):
//     * <pre>
//     * "where " + columnName + " = " + getColumnValueString(column, value)
//     * </pre>
//     * The value will be converted into the appropriate textual representation
//     * and surrounded by single quote characters if necessary.
//     */
//    String getColumnValueString(LSColumn column, Object value);
}
