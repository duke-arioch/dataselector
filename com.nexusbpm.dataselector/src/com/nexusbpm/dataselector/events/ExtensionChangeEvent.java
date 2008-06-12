package com.nexusbpm.dataselector.events;

import java.beans.PropertyChangeEvent;

import com.nexusbpm.dataselector.model.AbstractModelElement;
import com.nexusbpm.dataselector.model.AbstractModelExtension;

public class ExtensionChangeEvent extends PropertyChangeEvent {
    private static final long serialVersionUID = 1l;
    
    private String contributorId;
    private String extensionId;
    
    public ExtensionChangeEvent(
            AbstractModelElement source,
            String propertyName,
            AbstractModelExtension oldValue,
            AbstractModelExtension newValue) {
        this(source, propertyName, oldValue, newValue, null, null);
    }
    
    public ExtensionChangeEvent(
            AbstractModelElement source,
            String propertyName,
            AbstractModelExtension oldValue,
            AbstractModelExtension newValue,
            String contributorId,
            String extensionId) {
        super(source, propertyName, oldValue, newValue);
        this.contributorId = contributorId;
        this.extensionId = extensionId;
    }
    
    @Override
    public AbstractModelExtension getNewValue() {
        return (AbstractModelExtension) super.getNewValue();
    }
    
    @Override
    public AbstractModelExtension getOldValue() {
        return (AbstractModelExtension) super.getOldValue();
    }
    
    @Override
    public AbstractModelElement getSource() {
        return (AbstractModelElement) super.getSource();
    }
    
    public String getContributorId() {
        return contributorId;
    }
    
    public void setContributorId(String contributorId) {
        this.contributorId = contributorId;
    }
    
    public String getExtensionId() {
        return extensionId;
    }
    
    public void setExtensionId(String extensionId) {
        this.extensionId = extensionId;
    }
}
