/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.util;

import org.gauss.common.DMLSQL;
import org.gauss.jsonstruct.DMLValueStruct;
import org.gauss.jsonstruct.FieldStruct;
import org.gauss.jsonstruct.KeyStruct;
import org.gauss.parser.Parser;
import org.gauss.parser.ParserContainer;
import org.gauss.util.ddl.DDLCacheController;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class DMLProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DMLProcessor.class);

    private DDLCacheController ddlCacheController = DDLCacheController.getInstance();
    // executor execute SQL
    private final JDBCExecutor executor;

    private QuoteCharacter quoteCharacter = QuoteCharacter.DOUBLE_QUOTE;
    private String tableIdentity;
    private String table;
    private String insertSQL = null;
    private String deleteSQL = null;
    private String truncateSQL = null;
    private String truncateCascadeSQL = null;

    private int insertCount = 0;
    private int updateCount = 0;
    private int deleteCount = 0;

    // Stores information of all column
    private final List<ColumnInfo> columnInfos = new ArrayList<>();

    // Store information of key column (primary key)
    private final List<ColumnInfo> keyColumnInfos = new ArrayList<>();

    public DMLProcessor(String table, JDBCExecutor executor) {
        this.table = table;
        this.executor = executor;
    }

    public void process(KeyStruct key, DMLValueStruct value) {
        String op = value.getPayload().getOp();
        // using custom operation enum,fix using debezium-core dependency from github
        Operation operation = Operation.forCode(op);
        long currentScn;
        String commit_scn = value.getPayload().getSource().getCommit_scn();
        Long scn = value.getPayload().getSource().getScn();
        if(commit_scn !=null && scn !=null) {
            currentScn = Math.min(scn,Long.parseLong(commit_scn));
        } else {
            currentScn = Long.parseLong(commit_scn != null ? commit_scn : scn.toString());
        }
        LOGGER.info("currentScn: {}", currentScn);
        List<String> cacheDDlByScn = ddlCacheController.getCacheDDlByScn(currentScn);
        if (cacheDDlByScn.size() > 0) {
            // find ddl need to execute before
            LOGGER.info("there is {} cached ddl need execute before", cacheDDlByScn.size());
            ddlCacheController.consumeDDL(cacheDDlByScn);
        }
        // We assume that table struct don't change. This assumption may be changed
        // in the future.
        initColumnInfos(value);
        initKeyColumnInfos(key);
        initTableIdentity(value);

        PreparedStatement statement;
        switch (operation) {
            case READ:
                LOGGER.info("This record snapshot record. Ignore it.");
                return;
            case CREATE:
                statement = getInsertStatement(value);
                insertCount++;
                LOGGER.info("Insert SQL in {}, insert count: {}.", table, insertCount);
                break;
            case UPDATE:
                statement = getUpdateStatement(key, value);
                updateCount++;
                LOGGER.info("Update SQL in {}, update count: {}.", table, updateCount);
                break;
            case DELETE:
                statement = getDeleteStatement(key, value);
                deleteCount++;
                LOGGER.info("DELETE SQL in {}, delete count: {}.", table, deleteCount);
                break;
            case TRUNCATE:
                statement = getTruncateStatement(key, value);
                LOGGER.info("TRUNCATE SQL in {}.", table);
                break;
            case TRUNCATE_CASCADE:
                statement = getTruncateCascadeStatement(key, value);
                LOGGER.info("TRUNCATE CASCADE  SQL in {}.", table);
                break;
            case NOT_SUPPORT:
            default:
                // May be truncate. Truncate operation is not used in debezium-connector-oracle.
                statement = null;
                break;
        }

        if (statement != null) {
            executor.executeDML(statement);
        }
    }

    /**
     * create truncate statement
     *
     * @param key
     * @param value
     * @return
     */
    private PreparedStatement getTruncateStatement(KeyStruct key, DMLValueStruct value) {
        if (null == truncateSQL) {
            initTruncateSQL();
        }
        try {
            return executor.getConnection().prepareStatement(truncateSQL);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private PreparedStatement getTruncateCascadeStatement(KeyStruct key, DMLValueStruct value) {
        if (null == truncateCascadeSQL) {
            initTruncateCascadeSQL();
        }
        try {
            return executor.getConnection().prepareStatement(truncateCascadeSQL);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private void initColumnInfos(DMLValueStruct value) {
        columnInfos.clear();
        String fieldName;
        if (value.getPayload().getBefore() != null) {
            fieldName = "before";
        } else {
            fieldName = "after";
        }
        FieldStruct infoField = value.getSchema().getFields().stream()
                .filter(f -> f.getField().equals(fieldName))
                .findFirst()
                .orElse(null);
        List<FieldStruct> columnFields = infoField.getFields();

        for (FieldStruct colField : columnFields) {
            ColumnInfo columnInfo = new ColumnInfo(
                    colField.getField(), colField.getType(), colField.getName(), colField.getParameters());
            columnInfos.add(columnInfo);
        }
    }

    private void initTableIdentity(DMLValueStruct value) {
        String schema = value.getPayload().getSource().getSchema();
        tableIdentity = String.format("%s.%s", getObjectNameForOpenGauss(schema), getObjectNameForOpenGauss(table));
    }

    private void initKeyColumnInfos(KeyStruct key) {
        if (key == null) {
            return;
        }
        keyColumnInfos.clear();
        List<FieldStruct> keyColumnFields = key.getSchema().getFields();
        for (FieldStruct keyColField : keyColumnFields) {
            //check if key field not in columnsInfo , we should skip it;
            for (ColumnInfo columnInfo : columnInfos) {
                if (columnInfo.getName().equals(keyColField.getField())) {
                    ColumnInfo keyColumnInfo = new ColumnInfo(keyColField.getField(),
                                                              keyColField.getType(),
                                                              keyColField.getName(),
                                                              keyColField.getParameters());
                    keyColumnInfos.add(keyColumnInfo);
                    break;
                }
            }
        }
    }

    private void initInsertSQL() {
        int n = columnInfos.size();
        String[] columnNames = new String[n];
        String[] columnValues = new String[n];
        for (int i = 0; i < n; ++i) {
            String rawColumnName = columnInfos.get(i).getName();
            columnNames[i] = getObjectNameForOpenGauss(rawColumnName);
            columnValues[i] = "?";
        }
        String columnNamesSQL = String.join(", ", columnNames);
        String columnValuesSQL = String.join(", ", columnValues);
        insertSQL = String.format(DMLSQL.INSERT_SQL, tableIdentity, columnNamesSQL, columnValuesSQL);
        LOGGER.info(insertSQL);
    }

    private void initDeleteSQL() {
        deleteSQL = String.format(DMLSQL.DELETE_SQL, tableIdentity);
    }

    private void initTruncateSQL() {
        truncateSQL = String.format(DMLSQL.TRUNCATE_SQL, tableIdentity);
    }
    private void initTruncateCascadeSQL() {
        truncateCascadeSQL = String.format(DMLSQL.TRUNCATE_CASCADE_SQL, tableIdentity);
    }

    private String getWhereClause(KeyStruct key, DMLValueStruct value,
                                  List<ColumnInfo> whereColInfos, List<Object> whereColValues) {
        // If there has primary key, we use key column in where clause.
        // Or we use all column in where clause.
        List<ColumnInfo> identifyColInfos = keyColumnInfos.size() > 0 ? keyColumnInfos : columnInfos;
        Map<String, Object> identifyColValues = value.getPayload().getBefore();

        List<String> whereSQL = new ArrayList<>();
        for (ColumnInfo columnInfo : identifyColInfos) {
            String rawName=columnInfo.getName();
            String name = getObjectNameForOpenGauss(rawName);
            Object colValue = identifyColValues.get(rawName);
            if (colValue == null) {
                // We can't set a == null in where clause so we build null string.
                whereSQL.add(name + " IS NULL");
            } else {
                String semanticType = columnInfo.getSemanticType();
                if (semanticType != null && (semanticType.equals(io.debezium.data.VariableScaleDecimal.LOGICAL_NAME) ||
                    semanticType.equals(org.apache.kafka.connect.data.Decimal.LOGICAL_NAME))) {
                    // Debezium maps DOUBLE PRECISION, FLOAT[(P)], NUMBER[(P[, *])], REAL etc data type to
                    // VariableScaleDecimal and Decimal. FLOAT and REAL are not precise data type in openGauss.
                    // When FLOAT or REAL attributes appears in where clause, we should use SQL like
                    // "select * from xxx where a::numeric = 1.53" to compare.
                    // https://debezium.io/documentation/reference/1.5/connectors/oracle.html#oracle-numeric-types
                    whereSQL.add(name + "::numeric = ?");
                } else {
                    whereSQL.add(name + " = ?");
                }
                whereColInfos.add(columnInfo);
                whereColValues.add(colValue);
            }
        }

        return String.join(" and ", whereSQL);
    }

    /**
     * convert oracle Object to openGauss Object
     * example: OBJECT_A convert to object_a
     *          Object_A convert to Object_A
     * @param rawColumnName
     * @return
     */
    public String getObjectNameForOpenGauss(String rawColumnName) {
        rawColumnName = quoteCharacter.wrap(ObjectNameConvertUtil.getObjectNameForOpenGauss(rawColumnName));
        return rawColumnName;
    }

    private PreparedStatement getInsertStatement(DMLValueStruct value) {
        initInsertSQL();
        List<Object> columnValues = new ArrayList<>();
        Map<String, Object> insertValues = value.getPayload().getAfter();
        for (ColumnInfo columnInfo : columnInfos) {
            String columnName = columnInfo.getName();
            columnValues.add(convertColumnValues(columnInfo.getSemanticType(), insertValues.get(columnName)));
        }

        return getStatement(insertSQL, columnInfos, columnValues);
    }

    private PreparedStatement getUpdateStatement(KeyStruct key, DMLValueStruct value) {
        // Get new value after updated.
        List<ColumnInfo> columnInSQL = new ArrayList<>();
        List<Object> valueInSQL = new ArrayList<>();
        Map<String, Object> afterValues = value.getPayload().getAfter();
        Map<String, Object> beforeValues = value.getPayload().getBefore();
        ArrayList<String> columnNameValues = new ArrayList<>();
        for (ColumnInfo colInfo : columnInfos) {
            String colName = colInfo.getName();
            Object afterVal = afterValues.get(colName);
            Object beforeVal = beforeValues.get(colName);
            if (!Objects.equals(afterVal, beforeVal)) {
                columnNameValues.add(getObjectNameForOpenGauss(colName) + " = ?");
                columnInSQL.add(colInfo);
                valueInSQL.add(convertColumnValues(colInfo.getSemanticType(), afterValues.get(colName)));
            }
        }
        String nameValues = String.join(", ", columnNameValues);
        String sql = String.format(DMLSQL.UPDATE_SQL, tableIdentity, nameValues);

        List<ColumnInfo> whereColInfos = new ArrayList<>();
        List<Object> whereColValues = new ArrayList<>();
        String whereClause = getWhereClause(key, value, whereColInfos, whereColValues);
        String completeUpdateSQL = sql + whereClause;
        LOGGER.info("UPDATE SQL:{}",completeUpdateSQL);
        columnInSQL.addAll(whereColInfos);
        valueInSQL.addAll(whereColValues);

        return getStatement(completeUpdateSQL, columnInSQL, valueInSQL);
    }

    private Object convertColumnValues(String semanticType, Object value) {
        if (value == null){
            return null;
        }
        if ("LONG RAW".equals((semanticType))) {
            return "\\x" + value;
        }
        else if ("UDT".equalsIgnoreCase(semanticType)) {
            String xml = trimXml((String) value);
            JSONObject jsonObject = XML.toJSONObject(xml);
            jsonObject = new JSONObject(trimJson(jsonObject));

            Map<String, Object> map = jsonObject.toMap();
            if (map.size() != 1) {
                throw new RuntimeException("Unexpected XML content: multiple roots.");
            }
            Entry<String, Object> p = map.entrySet().iterator().next();
            if ("".equals(p.getValue())) {
                return "{}";
            }
            return new JSONObject((Map<String, Object>)p.getValue()).toString();
        }
        else if ("Array".equalsIgnoreCase(semanticType)) {
            String xml = trimXml((String) value);

            JSONObject jsonObject = XML.toJSONObject(xml);
            jsonObject = new JSONObject(trimJson(jsonObject));

            Set<String> keySet = jsonObject.keySet();
            if (keySet.size() != 1) {
                throw new RuntimeException("Unexpected XML content: multiple roots.");
            }
            String key = keySet.iterator().next();

            Object o = jsonObject.get(key);
            if (o instanceof JSONArray) {
                return ((JSONArray) o).toString();
            } else if (o instanceof JSONObject) {
                jsonObject = (JSONObject) o;
                keySet = jsonObject.keySet();
                if (keySet.size() != 1) {
                    throw new RuntimeException("Unexpected XML content: multiple roots of an object.");
                }
                key = keySet.iterator().next();
                o = jsonObject.get(key);
                JSONArray jA = null;
                if (o instanceof JSONArray) {
                    jA = (JSONArray) o;
                } else if ("".equals(o)) {
                    return "[{}]";
                } else {
                    jA = new JSONArray();
                    jA.put(o);
                }
                return jA.toString();
            } else {
                if (!"".equals(o)) {
                    throw new RuntimeException("Unexpected XML content: unrecognized object type: "
                        + o.getClass() + ", expecting JSONObject, JSONArray or empty String.");
                }
                return "[]";
            }
        }
        else {
            return value;
        }
    }

    private static void trimArray(Set<Wrap> toBeTrimmed, Wrap parent, Wrap me) {
        if (me.obj instanceof Map<?, ?>) {
            Map<String, Wrap> map = (Map<String, Wrap>) me.obj;
            for (Entry<String, Wrap> entry : map.entrySet()) {
                trimArray(toBeTrimmed, me, entry.getValue());
            }
        } else if (me.obj instanceof List<?>) {
            List<Wrap> list = (List<Wrap>) me.obj;
            toBeTrimmed.add(parent);
            for (Wrap e : list) {
                trimArray(toBeTrimmed, me, e);
            }
        }
    }

    private static String trimJson(JSONObject jsonObject) {
        Map<String, Object> map = jsonObject.toMap();
        Set<Wrap> toBeTrimmed = new HashSet<>();
        Wrap wrapped = Wrap.wrap(map);
        trimArray(toBeTrimmed, null, wrapped);
        for (Wrap w : toBeTrimmed) {
            if (!(w.obj instanceof Map<?, ?>)) {
                throw new RuntimeException("Unexpected inner object.");
            }
            Map<String, Wrap> m = (Map<String, Wrap>) w.obj;
            if (m.size() != 1) {
                throw new RuntimeException("Unexpected item.");
            }
            Wrap child = m.entrySet().iterator().next().getValue();
            if (!(child.obj instanceof List<?>)) {
                throw new RuntimeException("Unexpected inner object, should be list.");
            }
            w.obj = child;
        }
        Object unwrapped = Wrap.unwrap(wrapped);
        if (unwrapped instanceof Map<?, ?>)
            return new JSONObject((Map<String, Object>) unwrapped).toString();
        else
            throw new RuntimeException("type error");
    }

    private static String trimXml(String xml) {
        StringWriter writer = new StringWriter();
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(
                            xml.replaceAll("<\\?.*\\?>", "<?xml version='1.0'?>"))));

            XPathExpression expression = XPathFactory.newInstance().newXPath().compile("//*[@typename or @schemaname]");
            NodeList nodes = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
            if (nodes.getLength() > 0) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node n = nodes.item(i);
                    if (n instanceof Element) {
                        Element el = (Element) n;
                        el.removeAttribute("typename");
                        el.removeAttribute("schemaname");
                    }
                }
            }

            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.transform(new DOMSource(document), new StreamResult(writer));

            return writer.getBuffer().toString();
        } catch (Exception e) {
            // Trim failed
            return xml;
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private PreparedStatement getDeleteStatement(KeyStruct key, DMLValueStruct value) {
        if (deleteSQL == null) {
            initDeleteSQL();
        }

        List<ColumnInfo> whereColInfos = new ArrayList<>();
        List<Object> whereColValues = new ArrayList<>();
        String whereClause = getWhereClause(key, value, whereColInfos, whereColValues);

        String completeDeleteSQL = deleteSQL + whereClause;
        LOGGER.info("DELETE SQL: {}",completeDeleteSQL);

        return getStatement(completeDeleteSQL, whereColInfos, whereColValues);
    }

    private PreparedStatement getStatement(String preparedSQL, List<ColumnInfo> columnInfos, List<Object> columnValues) {
        PreparedStatement statement = null;
        try {
            statement = executor.getConnection().prepareStatement(preparedSQL);
            setStatement(statement, columnInfos, columnValues);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return statement;
    }

    private void setStatement(PreparedStatement statement, List<ColumnInfo> colInfos, List<Object> colValues) {
        try {
            for (int i = 0; i < colInfos.size(); ++i) {
                ColumnInfo colInfo = colInfos.get(i);
                Object colValue = colValues.get(i);
                Object realValue = parseColumnValue(colInfo, colValue);
                statement.setObject(i + 1, realValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object parseColumnValue(ColumnInfo colInfo, Object colValue) {
        if (colValue == null) {
            return null;
        }

        String semanticType = colInfo.getSemanticType();
        Parser parser = ParserContainer.parsers.get(semanticType);
        if (parser != null) {
            return parser.parse(colInfo.getParameters(), colValue);
        } else {
            return colValue;
        }
    }


    public enum Operation {
        READ("r"),
        /**
         * An operation that resulted in a new record being created in the source.
         */
        CREATE("c"),
        /**
         * An operation that resulted in an existing record being updated in the source.
         */
        UPDATE("u"),
        /**
         * An operation that resulted in an existing record being removed from or deleted in the source.
         */
        DELETE("d"),
        /**
         * An operation that resulted in an existing table being truncated in the source.
         */
        TRUNCATE("t"),
        /**
         * An operation that resulted in an existing table being truncated cascade in the source.
         */
        TRUNCATE_CASCADE("tc"),
        /**
         * An operation that resulted in a generic message
         */
        MESSAGE("m"),
        /**
         * Not support operation
         */
        NOT_SUPPORT("");

        private final String code;

        private Operation(String code) {
            this.code = code;
        }

        public static Operation forCode(String code) {
            for (Operation op : Operation.values()) {
                if (op.code().equalsIgnoreCase(code)) {
                    return op;
                }
            }
            return NOT_SUPPORT;
        }

        public String code() {
            return code;
        }
    }
}

class Wrap {

    public Object obj;

    public Wrap(Object o) {
        obj = o;
    }

    public static Wrap wrap(Object obj) {
        if (obj instanceof Map<?, ?>) {
            Map<String, ?> map = (Map<String, ?>) obj;
            Map<String, Wrap> newMap = new HashMap<>();
            for (Entry<String, ?> entry : map.entrySet()) {
                newMap.put(entry.getKey(), wrap(entry.getValue()));
            }
            return new Wrap(newMap);
        } else if (obj instanceof List<?>) {
            List<?> list = (List<?>) obj;
            List<Wrap> newList = new ArrayList<>();
            for (Object e : list) {
                newList.add(wrap(e));
            }
            return new Wrap(newList);
        } else {
            return new Wrap(obj);
        }
    }

    public static Object unwrap(Wrap wrap) {
        if (wrap.obj instanceof Map<?, ?>) {
            Map<String, Wrap> map = (Map<String, Wrap>) wrap.obj;
            Map<String, Object> newMap = new HashMap<>();
            for (Entry<String, Wrap> entry : map.entrySet()) {
                newMap.put(entry.getKey(), unwrap(entry.getValue()));
            }
            return newMap;
        } else if (wrap.obj instanceof List<?>) {
            List<Wrap> list = (List<Wrap>) wrap.obj;
            List<Object> newList = new ArrayList<>();
            for (Wrap w : list) {
                newList.add(unwrap(w));
            }
            return newList;
        } else if (wrap.obj instanceof Wrap) {
            return unwrap((Wrap) wrap.obj);
        } else {
            return wrap.obj;
        }
    }
}
