package com.nexusbpm.dataselector.wizard;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import com.nexusbpm.dataselector.model.config.LSColumn;
import com.nexusbpm.dataselector.util.ColumnTableUtil;
import com.nexusbpm.dataselector.util.RangeSetUtil;

public class SelectPredictorPage extends WizardPage implements ISelectionChangedListener, IDoubleClickListener {
    public static final String PAGE_NAME = "Select Predictor";
    
    private RangeSetUtil rangeSets;
    
    public SelectPredictorPage(RangeSetUtil rangeSets) {
        super(PAGE_NAME);
        this.rangeSets = rangeSets;
        setPageComplete(false);
        setDescription("Select the column on which the split will be performed");
    }
    
    public void createControl(Composite parent) {
        Table table = new Table(parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
        TableViewer viewer = new TableViewer(table);
        
        viewer.addSelectionChangedListener(this);
        viewer.addDoubleClickListener(this);
        ColumnTableUtil.adapt(viewer);
        
        viewer.setInput(rangeSets.getNode());
        
        setControl(table);
        setPageComplete(!viewer.getSelection().isEmpty());
    }
    
    public void selectionChanged(SelectionChangedEvent event) {
        Object o = ((IStructuredSelection) event.getSelection()).getFirstElement();
        LSColumn column = null;
        if(o instanceof LSColumn) {
            column = (LSColumn) o;
        }
        rangeSets.setColumn(column);
        setPageComplete(column != null);
    }
    
    public void doubleClick(DoubleClickEvent event) {
        if(!((IStructuredSelection) event.getSelection()).isEmpty()) {
            getWizard().getContainer().showPage(getWizard().getNextPage(this));
        }
    }
}
