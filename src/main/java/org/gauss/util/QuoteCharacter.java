package org.gauss.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author saxisuer
 * @Description quote character util
 * @date 2022/2/10
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public enum QuoteCharacter {
    /**
     * BACK_QUOTE
     */
    BACK_QUOTE("`", "`"),

    /**
     * SINGLE_QUOTE
     */
    SINGLE_QUOTE("'", "'"),

    /**
     * DOUBLE_QUOTE
     */
    DOUBLE_QUOTE("\"", "\""),

    /**
     * BRACKETS
     */
    BRACKETS("[", "]"),

    /**
     * NONE
     */
    NONE("", "");

    private final String startDelimiter;

    private final String endDelimiter;

    QuoteCharacter(String startDelimiter, String endDelimiter) {
        this.startDelimiter = startDelimiter;
        this.endDelimiter = endDelimiter;
    }

    public String getStartDelimiter() {
        return startDelimiter;
    }

    public String getEndDelimiter() {
        return endDelimiter;
    }

    /**
     * @param text
     * @return value of quote
     */
    public static QuoteCharacter getQuoteCharacter(final String text) {
        if (StringUtils.isBlank(text)) {
            return NONE;
        }
        return Arrays.stream(values()).filter(each -> NONE != each && each.startDelimiter.charAt(0) == text.charAt(0)).findFirst().orElse(NONE);
    }

    /**
     * Wrap value with quote character.
     *
     * @param value value to be wrapped
     * @return wrapped value
     */
    public String wrap(final String value) {
        return String.format("%s%s%s", startDelimiter, value, endDelimiter);
    }
}
