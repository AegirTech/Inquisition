java -Duser.timezone=Asia/Shanghai -jar Inquisition-1.2.8.jar
# 自行部署
如果你喜欢折腾，或者你的服务器不支持docker，那么你可以选择自行部署。
请享受环境部署的快乐
本文档将提供基于`windows 11 22H2`的完整快速部署流程，通过本文，您可以实现龟速在服务器上配置Inquisition

# 环境准备
## OPENJDK 11
请自行下载并安装`openjdk 11`，并配置环境变量（多数安装程序会自行配置）
国内用户推荐使用[微软的构建](https://learn.microsoft.com/zh-cn/java/openjdk/download#openjdk-11)
Windows可以直接点[这里](https://aka.ms/download-jdk/microsoft-jdk-11.0.21-windows-x64.msi)下载安装包

## mysql8 安装

TODO
请自行查阅教程，注意版本号，本项目所使用的utf8_general_ci不支持mysql5
注意：Inquisition需配置所用数据表为character-set-server=utf8mb4 collation-server=utf8mb4_general_ci

## Inquisition安装
1.挑一个心仪的目录，从release内下载Inquisition-1.2.8.jar，放入目录内。
2.在目录内新建一个文件夹，命名为`config`，从release内下载`application.yml`，放入`config`文件夹内。
3.编辑`application.yml`，修改数据库配置
```yaml

```