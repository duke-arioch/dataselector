package com.nexusbpm.dataselector.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

import com.nexusbpm.dataselector.util.Range;
import com.nexusbpm.dataselector.util.RangeSetUtil;
import com.nexusbpm.dataselector.util.RangeSetUtil.RangeSetListener;

public class SelectCategoriesPage extends WizardPage implements SelectionListener, RangeSetListener {
    public static final String PAGE_NAME = "Select Categories";
    
    private Label remainderLabel;
    private Label primaryLabel;
    
    private List remainderList;
    private List primaryList;
    
    private ListViewer remainderViewer;
    private ListViewer primaryViewer;
    
    private Label remainderDescription;
    private Label primaryDescription;
    
    private Button includeButton;
    private Button excludeButton;
    
    private RangeSetUtil rangeSets;
    
    public SelectCategoriesPage(RangeSetUtil rangeSets) {
        super(PAGE_NAME);
        this.rangeSets = rangeSets;
        rangeSets.addListener(this);
    }
    
//    @Override
//    public SplitTreeWizard getWizard() {
//        return (SplitTreeWizard) super.getWizard();
//    }
    
    public void createControl(Composite container) {
        final Composite parent = new Composite(container, SWT.NONE);
        parent.setLayout(new FormLayout());
        
        final int marginHeight = 12;
        final int marginWidth = 18;
        final int verticalSpacing = 6;
        final int horizontalSpacing = 8;
        
        FormData data;
        
        remainderLabel = new Label(parent, SWT.CENTER);
        remainderLabel.setText("Remainder");
        
        primaryLabel = new Label(parent, SWT.CENTER);
        primaryLabel.setText("Primary Categories");
        
        remainderList = new List(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        remainderList.addSelectionListener(this);
        remainderViewer = new ListViewer(remainderList);
        
        includeButton = new Button(parent, SWT.PUSH);
        includeButton.addSelectionListener(this);
        includeButton.setText("->");
        
        primaryList = new List(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        primaryList.addSelectionListener(this);
        primaryViewer = new ListViewer(primaryList);
        
        excludeButton = new Button(parent, SWT.PUSH);
        excludeButton.addSelectionListener(this);
        excludeButton.setText("<-");
        
        remainderDescription = new Label(parent, SWT.LEFT | SWT.WRAP);
        remainderDescription.setText( //"");
                "All categories in this box will be grouped together as a \"remainder\" node");
        
        primaryDescription = new Label(parent, SWT.LEFT | SWT.WRAP);
        primaryDescription.setText( //"");
                "Each category in this box will be given its own node");
        
        data = new FormData();
        data.left = new FormAttachment(remainderList, 0, SWT.LEFT);
        data.right = new FormAttachment(remainderList, 0, SWT.RIGHT);
        data.top = new FormAttachment(0, marginHeight);
        remainderLabel.setLayoutData(data);
        
        data = new FormData();
        data.left = new FormAttachment(primaryList, 0, SWT.LEFT);
        data.right = new FormAttachment(primaryList, 0, SWT.RIGHT);
        data.top = new FormAttachment(0, marginHeight);
        primaryLabel.setLayoutData(data);
        
        data = new FormData();
        data.left = new FormAttachment(remainderList, 0, SWT.LEFT);
        data.right = new FormAttachment(remainderList, 0, SWT.RIGHT);
        data.bottom = new FormAttachment(100, -marginHeight);
        remainderDescription.setLayoutData(data);
        
        data = new FormData();
        data.left = new FormAttachment(primaryList, 0, SWT.LEFT);
        data.right = new FormAttachment(primaryList, 0, SWT.RIGHT);
        data.top = new FormAttachment(remainderDescription, 0, SWT.TOP);
        data.bottom = new FormAttachment(100, -marginHeight);
        primaryDescription.setLayoutData(data);
        
        data = new FormData();
        data.left = new FormAttachment(0, marginWidth);
        data.right = new FormAttachment(50, -35);
        data.top = new FormAttachment(remainderLabel, verticalSpacing);
        data.bottom = new FormAttachment(remainderDescription, -verticalSpacing);
        remainderList.setLayoutData(data);
        
        data = new FormData();
        data.left = new FormAttachment(50, 35);
        data.right = new FormAttachment(100, -marginWidth);
        data.top = new FormAttachment(primaryLabel, verticalSpacing);
        data.bottom = new FormAttachment(primaryDescription, -verticalSpacing);
        primaryList.setLayoutData(data);
        
        data = new FormData();
        data.left = new FormAttachment(50, horizontalSpacing - 35);
        data.right = new FormAttachment(50, 35 - horizontalSpacing);
        data.bottom = new FormAttachment(50, -verticalSpacing * 2);
        includeButton.setLayoutData(data);
        
        final FormData iData = data;
        
        data = new FormData();
        data.left = new FormAttachment(50, horizontalSpacing - 35);
        data.right = new FormAttachment(50, 35 - horizontalSpacing);
        data.top = new FormAttachment(50, -verticalSpacing);
        excludeButton.setLayoutData(data);
        
        final FormData eData = data;
        
        primaryList.addControlListener(new ControlListener() {
            public void controlMoved(ControlEvent e) {
            }
            public void controlResized(ControlEvent e) {
                iData.bottom = new FormAttachment(0,
                        marginHeight + (primaryList.getBounds().height - verticalSpacing) / 2);
                eData.top = new FormAttachment(0,
                        marginHeight + (primaryList.getBounds().height + verticalSpacing) / 2);
                parent.layout();
            }
        });
        
        primaryViewer.setContentProvider(new RangeSetUtil.ConditionContentProvider(false));
        remainderViewer.setContentProvider(new RangeSetUtil.ConditionContentProvider(true));
        
        primaryViewer.setLabelProvider(new RangeSetUtil.ConditionLabelProvider());
        remainderViewer.setLabelProvider(new RangeSetUtil.ConditionLabelProvider());
        
        rangeSets.addListener(this);
        
        primaryViewer.setInput(rangeSets);
        remainderViewer.setInput(rangeSets);
        
        updateButtonEnablement();
        
        setControl(parent);
    }
    
    public void rangeSetChanged(int event, Object oldValue, Object newValue) {
        if(primaryViewer != null && remainderViewer != null) {
            if(((event & EVENT_CLEAR_SETS) | (event & EVENT_PRIMARY_SET_CHANGED)) != 0) {
                primaryViewer.refresh();
            }
            if(((event & EVENT_CLEAR_SETS) | (event & EVENT_REMAINDER_CHANGED)) != 0) {
                remainderViewer.refresh();
            }
        }
    }
    
    protected void updateButtonEnablement() {
        includeButton.setEnabled(!remainderViewer.getSelection().isEmpty());
        excludeButton.setEnabled(!primaryViewer.getSelection().isEmpty());
    }
    
//    @Override
//    public void setVisible(boolean visible) {
//        if(visible) {
////            updateButtons(); // TODO do we need to call this here?
//            System.out.println("setVisible(true)");
//            if(getWizard().getSplitType() == null ||
//                    (!continuousButton.getSelection() &&
//                    !categoricalButton.getSelection() &&
//                    !nullButton.getSelection())) {
//                System.out.println("nothing selected");
//                String splitType = getWizard().getSplitType();
//                if(splitType == null) {
//                    splitType = getWizard().getDefaultSplitType();
//                }
//                if(splitType.equals(SplitTreeWizard.SPLIT_CONTINUOUS)) {
//                    continuousButton.setSelection(true);
//                    categoricalButton.setSelection(false);
//                    nullButton.setSelection(false);
//                } else if(splitType.equals(SplitTreeWizard.SPLIT_CATEGORICAL)) {
//                    continuousButton.setSelection(false);
//                    categoricalButton.setSelection(true);
//                    nullButton.setSelection(false);
//                } else {
//                    continuousButton.setSelection(false);
//                    categoricalButton.setSelection(false);
//                    nullButton.setSelection(true);
//                }
//            }
//        }
//        super.setVisible(visible);
//        getWizard().getContainer().updateButtons();
//    }
    
//    public void updateButtons() {
//        DBInfo info = getWizard().getDBInfo();
//        continuousButton.setEnabled(info.isContinuous(getWizard().getColumn()));
//        if(!continuousButton.getEnabled() && continuousButton.getSelection()) {
//            continuousButton.setSelection(false);
//        }
//    }
    
    public void widgetDefaultSelected(SelectionEvent e) {
    }
    
    @SuppressWarnings("unchecked")
    public void widgetSelected(SelectionEvent e) {
        if(e.widget == primaryList) {
//            excludedList.deselectAll();
        } else if(e.widget == remainderList) {
//            includedList.deselectAll();
        } else if(e.widget == includeButton) {
//            Object[] sel = ((IStructuredSelection) remainderViewer.getSelection()).toArray();
            rangeSets.moveFromRemainder(getSelectedRanges(remainderViewer));
            getContainer().updateButtons();
//            remainderViewer.remove(sel);
//            primaryViewer.add(sel);
        } else if(e.widget == excludeButton) {
            rangeSets.moveToRemainder(getSelectedRanges(primaryViewer));
            getContainer().updateButtons();
//            Object[] sel = ((IStructuredSelection) primaryViewer.getSelection()).toArray();
//            primaryViewer.remove(sel);
//            remainderViewer.add(sel);
        }
        updateButtonEnablement();
    }
    
    @SuppressWarnings("unchecked")
    protected Collection<Set<Range>> getSelectedRanges(ListViewer viewer) {
        Object[] sel = ((IStructuredSelection) viewer.getSelection()).toArray();
        Collection<Set<Range>> rangeSets = new ArrayList<Set<Range>>();
        for(Object o : sel) {
            rangeSets.add((Set<Range>) o);
        }
        return rangeSets;
    }
}
