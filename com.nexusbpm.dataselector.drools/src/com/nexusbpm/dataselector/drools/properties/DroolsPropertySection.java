package com.nexusbpm.dataselector.drools.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;

import com.nexusbpm.dataselector.controller.NodeController;
import com.nexusbpm.dataselector.drools.model.PropertyList;
import com.nexusbpm.dataselector.drools.model.PropertyMap;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.util.ColumnTableLayout;

public class DroolsPropertySection extends AbstractPropertySection implements SelectionListener {
//    private Text text;
    private Table table;
    private TableViewer viewer;
    
    private Button addButton;
    private Button removeButton;
    private Button upButton;
    private Button downButton;
    private Button includeButton;
    
    private LSNode node;
    
    public DroolsPropertySection() {
    }
    
    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
        super.createControls(parent, tabbedPropertySheetPage);
        
        TabbedPropertySheetWidgetFactory factory = getWidgetFactory();
        
        Composite container = factory.createComposite(parent);
        container.setLayout(new GridLayout(2, false));
        
//        text = getWidgetFactory().createText(parent, "", SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
//        text.addModifyListener(this);
        
        table = factory.createTable(container, SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 5));
        table.addSelectionListener(this);
        
        addButton = factory.createButton(container, "Add", SWT.PUSH);
        addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        addButton.addSelectionListener(this);
        
        removeButton = factory.createButton(container, "Remove", SWT.PUSH);
        removeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        removeButton.addSelectionListener(this);
        
        upButton = factory.createButton(container, "Up", SWT.PUSH);
        upButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        upButton.addSelectionListener(this);
        
        downButton = factory.createButton(container, "Down", SWT.PUSH);
        downButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        downButton.addSelectionListener(this);
        
        factory.createLabel(container, "").setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        
        includeButton = factory.createButton(container, "Include this node as a rule", SWT.CHECK);
        includeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        includeButton.addSelectionListener(this);
        
        viewer = new TableViewer(table);
        
        viewer.setContentProvider(new DroolsContentProvider());
//        viewer.setLabelProvider(new DroolsLabelProvider());
        
        TableLayout tlayout = new ColumnTableLayout();
        tlayout.addColumnData(new ColumnWeightData(33, true));
        tlayout.addColumnData(new ColumnWeightData(67, true));
        
        table.setLayout(tlayout);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        
        TableColumn nameColumn = new TableColumn(table, SWT.LEFT);
        nameColumn.setText("Output Column Name");
        TableViewerColumn nameColumnViewer = new TableViewerColumn(viewer, nameColumn);
        nameColumnViewer.setEditingSupport(new ColumnNameEditingSupport(viewer, factory));
        nameColumnViewer.setLabelProvider(new DroolsLabelProvider(0));
        
        TableColumn valueColumn = new TableColumn(table, SWT.LEFT);
        valueColumn.setText("Value");
        TableViewerColumn valueColumnViewer = new TableViewerColumn(viewer, valueColumn);
        valueColumnViewer.setEditingSupport(new ColumnValueEditingSupport(viewer, factory));
        valueColumnViewer.setLabelProvider(new DroolsLabelProvider(1));
    }
    
    @Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);
        node = null;
        if(!selection.isEmpty() && selection instanceof IStructuredSelection) {
            Object object = ((IStructuredSelection) selection).toArray()[0];
            if(object instanceof NodeController) {
                node = ((NodeController) object).getModel();
            } else if(object instanceof LSNode) {
                node = (LSNode) object;
            }
        }
    }
    
    @Override
    public void refresh() {
        PropertyMap map = PropertyMap.get(node, false);
        includeButton.setSelection(map != null);
        if(viewer.getInput() == node) {
            viewer.refresh();
        } else {
            viewer.setInput(node);
        }
//        String value = null;
//        DroolsElement element = DroolsElement.getDroolsElement(node, false);
//        if(element != null) {
//            value = element.getText();
//        }
//        if(value == null) {
//            value = "";
//        }
//        text.setText(value);
        resetEnablement();
    }
    
    public void widgetDefaultSelected(SelectionEvent e) {
    }
    
    public void widgetSelected(SelectionEvent e) {
        if(e.widget == addButton) {
            addColumn();
        } else if(e.widget == removeButton) {
            removeColumns();
        } else if(e.widget == upButton) {
            moveUp();
        } else if(e.widget == downButton) {
            moveDown();
        } else if(e.widget == includeButton) {
            includeNode();
        } else if(e.widget == table) {
        }
        resetEnablement();
    }
    
    protected void addColumn() {
        PropertyList list = PropertyList.get(node.getTree(), true);
        List<String> names = list.getValues();
        String name = "column";
        int number = 0;
        if(names.contains(name)) {
            number = 1;
            while(names.contains(name + number)) {
                number += 1;
            }
        }
        if(number > 0) {
            name = name + number;
        }
        names.add(name);
        list.fireValuesChanged();
        viewer.refresh();
    }
    
    protected void removeColumns() {
        Object[] selection = ((IStructuredSelection) viewer.getSelection()).toArray();
        List<String> names = new ArrayList<String>();
        for(Object o : selection) {
            OutputColumnWrapper column = (OutputColumnWrapper) o;
            names.add(column.getName());
        }
        boolean marked = false;
        PropertyList list = PropertyList.get(node.getTree(), false);
        if(list != null) {
            list.getValues().removeAll(names);
            list.fireValuesChanged();
            list.markDirty();
            marked = true;
        }
        for(LSNode n : node.getTree().getNodes()) {
            PropertyMap map = PropertyMap.get(n, false);
            if(map != null) {
                map.getValues().keySet().removeAll(names);
                map.fireValuesChanged();
                if(!marked) {
                    map.markDirty();
                    marked = true;
                }
            }
        }
        viewer.refresh();
    }
    
    protected void moveUp() {
        moveSelected(-1);
        viewer.refresh();
    }
    
    protected void moveDown() {
        moveSelected(1);
        viewer.refresh();
    }
    
    protected void moveSelected(int offset) {
        Object[] selection = ((IStructuredSelection) viewer.getSelection()).toArray();
        String name = ((OutputColumnWrapper) selection[0]).getName();
        PropertyList names = PropertyList.get(node.getTree(), false);
        List<String> list = names.getValues();
        int index = list.indexOf(name);
        list.remove(name);
        list.add(index + offset, name);
    }
    
    protected void includeNode() {
        if(includeButton.getSelection()) {
            PropertyMap.get(node, true);
        } else {
            PropertyMap.remove(node);
        }
        viewer.refresh();
    }
    
    protected void resetEnablement() {
        table.setEnabled(includeButton.getSelection());
        boolean canAdd = false;
        boolean canRemove = false;
        boolean canUp = false;
        boolean canDown = false;
        if(node != null && table.getEnabled()) {
            canAdd = true;
            Object[] selection = ((IStructuredSelection) viewer.getSelection()).toArray();
            canRemove = selection.length > 0;
            if(selection.length == 1) {
                PropertyList list = PropertyList.get(node.getTree(), false);
                if(list != null) {
                    String name = ((OutputColumnWrapper) selection[0]).getName();
                    int index = list.getValues().indexOf(name);
                    canUp = index > 0;
                    canDown = index >= 0 && index < list.getValues().size() - 1;
                }
            }
        }
        addButton.setEnabled(canAdd);
        removeButton.setEnabled(canRemove);
        upButton.setEnabled(canUp);
        downButton.setEnabled(canDown);
    }
    
//    public void modifyText(ModifyEvent e) {
//        if(node != null) {
//            DroolsElement d = DroolsElement.getDroolsElement(node, true);
//            String oldText = d.getText();
//            if(oldText == null) {
//                oldText = "";
//            }
////            if(!text.getText().equals(oldText)) {
////                d.setText(text.getText());
////                d.markDirty();
////            }
//        }
//    }
    
    @Override
    public boolean shouldUseExtraSpace() {
        return true;
    }
}
