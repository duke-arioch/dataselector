package com.nexusbpm.dataselector.properties;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import com.nexusbpm.dataselector.controller.NodeController;
import com.nexusbpm.dataselector.util.ColumnTableUtil;

public class StatisticsPropertySection extends AbstractPropertySection {
    private Table table;
    private TableViewer viewer;
    
    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
        super.createControls(parent, tabbedPropertySheetPage);
        
        table = new Table(parent, SWT.SINGLE | SWT.FULL_SELECTION);
        table.addMouseListener(new MouseListener() {
            public void mouseDoubleClick(MouseEvent e) {
            }
            public void mouseDown(MouseEvent e) {
                if(table.getItem(new Point(e.x, e.y)) == null) table.deselectAll();
            }
            public void mouseUp(MouseEvent e) {
            }
        });
        viewer = new TableViewer(table);
        
        ColumnTableUtil.adapt(viewer);
    }
    
    @Override
    public void dispose() {
        if(table != null && !table.isDisposed()) {
            table.dispose();
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
}
