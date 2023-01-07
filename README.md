# Inquisition 审判庭

明日方舟速通云控后端

## 什么是 Inquisition 审判庭

Inquisition 是一款基于 [ArkLights](https://github.com/tkkcc/ArkLights)
开发的云控后端程序，是集成了多账号多终端负载均衡，数据管理，任务分发，消息推送，日志等等功能于一体的强大后端管理程序，使用
Inquisition 可以轻松管理数百个账号与设备。

## 组织架构

![](https://fastly.jsdelivr.net/gh/DazeCake/image-host/blogaeirtech_structure.png)

## 前端实现

[伊比利亚之眼](https://github.com/AegirTech/IberiaEye)

## 如何使用

请参考 [快速部署](doc/FastDeploy.md) 文档

一般用户可以使用由项目组提供支持的的付费 [在线托管服务](http://ark.aegirtech.com/)
，此服务将会提供最优先甚至领先于release版本的服务与支持，亦是对开发的支持。

## 近闻

咕咕了这么久终于要迎来正式的`v1.0.0`版本了，在销声匿迹的这段时间里（其实就是单纯考试太忙了），审判庭添加了相当多的功能，因为一些原因，期间的commit记录有所丢失，故在此标注

- 华云（Chinac）对接与动态设备增补管理
- 新的反`bilibili account`风控策略
- 在线支付实现
- 伊比利亚之眼admin端开发（是的，终于要有可用的前端了）
- 数据库结构优化
- 性能优化

预计在年底`v1.0.0`将正式上线，这会是一个**完备的**，**可投入生产使用的**，**交互友好的**
版本，且根据[GPL-3.0 license](https://github.com/AegirTech/Inquisition/blob/main/LICENSE)，你可以自由的将其投入到商业使用中

