本文档主要介绍如何配置Oracle和Debezium。

# 配置Oracle

setup-xstream.sh脚本用于启动Oracle XStream服务。

该脚本需要使用sys用户执行（设置期间会关闭和重启数据库），用户需修改脚本中sqlplus连接的sys用户的密码。脚本创建了XStream admin user（c##dbzadmin）、XStream user（c##dbzuser）、test user（debezium）、XStream Outbound server（dbzxout），用户可根据需要自行修改用户名和配置。

相关语句修改如下：

```
# 修改前
sqlplus sys/top_secret@//localhost:1521/ORCLCDB as sysdba <<- EOF
# 修改后
sqlplus sys/your_passwd@//localhost:1521/ORCLCDB as sysdba <<- EOF

# 若忘记密码，可通过如下方式设置sys用户密码
sqlplus / as sysdba
SQL>ALTER USER SYS IDENTIFIED BY "your_passwd";
SQL>exit;
```

使用oracle用户执行该脚本配置Oracle(否则可能出现ORA-12547: TNS:lost contact)

```
sh setup-xstream.sh
```

使用我们在脚本中创建的**debezium**用户连接上**ORCLPDB1**，对需要监听的表赋予权限，只有赋权过的表才能被捕获到更新。

```
# 假设schema中已有表customers，其建表语句如下
CREATE TABLE customers (
  id NUMBER(4) NOT NULL PRIMARY KEY,
  first_name VARCHAR2(255) NOT NULL,
  last_name VARCHAR2(255) NOT NULL,
  email VARCHAR2(255) NOT NULL
);

# 连接数据库对表进行赋权
sqlplus debezium/dbz@//localhost:1521/ORCLPDB1
SQL>GRANT SELECT ON customers to c##dbzuser;
SQL>ALTER TABLE customers ADD SUPPLEMENTAL LOG DATA (ALL) COLUMNS;
SQL>exit;
```

# 下载Debezium

```
# 下载解压官网编译好的debezium-connector-oracle
wget -c https://repo1.maven.org/maven2/io/debezium/debezium-connector-oracle/1.5.0.Final/debezium-connector-oracle-1.5.0.Final-plugin.tar.gz
tar -zxf debezium-connector-oracle-1.5.0.Final-plugin.tar.gz
```

至此，我们获得了**debezium-connector-oracle**

# 配置Kafka

下载**Kafka**（本例程使用2.8.0版本，用户可根据需要自行选择版本）

```
wget -c https://mirrors.tuna.tsinghua.edu.cn/apache/kafka/2.8.0/kafka_2.13-2.8.0.tgz
tar -zxf kafka_2.13-2.8.0.tgz
```

下载**Oracle Instant Client**（x86与arm平台需要下载不同的包）

```
# x86_64
wget -c https://download.oracle.com/otn_software/linux/instantclient/211000/instantclient-basic-linux.x64-21.1.0.0.0.zip
unzip instantclient-basic-linux.x64-21.1.0.0.0.zip
mv instantclient_21_1 oracle_instant_client

# aarch64
wget -c https://download.oracle.com/otn_software/linux/instantclient/191000/instantclient-basic-linux.arm64-19.10.0.0.0dbru.zip
unzip instantclient-basic-linux.arm64-19.10.0.0.0dbru
mv instantclient_19_10 oracle_instant_client

# 将需要的Oracle JDBC和XStream jar包复制到Kafka库下
cp oracle_instant_client/xstreams.jar oracle_instant_client/ojdbc8.jar kafka_2.13-2.8.0/libs

# 将该库路径添加到环境变量文件中
echo "export LD_LIBRARY_PATH=your_work_dir/oracle_instant_client:$LD_LIBRARY_PATH" >> ~/.bashrc
source ~/.bashrc
```

设置**Kafka-connect**

```
mkdir kafka_2.13-2.8.0/connect
# 将上文中的debezium-connector-oracle移动到connect目录下
mv debezium-connector-oracle kafka_2.13-2.8.0/connect
# 设置kafka-connect的插件路径
echo "plugin.path=you_work_dir/kafka_2.13-2.8.0/connect" >> kafka_2.13-2.8.0/config/connect-distributed.properties
```

# 启动Kafka和Debezium

启动**Zookeeper**

```
cd kafka_2.13-2.8.0
./bin/zookeeper-server-start.sh ./config/zookeeper.properties
```

另开一个终端启动**Kafka**

```
cd kafka_2.13-2.8.0
./bin/kafka-server-start.sh ./config/server.properties
```

另开一个终端启动Kafka-connect

```
cd kafka_2.13-2.8.0
./bin/connect-distributed.sh ./config/connect-distributed.properties
```

注册**debezium-oracle-connector**（用户需修改json文件里的相关参数值，参数的含义见Debezium官网）

```
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" http://localhost:8083/connectors/ -d @register-oracle-xtreams.json
```

# 附录

查看Kafka有哪些topic

```
./bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

查看Kafak topic中的内容（xxx为要查看的topic名称）

```
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic xxx --from-beginning
```

如果要重新配置XStream服务，请参见脚本[alter-xstream.sh](alter-xstream.sh)。