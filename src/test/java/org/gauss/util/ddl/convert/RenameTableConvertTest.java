package org.gauss.util.ddl.convert;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gauss.jsonstruct.DDLValueStruct;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @author saxisuer
 * @Description //TODO
 * @date 2022/6/29
 * @email sheng.pu@enmotech.com
 * @COMPANY ENMOTECH
 */
public class RenameTableConvertTest {

    private final ObjectMapper topicMapper = new ObjectMapper();

    private RenameTableConvert renameTableConvert = new RenameTableConvert();

    private DDLValueStruct ddlValueStruct= null;
    private String topicValue="{\n" + "  \"schema\": {\n" + "    \"type\": \"struct\",\n" + "    \"fields\": [\n" + "      {\n" +
            "        \"type\": \"struct\",\n" + "        \"fields\": [\n" + "          {\n" + "            \"type\": \"string\",\n" +
            "            \"optional\": false,\n" + "            \"field\": \"version\"\n" + "          },\n" + "          {\n" +
            "            \"type\": \"string\",\n" + "            \"optional\": false,\n" + "            \"field\": \"connector\"\n" +
            "          },\n" + "          {\n" + "            \"type\": \"string\",\n" + "            \"optional\": false,\n" +
            "            \"field\": \"name\"\n" + "          },\n" + "          {\n" + "            \"type\": \"int64\",\n" +
            "            \"optional\": false,\n" + "            \"field\": \"ts_ms\"\n" + "          },\n" + "          {\n" +
            "            \"type\": \"string\",\n" + "            \"optional\": true,\n" + "            \"name\": \"io.debezium.data.Enum\",\n" +
            "            \"version\": 1,\n" + "            \"parameters\": {\n" + "              \"allowed\": \"true,last,false,incremental\"\n" +
            "            },\n" + "            \"default\": \"false\",\n" + "            \"field\": \"snapshot\"\n" + "          },\n" +
            "          {\n" + "            \"type\": \"string\",\n" + "            \"optional\": false,\n" + "            \"field\": \"db\"\n" +
            "          },\n" + "          {\n" + "            \"type\": \"string\",\n" + "            \"optional\": true,\n" +
            "            \"field\": \"sequence\"\n" + "          },\n" + "          {\n" + "            \"type\": \"string\",\n" +
            "            \"optional\": false,\n" + "            \"field\": \"schema\"\n" + "          },\n" + "          {\n" +
            "            \"type\": \"string\",\n" + "            \"optional\": false,\n" + "            \"field\": \"table\"\n" + "          },\n" +
            "          {\n" + "            \"type\": \"string\",\n" + "            \"optional\": true,\n" + "            \"field\": \"txId\"\n" +
            "          },\n" + "          {\n" + "            \"type\": \"string\",\n" + "            \"optional\": true,\n" +
            "            \"field\": \"scn\"\n" + "          },\n" + "          {\n" + "            \"type\": \"string\",\n" +
            "            \"optional\": true,\n" + "            \"field\": \"commit_scn\"\n" + "          },\n" + "          {\n" +
            "            \"type\": \"string\",\n" + "            \"optional\": true,\n" + "            \"field\": \"lcr_position\"\n" +
            "          }\n" + "        ],\n" + "        \"optional\": false,\n" + "        \"name\": \"io.debezium.connector.oracle.Source\",\n" +
            "        \"field\": \"source\"\n" + "      },\n" + "      {\n" + "        \"type\": \"string\",\n" + "        \"optional\": true,\n" +
            "        \"field\": \"databaseName\"\n" + "      },\n" + "      {\n" + "        \"type\": \"string\",\n" +
            "        \"optional\": true,\n" + "        \"field\": \"schemaName\"\n" + "      },\n" + "      {\n" + "        \"type\": \"string\",\n" +
            "        \"optional\": true,\n" + "        \"field\": \"ddl\"\n" + "      },\n" + "      {\n" + "        \"type\": \"array\",\n" +
            "        \"items\": {\n" + "          \"type\": \"struct\",\n" + "          \"fields\": [\n" + "            {\n" +
            "              \"type\": \"string\",\n" + "              \"optional\": false,\n" + "              \"field\": \"type\"\n" +
            "            },\n" + "            {\n" + "              \"type\": \"string\",\n" + "              \"optional\": false,\n" +
            "              \"field\": \"id\"\n" + "            },\n" + "            {\n" + "              \"type\": \"struct\",\n" +
            "              \"fields\": [\n" + "                {\n" + "                  \"type\": \"string\",\n" +
            "                  \"optional\": true,\n" + "                  \"field\": \"defaultCharsetName\"\n" + "                },\n" +
            "                {\n" + "                  \"type\": \"array\",\n" + "                  \"items\": {\n" +
            "                    \"type\": \"string\",\n" + "                    \"optional\": false\n" + "                  },\n" +
            "                  \"optional\": true,\n" + "                  \"field\": \"primaryKeyColumnNames\"\n" + "                },\n" +
            "                {\n" + "                  \"type\": \"array\",\n" + "                  \"items\": {\n" +
            "                    \"type\": \"struct\",\n" + "                    \"fields\": [\n" + "                      {\n" +
            "                        \"type\": \"string\",\n" + "                        \"optional\": false,\n" +
            "                        \"field\": \"name\"\n" + "                      },\n" + "                      {\n" +
            "                        \"type\": \"int32\",\n" + "                        \"optional\": false,\n" +
            "                        \"field\": \"jdbcType\"\n" + "                      },\n" + "                      {\n" +
            "                        \"type\": \"int32\",\n" + "                        \"optional\": true,\n" +
            "                        \"field\": \"nativeType\"\n" + "                      },\n" + "                      {\n" +
            "                        \"type\": \"string\",\n" + "                        \"optional\": false,\n" +
            "                        \"field\": \"typeName\"\n" + "                      },\n" + "                      {\n" +
            "                        \"type\": \"string\",\n" + "                        \"optional\": true,\n" +
            "                        \"field\": \"typeExpression\"\n" + "                      },\n" + "                      {\n" +
            "                        \"type\": \"string\",\n" + "                        \"optional\": true,\n" +
            "                        \"field\": \"charsetName\"\n" + "                      },\n" + "                      {\n" +
            "                        \"type\": \"int32\",\n" + "                        \"optional\": true,\n" +
            "                        \"field\": \"length\"\n" + "                      },\n" + "                      {\n" +
            "                        \"type\": \"int32\",\n" + "                        \"optional\": true,\n" +
            "                        \"field\": \"scale\"\n" + "                      },\n" + "                      {\n" +
            "                        \"type\": \"int32\",\n" + "                        \"optional\": false,\n" +
            "                        \"field\": \"position\"\n" + "                      },\n" + "                      {\n" +
            "                        \"type\": \"boolean\",\n" + "                        \"optional\": true,\n" +
            "                        \"field\": \"optional\"\n" + "                      },\n" + "                      {\n" +
            "                        \"type\": \"boolean\",\n" + "                        \"optional\": true,\n" +
            "                        \"field\": \"autoIncremented\"\n" + "                      },\n" + "                      {\n" +
            "                        \"type\": \"boolean\",\n" + "                        \"optional\": true,\n" +
            "                        \"field\": \"generated\"\n" + "                      },\n" + "                      {\n" +
            "                        \"type\": \"string\",\n" + "                        \"optional\": true,\n" +
            "                        \"field\": \"comment\"\n" + "                      }\n" + "                    ],\n" +
            "                    \"optional\": false,\n" + "                    \"name\": \"io.debezium.connector.schema.Column\"\n" +
            "                  },\n" + "                  \"optional\": false,\n" + "                  \"field\": \"columns\"\n" +
            "                },\n" + "                {\n" + "                  \"type\": \"string\",\n" + "                  \"optional\": true,\n" +
            "                  \"field\": \"comment\"\n" + "                }\n" + "              ],\n" + "              \"optional\": false,\n" +
            "              \"name\": \"io.debezium.connector.schema.Table\",\n" + "              \"field\": \"table\"\n" + "            }\n" +
            "          ],\n" + "          \"optional\": false,\n" + "          \"name\": \"io.debezium.connector.schema.Change\"\n" + "        },\n" +
            "        \"optional\": false,\n" + "        \"field\": \"tableChanges\"\n" + "      }\n" + "    ],\n" + "    \"optional\": false,\n" +
            "    \"name\": \"io.debezium.connector.oracle.SchemaChangeValue\"\n" + "  },\n" + "  \"payload\": {\n" + "    \"source\": {\n" +
            "      \"version\": \"1.8.1.Final\",\n" + "      \"connector\": \"oracle\",\n" + "      \"name\": \"my_oracle_connector_001\",\n" +
            "      \"ts_ms\": 1656519928000,\n" + "      \"snapshot\": \"false\",\n" + "      \"db\": \"ORCLCDB\",\n" +
            "      \"sequence\": null,\n" + "      \"schema\": \"C##ROMA_LOGMINER\",\n" + "      \"table\": \"T_DDL_0031_01,T_DDL_0031\",\n" +
            "      \"txId\": \"03001d00d8180100\",\n" + "      \"scn\": \"99038178\",\n" + "      \"commit_scn\": \"99037940\",\n" +
            "      \"lcr_position\": null\n" + "    },\n" + "    \"databaseName\": \"ORCLCDB\",\n" + "    \"schemaName\": \"C##ROMA_LOGMINER\",\n" +
            "    \"ddl\": \"alter table t_ddl_0031 rename to t_ddl_0031_01;\",\n" + "    \"tableChanges\": [\n" + "      {\n" +
            "        \"type\": \"ALTER\",\n" + "        \"id\": \"\\\"ORCLCDB\\\".\\\"C##ROMA_LOGMINER\\\".\\\"T_DDL_0031_01\\\"\",\n" +
            "        \"table\": {\n" + "          \"defaultCharsetName\": null,\n" + "          \"primaryKeyColumnNames\": [\n" + "            \n" +
            "          ],\n" + "          \"columns\": [\n" + "            {\n" + "              \"name\": \"ID\",\n" +
            "              \"jdbcType\": 2,\n" + "              \"nativeType\": null,\n" + "              \"typeName\": \"NUMBER\",\n" +
            "              \"typeExpression\": \"NUMBER\",\n" + "              \"charsetName\": null,\n" + "              \"length\": 38,\n" +
            "              \"scale\": 0,\n" + "              \"position\": 1,\n" + "              \"optional\": true,\n" +
            "              \"autoIncremented\": false,\n" + "              \"generated\": false,\n" + "              \"comment\": null\n" +
            "            },\n" + "            {\n" + "              \"name\": \"NAME\",\n" + "              \"jdbcType\": 12,\n" +
            "              \"nativeType\": null,\n" + "              \"typeName\": \"VARCHAR2\",\n" +
            "              \"typeExpression\": \"VARCHAR2\",\n" + "              \"charsetName\": null,\n" + "              \"length\": 50,\n" +
            "              \"scale\": null,\n" + "              \"position\": 2,\n" + "              \"optional\": true,\n" +
            "              \"autoIncremented\": false,\n" + "              \"generated\": false,\n" + "              \"comment\": null\n" +
            "            },\n" + "            {\n" + "              \"name\": \"ADDRESS\",\n" + "              \"jdbcType\": 12,\n" +
            "              \"nativeType\": null,\n" + "              \"typeName\": \"VARCHAR2\",\n" +
            "              \"typeExpression\": \"VARCHAR2\",\n" + "              \"charsetName\": null,\n" + "              \"length\": 50,\n" +
            "              \"scale\": null,\n" + "              \"position\": 3,\n" + "              \"optional\": true,\n" +
            "              \"autoIncremented\": false,\n" + "              \"generated\": false,\n" + "              \"comment\": null\n" +
            "            },\n" + "            {\n" + "              \"name\": \"DEPT_ADD\",\n" + "              \"jdbcType\": 12,\n" +
            "              \"nativeType\": null,\n" + "              \"typeName\": \"VARCHAR2\",\n" +
            "              \"typeExpression\": \"VARCHAR2\",\n" + "              \"charsetName\": null,\n" + "              \"length\": 50,\n" +
            "              \"scale\": null,\n" + "              \"position\": 4,\n" + "              \"optional\": true,\n" +
            "              \"autoIncremented\": false,\n" + "              \"generated\": false,\n" + "              \"comment\": null\n" +
            "            }\n" + "          ],\n" + "          \"comment\": null\n" + "        }\n" + "      }\n" + "    ]\n" + "  }\n" + "}";

    @Before
    public void before() throws IOException {
        topicMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ddlValueStruct = topicMapper.readValue(topicValue, DDLValueStruct.class);
    }

    @Test
    public void test() {
        String s = renameTableConvert.convertToOpenGaussDDL(ddlValueStruct);
        Assert.assertEquals("ALTER TABLE \"C##ROMA_LOGMINER\".\"T_DDL_0031\" RENAME TO \"T_DDL_0031_01\"",s);
    }
}
