INSERT INTO land_parcels VALUES ('P1', 
  SDO_TOPO_GEOMETRY(
    'CITY_DATA', 
    3, 
    1, 
    SDO_TOPO_OBJECT_ARRAY (
      SDO_TOPO_OBJECT (3, 3), 
      SDO_TOPO_OBJECT (6, 3)
    )
  ) 
);