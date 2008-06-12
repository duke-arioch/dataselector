package com.nexusbpm.dataselector.actions;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.nexusbpm.dataselector.Plugin;
import com.nexusbpm.dataselector.controller.NodeController;
import com.nexusbpm.dataselector.model.LSColumnStats;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSStats;

public class ViewValuesAction extends Action {
    public static final String ACTION_ID = Plugin.PLUGIN_ID + ".ViewValuesAction";
    
    private GraphicalViewer viewer;
    
    public ViewValuesAction(GraphicalViewer viewer) {
        super("View Values", IAction.AS_PUSH_BUTTON);
        this.viewer = viewer;
        setId(ACTION_ID);
    }
    
    @Override
    public void run() {
        Object[] selection = ((IStructuredSelection) viewer.getSelection()).toArray();
        for(int index = 0; index < selection.length; index++) {
            if(selection[index] instanceof NodeController) {
                LSNode node = ((NodeController) selection[index]).getModel();
                System.out.println("node:" + node.getName());
                LSStats stats = node.getStats();
                for(LSColumnStats cstats : stats.getColumnStats()) {
                    System.out.println(cstats.getColumnOrdinal() + ":");
                    for(Object o : cstats.getValues()) {
                        System.out.println(String.valueOf(o));
                    }
                }
            }
        }
        System.out.println("done");
    }
}
