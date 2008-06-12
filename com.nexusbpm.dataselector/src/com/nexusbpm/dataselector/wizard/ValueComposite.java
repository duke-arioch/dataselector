package com.nexusbpm.dataselector.wizard;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

import com.nexusbpm.dataselector.util.ObjectConverter;
import com.nexusbpm.dataselector.util.RangeSetUtil;

public abstract class ValueComposite extends Composite implements FocusListener {
    private ObjectConverter converter;
    private RangeSetUtil rangeSets;
    
    public ValueComposite(AddConditionComposite parent, RangeSetUtil rangeSets, int style) {
        super(parent, style);
        this.rangeSets = rangeSets;
    }
    
    public RangeSetUtil getRangeSets() {
        return rangeSets;
    }
    
    protected ObjectConverter getObjectConverter() {
        if(converter == null) {
            converter = ObjectConverter.getInstance();
        }
        return converter;
    }
    
    @Override
    public AddConditionComposite getParent() {
        return (AddConditionComposite) super.getParent();
    }
    
    public abstract void resetValue();
    public abstract Object getValue(); // throw an exception if the value isn't currently valid
//    public abstract boolean validateValue(); // can return false or throw an exception for invalid values
    
    // the following methods are named specifically to avoid overriding SWT methods
    public abstract void enable(boolean enabled);
    public abstract void focus();
    
    public void focusGained(FocusEvent e) {
        getParent().focusGained(createFocusEvent(e));
    }
    public void focusLost(FocusEvent e) {
        getParent().focusLost(createFocusEvent(e));
    }
    
    protected FocusEvent createFocusEvent(FocusEvent evt) {
        Event e = new Event();
        e.widget = this;
        e.data = evt.data;
        e.display = evt.display;
        e.time = evt.time;
        return new FocusEvent(e);
    }
}
