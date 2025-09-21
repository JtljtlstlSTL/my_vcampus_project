-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: virtual_campus
-- ------------------------------------------------------
-- Server version	8.0.43

CREATE DATABASE `virtual_campus` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `virtual_campus`;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `tblbook`
--

DROP TABLE IF EXISTS `tblbook`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tblbook` (
  `book_Id` int NOT NULL AUTO_INCREMENT COMMENT '图书ID',
  `isbn` varchar(20) NOT NULL COMMENT 'ISBN号（唯一约束）',
  `Title` varchar(100) NOT NULL COMMENT '书名',
  `Author` varchar(50) NOT NULL COMMENT '作者',
  `Publisher` varchar(100) DEFAULT NULL COMMENT '出版社',
  `Publish_date` date DEFAULT NULL COMMENT '出版日期',
  `Category` varchar(2) DEFAULT NULL COMMENT '图书分类（中图法代码A-Z）',
  `Location` varchar(50) DEFAULT NULL COMMENT '馆藏位置',
  `Total_qty` int DEFAULT '1' COMMENT '馆藏总数量',
  `Avail_qty` int DEFAULT '1' COMMENT '当前可借数量',
  `Status` enum('IN_LIBRARY','BORROWED') DEFAULT 'IN_LIBRARY' COMMENT '图书状态：在馆/借出',
  PRIMARY KEY (`book_Id`),
  UNIQUE KEY `isbn` (`isbn`),
  KEY `idx_book_search` (`Title`,`Author`,`Category`),
  KEY `idx_book_status_location` (`Status`,`Location`),
  KEY `idx_book_category` (`Category`),
  KEY `idx_book_isbn` (`isbn`),
  KEY `idx_book_publisher` (`Publisher`),
  KEY `idx_book_publish_date` (`Publish_date`),
  KEY `idx_book_avail_qty` (`Avail_qty`),
  CONSTRAINT `fk_book_category` FOREIGN KEY (`Category`) REFERENCES `tblbookcategory` (`category_code`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='图书表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tblbook`
--

LOCK TABLES `tblbook` WRITE;
/*!40000 ALTER TABLE `tblbook` DISABLE KEYS */;
INSERT INTO `tblbook` VALUES (1,'978-7-01-001234-5','共产党宣言','马克思、恩格斯','人民出版社','2018-01-01','A','A区1层马列类',15,14,'BORROWED'),(2,'978-7-01-002345-6','毛泽东选集（第一卷）','毛泽东','人民出版社','2019-05-01','A','A区1层马列类',10,9,'BORROWED'),(3,'978-7-100-12345-6','中国哲学简史','冯友兰','商务印书馆','2018-03-01','B','B区1层哲学类',12,11,'BORROWED'),(4,'978-7-108-02345-7','理想国','柏拉图','人民出版社','2019-08-01','B','B区1层哲学类',8,7,'BORROWED'),(5,'978-7-100-23456-7','社会学研究方法','风笑天','商务印书馆','2019-02-01','C','C区1层社科类',10,9,'BORROWED'),(6,'978-7-300-12345-6','社会心理学','戴维·迈尔斯','中国人民大学出版社','2018-06-01','C','C区1层社科类',12,11,'BORROWED'),(7,'978-7-01-003456-7','宪法学导论','张千帆','人民出版社','2019-04-01','D','D区1层法律类',8,7,'BORROWED'),(8,'978-7-5620-2345-6','民法总论','朱庆育','中国政法大学出版社','2018-09-01','D','D区1层法律类',10,9,'BORROWED'),(9,'978-7-5065-1234-5','孙子兵法','孙武','解放军出版社','2018-11-01','E','E区1层军事类',12,11,'BORROWED'),(10,'978-7-80150-234-5','战争论','克劳塞维茨','军事科学出版社','2019-03-01','E','E区1层军事类',8,8,'IN_LIBRARY'),(11,'978-7-111-56789-0','经济学原理','曼昆','机械工业出版社','2019-01-01','F','F区1层经济类',15,15,'IN_LIBRARY'),(12,'978-7-300-23456-7','货币金融学','弗雷德里克·米什金','中国人民大学出版社','2018-07-01','F','F区1层经济类',10,10,'IN_LIBRARY'),(13,'978-7-04-045678-9','教育学','王道俊','高等教育出版社','2019-06-01','G','G区1层教育类',12,12,'IN_LIBRARY'),(14,'978-7-100-34567-8','图书馆学基础','吴慰慈','商务印书馆','2018-12-01','G','G区1层教育类',8,8,'IN_LIBRARY'),(15,'978-7-100-45678-9','现代汉语','黄伯荣、廖序东','商务印书馆','2019-02-01','H','H区1层语言类',15,15,'IN_LIBRARY'),(16,'978-7-5446-1234-5','新编英语语法教程','章振邦','上海外语教育出版社','2018-10-01','H','H区1层语言类',10,10,'IN_LIBRARY'),(17,'978-7-02-012345-6','红楼梦','曹雪芹','人民文学出版社','2018-05-01','I','I区1层文学类',20,19,'BORROWED'),(18,'978-7-5327-1234-5','百年孤独','加西亚·马尔克斯','上海译文出版社','2019-03-01','I','I区1层文学类',15,15,'IN_LIBRARY'),(19,'978-7-102-05678-9','艺术的故事','贡布里希','广西美术出版社','2018-08-01','J','J区1层艺术类',10,10,'IN_LIBRARY'),(20,'978-7-5344-1234-5','中国美术史','洪再新','中国美术学院出版社','2019-01-01','J','J区1层艺术类',8,8,'IN_LIBRARY'),(21,'978-7-101-03456-7','史记','司马迁','中华书局','2018-04-01','K','K区1层历史类',12,12,'IN_LIBRARY'),(22,'978-7-100-56789-0','中国历史地理概述','邹逸麟','商务印书馆','2019-07-01','K','K区1层历史类',10,10,'IN_LIBRARY'),(23,'978-7-100-67890-1','科学史','W.C.丹皮尔','商务印书馆','2018-11-01','N','N区1层科学类',8,8,'IN_LIBRARY'),(24,'978-7-03-012345-6','自然科学导论','张民生','科学出版社','2019-02-01','N','N区1层科学类',10,9,'BORROWED'),(25,'978-7-04-034567-8','高等数学','同济大学数学系','高等教育出版社','2019-09-01','O','O区1层数学类',25,25,'IN_LIBRARY'),(26,'978-7-122-12345-6','有机化学','邢其毅','化学工业出版社','2018-12-01','O','O区1层化学类',15,15,'IN_LIBRARY'),(27,'978-7-03-023456-7','普通天文学','胡中为','科学出版社','2019-05-01','P','P区1层天文类',8,8,'IN_LIBRARY'),(28,'978-7-116-12345-6','普通地质学','舒良树','地质出版社','2018-08-01','P','P区1层地质类',10,10,'IN_LIBRARY'),(29,'978-7-04-045679-6','普通生物学','陈阅增','高等教育出版社','2019-03-01','Q','Q区1层生物类',12,12,'IN_LIBRARY'),(30,'978-7-109-12345-6','植物学','强胜','中国农业出版社','2018-10-01','Q','Q区1层生物类',10,10,'IN_LIBRARY'),(31,'978-7-117-23456-7','内科学','葛均波','人民卫生出版社','2019-06-01','R','R区1层医学类',15,15,'IN_LIBRARY'),(32,'978-7-5132-1234-5','中医基础理论','孙广仁','中国中医药出版社','2018-09-01','R','R区1层医学类',10,10,'IN_LIBRARY'),(33,'978-7-109-23456-7','作物栽培学','董钻','中国农业出版社','2019-04-01','S','S区1层农业类',8,8,'IN_LIBRARY'),(34,'978-7-5082-1234-5','果树栽培学','张玉星','金盾出版社','2018-11-01','S','S区1层农业类',10,10,'IN_LIBRARY'),(35,'978-7-111-67890-1','机械设计手册','成大先','机械工业出版社','2019-07-01','T','T区1层机械类',12,12,'IN_LIBRARY'),(36,'978-7-121-45678-9','电子技术基础','康华光','电子工业出版社','2018-12-01','T','T区1层电子类',15,15,'IN_LIBRARY'),(37,'978-7-114-12345-6','交通工程学','王炜','人民交通出版社','2019-02-01','U','U区1层交通类',10,10,'IN_LIBRARY'),(38,'978-7-313-12345-6','船舶原理','盛振邦','上海交通大学出版社','2018-10-01','U','U区1层交通类',8,8,'IN_LIBRARY'),(39,'978-7-118-12345-6','航空航天概论','贾玉红','国防工业出版社','2019-01-01','V','V区1层航天类',8,8,'IN_LIBRARY'),(40,'978-7-5612-1234-5','飞机飞行力学','方振平','西北工业大学出版社','2018-08-01','V','V区1层航天类',10,10,'IN_LIBRARY'),(41,'978-7-5025-1234-5','环境科学导论','何强','化学工业出版社','2019-05-01','X','X区1层环境类',12,12,'IN_LIBRARY'),(42,'978-7-5641-1234-5','安全系统工程','张景林','东南大学出版社','2018-11-01','X','X区1层安全类',10,10,'IN_LIBRARY'),(43,'978-7-100-78901-2','中国大百科全书','编委会','商务印书馆','2019-08-01','Z','Z区1层综合类',5,5,'IN_LIBRARY'),(44,'978-7-5326-1234-5','辞海','编委会','上海辞书出版社','2018-12-01','Z','Z区1层综合类',8,8,'IN_LIBRARY'),(45,'1234567891','不白吃','不白吃','不白吃',NULL,'A','不白吃',1,1,'IN_LIBRARY');
/*!40000 ALTER TABLE `tblbook` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `tr_book_before_insert` BEFORE INSERT ON `tblbook` FOR EACH ROW BEGIN
  IF NEW.Avail_qty>NEW.Total_qty THEN SIGNAL SQLSTATE '45000'; END IF;
  IF NEW.Total_qty<=0 THEN SIGNAL SQLSTATE '45000'; END IF;
  IF NEW.Avail_qty<0 THEN SIGNAL SQLSTATE '45000'; END IF;
  IF NEW.Status IS NULL THEN SET NEW.Status='IN_LIBRARY'; END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `tr_book_before_update` BEFORE UPDATE ON `tblbook` FOR EACH ROW BEGIN
  IF NEW.Avail_qty>NEW.Total_qty THEN SIGNAL SQLSTATE '45000'; END IF;
  IF NEW.Total_qty<=0 THEN SIGNAL SQLSTATE '45000'; END IF;
  IF NEW.Avail_qty<0 THEN SIGNAL SQLSTATE '45000'; END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `tblbook_trans`
--

DROP TABLE IF EXISTS `tblbook_trans`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tblbook_trans` (
  `trans_Id` int NOT NULL AUTO_INCREMENT COMMENT '借还记录ID',
  `book_Id` int NOT NULL COMMENT '图书ID',
  `cardNum` varchar(10) NOT NULL COMMENT '一卡通号',
  `Borrow_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '借出时间',
  `Return_time` datetime DEFAULT NULL COMMENT '归还时间',
  `Due_time` datetime NOT NULL COMMENT '应还时间',
  `Status` enum('BORROWED','RETURNED','OVERDUE') DEFAULT 'BORROWED' COMMENT '状态',
  `Renew_count` int DEFAULT '0' COMMENT '续借次数',
  `Remarks` text COMMENT '备注信息',
  PRIMARY KEY (`trans_Id`),
  KEY `idx_borrow_user_status` (`cardNum`,`Status`),
  KEY `idx_borrow_book_status` (`book_Id`,`Status`),
  KEY `idx_borrow_due_time` (`Due_time`,`Status`),
  KEY `idx_borrow_borrow_time` (`Borrow_time`),
  KEY `idx_borrow_return_time` (`Return_time`),
  KEY `idx_borrow_renew_count` (`Renew_count`),
  KEY `idx_borrow_user_time` (`cardNum`,`Borrow_time`),
  KEY `idx_borrow_book_time` (`book_Id`,`Borrow_time`),
  CONSTRAINT `fk_bktrans_book` FOREIGN KEY (`book_Id`) REFERENCES `tblbook` (`book_Id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_bktrans_user` FOREIGN KEY (`cardNum`) REFERENCES `tbluser` (`cardNum`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='图书借还记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tblbook_trans`
--

LOCK TABLES `tblbook_trans` WRITE;
/*!40000 ALTER TABLE `tblbook_trans` DISABLE KEYS */;
INSERT INTO `tblbook_trans` VALUES (1,1,'213230001','2025-09-19 12:22:41',NULL,'2025-10-19 12:22:41','BORROWED',0,NULL),(2,2,'213230001','2025-09-19 18:07:12',NULL,'2025-10-19 18:07:12','BORROWED',0,NULL),(3,5,'213230001','2025-09-19 18:07:17',NULL,'2025-10-19 18:07:17','BORROWED',0,NULL),(4,4,'213230002','2025-09-19 18:07:40','2025-09-19 18:07:51','2025-10-19 18:07:40','RETURNED',0,NULL),(5,8,'213230002','2025-09-19 18:07:44',NULL,'2025-10-19 18:07:44','BORROWED',0,NULL),(6,3,'213230003','2025-09-19 18:09:24',NULL,'2025-10-19 18:09:24','BORROWED',0,NULL),(7,7,'213230003','2025-09-19 18:09:27',NULL,'2025-10-19 18:09:27','BORROWED',0,NULL),(8,4,'213230003','2025-09-19 18:09:31',NULL,'2025-10-19 18:09:31','BORROWED',0,NULL),(9,9,'213230003','2025-09-19 18:09:34',NULL,'2025-10-19 18:09:34','BORROWED',0,NULL),(10,17,'213230004','2025-09-19 18:09:51',NULL,'2025-10-19 18:09:51','BORROWED',0,NULL),(11,24,'213230005','2025-09-19 18:10:14',NULL,'2025-10-19 18:10:14','BORROWED',0,NULL),(12,6,'213230006','2025-09-19 18:11:09',NULL,'2025-10-19 18:11:09','BORROWED',0,NULL);
/*!40000 ALTER TABLE `tblbook_trans` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `tr_book_trans_after_insert` AFTER INSERT ON `tblbook_trans` FOR EACH ROW BEGIN
  IF NEW.Status='BORROWED' THEN
     UPDATE tblBook SET Status='BORROWED' WHERE book_Id=NEW.book_Id;
  END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `tr_book_trans_after_update` AFTER UPDATE ON `tblbook_trans` FOR EACH ROW BEGIN
  IF NEW.Status='RETURNED' AND OLD.Status<>'RETURNED' THEN
     UPDATE tblBook SET Status='IN_LIBRARY' WHERE book_Id=NEW.book_Id;
  END IF;
  IF NEW.Status='OVERDUE' AND OLD.Status='BORROWED' THEN
     UPDATE tblBook SET Status='BORROWED' WHERE book_Id=NEW.book_Id;
  END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `tblbookcategory`
--

DROP TABLE IF EXISTS `tblbookcategory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tblbookcategory` (
  `category_id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_code` varchar(2) NOT NULL COMMENT '分类代码（A-Z）',
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  `description` text COMMENT '分类描述',
  `sort_order` int DEFAULT '0' COMMENT '排序顺序',
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `category_code` (`category_code`),
  KEY `idx_category_code` (`category_code`),
  KEY `idx_category_name` (`category_name`),
  KEY `idx_category_sort` (`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='图书分类表（中图法标准）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tblbookcategory`
--

LOCK TABLES `tblbookcategory` WRITE;
/*!40000 ALTER TABLE `tblbookcategory` DISABLE KEYS */;
INSERT INTO `tblbookcategory` VALUES (1,'A','马克思主义、列宁主义、毛泽东思想、邓小平理论',NULL,1),(2,'B','哲学、宗教',NULL,2),(3,'C','社会科学总论',NULL,3),(4,'D','政治、法律',NULL,4),(5,'E','军事',NULL,5),(6,'F','经济',NULL,6),(7,'G','文化、科学、教育、体育',NULL,7),(8,'H','语言、文字',NULL,8),(9,'I','文学',NULL,9),(10,'J','艺术',NULL,10),(11,'K','历史、地理',NULL,11),(12,'N','自然科学总论',NULL,12),(13,'O','数理科学和化学',NULL,13),(14,'P','天文学、地球科学',NULL,14),(15,'Q','生物科学',NULL,15),(16,'R','医药、卫生',NULL,16),(17,'S','农业科学',NULL,17),(18,'T','工业技术',NULL,18),(19,'U','交通运输',NULL,19),(20,'V','航空、航天',NULL,20),(21,'X','环境科学、安全科学',NULL,21),(22,'Z','综合性图书',NULL,22);
/*!40000 ALTER TABLE `tblbookcategory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tblbookrecommendation`
--

DROP TABLE IF EXISTS `tblbookrecommendation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tblbookrecommendation` (
  `rec_id` int NOT NULL AUTO_INCREMENT COMMENT '荐购ID',
  `cardNum` varchar(10) NOT NULL COMMENT '荐购用户卡号',
  `book_title` varchar(100) NOT NULL COMMENT '书名',
  `book_author` varchar(50) NOT NULL COMMENT '作者',
  `book_publisher` varchar(100) DEFAULT NULL COMMENT '出版社',
  `book_isbn` varchar(20) DEFAULT NULL COMMENT 'ISBN',
  `book_category` varchar(2) DEFAULT NULL COMMENT '分类代码',
  `recommend_qty` int NOT NULL DEFAULT '1' COMMENT '推荐购买数量',
  `recommend_reason` text NOT NULL COMMENT '荐购理由',
  `recommend_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '荐购时间',
  `status` enum('PENDING','APPROVED','REJECTED','PURCHASED') DEFAULT 'PENDING' COMMENT '状态',
  `admin_feedback` text COMMENT '管理员反馈',
  `admin_cardNum` varchar(10) DEFAULT NULL COMMENT '处理管理员卡号',
  `process_time` datetime DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`rec_id`),
  KEY `idx_recommendation_user` (`cardNum`),
  KEY `idx_recommendation_status` (`status`),
  KEY `idx_recommendation_time` (`recommend_time`),
  KEY `idx_recommendation_isbn` (`book_isbn`),
  KEY `idx_recommendation_title` (`book_title`),
  KEY `idx_recommendation_user_status` (`cardNum`,`status`),
  KEY `idx_recommendation_admin` (`admin_cardNum`),
  CONSTRAINT `fk_recommendation_user` FOREIGN KEY (`cardNum`) REFERENCES `tbluser` (`cardNum`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='图书荐购表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tblbookrecommendation`
--

LOCK TABLES `tblbookrecommendation` WRITE;
/*!40000 ALTER TABLE `tblbookrecommendation` DISABLE KEYS */;
/*!40000 ALTER TABLE `tblbookrecommendation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tblborrowrule`
--

DROP TABLE IF EXISTS `tblborrowrule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tblborrowrule` (
  `rule_id` int NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `user_type` enum('student','staff','manager') NOT NULL COMMENT '用户类型',
  `max_borrow_count` int DEFAULT '5' COMMENT '最大借阅数量',
  `max_borrow_days` int DEFAULT '30' COMMENT '最大借阅天数',
  `max_renew_count` int DEFAULT '2' COMMENT '最大续借次数',
  `renew_extend_days` int DEFAULT '15' COMMENT '续借延长天数',
  `overdue_fine` decimal(5,2) DEFAULT '0.50' COMMENT '逾期罚金（每天）',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  PRIMARY KEY (`rule_id`),
  KEY `idx_borrow_rule_user_type` (`user_type`),
  KEY `idx_borrow_rule_active` (`is_active`),
  KEY `idx_borrow_rule_composite` (`user_type`,`is_active`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='借阅规则配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tblborrowrule`
--

LOCK TABLES `tblborrowrule` WRITE;
/*!40000 ALTER TABLE `tblborrowrule` DISABLE KEYS */;
INSERT INTO `tblborrowrule` VALUES (1,'student',5,30,2,15,0.50,1),(2,'staff',8,45,3,20,0.30,1);
/*!40000 ALTER TABLE `tblborrowrule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tblcard`
--

DROP TABLE IF EXISTS `tblcard`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tblcard` (
  `card_Id` int NOT NULL AUTO_INCREMENT COMMENT '卡号',
  `cardNum` varchar(10) NOT NULL COMMENT '所属用户',
  `balance` decimal(10,2) DEFAULT '0.00' COMMENT '余额',
  `status` enum('正常','挂失','注销') DEFAULT '正常' COMMENT '状态',
  PRIMARY KEY (`card_Id`),
  KEY `fk_card_user` (`cardNum`),
  CONSTRAINT `fk_card_user` FOREIGN KEY (`cardNum`) REFERENCES `tbluser` (`cardNum`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='一卡通表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tblcard`
--

LOCK TABLES `tblcard` WRITE;
/*!40000 ALTER TABLE `tblcard` DISABLE KEYS */;
INSERT INTO `tblcard` VALUES (1,'111111',0.00,'正常'),(2,'213230001',387.20,'正常'),(3,'101010001',0.00,'正常'),(4,'213230000',0.00,'正常'),(5,'101010002',0.00,'正常'),(6,'101010003',0.00,'正常'),(7,'101010004',0.00,'正常'),(8,'213230002',37.10,'正常'),(9,'213230003',0.00,'正常'),(10,'101010005',0.00,'正常'),(11,'213230006',0.00,'正常'),(12,'213230005',0.00,'正常'),(13,'101010006',0.00,'正常'),(14,'213230004',0.00,'正常'),(15,'213233000',0.00,'正常'),(16,'213230008',0.00,'正常'),(17,'222222',0.00,'正常'),(18,'333333',0.00,'正常');
/*!40000 ALTER TABLE `tblcard` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tblcard_trans`
--

DROP TABLE IF EXISTS `tblcard_trans`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tblcard_trans` (
  `trans_id` int NOT NULL AUTO_INCREMENT COMMENT '交易记录ID',
  `cardNum` varchar(10) NOT NULL COMMENT '一卡通号',
  `Trans_time` datetime(6) DEFAULT CURRENT_TIMESTAMP(6) COMMENT '交易时间',
  `Trans_type` enum('RECHARGE','CONSUME','REFUND') NOT NULL COMMENT '交易类型',
  `Amount` decimal(8,2) NOT NULL DEFAULT '0.00' COMMENT '交易金额',
  PRIMARY KEY (`trans_id`),
  KEY `idx_cardnum_time` (`cardNum`,`Trans_time`),
  CONSTRAINT `fk_ct_user` FOREIGN KEY (`cardNum`) REFERENCES `tbluser` (`cardNum`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='一卡通交易记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tblcard_trans`
--

LOCK TABLES `tblcard_trans` WRITE;
/*!40000 ALTER TABLE `tblcard_trans` DISABLE KEYS */;
INSERT INTO `tblcard_trans` VALUES (1,'213230001','2025-09-11 20:30:41.843764','RECHARGE',500.00),(2,'213230001','2025-09-11 20:30:57.800347','CONSUME',59.00),(3,'213230002','2025-09-13 09:42:25.459486','RECHARGE',50.00),(4,'213230002','2025-09-13 09:42:31.046082','CONSUME',12.90),(5,'213230001','2025-09-13 09:50:14.757655','CONSUME',12.90),(6,'213230001','2025-09-16 09:34:01.189102','CONSUME',1.00),(7,'213230001','2025-09-16 22:48:05.409567','CONSUME',39.90);
/*!40000 ALTER TABLE `tblcard_trans` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tblcomment`
--

DROP TABLE IF EXISTS `tblcomment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tblcomment` (
  `commentId` int NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `cardNum` varchar(10) NOT NULL COMMENT '卡号',
  `postTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `content` varchar(60) NOT NULL COMMENT '帖子内容',
  `likeCount` int NOT NULL DEFAULT '0' COMMENT '赞数',
  PRIMARY KEY (`commentId`),
  KEY `idx_cardNum` (`cardNum`),
  KEY `idx_postTime` (`postTime`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='校园集市评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tblcomment`
--

LOCK TABLES `tblcomment` WRITE;
/*!40000 ALTER TABLE `tblcomment` DISABLE KEYS */;
INSERT INTO `tblcomment` VALUES (1,'001','2025-09-17 19:42:52','求购二手自行车，价格面议',0),(2,'002','2025-09-17 19:42:52','出售大学英语教材，九成新',1),(3,'003','2025-09-17 19:42:52','寻找学习伙伴，一起准备期末考试',1),(4,'001','2025-09-17 19:42:52','出租宿舍床位，限女生',1),(5,'004','2025-09-17 19:42:52','求推荐好的编程学习资源',3),(6,'213230001','2025-09-17 19:45:47','我是不白吃，我真是太有文化了，嘤嘤嘤',1),(12,'213230001','2025-09-17 20:03:18','不白吃',1),(13,'213230001','2025-09-18 15:04:27','奶龙',1);
/*!40000 ALTER TABLE `tblcomment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tblcommentlike`
--

DROP TABLE IF EXISTS `tblcommentlike`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tblcommentlike` (
  `likeId` int NOT NULL AUTO_INCREMENT COMMENT '点赞记录ID',
  `commentId` int NOT NULL COMMENT '评论ID',
  `cardNum` varchar(10) NOT NULL COMMENT '点赞用户卡号',
  `likeTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`likeId`),
  UNIQUE KEY `uk_comment_user` (`commentId`,`cardNum`) COMMENT '每个用户对每个评论只能点赞一次',
  KEY `idx_commentId` (`commentId`),
  KEY `idx_cardNum` (`cardNum`),
  CONSTRAINT `tblcommentlike_ibfk_1` FOREIGN KEY (`commentId`) REFERENCES `tblcomment` (`commentId`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论点赞记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tblcommentlike`
--

LOCK TABLES `tblcommentlike` WRITE;
/*!40000 ALTER TABLE `tblcommentlike` DISABLE KEYS */;
INSERT INTO `tblcommentlike` VALUES (25,5,'213230002','2025-09-17 20:44:55'),(28,5,'213230003','2025-09-17 20:45:21'),(34,2,'213230001','2025-09-17 22:10:45'),(35,3,'213230001','2025-09-17 22:10:46'),(36,4,'213230001','2025-09-17 22:10:47'),(37,12,'213230002','2025-09-17 22:11:28'),(38,6,'213230002','2025-09-17 22:11:29'),(41,5,'213230001','2025-09-17 22:12:05'),(45,13,'213230002','2025-09-18 15:05:00');
/*!40000 ALTER TABLE `tblcommentlike` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tblcourse`
--

DROP TABLE IF EXISTS `tblcourse`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tblcourse` (
  `course_Id` int NOT NULL AUTO_INCREMENT COMMENT '课程号',
  `courseName` varchar(20) NOT NULL COMMENT '课程名',
  `Credit` tinyint NOT NULL COMMENT '学分',
  `Department` varchar(20) DEFAULT NULL COMMENT '开课学院',
  PRIMARY KEY (`course_Id`),
  UNIQUE KEY `courseName` (`courseName`),
  CONSTRAINT `tblcourse_chk_1` CHECK ((`Credit` between 1 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tblcourse`
--

LOCK TABLES `tblcourse` WRITE;
/*!40000 ALTER TABLE `tblcourse` DISABLE KEYS */;
INSERT INTO `tblcourse` VALUES (1,'计算机组成原理',4,'计算机学院'),(2,'数据结构',4,'计算机学院'),(3,'工科数分',5,'数学学院'),(4,'大学英语',2,'外国语学院'),(5,'大学物理',3,'物理学院'),(7,'马原',3,'马克思主义学院'),(10,'线性代数',3,'数学学院'),(11,'毛概',3,'马克思主义学院'),(12,'习概',3,'马克思主义学院'),(13,'操作系统',4,'计算机学院'),(14,'计算机网络',3,'计算机学院'),(15,'编译原理',4,'软件学院'),(16,'C++程序设计',4,'计算机学院'),(19,'人工智能',2,'人工智能学院'),(20,'数电',3,'电院'),(21,'高等数学',1,'数学学院');
/*!40000 ALTER TABLE `tblcourse` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tbledu_evaluate`
--

DROP TABLE IF EXISTS `tbledu_evaluate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbledu_evaluate` (
  `eval_Id` int NOT NULL AUTO_INCREMENT COMMENT '评教记录ID',
  `section_Id` int NOT NULL COMMENT '被评价教学班',
  `student_Id` char(8) NOT NULL COMMENT '评价学生学号',
  `Score` decimal(5,2) DEFAULT '0.00' COMMENT '评教分数',
  `Eval_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '评教时间',
  PRIMARY KEY (`eval_Id`),
  UNIQUE KEY `unique_evaluation` (`section_Id`,`student_Id`),
  KEY `fk_eval_student` (`student_Id`),
  CONSTRAINT `fk_eval_section` FOREIGN KEY (`section_Id`) REFERENCES `tblsection` (`section_Id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_eval_student` FOREIGN KEY (`student_Id`) REFERENCES `tblstudent` (`student_Id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教学评价表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbledu_evaluate`
--

LOCK TABLES `tbledu_evaluate` WRITE;
/*!40000 ALTER TABLE `tbledu_evaluate` DISABLE KEYS */;
INSERT INTO `tbledu_evaluate` VALUES (2,3,'09023521',9.00,'2025-09-13 10:26:32'),(3,3,'09023502',8.00,'2025-09-13 09:40:08'),(5,1,'09023521',9.00,'2025-09-13 18:42:29'),(6,6,'09023521',8.00,'2025-09-11 21:42:20'),(7,6,'09023503',9.00,'2025-09-11 21:43:04'),(8,1,'09023505',8.00,'2025-09-11 22:30:04'),(11,4,'09023521',8.36,'2025-09-13 10:28:03'),(12,4,'09023502',9.45,'2025-09-16 10:13:44'),(13,4,'09023506',8.27,'2025-09-13 17:10:23'),(14,1,'09023504',8.00,'2025-09-13 18:41:25');
/*!40000 ALTER TABLE `tbledu_evaluate` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tblenrollment`
--

DROP TABLE IF EXISTS `tblenrollment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tblenrollment` (
  `student_Id` char(8) NOT NULL,
  `section_Id` int NOT NULL,
  `Select_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
  `Score` smallint DEFAULT '0' COMMENT '成绩',
  `GPA` decimal(5,4) DEFAULT '0.0000' COMMENT '绩点',
  PRIMARY KEY (`student_Id`,`section_Id`),
  KEY `fk_enr_section` (`section_Id`),
  CONSTRAINT `fk_enr_section` FOREIGN KEY (`section_Id`) REFERENCES `tblsection` (`section_Id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_enr_student` FOREIGN KEY (`student_Id`) REFERENCES `tblstudent` (`student_Id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `tblenrollment_chk_1` CHECK ((`Score` between 0 and 100)),
  CONSTRAINT `tblenrollment_chk_2` CHECK ((`GPA` between 0 and 4.8000))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='选课及成绩表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tblenrollment`
--

LOCK TABLES `tblenrollment` WRITE;
/*!40000 ALTER TABLE `tblenrollment` DISABLE KEYS */;
INSERT INTO `tblenrollment` VALUES ('09023502',2,'2025-09-17 10:07:51',88,3.8000),('09023502',3,'2025-09-11 16:59:21',95,4.5000),('09023502',4,'2025-09-13 10:41:57',83,3.5000),('09023503',6,'2025-09-11 21:42:36',93,4.5000),('09023504',1,'2025-09-13 18:41:19',84,3.5000),('09023505',1,'2025-09-11 22:30:00',85,3.5000),('09023505',2,'2025-09-11 22:32:36',86,3.8000),('09023506',4,'2025-09-13 17:10:15',85,3.5000),('09023506',5,'2025-09-11 22:14:58',91,4.0000),('09023506',6,'2025-09-11 22:15:03',88,3.8000),('09023521',1,'2025-09-13 09:50:39',88,3.8000),('09023521',2,'2025-09-11 14:14:35',92,4.0000),('09023521',3,'2025-09-11 14:14:41',98,4.8000),('09023521',4,'2025-09-11 14:14:50',83,3.5000),('09023521',5,'2025-09-11 14:14:59',85,3.5000),('09023521',6,'2025-09-11 16:49:50',90,4.0000);
/*!40000 ALTER TABLE `tblenrollment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tblmybookshelf`
--

DROP TABLE IF EXISTS `tblmybookshelf`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tblmybookshelf` (
  `shelf_id` int NOT NULL AUTO_INCREMENT COMMENT '书架ID',
  `cardNum` varchar(10) NOT NULL COMMENT '用户卡号',
  `book_id` int NOT NULL COMMENT '图书ID',
  `category_name` varchar(50) NOT NULL COMMENT '自定义分类名称',
  `add_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `notes` text COMMENT '个人备注',
  PRIMARY KEY (`shelf_id`),
  UNIQUE KEY `uk_user_book` (`cardNum`,`book_id`) COMMENT '同一用户同一图书只能收藏一次',
  KEY `idx_mybookshelf_user` (`cardNum`),
  KEY `idx_mybookshelf_book` (`book_id`),
  KEY `idx_mybookshelf_category` (`category_name`),
  KEY `idx_mybookshelf_add_time` (`add_time`),
  KEY `fk_bookshelf_category` (`cardNum`,`category_name`),
  CONSTRAINT `fk_bookshelf_category` FOREIGN KEY (`cardNum`, `category_name`) REFERENCES `tbluserpersonalcategory` (`cardNum`, `category_name`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_mybookshelf_book` FOREIGN KEY (`book_id`) REFERENCES `tblbook` (`book_Id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_mybookshelf_user` FOREIGN KEY (`cardNum`) REFERENCES `tbluser` (`cardNum`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='我的书架表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tblmybookshelf`
--

LOCK TABLES `tblmybookshelf` WRITE;
/*!40000 ALTER TABLE `tblmybookshelf` DISABLE KEYS */;
/*!40000 ALTER TABLE `tblmybookshelf` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tblproduct`
--

DROP TABLE IF EXISTS `tblproduct`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tblproduct` (
  `product_Id` int NOT NULL AUTO_INCREMENT COMMENT '商品号',
  `Product_code` varchar(20) NOT NULL COMMENT '商品编码',
  `Productname` varchar(50) DEFAULT NULL COMMENT '商品名',
  `Price` decimal(8,2) NOT NULL COMMENT '单价',
  `Stock` int DEFAULT '0' COMMENT '库存数量',
  `Product_status` enum('ON_SHELF','OFF_SHELF','SOLD_OUT') DEFAULT 'ON_SHELF' COMMENT '商品状态',
  `Product_description` text COMMENT '商品描述',
  `Product_category` varchar(50) DEFAULT NULL COMMENT '商品分类',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`product_Id`),
  UNIQUE KEY `Product_code` (`Product_code`)
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tblproduct`
--

LOCK TABLES `tblproduct` WRITE;
/*!40000 ALTER TABLE `tblproduct` DISABLE KEYS */;
INSERT INTO `tblproduct` VALUES (37,'ELEC-001','Type-C快充数据线',19.90,189,'ON_SHELF','1米线长，支持PD 60W快充','ELEC','2025-09-16 22:38:31'),(38,'ELEC-002','蓝牙耳机',129.00,50,'ON_SHELF','入耳式，ENC降噪，续航20小时','ELEC','2025-09-16 22:38:31'),(39,'ELEC-003','移动电源 10000mAh',129.00,40,'OFF_SHELF','22.5W快充，双向PD','ELEC','2025-09-16 22:38:31'),(40,'BOOK-001','算法导论(第3版)',99.00,20,'ON_SHELF','经典算法教材，偏理论+实践','BOOK','2025-09-16 22:38:32'),(41,'BOOK-002','Java核心技术 卷I',109.00,10,'ON_SHELF','Java SE基础，示例丰富','BOOK','2025-09-16 22:38:32'),(42,'FOOD-001','挂耳咖啡 10袋',49.90,80,'ON_SHELF','阿拉比卡豆，中度烘焙','FOOD','2025-09-16 22:38:32'),(43,'FOOD-002','每日坚果 750g',79.90,301,'ON_SHELF','六种坚果混合装','FOOD','2025-09-16 22:38:32'),(44,'LIFE-001','保温杯 500ml',59.00,120,'ON_SHELF','316不锈钢，长效保温','LIFE','2025-09-16 22:38:32'),(45,'LIFE-002','充电式台灯',89.00,30,'ON_SHELF','三色温，触控调光','LIFE','2025-09-16 22:38:32'),(46,'SPORT-001','瑜伽垫 6mm',69.00,60,'ON_SHELF','TPE材质，防滑耐用','SPORT','2025-09-16 22:38:32'),(47,'SPORT-002','运动水壶 800ml',39.90,50,'ON_SHELF','不含BPA，防漏设计','SPORT','2025-09-16 22:38:32'),(48,'STAT-001','A5笔记本 120页',12.90,292,'ON_SHELF','米色护眼纸张','STAT','2025-09-16 22:38:32'),(49,'STAT-002','中性笔 0.5mm(12支)',15.90,220,'ON_SHELF','顺滑耐用，学生办公适用','STAT','2025-09-16 22:38:32'),(50,'ELEC-004','无线鼠标',79.00,150,'ON_SHELF','人体工学无线鼠标，适配多平台','ELEC','2025-09-16 22:38:32'),(51,'ELEC-005','蓝牙耳机2.0',199.00,80,'ON_SHELF','降噪蓝牙耳机，续航 30 小时','ELEC','2025-09-16 22:38:32'),(52,'BOOK-003','Java 核心技术 卷II',59.00,198,'ON_SHELF','适合初学者的 Java 入门教材','BOOK','2025-09-16 22:38:32'),(53,'BOOK-004','数据结构与算法之美',89.00,60,'ON_SHELF','经典算法参考与练习题','BOOK','2025-09-16 22:38:32'),(54,'FOOD-003','燕麦能量棒（10 支装）',29.90,300,'ON_SHELF','早餐能量补给，低糖配方','FOOD','2025-09-16 22:38:32'),(55,'FOOD-004','即食绿豆汤（瓶装）',6.50,120,'ON_SHELF','大学生易保存即食甜品','FOOD','2025-09-16 22:38:32'),(56,'LIFE-003','智能体脂称',59.00,90,'ON_SHELF','精准测量，健康保障','LIFE','2025-09-16 22:38:32'),(57,'STAT-003','便携式文件夹',19.90,400,'ON_SHELF','多层设计，方便分类存放文件，适合学生和办公使用','STAT','2025-09-16 22:38:32'),(58,'SPORT-003','运动手环',89.00,40,'ON_SHELF','实时监测运动数据，记录步数与距离，防水设计，适合跑步爱好者','SPORT','2025-09-16 22:38:32'),(59,'ELEC-006','USB-C 充电器 30W',49.00,130,'ON_SHELF','快充，支持多设备','ELEC','2025-09-16 22:38:32'),(60,'ELEC-007','智能手表',99.00,100,'ON_SHELF','健康监测，运动追踪','ELEC','2025-09-16 22:38:32'),(61,'ELEC-008','蓝牙音箱',149.00,60,'ON_SHELF','便携式，高保真音质','ELEC','2025-09-16 22:38:32'),(62,'BOOK-005','Python编程从入门到实践',89.00,120,'ON_SHELF','适合初学者的Python教材','BOOK','2025-09-16 22:38:32'),(63,'BOOK-006','C++ Primer',129.00,30,'ON_SHELF','C++基础与高级应用','BOOK','2025-09-16 22:38:32'),(64,'FOOD-005','巧克力棒（12 支装）',39.90,150,'ON_SHELF','多种口味，美味可口','FOOD','2025-09-16 22:38:32'),(65,'FOOD-006','水果干礼盒',59.90,90,'ON_SHELF','多种水果干混合装','FOOD','2025-09-16 22:38:32'),(66,'LIFE-004','香薰机',129.00,50,'ON_SHELF','加湿与香薰二合一','LIFE','2025-09-16 22:38:32'),(67,'LIFE-005','按摩器',199.00,40,'ON_SHELF','颈部按摩，缓解疲劳','LIFE','2025-09-16 22:38:32'),(68,'SPORT-004','运动护膝',49.90,80,'ON_SHELF','保护膝盖，适合运动','SPORT','2025-09-16 22:38:32'),(69,'SPORT-005','健身手套',39.90,100,'ON_SHELF','防滑耐磨，适合健身','SPORT','2025-09-16 22:38:32'),(70,'STAT-004','便签本套装',18.90,350,'ON_SHELF','多种颜色，方便分类','STAT','2025-09-16 22:38:32'),(71,'STAT-005','彩色文件夹',9.90,500,'ON_SHELF','多种颜色，整理文件','STAT','2025-09-16 22:38:32'),(72,'DAILY-001','收纳盒（大号）',39.00,70,'ON_SHELF','多层收纳，整理宿舍好帮手','DAILY','2025-09-16 22:38:32'),(73,'DAILY-002','垃圾袋（50个装）',12.90,200,'ON_SHELF','环保材料，家庭适用','DAILY','2025-09-16 22:38:32'),(75,'FOOD-520','我是不白吃藕的脆片',8.00,520,'ON_SHELF','好吃又会吃，就是不白吃','FOOD','2025-09-16 22:39:02'),(76,'BOOK-090','不白吃话山海经',18.80,250,'ON_SHELF','嘤嘤嘤','BOOK','2025-09-16 22:40:23'),(77,'LIFE-111','不白吃抱枕',39.90,249,'ON_SHELF','我真是太有文化了','LIFE','2025-09-16 22:48:05');
/*!40000 ALTER TABLE `tblproduct` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tblproduct_trans`
--

DROP TABLE IF EXISTS `tblproduct_trans`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tblproduct_trans` (
  `trans_Id` int NOT NULL AUTO_INCREMENT COMMENT '商品交易ID',
  `product_Id` int NOT NULL COMMENT '商品号',
  `cardNum` varchar(10) NOT NULL COMMENT '购买人ID',
  `Qty` int NOT NULL COMMENT '购买数量',
  `Amount` decimal(10,2) NOT NULL COMMENT '实付金额',
  `Trans_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
  `status` enum('CREATED','PAID','CANCELLED','REFUNDED') DEFAULT 'CREATED' COMMENT '订单状态',
  PRIMARY KEY (`trans_Id`),
  KEY `fk_pdtrans_product` (`product_Id`),
  KEY `fk_pdtrans_user` (`cardNum`),
  CONSTRAINT `fk_pdtrans_product` FOREIGN KEY (`product_Id`) REFERENCES `tblproduct` (`product_Id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_pdtrans_user` FOREIGN KEY (`cardNum`) REFERENCES `tbluser` (`cardNum`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品交易记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tblproduct_trans`
--

LOCK TABLES `tblproduct_trans` WRITE;
/*!40000 ALTER TABLE `tblproduct_trans` DISABLE KEYS */;
INSERT INTO `tblproduct_trans` VALUES (5,77,'213230001',1,39.90,'2025-09-16 22:48:05','PAID');
/*!40000 ALTER TABLE `tblproduct_trans` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tblproductcategory`
--

DROP TABLE IF EXISTS `tblproductcategory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tblproductcategory` (
  `category_id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_code` varchar(10) NOT NULL COMMENT '分类代码（自定义，唯一）',
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  `sort_order` int DEFAULT '0' COMMENT '排序顺序',
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `category_code` (`category_code`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tblproductcategory`
--

LOCK TABLES `tblproductcategory` WRITE;
/*!40000 ALTER TABLE `tblproductcategory` DISABLE KEYS */;
INSERT INTO `tblproductcategory` VALUES (1,'ELEC','数码电器',10),(2,'BOOK','图书教材',20),(3,'FOOD','零食饮料',30),(4,'LIFE','生活用品',40),(5,'SPORT','运动户外',50),(6,'STAT','文具办公',60),(7,'DAILY','日用收纳',70);
/*!40000 ALTER TABLE `tblproductcategory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tblsection`
--

DROP TABLE IF EXISTS `tblsection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tblsection` (
  `section_Id` int NOT NULL AUTO_INCREMENT COMMENT '教学班号',
  `course_Id` int NOT NULL COMMENT '课程号',
  `Term` varchar(20) DEFAULT NULL COMMENT '学期',
  `Teacher_id` varchar(20) DEFAULT NULL COMMENT '任课老师工号',
  `Room` varchar(20) DEFAULT NULL COMMENT '教室',
  `Capacity` smallint NOT NULL COMMENT '课程容量',
  `Schedule` varchar(50) DEFAULT NULL COMMENT '上课时间',
  PRIMARY KEY (`section_Id`),
  KEY `fk_section_course` (`course_Id`),
  KEY `fk_section_teacher` (`Teacher_id`),
  CONSTRAINT `fk_section_course` FOREIGN KEY (`course_Id`) REFERENCES `tblcourse` (`course_Id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_section_teacher` FOREIGN KEY (`Teacher_id`) REFERENCES `tblstaff` (`staff_Id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教学班表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tblsection`
--

LOCK TABLES `tblsection` WRITE;
/*!40000 ALTER TABLE `tblsection` DISABLE KEYS */;
INSERT INTO `tblsection` VALUES (1,1,'2025秋','10123001','教一311',160,'周一3-5节，周四3-5节'),(2,2,'2025秋','10123001','教四203',80,'周一6-7节，周四8-10节'),(3,3,'2025秋','10123002','教八404',150,'周一1-2节，周三3-4节，周五1-2节'),(4,4,'2025秋','10123003','教七101',60,'周四1-2节'),(5,5,'2025秋','10123004','教六303',70,'周二1-2节，周五3-4节'),(6,7,'2025秋','10123005','教二109',100,'周二3-5节'),(7,16,'2025秋','10123001','教一404',60,'周五6-8节'),(9,13,'2025秋','10123001','教八301',90,'周一8-10节');
/*!40000 ALTER TABLE `tblsection` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tblstaff`
--

DROP TABLE IF EXISTS `tblstaff`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tblstaff` (
  `cardNum` varchar(10) NOT NULL COMMENT '登录 ID，外键',
  `staff_Id` char(8) NOT NULL COMMENT '工号',
  `Title` varchar(10) DEFAULT NULL COMMENT '职称/职位',
  `Department` varchar(20) DEFAULT NULL COMMENT '学院',
  `WorkYear` char(4) DEFAULT NULL COMMENT '参工年份',
  PRIMARY KEY (`cardNum`),
  UNIQUE KEY `staff_Id` (`staff_Id`),
  KEY `idx_staff_id` (`staff_Id`),
  KEY `idx_staff_title` (`Title`),
  KEY `idx_staff_department` (`Department`),
  KEY `idx_staff_dept_title` (`Department`,`Title`),
  CONSTRAINT `fk_staff_user` FOREIGN KEY (`cardNum`) REFERENCES `tbluser` (`cardNum`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教职工扩展信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tblstaff`
--

LOCK TABLES `tblstaff` WRITE;
/*!40000 ALTER TABLE `tblstaff` DISABLE KEYS */;
INSERT INTO `tblstaff` VALUES ('101010001','10123001','副高','计软智','2001'),('101010002','10123002','正高','计软智','2010'),('101010003','10123003','教授','计软智','1999'),('101010004','10123004','副教授','计软智','1993'),('101010005','10123005','副高','计软智','2020'),('101010006','10123006','正高','计软智','2015'),('101010007','10123007','正高','计软智','2023'),('101010008','10123008','讲师','计软智','2003'),('101010009','10123009','硕导','计软智','1991'),('101010010','10123010','博导','计软智','2025');
/*!40000 ALTER TABLE `tblstaff` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tblstudent`
--

DROP TABLE IF EXISTS `tblstudent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tblstudent` (
  `cardNum` varchar(10) NOT NULL COMMENT '登录 ID，外键',
  `student_Id` char(8) NOT NULL COMMENT '学号',
  `EnrollmentYear` char(4) DEFAULT NULL COMMENT '入学年份',
  `Grade` int DEFAULT '1' COMMENT '年级',
  `Major` varchar(20) DEFAULT NULL COMMENT '专业',
  `Department` varchar(20) DEFAULT NULL COMMENT '学院',
  PRIMARY KEY (`cardNum`),
  UNIQUE KEY `student_Id` (`student_Id`),
  KEY `idx_student_id` (`student_Id`),
  KEY `idx_student_major` (`Major`),
  KEY `idx_student_department` (`Department`),
  KEY `idx_student_grade` (`Grade`),
  KEY `idx_student_major_grade` (`Major`,`Grade`),
  KEY `idx_student_dept_major` (`Department`,`Major`),
  CONSTRAINT `fk_stu_user` FOREIGN KEY (`cardNum`) REFERENCES `tbluser` (`cardNum`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生扩展信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tblstudent`
--

LOCK TABLES `tblstudent` WRITE;
/*!40000 ALTER TABLE `tblstudent` DISABLE KEYS */;
INSERT INTO `tblstudent` VALUES ('213230000','09023555','2025',1,'计算机','计软智'),('213230001','09023521','2023',1,'计算机','计软智'),('213230002','09023502','2023',1,'计算机','计软智'),('213230003','09023503','2023',1,'计算机','计软智'),('213230004','09023504','2023',1,'计算机','计软智'),('213230005','09023505','2023',1,'计算机','计软智'),('213230006','09023506','2023',1,'计算机','计软智'),('213230007','09023507','2023',1,'计算机','计软智'),('213230008','09023508','2023',1,'计算机','计软智'),('213230009','09023509','2023',1,'计算机','计软智'),('213230010','09023510','2023',1,'计算机','计软智'),('213230011','09023511','2023',1,'计算机','计软智'),('213230012','09023512','2023',1,'计算机','计软智');
/*!40000 ALTER TABLE `tblstudent` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tbluser`
--

DROP TABLE IF EXISTS `tbluser`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbluser` (
  `cardNum` varchar(10) NOT NULL COMMENT '登录 ID',
  `cardNumPassword` varchar(128) NOT NULL COMMENT '登录密码',
  `Name` varchar(10) DEFAULT NULL COMMENT '真实姓名',
  `BirthDate` char(7) DEFAULT NULL COMMENT '出生年月',
  `Age` int DEFAULT '20' COMMENT '年龄',
  `Gender` enum('男','女') NOT NULL DEFAULT '男' COMMENT '性别',
  `userType` enum('student','staff','manager') NOT NULL COMMENT '身份标志',
  `Phone` varchar(11) NOT NULL COMMENT '电话号码',
  `Ethnicity` varchar(10) DEFAULT NULL COMMENT '民族',
  `IdCard` char(18) DEFAULT NULL COMMENT '身份证号',
  `Hometown` varchar(40) DEFAULT NULL COMMENT '籍贯',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像图片路径',
  PRIMARY KEY (`cardNum`),
  KEY `idx_user_type` (`userType`),
  KEY `idx_user_name` (`Name`),
  KEY `idx_user_type_name` (`userType`,`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户总表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbluser`
--

LOCK TABLES `tbluser` WRITE;
/*!40000 ALTER TABLE `tbluser` DISABLE KEYS */;
INSERT INTO `tbluser` VALUES ('101010001','E/6XMNq6wm5PvMVFk7dmcg==:OZHoJHH42cPyZ0DlhQwmbIW/TVdH0wU7fAqy21WO0r4=','甲','1975-10',20,'男','staff','18551736817','汉','12345678901234557','江苏省南京市','C:\\Users\\zqpwx\\.vcampus\\avatars\\avatar_101010001_1757731523459.png'),('101010002','PfBVGvnEbLPhBCvxfyi84A==:k0vgjTvMvbUCNIpx7GkkSJhKZl/hdiLLk77ULCF3kkg=','乙','1985-02',20,'男','staff','15588262168','汉','12345678901234537','山东省济南市',NULL),('101010003','rPmLuOGMTA4aVPYgahFDeg==:4h9hI9zVdMVz9epKaSvUh7UlbHKfrVY2gGIV4bqWMMw=','丙','1974-06',20,'女','staff','17863236876','汉','12345678901234567','浙江省杭州市',NULL),('101010004','+R4ToQT5KTIsWl509BKo7Q==:jdHk/cPd87lzVRkeDMI+VSmz4nsMqEZ7Vd4JQiLcaYA=','丁','1968-12',20,'女','staff','16645912012','汉','12345678901234547','北京市',NULL),('101010005','/CrtTh3lbvHdyPB0iPhPJQ==:vKbTeBqk3Vtk192JoUjMhWc6TIcWxepJ230N0xY+nSs=','戊','1994-05',20,'男','staff','16301662042','汉','12345678901234517','上海市',NULL),('101010006','f5qyUwkN/xZa7dFUDAksRQ==:z5/0VCLAkw/CQUkuAsu8rPHJrCSmlu4g2s8LC4GO8lY=','己','1990-07',20,'女','staff','15957412071','汉','12345678901234587','天津市',NULL),('101010007','123456','庚','1998-11',20,'男','staff','15613162101','汉','12345678901234417','重庆市',NULL),('101010008','123456','辛','1978-08',20,'女','staff','15268912130','壮','12345678901234427','广西壮族自治区南宁市',NULL),('101010009','123456','壬','1966-10',20,'男','staff','14924662160','回','1234567890123459X','新疆维吾尔自治区乌鲁木齐市',NULL),('101010010','123456','癸','2000-01',20,'男','staff','14580412189','满','12345678901234115','黑龙江省漠河市',NULL),('101010520','BuBaiChi','不白吃','199301',20,'男','staff','12345678910','汉族','110101199301011237','广东省',NULL),('111111','hExtY8WOqCecvs9eLlQpXQ==:4VbBX7sdMI8kO3uezNQ18VSehbuZiH4+QGhyrQwpexU=','系统管理员','199501',20,'男','manager','1380000005','汉族','110101199501011236','北京市','C:\\Users\\zqpwx\\.vcampus\\avatars\\avatar_111111_1757691431194.png'),('213230000','78I6X95kGSC7YlM7SINvFg==:pjOgD2pN1u274bEQyX1KRjrQud2KgSAN+wIzgiVYs14=','不白吃','2019-07',20,'男','student','18551736817','汉','370402200501017834','北京市',NULL),('213230001','pLmM6RiKuX/CiqwIYm4HFQ==:gKWb2jQQ19rJONzvNy+InIpWT/LiplVFGq6rHD3Pp5o=','子','2004-10',20,'男','student','18551736817','汉','32010220041223961X','江苏省南京市','C:\\Users\\zqpwx\\.vcampus\\avatars\\avatar_213230001_1757732035720.png'),('213230002','+B2xyq0xXda8q8EPGFZlaw==:jFb2Hp3ORjPNn3u6yw62I7gV3hFTtkqjfXAKJdCk/9E=','可爱','2004-08',20,'男','student','15588262168','汉','212345678901234537','山东省济南市','C:\\Users\\zqpwx\\.vcampus\\avatars\\avatar_213230002_1758073788907.png'),('213230003','q5RsiGrnGp3qaJgPv6WYAQ==:f3l4AY0cqc2MXz68N8Ral+nv1w35a7knzicjcUIDDWQ=','寅','2004-09',20,'女','student','17863236876','汉','212345678901234567','浙江省杭州市',NULL),('213230004','aIjmaxtS6EYMaLftUaQUAw==:frcBlj+CW6CqdFpp63i74AciSy2B6cVYziE4ZidVYPQ=','卯','2004-10',20,'女','student','16645912012','汉','212345678901234557','北京市',NULL),('213230005','CgVc+AO0juKoCDlwTE1IjA==:fK94Ub6SApbZHFgSXU5LnghGBe3CfmafyrXexQxXidQ=','辰','2004-11',20,'男','student','16301662042','汉','212345678901234537','上海市',NULL),('213230006','fVQjtiNwiJDE6dSZhn24ww==:jl5z/8wmEllXlnsWPGU69JEx+ICTDWTejaiGp8PfIQI=','巳','2004-12',20,'女','student','15957412071','汉','212345678901234567','天津市',NULL),('213230007','123456','午','2005-01',20,'男','student','15613162101','汉','212345678901234557','重庆市',NULL),('213230008','+v8YKN+46M+5N5s2WWKnxA==:90SfzgDxEMEkG3lMC2mH9xqwgWQE+i7OLxEoQH5TGpQ=','未','2005-02',20,'女','student','15268912130','壮','212345678901234537','广西壮族自治区南宁市',NULL),('213230009','123456','申','2005-03',20,'男','student','14924662160','回','212345678901234567','宁夏回族自治区银川市',NULL),('213230010','123456','酉','2005-04',20,'男','student','14580412189','满','212345678901234557','黑龙江省漠河市',NULL),('213230011','123456','戌','2005-05',20,'女','student','14236162219','维吾尔','212345678901234537','新疆维吾尔自治区乌鲁木齐市',NULL),('213230012','123456','亥','2005-06',20,'男','student','13891912248','汉','212345678901234567','海南省三沙市',NULL),('213233000','rB+tlOYyGOMo6JZhYivrdA==:YDVZhNE0GSFjXVbp8GhjugMcmuL0epeUGpDvXFfNldM=','张三','200301',20,'男','student','1380000001','汉族','110101200301011234','北京市',NULL),('213233001','pwd0002','李四','200201',20,'女','student','1380000002','汉族','110101200201011235','上海市',NULL),('222222','P058OTEVhT7nMUIw4pKYFg==:UrmU7/6uEFVIwaWt4wlSiqe3H/b8sLtW67uNlSOBU5Q=','图书馆管理员','199501',20,'女','manager','1380000005','汉族','110101199501011236','北京市',NULL),('333333','ZX1nb3Gtb9ZAQCTxe/FtXg==:7r+hpt/wfAQtdmATkFTdxzJCRk7kJ1K5v3wK/7kzjmY=','商店管理员','199501',20,'女','manager','1380000005','汉族','110101199501011236','北京市',NULL);
/*!40000 ALTER TABLE `tbluser` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tbluserpersonalcategory`
--

DROP TABLE IF EXISTS `tbluserpersonalcategory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbluserpersonalcategory` (
  `category_id` int NOT NULL AUTO_INCREMENT COMMENT '个人分类ID',
  `cardNum` varchar(10) NOT NULL COMMENT '用户卡号',
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  `description` text COMMENT '分类描述',
  `color_code` varchar(7) DEFAULT '#3498db' COMMENT '分类颜色代码',
  `sort_order` int DEFAULT '0' COMMENT '排序顺序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_user_category` (`cardNum`,`category_name`) COMMENT '同一用户分类名称唯一',
  KEY `idx_personal_category_user` (`cardNum`),
  KEY `idx_personal_category_name` (`category_name`),
  KEY `idx_personal_category_sort` (`sort_order`),
  CONSTRAINT `fk_personal_category_user` FOREIGN KEY (`cardNum`) REFERENCES `tbluser` (`cardNum`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户个人分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tbluserpersonalcategory`
--

LOCK TABLES `tbluserpersonalcategory` WRITE;
/*!40000 ALTER TABLE `tbluserpersonalcategory` DISABLE KEYS */;
INSERT INTO `tbluserpersonalcategory` VALUES (1,'213230001','不白吃','从书架自动创建的分类','#3498db',999,NULL,NULL);
/*!40000 ALTER TABLE `tbluserpersonalcategory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `v_book_borrow_statistics`
--

DROP TABLE IF EXISTS `v_book_borrow_statistics`;
/*!50001 DROP VIEW IF EXISTS `v_book_borrow_statistics`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_book_borrow_statistics` AS SELECT 
 1 AS `book_Id`,
 1 AS `Title`,
 1 AS `Author`,
 1 AS `Category`,
 1 AS `category_name`,
 1 AS `Total_qty`,
 1 AS `Avail_qty`,
 1 AS `total_borrows`,
 1 AS `current_borrows`,
 1 AS `overdue_count`,
 1 AS `borrow_rate`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_book_borrow_status`
--

DROP TABLE IF EXISTS `v_book_borrow_status`;
/*!50001 DROP VIEW IF EXISTS `v_book_borrow_status`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_book_borrow_status` AS SELECT 
 1 AS `book_Id`,
 1 AS `isbn`,
 1 AS `Title`,
 1 AS `Author`,
 1 AS `Category`,
 1 AS `CategoryName`,
 1 AS `Location`,
 1 AS `Total_qty`,
 1 AS `Avail_qty`,
 1 AS `book_status`,
 1 AS `borrow_status`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_category_statistics`
--

DROP TABLE IF EXISTS `v_category_statistics`;
/*!50001 DROP VIEW IF EXISTS `v_category_statistics`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_category_statistics` AS SELECT 
 1 AS `category_code`,
 1 AS `category_name`,
 1 AS `total_books`,
 1 AS `total_copies`,
 1 AS `available_copies`,
 1 AS `total_borrows`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_overdue_details`
--

DROP TABLE IF EXISTS `v_overdue_details`;
/*!50001 DROP VIEW IF EXISTS `v_overdue_details`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_overdue_details` AS SELECT 
 1 AS `trans_Id`,
 1 AS `cardNum`,
 1 AS `user_name`,
 1 AS `userType`,
 1 AS `book_title`,
 1 AS `book_author`,
 1 AS `Borrow_time`,
 1 AS `Due_time`,
 1 AS `overdue_days`,
 1 AS `Renew_count`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_overdue_statistics`
--

DROP TABLE IF EXISTS `v_overdue_statistics`;
/*!50001 DROP VIEW IF EXISTS `v_overdue_statistics`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_overdue_statistics` AS SELECT 
 1 AS `userType`,
 1 AS `overdue_count`,
 1 AS `avg_overdue_days`,
 1 AS `max_overdue_days`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_popular_books`
--

DROP TABLE IF EXISTS `v_popular_books`;
/*!50001 DROP VIEW IF EXISTS `v_popular_books`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_popular_books` AS SELECT 
 1 AS `book_Id`,
 1 AS `Title`,
 1 AS `Author`,
 1 AS `Category`,
 1 AS `category_name`,
 1 AS `Total_qty`,
 1 AS `Avail_qty`,
 1 AS `borrow_count`,
 1 AS `popularity_rank`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_popular_recommendations`
--

DROP TABLE IF EXISTS `v_popular_recommendations`;
/*!50001 DROP VIEW IF EXISTS `v_popular_recommendations`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_popular_recommendations` AS SELECT 
 1 AS `book_title`,
 1 AS `book_author`,
 1 AS `book_publisher`,
 1 AS `book_isbn`,
 1 AS `book_category`,
 1 AS `book_category_name`,
 1 AS `recommend_count`,
 1 AS `user_count`,
 1 AS `total_qty`,
 1 AS `first_recommend_time`,
 1 AS `last_recommend_time`,
 1 AS `popularity_rank`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_recent_borrows`
--

DROP TABLE IF EXISTS `v_recent_borrows`;
/*!50001 DROP VIEW IF EXISTS `v_recent_borrows`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_recent_borrows` AS SELECT 
 1 AS `trans_Id`,
 1 AS `cardNum`,
 1 AS `user_name`,
 1 AS `book_title`,
 1 AS `book_author`,
 1 AS `Borrow_time`,
 1 AS `Due_time`,
 1 AS `Status`,
 1 AS `days_since_borrow`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_recommendation_history`
--

DROP TABLE IF EXISTS `v_recommendation_history`;
/*!50001 DROP VIEW IF EXISTS `v_recommendation_history`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_recommendation_history` AS SELECT 
 1 AS `rec_id`,
 1 AS `cardNum`,
 1 AS `user_name`,
 1 AS `userType`,
 1 AS `book_title`,
 1 AS `book_author`,
 1 AS `book_publisher`,
 1 AS `book_isbn`,
 1 AS `book_category`,
 1 AS `book_category_name`,
 1 AS `recommend_qty`,
 1 AS `recommend_reason`,
 1 AS `recommend_time`,
 1 AS `status`,
 1 AS `status_description`,
 1 AS `admin_feedback`,
 1 AS `admin_cardNum`,
 1 AS `admin_name`,
 1 AS `process_time`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_recommendation_statistics`
--

DROP TABLE IF EXISTS `v_recommendation_statistics`;
/*!50001 DROP VIEW IF EXISTS `v_recommendation_statistics`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_recommendation_statistics` AS SELECT 
 1 AS `status`,
 1 AS `status_description`,
 1 AS `count`,
 1 AS `user_count`,
 1 AS `avg_qty`,
 1 AS `earliest_time`,
 1 AS `latest_time`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_user_borrow_history`
--

DROP TABLE IF EXISTS `v_user_borrow_history`;
/*!50001 DROP VIEW IF EXISTS `v_user_borrow_history`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_user_borrow_history` AS SELECT 
 1 AS `trans_Id`,
 1 AS `cardNum`,
 1 AS `user_name`,
 1 AS `userType`,
 1 AS `book_title`,
 1 AS `book_author`,
 1 AS `book_category`,
 1 AS `book_category_name`,
 1 AS `Borrow_time`,
 1 AS `Due_time`,
 1 AS `Return_time`,
 1 AS `borrow_status`,
 1 AS `Renew_count`,
 1 AS `display_status`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_user_borrow_statistics`
--

DROP TABLE IF EXISTS `v_user_borrow_statistics`;
/*!50001 DROP VIEW IF EXISTS `v_user_borrow_statistics`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_user_borrow_statistics` AS SELECT 
 1 AS `cardNum`,
 1 AS `Name`,
 1 AS `userType`,
 1 AS `total_borrows`,
 1 AS `current_borrows`,
 1 AS `overdue_count`,
 1 AS `returned_count`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_user_recommendation_statistics`
--

DROP TABLE IF EXISTS `v_user_recommendation_statistics`;
/*!50001 DROP VIEW IF EXISTS `v_user_recommendation_statistics`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_user_recommendation_statistics` AS SELECT 
 1 AS `cardNum`,
 1 AS `user_name`,
 1 AS `userType`,
 1 AS `total_recommendations`,
 1 AS `pending_count`,
 1 AS `approved_count`,
 1 AS `rejected_count`,
 1 AS `purchased_count`,
 1 AS `total_recommend_qty`,
 1 AS `first_recommend_time`,
 1 AS `last_recommend_time`*/;
SET character_set_client = @saved_cs_client;

--
-- Dumping events for database 'virtual_campus'
--

--
-- Dumping routines for database 'virtual_campus'
--
/*!50003 DROP PROCEDURE IF EXISTS `sp_add_book` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_add_book`(
  IN p_isbn VARCHAR(20),
  IN p_title VARCHAR(100),
  IN p_author VARCHAR(50),
  IN p_publisher VARCHAR(100),
  IN p_publish_date DATE,
  IN p_category VARCHAR(2),
  IN p_location VARCHAR(50),
  IN p_total_qty INT
)
BEGIN
  IF EXISTS(SELECT 1 FROM tblBook WHERE isbn=p_isbn) THEN
     SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='ISBN已存在';
  END IF;

  IF NOT EXISTS(SELECT 1 FROM tblBookCategory WHERE category_code=p_category) THEN
     SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='图书分类不存在';
  END IF;

  INSERT INTO tblBook(isbn,Title,Author,Publisher,Publish_date,Category,Location,Total_qty,Avail_qty,Status)
  VALUES(p_isbn,p_title,p_author,p_publisher,p_publish_date,p_category,p_location,p_total_qty,p_total_qty,'IN_LIBRARY');

  SELECT '图书添加成功' AS result, LAST_INSERT_ID() AS book_id;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_borrow_book` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_borrow_book`(
  IN p_book_id INT,
  IN p_card_num VARCHAR(10),
  IN p_borrow_days INT
)
BEGIN
  DECLARE v_avail_qty INT DEFAULT 0;
  DECLARE v_max_borrow_count INT DEFAULT 5;
  DECLARE v_curr INT DEFAULT 0;
  DECLARE v_user_type VARCHAR(20);
  DECLARE v_max_days INT DEFAULT 30;
  DECLARE v_msg VARCHAR(100);  -- 新增变量用于存储动态消息

  SELECT Avail_qty INTO v_avail_qty FROM tblBook WHERE book_Id=p_book_id;
  IF v_avail_qty<=0 THEN 
     SELECT 0 AS success, '图书库存不足' AS result, NULL AS trans_id;
  ELSE
    SELECT userType INTO v_user_type FROM tblUser WHERE cardNum=p_card_num;
    IF v_user_type IS NULL THEN 
       SELECT 0 AS success, '用户不存在' AS result, NULL AS trans_id;
    ELSE
      SELECT max_borrow_count,max_borrow_days INTO v_max_borrow_count,v_max_days
      FROM tblBorrowRule WHERE user_type=v_user_type;

      IF p_borrow_days>v_max_days THEN
         SET v_msg = CONCAT('最多借', v_max_days, '天');
         SELECT 0 AS success, v_msg AS result, NULL AS trans_id;
      ELSE
        SELECT COUNT(*) INTO v_curr
        FROM tblBook_trans
        WHERE cardNum=p_card_num AND Status='BORROWED';

        IF v_curr>=v_max_borrow_count THEN
           SELECT 0 AS success, '已达个人最大借书量' AS result, NULL AS trans_id;
        ELSE
          START TRANSACTION;
            INSERT INTO tblBook_trans(book_Id,cardNum,Due_time,Status)
            VALUES(p_book_id,p_card_num,DATE_ADD(NOW(),INTERVAL p_borrow_days DAY),'BORROWED');
            UPDATE tblBook SET Avail_qty=Avail_qty-1 WHERE book_Id=p_book_id;
          COMMIT;
          SELECT 1 AS success, '借阅成功' AS result, LAST_INSERT_ID() AS trans_id;
        END IF;
      END IF;
    END IF;
  END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_get_book_statistics` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_get_book_statistics`(IN p_book_id INT)
BEGIN
  SELECT b.book_Id,b.Title,b.Author,b.Category,bc.category_name,
         b.Total_qty,b.Avail_qty,
         COUNT(bt.trans_Id) AS total_borrows,
         COUNT(CASE WHEN bt.Status='BORROWED' THEN 1 END) AS current_borrows,
         COUNT(CASE WHEN bt.Status='OVERDUE' THEN 1 END) AS overdue_count,
         COUNT(CASE WHEN bt.Status='RETURNED' THEN 1 END) AS returned_count
  FROM tblBook b
  LEFT JOIN tblBook_trans bt ON b.book_Id=bt.book_Id
  LEFT JOIN tblBookCategory bc ON b.Category=bc.category_code
  WHERE b.book_Id=p_book_id
  GROUP BY b.book_Id,b.Title,b.Author,b.Category,bc.category_name,b.Total_qty,b.Avail_qty;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_get_user_statistics` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_get_user_statistics`(IN p_card_num VARCHAR(10))
BEGIN
  SELECT u.cardNum,u.Name,u.userType,
         COUNT(bt.trans_Id) AS total_borrows,
         COUNT(CASE WHEN bt.Status='BORROWED' THEN 1 END) AS current_borrows,
         COUNT(CASE WHEN bt.Status='OVERDUE' THEN 1 END) AS overdue_count,
         COUNT(CASE WHEN bt.Status='RETURNED' THEN 1 END) AS returned_count,
         AVG(DATEDIFF(bt.Return_time,bt.Borrow_time)) AS avg_borrow_days
  FROM tblUser u
  LEFT JOIN tblBook_trans bt ON u.cardNum=bt.cardNum
  WHERE u.cardNum=p_card_num
  GROUP BY u.cardNum,u.Name,u.userType;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_renew_book` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_renew_book`(
  IN p_trans_id INT,
  IN p_extend_days INT
)
BEGIN
  DECLARE v_status VARCHAR(20);
  DECLARE v_renew_count INT;
  DECLARE v_max_renew INT;
  DECLARE v_due_time DATETIME;
  DECLARE v_card VARCHAR(10);
  DECLARE v_user_type VARCHAR(20);

  SELECT Status,Renew_count,Due_time,cardNum INTO v_status,v_renew_count,v_due_time,v_card
  FROM tblBook_trans WHERE trans_Id=p_trans_id;

  IF v_status IS NULL THEN 
     SELECT 0 AS success, '记录不存在' AS result, NULL AS trans_id;
  ELSE
    IF v_status!='BORROWED' THEN 
       SELECT 0 AS success, '只能续借已借出图书' AS result, NULL AS trans_id;
    ELSE
      IF v_due_time<NOW() THEN 
         SELECT 0 AS success, '逾期不能续借' AS result, NULL AS trans_id;
      ELSE
        SELECT userType INTO v_user_type FROM tblUser WHERE cardNum=v_card;
        SELECT max_renew_count INTO v_max_renew FROM tblBorrowRule WHERE user_type=v_user_type;

        IF v_renew_count>=v_max_renew THEN
           SELECT 0 AS success, '续借次数已达上限' AS result, NULL AS trans_id;
        ELSE
          UPDATE tblBook_trans
          SET Due_time=DATE_ADD(Due_time,INTERVAL p_extend_days DAY),
              Renew_count=Renew_count+1
          WHERE trans_Id=p_trans_id;

          SELECT 1 AS success, '续借成功' AS result, p_trans_id AS trans_id;
        END IF;
      END IF;
    END IF;
  END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_return_book` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_return_book`(IN p_trans_id INT)
BEGIN
  DECLARE v_book_id INT;
  DECLARE v_status VARCHAR(20);
  DECLARE v_due_time DATETIME;
  DECLARE v_days INT DEFAULT 0;

  SELECT book_Id,Status,Due_time INTO v_book_id,v_status,v_due_time
  FROM tblBook_trans WHERE trans_Id=p_trans_id;

  IF v_book_id IS NULL THEN 
     SELECT 0 AS success, '记录不存在' AS result, NULL AS trans_id;
  ELSE
    IF v_status='RETURNED' THEN 
       SELECT 0 AS success, '已归还' AS result, NULL AS trans_id;
    ELSE
      IF v_due_time<NOW() THEN SET v_days=DATEDIFF(NOW(),v_due_time); END IF;

      START TRANSACTION;
        UPDATE tblBook_trans
        SET Return_time=NOW(),
            Status=CASE WHEN v_days>0 THEN 'OVERDUE' ELSE 'RETURNED' END
        WHERE trans_Id=p_trans_id;
        UPDATE tblBook SET Avail_qty=Avail_qty+1 WHERE book_Id=v_book_id;
      COMMIT;
      SELECT 1 AS success, '归还成功' AS result, p_trans_id AS trans_id;
    END IF;
  END IF;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_search_books` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_search_books`(
  IN p_keyword VARCHAR(100),
  IN p_category VARCHAR(2),
  IN p_author VARCHAR(50),
  IN p_publisher VARCHAR(100),
  IN p_location VARCHAR(50),
  IN p_available_only TINYINT(1),
  IN p_limit INT
)
BEGIN
  SET @sql = '
    SELECT b.book_Id,b.isbn,b.Title,b.Author,b.Publisher,b.Publish_date,
           b.Category,bc.category_name,b.Location,b.Total_qty,b.Avail_qty,
           CASE WHEN b.Avail_qty>0 THEN ''可借阅'' ELSE ''已借完'' END AS borrow_status,
           COALESCE(br.borrow_count,0) AS borrow_count
    FROM tblBook b
    LEFT JOIN tblBookCategory bc ON b.Category=bc.category_code
    LEFT JOIN (
      SELECT book_Id,COUNT(*) AS borrow_count
      FROM tblBook_trans
      GROUP BY book_Id
    ) br ON b.book_Id=br.book_Id
    WHERE 1=1';

  IF p_keyword IS NOT NULL AND p_keyword<>'' THEN
     SET @sql = CONCAT(@sql,' AND (b.Title LIKE ''%',p_keyword,'%'' OR b.Author LIKE ''%',p_keyword,'%'' OR b.Publisher LIKE ''%',p_keyword,'%'')');
  END IF;
  IF p_category IS NOT NULL AND p_category<>'' THEN
     SET @sql = CONCAT(@sql,' AND b.Category=''',p_category,'''');
  END IF;
  IF p_author IS NOT NULL AND p_author<>'' THEN
     SET @sql = CONCAT(@sql,' AND b.Author LIKE ''%',p_author,'%''');
  END IF;
  IF p_publisher IS NOT NULL AND p_publisher<>'' THEN
     SET @sql = CONCAT(@sql,' AND b.Publisher LIKE ''%',p_publisher,'%''');
  END IF;
  IF p_location IS NOT NULL AND p_location<>'' THEN
     SET @sql = CONCAT(@sql,' AND b.Location LIKE ''%',p_location,'%''');
  END IF;
  IF p_available_only=1 THEN
     SET @sql = CONCAT(@sql,' AND b.Avail_qty>0');
  END IF;
  SET @sql = CONCAT(@sql,' ORDER BY b.Title LIMIT ',p_limit);

  PREPARE stmt FROM @sql;
  EXECUTE stmt;
  DEALLOCATE PREPARE stmt;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_search_books_by_category` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_search_books_by_category`(
  IN p_category VARCHAR(2),
  IN p_available_only TINYINT(1),
  IN p_limit INT
)
BEGIN
  SELECT b.book_Id,b.Title,b.Author,b.Publisher,b.Category,bc.category_name,
         b.Location,b.Total_qty,b.Avail_qty,
         CASE WHEN b.Avail_qty>0 THEN '可借阅' ELSE '已借完' END AS borrow_status
  FROM tblBook b
  LEFT JOIN tblBookCategory bc ON b.Category=bc.category_code
  WHERE b.Category=p_category
    AND (p_available_only=0 OR b.Avail_qty>0)
  ORDER BY b.Title
  LIMIT p_limit;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_search_popular_books` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_search_popular_books`(
  IN p_category VARCHAR(2),
  IN p_limit INT
)
BEGIN
  SELECT b.book_Id,b.Title,b.Author,b.Category,bc.category_name,
         b.Total_qty,b.Avail_qty,
         COALESCE(br.borrow_count,0) AS borrow_count,
         RANK() OVER (ORDER BY COALESCE(br.borrow_count,0) DESC) AS popularity_rank
  FROM tblBook b
  LEFT JOIN tblBookCategory bc ON b.Category=bc.category_code
  LEFT JOIN (
    SELECT book_Id,COUNT(*) AS borrow_count
    FROM tblBook_trans
    GROUP BY book_Id
  ) br ON b.book_Id=br.book_Id
  WHERE (p_category IS NULL OR b.Category=p_category)
  ORDER BY borrow_count DESC
  LIMIT p_limit;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_update_book` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_update_book`(
  IN p_book_id INT,
  IN p_title VARCHAR(100),
  IN p_author VARCHAR(50),
  IN p_publisher VARCHAR(100),
  IN p_publish_date DATE,
  IN p_category VARCHAR(2),
  IN p_location VARCHAR(50),
  IN p_total_qty INT
)
BEGIN
  DECLARE v_old_total INT;
  DECLARE v_old_avail INT;
  DECLARE v_new_avail INT;

  SELECT Total_qty,Avail_qty INTO v_old_total,v_old_avail
  FROM tblBook WHERE book_Id=p_book_id;

  IF v_old_total IS NULL THEN
     SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='图书不存在';
  END IF;

  SET v_new_avail = v_old_avail + (p_total_qty - v_old_total);

  UPDATE tblBook
  SET Title=p_title,Author=p_author,Publisher=p_publisher,
      Publish_date=p_publish_date,Category=p_category,
      Location=p_location,Total_qty=p_total_qty,Avail_qty=v_new_avail
  WHERE book_Id=p_book_id;

  SELECT '图书信息更新成功' AS result;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Final view structure for view `v_book_borrow_statistics`
--

/*!50001 DROP VIEW IF EXISTS `v_book_borrow_statistics`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_book_borrow_statistics` AS select `b`.`book_Id` AS `book_Id`,`b`.`Title` AS `Title`,`b`.`Author` AS `Author`,`b`.`Category` AS `Category`,`bc`.`category_name` AS `category_name`,`b`.`Total_qty` AS `Total_qty`,`b`.`Avail_qty` AS `Avail_qty`,count(`bt`.`trans_Id`) AS `total_borrows`,count((case when (`bt`.`Status` = 'BORROWED') then 1 end)) AS `current_borrows`,count((case when (`bt`.`Status` = 'OVERDUE') then 1 end)) AS `overdue_count`,round((count(`bt`.`trans_Id`) / `b`.`Total_qty`),2) AS `borrow_rate` from ((`tblbook` `b` left join `tblbook_trans` `bt` on((`b`.`book_Id` = `bt`.`book_Id`))) left join `tblbookcategory` `bc` on((`b`.`Category` = `bc`.`category_code`))) group by `b`.`book_Id`,`b`.`Title`,`b`.`Author`,`b`.`Category`,`bc`.`category_name`,`b`.`Total_qty`,`b`.`Avail_qty` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_book_borrow_status`
--

/*!50001 DROP VIEW IF EXISTS `v_book_borrow_status`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_book_borrow_status` AS select `b`.`book_Id` AS `book_Id`,`b`.`isbn` AS `isbn`,`b`.`Title` AS `Title`,`b`.`Author` AS `Author`,`b`.`Category` AS `Category`,`bc`.`category_name` AS `CategoryName`,`b`.`Location` AS `Location`,`b`.`Total_qty` AS `Total_qty`,`b`.`Avail_qty` AS `Avail_qty`,`b`.`Status` AS `book_status`,(case when (`b`.`Avail_qty` > 0) then '可借阅' else '已借完' end) AS `borrow_status` from (`tblbook` `b` left join `tblbookcategory` `bc` on((`b`.`Category` = `bc`.`category_code`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_category_statistics`
--

/*!50001 DROP VIEW IF EXISTS `v_category_statistics`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_category_statistics` AS select `bc`.`category_code` AS `category_code`,`bc`.`category_name` AS `category_name`,count(`b`.`book_Id`) AS `total_books`,sum(`b`.`Total_qty`) AS `total_copies`,sum(`b`.`Avail_qty`) AS `available_copies`,count(`bt`.`trans_Id`) AS `total_borrows` from ((`tblbookcategory` `bc` left join `tblbook` `b` on((`bc`.`category_code` = `b`.`Category`))) left join `tblbook_trans` `bt` on((`b`.`book_Id` = `bt`.`book_Id`))) group by `bc`.`category_code`,`bc`.`category_name` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_overdue_details`
--

/*!50001 DROP VIEW IF EXISTS `v_overdue_details`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_overdue_details` AS select `bt`.`trans_Id` AS `trans_Id`,`bt`.`cardNum` AS `cardNum`,`u`.`Name` AS `user_name`,`u`.`userType` AS `userType`,`b`.`Title` AS `book_title`,`b`.`Author` AS `book_author`,`bt`.`Borrow_time` AS `Borrow_time`,`bt`.`Due_time` AS `Due_time`,(to_days(now()) - to_days(`bt`.`Due_time`)) AS `overdue_days`,`bt`.`Renew_count` AS `Renew_count` from ((`tblbook_trans` `bt` join `tblbook` `b` on((`bt`.`book_Id` = `b`.`book_Id`))) join `tbluser` `u` on((`bt`.`cardNum` = `u`.`cardNum`))) where (`bt`.`Status` = 'OVERDUE') order by (to_days(now()) - to_days(`bt`.`Due_time`)) desc */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_overdue_statistics`
--

/*!50001 DROP VIEW IF EXISTS `v_overdue_statistics`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_overdue_statistics` AS select `u`.`userType` AS `userType`,count(0) AS `overdue_count`,avg((to_days(now()) - to_days(`bt`.`Due_time`))) AS `avg_overdue_days`,max((to_days(now()) - to_days(`bt`.`Due_time`))) AS `max_overdue_days` from (`tblbook_trans` `bt` join `tbluser` `u` on((`bt`.`cardNum` = `u`.`cardNum`))) where (`bt`.`Status` = 'OVERDUE') group by `u`.`userType` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_popular_books`
--

/*!50001 DROP VIEW IF EXISTS `v_popular_books`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_popular_books` AS select `b`.`book_Id` AS `book_Id`,`b`.`Title` AS `Title`,`b`.`Author` AS `Author`,`b`.`Category` AS `Category`,`bc`.`category_name` AS `category_name`,`b`.`Total_qty` AS `Total_qty`,`b`.`Avail_qty` AS `Avail_qty`,coalesce(`br`.`borrow_count`,0) AS `borrow_count`,rank() OVER (ORDER BY coalesce(`br`.`borrow_count`,0) desc )  AS `popularity_rank` from ((`tblbook` `b` left join `tblbookcategory` `bc` on((`b`.`Category` = `bc`.`category_code`))) left join (select `tblbook_trans`.`book_Id` AS `book_Id`,count(0) AS `borrow_count` from `tblbook_trans` group by `tblbook_trans`.`book_Id`) `br` on((`b`.`book_Id` = `br`.`book_Id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_popular_recommendations`
--

/*!50001 DROP VIEW IF EXISTS `v_popular_recommendations`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_popular_recommendations` AS select `br`.`book_title` AS `book_title`,`br`.`book_author` AS `book_author`,`br`.`book_publisher` AS `book_publisher`,`br`.`book_isbn` AS `book_isbn`,`br`.`book_category` AS `book_category`,`bc`.`category_name` AS `book_category_name`,count(0) AS `recommend_count`,count(distinct `br`.`cardNum`) AS `user_count`,sum(`br`.`recommend_qty`) AS `total_qty`,min(`br`.`recommend_time`) AS `first_recommend_time`,max(`br`.`recommend_time`) AS `last_recommend_time`,rank() OVER (ORDER BY count(0) desc )  AS `popularity_rank` from (`tblbookrecommendation` `br` left join `tblbookcategory` `bc` on((`br`.`book_category` = `bc`.`category_code`))) where (`br`.`status` in ('PENDING','APPROVED','PURCHASED')) group by `br`.`book_title`,`br`.`book_author`,`br`.`book_publisher`,`br`.`book_isbn`,`br`.`book_category`,`bc`.`category_name` order by `recommend_count` desc,`user_count` desc */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_recent_borrows`
--

/*!50001 DROP VIEW IF EXISTS `v_recent_borrows`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_recent_borrows` AS select `bt`.`trans_Id` AS `trans_Id`,`bt`.`cardNum` AS `cardNum`,`u`.`Name` AS `user_name`,`b`.`Title` AS `book_title`,`b`.`Author` AS `book_author`,`bt`.`Borrow_time` AS `Borrow_time`,`bt`.`Due_time` AS `Due_time`,`bt`.`Status` AS `Status`,(to_days(now()) - to_days(`bt`.`Borrow_time`)) AS `days_since_borrow` from ((`tblbook_trans` `bt` join `tblbook` `b` on((`bt`.`book_Id` = `b`.`book_Id`))) join `tbluser` `u` on((`bt`.`cardNum` = `u`.`cardNum`))) where (`bt`.`Borrow_time` >= (now() - interval 30 day)) order by `bt`.`Borrow_time` desc */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_recommendation_history`
--

/*!50001 DROP VIEW IF EXISTS `v_recommendation_history`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_recommendation_history` AS select `br`.`rec_id` AS `rec_id`,`br`.`cardNum` AS `cardNum`,`u`.`Name` AS `user_name`,`u`.`userType` AS `userType`,`br`.`book_title` AS `book_title`,`br`.`book_author` AS `book_author`,`br`.`book_publisher` AS `book_publisher`,`br`.`book_isbn` AS `book_isbn`,`br`.`book_category` AS `book_category`,`bc`.`category_name` AS `book_category_name`,`br`.`recommend_qty` AS `recommend_qty`,`br`.`recommend_reason` AS `recommend_reason`,`br`.`recommend_time` AS `recommend_time`,`br`.`status` AS `status`,(case when (`br`.`status` = 'PENDING') then '待审核' when (`br`.`status` = 'APPROVED') then '已通过' when (`br`.`status` = 'REJECTED') then '已拒绝' when (`br`.`status` = 'PURCHASED') then '已采购' else '未知状态' end) AS `status_description`,`br`.`admin_feedback` AS `admin_feedback`,`br`.`admin_cardNum` AS `admin_cardNum`,`admin_u`.`Name` AS `admin_name`,`br`.`process_time` AS `process_time` from (((`tblbookrecommendation` `br` left join `tbluser` `u` on((`br`.`cardNum` = `u`.`cardNum`))) left join `tblbookcategory` `bc` on((`br`.`book_category` = `bc`.`category_code`))) left join `tbluser` `admin_u` on((`br`.`admin_cardNum` = `admin_u`.`cardNum`))) order by `br`.`rec_id` desc */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_recommendation_statistics`
--

/*!50001 DROP VIEW IF EXISTS `v_recommendation_statistics`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_recommendation_statistics` AS select `br`.`status` AS `status`,(case when (`br`.`status` = 'PENDING') then '待审核' when (`br`.`status` = 'APPROVED') then '已通过' when (`br`.`status` = 'REJECTED') then '已拒绝' when (`br`.`status` = 'PURCHASED') then '已采购' else '未知状态' end) AS `status_description`,count(0) AS `count`,count(distinct `br`.`cardNum`) AS `user_count`,avg(`br`.`recommend_qty`) AS `avg_qty`,min(`br`.`recommend_time`) AS `earliest_time`,max(`br`.`recommend_time`) AS `latest_time` from `tblbookrecommendation` `br` group by `br`.`status` order by (case `br`.`status` when 'PENDING' then 1 when 'APPROVED' then 2 when 'REJECTED' then 3 when 'PURCHASED' then 4 else 5 end) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_user_borrow_history`
--

/*!50001 DROP VIEW IF EXISTS `v_user_borrow_history`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_user_borrow_history` AS select `bt`.`trans_Id` AS `trans_Id`,`bt`.`cardNum` AS `cardNum`,`u`.`Name` AS `user_name`,`u`.`userType` AS `userType`,`b`.`Title` AS `book_title`,`b`.`Author` AS `book_author`,`b`.`Category` AS `book_category`,`bc`.`category_name` AS `book_category_name`,`bt`.`Borrow_time` AS `Borrow_time`,`bt`.`Due_time` AS `Due_time`,`bt`.`Return_time` AS `Return_time`,`bt`.`Status` AS `borrow_status`,`bt`.`Renew_count` AS `Renew_count`,(case when ((`bt`.`Status` = 'BORROWED') and (`bt`.`Due_time` < now())) then '已逾期' when (`bt`.`Status` = 'BORROWED') then '借阅中' else `bt`.`Status` end) AS `display_status` from (((`tblbook_trans` `bt` join `tblbook` `b` on((`bt`.`book_Id` = `b`.`book_Id`))) left join `tblbookcategory` `bc` on((`b`.`Category` = `bc`.`category_code`))) join `tbluser` `u` on((`bt`.`cardNum` = `u`.`cardNum`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_user_borrow_statistics`
--

/*!50001 DROP VIEW IF EXISTS `v_user_borrow_statistics`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_user_borrow_statistics` AS select `u`.`cardNum` AS `cardNum`,`u`.`Name` AS `Name`,`u`.`userType` AS `userType`,count(`bt`.`trans_Id`) AS `total_borrows`,count((case when (`bt`.`Status` = 'BORROWED') then 1 end)) AS `current_borrows`,count((case when (`bt`.`Status` = 'OVERDUE') then 1 end)) AS `overdue_count`,count((case when (`bt`.`Status` = 'RETURNED') then 1 end)) AS `returned_count` from (`tbluser` `u` left join `tblbook_trans` `bt` on((`u`.`cardNum` = `bt`.`cardNum`))) group by `u`.`cardNum`,`u`.`Name`,`u`.`userType` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_user_recommendation_statistics`
--

/*!50001 DROP VIEW IF EXISTS `v_user_recommendation_statistics`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_user_recommendation_statistics` AS select `u`.`cardNum` AS `cardNum`,`u`.`Name` AS `user_name`,`u`.`userType` AS `userType`,count(`br`.`rec_id`) AS `total_recommendations`,count((case when (`br`.`status` = 'PENDING') then 1 end)) AS `pending_count`,count((case when (`br`.`status` = 'APPROVED') then 1 end)) AS `approved_count`,count((case when (`br`.`status` = 'REJECTED') then 1 end)) AS `rejected_count`,count((case when (`br`.`status` = 'PURCHASED') then 1 end)) AS `purchased_count`,sum(`br`.`recommend_qty`) AS `total_recommend_qty`,min(`br`.`recommend_time`) AS `first_recommend_time`,max(`br`.`recommend_time`) AS `last_recommend_time` from (`tbluser` `u` left join `tblbookrecommendation` `br` on((`u`.`cardNum` = `br`.`cardNum`))) group by `u`.`cardNum`,`u`.`Name`,`u`.`userType` order by `total_recommendations` desc */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-09-19 18:23:40
