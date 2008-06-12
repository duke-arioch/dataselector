package com.nexusbpm.dataselector.conditions;

import java.util.List;

import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSWhere.Match;
import com.nexusbpm.dataselector.model.config.LSColumn;

public interface DataConditionGenerator {
    /**
     * Returns the appropriate condition string for the given node or an empty
     * string for the root node. Will not return null.
     * If the given node is a remainder node, then the condition string will
     * be generated assuming that the data corresponding to the nodes in the
     * given list should be included as well.
     */
    String getFullConditionString(LSNode node, List<LSNode> nodes);
    
    /**
     * Gets the part of the condition string for the given node (and not its ancestors).
     * Returns the empty string for the root node.
     */
    String getNodeConditionString(LSNode node, List<LSNode> nodes);
    
    /**
     * Returns an appropriate string expression to match the given column
     * to the given value according to the specified Match type.
     */
    String getMatchString(Match match, LSColumn column, Object value);
    
    /**
     * Returns an appropriate string value for the given object value.
     * The string value should be converted into the appropriate textual
     * representation, surrounded by quote characters, etc as necessary.
     */
    String getColumnValueString(LSColumn column, Object value);
}
