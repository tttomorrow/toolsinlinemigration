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

#### 运行前准备
启动kafka, debezium
[安装Debeziun](https://debezium.io/documentation/reference/1.5/install.html)

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

配置参数可在resources/consummer_setting.properties中修改，或者自己创建新的properties文件并作为命令行参数传递给程序
```
java -jar OnlineMigration-1.0-SNAPSHOT.jar --schema schema_name --from-beginning --consumer-file-path prop_file_path
```

#### 如何结束
按CTRL+C结束即可。