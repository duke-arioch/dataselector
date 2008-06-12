package com.nexusbpm.dataselector.model.persistance;

import org.dom4j.Element;

import com.nexusbpm.dataselector.model.AbstractModelElement;

public interface ModelExtensionPersistance {
    void parseExtensions(AbstractModelElement model, Element element);
    void writeExtensions(AbstractModelElement model, Element element);
}
