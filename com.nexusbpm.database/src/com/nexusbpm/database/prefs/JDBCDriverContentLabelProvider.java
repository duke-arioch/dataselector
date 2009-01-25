package com.nexusbpm.database.prefs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import com.nexusbpm.database.driver.DriverClassloader;
import com.nexusbpm.database.driver.SQLDriver;

public class JDBCDriverContentLabelProvider extends CellLabelProvider implements IStructuredContentProvider {
    protected List<SQLDriverWrapper> drivers;
    
    protected String jarsList;
    
    protected Font standardFont;
    protected Font boldFont;
    
    public JDBCDriverContentLabelProvider(Font standardFont) {
        drivers = new ArrayList<SQLDriverWrapper>();
        jarsList = "";
        
        for(SQLDriver d : SQLDriver.getDrivers()) {
            drivers.add(new SQLDriverWrapper(d));
        }
        
        this.standardFont = standardFont;
        this.boldFont = deriveBoldFont(standardFont);
    }
    
    protected Font deriveBoldFont(Font original) {
        FontData[] data = original.getFontData();
        FontData[] d2 = new FontData[data.length];
        
        for(int index = 0; index < data.length; index++) {
            FontData d = data[index];
            d2[index] = new FontData(d.getName(), d.getHeight(), d.getStyle() | SWT.BOLD);
        }
        return new Font(original.getDevice(), d2);
    }
    
    public void addDriver(SQLDriverWrapper driver) {
        drivers.add(driver);
    }
    
    public void removeDrivers(List<SQLDriverWrapper> drivers) {
        this.drivers.removeAll(drivers);
    }
    
    public List<String> getDriverNames() {
        List<String> names = new ArrayList<String>();
        
        for(SQLDriverWrapper driver : drivers) {
            names.add(driver.getName());
        }
        
        return names;
    }
    
    public List<SQLDriverWrapper> getDrivers() {
        return new ArrayList<SQLDriverWrapper>(drivers);
    }
    
    @Override
    public void update(ViewerCell cell) {
        SQLDriverWrapper driver = (SQLDriverWrapper) cell.getElement();
        if(driver.isBuiltIn() && cell.getColumnIndex() == 0 && boldFont != null) {
            cell.setFont(boldFont);
        } else {
            cell.setFont(standardFont);
        }
        cell.setText(getColumnText(driver, cell.getColumnIndex()));
    }
    
    public String getColumnText(Object element, int columnIndex) {
        String text = "";
        if(element instanceof SQLDriverWrapper) {
            SQLDriverWrapper driver = (SQLDriverWrapper) element;
            switch(columnIndex) {
            case 0:
                text = driver.getName();
                break;
            case 1:
                try {
                    DriverClassloader.getDriver(driver.getDriverClassName(), jarsList);
                    text = "yes";
                } catch(Throwable t) {
                    text = "no";
                }
                break;
            }
        }
        return text;
    }
    
    @Override
    public String getToolTipText(Object element) {
        if(element instanceof SQLDriverWrapper) {
            return ((SQLDriverWrapper) element).getDriverClassName();
        }
        return null;
    }
    
    @Override
    public boolean useNativeToolTip(Object object) {
        return true;
    }
    
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if(newInput instanceof String) {
            jarsList = (String) newInput;
        } else {
            jarsList = "";
        }
    }
    public Object[] getElements(Object inputElement) {
        return drivers.toArray();
    }
    
    public void dispose() {
        // dispose is called twice, since this is both a label provider and content provider,
        // so check if the font has already been disposed
        if(boldFont != null) {
            boldFont.dispose();
            boldFont = null;
        }
    }
}
