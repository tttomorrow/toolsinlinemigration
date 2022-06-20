/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.util.ddl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.gauss.jsonstruct.DDLValueStruct;
import org.gauss.jsonstruct.SourceStruct;
import org.gauss.util.JDBCExecutor;
import org.gauss.util.OpenGaussConstant;
import org.gauss.util.Processor;
import org.gauss.util.ddl.convert.DDLConvertHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DDLProcessor extends Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DDLProcessor.class);
    private final ObjectMapper topicMapper = new ObjectMapper();
    private final JDBCExecutor ddlExecutor = new JDBCExecutor();
    private final DDLCacheController ddlCacheController = DDLCacheController.getInstance();

    public DDLProcessor() {
        topicMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void process(ConsumerRecord<String, String> record) {
        DDLValueStruct value = null;
        try {
            value = topicMapper.readValue(record.value(), DDLValueStruct.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SourceStruct source = value.getPayload().getSource();
        String snapshot = source.getSnapshot();
        if (snapshot.equals("true") || snapshot.equals("last")) {
            return;
        }
        String ddl = value.getPayload().getDdl();
        LOGGER.info("get DDL SQL: {}", ddl);

        String openGaussDDL = DDLConvertHandler.getDDlConvert(value.getPayload()).convertToOpenGaussDDL(value);
        // when get ddl sql ,if sql contains rename table ,drop table, drop column , must compare current dml scn and ddl scn
        if (null != openGaussDDL) {
            if (needCache(value.getPayload())) {
                // some time the field commit_scn will be null ,so we use scn field, if scn and commit_scn both have value, we use Math.min(scn,
                // commit_scn) as value
                long currentScn;
                String commit_scn = value.getPayload().getSource().getCommit_scn();
                Long scn = value.getPayload().getSource().getScn();
                if (commit_scn != null && scn != null) {
                    currentScn = Math.min(scn, Long.parseLong(commit_scn));
                } else {
                    currentScn = commit_scn != null ? Long.parseLong(commit_scn) : scn;
                }
                LOGGER.info("add ddl sql: {} to cache,ddl_scn: {}", openGaussDDL, currentScn);
                ddlCacheController.addDdl(currentScn, openGaussDDL);
            } else {
                ddlExecutor.executeDDL(openGaussDDL);
            }
        }
    }


    public boolean needCache(DDLValueStruct.PayloadStruct payload) {
        String ddl = payload.getDdl();
        if (StringUtils.containsIgnoreCase(ddl, OpenGaussConstant.TABLE_RENAME)) {
            return true;
        } else if (payload.getTableChanges()
                          .stream()
                          .anyMatch((tableChangeStruct -> tableChangeStruct.getType().equals(OpenGaussConstant.TABLE_ALTER)))) {
            return true;
        } else if (StringUtils.containsIgnoreCase(payload.getTableChanges().get(0).getType(), OpenGaussConstant.TABLE_PRIMARY_KEY_DROP)) {
            return true;
        }
        return false;
    }
}
