package com.nexusbpm.dataselector.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;

import com.nexusbpm.dataselector.controller.NodeController;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.wizard.NoProgressWizardDialog;
import com.nexusbpm.dataselector.wizard.SplitTreeWizard;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class SplitTreeAction extends AbstractEventBusAction {
    private ISelectionProvider selectionProvider;
    private IShellProvider shellProvider;
    public SplitTreeAction(
            ISelectionProvider selectionProvider,
            EventRequestBus bus,
            IShellProvider shellProvider) {
        super("Split Tree", IAction.AS_PUSH_BUTTON, bus);
        this.selectionProvider = selectionProvider;
        this.shellProvider = shellProvider;
        setId(ActionRegistry.SPLIT_TREE_ID);
    }
    
    @Override
    public void performRun() {
        // I believe the following bug is fixed, but I'll leave the following description until I'm more certain
        // (I fixed a null pointer exception that sometimes happened when the dialog tried to pop up)
        // bug: sometimes the dialog isn't showing up... not sure why or under what circumstances...
        // the bug was observed in the following situation:
        // setup db info, split tree, split tree further, download some stats, combine nodes,
        // repeat downloading and combining,
        // select all nodes (root node contains lots of data; ie: > 4 million rows) start to dl stats
        // split dialog no longer shows up
        
        /* Also observed the following:
inc from 3 to 4 LSNode@3965704:null com.nexusbpm.dataselector.util.StatisticsContentLabelProvider@d86c58
inc from 3 to 4 LSStats@10643938 com.nexusbpm.dataselector.util.StatisticsContentLabelProvider@d86c58
inc from 4 to 5 LSNode@3965704:null com.nexusbpm.dataselector.util.StatisticsContentLabelProvider@11c9fcc
inc from 4 to 5 LSStats@10643938 com.nexusbpm.dataselector.util.StatisticsContentLabelProvider@11c9fcc
dec from 5 to 4 LSNode@3965704:null com.nexusbpm.dataselector.util.StatisticsContentLabelProvider@d86c58
dec from 5 to 4 LSStats@10643938 com.nexusbpm.dataselector.util.StatisticsContentLabelProvider@d86c58
inc from 4 to 5 LSNode@3965704:null com.nexusbpm.dataselector.util.StatisticsContentLabelProvider@d86c58
inc from 4 to 5 LSStats@10643938 com.nexusbpm.dataselector.util.StatisticsContentLabelProvider@d86c58
inc from 5 to 6 LSNode@3965704:null com.nexusbpm.dataselector.util.StatisticsContentLabelProvider@a33d00
inc from 5 to 6 LSStats@10643938 com.nexusbpm.dataselector.util.StatisticsContentLabelProvider@a33d00
dec from 6 to 5 LSNode@3965704:null com.nexusbpm.dataselector.util.StatisticsContentLabelProvider@d86c58
dec from 6 to 5 LSStats@10643938 com.nexusbpm.dataselector.util.StatisticsContentLabelProvider@d86c58
inc from 5 to 6 LSNode@3965704:null com.nexusbpm.dataselector.util.StatisticsContentLabelProvider@d86c58
inc from 5 to 6 LSStats@10643938 com.nexusbpm.dataselector.util.StatisticsContentLabelProvider@d86c58
inc from 6 to 7 LSNode@3965704:null com.nexusbpm.dataselector.util.StatisticsContentLabelProvider@465648
inc from 6 to 7 LSStats@10643938 com.nexusbpm.dataselector.util.StatisticsContentLabelProvider@465648
         * every time you click "split tree" it adds two listeners, but only one of the two gets removed...
         * (only happening once the split tree dialog stops appearing)
         */
        try {
        Object[] selection = ((IStructuredSelection) selectionProvider.getSelection()).toArray();
        LSNode node = null;
        for(Object o : selection) {
            if(o instanceof NodeController) {
                node = ((NodeController) o).getModel();
                break;
            }
        }
        if(node == null) {
            System.err.println("no node selected");
            return;
        }
        NoProgressWizardDialog d = new NoProgressWizardDialog(
                shellProvider.getShell(),
                new SplitTreeWizard(node, getEventRequestBus()));
        d.open();
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }
    
    @Override
    public boolean canPerform() {
        Object[] selection = ((IStructuredSelection) selectionProvider.getSelection()).toArray();
        
        // can only split when only 1 node is selected and it has no split yet
        if(selection.length == 1 && selection[0] instanceof NodeController) {
            NodeController controller = (NodeController) selection[0];
            return controller.getModel().getSplit() == null;
        }
        return false;
    }
}
