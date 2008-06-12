package com.nexusbpm.dataselector.animation;

import java.util.HashSet;
import java.util.Set;

public class AnimationManager {
    private long delay;
    private int frames;
    private int currentFrame;
    private NotificationThread thread;
    private Set<AnimationEventListener> listeners;
    private boolean disposed;
    
    public AnimationManager(long delay, int frames) {
        this.delay = delay;
        this.frames = frames;
        listeners = new HashSet<AnimationEventListener>();
    }
    
    public synchronized void addListener(AnimationEventListener listener) {
        if(disposed) return;
        if(!listeners.contains(listener)) {
            listeners.add(listener);
            if(thread == null) {
                thread = new NotificationThread();
                thread.start();
            }
        }
    }
    
    public synchronized void removeListener(AnimationEventListener listener) {
        if(disposed) return;
        listeners.remove(listener);
        if(thread != null && listeners.isEmpty()) {
            thread.shutdown();
            thread.interrupt();
            thread = null;
        }
    }
    
    protected synchronized void notifyAnimations(long delay, int frames, int currentFrame) {
        if(disposed) return;
        for(AnimationEventListener listener : listeners) {
            listener.animate(delay, frames, currentFrame);
        }
    }
    
    protected class NotificationThread extends Thread {
        private boolean shutdown = false;
        
        public void run() {
            try {
                long nextTime = System.currentTimeMillis() - 1;
                while(!disposed && !isShutdown()) {
                    AnimationManager.this.notifyAnimations(delay, frames, currentFrame);
                    long currentTime = System.currentTimeMillis();
                    while(nextTime < currentTime) {
                        nextTime += delay;
                        currentFrame++;
                        currentFrame %= frames;
                    }
                    if(nextTime - currentTime > 0) {
                        Thread.sleep(nextTime - currentTime);
                    }
                }
            } catch(InterruptedException e) {
            }//end the current animation
        }
        
        public synchronized boolean isShutdown() {
            return shutdown;
        }
        
        public synchronized void shutdown() {
            shutdown = true;
        }
    }
    
    public synchronized void dispose() {
        disposed = true;
        if(thread != null) {
            thread.shutdown();
            thread = null;
        }
        listeners.clear();
        listeners = null;
    }
    
    public synchronized boolean isDisposed() {
        return disposed;
    }
}
