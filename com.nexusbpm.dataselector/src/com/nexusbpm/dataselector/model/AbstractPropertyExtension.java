package com.nexusbpm.dataselector.model;

import com.nexusbpm.dataselector.Plugin;

/**
 * Provides a base implementation of a Model Extension that can hold
 * key/value property pairs.
 */
public abstract class AbstractPropertyExtension extends AbstractModelExtension {
    private String text;
    
    public AbstractPropertyExtension(AbstractModelElement node, String name) {
        super(node);
        super.setName(name);
    }
    
    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Cannot change the name of a property extension");
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        String oldText = this.text;
        this.text = text;
        System.out.println("Display extension " + getName() + " (" + oldText + " -> " + text + ")");
        firePropertyChange(getExtensionValuePropertyName(), oldText, text);
    }
    
    /** Returns the name of the property that is used for events when this extension's value changes. */
    protected abstract String getExtensionValuePropertyName();
    
    public int getInt() throws NumberFormatException {
        return Integer.parseInt(String.valueOf(text));
    }
    
    public int getInt(int defaultValue) {
        try {
            return getInt();
        } catch(NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public void setInt(int value) {
        setText(String.valueOf(value));
    }
    
    public double getDouble() throws NumberFormatException {
        return Double.parseDouble(String.valueOf(text));
    }
    
    public double getDouble(double defaultValue) {
        try {
            return getDouble();
        } catch(NumberFormatException e) {
            return -1.0;
        }
    }
    
    public void setDouble(double value) {
        setText(String.valueOf(value));
    }
    
    public boolean getBoolean() {
        return Boolean.parseBoolean(text);
    }
    
    public boolean getBoolean(boolean defaultValue) {
        if(Boolean.TRUE.toString().equals(String.valueOf(text))) {
            return true;
        } else if(Boolean.FALSE.toString().equals(String.valueOf(text))) {
            return false;
        } else {
            return defaultValue;
        }
    }
    
    public void setBoolean(boolean value) {
        setText(String.valueOf(value));
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends AbstractPropertyExtension> T getExtension(
            AbstractModelElement element,
            String contributorId,
            String elementKey,
            String propertyName,
            Class<T> extensionClass,
            boolean create) {
        T extension = null;
        if(element != null) {
            extension = (T) element.getExtension(contributorId, elementKey + "." + propertyName);
            if(extension == null && create) {
                try {
                    extension = extensionClass.getConstructor(AbstractModelElement.class, String.class).newInstance(element, propertyName);
                } catch(Exception e) {
                    throw new IllegalArgumentException("Error creating extension", e);
                }
                element.setExtension(Plugin.PLUGIN_ID, elementKey + "." + propertyName, extension);
            }
        }
        return extension;
    }
    
    public static void removeExtension(
            AbstractModelElement element,
            String contributorId,
            String elementKey,
            String propertyName) {
        if(element != null) {
            element.removeExtension(contributorId, elementKey + "." + propertyName);
        }
    }
}
