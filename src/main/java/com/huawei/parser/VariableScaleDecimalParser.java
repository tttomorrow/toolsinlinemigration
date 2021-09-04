package com.huawei.parser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Map;

public class VariableScaleDecimalParser implements Parser {

    @Override
    public Object parse(Map<String, String> params, Object value) {
        Map<String, Object> realValue = (Map<String, Object>)value;

        int scale = Integer.parseInt(realValue.get("scale").toString());
        String valStr = realValue.get("value").toString();

        BigDecimal number = new BigDecimal(new BigInteger(Base64.getDecoder().decode(valStr)), scale);

        return number;
    }
}
