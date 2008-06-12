package com.nexusbpm.database.prefs;

import java.io.File;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.nexusbpm.database.Activator;

public class JDBCDriversPage extends PreferencePage implements IWorkbenchPreferencePage {
    private List jarsList;
    private Button addButton;
    private Button editButton;
    private Button removeButton;
    
    private FileDialog openDialog;
    
    private Preferences preferences;
    
    public JDBCDriversPage() {
        super();
        preferences = Activator.getDefault().getPluginPreferences();
    }
    
    public void init(IWorkbench workbench) {
    }
    
    protected Control createContents(Composite parent) {
        Composite clientArea = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginBottom = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginTop = 0;
        clientArea.setLayout(layout);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        clientArea.setLayoutData(gridData);
        
        jarsList = new List(clientArea, SWT.MULTI | SWT.BORDER);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.verticalSpan = 3;
        gridData.heightHint = 200;
        jarsList.setLayoutData(gridData);
        
        jarsList.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                updateButtons();
            }
        });
        
        String jars = preferences.getString("JDBC Jars");
        StringTokenizer tokenizer = new StringTokenizer(jars, ",");
        while(tokenizer.hasMoreTokens()) {
            jarsList.add(tokenizer.nextToken());
        }
        
        gridData = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        gridData.widthHint = 80;
        addButton = new Button(clientArea, SWT.PUSH);
        addButton.setText("Add...");
        addButton.setLayoutData(gridData);
        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                addNewJar();
            }
        });
        editButton = new Button(clientArea, SWT.PUSH);
        editButton.setText("Edit...");
        editButton.setLayoutData(gridData);
        editButton.setEnabled(false);
        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                editJar();
            }
        });
        removeButton = new Button(clientArea, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(gridData);
        removeButton.setEnabled(false);
        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                removeJars();
            }
        });
        
        return clientArea;
    }
    
    private FileDialog getOpenDialog() {
        if(openDialog == null) {
            openDialog = new FileDialog(getShell(), SWT.OPEN);
            openDialog.setText("Select a JAR");
            openDialog.setFilterPath(System.getProperty("user.home"));
            openDialog.setFilterExtensions(new String[] {"*.jar", "*.*"});
            openDialog.setFilterNames(new String[] {"JAR Files (*.jar)", "All Files (*.*)"});
        }
        return openDialog;
    }
    
    private void addNewJar() {
        FileDialog dialog = getOpenDialog();
        String filename = dialog.open();
        if(filename != null) {
            File file = new File(filename);
            if(file.exists()) {
                jarsList.add(filename);
                updateButtons();
            } else {
                showErrorDialog(getShell(), "File does not exist", "The specified file does not exist!");
            }
        }
    }
    
    private void editJar() {
        FileDialog dialog = getOpenDialog();
        File selected = new File(jarsList.getSelection()[0]);
        dialog.setFilterPath(selected.getPath());
        dialog.setFileName(selected.getName());
        String filename = dialog.open();
        if(filename != null) {
            File file = new File(filename);
            if(file.exists()) {
                jarsList.setItem(jarsList.getSelectionIndex(), filename);
                updateButtons();
            } else {
                showErrorDialog(getShell(), "File does not exist", "The specified file does not exist!");
            }
        }
    }
    
    private void removeJars() {
        String[] selection = jarsList.getSelection();
        for(int index = 0; index < selection.length; index++) {
            jarsList.remove(selection[index]);
        }
        updateButtons();
    }
    
    private void updateButtons() {
        String[] selection = jarsList.getSelection();
        editButton.setEnabled(selection.length == 1);
        removeButton.setEnabled(selection.length > 0);
    }
    
    private void showErrorDialog(Shell parent, String title, String message) {
        IStatus s = new Status(IStatus.ERROR, "org.jbpm.gd.jpdl", 123, message, null);
        ErrorDialog d = new ErrorDialog(parent, title, message, s,
                IStatus.CANCEL | IStatus.ERROR | IStatus.INFO | IStatus.WARNING | IStatus.OK);
        d.create();
        d.open();
    }
    
    public boolean performOk() {
        String value = "";
        String[] jars = jarsList.getItems();
        for(int index = 0; index < jars.length; index++) {
            value += jars[index] + ",";
        }
        if(value.endsWith(",")) {
            value = value.substring(0, value.length() - 1);
        }
        preferences.setValue("JDBC Jars", value);
        return true;
    }
    
    public void performDefaults() {
        preferences.setToDefault("JDBC Jars");
    }
}
