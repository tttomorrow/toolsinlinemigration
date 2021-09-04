package com.huawei.common;

public class DMLSQL {
    public static final String INSERT_SQL = "INSERT INTO %s (%s) VALUES (%s)";

    // where clause: where a is null or where a = ?.
    public static final String UPDATE_SQL = "UPDATE %s SET %s WHERE ";

    // where clause: where a is null or where a = ?.
    public static final String DELETE_SQL = "DELETE FROM %s WHERE ";
}
