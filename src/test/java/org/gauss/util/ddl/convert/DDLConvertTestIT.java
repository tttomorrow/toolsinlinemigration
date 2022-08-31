package org.gauss.util.ddl.convert;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.gauss.jsonstruct.DDLValueStruct;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author saxisuer
 * @Description
 * @date 2022/8/31
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public class DDLConvertTestIT {

    private final ObjectMapper topicMapper = new ObjectMapper();
    private List<DDLValueStruct> ddlValueStruct = null;

    @Before
    public void before() throws IOException {
        topicMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        topicMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        CollectionType collectionType = topicMapper.getTypeFactory().constructCollectionType(List.class, DDLValueStruct.class);
        ddlValueStruct = topicMapper.readValue(DDLConvertTestIT.class.getClassLoader().getResourceAsStream("MultipleJsonFile.json"),
                                               collectionType);
    }

    @Test
    public void test() {
        ddlValueStruct.size();
        for (DDLValueStruct valueStruct : ddlValueStruct) {
            List<DDLConvert> dDlConvert = DDLConvertHandler.getDDlConvert(valueStruct.getPayload());
            for (DDLConvert ddlConvert : dDlConvert) {
                List<String> strings = ddlConvert.convertToOpenGaussDDL(valueStruct);
                System.out.println(strings);
            }
        }
    }
}
