package com.nexusbpm.dataselector.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import com.nexusbpm.dataselector.Plugin;
import com.nexusbpm.dataselector.commands.ChangePropertyCommand;
import com.nexusbpm.dataselector.model.config.LSConfig;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class LSTree extends NamedModelElement {
    public static final String PROPERTY_TREE_CONFIG = "treeConfig";
    public static final String PROPERTY_TREE_STATE = "treeState";
    public static final String PROPERTY_ROOT_NODE = "rootNode";
    public static final String PROPERTY_NODE_ADDED = "nodeAdded";
    public static final String PROPERTY_NODE_REMOVED = "nodeRemoved";
//    public static final String PROPERTY_NODES_REMOVED = "nodesRemoved";
    
    private static IPropertyDescriptor[] DESCRIPTORS;
    
    private LSConfig config;
    private State state;
    private LSNode root;
    private Set<LSNode> nodes;
    private List<LSNode> nodeList;
    
    private EventRequestBus bus;
    
    public LSTree() {
        super(null);
        state = State.CONFIG_DATASOURCE;
        nodes = new HashSet<LSNode>();
        nodeList = new ArrayList<LSNode>();
    }
    
    public void setEventRequestBus(EventRequestBus bus) {
        this.bus = bus;
    }
    
    public EventRequestBus getEventRequestBus() {
        return bus;
    }
    
    public LSConfig getConfig() {
        return config;
    }
    
    public void setConfig(LSConfig config) {
        LSConfig oldConfig = this.config;
        this.config = config;
        firePropertyChange(PROPERTY_TREE_CONFIG, oldConfig, config);
    }
    
    public State getState() {
        return state;
    }
    
    public void setState(State state) {
        State oldState = this.state;
        this.state = state;
        firePropertyChange(PROPERTY_TREE_STATE, oldState, state);
    }
    
    public LSNode getRoot() {
        return root;
    }
    
    public void setRoot(LSNode root) {
        // TODO how do we want to handle changing the root when it was already set before?
        LSNode oldRoot = this.root;
        this.root = root;
        firePropertyChange(PROPERTY_ROOT_NODE, oldRoot, root);
    }
    
    protected void initNodeList() {
        if(nodeList.size() == 0 && nodes.size() > 0 && root != null) {
            nodeList.add(root);
            for(int index = 0; index < nodeList.size(); index++) {
                LSNode node = nodeList.get(index);
                for(LSNode child : node.getSubNodes()) {
                    nodeList.add(child);
                }
            }
        }
    }
    
    public Set<LSNode> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }
    
    public void addNode(LSNode node) {
        nodes.add(node);
        nodeList.clear();
        firePropertyChange(PROPERTY_NODE_ADDED, null, node);
    }
    
    public void removeNode(LSNode node) {
        nodes.remove(node);
        nodeList.clear();
        firePropertyChange(PROPERTY_NODE_REMOVED, node, null);
    }
    
    public int getNodeIndex(LSNode node) {
        initNodeList();
        return nodeList.indexOf(node);
    }
    
    @Override
    public LSTree getTree() {
        return this;
    }
    
    public enum State {
        CONFIG_DATASOURCE,
        CONFIG_COLUMNS,
        SPLIT_TREE;
    }
    
    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        if(DESCRIPTORS == null) {
            IPropertyDescriptor[] parentDescriptors = super.getPropertyDescriptors();
            int offset = parentDescriptors.length;
            DESCRIPTORS = new IPropertyDescriptor[offset + 4];
            for(int index = 0; index < parentDescriptors.length; index++) {
                DESCRIPTORS[index] = parentDescriptors[index];
            }
            String dlCategory = "Automatic Downloading";
            String displayCategory = "Display";
            DESCRIPTORS[offset] = createComboBoxPropertyDescriptor(
                    LSConfig.PROPERTY_AUTO_DOWNLOAD_STATS,
                    "Stats",
                    "Automatically download statistics for nodes",
                    dlCategory,
                    ChangePropertyCommand.BOOLEAN_LABELS);
            DESCRIPTORS[offset + 1] = createComboBoxPropertyDescriptor(
                    LSConfig.PROPERTY_AUTO_DOWNLOAD_CATEGORICAL_SPLITS,
                    "Categorical Splits",
                    "Automatically download categorical splits for " +
                    "columns with a small number of distinct values",
                    dlCategory,
                    ChangePropertyCommand.BOOLEAN_LABELS);
            DESCRIPTORS[offset + 2] = createComboBoxPropertyDescriptor(
                    DisplayExtension.ELEMENT_KEY + ".showGrid",
                    "Show Grid", "", displayCategory, ChangePropertyCommand.BOOLEAN_LABELS);
            DESCRIPTORS[offset + 3] = createComboBoxPropertyDescriptor(
                    DisplayExtension.ELEMENT_KEY + ".countsAsPercents",
                    "Row Counts as Percents",
                    "On the Nodes display the row counts as a percentage " +
                    "of the whole instead of as a number",
                    displayCategory,
                    ChangePropertyCommand.BOOLEAN_LABELS);
        }
        return DESCRIPTORS;
    }
    
    protected IPropertyDescriptor createComboBoxPropertyDescriptor(
            Object id,
            String displayName,
            String description,
            String category,
            String[] labels) {
        ComboBoxPropertyDescriptor descriptor = new ComboBoxPropertyDescriptor(id, displayName, labels);
        descriptor.setCategory(category);
        descriptor.setDescription(description);
        return descriptor;
    }
    
    @Override
    public Object getPropertyValue(Object id) {
        if(id.equals(LSConfig.PROPERTY_AUTO_DOWNLOAD_STATS)) {
            return ChangePropertyCommand.getBooleanIndex(getConfig().isAutoDownloadStats());
        } else if(id.equals(LSConfig.PROPERTY_AUTO_DOWNLOAD_CATEGORICAL_SPLITS)) {
            return ChangePropertyCommand.getBooleanIndex(getConfig().isAutoDownloadCategoricalSplits());
        } else if(id.equals(DisplayExtension.ELEMENT_KEY + ".showGrid")) {
            DisplayExtension ext = DisplayExtension.getDisplayExtension(getConfig(), "showGrid", false);
            return ChangePropertyCommand.getBooleanIndex(ext != null && ext.getBoolean(true));
        } else if(id.equals(DisplayExtension.ELEMENT_KEY + ".countsAsPercents")) {
            DisplayExtension ext = DisplayExtension.getDisplayExtension(getConfig(), "countsAsPercents", false);
            return ChangePropertyCommand.getBooleanIndex(ext != null && ext.getBoolean(false));
        } else {
            return super.getPropertyValue(id);
        }
    }
    
    @Override
    public boolean isPropertySet(Object id) {
        if(id.equals(LSConfig.PROPERTY_AUTO_DOWNLOAD_STATS)) {
            return getConfig().isAutoDownloadStats();
        } else if(id.equals(LSConfig.PROPERTY_AUTO_DOWNLOAD_CATEGORICAL_SPLITS)) {
            return getConfig().isAutoDownloadCategoricalSplits();
        } else if(id.equals(DisplayExtension.ELEMENT_KEY + ".showGrid")) {
            DisplayExtension ext = DisplayExtension.getDisplayExtension(this, "showGrid", true);
            return ext != null && ext.getBoolean(true);
        } else if(id.equals(DisplayExtension.ELEMENT_KEY + ".countsAsPercents")) {
            DisplayExtension ext = DisplayExtension.getDisplayExtension(this, "countsAsPercents", false);
            return ext != null && ext.getBoolean(false);
        } else {
            return super.isPropertySet(id);
        }
    }
    
    @Override
    public void resetPropertyValue(Object id) {
        if(id.equals(LSConfig.PROPERTY_AUTO_DOWNLOAD_STATS)) {
            getConfig().setAutoDownloadStats(false);
        } else if(id.equals(LSConfig.PROPERTY_AUTO_DOWNLOAD_CATEGORICAL_SPLITS)) {
            getConfig().setAutoDownloadCategoricalSplits(false);
        } else if(id.equals(DisplayExtension.ELEMENT_KEY + ".showGrid")) {
            removeExtension(Plugin.PLUGIN_ID, DisplayExtension.ELEMENT_KEY + ".showGrid");
        } else if(id.equals(DisplayExtension.ELEMENT_KEY + ".countsAsPercents")) {
            removeExtension(Plugin.PLUGIN_ID, DisplayExtension.ELEMENT_KEY + ".countsAsPercents");
        } else {
            super.resetPropertyValue(id);
        }
    }
    
//    @Override
//    public void setPropertyValue(Object id, Object value) {
//        if(isPropertyValueEqual(id, value)) {
//            return;
//        }
//        if(id.equals(LSConfig.PROPERTY_AUTO_DOWNLOAD_STATS)) {
//            getConfig().setAutoDownloadStats(
//                    Boolean.parseBoolean(BOOLEAN_LABELS[((Integer) value).intValue()]));
//        } else if(id.equals(LSConfig.PROPERTY_AUTO_DOWNLOAD_CATEGORICAL_SPLITS)) {
//            getConfig().setAutoDownloadCategoricalSplits(
//                    Boolean.parseBoolean(BOOLEAN_LABELS[((Integer) value).intValue()]));
//        } else if(id.equals(DisplayExtension.ELEMENT_KEY + ".showGrid")) {
//            DisplayExtension ext = DisplayExtension.getDisplayExtension(this, "showGrid", true);
//            ext.setBoolean(Boolean.parseBoolean(BOOLEAN_LABELS[((Integer) value).intValue()]));
//        } else {
//            super.setPropertyValue(id, value);
//        }
//    }
}
