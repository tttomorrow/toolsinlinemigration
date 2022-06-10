package org.gauss.util.ddl.convert;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gauss.jsonstruct.DDLValueStruct;
import org.gauss.util.TestUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author saxisuer
 * @Description
 * @date 2022/7/18
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public class AlterTableColumnConvertTest {


    private final ObjectMapper topicMapper = new ObjectMapper();

    private AlterTableColumnConvert alterTableColumnConvert = new AlterTableColumnConvert();

    private DDLValueStruct modifyValueStruct = null;
    private DDLValueStruct addValueStruct = null;
    private DDLValueStruct dropValueStruct = null;
    private DDLValueStruct multipleChangeStruct = null;

    private String dropTopicValue;
    private String modifyTopicValue;
    private String addTopicValue;


    private String multipleColumnChangeSql;

    @Before
    public void before() throws IOException {
        topicMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        modifyTopicValue = TestUtil.readJsonFromFile("AlterTableModifyColumn.json");
        modifyValueStruct = topicMapper.readValue(modifyTopicValue, DDLValueStruct.class);
        addTopicValue = TestUtil.readJsonFromFile("AlterTableAddColumn.json");
        addValueStruct = topicMapper.readValue(addTopicValue, DDLValueStruct.class);
        dropTopicValue = TestUtil.readJsonFromFile("AlterTableDropColumn.json");
        dropValueStruct = topicMapper.readValue(dropTopicValue, DDLValueStruct.class);
        multipleColumnChangeSql = TestUtil.readJsonFromFile("AlterTableMultipleColumnChange.json");
        multipleChangeStruct = topicMapper.readValue(multipleColumnChangeSql, DDLValueStruct.class);
    }

    @Test
    public void testModify() {
        List<String> s = alterTableColumnConvert.convertToOpenGaussDDL(modifyValueStruct);
        assertEquals(3, s.size());
        String expected = "ALTER TABLE \"HONGYE\".\"TEST_1\" MODIFY \"TEST_HID\" numeric";
        assertEquals(expected, s.get(0));
    }

    @Test
    public void testAddColumn() {
        List<String> s = alterTableColumnConvert.convertToOpenGaussDDL(addValueStruct);
        assertEquals(1, s.size());
        String expected = "ALTER TABLE \"HONGYE\".\"TEST_1\" ADD \"TEST_HID\" numeric";
        assertEquals(expected, s.get(0));
    }

    @Test
    public void testDropColumn() {
        List<String> s = alterTableColumnConvert.convertToOpenGaussDDL(dropValueStruct);
        assertEquals(1, s.size());
        String expected = "ALTER TABLE \"HONGYE\".\"TEST_1\" DROP COLUMN \"TEST_HID\"";
        assertEquals(expected, s.get(0));
    }

    @Test
    public void testMultipleChange() {
        List<String> strings = alterTableColumnConvert.convertToOpenGaussDDL(multipleChangeStruct);
        System.out.println(strings);
    }

}