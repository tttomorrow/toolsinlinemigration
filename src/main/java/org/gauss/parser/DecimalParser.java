/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.parser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Map;

public class DecimalParser implements Parser {

    @Override
    public Object parse(Map<String, String> params, Object value) {
        int scale = Integer.parseInt(params.get("scale"));

        BigDecimal number = new BigDecimal(new BigInteger(Base64.getDecoder().decode((String)value)), scale);

        return number;
    }
}
