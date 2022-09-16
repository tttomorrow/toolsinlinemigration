update test_longraw set content='0123456789ABCDEF0011111' where id='1';
update test_longraw set content='' where id='2';
update test_longraw set content='0123456789ABCDEF0033333' where id='3';
update test_longraw set content='0123456789ABCDEF0044444' where id='4';
update test_longraw set content=null where id='5';