package com.nexusbpm.dataselector.actions;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.action.IAction;

import com.nexusbpm.dataselector.commands.LayoutTreeCommand;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.requests.ExecuteCommandRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class LayoutTreeAction extends AbstractEventBusAction {
    private GraphicalViewer viewer;
    public LayoutTreeAction(GraphicalViewer viewer, EventRequestBus bus) {
        super("Layout Tree", IAction.AS_PUSH_BUTTON, bus);
        this.viewer = viewer;
        setId(ActionRegistry.LAYOUT_TREE_ID);
    }
    
    @Override
    public void performRun() {
        Object contents = viewer.getContents().getModel();
        if(contents instanceof LSTree) {
            sendRequest(new ExecuteCommandRequest(new LayoutTreeCommand(
                    getEventRequestBus(), (LSTree) contents)));
        }
    }
    
    @Override
    public boolean canPerform() {
        Object contents = viewer.getContents().getModel();
        if(contents instanceof LSTree) {
            return ((LSTree) contents).getNodes().size() > 1;
        }
        return false;
    }
}
