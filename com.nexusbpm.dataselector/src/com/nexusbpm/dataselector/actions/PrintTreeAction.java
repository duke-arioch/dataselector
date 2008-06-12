package com.nexusbpm.dataselector.actions;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.print.PrintGraphicalViewerOperation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;

public class PrintTreeAction extends Action {
    private GraphicalViewer viewer;
    
    public PrintTreeAction(GraphicalViewer viewer) {
        super("Print", IAction.AS_PUSH_BUTTON);
        this.viewer = viewer;
    }
    
    @Override
    public void run() {
        PrintDialog d = new PrintDialog(viewer.getControl().getShell());
        PrinterData data = d.open();
        if(data != null) {
            Printer p = new Printer(data);
            PrintGraphicalViewerOperation op = new PrintGraphicalViewerOperation(p, viewer);
            op.setPrintMode(PrintGraphicalViewerOperation.FIT_PAGE);
            op.run("Data_Selector_Tree");
        }
    }
}
