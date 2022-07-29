package org.gauss.util.ddl.convert;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.gauss.jsonstruct.DDLValueStruct;

import org.gauss.jsonstruct.TableChangeStruct;
import org.gauss.util.OpenGaussConstant;

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

    public static boolean isAlterNeedParseSql(TableChangeStruct tableChangeStruct){
        TableChangeStruct.Table table = tableChangeStruct.getTable();
        if (table != null && StringUtils.equals(tableChangeStruct.getType(), OpenGaussConstant.TABLE_ALTER)) {
            return (CollectionUtils.isNotEmpty(table.getPrimaryKeyColumnChanges()))
                    || CollectionUtils.isNotEmpty(table.getCheckColumns())
                    || CollectionUtils.isNotEmpty(table.getUniqueColumns())
                    || CollectionUtils.isNotEmpty(table.getForeignKeyColumns())
                    || CollectionUtils.isNotEmpty(getColumnChanges(table.getColumns()));
        }
        return Boolean.FALSE;
    }

    private static List<TableChangeStruct.column> getColumnChanges(List<TableChangeStruct.column> columns) {
        return columns.stream().filter(column -> CollectionUtils.isNotEmpty(column.getModifyKeys())).collect(Collectors.toList());
    }

    /**
     * @param payload
     * @return
     */
    public static DDLConvert getDDlConvert(DDLValueStruct.PayloadStruct payload) {
        if (StringUtils.containsIgnoreCase(payload.getDdl(), OpenGaussConstant.TABLE_RENAME)) {
            return new RenameTableConvert();
        } else if (StringUtils.containsIgnoreCase(payload.getTableChanges().get(0).getType(), OpenGaussConstant.TABLE_PRIMARY_KEY_DROP)) {
            return new DropTableConvert();
        } else if (payload.getTableChanges()
                          .stream()
                          .anyMatch(tableChangeStruct -> StringUtils.equals(tableChangeStruct.getType(), OpenGaussConstant.TABLE_CREATE))) {
            return new CreateTableConvert();
        } else if (payload.getTableChanges().stream().anyMatch((DDLConvertHandler::isAlterNeedParseSql))) {
            return new AlterTableConstraintConvert();
        }
        return ddlValueStruct -> ddlValueStruct.getPayload().getDdl();
    }
}
