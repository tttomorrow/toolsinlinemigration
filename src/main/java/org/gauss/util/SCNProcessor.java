/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.util;

import org.gauss.jsonstruct.DDLValueStruct;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

public class SCNProcessor {
    public static String getSCN(String valueRecord) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        DDLValueStruct value = null;
        try {
            value = objectMapper.readValue(valueRecord, DDLValueStruct.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String scn = value.getPayload().getSource().getScn();

        return scn;
    }

    public static void write(String scnFile, String scn) {
        try {
            File file = new File(scnFile);
            Writer writer = new FileWriter(file);
            writer.write(scn);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
