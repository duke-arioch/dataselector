package com.nexusbpm.dataselector.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.Tool;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

public class SwitchToolAction extends Action {
    private EditDomain editDomain;
    private Tool tool;
    private SwitchToolListener listener;
    
    public SwitchToolAction(String id, String text, ImageDescriptor image, EditDomain editDomain, Tool tool, SwitchToolListener listener) {
        super(text, IAction.AS_CHECK_BOX);
        if(id == null) throw new IllegalArgumentException("ID must not be null");
        setId(id);
        setImageDescriptor(image);
        this.editDomain = editDomain;
        this.tool = tool;
        this.listener = listener;
        listener.addAction(this);
        updateChecked();
    }
    
    @Override
    public void run() {
        editDomain.setActiveTool(tool);
        listener.update();
    }
    
    protected void updateChecked() {
        setChecked(editDomain.getActiveTool() == tool);
    }
    
    protected static class SwitchToolListener {
        private List<SwitchToolAction> actions;
        
        public SwitchToolListener() {
            actions = new ArrayList<SwitchToolAction>();
        }
        
        protected void addAction(SwitchToolAction action) {
            actions.add(action);
        }
        
        protected void update() {
            for(SwitchToolAction action : actions) {
                action.updateChecked();
            }
        }
    }
}
