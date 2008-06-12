package com.nexusbpm.dataselector.wizard;

import java.util.Calendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.DateTime;

import com.nexusbpm.dataselector.util.RangeSetUtil;

public class DateTimeValueComposite extends ValueComposite implements SelectionListener {
    private static final int TYPE_DATE = 1;
    private static final int TYPE_TIME = 2;
    private static final int TYPE_DATE_TIME = TYPE_DATE | TYPE_TIME;
    
    private DateTime date;
    private DateTime time;
    
    private int type;
    
    public DateTimeValueComposite(AddConditionComposite parent, RangeSetUtil rangeSets) {
        super(parent, rangeSets, SWT.NONE);
        
        FillLayout layout = new FillLayout();
        layout.spacing = 4;
        setLayout(layout);
        
        String typeName = rangeSets.getColumn().getJavaTypeName();
        
        if(typeName.equals(java.sql.Date.class.getName())) {
            type = TYPE_DATE;
        } else if(typeName.equals(java.sql.Time.class.getName())) {
            type = TYPE_TIME;
        } else if(typeName.equals(java.sql.Timestamp.class.getName())) {
            type = TYPE_DATE_TIME;
        } else {
            throw new IllegalStateException("Invalid data type " + typeName);
        }
        
        System.out.println(typeName + " " + type);
        if((type & TYPE_DATE) != 0) {
            date = new DateTime(this, SWT.DATE);
            date.addSelectionListener(this);
            date.addFocusListener(this);
        }
        if((type & TYPE_TIME) != 0) {
            time = new DateTime(this, SWT.TIME);
            time.addSelectionListener(this);
            time.addFocusListener(this);
        }
    }
    
    @Override
    public Object getValue() {
        Calendar c = Calendar.getInstance();
        if(date != null) {
            c.set(Calendar.YEAR, date.getYear());
            c.set(Calendar.MONTH, date.getMonth());
            c.set(Calendar.DAY_OF_MONTH, date.getDay());
        }
        if(time != null) {
            c.set(Calendar.HOUR_OF_DAY, time.getHours());
            c.set(Calendar.MINUTE, time.getMinutes());
            c.set(Calendar.SECOND, time.getSeconds());
            c.set(Calendar.MILLISECOND, 0);
        } else {
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
        }
        Object value = null;
        switch(type) {
            case TYPE_DATE:
                value = new java.sql.Date(c.getTimeInMillis());
                break;
            case TYPE_TIME:
                value = new java.sql.Time(c.getTimeInMillis());
                break;
            case TYPE_DATE_TIME:
                value = new java.sql.Timestamp(c.getTimeInMillis());
                break;
        }
//        System.out.println("" + value);
        return value;
    }
    @Override
    public void resetValue() {
        // don't reset the value for this kind of data entry
//        Calendar c = Calendar.getInstance();
//        if(date != null) {
//            date.setYear(c.get(Calendar.YEAR));
//            date.setMonth(c.get(Calendar.MONTH));
//            date.setDay(c.get(Calendar.DAY_OF_MONTH));
//        }
//        if(time != null) {
//            time.setHours(c.get(Calendar.HOUR_OF_DAY));
//            time.setMinutes(c.get(Calendar.MINUTE));
//            time.setSeconds(c.get(Calendar.SECOND));
//        }
    }
//    @Override
//    public boolean validateValue() {
//        getValue();
//        return true;
//    }
    
    @Override
    public void enable(boolean enabled) {
        if(date != null && enabled != date.getEnabled()) {
            date.setEnabled(enabled);
        }
        if(time != null && enabled != time.getEnabled()) {
            time.setEnabled(enabled);
        }
    }
    
    @Override
    public void focus() {
        date.setFocus();
    }
    
    @Override
    public boolean isFocusControl() {
        return super.isFocusControl() ||
            (date != null && date.isFocusControl()) ||
            (time != null && time.isFocusControl());
    }
    
    public void widgetDefaultSelected(SelectionEvent e) {
    }
    
    public void widgetSelected(SelectionEvent e) {
        getParent().validateValue();
    }
}
