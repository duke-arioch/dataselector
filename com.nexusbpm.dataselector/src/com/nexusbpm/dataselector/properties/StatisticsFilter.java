package com.nexusbpm.dataselector.properties;

import org.eclipse.jface.viewers.IFilter;

import com.nexusbpm.dataselector.controller.NodeController;

public class StatisticsFilter implements IFilter {
    public boolean select(Object object) {
        return object instanceof NodeController;
    }
}
