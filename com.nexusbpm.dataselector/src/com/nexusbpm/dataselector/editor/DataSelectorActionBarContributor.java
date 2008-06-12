package com.nexusbpm.dataselector.editor;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;

public class DataSelectorActionBarContributor extends EditorActionBarContributor {
    private static final String[] IDS = {
        IDEActionFactory.BOOKMARK.getId(),
        ActionFactory.COPY.getId(),
        ActionFactory.CUT.getId(),
        ActionFactory.DELETE.getId(),
        ActionFactory.FIND.getId(),
        ActionFactory.PASTE.getId(),
        ActionFactory.PRINT.getId(),
        ActionFactory.REDO.getId(),
        ActionFactory.SELECT_ALL.getId(),
        ActionFactory.UNDO.getId()
    };
    
    public DataSelectorActionBarContributor() {
    }
    
    @Override
    public void init(IActionBars bars, IWorkbenchPage page) {
        super.init(bars, page);
    }
    
    @Override
    public void setActiveEditor(IEditorPart part) {
        DataSelectorEditor editor = (DataSelectorEditor) part;
        for(String id : IDS) {
            installAction(id, editor);
        }
    }
    
    protected void installAction(String id, DataSelectorEditor editor) {
        IActionBars bars = editor.getEditorSite().getActionBars();
        IAction action = null;
        if(editor.getEditDomain() != null) {
            action = editor.getEditDomain().getAction(id);
        }
        bars.setGlobalActionHandler(id, action);
    }
}
