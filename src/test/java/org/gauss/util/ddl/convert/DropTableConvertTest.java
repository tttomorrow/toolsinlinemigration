package org.gauss.util.ddl.convert;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gauss.jsonstruct.DDLValueStruct;
import org.gauss.util.TestUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;


/**
 * @author saxisuer
 * @Description //TODO
 * @date 2022/6/23
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public class DropTableConvertTest {

    private final ObjectMapper topicMapper = new ObjectMapper();

    private DropTableConvert dropTableConvert = new DropTableConvert();

    private DDLValueStruct ddlValueStruct = null;

    private String topicValue;


    @Before
    public void before() throws IOException {
        topicMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        topicValue = TestUtil.readJsonFromFile("DropTableTopicValue.json");
        ddlValueStruct = topicMapper.readValue(topicValue, DDLValueStruct.class);
    }

    @Test
    public void test() {
        List<String> s = dropTableConvert.convertToOpenGaussDDL(ddlValueStruct);
        Assert.assertEquals(1, s.size());
    }

}