package com.nexusbpm.dataselector.drools.actions;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.dialogs.SaveAsDialog;

import com.nexusbpm.dataselector.actions.AbstractExtensionAction;
import com.nexusbpm.dataselector.controller.AbstractController;
import com.nexusbpm.dataselector.drools.model.PropertyList;
import com.nexusbpm.dataselector.drools.model.PropertyMap;
import com.nexusbpm.dataselector.drools.util.DRLGenerator;
import com.nexusbpm.dataselector.model.AbstractModelElement;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSTree;

public class SaveDroolsAction extends AbstractExtensionAction {
    
    public SaveDroolsAction() {
        super("Save Drools File", IAction.AS_PUSH_BUTTON);
    }
    
    @Override
    public boolean canPerform() {
        LSTree tree = getTree();
        return tree != null && PropertyList.get(tree, false) != null && hasDrools(tree.getRoot());
    }
    
    protected LSTree getTree() {
        LSTree tree = null;
        ISelection selection = getSelectionProvider().getSelection();
        if(selection instanceof IStructuredSelection) {
            Object[] sel = ((IStructuredSelection) selection).toArray();
            for(Object o : sel) {
                if(o instanceof AbstractController) {
                    o = ((AbstractController<?>) o).getModel();
                }
                if(o instanceof LSTree) {
                    tree = (LSTree) o;
                    break;
                } else if(o instanceof AbstractModelElement) {
                    tree = ((AbstractModelElement) o).getTree();
                    if(tree != null) {
                        break;
                    }
                }
            }
        }
        return tree;
    }
    
    protected boolean hasDrools(LSNode node) {
        if(node != null) {
            if(PropertyMap.get(node, false) != null) {
                return true;
            }
//            if(DroolsElement.getDroolsElement(node, false) != null) {
//                return true;
//            }
            for(LSNode child : node.getSubNodes()) {
                if(hasDrools(child)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public void performRun() {
        try {
            SaveAsDialog d = new SaveAsDialog(getShellProvider().getShell());
            if(d.open() == Window.OK) {
                IPath p = d.getResult();
                if(p.getFileExtension() == null || p.getFileExtension().equals("")) {
                    p = p.addFileExtension("drl");
                }
                ContainerGenerator gen = new ContainerGenerator(p.removeLastSegments(1));
                IContainer container = gen.generateContainer(null);
                
                DRLGenerator generator = new DRLGenerator();
                String drl = generator.generateDRL(getTree());
                
                ByteArrayInputStream stream = new ByteArrayInputStream(drl.getBytes());
                
                IFile file = container.getFile(p.removeFirstSegments(p.segmentCount() - 1));
                if(file.exists()) {
                    file.setContents(stream, true, true, null);
                } else {
                    file.create(stream, true, null);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
//    protected String createRules() {
//        StringBuilder b = new StringBuilder();
//        
//        LSTree tree = getTree();
//        
//        PropertyList list = PropertyList.get(tree, false);
//        
//        List<LSNode> queue = new LinkedList<LSNode>();
//        queue.add(tree.getRoot());
//        
//        String newline = System.getProperty("line.separator");
//        
//        ObjectConverter converter = ObjectConverter.getInstance();
//        DataConditionGenerator gen = new DroolsConditionGenerator();
//        
//        b.append("package com.nexusbpm.drools").append(newline);
//        b.append("import com.nexusbpm.drools.ComparisonUtil;").append(newline);
//        b.append("import java.util.HashMap;").append(newline);
//        b.append("global java.util.HashMap output;").append(newline).append(newline);
//        
//        while(queue.size() > 0) {
//            LSNode node = queue.remove(0);
//            queue.addAll(node.getSubNodes());
//            
//            PropertyMap map = PropertyMap.get(node, false);
//            
//            if(map != null) {
//                int index = tree.getNodeIndex(node);
//                b.append("rule \"node").append(index).append("\"").append(newline);
//                
//                // give each rule the same group and a different salience so that only
//                // the rule for the most specific segment will fire
//                b.append("\tactivation-group \"data-selector-group\"").append(newline);
//                b.append("\tsalience ").append(index).append(newline);
//                
//                // condition (LHS)
//                b.append("\twhen").append(newline);
//                String condition = gen.getFullConditionString(node, null);
//                if(condition.length() == 0) {
//                    b.append("\t\tinput : HashMap()").append(newline);
//                } else {
//                    b.append("\t\tinput : HashMap(eval(").append(newline);
//                    b.append("\t\t\t").append(gen.getFullConditionString(node, null)).append(newline);
//                    b.append("\t\t\t))").append(newline);
//                }
//                
//                // consequence (RHS)
//                b.append("\tthen").append(newline);
//                for(String name : list.getValues()) {
//                    b.append("\t\toutput.put(\"").append(name).append("\", ");
//                    String value = map.getValues().get(name);
//                    if(value == null) {
//                        b.append("null");
//                    } else {
//                        // TODO value is already a string... we need to take care of stuff like:
//                        // Welcome to ${groupName}, ${memberName}!
//                        // (or something similar)
//                        /* the value itself is a String, and the variables it references
//                         * come from the input map, so those are strings too
//                         */
//                        b.append('"').append(converter.format(value)).append('"');
//                    }
//                    b.append(");").append(newline);
//                }
//                
//                b.append("end").append(newline).append(newline);
//            }
//            
////            DroolsElement e = DroolsElement.getDroolsElement(node, false);
////            if(e != null) {
////                b.append("rule \"node").append(tree.getNodeIndex(node)).append("\"").append(newline);
////                
////                // condition (LHS)
////                b.append("\twhen").append(newline);
////                b.append("\t\tinput : HashMap(eval(").append(newline).append("\t\t\t");
////                b.append(gen.getFullConditionString(node, null));
////                b.append(newline).append("\t\t\t))").append(newline);
////                
////                // consequence (RHS)
////                b.append("\tthen").append(newline);
////                b.append(e.getText());
////                b.append(newline);
////                
////                b.append("end").append(newline).append(newline);
////            }
//        }
//        
//        return b.toString();
//    }
//    
//    protected static class DroolsConditionGenerator extends AbstractDataConditionGenerator {
//        protected static final String DATE_FORMAT = "yyyy-MM-dd";
//        protected static final String TIME_FORMAT = "HH:mm:ss.SSS";
//        protected static final String TIMESTAMP_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;
//        
//        protected SimpleDateFormat dateFormat;
//        protected SimpleDateFormat timeFormat;
//        protected SimpleDateFormat timestampFormat;
//        
//        public DroolsConditionGenerator() {
//            super(" &&\r\n\t\t\t", " ||\r\n\t\t\t");
//            dateFormat = new SimpleDateFormat(DATE_FORMAT);
//            timeFormat = new SimpleDateFormat(TIME_FORMAT);
//            timestampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
//        }
//        public String getMatchString(Match match, LSColumn column, Object value) {
//            return "ComparisonUtil." + match.toString().toLowerCase() + "(" +
//                "input.get(\"" + column.getName() + "\"), " +
//                getColumnValueString(column, value) + ", \"" +
//                column.getJavaTypeName() + "\")";
//        }
////        public String getMatchString(Match match, LSColumn column, Object value) {
////            String columnString = "input.get(\"" + column.getName() + "\")";
////            String matchString = null;
////            String preString = "ComparisonUtil.";
////            String postString = "";
////            // TODO we'll need better handling for primitives vs objects for the comparisons
////            // TODO since we're using a map, they're all objects... we'll need to handle unwrapping :(
////            if(value == null) {
////                if(match == Match.EQ) {
////                    matchString = " == ";
////                } else /*if(match == Match.NEQ)*/ {
////                    matchString = " != ";
////                }
////            } else if(match == Match.EQ) {
////                matchString = ".equals(";
////                postString = ")";
////            } else if(match == Match.NEQ) {
////                preString = "!(";
////                matchString = ".equals(";
////                postString = "))";
////            } else {
////                matchString = " " + match.getDisplayString() + " ";
////            }
////            String valueString = getColumnValueString(column, value);
////            
////            return preString + columnString + matchString + valueString + postString;
////        }
//        public String getColumnValueString(LSColumn column, Object value) {
//            if(value instanceof BigDecimal) {
//                value = ((BigDecimal) value).toPlainString();
//            } else if(value == null) {
//                return "null";
//            }
//            String string;
//            if(column.getJavaTypeName().equals(String.class.getName()) ||
//                    column.getJavaTypeName().equals(Character.class.getName())) {
//                string = value.toString();
//            } else if(column.getJavaTypeName().equals(java.sql.Timestamp.class.getName())) {
//                string = toDateTimeString(timestampFormat, value);
//            } else if(column.getJavaTypeName().equals(java.sql.Time.class.getName())) {
//                string = toDateTimeString(timeFormat, value);
//            } else if(column.getJavaTypeName().equals(java.sql.Date.class.getName())) {
//                string = toDateTimeString(dateFormat, value);
//            } else if(column.getJavaTypeName().equals(java.util.Date.class.getName())) {
//                string = toDateTimeString(timestampFormat, value);
//            } else {
//                string = value.toString();
//            }
//            return "\"" + string + "\"";
//        }
//        protected String toDateTimeString(SimpleDateFormat format, Object value) {
//            if(value instanceof java.util.Date) {
//                return format.format((java.util.Date) value);
//            } else {
//                return value.toString();
//            }
//        }
//        @Override
//        protected String getNegatedCondition(String condition) {
//            return "!(" + condition + ")";
//        }
//    }
}
