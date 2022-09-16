--------------------------------------------------------------
--------------------------type Create ------------------------

create type arrayChar as varray(5) of varchar2(50);


create type arrayInt as varray(5) of int;


create type arrayUri as varray(5) of uritype;

create type testObjectType2 as object (
    a1 VARCHAR2(50),
    a2 VARCHAR2(50)
);

create type testObjectType1 as object (
    a1 VARCHAR2(50),
    a2 VARCHAR2(50),
    obj2 testObjectType2
);


--------------------------------------------------------------
---------------------type Create SUCCESSFUL-------------------