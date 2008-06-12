package com.nexusbpm.dataselector.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.tools.PanningSelectionTool;
import org.eclipse.jface.window.SameShellProvider;

import com.nexusbpm.dataselector.Plugin;
import com.nexusbpm.dataselector.actions.SwitchToolAction.SwitchToolListener;
import com.nexusbpm.dataselector.util.ZoomTool;
import com.nexusbpm.multipage.bus.EventRequestBus;

public class ActionRegistry extends org.eclipse.gef.ui.actions.ActionRegistry {
    public static final String CANCEL_DOWNLOAD_ID = Plugin.PLUGIN_ID + ".CancelStatsDownload";
    public static final String CLEAR_STATS_ID = Plugin.PLUGIN_ID + ".ClearStatsAction";
    public static final String COMBINE_NODES_ID = Plugin.PLUGIN_ID + ".CombineNodesAction";
    public static final String DOWNLOAD_STATS_ID = Plugin.PLUGIN_ID + ".DownloadStatsAction";
    public static final String LAYOUT_TREE_ID = Plugin.PLUGIN_ID + ".LayoutTreeAction";
    public static final String PRUNE_TREE_ID = Plugin.PLUGIN_ID + ".PruneTreeAction";
    public static final String RESET_ZOOM_ID = Plugin.PLUGIN_ID + ".ResetZoomAction";
    public static final String SAMPLE_DATA_ID = Plugin.PLUGIN_ID + ".SampleDataAction";
    public static final String SEPARATE_NODES_ID = Plugin.PLUGIN_ID + ".SeparateNodesAction";
    public static final String SPLIT_TREE_ID = Plugin.PLUGIN_ID + ".SplitTreeAction";
    
    public static final String SELECTION_TOOL_ID = Plugin.PLUGIN_ID + ".SelectionTool";
    public static final String ZOOM_TOOL_ID = Plugin.PLUGIN_ID + ".ZoomTool";
    
    private Map<String, List<AbstractExtensionAction>> extensionActions;
    
    public ActionRegistry(
            EditDomain editDomain,
            GraphicalViewer graphicalViewer,
            EventRequestBus bus) {
        extensionActions = new HashMap<String, List<AbstractExtensionAction>>();
        createActions(editDomain, graphicalViewer, bus);
    }
    
    protected void createActions(
            EditDomain editDomain,
            GraphicalViewer graphicalViewer,
            EventRequestBus bus) {
        SwitchToolListener listener = new SwitchToolListener();
        registerAction(new SwitchToolAction(
                SELECTION_TOOL_ID,
                "Selection tool",
                null,
                editDomain,
                new PanningSelectionTool(),
                listener));
        registerAction(new SwitchToolAction(
                ZOOM_TOOL_ID,
                "Zoom tool",
                null,
                editDomain,
                new ZoomTool(graphicalViewer.getControl().getDisplay()),
                listener));
        registerAction(new ResetZoomAction(
                ((ScalableRootEditPart)graphicalViewer.getRootEditPart()).getZoomManager()));
        registerAction(new DownloadNodeStatsAction(graphicalViewer, bus));
//        registerAction(new ViewValuesAction(graphicalViewer));
        registerAction(new SplitTreeAction(graphicalViewer, bus, new SameShellProvider(graphicalViewer.getControl())));
        registerAction(new PruneTreeAction(graphicalViewer, bus));
        registerAction(new LayoutTreeAction(graphicalViewer, bus));
        registerAction(new CombineNodesAction(graphicalViewer, bus));
        registerAction(new SeparateNodesAction(graphicalViewer, bus));
        registerAction(new ClearStatsAction(graphicalViewer, bus));
        registerAction(new SampleDataAction(graphicalViewer, bus));
        registerAction(new CancelStatsDownloadingAction(graphicalViewer, bus));
        
        createExtensionActions(graphicalViewer, bus);
    }
    
    protected void createExtensionActions(GraphicalViewer graphicalViewer, EventRequestBus bus) {
        try {
            IExtensionPoint extPoint =
                Platform.getExtensionRegistry().getExtensionPoint(Plugin.PLUGIN_ID + ".treeMenuExtension");
            
            IExtension[] extensions = extPoint.getExtensions();
            
            for(IExtension extension : extensions) {
                IConfigurationElement[] items = extension.getConfigurationElements();
                for(IConfigurationElement item : items) {
                    AbstractExtensionAction action =
                        (AbstractExtensionAction) item.createExecutableExtension("class");
                    action.setEventRequestBus(bus);
                    action.setSelectionProvider(graphicalViewer);
                    action.setShellProvider(new SameShellProvider(graphicalViewer.getControl()));
                    addExtensionAction(item.getAttribute("group"), action);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            Plugin.logError("error reading action extensions", e);
        }
    }
    
    protected void addExtensionAction(String group, AbstractExtensionAction action) {
        if(extensionActions.get(group) == null) {
            extensionActions.put(group, new ArrayList<AbstractExtensionAction>());
        }
        extensionActions.get(group).add(action);
    }
    
    public List<AbstractExtensionAction> getExtensionActions(String group) {
        if(extensionActions.get(group) == null) {
            return Collections.emptyList();
        } else {
            return extensionActions.get(group);
        }
    }
}
