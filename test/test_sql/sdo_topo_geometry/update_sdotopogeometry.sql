update land_parcels set feature_name = 'P2' where feature_name = 'P1';

update land_parcels set feature=SDO_TOPO_GEOMETRY(
    'CITY_DATA', 
    3,
    1,
    SDO_TOPO_OBJECT_ARRAY (
      SDO_TOPO_OBJECT (4, 3), 
      SDO_TOPO_OBJECT (7, 3)
    )
) where feature_name = 'P2';