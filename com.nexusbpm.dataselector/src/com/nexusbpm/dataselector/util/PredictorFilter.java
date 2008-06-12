package com.nexusbpm.dataselector.util;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.nexusbpm.dataselector.model.config.LSColumn;

public class PredictorFilter extends ViewerFilter {
    private StructuredViewer viewer;
    private boolean showingPredictors;
    
    public PredictorFilter(StructuredViewer viewer, boolean showingPredictors) {
        this.viewer = viewer;
        this.showingPredictors = showingPredictors;
    }
    
    public boolean isShowingPredictors() {
        return showingPredictors;
    }
    
    public void setShowingPredictors(boolean showingPredictors) {
        this.showingPredictors = showingPredictors;
        viewer.refresh(false);
    }
    
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if(element instanceof LSColumn) {
            boolean isPredictor = ((LSColumn) element).isPredictor();
            return showingPredictors == isPredictor;
        }
        return false;
    }
    
}
