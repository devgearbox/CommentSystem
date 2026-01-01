-- MySQL dump 10.13  Distrib 8.0.26, for Win64 (x86_64)
--
-- Host: localhost    Database: lizhi
-- ------------------------------------------------------
-- Server version	8.0.26

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `feedback`
--

DROP TABLE IF EXISTS `feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `feedback_type` varchar(20) NOT NULL,
  `content` text NOT NULL,
  `contact_info` varchar(255) DEFAULT NULL,
  `attachment_paths` text,
  `status` int NOT NULL DEFAULT '0',
  `submitter_id` bigint DEFAULT NULL,
  `submitter_name` varchar(100) DEFAULT NULL,
  `processor_id` bigint DEFAULT NULL,
  `processor_name` varchar(100) DEFAULT NULL,
  `process_remark` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `process_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feedback`
--

LOCK TABLES `feedback` WRITE;
/*!40000 ALTER TABLE `feedback` DISABLE KEYS */;
INSERT INTO `feedback` VALUES (2,'other','测试反馈信息传导','10000000001','',2,1,'admin',1,'admin','已解决','2025-10-29 14:33:47','2025-11-18 12:18:23','2025-11-18 12:18:23'),(3,'other','陆屋农副产品商行发布不实信息','10000000002','',2,2,'user1',1,'admin','完美解决','2025-10-29 15:31:10','2025-11-18 12:26:57','2025-11-18 12:26:57'),(4,'suggest','建议新增AI进行自动监管供应商刷单行为','10000000002','',2,2,'user1',1,'admin','','2025-10-29 15:32:03','2025-10-29 15:32:25','2025-10-29 15:32:25'),(7,'suggest','系统帮助功能良好','','',2,2,'user1',1,'admin','我们已了解，感谢你的反馈！','2025-11-19 02:29:02','2025-11-19 02:29:38','2025-11-19 02:29:38'),(8,'experience','系统功能良好','','',2,2,'user1',1,'admin','感谢您的反馈！','2025-11-19 02:42:40','2025-11-19 02:43:07','2025-11-19 02:43:07'),(9,'other','系统良好','12222222222','',0,2,'user1',NULL,NULL,NULL,'2025-11-24 13:44:04','2025-11-24 13:44:04',NULL);
/*!40000 ALTER TABLE `feedback` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `litchi_variety`
--

DROP TABLE IF EXISTS `litchi_variety`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `litchi_variety` (
  `id` int NOT NULL AUTO_INCREMENT,
  `supplier_id` int DEFAULT NULL COMMENT '关联供应商ID',
  `variety_name` varchar(50) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `stock` int DEFAULT '0',
  `description` text COMMENT '品种描述信息',
  `image_path` varchar(255) DEFAULT NULL COMMENT '商品图片路径',
  `order_count` int DEFAULT '0' COMMENT '商品订单量',
  `specification` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_litchi_supplier` (`supplier_id`),
  KEY `idx_variety_name` (`variety_name`),
  CONSTRAINT `fk_litchi_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`supplier_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `litchi_variety`
--

LOCK TABLES `litchi_variety` WRITE;
/*!40000 ALTER TABLE `litchi_variety` DISABLE KEYS */;
INSERT INTO `litchi_variety` VALUES (24,28,'妃子笑',4.60,7659,'新鲜荔枝水果妃子笑，大量上市价格优惠。','/uploads/img/30b5185b-d6d0-4bd2-b1c7-a901edc5a33c.png',7,'标准规格'),(25,28,'三月红',5.00,7115,'口感自然正，酸酸甜甜就是，三月红荔枝。','/uploads/img/bb9a9ce6-fa94-4315-a9ff-7baffe6dbfd3.jpg',2,'标准规格'),(26,29,'桂味',3.50,19493,'桂味荔枝核小肉厚清甜，现摘现发.','/uploads/img/79be1cb5-fd22-438d-b3e0-d73da843dff7.jpeg',1,'标准规格'),(27,29,'糯米糍',8.00,14504,'糯米滋 果肉乳白色，口感嫩滑，味极清甜，核小肉多，口感嫩滑。','/uploads/img/0f3b9e7b-027a-4be1-9435-7acea05bb620.png',0,'标准规格'),(28,30,'白糖罂',6.00,9732,'白糖罂荔枝顺丰冷链空运新鲜现摘现发，一件代发批发零售。','/uploads/img/c77bb2d7-81b3-4ad8-93a0-98626a6cf776.png',3,'标准规格'),(29,30,'鸡嘴荔',3.00,8355,'新鲜鸡嘴荔批发售卖，当天下单次日即送。','/uploads/img/2ceb78e0-509e-4edc-a1d0-cadbd9118238.png',1,'标准规格'),(30,31,'黒叶',1.50,6818,'大量现摘黒叶荔枝，口感厚实，鲜甜到胃。','/uploads/img/d0699412-f08c-4aa1-9f1e-472a442c4ec9.png',0,'标准规格'),(31,31,'灵山香荔',3.50,8955,'广西正宗灵山特有香荔 荔枝果。','/uploads/img/c3594e80-2e59-49e7-91ea-154d5507ba23.png',0,'标准规格'),(36,29,'碧玉荔枝',5.00,9990,'鲜美','/uploads/img/291b21f9-9c00-401b-b018-4f1479e0e52e.jpeg',0,NULL);
/*!40000 ALTER TABLE `litchi_variety` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message`
--

DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `content` text,
  `type` enum('ORDER_SHIPPED','SYSTEM_NOTICE','WARNING') NOT NULL,
  `status` enum('UNREAD','READ') DEFAULT 'UNREAD',
  `order_no` varchar(255) DEFAULT NULL,
  `recipient_id` bigint NOT NULL,
  `sender_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `read_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_recipient_status` (`recipient_id`,`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message`
--

LOCK TABLES `message` WRITE;
/*!40000 ALTER TABLE `message` DISABLE KEYS */;
INSERT INTO `message` VALUES (11,'新供应商申请待审核','新的供应商申请等待审核：\n供应商名称：灵山荔枝产销合33\n联系人：黄志强3\n联系电话：13977752344\n提交者：supplier02 (ID:8)\n请及时登录系统进行审核处理。','SYSTEM_NOTICE','READ',NULL,1,0,'2025-11-18 12:41:41','2025-11-18 20:47:02'),(13,'订单发货提醒','您的订单 49244a27a35847828404e6d0fd22a1fc 已由供应商 新圩荔枝种植基地 发货，请注意查收。','ORDER_SHIPPED','UNREAD','49244a27a35847828404e6d0fd22a1fc',3,0,'2025-11-18 15:20:32',NULL),(14,'订单发货提醒','您的订单 9b94e5b59f5c45e9933b55d24042bc84 已由供应商 新圩荔枝种植基地 发货，请注意查收。','ORDER_SHIPPED','UNREAD','9b94e5b59f5c45e9933b55d24042bc84',3,0,'2025-11-18 15:23:37',NULL),(15,'新供应商申请待审核','新的供应商申请等待审核：\n供应商名称：灵山荔枝产销合33\n联系人：黄志强3\n联系电话：13977752344\n提交者：supplier02\n请及时进行审核处理。','SYSTEM_NOTICE','UNREAD',NULL,1,0,'2025-11-18 15:33:18',NULL),(16,'订单发货提醒','您的订单 cfaa3eac42314078a61396a16ac8e269 已由供应商 灵山荔枝产销合作社 发货，请注意查收。','ORDER_SHIPPED','READ','cfaa3eac42314078a61396a16ac8e269',2,0,'2025-11-19 01:28:15','2025-11-24 21:42:58'),(17,'库存保鲜紧急提醒','入库单 ealdb97684d74d05981ed173080cf63 的荔枝品种 糯米糍 数量 200 斤已进入紧急保鲜状态，请及时处理！入库时间：2025-11-11 10:30','WARNING','READ','ealdb97684d74d05981ed173080cf63',2,1,'2025-11-19 10:26:42','2025-11-24 21:42:58'),(19,'反馈处理完成通知','您的反馈已被处理；反馈内容：系统功能良好；处理备注：感谢您的反馈！','SYSTEM_NOTICE','READ',NULL,2,0,'2025-11-19 02:43:07','2025-11-24 21:42:58'),(21,'订单发货提醒','您的订单 306f6f2ec44b4acb9d376c8de6391b7d 已由供应商 新圩荔枝种植基地 发货，请注意查收。','ORDER_SHIPPED','UNREAD','306f6f2ec44b4acb9d376c8de6391b7d',2,0,'2025-11-24 13:46:25',NULL);
/*!40000 ALTER TABLE `message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_record`
--

DROP TABLE IF EXISTS `payment_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
  `order_id` int NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单编号',
  `payment_method` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '支付方式：WECHAT-微信支付, ALIPAY-支付宝',
  `payment_amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `payment_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '支付状态：PENDING-待支付, SUCCESS-支付成功, FAILED-支付失败, REFUNDED-已退款',
  `transaction_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付平台交易号（唯一）',
  `sandbox_mode` tinyint(1) DEFAULT '1' COMMENT '是否为沙盒模式：0-生产环境, 1-沙盒环境',
  `request_data` text COLLATE utf8mb4_unicode_ci COMMENT '支付请求数据（JSON格式）',
  `response_data` text COLLATE utf8mb4_unicode_ci COMMENT '支付响应数据（JSON格式）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `payment_time` datetime DEFAULT NULL COMMENT '支付完成时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `transaction_id` (`transaction_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_transaction_id` (`transaction_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_payment_time` (`payment_time`),
  CONSTRAINT `payment_record_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `purchase_order` (`order_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_payment_amount` CHECK ((`payment_amount` > 0)),
  CONSTRAINT `chk_payment_method` CHECK ((`payment_method` in (_gbk'WECHAT',_gbk'ALIPAY',_gbk'BANK_TRANSFER',_gbk'SANDBOX'))),
  CONSTRAINT `chk_payment_status` CHECK ((`payment_status` in (_utf8mb4'PENDING',_utf8mb4'SUCCESS',_utf8mb4'FAILED',_utf8mb4'REFUNDED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_record`
--

LOCK TABLES `payment_record` WRITE;
/*!40000 ALTER TABLE `payment_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `payment_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `purchase_order`
--

DROP TABLE IF EXISTS `purchase_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_order` (
  `order_id` int NOT NULL AUTO_INCREMENT COMMENT '订单ID，唯一标识',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号，业务系统生成的唯一编号',
  `purchase_quantity` decimal(10,2) NOT NULL COMMENT '采购荔枝的数量',
  `order_status` enum('pending','paid','shipping','shipped','received','cancelled','rejected') DEFAULT NULL,
  `purchase_variety` varchar(100) NOT NULL COMMENT '采购的荔枝品种，如：妃子笑、桂味等',
  `supplier_id` int NOT NULL COMMENT '供应商ID，关联供应商信息表',
  `purchaser_id` bigint DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `total_price` decimal(10,2) NOT NULL COMMENT '订单总价',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删，1-已删',
  `variety_id` int DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `specification` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `order_no` (`order_no`),
  KEY `fk_purchase_supplier` (`supplier_id`),
  KEY `fk_purchase_user` (`purchaser_id`),
  KEY `fk_order_variety` (`purchase_variety`),
  KEY `fk_purchase_variety` (`variety_id`),
  CONSTRAINT `fk_purchase_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`supplier_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_purchase_user` FOREIGN KEY (`purchaser_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_purchase_variety` FOREIGN KEY (`variety_id`) REFERENCES `litchi_variety` (`id`),
  CONSTRAINT `purchase_order_chk_1` CHECK ((`purchase_quantity` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=223 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_order`
--

LOCK TABLES `purchase_order` WRITE;
/*!40000 ALTER TABLE `purchase_order` DISABLE KEYS */;
INSERT INTO `purchase_order` VALUES (195,'2dbb27cb18c4439c91f45e7316af14e6',2.00,'pending','妃子笑',28,2,'2025-10-29 16:11:00',9.20,0,24,'2025-10-29 16:11:00','头茬鲜果（原价）'),(196,'8bbe446c777d40e7a4ab04aa70d38131',3.00,'shipped','三月红',28,2,'2025-10-29 16:12:38',16.50,0,25,'2025-11-17 14:34:11','商超大果（+10%）'),(197,'392dd56a0ab045439b2cb847a4d6ae27',10.00,'shipped','桂味',29,2,'2025-10-29 16:13:25',40.25,0,26,'2025-11-17 14:57:00','稀缺果王（+15%）'),(198,'ea1db97684d74d05981ed173080cf63a',10.00,'shipped','糯米糍',29,2,'2025-10-29 16:13:35',90.99,0,27,'2025-10-29 16:15:06','商超大果（+10%）'),(199,'696084dfe0564e88aa18177542ce8c6b',100.00,'received','白糖罂',30,2,'2025-10-29 16:13:45',600.00,0,28,'2025-10-29 16:15:29','头茬鲜果（原价）'),(200,'c05272bc802b474184dad72b8cc9b490',200.00,'received','鸡嘴荔',30,2,'2025-10-29 16:13:54',660.00,0,29,'2025-11-15 09:13:50','商超大果（+10%）'),(201,'da1f5b3195d94a16a250c4eea8ffe41a',500.00,'rejected','黒叶',31,2,'2025-10-29 16:14:02',862.50,0,30,'2025-10-29 16:16:47','稀缺果王（+15%）'),(202,'9078dc490ab0420984ae0ea3e7cb0e2d',1000.00,'rejected','灵山香荔',31,2,'2025-10-29 16:14:10',3500.00,0,31,'2025-10-29 16:17:00','头茬鲜果（原价）'),(203,'ab85c2ec0fbb40c99decc61b9c7a9238',600.00,'rejected','妃子笑',28,2,'2025-10-29 16:14:19',3036.00,0,24,'2025-10-29 16:17:09','商超大果（+10%）'),(204,'af433e9740044947b8ced20be9101a24',60.00,'rejected','三月红',28,2,'2025-10-29 16:14:35',347.99,0,25,'2025-10-29 16:17:33','稀缺果王（+15%）'),(205,'881819e7e2d14eefa05e0b80ca0d6fbc',500.00,'rejected','鸡嘴荔',30,2,'2025-11-01 06:34:57',1650.00,0,29,'2025-11-15 09:16:38','商超大果（+10%）'),(206,'cfaa3eac42314078a61396a16ac8e269',400.00,'received','妃子笑',28,2,'2025-11-10 12:11:28',1840.00,0,24,'2025-11-19 01:28:17','头茬鲜果（原价）'),(207,'d4995198f37c47489badca59c7693359',10.00,'pending','妃子笑',28,3,'2025-11-18 15:18:56',46.00,0,24,'2025-11-18 15:18:56','头茬鲜果（原价）'),(208,'49244a27a35847828404e6d0fd22a1fc',10.00,'received','桂味',29,3,'2025-11-18 15:20:02',35.00,0,26,'2025-11-18 15:21:05','头茬鲜果（原价）'),(209,'9b94e5b59f5c45e9933b55d24042bc84',5.00,'rejected','桂味',29,3,'2025-11-18 15:23:18',17.50,0,26,'2025-11-18 15:23:57','头茬鲜果（原价）'),(210,'12c7d20d04ff4a9aa377e37cb1ad16e3',110.00,'pending','鸡嘴荔',30,2,'2025-11-24 13:01:07',365.99,0,29,'2025-11-24 13:01:07','商超大果（+10%）'),(211,'42df1bc1933a4b4d85753421a2693b4f',5.00,'pending','鸡嘴荔',30,2,'2025-11-24 13:04:13',17.25,0,29,'2025-11-24 13:04:13','稀缺果王（+15%）'),(212,'554458fd3c15422b85fb7280fdf7a761',2.00,'pending','桂味',29,2,'2025-11-24 13:08:54',7.00,0,26,'2025-11-24 13:08:54','头茬鲜果（原价）'),(213,'598bcbf49ccc4aeba3dcaf8466319171',2.00,'received','三月红',28,2,'2025-11-24 13:13:17',10.00,0,25,'2025-11-24 13:42:10','头茬鲜果（原价）'),(214,'306f6f2ec44b4acb9d376c8de6391b7d',3.00,'rejected','糯米糍',29,2,'2025-11-24 13:21:54',24.00,0,27,'2025-11-24 13:46:43','头茬鲜果（原价）'),(215,'6c85b3eb29024485b85c73f839942a58',5.00,'paid','糯米糍',29,2,'2025-11-24 13:23:14',40.00,0,27,'2025-11-24 13:23:33','头茬鲜果（原价）'),(216,'ddf257f5a6ee49019f3286e901d1a467',10.00,'paid','鸡嘴荔',30,2,'2025-11-24 13:28:02',30.00,0,29,'2025-11-24 13:28:29','头茬鲜果（原价）'),(217,'e5a114b203f14479b14e989ca1164620',3.00,'paid','白糖罂',30,2,'2025-11-24 13:32:13',18.00,0,28,'2025-11-24 13:32:42','头茬鲜果（原价）'),(218,'daec302051b14a1bbe79bbef0303c053',2.00,'paid','糯米糍',29,2,'2025-11-24 13:38:39',18.99,0,27,'2025-11-24 13:39:16','头茬鲜果（原价）'),(219,'e27501d2ff884e2e92f7fd435f546544',5.00,'pending','三月红',28,3,'2025-11-24 13:51:34',25.00,0,25,'2025-11-24 13:51:34','头茬鲜果（原价）'),(220,'0d6fbb61fb9344a08ca98872790283d0',10.00,'paid','桂味',29,3,'2025-11-24 13:56:03',35.00,0,26,'2025-11-24 13:56:28','头茬鲜果（原价）'),(221,'6d6280a413644205845fac7b8ac249b0',3.00,'paid','鸡嘴荔',30,2,'2025-11-24 14:56:22',9.00,0,29,'2025-11-24 14:56:44','头茬鲜果（原价）'),(222,'f67f16c8f11040adafbc938701694c84',10.00,'paid','碧玉荔枝',29,2,'2025-11-25 05:44:30',52.99,0,36,'2025-11-25 05:45:02','头茬鲜果（原价）');
/*!40000 ALTER TABLE `purchase_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `return_order`
--

DROP TABLE IF EXISTS `return_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `return_order` (
  `return_id` int NOT NULL AUTO_INCREMENT,
  `return_no` varchar(255) NOT NULL,
  `order_no` varchar(255) NOT NULL,
  `litchi_variety` varchar(255) DEFAULT NULL,
  `quantity` decimal(19,2) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `return_status` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `operator_name` varchar(255) DEFAULT NULL,
  `operator_id` int DEFAULT NULL,
  `refund_amount` decimal(19,2) DEFAULT NULL,
  `supplier_name` varchar(255) DEFAULT NULL,
  `purchaser_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`return_id`),
  UNIQUE KEY `return_no` (`return_no`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `return_order`
--

LOCK TABLES `return_order` WRITE;
/*!40000 ALTER TABLE `return_order` DISABLE KEYS */;
INSERT INTO `return_order` VALUES (12,'RET1761754606787','da1f5b3195d94a16a250c4eea8ffe41a','黒叶',500.00,'运输超时','completed','2025-10-29 16:16:47','2025-10-29 16:18:03','张三',2,862.50,'石塘镇荔枝合作社','张三'),(13,'RET1761754619589','9078dc490ab0420984ae0ea3e7cb0e2d','灵山香荔',1000.00,'品质不对','refunded','2025-10-29 16:17:00','2025-10-29 16:17:59','张三',2,3500.00,'石塘镇荔枝合作社','张三'),(14,'RET1761754629438','ab85c2ec0fbb40c99decc61b9c7a9238','妃子笑',600.00,'大量坏果','approved','2025-10-29 16:17:09','2025-10-29 16:17:55','张三',2,3036.00,'灵山荔枝产销合作社','张三'),(15,'RET1761754652722','af433e9740044947b8ced20be9101a24','三月红',60.00,'质量不对','pending','2025-10-29 16:17:33','2025-10-29 16:17:33','张三',2,347.99,'灵山荔枝产销合作社','张三'),(16,'RET1763198197845','881819e7e2d14eefa05e0b80ca0d6fbc','鸡嘴荔',500.00,'超期','pending','2025-11-15 09:16:38','2025-11-15 09:16:38','张三',2,1650.00,'檀圩鲜果供应链','张三'),(17,'RET1763479436569','9b94e5b59f5c45e9933b55d24042bc84','桂味',5.00,'不想要了','completed','2025-11-18 15:23:57','2025-11-18 15:26:16','李四',3,17.50,'新圩荔枝种植基地','李四'),(18,'RET1763992003277','306f6f2ec44b4acb9d376c8de6391b7d','糯米糍',3.00,'品种不佳','rejected','2025-11-24 13:46:43','2025-11-24 13:48:10','张三',2,24.00,'新圩荔枝种植基地','张三');
/*!40000 ALTER TABLE `return_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock_in`
--

DROP TABLE IF EXISTS `stock_in`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_in` (
  `stock_id` int NOT NULL AUTO_INCREMENT COMMENT '入库单唯一ID',
  `order_no` varchar(50) DEFAULT NULL COMMENT '关联采购订单编号（外键关联采购订单表）',
  `litchi_variety` varchar(100) NOT NULL COMMENT '荔枝品种',
  `quantity` decimal(10,2) NOT NULL COMMENT '入库数量（斤）',
  `quality_feedback` text COMMENT '质量反馈（如：无坏果、部分挤压等）',
  `stock_in_status` enum('pending','checking','completed','partial','rejected') NOT NULL DEFAULT 'pending' COMMENT '入库单状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库单创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '状态更新时间',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID（关联用户表，记录谁操作的入库）',
  `operator_name` varchar(255) DEFAULT NULL COMMENT '经办人姓名',
  `stock_in_time` datetime DEFAULT NULL,
  PRIMARY KEY (`stock_id`),
  UNIQUE KEY `idx_order_no` (`order_no`),
  KEY `idx_stock_in_status` (`stock_in_status`),
  CONSTRAINT `fk_stock_in_order` FOREIGN KEY (`order_no`) REFERENCES `purchase_order` (`order_no`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_stock_in_order_no` FOREIGN KEY (`order_no`) REFERENCES `purchase_order` (`order_no`)
) ENGINE=InnoDB AUTO_INCREMENT=85 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='入库信息表（简化版，无单独仓库表关联）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_in`
--

LOCK TABLES `stock_in` WRITE;
/*!40000 ALTER TABLE `stock_in` DISABLE KEYS */;
INSERT INTO `stock_in` VALUES (66,'ea1db97684d74d05981ed173080cf63a','糯米糍',10.00,NULL,'pending','2025-10-29 16:15:06','2025-10-29 16:15:06',2,'张三',NULL),(67,'696084dfe0564e88aa18177542ce8c6b','白糖罂',100.00,NULL,'completed','2025-10-29 16:15:14','2025-11-15 09:13:23',2,'张三','2025-11-15 09:13:23'),(68,'c05272bc802b474184dad72b8cc9b490','鸡嘴荔',200.00,NULL,'completed','2025-11-11 10:21:02','2025-11-19 10:21:02',2,'张三','2025-11-15 09:13:50'),(69,'da1f5b3195d94a16a250c4eea8ffe41a','黒叶',500.00,NULL,'rejected','2025-10-29 16:15:57','2025-10-29 16:15:57',2,'张三',NULL),(70,'9078dc490ab0420984ae0ea3e7cb0e2d','灵山香荔',1000.00,NULL,'rejected','2025-10-29 16:16:06','2025-10-29 16:16:06',2,'张三',NULL),(71,'ab85c2ec0fbb40c99decc61b9c7a9238','妃子笑',600.00,NULL,'rejected','2025-10-29 16:16:13','2025-10-29 16:16:13',2,'张三',NULL),(72,'af433e9740044947b8ced20be9101a24','三月红',60.00,NULL,'rejected','2025-10-29 16:16:20','2025-10-29 16:16:20',2,'张三',NULL),(73,'881819e7e2d14eefa05e0b80ca0d6fbc','鸡嘴荔',500.00,NULL,'rejected','2025-11-15 09:16:14','2025-11-15 09:16:38',2,'张三',NULL),(74,'8bbe446c777d40e7a4ab04aa70d38131','三月红',3.00,NULL,'pending','2025-11-17 14:34:11','2025-11-17 14:34:11',2,'张三',NULL),(75,'392dd56a0ab045439b2cb847a4d6ae27','桂味',10.00,NULL,'pending','2025-11-17 14:57:00','2025-11-17 14:57:00',2,'张三',NULL),(80,'49244a27a35847828404e6d0fd22a1fc','桂味',10.00,NULL,'completed','2025-11-18 15:20:32','2025-11-18 15:21:05',3,'李四','2025-11-18 15:21:05'),(81,'9b94e5b59f5c45e9933b55d24042bc84','桂味',5.00,NULL,'rejected','2025-11-18 15:23:37','2025-11-18 15:23:57',3,'李四',NULL),(82,'cfaa3eac42314078a61396a16ac8e269','妃子笑',400.00,NULL,'completed','2025-11-19 01:28:15','2025-11-19 01:29:22',2,'张三','2025-11-19 01:29:22'),(83,'598bcbf49ccc4aeba3dcaf8466319171','三月红',2.00,NULL,'completed','2025-11-24 13:41:40','2025-11-24 13:42:10',2,'张三','2025-11-24 13:42:10'),(84,'306f6f2ec44b4acb9d376c8de6391b7d','糯米糍',3.00,NULL,'rejected','2025-11-24 13:46:25','2025-11-24 13:46:43',2,'张三',NULL);
/*!40000 ALTER TABLE `stock_in` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `supplier`
--

DROP TABLE IF EXISTS `supplier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier` (
  `supplier_id` int NOT NULL AUTO_INCREMENT,
  `supplier_name` varchar(255) DEFAULT '默认供应商',
  `contact` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `varieties` varchar(255) DEFAULT NULL COMMENT '供应品种（逗号分隔）',
  `cooperation_start_date` varchar(255) DEFAULT NULL COMMENT '合作开始日期',
  `status` int DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `order_count` int DEFAULT '0' COMMENT '订单量',
  `user_id` bigint DEFAULT NULL COMMENT '关联的用户ID（供应商角色用户）',
  PRIMARY KEY (`supplier_id`),
  KEY `idx_phone` (`phone`) COMMENT '联系电话索引'
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplier`
--

LOCK TABLES `supplier` WRITE;
/*!40000 ALTER TABLE `supplier` DISABLE KEYS */;
INSERT INTO `supplier` VALUES (28,'灵山荔枝产销合作社','黄志强','13977752345','灵山县灵城镇江南路89号','三月红,妃子笑','2023-01-14',1,'2025-08-02 15:08:06','2025-11-24 21:42:10',9,8),(29,'新圩荔枝种植基地','李梅','13877786789','灵山县新圩镇荔枝大道12号','桂味,糯米糍','2023-02-20',1,'2025-08-02 15:08:06','2025-11-19 09:33:54',1,9),(30,'檀圩鲜果供应链','张伟','13768191234','灵山县檀圩镇农贸路36号','白糖罂,鸡嘴荔','2023-03-05',1,'2025-08-02 15:08:06','2025-11-19 09:31:31',0,10),(31,'石塘镇荔枝合作社','王丽','13517575678','灵山县石塘镇新兴街22号','黑叶荔,灵山香荔','2023-04-10',1,'2025-08-02 15:08:06','2025-11-19 09:31:31',0,11),(32,'陆屋农副产品商行','陈明','13457728901','灵山县陆屋镇商贸大道59号','无核荔,桂早荔','2023-05-18',0,'2025-08-02 15:08:06','2025-11-19 09:31:31',0,12),(33,'武利荔枝批发中心','杨丽','13317773456','灵山县武利镇南路45号','妃子笑,桂味','2023-06-22',2,'2025-08-02 15:08:06','2025-11-19 09:31:31',0,13),(34,'伯劳镇果农联盟','黄建国','13207776789','灵山县伯劳镇北街18号','三月红,白糖罂','2023-07-08',1,'2025-08-02 15:08:06','2025-11-19 09:31:31',0,14),(35,'文利荔枝种植场','林小芳','13137779012','灵山县文利镇沿江路33号','糯米糍,鸡嘴荔','2023-08-15',1,'2025-08-02 15:08:06','2025-11-19 09:31:31',0,15),(36,'平南镇鲜果合作社','刘军','13077752345','灵山县平南镇新街56号','黑叶荔,灵山香荔','2023-09-20',1,'2025-08-02 15:08:06','2025-11-19 09:31:31',0,16),(37,'烟墩镇荔枝产销中心','覃艳','18977786789','灵山县烟墩镇正街29号','无核荔,桂味','2023-10-05',1,'2025-08-02 15:08:06','2025-11-19 09:31:31',0,17),(38,'伯劳荔枝种植厂','陈富贵','13635026351','灵山县伯劳镇北街10号','黑叶','2024-01-01',1,'2025-08-02 15:47:29','2025-11-19 09:31:31',0,18);
/*!40000 ALTER TABLE `supplier` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID（主键）',
  `username` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `real_name` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `role` int DEFAULT NULL,
  `status` int DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL COMMENT '用户性别',
  `signature` varchar(255) DEFAULT NULL COMMENT '个性签名',
  `main_address` varchar(255) DEFAULT NULL COMMENT '默认地址',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','$2a$10$Cf1ppJSECp7TglSZV3NJne3uOYwLHy0lUo8xyC91ly8hsoajUxE7G','系统管理员','13778778777',1,1,'男','该用户很懒，什么都没有留下...',NULL),(2,'user1','$2a$10$yp8cHgntX4H26nQMSPnEDedCP0S5kAKjCdVzoDqSS5f2M21V.ayA2','张三','10000000011',2,1,'男','皇家测试员001号为您服务！',NULL),(3,'user2','$2a$10$GeDc.arwxeZU3RjAGjlDfuQjfsPLxfJYFuVs016TjiYfgg64zoJCW','李四','13533335555',2,1,'男','该用户很懒，什么都没有留下...',NULL),(4,'user3','$2a$10$pfml6SSzkgXbljF7sjAp/ulS5yC22q1.z8ZeJiM8QOwBkjKzQ1H6q','蔡徐坤',NULL,2,1,NULL,NULL,NULL),(5,'虎哥','$2a$10$1NKMNlGOdC/TMsMoeokSGu9ulQTetJus1/AQmHJkn5mgVsNevGlZ2','陈大虎','13586652525',2,1,'男',NULL,NULL),(6,'supplier01','$2a$10$6C4fyVs2eF4i4yaOFDMbTeDSP63flYOFpFn3toiFUV.BJWrAM34NW','供应商01','13800138000',3,1,'男','专注优质荔枝供应','XX省XX市荔枝产业园'),(7,'user4','$2a$10$ICiPQkwqV6MYFX7KglgE2.YxptSkOwGoo/Py0SZ9ulvH9hlQlpImi','黄四虎','15745505423',2,1,'男',NULL,NULL),(8,'supplier02','$2a$10$90aJT8k0wIZhdsHibD.CiecVNVce61KHW/irmewHvjNIErrNFjlFO','供应商02','13800138001',3,1,'男',NULL,NULL),(9,'supplier03','$2a$10$KaRcct6GUw5qG.4omV7RGuGEDx4dYUu2S8nQoNGZ3OIWCaJKcYd2.','供应商03','13800138003',3,1,'男',NULL,NULL),(10,'supplier04','$2a$10$B0hhQIhMpYB4bUpkET/5neP4srdVDPcLFZoSu8gHiDH8hjx2Bk0Ra','供应商04','13800138004',3,1,'男',NULL,NULL),(11,'supplier05','$2a$10$fF07znOiBgsMlADbk1Z7ZOA6GUty3b302/56rbcm53pxddRI.2L7W','供应商05','13800138005',3,1,'男',NULL,NULL),(12,'supplier06','$2a$10$1UhEx9BYcfF3eJuJw4qEHut/X8AYt5M.ld7zXRL.lb..9U3qT/chm','供应商06','13800138006',3,1,'男',NULL,NULL),(13,'supplier07','$2a$10$ndT7k0Ltr6vw4rX60GxzIe/uOXj.i2XH0dZ9.UIJQEtd.dSDqhWjS','供应商07','13800138007',3,1,'男',NULL,NULL),(14,'supplier08','$2a$10$2pk3eHM7W4L0j8XumIaMU.lIKWOVL1cM5cFJcwRAu.c/7WmDYH/5y','供应商08','13800138008',3,1,'男',NULL,NULL),(15,'supplier09','$2a$10$oaBKniFQn9Mfh.d6xyRrDuuI5MADH8hbKjBRcLHcRboo6xz./gEp2','供应商09','13800138009',3,1,'男',NULL,NULL),(16,'supplier10','$2a$10$0iAIKs0sdsXFeVlkvYqsneVf6m83ekYKkBtri/Q6Dazsq0RgeOqZK','供应商10','13800138010',3,1,'男',NULL,NULL),(17,'supplier11','$2a$10$eZBdg8dtj57v7x7IsDiJluVV0RzCJcPRg8AyVN.6BXAm0v9oEHU8O','供应商11','13800138011',3,1,'男',NULL,NULL),(18,'supplier12','$2a$10$1W.AbfeR5/i1sDAVX7nRD.u/VeLZ7w7poCaCSPxgmrE3roKtUV6EG','供应商12','13800138012',3,1,'男',NULL,NULL),(19,'supplier13','$2a$10$AF/tfG8clLxdKaEe59JLl.3yQZ11TnylgXjTJfjQ19MTVC3wbA17G','供应商13','13800138013',3,1,'男',NULL,NULL);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_address`
--

DROP TABLE IF EXISTS `user_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址 ID',
  `user_id` bigint NOT NULL COMMENT '关联用户',
  `address` varchar(255) NOT NULL COMMENT '详细地址',
  `is_default` tinyint(1) DEFAULT '0' COMMENT '是否默认地址（1:是，0:否）',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `user_address_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_address`
--

LOCK TABLES `user_address` WRITE;
/*!40000 ALTER TABLE `user_address` DISABLE KEYS */;
INSERT INTO `user_address` VALUES (2,1,'广西灵山县龙武庄园',0),(4,1,'灵山县檀圩镇桥梓村委 209 国道旁',1),(5,2,'南宁学院',1),(6,3,'广西南宁邕宁区天天批发有限公司',1),(7,2,'广西灵山平南镇1号',NULL);
/*!40000 ALTER TABLE `user_address` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-01 15:14:50
