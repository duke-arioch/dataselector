package com.nexusbpm.dataselector.properties;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import com.nexusbpm.dataselector.controller.NodeController;
import com.nexusbpm.dataselector.model.LSCondition;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSWhere;

public class NodeConditionsSection extends AbstractPropertySection {
    private List list;
    private ListViewer viewer;
    
    public NodeConditionsSection() {
    }
    
    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
        super.createControls(parent, tabbedPropertySheetPage);
        
        list = new List(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        viewer = new ListViewer(list);
        
        viewer.setLabelProvider(new ConditionLabelProvider());
        viewer.setContentProvider(new ConditionContentProvider());
    }
    
    @Override
    public void dispose() {
        if(list != null && !list.isDisposed()) {
            list.dispose();
        }
    }
    
    @Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
        Object input = null;
        if(selection instanceof IStructuredSelection) {
            Object[] sel = ((IStructuredSelection) selection).toArray();
            if(sel.length == 1 && sel[0] instanceof NodeController) {
                NodeController controller = (NodeController) sel[0];
                input = controller.getModel();
            }
        }
        viewer.setInput(input);
    }
    
    @Override
    public void refresh() {
        viewer.refresh();
    }
    
    @Override
    public boolean shouldUseExtraSpace() {
        return true;
    }
    
    protected class ConditionLabelProvider implements ILabelProvider {
        public void addListener(ILabelProviderListener listener) {
        }
        public void removeListener(ILabelProviderListener listener) {
        }
        public Image getImage(Object element) {
            return null;
        }
        public String getText(Object element) {
            LSCondition condition = (LSCondition) element;
            StringBuilder b = new StringBuilder();
            if(condition.isRemainderCondition()) {
                b.append("remainder");
            } else {
                for(LSWhere where : condition.getWhereClauses()) {
                    if(b.length() > Short.MAX_VALUE * 2) {
                        break;
                    }
                    if(b.length() > 0) {
                        b.append(" AND ");
                    }
                    b.append(where.getMatch().getDisplayString());
                    b.append(' ');
                    b.append(where.getValue());
                }
            }
            return b.substring(0, Math.min(b.length(), Short.MAX_VALUE * 2));
        }
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }
        public void dispose() {
        }
    }
    
    protected class ConditionContentProvider implements IStructuredContentProvider {
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
        public Object[] getElements(Object inputElement) {
            LSNode node = (LSNode) inputElement;
            return node.getConditions().toArray();
        }
        public void dispose() {
        }
    }
}
