Instructions for running BenchmarkSQL on Oracle
---------------------------------------------------


一、下载
1.从github 直接下载源码进行编译安装
git clone https://github.com/petergeoghegan/benchmarksql
2.安装ant 编译工具
yum install -y ant
3.环境配置
鉴于 BenchmarkSQL 是使用Java语言开发的，所以在安装压测工具之前，必须先安装JDK 并且配置JAVA 环境变量
export JAVA_HOME=/usr/local/java/jdk-17.0.2
export CLASSPATH=$:CLASSPATH:$JAVA_HOME/lib/
export PATH=$PATH:$JAVA_HOME/bin

二、配置与使用

1. 创建benchmarksql用户
注意：如果在创建过出站服务器过程中如果创建了用户DEBEZIUM，这里就无需创建以下用户，只需要对其进行授权。
  	CREATE USER debezium
	IDENTIFIED BY "dbz"
	DEFAULT TABLESPACE users
	TEMPORARY TABLESPACE temp;
对以上用户进行授权
GRANT CONNECT TO debezium;
GRANT CREATE PROCEDURE TO debezium;
GRANT CREATE SEQUENCE TO debezium;
GRANT CREATE SESSION TO debezium;
GRANT CREATE TABLE TO debezium;
GRANT CREATE TRIGGER TO debezium;
GRANT CREATE TYPE TO debezium;
GRANT UNLIMITED TABLESPACE TO debezium;

2. 编译BenchmarkSQL源码
    [wieck@localhost ~] $ cd benchmarksql
    [wieck@localhost benchmarksql] $ ant
    Buildfile: /nas1/home/wieck/benchmarksql.git/build.xml

    init:
        [mkdir] Created dir: /home/wieck/benchmarksql/build

    compile:
	[javac] Compiling 11 source files to /home/wieck/benchmarksql/build

    dist:
	[mkdir] Created dir: /home/wieck/benchmarksql/dist
	  [jar] Building jar: /home/wieck/benchmarksql/dist/BenchmarkSQL-6.devel.jar
    BUILD SUCCESSFUL
    Total time: 1 second
    [wieck@localhost benchmarksql] $

3. 创建benchmark配置文件

    [wieck@localhost benchmarksql] $ cd run
    [wieck@localhost run] $ cp sample.postgresql.properties my_oracle.properties
    [wieck@localhost run] $ vi my_oracle.properties
    配置文件见my_oracle.properties

4.创建脚本文件来执行sql.oracle中的文件sql文件（详情见对应文件文件）
脚本文件：
   runDatabaseCreate.sh
   runDatabaseInsert.sh
   runDatabaseUpdate.sh
   runDatabaseDelete.sh
   runDatabaseDrop.sh
sql文件：
   dataDelete.sql
   storedProcedureCreates.sql
   storedProcedureDrops.sql
   tableCreates.sql
   tableDrops.sql
   typeCreates.sql
   updateTableData.sql
注意：命名冲突请自己修改

5. 用以下命令执行sql操作
   同时向数据库中进行多条DDL操作和DML操作

    [wieck@localhost run]$ ./runDatabaseCreate.sh my_oracle.properties
    //创建自定义相关数据类型和测试所用的多个表

    [wieck@localhost run]$ ./runDatabaseInsert.sh my_oracle.properties
    //向各个表中一次性插入多条数据

    [wieck@localhost run]$ ./runDatabaseUpdate.sh my_oracle.properties
    //向各个表中一次性修改多条数据

    [wieck@localhost run]$ ./runDatabaseDelete.sh my_oracle.properties
    //向各个表中一次性删除多条数据

    [wieck@localhost run]$ ./runDatabaseDrop.sh my_oracle.properties
    //删除对应的表及其相关的数据类型
 


