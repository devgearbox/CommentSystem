# 荔枝批发管理系统

本项目是使用 Spring Boot 框架开发的一个前后端不分离单体电商批发管理系统

## 技术栈

- 后端框架
  - SpringBoot (3.5.3)
  - JPA
- 前端
  -Thymeleaf
- 数据库
  - MySql
- 前后端通信
  - RESTful API

## Windows 开发环境搭建

1. 安装 Java JDK 17 并配置环境变量
2. 安装 MySQL 数据库并创建相应数据库

   - 创建 MySQL 数据库与表: 运行 [commentsystem.sql](./lizhi/commentsystem.sql)

3. 安装 Maven 构建工具

4. 克隆项目到本地 `git clone https://github.com/devgearbox/CommentSystem.git `

5. 修改配置文件 [application.yml](./lizhi/src/main/resources/application.properties)

   ```properties
   spring:
     datasource:
       username: root
       password: 数据库密码
   ```

6. 运行项目,访问http://localhost:7676/login
