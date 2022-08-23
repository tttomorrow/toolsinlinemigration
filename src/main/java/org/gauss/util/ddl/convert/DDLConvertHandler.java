package org.gauss.util.ddl.convert;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.gauss.jsonstruct.DDLValueStruct;

import org.gauss.jsonstruct.TableChangeStruct;
import org.gauss.util.OpenGaussConstant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author saxisuer
 * @Description
 * @date 2022/2/11
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public final class DDLConvertHandler {

    private DDLConvertHandler() {
    }

    public static boolean isAlterNeedParseSql(TableChangeStruct tableChangeStruct) {
        TableChangeStruct.Table table = tableChangeStruct.getTable();
        if (table != null && StringUtils.equals(tableChangeStruct.getType(), OpenGaussConstant.TABLE_ALTER)) {
            return (CollectionUtils.isNotEmpty(table.getPrimaryKeyColumnChanges())) || CollectionUtils.isNotEmpty(table.getCheckColumns()) ||
                    CollectionUtils.isNotEmpty(table.getUniqueColumns()) || CollectionUtils.isNotEmpty(table.getForeignKeyColumns());
        }
        return Boolean.FALSE;
    }

    public static boolean isTableColumnModify(TableChangeStruct tableChangeStruct) {
        TableChangeStruct.Table table = tableChangeStruct.getTable();
        if (table != null && StringUtils.equals(tableChangeStruct.getType(), OpenGaussConstant.TABLE_ALTER)) {
            return CollectionUtils.isNotEmpty(getColumnChanges(table.getColumns())) ||
                    (table.getChangeColumns() != null && table.getChangeColumns().hasChangeColumn());
        }
        return false;
    }

    private static List<TableChangeStruct.column> getColumnChanges(List<TableChangeStruct.column> columns) {
        return columns.stream().filter(column -> CollectionUtils.isNotEmpty(column.getModifyKeys())).collect(Collectors.toList());
    }

    /**
     * @param payload
     * @return
     */
    public static List<DDLConvert> getDDlConvert(DDLValueStruct.PayloadStruct payload) {
        List<DDLConvert> ddlConverts = new ArrayList<>();
        // create index or drop index
        if (StringUtils.equalsAny(payload.getTableChanges().get(0).getType(), OpenGaussConstant.CREATE_INDEX,OpenGaussConstant.DROP_INDEX)) {
            ddlConverts.add(new IndexConvert());
        }
        //rename table
        if (StringUtils.containsIgnoreCase(payload.getDdl(), OpenGaussConstant.TABLE_RENAME)) {
            ddlConverts.add(new RenameTableConvert());

        }
        //drop table
        if (StringUtils.equals(payload.getTableChanges().get(0).getType(), OpenGaussConstant.TABLE_PRIMARY_KEY_DROP)) {
            ddlConverts.add(new DropTableConvert());

        }
        //create table
        if (payload.getTableChanges()
                   .stream()
                   .anyMatch(tableChangeStruct -> StringUtils.equals(tableChangeStruct.getType(), OpenGaussConstant.TABLE_CREATE))) {
            ddlConverts.add(new CreateTableConvert());

        }
        if (payload.getTableChanges().stream().anyMatch(DDLConvertHandler::isTableColumnModify)) {
            ddlConverts.add(new AlterTableColumnConvert());
        }
        // table constraint
        if (payload.getTableChanges().stream().anyMatch((DDLConvertHandler::isAlterNeedParseSql))) {
            ddlConverts.add(new AlterTableConstraintConvert());
        }
        if (CollectionUtils.isEmpty(ddlConverts)) {
            ddlConverts.add(ddlValueStruct -> Collections.singletonList(ddlValueStruct.getPayload().getDdl()));
        }
        return ddlConverts;
    }
}
