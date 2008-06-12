package com.nexusbpm.dataselector.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import com.nexusbpm.dataselector.controller.NodeController;
import com.nexusbpm.dataselector.controller.TreeController;
import com.nexusbpm.dataselector.database.SQLGenerator;
import com.nexusbpm.dataselector.database.SQLGeneratorFactory;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.model.config.LSConfig;

public class SQLPropertySection extends AbstractPropertySection implements SelectionListener {
    private Text text;
    private Button restrictColumns;
    private Button useNodeNames;
    private TreeSet<LSNode> nodes;
    private LSTree tree;
    
    public SQLPropertySection() {
        nodes = new TreeSet<LSNode>();
    }
    
    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
        super.createControls(parent, tabbedPropertySheetPage);
        
        Composite container = getWidgetFactory().createComposite(parent);
        GridLayout layout = new GridLayout(1, true);
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        container.setLayout(layout);
        
        text = getWidgetFactory().createText(
                container, "SQL", SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        restrictColumns = getWidgetFactory().createButton(container, "Select only Predictor Columns", SWT.CHECK);
        restrictColumns.addSelectionListener(this);
        restrictColumns.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        useNodeNames = getWidgetFactory().createButton(container, "Use Node Names in SQL Query", SWT.CHECK);
        useNodeNames.addSelectionListener(this);
        useNodeNames.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    }
    
    @Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
        nodes.clear();
        tree = null;
        TreeSet<LSNode> queue = new TreeSet<LSNode>();
        for(Object o : ((IStructuredSelection) selection).toArray()) {
            if(o instanceof NodeController) {
                queue.add(((NodeController) o).getModel());
            } else if(o instanceof LSNode) {
                queue.add((LSNode) o);
            } else if(o instanceof TreeController) {
                tree = ((TreeController) o).getModel();
            } else if(o instanceof LSTree) {
                tree = (LSTree) o;
            }
        }
        while(queue.size() > 0) {
            // note: it's important that the queue is sorted and we're traversing it back-to-front
            LSNode node = queue.last();
            queue.remove(node);
            // first check if all children of the given parent are in the queue
            if(node.getConnector() != null) {
                boolean allChildren = true;
                LSNode parent = node.getConnector().getSource();
                for(LSNode child : parent.getSubNodes()) {
                    if(child != node && !queue.contains(child)) {
                        allChildren = false;
                        break;
                    }
                }
                // if all children are selected it's the same as just selecting the parent
                if(allChildren) {
                    queue.removeAll(node.getSubNodes());
                    queue.add(parent);
                    continue;
                }
            }
            if(containsParent(nodes, node) || containsParent(queue, node)) {
                // we already have a parent of this node in the final collection, or
                // there is another node in the queue that is this node's parent
                continue;
            } else {
                nodes.add(node);
            }
        }
        /* the original algorithm below will not detect if all children of an
         * unselected parent node are selected, which is equivalent to selecting
         * the parent node
         */
//        while(queue.size() > 0) {
//            LSNode node = queue.first();
//            queue.remove(node);
//            if(containsParent(nodes, node) || containsParent(queue, node)) {
//                // we already have a parent of this node in the final collection, or
//                // there is another node in the queue that is this node's parent
//                continue;
//            } else {
//                nodes.add(node);
//            }
//        }
    }
    
    protected boolean containsParent(Collection<LSNode> collection, LSNode node) {
        if(node.getConnector() != null && node.getConnector().getSource() != null) {
            LSNode parent = node.getConnector().getSource();
            if(collection.contains(parent)) {
                return true;
            } else {
                return containsParent(collection, parent);
            }
        }
        return false;
    }
    
    @Override
    public void refresh() {
        if(text == null) return;
        if(nodes.size() > 0) {
            restrictColumns.setEnabled(true);
            useNodeNames.setEnabled(containsNamedData(nodes));
            LSConfig config = nodes.first().getTree().getConfig();
            String driverName = config.getDriver().getName();
            SQLGenerator generator = SQLGeneratorFactory.getInstance().getGenerator(driverName);
            String query = generator.getOutputQuery(
                    new ArrayList<LSNode>(nodes), config,
                    useNodeNames.getSelection(), restrictColumns.getSelection());
            text.setText(query);
        } else if(tree != null && tree.getRoot() != null) {
            restrictColumns.setEnabled(true);
            useNodeNames.setEnabled(containsNamedData(tree.getRoot()));
            LSConfig config = tree.getConfig();
            String driverName = config.getDriver().getName();
            SQLGenerator generator = SQLGeneratorFactory.getInstance().getGenerator(driverName);
            ArrayList<LSNode> nodes = new ArrayList<LSNode>();
            nodes.add(tree.getRoot());
            String query = generator.getOutputQuery(nodes, config,
                    useNodeNames.getSelection(), restrictColumns.getSelection());
            text.setText(query);
        } else {
            restrictColumns.setEnabled(false);
            useNodeNames.setEnabled(false);
            text.setText("");
        }
    }
    
    protected boolean containsNamedData(LSNode node) {
        return containsNamedData(Collections.singleton(node));
    }
    
    protected boolean containsNamedData(Collection<LSNode> nodes) {
        Set<LSNode> checked = new HashSet<LSNode>();
        List<LSNode> queue = new LinkedList<LSNode>(nodes);
        while(!queue.isEmpty()) {
            LSNode node = queue.remove(0);
            if(checked.contains(node)) {
                continue;
            }
            checked.add(node);
            if(isNodeNamed(node)) {
                return true;
            }
            LSNode parent = getTreeParent(node);
            while(parent != null && !checked.contains(parent)) {
                if(isNodeNamed(parent)) {
                    return true;
                }
                parent = getTreeParent(parent);
            }
            for(LSNode child : node.getSubNodes()) {
                queue.add(child);
            }
        }
        return false;
    }
    
    protected boolean isNodeNamed(LSNode node) {
        if(node != null) {
            if(node.getName() != null && node.getName().length() > 0) {
                return true;
            }
        }
        return false;
    }
    
    protected LSNode getTreeParent(LSNode node) {
        if(node != null && node.getConnector() != null) {
            return node.getConnector().getSource();
        }
        return null;
    }
    
    public void widgetDefaultSelected(SelectionEvent e) {
    }
    
    public void widgetSelected(SelectionEvent e) {
        refresh();
    }
    
    @Override
    public boolean shouldUseExtraSpace() {
        return true;
    }
}
