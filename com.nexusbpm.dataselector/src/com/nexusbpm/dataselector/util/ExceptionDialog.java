package com.nexusbpm.dataselector.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;

import com.nexusbpm.dataselector.Plugin;

public class ExceptionDialog extends ErrorDialog {
    private IStatus status;
    
    public ExceptionDialog(Shell parentShell, String dialogTitle, String message, Throwable throwable) {
        super(parentShell, dialogTitle, message, new ReasonStatus(getReason(throwable)),
                IStatus.CANCEL | IStatus.ERROR | IStatus.WARNING | IStatus.INFO);
        setStatus(new ExceptionStatus(throwable));
        status = new Status(IStatus.ERROR, Plugin.PLUGIN_ID, IStatus.ERROR, message, throwable);
    }
    
    public IStatus getStatus() {
        return status;
    }
    
    private static String getReason(Throwable throwable) {
        String reason = throwable.getClass().getSimpleName();
        if(throwable.getLocalizedMessage() != null) {
            reason += ": " + throwable.getLocalizedMessage();
        }
        return reason;
    }
    
    private static class ReasonStatus extends Status {
        /**
         * @param reason this is used for the message that appears in the dialog
         *        under "Reason:" (not in the details section)
         */
        public ReasonStatus(String reason) {
            super(IStatus.ERROR, Plugin.PLUGIN_ID, reason);
        }
    }
    
    private static class ExceptionStatus extends Status {
        private IStatus[] children;
        
        /**
         * 
         * @param exception the exception that is displayed in the details
         */
        public ExceptionStatus(Throwable exception) {
            super(IStatus.ERROR, Plugin.PLUGIN_ID, IStatus.ERROR, exception.toString(), null);
            StackTraceElement[] elements = exception.getStackTrace();
            children = new IStatus[elements.length];
            for(int index = 0; index < elements.length; index++) {
                children[index] = new StackTraceElementStatus(elements[index]);
            }
        }
        
        @Override
        public boolean isMultiStatus() {
            return true;
        }
        
        @Override
        public IStatus[] getChildren() {
            return children;
        }
    }
    
    private static class StackTraceElementStatus extends Status {
        public StackTraceElementStatus(StackTraceElement element) {
            super(IStatus.ERROR, Plugin.PLUGIN_ID, IStatus.ERROR, "  at " + element, null);
        }
    }
}
