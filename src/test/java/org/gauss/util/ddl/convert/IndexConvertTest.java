package org.gauss.util.ddl.convert;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gauss.jsonstruct.DDLValueStruct;
import org.gauss.util.TestUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author saxisuer
 * @Description index convert test case
 * @date 2022/8/11
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public class IndexConvertTest {
    private ObjectMapper topicMapper = new ObjectMapper();
    private IndexConvert indexConvert = new IndexConvert();
    private DDLValueStruct createIndexStruct_1 = null;
    private DDLValueStruct createIndexStruct_2 = null;
    private DDLValueStruct createIndexStruct_3 = null;
    private String createIndexString_1;
    private String createIndexString_2;
    private String createIndexString_3;

    @Before
    public void before() throws IOException {
        topicMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        createIndexString_1 = TestUtil.readJsonFromFile("CreateIndexTopic1.json");
        createIndexString_2 = TestUtil.readJsonFromFile("CreateIndexTopic2.json");
        createIndexString_3 = TestUtil.readJsonFromFile("CreateIndexTopic3.json");
        createIndexStruct_1 = topicMapper.readValue(createIndexString_1, DDLValueStruct.class);
        createIndexStruct_2 = topicMapper.readValue(createIndexString_2, DDLValueStruct.class);
        createIndexStruct_3 = topicMapper.readValue(createIndexString_3, DDLValueStruct.class);
    }

    @Test
    public void testConvertCreateIndex() {
        indexConvert.convertToOpenGaussDDL(createIndexStruct_1);
        indexConvert.convertToOpenGaussDDL(createIndexStruct_2);
        indexConvert.convertToOpenGaussDDL(createIndexStruct_3);
    }
}
