package com.nexusbpm.dataselector.util;

import java.util.Set;

import com.nexusbpm.dataselector.model.LSWhere.Match;

@SuppressWarnings("unchecked")
public class NullRange extends Range {
    public NullRange(Set<Range> container, Match match) {
        super(container, null, match, null, null);
    }
    @Override
    public boolean isBoundary(Comparable value) {
        return value == null;
    }
    
    @Override
    public boolean contains(Comparable value) {
        return value == null;
    }
}