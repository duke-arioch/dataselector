package com.nexusbpm.dataselector.drools.properties;

import org.eclipse.jface.viewers.IFilter;

import com.nexusbpm.dataselector.controller.NodeController;

public class DroolsFilter implements IFilter {
    public boolean select(Object object) {
        return object instanceof NodeController;
    }
}
