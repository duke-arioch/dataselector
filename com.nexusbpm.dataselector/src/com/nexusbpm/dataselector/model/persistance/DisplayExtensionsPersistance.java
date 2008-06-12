package com.nexusbpm.dataselector.model.persistance;

import com.nexusbpm.dataselector.Plugin;
import com.nexusbpm.dataselector.model.DisplayExtension;

public class DisplayExtensionsPersistance extends AbstractPropertyExtensionPersistance<DisplayExtension> {
    public DisplayExtensionsPersistance() {
    }
    
    @Override
    protected String getContributorId() {
        return Plugin.PLUGIN_ID;
    }
    @Override
    protected Class<DisplayExtension> getExtensionClass() {
        return DisplayExtension.class;
    }
    @Override
    protected String getExtensionElementName() {
        return DisplayExtension.ELEMENT_KEY;
    }
    
//    public void parseExtensions(AbstractModelElement model, Element element) {
//        for(Object o : element.elements(DisplayExtension.ELEMENT_KEY)) {
//            Element e = (Element) o;
//            String name = e.attributeValue("name");
//            String value = e.attributeValue("value");
//            DisplayExtension extension = DisplayExtension.getDisplayExtension(model, name, true);
//            extension.setText(value);
//        }
//    }
//    public void writeExtensions(AbstractModelElement model, Element element) {
//        String prefix = DisplayExtension.ELEMENT_KEY + ".";
//        for(Entry<String, AbstractModelExtension> entry : model.getExtensionMap(Plugin.PLUGIN_ID).entrySet()) {
//            if(entry.getKey().startsWith(prefix)) {
//                DisplayExtension extension = (DisplayExtension) entry.getValue();
//                Element e = element.addElement(DisplayExtension.ELEMENT_KEY);
//                e.addAttribute("name", extension.getName());
//                if(extension.getText() != null) {
//                    e.addAttribute("value", extension.getText());
//                }
//            }
//        }
//    }
}
