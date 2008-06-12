package com.nexusbpm.dataselector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Plugin extends AbstractUIPlugin {
    // The plug-in ID
    public static final String PLUGIN_ID = "com.nexusbpm.dataselector";
    
    // The shared instance
    private static Plugin plugin;
    
    /**
     * The constructor
     */
    public Plugin() {
    }
    
    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }
    
    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }
    
    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Plugin getDefault() {
        return plugin;
    }
    
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
    
    public static void logInfo(String msg, Throwable t) {
        IStatus status = new Status(Status.INFO, PLUGIN_ID, Status.OK, msg, t);
        getDefault().getLog().log(status);
    }
    
    public static void logWarning(String msg, Throwable t) {
        IStatus status = new Status(Status.WARNING, PLUGIN_ID, Status.WARNING, msg, t);
        getDefault().getLog().log(status);
    }
    
    public static void logError(String msg, Throwable t) {
        IStatus status = new Status(Status.ERROR, PLUGIN_ID, Status.ERROR, msg, t);
        getDefault().getLog().log(status);
    }
}
