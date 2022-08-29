package org.gauss.util.ddl.convert;

import org.apache.commons.lang3.StringUtils;
import org.gauss.jsonstruct.DDLValueStruct;
import org.gauss.jsonstruct.TableChangeStruct;
import org.gauss.util.ObjectNameConvertUtil;
import org.gauss.util.OpenGaussConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author saxisuer
 * @Description convert oracle index ddl to openGauss ddl
 * @date 2022/8/11
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public class IndexConvert extends BaseConvert implements DDLConvert {

    private static final Logger logger = LoggerFactory.getLogger(IndexConvert.class);

    @Override
    public List<String> convertToOpenGaussDDL(DDLValueStruct ddlValueStruct) {
        DDLValueStruct.PayloadStruct payload = ddlValueStruct.getPayload();
        TableChangeStruct tableChangeStruct = payload.getTableChanges().get(0);
        TableChangeStruct.Table table = tableChangeStruct.getTable();
        TableChangeStruct.IndexChanges indexChanges = table.getIndexChanges();
        if (null == indexChanges) {
            logger.error("none index change message found in message, ddl:{}", payload.getDdl());
            return null;
        }
        if (tableChangeStruct.getType().equals(OpenGaussConstant.CREATE_INDEX)) {
            return buildCreateIndex(table, indexChanges);
        } else if (tableChangeStruct.getType().equals(OpenGaussConstant.DROP_INDEX)) {
            return buildDropIndex(table, indexChanges);
        }
        return null;
    }

    /**
     * build drop index ddl
     *
     * @param table
     * @param indexChanges
     * @return
     */
    private List<String> buildDropIndex(TableChangeStruct.Table table, TableChangeStruct.IndexChanges indexChanges) {
        String stringBuilder = OpenGaussConstant.TABLE_PRIMARY_KEY_DROP + StringUtils.SPACE + OpenGaussConstant.INDEX + StringUtils.SPACE +
                wrapQuote(indexChanges.getSchemaName()) + OpenGaussConstant.DOT +
                wrapQuote(ObjectNameConvertUtil.getIndexNameForOpenGauss(indexChanges.getIndexName()));
        logger.info(stringBuilder);
        return Collections.singletonList(stringBuilder);
    }

    private List<String> buildCreateIndex(TableChangeStruct.Table table, TableChangeStruct.IndexChanges indexChanges) {
        StringBuilder indexBuilder = new StringBuilder();
        indexBuilder.append(OpenGaussConstant.TABLE_CREATE).append(StringUtils.SPACE);
        if (indexChanges.isUnique()) {
            indexBuilder.append(OpenGaussConstant.UNIQUE).append(StringUtils.SPACE);
        }
        indexBuilder.append(OpenGaussConstant.INDEX).append(StringUtils.SPACE);
        indexBuilder.append(wrapQuote(indexChanges.getSchemaName()))
                    .append(OpenGaussConstant.DOT)
                    .append(wrapQuote(ObjectNameConvertUtil.getIndexNameForOpenGauss(indexChanges.getIndexName())));
        indexBuilder.append(StringUtils.SPACE)
                    .append(OpenGaussConstant.ON)
                    .append(StringUtils.SPACE)
                    .append(wrapQuote(indexChanges.getSchemaName()))
                    .append(OpenGaussConstant.DOT)
                    .append(wrapQuote(indexChanges.getTableName()))
                    .append(StringUtils.SPACE);
        String collect = indexChanges.getIndexColumnExpr()
                                     .stream()
                                     .map(e -> OpenGaussConstant.BRACKETS_START + replaceExpression(e.getColumnExpr(), e.getIncludeColumn()) + OpenGaussConstant.BRACKETS_ENDT + StringUtils.SPACE +
                                             (e.isDesc() ? OpenGaussConstant.DESC : OpenGaussConstant.ASC))
                                     .collect(Collectors.joining(OpenGaussConstant.COMMA + StringUtils.SPACE,
                                                                 OpenGaussConstant.BRACKETS_START,
                                                                 OpenGaussConstant.BRACKETS_ENDT));
        indexBuilder.append(StringUtils.SPACE);
        indexBuilder.append(collect);
        logger.info(indexBuilder.toString());
        return Collections.singletonList(indexBuilder.toString());
    }

    @Override
    public boolean needCacheSql() {
        return true;
    }
}
