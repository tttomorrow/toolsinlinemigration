# drop XStream Outbound server
sqlplus c##dbzadmin/xsa@//localhost:1521/ORCLCDB <<- EOF
	BEGIN
	  DBMS_XSTREAM_ADM.DROP_OUTBOUND(
	    server_name   =>  'dbzxout');
	END;
	/

	exit;
EOF

# create XStream Outbuound server
sqlplus c##dbzadmin/xsa@//localhost:1521/ORCLCDB <<- EOF
	DECLARE
	  tables  DBMS_UTILITY.UNCL_ARRAY;
	  schemas DBMS_UTILITY.UNCL_ARRAY;
	BEGIN
	    tables(1)  := NULL;
	    schemas(1) := 'debezium';
        schemas(2) := 'new_schema';
	  DBMS_XSTREAM_ADM.CREATE_OUTBOUND(
	    server_name     =>  'dbzxout',
	    table_names     =>  tables,
	    schema_names    =>  schemas);
	END;
	/

	exit;
EOF

# Alter XStream to XStream user
sqlplus sys/top_secret@//localhost:1521/ORCLCDB as sysdba <<- EOF
    BEGIN
        DBMS_XSTREAM_ADM.ALTER_OUTBOUND(
        server_name  => 'dbzxout',
        connect_user => 'c##dbzuser');
    END;
    /

    exit;
EOF