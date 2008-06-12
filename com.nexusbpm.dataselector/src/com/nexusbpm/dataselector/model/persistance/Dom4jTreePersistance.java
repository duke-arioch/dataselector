package com.nexusbpm.dataselector.model.persistance;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.eclipse.core.runtime.CoreException;

import com.nexusbpm.dataselector.model.AbstractModelElement;
import com.nexusbpm.dataselector.model.LSColumnStats;
import com.nexusbpm.dataselector.model.LSCondition;
import com.nexusbpm.dataselector.model.LSNode;
import com.nexusbpm.dataselector.model.LSNodeConnector;
import com.nexusbpm.dataselector.model.LSSplit;
import com.nexusbpm.dataselector.model.LSStats;
import com.nexusbpm.dataselector.model.LSTree;
import com.nexusbpm.dataselector.model.LSWhere;
import com.nexusbpm.dataselector.model.LSTree.State;
import com.nexusbpm.dataselector.model.config.LSColumn;
import com.nexusbpm.dataselector.model.config.LSConfig;
import com.nexusbpm.dataselector.model.config.LSConnection;
import com.nexusbpm.dataselector.model.config.LSDriver;
import com.nexusbpm.dataselector.util.ObjectConverter;

public class Dom4jTreePersistance extends AbstractTreePersistance {
    private ObjectConverter converter;
    
    public Dom4jTreePersistance() {
        converter = new ObjectConverter();
    }
    
    public LSTree parseTree(InputStream stream) throws IOException {
        BufferedInputStream is = new BufferedInputStream(stream);
        try {
            is.mark(16);
            if(is.read() == -1) {
                // an empty file, so create a new tree
                return getNewTree();
            }
            is.reset();
            SAXReader sax = new SAXReader();
            Document document = sax.read(is);
            return parseTree(document.getRootElement());
        } catch(DocumentException e) {
            IOException ex = new IOException("Could not parse input tree");
            ex.initCause(e);
            throw ex;
        } finally {
            try {
                is.close();
            } catch(Exception e) {
                // ignore
            }
        }
    }
    
    public void writeTree(OutputStream stream, LSTree tree) throws IOException {
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = new XMLWriter(stream, format);
        
        Document document = DocumentHelper.createDocument();
        
        writeTree(document, tree);
        
        writer.write(document);
        writer.close();
    }
    
    protected void parseExtensions(AbstractModelElement model, Element element) throws IOException {
        try {
            for(ModelExtensionPersistance persistance : getExtensionPersistance().values()) {
                persistance.parseExtensions(model, element);
            }
        } catch(CoreException e) {
            IOException ex = new IOException("Error parsing extension information");
            ex.initCause(e);
            throw ex;
        }
    }
    
    protected void writeExtensions(AbstractModelElement model, Element element) throws IOException {
        try {
            for(ModelExtensionPersistance persistance : getExtensionPersistance().values()) {
                persistance.writeExtensions(model, element);
            }
        } catch(CoreException e) {
            IOException ex = new IOException("Error writing extension information");
            ex.initCause(e);
            throw ex;
        }
    }
    
    protected LSTree parseTree(Element element) throws IOException {
        checkNode(element, "tree");
        LSTree tree = new LSTree();
        tree.setName(element.attributeValue("name"));
        if(element.attributeValue("state") != null) {
            tree.setState(State.valueOf(element.attributeValue("state")));
        } else {
            tree.setState(State.CONFIG_DATASOURCE);
        }
        Element config = element.element("config");
        parseConfig(tree, config);
        Element node = element.element("node");
        if(node != null) {
            parseNode(tree, node);
        }
        parseExtensions(tree, element);
        return tree;
    }
    
    protected void writeTree(Document document, LSTree tree) throws IOException {
        Element element = document.addElement("tree");
        element.addAttribute("name", tree.getName());
        element.addAttribute("state", tree.getState().name());
        writeConfig(element, tree.getConfig());
        writeNode(element, tree.getRoot());
        writeExtensions(tree, element);
    }
    
    protected void parseConfig(LSTree tree, Element element) throws IOException {
        LSConfig config = new LSConfig(tree);
        if(element != null) {
            checkNode(element, "config");
            config.setAutoDownloadStats(parseBooleanAttribute(element, "autoDownloadStats"));
            config.setAutoDownloadCategoricalSplits(
                    parseBooleanAttribute(element, "autoDownloadCategoricalSplits"));
            Element driver = element.element("driver");
            if(driver != null) {
                parseDriver(config, driver);
            }
            Element connection = element.element("connection");
            if(connection != null) {
                parseConnection(config, connection);
            }
            Element query = element.element("query");
            if(query != null) {
                parseQuery(config, query);
            }
            Element target = element.element("target");
            if(target != null) {
                parseTarget(config, target);
            }
            for(Object o : element.elements("column")) {
                parseColumn(config, (Element) o);
            }
        }
        if(config.getDriver() == null) {
            config.setDriver(new LSDriver(config));
        }
        if(config.getConnection() == null) {
            config.setConnection(new LSConnection(config));
        }
        tree.setConfig(config);
        parseExtensions(config, element);
    }
    
    protected void writeConfig(Element parent, LSConfig config) throws IOException {
        if(config == null) return;
        Element element = parent.addElement("config");
        if(config.isAutoDownloadStats()) {
            element.addAttribute("autoDownloadStats", String.valueOf(Boolean.TRUE));
        }
        if(config.isAutoDownloadCategoricalSplits()) {
            element.addAttribute("autoDownloadCategoricalSplits", String.valueOf(Boolean.TRUE));
        }
        writeDriver(element, config.getDriver());
        writeConnection(element, config.getConnection());
        writeQuery(element, config.getQuery());
        writeTarget(element, config.getTargetColumn());
        for(LSColumn column : config.getColumns()) {
            writeColumn(element, column);
        }
        writeExtensions(config, element);
    }
    
    protected void parseDriver(LSConfig config, Element element) throws IOException {
        checkNode(element, "driver");
        LSDriver driver = new LSDriver(config);
        driver.setName(element.attributeValue("name"));
        driver.setDriverClass(element.attributeValue("class"));
        config.setDriver(driver);
        parseExtensions(driver, element);
    }
    
    protected void writeDriver(Element parent, LSDriver driver) throws IOException {
        if(driver == null) return;
        Element element = parent.addElement("driver");
        element.addAttribute("name", driver.getName());
        element.addAttribute("class", driver.getDriverClass());
        writeExtensions(driver, element);
    }
    
    protected void parseConnection(LSConfig config, Element element) throws IOException {
        checkNode(element, "connection");
        LSConnection connection = new LSConnection(config);
        connection.setURI(element.attributeValue("uri"));
        connection.setUsername(element.attributeValue("username"));
        connection.setPassword(element.attributeValue("password"));
        connection.setSavePassword(parseBooleanAttribute(element, "savePassword"));
        config.setConnection(connection);
        parseExtensions(connection, element);
    }
    
    protected void writeConnection(Element parent, LSConnection connection) throws IOException {
        if(connection == null) return;
        Element element = parent.addElement("connection");
        element.addAttribute("uri", connection.getURI());
        element.addAttribute("username", connection.getUsername());
        if(connection.isSavePassword()) {
            element.addAttribute("password", connection.getPassword());
            element.addAttribute("savePassword", Boolean.toString(connection.isSavePassword()));
        }
        writeExtensions(connection, element);
    }
    
    protected void parseQuery(LSConfig config, Element element) {
        checkNode(element, "query");
        config.setQuery(element.getText());
    }
    
    protected void writeQuery(Element parent, String query) {
        if(query == null) query = "";
        Element element = parent.addElement("query");
        element.addCDATA(query);
    }
    
    protected void parseTarget(LSConfig config, Element element) {
        checkNode(element, "target");
        config.setTargetColumn(element.attributeValue("column"));
    }
    
    protected void writeTarget(Element parent, String targetColumn) {
        if(targetColumn == null) return;
        Element element = parent.addElement("target");
        element.addAttribute("column", targetColumn);
    }
    
    protected void parseColumn(LSConfig config, Element element) throws IOException {
        checkNode(element, "column");
        LSColumn column = new LSColumn(config);
        column.setName(element.attributeValue("name"));
        column.setOrdinal(parseIntAttribute(element, "ordinal", -1));
        column.setSQLType(parseIntAttribute(element, "sqlType", -1));
        column.setTypeName(element.attributeValue("typeName"));
        column.setJavaTypeName(element.attributeValue("javaType"));
        column.setPredictor(parseBooleanAttribute(element, "predictor"));
        config.addColumn(column);
        parseExtensions(column, element);
    }
    
    protected void writeColumn(Element parent, LSColumn column) throws IOException {
        if(column == null) return;
        Element element = parent.addElement("column");
        element.addAttribute("name", column.getName());
        element.addAttribute("ordinal", Integer.toString(column.getOrdinal()));
        element.addAttribute("sqlType", Integer.toString(column.getSQLType()));
        element.addAttribute("typeName", column.getTypeName());
        element.addAttribute("javaType", column.getJavaTypeName());
        if(column.isPredictor()) {
            element.addAttribute("predictor", Boolean.toString(column.isPredictor()));
        }
        writeExtensions(column, element);
    }
    
    protected void parseNode(AbstractModelElement parent, Element element) throws IOException {
        checkNode(element, "node");
        LSNode node = new LSNode(parent.getTree());
        node.setDefaultBounds();
        node.setName(element.attributeValue("name"));
        node.setX(parseIntAttribute(element, "x", 0));
        node.setY(parseIntAttribute(element, "y", 0));
        node.setWidth(parseIntAttribute(element, "width", node.getWidth()));
        node.setHeight(parseIntAttribute(element, "height", node.getHeight()));
        for(Object o : element.elements("condition")) {
            parseCondition(node, (Element) o);
        }
        parent.getTree().addNode(node);
        if(parent instanceof LSTree) {
            ((LSTree) parent).setRoot(node);
        }
        Element stats = element.element("stats");
        if(stats != null) {
            parseStats(node, stats);
        }
        Element split = element.element("split");
        if(split != null) {
            parseSplit(node, split);
        }
        for(Object o : element.elements("node")) {
//            LSNodeConnector c = new LSNodeConnector(node);
            parseNode(node, (Element) o);
        }
        if(parent instanceof LSNode) {
            LSNodeConnector connector = new LSNodeConnector((LSNode) parent, node);
            node.setConnector(connector);
            ((LSNode) parent).addSubNode(node);
//            ((LSNode) parent).addNodeConnector(connector);
//            connectors.add(connector);
        }
        parseExtensions(node, element);
    }
    
    protected void writeNode(Element parent, LSNode node) throws IOException {
        if(node == null) return;
        Element element = parent.addElement("node");
        element.addAttribute("name", node.getName());
        element.addAttribute("x", String.valueOf(node.getX()));
        element.addAttribute("y", String.valueOf(node.getY()));
        element.addAttribute("width", String.valueOf(node.getWidth()));
        element.addAttribute("height", String.valueOf(node.getHeight()));
        for(LSCondition condition : node.getConditions()) {
            writeCondition(element, condition);
        }
        writeStats(element, node.getStats());
        writeSplit(element, node.getSplit());
        for(LSNode child : node.getSubNodes()) {
            writeNode(element, child);
        }
        writeExtensions(node, element);
    }
    
    protected void parseCondition(LSNode node, Element element) throws IOException {
        checkNode(element, "condition");
        LSCondition condition = new LSCondition(node);
        for(Object o : element.elements("where")) {
            parseWhere(condition, (Element) o);
        }
        node.addCondition(condition);
        parseExtensions(condition, element);
    }
    
    protected void writeCondition(Element parent, LSCondition condition) throws IOException {
        if(condition == null) return;
        Element element = parent.addElement("condition");
        for(LSWhere where : condition.getWhereClauses()) {
            writeWhere(element, where);
        }
        writeExtensions(condition, element);
    }
    
    protected void parseWhere(LSCondition condition, Element element) throws IOException {
        checkNode(element, "where");
        LSWhere where = new LSWhere(condition);
        where.setMatch(LSWhere.Match.getMatch(element.attributeValue("match")));
        if(element.attribute("value") != null) {
            where.setValue(element.attributeValue("value"));
        }
        condition.addWhereClause(where);
        parseExtensions(where, element);
    }
    
    protected void writeWhere(Element parent, LSWhere where) throws IOException {
        if(where == null) return;
        Element element = parent.addElement("where");
        if(where.getValue() != null) {
            element.addAttribute("value", where.getValue().toString());
        }
        element.addAttribute("match", where.getMatch().getXMLString());
        writeExtensions(where, element);
    }
    
    protected void parseStats(LSNode node, Element element) throws IOException {
        checkNode(element, "stats");
        LSStats stats = new LSStats(node);
        stats.setRowCount(parseLongAttribute(element, "count", -1));
        for(Object o : element.elements("columnstats")) {
            parseColumnStats(stats, (Element) o);
        }
        node.setStats(stats);
        parseExtensions(stats, element);
//        return stats;
    }
    
    protected void writeStats(Element parent, LSStats stats) throws IOException {
        if(stats == null) return;
        Element element = parent.addElement("stats");
        element.addAttribute("count", String.valueOf(stats.getRowCount()));
        for(LSColumnStats column : stats.getColumnStats()) {
            writeColumnStats(element, column);
        }
        writeExtensions(stats, element);
    }
    
    protected void parseColumnStats(LSStats parent, Element element) throws IOException {
        checkNode(element, "columnstats");
        LSColumnStats stats = new LSColumnStats(parent);
        stats.setColumnOrdinal(parseIntAttribute(element, "column", -1));
        stats.setDistinctCount(parseLongAttribute(element, "distinct", -1));
        stats.setMin(decodeString(element.attributeValue("min")));
//        if(stats.getMin() == null && element.element("min") != null) {
//            stats.setMin(element.element("min").getText());
//        }
        stats.setMax(decodeString(element.attributeValue("max")));
//        if(stats.getMax() == null && element.element("max") != null) {
//            stats.setMax(element.element("max").getText());
//        }
        stats.setAverage(element.attributeValue("avg"));
        stats.setStandardDeviation(element.attributeValue("stddev"));
        stats.setSum(element.attributeValue("sum"));
        if(stats.getDistinctCount() > -1 && stats.getSum() == null) {
            stats.setSum("n/a");
        }
        List<Object> values = new ArrayList<Object>();
        LSColumn column = getColumn(parent.getTree().getConfig(), stats.getColumnOrdinal());
        if(column != null) {
            for(Object o : element.elements("columnvalue")) {
                Element e = (Element) o;
                if(parseBooleanAttribute(e, "isNull")) {
                    values.add(null);
                } else {
                    values.add(converter.parse(e.getText(), column));
                }
            }
        }
        stats.setValues(values);
        parent.addColumnStats(stats);
        parseExtensions(stats, element);
    }
    
    protected LSColumn getColumn(LSConfig config, int columnOrdinal) {
        for(LSColumn column : config.getColumns()) {
            if(column.getOrdinal() == columnOrdinal) {
                return column;
            }
        }
        return null;
    }
    
    protected void writeColumnStats(Element parent, LSColumnStats column) throws IOException {
        if(column == null) return;
        Element element = parent.addElement("columnstats");
        element.addAttribute("column", String.valueOf(column.getColumnOrdinal()));
        element.addAttribute("distinct", String.valueOf(column.getDistinctCount()));
        if(column.getMin() != null) {
//            Element min = element.addElement("min");
//            min.addCDATA(column.getMin());
            element.addAttribute("min", encodeString(column.getMin()));
        }
        if(column.getMax() != null) {
//            Element max = element.addElement("max");
//            max.addCDATA(column.getMax());
            element.addAttribute("max", encodeString(column.getMax()));
        }
        if(column.getAverage() != null) {
            element.addAttribute("avg", column.getAverage());
        }
        if(column.getStandardDeviation() != null) {
            element.addAttribute("stddev", column.getStandardDeviation());
        }
        if(column.getSum() != null) {
            element.addAttribute("sum", column.getSum());
        }
        for(Object value : column.getValues()) {
            Element e = element.addElement("columnvalue");
            if(value == null) {
                e.addAttribute("isNull", Boolean.TRUE.toString());
            } else {
                e.addCDATA(converter.format(value));
            }
        }
        writeExtensions(column, element);
    }
    
    protected void parseSplit(LSNode node, Element element) throws IOException {
        checkNode(element, "split");
        LSSplit split = new LSSplit(node);
        split.setColumn(element.attributeValue("column"));
//        split.setType(element.attributeValue("type"));
        node.setSplit(split);
        parseExtensions(split, element);
//        return split;
    }
    
    protected void writeSplit(Element parent, LSSplit split) throws IOException {
        if(split == null) return;
        Element element = parent.addElement("split");
        element.addAttribute("column", split.getColumn());
//        element.addAttribute("type", split.getType());
        writeExtensions(split, element);
    }
    
    protected int parseIntAttribute(Element element, String attribute, int defaultValue) {
        try {
            return Integer.parseInt(element.attributeValue(attribute));
        } catch(Exception e) {
            return defaultValue;
        }
    }
    
    protected long parseLongAttribute(Element element, String attribute, long defaultValue) {
        try {
            return Long.parseLong(element.attributeValue(attribute));
        } catch(Exception e) {
            return defaultValue;
        }
    }
    
    protected boolean parseBooleanAttribute(Element element, String attribute) {
        return Boolean.parseBoolean(element.attributeValue(attribute));
    }
    
    protected void checkNode(Element element, String name) {
        if(!element.getName().equals(name)) {
            throw new IllegalArgumentException("Expected: " + name + " but got " + element.getName());
        }
    }
    
    protected String encodeString(String str) {
        if(str == null) {
            return null;
        }
        StringBuilder b = new StringBuilder();
        for(int index = 0; index < str.length(); index++) {
            char c = str.charAt(index);
            if(c >= 32 && c < '%' ||
                    c > '%' && c <= '9' ||
                    c >= 64 && c <= 126) {
                b.append(c);
            } else {
                b.append('%');
                b.append((int) c);
                b.append(',');
            }
        }
        return b.toString();
    }
    
    protected String decodeString(String str) {
        if(str == null) {
            return null;
        }
        StringBuilder b = new StringBuilder();
        for(int index = 0; index < str.length(); index++) {
            char c = str.charAt(index);
            if(c != '%') {
                b.append(c);
            } else {
                int index2 = str.indexOf(',', index);
                String s2 = str.substring(index + 1, index2);
                b.append((char) Integer.parseInt(s2));
                index = index2;
            }
        }
        return b.toString();
    }
    
//    public static void main(String[] args) {
//        Dom4jTreePersistance p = new Dom4jTreePersistance();
//        for(int i = 0; i < 200; i++) {
//            System.out.println(
//                    i + ":\t" +
//                    ((char)i) + "\t" +
//                    p.encodeString(String.valueOf((char) i)) + "\t" +
//                    p.decodeString(p.encodeString(String.valueOf((char) i))));
//        }
//    }
}
