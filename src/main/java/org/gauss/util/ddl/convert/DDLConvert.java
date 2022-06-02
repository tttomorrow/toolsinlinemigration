package org.gauss.util.ddl.convert;

import org.gauss.jsonstruct.DDLValueStruct;

/**
 * @author saxisuer
 * @Description ddl convert
 * @date 2022/2/11
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public interface DDLConvert {

    /**
     * capture ORACLE DDL convert to OPENGAUSS DDL
     *
     * @param ddlValueStruct debezium captured DDL record
     * @return
     */
    String convertToOpenGaussDDL(DDLValueStruct ddlValueStruct);


}
