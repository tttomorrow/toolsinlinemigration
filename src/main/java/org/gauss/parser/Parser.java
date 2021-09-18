/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.parser;

import java.util.Map;

public interface Parser {
    Object parse(Map<String, String> params, Object value);
}
