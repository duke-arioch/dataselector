package com.nexusbpm.dataselector.requests;

import java.util.List;

import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.multipage.bus.BusRequest;

public class CancelDownloadRequest implements BusRequest {
    private List<LSNode> nodes;
    
    public CancelDownloadRequest(List<LSNode> nodes) {
        this.nodes = nodes;
    }
    
    public List<LSNode> getNodes() {
        return nodes;
    }
    
    public void setNodes(List<LSNode> nodes) {
        this.nodes = nodes;
    }
}
