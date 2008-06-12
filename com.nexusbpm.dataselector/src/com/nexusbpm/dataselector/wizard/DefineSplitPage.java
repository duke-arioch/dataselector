package com.nexusbpm.dataselector.wizard;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;

import com.nexusbpm.dataselector.connection.ConnectionPool;
import com.nexusbpm.dataselector.database.SQLGenerator;
import com.nexusbpm.dataselector.database.SQLGeneratorFactory;
import com.nexusbpm.dataselector.events.ExceptionEvent;
import com.nexusbpm.dataselector.model.LSColumnStats;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.requests.ConfirmationRequest;
import com.nexusbpm.dataselector.requests.GetConnectionPoolRequest;
import com.nexusbpm.dataselector.stats.ResultSetTranslator;
import com.nexusbpm.dataselector.util.Range;
import com.nexusbpm.dataselector.util.RangeSetUtil;
import com.nexusbpm.dataselector.util.RangeSetUtil.RangeSetListener;
import com.nexusbpm.dataselector.wizard.NoProgressWizardDialog.DefaultButtonWizardPage;
import com.nexusbpm.multipage.bus.BusEvent;
import com.nexusbpm.multipage.bus.BusRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;
import com.nexusbpm.multipage.bus.UnhandledRequestException;

public class DefineSplitPage extends WizardPage
    implements SelectionListener, RangeSetListener, DefaultButtonWizardPage {
    public static final String PAGE_NAME = "Define Split";
    
    private EventRequestBus bus;
    
    private List list;
    private ListViewer viewer;
    
    private Button combineButton;
    private Button separateButton;
    private Button populateButton;
    private Button resetButton;
    private Button removeButton;
    
    private AddConditionComposite addConditionComposite;
    
    private RangeSetUtil rangeSets;
    
    private boolean populated;
    
    public DefineSplitPage(EventRequestBus bus, RangeSetUtil rangeSets) {
        super(PAGE_NAME);
        this.bus = bus;
        this.rangeSets = rangeSets;
        rangeSets.addListener(this);
        setPageComplete(false);
        setDescription("Define the values at which to split the data");
    }
    
    public void rangeSetChanged(int event, Object oldValue, Object newValue) {
        if(viewer != null) {
            if((event & EVENT_COLUMN_CHANGED) != 0) {
                resetValues();
                populate(false);
            }
            if((event & EVENT_CLEAR_SETS) != 0) {
                populated = false;
            }
            if(((event & EVENT_CLEAR_SETS) |
                    (event & EVENT_REMAINDER_CHANGED) |
                    (event & EVENT_PRIMARY_SET_CHANGED)) != 0) {
//                if(event == EVENT_CLEAR_SETS) {
//                    populated = false;
//                }
                viewer.refresh();
                resetButtonEnablement();
            }
            if((event & EVENT_SPLIT_TYPE_CHANGED) != 0) {
                if(rangeSets.isCategorical()) {
                    setButtonOrder(
                            new Button[] {removeButton, combineButton, separateButton,
                                    populateButton, resetButton},
                            null);
                    resetButtonEnablement();
                    populate(false);
                } else {
                    setButtonOrder(
                            new Button[] {combineButton, separateButton, resetButton},
                            new Button[] {removeButton, populateButton});
                    resetButtonEnablement();
                }
            }
        }
    }
    
    protected void setButtonOrder(Button[] enabled, Button[] disabled) {
        enabled[0].moveBelow(list);
        enabled[0].setVisible(true);
        
        for(int index = 1; index < enabled.length; index++) {
            enabled[index].moveBelow(enabled[index - 1]);
            enabled[index].setVisible(true);
        }
        
        if(disabled != null && disabled.length > 0) {
            disabled[0].moveBelow(enabled[enabled.length - 1]);
            disabled[0].setVisible(false);
            
            for(int index = 1; index < disabled.length; index++) {
                disabled[index].moveBelow(disabled[index - 1]);
                disabled[index].setVisible(false);
            }
        }
        
        ((Composite) getControl()).layout();
    }
    
    protected void sendRequest(BusRequest request) {
        try {
            bus.handleRequest(request);
        } catch(UnhandledRequestException e) {
            sendEvent(new ExceptionEvent("Bus not configured", e));
        }
    }
    
    protected void sendEvent(BusEvent event) {
        bus.handleEvent(event);
    }
    
    protected void resetValues() {
        rangeSets.clear();
    }
    
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.DOUBLE_BUFFERED);
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 5;
        layout.verticalSpacing = 4;
        composite.setLayout(layout);
        
        list = new List(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 10));
        viewer = new ListViewer(list);
        
        viewer.setContentProvider(new RangeSetUtil.ConditionContentProvider());
        viewer.setLabelProvider(new RangeSetUtil.ConditionLabelProvider());
        
        viewer.addSelectionChangedListener(new SplitTreeSelectionListener());
        
        viewer.setInput(rangeSets);
        
        combineButton = addButton(composite, "Combine");
        separateButton = addButton(composite, "Separate");
        populateButton = addButton(composite, "Populate");
        resetButton = addButton(composite, "Reset");
        removeButton = addButton(composite, "Remove");
        
        Composite bottomComposite = addConditionComposite =
            new AddConditionComposite(composite, rangeSets, getContainer());
        bottomComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
        
        setControl(composite);
        
        resetValues();
        populate(false);
    }
    
    protected Button addButton(Composite parent, String text) {
        Button button = new Button(parent, SWT.PUSH);
        button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        button.setText(text);
        
        button.addSelectionListener(this);
        
        return button;
    }
    
    public void updateDefaultButton() {
        if(addConditionComposite != null) {
            addConditionComposite.updateDefaultButton();
        }
    }
    
    public void widgetDefaultSelected(SelectionEvent e) {
    }
    
    public void widgetSelected(SelectionEvent e) {
        try {
            // TODO update for categorical mode
            if(e.widget == removeButton) {
                removeSelectedRanges();
                // TODO
            } else if(e.widget == combineButton) {
                rangeSets.combine(removeSelectedRanges());
            } else if(e.widget == separateButton) {
                rangeSets.separate(removeSelectedRanges());
            } else if(e.widget == populateButton) {
                populate(true);
            } else if(e.widget == resetButton) {
                resetValues();
            }
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }
    
    /**
     * Removes all selected rangeSets and returns the ranges they contained as
     * a single rangeSet.
     */
    @SuppressWarnings("unchecked")
    protected Set<Range> removeSelectedRanges() {
        Object[] selection = ((IStructuredSelection)viewer.getSelection()).toArray();
        java.util.List<Set<Range>> rangeSets = new ArrayList<Set<Range>>();
        Set<Range> ranges = new TreeSet<Range>();
        for(Object o : selection) {
            Set<Range> rset = (Set<Range>) o;
            rangeSets.add(rset);
            for(Range r : rset) {
                ranges.add(r);
            }
        }
        this.rangeSets.removeAll(rangeSets);
        return ranges;
    }
    
    protected void populate(boolean downloadAsNeeded) {
//        new Exception("POPULATE(" + downloadAsNeeded + ")").printStackTrace();
        if(!rangeSets.isCategorical(true)) {
            return;
        }
        // first see if the values have already been downloaded
        if(rangeSets.getColumn() != null && rangeSets.getNode().getStats() != null) {
            LSColumnStats stats = null;
            for(LSColumnStats cstats : rangeSets.getNode().getStats().getColumnStats()) {
                if(cstats.getColumnOrdinal() == rangeSets.getColumn().getOrdinal()) {
                    stats = cstats;
                    break;
                }
            }
            if(stats != null && stats.getValues().size() > 0) {
                rangeSets.populate(stats.getValues());
                populated = true;
                return;
            }
        }
        if(downloadAsNeeded) {
            // ask the user if we should download the values
            ConfirmationRequest request = new ConfirmationRequest(
                    "Do you want to download the distinct values for the column '" +
                    rangeSets.getColumn().getName() +
                    "' from the database? This operation may take a few minutes.");
            sendRequest(request);
            if(!request.isCancelled()) {
                GetConnectionPoolRequest poolRequest = new GetConnectionPoolRequest(true);
                sendRequest(poolRequest);
                if(poolRequest.getConnectionPool() == null) {
                    // TODO ???
                    return;
                }
                ProgressMonitorDialog dialog = new ProgressMonitorDialog(getContainer().getShell());
                IRunnableWithProgress runnable = new DownloadValuesProgressRunnable(
                        poolRequest.getConnectionPool(), rangeSets.getNode().getTree());
                try {
                    dialog.run(true, true, runnable);
                } catch(InvocationTargetException e) {
                    sendEvent(new ExceptionEvent("Error downloading distinct values", e));
                } catch(InterruptedException e) {
                    sendEvent(new ExceptionEvent("Error downloading distinct values", e));
                }
            }
        }
    }
    
    /** Resets the enablement state of all the buttons based on the current dialog state. */
    @SuppressWarnings("unchecked")
    protected void resetButtonEnablement() {
        if(list != null && viewer != null && combineButton != null && separateButton != null &&
                resetButton != null && rangeSets != null) {
            Object[] selection = ((IStructuredSelection) viewer.getSelection()).toArray();
            int size = selection.length;
//            addButton.setEnabled(!categorical); // leave the add button enabled all the time
            removeButton.setEnabled(size > 0);
            combineButton.setEnabled(size > 1 && size < viewer.getList().getItemCount());
            resetButton.setEnabled(list.getItemCount() > 0);
            boolean separable = false;
            for(Object o : selection) {
                Set<Range> conds = (Set<Range>) o;
                if(conds.size() > 1) {
                    separable = true;
                    break;
                }
            }
            separateButton.setEnabled(separable);
            populateButton.setEnabled(!populated);
            setPageComplete(rangeSets.getRemainderSize() > 0 || rangeSets.getTotalSize() > 1);
        }
    }
    
    protected class DownloadValuesRunnable implements Runnable {
        private ConnectionPool pool;
        private java.util.List<Object> values;
        private boolean done;
        private Statement statement;
        private LSTree tree;
        
        public DownloadValuesRunnable(ConnectionPool pool, LSTree tree) {
            this.pool = pool;
            this.tree = tree;
        }
        
        public void run() {
            // TODO optimally we can off-load this functionality into the
            // com.nexusbpm.dataselector.stats package somewhere
            
            synchronized(tree) {
                String driverName = tree.getConfig().getDriver().getName();
                SQLGenerator generator = SQLGeneratorFactory.getInstance().getGenerator(driverName);
                
                Connection connection = null;
                try {
                    connection = pool.getConnection();
                    statement = connection.createStatement();
                    
                    ResultSet rs = statement.executeQuery(
                            "select distinct " + rangeSets.getColumn().getName() + " " +
                            generator.getFromClause(rangeSets.getNode()) + " " +
                            generator.getWhereClause(rangeSets.getNode()));
                    
                    java.util.List<Object> values = ResultSetTranslator.getValues(
                            rangeSets.getColumn().getJavaTypeName(), rs);
                    
                    // TODO save stats back to the node?
                    
                    this.values = values;
                    done = true;
                } catch(SQLException e) {
                    e.printStackTrace(); // TODO
                } finally {
                    Statement statement = this.statement;
                    this.statement = null;
                    try {
                        statement.close();
                    } catch(SQLException e) {
                    }
                    pool.releaseConnection(connection);
                }
            }
        }
        
        public java.util.List<Object> getValues() {
            return values;
        }
        
        public boolean isDone() {
            return done;
        }
        
        public void cancel() {
            Statement statement = this.statement;
            if(statement != null) {
                try {
                    statement.cancel();
                    statement.close();
                } catch(SQLException e) {
                    // ignore
                }
            }
        }
    }
    
    protected class DownloadValuesProgressRunnable implements IRunnableWithProgress {
        private ConnectionPool pool;
        private LSTree tree;
        
        public DownloadValuesProgressRunnable(ConnectionPool pool, LSTree tree) {
            this.pool = pool;
            this.tree = tree;
        }
        
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            try {
                monitor.beginTask("Downloading distinct values", IProgressMonitor.UNKNOWN);
                final DownloadValuesRunnable runnable = new DownloadValuesRunnable(pool, tree);
                Thread t = new Thread(runnable);
                t.start();
                
                while(!monitor.isCanceled() && !runnable.isDone()) {
                    Thread.yield();
                    try {
                        Thread.sleep(125);
                    } catch(InterruptedException e) {
                    }
                }
                if(monitor.isCanceled()) {
                    runnable.cancel();
                }
                if(runnable.isDone() && runnable.getValues() != null) {
                    Display display = null;
                    try {
                        display = list.getDisplay();
                    } catch(SWTException e) {
                    }
                    if(display != null) {
                        display.asyncExec(new Runnable() {
                            public void run() {
                                rangeSets.populate(runnable.getValues());
                                populated = true;
                            }
                        });
                    }
                }
            } catch(Exception e) {
                sendEvent(new ExceptionEvent("Error downloading distinct values", e));
            } finally {
                monitor.done();
            }
        }
    }
    
    protected class SplitTreeSelectionListener implements ISelectionChangedListener {
        public SplitTreeSelectionListener() {
        }
        public void selectionChanged(SelectionChangedEvent event) {
            resetButtonEnablement();
        }
    }
}
