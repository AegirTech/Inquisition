# Inquisition 审判庭
明日方舟速通云控后端

## 如何使用

> **警告: 本项目正处于 Fast Moving 阶段。任何数据库结构变化和API变动将不会提前通知。强烈建议您等待 release 版本。若执意运行，请确保自己拥有一定的纠错能力并友善的提出issue，我们将很乐意为您解答。**

### 数据库配置

```sql
# 请使用 Mysql8
# 创建数据库
CREATE DATABASE inquisition;
# 选择数据库
USE inquisition;
# 运行 sql file 位于仓库中sql文件夹内
SOURCE /xxx/xxx/arklights.sql
# 写入 admin 记录 其中password的加密为 MD5(明文密码+arklightscloud)
# 例如创建一个用户名为root 密码为123456的管理员账号
INSERT INTO `admin`(`id`, `user_name`, `password`, `permission`, `notice`, `delete`) VALUES (1, 'root', '7966fd2201810e386e8407feaf09b4ea', 'root', '{}', 0);
```

### 编译

```shell
git clone https://github.com/AegirTech/Inquisition.git
cd Inquisition
./gradlew bootJar # 编译jar包至./build/libs
cp ./build/libs/xxx.jar ./
java -jar xxx.jar # 请使用java 11+
```



### 配置文件

```yaml
# yaml配置，请在jar包同级目录创建 application.yml 

# 启动端口
server:
  port: 2000
  # 启用证书
  ssl:
    key-store: /xxx/xxx/xxx.keystore
    key-store-type: PKCS12
    key-store-password: xxxxxx
    enabled: false
    
spring:
  # 数据库连接配置 Mysql 8+
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/inquisition?characterEncoding=UTF-8
    username: root
    password: 123456
  # 邮箱推送配置
  mail:
    enable: true
    host: smtp.qq.com
    port: 465
    protocol: smtps
    username: xxxxx@qq.com
    # 部分邮箱password为授权码 如qq邮箱
    password: xxxxxx
    from: xxxxx@qq.com
    to: xxxxx@qq.com
# 每日任务刷新cron表达式
cron: '0 0 4,12,20 * * ?'

# sql日志
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl
```

### 前端部署

请确保已经部署证书开启https，后访问 [http://aegirtech.com](http://aegirtech.com/) 填写后端地址并登陆即可

## API文档

部署后[访问](http://127.0.0.1:2000/swagger-ui/index.html)

## TODO

- [x] 心跳协议
- [x] Task拉取
- [x] ~~多设备管理~~ 负载均衡
- [x] Admin管理
- [x] 日志
- [ ] 前端实现 --> 当前开发
- [ ] 截图获取
- [ ] User管理
- [x] 通知

## 低优先度TODO

- [ ] CDK
