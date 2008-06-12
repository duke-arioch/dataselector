package com.nexusbpm.dataselector.model;


public class LSWhere extends AbstractModelElement implements Comparable<LSWhere> {
    public static final String PROPERTY_WHERE_VALUE = "whereValue";
    public static final String PROPERTY_WHERE_MATCH = "whereMatch";
    
    private Object value;
    private Match match;
    
    public LSWhere(AbstractModelElement parent) {
        super(parent);
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        Object oldValue = this.value;
        this.value = value;
        firePropertyChange(PROPERTY_WHERE_VALUE, oldValue, value);
    }
    
    public Match getMatch() {
        return match;
    }
    
    public void setMatch(Match match) {
        Match oldMatch = this.match;
        this.match = match;
        firePropertyChange(PROPERTY_WHERE_MATCH, oldMatch, match);
    }
    
    public int compareTo(LSWhere o) {
        int value = compareValues(this.value, o.value);
        if(value == 0) {
            value = this.match.ordering - o.match.ordering;
        }
        if(value == 0) {
            switch(this.match) {
                case LT:
                case GT:
                case EQ:
                    value = -1;
                    break;
                default:
                    value = 1;
            }
        }
        return value;
    }
    
    @SuppressWarnings("unchecked")
    protected int compareValues(Object value1, Object value2) {
        if(value1 == value2) {
            return 0;
        } else if(value1 == null) {
            return 1;
        } else if(value2 == null) {
            return -1;
//        } else if(value1 instanceof Number && value2 instanceof Number ||
//                value1 instanceof String && value2 instanceof String ||
//                value1 instanceof Date && value2 instanceof Date) {
//            return ((Comparable) value1).compareTo(value2);
        } else {
            try {
                return ((Comparable) value1).compareTo(value2);
            } catch(Exception e) {
                return value1.getClass().getName().compareTo(value2.getClass().getName());
            }
        }
    }
    
    public enum Match {
//        EXACT("", ""),
        LTE("<=", -1),
        LT("<", -1),
        GTE(">=", 1),
        GT(">", 1),
        EQ("=", 0),
        NEQ("<>", 0);
//        INVALID("*", "");
        
        private static Match[][] OPPOSITES = {
            {LTE, GT},
            {LT, GTE},
//            {EQ, NEQ} // TODO do we want EQ/NEQ as opposites? need to check references to getOpposite first
        };
        private String displayString;
//        private String rangeString;
        private int ordering;
        
        Match(String displayString, int ordering) {
            this.displayString = displayString;
            this.ordering = ordering;
//            this.rangeString = rangeString;
        }
        
        public String getDisplayString() {
            return displayString;
        }
        
//        public String getRangeString() {
//            return rangeString;
//        }
        
        @SuppressWarnings("unchecked")
        public boolean compare(Comparable c1, Comparable c2) {
            if(c1 == null || c2 == null) {
                // if this is EQ: true when they're both null
                // if this is NEQ: true if one isn't null
                return (this == EQ && c1 == c2) || (this == NEQ && c1 != c2);
            }
            try {
                int v = c1.compareTo(c2);
                return
                    (v != 0 && this == NEQ) ||
                    (v == 0 && (/*this == EXACT ||*/ this == LTE || this == GTE || this == EQ)) ||
                    (v < 0 && (this == LTE || this == LT)) ||
                    (v > 0 && (this == GT || this == GTE));
            } catch(ClassCastException e) {
                return false;
            } catch(NullPointerException e) {
                return false;
            }
        }
        
        public Match getOpposite() {
            for(int index = 0; index < OPPOSITES.length; index++) {
                if(OPPOSITES[index][0] == this) {
                    return OPPOSITES[index][1];
                } else if(OPPOSITES[index][1] == this) {
                    return OPPOSITES[index][0];
                }
            }
            return null;
        }
        
        public static Match getMatch(String match) {
            try {
                return Match.valueOf(match.toUpperCase());
            } catch(IllegalArgumentException e) {
                return null;
            }
        }
        
        public static Match getMatchByDisplay(String displayString) {
            for(Match m : Match.values()) {
                if(m.displayString.equals(displayString)) {
                    return m;
                }
            }
            return null;
        }
        
        public String getXMLString() {
            return name().toLowerCase();
        }
    }
}
