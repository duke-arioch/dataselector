package com.nexusbpm.database.prefs;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.nexusbpm.database.Activator;
import com.nexusbpm.database.driver.SQLDriver;

public class JDBCDriversPage extends PreferencePage implements IWorkbenchPreferencePage {
    protected TabFolder tabFolder;
    
    protected TabItem jarsTab;
    protected TabItem driversTab;
    
    protected List jarsList;
    protected Button addJarButton;
    protected Button editJarButton;
    protected Button removeJarButton;
    
    protected Table driversTable;
    protected TableViewer driversTableViewer;
    protected Button addDriverButton;
    protected Button editDriverButton;
    protected Button removeDriverButton;
    
    protected JDBCDriverContentLabelProvider driverProvider;
    
    protected FileDialog openDialog;
    protected DriverDialog driverDialog;
    
    protected Preferences preferences;
    
    public JDBCDriversPage() {
        super();
        preferences = Activator.getDefault().getPluginPreferences();
    }
    
    public void init(IWorkbench workbench) {
    }
    
    protected Control createContents(Composite parent) {
        GridData gridData;
        
        tabFolder = new TabFolder(parent, SWT.TOP);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        jarsTab = new TabItem(tabFolder, SWT.NONE);
        jarsTab.setText("Jars");
        
        Composite jarsClientArea = new Composite(tabFolder, SWT.NONE);
        jarsTab.setControl(jarsClientArea);
        GridLayout layout = new GridLayout(2, false);
        layout.marginBottom = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginTop = 0;
//        layout.verticalSpacing = 1;
        jarsClientArea.setLayout(layout);
        
        jarsList = new List(jarsClientArea, SWT.MULTI | SWT.BORDER);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.verticalSpan = 3;
        gridData.heightHint = 200;
        jarsList.setLayoutData(gridData);
        
        jarsList.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                updateJarButtons();
            }
        });
        
        String jars = preferences.getString("JDBC Jars");
        StringTokenizer tokenizer = new StringTokenizer(jars, ",");
        while(tokenizer.hasMoreTokens()) {
            jarsList.add(tokenizer.nextToken());
        }
        
        gridData = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        gridData.widthHint = 80;
        addJarButton = new Button(jarsClientArea, SWT.PUSH);
        addJarButton.setText("Add...");
        addJarButton.setLayoutData(gridData);
        addJarButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                addNewJar();
            }
        });
        gridData = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        gridData.widthHint = 80;
        editJarButton = new Button(jarsClientArea, SWT.PUSH);
        editJarButton.setText("Edit...");
        editJarButton.setLayoutData(gridData);
        editJarButton.setEnabled(false);
        editJarButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                editJar();
            }
        });
        gridData = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        gridData.widthHint = 80;
        removeJarButton = new Button(jarsClientArea, SWT.PUSH);
        removeJarButton.setText("Remove");
        removeJarButton.setLayoutData(gridData);
        removeJarButton.setEnabled(false);
        removeJarButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                removeJars();
            }
        });
        
        driversTab = new TabItem(tabFolder, SWT.NONE);
        driversTab.setText("Drivers");
        
        Composite driversClientArea = new Composite(tabFolder, SWT.NONE);
        driversTab.setControl(driversClientArea);
        layout = new GridLayout(2, false);
        layout.marginBottom = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginTop = 0;
//        layout.verticalSpacing = 1;
        driversClientArea.setLayout(layout);
        
        driversTable = new Table(driversClientArea, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
        gridData = new GridData(GridData.FILL_BOTH);
        gridData.verticalSpan = 3;
        gridData.heightHint = 200;
        driversTable.setLayoutData(gridData);
        
        driversTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                updateDriverButtons();
            }
        });
        
        driversTableViewer = new TableViewer(driversTable);
        
        driverProvider = new JDBCDriverContentLabelProvider(jarsList.getFont());
        
        driversTableViewer.setLabelProvider(driverProvider);
        driversTableViewer.setComparator(new ViewerComparator());
        driversTableViewer.setContentProvider(driverProvider);
        
        ColumnViewerToolTipSupport.enableFor(driversTableViewer);
        
        TableLayout tlayout = new TableLayout();
        tlayout.addColumnData(new ColumnWeightData(75, true));
        tlayout.addColumnData(new ColumnWeightData(25, true));
        
        driversTable.setLayout(tlayout);
        driversTable.setLinesVisible(true);
        driversTable.setHeaderVisible(true);
        
        TableColumn column = new TableColumn(driversTable, SWT.LEFT);
        column.setText("Driver Name");
        column = new TableColumn(driversTable, SWT.LEFT);
        column.setText("Available");
        
        driversTableViewer.setInput(getJarsList());
        
        gridData = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        gridData.widthHint = 80;
        addDriverButton = new Button(driversClientArea, SWT.PUSH);
        addDriverButton.setText("Add...");
        addDriverButton.setLayoutData(gridData);
        addDriverButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                addNewDriver();
            }
        });
        gridData = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        gridData.widthHint = 80;
        editDriverButton = new Button(driversClientArea, SWT.PUSH);
        editDriverButton.setText("Edit...");
        editDriverButton.setLayoutData(gridData);
        editDriverButton.setEnabled(false);
        editDriverButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                editDriver();
            }
        });
        gridData = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        gridData.widthHint = 80;
        removeDriverButton = new Button(driversClientArea, SWT.PUSH);
        removeDriverButton.setText("Remove");
        removeDriverButton.setLayoutData(gridData);
        removeDriverButton.setEnabled(false);
        removeDriverButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                removeDrivers();
            }
        });
        
        Label driversNoteLabel = new Label(driversClientArea, SWT.LEFT | SWT.WRAP);
        driversNoteLabel.setText("Note: Built-in driver definitions cannot be modified or removed.");
        
        updateDriverList();
        
        return jarsClientArea;
    }
    
    protected FileDialog getOpenDialog() {
        if(openDialog == null) {
            openDialog = new FileDialog(getShell(), SWT.OPEN);
            openDialog.setText("Select a JAR");
            openDialog.setFilterPath(System.getProperty("user.home"));
            openDialog.setFilterExtensions(new String[] {"*.jar", "*.*"});
            openDialog.setFilterNames(new String[] {"JAR Files (*.jar)", "All Files (*.*)"});
        }
        return openDialog;
    }
    
    protected void addNewJar() {
        FileDialog dialog = getOpenDialog();
        String filename = dialog.open();
        if(filename != null) {
            File file = new File(filename);
            if(file.exists()) {
                jarsList.add(filename);
                updateJarButtons();
            } else {
                showErrorDialog(getShell(), "File does not exist", "The specified file does not exist!");
            }
        }
        updateDriverList();
    }
    
    protected void editJar() {
        FileDialog dialog = getOpenDialog();
        File selected = new File(jarsList.getSelection()[0]);
        dialog.setFilterPath(selected.getPath());
        dialog.setFileName(selected.getName());
        String filename = dialog.open();
        if(filename != null) {
            File file = new File(filename);
            if(file.exists()) {
                jarsList.setItem(jarsList.getSelectionIndex(), filename);
                updateJarButtons();
            } else {
                showErrorDialog(getShell(), "File does not exist", "The specified file does not exist!");
            }
        }
        updateDriverList();
    }
    
    protected void removeJars() {
        String[] selection = jarsList.getSelection();
        for(int index = 0; index < selection.length; index++) {
            jarsList.remove(selection[index]);
        }
        updateJarButtons();
        updateDriverList();
    }
    
    protected void updateJarButtons() {
        String[] selection = jarsList.getSelection();
        editJarButton.setEnabled(selection.length == 1);
        removeJarButton.setEnabled(selection.length > 0);
    }
    
    protected void addNewDriver() {
        DriverDialog dialog = new DriverDialog(getShell());
        
        dialog.setInvalidNames(driverProvider.getDriverNames());
        dialog.setStdDevFunction("stddev");
        dialog.setSupportsMinMaxStrings(true);
        
        int choice = dialog.open();
        if(choice == Window.OK) {
            SQLDriverWrapper driver = new SQLDriverWrapper(
                    dialog.getDriverName(),
                    dialog.getDriverSampleConnectString(),
                    dialog.getDriverClassName(),
                    dialog.getStdDevFunction(),
                    dialog.supportsMinMaxStrings());
            driverProvider.addDriver(driver);
            driversTableViewer.add(driver);
        }
    }
    
    protected void editDriver() {
        DriverDialog dialog = new DriverDialog(getShell());
        
        SQLDriverWrapper driver = (SQLDriverWrapper)
            ((IStructuredSelection) driversTableViewer.getSelection()).getFirstElement();
        
        java.util.List<String> names = driverProvider.getDriverNames();
        names.remove(driver.getName());
        
        dialog.setInvalidNames(names);
//        dialog.initialize(driver.getName(), driver.getSampleConnectString(), driver.getDriverClassName());
        dialog.setDriverName(driver.getName());
        dialog.setDriverSampleConnectString(driver.getSampleConnectString());
        dialog.setDriverClassName(driver.getDriverClassName());
        dialog.setStdDevFunction(driver.getStdDevFunction());
        dialog.setSupportsMinMaxStrings(driver.supportsMinMaxStrings());
        
        int choice = dialog.open();
        if(choice == Window.OK) {
            driver.setName(dialog.getDriverName());
            driver.setSampleConnectString(dialog.getDriverSampleConnectString());
            driver.setDriverClassName(dialog.getDriverClassName());
            driversTableViewer.refresh();
        }
    }
    
    protected void removeDrivers() {
        IStructuredSelection selection = (IStructuredSelection) driversTableViewer.getSelection();
        Object[] objects = selection.toArray();
        ArrayList<SQLDriverWrapper> drivers = new ArrayList<SQLDriverWrapper>();
        for(Object o : objects) {
            SQLDriverWrapper driver = (SQLDriverWrapper) o;
            if(!driver.isBuiltIn()) {
                drivers.add(driver);
            }
        }
        driverProvider.removeDrivers(drivers);
        driversTableViewer.remove(drivers.toArray());
    }
    
    protected void updateDriverButtons() {
        IStructuredSelection selection = (IStructuredSelection) driversTableViewer.getSelection();
        Object[] objects = selection.toArray();
        editDriverButton.setEnabled(objects.length == 1 && !((SQLDriverWrapper) objects[0]).isBuiltIn());
        boolean removable = false;
        for(Object o : objects) {
            if(!((SQLDriverWrapper) o).isBuiltIn()) {
                removable = true;
                break;
            }
        }
        removeDriverButton.setEnabled(removable);
    }
    
    private void showErrorDialog(Shell parent, String title, String message) {
        IStatus s = new Status(IStatus.ERROR, "org.jbpm.gd.jpdl", 123, message, null);
        ErrorDialog d = new ErrorDialog(parent, title, message, s,
                IStatus.CANCEL | IStatus.ERROR | IStatus.INFO | IStatus.WARNING | IStatus.OK);
        d.create();
        d.open();
    }
    
    public boolean performOk() {
        preferences.setValue("JDBC Jars", getJarsList());
        preferences.setValue("JDBC Drivers", getDriversList());
        return true;
    }
    
    protected String getJarsList() {
        String value = "";
        String[] jars = jarsList.getItems();
        for(int index = 0; index < jars.length; index++) {
            value += jars[index] + ",";
        }
        if(value.endsWith(",")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
    
    protected String getDriversList() {
        java.util.List<SQLDriverWrapper> wrappers = driverProvider.getDrivers();
        java.util.List<SQLDriver> drivers = new ArrayList<SQLDriver>();
        
        for(SQLDriverWrapper wrapper : wrappers) {
            if(!wrapper.isBuiltIn()) {
                drivers.add(new SQLDriver(
                        wrapper.getName(),
                        wrapper.getSampleConnectString(),
                        wrapper.getDriverClassName(),
                        wrapper.getStdDevFunction(),
                        wrapper.supportsMinMaxStrings()));
            }
        }
        
        return SQLDriver.getDriversString(drivers);
    }
    
    public void performDefaults() {
        preferences.setToDefault("JDBC Jars");
    }
    
    protected void updateDriverList() {
        driversTableViewer.setInput(getJarsList());
    }
}
