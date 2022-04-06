环境搭建概要步骤：

1、准备mysql数据库，创建名称为“bizzan”的数据库
2、准备redis缓存数据库
3、准备kafka流式处理环境（先配置运行zookper，接着配置运行kafka）
4、准备mongodb数据库环境，创建用户admin、bizzan，创建bitrade数据库
5、准备nginx，修改配置文件
6、修改framework代码中的配置文件为准备环境配置参数
7、编译生成jar可执行文件
8、运行cloud.jar（微服务注册中心）
9、运行market.jar（行情中心）
10、运行exchange.jar（交易中心）
11、运行ucenter.jar（用户中心）
12、运行其他模块
13、打开mysql，导入framework代码中的sql文件夹中xxxxxxx.sql文件，注意，trigger的sql如果报错，需要针对wallet表添加trigger
14、运行前端vue项目
15、运行后端vue项目
16、运行钱包


注意事项：
当内存不足时，在linux控制台输入top可以查看java进程占用了大量内存（一个java进程占用1G以上），因为有很多jar包需要运行，所以需要控制某些jar包使用的内存，目前控制以下4个：

java -jar -Xms512m -Xmx512m -Xmn200m -Xss256k  admin-api.jar
java -jar -Xms512m -Xmx512m -Xmn200m -Xss256k  cloud.jar
java -jar -Xms512m -Xmx512m -Xmn200m -Xss256k  wallet.jar
java -jar -Xms512m -Xmx512m -Xmn200m -Xss256k  activity.jar

# 配置文件：
`application.properties`

该文件位于每个java模块的resource目录下

有一个总的配置文件，所有jar包启动运行时带上 `-Dspring.config.location=XXX/application.properties`

# 修改内容：
```
eureka.client.serviceUrl.defaultZone=http://172.19.0.8:7000/eureka/
```

> 这个配置的IP地址改成Cloud.jar文件运行的服务器所在IP地址即可

```
spring.datasource.url=jdbc:mysql://172.19.0.5:3306/bizzan?characterEncoding=utf-8&serverTimezone=GMT%2B8&useSSL=false
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.username=bizzan
spring.datasource.password=123456789
```

> 数据库的配置

```
spring.kafka.bootstrap-servers=172.19.0.6:9092
Kafka的IP配置，改个IP地址的事

spring.data.mongodb.uri=mongodb://bizzan:123456@172.19.0.6:27017/bitrade
spring.data.mongodb.database=bitrade
```

> MongoDB的配置

```
sms.driver=diyi
sms.gateway=
sms.username=111111
sms.password=xxxxxxxxxxxxxxxxxx
sms.sign=BBEX
sms.internationalGateway=
sms.internationalUsername=
sms.internationalPassword=
```

# Java微服务运行

## 第一台：WEB服务等 内网：172.22.0.13

运行内容：前端 (www.xxxx.com / api.xxxx.com) / Nginx(SSL证书解析)
Nginx路径：/usr/local/nginx/html

## 第二台：交易引擎等 微服务 内网：172.22.0.3

运行内容1：cloud.jar / exchange.jar / market.jar / ucenter.jar / exchange-api.jar / wallet.jar
运行内容2：nginx微服务转发(/usr/share/nginx/html)
注意：exchange.jar 和 market.jar只能有一个实例存在，不可做分布式/负载均衡。

## 第三台：Kafka/DB/Redis数据库服务等 内网：172.22.0.12

运行内容1：zookeeper / kafka / mongodb / redis
运行内容2：admin.jar / admin前端资源（/usr/share/nginx/html）
运行内容3：er_robot_market.jar / er_robot_normal.jar

## 第四台：第五台：MySQL云服务器

运行内容：业务数据库

# Jar微服务启动顺序

1、cloud.jar - 微服务注册中心

2、exchange.jar - 撮合引擎（需等待cloud完全启动）

3、market.jar - 行情引擎（需等待exchange完全启动）

4、xxx.jar（其他jar包，需等待market完全启动），如下

- exchange-api.jar·······························币币委托接口服务
- ucenter-api.jar·································用户中心接口服务
- chat.jar··········································OTC聊天接口服务
- otc-api.jar·······································OTC委托接口服务
- wallet.jar········································钱包服务
- agent-api.jar····································代理商接口服务
- contract-swap-api.jar·························永续合约服务
- contract-option-api.jar························期权合约服务

# 运行命令带上配置文件

```shell
java -Xms512m -Xmx512m -Xmn255m -Xss256k -server -XX:+HeapDumpOnOutOfMemoryError -jar -Dspring.config.location=XXX/application.properties XXX.jar
```

# 端口情况

```shell
28901 market.jar
28902 chat.jar
28903 chat.jar
38985 contract-swap-api
6001 ucenter-api
6003 exchange-api
6004 market.jar
38901 contract-swap-api
6005 exchange
6006 otc-api
6008 chat
7000 cloud
6009 wallet
28985 market    
6010 admin-api
6011 agent-api
6012 contract-swap-api
```