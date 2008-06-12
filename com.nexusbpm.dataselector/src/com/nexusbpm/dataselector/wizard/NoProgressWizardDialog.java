package com.nexusbpm.dataselector.wizard;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class NoProgressWizardDialog extends WizardDialog {
    public NoProgressWizardDialog(Shell parentShell, IWizard newWizard) {
        super(parentShell, newWizard);
    }
    
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setFont(parent.getFont());
        // Build the separator line
        Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
        titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        // Build the Page container
        Composite pageContainer = (Composite) executeMethod(
                "createPageContainer",
                new Class[] {Composite.class},
                new Object[] {composite});
        setField("pageContainer", pageContainer);
//        pageContainer = createPageContainer(composite);
        pageContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
        pageContainer.setFont(parent.getFont());
        // Insert a progress monitor
        GridLayout pmlayout = new GridLayout();
        pmlayout.numColumns = 1;
        // Build the separator line
        Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }
    
    @Override
    public void updateButtons() {
        super.updateButtons();
        if(getCurrentPage() instanceof DefaultButtonWizardPage) {
            ((DefaultButtonWizardPage) getCurrentPage()).updateDefaultButton();
        }
    }
    
    @Override
    protected Point getInitialSize() {
        Point p = super.getInitialSize();
        if(p.y > 500) {
            p.y = 500;
        }
        if(p.x < 425) {
            p.x = 425;
        } else if(p.x > 525) {
            p.x = 525;
        }
        return p;
    }
    
    public static interface DefaultButtonWizardPage {
        void updateDefaultButton();
    }
    
    @SuppressWarnings("all")
    protected Object executeMethod(String methodName, Class[] paramTypes, Object[] params) {
        Class c = getClass();
        while(c != null) {
            try {
                Method m = c.getDeclaredMethod(methodName, paramTypes);
                if(m != null) {
                    m.setAccessible(true);
                    return m.invoke(this, params);
                }
            } catch(NoSuchMethodException e) {
            } catch(Exception e) {
                e.printStackTrace();
            }
            c = c.getSuperclass();
        }
        return null;
    }
    
    @SuppressWarnings("all")
    protected void setField(String fieldName, Object value) {
        Class c = getClass();
        while(c != null) {
            try {
                Field f = c.getDeclaredField(fieldName);
                if(f != null) {
                    f.setAccessible(true);
                    f.set(this, value);
                    return;
                }
            } catch(NoSuchFieldException e) {
            } catch(Exception e) {
                e.printStackTrace();
            }
            c = c.getSuperclass();
        }
    }
}
