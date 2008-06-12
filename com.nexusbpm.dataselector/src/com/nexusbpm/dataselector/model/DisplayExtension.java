package com.nexusbpm.dataselector.model;

import com.nexusbpm.dataselector.Plugin;

public class DisplayExtension extends AbstractPropertyExtension {
    public static final String PROPERTY_EXTENSION_VALUE = "displayExtensionValue";
    public static final String ELEMENT_KEY = "displayExtension";
    
    public DisplayExtension(AbstractModelElement node, String name) {
        super(node, name);
    }
    
    @Override
    protected String getExtensionValuePropertyName() {
        return PROPERTY_EXTENSION_VALUE;
    }
    
    public static DisplayExtension getDisplayExtension(
            AbstractModelElement element, String name, boolean create) {
        return getExtension(element, Plugin.PLUGIN_ID, ELEMENT_KEY, name, DisplayExtension.class, create);
    }
    
    public static String getDisplayExtensionText(
            AbstractModelElement element, String name, String defaultText) {
        DisplayExtension extension = getDisplayExtension(element, name, false);
        String value = defaultText;
        if(extension != null) {
            value = extension.getText();
        }
        return value;
    }
}
