package com.nexusbpm.dataselector.editor.pages;

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.nexusbpm.dataselector.actions.ActionRegistry;
import com.nexusbpm.dataselector.animation.AnimationManagerFactory;
import com.nexusbpm.dataselector.controller.ControllerFactory;
import com.nexusbpm.dataselector.controller.GraphUpdateQueue;
import com.nexusbpm.dataselector.editor.DataSelectorMenuProvider;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.requests.ChangePageRequest;
import com.nexusbpm.dataselector.requests.GetGraphUpdateQueueRequest;
import com.nexusbpm.dataselector.util.FontCache;
import com.nexusbpm.dataselector.util.ImageCache;
import com.nexusbpm.multipage.bus.BusRequest;
import com.nexusbpm.multipage.bus.BusRequestHandler;

public class TreePage extends AbstractEditorPage implements SelectionListener {
    private GraphicalViewer graphicalViewer;
    private ActionRegistry actionRegistry;
    
    private Button previousButton;
    private Button nextButton;
    private Button editButton;
    private Button cancelButton;
    
    private EditDomain editDomain;
    private IWorkbenchPartSite site;
    
    public TreePage(LSTree tree, EditDomain editDomain, IWorkbenchPartSite site) {
        super(tree);
        this.editDomain = editDomain;
        this.site = site;
    }
    
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        parent.setLayout(layout);
        
        FormToolkit toolkit = getFormToolkit();
        
        graphicalViewer = new ScrollingGraphicalViewer();
        graphicalViewer.setProperty("imageCache", new ImageCache());
        graphicalViewer.setProperty("colorCache", getColorCache());
        graphicalViewer.setProperty("fontCache", new FontCache(parent.getDisplay()));
        graphicalViewer.setProperty("updateQueue", new GraphUpdateQueue());
        getEventRequestBus().addRequestHandler(new BusRequestHandler() {
            public boolean canHandleRequest(BusRequest request) {
                return request instanceof GetGraphUpdateQueueRequest;
            }
            public Object handleRequest(BusRequest request) {
                if(request instanceof GetGraphUpdateQueueRequest) {
                    GetGraphUpdateQueueRequest r = (GetGraphUpdateQueueRequest) request;
                    r.setQueue((GraphUpdateQueue) graphicalViewer.getProperty("updateQueue"));
                }
                return null;
            }
        });
        ScalableRootEditPart part = new ScalableRootEditPart();
        part.getZoomManager().setZoomLevels(
                new double[] {0.1, 0.125, 0.25, 0.5, 1.0, 1.5, 2.0, 3.0, 4.0, 6.0, 8.0});
        graphicalViewer.setRootEditPart(part);
        graphicalViewer.createControl(parent);
        graphicalViewer.setProperty(
                "animationManagerFactory",
                new AnimationManagerFactory(graphicalViewer.getControl()));
        graphicalViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        toolkit.adapt(graphicalViewer.getControl(), false, false);
        graphicalViewer.setEditPartFactory(new ControllerFactory());
        graphicalViewer.setContents(getTree());
        editDomain.addViewer(graphicalViewer);
        
        // set the selection tool as the active tool
        getActionRegistry().getAction(ActionRegistry.SELECTION_TOOL_ID).run();
        
        graphicalViewer.setContextMenu(new DataSelectorMenuProvider(graphicalViewer, getActionRegistry()));
        
        site.setSelectionProvider(graphicalViewer);
        
        // create the bottom separator
        toolkit.createLabel(parent, "", SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        
//        // create the button bar
//        Composite buttonBar = toolkit.createComposite(parent);
//        buttonBar.setLayoutData(createGridData(SWT.FILL, true));
//        buttonBar.setBackground(new Color(null, 240, 245, 250));
        
        createButtonBar(parent).setLayoutData(createGridData(SWT.FILL, true));
    }
    
    protected void createButtonBarContents(Composite parent) {
        parent.setLayout(new GridLayout(10, false));
        
        FormToolkit toolkit = getFormToolkit();
        Label separator = toolkit.createLabel(parent, "", SWT.LEFT);
        separator.setLayoutData(createGridData(SWT.FILL, true));
        separator.setVisible(false);
        
        previousButton = toolkit.createButton(parent, "<< Previous", SWT.PUSH);
        previousButton.setLayoutData(new GridData(85, SWT.DEFAULT));
        previousButton.addSelectionListener(this);
        
        nextButton = toolkit.createButton(parent, "Next >>", SWT.PUSH);
        nextButton.setLayoutData(new GridData(85, SWT.DEFAULT));
        nextButton.addSelectionListener(this);
        nextButton.setEnabled(false);
        
        editButton = toolkit.createButton(parent, "Edit", SWT.PUSH);
        editButton.setLayoutData(new GridData(85, SWT.DEFAULT));
        editButton.addSelectionListener(this);
        editButton.setEnabled(false);
        
        cancelButton = toolkit.createButton(parent, "Cancel", SWT.PUSH);
        cancelButton.setLayoutData(new GridData(85, SWT.DEFAULT));
        cancelButton.addSelectionListener(this);
        cancelButton.setEnabled(false);
    }
    
    protected GridData createGridData(int horizontalAlignment, boolean grabHorizontal) {
        return createGridData(horizontalAlignment, grabHorizontal, 1);
    }
    
    protected GridData createGridData(int horizontalAlignment, boolean grabHorizontal, int horizontalSpan) {
        return new GridData(horizontalAlignment, SWT.CENTER, grabHorizontal, false, horizontalSpan, 1);
    }
    
    public ActionRegistry getActionRegistry() {
        if(actionRegistry == null) {
            actionRegistry = new ActionRegistry(
                    editDomain,
                    graphicalViewer,
                    getEventRequestBus());
        }
        return actionRegistry;
    }
    
    public void widgetDefaultSelected(SelectionEvent e) {
        System.out.println("d:" + e);
    }
    
    public void widgetSelected(SelectionEvent e) {
        if(e.widget == previousButton) {
            graphicalViewer.deselectAll();
            sendRequest(new ChangePageRequest(1));
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
        ((GraphUpdateQueue) graphicalViewer.getProperty("updateQueue")).shutdown();
        ((ImageCache) graphicalViewer.getProperty("imageCache")).dispose();
        ((FontCache) graphicalViewer.getProperty("fontCache")).dispose();
    }
    
    public void activate() {
    }
    
    public void setFocus() {
    }
}
