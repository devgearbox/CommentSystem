#荔枝批发管理系统
项目概述：
荔枝批发管理系统是一个基于Spring Boot 3.5.3开发的B/S架构电商批发管理系统，采用前后端不分离的单体架构设计。系统面向荔枝批发行业的实际业务场景，为采购员、供应商和管理员提供全流程的数字化管理解决方案，涵盖商品管理、采购订单、库存管理、支付结算及数据分析等核心功能。

##📁 项目结构
lizhi/
├── src/main/java/com/example/lizhi/
│   ├── config/          # 配置类
│   ├── controller/      # 控制器层
│   ├── entity/          # 实体类
│   ├── repository/      # 数据访问层
│   ├── service/         # 业务逻辑层
│   │   └── Impl/        # 服务实现类
│   └── LizhiApplication.java           # Spring Boot启动类
├── src/main/resources/
│   ├── static/          # 静态资源
│   │   ├── css/         # 样式文件
│   │   ├── js/          # JavaScript文件
│   │   ├── img/         # 图片资源
│   │   └── otherHtml/   # 辅助HTML页面
│   ├── templates/       # Thymeleaf模板
│   └── application.properties          # 配置文件
├── uploads/             # 上传文件目录
├── commentsystem.sql    # 数据库初始化脚本
├── pom.xml             # Maven依赖配置
└── README.md           # 项目说明

##🛠️ 技术栈
###后端技术
-核心框架: Spring Boot 3.5.3
-数据持久层: Spring Data JPA + Hibernate
-安全认证: Spring Security Crypto（密码加密）
-模板引擎: Thymeleaf 3.1
-依赖管理: Maven
###数据库
-主数据库: MySQL 8.0+
-连接池: HikariCP
###第三方服务
-支付集成: 支付宝沙箱环境（Alipay EasySDK 2.2.3）
-文件处理: Apache POI 5.2.4（Excel导入导出）
-工具库: Lombok 1.18.24（代码简化）
###前端技术
-服务端渲染: Thymeleaf
-样式: 原生CSS
-交互: 原生JavaScript + Fetch API
-图表: ECharts（数据分析可视化）
##✨ 核心功能
1. 多角色权限管理
1.采购员: 创建采购订单、管理入库单、查看商品库存
2.供应商: 接收订单、更新物流状态、管理商品信息
3.管理员: 用户管理、数据统计、系统配置、审核操作
2. 采购与库存管理
1.采购订单全流程: 创建 → 审核 → 支付 → 发货 → 入库 → 完成
2.智能入库系统: 自动生成入库单，支持验收、拒收、部分入库等状态
3.保鲜状态预警: 基于入库时间自动计算荔枝新鲜度状态（新鲜/预警/紧急/过期）
4.库存联动更新: 入库状态变更时自动同步采购订单和商品库存
3. 支付与结算
1.支付宝集成: 完整支付流程（下单、支付、回调、状态查询）
2.沙箱环境: 支持测试环境完整支付验证
3.支付调试: 提供配置验证接口，便于调试
4. 数据报表与分析
1.Excel导出: 支持入库单、订单等数据的批量导出
2.数据分析: 销售统计、库存分析、供应商绩效等可视化报表
3.实时监控: 保鲜状态实时计算与预警通知
5. 系统特性
1.RESTful API设计: 前后端数据交互标准化
2.全局异常处理: 统一异常处理机制
3.文件上传: 支持图片等文件上传功能
4.分页查询: 所有列表支持分页与搜索
5.批量操作: 支持批量删除、状态更新等操作

##🚀 快速开始
###环境要求
JDK 17+
MySQL 8.0+
Maven 3.6+
Git

###部署步骤
-克隆项目
git clone https://github.com/devgearbox/CommentSystem.git
cd CommentSystem
###数据库配置
-创建MySQL数据库（如lizhi_db）
-执行初始化脚本：
mysql -u root -p lizhi_db < commentsystem.sql

###应用配置
修改 src/main/resources/application.properties：
properties
### 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/lizhi_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
spring.datasource.username=your_username
spring.datasource.password=your_password

### 支付宝沙箱配置（需前往支付宝开放平台申请）
alipay.appId=你的应用ID
alipay.privateKey=你的应用私钥
alipay.alipayPublicKey=支付宝公钥
alipay.returnUrl=http://localhost:7676/payment/return
alipay.notifyUrl=http://localhost:7676/payment/notify

### 文件上传配置
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
构建与运行

### 使用Maven构建
mvn clean package

### 运行项目
java -jar target/lizhi-0.0.1-SNAPSHOT.jar

### 或直接使用Maven运行
mvn spring-boot:run
访问系统
应用地址: http://localhost:7676
登录页面: http://localhost:7676/login
测试账号
角色	用户名	密码	权限说明
管理员	admin	123456	系统管理、数据查看、用户管理
采购员	user01	123456	采购订单创建、入库管理
供应商	supplier01	123456	订单接收、商品管理
##🔧 开发指南
###代码规范

遵循Java命名规范，使用驼峰命名法

实体类使用JPA注解进行ORM映射

服务层接口与实现分离，便于单元测试

控制器统一返回ResponseEntity或视图名称
###新增模块步骤

在entity包创建实体类，添加JPA注解

在repository包创建Repository接口，继承JpaRepository

在service包创建Service接口和Impl实现类

在controller包创建Controller，定义路由和业务逻辑

在templates包创建对应的HTML页面

###调试建议
支付调试: 访问 /payment/debug/config 验证支付宝配置

数据库调试: 开启JPA SQL日志：spring.jpa.show-sql=true

会话调试: 检查Session中用户信息是否正确存储

🧪 测试
单元测试

### 运行所有测试
mvn test

### 运行特定测试类
mvn test -Dtest=StockInServiceImplTest

接口测试
推荐使用Postman或Apifox进行API测试，已内置以下测试集合： 
用户认证接口 订单管理接口 支付相关接口 入库管理接口
##📈 性能优化
已实现的优化
数据库索引: 关键字段（order_no, user_id等）添加索引

连接池配置: 使用HikariCP连接池管理数据库连接

分页查询: 所有列表接口支持分页，避免大数据量查询

缓存策略: 频繁访问的配置数据使用内存缓存

建议优化方向
查询优化: 复杂查询添加数据库索引

静态资源: 使用CDN加速静态资源加载

异步处理: 耗时的导出操作可改为异步任务

##🔄 部署与维护
生产环境建议
数据库: 建议使用云数据库服务（如阿里云RDS）

文件存储: 使用对象存储（如OSS）替代本地存储

日志管理: 集成ELK或Logback进行日志收集

监控报警: 添加Spring Boot Actuator进行健康检查

##常见问题
支付宝回调失败: 检查服务器网络配置和回调地址可达性

文件上传失败: 检查uploads目录权限和磁盘空间

数据库连接超时: 调整连接池配置和数据库超时设置

##📄 许可证
本项目采用 MIT 许可证 - 查看 LICENSE 文件了解详情

##👥 贡献指南
欢迎提交Issue和Pull Request来帮助改进项目。在提交PR前，请确保：

代码符合现有编码规范

添加或更新相应的测试用例

更新相关文档

提交GitHub Issue

查看项目Wiki获取更多文档

##最后更新: 2025年11月
开发者: devgearbox
项目状态: 已完成开发，可用于学习参考

_备注: 此项目为学习型项目，展示了完整的Spring Boot全栈开发流程，包括需求分析、系统设计、编码实现、测试部署等环节，适合作为Java后端开发的学习参考和面试展示项目。_