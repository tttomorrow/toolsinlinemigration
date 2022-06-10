package org.gauss.util.ddl.convert;

import org.apache.commons.lang3.StringUtils;
import org.gauss.MigrationConfig;
import org.gauss.util.ObjectNameConvertUtil;
import org.gauss.util.OpenGaussConstant;
import org.gauss.util.QuoteCharacter;

import java.util.Collection;

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

    public String unwrapQuote(String s) {
        if (s.contains(quoteCharacter.getStartDelimiter())) {
            return s.replaceAll(quoteCharacter.getStartDelimiter(), "");
        }
        if (s.contains(quoteCharacter.getEndDelimiter())) {
            return s.replaceAll(quoteCharacter.getEndDelimiter(), "");
        }
        return s;
    }
}
