package com.nexusbpm.dataselector.editor;

import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import com.nexusbpm.dataselector.actions.PrintTreeAction;
import com.nexusbpm.dataselector.actions.RedoAction;
import com.nexusbpm.dataselector.actions.UndoAction;

public class DataSelectorEditDomain extends DefaultEditDomain {
    private RedoAction redoAction;
    private UndoAction undoAction;
    private PrintTreeAction printAction;
    
    public DataSelectorEditDomain(IEditorPart editorPart) {
        super(editorPart);
    }
    
    public IAction getAction(String id) {
        if(id.equals(ActionFactory.REDO.getId())) {
            if(redoAction == null) {
                redoAction = new RedoAction(this);
                redoAction.setImageDescriptor(getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
                redoAction.setDisabledImageDescriptor(getImageDescriptor(ISharedImages.IMG_TOOL_REDO_DISABLED));
            }
            return redoAction;
        } else if(id.equals(ActionFactory.UNDO.getId())) {
            if(undoAction == null) {
                undoAction = new UndoAction(this);
                undoAction.setImageDescriptor(getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
                undoAction.setDisabledImageDescriptor(getImageDescriptor(ISharedImages.IMG_TOOL_UNDO_DISABLED));
            }
            return undoAction;
        } else if(id.equals(ActionFactory.PRINT.getId())) {
            if(printAction == null) {
                printAction = new PrintTreeAction(
                        (GraphicalViewer) getEditorPart().getSite().getSelectionProvider());
            }
            return printAction;
        }
        return null;
    }
    
    protected ImageDescriptor getImageDescriptor(String name) {
        ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
        return images.getImageDescriptor(name);
    }
}
