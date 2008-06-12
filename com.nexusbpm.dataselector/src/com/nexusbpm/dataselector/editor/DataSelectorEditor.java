package com.nexusbpm.dataselector.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;

import com.nexusbpm.dataselector.connection.ConnectionControl;
import com.nexusbpm.dataselector.controller.AbstractController;
import com.nexusbpm.dataselector.editor.pages.ColumnConfigPage;
import com.nexusbpm.dataselector.editor.pages.DatasourceConfigPage;
import com.nexusbpm.dataselector.editor.pages.TreePage;
import com.nexusbpm.dataselector.events.ExceptionEvent;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.model.LSTree.State;
import com.nexusbpm.dataselector.model.config.LSConfig;
import com.nexusbpm.dataselector.model.persistance.Dom4jTreePersistance;
import com.nexusbpm.dataselector.model.persistance.TreePersistance;
import com.nexusbpm.dataselector.properties.TabbedPropertySheetPage;
import com.nexusbpm.dataselector.requests.DownloadStatisticsRequest;
import com.nexusbpm.dataselector.stats.StatsDownloadControl;
import com.nexusbpm.multipage.editor.EditorPage;
import com.nexusbpm.multipage.editor.MultiPageEditor;

public class DataSelectorEditor extends MultiPageEditor
    implements ITabbedPropertySheetPageContributor, PropertyChangeListener, DisposeListener {
    private DataSelectorEditDomain editDomain;
    private TreePersistance treePersistance;
    private LSTree tree;
    private StatsDownloadControl statsDownloader;
    private ConnectionControl connectionControl;
    
    private List<TabbedPropertySheetPage> propertySheets = new ArrayList<TabbedPropertySheetPage>();
    
    public DataSelectorEditor() {
    }
    
    protected TreePersistance getTreePersistance() {
        if(treePersistance == null) {
            treePersistance = new Dom4jTreePersistance();
        }
        return treePersistance;
    }
    
    protected LSTree getTree() {
        return tree;
    }
    
    @Override
    protected void createEditorPages() {
        editDomain = new DataSelectorEditDomain(this);
        
        getEventRequestBus().addRequestHandler(new EditorRequestHandler(this, editDomain, getEventRequestBus()));
        getEventRequestBus().addEventListener(new EditorBusEventListener(this));
        
        connectionControl = new ConnectionControl(getEventRequestBus(), getTree());
        statsDownloader = new StatsDownloadControl(getEditorInput().getName(), getEventRequestBus());
        
        addPage(new DatasourceConfigPage(getTree()));
        addPage(new ColumnConfigPage(getTree()));
        addPage(new TreePage(getTree(), editDomain, getSite()));
    }
    
    @Override
    public void setCurrentPage(EditorPage page) {
        super.setCurrentPage(page);
    }
    
    @Override
    public void setCurrentPage(int page) {
        super.setCurrentPage(page);
    }
    
    public DataSelectorEditDomain getEditDomain() {
        return editDomain;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if(IPropertySheetPage.class.equals(adapter)) {
//            return new LSPropertiesPage(/*this*/);
            TabbedPropertySheetPage page = new TabbedPropertySheetPage(this);
            propertySheets.add(page);
            page.addDisposeListener(this);
            return page;
        }
        return super.getAdapter(adapter);
    }
    
    public String getContributorId() {
        return "com.nexusbpm.dataselector.propertyContributor";
    }
    
    @Override
    public void doSave(IProgressMonitor monitor) {
        IEditorInput input = getEditorInput();
        if(input instanceof IFileEditorInput) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                getTreePersistance().writeTree(out, getTree());
                ((IFileEditorInput) input).getFile().setContents(
                        new ByteArrayInputStream(out.toByteArray()), true, true, null);
                setDirty(false);
            } catch(Exception e) {
                getEventRequestBus().handleEvent(new ExceptionEvent("Error saving tree " + getEditorInput().getName(), e));
            }
        } else {
            throw new UnsupportedOperationException("Cannot save for input type " + input.getClass());
        }
    }
    
    @Override
    public void doSaveAs() {
    }
    
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        if(input instanceof IStorageEditorInput) {
            try {
                InputStream is = ((IStorageEditorInput) input).getStorage().getContents();
                LSTree tree = getTreePersistance().parseTree(is);
                tree.setEventRequestBus(getEventRequestBus());
                tree.addPropertyChangeListener(this);
                tree.getConfig().addPropertyChangeListener(this);
                for(LSNode node : tree.getNodes()) {
                    node.addPropertyChangeListener(this);
                }
                if(tree.getName() != null && tree.getName().length() > 0) {
                    setPartName(tree.getName());
                } else {
                    setPartName(getEditorInput().getName());
                }
                this.tree = tree;
            } catch(CoreException e) {
                PartInitException ex = new PartInitException("Error initializing DataSelector");
                ex.initCause(e);
                throw ex;
            } catch(IOException e) {
                PartInitException ex = new PartInitException("Error initializing DataSelector");
                ex.initCause(e);
                throw ex;
            }
        } else {
            throw new PartInitException("Cannot read input of type " + input.getClass());
        }
    }
    
    public void widgetDisposed(DisposeEvent e) {
        propertySheets.remove(e.data);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(LSTree.PROPERTY_ELEMENT_NAME)) {
            if(getTree().getName() != null && getTree().getName().length() > 0) {
                setPartName(getTree().getName());
            } else {
                setPartName(getEditorInput().getName());
            }
        } else if(evt.getPropertyName().equals(LSConfig.PROPERTY_AUTO_DOWNLOAD_STATS)) {
            if(getTree().getState() == State.SPLIT_TREE &&
                    ((Boolean) evt.getNewValue()).booleanValue() &&
                    getTree().getNodes().size() > 0) {
                sendRequest(new DownloadStatisticsRequest(
                        new ArrayList<LSNode>(getTree().getNodes())));
            }
        } else if(evt.getPropertyName().equals(LSConfig.PROPERTY_AUTO_DOWNLOAD_CATEGORICAL_SPLITS)) {
            if(getTree().getState() == State.SPLIT_TREE &&
                    getTree().getConfig().isAutoDownloadStats() &&
                    getTree().getConfig().isAutoDownloadCategoricalSplits() &&
                    getTree().getNodes().size() > 0) {
                sendRequest(new DownloadStatisticsRequest(
                        new ArrayList<LSNode>(getTree().getNodes())));
            }
        } else if(evt.getPropertyName().equals(LSTree.PROPERTY_NODE_ADDED)) {
            ((LSNode) evt.getNewValue()).addPropertyChangeListener(this);
            if(getTree().getConfig().isAutoDownloadStats()) {
                sendRequest(new DownloadStatisticsRequest((LSNode) evt.getNewValue()));
            }
        } else if(evt.getPropertyName().equals(LSTree.PROPERTY_NODE_REMOVED)) {
            ((LSNode) evt.getOldValue()).removePropertyChangeListener(this);
        } else if(evt.getPropertyName().equals(LSTree.PROPERTY_TREE_STATE)) {
            if(getTree().getConfig().isAutoDownloadStats() &&
                    evt.getNewValue() == State.SPLIT_TREE &&
                    evt.getOldValue() != State.SPLIT_TREE &&
                    getTree().getNodes().size() > 0) {
                sendRequest(new DownloadStatisticsRequest(
                        new ArrayList<LSNode>(getTree().getNodes())));
            }
        }
        ISelection selection = getSite().getSelectionProvider().getSelection();
        if(!selection.isEmpty() && selection instanceof IStructuredSelection) {
            Object source = evt.getSource();
            for(Object o : ((IStructuredSelection) selection).toArray()) {
                if(o == source || o instanceof AbstractController &&
                        ((AbstractController<?>) o).getModel() == source) {
                    for(TabbedPropertySheetPage page : propertySheets) {
                        page.refresh();
                    }
                    break;
                }
            }
        }
    }
    
    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }
    
    @Override
    public void setFocus() {
    }
    
    @Override
    public void dispose() {
        if(statsDownloader != null) {
            statsDownloader.shutdown(true);
        }
        if(connectionControl != null) {
            connectionControl.shutdown();
        }
        if(getTree() != null) {
            for(LSNode node : getTree().getNodes()) {
                node.removePropertyChangeListener(this);
            }
            getTree().removePropertyChangeListener(this);
            if(getTree().getConfig() != null) {
                getTree().getConfig().removePropertyChangeListener(this);
            }
        }
        super.dispose();
        if(editDomain != null && editDomain.getCommandStack() != null) {
            editDomain.getCommandStack().dispose();
        }
    }
}
