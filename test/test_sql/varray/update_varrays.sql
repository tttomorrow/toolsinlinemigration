update test_array set array_int=arrayInt(1,1,1,1,1) where id=8;
update test_array set id=6 where id=2;
update test_array set name='test_array' where id=3;
update test_array set array_char=arrayChar('f','ff') where id=4;
update test_array set array_char=arrayChar('g','gg') where id=5;

update test_array_uri set array_uri=arrayUri(HTTPURITYPE.createURI('http://www.mail.qq.com')) where id=9;