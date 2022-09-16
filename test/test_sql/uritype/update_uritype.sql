update test_uri set web=HTTPURITYPE.createURI('http://12345.com') where id=1;
update test_uri set id=6 where id=2;
update test_uri set name='zhang' where id=6;
update test_uri set web=SYS.URIFACTORY.getURI('/public/hr/doc3.xml') where id=3;
update test_uri set web=XDBURITYPE.createURI('/public/hr/doc6.xml') where id=4;
update test_uri set web=HTTPURITYPE.createURI('http://www.baidu.com') where id=5;
