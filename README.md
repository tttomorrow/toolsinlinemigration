# OnlineMigration

#### 介绍
OnlineMigration是基于[Debezium](https://debezium.io/)的在线迁移工具，当前只支持Oracle迁移到openGauss。

#### 编译
```
mvn compile
```

#### 打包
```
mvn package
```

生成的jar包位于**target/**下。

#### 运行前准备
配置oracle、启动Kafka等运行前准备操作详见instruction目录下的[PREPARATION](instruction/PREPARATION.md)

#### 如何运行
参数说明：
```
--write-scn: write scn captured in debezium snapshot mode.
--schema: the schema want to migrate
--from-beginning: consume the messages from beginning.
--consumer-file-path: the application property file
--help: print the help message
```

读取做快照时的scn并写入到scn.txt
```
java -jar OnlineMigration-1.0-SNAPSHOT.jar --write-scn
```

从topic起始端开始消费
```
java -jar OnlineMigration-1.0-SNAPSHOT.jar --schema schema_name --from-beginning
```

从上次消费的topic偏移量处继续消费
```
java -jar OnlineMigration-1.0-SNAPSHOT.jar --schema schema_name
```

使用智能对象名称转换功能:
```
java -jar OnlineMigration-1.0-SNAPSHOT.jar --smartConversionOfObjectNames
```

当开启智能对象转义功能后,onlineMigration会对对象名做如下处理:

| oracle   | openGauss |
|----------|-----------|
| Object_a | Object_a  |
| OBJECT_A | object_a  |
| object_a | object_a  |

配置参数可在resources/consummer_setting.properties中修改后重新编译，或者自己创建新的properties文件并作为命令行参数传递给程序
```
cp src/main/resources/consumer_setting.properties ./my_consumer_setting.properties
# 修改my_consumer_setting.properties中的相关配置参数，然后启动
java -jar OnlineMigration-1.0-SNAPSHOT.jar --schema schema_name --from-beginning --consumer-file-path my_consumer_setting.properties
```

#### 如何结束
按CTRL+C结束即可。