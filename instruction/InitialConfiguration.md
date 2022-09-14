# 初始化配置文件说明文档

**请务必在进行配置前详细阅读此文档！**

## 一. Oracle端配置XSTREAM

可以 [`PREPARATION.md`](PREPARATION.md) 文档进行XSTREAM的配置，使用脚本进行快速配置，在阅读文档的过程中，建议打开 [`setup-xstream.sh`](setup-xstream.sh) 脚本进行对照。如果配置过程中出现任何问题，点开配置脚本进行Oracle安装路径和账户密码以及具体Oracle容器名称的修改，并且熟记脚本中为Oracle创建的三个账户以及密码。

1.  c##dbzadmin   xsa  账户为xtream组件管理员账户，主要用来进行xstream进程的启停，重新配置等，可结合PREPARATION.md文档和Oracle官方文档进行操作。注意，Oracle数据库重启之后，必须重新启动XTREAM进程才可以进行捕获，以c##dbzadmin或者SYS连接Oracle CDB根容器，启动命令如下：

    ```sql
    BEGIN
      DBMS_XSTREAM_ADM.START_OUTBOUND(
        server_name => 'dbzxout');--默认在脚本中配置的出站服务器名称
    END;
    /
    ```

2.  c##dbzuser  dbz 账户为debezium连接器连接Oracle数据库的账户，注意， 必须在待迁移的表所在的容器中为此账户设置权限才可以进行捕获，例如，orclpdb1中有一张表customers待迁移，以管理员账户连接此容器，授权语句如下

    ```sql
    GRANT SELECT ON customers to c##dbzuser;
    ```

    如迁移过程中涉及权限问题较多，可直接在指定容器下为此账户设置DBA权限

3.  debezium dbz 此账户为pdb容器下的账户，连接器账户 c##dbzuser以XTREAM方式捕获此模式下的表，在为c##dbzuser账户授权的前提下，此模式下所有表都将会被xstream捕获。如果oracle数据库中已有待迁移的模式存在，则应该在配置XSTREAM的脚本中，创建XTREAM出站服务器，即Create XStream Outbound server下的代码块中修改模式名称，具体如下：

    ```sql
    sqlplus c##dbzadmin/xsa@//localhost:1521/ORCLCDB <<- EOF
      DECLARE
        tables  DBMS_UTILITY.UNCL_ARRAY;
        schemas DBMS_UTILITY.UNCL_ARRAY;
      BEGIN
        tables(1)  := NULL;
        schemas(1) := 'debezium';--将此处修改为待迁移的表所在的模式
        DBMS_XSTREAM_ADM.CREATE_OUTBOUND(
          server_name     =>  'dbzxout',
          table_names     =>  tables,
          schema_names    =>  schemas);
      END;
      /

      exit;

      EOF
    ```

    并且在模式所在的容器中为 c##dbzuser授权。

至此，XSTREAM配置完成。

## 二. debezium连接器配置

参考 [`PREPARATION.md`](PREPARATION.md) 文档安装好kafka并且下载好debezium-connector-oracle后，需要进行debezium连接器的配置，在参考 [`register-oracle-xtreams.json`](register-oracle-xtreams.json) 配置文件的基础上，并参考以下连接器配置:

```json
{
  "name": "demo-connector",
  "config": {
    "connector.class": "io.debezium.connector.oracle.OracleConnector",
    "tasks.max" : "1",
    "database.server.name": "server1", //自定义的服务名
    "database.hostname": "", //Oracle数据库IP地址
    "database.port": "1521", //Oracle数据库端口
    "database.user": " c##dbzuser", //连接Oracle的账户，使用在脚本中创建的用户
    "database.password": "dbz", //账户密码，脚本创建的为dbz
    "database.dbname": "ORCLCDB", //CDB容器
    "database.pdb.name": "ORCLPDB1", //PDB容器
    "schema.include.list": "debezium", //需要捕获的schema，可以修改为自己需要迁移的schema，但是要和创建出站服务器时配置的相同
    "lob.enabled":"true", //默认启用
    "database.connection.adapter": "xstream", //以XSTREAM方式连接，此处不应做修改
    "database.out.server.name" : "dbzxout", //脚本创建的出站服务器名称
    "database.history.kafka.bootstrap.servers": "127.0.0.1:9092", //kafka broker地址，本地则不用修改
    "database.history.kafka.topic": "oracle.history", //不用修改
    "snapshot.mode": "schema_only", //设置快照模式，必须为schema_only，仅进行增量迁移
    "provide.transaction.metadata": "true", //默认开启
    "transforms": "route", //不用修改
    "transforms.route.type": "org.apache.kafka.connect.transforms.RegexRouter", //不用修改
    "transforms.route.regex":"([^.]+)\.([^.]+)\.([^.]+)", //不用修改
    "transforms.route.replacement":"1.$2.ALL_TABLES" //不用修改
     //最后4项参数是为了在不修改debezium源码的前提下，配置all_tables的topic，不应该修改
  }
}
```

在上述配置文件中，特别需要注意，

`"database.connection.adapter": "xstream"`，  
`"database.out.server.name" :"dbzxout"`，  
`"snapshot.mode": "schema_only"`，  
`"schema.include.list": "debezium"`，

这几项配置参数，要以XSTREAM方式连接数据库，并且要指定正确待迁移的表所在模式，初始化快照模式为schema_only，配置正确出站服务器名称，即在脚本中创建的 dbzxout 。

目前 在配置logminger连接的情况下是long类型可以迁移到opengauss，而long raw,raw，xmltype，arrays，object，uritype，httpuritype，xdburitype，sdo_geometry,sdo_topo_geometry，json需要用xstream方式接入才可以迁移到opengauss；

此外，自定义连接器配置参数可以参考 [Debezium官网](https://debezium.io/documentation/reference/nightly/connectors/oracle.html)。

至此，连接器配置完成。

## 三. onlineMigration端配置文件的修改

在启动迁移之前，需要对onlineMigration的配置文件进行部分修改，配置文件在 `/src/main/resources/consumer_setting.properties`，具体说明如下：

```properties
bootstrap.servers=localhost:9092 //kafka broker所在地址，如果在本地则维持默认就好
enable.auto.commit=False //自动提交，默认就好
auto.commit.interval.ms=1000 //自动提交间隔1秒，默认就好
key.deserializer=org.apache.kafka.common.serialization.StringDeserializer //键反序列化类，默认
value.deserializer=org.apache.kafka.common.serialization.StringDeserializer //值反序列化类，默认
scnfile=scn.txt //Oracle的scn文件，默认
database.server.name=server1 //此处为在debezium连接器配置文件中自定义的服务名，必须保持一致
group.id=consumer //groupID，此处为kafka消费者的groupID，维持默认就好
database.driver.classname=org.opengauss.Driver //JDBC驱动，不用修改
database.url=jdbc:opengauss://127.0.0.1:15630/mydatabase?stringtype=unspecified //opengauss数据库URL
database.user=opengauss //opengauss用户
database.password=opengauss@123 //opengauss用户密码
```

需要注意，必须在 `database.url` 的最后添加 `?stringtype=unspecified` 以启用JDBC自动格式转换机制，目前，考虑仅LONG类型迁移成TEXT类型时不需要添加。
而long raw,raw，xmltype，arrays，object，uritype，httpuritype，xdburitype，sdo_geometry,sdo_topo_geometry，json类型迁移到opengauss时需要添加 ?stringtype=unspecified。
