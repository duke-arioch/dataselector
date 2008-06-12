package com.nexusbpm.dataselector.editor;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.progress.IProgressService;

import com.nexusbpm.dataselector.events.ExceptionEvent;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.requests.ChangePageRequest;
import com.nexusbpm.dataselector.requests.ConfirmationRequest;
import com.nexusbpm.dataselector.requests.ExecuteCommandRequest;
import com.nexusbpm.dataselector.requests.LoginRequest;
import com.nexusbpm.dataselector.requests.RefreshTreeRequest;
import com.nexusbpm.dataselector.requests.RetryRequest;
import com.nexusbpm.dataselector.requests.RunInUIThreadRequest;
import com.nexusbpm.dataselector.requests.RunWithProgressRequest;
import com.nexusbpm.dataselector.requests.SetDirtyRequest;
import com.nexusbpm.dataselector.util.LogonDialog;
import com.nexusbpm.multipage.bus.BusRequest;
import com.nexusbpm.multipage.bus.BusRequestHandler;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class EditorRequestHandler implements BusRequestHandler {
    private DataSelectorEditor editor;
    private DefaultEditDomain editDomain;
    private EventRequestBus bus;
    
    public EditorRequestHandler(DataSelectorEditor editor, DefaultEditDomain editDomain, EventRequestBus bus) {
        this.editor = editor;
        this.editDomain = editDomain;
        this.bus = bus;
    }
    
    public boolean canHandleRequest(BusRequest request) {
        return request instanceof ChangePageRequest ||
            request instanceof SetDirtyRequest ||
            request instanceof LoginRequest ||
            request instanceof RunWithProgressRequest ||
            request instanceof RetryRequest ||
            request instanceof ExecuteCommandRequest ||
            request instanceof ConfirmationRequest ||
            request instanceof RunInUIThreadRequest ||
            request instanceof RefreshTreeRequest;
    }
    
    public Object handleRequest(BusRequest request) {
        if(request instanceof ChangePageRequest) {
            ChangePageRequest cpr = (ChangePageRequest) request;
            editor.setCurrentPage(cpr.getPageNumber());
        } else if(request instanceof SetDirtyRequest) {
            SetDirtyRequest sdr = (SetDirtyRequest) request;
            editor.setDirty(sdr.isDirty());
        } else if(request instanceof LoginRequest) {
            handleLoginRequest((LoginRequest) request);
        } else if(request instanceof RunWithProgressRequest) {
            handleRunWithProgressRequest((RunWithProgressRequest) request);
        } else if(request instanceof RetryRequest) {
            handleRetryRequest((RetryRequest) request);
        } else if(request instanceof ExecuteCommandRequest) {
            handleExecuteCommandRequest((ExecuteCommandRequest) request);
        } else if(request instanceof ConfirmationRequest) {
            handleConfirmationRequest((ConfirmationRequest) request);
        } else if(request instanceof RunInUIThreadRequest) {
            handleRunInUIThreadRequest((RunInUIThreadRequest) request);
        } else if(request instanceof RefreshTreeRequest) {
            handleRefreshTreeRequest((RefreshTreeRequest) request);
        }
        return null;
    }
    
    protected void handleRefreshTreeRequest(RefreshTreeRequest request) {
        editor.getTree().firePropertyChange("refresh", null, null);
        for(LSNode node : editor.getTree().getNodes()) {
            node.firePropertyChange("refresh", null, null);
        }
    }
    
    protected void handleRunInUIThreadRequest(RunInUIThreadRequest request) {
        Runnable runnable = request.getRunnable();
        Display display = editor.getEditorSite().getWorkbenchWindow().getWorkbench().getDisplay();
        Boolean async = request.getRunAsynchronously();
        boolean mustRunAsync = async != null && async.booleanValue();
        boolean mustRunSync = async != null && !async.booleanValue();
        boolean isUIThread = display.getThread() == Thread.currentThread();
        
        if(isUIThread && !mustRunAsync) {
            runnable.run();
        } else if(!mustRunSync) {
            display.asyncExec(runnable);
        } else {
            display.syncExec(runnable);
        }
    }
    
    protected void handleConfirmationRequest(ConfirmationRequest request) {
        MessageBox box = new MessageBox(
                editor.getSite().getShell(),
                SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
        box.setMessage(request.getMessage());
        box.setText("");
        request.setCancelled(box.open() == SWT.CANCEL);
    }
    
    protected void handleExecuteCommandRequest(ExecuteCommandRequest request) {
        editDomain.getCommandStack().execute(request.getCommand());
    }
    
    protected void handleRetryRequest(RetryRequest request) {
        MessageBox box = new MessageBox(
                editor.getSite().getShell(),
                SWT.ICON_QUESTION | SWT.RETRY | SWT.CANCEL);
        box.setMessage(request.getMessage());
        box.setText("Retry Operation?");
        request.setCancelled(box.open() == SWT.CANCEL);
    }
    
    protected void handleLoginRequest(LoginRequest request) {
        LogonDialog d = new LogonDialog(
                editor.getSite().getShell(),
                request.getUsername(),
                request.getPassword(),
                request.isSavePassword());
        d.open();
        request.setCancelled(d.getUsername() == null);
        if(d.getUsername() != null) {
            request.setUsername(d.getUsername());
            request.setPassword(d.getPassword());
            request.setSavePassword(d.isSavePassword());
        }
    }
    
    protected void handleRunWithProgressRequest(final RunWithProgressRequest request) {
        IProgressService progress = editor.getSite().getWorkbenchWindow().getWorkbench().getProgressService();
        try {
            progress.run(true, false, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(request.getDescription(), IProgressMonitor.UNKNOWN);
                    try {
                        request.getRunnable().run();
                    } finally {
                        monitor.done();
                    }
                }
            });
        } catch(Exception e) {
            bus.handleEvent(new ExceptionEvent("Error while " + request.getAction(), e));
        }
    }
}
