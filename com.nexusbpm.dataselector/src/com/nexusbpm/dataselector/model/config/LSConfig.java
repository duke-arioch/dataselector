package com.nexusbpm.dataselector.model.config;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import com.nexusbpm.dataselector.model.AbstractModelElement;
import com.nexusbpm.dataselector.model.LSTree;

public class LSConfig extends AbstractModelElement {
    public static final String PROPERTY_CONFIG_DRIVER = "configDriver";
    public static final String PROPERTY_CONFIG_CONNECTION = "configConnection";
    public static final String PROPERTY_CONFIG_QUERY = "configQuery";
    public static final String PROPERTY_CONFIG_TARGET_COLUMN = "configTargetColumn";
    public static final String PROPERTY_ADD_CONFIG_COLUMN = "addConfigColumns";
    public static final String PROPERTY_CLEAR_CONFIG_COLUMNS = "clearConfigColumns";
    public static final String PROPERTY_AUTO_DOWNLOAD_STATS = "autoDownloadStats";
    public static final String PROPERTY_AUTO_DOWNLOAD_CATEGORICAL_SPLITS = "autoDownloadCategoricalSplits";
    
    private LSDriver driver;
    private LSConnection connection;
    private String query = "";
    private String targetColumn;
    private Set<LSColumn> columns;
    private boolean autoDownloadStats;
    private boolean autoDownloadCategoricalSplits;
    
    public LSConfig(LSTree tree) {
        super(tree);
        columns = new TreeSet<LSColumn>();
    }
    
    public LSDriver getDriver() {
        return driver;
    }
    
    public void setDriver(LSDriver driver) {
        LSDriver oldDriver = this.driver;
        this.driver = driver;
        firePropertyChange(PROPERTY_CONFIG_DRIVER, oldDriver, driver);
    }
    
    public LSConnection getConnection() {
        return connection;
    }
    
    public void setConnection(LSConnection connection) {
        LSConnection oldConnection = this.connection;
        this.connection = connection;
        firePropertyChange(PROPERTY_CONFIG_CONNECTION, oldConnection, connection);
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        String oldQuery = this.query;
        this.query = query;
        firePropertyChange(PROPERTY_CONFIG_QUERY, oldQuery, query);
    }
    
    public String getTargetColumn() {
        return targetColumn;
    }
    
    public void setTargetColumn(String targetColumn) {
        String oldTargetColumn = this.targetColumn;
        this.targetColumn = targetColumn;
        firePropertyChange(PROPERTY_CONFIG_TARGET_COLUMN, oldTargetColumn, targetColumn);
    }
    
    public Set<LSColumn> getColumns() {
        return Collections.unmodifiableSet(columns);
    }
    
    public void addColumn(LSColumn column) {
        columns.add(column);
        firePropertyChange(PROPERTY_ADD_CONFIG_COLUMN, null, column);
    }
    
    public void clearColumns() {
        Set<LSColumn> oldColumns = new LinkedHashSet<LSColumn>(columns);
        columns.clear();
        firePropertyChange(PROPERTY_CLEAR_CONFIG_COLUMNS, oldColumns, null);
    }
    
    public boolean isAutoDownloadStats() {
        return autoDownloadStats;
    }
    
    public void setAutoDownloadStats(boolean autoDownloadStats) {
        Boolean oldValue = Boolean.valueOf(this.autoDownloadStats);
        this.autoDownloadStats = autoDownloadStats;
        firePropertyChange(PROPERTY_AUTO_DOWNLOAD_STATS, oldValue, Boolean.valueOf(autoDownloadStats));
    }
    
    public boolean isAutoDownloadCategoricalSplits() {
        return autoDownloadCategoricalSplits;
    }
    
    public void setAutoDownloadCategoricalSplits(boolean autoDownloadCategoricalSplits) {
        Boolean oldValue = Boolean.valueOf(this.autoDownloadCategoricalSplits);
        this.autoDownloadCategoricalSplits = autoDownloadCategoricalSplits;
        firePropertyChange(PROPERTY_AUTO_DOWNLOAD_CATEGORICAL_SPLITS, oldValue, Boolean.valueOf(autoDownloadCategoricalSplits));
    }
}
