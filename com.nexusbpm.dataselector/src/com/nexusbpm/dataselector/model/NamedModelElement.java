package com.nexusbpm.dataselector.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.nexusbpm.dataselector.commands.ChangePropertyCommand;
import com.nexusbpm.dataselector.events.ExceptionEvent;
import com.nexusbpm.dataselector.requests.ExecuteCommandRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;
import com.nexusbpm.multipage.bus.UnhandledRequestException;

public abstract class NamedModelElement extends AbstractModelElement implements IAdaptable, IPropertySource {
    public static final String PROPERTY_ELEMENT_NAME = "elementName";
    
    private static IPropertyDescriptor[] DESCRIPTORS;
    
    private String name;
    
    public NamedModelElement(AbstractModelElement parent) {
        super(parent);
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        firePropertyChange(PROPERTY_ELEMENT_NAME, oldName, name);
    }
    
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class key) {
        if(IPropertySource.class.equals(key)) {
            return this;
        } else {
            return Platform.getAdapterManager().getAdapter(this, key);
        }
    }
    
    public Object getEditableValue() {
        return this;
    }
    
    public IPropertyDescriptor[] getPropertyDescriptors() {
        if(DESCRIPTORS == null) {
            DESCRIPTORS = new IPropertyDescriptor[1];
            DESCRIPTORS[0] = createTextPropertyDescriptor(
                    PROPERTY_ELEMENT_NAME, "Name",
                    "The name of the selected element",
                    null, null, false);
        }
        return DESCRIPTORS;
    }
    
    protected IPropertyDescriptor createTextPropertyDescriptor(
            Object id,
            String displayName,
            String description,
            String category,
            ICellEditorValidator validator,
            boolean alwaysIncompatible) {
        TextPropertyDescriptor descriptor = new TextPropertyDescriptor(id, displayName);
        descriptor.setCategory(category);
        descriptor.setDescription(description);
        descriptor.setValidator(validator);
        descriptor.setAlwaysIncompatible(alwaysIncompatible);
        return descriptor;
    }
    
    public Object getPropertyValue(Object id) {
        if(id.equals(PROPERTY_ELEMENT_NAME)) {
            return getName() != null ? getName() : "";
        } else {
            return null;
        }
    }
    
    public boolean isPropertySet(Object id) {
        if(id.equals(PROPERTY_ELEMENT_NAME)) {
            return getName() != null && getName().length() > 0;
        } else {
            return false;
        }
    }
    
    public void resetPropertyValue(Object id) {
        if(id.equals(PROPERTY_ELEMENT_NAME)) {
            setName(null);
        }
    }
    
    public boolean isPropertyValueEqual(Object id, Object value) {
        Object oldValue = getPropertyValue(id);
        if(oldValue == value) {
            return true;
        } else if(oldValue != null && value != null) {
            return value.equals(oldValue);
        } else {
            return false;
        }
    }
    
    public void setPropertyValue(Object id, Object value) {
        if(isPropertyValueEqual(id, value)) {
            return;
        }
        try {
            EventRequestBus bus = getTree().getEventRequestBus();
            bus.handleRequest(new ExecuteCommandRequest(
                    new ChangePropertyCommand(bus, this, id, value)));
        } catch(UnhandledRequestException e) {
            getTree().getEventRequestBus().handleEvent(new ExceptionEvent("bus not configured", e));
        }
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + name + "@" + hashCode();
    }
}
