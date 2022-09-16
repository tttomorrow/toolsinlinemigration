--------------------------------------------------------------
--------------------------table Create -----------------------
-------------------------------LONG---------------------------
create table test_long(
id char(3) primary key ,
content LONG);

-----------------------------LONG RAW-------------------------
create table test_longraw(
id char(3) primary key , 
content LONG RAW);

------------------------------ RAW----------------------------
create table test_raw(
id char(3) primary key , 
content RAW(100));

------------------------JSON (VARCHAR2, CLOB, BLOB)-----------
create table test_json_varchar2(
id char(3) primary key , 
content varchar2(100) CONSTRAINT test_json_varchar2_ensure_json CHECK (content IS JSON)); 
-- (before CONSTRAINT) ORA-00906: missing left parenthesis

create table test_json_clob(
id char(3) primary key , 
content CLOB CONSTRAINT test_json_clob_ensure_json CHECK (content IS JSON));
-- create table test_json_blob(id char(3) primary key, content BLOB CONSTRAINT test_json_blob_ensure_json CHECK (content IS JSON));

--------------------------------XML-------------------------
create table xmltest3 (
name varchar(32) primary key ,
id number ,
doc xmltype);

------------------------------OBJECT-------------------------
-- create testObjectType first!
create table test_object(
id char(3) primary key , 
content testObjectType1);

--------------------------SDO_GEOMETRY-----------------------
create table TEST_SDOGEOMETRY(
 id NUMBER primary key  ,
 content SDO_GEOMETRY
);

-------------------------------VARRAY-----------------------
-- create testVarrayType first!
create table test_array(
id int primary key ,
name varchar2(20),
array_int arrayInt,
array_char arrayChar
);

-------------------------------URITYPE-----------------------
create table test_uri(
id int primary key ,
name varchar2(20),
web uritype
);


-----------------------------HTTPURITYPE---------------------
create table test_http(
id int primary key  ,
name varchar2(20),
web httpuritype
);


------------------------------XDBURITYPE---------------------
create  table test_xdb(
id int primary key  ,
name varchar2(20),
web xdburitype
);


-------------------------------------------------------------
---------------------table Create SUCCESSFUL-----------------

