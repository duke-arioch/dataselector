package com.nexusbpm.dataselector.animation;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Widget;

public class AnimationManagerFactory implements DisposeListener {
//    private static AnimationManagerFactory INSTANCE;
    
    private Map<String, AnimationManager> managers;
    private AnimationManager disposedManager;
    private boolean disposed;
    
    public AnimationManagerFactory(Widget widget) {
        managers = new HashMap<String, AnimationManager>();
        widget.addDisposeListener(this);
    }
    
//    public static synchronized AnimationManagerFactory getInstance() {
//        if(INSTANCE == null) {
//            INSTANCE = new AnimationManagerFactory();
//        }
//        return INSTANCE;
//    }
    
    public synchronized AnimationManager getAnimationManager(String key) {
        if(disposed) {
            return getDisposedManager();
        }
        return managers.get(key);
    }
    
    public synchronized AnimationManager getAnimationManager(String key, long delay, int frames) {
        if(disposed) {
            return getDisposedManager();
        }
        if(managers.get(key) == null) {
            managers.put(key, new AnimationManager(delay, frames));
        }
        return managers.get(key);
    }
    
    public synchronized void widgetDisposed(DisposeEvent e) {
        disposed = true;
        if(managers != null) {
            for(AnimationManager manager : managers.values()) {
                manager.dispose();
            }
            managers.clear();
            managers = null;
        }
    }
    
    protected AnimationManager getDisposedManager() {
        if(disposedManager == null) {
            disposedManager = new DisposedAnimationManager();
        }
        return disposedManager;
    }
    
    protected class DisposedAnimationManager extends AnimationManager {
        public DisposedAnimationManager() {
            super(1000, 2);
            dispose();
        }
        @Override
        public void addListener(AnimationEventListener listener) {
        }
        
        @Override
        public boolean isDisposed() {
            return true;
        }
        
        @Override
        protected void notifyAnimations(long delay, int frames, int currentFrame) {
        }
        
        @Override
        public void removeListener(AnimationEventListener listener) {
        }
    }
}
