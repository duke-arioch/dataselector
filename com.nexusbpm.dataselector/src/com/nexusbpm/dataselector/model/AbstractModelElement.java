package com.nexusbpm.dataselector.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.nexusbpm.dataselector.events.ExtensionChangeEvent;

public abstract class AbstractModelElement {
    public static final String PROPERTY_EXTENSION_ADDED = "elementExtensionAdded";
    public static final String PROPERTY_EXTENSION_REMOVED = "elementExtensionRemoved";
    
    private PropertyChangeSupport changeSupport;
    private AbstractModelElement parent;
    private Map<String, Map<String, AbstractModelExtension>> extensionMaps;
    
    protected AbstractModelElement(AbstractModelElement parent) {
        this.parent = parent;
        changeSupport = new PropertyChangeSupport(this);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        for(PropertyChangeListener l : changeSupport.getPropertyChangeListeners()) {
            if(l == listener) return;
        }
        PropertyChangeListener[] before = changeSupport.getPropertyChangeListeners();
        changeSupport.addPropertyChangeListener(listener);
        PropertyChangeListener[] after = changeSupport.getPropertyChangeListeners();
        System.out.println("inc from " + before.length + " to " + after.length + " " + this + " " + listener);
    }
    
    public void firePropertyChange(PropertyChangeEvent evt) {
        changeSupport.firePropertyChange(evt);
    }
    
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        PropertyChangeListener[] before = changeSupport.getPropertyChangeListeners();
        changeSupport.removePropertyChangeListener(listener);
        PropertyChangeListener[] after = changeSupport.getPropertyChangeListeners();
        System.out.println("dec from " + before.length + " to " + after.length + " " + this + " " + listener);
    }
    
    public AbstractModelElement getParent() {
        return parent;
    }
    
    public void setParent(AbstractModelElement parent) {
        this.parent = parent;
    }
    
    public AbstractModelExtension getExtension(String contributorId, String extensionId) {
        AbstractModelExtension extension = null;
        if(extensionMaps != null && extensionMaps.get(contributorId) != null) {
            extension = extensionMaps.get(contributorId).get(extensionId);
        }
        return extension;
    }
    
    /**
     * Returns a convenience map that is backed by this object's extensions belonging
     * to the specified contributor.
     */
    public Map<String, AbstractModelExtension> getExtensionMap(String contributorId) {
        return new ExtensionMap(contributorId);
    }
    
    public void setExtension(String contributorId, String extensionId, AbstractModelExtension extension) {
        AbstractModelExtension oldValue = getExtension(contributorId, extensionId);
        if(extensionMaps == null) {
            extensionMaps = new HashMap<String, Map<String,AbstractModelExtension>>();
        }
        if(extensionMaps.get(contributorId) == null) {
            extensionMaps.put(contributorId, new HashMap<String, AbstractModelExtension>());
        }
        extensionMaps.get(contributorId).put(extensionId, extension);
        firePropertyChange(new ExtensionChangeEvent(
                this, PROPERTY_EXTENSION_ADDED, oldValue, extension, contributorId, extensionId));
    }
    
    public void removeExtension(String contributorId, String extensionId) {
        if(extensionMaps != null) {
            if(extensionMaps.get(contributorId) != null) {
                AbstractModelExtension oldValue = extensionMaps.get(contributorId).remove(extensionId);
                if(oldValue != null) {
                    if(extensionMaps.get(contributorId).size() == 0) {
                        extensionMaps.remove(contributorId);
                        if(extensionMaps.size() == 0) {
                            extensionMaps = null;
                        }
                    }
                    firePropertyChange(new ExtensionChangeEvent(
                            this, PROPERTY_EXTENSION_REMOVED, oldValue, null, contributorId, extensionId));
                }
            }
        }
    }
    
    public LSTree getTree() {
        if(parent != null) {
            return parent.getTree();
        } else {
            throw new IllegalStateException("Model element not connected to tree!");
        }
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + hashCode();
    }
    
    protected class ExtensionMap implements Map<String, AbstractModelExtension> {
        private String contributorId;
        public ExtensionMap(String contributorId) {
            this.contributorId = contributorId;
        }
        public void clear() {
            if(extensionMaps != null && extensionMaps.get(contributorId) != null) {
                for(String extensionId : new HashSet<String>(extensionMaps.get(contributorId).keySet())) {
                    removeExtension(contributorId, extensionId);
                }
            }
        }
        public boolean containsKey(Object key) {
            return getMap().containsKey(key);
        }
        public boolean containsValue(Object value) {
            return getMap().containsValue(value);
        }
        public Set<Entry<String, AbstractModelExtension>> entrySet() {
            return getMap().entrySet();
        }
        public AbstractModelExtension get(Object key) {
            return getMap().get(key);
        }
        public boolean isEmpty() {
            return getMap().isEmpty();
        }
        public Set<String> keySet() {
            return getMap().keySet();
        }
        public AbstractModelExtension put(String key, AbstractModelExtension value) {
            AbstractModelExtension ext = getExtension(contributorId, key);
            setExtension(contributorId, key, value);
            return ext;
        }
        public void putAll(Map<? extends String, ? extends AbstractModelExtension> map) {
            for(Entry<? extends String, ? extends AbstractModelExtension> entry : map.entrySet()) {
                setExtension(contributorId, entry.getKey(), entry.getValue());
            }
        }
        public AbstractModelExtension remove(Object key) {
            AbstractModelExtension extension = null;
            if(key instanceof String || key == null) {
                extension = getExtension(contributorId, (String) key);
            }
            return extension;
        }
        public int size() {
            return getMap().size();
        }
        public Collection<AbstractModelExtension> values() {
            return getMap().values();
        }
        protected Map<String, AbstractModelExtension> getMap() {
            if(extensionMaps != null && extensionMaps.get(contributorId) != null) {
                return extensionMaps.get(contributorId);
            } else {
                return Collections.emptyMap();
            }
        }
    }
}
