--------------------------------插入操作---------------------------------
------------------------------test_array---------------------------------
insert into test_array values (1,'array1',arrayInt(1,2,3,4,5),arrayChar('a','aa','aaa','aaaaa','aaaaa'));
insert into test_array values (2,'array2',arrayInt(1,2,3,4),arrayChar('b','bb','bbb','bbbb'));
insert into test_array values (3,'array3',arrayInt(1,2,3),arrayChar('c','cc','ccc'));
insert into test_array values (4,'array4',arrayInt(1,2),arrayChar('d','dd'));
insert into test_array values (5,'array5',arrayInt(1),arrayChar('e'));
insert into test_array values (6,'array6',arrayInt(1),arrayChar('e'));
insert into test_array values (7,'array6',arrayInt(),arrayChar());
insert into test_array values (8,'array6',null,null);
--------------------------------------------------------------------------
------------------------------test_http-----------------------------------
insert into test_http values (1,'baidu',HTTPURITYPE.createURI('http://www.baidu.com'));
insert into test_http values (2,'wangyi',HTTPURITYPE.createURI('http://www.163.com'));
---------------------------------------------------------------------------
------------------------------test_uri-------------------------------------
insert into test_uri values (1,'baidu',HTTPURITYPE.createURI('http://www.baidu.com'));
insert into test_uri values (2,'wangyi',HTTPURITYPE.createURI('http://www.163.com'));
insert into test_uri values (3,'qq',HTTPURITYPE.createURI('https://mail.qq.com'));
insert into test_uri values (4,'xml1',XDBURITYPE.createURI('/public/hr/doc1.xml'));
insert into test_uri values (5,'xml2',SYS.URIFACTORY.getURI('/public/hr/doc2.xml'));
---------------------------------------------------------------------------
------------------------------test_xdb-------------------------------------
insert into test_xdb values (4,'xml1',XDBURITYPE.createURI('/public/hr/doc1.xml'));
insert into test_xdb values (5,'xml2',XDBURITYPE.createURI('/public/hr/doc2.xml'));
---------------------------------------------------------------------------
------------------------------test_json_clob-------------------------------
insert into test_json_clob values('1',to_clob('{"id":"1","name":"testjson"}'));
insert into test_json_clob values('2',to_clob('{"id":"2","name":"testjson"}'));
insert into test_json_clob values('3',to_clob('{"id":"3","name":"testjson"}'));
insert into test_json_clob values('4',to_clob('{"id":"4","name":"testjson"}'));
insert into test_json_clob values('5',to_clob('{"id":"5","name":"testjson"}'));
---------------------------------------------------------------------------
------------------------------test_json_varchar2---------------------------
insert into test_json_varchar2 values('1','{"id":"1","name":"testjson"}');
insert into test_json_varchar2 values('2','{"id":"2","name":"testjson"}');
insert into test_json_varchar2 values('3','{"id":"3","name":"testjson"}');
insert into test_json_varchar2 values('4','{"id":"4","name":"testjson"}');
insert into test_json_varchar2 values('5','{"id":"5","name":"testjson"}');
---------------------------------------------------------------------------
------------------------------test_long------------------------------------
insert into test_long values('1', 'test_long');
insert into test_long values('2', 'test_long');
insert into test_long values('3', '');
insert into test_long values('4', null);
insert into test_long values('5', 'test_long');
---------------------------------------------------------------------------
------------------------------test_longraw---------------------------------
insert into test_longraw values('1', '0123456789ABCDEF00');
insert into test_longraw values('2', '0123456789ABCDEF00');
insert into test_longraw values('3', '');
insert into test_longraw values('4', null);
insert into test_longraw values('5', '0123456789ABCDEF00');
---------------------------------------------------------------------------
------------------------------test_object----------------------------------
insert into test_object values('1', testObjectType1('testobject1_a1','testobject1_a2',testObjectType2('testobject2_a1','testobject2_a2')));
insert into test_object values('2', testObjectType1('testobject1_a1','testobject1_a2',testObjectType2('testobject2_a1','testobject2_a2')));
insert into test_object values('3', testObjectType1('testobject1_a1','testobject1_a2',testObjectType2('testobject2_a1','testobject2_a2')));
insert into test_object values('4', testObjectType1('testobject1_a1','testobject1_a2',testObjectType2('testobject2_a1','testobject2_a2')));
insert into test_object values('5', testObjectType1('testobject1_a1','testobject1_a2',testObjectType2('testobject2_a1','testobject2_a2')));
---------------------------------------------------------------------------
------------------------------test_raw-------------------------------------
insert into test_raw values('1', '0123456789ABCDEF00');
insert into test_raw values('2', '0123456789ABCDEF00');
insert into test_raw values('3', '');
insert into test_raw values('4', null);
insert into test_raw values('5', '0123456789ABCDEF00');
---------------------------------------------------------------------------
------------------------------test_sdogeometry-------------------------------------
insert into TEST_SDOGEOMETRY values (1,SDO_GEOMETRY(2003,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1003,3),SDO_ORDINATE_ARRAY(1,1, 5,7) ));

insert into TEST_SDOGEOMETRY values (2,SDO_GEOMETRY(2003,NULL,NULL,SDO_ELEM_INFO_ARRAY(1,1003,1),SDO_ORDINATE_ARRAY(5,1, 8,1, 8,6, 5,7, 5,1)));
---------------------------------------------------------------------------
------------------------------xmltest3-------------------------------------
insert into xmltest3 values ('huawei',1,'<archive><manifest><mainClass>org.gauss.Consumer</mainClass></manifest></archive>');

insert into xmltest3 values ('zhongzhi',2, '<descriptorRefs><descriptorRef>jar-with-dependencies</descriptorRef></descriptorRefs>');

---------------------------------操作完成----------------------------------
---------------------------------SUCCESSFUL--------------------------------
commit;
---------------------------------提交完成----------------------------------
---------------------------------SUCCESSFUL--------------------------------