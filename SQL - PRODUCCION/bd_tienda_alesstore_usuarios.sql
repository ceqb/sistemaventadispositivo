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
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `idusuario` bigint NOT NULL AUTO_INCREMENT,
  `bloqueado` bit(1) NOT NULL,
  `clave` varchar(255) DEFAULT NULL,
  `codigo_desbloqueo` varchar(255) DEFAULT NULL,
  `correo` varchar(255) DEFAULT NULL,
  `expiracion_codigo` datetime(6) DEFAULT NULL,
  `intentos_fallidos` int DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `usuario` varchar(255) DEFAULT NULL,
  `idcargo` int DEFAULT NULL,
  `token_verificacion` varchar(255) DEFAULT NULL,
  `verificado` bit(1) DEFAULT NULL,
  `expiracion_reset_pass` datetime(6) DEFAULT NULL,
  `token_reset_password` varchar(255) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`idusuario`),
  KEY `FKt1dqiai13qm1jx8r0pqows7q8` (`idcargo`),
  CONSTRAINT `FKt1dqiai13qm1jx8r0pqows7q8` FOREIGN KEY (`idcargo`) REFERENCES `cargos` (`idcargo`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,_binary '\0','$2a$10$PXIpWk.RI6P.uzNsla3yKeLNlk1o5sa/0Fy2ncqRxoysUsnJFXbxe',NULL,'crearveint@gmail.com',NULL,0,'Carlos','cquiroz',1,NULL,_binary '',NULL,NULL,NULL),(4,_binary '','$2a$10$dNVy7T8butg8Z/mD98UMheLk3RgMeMAUL4T07TbZEy7mPZoLNb0ey','291489','gchilon@imarpe.gob.pe','2025-12-15 19:28:04.696007',3,'Gabriela Chilon','gchilon',2,NULL,_binary '',NULL,NULL,NULL),(5,_binary '\0','$2a$10$2ekRV4NUxonhUtxjt/JqkOMZjKkO.krY3ZtDzWKFoZt3PG3gihGk.',NULL,'aquiroz@imarpe.gob.pe',NULL,0,'Alessia Quiroz','aquiroz',3,NULL,_binary '',NULL,NULL,NULL),(6,_binary '\0','$2a$10$zDDXN3jutvfohrA4Q1A1uuHR0v5KZR3JxxcvL9m8AaHwJd/sGT1XC',NULL,'crearveint@gmail.com',NULL,0,'prueba','prueba',2,NULL,_binary '',NULL,NULL,NULL),(23,_binary '\0','$2a$10$tUvA/ogd5mp4Tc53UCdQv.Rtyl4YN6NZ7d8i2q34sL2a9SbjBkVDK',NULL,'compani_007@hotmail.com',NULL,0,'Vilma Balarezo','vbalarezo',2,NULL,_binary '','2025-11-14 16:24:15.000305','26ee0f99-24e4-4610-b5fe-b22e6e82023b',NULL);
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-25 13:17:42
