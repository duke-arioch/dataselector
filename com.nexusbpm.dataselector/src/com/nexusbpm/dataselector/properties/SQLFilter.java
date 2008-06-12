package com.nexusbpm.dataselector.properties;

import org.eclipse.jface.viewers.IFilter;

import com.nexusbpm.dataselector.controller.NodeController;
import com.nexusbpm.dataselector.controller.TreeController;

public class SQLFilter implements IFilter {
    public boolean select(Object object) {
        return object instanceof NodeController || object instanceof TreeController;
    }
}
