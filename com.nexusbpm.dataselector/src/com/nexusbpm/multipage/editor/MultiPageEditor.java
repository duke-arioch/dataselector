package com.nexusbpm.multipage.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.nexusbpm.dataselector.events.ExceptionEvent;
import com.nexusbpm.multipage.bus.BusEvent;
import com.nexusbpm.multipage.bus.BusRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;
import com.nexusbpm.multipage.bus.UnhandledRequestException;

public abstract class MultiPageEditor extends EditorPart {
    private boolean dirty;
    
    private int currentPageIndex = -1;
    private EditorPage[] pages;
    private Control[] controls;
    
    private Composite parent;
    private StackLayout layout;
    
    private EventRequestBus bus;
    
    public MultiPageEditor() {
        pages = new EditorPage[0];
        controls = new Control[0];
    }
    
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
        setPartName(input.getName());
        setTitleToolTip(input.getToolTipText());
    }
    
    @Override
    public boolean isDirty() {
        return dirty;
    }
    
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        firePropertyChange(PROP_DIRTY);
    }
    
    @Override
    public void createPartControl(Composite parent) {
        this.parent = new Composite(parent, SWT.NONE);
        parent.setLayout(new FillLayout());
        this.parent.setLayout(layout = new StackLayout());
        
        createEditorPages();
    }
    
    protected void addPage(EditorPage page) {
        EditorPage[] pages = new EditorPage[this.pages.length + 1];
        Control[] controls = new Control[this.controls.length + 1];
        
        System.arraycopy(this.pages, 0, pages, 0, this.pages.length);
        System.arraycopy(this.controls, 0, controls, 0, this.controls.length);
        
        page.setEventRequestBus(getEventRequestBus());
        Composite pageComposite = new Composite(parent, SWT.NONE);
        pageComposite.setLayout(new FillLayout());
        
        page.createPartControl(pageComposite);
        
        pages[pages.length - 1] = page;
        controls[controls.length - 1] = pageComposite;
        
        this.pages = pages;
        this.controls = controls;
        
        if(currentPageIndex == -1) {
            setCurrentPage(0);
        }
    }
    
    protected EditorPage getPage(int page) {
        return pages[page];
    }
    
    protected EditorPage[] getPages() {
        EditorPage[] pages = new EditorPage[this.pages.length];
        System.arraycopy(this.pages, 0, pages, 0, pages.length);
        return pages;
    }
    
    protected int getIndex(EditorPage page) {
        for(int index = 0; index < pages.length; index++) {
            if(pages[index] == page) {
                return index;
            }
        }
        throw new IllegalArgumentException("Page is not part of this editor: " + page);
    }
    
    protected void setCurrentPage(EditorPage page) {
        setCurrentPage(getIndex(page));
    }
    
    protected void setCurrentPage(int page) {
        layout.topControl = controls[page];
        currentPageIndex = page;
        pages[page].activate();
        parent.layout();
    }
    
    protected int getCurrentPageIndex() {
        return currentPageIndex;
    }
    
    protected EditorPage getCurrentPage() {
        return pages[currentPageIndex];
    }
    
    protected abstract void createEditorPages();
    
    protected EventRequestBus getEventRequestBus() {
        if(bus == null) {
            bus = new EventRequestBus();
        }
        return bus;
    }
    
    protected void sendEvent(BusEvent event) {
        getEventRequestBus().handleEvent(event);
    }
    
    protected void sendRequest(BusRequest request) {
        try {
            getEventRequestBus().handleRequest(request);
        } catch(UnhandledRequestException e) {
            sendEvent(new ExceptionEvent("bus not configured", e));
        }
    }
    
    @Override
    public void setFocus() {
        pages[currentPageIndex].setFocus();
    }
    
    @Override
    public void dispose() {
        for(int index = 0; index < pages.length; index++) {
            pages[index].dispose();
        }
    }
}
