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
-- Table structure for table `pedidos`
--

DROP TABLE IF EXISTS `pedidos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedidos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `direccionEntrega` varchar(255) NOT NULL,
  `estadoPago` varchar(255) DEFAULT NULL,
  `estadoPedido` enum('ABANDONADO','CANCELADO','CONFIRMADO','CREADO','ENTREGADO','ENTREGADO_REVISADO','EN_CAMINO','PROCESANDO') DEFAULT NULL,
  `fecha_asignacion` datetime(6) DEFAULT NULL,
  `fecha_confirmacion_reserva` datetime(6) DEFAULT NULL,
  `fecha_entrega` datetime(6) DEFAULT NULL,
  `fecha_pedido` datetime(6) NOT NULL,
  `fecha_procesando` datetime(6) DEFAULT NULL,
  `metodo_pago` enum('CONTRAENTREGA','MERCADOPAGO','YAPE') DEFAULT NULL,
  `numeroPedido` varchar(255) DEFAULT NULL,
  `paymentId` varchar(255) DEFAULT NULL,
  `preferenciaId` varchar(255) DEFAULT NULL,
  `qr_token` varchar(255) DEFAULT NULL,
  `qr_usado` bit(1) DEFAULT NULL,
  `qr_usado_en` datetime(6) DEFAULT NULL,
  `reservaConfirmada` bit(1) NOT NULL,
  `reservaLiberada` bit(1) NOT NULL,
  `total` decimal(10,2) NOT NULL,
  `id_repartidor` bigint DEFAULT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5kkpg21q25x9487oer73wxvq9` (`qr_token`),
  KEY `FK3uayvsdbsbt5114wqdw7j0427` (`id_repartidor`),
  KEY `FK5g0es69v35nmkmpi8uewbphs2` (`usuario_id`),
  CONSTRAINT `FK3uayvsdbsbt5114wqdw7j0427` FOREIGN KEY (`id_repartidor`) REFERENCES `usuarios` (`idusuario`),
  CONSTRAINT `FK5g0es69v35nmkmpi8uewbphs2` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`idusuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedidos`
--

LOCK TABLES `pedidos` WRITE;
/*!40000 ALTER TABLE `pedidos` DISABLE KEYS */;
/*!40000 ALTER TABLE `pedidos` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-25 13:17:41
