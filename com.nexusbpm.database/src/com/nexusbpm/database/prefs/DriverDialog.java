package com.nexusbpm.database.prefs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DriverDialog extends Dialog {
    protected Text driverNameText;
    protected Text driverSampleConnectStringText;
    protected Text driverClassNameText;
    
    protected Label errorMessageLabel;
    
    protected String driverName;
    protected String driverSampleConnectString;
    protected String driverClassName;
    
    protected List<String> invalidNames;
    
    public DriverDialog(Shell parent) {
        super(parent);
    }
    
    public void setInvalidNames(List<String> names) {
        this.invalidNames = names;
    }
    
    public String getDriverName() {
        return driverName;
    }
    
    public String getDriverSampleConnectString() {
        return driverSampleConnectString;
    }
    
    public String getDriverClassName() {
        return driverClassName;
    }
    
    public void initialize(String driverName, String driverSampleConnectString, String driverClassName) {
        this.driverName = driverName;
        this.driverSampleConnectString = driverSampleConnectString;
        this.driverClassName = driverClassName;
    }
    
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Enter Driver Information");
    }
    
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        Control okButton = getButton(IDialogConstants.OK_ID);
        okButton.setEnabled(false);
        validateInput();
    }
    
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.numColumns = 1;
        
        GridData data;
        
        Label driverNameLabel = new Label(composite, SWT.LEFT);
        driverNameLabel.setText("Driver name:");
        data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        data.widthHint = 65;
        driverNameLabel.setLayoutData(data);
        driverNameLabel.setFont(parent.getFont());
        
        driverNameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        if(driverName != null) {
            driverNameText.setText(driverName);
        }
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.widthHint = 180;
        driverNameText.setLayoutData(data);
        driverNameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });
        
        Label driverSampleConnectStringLabel = new Label(composite, SWT.LEFT);
        driverSampleConnectStringLabel.setText("Sample Connect String:");
        data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        driverSampleConnectStringLabel.setLayoutData(data);
        driverSampleConnectStringLabel.setFont(parent.getFont());
        
        driverSampleConnectStringText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        if(driverSampleConnectString != null) {
            driverSampleConnectStringText.setText(driverSampleConnectString);
        }
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.widthHint = 150;
        driverSampleConnectStringText.setLayoutData(data);
        driverSampleConnectStringText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });
        
        Label driverClassNameLabel = new Label(composite, SWT.LEFT);
        driverClassNameLabel.setText("Driver class name:");
        data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        driverClassNameLabel.setLayoutData(data);
        driverClassNameLabel.setFont(parent.getFont());
        
        driverClassNameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        if(driverClassName != null) {
            driverClassNameText.setText(driverClassName);
        }
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.widthHint = 150;
        driverClassNameText.setLayoutData(data);
        driverClassNameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });
        
        errorMessageLabel = new Label(composite, SWT.LEFT | SWT.WRAP);
        data = new GridData(SWT.FILL, SWT.FILL, false, false);
        data.verticalIndent = 8;
        data.heightHint = 30;
        errorMessageLabel.setLayoutData(data);
        errorMessageLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
        
        applyDialogFont(composite);
        validateInput();
        return composite;
    }
    
    protected void validateInput() {
        String message = "";
        if(driverNameText != null) {
            String name = driverNameText.getText();
            if(name.length() == 0) {
                message = "A valid name must be specified.";
            } else if(name.startsWith(")")) {
                message = "The name cannot start with \")\"";
            } else if(name.endsWith("(")) {
                message = "The name cannot end with \"(\"";
            } else if(name.contains("|")) {
                message = "The name cannot contain \"|\"";
            } else if(invalidNames != null && invalidNames.contains(name)) {
                message = "The specified name is already in use.";
            }
        }
        
        if(driverSampleConnectStringText != null && message.length() == 0) {
            String sample = driverSampleConnectStringText.getText();
            if(sample.startsWith(")")) {
                message = "The sample string cannot start with \")\"";
            } else if(sample.endsWith("(")) {
                message = "The sample string cannot end with \"(\"";
            } else if(sample.contains("|")) {
                message = "The sample string cannot contain \"|\"";
            }
        }
        
        if(driverClassNameText != null && message.length() == 0) {
            String cname = driverClassNameText.getText();
            if(cname.length() == 0) {
                message = "A driver class name must be specified.";
            } else if(!cname.matches("[a-zA-Z][a-zA-Z0-9_.]*[a-zA-Z0-9]")) {
                message = "The driver class name specified is not valid.";
            }
        }
        
        if(errorMessageLabel != null) {
            errorMessageLabel.setText(message);
        }
        Control ok = getButton(IDialogConstants.OK_ID);
        if(ok != null) {
            ok.setEnabled(message.length() == 0);
        }
    }
    
    protected void buttonPressed(int buttonId) {
        if(buttonId == IDialogConstants.OK_ID) {
            driverName = driverNameText.getText();
            driverSampleConnectString = driverSampleConnectStringText.getText();
            driverClassName = driverClassNameText.getText();
        } else {
            driverName = null;
            driverSampleConnectString = null;
            driverClassName = null;
        }
        super.buttonPressed(buttonId);
    }
}
