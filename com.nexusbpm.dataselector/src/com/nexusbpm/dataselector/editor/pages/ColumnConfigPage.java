package com.nexusbpm.dataselector.editor.pages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.nexusbpm.database.info.DBInfo;
import com.nexusbpm.database.info.DBInfoFactory;
import com.nexusbpm.dataselector.events.PredictorChangeEvent;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.model.LSTree.State;
import com.nexusbpm.dataselector.model.config.LSColumn;
import com.nexusbpm.dataselector.model.config.LSConfig;
import com.nexusbpm.dataselector.requests.ChangePageRequest;
import com.nexusbpm.dataselector.requests.PredictorChangeRequest;
import com.nexusbpm.dataselector.requests.RefreshTreeRequest;
import com.nexusbpm.multipage.bus.BusEvent;
import com.nexusbpm.multipage.bus.BusEventListener;

public class ColumnConfigPage extends AbstractEditorPage
        implements SelectionListener, PropertyChangeListener, MenuDetectListener, BusEventListener {
    private Combo targetCombo;
    private Table predictorTable;
    private TableViewer predictorViewer;
    
    private LSColumnComparator comparator;
    
    private Button previousButton;
    private Button nextButton;
    private Button editButton;
    private Button cancelButton;
    
    private MenuItem checkSelectedItem;
    private MenuItem uncheckSelectedItem;
    private MenuItem checkAllItem;
    private MenuItem uncheckAllItem;
    
    private boolean editing;
    
    public ColumnConfigPage(LSTree tree) {
        super(tree);
    }
    
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        
        getEventRequestBus().addEventListener(this);
        
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        parent.setLayout(layout);
        
        FormToolkit toolkit = getFormToolkit();
        Form form = toolkit.createForm(parent);
        form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        form.setText("Data Selector" /* + " Target and Predictor Columns" */);
        
        Composite body = form.getBody();
        body.setLayout(new FormLayout());
        
        FormData data;
//        Section section;
        Composite client;
        
        // create the Database Columns section
        Section section1 = toolkit.createSection(body, Section.TITLE_BAR);
        section1.setText("Database Columns Setup");
        section1.setLayoutData(data = new FormData());
        data.top = new FormAttachment(0, 2);
        data.left = new FormAttachment(0, 2);
        data.right = new FormAttachment(100, -2);
        data.bottom = new FormAttachment(100, -2);
        
        client = toolkit.createComposite(section1);
        section1.setClient(client);
        createDatabaseColumnsSection(client);
        
        // create the bottom separator
        toolkit.createLabel(parent, "", SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        
        // create the button bar
//        Composite buttonBar = toolkit.createComposite(parent);
//        buttonBar.setLayoutData(createGridData(SWT.FILL, true));
        
        createButtonBar(parent).setLayoutData(createGridData(SWT.FILL, true));
        
        getTree().addPropertyChangeListener(this);
        getTree().getConfig().addPropertyChangeListener(this);
        getTree().getConfig().getConnection().addPropertyChangeListener(this);
        getTree().getConfig().getDriver().addPropertyChangeListener(this);
    }
    
    protected void createDatabaseColumnsSection(Composite body) {
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        body.setLayout(layout);
        
        FormToolkit toolkit = getFormToolkit();
        
        toolkit.createLabel(body, "Target column:", SWT.LEFT).setLayoutData(createGridData(SWT.FILL, false));
        
        targetCombo = new Combo(body, SWT.DROP_DOWN | SWT.READ_ONLY);
        toolkit.adapt(targetCombo, false, false);
        targetCombo.setText(notNull(getTree().getConfig().getTargetColumn()));
        targetCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        
        toolkit.createLabel(body, "Predictors:", SWT.LEFT).setLayoutData(createGridData(SWT.FILL, false));
        
        predictorTable = toolkit.createTable(body, SWT.MULTI | SWT.CHECK | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL);
        predictorTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        
        Menu menu = new Menu(predictorTable);
        checkSelectedItem = createMenuItem(menu, "Check Selected");
        uncheckSelectedItem = createMenuItem(menu, "Uncheck Selected");
        new MenuItem(menu, SWT.SEPARATOR);
        checkAllItem = createMenuItem(menu, "Check All");
        uncheckAllItem = createMenuItem(menu, "Uncheck All");
        
        predictorTable.setMenu(menu);
        predictorTable.addMenuDetectListener(this);
        
        predictorViewer = new TableViewer(predictorTable);
        
        predictorViewer.setLabelProvider(new LSColumnLabelProvider());
        predictorViewer.setComparator(getColumnComparator());
        predictorViewer.setContentProvider(new LSColumnContentProvider());
        
//        predictorViewer.setCellModifier(new LSColumnCellModifier(predictorViewer));
//        predictorViewer.setCellEditors(new CellEditor[] {null, new VariableCellEditor(predictorTable)});
        predictorViewer.setColumnProperties(new String[] {"predictor", "ordinal", "name", "sqltype", "javatype"});
        
        TableLayout tlayout = new TableLayout();
        tlayout.addColumnData(new ColumnWeightData(14, true));
        tlayout.addColumnData(new ColumnWeightData(12, true));
        tlayout.addColumnData(new ColumnWeightData(55, true));
        tlayout.addColumnData(new ColumnWeightData(20, true));
        tlayout.addColumnData(new ColumnWeightData(20, true));
        
        predictorTable.setLayout(tlayout);
        predictorTable.setLinesVisible(true);
        predictorTable.setHeaderVisible(true);
        
        createTableColumn(SWT.CENTER, "Predictor", -1);
        createTableColumn(SWT.CENTER, "Ordinal", 0);
        createTableColumn(SWT.LEFT, "Name", 1);
        createTableColumn(SWT.LEFT, "SQL Type", 2);
        createTableColumn(SWT.LEFT, "Java Type", 3);
        
        resetCombo();
        resetTable();
    }
    
    protected void createTableColumn(int alignment, String text, final int columnNumber) {
        TableColumn column = new TableColumn(predictorTable, alignment);
        column.setText(text);
        if(columnNumber >= 0) {
            column.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                }
                public void widgetSelected(SelectionEvent e) {
                    TableItem[] items = predictorTable.getItems();
                    boolean[] checkedItems = new boolean[items.length];
                    for(int index = 0; index < items.length; index++) {
                        LSColumn column = (LSColumn) items[index].getData();
                        checkedItems[column.getOrdinal() - 1] = items[index].getChecked();
                    }
                    LSColumnComparator comparator = getColumnComparator();
                    if(comparator.column != columnNumber) {
                        comparator.column = columnNumber;
                        comparator.direction = 1;
                        predictorViewer.refresh();
                    } else {
                        comparator.direction = 0 - comparator.direction;
                        predictorViewer.refresh();
                    }
                    items = predictorTable.getItems();
                    for(int index = 0; index < items.length; index++) {
                        LSColumn column = (LSColumn) items[index].getData();
                        items[index].setChecked(checkedItems[column.getOrdinal() - 1]);
                    }
                }
            });
        }
    }
    
    protected void createButtonBarContents(Composite parent) {
        parent.setLayout(new GridLayout(10, false));
        
        FormToolkit toolkit = getFormToolkit();
        
        Label separator = toolkit.createLabel(parent, "", SWT.LEFT);
        separator.setLayoutData(createGridData(SWT.FILL, true));
//        separator.setBackground(parent.getBackground());
        separator.setVisible(false);
        
        previousButton = toolkit.createButton(parent, "<< Previous", SWT.PUSH);
        previousButton.setLayoutData(new GridData(85, SWT.DEFAULT));
        previousButton.addSelectionListener(this);
        
        nextButton = toolkit.createButton(parent, "Next >>", SWT.PUSH);
        nextButton.setLayoutData(new GridData(85, SWT.DEFAULT));
        nextButton.addSelectionListener(this);
        
        editButton = toolkit.createButton(parent, "Edit", SWT.PUSH);
        editButton.setLayoutData(new GridData(85, SWT.DEFAULT));
        editButton.addSelectionListener(this);
        
        cancelButton = toolkit.createButton(parent, "Cancel", SWT.PUSH);
        cancelButton.setLayoutData(new GridData(85, SWT.DEFAULT));
        cancelButton.addSelectionListener(this);
    }
    
    protected MenuItem createMenuItem(Menu parent, String text) {
        MenuItem item = new MenuItem(parent, SWT.PUSH);
        item.setText(text);
        item.addSelectionListener(this);
        return item;
    }
    
    protected GridData createGridData(int horizontalAlignment, boolean grabHorizontal) {
        return createGridData(horizontalAlignment, grabHorizontal, 1);
    }
    
    protected GridData createGridData(int horizontalAlignment, boolean grabHorizontal, int horizontalSpan) {
        return new GridData(horizontalAlignment, SWT.CENTER, grabHorizontal, false, horizontalSpan, 1);
    }
    
    protected String notNull(String str) {
        if(str == null) {
            return "";
        } else {
            return str;
        }
    }
    
    protected void resetCombo() {
        Set<String> columns = new TreeSet<String>();
        DBInfo info = DBInfoFactory.getInstance().getDBInfo(getTree().getConfig().getDriver().getName());
        for(LSColumn column : getTree().getConfig().getColumns()) {
            if(!info.isOther(column)) {
                columns.add(column.getName());
            }
        }
        targetCombo.setItems(columns.toArray(new String[columns.size()]));
        targetCombo.setText(notNull(getTree().getConfig().getTargetColumn()));
        if(columns.size() > 0 && targetCombo.getSelectionIndex() == -1) {
            targetCombo.select(0);
        }
    }
    
    protected void resetTable() {
        predictorViewer.setInput(getTree().getConfig());
        TableItem[] items = predictorTable.getItems();
        for(int index = 0; index < items.length; index++) {
            LSColumn column = (LSColumn) items[index].getData();
            items[index].setChecked(column.isPredictor());
        }
    }
    
    protected LSColumnComparator getColumnComparator() {
        if(comparator == null) {
            comparator = new LSColumnComparator();
        }
        return comparator;
    }
    
    public void menuDetected(MenuDetectEvent e) {
        boolean selectedChecked = false;
        boolean selectedUnchecked = false;
        boolean anyChecked = false;
        boolean anyUnchecked = false;
        
        for(TableItem item : predictorTable.getSelection()) {
            if(item.getChecked()) {
                selectedChecked = true;
            } else {
                selectedUnchecked = true;
            }
        }
        
        for(TableItem item : predictorTable.getItems()) {
            if(item.getChecked()) {
                anyChecked = true;
            } else {
                anyUnchecked = true;
            }
        }
        
        checkSelectedItem.setEnabled(selectedUnchecked);
        uncheckSelectedItem.setEnabled(selectedChecked);
        checkAllItem.setEnabled(anyUnchecked);
        uncheckAllItem.setEnabled(anyChecked);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        /* note: we don't set ourself up as a listener to the LSColumn objects because
         * we're assuming that the column objects aren't being changed, they're only
         * getting added or removed (except for the boolean determining which columns
         * are predictors, and we're also assuming all changes to that boolean always
         * come from this page)
         */
        if(evt.getPropertyName().equals(LSConfig.PROPERTY_CLEAR_CONFIG_COLUMNS) ||
                evt.getPropertyName().equals(LSConfig.PROPERTY_ADD_CONFIG_COLUMN)) {
            resetCombo();
            resetTable();
        }
    }
    
    public void widgetDefaultSelected(SelectionEvent e) {
        System.out.println("d:" + e);
    }
    
    public void widgetSelected(SelectionEvent e) {
        if(e.widget == previousButton) {
            sendRequest(new ChangePageRequest(0));
        } else if(e.widget == nextButton) {
            sendRequest(new ChangePageRequest(2));
        } else if(e.widget == editButton) {
            if(editing) {
                // attempt to save changes to the model
                TableItem[] items = predictorTable.getItems();
                boolean[] predictors = new boolean[items.length];
                for(int index = 0; index < items.length; index++) {
                    LSColumn column = (LSColumn) items[index].getData();
                    predictors[column.getOrdinal() - 1] = items[index].getChecked();
                }
                String target = targetCombo.getText();
                
                // send a request. If the config is changed, the bus will send out an event
                sendRequest(new PredictorChangeRequest(getTree().getConfig(), target, predictors));
            } else {
                setEditing(true);
            }
        } else if(e.widget == cancelButton) {
            targetCombo.setText(getTree().getConfig().getTargetColumn());
            resetTable();
            setEditing(false);
        } else if(e.widget == checkSelectedItem) {
            setChecked(predictorTable.getSelection(), true);
        } else if(e.widget == uncheckSelectedItem) {
            setChecked(predictorTable.getSelection(), false);
        } else if(e.widget == checkAllItem) {
            setChecked(predictorTable.getItems(), true);
        } else if(e.widget == uncheckAllItem) {
            setChecked(predictorTable.getItems(), false);
        }
    }
    
    protected void setChecked(TableItem[] items, boolean checked) {
        for(TableItem item : items) {
            item.setChecked(checked);
        }
    }
    
    public void handleEvent(BusEvent event) {
        if(event instanceof PredictorChangeEvent) {
//            PredictorChangeEvent evt = (PredictorChangeEvent) event;
            // TODO what needs to be done here?
            
            targetCombo.setText(getTree().getConfig().getTargetColumn());
            resetTable();
            
            if(getTree().getState() == State.CONFIG_COLUMNS) {
                getTree().setState(State.SPLIT_TREE);
                sendRequest(new ChangePageRequest(2));
            } else if(getTree().getState() != State.SPLIT_TREE) {
                getTree().setState(State.SPLIT_TREE);
            }
            
            sendRequest(new RefreshTreeRequest());
            
            setEditing(false);
        }
    }
    
    protected void setEditing(boolean editing) {
        this.editing = editing;
        targetCombo.setEnabled(editing);
        predictorTable.setEnabled(editing);
        
        nextButton.setEnabled(!editing && getTree().getState() != State.CONFIG_COLUMNS);
        editButton.setText(editing ? "Accept" : "Edit");
        cancelButton.setEnabled(editing && getTree().getState() != State.CONFIG_COLUMNS);
        
        Color bg = getColorCache().getColor(editing ? new RGB(255, 255, 255) : new RGB(240, 242, 242));
        
        targetCombo.setBackground(bg);
        predictorTable.setBackground(bg);
    }
    
    public void activate() {
        setEditing(getTree().getState() == State.CONFIG_COLUMNS);
    }
    
    @Override
    public void dispose() {
        super.dispose();
        if(getTree() != null) {
            getTree().removePropertyChangeListener(this);
            if(getTree().getConfig() != null) {
                getTree().getConfig().removePropertyChangeListener(this);
            }
        }
    }
    
    public void setFocus() {
        targetCombo.setFocus();
    }
    
    private class LSColumnLabelProvider implements ITableLabelProvider {
        public void addListener(ILabelProviderListener listener) {}
        public void dispose() {}
        public boolean isLabelProperty(Object element, String property) {
            return "predictor".equals(property) || "name".equals(property);
        }
        public void removeListener(ILabelProviderListener listener) {}
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
        public String getColumnText(Object element, int columnIndex) {
            if(columnIndex == 0) return null;
            String value = "";
            if(element instanceof LSColumn) {
                LSColumn column = (LSColumn) element;
                if(columnIndex == 1) {
                    value = String.valueOf(column.getOrdinal());
                } else if(columnIndex == 2) {
                    value = column.getName();
                } else if(columnIndex == 3) {
                    value = column.getTypeName();
                } else if(columnIndex == 4) {
                    value = column.getJavaTypeName();
                    if(value != null && value.indexOf('.') > 0) {
                        value = value.substring(value.lastIndexOf('.') + 1);
                    }
                }
            }
            return value;
        }
    }
    
    private class LSColumnComparator extends ViewerComparator {
        int column;
        int direction = 1;
        
        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            LSColumn c1 = (LSColumn) e1;
            LSColumn c2 = (LSColumn) e2;
            if(c1 == null && c2 == null) {
                return 0;
            } else if(c1 == null) {
                return 1;
            } else if(c2 == null) {
                return -1;
            } else {
                int value = 0;
                int column = this.column;
                
                while(value == 0 && column >= 0) {
                    switch(column) {
                        case 0:
                            value = c1.getOrdinal() - c2.getOrdinal();
                            break;
                        case 1:
                            value = c1.getName().compareTo(c2.getName());
                            break;
                        case 2:
                            value = c1.getTypeName().compareTo(c2.getTypeName());
                            break;
                        case 3:
                            String type1 = c1.getJavaTypeName();
                            type1 = type1.substring(type1.lastIndexOf('.') + 1);
                            String type2 = c2.getJavaTypeName();
                            type2 = type2.substring(type2.lastIndexOf('.') + 1);
                            value = type1.compareTo(type2);
                            break;
                    }
                    column -= 1;
                }
                
                return value * direction;
            }
        }
    }
    
    private class LSColumnContentProvider implements IStructuredContentProvider {
        private Object[] NOTHING = new Object[0];
        public Object[] getElements(Object inputElement) {
            if(inputElement instanceof LSConfig) {
                LSConfig config = (LSConfig) inputElement;
                return config.getColumns().toArray();
            }
            return NOTHING;
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
}
