package com.nexusbpm.database.table;

import org.eclipse.swt.widgets.Composite;

import de.kupzog.ktable.KTable;

public class ResultSetKTable extends KTable implements ResultSetTableUpdateListener {
    public ResultSetKTable(Composite parent, int style) {
        super(parent, style);
    }

    public void handleUpdate(int rows) {
        m_FocusRow = rows - 1;
        scroll(m_LeftColumn, rows - 1);
    }
}
