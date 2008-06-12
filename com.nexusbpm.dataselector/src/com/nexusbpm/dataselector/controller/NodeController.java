package com.nexusbpm.dataselector.controller;

import java.beans.PropertyChangeEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;

import com.nexusbpm.dataselector.animation.AnimationManagerFactory;
import com.nexusbpm.dataselector.figures.SelectorNodeFigure;
import com.nexusbpm.dataselector.model.AbstractModelElement;
import com.nexusbpm.dataselector.model.DisplayExtension;
import com.nexusbpm.dataselector.model.LSColumnStats;
import com.nexusbpm.dataselector.model.LSCondition;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSNodeConnector;
import com.nexusbpm.dataselector.model.LSSplit;
import com.nexusbpm.dataselector.model.LSStats;
import com.nexusbpm.dataselector.model.LSWhere;
import com.nexusbpm.dataselector.model.config.LSColumn;
import com.nexusbpm.dataselector.util.ColorCache;
import com.nexusbpm.dataselector.util.FontCache;
import com.nexusbpm.dataselector.util.ImageCache;
import com.nexusbpm.dataselector.util.ObjectConverter;

public class NodeController extends AbstractController<LSNode> implements NodeEditPart {
    private static ObjectConverter converter = ObjectConverter.getInstance();
    private NumberFormat PERCENT_FORMAT = new DecimalFormat("####.#%");
    
    public NodeController(TreeController tree, LSNode node) {
        super(tree);
        setModel(node);
    }
    
    @Override
    protected void listenToModel() {
        LSNode node = getModel();
        node.addPropertyChangeListener(this);
        LSStats stats = node.getStats();
        if(stats != null) {
            stats.addPropertyChangeListener(this);
        }
    }
    
    @Override
    protected void stopListeningToModel() {
        LSNode node = getModel();
        node.removePropertyChangeListener(this);
        LSStats stats = node.getStats();
        if(stats != null) {
            stats.removePropertyChangeListener(this);
        }
    }
    
    @Override
    protected List<AbstractModelElement> getModelSourceConnections() {
        List<AbstractModelElement> connections = new ArrayList<AbstractModelElement>();
        for(LSNode child : getModel().getSubNodes()) {
            if(child.getConnector() != null) {
                connections.add(child.getConnector());
            }
        }
        return connections;
    }
    
    @Override
    protected List<LSNodeConnector> getModelTargetConnections() {
        if(getModel().getConnector() != null) {
            return Collections.singletonList(getModel().getConnector());
        } else {
            return Collections.emptyList();
        }
    }
    
    @Override
    protected void createEditPolicies() {
        // TODO
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if(event.getPropertyName().equals(LSNode.PROPERTY_NODE_BOUNDS) ||
                event.getPropertyName().equals(LSNode.PROPERTY_ELEMENT_NAME) ||
                event.getPropertyName().equals(LSNode.PROPERTY_ADD_CONDITION) ||
                event.getPropertyName().equals(LSNode.PROPERTY_REMOVE_CONDITION) ||
                event.getPropertyName().equals(LSSplit.PROPERTY_SPLIT_COLUMN)) {
            queueRefreshVisuals();
        } else if(event.getPropertyName().equals(LSNode.PROPERTY_NODE_SPLIT)) {
            if(event.getOldValue() != null) {
                ((LSSplit) event.getOldValue()).removePropertyChangeListener(this);
            }
            if(event.getNewValue() != null) {
                ((LSSplit) event.getNewValue()).addPropertyChangeListener(this);
            }
            queueRefreshVisuals();
        } else if(event.getPropertyName().equals(LSNode.PROPERTY_ADD_SUB_NODE) ||
                event.getPropertyName().equals(LSNode.PROPERTY_REMOVE_SUB_NODE)) {
            queueRefreshSourceConnections();
        } else if(event.getPropertyName().equals(LSNode.PROPERTY_NODE_STATS)) {
            LSStats oldStats = (LSStats) event.getOldValue();
            LSStats newStats = (LSStats) event.getNewValue();
            if(oldStats != null) {
                oldStats.removePropertyChangeListener(this);
            }
            if(newStats != null) {
                newStats.addPropertyChangeListener(this);
            }
            queueRefreshVisuals();
        } else if(event.getPropertyName().equals(LSStats.PROPERTY_ADD_COLUMN_STATS) ||
                event.getPropertyName().equals(LSStats.PROPERTY_CLEAR_COLUMN_STATS) ||
                event.getPropertyName().equals(LSStats.PROPERTY_STATS_ROW_COUNT) ||
                event.getPropertyName().equals(LSStats.PROPERTY_UPDATE_COLUMN_STATS)) {
            queueRefreshVisuals();
        } else {
            super.propertyChange(event);
        }
    }
    
    @Override
    public void refreshVisuals() {
        Rectangle bounds = getModel().getBounds();
        ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), bounds);
        
        SelectorNodeFigure figure = (SelectorNodeFigure) getFigure();
        figure.setName(getModel().getName());
        figure.setSegment(getValueString(getModel()));
        figure.setSegmentItalicized(getModel().getConnector() == null || getModel().isRemainderNode());
//        figure.setValues(getValueString(getModel()));
        String column = "";
        if(getModel().getSplit() != null && getModel().getSplit().getColumn() != null) {
            column = getModel().getSplit().getColumn();
        }
//        figure.setColumn(column);
        figure.setSplitColumn(column);
        
        String predictor = getModel().getTree().getConfig().getTargetColumn();
        figure.setPredictor(predictor);
        
        int ordinal = -1;
        for(LSColumn col : getModel().getTree().getConfig().getColumns()) {
            if(col.getName().equals(predictor)) {
                ordinal = col.getOrdinal();
                break;
            }
        }
        
        boolean hasCount = false;
        boolean hasStats = false;
        boolean countAsPercent = false;
        long rowCount = -1;
        long rootCount = -1;
        
        // first try to find stats for this particular node
        // set the values on the figure if we find stats (other than the row count)
        if(getModel().getStats() != null) {
            LSStats stats = getModel().getStats();
            rowCount = stats.getRowCount();
            if(rowCount >= 0) {
                hasCount = true;
            }
//            if(stats.getRowCount() >= 0) {
//                hasCount = true;
//                figure.setCount(converter.formatForDisplay(stats.getRowCount()));
//            }
            for(LSColumnStats col : stats.getColumnStats()) {
                if(col.getColumnOrdinal() == ordinal) {
                    hasStats = true;
                    figure.setMin(reformatNumber(col.getMin()));
                    figure.setMax(reformatNumber(col.getMax()));
                    figure.setMean(reformatNumber(col.getAverage()));
                    figure.setStdDev(reformatNumber(col.getStandardDeviation()));
                    figure.setSum(reformatNumber(col.getSum()));
                    figure.setDistinct(converter.formatForDisplay(col.getDistinctCount()));
                    break;
                }
            }
        }
        
        // now determine how we want to display the row count
        DisplayExtension ext = DisplayExtension.getDisplayExtension(
                getModel().getTree().getConfig(), "countsAsPercents", false);
        countAsPercent = ext != null && ext.getBoolean(false);
        
        // if we want to display the count as a percent, or we don't have a count, we'll have
        // to walk up the tree
        if(countAsPercent || rowCount < 0) {
            // now go through the ancestors in the tree to find the root node's count,
            // and if we couldn't find an exact count for this node try to find an upper bound
            LSNode node = getModel();
            while(node != null /*&& node.getConnector() != null && node.getConnector().getSource() != null*/) {
//                LSNode parent = node.getConnector().getSource();
                
                if(node.getStats() != null && node.getStats().getRowCount() >= 0 && rowCount < 0) {
                    rowCount = node.getStats().getRowCount();
                    if(rowCount >= 0 && !countAsPercent) {
                        // we can stop if we found an upper bound and we don't need to display a percent
                        break;
                    }
                }
                if(node.getConnector() == null && node.getStats() != null) {
                    rootCount = node.getStats().getRowCount();
                }
                if(node.getConnector() != null) {
                    node = node.getConnector().getSource();
                } else {
                    node = null;
                }
            }
        }
        
        // now set the row count on the figure however the user wants it (i.e.: as a value or percent)
        if(countAsPercent) {
            String value = "";
            if(getModel().getConnector() == null) {
                // root node is always 100%
                value = "100%";
            } else {
                if(rootCount >= 0 && rowCount >= 0) {
                    value += PERCENT_FORMAT.format(rowCount * 1.0 / rootCount);
                }
                if(value.length() == 0) {
                    value = "100%";
                }
                if(!hasCount || rootCount < 0) {
                    value = "<=" + value;
                }
            }
            figure.setCount(value);
        } else {
            String value = "";
            if(rowCount >= 0) {
                value = converter.formatForDisplay(rowCount) + " rows";
            }
            if(!hasCount && value.length() > 0) {
                value = "<=" + value;
            }
            if(value.length() > 0) {
                figure.setCount(value);
            } else {
                figure.setCount("N/A");
            }
        }
        
//        if(!hasCount) {
//            figure.setCount("N/A");
//            
//            LSNode node = getModel();
//            
//            while(node != null && node.getConnector() != null && node.getConnector().getSource() != null) {
//                LSNode parent = node.getConnector().getSource();
//                
//                if(parent.getStats() != null && parent.getStats().getRowCount() >= 0) {
//                    figure.setCount("<=" + converter.formatForDisplay(parent.getStats().getRowCount()));
//                }
//                node = parent;
//            }
//        }
        
        if(!hasStats) {
            figure.setMin("");
            figure.setMax("");
            figure.setMean("");
            figure.setStdDev("");
            figure.setSum("");
            if(rowCount >= 0) {
                figure.setDistinct("<=" + converter.formatForDisplay(rowCount));
            } else {
                figure.setDistinct("");
            }
//            if(hasCount) {
//                figure.setDistinct("<=" + figure.getCount());
//            } else if(figure.getCount().startsWith("<=")) {
//                figure.setDistinct(figure.getCount());
//            } else {
//                figure.setDistinct("");
//            }
        }
        
        boolean locked = getModel().isLocked();
        if(locked) { // TODO choose icon to match the current state better
//            System.out.println(getModel() + " is locked");
            figure.setState(SelectorNodeFigure.State.WORKING);
        } else if(hasCount && hasStats) {
//            System.out.println(getModel() + " is complete");
            figure.setState(SelectorNodeFigure.State.COMPLETE);
        } else {
//            System.out.println(getModel() + " is incomplete");
            figure.setState(SelectorNodeFigure.State.INCOMPLETE);
        }
    }
    
    protected String reformatNumber(String number) {
        try {
            return converter.formatForDisplay(converter.parse(number, BigDecimal.class.getName()));
        } catch(Exception e) {
            // may throw an exception if it wasn't actually a number
            return number;
        }
    }
    
    @Override
    protected IFigure createFigure() {
        SelectorNodeFigure figure = new SelectorNodeFigure(
                (AnimationManagerFactory) getViewer().getProperty("animationManagerFactory"),
                (ImageCache) getViewer().getProperty("imageCache"),
                (ColorCache) getViewer().getProperty("colorCache"),
                (FontCache) getViewer().getProperty("fontCache"));
        return figure;
    }
    
    protected String getValueString(LSNode node) {
        String values = "root";
        if(getModel().isRemainderNode()) {
            values = "remainder";
        } else if(getModel().getConditions().size() > 0) {
            values = "";
            for(LSCondition cond : getModel().getConditions()) {
                if(values.length() > 0) {
                    values += ", ";
                }
                String value = "";
                for(LSWhere where : cond.getWhereClauses()) {
                    if(value.length() > 0) {
                        value += " AND ";
                    }
                    value += where.getMatch().getDisplayString() + converter.formatForDisplay(where.getValue());
                }
                values += value;
            }
        }
        return values;
    }
    
//    @Override
//    public SelectorNodeFigure getFigure() {
//        return (SelectorNodeFigure) super.getFigure();
//    }
    
    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
        return new WeightedAnchor(0.5, 1.0);
    }
    
    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        return new WeightedAnchor(0.5, 1.0);
    }
    
    public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
        return new WeightedAnchor(0.5, 0.0);
    }
    
    public ConnectionAnchor getTargetConnectionAnchor(Request request) {
        return new WeightedAnchor(0.5, 0.0);
    }
    
    class WeightedAnchor extends AbstractConnectionAnchor {
        private double xweight;
        private double yweight;
        
        public WeightedAnchor(double xweight, double yweight) {
            this.xweight = xweight;
            this.yweight = yweight;
        }
        
        @Override
        public IFigure getOwner() {
            return getFigure();
        }
        
        public Point getLocation(Point reference) {
            Rectangle r = Rectangle.SINGLETON;
            r.setBounds(getOwner().getBounds());
            r.translate(-1, -1);
            r.resize(1, 1);
            
            getOwner().translateToAbsolute(r);
            return new Point(r.x + xweight * r.width, r.y + yweight * r.height);
        }
    }
}
