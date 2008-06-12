package com.nexusbpm.dataselector.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.RGB;

public class ColorCache {
    private Device device;
    private Map<RGB, Color> cache;
    
    public ColorCache(Device device) {
        this.device = device;
        cache = new HashMap<RGB, Color>();
    }
    
    public synchronized Color getColor(int red, int green, int blue) {
        return getColor(new RGB(red, green, blue));
    }
    
    public synchronized Color getColor(RGB rgb) {
        if(cache == null) {
            throw new IllegalStateException("ColorCache was disposed!");
        }
        if(cache.get(rgb) == null) {
            cache.put(rgb, new Color(device, rgb));
        }
        return cache.get(rgb);
    }
    
    public synchronized void dispose() {
        for(Color color : cache.values()) {
            color.dispose();
        }
        cache = null;
    }
    
    public synchronized boolean isDisposed() {
        return cache == null;
    }
}
