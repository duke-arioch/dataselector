package com.nexusbpm.dataselector.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;

import com.nexusbpm.database.driver.SQLDriver;
import com.nexusbpm.database.table.ResultSetTableUtil;
import com.nexusbpm.dataselector.controller.NodeController;
import com.nexusbpm.dataselector.database.SQLGenerator;
import com.nexusbpm.dataselector.database.SQLGeneratorFactory;
import com.nexusbpm.dataselector.events.ExceptionEvent;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.model.config.LSConfig;
import com.nexusbpm.dataselector.model.config.LSConnection;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class SampleDataAction extends AbstractEventBusAction {
    private ISelectionProvider selectionProvider;
    
    public SampleDataAction(ISelectionProvider selectionProvider, EventRequestBus bus) {
        super("Sample Data", IAction.AS_PUSH_BUTTON, bus);
        setId(ActionRegistry.SAMPLE_DATA_ID);
        this.selectionProvider = selectionProvider;
    }
    
    @Override
    public boolean canPerform() {
        ISelection sel = selectionProvider.getSelection();
        if(sel instanceof IStructuredSelection) {
            Object[] selection = ((IStructuredSelection) sel).toArray();
            for(Object o : selection) {
                if(o instanceof LSNode || o instanceof NodeController) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public void performRun() {
        LSTree tree = null;
        List<LSNode> nodes = new ArrayList<LSNode>();
        ISelection sel = selectionProvider.getSelection();
        if(sel instanceof IStructuredSelection) {
            Object[] selection = ((IStructuredSelection) sel).toArray();
            for(Object o : selection) {
                LSNode node = null;
                if(o instanceof NodeController) {
                    node = ((NodeController) o).getModel();
                } else if(o instanceof LSNode) {
                    node = (LSNode) o;
                }
                if(node != null) {
                    nodes.add(node);
                    if(tree == null) {
                        tree = node.getTree();
                    }
                }
            }
        }
        
        if(tree == null || nodes.size() == 0) {
            return;
        }
        
        try {
            LSConfig config = tree.getConfig();
            LSConnection connection = config.getConnection();
            SQLGenerator gen = SQLGeneratorFactory.getInstance().getGenerator(
                    tree.getConfig().getDriver().getDriverClass());
            ResultSetTableUtil.openTable(
                    PlatformUI.getWorkbench().getProgressService(),
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                    true,
                    SQLDriver.getDriverInstanceByName(config.getDriver().getName()),
                    connection.getURI(),
                    connection.getUsername(),
                    connection.getPassword(),
                    gen.getOutputQuery(nodes, tree.getConfig(), true, false),
                    1000);
        } catch(Exception exc) {
            getEventRequestBus().handleEvent(new ExceptionEvent("Error sampling data", exc));
        }
    }
    
}
