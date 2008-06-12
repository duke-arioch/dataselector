package com.nexusbpm.dataselector.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

public class FontCache {
    private Device device;
    private Map<String, Font> cache;
    
    public FontCache(Device device) {
        this.device = device;
        cache = new HashMap<String, Font>();
    }
    
    public synchronized Font getFont(int size, int style) {
        if(cache == null) {
            throw new IllegalStateException("FontCache was disposed!");
        }
        String key = size + "," + style;
        if(cache.get(key) == null) {
            FontData fontData = new FontData();
            fontData.setStyle(style);
            fontData.setHeight(size);
            cache.put(key, new Font(device, fontData));
        }
        return cache.get(key);
    }
    
    public synchronized Font getFont(int size, int style, String name) {
        if(cache == null) {
            throw new IllegalStateException("FontCache was disposed!");
        }
        String key = size + "," + style + "," + name;
        if(cache.get(key) == null) {
            FontData fontData = new FontData();
            fontData.setStyle(style);
            fontData.setHeight(size);
            fontData.setName(name);
            cache.put(key, new Font(device, fontData));
        }
        return cache.get(key);
    }
    
    public synchronized void dispose() {
        for(Font font : cache.values()) {
            font.dispose();
        }
        cache = null;
    }
    
    public synchronized boolean isDisposed() {
        return cache == null;
    }
}
