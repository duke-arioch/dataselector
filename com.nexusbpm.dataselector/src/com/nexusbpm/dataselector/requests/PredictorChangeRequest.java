package com.nexusbpm.dataselector.requests;

import com.nexusbpm.dataselector.model.config.LSConfig;
import com.nexusbpm.multipage.bus.BusRequest;

public class PredictorChangeRequest implements BusRequest {
    private LSConfig config;
    private String target;
    private boolean[] predictors;
    
    public PredictorChangeRequest(LSConfig config, String target, boolean[] predictors) {
        this.config = config;
        this.target = target;
        this.predictors = predictors;
    }
    
    public LSConfig getConfig() {
        return config;
    }
    
    public void setConfig(LSConfig config) {
        this.config = config;
    }
    
    public String getTarget() {
        return target;
    }
    
    public void setTarget(String target) {
        this.target = target;
    }
    
    public boolean[] getPredictors() {
        return predictors;
    }
    
    public void setPredictors(boolean[] predictors) {
        this.predictors = predictors;
    }
}
