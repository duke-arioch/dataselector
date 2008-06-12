package com.nexusbpm.dataselector.requests;

import com.nexusbpm.multipage.bus.BusRequest;

public class ChangePageRequest implements BusRequest {
    private int pageNumber;
    
    public ChangePageRequest(int pageNumber) {
        this.pageNumber = pageNumber;
    }
    
    public int getPageNumber() {
        return pageNumber;
    }
    
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + pageNumber + ")";
    }
}
