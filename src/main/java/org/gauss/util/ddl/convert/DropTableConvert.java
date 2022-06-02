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
            TableChangeStruct tableChangeStruct = ddlValueStruct.getPayload().getTableChanges().get(0);
            String id = tableChangeStruct.getId();
            String[] split = id.split("\\.");
            String schema = unwrapQuote(split[1]);
            String tableName = unwrapQuote(split[2]);
            return String.format("drop table %s.%s%n", wrapQuote(schema), wrapQuote(tableName));
        }
        return null;
    }
}
