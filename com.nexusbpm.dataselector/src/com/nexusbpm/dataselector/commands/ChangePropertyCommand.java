package com.nexusbpm.dataselector.commands;

import com.nexusbpm.dataselector.controller.GraphUpdateQueue;
import com.nexusbpm.dataselector.model.AbstractModelElement;
import com.nexusbpm.dataselector.model.DisplayExtension;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.model.NamedModelElement;
import com.nexusbpm.dataselector.model.config.LSConfig;
import com.nexusbpm.dataselector.requests.SetDirtyRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class ChangePropertyCommand extends AbstractGraphUpdateCommand {
    private AbstractModelElement object;
    private Object propertyIdentifier;
    private Object value;
    private Object originalValue;
    private LSTree tree;
    
    public static final String[] BOOLEAN_LABELS = {
        String.valueOf(Boolean.TRUE),
        String.valueOf(Boolean.FALSE)
    };
    
    public ChangePropertyCommand(EventRequestBus bus, AbstractModelElement object, Object propertyIdentifier, Object value) {
        super("Change property '" + propertyIdentifier + "'", bus);
        this.object = object;
        this.propertyIdentifier = propertyIdentifier;
        this.value = value;
        tree = object.getTree();
    }
    
    @Override
    public boolean canExecute() {
        if(object == null || propertyIdentifier == null || tree == null) {
            return false;
        }
        if(object instanceof LSTree) {
            if(LSConfig.PROPERTY_AUTO_DOWNLOAD_STATS.equals(propertyIdentifier) ||
                    LSConfig.PROPERTY_AUTO_DOWNLOAD_CATEGORICAL_SPLITS.equals(propertyIdentifier) ||
                    propertyIdentifier.equals(DisplayExtension.ELEMENT_KEY + ".showGrid") ||
                    propertyIdentifier.equals(DisplayExtension.ELEMENT_KEY + ".countsAsPercents")) {
                return isIntValueInRange(value, Integer.valueOf(0), Integer.valueOf(BOOLEAN_LABELS.length - 1));
            }
        }
        if(object instanceof LSNode) {
            if(LSNode.PROPERTY_NODE_BOUNDS_X.equals(propertyIdentifier) ||
                    LSNode.PROPERTY_NODE_BOUNDS_Y.equals(propertyIdentifier)) {
                return isIntValueInRange(value, null, null);
            } else if(LSNode.PROPERTY_NODE_BOUNDS_WIDTH.equals(propertyIdentifier) ||
                    LSNode.PROPERTY_NODE_BOUNDS_HEIGHT.equals(propertyIdentifier)) {
                return isIntValueInRange(value, Integer.valueOf(1), null);
            }
        }
        if(object instanceof NamedModelElement) {
            if(NamedModelElement.PROPERTY_ELEMENT_NAME.equals(propertyIdentifier)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void execute() {
        if(propertyIdentifier.equals(LSNode.PROPERTY_NODE_BOUNDS_X)) {
            originalValue = Integer.valueOf(((LSNode) object).getX());
        } else if(propertyIdentifier.equals(LSNode.PROPERTY_NODE_BOUNDS_Y)) {
            originalValue = Integer.valueOf(((LSNode) object).getY());
        } else if(propertyIdentifier.equals(LSNode.PROPERTY_NODE_BOUNDS_WIDTH)) {
            originalValue = Integer.valueOf(((LSNode) object).getWidth());
        } else if(propertyIdentifier.equals(LSNode.PROPERTY_NODE_BOUNDS_HEIGHT)) {
            originalValue = Integer.valueOf(((LSNode) object).getHeight());
        } else if(propertyIdentifier.equals(NamedModelElement.PROPERTY_ELEMENT_NAME)) {
            originalValue = ((NamedModelElement) object).getName();
        } else if(propertyIdentifier.equals(LSConfig.PROPERTY_AUTO_DOWNLOAD_STATS)) {
            originalValue = getBooleanIndex(((LSTree) object).getConfig().isAutoDownloadStats());
        } else if(propertyIdentifier.equals(LSConfig.PROPERTY_AUTO_DOWNLOAD_CATEGORICAL_SPLITS)) {
            originalValue = getBooleanIndex(((LSTree) object).getConfig().isAutoDownloadCategoricalSplits());
        } else if(propertyIdentifier.equals(DisplayExtension.ELEMENT_KEY + ".showGrid")) {
            originalValue = getBooleanIndex(DisplayExtension.getDisplayExtensionText(
                    ((LSTree) object).getConfig(), "showGrid", Boolean.FALSE.toString()));
        } else if(propertyIdentifier.equals(DisplayExtension.ELEMENT_KEY + ".countsAsPercents")) {
            originalValue = getBooleanIndex(DisplayExtension.getDisplayExtensionText(
                    ((LSTree) object).getConfig(), "countsAsPercents", Boolean.FALSE.toString()));
        }
        redo();
    }
    
    @Override
    public boolean canUndo() {
        return true;
    }
    
    @Override
    public void undo() {
        GraphUpdateQueue queue = getGraphUpdateQueue();
        queue.startNonFlushingOperation();
        try {
            setValue(originalValue);
        } finally {
            queue.endNonFlushingOperation();
        }
    }
    
    @Override
    public void redo() {
        GraphUpdateQueue queue = getGraphUpdateQueue();
        queue.startNonFlushingOperation();
        try {
            setValue(value);
        } finally {
            queue.endNonFlushingOperation();
        }
    }
    
    protected void setValue(Object value) {
        synchronized(tree) {
            if(propertyIdentifier.equals(LSNode.PROPERTY_NODE_BOUNDS_X)) {
                ((LSNode) object).setX(Integer.parseInt(String.valueOf(value)));
            } else if(propertyIdentifier.equals(LSNode.PROPERTY_NODE_BOUNDS_Y)) {
                ((LSNode) object).setY(Integer.parseInt(String.valueOf(value)));
            } else if(propertyIdentifier.equals(LSNode.PROPERTY_NODE_BOUNDS_WIDTH)) {
                ((LSNode) object).setWidth(Integer.parseInt(String.valueOf(value)));
            } else if(propertyIdentifier.equals(LSNode.PROPERTY_NODE_BOUNDS_HEIGHT)) {
                ((LSNode) object).setHeight(Integer.parseInt(String.valueOf(value)));
            } else if(propertyIdentifier.equals(NamedModelElement.PROPERTY_ELEMENT_NAME)) {
                ((NamedModelElement) object).setName((String) value);
            } else if(propertyIdentifier.equals(LSConfig.PROPERTY_AUTO_DOWNLOAD_STATS)) {
                ((LSTree) object).getConfig().setAutoDownloadStats(getBooleanFromIndex((Integer) value));
            } else if(propertyIdentifier.equals(LSConfig.PROPERTY_AUTO_DOWNLOAD_CATEGORICAL_SPLITS)) {
                ((LSTree) object).getConfig().setAutoDownloadCategoricalSplits(getBooleanFromIndex((Integer) value));
            } else if(propertyIdentifier.equals(DisplayExtension.ELEMENT_KEY + ".showGrid")) {
                DisplayExtension ext = DisplayExtension.getDisplayExtension(
                        ((LSTree) object).getConfig(), "showGrid", true);
                ext.setBoolean(getBooleanFromIndex((Integer) value));
            } else if(propertyIdentifier.equals(DisplayExtension.ELEMENT_KEY + ".countsAsPercents")) {
                DisplayExtension ext = DisplayExtension.getDisplayExtension(
                        ((LSTree) object).getConfig(), "countsAsPercents", true);
                ext.setBoolean(getBooleanFromIndex((Integer) value));
            }
            sendRequest(new SetDirtyRequest(true));
        }
    }
    
    protected boolean isIntValueInRange(Object value, Integer minimum, Integer maximum) {
        try {
            int v = Integer.parseInt(String.valueOf(value));
            if(minimum != null && v < minimum.intValue() ||
                    maximum != null && v > maximum.intValue()) {
                return false;
            }
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean getBooleanFromIndex(Integer index) {
        return Boolean.parseBoolean(BOOLEAN_LABELS[index.intValue()]);
    }
    
    public static Integer getBooleanIndex(boolean value) {
        return getBooleanIndex(String.valueOf(Boolean.valueOf(value)));
    }
    
    public static Integer getBooleanIndex(String value) {
        if(value != null) {
            for(int index = 0; index < BOOLEAN_LABELS.length; index++) {
                if(BOOLEAN_LABELS[index].equals(value)) {
                    return Integer.valueOf(index);
                }
            }
        }
        return Integer.valueOf(0);
    }
}
