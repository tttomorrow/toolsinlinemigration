------------------------------更新操作-----------------------------------
------------------------------test_array---------------------------------
update test_array set array_int=arrayInt(1,1,1,1,1) where id=8;
update test_array set id=10 where id=2;
update test_array set name='test_array' where id=3;
update test_array set array_char=arrayChar('f','ff') where id=4;
update test_array set array_char=arrayChar('g','gg') where id=5;

--------------------------------------------------------------------------
------------------------------test_http-----------------------------------
update test_http set web=HTTPURITYPE.createURI('http://12345.com') where id=1;
update test_http set id=6 where id=2;
update test_http set name='zhang' where id=6;
---------------------------------------------------------------------------
------------------------------test_uri-------------------------------------
update test_uri set web=HTTPURITYPE.createURI('http://12345.com') where id=1;
update test_uri set id=6 where id=2;
update test_uri set name='zhang' where id=6;
update test_uri set web=SYS.URIFACTORY.getURI('/public/hr/doc3.xml') where id=3;
update test_uri set web=XDBURITYPE.createURI('/public/hr/doc6.xml') where id=4;
update test_uri set web=HTTPURITYPE.createURI('http://www.baidu.com') where id=5;

---------------------------------------------------------------------------
------------------------------test_xdb-------------------------------------
update test_xdb set web=XDBURITYPE.createURI('/public/hr/doc3.xml') where id=4;
update test_xdb set web=XDBURITYPE.createURI('/public/hr/doc6.xml') where id=5;
update test_xdb set id=7 where id=4;
---------------------------------------------------------------------------
------------------------------test_json_clob-------------------------------
update test_json_clob set content=to_clob('{"id":"1","name":"testjson1"}') where id='1';
update test_json_clob set content=to_clob('{"id":"2","name":"testjson2"}') where id='2';
update test_json_clob set content=to_clob('{"id":"3","name":"testjson3"}') where id='3';
update test_json_clob set content=to_clob('{"id":"4","name":"testjson4"}') where id='4';
update test_json_clob set content=to_clob('{"id":"5","name":"testjson5"}') where id='5';

---------------------------------------------------------------------------
------------------------------test_json_varchar2---------------------------
update test_json_varchar2 set content='{"id":"1","name":"testjson1"}' where id='1';
update test_json_varchar2 set content='{"id":"2","name":"testjson2"}' where id='2';
update test_json_varchar2 set content='{"id":"3","name":"testjson3"}' where id='3';
update test_json_varchar2 set content='{"id":"4","name":"testjson4"}' where id='4';
update test_json_varchar2 set content='{"id":"5","name":"testjson5"}' where id='5';

---------------------------------------------------------------------------
------------------------------test_long------------------------------------
update test_long set content='test_long11111' where id='1';
update test_long set content='' where id='2';
update test_long set content='test_long33333' where id='3';
update test_long set content='test_long44444' where id='4';
update test_long set content=null where id='5';
---------------------------------------------------------------------------
------------------------------test_longraw---------------------------------
update test_longraw set content='0123456789ABCDEF0011111' where id='1';
update test_longraw set content='' where id='2';
update test_longraw set content='0123456789ABCDEF0033333' where id='3';
update test_longraw set content='0123456789ABCDEF0044444' where id='4';
update test_longraw set content=null where id='5';
---------------------------------------------------------------------------
------------------------------test_object----------------------------------
update test_object set content=testObjectType1('testobject1_a1_111','testobject1_a2_111',testObjectType2('testobject2_a1_111','testobject2_a2_111')) where id='1';
update test_object set content=testObjectType1('testobject1_a1_222','testobject1_a2_222',testObjectType2('testobject2_a1_222','testobject2_a2_222')) where id='2';
update test_object set content=testObjectType1('testobject1_a1_333','testobject1_a2_333',testObjectType2('testobject2_a1_333','testobject2_a2_333')) where id='3';
update test_object set content=testObjectType1('testobject1_a1_444','testobject1_a2_444',testObjectType2('testobject2_a1_444','testobject2_a2_444')) where id='4';
update test_object set content=testObjectType1('testobject1_a1_555','testobject1_a2_555',testObjectType2('testobject2_a1_555','testobject2_a2_555')) where id='5';

---------------------------------------------------------------------------
------------------------------test_raw-------------------------------------
update test_raw set content='0123456789ABCDEF001' where id='1';
update test_raw set content='' where id='2';
update test_raw set content='0123456789ABCDEF003' where id='3';
update test_raw set content='0123456789ABCDEF004' where id='4';
update test_raw set content=null where id='5';
---------------------------------------------------------------------------
------------------------------test_xml-------------------------------------
update xmltest3 set doc ='<execution><id>make-assembly</id><phase>package</phase><goals><goal>assembly</goal></goals></execution>'  where name = 'huawei';
update xmltest3 set name = 'opengauss' where name = 'zhongzhi';
update xmltest3 set id = 3 where name = 'opengauss';
---------------------------------------------------------------------------
------------------------------test_sdogeometry-------------------------------------
update TEST_SDOGEOMETRY set id = 3 where id = 1;
update TEST_SDOGEOMETRY set content= SDO_GEOMETRY(2003,  NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1003,4), SDO_ORDINATE_ARRAY(8,7, 10,9, 8,11)) where id = 2;
---------------------------------------------------------------------------
------------------------------test_land_parcels-------------------------------------

--update land_parcels set feature_name = 'P2' where feature_name = 'P1';
--update land_parcels set feature=SDO_TOPO_GEOMETRY('CITY_DATA', 3,1,SDO_TOPO_OBJECT_ARRAY (SDO_TOPO_OBJECT (4, 3), SDO_TOPO_OBJECT (7, 3))) where feature_name = 'P2';

---------------------------------操作完成----------------------------------
---------------------------------SUCCESSFUL--------------------------------