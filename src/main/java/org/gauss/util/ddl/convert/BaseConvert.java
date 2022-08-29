package org.gauss.util.ddl.convert;

import org.apache.commons.lang3.StringUtils;
import org.gauss.jsonstruct.SourceStruct;
import org.gauss.util.ObjectNameConvertUtil;
import org.gauss.util.OpenGaussConstant;
import org.gauss.util.QuoteCharacter;

import java.util.List;

/**
 * @author saxisuer
 * @Description
 * @date 2022/2/11
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public abstract class BaseConvert implements DDLConvert {

    private QuoteCharacter quoteCharacter = QuoteCharacter.DOUBLE_QUOTE;

    public String wrapQuote(String s) {
        s = ObjectNameConvertUtil.getObjectNameForOpenGauss(s);
        return quoteCharacter.wrap(s);
    }
    public String addBrackets(Object str) {
        StringBuilder sb = new StringBuilder();
        sb.append(OpenGaussConstant.BRACKETS_START).append(str).append(OpenGaussConstant.BRACKETS_ENDT);
        return sb.toString();
    }

    public String getColumnJoinStr() {
        StringBuilder sb = new StringBuilder();
        sb.append(OpenGaussConstant.COMMA).append(StringUtils.CR).append(StringUtils.LF);
        return sb.toString();
    }

    protected String getTableAlterTitleSql(SourceStruct source) {
        return OpenGaussConstant.TABLE_ALTER + StringUtils.SPACE + OpenGaussConstant.TABLE + StringUtils.SPACE + wrapQuote(source.getSchema()) +
                OpenGaussConstant.DOT + wrapQuote(source.getTable()) + StringUtils.SPACE;
    }

    /**
     * remove quote from text
     * @param text
     * @return text remove begin quote and end quote
     */
    public String unwrapQuote(String text) {
        if (text.contains(quoteCharacter.getStartDelimiter())) {
            return text.replaceAll(quoteCharacter.getStartDelimiter(), "");
        }
        if (text.contains(quoteCharacter.getEndDelimiter())) {
            return text.replaceAll(quoteCharacter.getEndDelimiter(), "");
        }
        return text;
    }

    public String replaceExpression(String expr, List<String> includeColumn) {
        String expression = expr;
        for (int i = 0; i < includeColumn.size(); i++) {
            expression = expression.replace(":$" + i, wrapQuote(includeColumn.get(i)));
        }
        return expression;
    }
}
