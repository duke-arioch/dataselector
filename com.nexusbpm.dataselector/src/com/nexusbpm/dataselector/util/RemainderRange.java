package com.nexusbpm.dataselector.util;

import java.util.Set;

@SuppressWarnings("unchecked")
public class RemainderRange extends Range {
    public RemainderRange(Set<Range> container) {
        super(container, null, null, null, null);
    }
//    @Override
//    public boolean isBoundary(Comparable value) {
//        return false;
//    }
//    
//    @Override
//    public boolean contains(Comparable value) {
//        return false;
//    }
}