package com.nexusbpm.dataselector.drools.model;

import java.util.Map.Entry;

import org.dom4j.Element;

import com.nexusbpm.dataselector.model.AbstractModelElement;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.model.persistance.ModelExtensionPersistance;

public class DroolsExtensionPersistance implements ModelExtensionPersistance {
    public void parseExtensions(AbstractModelElement model, Element element) {
        if(model instanceof LSTree) {
            LSTree tree = (LSTree) model;
            Element child = element.element(PropertyList.ELEMENT_KEY);
            if(child != null) {
                PropertyList list = PropertyList.get(tree, true);
                for(Object o : child.elements()) {
                    Element e = (Element) o;
                    list.getValues().add(e.getText());
                }
                list.fireValuesChanged();
            }
        } else if(model instanceof LSNode) {
            LSNode node = (LSNode) model;
            Element child = element.element(PropertyMap.ELEMENT_KEY);
            if(child != null) {
                PropertyMap map = PropertyMap.get(node, true);
                for(Object o : child.elements()) {
                    Element entry = (Element) o;
                    Element key = entry.element("key");
                    Element value = entry.element("value");
                    map.getValues().put(key.getText(), value.getText());
                }
                map.fireValuesChanged();
            }
        }
    }
    public void writeExtensions(AbstractModelElement model, Element element) {
        if(model instanceof LSTree) {
            PropertyList list = PropertyList.get((LSTree) model, false);
            if(list != null) {
                Element child = element.addElement(PropertyList.ELEMENT_KEY);
                for(String value : list.getValues()) {
                    Element e = child.addElement("value");
                    e.addCDATA(value);
                }
            }
        } else if(model instanceof LSNode) {
            PropertyMap map = PropertyMap.get((LSNode) model, false);
            if(map != null) {
                Element child = element.addElement(PropertyMap.ELEMENT_KEY);
                for(Entry<String, String> entry : map.getValues().entrySet()) {
                    Element entryElement = child.addElement("entry");
                    Element keyElement = entryElement.addElement("key");
                    keyElement.addCDATA(entry.getKey());
                    Element valueElement = entryElement.addElement("value");
                    valueElement.addCDATA(entry.getValue());
                }
            }
        }
    }
}
