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
-- Table structure for table `rental_stores`
--

DROP TABLE IF EXISTS `rental_stores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rental_stores` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) NOT NULL,
  `code` varchar(255) DEFAULT NULL,
  `contact_phone` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` longtext,
  `image_url` varchar(255) DEFAULT NULL,
  `last_update_time` datetime(6) NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `name` varchar(255) NOT NULL,
  `opening_hours` varchar(255) NOT NULL,
  `status` enum('CLOSED_PERMANENT','CLOSED_TEMPORARY','OPERATIONAL') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKphodvp2vg64a20euc0ylccdau` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rental_stores`
--

LOCK TABLES `rental_stores` WRITE;
/*!40000 ALTER TABLE `rental_stores` DISABLE KEYS */;
INSERT INTO `rental_stores` VALUES (1,'Xipu Subdistrict, Pidu District, Chengdu, Sichuan, 610010, China','1','123456','2025-04-12 08:00:00.000000','1',NULL,'2025-05-05 23:19:47.931258',30.745,103.977,'a','09:00-18:00','OPERATIONAL'),(2,'校园路, Xipu Subdistrict, Pidu District, Chengdu, Sichuan, 610010, China','2','123456','2025-05-05 14:47:58.217453',NULL,NULL,'2025-05-05 20:54:10.474556',30.755,103.988,'b','09:00-18:00','OPERATIONAL'),(3,'Xiaoyuan Road, Xipu Subdistrict, Pidu District, Chengdu, Sichuan, 611731, China','3','123456','2025-05-05 17:37:49.086000',NULL,NULL,'2025-05-05 20:54:04.605359',30.768,103.976,'c','09:00-18:00','CLOSED_TEMPORARY');
/*!40000 ALTER TABLE `rental_stores` ENABLE KEYS */;
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
