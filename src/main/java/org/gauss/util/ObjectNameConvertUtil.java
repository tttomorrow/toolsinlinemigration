package org.gauss.util;

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
     * @return
     */
    public static String getObjectNameForOpenGauss(String rawObjectName) {
        if (MigrationConfig.isSmartConversionOfObjectNames()) {
            if (rawObjectName.toLowerCase().equals(rawObjectName) || rawObjectName.toUpperCase().equals(rawObjectName)) {
                rawObjectName = rawObjectName.toLowerCase();
            }
        }
        return rawObjectName;
    }
}
