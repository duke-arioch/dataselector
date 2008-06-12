package com.nexusbpm.dataselector.wizard;

import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

import com.nexusbpm.database.info.DBInfo;
import com.nexusbpm.dataselector.model.LSWhere.Match;
import com.nexusbpm.dataselector.model.config.LSColumn;
import com.nexusbpm.dataselector.util.RangeSetUtil;
import com.nexusbpm.dataselector.util.RangeSetUtil.RangeSetListener;
import com.nexusbpm.dataselector.wizard.NoProgressWizardDialog.DefaultButtonWizardPage;

public class AddConditionComposite extends Composite
    implements RangeSetListener, DefaultButtonWizardPage, ModifyListener, SelectionListener, FocusListener {
    protected static final String NULL_OPERATOR = "=null";
    protected static final String[] CONTINUOUS_OPERATORS = new String[] {
        Match.LT.getDisplayString(),
        Match.LTE.getDisplayString(),
        Match.EQ.getDisplayString(),
        NULL_OPERATOR
    };
    protected static final String[] CATEGORICAL_OPERATORS = new String[] {
        Match.EQ.getDisplayString(),
        NULL_OPERATOR
    };
    
    private RangeSetUtil rangeSets;
    
    private Composite operatorComposite;
    private Combo operatorCombo;
    private ValueComposite valueComposite;
    private Composite addComposite;
    private Button addButton;
    
    private IWizardContainer container;
    
    public AddConditionComposite(Composite parent, RangeSetUtil rangeSets, IWizardContainer container) {
        super(parent, SWT.NONE);
        this.container = container;
        this.rangeSets = rangeSets;
        rangeSets.addListener(this);
        
        setLayout(new FormLayout());
        
        operatorComposite = new Composite(this, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        operatorComposite.setLayout(layout);
        
        operatorCombo = new Combo(operatorComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        operatorCombo.addModifyListener(this);
        operatorCombo.addFocusListener(this);
        operatorCombo.setLayoutData(new GridData(100, SWT.DEFAULT));
        
        addComposite = new Composite(this, SWT.NONE);
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        addComposite.setLayout(layout);
        
        addButton = new Button(addComposite, SWT.PUSH);
        addButton.setText("Add");
        addButton.addSelectionListener(this);
        addButton.setLayoutData(new GridData(60, SWT.DEFAULT));
        addButton.addFocusListener(this);
        
        createValueComposite(getRangeSets().getColumn());
        updateSplitType();
        
        {
            GridData data = new GridData();
            data.widthHint = 100;
            data.verticalAlignment = SWT.CENTER;
            operatorCombo.setLayoutData(data);
            
            data = new GridData();
            data.widthHint = 60;
            data.verticalAlignment = SWT.CENTER;
            addButton.setLayoutData(data);
        }
        
        {
            FormData data = new FormData();
            data.left = new FormAttachment(0);
            operatorComposite.setLayoutData(data);
            
            data = new FormData();
            data.right = new FormAttachment(100);
            addComposite.setLayoutData(data);
        }
    }
    
    protected RangeSetUtil getRangeSets() {
        return rangeSets;
    }
    
    protected void updateSplitType() {
        // TODO don't show "=null" if null is already added
        if(getRangeSets().isCategorical(true)) {
            operatorCombo.setItems(CATEGORICAL_OPERATORS);
            operatorCombo.select(0);
        } else if(getRangeSets().isContinuous(true)) {
            operatorCombo.setItems(CONTINUOUS_OPERATORS);
            operatorCombo.select(0);
        } // TODO handle another else here?
//        operatorCombo.setItems(this.)
    }
    
    protected void add() {
        if(operatorCombo.getText().equals(NULL_OPERATOR)) {
            getRangeSets().addCondition(Match.EQ, null);
            operatorCombo.select(0);
        } else {
            Match match = Match.getMatchByDisplay(operatorCombo.getText());
            Object value = valueComposite.getValue();
            
            getRangeSets().addCondition(match, value);
            valueComposite.resetValue();
        }
        valueComposite.focus();
    }
    
    protected void validateValue() {
        boolean success = true;
        try {
            if(operatorCombo.getText().equals(NULL_OPERATOR)) {
                success = getRangeSets().canAddCondition(Match.EQ, null);
            } else {
                Object value = valueComposite.getValue();
                success = getRangeSets().canAddCondition(Match.getMatchByDisplay(operatorCombo.getText()), value);
//                if(!valueComposite.validateValue()) {
//                    success = false;
//                }
            }
        } catch(Exception e) {
            success = false;
        }
        addButton.setEnabled(success);
    }
    
    public void rangeSetChanged(int event, Object oldValue, Object newValue) {
        // TODO Auto-generated method stub
        if((event & EVENT_SPLIT_TYPE_CHANGED) != 0) {
            updateSplitType();
        } else if((event & EVENT_COLUMN_CHANGED) != 0) {
            createValueComposite((LSColumn) newValue);
            getParent().layout(true, true);
            validateValue();
        }
    }
    
    protected void createValueComposite(LSColumn column) {
        if(valueComposite != null) {
            valueComposite.dispose();
            valueComposite = null;
        }
        
        DBInfo i = getRangeSets().getDBInfo();
        
        if(column != null && i.isDateTime(column)) {
            valueComposite = new DateTimeValueComposite(this, getRangeSets());
        } else {
            valueComposite = new TextValueComposite(this, getRangeSets());
        }
        
        setTabList(new Control[] {operatorComposite, valueComposite, addComposite});
        
        FormData data = new FormData();
        data.left = new FormAttachment(operatorComposite, 6);
        data.right = new FormAttachment(addComposite, -6);
        valueComposite.setLayoutData(data);
    }
    
    public void modifyText(ModifyEvent e) {
        if(e.widget == operatorCombo) {
            if(operatorCombo.getText().equals(NULL_OPERATOR)) {
                valueComposite.resetValue();
                valueComposite.enable(false);
            } else {
                valueComposite.enable(true);
            }
        }
        validateValue();
    }
    
    public void widgetDefaultSelected(SelectionEvent e) {
    }
    
    public void widgetSelected(SelectionEvent e) {
        if(e.widget == addButton) {
            add();
        }
    }
    
    public void focusGained(FocusEvent e) {
        if(container != null) {
            container.updateButtons();
        }
    }
    
    private Widget ignoredWidget;
    
    public void focusLost(FocusEvent e) {
        if(container != null) {
            ignoredWidget = e.widget;
            container.updateButtons();
            ignoredWidget = null;
        }
    }
    
    public void updateDefaultButton() {
//        System.out.println(operatorCombo.isFocusControl() + " " + valueComposite.isFocusControl() + " " + addButton.isFocusControl());
        if((operatorCombo.isFocusControl() && !(operatorCombo == ignoredWidget)) ||
                (valueComposite.isFocusControl() && !(valueComposite == ignoredWidget)) ||
                (addButton.isFocusControl() && !(addButton == ignoredWidget))) {
            getShell().setDefaultButton(addButton);
        }
    }
}
