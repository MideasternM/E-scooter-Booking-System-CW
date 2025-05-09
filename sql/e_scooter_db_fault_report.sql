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
-- Table structure for table `fault_report`
--

DROP TABLE IF EXISTS `fault_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fault_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assigned_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) NOT NULL,
  `fault_type` varchar(255) NOT NULL,
  `location` varchar(255) NOT NULL,
  `replacement_parts` varchar(255) DEFAULT NULL,
  `reported_at` datetime(6) NOT NULL,
  `requires_part_replacement` bit(1) DEFAULT NULL,
  `resolution` varchar(255) DEFAULT NULL,
  `resolved_at` datetime(6) DEFAULT NULL,
  `severity` varchar(255) NOT NULL,
  `staff_notes` varchar(255) DEFAULT NULL,
  `started_at` datetime(6) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `assigned_staff_id` bigint DEFAULT NULL,
  `booking_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `scooter_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK64i213i0wqkks93yygr5voets` (`assigned_staff_id`),
  KEY `FK78qx5tqr4idnkluhkkreqveqx` (`booking_id`),
  KEY `FKci2bq9moio9gtjfyfrr4fvymf` (`user_id`),
  KEY `FK8omhedvrflyti6w41x3mn8w93` (`scooter_id`),
  CONSTRAINT `FK64i213i0wqkks93yygr5voets` FOREIGN KEY (`assigned_staff_id`) REFERENCES `staff` (`id`),
  CONSTRAINT `FK78qx5tqr4idnkluhkkreqveqx` FOREIGN KEY (`booking_id`) REFERENCES `booking` (`id`),
  CONSTRAINT `FK8omhedvrflyti6w41x3mn8w93` FOREIGN KEY (`scooter_id`) REFERENCES `scooter` (`id`),
  CONSTRAINT `FKci2bq9moio9gtjfyfrr4fvymf` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fault_report`
--

LOCK TABLES `fault_report` WRITE;
/*!40000 ALTER TABLE `fault_report` DISABLE KEYS */;
INSERT INTO `fault_report` VALUES (1,NULL,'123','mechanical','cd',NULL,'2025-04-18 19:20:28.291000',_binary '\0',NULL,NULL,'low',NULL,NULL,'NEW',NULL,10,1,3),(2,NULL,'123','mechanical','123',NULL,'2025-04-21 17:11:20.580000',_binary '\0',NULL,NULL,'medium',NULL,NULL,'NEW',NULL,27,1,6),(3,NULL,'123','electrical','123',NULL,'2025-04-21 17:13:19.153000',_binary '\0',NULL,NULL,'low',NULL,NULL,'NEW',NULL,28,1,4),(4,NULL,'123','cosmetic','123',NULL,'2025-05-05 20:58:51.523000',_binary '\0',NULL,NULL,'low',NULL,NULL,'NEW',NULL,29,1,1),(5,NULL,'123','mechanical','123',NULL,'2025-05-05 21:01:03.107000',_binary '\0',NULL,NULL,'low',NULL,NULL,'NEW',NULL,30,1,1),(6,NULL,'123','safety','123',NULL,'2025-05-05 23:09:41.458000',_binary '\0',NULL,NULL,'high',NULL,NULL,'NEW',NULL,39,1,1),(7,NULL,'123','mechanical','123',NULL,'2025-05-05 23:16:49.799000',_binary '\0',NULL,NULL,'high',NULL,NULL,'NEW',NULL,41,1,6);
/*!40000 ALTER TABLE `fault_report` ENABLE KEYS */;
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
