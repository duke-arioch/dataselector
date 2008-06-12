package com.nexusbpm.dataselector.requests;

import java.util.Collections;
import java.util.List;

import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.multipage.bus.BusRequest;

public class DownloadStatisticsRequest implements BusRequest {
    private List<LSNode> nodes;
    
    public DownloadStatisticsRequest(LSNode node) {
        this.nodes = Collections.singletonList(node);
    }
    
    public DownloadStatisticsRequest(List<LSNode> nodes) {
        this.nodes = nodes;
    }
    
    public List<LSNode> getNodes() {
        return nodes;
    }
    
    public void setNodes(List<LSNode> nodes) {
        this.nodes = nodes;
    }
}
