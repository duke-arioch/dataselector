package com.nexusbpm.dataselector.editor.pages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.nexusbpm.database.driver.SQLDriver;
import com.nexusbpm.database.table.ResultSetTableUtil;
import com.nexusbpm.dataselector.events.ConfigurationChangeEvent;
import com.nexusbpm.dataselector.events.ExceptionEvent;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.model.LSTree.State;
import com.nexusbpm.dataselector.model.config.LSConfig;
import com.nexusbpm.dataselector.model.config.LSConnection;
import com.nexusbpm.dataselector.model.config.LSDriver;
import com.nexusbpm.dataselector.requests.ChangePageRequest;
import com.nexusbpm.dataselector.requests.ConfigurationChangeRequest;
import com.nexusbpm.dataselector.requests.RefreshTreeRequest;
import com.nexusbpm.dataselector.requests.TestConnectionRequest;
import com.nexusbpm.multipage.bus.BusEvent;
import com.nexusbpm.multipage.bus.BusEventListener;

public class DatasourceConfigPage extends AbstractEditorPage
        implements ModifyListener, SelectionListener, PropertyChangeListener, BusEventListener {
    private Combo driverCombo;
    private Text uriText;
    private SQLDriver oldDriver;
    
    private Text usernameText;
    private Text passwordText;
    private Button savePasswordButton;
    private Button testConnectionButton;
    private Button sampleDataButton;
    
    private Text queryText;
    
    private Button previousButton;
    private Button nextButton;
    private Button editButton;
    private Button cancelButton;
    
    private boolean editing;
    
    public DatasourceConfigPage(LSTree tree) {
        super(tree);
    }
    
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        
        getEventRequestBus().addEventListener(this);
        
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        parent.setLayout(layout);
        
        FormToolkit toolkit = getFormToolkit();
        Form form = toolkit.createForm(parent);
        form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        form.setText("Data Selector" /* + " Datasource"*/);
        
        Composite body = form.getBody();
        body.setLayout(new FormLayout());
        
        FormData data;
        Composite client;
        
        // create the Database Setup section (top-left)
        Section section1 = toolkit.createSection(body, Section.TITLE_BAR);
        section1.setText(" Database Connection");
        section1.titleBarTextMarginWidth = 2;
        section1.setLayoutData(data = new FormData());
        data.top = new FormAttachment(0, 2);
        data.left = new FormAttachment(0, 2);
        data.right = new FormAttachment(50, -2);
        
        client = toolkit.createComposite(section1);
        section1.setClient(client);
        createDatabaseSetupSection(client);
        
        // create the Credentials section (top-right)
        Section section2 = toolkit.createSection(body, Section.TITLE_BAR);
        section2.setText(" Credentials");
        section2.titleBarTextMarginWidth = 2;
        section2.setLayoutData(data = new FormData());
        data.top = new FormAttachment(0, 2);
        data.left = new FormAttachment(50, 2);
        data.right = new FormAttachment(100, -2);
        data.bottom = new FormAttachment(section1, 0, SWT.BOTTOM);
        
        client = toolkit.createComposite(section2);
        section2.setClient(client);
        createCredentialsSection(client);
        
        // create the query section (bottom)
        Section section3 = toolkit.createSection(body, Section.TITLE_BAR);
        section3.setText(" SQL Query");
        section3.titleBarTextMarginWidth = 2;
        section3.setLayoutData(data = new FormData());
        data.top = new FormAttachment(section1, 12, SWT.BOTTOM);
        data.left = new FormAttachment(0, 2);
        data.right = new FormAttachment(100, -2);
        data.bottom = new FormAttachment(100, -2);
        
        client = toolkit.createComposite(section3);
        section3.setClient(client);
        createQuerySection(client);
        
        // create the bottom separator
        toolkit.createLabel(parent, "", SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        
        // create the button bar
//        Composite buttonBar = toolkit.createComposite(parent);
//        buttonBar.setLayoutData(createGridData(SWT.FILL, true));
        
        createButtonBar(parent).setLayoutData(createGridData(SWT.FILL, true));
        
        getTree().addPropertyChangeListener(this);
        getTree().getConfig().addPropertyChangeListener(this);
        getTree().getConfig().getConnection().addPropertyChangeListener(this);
        getTree().getConfig().getDriver().addPropertyChangeListener(this);
        
        setEditing(false);
    }
    
    protected void createDatabaseSetupSection(Composite body) {
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        body.setLayout(layout);
        
        FormToolkit toolkit = getFormToolkit();
        
        toolkit.createLabel(body, "Database type:", SWT.LEFT).setLayoutData(createGridData(SWT.FILL, false));
        
        driverCombo = new Combo(body, SWT.DROP_DOWN | SWT.READ_ONLY);
        toolkit.adapt(driverCombo, false, false);
        driverCombo.setItems(SQLDriver.getDriverNames().toArray(new String[0]));
        driverCombo.setText(notNull(getTree().getConfig().getDriver().getName()));
        driverCombo.setLayoutData(createGridData(SWT.FILL, true));
        driverCombo.addModifyListener(this);
        
        toolkit.createLabel(body, "Database URI:", SWT.LEFT).setLayoutData(createGridData(SWT.FILL, false));
        
        uriText = toolkit.createText(body, "", SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        uriText.setText(notNull(getTree().getConfig().getConnection().getURI()));
        uriText.setLayoutData(createGridData(SWT.FILL, false));
        uriText.addModifyListener(this);
    }
    
    protected  void createCredentialsSection(Composite body) {
        GridLayout layout = new GridLayout(4, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        body.setLayout(layout);
        
        FormToolkit toolkit = getFormToolkit();
        
        toolkit.createLabel(body, "Username:", SWT.RIGHT).setLayoutData(createGridData(SWT.FILL, false));
        
        usernameText = toolkit.createText(body, "", SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        usernameText.setText(notNull(getTree().getConfig().getConnection().getUsername()));
        usernameText.setLayoutData(createGridData(SWT.FILL, true, 3));
        usernameText.addModifyListener(this);
        
        toolkit.createLabel(body, "Password:", SWT.RIGHT).setLayoutData(createGridData(SWT.FILL, false));
        
        passwordText = toolkit.createText(body, "", SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        passwordText.setEchoChar('*');
        passwordText.setText(notNull(getTree().getConfig().getConnection().getPassword()));
        passwordText.setLayoutData(createGridData(SWT.FILL, true, 3));
        passwordText.addModifyListener(this);
        
        toolkit.createLabel(body, "", SWT.CENTER).setLayoutData(createGridData(SWT.FILL, false));
        
        savePasswordButton = toolkit.createButton(body, "Save password", SWT.CHECK | SWT.LEFT);
        savePasswordButton.setSelection(getTree().getConfig().getConnection().isSavePassword());
        savePasswordButton.setLayoutData(createGridData(SWT.FILL, true));
        savePasswordButton.addSelectionListener(this);
        
        testConnectionButton = toolkit.createButton(body, "Test Connection", SWT.PUSH);
        testConnectionButton.setLayoutData(createGridData(SWT.CENTER, false));
        testConnectionButton.addSelectionListener(this);
        
        sampleDataButton = toolkit.createButton(body, "Sample Data", SWT.PUSH);
        sampleDataButton.setLayoutData(createGridData(SWT.CENTER, false));
        sampleDataButton.addSelectionListener(this);
    }
    
    protected void createQuerySection(Composite body) {
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        body.setLayout(layout);
        
        FormToolkit toolkit = getFormToolkit();
        
        queryText = toolkit.createText(body, "", SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        queryText.setText(notNull(getTree().getConfig().getQuery()));
        queryText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        queryText.addModifyListener(this);
    }
    
    protected void createButtonBarContents(Composite parent) {
        parent.setLayout(new GridLayout(10, false));
        
        FormToolkit toolkit = getFormToolkit();
        
        Label separator = toolkit.createLabel(parent, "", SWT.LEFT);
        separator.setLayoutData(createGridData(SWT.FILL, true));
//        separator.setBackground(parent.getBackground());
        separator.setVisible(false);
        
        previousButton = toolkit.createButton(parent, "<< Previous", SWT.PUSH);
        previousButton.setLayoutData(new GridData(85, SWT.DEFAULT));
        previousButton.addSelectionListener(this);
        previousButton.setEnabled(false); // first tab, so not available
        
        nextButton = toolkit.createButton(parent, "Next >>", SWT.PUSH);
        nextButton.setLayoutData(new GridData(85, SWT.DEFAULT));
        nextButton.addSelectionListener(this);
        
        editButton = toolkit.createButton(parent, "Edit", SWT.PUSH);
        editButton.setLayoutData(new GridData(85, SWT.DEFAULT));
        editButton.addSelectionListener(this);
        
        cancelButton = toolkit.createButton(parent, "Cancel", SWT.PUSH);
        cancelButton.setLayoutData(new GridData(85, SWT.DEFAULT));
        cancelButton.addSelectionListener(this);
    }
    
    protected GridData createGridData(int horizontalAlignment, boolean grabHorizontal) {
        return createGridData(horizontalAlignment, grabHorizontal, 1);
    }
    
    protected GridData createGridData(int horizontalAlignment, boolean grabHorizontal, int horizontalSpan) {
        return new GridData(horizontalAlignment, SWT.CENTER, grabHorizontal, false, horizontalSpan, 1);
    }
    
    protected String notNull(String str) {
        if(str == null) {
            return "";
        } else {
            return str;
        }
    }
    
    public void modifyText(ModifyEvent e) {
        // when the user selects a different driver change the URI to a new sample URI
        if(e.widget == driverCombo) {
            SQLDriver oldDriver = null;
            if(this.oldDriver != null) {
                oldDriver = this.oldDriver;
            } else {
                oldDriver = SQLDriver.getDriverInstanceByClassName(getTree().getConfig().getDriver().getDriverClass());
            }
            SQLDriver driver = SQLDriver.getDriverInstanceByName(driverCombo.getText());
            if(driver != null && (oldDriver == null ||
                    !oldDriver.getSampleConnectString().equals(driver.getSampleConnectString()))) {
                uriText.setText(driver.getSampleConnectString());
                this.oldDriver = driver;
            }
        }
    }
    
    public void widgetDefaultSelected(SelectionEvent e) {
        System.out.println("d:" + e);
    }
    
    public void widgetSelected(SelectionEvent e) {
        if(e.widget == savePasswordButton) {
            System.out.println(e);
        } else if(e.widget == testConnectionButton) {
            if(editing) {
                SQLDriver sqlDriver = SQLDriver.getDriverInstanceByName(driverCombo.getText());
                String uri = uriText.getText();
                String username = usernameText.getText();
                String password = passwordText.getText();
                
                sendRequest(new TestConnectionRequest(sqlDriver, uri, username, password));
            } else {
                sendRequest(new TestConnectionRequest());
            }
        } else if(e.widget == sampleDataButton) {
            try {
                ResultSetTableUtil.openTable(
                        PlatformUI.getWorkbench().getProgressService(),
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        true,
                        SQLDriver.getDriverInstanceByName(driverCombo.getText()),
                        uriText.getText(),
                        usernameText.getText(),
                        passwordText.getText(),
                        queryText.getText(),
                        1000);
            } catch(Exception exc) {
                getEventRequestBus().handleEvent(new ExceptionEvent("Error sampling data", exc));
            }
        } else if(e.widget == nextButton) {
            State state = getTree().getState();
            if(state != State.CONFIG_DATASOURCE) {
                sendRequest(new ChangePageRequest(1));
            }
        } else if(e.widget == editButton) {
            if(editing) {
                // attempt to save the configuration
                SQLDriver sqlDriver = SQLDriver.getDriverInstanceByName(driverCombo.getText());
                String uri = uriText.getText();
                String username = usernameText.getText();
                String password = passwordText.getText();
                boolean savePassword = savePasswordButton.getSelection();
                String query = queryText.getText();
                
                // send a request. If the config is changed, the bus will send out an event
                sendRequest(new ConfigurationChangeRequest(sqlDriver, uri, username, password, savePassword, query));
            } else {
                setEditing(true);
            }
        } else if(e.widget == cancelButton) {
            LSConfig config = getTree().getConfig();
            
            queryText.setText(config.getQuery());
            
            LSDriver driver = config.getDriver();
            driverCombo.setText(driver.getName());
            
            LSConnection conn = config.getConnection();
            
            uriText.setText(conn.getURI());
            usernameText.setText(conn.getUsername());
            passwordText.setText(conn.getPassword());
            savePasswordButton.setSelection(conn.isSavePassword());
            
            setEditing(false);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(LSConfig.PROPERTY_CONFIG_QUERY)) {
            queryText.setText((String) evt.getNewValue());
        } else if(evt.getPropertyName().equals(LSDriver.PROPERTY_ELEMENT_NAME)) {
            SQLDriver driver = SQLDriver.getDriverInstanceByName((String) evt.getNewValue());
            if(driver != null) {
                driverCombo.setText(driver.getName());
            }
        } else if(evt.getPropertyName().equals(LSConnection.PROPERTY_CONNECTION_URI)) {
            uriText.setText((String) evt.getNewValue());
        } else if(evt.getPropertyName().equals(LSConnection.PROPERTY_CONNECTION_USERNAME)) {
            usernameText.setText((String) evt.getNewValue());
        } else if(evt.getPropertyName().equals(LSConnection.PROPERTY_CONNECTION_PASSWORD)) {
            passwordText.setText((String) evt.getNewValue());
        } else if(evt.getPropertyName().equals(LSConnection.PROPERTY_SAVE_CONNECTION_PASSWORD)) {
            Boolean b = (Boolean) evt.getNewValue();
            savePasswordButton.setSelection(b != null && b.booleanValue());
        }
    }
    
    public void handleEvent(BusEvent event) {
        if(event instanceof ConfigurationChangeEvent) {
            ConfigurationChangeEvent evt = (ConfigurationChangeEvent) event;
            
            if(evt.isPreChange()) {
                return;
            }
            
            if(editing) {
                LSConnection connection = getTree().getConfig().getConnection();
                
                // get the username and password back out of the configuration because under
                // some circumstances the user may have changed them (from what the text fields
                // currently show) in the popup dialog without an event being sent out
                usernameText.setText(connection.getUsername());
                passwordText.setText(connection.getPassword());
                savePasswordButton.setSelection(connection.isSavePassword());
                
                // TODO change the tree state in the command instead of here?
                if(getTree().getState() == State.CONFIG_DATASOURCE) {
                    getTree().setState(State.CONFIG_COLUMNS);
                    sendRequest(new ChangePageRequest(1));
                } else if(evt.getSeverity() == ConfigurationChangeEvent.SEVERITY_TABLE_INFO_PARTIAL ||
                    evt.getSeverity() == ConfigurationChangeEvent.SEVERITY_TABLE_INFO_TOTAL) {
                    getTree().setState(State.CONFIG_COLUMNS);
                }
                
                sendRequest(new RefreshTreeRequest());
                
                setEditing(false);
            }
        }
    }
    
    protected void setEditing(boolean editing) {
        this.editing = editing;
        driverCombo.setEnabled(editing);
        uriText.setEnabled(editing);
        usernameText.setEnabled(editing);
        passwordText.setEnabled(editing);
        savePasswordButton.setEnabled(editing);
        queryText.setEnabled(editing);
        
        nextButton.setEnabled(!editing && getTree().getState() != State.CONFIG_DATASOURCE);
        editButton.setText(editing ? "Accept" : "Edit");
        cancelButton.setEnabled(editing && getTree().getState() != State.CONFIG_DATASOURCE);
        
        Color bg = getColorCache().getColor(editing ? new RGB(255, 255, 255) : new RGB(240, 242, 242));
        
        driverCombo.setBackground(bg);
        uriText.setBackground(bg);
        usernameText.setBackground(bg);
        passwordText.setBackground(bg);
        queryText.setBackground(bg);
    }
    
    public void activate() {
        setEditing(getTree().getState() == State.CONFIG_DATASOURCE);
    }
    
    @Override
    public void dispose() {
        super.dispose();
        if(getTree() != null) {
            getTree().removePropertyChangeListener(this);
            if(getTree().getConfig() != null) {
                getTree().getConfig().removePropertyChangeListener(this);
            }
        }
    }
    
    public void setFocus() {
        driverCombo.setFocus();
    }
}
