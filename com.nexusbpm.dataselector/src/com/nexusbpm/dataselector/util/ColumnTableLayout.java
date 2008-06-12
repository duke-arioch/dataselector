/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.nexusbpm.dataselector.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Exactly like {@link TableLayout} except it resizes the columns as the table resizes.
 */
public class ColumnTableLayout extends TableLayout {

    /**
     * The number of extra pixels taken as horizontal trim by the table column.
     * To ensure there are N pixels available for the content of the column,
     * assign N+COLUMN_TRIM for the column width.
     * 
     * @since 3.1
     */
    private static int COLUMN_TRIM = "carbon".equals(SWT.getPlatform()) ? 24 : 3; //$NON-NLS-1$

    /**
     * The list of column layout data (element type:
     * <code>ColumnLayoutData</code>).
     */
    private List<ColumnLayoutData> columns = new ArrayList<ColumnLayoutData>();
    
    private boolean layingOut;

//    /**
//     * Indicates whether <code>layout</code> has yet to be called.
//     */
//    private boolean firstTime = true;

    /**
     * Creates a new table layout.
     */
    public ColumnTableLayout() {
    }

    /**
     * Adds a new column of data to this table layout.
     * 
     * @param data
     *            the column layout data
     */
    public void addColumnData(ColumnLayoutData data) {
        columns.add(data);
    }

    /*
     * (non-Javadoc) Method declared on Layout.
     */
    public Point computeSize(Composite c, int wHint, int hHint, boolean flush) {
        if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
            return new Point(wHint, hHint);
        }

        Table table = (Table) c;
        // To avoid recursions.
        table.setLayout(null);
        // Use native layout algorithm
        Point result = table.computeSize(wHint, hHint, flush);
        table.setLayout(this);

        int width = 0;
        int size = columns.size();
        for (int i = 0; i < size; ++i) {
            ColumnLayoutData layoutData = columns.get(i);
            if (layoutData instanceof ColumnPixelData) {
                ColumnPixelData col = (ColumnPixelData) layoutData;
                width += col.width;
                if (col.addTrim) {
                    width += COLUMN_TRIM;
                }
            } else if (layoutData instanceof ColumnWeightData) {
                ColumnWeightData col = (ColumnWeightData) layoutData;
                width += col.minimumWidth;
            } else {
                Assert.isTrue(false, "Unknown column layout data");//$NON-NLS-1$
            }
        }
        if (width > result.x) {
            result.x = width;
        }
        return result;
    }

    /*
     * (non-Javadoc) Method declared on Layout.
     */
    public void layout(Composite c, boolean flush) {
        // Only do initial layout. Trying to maintain proportions when resizing
        // is too hard,
        // causes lots of widget flicker, causes scroll bars to appear and
        // occasionally stick around (on Windows),
        // requires hooking column resizing as well, and may not be what the
        // user wants anyway.
//        if (!firstTime) {
//            return;
//        }

        int width = c.getClientArea().width;

        // XXX: Layout is being called with an invalid value the first time
        // it is being called on Linux. This method resets the
        // Layout to null so we make sure we run it only when
        // the value is OK.
        if (width <= 1) {
            return;
        }
        
        // The following excerpt is part of a stack overflow exception's stacktrace:
        /*
    at com.nexusbpm.dataselector.util.ColumnTableLayout.setWidth(ColumnTableLayout.java:219)
    at com.nexusbpm.dataselector.util.ColumnTableLayout.layout(ColumnTableLayout.java:205)
    at org.eclipse.swt.widgets.Composite.updateLayout(Composite.java:1025)
    at org.eclipse.swt.widgets.Composite.WM_SIZE(Composite.java:1371)
    at org.eclipse.swt.widgets.Table.WM_SIZE(Table.java:5469)
    at org.eclipse.swt.widgets.Control.windowProc(Control.java:3743)
    at org.eclipse.swt.widgets.Table.windowProc(Table.java:5025)
    at org.eclipse.swt.widgets.Display.windowProc(Display.java:4351)
    at org.eclipse.swt.internal.win32.OS.CallWindowProcW(Native Method)
    at org.eclipse.swt.internal.win32.OS.CallWindowProc(OS.java:2179)
    at org.eclipse.swt.widgets.Table.callWindowProc(Table.java:261)
    at org.eclipse.swt.widgets.Table.callWindowProc(Table.java:189)
    at org.eclipse.swt.widgets.Control.windowProc(Control.java:3760)
    at org.eclipse.swt.widgets.Table.windowProc(Table.java:5025)
    at org.eclipse.swt.widgets.Display.windowProc(Display.java:4364)
    at org.eclipse.swt.internal.win32.OS.CallWindowProcW(Native Method)
    at org.eclipse.swt.internal.win32.OS.CallWindowProc(OS.java:2179)
    at org.eclipse.swt.widgets.Table.callWindowProc(Table.java:261)
    at org.eclipse.swt.widgets.Table.callWindowProc(Table.java:189)
    at org.eclipse.swt.widgets.Control.windowProc(Control.java:3760)
    at org.eclipse.swt.widgets.Table.windowProc(Table.java:5025)
    at org.eclipse.swt.widgets.Display.windowProc(Display.java:4351)
    at org.eclipse.swt.internal.win32.OS.CallWindowProcW(Native Method)
    at org.eclipse.swt.internal.win32.OS.CallWindowProc(OS.java:2179)
    at org.eclipse.swt.widgets.Table.callWindowProc(Table.java:195)
    at org.eclipse.swt.widgets.Table.callWindowProc(Table.java:189)
    at org.eclipse.swt.widgets.Table.windowProc(Table.java:5023)
    at org.eclipse.swt.widgets.Display.windowProc(Display.java:4364)
    at org.eclipse.swt.internal.win32.OS.CallWindowProcW(Native Method)
    at org.eclipse.swt.internal.win32.OS.CallWindowProc(OS.java:2179)
    at org.eclipse.swt.widgets.Table.callWindowProc(Table.java:261)
    at org.eclipse.swt.widgets.Table.callWindowProc(Table.java:189)
    at org.eclipse.swt.widgets.Control.windowProc(Control.java:3760)
    at org.eclipse.swt.widgets.Table.windowProc(Table.java:5025)
    at org.eclipse.swt.widgets.Display.windowProc(Display.java:4351)
    at org.eclipse.swt.internal.win32.OS.SendMessageW(Native Method)
    at org.eclipse.swt.internal.win32.OS.SendMessage(OS.java:2901)
    at org.eclipse.swt.widgets.TableColumn.setWidth(TableColumn.java:836)
    at com.nexusbpm.dataselector.util.ColumnTableLayout.setWidth(ColumnTableLayout.java:219)
    at com.nexusbpm.dataselector.util.ColumnTableLayout.layout(ColumnTableLayout.java:205)
         */
        if(layingOut) return;
        
        layingOut = true;
        try {
            Item[] tableColumns = getColumns(c);
            int size = Math.min(columns.size(), tableColumns.length);
            int[] widths = new int[size];
            int fixedWidth = 0;
            int numberOfWeightColumns = 0;
            int totalWeight = 0;
    
            // First calc space occupied by fixed columns
            for (int i = 0; i < size; i++) {
                ColumnLayoutData col = columns.get(i);
                if (col instanceof ColumnPixelData) {
                    ColumnPixelData cpd = (ColumnPixelData) col;
                    int pixels = cpd.width;
                    if (cpd.addTrim) {
                        pixels += COLUMN_TRIM;
                    }
                    widths[i] = pixels;
                    fixedWidth += pixels;
                } else if (col instanceof ColumnWeightData) {
                    ColumnWeightData cw = (ColumnWeightData) col;
                    numberOfWeightColumns++;
                    // first time, use the weight specified by the column data,
                    // otherwise use the actual width as the weight
                    // int weight = firstTime ? cw.weight :
                    // tableColumns[i].getWidth();
                    int weight = cw.weight;
                    totalWeight += weight;
                } else {
                    Assert.isTrue(false, "Unknown column layout data");//$NON-NLS-1$
                }
            }
    
            // Do we have columns that have a weight
            if (numberOfWeightColumns > 0) {
                // Now distribute the rest to the columns with weight.
                int rest = width - fixedWidth;
                int totalDistributed = 0;
                for (int i = 0; i < size; ++i) {
                    ColumnLayoutData col = columns.get(i);
                    if (col instanceof ColumnWeightData) {
                        ColumnWeightData cw = (ColumnWeightData) col;
                        // calculate weight as above
                        // int weight = firstTime ? cw.weight :
                        // tableColumns[i].getWidth();
                        int weight = cw.weight;
                        int pixels = totalWeight == 0 ? 0 : weight * rest
                                / totalWeight;
                        if (pixels < cw.minimumWidth) {
                            pixels = cw.minimumWidth;
                        }
                        totalDistributed += pixels;
                        widths[i] = pixels;
                    }
                }
    
                // Distribute any remaining pixels to columns with weight.
                int diff = rest - totalDistributed;
                for (int i = 0; diff > 0; ++i) {
                    if (i == size) {
                        i = 0;
                    }
                    ColumnLayoutData col = columns.get(i);
                    if (col instanceof ColumnWeightData) {
                        ++widths[i];
                        --diff;
                    }
                }
            }
    
    //        firstTime = false;
    
            for (int i = 0; i < size; i++) {
                setWidth(tableColumns[i], widths[i]);
            }
        } finally {
            layingOut = false;
        }
    }

    /**
     * Set the width of the item.
     * 
     * @param item
     * @param width
     */
    private void setWidth(Item item, int width) {
        if (item instanceof TreeColumn) {
            ((TreeColumn) item).setWidth(width);
        } else {
            ((TableColumn) item).setWidth(width);
        }

    }

    /**
     * Return the columns for the receiver.
     * 
     * @param composite
     * @return Item[]
     */
    private Item[] getColumns(Composite composite) {
        if (composite instanceof Tree) {
            return ((Tree) composite).getColumns();
        }
        return ((Table) composite).getColumns();
    }
}
