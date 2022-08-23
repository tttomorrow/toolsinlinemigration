package org.gauss.converter;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author saxisuer
 * @Description
 * @date 2022/7/20
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public class OracleToOpenGaussKeywordsConvert {
    /**
     * keywords
     */
    private final String keywords;
    /**
     * replace words of keywords
     */
    private final String replace;
    /**
     * regex pattern of keywords
     */
    private final String keywordsRegex;

    /**
     * description
     */
    private final String description;

    public OracleToOpenGaussKeywordsConvert(String keywords, String replace, String keywordsRegex, String description) {
        this.keywords = keywords;
        this.replace = replace;
        this.keywordsRegex = keywordsRegex;
        this.description = description;
    }

    /**
     * replace keywords to replace string
     *
     * @param text keywords
     * @return text replaced keywords
     */
    public String replaceKeyWord(String text) {
        if (StringUtils.isNotEmpty(text)) {
            Pattern wp = Pattern.compile(this.keywordsRegex, Pattern.MULTILINE);
            Matcher matcher = wp.matcher(text);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb, replace);
            }
            matcher.appendTail(sb);
            return sb.toString();
        } else {
            return null;
        }
    }
}
