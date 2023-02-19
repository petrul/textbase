-- /usr/local/mysql/bin/mysqldump scriptorium_dev -h localhost -u scriptorium_dev -pstyaccutVocItit > tables.ddl

-- MySQL dump 10.13  Distrib 5.7.19, for macos10.12 (x86_64)
--
-- Host: localhost    Database: scriptorium_dev
-- ------------------------------------------------------
-- Server version	5.7.19

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `author`
--

DROP TABLE IF EXISTS `author`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `author` (
  `str_id` varchar(255) COLLATE utf8mb4_romanian_ci NOT NULL,
  `first_name` varchar(255) COLLATE utf8mb4_romanian_ci DEFAULT NULL,
  `last_name` varchar(255) COLLATE utf8mb4_romanian_ci DEFAULT NULL,
  PRIMARY KEY (`str_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_romanian_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `author`
--

LOCK TABLES `author` WRITE;
/*!40000 ALTER TABLE `author` DISABLE KEYS */;
/*!40000 ALTER TABLE `author` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tei_div`
--

DROP TABLE IF EXISTS `tei_div`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tei_div` (
  `id` bigint(20) NOT NULL,
  `xpath` varchar(255) COLLATE utf8mb4_romanian_ci DEFAULT NULL,
  `tei_file_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpvdaad87p0h88k3hfp5159sjl` (`tei_file_id`),
  CONSTRAINT `FKpvdaad87p0h88k3hfp5159sjl` FOREIGN KEY (`tei_file_id`) REFERENCES `tei_file` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_romanian_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tei_div`
--

LOCK TABLES `tei_div` WRITE;
/*!40000 ALTER TABLE `tei_div` DISABLE KEYS */;
/*!40000 ALTER TABLE `tei_div` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tei_file`
--

DROP TABLE IF EXISTS `tei_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tei_file` (
  `id` bigint(20) NOT NULL,
  `filename` varchar(255) COLLATE utf8mb4_romanian_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_romanian_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tei_file`
--

LOCK TABLES `tei_file` WRITE;
/*!40000 ALTER TABLE `tei_file` DISABLE KEYS */;
/*!40000 ALTER TABLE `tei_file` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `work`
--

DROP TABLE IF EXISTS `work`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `work` (
  `id` bigint(20) NOT NULL,
  `authors` tinyblob,
  `language` varchar(255) COLLATE utf8mb4_romanian_ci DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_romanian_ci DEFAULT NULL,
  `div_id` bigint(20) DEFAULT NULL,
  `tei_div_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKp6iggpnf4j5v93qs66a1lx4vv` (`tei_div_id`),
  CONSTRAINT `FKp6iggpnf4j5v93qs66a1lx4vv` FOREIGN KEY (`tei_div_id`) REFERENCES `tei_div` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_romanian_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `work`
--

LOCK TABLES `work` WRITE;
/*!40000 ALTER TABLE `work` DISABLE KEYS */;
/*!40000 ALTER TABLE `work` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-12-16 19:52:21
