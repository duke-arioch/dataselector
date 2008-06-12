package com.nexusbpm.dataselector.editor;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;

import com.nexusbpm.dataselector.Plugin;
import com.nexusbpm.dataselector.actions.AbstractExtensionAction;
import com.nexusbpm.dataselector.actions.ActionRegistry;
import com.nexusbpm.dataselector.actions.ValidatedAction;

public class DataSelectorMenuProvider extends ContextMenuProvider {
    private ActionRegistry registry;
    
    public DataSelectorMenuProvider(GraphicalViewer viewer, ActionRegistry registry) {
        super(viewer);
        this.registry = registry;
    }
    
    @Override
    public void buildContextMenu(IMenuManager menu) {
//        String edit = GEFActionConstants.GROUP_EDIT;
        String edit = Plugin.PLUGIN_ID + ".group.edit";
        String statistics = Plugin.PLUGIN_ID + ".group.statistics";
        String save = Plugin.PLUGIN_ID + ".group.save";
        String tools = Plugin.PLUGIN_ID + ".group.tools";
//        String view = GEFActionConstants.GROUP_VIEW;
        String view = Plugin.PLUGIN_ID + ".group.view";
        menu.add(new Separator(edit));
        menu.add(new Separator(statistics));
        menu.add(new Separator(save));
        menu.add(new Separator(tools));
        menu.add(new Separator(view));
        menu.add(new Separator(GEFActionConstants.MB_ADDITIONS));
        
        appendToGroup(menu, edit, ActionRegistry.COMBINE_NODES_ID);
        appendToGroup(menu, edit, ActionRegistry.SEPARATE_NODES_ID);
        appendToGroup(menu, edit, ActionRegistry.SPLIT_TREE_ID);
        appendToGroup(menu, edit, ActionRegistry.PRUNE_TREE_ID);
        appendAdditions(menu, edit);
        
        appendToGroup(menu, statistics, ActionRegistry.DOWNLOAD_STATS_ID);
        appendToGroup(menu, statistics, ActionRegistry.CANCEL_DOWNLOAD_ID);
        appendToGroup(menu, statistics, ActionRegistry.CLEAR_STATS_ID);
        appendAdditions(menu, statistics);
        
        appendAdditions(menu, save);
        
//        menu.appendToGroup(GEFActionConstants.GROUP_VIEW, registry.getAction(ViewValuesAction.ACTION_ID));
        appendToGroup(menu, tools, ActionRegistry.SELECTION_TOOL_ID);
        appendToGroup(menu, tools, ActionRegistry.ZOOM_TOOL_ID);
        appendAdditions(menu, tools);
        
        appendToGroup(menu, view, ActionRegistry.RESET_ZOOM_ID);
        appendToGroup(menu, view, ActionRegistry.LAYOUT_TREE_ID);
        appendToGroup(menu, view, ActionRegistry.SAMPLE_DATA_ID);
        appendAdditions(menu, view);
        
        appendAdditions(menu, GEFActionConstants.MB_ADDITIONS);
        
//        Object[] selection = getSelection();
//        System.out.println("selection:" + selection.length + ":" + selection);
//        if(selection.length == 0 || selection.length == 1 && selection[0] == getViewer().getContents()) {
//            menu.appendToGroup(GEFActionConstants.GROUP_EDIT, registry.getAction(ActionRegistry.LAYOUT_TREE_ID));
//        }
    }
    
    protected void appendAdditions(IMenuManager menu, String group) {
        for(AbstractExtensionAction action : registry.getExtensionActions(group)) {
            if(action.canPerform()) {
                menu.appendToGroup(group, action);
            }
        }
    }
    
    protected void appendToGroup(IMenuManager menu, String group, String actionID) {
        IAction action = registry.getAction(actionID);
        if(action != null) {
            if(!(action instanceof ValidatedAction) || ((ValidatedAction) action).canPerform()) {
                menu.appendToGroup(group, action);
            }
        }
    }
    
//    protected Object[] getSelection() {
//        Object[] selection = null;
//        ISelection sel = getViewer().getSelection();
//        if(sel instanceof IStructuredSelection) {
//            selection = ((IStructuredSelection) sel).toArray();
//        }
//        if(selection == null) {
//            selection = new Object[0];
//        }
//        return selection;
//    }
//    
//    @Override
//    protected GraphicalViewer getViewer() {
//        return (GraphicalViewer) super.getViewer();
//    }
}
