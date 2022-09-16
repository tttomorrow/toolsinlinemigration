create type testObjectType1 as object (
    a1 VARCHAR2(50),
    a2 VARCHAR2(50),
    obj2 testObjectType2
);
/