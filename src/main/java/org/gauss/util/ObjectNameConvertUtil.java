package org.gauss.util;

import org.apache.commons.lang3.StringUtils;
import org.gauss.MigrationConfig;

/**
 * @author saxisuer
 * @Description
 * @date 2022/6/10
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public class ObjectNameConvertUtil {
    /**
     * convert oracle Object to openGauss Object
     * example: OBJECT_A convert to object_a
     * Object_A convert to Object_A
     *
     * @param rawObjectName
     * @return string opengauss object name
     */
    public static String getObjectNameForOpenGauss(String rawObjectName) {
        if (MigrationConfig.isSmartConversionOfObjectNames()) {
            if (rawObjectName.toLowerCase().equals(rawObjectName) || rawObjectName.toUpperCase().equals(rawObjectName)) {
                rawObjectName = rawObjectName.toLowerCase();
            }
        }
        return rawObjectName;
    }

    /**
     * get index_name with prefix
     *
     * @param indexName
     * @return string opengauss index name
     */
    public static String getIndexNameForOpenGauss(String indexName) {
        if (StringUtils.isNotBlank(MigrationConfig.getIndexPrefix())) {
            indexName = MigrationConfig.getIndexPrefix() + indexName;
        }
        return indexName;
    }
}
