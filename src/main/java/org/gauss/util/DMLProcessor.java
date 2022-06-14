/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.util;

import org.gauss.common.DMLSQL;
import org.gauss.jsonstruct.DMLValueStruct;
import org.gauss.jsonstruct.FieldStruct;
import org.gauss.parser.Parser;
import org.gauss.jsonstruct.KeyStruct;
import org.gauss.parser.ParserContainer;

import io.debezium.data.Envelope;

import org.gauss.util.ddl.DDLCacheController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Map;

public class DMLProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DMLProcessor.class);

    private DDLCacheController ddlCacheController = DDLCacheController.getInstance();
    // executor execute SQL
    private final JDBCExecutor executor;

    private QuoteCharacter quoteCharacter = QuoteCharacter.DOUBLE_QUOTE;
    private String tableIdentity;
    private String table;
    private String insertSQL = null;
    private String updateSQL = null;
    private String deleteSQL = null;
    private String truncateSQL = null;

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
        Envelope.Operation operation = Envelope.Operation.forCode(op);
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
//        if (columnInfos.size() == 0) {
            initKeyColumnInfos(key);
            initColumnInfos(value);
            initTableIdentity(value);
//        }

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
            e.printStackTrace();
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
            ColumnInfo keyColumnInfo = new ColumnInfo(
                    keyColField.getField(), keyColField.getType(), keyColField.getName(), keyColField.getParameters());
            keyColumnInfos.add(keyColumnInfo);
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

    private void initUpdateSQL() {
        int n = columnInfos.size();
        String[] columnNameValues = new String[n];
        for (int i = 0; i < n; ++i) {
            String rawColumnName = columnInfos.get(i).getName();
            columnNameValues[i] = getObjectNameForOpenGauss(rawColumnName)+ " = ?";
        }
        String nameValues = String.join(", ", columnNameValues);
        updateSQL = String.format(DMLSQL.UPDATE_SQL, tableIdentity, nameValues);
    }

    private void initDeleteSQL() {
        deleteSQL = String.format(DMLSQL.DELETE_SQL, tableIdentity);
    }

    private void initTruncateSQL() {
        truncateSQL = String.format(DMLSQL.TRUNCATE_SQL, tableIdentity);
    }

    private String getWhereClause(KeyStruct key, DMLValueStruct value,
                                  List<ColumnInfo> whereColInfos, List<Object> whereColValues) {
        // If there has primary key, we use key column in where clause.
        // Or we use all column in where clause.
        List<ColumnInfo> identifyColInfos = keyColumnInfos.size() > 0 ? keyColumnInfos : columnInfos;
        Map<String, Object> identifyColValues = keyColumnInfos.size() > 0 ?
                key.getPayload() : value.getPayload().getBefore();

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
        if (rawColumnName.toLowerCase().equals(rawColumnName) || rawColumnName.toUpperCase().equals(rawColumnName)) {
            rawColumnName = rawColumnName.toLowerCase();
        }
        rawColumnName = quoteCharacter.wrap(rawColumnName);
        return rawColumnName;
    }

    private PreparedStatement getInsertStatement(DMLValueStruct value) {
        if (insertSQL == null) {
            initInsertSQL();
        }

        List<Object> columnValues = new ArrayList<>();
        Map<String, Object> insertValues = value.getPayload().getAfter();
        for (ColumnInfo columnInfo : columnInfos) {
            String columnName = columnInfo.getName();
            columnValues.add(insertValues.get(columnName));
        }

        PreparedStatement statement = getStatement(insertSQL, columnInfos, columnValues);

        return statement;
    }

    private PreparedStatement getUpdateStatement(KeyStruct key, DMLValueStruct value) {
        if (updateSQL == null) {
            initUpdateSQL();
        }

        // Get new value after updated.
        List<ColumnInfo> columnInSQL = new ArrayList<>();
        List<Object> valueInSQL = new ArrayList<>();
        Map<String, Object> afterValues = value.getPayload().getAfter();
        for (ColumnInfo colInfo : columnInfos) {
            String colName = colInfo.getName();
            columnInSQL.add(colInfo);
            valueInSQL.add(afterValues.get(colName));
        }

        List<ColumnInfo> whereColInfos = new ArrayList<>();
        List<Object> whereColValues = new ArrayList<>();
        String whereClause = getWhereClause(key, value, whereColInfos, whereColValues);
        String completeUpdateSQL = updateSQL + whereClause;
        LOGGER.info("UPDATE SQL:{}",completeUpdateSQL);
        columnInSQL.addAll(whereColInfos);
        valueInSQL.addAll(whereColValues);
        PreparedStatement statement = getStatement(completeUpdateSQL, columnInSQL, valueInSQL);

        return statement;
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
        PreparedStatement statement = getStatement(completeDeleteSQL, whereColInfos, whereColValues);

        return statement;
    }

    private PreparedStatement getStatement(String preparedSQL, List<ColumnInfo> columnInfos, List<Object> columnValues) {
        PreparedStatement statement = null;
        try {
            statement = executor.getConnection().prepareStatement(preparedSQL);
            setStatement(statement, columnInfos, columnValues);
        } catch (Exception e) {
            e.printStackTrace();
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
}
