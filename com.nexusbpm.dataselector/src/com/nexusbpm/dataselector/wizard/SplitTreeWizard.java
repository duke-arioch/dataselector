package com.nexusbpm.dataselector.wizard;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import com.nexusbpm.dataselector.Plugin;
import com.nexusbpm.dataselector.commands.SplitTreeCommand;
import com.nexusbpm.dataselector.events.ExceptionEvent;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSWhere.Match;
import com.nexusbpm.dataselector.requests.ExecuteCommandRequest;
import com.nexusbpm.dataselector.util.NullRange;
import com.nexusbpm.dataselector.util.Range;
import com.nexusbpm.dataselector.util.RangeSetUtil;
import com.nexusbpm.dataselector.util.RangeSetUtil.RangeSetListener;
import com.nexusbpm.multipage.bus.BusRequest;
import com.nexusbpm.multipage.bus.EventRequestBus;
import com.nexusbpm.multipage.bus.UnhandledRequestException;

public class SplitTreeWizard extends Wizard implements RangeSetListener {
    private RangeSetUtil rangeSets;
    
    private EventRequestBus bus;
    
    public SplitTreeWizard(LSNode node, EventRequestBus bus) {
        this.bus = bus;
        rangeSets = new RangeSetUtil(node);
        rangeSets.addListener(this);
        ImageDescriptor desc = Plugin.imageDescriptorFromPlugin(Plugin.PLUGIN_ID, "/icons/tree_wizard.png");
        setDefaultPageImageDescriptor(desc);
        setNeedsProgressMonitor(false);
        if(node.getName() == null) {
            setWindowTitle("Split Tree");
        } else {
            setWindowTitle("Split Tree at node '" + node.getName() + "'");
        }
    }
    
    protected RangeSetUtil getRangeSets() {
        return rangeSets;
    }
    
    @Override
    public void addPages() {
         addPage(new SelectPredictorPage(rangeSets));
         addPage(new SelectSplitTypePage(rangeSets));
         addPage(new DefineSplitPage(bus, rangeSets));
         addPage(new SelectCategoriesPage(rangeSets));
    }
    
    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        IWizardPage next = null;
        if(page.getName().equals(SelectPredictorPage.PAGE_NAME)) {
            next = getPage(SelectSplitTypePage.PAGE_NAME);
        } else if(page.getName().equals(SelectSplitTypePage.PAGE_NAME)) {
            String splitType = getRangeSets().getSplitType();
            if(splitType == null) {
                splitType = getRangeSets().getDefaultSplitType();
            }
            if(!splitType.equals(RangeSetUtil.SPLIT_NULL)) {
                next = getPage(DefineSplitPage.PAGE_NAME);
            }
        } else if(page.getName().equals(DefineSplitPage.PAGE_NAME)) {
            next = getPage(SelectCategoriesPage.PAGE_NAME);
        }
        return next;
    }
    
    @Override
    public boolean canFinish() {
        if(getRangeSets() == null || getRangeSets().getNode() == null || getRangeSets().getColumn() == null) {
            return false;
        }
        
        String splitType = getRangeSets().getSplitType();
        if(splitType == null) {
            splitType = getRangeSets().getDefaultSplitType();
        }
        
        if(splitType.equals(RangeSetUtil.SPLIT_CONTINUOUS)) {
            
        } else if(splitType.equals(RangeSetUtil.SPLIT_CATEGORICAL)) {
            
        } else if(splitType.equals(RangeSetUtil.SPLIT_NULL)) {
            return true;
        }
        
        if(rangeSets.getTotalSize() < 2) {
            return false;
        }
        
        // TODO Auto-generated method stub
        return super.canFinish();
    }
    
    @Override
    public boolean performFinish() {
        if(getRangeSets() == null || getRangeSets().getNode() == null || getRangeSets().getColumn() == null) {
            return false;
        }
        
        String splitType = getRangeSets().getSplitType();
        if(splitType == null) {
            splitType = getRangeSets().getDefaultSplitType();
        }
        
        if(splitType.equals(RangeSetUtil.SPLIT_NULL)) {
            Set<Range> isNull = new HashSet<Range>();
            Set<Range> notNull = new HashSet<Range>();
            Set<Set<Range>> rangeSets = new HashSet<Set<Range>>();
            isNull.add(new NullRange(isNull, Match.EQ));
            notNull.add(new NullRange(notNull, Match.NEQ));
            rangeSets.add(isNull);
            rangeSets.add(notNull);
            
            sendRequest(new ExecuteCommandRequest(new SplitTreeCommand(
                    bus, getRangeSets().getNode(), getRangeSets().getColumn(), rangeSets)));
            
            return true;
        }
        DefineSplitPage page = (DefineSplitPage) getPage(DefineSplitPage.PAGE_NAME);
        if(page == null || rangeSets.getTotalSize() < 2) {
            return false;
        }
        
        sendRequest(new ExecuteCommandRequest(new SplitTreeCommand(
                bus, getRangeSets().getNode(), getRangeSets().getColumn(), rangeSets.getRangeSets())));
        
        return true;
    }
    
    public void rangeSetChanged(int event, Object oldValue, Object newValue) {
        if((event & EVENT_SPLIT_TYPE_CHANGED) != 0) {
            getContainer().updateButtons();
        }
    }
    
    protected void sendRequest(BusRequest request) {
        try {
            bus.handleRequest(request);
        } catch(UnhandledRequestException e) {
            bus.handleEvent(new ExceptionEvent("bus not configured", e));
        }
    }
}
