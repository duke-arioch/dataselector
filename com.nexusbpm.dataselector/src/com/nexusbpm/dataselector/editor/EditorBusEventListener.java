package com.nexusbpm.dataselector.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import com.nexusbpm.dataselector.events.ExceptionEvent;
import com.nexusbpm.dataselector.events.UserMessageEvent;
import com.nexusbpm.dataselector.util.ExceptionDialog;
import com.nexusbpm.multipage.bus.BusEvent;
import com.nexusbpm.multipage.bus.BusEventListener;

public class EditorBusEventListener implements BusEventListener {
    private DataSelectorEditor editor;
    
    public EditorBusEventListener(DataSelectorEditor editor) {
        this.editor = editor;
    }
    
    public void handleEvent(BusEvent event) {
        if(event instanceof ExceptionEvent) {
            ExceptionEvent ev = (ExceptionEvent) event;
            ev.getThrowable().printStackTrace();
            ExceptionDialog d = new ExceptionDialog(
                    editor.getSite().getShell(),
                    "Error",
                    ev.getMessage(),
                    ev.getThrowable());
            d.open();
        } else if(event instanceof UserMessageEvent) {
            UserMessageEvent ev = (UserMessageEvent) event;
            int icon = SWT.ICON_INFORMATION;
            if(ev.getStatus() == UserMessageEvent.STATUS_WARNING) {
                icon = SWT.ICON_WARNING;
            } else if(ev.getStatus() == UserMessageEvent.STATUS_ERROR) {
                icon = SWT.ICON_ERROR;
            }
            MessageBox box = new MessageBox(editor.getSite().getShell(), icon | SWT.OK);
            box.setMessage(ev.getMessage());
            box.setText(ev.getTitle());
            box.open();
        }
    }
}
