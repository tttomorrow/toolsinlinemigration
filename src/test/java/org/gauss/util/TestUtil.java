package org.gauss.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * @author saxisuer
 * @Description
 * @date 2022/8/22
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public class TestUtil {

    public static String readJsonFromFile(String fileName) {
        InputStream resourceAsStream = TestUtil.class.getClassLoader().getResourceAsStream(fileName);
        return new BufferedReader(new InputStreamReader(resourceAsStream)).lines().collect(Collectors.joining("\n"));
    }
}
