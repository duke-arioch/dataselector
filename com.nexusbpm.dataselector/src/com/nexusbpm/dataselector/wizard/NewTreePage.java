package com.nexusbpm.dataselector.wizard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import com.nexusbpm.dataselector.Plugin;
import com.nexusbpm.dataselector.model.persistance.Dom4jTreePersistance;

public class NewTreePage extends WizardNewFileCreationPage {
    public NewTreePage(String pageName, IStructuredSelection selection) {
        super(pageName, selection);
    }
    
    @Override
    protected InputStream getInitialContents() {
        Dom4jTreePersistance persistance = new Dom4jTreePersistance();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            persistance.writeTree(out, persistance.getNewTree());
            return new ByteArrayInputStream(out.toByteArray());
        } catch(IOException e) {
        }
        return super.getInitialContents();
    }
    
    @Override
    protected String getNewFileLabel() {
        return "Data Selector file name:";
    }
    
    @Override
    public IFile createNewFile() {
        updateFileName();
        return super.createNewFile();
    }
    
    protected void updateFileName() {
        String filename = getFileName();
        if(filename != null && filename.length() > 0) {
            if(!(filename.endsWith(".lst") || filename.endsWith(".dst") || filename.endsWith(".tree"))) {
                setFileName(filename + ".dst");
            }
        }
    }
    
    @Override
    protected void createAdvancedControls(Composite parent) {
    }
    
    @Override
    protected void createLinkTarget() {
    }
    
    private boolean ignoreSelection;
    private boolean setSelection;
    private int selectionStart;
    private int selectionEnd;
    
    @Override
    public void handleEvent(Event event) {
        // the caret gets moved to the beginning of the text box if the text is modified
        // programmaticaly, so keep track of the caret when the user modifies the text,
        // ignore the caret's position change from programmatic text changes, and restore
        // the caret position when the text is programmatically reset to the value entered
        // by the user (we could do this directly except the superclass does not expose
        // the necessary variables to subclasses)
        if(!ignoreSelection && event.widget instanceof Text) {
            selectionStart = ((Text) event.widget).getCaretPosition();
            selectionEnd = selectionStart + ((Text) event.widget).getSelectionCount();
        } else if(setSelection && event.widget instanceof Text) {
            ((Text) event.widget).setSelection(selectionStart, selectionEnd);
            setSelection = false;
        }
        super.handleEvent(event);
    }
    
    
    private boolean validating;
    
    @Override
    protected boolean validatePage() {
        // calling setFileName in this method causes recursion, so return false early in those cases
        if(validating) {
            return false;
        }
        boolean valid = true;
        try {
            validating = true;
            ignoreSelection = true;
            String filename = getFileName();
            try{
                updateFileName();
                valid = super.validatePage();
            } catch(Exception e) {
            }
            
            setSelection = true;
            setFileName(filename);
        } finally {
            ignoreSelection = false;
            setSelection = false;
            validating = false;
        }
        
        if(valid) {
            String filename = getFileName();
            if(filename == null || filename.length() == 0) {
//                    !(filename.endsWith(".lst") || filename.endsWith(".dst") || filename.endsWith(".tree"))) {
                setMessage(null);
                setErrorMessage("error");
//                setErrorMessage("Data Selector files must have the extension .dst, .lst, or .tree");
                valid = false;
            }
        }
        
        if(valid) {
            setMessage(null);
            setErrorMessage(null);
        }
        
        return valid;
    }
    
    @Override
    protected IStatus validateLinkedResource() {
        return new Status(IStatus.OK, Plugin.PLUGIN_ID, "");
    }
}
