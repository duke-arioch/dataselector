package com.nexusbpm.dataselector.editor.pages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.nexusbpm.dataselector.events.ExceptionEvent;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.util.ColorCache;
import com.nexusbpm.multipage.bus.BusEvent;
import com.nexusbpm.multipage.bus.BusRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;
import com.nexusbpm.multipage.bus.UnhandledRequestException;
import com.nexusbpm.multipage.editor.EditorPage;

public abstract class AbstractEditorPage implements EditorPage {
    private LSTree tree;
    private EventRequestBus bus;
    private FormToolkit toolkit;
    private ColorCache colorCache;
    
    public AbstractEditorPage(LSTree tree) {
        this.tree = tree;
    }
    
    public void createPartControl(Composite parent) {
        toolkit = new FormToolkit(parent.getDisplay());
        colorCache = new ColorCache(parent.getDisplay());
    }
    
    protected FormToolkit getFormToolkit() {
        return toolkit;
    }
    
    protected ColorCache getColorCache() {
        return colorCache;
    }
    
    protected Object sendRequest(BusRequest request) {
        try {
            return bus.handleRequest(request);
        } catch(UnhandledRequestException e) {
            sendEvent(new ExceptionEvent("Invalid event bus configuration!", e));
            return null;
        }
    }
    
    protected void sendEvent(BusEvent event) {
        bus.handleEvent(event);
    }
    
    public void setEventRequestBus(EventRequestBus bus) {
        this.bus = bus;
    }
    
    protected EventRequestBus getEventRequestBus() {
        return bus;
    }
    
    protected LSTree getTree() {
        return tree;
    }
    
    protected Composite createButtonBar(Composite parent) {
        final Composite buttonBar = toolkit.createComposite(parent);
        
        Color tb1 = buttonBar.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
        Color tb2 = buttonBar.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
        
        RGB bg1 = FormColors.blend(tb1.getRGB(), tb2.getRGB(), 65);
        RGB bg2 = FormColors.blend(tb1.getRGB(), tb2.getRGB(), 35);
        
        int bright = 680;
        int dark = 600;
        
        int color1Brightness = dark;
        int color2Brightness = bright;
        if(bg1.red + bg1.green + bg1.blue > bg2.red + bg2.green + bg2.blue) {
            color1Brightness = bright;
            color2Brightness = dark;
        }
        
        while(bg1.red + bg1.green + bg1.blue < color1Brightness) {
            bg1 = FormColors.blend(bg1, new RGB(255, 255, 255), 85);
        }
        while(bg2.red + bg2.green + bg2.blue < color2Brightness) {
            bg2 = FormColors.blend(bg2, new RGB(255, 255, 255), 85);
        }
        
        final RGB rgb1 = bg1;
        final RGB rgb2 = bg2;
        
//        Composite buttonBar = new Composite(parent, SWT.NONE);
        buttonBar.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                Color fg = e.gc.getForeground();
                Color bg = e.gc.getBackground();
                
                e.gc.setForeground(colorCache.getColor(rgb1));
                e.gc.setBackground(colorCache.getColor(rgb2));
                
                Rectangle r = buttonBar.getBounds();
                
                e.gc.fillGradientRectangle(0, 0, r.width, r.height, false);
                
                e.gc.setForeground(fg);
                e.gc.setBackground(bg);
            }
        });
//        buttonBar.setLayoutData(createGridData(SWT.FILL, true));
//        buttonBar.setBackground(new Color(null, 240, 245, 250));
//        buttonBar.setBackground(colorCache.getColor(232, 236, 240));
//        parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
        
        createButtonBarContents(buttonBar);
        
        return buttonBar;
    }
    
    protected abstract void createButtonBarContents(Composite parent);
    
    public void dispose() {
        colorCache.dispose();
        toolkit.dispose();
    }
}
