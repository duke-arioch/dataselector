package com.nexusbpm.database.table;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.progress.IProgressService;

import com.Ostermiller.util.CSVPrinter;
import com.nexusbpm.database.driver.DriverClassloader;
import com.nexusbpm.database.driver.SQLDriver;

import de.kupzog.ktable.SWTX;

public class ResultSetTableUtil {
    public static void openTable(
            IProgressService progressService, Shell parent, boolean preFetch,
            SQLDriver driver, String uri, String username, String password, String sql)
            throws Exception {
        openTable(
                progressService, parent, preFetch,
                driver, uri, username, password, sql, null);
    }
    
    public static void openTable(
            IProgressService progressService, Shell parent, boolean preFetch,
            SQLDriver driver, String uri, String username, String password, String sql, int rowLimit)
            throws Exception {
        openTable(
                progressService, parent, preFetch,
                driver, uri, username, password, sql, Integer.valueOf(rowLimit));
    }
    
    public static void openTable(
            IProgressService progressService, Shell parent, boolean preFetch,
            SQLDriver driver, String uri, String username, String password, String sql, Integer rowLimit)
            throws Exception {
        ConnectionRunnable runnable = new ConnectionRunnable(
                preFetch, driver, uri, username, password, sql, rowLimit);
        try {
            progressService.run(true, true, new ConnectionProgressRunnable(
                    runnable, parent, preFetch));
        } catch(InvocationTargetException e) {
            throw (Exception) e.getTargetException();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    protected static class ShowTableRunnable implements Runnable {
        private ConnectionRunnable runnable;
        private Shell parent;
        
        public ShowTableRunnable(ConnectionRunnable runnable, Shell parent) {
            this.runnable = runnable;
            this.parent = parent;
        }
        
        public void run() {
            try {
                Shell s = new Shell(parent, SWT.PRIMARY_MODAL | SWT.SHELL_TRIM);
                s.setLayout(new FillLayout());
                
                List<String> headers = new ArrayList<String>();
                Object[][] data = fetchData(runnable.getResultSet(), headers);
                
                Table table = new Table(s, SWT.MULTI | SWT.FULL_SELECTION);
                TableViewer viewer = new TableViewer(table);
                
                table.addKeyListener(new TableCopyPasteListener(viewer, headers));
                
                ResultSetContentProvider provider = new ResultSetContentProvider(data);
                viewer.setLabelProvider(provider);
                viewer.setContentProvider(provider);
                
                table.setLinesVisible(true);
                table.setHeaderVisible(true);
                
                ColumnComparator comparator = new ColumnComparator();
                viewer.setComparator(comparator);
                
                TableLayout layout = new TableLayout();
                
                for(int index = 0; index < headers.size(); index++) {
                    if(index == 0) {
                        layout.addColumnData(new ColumnPixelData(50, true, true));
                    } else {
                        layout.addColumnData(new ColumnPixelData(90, true, true));
                    }
                    TableColumn column = new TableColumn(table, SWT.CENTER);
                    column.setText(headers.get(index));
                    column.addSelectionListener(comparator.createSelectionListener(index, viewer));
                }
                
                table.setLayout(layout);
                
                viewer.setInput(data);
                
                s.setVisible(true);
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    protected static class ShowKTableRunnable implements Runnable {
        private ConnectionRunnable runnable;
        private Shell parent;
        private boolean preFetch;
        
        public ShowKTableRunnable(ConnectionRunnable runnable, Shell parent, boolean preFetch) {
            this.runnable = runnable;
            this.parent = parent;
            this.preFetch = preFetch;
        }
        
        public void run() {
            try {
                Shell s = new Shell(parent, SWT.PRIMARY_MODAL | SWT.SHELL_TRIM);
                s.setLayout(new FillLayout());
                
                ResultSetKTable table = new ResultSetKTable(s, SWTX.AUTO_SCROLL | SWTX.FILL_WITH_DUMMYCOL);
                
                if(preFetch) {
                    ResultSetPreFetchedTableModel model =
                        new ResultSetPreFetchedTableModel(runnable.getResultSet());
                    table.setModel(model);
                } else {
                    ResultSetCachedTableModel model = new ResultSetCachedTableModel(runnable.getResultSet());
                    model.addUpdateListener(table);
                    table.setModel(model);
                }
                
                s.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e) {
                        runnable.close();
                    }
                });
                
                s.setVisible(true);
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    protected static class ConnectionProgressRunnable implements IRunnableWithProgress {
        private ConnectionRunnable runnable;
        private Display display;
        private Shell shell;
        private boolean preFetch;
        
        public ConnectionProgressRunnable(ConnectionRunnable runnable, Shell parent, boolean preFetch) {
            this.runnable = runnable;
            this.shell = parent;
            this.preFetch = preFetch;
            if(parent != null && !parent.isDisposed()) {
                display = parent.getDisplay();
            } else {
                display = Display.getCurrent();
            }
        }
        
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            monitor.beginTask("Downloading Dataset", IProgressMonitor.UNKNOWN);
            try {
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
                    runnable.close();
                } else if(runnable.getException() != null) {
                    throw new InvocationTargetException(runnable.getException());
                } else if(runnable.getResultSet() != null) {
                    if(display != null && !display.isDisposed()) {
                        if(preFetch) {
                            display.asyncExec(new ShowTableRunnable(runnable, shell));
                        } else {
                            display.asyncExec(new ShowKTableRunnable(runnable, shell, preFetch));
                        }
                    }
                }
            } finally {
                monitor.done();
            }
        }
    }
    
    protected static class ConnectionRunnable implements Runnable {
        private SQLDriver driver;
        private String uri;
        private String username;
        private String password;
        private String sql;
        private Integer rowLimit;
        private boolean preFetch;
        
        private Connection connection;
        private Statement statement;
        private ResultSet resultSet;
        private Exception exception;
        
        private boolean closed;
        
        public ConnectionRunnable(
                boolean preFetch,
                SQLDriver driver,
                String uri,
                String username,
                String password,
                String sql,
                Integer rowLimit) {
            this.driver = driver;
            this.uri = uri;
            this.username = username;
            this.password = password;
            this.sql = sql;
            this.rowLimit = rowLimit;
            this.preFetch = preFetch;
        }
        
        public void run() {
            Connection connection = null;
            try {
                connection = DriverClassloader.getConnection(
                        driver.getDriverClassName(), uri, username, password);
            } catch(Exception e) {
                setException(e);
                close();
                return;
            }
            synchronized(this) {
                if(closed) {
                    close(connection);
                    return;
                } else {
                    this.connection = connection;
                }
            }
            
            Statement statement = null;
            try {
                if(preFetch) {
                    statement = connection.createStatement(
                            ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                } else {
                    statement = connection.createStatement(
                            ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                }
            } catch(Exception e) {
                setException(e);
                close();
                return;
            }
            synchronized(this) {
                if(closed) {
                    close(statement);
                    close(this.connection);
                    return;
                } else {
                    this.statement = statement;
                }
            }
            
            ResultSet results = null;
            try {
                if(rowLimit != null) {
                    statement.setMaxRows(rowLimit.intValue());
                }
                results = statement.executeQuery(sql);
            } catch(Exception e) {
                setException(e);
                close();
                return;
            }
            synchronized(this) {
                if(closed) {
                    close(results);
                    close(this.statement);
                    close(this.connection);
                    return;
                } else {
                    this.resultSet = results;
                }
            }
        }
        
        public synchronized void close() {
            closed = true;
            close(resultSet);
            resultSet = null;
            close(statement);
            statement = null;
            close(connection);
            connection = null;
        }
        
        protected void close(Connection connection) {
            if(connection != null) {
                try {
                    connection.close();
                } catch(Exception e) {
                }
            }
        }
        
        protected void close(Statement statement) {
            if(statement != null) {
                try {
                    statement.close();
                } catch(Exception e) {
                }
            }
        }
        
        protected void close(ResultSet resultSet) {
            if(resultSet != null) {
                try {
                    resultSet.close();
                } catch(Exception e) {
                }
            }
        }
        
        public synchronized boolean isClosed() {
            return closed;
        }
        
        public synchronized boolean isDone() {
            return closed || resultSet != null;
        }
        
        public synchronized Connection getConnection() {
            return connection;
        }
        public synchronized void setConnection(Connection connection) {
            this.connection = connection;
        }
        
        public synchronized Statement getStatement() {
            return statement;
        }
        public synchronized void setStatement(Statement statement) {
            this.statement = statement;
        }
        
        public synchronized ResultSet getResultSet() {
            return resultSet;
        }
        public synchronized void setResultSet(ResultSet resultSet) {
            this.resultSet = resultSet;
        }
        
        public synchronized Exception getException() {
            return exception;
        }
        public synchronized void setException(Exception exception) {
            this.exception = exception;
        }
    }
    
    protected static Object[][] fetchData(ResultSet rs, List<String> headers) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        headers.add("row");
        for(int index = 0; index < md.getColumnCount(); index++) {
            headers.add(md.getColumnName(index + 1));
        }
        
        List<Object[]> dataList = new ArrayList<Object[]>();
        
        int rowNum = 1;
        while(rs.next()) {
            Object[] row = new Object[md.getColumnCount() + 1];
            
            row[0] = Integer.valueOf(rowNum);
            rowNum += 1;
            
            for(int index = 1; index <= md.getColumnCount(); index++) {
                row[index] = rs.getObject(index);
            }
            
            dataList.add(row);
        }
        
        return dataList.toArray(new Object[dataList.size()][]);
    }
    
    protected static class TableCopyPasteListener implements KeyListener {
        protected static final int CHAR_C = 'c';
        
        protected ISelectionProvider selectionProvider;
        protected List<String> headers;
        
        public TableCopyPasteListener(ISelectionProvider selectionProvider, List<String> headers) {
            this.selectionProvider = selectionProvider;
            this.headers = headers;
        }
        public void keyPressed(KeyEvent e) {
        }
        public void keyReleased(KeyEvent e) {
            if((SWT.CTRL & e.stateMask) != 0 && e.keyCode == CHAR_C) {
                StringWriter sw = new StringWriter();
                CSVPrinter printer = new CSVPrinter(sw) {
                    String newline = System.getProperty("line.separator");
                    public void writeln() throws IOException {
                        try {
                            out.write(newline);
                            if (autoFlush) flush();
                            newLine = true;
                        } catch (IOException iox){
                            error = true;
                            throw iox;
                        }
                    }
                };
                Object[] selection = ((IStructuredSelection) selectionProvider.getSelection()).toArray();
                
                for(int index = 0; index < headers.size(); index++) {
                    printer.print(headers.get(index));
                }
                printer.println();
                
                for(int rowIndex = 0; rowIndex < selection.length; rowIndex++) {
                    Object[] row = (Object[]) selection[rowIndex];
                    for(int colIndex = 0; colIndex < row.length; colIndex++) {
                        String value = null;
                        if(row[colIndex] != null) {
                            value = row[colIndex].toString();
                        }
                        printer.print(value);
                    }
                    printer.println();
                }
                try {
                    printer.flush();
                    printer.close();
                } catch(IOException ex) {
                }
                
                Clipboard c = new Clipboard(e.display);
                c.setContents(
                        new Object[] {sw.toString()},
                        new Transfer[] {TextTransfer.getInstance()});
            }
        }
    }
}
