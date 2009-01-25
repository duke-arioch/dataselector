package com.nexusbpm.database.driver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;

import com.nexusbpm.database.Activator;

public class DriverClassloader extends URLClassLoader {
    private static DriverClassloader instance;
    private static URL[] urls;
    
    private static synchronized void initialize() {
        Preferences prefs = Activator.getDefault().getPluginPreferences();
        if(urls == null) {
            String jars = prefs.getString("JDBC Jars");
            urls = getURLList(jars);
            instance = null;
        }
        if(listener == null) {
            listener = new PropertyChangeListener();
            prefs.addPropertyChangeListener(listener);
        }
    }
    
    private static URL[] getURLList(String jars) {
        StringTokenizer tokenizer = new StringTokenizer(jars, ",");
        List<URL> urlList = new ArrayList<URL>();
        while(tokenizer.hasMoreTokens()) {
            try {
                urlList.add(new File(tokenizer.nextToken()).toURL());
            } catch(MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return urlList.toArray(new URL[urlList.size()]);
    }
    
    private static synchronized DriverClassloader getInstance() {
        initialize();
        if(instance == null) {
            instance = new DriverClassloader(urls, Thread.currentThread().getContextClassLoader());
        }
        return instance;
    }
    
    private static synchronized void setURLs(URL[] urls) {
        DriverClassloader.urls = urls;
    }
    
    public static synchronized Connection getConnection(SQLDriver driver, String uri, String username, String password)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        return getConnection(driver.getDriverClassName(), uri, username, password);
    }
    
    public static synchronized Connection getConnection(String driver, String uri, String username, String password)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        DriverClassloader loader = getInstance();
        
        Driver d = (Driver) Class.forName(driver, true, loader).newInstance();
        Properties info = new Properties();
        info.setProperty("user", username);
        info.setProperty("password", password);
        return d.connect(uri, info);
    }
    
    public static synchronized Driver getDriver(SQLDriver driver, String jars)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return getDriver(driver.getDriverClassName(), jars);
    }
    
    public static synchronized Driver getDriver(String driverClassName, String jars)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        DriverClassloader loader =
            new DriverClassloader(getURLList(jars), Thread.currentThread().getContextClassLoader());
        
        return (Driver) Class.forName(driverClassName, true, loader).newInstance();
    }
    
    public static synchronized Driver getDriver(SQLDriver driver)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return getDriver(driver.getDriverClassName());
    }
    
    public static synchronized Driver getDriver(String driverClassName)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        DriverClassloader loader = getInstance();
        
        return (Driver) Class.forName(driverClassName, true, loader).newInstance();
    }
    
    private DriverClassloader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
    
    private static PropertyChangeListener listener;
    private static class PropertyChangeListener implements IPropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            if(event.getProperty().equals("JDBC Jars")) {
                setURLs(null);
            }
        }
    }
}
