package com.nexusbpm.dataselector.util;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LogonDialog extends Dialog {
    private Text usernameText;
    private Text passwordText;
    private Button savePasswordButton;
    
    private String username;
    private String password;
    private boolean savePassword;
    
    public LogonDialog(Shell parent) {
        super(parent);
    }
    
    public LogonDialog(Shell parent, String username, String password, boolean savePassword) {
        super(parent);
        this.username = username;
        this.password = password;
        this.savePassword = savePassword;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isSavePassword() {
        return savePassword;
    }
    
    public void setSavePassword(boolean savePassword) {
        this.savePassword = savePassword;
    }
    
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Enter login credentials");
    }
    
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        Control okButton = getButton(IDialogConstants.OK_ID);
        okButton.setEnabled(usernameText != null && usernameText.getText().length() > 0);
    }
    
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.numColumns = 2;
        
        GridData data;
        
        Label usernameLabel = new Label(composite, SWT.LEFT);
        usernameLabel.setText("Username:");
        data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        data.widthHint = 65;
        usernameLabel.setLayoutData(data);
        usernameLabel.setFont(parent.getFont());
        
        usernameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        if(username != null) {
            usernameText.setText(username);
        }
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.widthHint = 150;
        usernameText.setLayoutData(data);
        usernameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });
        
        Label passwordLabel = new Label(composite, SWT.LEFT);
        passwordLabel.setText("Password:");
        data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        passwordLabel.setLayoutData(data);
        passwordLabel.setFont(parent.getFont());
        
        passwordText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        if(password != null) {
            passwordText.setText(password);
        }
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.widthHint = 150;
        passwordText.setLayoutData(data);
        passwordText.setEchoChar('*');
        passwordText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });
        
        new Label(composite, SWT.CENTER);
        
        savePasswordButton = new Button(composite, SWT.CHECK);
        savePasswordButton.setText("Save Password");
        savePasswordButton.setSelection(savePassword);
        data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.widthHint = 150;
        savePasswordButton.setLayoutData(data);
        
        validateInput();
        
        applyDialogFont(composite);
        return composite;
    }
    
    protected String notNull(String param) {
        return param == null ? "" : param;
    }
    
    protected void validateInput() {
        if(getButton(IDialogConstants.OK_ID) != null) {
            getButton(IDialogConstants.OK_ID).setEnabled(usernameText.getText().length() > 0);
        }
    }
    
    protected void buttonPressed(int buttonId) {
        if(buttonId == IDialogConstants.OK_ID) {
            username = usernameText.getText();
            password = passwordText.getText();
            savePassword = savePasswordButton.getSelection();
        } else {
            username = null;
            password = null;
            savePassword = false;
        }
        super.buttonPressed(buttonId);
    }
}
