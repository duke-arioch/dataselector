package com.nexusbpm.dataselector.properties;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AdvancedPropertySection;

import com.nexusbpm.dataselector.controller.AbstractController;
import com.nexusbpm.dataselector.controller.TreeController;

public class TreePropertiesSection extends AdvancedPropertySection {
    
    public TreePropertiesSection() {
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void setInput(IWorkbenchPart part, ISelection selection) {
        if(selection instanceof IStructuredSelection) {
            Object[] objects = ((IStructuredSelection) selection).toArray();
            TreeController tree = null;
            for(Object object : objects) {
                if(object instanceof AbstractController) {
                    tree = ((AbstractController) object).getTreeController();
                }
            }
            if(tree != null) {
                selection = new StructuredSelection(tree);
            } else {
                selection = StructuredSelection.EMPTY;
            }
        }
        super.setInput(part, selection);
    }
}
