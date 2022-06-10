package org.gauss.util.ddl.convert;

import org.gauss.jsonstruct.DDLValueStruct;

import java.util.List;

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
    List<String> convertToOpenGaussDDL(DDLValueStruct ddlValueStruct);

    /**
     * convert tell ddlProcessor this sql need be add to cache or execute directly
     * @return
     */
    default boolean needCacheSql() {
        return false;
    }


}
