-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: bd_tienda_alesstore
-- ------------------------------------------------------
-- Server version	8.0.43

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
-- Table structure for table `producto_categoria`
--

DROP TABLE IF EXISTS `producto_categoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producto_categoria` (
  `producto_id` bigint NOT NULL,
  `categoria_id` bigint NOT NULL,
  KEY `FKck76h1dqwbw3rp8gkxkxytqe6` (`categoria_id`),
  KEY `FKfahqc7k27mgnlrr5q6oylure7` (`producto_id`),
  CONSTRAINT `FKck76h1dqwbw3rp8gkxkxytqe6` FOREIGN KEY (`categoria_id`) REFERENCES `categorias` (`id`),
  CONSTRAINT `FKfahqc7k27mgnlrr5q6oylure7` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id_dpc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `producto_categoria`
--

LOCK TABLES `producto_categoria` WRITE;
/*!40000 ALTER TABLE `producto_categoria` DISABLE KEYS */;
INSERT INTO `producto_categoria` VALUES (254,3),(254,5),(255,11),(256,10),(253,10),(196,10),(195,10),(194,10),(193,10),(192,10),(191,10),(190,10),(189,4),(189,10),(188,10),(187,10),(186,10),(185,10),(184,8),(184,10),(165,1),(252,2),(252,8),(251,2),(251,6),(250,11),(249,11),(248,1),(247,1),(183,5),(182,1),(181,1),(181,5),(180,2),(180,3),(180,5),(179,1),(179,3),(179,5),(178,8),(177,8),(176,8),(175,1),(175,3),(175,5),(174,1),(174,3),(174,5),(173,8),(172,2),(172,8),(171,8),(170,1),(169,9),(238,6),(238,10),(246,1),(245,1),(244,11),(243,11),(242,11),(241,11),(240,11),(239,2),(239,8),(237,10),(236,10),(235,10),(234,10),(233,10),(232,10),(231,10),(230,10),(229,10),(228,10),(227,2),(227,10),(226,10),(225,10),(224,10),(223,10),(222,10),(221,10),(220,10),(219,10),(218,10),(217,10),(216,10),(215,10),(197,10),(214,10),(213,10),(212,10),(211,10),(210,10),(209,10),(208,10),(207,10),(206,10),(205,10),(204,10),(203,10),(202,10),(201,10),(201,1),(200,10),(199,10),(198,10);
/*!40000 ALTER TABLE `producto_categoria` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-25 13:17:40
