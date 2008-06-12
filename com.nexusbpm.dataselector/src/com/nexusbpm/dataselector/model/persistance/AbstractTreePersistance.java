package com.nexusbpm.dataselector.model.persistance;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import com.nexusbpm.dataselector.Plugin;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.model.config.LSConfig;
import com.nexusbpm.dataselector.model.config.LSConnection;
import com.nexusbpm.dataselector.model.config.LSDriver;

/**
 * Provides an implementation of {@link TreePersistance#getNewTree()}.
 */
public abstract class AbstractTreePersistance implements TreePersistance {
    private Map<String, ModelExtensionPersistance> extensionPersistance;
    
    public AbstractTreePersistance() {
    }
    
    protected Map<String, ModelExtensionPersistance> getExtensionPersistance() throws CoreException {
        if(extensionPersistance == null) {
            extensionPersistance = new HashMap<String, ModelExtensionPersistance>();
            IExtensionPoint point =
                Platform.getExtensionRegistry().getExtensionPoint(Plugin.PLUGIN_ID, "modelExtension");
            IExtension[] extensions = point.getExtensions();
            for(int index = 0; index < extensions.length; index++) {
                IConfigurationElement[] elements = extensions[index].getConfigurationElements();
                for(int i = 0; i < elements.length; i++) {
                    String id = elements[i].getAttribute("id");
                    ModelExtensionPersistance persistance =
                        (ModelExtensionPersistance) elements[i].createExecutableExtension("class");
                    extensionPersistance.put(id, persistance);
                }
            }
        }
        return extensionPersistance;
    }
    
    public LSTree getNewTree() {
        LSTree tree = new LSTree();
        LSConfig config = new LSConfig(tree);
        LSConnection connection = new LSConnection(config);
        config.setConnection(connection);
        LSDriver driver = new LSDriver(config);
        config.setDriver(driver);
        tree.setConfig(config);
        return tree;
    }
}
