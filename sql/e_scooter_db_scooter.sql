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
-- Table structure for table `scooter`
--

DROP TABLE IF EXISTS `scooter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scooter` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `battery_level` decimal(38,2) NOT NULL,
  `is_available` bit(1) NOT NULL,
  `location` varchar(255) NOT NULL,
  `model` varchar(255) NOT NULL,
  `scooter_code` varchar(255) NOT NULL,
  `last_maintenance_date` datetime(6) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKslivr0251fjqddkw2murjbumv` (`scooter_code`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scooter`
--

LOCK TABLES `scooter` WRITE;
/*!40000 ALTER TABLE `scooter` DISABLE KEYS */;
INSERT INTO `scooter` VALUES (1,52.00,_binary '','Xipu Subdistrict,  Pidu District,  Chengdu','Basic','cd','2025-04-12 08:00:00.000000',30.7485,103.977),(3,50.00,_binary '','蓝光.幸福满庭,  Xipu Subdistrict,  Pidu District','Basic','test_code_123','2025-04-06 08:00:00.000000',30.752,103.981),(4,100.00,_binary '','金牛坝路,  Xipu Subdistrict,  Pidu District','Basic','2','2025-04-05 08:00:00.000000',30.751,103.979),(6,100.00,_binary '','福梓西街,  Xipu Subdistrict,  Pidu District','Premium','6','2025-04-11 08:00:00.000000',30.7495,103.9825),(7,85.00,_binary '','龙吟路,  Xipu Subdistrict,  Pidu District','Standard','5','2025-04-26 08:00:00.000000',30.75,103.98);
/*!40000 ALTER TABLE `scooter` ENABLE KEYS */;
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
