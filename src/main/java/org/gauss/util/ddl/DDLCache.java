package org.gauss.util.ddl;

/**
 * @author saxisuer
 * @Description cache ddl execute sql
 * @date 2022/2/10
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public class DDLCache {
    private String ddl;
    private Long scn;

    public String getDdl() {
        return ddl;
    }

    public void setDdl(String ddl) {
        this.ddl = ddl;
    }

    public Long getScn() {
        return scn;
    }

    public void setScn(Long scn) {
        this.scn = scn;
    }
}
