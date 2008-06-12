package com.nexusbpm.dataselector.wizard;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import com.nexusbpm.dataselector.Plugin;
import com.nexusbpm.dataselector.util.ExceptionDialog;

public class NewTreeWizard extends BasicNewResourceWizard implements INewWizard {
    private WizardNewFileCreationPage mainPage;
    public NewTreeWizard() {
    }
    
    @Override
    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        super.init(workbench, currentSelection);
        setWindowTitle("New Data Selector Tree");
    }
    
    @Override
    protected void initializeDefaultPageImageDescriptor() {
        ImageDescriptor desc = Plugin.imageDescriptorFromPlugin(Plugin.PLUGIN_ID, "/icons/tree_wizard.png");
        setDefaultPageImageDescriptor(desc);
    }
    
    @Override
    public void addPages() {
        mainPage = new NewTreePage("newTreePage1", getSelection());//$NON-NLS-1$
        mainPage.setTitle("Create a Data Selector tree");
        mainPage.setDescription("Create a new Data Selector tree"); 
        addPage(mainPage);
    }
    
    @Override
    public boolean performCancel() {
        return true;
    }
    
    @Override
    public boolean performFinish() {
        IFile file = mainPage.createNewFile();
        if (file == null) {
            return false;
        }
        
        selectAndReveal(file);
        
        // Open editor on new file.
        IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
        try {
            if (dw != null) {
                IWorkbenchPage page = dw.getActivePage();
                if (page != null) {
                    IDE.openEditor(page, file, true);
                }
            }
        } catch (PartInitException e) {
            ExceptionDialog d = new ExceptionDialog(
                    dw.getShell(),
                    "Error opening editor",
                    "Could not open editor for file",
                    e);
            d.open();
        }
        
        return true;
    }
}
