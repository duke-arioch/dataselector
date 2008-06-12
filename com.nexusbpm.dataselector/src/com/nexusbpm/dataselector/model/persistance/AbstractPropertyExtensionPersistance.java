package com.nexusbpm.dataselector.model.persistance;

import java.util.Map.Entry;

import org.dom4j.Element;

import com.nexusbpm.dataselector.model.AbstractModelElement;
import com.nexusbpm.dataselector.model.AbstractModelExtension;
import com.nexusbpm.dataselector.model.AbstractPropertyExtension;

public abstract class AbstractPropertyExtensionPersistance<T extends AbstractPropertyExtension>
        implements ModelExtensionPersistance {
    public AbstractPropertyExtensionPersistance() {
    }
    
    public void parseExtensions(AbstractModelElement model, Element element) {
        for(Object o : element.elements(getExtensionElementName())) {
            Element e = (Element) o;
            String name = e.attributeValue("name");
            String value = e.attributeValue("value");
            T extension = AbstractPropertyExtension.getExtension(
                    model, getContributorId(), getExtensionElementName(), name, getExtensionClass(), true);
            extension.setText(value);
        }
    }
    
    protected abstract String getExtensionElementName();
    protected abstract String getContributorId();
    protected abstract Class<T> getExtensionClass();
    
    @SuppressWarnings("unchecked")
    public void writeExtensions(AbstractModelElement model, Element element) {
        String prefix = getExtensionElementName() + ".";
        for(Entry<String, AbstractModelExtension> entry : model.getExtensionMap(getContributorId()).entrySet()) {
            if(entry.getKey().startsWith(prefix)) {
                T extension = (T) entry.getValue();
                Element e = element.addElement(getExtensionElementName());
                e.addAttribute("name", extension.getName());
                if(extension.getText() != null) {
                    e.addAttribute("value", extension.getText());
                }
            }
        }
    }
}
