package com.nexusbpm.dataselector.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Text;

import com.nexusbpm.dataselector.util.RangeSetUtil;

public class TextValueComposite extends ValueComposite implements ModifyListener {
    private Text valueText;
    
    public TextValueComposite(AddConditionComposite parent, RangeSetUtil rangeSets) {
        super(parent, rangeSets, SWT.NONE);
        
        setLayout(new FillLayout());
        
        valueText = new Text(this, SWT.BORDER | SWT.SINGLE);
//        valueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//        valueText.addKeyListener(this);
        valueText.addModifyListener(this);
        valueText.addFocusListener(this);
    }
    
    @Override
    public Object getValue() {
        return getObjectConverter().parse(valueText.getText(), getRangeSets().getColumn());
    }
    @Override
    public void resetValue() {
        valueText.setText("");
    }
//    @Override
//    public boolean validateValue() {
//        getValue();
//        return true;
//    }
    
    @Override
    public void enable(boolean enabled) {
        if(enabled != valueText.getEnabled()) {
            valueText.setEnabled(enabled);
        }
    }
    
    @Override
    public void focus() {
        valueText.setFocus();
    }
    
    @Override
    public boolean isFocusControl() {
        return super.isFocusControl() || valueText.isFocusControl();
    }
    
    public void modifyText(ModifyEvent e) {
        getParent().validateValue();
    }
}
