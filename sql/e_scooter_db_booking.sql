CREATE DATABASE  IF NOT EXISTS `e_scooter_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `e_scooter_db`;
-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: e_scooter_db
-- ------------------------------------------------------
-- Server version	8.0.41

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
-- Table structure for table `booking`
--

DROP TABLE IF EXISTS `booking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `booking` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `end_time` datetime(6) NOT NULL,
  `start_time` datetime(6) NOT NULL,
  `status` varchar(255) NOT NULL,
  `scooter_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `has_fault` bit(1) NOT NULL,
  `selected_duration_label` varchar(255) DEFAULT NULL,
  `has_discount` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKha8w2fbve00trrtu2mfddswdb` (`scooter_id`),
  KEY `FKkgseyy7t56x7lkjgu3wah5s3t` (`user_id`),
  CONSTRAINT `FKha8w2fbve00trrtu2mfddswdb` FOREIGN KEY (`scooter_id`) REFERENCES `scooter` (`id`),
  CONSTRAINT `FKkgseyy7t56x7lkjgu3wah5s3t` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `booking`
--

LOCK TABLES `booking` WRITE;
/*!40000 ALTER TABLE `booking` DISABLE KEYS */;
INSERT INTO `booking` VALUES (9,'2025-04-17 20:44:14.298000','2025-04-17 20:44:18.684017','2025-04-17 20:44:14.295000','Completed',3,1,_binary '\0',NULL,_binary '\0'),(10,'2025-04-18 19:13:03.861000','2025-04-18 20:33:35.545607','2025-04-18 19:13:03.850000','Completed',3,1,_binary '\0',NULL,_binary '\0'),(11,'2025-04-18 20:39:34.403000','2025-04-18 20:39:39.379447','2025-04-18 20:39:34.394000','Completed',4,1,_binary '\0',NULL,_binary '\0'),(12,'2025-04-19 01:27:31.648000','2025-04-19 01:27:36.826082','2025-04-19 01:27:31.629000','Completed',3,1,_binary '\0',NULL,_binary '\0'),(13,'2025-04-19 15:40:51.747000','2025-04-19 16:22:40.369179','2025-04-19 15:40:51.723000','Completed',4,1,_binary '\0',NULL,_binary '\0'),(14,'2025-04-19 17:36:10.846000','2025-04-19 17:36:35.375378','2025-04-19 09:36:10.846000','Completed',3,1,_binary '\0','1 Hour',_binary '\0'),(15,'2025-04-19 17:37:03.477000','2025-04-19 17:37:16.546117','2025-04-19 09:37:03.469000','Completed',1,1,_binary '\0','1 Hour',_binary '\0'),(16,'2025-04-19 17:38:05.730000','2025-04-19 17:38:12.218655','2025-04-19 09:38:05.720000','Completed',3,1,_binary '\0','1 Hour',_binary '\0'),(17,'2025-04-19 17:45:15.051000','2025-04-19 17:45:27.213374','2025-04-19 17:45:14.989000','Completed',3,1,_binary '\0','1 Hour',_binary '\0'),(18,'2025-04-19 17:56:39.305000','2025-04-19 18:13:15.788861','2025-04-19 17:56:39.303000','Completed',6,1,_binary '\0','4 Hours',_binary '\0'),(19,'2025-04-19 18:14:23.459000','2025-04-19 18:48:00.404096','2025-04-19 18:14:23.456000','Completed',4,1,_binary '\0','1 Week',_binary '\0'),(20,'2025-04-19 18:48:23.302000','2025-04-19 18:48:56.467847','2025-04-19 18:48:23.271000','Completed',3,1,_binary '\0','1 Hour',_binary '\0'),(21,'2025-04-19 22:30:11.171000','2025-04-19 23:30:45.893824','2025-04-19 22:30:11.155000','Completed',6,1,_binary '\0','1 Hour',_binary '\0'),(22,'2025-04-21 16:49:25.253000','2025-04-21 16:51:05.867817','2025-04-21 16:49:25.233000','Completed',1,1,_binary '\0','1 Hour',_binary '\0'),(24,'2025-04-21 16:51:15.001000','2025-04-21 16:51:56.684029','2025-04-21 16:51:14.937000','Completed',1,1,_binary '\0','1 Hour',_binary '\0'),(25,'2025-04-21 17:03:09.503000','2025-04-21 17:03:40.615226','2025-04-21 17:03:09.500000','Completed',3,1,_binary '\0','4 Hours',_binary '\0'),(26,'2025-04-21 17:06:58.140000','2025-04-21 17:07:45.234962','2025-04-21 17:06:58.130000','Completed',4,1,_binary '\0','1 Hour',_binary '\0'),(27,'2025-04-21 17:08:45.597000','2025-04-21 17:12:26.468743','2025-04-21 17:08:45.589000','Completed',6,1,_binary '\0','4 Hours',_binary '\0'),(28,'2025-04-21 17:12:34.831000','2025-04-21 17:13:25.384150','2025-04-21 17:12:34.823000','Completed',4,1,_binary '\0','4 Hours',_binary '\0'),(29,'2025-05-05 20:58:27.195000','2025-05-05 20:59:20.117370','2025-05-04 14:58:27.178000','Completed',1,1,_binary '\0','1 Hour',_binary '\0'),(30,'2025-05-05 21:00:36.694000','2025-05-05 21:01:22.537248','2025-05-05 21:00:36.686000','Completed',1,1,_binary '\0','4 Hours',_binary '\0'),(31,'2025-05-05 21:59:50.014000','2025-05-05 21:59:58.377129','2025-05-05 21:59:49.987000','Completed',4,1,_binary '\0','1 Hour',_binary ''),(32,'2025-05-05 22:05:06.309000','2025-05-05 22:16:30.170259','2025-05-05 22:05:06.296000','Completed',4,1,_binary '\0','1 Hour',_binary ''),(33,'2025-05-05 22:39:26.985000','2025-05-05 22:39:41.372961','2025-05-05 22:39:26.962000','Completed',6,1,_binary '\0','1 Hour',_binary ''),(34,'2025-05-05 22:48:26.333000','2025-05-05 22:48:35.126228','2025-05-05 22:48:26.308000','Completed',3,1,_binary '\0','1 Hour',_binary ''),(35,'2025-05-05 23:02:43.067000','2025-05-05 23:02:53.591389','2025-05-05 23:02:43.042000','Completed',3,1,_binary '\0','1 Hour',_binary ''),(36,'2025-05-05 23:03:22.713000','2025-05-05 23:03:30.706447','2025-05-05 23:03:22.696000','Completed',6,1,_binary '\0','1 Hour',_binary ''),(37,'2025-05-05 23:06:14.987000','2025-05-05 23:06:30.631429','2025-05-05 23:06:14.959000','Completed',6,1,_binary '\0','1 Hour',_binary ''),(38,'2025-05-05 23:07:58.325000','2025-05-06 00:07:58.305000','2025-05-05 23:07:58.305000','CANCELLED',7,1,_binary '\0','1 Hour',_binary ''),(39,'2025-05-05 23:09:18.884000','2025-05-05 23:09:58.028482','2025-05-05 23:09:18.862000','Completed',1,1,_binary '\0','4 Hours',_binary ''),(40,'2025-05-05 23:16:07.662000','2025-05-06 00:16:07.642000','2025-05-05 23:16:07.642000','CANCELLED',3,1,_binary '\0','1 Hour',_binary ''),(41,'2025-05-05 23:16:31.147000','2025-05-05 23:17:01.357723','2025-05-05 23:16:31.135000','Completed',6,1,_binary '\0','1 Day',_binary '');
/*!40000 ALTER TABLE `booking` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-08 11:13:58
