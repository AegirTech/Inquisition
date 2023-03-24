# 快速部署

出于易用性考虑，Inquisition 提供了docker部署服务，您可以使用docker快速部署 Inquisition 和所需要的 MySQL 环境，免去了环境配置的麻烦，本文档将提供基于`Ubuntu 20.04`的完整快速部署流程，`Windows`环境请使用 [Docker Desktop](https://www.docker.com/get-started/) ，通过本文，您可以实现快速在服务器上配置Inquisition

## Docker安装

快速部署服务依赖于 docker

```shell
# 一键安装命令
curl -sSL https://get.daocloud.io/docker | sh
```

## MySQL安装

由于docker容器自动销毁，我们需要将数据库文件挂载到本地

```shell
# 创建本地挂载路径
mkdir -p /usr/local/mysql/conf
mkdir -p /usr/local/mysql/data
mkdir -p /usr/local/mysql/logs
```

设置配置文件

```shell
vim /usr/local/mysql/conf/my.cnf
```

按`i`进入编辑模式，将以下内容粘贴进去

```
[client]
default-character-set = utf8mb4

[mysqld]
pid-file        = /var/run/mysqld/mysqld.pid
socket          = /var/run/mysqld/mysqld.sock
datadir         = /var/lib/mysql
secure-file-priv= NULL
# Disabling symbolic-links is recommended to prevent assorted security risks
symbolic-links=0

# Custom config should go here
# 字符集
character_set_server=utf8
collation-server=utf8_general_ci

# 是否对sql语句大小写敏感，1表示不敏感
lower_case_table_names = 1

# 最大连接数
max_connections = 1000

# Innodb缓存池大小
innodb_buffer_pool_size = 4G

# 表文件描述符的缓存大小
table_open_cache_instances=1
table_open_cache=2000
table_definition_cache=2000

!includedir /etc/mysql/conf.d/
```

按`Esc`，输入`:wq`保存

创建多容器通信网络

```shell
docker network create aegirtech-net
```

拉取数据库镜像

```shell
docker run -p 3306:3306 --name inquisition-mysql \
--network aegirtech-net \
--network-alias inquisition-mysql \
-v /usr/local/mysql/conf/my.cnf:/etc/mysql/my.cnf \
-v /usr/local/mysql/logs:/logs \
-v /usr/local/mysql/data/mysql:/var/lib/mysql \
-e MYSQL_ROOT_PASSWORD=123456 \
-d mysql:8 \
--character-set-server=utf8mb4 \
--collation-server=utf8mb4_general_ci \
--default_authentication_plugin=mysql_native_password
```

至此，数据库配置已经全部完成

## Inquisition安装

同样使用docker，仅使用默认配置的话，只需要执行

```shell
docker run -d -p 2000:2000 --name inquisition --network aegirtech-net dazecake/inquisition:latest
```

访问`http://服务器IP:2000/swagger-ui/index.html`检查是否部署成功

同时可以在`管理员登陆接口`使用默认账号登陆检查是否成功连接至数据库

默认账号: `root`

默认密码: `123456`

成功将返回的响应

```json
{
  "code": 200,
  "msg": "login success",
  "data": {
    "token": "xxxxxxxxxxxxxxx"
  }
}
```

如果以上测试均成功，恭喜你，你已经正确的安装了Inquisition与其所需依赖的数据库

## 进阶

### 前端部署

单纯的后端部署完成后无法正常使用，您需要部署前端以获取图形化的操作界面

Inquisition 的前端实现为 [IberiaEye 伊比利亚之眼](https://github.com/AegirTech/IberiaEye)


### 自定义配置

默认的docker容器使用默认配置，如果您需要开启邮件推送或修改其他任意设置，需要进行[目录挂载](https://docker.easydoc.net/doc/81170005/cCewZWoN/kze7f0ZR)

创建自定义配置文件 [配置文件参考](https://github.com/AegirTech/Inquisition/blob/main/src/main/resources/application.yml)

```shell
vim /usr/local/inquisition/config/application.yml
```

编辑并保存，停止原先运行的容器，增加启动参数

```shell
docker run -d -p 2000:2000 --name inquisition --network aegirtech-net \
-v /usr/local/inquisition/config:/config \
dazecake/inquisition:latest
```

此时 Inquisition 将以自定义配置运行

### 升级

```shell
# 停止并删除旧容器
docker stop inquisition
docker rm inquisition

# 更新容器
docker pull dazecake/inquisition:latest

# 重新运行
docker run -d -p 2000:2000 --name inquisition --network aegirtech-net \
-v /usr/local/inquisition/config:/config \
dazecake/inquisition:latest
```