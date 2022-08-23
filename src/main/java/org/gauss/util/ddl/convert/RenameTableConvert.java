package org.gauss.util.ddl.convert;

import org.apache.commons.lang3.StringUtils;
import org.gauss.jsonstruct.DDLValueStruct;

import java.util.Collections;
import java.util.List;

/**
 * @author saxisuer
 * @Description renameTable ddl convert
 * @date 2022/2/11
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public class RenameTableConvert extends BaseConvert implements DDLConvert {
    /**
     * capture ORACLE DDL convert to OPENGAUSS DDL
     *
     * @param ddlValueStruct debezium captured rename table record
     * @return
     */
    @Override
    public List<String> convertToOpenGaussDDL(DDLValueStruct ddlValueStruct) {
        String ddl = ddlValueStruct.getPayload().getDdl();
        if (StringUtils.containsIgnoreCase(ddl, "RENAME TO")) {
            String tableId = ddlValueStruct.getPayload().getTableChanges().get(0).getId();
            String[] split = tableId.split("\\.");
            if (split.length == 3) {
                String schemaName = ddlValueStruct.getPayload().getSource().getSchema();
                String newTableName = unwrapQuote(split[2]);
                String[] tableNames = ddlValueStruct.getPayload().getSource().getTable().split(",");
                String oldTableName = "";
                for (String tableName : tableNames) {
                    if (!tableName.equals(newTableName)) {
                        oldTableName = tableName;
                    }
                }
                return Collections.singletonList(String.format("ALTER TABLE %s.%s RENAME TO %s",
                                                               wrapQuote(schemaName),
                                                               wrapQuote(oldTableName),
                                                               wrapQuote(newTableName)));
            }
        }
        return null;
    }

    @Override
    public boolean needCacheSql() {
        return true;
    }
}
