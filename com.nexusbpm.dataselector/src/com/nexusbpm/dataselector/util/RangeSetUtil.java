package com.nexusbpm.dataselector.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

import com.nexusbpm.database.info.DBInfo;
import com.nexusbpm.database.info.DBInfoFactory;
import com.nexusbpm.dataselector.model.LSColumnStats;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSWhere.Match;
import com.nexusbpm.dataselector.model.config.LSColumn;

public class RangeSetUtil {
    public static final String SPLIT_CONTINUOUS = "Continuous";
    public static final String SPLIT_CATEGORICAL = "Categorical";
    public static final String SPLIT_NULL = "Null";
    
    private List<RangeSetListener> listeners;
    private Set<Set<Range>> primaryRangeSets;
    private Set<Set<Range>> remainderRangeSets;
    private Set<Set<Range>> allRangeSets;
    
    private LSNode node;
    private LSColumn column;
    
    private String splitType;
    
    public RangeSetUtil(LSNode node) {
        this.node = node;
        this.listeners = new ArrayList<RangeSetListener>();
        this.primaryRangeSets = new TreeSet<Set<Range>>(new RangeSetComparator());
        this.remainderRangeSets = new TreeSet<Set<Range>>(new RangeSetComparator());
        this.allRangeSets = new TreeSet<Set<Range>>(new RangeSetComparator());
    }
    
    public void addListener(RangeSetListener listener) {
        if(!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(RangeSetListener listener) {
        listeners.remove(listener);
    }
    
    protected void fireEvent(int event) {
        fireEvent(event, null, null);
    }
    
    protected void fireEvent(int event, Object oldValue, Object newValue) {
        for(RangeSetListener listener : listeners.toArray(new RangeSetListener[listeners.size()])) {
            listener.rangeSetChanged(event, oldValue, newValue);
        }
    }
    
    public LSColumn getColumn() {
        return column;
    }
    
    public void setColumn(LSColumn column) {
        if(this.column != column &&
                (this.column == null || column == null || this.column.compareTo(column) != 0)) {
            String oldSplitType = getSplitType(true);
            LSColumn oldColumn = this.column;
            this.column = column;
            String newSplitType = getSplitType(true);
            int event = RangeSetListener.EVENT_COLUMN_CHANGED;
            if(oldSplitType != newSplitType || oldSplitType != null && !oldSplitType.equals(newSplitType)) {
                event |= RangeSetListener.EVENT_SPLIT_TYPE_CHANGED;
            }
            fireEvent(event, oldColumn, column);
        }
    }
    
    public LSNode getNode() {
        return node;
    }
    
    public String getSplitType() {
        return splitType;
    }
    
    public String getSplitType(boolean defaultOnNull) {
        String splitType = this.splitType;
        if(splitType == null && defaultOnNull) {
            splitType = getDefaultSplitType();
        }
        return splitType;
    }
    
    public void setSplitType(String splitType) {
        if(this.splitType != splitType &&
                (this.splitType == null || splitType == null || !this.splitType.equals(splitType))) {
            String oldSplitType = this.splitType;
            this.splitType = splitType;
            clear();
            fireEvent(RangeSetListener.EVENT_SPLIT_TYPE_CHANGED, oldSplitType, splitType);
        }
    }
    
    public DBInfo getDBInfo() {
        String driver = null;
        if(getColumn() != null) {
            driver = getColumn().getTree().getConfig().getDriver().getName();
        }
        return DBInfoFactory.getInstance().getDBInfo(driver);
    }
    
    /** Get the default split type for the current column. */
    public String getDefaultSplitType() {
        String splitType = SPLIT_CONTINUOUS;
        if(getColumn() != null) {
            if(!getDBInfo().isContinuous(getColumn())) {
                splitType = SPLIT_CATEGORICAL;
            } else if(getNode() != null && getNode().getStats() != null) {
                int ordinal = getColumn().getOrdinal();
                for(LSColumnStats cstats : getNode().getStats().getColumnStats()) {
                    if(cstats.getColumnOrdinal() == ordinal) {
                        if(cstats.getValues().size() > 0) {
                            splitType = SPLIT_CATEGORICAL;
                        }
                        break;
                    }
                }
            }
        }
        return splitType;
    }
    
    public boolean isContinuous() {
        return isContinuous(false);
    }
    
    public boolean isContinuous(boolean defaultOnNull) {
        String splitType = getSplitType(defaultOnNull);
        return splitType != null && splitType.equals(SPLIT_CONTINUOUS);
    }
    
    public boolean isCategorical() {
        return isCategorical(false);
    }
    
    public boolean isCategorical(boolean defaultOnNull) {
        String splitType = getSplitType(defaultOnNull);
        return splitType != null && splitType.equals(SPLIT_CATEGORICAL);
    }
    
    /** The total number of nodes that will be needed. */
    public int getTotalSize() {
        return primaryRangeSets.size() + (remainderRangeSets.size() > 0 ? 1 : 0);
    }
    
    public int getPrimarySetSize() {
        return primaryRangeSets.size();
    }
    
    public int getRemainderSize() {
        return remainderRangeSets.size();
    }
    
    /** Return the final range sets to use to split the tree. */
    public Collection<Set<Range>> getRangeSets() {
        List<Set<Range>> rangeSets = new ArrayList<Set<Range>>(primaryRangeSets);
        if(getRemainderSize() > 0) {
            Set<Range> rset = new TreeSet<Range>();
            rset.add(new RemainderRange(rset));
            rangeSets.add(rset);
        }
        return rangeSets;
    }
    
    public void clear() {
        int event = RangeSetListener.EVENT_CLEAR_SETS;
        if(primaryRangeSets.size() > 0) {
            primaryRangeSets.clear();
            event |= RangeSetListener.EVENT_PRIMARY_SET_CHANGED;
        }
        if(remainderRangeSets.size() > 0) {
            remainderRangeSets.clear();
            event |= RangeSetListener.EVENT_REMAINDER_CHANGED;
        }
        if(allRangeSets.size() > 0) {
            allRangeSets.clear();
            fireEvent(event);
        }
    }
    
    public void removeAll(Collection<Set<Range>> rangeSets) {
        if(allRangeSets.removeAll(rangeSets)) {
            boolean primaryChanged = primaryRangeSets.removeAll(rangeSets);
            boolean remainderChanged = remainderRangeSets.removeAll(rangeSets);
            int event = 0;
            if(primaryChanged) {
                event |= RangeSetListener.EVENT_PRIMARY_SET_CHANGED;
            }
            if(remainderChanged) {
                event |= RangeSetListener.EVENT_REMAINDER_CHANGED;
            }
            if(event != 0) {
                fireEvent(event);
            }
        }
    }
    
    /** Returns true if the given match/value pair can be added. */
    public boolean canAddCondition(Match match, Object value) {
        try {
            getContainingRange(match, value, allRangeSets);
            // at this point we either have a range we can modify or no ranges
            // need to be modified to add the value
            return true;
        } catch(IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Returns the range that will need to be modified to add the new split.
     * If null is returned, then no ranges were found within the given set.
     * If this function returns null for both the primary and remainder range
     * sets, then no ranges need to be modified and new ranges can be added.
     * 
     * @throws IllegalArgumentException if the specified split cannot be added
     * @see #canAddCondition(Match, Object)
     */
    @SuppressWarnings("unchecked")
    protected Range getContainingRange(Match match, Object value, Set<Set<Range>> rangeSets) {
        if(value == null) {
            if(match != Match.EQ) {
                throw new IllegalArgumentException();
            }
            for(Set<Range> rset : rangeSets) {
                for(Range r : rset) {
                    if(r.contains(null)) {
                        throw new IllegalArgumentException();
                    }
                }
            }
        } else {
            for(Set<Range> rset : rangeSets) {
                for(Range r : rset) {
                    // first check the boundaries to make sure it's not an invalid match
                    if(r.getMin() != null && r.getMin().compareTo(value) == 0) {
                        if(match == Match.EQ && r.getMinMatch() == Match.GTE) {
                            // we can convert the GTE to GT and add EQ
                            return r;
                        } else if(match != Match.EQ || r.getMinMatch() == Match.EQ) {
                            // at this point we know we have a categorical split
                            // or EQ, GT, and LT, so we can't split
                            throw new IllegalArgumentException();
                        }
                    }
                    if(r.getMax() != null && r.getMax().compareTo(value) == 0) {
                        if(match == Match.EQ && r.getMaxMatch() == Match.LTE) {
                            // we can convert the LTE to LT and add EQ
                            return r;
                        } else if(match != Match.EQ || r.getMaxMatch() == Match.EQ) {
                            // at this point we know we have a categorical split
                            // or EQ, GT, and LT, so we can't split
                            throw new IllegalArgumentException();
                        }
                    }
                    // if the boundaries didn't match, see if the range contains the new boundary value
                    if(r.contains((Comparable) value)) {
                        return r;
                    }
                }
            }
        }
        
        return null;
    }
    
    public void addCondition(Match match, Object value) {
        Range old = null;
        boolean remainderModified = false;
        try {
            old = getContainingRange(match, value, allRangeSets);
        } catch(IllegalArgumentException e) {
            // trying to add a condition that can't be added...
            // eat the exception or let it propagate?
            e.printStackTrace();
            return;
        }
        
        if(value == null || (isCategorical(true) && match == Match.EQ)) {
            // add =null and categorical values directly
            Set<Range> range = new TreeSet<Range>();
            range.add(createRange(range, match, value));
            addToPrimary(range);
        } else if(old == null) {
            // if(old == null) then we don't need to modify existing ranges, so just add new ones
            if(match == Match.EQ) {
                // for EQ create 3 sets: less than value, equal to value, greater than value
                Set<Range> range1 = new TreeSet<Range>();
                range1.add(createRange(range1, Match.LT, value));
                Set<Range> range2 = new TreeSet<Range>();
                range2.add(createRange(range2, Match.EQ, value));
                Set<Range> range3 = new TreeSet<Range>();
                range3.add(createRange(range3, Match.GT, value));
                addToPrimary(range1);
                addToPrimary(range2);
                addToPrimary(range3);
            } else {
                // for non-EQ create 2 sets based on whatever match type was given
                Set<Range> range1 = new TreeSet<Range>();
                range1.add(createRange(range1, match, value));
                Set<Range> range2 = new TreeSet<Range>();
                range2.add(createRange(range2, match.getOpposite(), value));
                addToPrimary(range1);
                addToPrimary(range2);
            }
        } else {
            // here old != null so we'll need to modify the existing range in some way
            
            // now split the old range up based on the new value:
            // if the new match type is EQ, split the range into 3, otherwise split into 2
            Match r1Match = match;
            Match r2Match = match.getOpposite();
            Set<Range> newRanges = new TreeSet<Range>();
            if(match == Match.EQ) {
                newRanges.add(new Range(old.getContainer(), (Comparable<?>) value, match, null, null));
                r1Match = Match.LT;
                r2Match = Match.GT;
            } else if(match == Match.GT || match == Match.GTE) {
                r1Match = r2Match;
                r2Match = r1Match.getOpposite();
            }
            if(value != old.getMin() && !value.equals(old.getMin())) {
                newRanges.add(new Range(old.getContainer(), old.getMin(), old.getMinMatch(), (Comparable<?>) value, r1Match));
            }
            if(value != old.getMax() && !value.equals(old.getMax())) {
                newRanges.add(new Range(old.getContainer(), (Comparable<?>) value, r2Match, old.getMax(), old.getMaxMatch()));
            }
            
            /*
             * The following boolean indicates whether any of the newly created ranges can go
             * back into the old container. If the old container's size was 1, then we remove
             * the old container and put no new ones back in. If the old container's size was
             * greater than 1, then we might re-use it (see below). However, whether or not
             * we re-use the old container for *newly created* ranges, we still want to keep
             * the old container around when it had more than one range in it (ie: more ranges
             * than just the one we've modified)
             */
            boolean reuseOldContainer = false;
            if(old.getContainer().size() == 1) {
                if(!removeFromPrimary(old.getContainer())) {
                    remainderModified = true;
                    removeFromRemainder(old.getContainer());
                }
            } else {
                for(Range r : newRanges) {
                    if(!(r.getMinMatch() == old.getMinMatch() && r.getMin() == old.getMin() && r.getMin() != null ||
                        r.getMaxMatch() == old.getMaxMatch() && r.getMax() == old.getMax() && r.getMax() != null)) {
                        /* re-use the old container if:
                         * The old container contained multiple ranges (ie: we're only pulling
                         * one of the ranges out of it, and so we might want to put a new smaller
                         * range back in)
                         * AND
                         * there is at least one new range that would end up outside the old
                         * container (if all the new ranges end up in the old container then
                         * the new split doesn't do any good)
                         */
                        reuseOldContainer = true;
                    }
                }
                old.getContainer().remove(old);
            }
            for(Range r : newRanges) {
                if(reuseOldContainer && (
                        r.getMinMatch() == old.getMinMatch() && r.getMin() == old.getMin() && r.getMin() != null ||
                        r.getMaxMatch() == old.getMaxMatch() && r.getMax() == old.getMax() && r.getMax() != null)) {
                    r.setContainer(old.getContainer());
                    old.getContainer().add(r);
                } else {
                    Set<Range> newSet = new TreeSet<Range>();
                    newSet.add(r);
                    r.setContainer(newSet);
                    addToPrimary(newSet);
                }
            }
        }
        int event = RangeSetListener.EVENT_PRIMARY_SET_CHANGED;
        if(remainderModified) {
            event |= RangeSetListener.EVENT_REMAINDER_CHANGED;
        }
        fireEvent(event);
    }
    
    public void moveToRemainder(Collection<Set<Range>> rangeSets) {
        primaryRangeSets.removeAll(rangeSets);
        remainderRangeSets.addAll(rangeSets);
        fireEvent(RangeSetListener.EVENT_PRIMARY_SET_CHANGED |
                RangeSetListener.EVENT_REMAINDER_CHANGED);
    }
    
    public void moveFromRemainder(Collection<Set<Range>> rangeSets) {
        remainderRangeSets.removeAll(rangeSets);
        primaryRangeSets.addAll(rangeSets);
        fireEvent(RangeSetListener.EVENT_PRIMARY_SET_CHANGED |
                RangeSetListener.EVENT_REMAINDER_CHANGED);
    }
    
    protected void addToPrimary(Set<Range> rangeSet) {
        primaryRangeSets.add(rangeSet);
        allRangeSets.add(rangeSet);
    }
    
    protected void addToRemainder(Set<Range> rangeSet) {
        remainderRangeSets.add(rangeSet);
        allRangeSets.add(rangeSet);
    }
    
    protected boolean removeFromPrimary(Set<Range> rangeSet) {
        if(primaryRangeSets.remove(rangeSet)) {
            return allRangeSets.remove(rangeSet);
        }
        return false;
    }
    
    protected boolean removeFromRemainder(Set<Range> rangeSet) {
        if(remainderRangeSets.remove(rangeSet)) {
            return allRangeSets.remove(rangeSet);
        }
        return false;
    }
    
    /**
     * Combines the given rangeSets into a single rangeSet.
     * The ranges in the resulting rangeSet are merged.
     * @see #merge(Set)
     * Also merges the resulting adjacent ranges such as
     * [10,20) and [20,30) into [10,30) in the resulting rangeSet.
     */
    public void combine(Set<Range> ranges) {
        // first remove all selected rangeSets as a single rangeSet
//        Set<Range> ranges = removeSelectedRanges();
//        getRangeSets().removeAll(ranges);
        // then merge the ranges in the rangeSet
        ranges = merge(ranges);
        // if the rangeSet is not empty, add it back in
        if(ranges.size() > 0) {
            addToPrimary(ranges);
//            this.primaryRangeSets.add(ranges);
        }
        fireEvent(RangeSetListener.EVENT_PRIMARY_SET_CHANGED);
    }
    
    /**
     * Merges the adjacent ranges in the given rangeSet. For example
     * [10,20) and [20,30) will be merged into [10,30).
     */
    protected Set<Range> merge(Set<Range> ranges) {
        if(ranges.size() > 0) {
            Set<Range> newRanges = new TreeSet<Range>();
            Iterator<Range> iter = ranges.iterator();
            Range r = iter.next();
            while(iter.hasNext()) {
                Range next = iter.next();
                if(canMerge(r, next)) {
                    r = merge(r, next);
                } else {
                    newRanges.add(r);
                    r.setContainer(newRanges);
                    r = next;
                }
            }
            newRanges.add(r);
            r.setContainer(newRanges);
            for(Range range : newRanges.toArray(new Range[newRanges.size()])) {
                if(range.getMinMatch() == null && range.getMaxMatch() == null) {
                    // remove ranges with no bounds on either side
                    newRanges.remove(range);
                }
            }
            return newRanges;
        }
        return ranges;
    }
    
    /**
     * @return true if calling {@link #merge(Range, Range)} on the given ranges will
     *         return a valid merged range.
     */
    @SuppressWarnings("unchecked")
    protected boolean canMerge(Range r1, Range r2) {
        if(r1 instanceof NullRange || r2 instanceof NullRange) {
            return false;
        }
        if(r1.getMinMatch() == Match.NEQ || r2.getMinMatch() == Match.NEQ) {
            // if either is a not-equal match then we can't merge
            return false;
        }
        if(r1.getMinMatch() == Match.EQ && r2.getMinMatch() == Match.EQ) {
            // if they're both equality matches then we can't merge
            return false;
        } else if(r1.getMinMatch() == Match.EQ && r1.getMin() != null && r2.isBoundary(r1.getMin()) ||
                r2.getMinMatch() == Match.EQ && r2.getMin() != null && r1.isBoundary(r2.getMin())) {
            // if one is an equality match and the other contains that value as a boundary, we can merge
            return true;
        }
        if(r1.getMax() != null && r2.contains(r1.getMax()) ||
                r2.getMin() != null && r1.contains(r2.getMin())) {
            // if an exclusive boundary of one is contained in the other, we can merge
            return true;
        }
//        if(r1.max != null && r2.compareToRange(r1.max) == 0 ||
//                r2.min != null && r1.compareToRange(r2.min) == 0) {
//            return true;
//        }
        return false;
    }
    
    /**
     * @return a new range that is the merger of the two given ranges.
     *         If {@link #canMerge(Range, Range)} does not return true
     *         for the given ranges the result is unspecified.
     */
    protected Range merge(Range r1, Range r2) {
        if(r1.getMinMatch() == Match.EQ) {
            return new Range(r1.getContainer(), r1.getMin(), Match.GTE, r2.getMax(), r2.getMaxMatch());
        } else if(r2.getMinMatch() == Match.EQ) {
            return new Range(r1.getContainer(), r1.getMin(), r1.getMinMatch(), r2.getMin(), Match.LTE);
        } else {
            return new Range(r1.getContainer(), r1.getMin(), r1.getMinMatch(), r2.getMax(), r2.getMaxMatch());
        }
    }
    
    /**
     * Separates all of the given rangeSets that include multiple ranges into
     * multiple rangeSets that each include only one range.
     */
    public void separate(Set<Range> ranges) {
        // first remove all selected rangeSets as a single rangeSet
//        Set<Range> ranges = removeSelectedRanges();
//        getRangeSets().removeAll(ranges);
        // then re-add all the ranges as individual rangeSets
        for(Range r : ranges) {
            Set<Range> rset = new TreeSet<Range>();
            rset.add(r);
            r.setContainer(rset);
            addToPrimary(rset);
//            this.primaryRangeSets.add(rset);
        }
        fireEvent(RangeSetListener.EVENT_PRIMARY_SET_CHANGED);
    }
    
    public void populate(java.util.List<Object> values) {
        // TODO what happens when a populate is done after some values are already there?
        for(Object value : values) {
            Set<Range> rset = new TreeSet<Range>();
            rset.add(new Range(rset, (Comparable<?>) value, Match.EQ, null, null));
            addToPrimary(rset);
//            primaryRangeSets.add(rset);
        }
        fireEvent(RangeSetListener.EVENT_PRIMARY_SET_CHANGED);
    }
    
    /** Creates a range bounded on one side by the specified value. */
    protected Range createRange(Set<Range> container, Match match, Object value) {
        if(value == null) {
            return new NullRange(container, match);
        }
        if(match == Match.LT || match == Match.LTE) {
            return new Range(container, null, null, (Comparable<?>) value, match);
        }
        return new Range(container, (Comparable<?>) value, match, null, null);
    }
    
    public static interface RangeSetListener {
        public static final int EVENT_CLEAR_SETS = 1;
        public static final int EVENT_PRIMARY_SET_CHANGED = 2;
        public static final int EVENT_REMAINDER_CHANGED = 4;
        public static final int EVENT_SPLIT_TYPE_CHANGED = 8;
        public static final int EVENT_COLUMN_CHANGED = 16;
        void rangeSetChanged(int event, Object oldValue, Object newValue);
    }
    
    public static class RangeSetComparator implements Comparator<Set<Range>> {
        public int compare(Set<Range> set1, Set<Range> set2) {
            if(set1 == set2) return 0;
            if(set1.size() == 0 && set2.size() == 0) {
                return 0;
            } else if(set1.size() == 0) {
                return 1;
            } else if(set2.size() == 0) {
                return -1;
            } else {
                return set1.iterator().next().compareTo(set2.iterator().next());
            }
        }
    }
    
    public static class ConditionLabelProvider implements ILabelProvider {
        private ObjectConverter converter;
        
        public ConditionLabelProvider() {
            converter = ObjectConverter.getInstance();
        }
        
        public Image getImage(Object element) {
            return null;
        }
        public String getText(Object element) {
            StringBuilder value = new StringBuilder();
            if(element instanceof Set) {
                @SuppressWarnings("unchecked")
                Set<Range> set = (Set<Range>) element;
                for(Range range : set) {
                    if(value.length() > Short.MAX_VALUE * 2) {
                        break;
                    }
                    StringBuilder buffer = new StringBuilder();
                    if(range.getMinMatch() != null) {
                        if(range.getMin() == null) {
                            buffer.append("is");
                            if(range.getMinMatch() == Match.NEQ) {
                                buffer.append(" not");
                            }
                            buffer.append(" null");
                        } else {
                            buffer.append(range.getMinMatch().getDisplayString());
                            buffer.append(converter.formatForDisplay(range.getMin()));
                        }
                    }
                    if(range.getMaxMatch() != null) {
                        if(buffer.length() > 0) {
                            buffer.append(" and ");
                        }
                        buffer.append(range.getMaxMatch().getDisplayString());
                        buffer.append(converter.formatForDisplay(range.getMax()));
                    }
                    if(range.getMinMatch() != null && range.getMaxMatch() != null && set.size() > 1) {
                        buffer.insert(0, "(").append(")");
                    }
                    if(value.length() > 0) {
                        value.append(" or ");
                    }
                    value.append(buffer.toString());
                }
            }
            return value.substring(0, Math.min(value.length(), Short.MAX_VALUE * 2));
        }
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }
        
        public void addListener(ILabelProviderListener listener) {
        }
        public void removeListener(ILabelProviderListener listener) {
        }
        public void dispose() {
        }
    }
    
    public static class ConditionContentProvider implements IStructuredContentProvider {
        private Object[] empty = new Object[0];
        private boolean all;
        private boolean remainder;
        
        public ConditionContentProvider() {
            all = true;
        }
        
        public ConditionContentProvider(boolean remainder) {
            this.remainder = remainder;
        }
        
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
        
        public Object[] getElements(Object inputElement) {
            if(inputElement instanceof RangeSetUtil) {
                if(all) {
                    return ((RangeSetUtil) inputElement).allRangeSets.toArray();
                } else if(remainder) {
                    return ((RangeSetUtil) inputElement).remainderRangeSets.toArray();
                } else {
                    return ((RangeSetUtil) inputElement).primaryRangeSets.toArray();
                }
            }
            return empty;
        }
        
        public void dispose() {
        }
    }
}
