package com.nexusbpm.dataselector.util;

import java.util.Set;

import com.nexusbpm.dataselector.model.LSWhere.Match;

@SuppressWarnings("unchecked")
public class Range implements Comparable<Range> {
    private Comparable min;
    private Match minMatch;
    private Comparable max;
    private Match maxMatch;
    private Set<Range> container;
    
    public Range(Set<Range> container, Comparable min, Match minMatch, Comparable max, Match maxMatch) {
        this.container = container;
        this.min = min;
        this.minMatch = minMatch;
        this.max = max;
        this.maxMatch = maxMatch;
    }
    
    public void setContainer(Set<Range> container) {
        this.container = container;
    }
    public Set<Range> getContainer() {
        return container;
    }
    public void setMin(Comparable min) {
        this.min = min;
    }
    public Comparable getMin() {
        return min;
    }
    public void setMinMatch(Match minMatch) {
        this.minMatch = minMatch;
    }
    public Match getMinMatch() {
        return minMatch;
    }
    public void setMax(Comparable max) {
        this.max = max;
    }
    public Comparable getMax() {
        return max;
    }
    public void setMaxMatch(Match maxMatch) {
        this.maxMatch = maxMatch;
    }
    public Match getMaxMatch() {
        return maxMatch;
    }
    
    /** @return true if the given value is equal to one of the boundaries of this range. */
    public boolean isBoundary(Comparable value) {
        if(value == null) {
            return false;
        }
        return
            (getMin() != null && value.compareTo(getMin()) == 0) ||
            (getMax() != null && value.compareTo(getMax()) == 0);
    }
    
    /** @return true if the given value is within the range. */
    public boolean contains(Comparable value) {
        if(getMinMatch() != null && !getMinMatch().compare(value, getMin())) {
            return false;
        }
        if(getMaxMatch() != null && !getMaxMatch().compare(value, getMax())) {
            return false;
        }
        return true;
    }
    
    /** @return -1, 0, or 1 if the given value is below, within, or above this range. */
    public int compareToRange(Comparable value) {
        if(getMin() != null) {
            int comp = value.compareTo(getMin());
            if(comp < 0 || (comp == 0 && getMinMatch() == Match.GT)) {
                return -1;
            }
        }
        if(getMax() != null) {
            int comp = value.compareTo(getMax());
            if(comp > 0 || (comp == 0 && getMaxMatch() == Match.LT)) {
                return 1;
            }
        }
        return 0;
    }
    
    public int compareTo(Range o) {
        if(this == o) return 0;
        Comparable myObject = getMin() != null ? getMin() : getMax();
        Comparable otherObject = null;
        if(o != null) {
            otherObject = o.getMin() != null ? o.getMin() : o.getMax();
        }
        if(o != null && myObject != null && o.compareToRange(myObject) != 0) {
            return o.compareToRange(myObject);
        } else if(otherObject != null && compareToRange(otherObject) != 0) {
            return -compareToRange(otherObject);
        } else if(myObject == null && otherObject != null) {
            return 1;
        } else if(myObject != null && otherObject == null) {
            return -1;
        } else {
            return 0;
        }
    }
}