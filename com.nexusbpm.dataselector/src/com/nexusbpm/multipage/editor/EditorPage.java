package com.nexusbpm.multipage.editor;

import org.eclipse.swt.widgets.Composite;

import com.nexusbpm.multipage.bus.EventRequestBus;

public interface EditorPage {
    void setEventRequestBus(EventRequestBus bus);
    void createPartControl(Composite parent);
    void activate();
    void setFocus();
    void dispose();
}
