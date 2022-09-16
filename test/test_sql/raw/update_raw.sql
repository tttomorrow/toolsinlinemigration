update test_raw set content='0123456789ABCDEF001' where id='1';
update test_raw set content='' where id='2';
update test_raw set content='0123456789ABCDEF003' where id='3';
update test_raw set content='0123456789ABCDEF004' where id='4';
update test_raw set content=null where id='5';