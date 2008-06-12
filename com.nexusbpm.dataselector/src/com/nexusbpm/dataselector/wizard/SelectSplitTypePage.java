package com.nexusbpm.dataselector.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.nexusbpm.database.info.DBInfo;
import com.nexusbpm.dataselector.util.RangeSetUtil;
import com.nexusbpm.dataselector.util.RangeSetUtil.RangeSetListener;

public class SelectSplitTypePage extends WizardPage implements SelectionListener, RangeSetListener {
    public static final String PAGE_NAME = "Select Split Type";
    
    private Button continuousButton;
    private Button categoricalButton;
    private Button nullButton;
    
    private RangeSetUtil rangeSets;
    
    public SelectSplitTypePage(RangeSetUtil rangeSets) {
        super(PAGE_NAME);
        this.rangeSets = rangeSets;
        rangeSets.addListener(this);
        setDescription("Select the type of split to perform");
    }
    
    public void rangeSetChanged(int event, Object oldValue, Object newValue) {
        if((event & RangeSetListener.EVENT_COLUMN_CHANGED) != 0) {
            updateButtons();
        }
    }
    
    public void createControl(Composite container) {
        Composite parent = new Composite(container, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 25;
        layout.marginWidth = 30;
        layout.verticalSpacing = 12;
        layout.horizontalSpacing = 12;
        parent.setLayout(layout);
        
        continuousButton = new Button(parent, SWT.RADIO);
        continuousButton.setText("Split continuous data into intervals based on boundary values");
        continuousButton.setToolTipText(
                "Select this option to split continuous data\n" +
                "(such as numeric or time data) into categories");
        continuousButton.setData(RangeSetUtil.SPLIT_CONTINUOUS);
        continuousButton.addSelectionListener(this);
        
        categoricalButton = new Button(parent, SWT.RADIO);
        categoricalButton.setText("Split categorical data");
        categoricalButton.setToolTipText(
                "Select this option to split the data based on exact value matches.\n" +
                "This is appropriate if the column contains a small number of distinct\n" +
                "values or if you wish to select a particular set of distinct values\n" +
                "out of all possible values for the column.");
        categoricalButton.setData(RangeSetUtil.SPLIT_CATEGORICAL);
        categoricalButton.addSelectionListener(this);
        
        nullButton = new Button(parent, SWT.RADIO);
        nullButton.setText("Split data into NULL and NOT NULL categories");
        nullButton.setToolTipText(
                "Select this option to split the data into two categories (one\n" +
                "category for null values and one category for non-null values)");
        nullButton.setData(RangeSetUtil.SPLIT_NULL);
        nullButton.addSelectionListener(this);
        
        setControl(parent);
    }
    
    @Override
    public void setVisible(boolean visible) {
        if(visible) {
//            updateButtons(); // TODO do we need to call this here?
            if(rangeSets.getSplitType() == null ||
                    (!continuousButton.getSelection() &&
                    !categoricalButton.getSelection() &&
                    !nullButton.getSelection())) {
                String splitType = rangeSets.getSplitType();
                if(splitType == null) {
                    splitType = rangeSets.getDefaultSplitType();
                }
                if(splitType.equals(RangeSetUtil.SPLIT_CONTINUOUS)) {
                    continuousButton.setSelection(true);
                    categoricalButton.setSelection(false);
                    nullButton.setSelection(false);
                } else if(splitType.equals(RangeSetUtil.SPLIT_CATEGORICAL)) {
                    continuousButton.setSelection(false);
                    categoricalButton.setSelection(true);
                    nullButton.setSelection(false);
                } else {
                    continuousButton.setSelection(false);
                    categoricalButton.setSelection(false);
                    nullButton.setSelection(true);
                }
            }
        }
        super.setVisible(visible);
        getWizard().getContainer().updateButtons();
    }
    
    public void updateButtons() {
        DBInfo info = rangeSets.getDBInfo();
        continuousButton.setEnabled(rangeSets.getColumn() == null || info.isContinuous(rangeSets.getColumn()));
        if(!continuousButton.getEnabled() && continuousButton.getSelection()) {
            continuousButton.setSelection(false);
        }
    }
    
    @Override
    public boolean isPageComplete() {
        return continuousButton.getSelection() ||
            categoricalButton.getSelection() ||
            nullButton.getDragDetect();
    }
    
    public void widgetDefaultSelected(SelectionEvent e) {
    }
    
    public void widgetSelected(SelectionEvent e) {
        rangeSets.setSplitType((String) e.widget.getData());
    }
}
