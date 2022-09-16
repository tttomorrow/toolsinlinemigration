update TEST_SDOGEOMETRY set id = 3 where id = 1;

update TEST_SDOGEOMETRY set content= SDO_GEOMETRY(
    2003,  
    NULL,
    NULL,
    SDO_ELEM_INFO_ARRAY(1,1003,4), 
    SDO_ORDINATE_ARRAY(8,7, 10,9, 8,11)
  )
where id = 2;