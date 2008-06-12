package com.nexusbpm.dataselector.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.nexusbpm.dataselector.Plugin;

public class ImageCache {
    private Map<String, Image> cache;
    private Map<String, ImageDescriptor> descriptorCache;
    private ImageDescriptor missingImageDescriptor;
    
    private String pluginID;
    
    public ImageCache() {
        this(Plugin.PLUGIN_ID);
    }
    
    public ImageCache(String pluginID) {
        this.pluginID = pluginID;
        cache = new HashMap<String, Image>();
        descriptorCache = new HashMap<String, ImageDescriptor>();
    }
    
    protected void checkDisposed() {
        if(cache == null || descriptorCache == null) {
            throw new IllegalStateException("ImageCache is disposed!");
        }
    }
    
    public synchronized Image getImage(String path) {
        checkDisposed();
        Image image = null;
        try {
            if(cache.get(path) == null) {
                ImageDescriptor descriptor = getImageDescriptor(path);
                if(descriptor != getMissingImageDescriptor()) {
                    cache.put(path, descriptor.createImage(true, Display.getCurrent()));
                }
            }
            image = cache.get(path);
            if(image == null) {
                image = getMissingImageDescriptor().createImage();
            }
        } catch(Exception e) {
//            Plugin.logError("", e);
        }
        return image;
    }
    
    public synchronized ImageDescriptor getImageDescriptor(String path) {
        checkDisposed();
        ImageDescriptor descriptor = null;
        if(descriptorCache.get(path) == null && path != null) {
            descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(pluginID, path);
            if(descriptor != null) {
                descriptorCache.put(path, descriptor);
            }
        }
        descriptor = descriptorCache.get(path);
        if(descriptor == null) {
            descriptor = getMissingImageDescriptor();
        }
        return descriptor;
    }
    
    public synchronized ImageDescriptor getMissingImageDescriptor() {
        checkDisposed();
        if(missingImageDescriptor == null) {
            missingImageDescriptor = ImageDescriptor.getMissingImageDescriptor();
        }
        return missingImageDescriptor;
    }
    
    public synchronized void dispose() {
        if(cache != null) {
            for(Image image : cache.values()) {
                image.dispose();
            }
        }
        cache = null;
        descriptorCache = null;
        missingImageDescriptor = null;
    }
    
    public synchronized boolean isDisposed() {
        return cache == null;
    }
}
