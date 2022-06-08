package org.gauss.util.ddl.convert;

import org.apache.commons.lang3.StringUtils;
import org.gauss.jsonstruct.DDLValueStruct;
import org.gauss.jsonstruct.TableChangeStruct;

/**
 * @author saxisuer
 * @Description
 * @date 2022/2/14
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public class DropTableConvert extends BaseConvert implements DDLConvert {
    /**
     * capture ORACLE DDL convert to OPENGAUSS DDL
     *
     * @param ddlValueStruct debezium captured drop table DDL record
     * @return
     */
    @Override
    public String convertToOpenGaussDDL(DDLValueStruct ddlValueStruct) {
        if (StringUtils.containsIgnoreCase(ddlValueStruct.getPayload().getTableChanges().get(0).getType(), "DROP")) {
            String schema = unwrapQuote(ddlValueStruct.getPayload().getSource().getSchema());
            String tableName = unwrapQuote(ddlValueStruct.getPayload().getSource().getTable());
            String ddl = String.format("drop table %s.%s ", wrapQuote(schema), wrapQuote(tableName));
            if (ddlValueStruct.getPayload().getDdl().toLowerCase().contains("cascade constraints purge")) {
                ddl += " cascade constraints purge";
            } else if (ddlValueStruct.getPayload().getDdl().toLowerCase().contains("cascade constraints")) {
                ddl += " cascade constraints ";
            } else if (ddlValueStruct.getPayload().getDdl().toLowerCase().contains(" purge")) {
                ddl += "  purge";
            }
            return ddl;
        }
        return null;
    }
}
