/*
 * Copyright (c) 2021 Huawei Technologies Co.,Ltd.
 */

package org.gauss.jsonstruct;

public class SourceStruct {
    private String version;
    private String connector;
    private String name;
    private Long ts_ms;
    private String snapshot;
    private String db;
    private String sequence;
    private String schema;
    private String table;
    private String txId;
    private String scn;
    private String commit_scn;
    private String lcr_position;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getConnector() {
        return connector;
    }

    public void setConnector(String connector) {
        this.connector = connector;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTs_ms() {
        return ts_ms;
    }

    public void setTs_ms(Long ts_ms) {
        this.ts_ms = ts_ms;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getScn() {
        return scn;
    }

    public void setScn(String scn) {
        this.scn = scn;
    }

    public String getCommit_scn() {
        return commit_scn;
    }

    public void setCommit_scn(String commit_scn) {
        this.commit_scn = commit_scn;
    }

    public String getLcr_position() {
        return lcr_position;
    }

    public void setLcr_position(String lcr_position) {
        this.lcr_position = lcr_position;
    }
}
