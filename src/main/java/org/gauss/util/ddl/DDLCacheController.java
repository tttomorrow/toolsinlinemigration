package org.gauss.util.ddl;

import io.debezium.util.FunctionalReadWriteLock;
import org.gauss.util.JDBCExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author saxisuer
 * @Description
 * @date 2022/2/10
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public class DDLCacheController {

    private final Logger LOGGER = LoggerFactory.getLogger(DDLCacheController.class);
    private static final FunctionalReadWriteLock LOCK = FunctionalReadWriteLock.reentrant();
    private static final DdlContent DDL_CONTENT = new DdlContent();
    private final JDBCExecutor ddlExecutor = new JDBCExecutor();
    private static final DDLCacheController INSTANCE = new DDLCacheController();

    private DDLCacheController() {
    }

    public static DDLCacheController getInstance() {
        return INSTANCE;
    }

    /**
     * add DDL to ddl cache
     *
     * @param scn
     * @param ddl
     */
    public void addDdl(Long scn, String ddl) {
        LOCK.write(() -> {
            DDL_CONTENT.put(ddl, scn);
        });
    }

    /**
     * check if there is some ddl need execute before this scn
     *
     * @param scn
     * @return
     */
    public boolean checkCacheDDl(Long scn) {
        return LOCK.read(() -> {
            boolean result = false;
            Enumeration<Long> allScn = DDL_CONTENT.getAllScn();
            while (allScn.hasMoreElements()) {
                Long cacheScn = allScn.nextElement();
                if (cacheScn < scn) {
                    result = true;
                    break;
                }
            }
            return result;
        });
    }

    /**
     * get all ddl sql before this scn
     *
     * @param scn
     * @return
     */
    public List<String> getCacheDDlByScn(Long scn) {
        return LOCK.write(() -> {
            Enumeration<Long> allScn = DDL_CONTENT.getAllScn();
            List<Long> beforeScn = new ArrayList<>();
            while (allScn.hasMoreElements()) {
                Long cacheScn = allScn.nextElement();
                if (cacheScn < scn) {
                    beforeScn.add(cacheScn);
                }
            }
            beforeScn = beforeScn.stream().sorted().collect(Collectors.toList());
            List<String> cacheSql = new ArrayList<>();
            for (Long aLong : beforeScn) {
                cacheSql.addAll(DDL_CONTENT.getDDlByScn(aLong));
                DDL_CONTENT.clearScn(aLong);
            }
            return cacheSql;
        });
    }

    public boolean consumeDDL(List<String> ddl) {
        return LOCK.write(() -> {
            for (String s : ddl) {
                ddlExecutor.executeDDL(s);
                LOGGER.info("execute ddl: {}", s);
            }
            return true;
        });
    }


    /**
     * ddl content
     */
    private static class DdlContent {

        private final ConcurrentHashMap<Long, List<String>> values = new ConcurrentHashMap<>();

        public Set<Map.Entry<Long, List<String>>> getAll() {
            return values.entrySet();
        }

        public List<String> getDDlByScn(Long scn) {
            return values.get(scn);
        }

        public Enumeration<Long> getAllScn() {
            return values.keys();
        }

        public void put(String ddl, Long scn) {
            if (values.containsKey(scn)) {
                values.get(scn).add(ddl);
            } else {
                List<String> ddlList = new ArrayList<>();
                ddlList.add(ddl);
                values.put(scn, ddlList);
            }
        }

        public void clearScn(Long scn) {
            values.remove(scn);
        }
    }
}
