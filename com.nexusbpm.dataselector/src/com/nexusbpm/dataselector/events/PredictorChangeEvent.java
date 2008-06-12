package com.nexusbpm.dataselector.events;

import java.util.List;

import com.nexusbpm.dataselector.model.config.LSColumn;
import com.nexusbpm.multipage.bus.BusEvent;

public class PredictorChangeEvent implements BusEvent {
    private LSColumn targetColumn;
    private List<LSColumn> addedPredictors;
    private List<LSColumn> removedPredictors;
    
    public PredictorChangeEvent() {
    }
    
    public PredictorChangeEvent(LSColumn targetColumn, List<LSColumn> addedPredictors, List<LSColumn> removedPredictors) {
        this.targetColumn = targetColumn;
        this.addedPredictors = addedPredictors;
        this.removedPredictors = removedPredictors;
    }
    
    public LSColumn getTargetColumn() {
        return targetColumn;
    }
    
    public void setTargetColumn(LSColumn targetColumn) {
        this.targetColumn = targetColumn;
    }
    
    public List<LSColumn> getAddedPredictors() {
        return addedPredictors;
    }
    
    public void setAddedPredictors(List<LSColumn> addedPredictors) {
        this.addedPredictors = addedPredictors;
    }
    
    public List<LSColumn> getRemovedPredictors() {
        return removedPredictors;
    }
    
    public void setRemovedPredictors(List<LSColumn> removedPredictors) {
        this.removedPredictors = removedPredictors;
    }
}
