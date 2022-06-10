package org.gauss.converter;

import org.gauss.jsonstruct.TableChangeStruct;
import org.gauss.util.OpenGaussConstant;

import java.util.Arrays;

/**
 * @author saxisuer
 * @Description column convert enum include column type convert and ddl convert function
 * @date 2022/8/20
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public enum ColumnConvert {
    BYTEA("bytea"),
    JSON("json"),
    TEXT("text"),
    REAL("real"),
    XML("xml"),
    TIMESTAMP_WITHOUT_TIMEZONE("timestamp without time zone"),
    SMALLINT("smallint"),
    BIGINT("bigint"),
    INTEGER("integer"),
    DATE("date"),
    DOUBLE_PRECISION("double precision"),
    BOOLEAN("boolean"),
    INTERVAL_YEAR_TO_MONTH("interval year to month"),
    NUMERIC("numeric", (sourceColumn) -> {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("numeric");
        if (sourceColumn.getLength() > 0 && sourceColumn.getScale() > 0) {
            long precision = sourceColumn.getLength();
            long scale = sourceColumn.getScale();
            if (scale > precision) {
                precision = scale;
            }
            if (scale < 0) {
                precision = precision + Math.abs(scale);
                scale = 0;
            }
            stringBuilder.append(OpenGaussConstant.BRACKETS_START)
                         .append(precision)
                         .append(",")
                         .append(scale)
                         .append(OpenGaussConstant.BRACKETS_ENDT);
        } else if (sourceColumn.getLength() > 0 && sourceColumn.getScale() == 0) {
            stringBuilder.append(OpenGaussConstant.BRACKETS_START)
                         .append(sourceColumn.getLength())
                         .append(",")
                         .append(sourceColumn.getScale())
                         .append(OpenGaussConstant.BRACKETS_ENDT);
        }
        return stringBuilder.toString();
    }),
    INTERVAL_DAY_TO_SECOND("interval day to second",
                           (sourceColumn) -> "interval day to second" + OpenGaussConstant.BRACKETS_START + sourceColumn.getScale() +
                                   OpenGaussConstant.BRACKETS_ENDT),
    TIMESTAMP("timestamp",
              (sourceColumn) -> "timestamp" + OpenGaussConstant.BRACKETS_START + sourceColumn.getScale() + OpenGaussConstant.BRACKETS_ENDT),
    TIMESTAMP_WITH_TIMEZONE("timestamp with time zone",
                            (sourceColumn) -> "timestamp" + OpenGaussConstant.BRACKETS_START + sourceColumn.getScale() +
                                    OpenGaussConstant.BRACKETS_ENDT + " with time zone"),
    CHARACTER_VARYING("character varying",
                      (sourceColumn) -> "character varying" + OpenGaussConstant.BRACKETS_START + sourceColumn.getLength() +
                              OpenGaussConstant.BRACKETS_ENDT);

    private final String targetTypeName;
    private final ConvertFunction convertFunction;

    public ConvertFunction getConvertFunction() {
        return convertFunction;
    }

    @FunctionalInterface
    public interface ConvertFunction {
        String apply(TableChangeStruct.column sourceColumn);

    }

    ColumnConvert(String targetTypeName, ConvertFunction convertFunction) {
        this.targetTypeName = targetTypeName;
        this.convertFunction = convertFunction;
    }

    ColumnConvert(String targetTypeName) {
        this.targetTypeName = targetTypeName;
        this.convertFunction = sourceColumn -> targetTypeName;
    }

    public static ColumnConvert convertToColumnConvert(String sourceTypeName) {
        return Arrays.stream(values())
                     .filter(each -> each.targetTypeName.equals(ColumnTypeConverter.convertTypeName(sourceTypeName)))
                     .findFirst()
                     .orElse(CHARACTER_VARYING);
    }
}
