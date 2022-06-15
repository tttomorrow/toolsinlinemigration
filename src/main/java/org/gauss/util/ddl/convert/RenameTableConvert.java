package org.gauss.util.ddl.convert;

import org.apache.commons.lang3.StringUtils;
import org.gauss.jsonstruct.DDLValueStruct;

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
    public String convertToOpenGaussDDL(DDLValueStruct ddlValueStruct) {
        String ddl = ddlValueStruct.getPayload().getDdl();
        if (StringUtils.containsIgnoreCase(ddl, "RENAME TO")) {
            String tableId = ddlValueStruct.getPayload().getTableChanges().get(0).getId();
            String[] split = tableId.split("\\.");
            if (split.length == 3) {
                String newTableName = split[2];
                String schemaName = ddlValueStruct.getPayload().getSource().getSchema();
                String oldTableName = null;
                int start = StringUtils.indexOfAny(ddl, "alter table", "ALTER TABLE");
                int end = StringUtils.lastIndexOfAny(ddl, "rename to", "RENAME TO");
                oldTableName = ddl.substring(start, end).trim();
                oldTableName = oldTableName.replaceAll("\\s+", " ");
                String[] s = oldTableName.split(" ");
                for (String s1 : s) {
                    if (!s1.equalsIgnoreCase("alter") && !s1.equalsIgnoreCase("table")) {
                        if (s1.contains(".")) {
                            oldTableName = s1.split("\\.")[1];
                        } else {
                            oldTableName = s1;
                        }
                    }
                }
                return String.format("ALTER TABLE %s.%s RENAME TO %s",
                                     wrapQuote(unwrapQuote(schemaName)),
                                     wrapQuote(unwrapQuote(oldTableName)),
                                     wrapQuote(unwrapQuote(newTableName)));
            }
        }
        return null;
    }
}
