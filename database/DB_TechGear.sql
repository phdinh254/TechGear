-- =====================================================================================================
-- TechGear – CSDL MySQL 8.4: lược đồ + dữ liệu mẫu (xuất bằng mysqldump từ backend profile dev, Flyway V1–V7 + R__)
-- Nạp vào CSDL TRỐNG (utf8mb4): mysql -u <user> -p <csdl_trong> < database/DB_TechGear.sql  (hoặc MySQL Workbench)
--   Với Docker Compose của dự án (CSDL techgear phải trống, VD sau docker compose down -v && docker compose up -d):
--   docker exec -i techgear-mysql mysql -utechgear -ptechgear techgear < database/DB_TechGear.sql
-- Có bảng flyway_schema_history nên backend (mvn spring-boot:run, profile dev) khởi động thẳng trên CSDL này.
-- Thời gian (DATETIME) lưu theo giờ Việt Nam (UTC+7).
-- Ảnh sản phẩm/banner/logo là link Wikimedia Commons (cần internet); nguồn và giấy phép: docs/nguon-anh-mau.md.
-- Tài khoản: admin@techgear.vn / Admin@123; khach@techgear.vn và khách mẫu *@example.com / Khach@123.
-- Ngày giờ trong file cố định theo lúc xuất; chạy backend profile dev trên CSDL trống thì dữ liệu mẫu tính theo ngày hiện tại.
-- =====================================================================================================
-- MySQL dump 10.13  Distrib 8.4.11, for Linux (x86_64)
--
-- Host: localhost    Database: techgear
-- ------------------------------------------------------
-- Server version	8.4.11

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
-- Table structure for table `banner`
--

DROP TABLE IF EXISTS `banner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `banner` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tieu_de` varchar(255) DEFAULT NULL,
  `hinh_anh` varchar(255) NOT NULL,
  `lien_ket` varchar(255) DEFAULT NULL,
  `thu_tu` int NOT NULL DEFAULT '0',
  `hien_thi` tinyint(1) NOT NULL DEFAULT '1',
  `bat_dau_luc` datetime(6) DEFAULT NULL,
  `ket_thuc_luc` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  CONSTRAINT `banner_chk_1` CHECK (((`bat_dau_luc` is null) or (`ket_thuc_luc` is null) or (`ket_thuc_luc` > `bat_dau_luc`)))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `banner`
--

LOCK TABLES `banner` WRITE;
/*!40000 ALTER TABLE `banner` DISABLE KEYS */;
INSERT INTO `banner` (`id`, `tieu_de`, `hinh_anh`, `lien_ket`, `thu_tu`, `hien_thi`, `bat_dau_luc`, `ket_thuc_luc`, `created_at`, `updated_at`) VALUES (1,'Âm thanh đỉnh cao – giảm đến 20%','https://thumb.wikimedia.org/wikipedia/commons/thumb/5/55/Top_view_of_headphones_hanging_from_a_desk_%28Unsplash%29.jpg/1920px-Top_view_of_headphones_hanging_from_a_desk_%28Unsplash%29.jpg','/san-pham?danhMuc=am-thanh',1,1,'2026-11-12 19:37:56.000000','2027-01-21 19:37:56.000000','2026-11-22 19:37:56.646697','2026-11-22 19:37:56.646697');
INSERT INTO `banner` (`id`, `tieu_de`, `hinh_anh`, `lien_ket`, `thu_tu`, `hien_thi`, `bat_dau_luc`, `ket_thuc_luc`, `created_at`, `updated_at`) VALUES (2,'Phụ kiện chính hãng, bảo hành tận nơi','https://thumb.wikimedia.org/wikipedia/commons/thumb/1/11/Xiaomi_Store_Portugal.jpg/1920px-Xiaomi_Store_Portugal.jpg','/san-pham',2,1,'2026-10-23 19:37:56.000000',NULL,'2026-11-22 19:37:56.646697','2026-11-22 19:37:56.646697');
INSERT INTO `banner` (`id`, `tieu_de`, `hinh_anh`, `lien_ket`, `thu_tu`, `hien_thi`, `bat_dau_luc`, `ket_thuc_luc`, `created_at`, `updated_at`) VALUES (3,'Bàn phím cơ mới về – Keychron K8','https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7b/Mechanical_Keyboard_Exhibition.jpg/1920px-Mechanical_Keyboard_Exhibition.jpg','/san-pham/ban-phim-co-keychron-k8',3,1,'2026-11-21 19:37:56.000000','2026-12-22 19:37:56.000000','2026-11-22 19:37:56.646697','2026-11-22 19:37:56.646697');
INSERT INTO `banner` (`id`, `tieu_de`, `hinh_anh`, `lien_ket`, `thu_tu`, `hien_thi`, `bat_dau_luc`, `ket_thuc_luc`, `created_at`, `updated_at`) VALUES (4,'Sale tai nghe tháng trước (đã kết thúc)','https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6b/Desk-music-headphones-earphones_%2824243083451%29.jpg/1920px-Desk-music-headphones-earphones_%2824243083451%29.jpg','/san-pham?danhMuc=tai-nghe',4,1,'2026-09-23 19:37:56.000000','2026-10-23 19:37:56.000000','2026-11-22 19:37:56.646697','2026-11-22 19:37:56.646697');
/*!40000 ALTER TABLE `banner` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bien_the`
--

DROP TABLE IF EXISTS `bien_the`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bien_the` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `san_pham_id` bigint NOT NULL,
  `sku` varchar(100) NOT NULL,
  `gia_ban` bigint NOT NULL,
  `gia_goc` bigint DEFAULT NULL,
  `so_luong_ton` int NOT NULL DEFAULT '0',
  `nguong_canh_bao` int NOT NULL DEFAULT '5',
  `hien_thi` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `deleted_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_bien_the_sku` ((if((`deleted_at` is null),`sku`,NULL))),
  KEY `san_pham_id` (`san_pham_id`),
  CONSTRAINT `bien_the_ibfk_1` FOREIGN KEY (`san_pham_id`) REFERENCES `san_pham` (`id`),
  CONSTRAINT `bien_the_chk_1` CHECK ((`gia_ban` >= 0)),
  CONSTRAINT `bien_the_chk_2` CHECK ((`so_luong_ton` >= 0)),
  CONSTRAINT `bien_the_chk_3` CHECK ((`nguong_canh_bao` >= 0)),
  CONSTRAINT `bien_the_chk_4` CHECK (((`gia_goc` is null) or (`gia_goc` >= `gia_ban`)))
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bien_the`
--

LOCK TABLES `bien_the` WRITE;
/*!40000 ALTER TABLE `bien_the` DISABLE KEYS */;
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (1,1,'ANK-NANO65-DEN',790000,990000,24,5,1,'2026-10-23 19:37:56.000000','2026-10-24 13:47:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (2,1,'ANK-NANO65-TRANG',790000,990000,17,5,1,'2026-10-23 19:37:56.000000','2026-11-05 13:07:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (3,2,'APL-20W',490000,550000,39,5,1,'2026-10-25 19:37:56.000000','2026-10-26 15:57:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (4,3,'BAS-GAN5-100-DEN',1090000,NULL,11,5,1,'2026-11-02 19:37:56.000000','2026-11-03 15:07:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (5,3,'BAS-GAN5-100-TRANG',1090000,NULL,0,5,1,'2026-11-02 19:37:56.000000','2026-11-02 19:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (6,5,'UGR-CC100-1M',159000,NULL,58,5,1,'2026-10-28 19:37:56.000000','2026-10-29 14:27:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (7,5,'UGR-CC100-2M',199000,NULL,44,5,1,'2026-10-28 19:37:56.000000','2026-11-22 19:07:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (8,4,'ANK-PL3-1M',290000,350000,29,5,1,'2026-10-13 19:37:56.000000','2026-10-14 16:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (9,6,'ANK-PC10K-DEN',590000,690000,19,5,1,'2026-11-04 19:37:56.000000','2026-11-07 17:47:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (10,6,'ANK-PC10K-XANH',590000,690000,6,5,1,'2026-11-04 19:37:56.000000','2026-11-05 13:07:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (11,7,'XMI-PB20K',650000,NULL,0,5,1,'2026-10-18 19:37:56.000000','2026-10-18 19:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (12,10,'SNY-XM5-DEN',7990000,8990000,5,5,1,'2026-11-12 19:37:56.000000','2026-11-13 12:07:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (13,10,'SNY-XM5-BAC',7990000,8990000,3,5,1,'2026-11-12 19:37:56.000000','2026-11-21 12:57:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (14,8,'APL-APP2',5490000,6190000,14,5,1,'2026-11-10 19:37:56.000000','2026-11-11 16:17:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (15,9,'SSG-BUDS3-TRANG',3490000,NULL,9,5,1,'2026-11-14 19:37:56.000000','2026-11-15 16:47:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (16,9,'SSG-BUDS3-BAC',3490000,NULL,10,5,1,'2026-11-14 19:37:56.000000','2026-11-14 19:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (17,11,'JBL-FLIP6-DEN',2690000,2990000,8,5,1,'2026-11-07 19:37:56.000000','2026-11-07 19:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (18,11,'JBL-FLIP6-XANH',2690000,2990000,5,5,1,'2026-11-07 19:37:56.000000','2026-11-07 19:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (19,11,'JBL-FLIP6-DO',2690000,2990000,3,5,1,'2026-11-07 19:37:56.000000','2026-11-07 19:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (20,12,'SNY-XB13',990000,NULL,11,5,1,'2026-10-08 19:37:56.000000','2026-10-09 14:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (21,13,'LOG-MX3S-DEN',2290000,2490000,8,5,1,'2026-11-08 19:37:56.000000','2026-11-09 13:57:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (22,13,'LOG-MX3S-XAM',2290000,2490000,5,5,1,'2026-11-08 19:37:56.000000','2026-11-21 17:07:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (23,14,'LOG-G304',790000,NULL,28,5,1,'2026-10-03 19:37:56.000000','2026-10-17 12:17:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (24,15,'LOG-MXMINI-TAC',3290000,NULL,4,5,1,'2026-11-16 19:37:56.000000','2026-11-17 13:17:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (25,15,'LOG-MXMINI-LIN',3290000,NULL,5,5,1,'2026-11-16 19:37:56.000000','2026-11-16 19:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (26,16,'UGR-OP16P',190000,NULL,49,5,1,'2026-11-19 19:37:56.000000','2026-11-20 14:07:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (27,16,'UGR-OP16PM',210000,NULL,50,5,1,'2026-11-19 19:37:56.000000','2026-11-19 19:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (28,17,'BAS-KCL16',150000,NULL,80,5,1,'2026-11-20 19:37:56.000000','2026-11-20 19:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (32,32,'SSG-25W',390000,450000,36,5,1,'2026-08-29 19:37:56.000000','2026-10-14 16:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (33,33,'UGR-CAR30',290000,NULL,24,5,1,'2026-11-18 19:37:56.000000','2026-11-19 15:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (34,34,'BAS-CC60-1M',129000,NULL,59,5,1,'2026-09-13 19:37:56.000000','2026-09-20 14:57:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (35,34,'BAS-CC60-2M',159000,NULL,38,5,1,'2026-09-13 19:37:56.000000','2026-09-14 17:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (36,35,'UGR-AC3A',99000,129000,74,5,1,'2026-08-19 19:37:56.000000','2026-10-24 13:47:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (37,36,'SSG-PB10K',690000,790000,19,5,1,'2026-08-24 19:37:56.000000','2026-11-01 22:27:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (38,37,'XMI-PB10K-PRO',550000,NULL,24,5,1,'2026-09-18 19:37:56.000000','2026-09-20 14:57:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (39,39,'SNY-CH510',990000,1290000,9,5,1,'2026-08-14 19:37:56.000000','2026-08-28 11:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (40,38,'SSG-BUDS2P-TRANG',3990000,4490000,9,5,1,'2026-09-08 19:37:56.000000','2026-09-09 16:07:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (41,38,'SSG-BUDS2P-DEN',3990000,4490000,5,5,1,'2026-09-08 19:37:56.000000','2026-09-22 16:27:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (42,40,'JBL-CHG4-DEN',2490000,2990000,13,5,1,'2026-09-03 19:37:56.000000','2026-10-20 16:57:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (43,40,'JBL-CHG4-XANH',2490000,2990000,8,5,1,'2026-09-03 19:37:56.000000','2026-09-05 20:07:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (44,41,'JBL-GO2-XANH',690000,790000,5,5,1,'2026-09-23 19:37:56.000000','2026-09-24 13:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (45,41,'JBL-GO2-DEN',690000,790000,14,5,1,'2026-09-23 19:37:56.000000','2026-10-02 12:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (46,41,'JBL-GO2-DO',690000,790000,2,5,1,'2026-09-23 19:37:56.000000','2026-09-23 19:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (47,42,'LOG-M331-DEN',390000,450000,47,5,1,'2026-08-26 19:37:56.000000','2026-11-22 18:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (48,42,'LOG-M331-DO',390000,450000,18,5,1,'2026-08-26 19:37:56.000000','2026-10-17 12:17:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (49,43,'LOG-M705',990000,NULL,14,5,1,'2026-11-17 19:37:56.000000','2026-11-19 15:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (50,45,'LOG-K380',790000,890000,29,5,1,'2026-08-22 19:37:56.000000','2026-10-11 21:27:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (51,44,'KEY-K8-RED',2190000,NULL,11,5,1,'2026-11-21 19:37:56.000000','2026-11-22 16:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (52,44,'KEY-K8-BROWN',2190000,NULL,9,5,1,'2026-11-21 19:37:56.000000','2026-11-22 18:37:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (53,46,'APL-SC17',1190000,1290000,19,5,1,'2026-11-15 19:37:56.000000','2026-11-20 14:07:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (54,47,'SSG-S22U-TPU',250000,NULL,29,5,1,'2026-08-16 19:37:56.000000','2026-09-07 12:57:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (55,49,'UGR-PRIV16',290000,NULL,43,5,1,'2026-09-28 19:37:56.000000','2026-09-29 15:47:56.000000',NULL);
INSERT INTO `bien_the` (`id`, `san_pham_id`, `sku`, `gia_ban`, `gia_goc`, `so_luong_ton`, `nguong_canh_bao`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (56,48,'SSG-KCL-A04E',120000,NULL,59,5,1,'2026-09-21 19:37:56.000000','2026-09-22 16:27:56.000000',NULL);
/*!40000 ALTER TABLE `bien_the` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bien_the_gia_tri`
--

DROP TABLE IF EXISTS `bien_the_gia_tri`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bien_the_gia_tri` (
  `bien_the_id` bigint NOT NULL,
  `gia_tri_tuy_chon_id` bigint NOT NULL,
  PRIMARY KEY (`bien_the_id`,`gia_tri_tuy_chon_id`),
  KEY `gia_tri_tuy_chon_id` (`gia_tri_tuy_chon_id`),
  CONSTRAINT `bien_the_gia_tri_ibfk_1` FOREIGN KEY (`bien_the_id`) REFERENCES `bien_the` (`id`) ON DELETE CASCADE,
  CONSTRAINT `bien_the_gia_tri_ibfk_2` FOREIGN KEY (`gia_tri_tuy_chon_id`) REFERENCES `gia_tri_tuy_chon` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bien_the_gia_tri`
--

LOCK TABLES `bien_the_gia_tri` WRITE;
/*!40000 ALTER TABLE `bien_the_gia_tri` DISABLE KEYS */;
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (1,1);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (2,2);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (4,3);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (5,4);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (6,5);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (7,6);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (9,7);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (10,8);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (15,9);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (16,10);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (12,11);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (13,12);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (17,13);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (18,14);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (19,15);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (21,16);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (22,17);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (24,18);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (25,19);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (26,20);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (27,21);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (34,32);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (35,33);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (40,34);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (41,35);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (42,36);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (43,37);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (44,38);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (45,39);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (46,40);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (47,41);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (48,42);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (51,43);
INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES (52,44);
/*!40000 ALTER TABLE `bien_the_gia_tri` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cai_dat`
--

DROP TABLE IF EXISTS `cai_dat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cai_dat` (
  `khoa` varchar(100) NOT NULL,
  `gia_tri` text NOT NULL,
  `mo_ta` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`khoa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cai_dat`
--

LOCK TABLES `cai_dat` WRITE;
/*!40000 ALTER TABLE `cai_dat` DISABLE KEYS */;
INSERT INTO `cai_dat` (`khoa`, `gia_tri`, `mo_ta`, `updated_at`) VALUES ('email_lien_he','hotro@techgear.vn','Email liên hệ','2026-11-22 19:37:55.870800');
INSERT INTO `cai_dat` (`khoa`, `gia_tri`, `mo_ta`, `updated_at`) VALUES ('hotline','1900 1234','Số điện thoại hỗ trợ','2026-11-22 19:37:55.870800');
INSERT INTO `cai_dat` (`khoa`, `gia_tri`, `mo_ta`, `updated_at`) VALUES ('mien_phi_van_chuyen_tu','500000','Đơn từ mức này (đồng) được miễn phí vận chuyển','2026-11-22 19:37:55.870800');
INSERT INTO `cai_dat` (`khoa`, `gia_tri`, `mo_ta`, `updated_at`) VALUES ('phi_van_chuyen','30000','Phí vận chuyển mỗi đơn (đồng)','2026-11-22 19:37:55.870800');
INSERT INTO `cai_dat` (`khoa`, `gia_tri`, `mo_ta`, `updated_at`) VALUES ('ten_cua_hang','TechGear','Tên cửa hàng','2026-11-22 19:37:55.870800');
/*!40000 ALTER TABLE `cai_dat` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chi_tiet_don_hang`
--

DROP TABLE IF EXISTS `chi_tiet_don_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chi_tiet_don_hang` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `don_hang_id` bigint NOT NULL,
  `san_pham_id` bigint DEFAULT NULL,
  `bien_the_id` bigint DEFAULT NULL,
  `ten_san_pham` varchar(255) NOT NULL,
  `sku` varchar(100) NOT NULL,
  `phan_loai` varchar(150) DEFAULT NULL,
  `hinh_anh` varchar(255) DEFAULT NULL,
  `don_gia` bigint NOT NULL,
  `so_luong` int NOT NULL,
  `thanh_tien` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `don_hang_id` (`don_hang_id`),
  KEY `san_pham_id` (`san_pham_id`),
  KEY `bien_the_id` (`bien_the_id`),
  CONSTRAINT `chi_tiet_don_hang_ibfk_1` FOREIGN KEY (`don_hang_id`) REFERENCES `don_hang` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chi_tiet_don_hang_ibfk_2` FOREIGN KEY (`san_pham_id`) REFERENCES `san_pham` (`id`) ON DELETE SET NULL,
  CONSTRAINT `chi_tiet_don_hang_ibfk_3` FOREIGN KEY (`bien_the_id`) REFERENCES `bien_the` (`id`) ON DELETE SET NULL,
  CONSTRAINT `chi_tiet_don_hang_chk_1` CHECK ((`don_gia` >= 0)),
  CONSTRAINT `chi_tiet_don_hang_chk_2` CHECK ((`so_luong` > 0)),
  CONSTRAINT `chi_tiet_don_hang_chk_3` CHECK ((`thanh_tien` = (`don_gia` * `so_luong`)))
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chi_tiet_don_hang`
--

LOCK TABLES `chi_tiet_don_hang` WRITE;
/*!40000 ALTER TABLE `chi_tiet_don_hang` DISABLE KEYS */;
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (1,1,45,50,'Bàn phím Bluetooth Logitech K380','LOG-K380',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e6/Logitech_K380_CJK_Keyboard.jpg/960px-Logitech_K380_CJK_Keyboard.jpg',790000,1,790000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (2,1,35,36,'Cáp USB-A to USB-C Ugreen 3A','UGR-AC3A',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5f/2026-08-22_UGreen-USB-C-Kabel_HOF1918_RAW-Export.png/960px-2026-08-22_UGreen-USB-C-Kabel_HOF1918_RAW-Export.png',99000,2,198000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (4,2,36,37,'Sạc dự phòng Samsung EB-P1100C 10000mAh','SSG-PB10K',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e9/SAMSUNG_BATTERY_PACK_%28POWER_BANK%29_EB-P1100C.jpg/960px-SAMSUNG_BATTERY_PACK_%28POWER_BANK%29_EB-P1100C.jpg',690000,1,690000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (5,2,35,36,'Cáp USB-A to USB-C Ugreen 3A','UGR-AC3A',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5f/2026-08-22_UGreen-USB-C-Kabel_HOF1918_RAW-Export.png/960px-2026-08-22_UGreen-USB-C-Kabel_HOF1918_RAW-Export.png',99000,1,99000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (7,3,42,48,'Chuột không dây Logitech M331 Silent Plus','LOG-M331-DO','Đỏ','https://thumb.wikimedia.org/wikipedia/commons/thumb/3/39/Logitech_M331_Silent_Mouse.jpg/960px-Logitech_M331_Silent_Mouse.jpg',390000,1,390000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (8,4,39,39,'Tai nghe Sony WH-CH510','SNY-CH510',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/01/Sony_WH-CH510_Bluetooth_Over-Ear_Headphone.jpg/960px-Sony_WH-CH510_Bluetooth_Over-Ear_Headphone.jpg',990000,1,990000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (9,4,42,47,'Chuột không dây Logitech M331 Silent Plus','LOG-M331-DEN','Đen','https://thumb.wikimedia.org/wikipedia/commons/thumb/3/39/Logitech_M331_Silent_Mouse.jpg/960px-Logitech_M331_Silent_Mouse.jpg',390000,1,390000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (11,5,32,32,'Củ sạc nhanh Samsung 25W','SSG-25W',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f5/Samsung_Charger.jpg/960px-Samsung_Charger.jpg',390000,2,780000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (12,6,40,43,'Loa JBL Charge 4','JBL-CHG4-XANH','Xanh','https://thumb.wikimedia.org/wikipedia/commons/thumb/3/33/JBL_Charge_4_1v2.jpg/960px-JBL_Charge_4_1v2.jpg',2490000,1,2490000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (13,7,47,54,'Ốp lưng TPU trong suốt Samsung Galaxy S22 Ultra','SSG-S22U-TPU',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/78/SAMSUNG_Galaxy_S22_Ultra_TPU_CASE.jpg/960px-SAMSUNG_Galaxy_S22_Ultra_TPU_CASE.jpg',250000,1,250000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (14,7,35,36,'Cáp USB-A to USB-C Ugreen 3A','UGR-AC3A',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5f/2026-08-22_UGreen-USB-C-Kabel_HOF1918_RAW-Export.png/960px-2026-08-22_UGreen-USB-C-Kabel_HOF1918_RAW-Export.png',99000,1,99000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (16,8,38,40,'Tai nghe Samsung Galaxy Buds2 Pro','SSG-BUDS2P-TRANG','Trắng','https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9e/Samsung_Galaxy_Buds_FE_and_Samsung_Galaxy_Buds2_Pro.jpg/960px-Samsung_Galaxy_Buds_FE_and_Samsung_Galaxy_Buds2_Pro.jpg',3990000,1,3990000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (17,9,40,42,'Loa JBL Charge 4','JBL-CHG4-DEN','Đen','https://thumb.wikimedia.org/wikipedia/commons/thumb/3/33/JBL_Charge_4_1v2.jpg/960px-JBL_Charge_4_1v2.jpg',2490000,1,2490000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (18,10,34,35,'Cáp USB-C to USB-C Baseus 60W','BAS-CC60-2M','2m','https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5c/Two_USB-C_connectors_being_held.jpg/960px-Two_USB-C_connectors_being_held.jpg',159000,2,318000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (19,10,32,32,'Củ sạc nhanh Samsung 25W','SSG-25W',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f5/Samsung_Charger.jpg/960px-Samsung_Charger.jpg',390000,1,390000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (21,11,37,38,'Sạc dự phòng Xiaomi Mi Power Bank Pro 10000mAh','XMI-PB10K-PRO',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e8/MiPowerBankPro10000mAhQC30Xiaomi20240820000.jpg/960px-MiPowerBankPro10000mAhQC30Xiaomi20240820000.jpg',550000,1,550000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (22,12,37,38,'Sạc dự phòng Xiaomi Mi Power Bank Pro 10000mAh','XMI-PB10K-PRO',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e8/MiPowerBankPro10000mAhQC30Xiaomi20240820000.jpg/960px-MiPowerBankPro10000mAhQC30Xiaomi20240820000.jpg',550000,1,550000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (23,12,34,34,'Cáp USB-C to USB-C Baseus 60W','BAS-CC60-1M','1m','https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5c/Two_USB-C_connectors_being_held.jpg/960px-Two_USB-C_connectors_being_held.jpg',129000,1,129000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (25,13,48,56,'Kính cường lực Samsung Galaxy A04e','SSG-KCL-A04E',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6a/Samsung_Galaxy_A04e_front_with_screen_protector.jpg/960px-Samsung_Galaxy_A04e_front_with_screen_protector.jpg',120000,1,120000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (26,13,38,41,'Tai nghe Samsung Galaxy Buds2 Pro','SSG-BUDS2P-DEN','Đen','https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9e/Samsung_Galaxy_Buds_FE_and_Samsung_Galaxy_Buds2_Pro.jpg/960px-Samsung_Galaxy_Buds_FE_and_Samsung_Galaxy_Buds2_Pro.jpg',3990000,1,3990000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (28,14,41,44,'Loa JBL Go 2','JBL-GO2-XANH','Xanh','https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9c/JBL_GO2_Bluetooth_speaker.jpeg/960px-JBL_GO2_Bluetooth_speaker.jpeg',690000,1,690000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (29,15,41,45,'Loa JBL Go 2','JBL-GO2-DEN','Đen','https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9c/JBL_GO2_Bluetooth_speaker.jpeg/960px-JBL_GO2_Bluetooth_speaker.jpeg',690000,3,2070000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (30,16,49,55,'Kính cường lực chống nhìn trộm Ugreen iPhone 16','UGR-PRIV16',NULL,'https://upload.wikimedia.org/wikipedia/commons/f/f0/%D9%85%D8%AD%D8%A7%D9%81%D8%B8_%D8%B5%D9%81%D8%AD%D9%87_%D9%86%D9%85%D8%A7%DB%8C%D8%B4_%D8%AD%D8%B1%DB%8C%D9%85_%D8%B4%D8%AE%D8%B5%DB%8C.jpg',290000,2,580000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (31,16,42,47,'Chuột không dây Logitech M331 Silent Plus','LOG-M331-DEN','Đen','https://thumb.wikimedia.org/wikipedia/commons/thumb/3/39/Logitech_M331_Silent_Mouse.jpg/960px-Logitech_M331_Silent_Mouse.jpg',390000,1,390000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (33,17,41,45,'Loa JBL Go 2','JBL-GO2-DEN','Đen','https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9c/JBL_GO2_Bluetooth_speaker.jpeg/960px-JBL_GO2_Bluetooth_speaker.jpeg',690000,1,690000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (34,18,14,23,'Chuột gaming Logitech G304','LOG-G304',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c9/Logitech%28G703%29_mouse.jpg/960px-Logitech%28G703%29_mouse.jpg',790000,1,790000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (35,19,12,20,'Loa Sony SRS-XB13','SNY-XB13',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9a/SONY_SRS-XB2_01.JPG/960px-SONY_SRS-XB2_01.JPG',990000,1,990000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (36,19,35,36,'Cáp USB-A to USB-C Ugreen 3A','UGR-AC3A',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5f/2026-08-22_UGreen-USB-C-Kabel_HOF1918_RAW-Export.png/960px-2026-08-22_UGreen-USB-C-Kabel_HOF1918_RAW-Export.png',99000,1,99000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (38,20,45,50,'Bàn phím Bluetooth Logitech K380','LOG-K380',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e6/Logitech_K380_CJK_Keyboard.jpg/960px-Logitech_K380_CJK_Keyboard.jpg',790000,1,790000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (39,21,4,8,'Cáp Lightning Anker PowerLine III','ANK-PL3-1M',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/df/Lightning_connector.jpg/960px-Lightning_connector.jpg',290000,1,290000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (40,21,32,32,'Củ sạc nhanh Samsung 25W','SSG-25W',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f5/Samsung_Charger.jpg/960px-Samsung_Charger.jpg',390000,1,390000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (42,22,14,23,'Chuột gaming Logitech G304','LOG-G304',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c9/Logitech%28G703%29_mouse.jpg/960px-Logitech%28G703%29_mouse.jpg',790000,1,790000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (43,22,42,48,'Chuột không dây Logitech M331 Silent Plus','LOG-M331-DO','Đỏ','https://thumb.wikimedia.org/wikipedia/commons/thumb/3/39/Logitech_M331_Silent_Mouse.jpg/960px-Logitech_M331_Silent_Mouse.jpg',390000,1,390000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (45,23,40,42,'Loa JBL Charge 4','JBL-CHG4-DEN','Đen','https://thumb.wikimedia.org/wikipedia/commons/thumb/3/33/JBL_Charge_4_1v2.jpg/960px-JBL_Charge_4_1v2.jpg',2490000,1,2490000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (46,24,1,1,'Củ sạc Anker Nano 65W GaN','ANK-NANO65-DEN','Đen','https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4a/Silicon_vs_GaN_30W_USB-C_chargers.jpg/960px-Silicon_vs_GaN_30W_USB-C_chargers.jpg',790000,1,790000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (47,24,35,36,'Cáp USB-A to USB-C Ugreen 3A','UGR-AC3A',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5f/2026-08-22_UGreen-USB-C-Kabel_HOF1918_RAW-Export.png/960px-2026-08-22_UGreen-USB-C-Kabel_HOF1918_RAW-Export.png',99000,1,99000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (49,25,2,3,'Củ sạc Apple USB-C 20W','APL-20W',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6b/Apple_USB-C_%28USB_Type_C%29_87W_Power_Adapter_for_MacBook_Pro_%2848979218053%29.jpg/960px-Apple_USB-C_%28USB_Type_C%29_87W_Power_Adapter_for_MacBook_Pro_%2848979218053%29.jpg',490000,1,490000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (50,26,5,6,'Cáp USB-C to USB-C Ugreen 100W','UGR-CC100-1M','1m','https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c3/USB-C_cable_2017_A.jpg/960px-USB-C_cable_2017_A.jpg',159000,2,318000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (51,27,36,37,'Sạc dự phòng Samsung EB-P1100C 10000mAh','SSG-PB10K',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e9/SAMSUNG_BATTERY_PACK_%28POWER_BANK%29_EB-P1100C.jpg/960px-SAMSUNG_BATTERY_PACK_%28POWER_BANK%29_EB-P1100C.jpg',690000,1,690000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (52,28,3,4,'Củ sạc Baseus GaN5 Pro 100W','BAS-GAN5-100-DEN','Đen','https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0c/Baseus_USB_chargers-1.jpg/960px-Baseus_USB_chargers-1.jpg',1090000,1,1090000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (53,29,6,10,'Sạc dự phòng Anker PowerCore 10000mAh','ANK-PC10K-XANH','Xanh','https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ee/Anker_power_bank_and_cable.jpg/960px-Anker_power_bank_and_cable.jpg',590000,1,590000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (54,29,1,2,'Củ sạc Anker Nano 65W GaN','ANK-NANO65-TRANG','Trắng','https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4a/Silicon_vs_GaN_30W_USB-C_chargers.jpg/960px-Silicon_vs_GaN_30W_USB-C_chargers.jpg',790000,1,790000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (56,30,6,9,'Sạc dự phòng Anker PowerCore 10000mAh','ANK-PC10K-DEN','Đen','https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ee/Anker_power_bank_and_cable.jpg/960px-Anker_power_bank_and_cable.jpg',590000,1,590000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (57,31,13,21,'Chuột Logitech MX Master 3S','LOG-MX3S-DEN','Đen','https://thumb.wikimedia.org/wikipedia/commons/thumb/6/69/Logitech_MX_Master_3S_HS13.jpg/960px-Logitech_MX_Master_3S_HS13.jpg',2290000,1,2290000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (58,32,8,14,'Tai nghe Apple AirPods Pro 2','APL-APP2',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2f/AirPods_Pro_%282nd_generation%29.jpg/960px-AirPods_Pro_%282nd_generation%29.jpg',5490000,1,5490000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (59,33,10,12,'Tai nghe Sony WH-1000XM5','SNY-XM5-DEN','Đen','https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b2/Sony_Headphones_%2840476165073%29.jpg/960px-Sony_Headphones_%2840476165073%29.jpg',7990000,1,7990000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (60,34,9,15,'Tai nghe Samsung Galaxy Buds3','SSG-BUDS3-TRANG','Trắng','https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a4/Samsung_Galaxy_Buds.jpg/960px-Samsung_Galaxy_Buds.jpg',3490000,1,3490000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (61,35,46,53,'Ốp lưng Silicone MagSafe Apple cho iPhone 17','APL-SC17',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/07/Apple_iPhone_17_Silicone_Case_%28Black%29.jpg/960px-Apple_iPhone_17_Silicone_Case_%28Black%29.jpg',1190000,1,1190000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (62,36,15,24,'Bàn phím cơ Logitech MX Mechanical Mini','LOG-MXMINI-TAC','Tactile','https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7f/Beautiful_Mechanical_Keyboard.jpg/960px-Beautiful_Mechanical_Keyboard.jpg',3290000,1,3290000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (63,37,43,49,'Chuột không dây Logitech M705 Marathon','LOG-M705',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8a/2023_Mysz_komputerowa_Logitech_M705.jpg/960px-2023_Mysz_komputerowa_Logitech_M705.jpg',990000,1,990000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (64,37,33,33,'Tẩu sạc ô tô Ugreen 30W','UGR-CAR30',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/92/Ugreen_car_charger_USB-C_USB-A_30W_25845.jpg/960px-Ugreen_car_charger_USB-C_USB-A_30W_25845.jpg',290000,1,290000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (66,38,16,26,'Ốp lưng trong suốt Ugreen cho iPhone 16 Pro','UGR-OP16P','iPhone 16 Pro','https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f3/IPhone_case_wikipedia.jpg/960px-IPhone_case_wikipedia.jpg',190000,1,190000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (67,38,46,53,'Ốp lưng Silicone MagSafe Apple cho iPhone 17','APL-SC17',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/07/Apple_iPhone_17_Silicone_Case_%28Black%29.jpg/960px-Apple_iPhone_17_Silicone_Case_%28Black%29.jpg',1190000,1,1190000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (69,39,10,13,'Tai nghe Sony WH-1000XM5','SNY-XM5-BAC','Bạc','https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b2/Sony_Headphones_%2840476165073%29.jpg/960px-Sony_Headphones_%2840476165073%29.jpg',7990000,1,7990000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (70,40,13,22,'Chuột Logitech MX Master 3S','LOG-MX3S-XAM','Xám nhạt','https://thumb.wikimedia.org/wikipedia/commons/thumb/6/69/Logitech_MX_Master_3S_HS13.jpg/960px-Logitech_MX_Master_3S_HS13.jpg',2290000,1,2290000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (71,41,44,51,'Bàn phím cơ Keychron K8','KEY-K8-RED','Red','https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fb/Keychron_K8_Non-Backlight_Wireless_Mechanical_Keyboard.jpg/960px-Keychron_K8_Non-Backlight_Wireless_Mechanical_Keyboard.jpg',2190000,1,2190000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (72,42,44,52,'Bàn phím cơ Keychron K8','KEY-K8-BROWN','Brown','https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fb/Keychron_K8_Non-Backlight_Wireless_Mechanical_Keyboard.jpg/960px-Keychron_K8_Non-Backlight_Wireless_Mechanical_Keyboard.jpg',2190000,1,2190000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (73,42,42,47,'Chuột không dây Logitech M331 Silent Plus','LOG-M331-DEN','Đen','https://thumb.wikimedia.org/wikipedia/commons/thumb/3/39/Logitech_M331_Silent_Mouse.jpg/960px-Logitech_M331_Silent_Mouse.jpg',390000,1,390000);
INSERT INTO `chi_tiet_don_hang` (`id`, `don_hang_id`, `san_pham_id`, `bien_the_id`, `ten_san_pham`, `sku`, `phan_loai`, `hinh_anh`, `don_gia`, `so_luong`, `thanh_tien`) VALUES (75,43,5,7,'Cáp USB-C to USB-C Ugreen 100W','UGR-CC100-2M','2m','https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c3/USB-C_cable_2017_A.jpg/960px-USB-C_cable_2017_A.jpg',199000,1,199000);
/*!40000 ALTER TABLE `chi_tiet_don_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `danh_gia`
--

DROP TABLE IF EXISTS `danh_gia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `danh_gia` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `san_pham_id` bigint NOT NULL,
  `nguoi_dung_id` bigint NOT NULL,
  `don_hang_id` bigint NOT NULL,
  `so_sao` smallint NOT NULL,
  `noi_dung` varchar(2000) DEFAULT NULL,
  `trang_thai` varchar(20) NOT NULL DEFAULT 'cho_duyet',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `nguoi_dung_id` (`nguoi_dung_id`,`san_pham_id`,`don_hang_id`),
  KEY `don_hang_id` (`don_hang_id`),
  KEY `ix_danh_gia_san_pham` (`san_pham_id`,`trang_thai`),
  CONSTRAINT `danh_gia_ibfk_1` FOREIGN KEY (`san_pham_id`) REFERENCES `san_pham` (`id`) ON DELETE CASCADE,
  CONSTRAINT `danh_gia_ibfk_2` FOREIGN KEY (`nguoi_dung_id`) REFERENCES `nguoi_dung` (`id`) ON DELETE CASCADE,
  CONSTRAINT `danh_gia_ibfk_3` FOREIGN KEY (`don_hang_id`) REFERENCES `don_hang` (`id`) ON DELETE CASCADE,
  CONSTRAINT `danh_gia_chk_1` CHECK ((`so_sao` between 1 and 5)),
  CONSTRAINT `danh_gia_chk_2` CHECK ((`trang_thai` in (_utf8mb4'cho_duyet',_utf8mb4'da_duyet',_utf8mb4'da_an')))
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `danh_gia`
--

LOCK TABLES `danh_gia` WRITE;
/*!40000 ALTER TABLE `danh_gia` DISABLE KEYS */;
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (1,45,3,1,5,'Gõ êm, kết nối nhanh với cả laptop và điện thoại.','da_duyet','2026-08-28 16:17:56.000000','2026-08-28 16:17:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (2,35,3,1,4,'Cáp chắc chắn, sạc ổn định.','da_duyet','2026-08-28 16:17:56.000000','2026-08-28 16:17:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (3,36,4,2,5,'Pin trâu, vỏ kim loại cầm rất chắc tay.','da_duyet','2026-08-31 14:17:56.000000','2026-08-31 14:17:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (4,42,5,3,5,'Click êm thật sự, dùng trong thư viện không làm phiền ai.','da_duyet','2026-08-31 17:07:56.000000','2026-08-31 17:07:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (5,39,6,4,4,'Nhẹ, đeo lâu không đau tai, bass hơi ít.','da_duyet','2026-09-02 11:37:56.000000','2026-09-02 11:37:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (6,47,8,7,3,'Ốp ổn nhưng sau một tháng hơi ngả màu.','da_duyet','2026-09-12 12:57:56.000000','2026-09-12 12:57:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (7,38,9,8,5,'Chống ồn tốt, kết nối với điện thoại Samsung rất nhanh.','da_duyet','2026-09-15 16:07:56.000000','2026-09-15 16:07:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (8,40,10,9,5,'Âm thanh to, pin dùng cả buổi dã ngoại.','da_duyet','2026-09-15 14:07:56.000000','2026-09-15 14:07:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (9,34,3,10,4,'Cáp dài 2m tiện, sạc laptop được.','da_duyet','2026-09-19 17:37:56.000000','2026-09-19 17:37:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (10,37,4,12,4,'Sạc nhanh, hơi nặng.','da_duyet','2026-09-24 14:57:56.000000','2026-09-24 14:57:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (11,38,2,13,4,'Tai nghe tốt, giao hàng nhanh.','da_duyet','2026-09-27 16:27:56.000000','2026-09-27 16:27:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (12,48,2,13,2,'Dán bị bọt khí ở góc, phải dán lại.','da_duyet','2026-09-27 16:27:56.000000','2026-09-27 16:27:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (13,41,5,14,5,'Nhỏ xinh, âm lượng bất ngờ.','da_duyet','2026-09-30 13:37:56.000000','2026-09-30 13:37:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (14,49,6,16,4,'Chống nhìn trộm tốt, màn hình hơi tối hơn.','da_duyet','2026-10-04 15:47:56.000000','2026-10-04 15:47:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (15,41,7,17,1,'Mua giá rẻ hơn liên hệ zalo 09xx!!!','da_an','2026-10-08 12:37:56.000000','2026-10-08 12:37:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (16,12,9,19,4,'Loa nhỏ mà bass khá.','da_duyet','2026-10-14 14:37:56.000000','2026-10-14 14:37:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (17,4,3,21,5,'Cáp chuẩn MFi, không báo lỗi.','da_duyet','2026-10-18 16:37:56.000000','2026-10-18 16:37:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (18,14,8,22,5,'Chuột nhẹ, chơi game mượt.','da_duyet','2026-10-22 12:17:56.000000','2026-10-22 12:17:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (19,40,11,23,4,'Âm hay, giá hơi cao.','da_duyet','2026-10-26 16:57:56.000000','2026-10-26 16:57:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (20,1,4,24,5,'Nhỏ gọn mà sạc được laptop 65W.','da_duyet','2026-10-28 13:47:56.000000','2026-10-28 13:47:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (21,2,2,25,5,'Hàng chính hãng, sạc iPhone nhanh.','da_duyet','2026-10-31 15:57:56.000000','2026-10-31 15:57:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (22,5,5,26,4,'Cáp bện dù chắc.','cho_duyet','2026-11-04 14:27:56.000000','2026-11-04 14:27:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (23,6,7,29,5,'Màu xanh đẹp, sạc nhanh.','cho_duyet','2026-11-11 13:07:56.000000','2026-11-11 13:07:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (24,13,10,31,5,'Chuột tốt nhất mình từng dùng, cuộn MagSpeed rất đã.','da_duyet','2026-11-14 13:57:56.000000','2026-11-14 13:57:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (25,8,3,32,5,'Chống ồn xuất sắc.','da_duyet','2026-11-17 16:17:56.000000','2026-11-17 16:17:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (26,10,8,33,5,'Đáng tiền, chống ồn số 1.','cho_duyet','2026-11-17 12:07:56.000000','2026-11-17 12:07:56.000000');
INSERT INTO `danh_gia` (`id`, `san_pham_id`, `nguoi_dung_id`, `don_hang_id`, `so_sao`, `noi_dung`, `trang_thai`, `created_at`, `updated_at`) VALUES (27,9,11,34,3,'Thiết kế mở nên chống ồn không bằng loại nhét tai.','cho_duyet','2026-11-20 16:47:56.000000','2026-11-20 16:47:56.000000');
/*!40000 ALTER TABLE `danh_gia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `danh_muc`
--

DROP TABLE IF EXISTS `danh_muc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `danh_muc` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `danh_muc_cha_id` bigint DEFAULT NULL,
  `ten` varchar(100) NOT NULL,
  `slug` varchar(120) NOT NULL,
  `mo_ta` text,
  `hinh_anh` varchar(255) DEFAULT NULL,
  `thu_tu` int NOT NULL DEFAULT '0',
  `hien_thi` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `deleted_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_danh_muc_slug` ((if((`deleted_at` is null),`slug`,NULL))),
  KEY `danh_muc_cha_id` (`danh_muc_cha_id`),
  CONSTRAINT `danh_muc_ibfk_1` FOREIGN KEY (`danh_muc_cha_id`) REFERENCES `danh_muc` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `danh_muc`
--

LOCK TABLES `danh_muc` WRITE;
/*!40000 ALTER TABLE `danh_muc` DISABLE KEYS */;
INSERT INTO `danh_muc` (`id`, `danh_muc_cha_id`, `ten`, `slug`, `mo_ta`, `hinh_anh`, `thu_tu`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (1,NULL,'Sạc & cáp','sac-cap',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/53/Charging_a_smartphone_with_a_USB_power_bank.jpg/960px-Charging_a_smartphone_with_a_USB_power_bank.jpg',1,1,'2026-11-22 19:37:56.634638','2026-11-22 19:37:56.634638',NULL);
INSERT INTO `danh_muc` (`id`, `danh_muc_cha_id`, `ten`, `slug`, `mo_ta`, `hinh_anh`, `thu_tu`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (2,NULL,'Âm thanh','am-thanh',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f6/Headphones_on_desk.jpg/960px-Headphones_on_desk.jpg',2,1,'2026-11-22 19:37:56.634638','2026-11-22 19:37:56.634638',NULL);
INSERT INTO `danh_muc` (`id`, `danh_muc_cha_id`, `ten`, `slug`, `mo_ta`, `hinh_anh`, `thu_tu`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (3,NULL,'Phụ kiện máy tính','phu-kien-may-tinh',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c6/Black_and_white_computer_desk_%28Unsplash%29.jpg/960px-Black_and_white_computer_desk_%28Unsplash%29.jpg',3,1,'2026-11-22 19:37:56.634638','2026-11-22 19:37:56.634638',NULL);
INSERT INTO `danh_muc` (`id`, `danh_muc_cha_id`, `ten`, `slug`, `mo_ta`, `hinh_anh`, `thu_tu`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (4,NULL,'Ốp lưng & bảo vệ','op-lung-bao-ve',NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a1/Apple_iPhone_17_Silicone_Case_%28Black%29_unboxing.jpg/960px-Apple_iPhone_17_Silicone_Case_%28Black%29_unboxing.jpg',4,1,'2026-11-22 19:37:56.634638','2026-11-22 19:37:56.634638',NULL);
INSERT INTO `danh_muc` (`id`, `danh_muc_cha_id`, `ten`, `slug`, `mo_ta`, `hinh_anh`, `thu_tu`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (5,1,'Củ sạc','cu-sac',NULL,NULL,1,1,'2026-11-22 19:37:56.634974','2026-11-22 19:37:56.634974',NULL);
INSERT INTO `danh_muc` (`id`, `danh_muc_cha_id`, `ten`, `slug`, `mo_ta`, `hinh_anh`, `thu_tu`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (6,1,'Cáp sạc','cap-sac',NULL,NULL,2,1,'2026-11-22 19:37:56.634974','2026-11-22 19:37:56.634974',NULL);
INSERT INTO `danh_muc` (`id`, `danh_muc_cha_id`, `ten`, `slug`, `mo_ta`, `hinh_anh`, `thu_tu`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (7,1,'Sạc dự phòng','sac-du-phong',NULL,NULL,3,1,'2026-11-22 19:37:56.634974','2026-11-22 19:37:56.634974',NULL);
INSERT INTO `danh_muc` (`id`, `danh_muc_cha_id`, `ten`, `slug`, `mo_ta`, `hinh_anh`, `thu_tu`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (8,2,'Tai nghe','tai-nghe',NULL,NULL,1,1,'2026-11-22 19:37:56.634974','2026-11-22 19:37:56.634974',NULL);
INSERT INTO `danh_muc` (`id`, `danh_muc_cha_id`, `ten`, `slug`, `mo_ta`, `hinh_anh`, `thu_tu`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (9,2,'Loa bluetooth','loa',NULL,NULL,2,1,'2026-11-22 19:37:56.634974','2026-11-22 19:37:56.634974',NULL);
INSERT INTO `danh_muc` (`id`, `danh_muc_cha_id`, `ten`, `slug`, `mo_ta`, `hinh_anh`, `thu_tu`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (10,3,'Chuột','chuot',NULL,NULL,1,1,'2026-11-22 19:37:56.634974','2026-11-22 19:37:56.634974',NULL);
INSERT INTO `danh_muc` (`id`, `danh_muc_cha_id`, `ten`, `slug`, `mo_ta`, `hinh_anh`, `thu_tu`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (11,3,'Bàn phím','ban-phim',NULL,NULL,2,1,'2026-11-22 19:37:56.634974','2026-11-22 19:37:56.634974',NULL);
INSERT INTO `danh_muc` (`id`, `danh_muc_cha_id`, `ten`, `slug`, `mo_ta`, `hinh_anh`, `thu_tu`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (12,4,'Ốp lưng','op-lung',NULL,NULL,1,1,'2026-11-22 19:37:56.634974','2026-11-22 19:37:56.634974',NULL);
INSERT INTO `danh_muc` (`id`, `danh_muc_cha_id`, `ten`, `slug`, `mo_ta`, `hinh_anh`, `thu_tu`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (13,4,'Kính cường lực','cuong-luc',NULL,NULL,2,1,'2026-11-22 19:37:56.634974','2026-11-22 19:37:56.634974',NULL);
/*!40000 ALTER TABLE `danh_muc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dia_chi`
--

DROP TABLE IF EXISTS `dia_chi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dia_chi` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nguoi_dung_id` bigint NOT NULL,
  `ten_nguoi_nhan` varchar(100) NOT NULL,
  `so_dien_thoai` varchar(20) NOT NULL,
  `tinh_thanh` varchar(100) NOT NULL,
  `phuong_xa` varchar(100) NOT NULL,
  `dia_chi_chi_tiet` varchar(255) NOT NULL,
  `la_mac_dinh` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_dia_chi_mac_dinh` ((if(`la_mac_dinh`,`nguoi_dung_id`,NULL))),
  KEY `nguoi_dung_id` (`nguoi_dung_id`),
  CONSTRAINT `dia_chi_ibfk_1` FOREIGN KEY (`nguoi_dung_id`) REFERENCES `nguoi_dung` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dia_chi`
--

LOCK TABLES `dia_chi` WRITE;
/*!40000 ALTER TABLE `dia_chi` DISABLE KEYS */;
INSERT INTO `dia_chi` (`id`, `nguoi_dung_id`, `ten_nguoi_nhan`, `so_dien_thoai`, `tinh_thanh`, `phuong_xa`, `dia_chi_chi_tiet`, `la_mac_dinh`, `created_at`, `updated_at`) VALUES (1,2,'Nguyễn Văn Khách','0900000002','Thành phố Hồ Chí Minh','Phường Sài Gòn','12 Nguyễn Huệ',1,'2026-11-22 19:37:56.489401','2026-11-22 19:37:56.489401');
INSERT INTO `dia_chi` (`id`, `nguoi_dung_id`, `ten_nguoi_nhan`, `so_dien_thoai`, `tinh_thanh`, `phuong_xa`, `dia_chi_chi_tiet`, `la_mac_dinh`, `created_at`, `updated_at`) VALUES (2,3,'Nguyễn Thị Lan','0912345601','Thành phố Hà Nội','Phường Cầu Giấy','25 Trần Thái Tông',1,'2026-11-22 19:37:56.706489','2026-11-22 19:37:56.706489');
INSERT INTO `dia_chi` (`id`, `nguoi_dung_id`, `ten_nguoi_nhan`, `so_dien_thoai`, `tinh_thanh`, `phuong_xa`, `dia_chi_chi_tiet`, `la_mac_dinh`, `created_at`, `updated_at`) VALUES (3,4,'Trần Quang Minh','0912345602','Thành phố Hồ Chí Minh','Phường Bến Thành','88 Lê Lợi',1,'2026-11-22 19:37:56.706489','2026-11-22 19:37:56.706489');
INSERT INTO `dia_chi` (`id`, `nguoi_dung_id`, `ten_nguoi_nhan`, `so_dien_thoai`, `tinh_thanh`, `phuong_xa`, `dia_chi_chi_tiet`, `la_mac_dinh`, `created_at`, `updated_at`) VALUES (4,5,'Lê Thu Hương','0912345603','Thành phố Đà Nẵng','Phường Hải Châu','15 Bạch Đằng',1,'2026-11-22 19:37:56.706489','2026-11-22 19:37:56.706489');
INSERT INTO `dia_chi` (`id`, `nguoi_dung_id`, `ten_nguoi_nhan`, `so_dien_thoai`, `tinh_thanh`, `phuong_xa`, `dia_chi_chi_tiet`, `la_mac_dinh`, `created_at`, `updated_at`) VALUES (5,6,'Phạm Anh Đức','0912345604','Thành phố Cần Thơ','Phường Ninh Kiều','42 Hai Bà Trưng',1,'2026-11-22 19:37:56.706489','2026-11-22 19:37:56.706489');
INSERT INTO `dia_chi` (`id`, `nguoi_dung_id`, `ten_nguoi_nhan`, `so_dien_thoai`, `tinh_thanh`, `phuong_xa`, `dia_chi_chi_tiet`, `la_mac_dinh`, `created_at`, `updated_at`) VALUES (6,7,'Võ Ngọc Mai','0912345605','Thành phố Hồ Chí Minh','Phường Tân Định','120 Hai Bà Trưng',1,'2026-11-22 19:37:56.706489','2026-11-22 19:37:56.706489');
INSERT INTO `dia_chi` (`id`, `nguoi_dung_id`, `ten_nguoi_nhan`, `so_dien_thoai`, `tinh_thanh`, `phuong_xa`, `dia_chi_chi_tiet`, `la_mac_dinh`, `created_at`, `updated_at`) VALUES (7,8,'Đặng Minh Khoa','0912345606','Thành phố Hải Phòng','Phường Hồng Bàng','7 Điện Biên Phủ',1,'2026-11-22 19:37:56.706489','2026-11-22 19:37:56.706489');
INSERT INTO `dia_chi` (`id`, `nguoi_dung_id`, `ten_nguoi_nhan`, `so_dien_thoai`, `tinh_thanh`, `phuong_xa`, `dia_chi_chi_tiet`, `la_mac_dinh`, `created_at`, `updated_at`) VALUES (8,9,'Bùi Phương Thảo','0912345607','Thành phố Huế','Phường Thuận Hóa','30 Lê Lợi',1,'2026-11-22 19:37:56.706489','2026-11-22 19:37:56.706489');
INSERT INTO `dia_chi` (`id`, `nguoi_dung_id`, `ten_nguoi_nhan`, `so_dien_thoai`, `tinh_thanh`, `phuong_xa`, `dia_chi_chi_tiet`, `la_mac_dinh`, `created_at`, `updated_at`) VALUES (9,10,'Hoàng Văn Tuấn','0912345608','Thành phố Hà Nội','Phường Ba Đình','12 Phan Đình Phùng',1,'2026-11-22 19:37:56.706489','2026-11-22 19:37:56.706489');
INSERT INTO `dia_chi` (`id`, `nguoi_dung_id`, `ten_nguoi_nhan`, `so_dien_thoai`, `tinh_thanh`, `phuong_xa`, `dia_chi_chi_tiet`, `la_mac_dinh`, `created_at`, `updated_at`) VALUES (10,11,'Đỗ Bảo Ngọc','0912345609','Thành phố Hồ Chí Minh','Phường Thủ Đức','5 Võ Văn Ngân',1,'2026-11-22 19:37:56.706489','2026-11-22 19:37:56.706489');
INSERT INTO `dia_chi` (`id`, `nguoi_dung_id`, `ten_nguoi_nhan`, `so_dien_thoai`, `tinh_thanh`, `phuong_xa`, `dia_chi_chi_tiet`, `la_mac_dinh`, `created_at`, `updated_at`) VALUES (11,12,'Ngô Thành Long','0912345610','Thành phố Hà Nội','Phường Hoàn Kiếm','9 Hàng Bài',1,'2026-11-22 19:37:56.706489','2026-11-22 19:37:56.706489');
INSERT INTO `dia_chi` (`id`, `nguoi_dung_id`, `ten_nguoi_nhan`, `so_dien_thoai`, `tinh_thanh`, `phuong_xa`, `dia_chi_chi_tiet`, `la_mac_dinh`, `created_at`, `updated_at`) VALUES (17,2,'Nguyễn Văn Khách (công ty)','0900000003','Thành phố Hồ Chí Minh','Phường Bến Nghé','Tầng 5, 2 Hải Triều',0,'2026-11-22 19:37:56.773899','2026-11-22 19:37:56.773899');
/*!40000 ALTER TABLE `dia_chi` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `don_hang`
--

DROP TABLE IF EXISTS `don_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `don_hang` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ma_don_hang` varchar(20) NOT NULL,
  `nguoi_dung_id` bigint DEFAULT NULL,
  `ma_giam_gia_id` bigint DEFAULT NULL,
  `ten_nguoi_nhan` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `so_dien_thoai` varchar(20) NOT NULL,
  `dia_chi_giao_hang` varchar(500) NOT NULL,
  `ghi_chu` varchar(500) DEFAULT NULL,
  `ghi_chu_quan_tri` varchar(500) DEFAULT NULL,
  `tam_tinh` bigint NOT NULL,
  `tien_giam_gia` bigint NOT NULL DEFAULT '0',
  `phi_van_chuyen` bigint NOT NULL DEFAULT '0',
  `tong_tien` bigint NOT NULL,
  `phuong_thuc_thanh_toan` varchar(10) NOT NULL,
  `trang_thai_thanh_toan` varchar(20) NOT NULL DEFAULT 'chua_thanh_toan',
  `trang_thai` varchar(20) NOT NULL DEFAULT 'cho_xac_nhan',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `ma_don_hang` (`ma_don_hang`),
  KEY `nguoi_dung_id` (`nguoi_dung_id`),
  KEY `ma_giam_gia_id` (`ma_giam_gia_id`),
  KEY `ix_don_hang_trang_thai` (`trang_thai`,`created_at`),
  KEY `ix_don_hang_email` (`email`),
  CONSTRAINT `don_hang_ibfk_1` FOREIGN KEY (`nguoi_dung_id`) REFERENCES `nguoi_dung` (`id`) ON DELETE SET NULL,
  CONSTRAINT `don_hang_ibfk_2` FOREIGN KEY (`ma_giam_gia_id`) REFERENCES `ma_giam_gia` (`id`),
  CONSTRAINT `don_hang_chk_1` CHECK ((`tam_tinh` >= 0)),
  CONSTRAINT `don_hang_chk_2` CHECK ((`tien_giam_gia` >= 0)),
  CONSTRAINT `don_hang_chk_3` CHECK ((`phi_van_chuyen` >= 0)),
  CONSTRAINT `don_hang_chk_4` CHECK ((`phuong_thuc_thanh_toan` in (_utf8mb4'cod',_utf8mb4'vnpay'))),
  CONSTRAINT `don_hang_chk_5` CHECK ((`trang_thai_thanh_toan` in (_utf8mb4'chua_thanh_toan',_utf8mb4'da_thanh_toan',_utf8mb4'da_hoan_tien'))),
  CONSTRAINT `don_hang_chk_6` CHECK ((`trang_thai` in (_utf8mb4'cho_xac_nhan',_utf8mb4'da_xac_nhan',_utf8mb4'dang_giao',_utf8mb4'hoan_thanh',_utf8mb4'da_huy'))),
  CONSTRAINT `don_hang_chk_7` CHECK ((`tien_giam_gia` <= `tam_tinh`)),
  CONSTRAINT `don_hang_chk_8` CHECK ((`tong_tien` = ((`tam_tinh` - `tien_giam_gia`) + `phi_van_chuyen`)))
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `don_hang`
--

LOCK TABLES `don_hang` WRITE;
/*!40000 ALTER TABLE `don_hang` DISABLE KEYS */;
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (1,'DH260823WBQ5JX',3,NULL,'Nguyễn Thị Lan','nguyen.lan@example.com','0912345601','25 Trần Thái Tông, Phường Cầu Giấy, Thành phố Hà Nội',NULL,'Đã thu tiền COD',988000,0,0,988000,'cod','da_thanh_toan','hoan_thanh','2026-08-23 16:17:56.000000','2026-08-26 16:17:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (2,'DH2608255JXCR6',4,1,'Trần Quang Minh','tran.minh@example.com','0912345602','88 Lê Lợi, Phường Bến Thành, Thành phố Hồ Chí Minh',NULL,NULL,789000,78900,0,710100,'vnpay','da_thanh_toan','hoan_thanh','2026-08-25 14:17:56.000000','2026-08-28 14:17:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (3,'DH260827CR6KYD',5,NULL,'Lê Thu Hương','le.huong@example.com','0912345603','15 Bạch Đằng, Phường Hải Châu, Thành phố Đà Nẵng','Giao giờ hành chính','Đã thu tiền COD',390000,0,30000,420000,'cod','da_thanh_toan','hoan_thanh','2026-08-27 17:07:56.000000','2026-08-30 17:07:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (4,'DH260828KYDS7L',6,NULL,'Phạm Anh Đức','pham.duc@example.com','0912345604','42 Hai Bà Trưng, Phường Ninh Kiều, Thành phố Cần Thơ',NULL,NULL,1380000,0,0,1380000,'vnpay','da_thanh_toan','hoan_thanh','2026-08-28 11:37:56.000000','2026-08-31 11:37:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (5,'DH260830S7LZET',NULL,NULL,'Phan Thị Hồng','phan.hong@example.com','0987654321','18 Nguyễn Trãi, Phường Thanh Xuân, Thành phố Hà Nội',NULL,'Đã thu tiền COD',780000,0,0,780000,'cod','da_thanh_toan','hoan_thanh','2026-08-30 15:17:56.000000','2026-09-02 15:17:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (6,'DH260905ZET8M2',7,NULL,'Võ Ngọc Mai','vo.mai@example.com','0912345605','120 Hai Bà Trưng, Phường Tân Định, Thành phố Hồ Chí Minh',NULL,NULL,2490000,0,0,2490000,'cod','chua_thanh_toan','da_huy','2026-09-05 18:07:56.000000','2026-09-05 20:07:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (7,'DH2609078M2FU9',8,3,'Đặng Minh Khoa','dang.khoa@example.com','0912345606','7 Điện Biên Phủ, Phường Hồng Bàng, Thành phố Hải Phòng',NULL,'Đã thu tiền COD',349000,20000,30000,359000,'cod','da_thanh_toan','hoan_thanh','2026-09-07 12:57:56.000000','2026-09-10 12:57:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (8,'DH260909FU9N3G',9,NULL,'Bùi Phương Thảo','bui.thao@example.com','0912345607','30 Lê Lợi, Phường Thuận Hóa, Thành phố Huế',NULL,NULL,3990000,0,0,3990000,'vnpay','da_thanh_toan','hoan_thanh','2026-09-09 16:07:56.000000','2026-09-12 16:07:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (9,'DH260911N3GVAP',10,2,'Hoàng Văn Tuấn','hoang.tuan@example.com','0912345608','12 Phan Đình Phùng, Phường Ba Đình, Thành phố Hà Nội','Gọi trước khi giao','Đã thu tiền COD',2490000,50000,0,2440000,'cod','da_thanh_toan','hoan_thanh','2026-09-11 14:07:56.000000','2026-09-14 14:07:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (10,'DH260914VAP4HW',3,NULL,'Nguyễn Thị Lan','nguyen.lan@example.com','0912345601','25 Trần Thái Tông, Phường Cầu Giấy, Thành phố Hà Nội',NULL,NULL,708000,0,0,708000,'vnpay','da_thanh_toan','hoan_thanh','2026-09-14 17:37:56.000000','2026-09-17 17:37:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (11,'DH2609194HWBQ5',11,NULL,'Đỗ Bảo Ngọc','do.ngoc@example.com','0912345609','5 Võ Văn Ngân, Phường Thủ Đức, Thành phố Hồ Chí Minh',NULL,NULL,550000,0,0,550000,'vnpay','chua_thanh_toan','da_huy','2026-09-19 11:17:56.000000','2026-09-19 11:52:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (12,'DH260920BQ5JXC',4,NULL,'Trần Quang Minh','tran.minh@example.com','0912345602','88 Lê Lợi, Phường Bến Thành, Thành phố Hồ Chí Minh',NULL,'Đã thu tiền COD',679000,0,0,679000,'cod','da_thanh_toan','hoan_thanh','2026-09-20 14:57:56.000000','2026-09-23 14:57:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (13,'DH260922JXCR6K',2,1,'Nguyễn Văn Khách','khach@techgear.vn','0900000002','12 Nguyễn Huệ, Phường Sài Gòn, Thành phố Hồ Chí Minh',NULL,NULL,4110000,100000,0,4010000,'vnpay','da_thanh_toan','hoan_thanh','2026-09-22 16:27:56.000000','2026-09-25 16:27:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (14,'DH260924R6KYDS',5,NULL,'Lê Thu Hương','le.huong@example.com','0912345603','15 Bạch Đằng, Phường Hải Châu, Thành phố Đà Nẵng',NULL,'Đã thu tiền COD',690000,0,0,690000,'cod','da_thanh_toan','hoan_thanh','2026-09-24 13:37:56.000000','2026-09-27 13:37:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (15,'DH260926YDS7LZ',12,NULL,'Ngô Thành Long','ngo.long@example.com','0912345610','9 Hàng Bài, Phường Hoàn Kiếm, Thành phố Hà Nội',NULL,NULL,2070000,0,0,2070000,'cod','chua_thanh_toan','da_huy','2026-09-26 17:57:56.000000','2026-09-26 22:57:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (16,'DH2609297LZET8',6,NULL,'Phạm Anh Đức','pham.duc@example.com','0912345604','42 Hai Bà Trưng, Phường Ninh Kiều, Thành phố Cần Thơ',NULL,NULL,970000,0,0,970000,'vnpay','da_thanh_toan','hoan_thanh','2026-09-29 15:47:56.000000','2026-10-02 15:47:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (17,'DH261002ET8M2F',7,3,'Võ Ngọc Mai','vo.mai@example.com','0912345605','120 Hai Bà Trưng, Phường Tân Định, Thành phố Hồ Chí Minh',NULL,'Đã thu tiền COD',690000,20000,0,670000,'cod','da_thanh_toan','hoan_thanh','2026-10-02 12:37:56.000000','2026-10-05 12:37:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (18,'DH261004M2FU9N',NULL,NULL,'Lý Gia Huy','ly.huy@example.com','0987654322','66 Cách Mạng Tháng Tám, Phường Bàn Cờ, Thành phố Hồ Chí Minh',NULL,NULL,790000,0,0,790000,'vnpay','da_thanh_toan','hoan_thanh','2026-10-04 17:17:56.000000','2026-10-07 17:17:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (19,'DH261009U9N3GV',9,NULL,'Bùi Phương Thảo','bui.thao@example.com','0912345607','30 Lê Lợi, Phường Thuận Hóa, Thành phố Huế',NULL,'Đã thu tiền COD',1089000,0,0,1089000,'cod','da_thanh_toan','hoan_thanh','2026-10-09 14:37:56.000000','2026-10-12 14:37:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (20,'DH2610113GVAP4',10,NULL,'Hoàng Văn Tuấn','hoang.tuan@example.com','0912345608','12 Phan Đình Phùng, Phường Ba Đình, Thành phố Hà Nội',NULL,NULL,790000,0,0,790000,'vnpay','da_hoan_tien','da_huy','2026-10-11 15:27:56.000000','2026-10-11 21:27:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (21,'DH261014AP4HWB',3,NULL,'Nguyễn Thị Lan','nguyen.lan@example.com','0912345601','25 Trần Thái Tông, Phường Cầu Giấy, Thành phố Hà Nội',NULL,'Đã thu tiền COD',680000,0,0,680000,'cod','da_thanh_toan','hoan_thanh','2026-10-14 16:37:56.000000','2026-10-17 16:37:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (22,'DH261017HWBQ5J',8,1,'Đặng Minh Khoa','dang.khoa@example.com','0912345606','7 Điện Biên Phủ, Phường Hồng Bàng, Thành phố Hải Phòng',NULL,NULL,1180000,100000,0,1080000,'vnpay','da_thanh_toan','hoan_thanh','2026-10-17 12:17:56.000000','2026-10-20 12:17:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (23,'DH261020Q5JXCR',11,NULL,'Đỗ Bảo Ngọc','do.ngoc@example.com','0912345609','5 Võ Văn Ngân, Phường Thủ Đức, Thành phố Hồ Chí Minh',NULL,'Đã thu tiền COD',2490000,0,0,2490000,'cod','da_thanh_toan','hoan_thanh','2026-10-20 16:57:56.000000','2026-10-23 16:57:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (24,'DH261024XCR6KY',4,NULL,'Trần Quang Minh','tran.minh@example.com','0912345602','88 Lê Lợi, Phường Bến Thành, Thành phố Hồ Chí Minh',NULL,NULL,889000,0,0,889000,'vnpay','da_thanh_toan','hoan_thanh','2026-10-24 13:47:56.000000','2026-10-27 13:47:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (25,'DH2610266KYDS7',2,3,'Nguyễn Văn Khách','khach@techgear.vn','0900000002','12 Nguyễn Huệ, Phường Sài Gòn, Thành phố Hồ Chí Minh',NULL,'Đã thu tiền COD',490000,20000,30000,500000,'cod','da_thanh_toan','hoan_thanh','2026-10-26 15:57:56.000000','2026-10-29 15:57:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (26,'DH261029DS7LZE',5,NULL,'Lê Thu Hương','le.huong@example.com','0912345603','15 Bạch Đằng, Phường Hải Châu, Thành phố Đà Nẵng',NULL,NULL,318000,0,30000,348000,'vnpay','da_thanh_toan','hoan_thanh','2026-10-29 14:27:56.000000','2026-11-01 14:27:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (27,'DH261101LZET8M',NULL,NULL,'Phan Thị Hồng','phan.hong@example.com','0987654321','18 Nguyễn Trãi, Phường Thanh Xuân, Thành phố Hà Nội',NULL,NULL,690000,0,0,690000,'cod','chua_thanh_toan','da_huy','2026-11-01 17:27:56.000000','2026-11-01 22:27:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (28,'DH261103T8M2FU',6,NULL,'Phạm Anh Đức','pham.duc@example.com','0912345604','42 Hai Bà Trưng, Phường Ninh Kiều, Thành phố Cần Thơ',NULL,'Đã thu tiền COD',1090000,0,0,1090000,'cod','da_thanh_toan','hoan_thanh','2026-11-03 15:07:56.000000','2026-11-06 15:07:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (29,'DH2611052FU9N3',7,2,'Võ Ngọc Mai','vo.mai@example.com','0912345605','120 Hai Bà Trưng, Phường Tân Định, Thành phố Hồ Chí Minh',NULL,NULL,1380000,50000,0,1330000,'vnpay','da_thanh_toan','hoan_thanh','2026-11-05 13:07:56.000000','2026-11-08 13:07:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (30,'DH2611079N3GVA',9,NULL,'Bùi Phương Thảo','bui.thao@example.com','0912345607','30 Lê Lợi, Phường Thuận Hóa, Thành phố Huế',NULL,'Đã thu tiền COD',590000,0,0,590000,'cod','da_thanh_toan','hoan_thanh','2026-11-07 17:47:56.000000','2026-11-10 17:47:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (31,'DH261109GVAP4H',10,NULL,'Hoàng Văn Tuấn','hoang.tuan@example.com','0912345608','12 Phan Đình Phùng, Phường Ba Đình, Thành phố Hà Nội',NULL,NULL,2290000,0,0,2290000,'vnpay','da_thanh_toan','hoan_thanh','2026-11-09 13:57:56.000000','2026-11-12 13:57:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (32,'DH261111P4HWBQ',3,1,'Nguyễn Thị Lan','nguyen.lan@example.com','0912345601','25 Trần Thái Tông, Phường Cầu Giấy, Thành phố Hà Nội',NULL,NULL,5490000,100000,0,5390000,'vnpay','da_thanh_toan','hoan_thanh','2026-11-11 16:17:56.000000','2026-11-14 16:17:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (33,'DH261113WBQ5JX',8,NULL,'Đặng Minh Khoa','dang.khoa@example.com','0912345606','7 Điện Biên Phủ, Phường Hồng Bàng, Thành phố Hải Phòng',NULL,'Đã thu tiền COD',7990000,0,0,7990000,'cod','da_thanh_toan','hoan_thanh','2026-11-13 12:07:56.000000','2026-11-16 12:07:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (34,'DH2611155JXCR6',11,NULL,'Đỗ Bảo Ngọc','do.ngoc@example.com','0912345609','5 Võ Văn Ngân, Phường Thủ Đức, Thành phố Hồ Chí Minh',NULL,'Đã thu tiền COD',3490000,0,0,3490000,'cod','da_thanh_toan','hoan_thanh','2026-11-15 16:47:56.000000','2026-11-18 16:47:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (35,'DH261116CR6KYD',4,NULL,'Trần Quang Minh','tran.minh@example.com','0912345602','88 Lê Lợi, Phường Bến Thành, Thành phố Hồ Chí Minh',NULL,NULL,1190000,0,0,1190000,'cod','chua_thanh_toan','da_huy','2026-11-16 14:47:56.000000','2026-11-16 16:47:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (36,'DH261117KYDS7L',2,NULL,'Nguyễn Văn Khách','khach@techgear.vn','0900000002','12 Nguyễn Huệ, Phường Sài Gòn, Thành phố Hồ Chí Minh',NULL,NULL,3290000,0,0,3290000,'vnpay','da_thanh_toan','hoan_thanh','2026-11-17 13:17:56.000000','2026-11-20 13:17:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (37,'DH261119S7LZET',5,NULL,'Lê Thu Hương','le.huong@example.com','0912345603','15 Bạch Đằng, Phường Hải Châu, Thành phố Đà Nẵng',NULL,NULL,1280000,0,0,1280000,'cod','chua_thanh_toan','dang_giao','2026-11-19 15:37:56.000000','2026-11-20 15:37:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (38,'DH261120ZET8M2',6,NULL,'Phạm Anh Đức','pham.duc@example.com','0912345604','42 Hai Bà Trưng, Phường Ninh Kiều, Thành phố Cần Thơ',NULL,NULL,1380000,0,0,1380000,'vnpay','da_thanh_toan','dang_giao','2026-11-20 14:07:56.000000','2026-11-21 14:07:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (39,'DH2611218M2FU9',7,1,'Võ Ngọc Mai','vo.mai@example.com','0912345605','120 Hai Bà Trưng, Phường Tân Định, Thành phố Hồ Chí Minh',NULL,NULL,7990000,100000,0,7890000,'cod','chua_thanh_toan','da_xac_nhan','2026-11-21 12:57:56.000000','2026-11-21 14:57:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (40,'DH261121FU9N3G',NULL,NULL,'Lý Gia Huy','ly.huy@example.com','0987654322','66 Cách Mạng Tháng Tám, Phường Bàn Cờ, Thành phố Hồ Chí Minh',NULL,NULL,2290000,0,0,2290000,'vnpay','da_thanh_toan','da_xac_nhan','2026-11-21 17:07:56.000000','2026-11-21 19:07:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (41,'DH261122N3GVAP',9,NULL,'Bùi Phương Thảo','bui.thao@example.com','0912345607','30 Lê Lợi, Phường Thuận Hóa, Thành phố Huế',NULL,NULL,2190000,0,0,2190000,'cod','chua_thanh_toan','cho_xac_nhan','2026-11-22 16:37:56.000000','2026-11-22 16:40:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (42,'DH261122VAP4HW',10,NULL,'Hoàng Văn Tuấn','hoang.tuan@example.com','0912345608','12 Phan Đình Phùng, Phường Ba Đình, Thành phố Hà Nội',NULL,NULL,2580000,0,0,2580000,'vnpay','da_thanh_toan','cho_xac_nhan','2026-11-22 18:37:56.000000','2026-11-22 18:40:56.000000');
INSERT INTO `don_hang` (`id`, `ma_don_hang`, `nguoi_dung_id`, `ma_giam_gia_id`, `ten_nguoi_nhan`, `email`, `so_dien_thoai`, `dia_chi_giao_hang`, `ghi_chu`, `ghi_chu_quan_tri`, `tam_tinh`, `tien_giam_gia`, `phi_van_chuyen`, `tong_tien`, `phuong_thuc_thanh_toan`, `trang_thai_thanh_toan`, `trang_thai`, `created_at`, `updated_at`) VALUES (43,'DH2611224HWBQ5',3,3,'Nguyễn Thị Lan','nguyen.lan@example.com','0912345601','25 Trần Thái Tông, Phường Cầu Giấy, Thành phố Hà Nội',NULL,NULL,199000,20000,30000,209000,'cod','chua_thanh_toan','cho_xac_nhan','2026-11-22 19:07:56.000000','2026-11-22 19:10:56.000000');
/*!40000 ALTER TABLE `don_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (1,'1','nen tang','SQL','V1__nen_tang.sql',550129571,'techgear','2026-11-22 12:37:54',12,1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (2,'2','tai khoan','SQL','V2__tai_khoan.sql',1058525079,'techgear','2026-11-22 12:37:55',163,1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (3,'3','san pham','SQL','V3__san_pham.sql',-109928935,'techgear','2026-11-22 12:37:55',682,1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (4,'4','gio hang','SQL','V4__gio_hang.sql',-574702183,'techgear','2026-11-22 12:37:55',68,1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (5,'5','don hang','SQL','V5__don_hang.sql',1790357194,'techgear','2026-11-22 12:37:56',312,1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (6,'6','thanh toan','SQL','V6__thanh_toan.sql',261168194,'techgear','2026-11-22 12:37:56',40,1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (7,'7','danh gia','SQL','V7__danh_gia.sql',1376756022,'techgear','2026-11-22 12:37:56',62,1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (8,NULL,'du lieu mau','SQL','R__du_lieu_mau.sql',-509508610,'techgear','2026-11-22 12:37:56',7,1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (9,NULL,'du lieu mau khuyen mai','SQL','R__du_lieu_mau_khuyen_mai.sql',1378570275,'techgear','2026-11-22 12:37:56',5,1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (10,NULL,'du lieu mau san pham','SQL','R__du_lieu_mau_san_pham.sql',-1938908534,'techgear','2026-11-22 12:37:56',92,1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (11,NULL,'du lieu mau van hanh','SQL','R__du_lieu_mau_van_hanh.sql',700928835,'techgear','2026-11-22 12:37:56',148,1);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gia_tri_tuy_chon`
--

DROP TABLE IF EXISTS `gia_tri_tuy_chon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gia_tri_tuy_chon` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tuy_chon_id` bigint NOT NULL,
  `gia_tri` varchar(50) NOT NULL,
  `ma_mau` char(7) DEFAULT NULL,
  `thu_tu` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `tuy_chon_id` (`tuy_chon_id`,`gia_tri`),
  CONSTRAINT `gia_tri_tuy_chon_ibfk_1` FOREIGN KEY (`tuy_chon_id`) REFERENCES `tuy_chon` (`id`) ON DELETE CASCADE,
  CONSTRAINT `gia_tri_tuy_chon_chk_1` CHECK (regexp_like(`ma_mau`,_utf8mb4'^#[0-9A-Fa-f]{6}$'))
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gia_tri_tuy_chon`
--

LOCK TABLES `gia_tri_tuy_chon` WRITE;
/*!40000 ALTER TABLE `gia_tri_tuy_chon` DISABLE KEYS */;
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (1,1,'Đen','#1F2937',1);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (2,1,'Trắng','#F9FAFB',2);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (3,2,'Đen','#1F2937',1);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (4,2,'Trắng','#F9FAFB',2);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (5,3,'1m',NULL,1);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (6,3,'2m',NULL,2);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (7,4,'Đen','#1F2937',1);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (8,4,'Xanh','#2563EB',2);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (9,5,'Trắng','#F9FAFB',1);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (10,5,'Bạc','#9CA3AF',2);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (11,6,'Đen','#111827',1);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (12,6,'Bạc','#D1D5DB',2);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (13,7,'Đen','#111827',1);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (14,7,'Xanh','#1D4ED8',2);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (15,7,'Đỏ','#DC2626',3);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (16,8,'Đen','#1F2937',1);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (17,8,'Xám nhạt','#E5E7EB',2);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (18,9,'Tactile',NULL,1);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (19,9,'Linear',NULL,2);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (20,10,'iPhone 16 Pro',NULL,1);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (21,10,'iPhone 16 Pro Max',NULL,2);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (32,16,'1m',NULL,1);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (33,16,'2m',NULL,2);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (34,17,'Trắng','#F9FAFB',1);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (35,17,'Đen','#111827',2);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (36,18,'Đen','#111827',1);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (37,18,'Xanh','#1D4ED8',2);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (38,19,'Xanh','#1E40AF',1);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (39,19,'Đen','#111827',2);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (40,19,'Đỏ','#DC2626',3);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (41,20,'Đen','#1F2937',1);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (42,20,'Đỏ','#DC2626',2);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (43,21,'Red',NULL,1);
INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES (44,21,'Brown',NULL,2);
/*!40000 ALTER TABLE `gia_tri_tuy_chon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gio_hang`
--

DROP TABLE IF EXISTS `gio_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gio_hang` (
  `nguoi_dung_id` bigint NOT NULL,
  `bien_the_id` bigint NOT NULL,
  `so_luong` int NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`nguoi_dung_id`,`bien_the_id`),
  KEY `bien_the_id` (`bien_the_id`),
  CONSTRAINT `gio_hang_ibfk_1` FOREIGN KEY (`nguoi_dung_id`) REFERENCES `nguoi_dung` (`id`) ON DELETE CASCADE,
  CONSTRAINT `gio_hang_ibfk_2` FOREIGN KEY (`bien_the_id`) REFERENCES `bien_the` (`id`) ON DELETE CASCADE,
  CONSTRAINT `gio_hang_chk_1` CHECK ((`so_luong` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gio_hang`
--

LOCK TABLES `gio_hang` WRITE;
/*!40000 ALTER TABLE `gio_hang` DISABLE KEYS */;
INSERT INTO `gio_hang` (`nguoi_dung_id`, `bien_the_id`, `so_luong`, `created_at`, `updated_at`) VALUES (2,32,2,'2026-11-22 19:37:56.831836','2026-11-22 19:37:56.831836');
INSERT INTO `gio_hang` (`nguoi_dung_id`, `bien_the_id`, `so_luong`, `created_at`, `updated_at`) VALUES (2,49,1,'2026-11-22 19:37:56.831836','2026-11-22 19:37:56.831836');
/*!40000 ALTER TABLE `gio_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hinh_anh_san_pham`
--

DROP TABLE IF EXISTS `hinh_anh_san_pham`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hinh_anh_san_pham` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `san_pham_id` bigint NOT NULL,
  `gia_tri_tuy_chon_id` bigint DEFAULT NULL,
  `duong_dan` varchar(255) NOT NULL,
  `mo_ta` varchar(255) DEFAULT NULL,
  `thu_tu` int NOT NULL DEFAULT '0',
  `la_anh_chinh` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_hinh_anh_chinh` ((if(`la_anh_chinh`,`san_pham_id`,NULL))),
  KEY `san_pham_id` (`san_pham_id`),
  KEY `gia_tri_tuy_chon_id` (`gia_tri_tuy_chon_id`),
  CONSTRAINT `hinh_anh_san_pham_ibfk_1` FOREIGN KEY (`san_pham_id`) REFERENCES `san_pham` (`id`) ON DELETE CASCADE,
  CONSTRAINT `hinh_anh_san_pham_ibfk_2` FOREIGN KEY (`gia_tri_tuy_chon_id`) REFERENCES `gia_tri_tuy_chon` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hinh_anh_san_pham`
--

LOCK TABLES `hinh_anh_san_pham` WRITE;
/*!40000 ALTER TABLE `hinh_anh_san_pham` DISABLE KEYS */;
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (1,1,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4a/Silicon_vs_GaN_30W_USB-C_chargers.jpg/960px-Silicon_vs_GaN_30W_USB-C_chargers.jpg','Củ sạc Anker Nano 65W GaN',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (2,2,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6b/Apple_USB-C_%28USB_Type_C%29_87W_Power_Adapter_for_MacBook_Pro_%2848979218053%29.jpg/960px-Apple_USB-C_%28USB_Type_C%29_87W_Power_Adapter_for_MacBook_Pro_%2848979218053%29.jpg','Củ sạc Apple USB-C 20W',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (3,3,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0c/Baseus_USB_chargers-1.jpg/960px-Baseus_USB_chargers-1.jpg','Củ sạc Baseus GaN5 Pro 100W',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (4,3,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/3d/Baseus_USB_chargers-0.jpg/960px-Baseus_USB_chargers-0.jpg','Củ sạc Baseus GaN5 Pro 100W',2,0);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (5,4,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/df/Lightning_connector.jpg/960px-Lightning_connector.jpg','Cáp Lightning Anker PowerLine III',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (6,5,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c3/USB-C_cable_2017_A.jpg/960px-USB-C_cable_2017_A.jpg','Cáp USB-C to USB-C Ugreen 100W',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (7,6,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ee/Anker_power_bank_and_cable.jpg/960px-Anker_power_bank_and_cable.jpg','Sạc dự phòng Anker PowerCore 10000mAh',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (8,6,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4e/Anker_power_bank_lit.jpg/960px-Anker_power_bank_lit.jpg','Sạc dự phòng Anker PowerCore 10000mAh',2,0);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (9,7,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5b/Xiaomi_PowerBank.jpg/960px-Xiaomi_PowerBank.jpg','Sạc dự phòng Xiaomi 20000mAh 22.5W',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (10,8,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2f/AirPods_Pro_%282nd_generation%29.jpg/960px-AirPods_Pro_%282nd_generation%29.jpg','Tai nghe Apple AirPods Pro 2',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (11,8,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4c/AirPods_Pro_case.jpg/960px-AirPods_Pro_case.jpg','Tai nghe Apple AirPods Pro 2',2,0);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (12,8,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/97/AirPods_Pro_2.jpg/960px-AirPods_Pro_2.jpg','Tai nghe Apple AirPods Pro 2',3,0);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (13,9,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a4/Samsung_Galaxy_Buds.jpg/960px-Samsung_Galaxy_Buds.jpg','Tai nghe Samsung Galaxy Buds3',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (14,10,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b2/Sony_Headphones_%2840476165073%29.jpg/960px-Sony_Headphones_%2840476165073%29.jpg','Tai nghe Sony WH-1000XM5',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (15,11,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f9/JBL_Flip_bluetooth_speaker.jpg/960px-JBL_Flip_bluetooth_speaker.jpg','Loa JBL Flip 6',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (16,11,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a2/JBL_Flip_4.jpg/960px-JBL_Flip_4.jpg','Loa JBL Flip 6',2,0);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (17,12,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9a/SONY_SRS-XB2_01.JPG/960px-SONY_SRS-XB2_01.JPG','Loa Sony SRS-XB13',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (18,13,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/69/Logitech_MX_Master_3S_HS13.jpg/960px-Logitech_MX_Master_3S_HS13.jpg','Chuột Logitech MX Master 3S',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (19,13,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/21/Logitech_MX_Master_3S_HS10.jpg/960px-Logitech_MX_Master_3S_HS10.jpg','Chuột Logitech MX Master 3S',2,0);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (20,13,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/90/Logitech_MX_Master_3S_HS01.jpg/960px-Logitech_MX_Master_3S_HS01.jpg','Chuột Logitech MX Master 3S',3,0);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (21,14,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c9/Logitech%28G703%29_mouse.jpg/960px-Logitech%28G703%29_mouse.jpg','Chuột gaming Logitech G304',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (22,15,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7f/Beautiful_Mechanical_Keyboard.jpg/960px-Beautiful_Mechanical_Keyboard.jpg','Bàn phím cơ Logitech MX Mechanical Mini',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (23,16,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f3/IPhone_case_wikipedia.jpg/960px-IPhone_case_wikipedia.jpg','Ốp lưng trong suốt Ugreen cho iPhone 16 Pro',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (24,17,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c4/White_iPhone_4S_with_screen_protector_vs_iPhone_5S.jpg/960px-White_iPhone_4S_with_screen_protector_vs_iPhone_5S.jpg','Kính cường lực Baseus iPhone 16',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (25,32,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f5/Samsung_Charger.jpg/960px-Samsung_Charger.jpg','Củ sạc nhanh Samsung 25W',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (26,33,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/92/Ugreen_car_charger_USB-C_USB-A_30W_25845.jpg/960px-Ugreen_car_charger_USB-C_USB-A_30W_25845.jpg','Tẩu sạc ô tô Ugreen 30W',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (27,34,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5c/Two_USB-C_connectors_being_held.jpg/960px-Two_USB-C_connectors_being_held.jpg','Cáp USB-C to USB-C Baseus 60W',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (28,35,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5f/2026-08-22_UGreen-USB-C-Kabel_HOF1918_RAW-Export.png/960px-2026-08-22_UGreen-USB-C-Kabel_HOF1918_RAW-Export.png','Cáp USB-A to USB-C Ugreen 3A',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (29,36,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e9/SAMSUNG_BATTERY_PACK_%28POWER_BANK%29_EB-P1100C.jpg/960px-SAMSUNG_BATTERY_PACK_%28POWER_BANK%29_EB-P1100C.jpg','Sạc dự phòng Samsung EB-P1100C 10000mAh',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (30,36,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1e/SAMSUNG_BATTERY_PACK_%28POWER_BANK%29_EB-P1100C_%282%29.jpg/960px-SAMSUNG_BATTERY_PACK_%28POWER_BANK%29_EB-P1100C_%282%29.jpg','Sạc dự phòng Samsung EB-P1100C 10000mAh',2,0);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (31,37,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e8/MiPowerBankPro10000mAhQC30Xiaomi20240820000.jpg/960px-MiPowerBankPro10000mAhQC30Xiaomi20240820000.jpg','Sạc dự phòng Xiaomi Mi Power Bank Pro 10000mAh',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (32,37,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/50/MiPowerBankPro10000mAhQC30Xiaomi20240820001.jpg/960px-MiPowerBankPro10000mAhQC30Xiaomi20240820001.jpg','Sạc dự phòng Xiaomi Mi Power Bank Pro 10000mAh',2,0);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (33,38,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9e/Samsung_Galaxy_Buds_FE_and_Samsung_Galaxy_Buds2_Pro.jpg/960px-Samsung_Galaxy_Buds_FE_and_Samsung_Galaxy_Buds2_Pro.jpg','Tai nghe Samsung Galaxy Buds2 Pro',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (34,39,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/01/Sony_WH-CH510_Bluetooth_Over-Ear_Headphone.jpg/960px-Sony_WH-CH510_Bluetooth_Over-Ear_Headphone.jpg','Tai nghe Sony WH-CH510',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (35,40,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/33/JBL_Charge_4_1v2.jpg/960px-JBL_Charge_4_1v2.jpg','Loa JBL Charge 4',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (36,40,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ef/JBL_Charge_4_2v2.jpg/960px-JBL_Charge_4_2v2.jpg','Loa JBL Charge 4',2,0);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (37,41,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9c/JBL_GO2_Bluetooth_speaker.jpeg/960px-JBL_GO2_Bluetooth_speaker.jpeg','Loa JBL Go 2',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (38,41,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/32/JBL_GO2_Bluetooth_speaker_00_10_27_681000_%28turned_upright%29.jpeg/960px-JBL_GO2_Bluetooth_speaker_00_10_27_681000_%28turned_upright%29.jpeg','Loa JBL Go 2',2,0);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (39,42,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/39/Logitech_M331_Silent_Mouse.jpg/960px-Logitech_M331_Silent_Mouse.jpg','Chuột không dây Logitech M331 Silent Plus',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (40,43,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8a/2023_Mysz_komputerowa_Logitech_M705.jpg/960px-2023_Mysz_komputerowa_Logitech_M705.jpg','Chuột không dây Logitech M705 Marathon',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (41,44,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fb/Keychron_K8_Non-Backlight_Wireless_Mechanical_Keyboard.jpg/960px-Keychron_K8_Non-Backlight_Wireless_Mechanical_Keyboard.jpg','Bàn phím cơ Keychron K8',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (42,44,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8d/Keychron_K8_Non-Backlight_Wireless_Mechanical_Keyboard_-_52771896827.jpg/960px-Keychron_K8_Non-Backlight_Wireless_Mechanical_Keyboard_-_52771896827.jpg','Bàn phím cơ Keychron K8',2,0);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (43,45,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e6/Logitech_K380_CJK_Keyboard.jpg/960px-Logitech_K380_CJK_Keyboard.jpg','Bàn phím Bluetooth Logitech K380',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (44,46,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/07/Apple_iPhone_17_Silicone_Case_%28Black%29.jpg/960px-Apple_iPhone_17_Silicone_Case_%28Black%29.jpg','Ốp lưng Silicone MagSafe Apple cho iPhone 17',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (45,46,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b7/Apple_iPhone_17_Silicone_Case_%28Black%29_rear.jpg/960px-Apple_iPhone_17_Silicone_Case_%28Black%29_rear.jpg','Ốp lưng Silicone MagSafe Apple cho iPhone 17',2,0);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (46,47,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/78/SAMSUNG_Galaxy_S22_Ultra_TPU_CASE.jpg/960px-SAMSUNG_Galaxy_S22_Ultra_TPU_CASE.jpg','Ốp lưng TPU trong suốt Samsung Galaxy S22 Ultra',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (47,47,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4e/SAMSUNG_Galaxy_S22_Ultra_TPU_CASE_%282%29.jpg/960px-SAMSUNG_Galaxy_S22_Ultra_TPU_CASE_%282%29.jpg','Ốp lưng TPU trong suốt Samsung Galaxy S22 Ultra',2,0);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (48,48,NULL,'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6a/Samsung_Galaxy_A04e_front_with_screen_protector.jpg/960px-Samsung_Galaxy_A04e_front_with_screen_protector.jpg','Kính cường lực Samsung Galaxy A04e',1,1);
INSERT INTO `hinh_anh_san_pham` (`id`, `san_pham_id`, `gia_tri_tuy_chon_id`, `duong_dan`, `mo_ta`, `thu_tu`, `la_anh_chinh`) VALUES (49,49,NULL,'https://upload.wikimedia.org/wikipedia/commons/f/f0/%D9%85%D8%AD%D8%A7%D9%81%D8%B8_%D8%B5%D9%81%D8%AD%D9%87_%D9%86%D9%85%D8%A7%DB%8C%D8%B4_%D8%AD%D8%B1%DB%8C%D9%85_%D8%B4%D8%AE%D8%B5%DB%8C.jpg','Kính cường lực chống nhìn trộm Ugreen iPhone 16',1,1);
/*!40000 ALTER TABLE `hinh_anh_san_pham` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lich_su_don_hang`
--

DROP TABLE IF EXISTS `lich_su_don_hang`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lich_su_don_hang` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `don_hang_id` bigint NOT NULL,
  `trang_thai` varchar(20) NOT NULL,
  `ghi_chu` varchar(500) DEFAULT NULL,
  `nguoi_thuc_hien_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `don_hang_id` (`don_hang_id`),
  KEY `nguoi_thuc_hien_id` (`nguoi_thuc_hien_id`),
  CONSTRAINT `lich_su_don_hang_ibfk_1` FOREIGN KEY (`don_hang_id`) REFERENCES `don_hang` (`id`) ON DELETE CASCADE,
  CONSTRAINT `lich_su_don_hang_ibfk_2` FOREIGN KEY (`nguoi_thuc_hien_id`) REFERENCES `nguoi_dung` (`id`) ON DELETE SET NULL,
  CONSTRAINT `lich_su_don_hang_chk_1` CHECK ((`trang_thai` in (_utf8mb4'cho_xac_nhan',_utf8mb4'da_xac_nhan',_utf8mb4'dang_giao',_utf8mb4'hoan_thanh',_utf8mb4'da_huy')))
) ENGINE=InnoDB AUTO_INCREMENT=149 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lich_su_don_hang`
--

LOCK TABLES `lich_su_don_hang` WRITE;
/*!40000 ALTER TABLE `lich_su_don_hang` DISABLE KEYS */;
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (1,1,'cho_xac_nhan','Đặt hàng',3,'2026-08-23 16:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (2,1,'da_xac_nhan',NULL,1,'2026-08-23 18:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (3,1,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-08-24 16:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (4,1,'hoan_thanh','Giao hàng thành công',1,'2026-08-26 16:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (5,2,'cho_xac_nhan','Đặt hàng',4,'2026-08-25 14:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (6,2,'da_xac_nhan',NULL,1,'2026-08-25 16:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (7,2,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-08-26 14:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (8,2,'hoan_thanh','Giao hàng thành công',1,'2026-08-28 14:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (9,3,'cho_xac_nhan','Đặt hàng',5,'2026-08-27 17:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (10,3,'da_xac_nhan',NULL,1,'2026-08-27 19:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (11,3,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-08-28 17:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (12,3,'hoan_thanh','Giao hàng thành công',1,'2026-08-30 17:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (13,4,'cho_xac_nhan','Đặt hàng',6,'2026-08-28 11:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (14,4,'da_xac_nhan',NULL,1,'2026-08-28 13:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (15,4,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-08-29 11:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (16,4,'hoan_thanh','Giao hàng thành công',1,'2026-08-31 11:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (17,5,'cho_xac_nhan','Đặt hàng',NULL,'2026-08-30 15:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (18,5,'da_xac_nhan',NULL,1,'2026-08-30 17:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (19,5,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-08-31 15:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (20,5,'hoan_thanh','Giao hàng thành công',1,'2026-09-02 15:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (21,6,'cho_xac_nhan','Đặt hàng',7,'2026-09-05 18:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (22,6,'da_huy','Đổi ý, muốn chọn màu khác',7,'2026-09-05 20:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (23,7,'cho_xac_nhan','Đặt hàng',8,'2026-09-07 12:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (24,7,'da_xac_nhan',NULL,1,'2026-09-07 14:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (25,7,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-09-08 12:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (26,7,'hoan_thanh','Giao hàng thành công',1,'2026-09-10 12:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (27,8,'cho_xac_nhan','Đặt hàng',9,'2026-09-09 16:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (28,8,'da_xac_nhan',NULL,1,'2026-09-09 18:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (29,8,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-09-10 16:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (30,8,'hoan_thanh','Giao hàng thành công',1,'2026-09-12 16:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (31,9,'cho_xac_nhan','Đặt hàng',10,'2026-09-11 14:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (32,9,'da_xac_nhan',NULL,1,'2026-09-11 16:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (33,9,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-09-12 14:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (34,9,'hoan_thanh','Giao hàng thành công',1,'2026-09-14 14:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (35,10,'cho_xac_nhan','Đặt hàng',3,'2026-09-14 17:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (36,10,'da_xac_nhan',NULL,1,'2026-09-14 19:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (37,10,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-09-15 17:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (38,10,'hoan_thanh','Giao hàng thành công',1,'2026-09-17 17:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (39,11,'cho_xac_nhan','Đặt hàng',11,'2026-09-19 11:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (40,11,'da_huy','Quá 30 phút chưa thanh toán VNPay, hệ thống tự hủy',NULL,'2026-09-19 11:52:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (41,12,'cho_xac_nhan','Đặt hàng',4,'2026-09-20 14:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (42,12,'da_xac_nhan',NULL,1,'2026-09-20 16:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (43,12,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-09-21 14:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (44,12,'hoan_thanh','Giao hàng thành công',1,'2026-09-23 14:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (45,13,'cho_xac_nhan','Đặt hàng',2,'2026-09-22 16:27:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (46,13,'da_xac_nhan',NULL,1,'2026-09-22 18:27:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (47,13,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-09-23 16:27:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (48,13,'hoan_thanh','Giao hàng thành công',1,'2026-09-25 16:27:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (49,14,'cho_xac_nhan','Đặt hàng',5,'2026-09-24 13:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (50,14,'da_xac_nhan',NULL,1,'2026-09-24 15:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (51,14,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-09-25 13:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (52,14,'hoan_thanh','Giao hàng thành công',1,'2026-09-27 13:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (53,15,'cho_xac_nhan','Đặt hàng',12,'2026-09-26 17:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (54,15,'da_xac_nhan',NULL,1,'2026-09-26 19:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (55,15,'da_huy','Gọi 3 lần không liên lạc được khách',1,'2026-09-26 22:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (56,16,'cho_xac_nhan','Đặt hàng',6,'2026-09-29 15:47:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (57,16,'da_xac_nhan',NULL,1,'2026-09-29 17:47:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (58,16,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-09-30 15:47:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (59,16,'hoan_thanh','Giao hàng thành công',1,'2026-10-02 15:47:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (60,17,'cho_xac_nhan','Đặt hàng',7,'2026-10-02 12:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (61,17,'da_xac_nhan',NULL,1,'2026-10-02 14:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (62,17,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-10-03 12:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (63,17,'hoan_thanh','Giao hàng thành công',1,'2026-10-05 12:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (64,18,'cho_xac_nhan','Đặt hàng',NULL,'2026-10-04 17:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (65,18,'da_xac_nhan',NULL,1,'2026-10-04 19:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (66,18,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-10-05 17:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (67,18,'hoan_thanh','Giao hàng thành công',1,'2026-10-07 17:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (68,19,'cho_xac_nhan','Đặt hàng',9,'2026-10-09 14:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (69,19,'da_xac_nhan',NULL,1,'2026-10-09 16:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (70,19,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-10-10 14:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (71,19,'hoan_thanh','Giao hàng thành công',1,'2026-10-12 14:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (72,20,'cho_xac_nhan','Đặt hàng',10,'2026-10-11 15:27:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (73,20,'da_xac_nhan',NULL,1,'2026-10-11 17:27:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (74,20,'da_huy','Khách đổi ý sau khi thanh toán, đã hoàn tiền qua VNPay',1,'2026-10-11 21:27:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (75,21,'cho_xac_nhan','Đặt hàng',3,'2026-10-14 16:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (76,21,'da_xac_nhan',NULL,1,'2026-10-14 18:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (77,21,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-10-15 16:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (78,21,'hoan_thanh','Giao hàng thành công',1,'2026-10-17 16:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (79,22,'cho_xac_nhan','Đặt hàng',8,'2026-10-17 12:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (80,22,'da_xac_nhan',NULL,1,'2026-10-17 14:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (81,22,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-10-18 12:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (82,22,'hoan_thanh','Giao hàng thành công',1,'2026-10-20 12:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (83,23,'cho_xac_nhan','Đặt hàng',11,'2026-10-20 16:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (84,23,'da_xac_nhan',NULL,1,'2026-10-20 18:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (85,23,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-10-21 16:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (86,23,'hoan_thanh','Giao hàng thành công',1,'2026-10-23 16:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (87,24,'cho_xac_nhan','Đặt hàng',4,'2026-10-24 13:47:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (88,24,'da_xac_nhan',NULL,1,'2026-10-24 15:47:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (89,24,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-10-25 13:47:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (90,24,'hoan_thanh','Giao hàng thành công',1,'2026-10-27 13:47:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (91,25,'cho_xac_nhan','Đặt hàng',2,'2026-10-26 15:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (92,25,'da_xac_nhan',NULL,1,'2026-10-26 17:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (93,25,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-10-27 15:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (94,25,'hoan_thanh','Giao hàng thành công',1,'2026-10-29 15:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (95,26,'cho_xac_nhan','Đặt hàng',5,'2026-10-29 14:27:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (96,26,'da_xac_nhan',NULL,1,'2026-10-29 16:27:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (97,26,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-10-30 14:27:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (98,26,'hoan_thanh','Giao hàng thành công',1,'2026-11-01 14:27:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (99,27,'cho_xac_nhan','Đặt hàng',NULL,'2026-11-01 17:27:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (100,27,'da_xac_nhan',NULL,1,'2026-11-01 19:27:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (101,27,'da_huy','Khách gọi điện xin hủy',1,'2026-11-01 22:27:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (102,28,'cho_xac_nhan','Đặt hàng',6,'2026-11-03 15:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (103,28,'da_xac_nhan',NULL,1,'2026-11-03 17:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (104,28,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-11-04 15:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (105,28,'hoan_thanh','Giao hàng thành công',1,'2026-11-06 15:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (106,29,'cho_xac_nhan','Đặt hàng',7,'2026-11-05 13:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (107,29,'da_xac_nhan',NULL,1,'2026-11-05 15:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (108,29,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-11-06 13:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (109,29,'hoan_thanh','Giao hàng thành công',1,'2026-11-08 13:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (110,30,'cho_xac_nhan','Đặt hàng',9,'2026-11-07 17:47:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (111,30,'da_xac_nhan',NULL,1,'2026-11-07 19:47:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (112,30,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-11-08 17:47:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (113,30,'hoan_thanh','Giao hàng thành công',1,'2026-11-10 17:47:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (114,31,'cho_xac_nhan','Đặt hàng',10,'2026-11-09 13:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (115,31,'da_xac_nhan',NULL,1,'2026-11-09 15:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (116,31,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-11-10 13:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (117,31,'hoan_thanh','Giao hàng thành công',1,'2026-11-12 13:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (118,32,'cho_xac_nhan','Đặt hàng',3,'2026-11-11 16:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (119,32,'da_xac_nhan',NULL,1,'2026-11-11 18:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (120,32,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-11-12 16:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (121,32,'hoan_thanh','Giao hàng thành công',1,'2026-11-14 16:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (122,33,'cho_xac_nhan','Đặt hàng',8,'2026-11-13 12:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (123,33,'da_xac_nhan',NULL,1,'2026-11-13 14:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (124,33,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-11-14 12:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (125,33,'hoan_thanh','Giao hàng thành công',1,'2026-11-16 12:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (126,34,'cho_xac_nhan','Đặt hàng',11,'2026-11-15 16:47:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (127,34,'da_xac_nhan',NULL,1,'2026-11-15 18:47:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (128,34,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-11-16 16:47:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (129,34,'hoan_thanh','Giao hàng thành công',1,'2026-11-18 16:47:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (130,35,'cho_xac_nhan','Đặt hàng',4,'2026-11-16 14:47:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (131,35,'da_huy','Khách hủy đơn',4,'2026-11-16 16:47:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (132,36,'cho_xac_nhan','Đặt hàng',2,'2026-11-17 13:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (133,36,'da_xac_nhan',NULL,1,'2026-11-17 15:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (134,36,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-11-18 13:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (135,36,'hoan_thanh','Giao hàng thành công',1,'2026-11-20 13:17:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (136,37,'cho_xac_nhan','Đặt hàng',5,'2026-11-19 15:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (137,37,'da_xac_nhan',NULL,1,'2026-11-19 17:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (138,37,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-11-20 15:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (139,38,'cho_xac_nhan','Đặt hàng',6,'2026-11-20 14:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (140,38,'da_xac_nhan',NULL,1,'2026-11-20 16:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (141,38,'dang_giao','Bàn giao cho đơn vị vận chuyển',1,'2026-11-21 14:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (142,39,'cho_xac_nhan','Đặt hàng',7,'2026-11-21 12:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (143,39,'da_xac_nhan',NULL,1,'2026-11-21 14:57:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (144,40,'cho_xac_nhan','Đặt hàng',NULL,'2026-11-21 17:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (145,40,'da_xac_nhan',NULL,1,'2026-11-21 19:07:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (146,41,'cho_xac_nhan','Đặt hàng',9,'2026-11-22 16:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (147,42,'cho_xac_nhan','Đặt hàng',10,'2026-11-22 18:37:56.000000');
INSERT INTO `lich_su_don_hang` (`id`, `don_hang_id`, `trang_thai`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (148,43,'cho_xac_nhan','Đặt hàng',3,'2026-11-22 19:07:56.000000');
/*!40000 ALTER TABLE `lich_su_don_hang` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lich_su_ton_kho`
--

DROP TABLE IF EXISTS `lich_su_ton_kho`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lich_su_ton_kho` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `bien_the_id` bigint NOT NULL,
  `don_hang_id` bigint DEFAULT NULL,
  `loai` varchar(20) NOT NULL,
  `so_luong_thay_doi` int NOT NULL,
  `so_luong_sau` int NOT NULL,
  `ghi_chu` varchar(255) DEFAULT NULL,
  `nguoi_thuc_hien_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `don_hang_id` (`don_hang_id`),
  KEY `nguoi_thuc_hien_id` (`nguoi_thuc_hien_id`),
  KEY `ix_lich_su_ton_kho_bien_the` (`bien_the_id`,`created_at`),
  CONSTRAINT `lich_su_ton_kho_ibfk_1` FOREIGN KEY (`bien_the_id`) REFERENCES `bien_the` (`id`),
  CONSTRAINT `lich_su_ton_kho_ibfk_2` FOREIGN KEY (`don_hang_id`) REFERENCES `don_hang` (`id`) ON DELETE SET NULL,
  CONSTRAINT `lich_su_ton_kho_ibfk_3` FOREIGN KEY (`nguoi_thuc_hien_id`) REFERENCES `nguoi_dung` (`id`) ON DELETE SET NULL,
  CONSTRAINT `lich_su_ton_kho_chk_1` CHECK ((`loai` in (_utf8mb4'khoi_tao',_utf8mb4'nhap_kho',_utf8mb4'ban_hang',_utf8mb4'huy_don',_utf8mb4'dieu_chinh'))),
  CONSTRAINT `lich_su_ton_kho_chk_2` CHECK ((`so_luong_thay_doi` <> 0)),
  CONSTRAINT `lich_su_ton_kho_chk_3` CHECK ((`so_luong_sau` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=145 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lich_su_ton_kho`
--

LOCK TABLES `lich_su_ton_kho` WRITE;
/*!40000 ALTER TABLE `lich_su_ton_kho` DISABLE KEYS */;
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (1,1,NULL,'khoi_tao',25,25,'Tồn kho ban đầu',1,'2026-10-23 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (2,2,NULL,'khoi_tao',18,18,'Tồn kho ban đầu',1,'2026-10-23 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (3,3,NULL,'khoi_tao',40,40,'Tồn kho ban đầu',1,'2026-10-25 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (4,4,NULL,'khoi_tao',12,12,'Tồn kho ban đầu',1,'2026-11-02 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (5,6,NULL,'khoi_tao',60,60,'Tồn kho ban đầu',1,'2026-10-28 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (6,7,NULL,'khoi_tao',45,45,'Tồn kho ban đầu',1,'2026-10-28 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (7,8,NULL,'khoi_tao',30,30,'Tồn kho ban đầu',1,'2026-10-13 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (8,9,NULL,'khoi_tao',20,20,'Tồn kho ban đầu',1,'2026-11-04 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (9,10,NULL,'khoi_tao',7,7,'Tồn kho ban đầu',1,'2026-11-04 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (10,12,NULL,'khoi_tao',6,6,'Tồn kho ban đầu',1,'2026-11-12 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (11,13,NULL,'khoi_tao',4,4,'Tồn kho ban đầu',1,'2026-11-12 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (12,14,NULL,'khoi_tao',15,15,'Tồn kho ban đầu',1,'2026-11-10 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (13,15,NULL,'khoi_tao',10,10,'Tồn kho ban đầu',1,'2026-11-14 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (14,16,NULL,'khoi_tao',10,10,'Tồn kho ban đầu',1,'2026-11-14 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (15,17,NULL,'khoi_tao',8,8,'Tồn kho ban đầu',1,'2026-11-07 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (16,18,NULL,'khoi_tao',5,5,'Tồn kho ban đầu',1,'2026-11-07 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (17,19,NULL,'khoi_tao',3,3,'Tồn kho ban đầu',1,'2026-11-07 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (18,20,NULL,'khoi_tao',12,12,'Tồn kho ban đầu',1,'2026-10-08 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (19,21,NULL,'khoi_tao',9,9,'Tồn kho ban đầu',1,'2026-11-08 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (20,22,NULL,'khoi_tao',6,6,'Tồn kho ban đầu',1,'2026-11-08 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (21,23,NULL,'khoi_tao',30,30,'Tồn kho ban đầu',1,'2026-10-03 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (22,24,NULL,'khoi_tao',5,5,'Tồn kho ban đầu',1,'2026-11-16 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (23,25,NULL,'khoi_tao',5,5,'Tồn kho ban đầu',1,'2026-11-16 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (24,26,NULL,'khoi_tao',50,50,'Tồn kho ban đầu',1,'2026-11-19 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (25,27,NULL,'khoi_tao',50,50,'Tồn kho ban đầu',1,'2026-11-19 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (26,28,NULL,'khoi_tao',80,80,'Tồn kho ban đầu',1,'2026-11-20 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (27,32,NULL,'khoi_tao',40,40,'Tồn kho ban đầu',1,'2026-08-29 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (28,33,NULL,'khoi_tao',25,25,'Tồn kho ban đầu',1,'2026-11-18 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (29,34,NULL,'khoi_tao',60,60,'Tồn kho ban đầu',1,'2026-09-13 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (30,35,NULL,'khoi_tao',40,40,'Tồn kho ban đầu',1,'2026-09-13 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (31,36,NULL,'khoi_tao',80,80,'Tồn kho ban đầu',1,'2026-08-19 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (32,37,NULL,'khoi_tao',20,20,'Tồn kho ban đầu',1,'2026-08-24 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (33,38,NULL,'khoi_tao',25,25,'Tồn kho ban đầu',1,'2026-09-18 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (34,39,NULL,'khoi_tao',10,10,'Tồn kho ban đầu',1,'2026-08-14 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (35,40,NULL,'khoi_tao',10,10,'Tồn kho ban đầu',1,'2026-09-08 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (36,41,NULL,'khoi_tao',6,6,'Tồn kho ban đầu',1,'2026-09-08 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (37,42,NULL,'khoi_tao',15,15,'Tồn kho ban đầu',1,'2026-09-03 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (38,43,NULL,'khoi_tao',8,8,'Tồn kho ban đầu',1,'2026-09-03 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (39,44,NULL,'khoi_tao',6,6,'Tồn kho ban đầu',1,'2026-09-23 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (40,45,NULL,'khoi_tao',15,15,'Tồn kho ban đầu',1,'2026-09-23 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (41,46,NULL,'khoi_tao',2,2,'Tồn kho ban đầu',1,'2026-09-23 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (42,47,NULL,'khoi_tao',50,50,'Tồn kho ban đầu',1,'2026-08-26 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (43,48,NULL,'khoi_tao',20,20,'Tồn kho ban đầu',1,'2026-08-26 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (44,49,NULL,'khoi_tao',15,15,'Tồn kho ban đầu',1,'2026-11-17 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (45,50,NULL,'khoi_tao',30,30,'Tồn kho ban đầu',1,'2026-08-22 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (46,51,NULL,'khoi_tao',12,12,'Tồn kho ban đầu',1,'2026-11-21 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (47,52,NULL,'khoi_tao',10,10,'Tồn kho ban đầu',1,'2026-11-21 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (48,53,NULL,'khoi_tao',20,20,'Tồn kho ban đầu',1,'2026-11-15 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (49,54,NULL,'khoi_tao',30,30,'Tồn kho ban đầu',1,'2026-08-16 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (50,55,NULL,'khoi_tao',45,45,'Tồn kho ban đầu',1,'2026-09-28 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (51,56,NULL,'khoi_tao',60,60,'Tồn kho ban đầu',1,'2026-09-21 19:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (64,50,1,'ban_hang',-1,29,NULL,3,'2026-08-23 16:17:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (65,36,1,'ban_hang',-2,78,NULL,3,'2026-08-23 16:17:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (67,37,2,'ban_hang',-1,19,NULL,4,'2026-08-25 14:17:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (68,36,2,'ban_hang',-1,77,NULL,4,'2026-08-25 14:17:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (70,48,3,'ban_hang',-1,19,NULL,5,'2026-08-27 17:07:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (71,39,4,'ban_hang',-1,9,NULL,6,'2026-08-28 11:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (72,47,4,'ban_hang',-1,49,NULL,6,'2026-08-28 11:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (74,32,5,'ban_hang',-2,38,NULL,NULL,'2026-08-30 15:17:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (75,43,6,'ban_hang',-1,7,NULL,7,'2026-09-05 18:07:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (76,43,6,'huy_don',1,8,NULL,7,'2026-09-05 20:07:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (77,54,7,'ban_hang',-1,29,NULL,8,'2026-09-07 12:57:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (78,36,7,'ban_hang',-1,76,NULL,8,'2026-09-07 12:57:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (80,40,8,'ban_hang',-1,9,NULL,9,'2026-09-09 16:07:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (81,42,9,'ban_hang',-1,14,NULL,10,'2026-09-11 14:07:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (82,35,10,'ban_hang',-2,38,NULL,3,'2026-09-14 17:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (83,32,10,'ban_hang',-1,37,NULL,3,'2026-09-14 17:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (85,38,11,'ban_hang',-1,24,NULL,11,'2026-09-19 11:17:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (86,38,11,'huy_don',1,25,NULL,NULL,'2026-09-19 11:52:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (87,38,12,'ban_hang',-1,24,NULL,4,'2026-09-20 14:57:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (88,34,12,'ban_hang',-1,59,NULL,4,'2026-09-20 14:57:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (90,56,13,'ban_hang',-1,59,NULL,2,'2026-09-22 16:27:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (91,41,13,'ban_hang',-1,5,NULL,2,'2026-09-22 16:27:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (93,44,14,'ban_hang',-1,5,NULL,5,'2026-09-24 13:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (94,45,15,'ban_hang',-3,12,NULL,12,'2026-09-26 17:57:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (95,45,15,'huy_don',3,15,NULL,1,'2026-09-26 22:57:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (96,55,16,'ban_hang',-2,43,NULL,6,'2026-09-29 15:47:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (97,47,16,'ban_hang',-1,48,NULL,6,'2026-09-29 15:47:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (99,45,17,'ban_hang',-1,14,NULL,7,'2026-10-02 12:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (100,23,18,'ban_hang',-1,29,NULL,NULL,'2026-10-04 17:17:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (101,20,19,'ban_hang',-1,11,NULL,9,'2026-10-09 14:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (102,36,19,'ban_hang',-1,75,NULL,9,'2026-10-09 14:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (104,50,20,'ban_hang',-1,28,NULL,10,'2026-10-11 15:27:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (105,50,20,'huy_don',1,29,NULL,1,'2026-10-11 21:27:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (106,8,21,'ban_hang',-1,29,NULL,3,'2026-10-14 16:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (107,32,21,'ban_hang',-1,36,NULL,3,'2026-10-14 16:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (109,23,22,'ban_hang',-1,28,NULL,8,'2026-10-17 12:17:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (110,48,22,'ban_hang',-1,18,NULL,8,'2026-10-17 12:17:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (112,42,23,'ban_hang',-1,13,NULL,11,'2026-10-20 16:57:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (113,1,24,'ban_hang',-1,24,NULL,4,'2026-10-24 13:47:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (114,36,24,'ban_hang',-1,74,NULL,4,'2026-10-24 13:47:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (116,3,25,'ban_hang',-1,39,NULL,2,'2026-10-26 15:57:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (117,6,26,'ban_hang',-2,58,NULL,5,'2026-10-29 14:27:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (118,37,27,'ban_hang',-1,18,NULL,NULL,'2026-11-01 17:27:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (119,37,27,'huy_don',1,19,NULL,1,'2026-11-01 22:27:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (120,4,28,'ban_hang',-1,11,NULL,6,'2026-11-03 15:07:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (121,10,29,'ban_hang',-1,6,NULL,7,'2026-11-05 13:07:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (122,2,29,'ban_hang',-1,17,NULL,7,'2026-11-05 13:07:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (124,9,30,'ban_hang',-1,19,NULL,9,'2026-11-07 17:47:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (125,21,31,'ban_hang',-1,8,NULL,10,'2026-11-09 13:57:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (126,14,32,'ban_hang',-1,14,NULL,3,'2026-11-11 16:17:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (127,12,33,'ban_hang',-1,5,NULL,8,'2026-11-13 12:07:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (128,15,34,'ban_hang',-1,9,NULL,11,'2026-11-15 16:47:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (129,53,35,'ban_hang',-1,19,NULL,4,'2026-11-16 14:47:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (130,53,35,'huy_don',1,20,NULL,4,'2026-11-16 16:47:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (131,24,36,'ban_hang',-1,4,NULL,2,'2026-11-17 13:17:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (132,49,37,'ban_hang',-1,14,NULL,5,'2026-11-19 15:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (133,33,37,'ban_hang',-1,24,NULL,5,'2026-11-19 15:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (135,26,38,'ban_hang',-1,49,NULL,6,'2026-11-20 14:07:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (136,53,38,'ban_hang',-1,19,NULL,6,'2026-11-20 14:07:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (138,13,39,'ban_hang',-1,3,NULL,7,'2026-11-21 12:57:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (139,22,40,'ban_hang',-1,5,NULL,NULL,'2026-11-21 17:07:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (140,51,41,'ban_hang',-1,11,NULL,9,'2026-11-22 16:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (141,52,42,'ban_hang',-1,9,NULL,10,'2026-11-22 18:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (142,47,42,'ban_hang',-1,47,NULL,10,'2026-11-22 18:37:56.000000');
INSERT INTO `lich_su_ton_kho` (`id`, `bien_the_id`, `don_hang_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`, `created_at`) VALUES (144,7,43,'ban_hang',-1,44,NULL,3,'2026-11-22 19:07:56.000000');
/*!40000 ALTER TABLE `lich_su_ton_kho` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ma_dat_lai_mat_khau`
--

DROP TABLE IF EXISTS `ma_dat_lai_mat_khau`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ma_dat_lai_mat_khau` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nguoi_dung_id` bigint NOT NULL,
  `ma_bam` char(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  `het_han_luc` datetime(6) NOT NULL,
  `da_su_dung` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `ma_bam` (`ma_bam`),
  KEY `nguoi_dung_id` (`nguoi_dung_id`),
  CONSTRAINT `ma_dat_lai_mat_khau_ibfk_1` FOREIGN KEY (`nguoi_dung_id`) REFERENCES `nguoi_dung` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ma_dat_lai_mat_khau`
--

LOCK TABLES `ma_dat_lai_mat_khau` WRITE;
/*!40000 ALTER TABLE `ma_dat_lai_mat_khau` DISABLE KEYS */;
/*!40000 ALTER TABLE `ma_dat_lai_mat_khau` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ma_giam_gia`
--

DROP TABLE IF EXISTS `ma_giam_gia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ma_giam_gia` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ma` varchar(50) NOT NULL,
  `mo_ta` varchar(255) DEFAULT NULL,
  `loai_giam` varchar(20) NOT NULL,
  `gia_tri_giam` bigint NOT NULL,
  `don_toi_thieu` bigint NOT NULL DEFAULT '0',
  `giam_toi_da` bigint DEFAULT NULL,
  `tong_luot_dung` int DEFAULT NULL,
  `luot_dung_moi_nguoi` int DEFAULT NULL,
  `da_dung` int NOT NULL DEFAULT '0',
  `bat_dau_luc` datetime(6) NOT NULL,
  `ket_thuc_luc` datetime(6) NOT NULL,
  `kich_hoat` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `deleted_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ma_giam_gia_ma` ((if((`deleted_at` is null),`ma`,NULL))),
  CONSTRAINT `ma_giam_gia_chk_1` CHECK ((`loai_giam` in (_utf8mb4'phan_tram',_utf8mb4'so_tien'))),
  CONSTRAINT `ma_giam_gia_chk_10` CHECK ((`ket_thuc_luc` > `bat_dau_luc`)),
  CONSTRAINT `ma_giam_gia_chk_2` CHECK ((`gia_tri_giam` > 0)),
  CONSTRAINT `ma_giam_gia_chk_3` CHECK ((`don_toi_thieu` >= 0)),
  CONSTRAINT `ma_giam_gia_chk_4` CHECK ((`giam_toi_da` > 0)),
  CONSTRAINT `ma_giam_gia_chk_5` CHECK ((`tong_luot_dung` > 0)),
  CONSTRAINT `ma_giam_gia_chk_6` CHECK ((`luot_dung_moi_nguoi` > 0)),
  CONSTRAINT `ma_giam_gia_chk_7` CHECK ((`da_dung` >= 0)),
  CONSTRAINT `ma_giam_gia_chk_8` CHECK (((`loai_giam` <> _utf8mb4'phan_tram') or (`gia_tri_giam` <= 100))),
  CONSTRAINT `ma_giam_gia_chk_9` CHECK (((`tong_luot_dung` is null) or (`da_dung` <= `tong_luot_dung`)))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ma_giam_gia`
--

LOCK TABLES `ma_giam_gia` WRITE;
/*!40000 ALTER TABLE `ma_giam_gia` DISABLE KEYS */;
INSERT INTO `ma_giam_gia` (`id`, `ma`, `mo_ta`, `loai_giam`, `gia_tri_giam`, `don_toi_thieu`, `giam_toi_da`, `tong_luot_dung`, `luot_dung_moi_nguoi`, `da_dung`, `bat_dau_luc`, `ket_thuc_luc`, `kich_hoat`, `created_at`, `updated_at`, `deleted_at`) VALUES (1,'GIAM10','Giảm 10% tối đa 100.000đ cho đơn từ 200.000đ','phan_tram',10,200000,100000,NULL,NULL,5,'2025-11-22 19:37:56.000000','2031-11-22 19:37:56.000000',1,'2026-11-22 19:37:56.518485','2026-11-22 19:37:56.518485',NULL);
INSERT INTO `ma_giam_gia` (`id`, `ma`, `mo_ta`, `loai_giam`, `gia_tri_giam`, `don_toi_thieu`, `giam_toi_da`, `tong_luot_dung`, `luot_dung_moi_nguoi`, `da_dung`, `bat_dau_luc`, `ket_thuc_luc`, `kich_hoat`, `created_at`, `updated_at`, `deleted_at`) VALUES (2,'GIAM50K','Giảm 50.000đ cho đơn từ 500.000đ','so_tien',50000,500000,NULL,NULL,NULL,2,'2025-11-22 19:37:56.000000','2031-11-22 19:37:56.000000',1,'2026-11-22 19:37:56.518485','2026-11-22 19:37:56.518485',NULL);
INSERT INTO `ma_giam_gia` (`id`, `ma`, `mo_ta`, `loai_giam`, `gia_tri_giam`, `don_toi_thieu`, `giam_toi_da`, `tong_luot_dung`, `luot_dung_moi_nguoi`, `da_dung`, `bat_dau_luc`, `ket_thuc_luc`, `kich_hoat`, `created_at`, `updated_at`, `deleted_at`) VALUES (3,'MOILAN1','Giảm 20.000đ, mỗi khách dùng 1 lần','so_tien',20000,0,NULL,NULL,1,4,'2025-11-22 19:37:56.000000','2031-11-22 19:37:56.000000',1,'2026-11-22 19:37:56.518485','2026-11-22 19:37:56.518485',NULL);
INSERT INTO `ma_giam_gia` (`id`, `ma`, `mo_ta`, `loai_giam`, `gia_tri_giam`, `don_toi_thieu`, `giam_toi_da`, `tong_luot_dung`, `luot_dung_moi_nguoi`, `da_dung`, `bat_dau_luc`, `ket_thuc_luc`, `kich_hoat`, `created_at`, `updated_at`, `deleted_at`) VALUES (4,'HETHAN','Mã đã hết hạn (để thử)','so_tien',10000,0,NULL,NULL,NULL,0,'2024-11-22 19:37:56.000000','2025-11-22 19:37:56.000000',1,'2026-11-22 19:37:56.518485','2026-11-22 19:37:56.518485',NULL);
/*!40000 ALTER TABLE `ma_giam_gia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nguoi_dung`
--

DROP TABLE IF EXISTS `nguoi_dung`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nguoi_dung` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ho_ten` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `mat_khau` varchar(100) NOT NULL,
  `so_dien_thoai` varchar(20) DEFAULT NULL,
  `gioi_tinh` varchar(10) DEFAULT NULL,
  `ngay_sinh` date DEFAULT NULL,
  `anh_dai_dien` varchar(255) DEFAULT NULL,
  `vai_tro` varchar(20) NOT NULL DEFAULT 'khach_hang',
  `trang_thai` varchar(20) NOT NULL DEFAULT 'hoat_dong',
  `doi_mat_khau_luc` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  CONSTRAINT `nguoi_dung_chk_1` CHECK ((`gioi_tinh` in (_utf8mb4'nam',_utf8mb4'nu',_utf8mb4'khac'))),
  CONSTRAINT `nguoi_dung_chk_2` CHECK ((`vai_tro` in (_utf8mb4'quan_tri',_utf8mb4'khach_hang'))),
  CONSTRAINT `nguoi_dung_chk_3` CHECK ((`trang_thai` in (_utf8mb4'hoat_dong',_utf8mb4'bi_khoa')))
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nguoi_dung`
--

LOCK TABLES `nguoi_dung` WRITE;
/*!40000 ALTER TABLE `nguoi_dung` DISABLE KEYS */;
INSERT INTO `nguoi_dung` (`id`, `ho_ten`, `email`, `mat_khau`, `so_dien_thoai`, `gioi_tinh`, `ngay_sinh`, `anh_dai_dien`, `vai_tro`, `trang_thai`, `doi_mat_khau_luc`, `created_at`, `updated_at`) VALUES (1,'Quản trị viên','admin@techgear.vn','$2a$12$RIBO5dFY.4A2c9ZDQRN/oe4zLez.luB9urq.0clEoP2NxZz8Y2fWG','0900000001',NULL,NULL,NULL,'quan_tri','hoat_dong',NULL,'2026-11-22 19:37:56.487335','2026-11-22 19:37:56.487335');
INSERT INTO `nguoi_dung` (`id`, `ho_ten`, `email`, `mat_khau`, `so_dien_thoai`, `gioi_tinh`, `ngay_sinh`, `anh_dai_dien`, `vai_tro`, `trang_thai`, `doi_mat_khau_luc`, `created_at`, `updated_at`) VALUES (2,'Nguyễn Văn Khách','khach@techgear.vn','$2a$12$JZZeyeYy71xijX5IowHhU.GUbCQe.OvmO9g38yyCeM6PlEGC/3Zp2','0900000002',NULL,NULL,NULL,'khach_hang','hoat_dong',NULL,'2026-11-22 19:37:56.487335','2026-11-22 19:37:56.487335');
INSERT INTO `nguoi_dung` (`id`, `ho_ten`, `email`, `mat_khau`, `so_dien_thoai`, `gioi_tinh`, `ngay_sinh`, `anh_dai_dien`, `vai_tro`, `trang_thai`, `doi_mat_khau_luc`, `created_at`, `updated_at`) VALUES (3,'Nguyễn Thị Lan','nguyen.lan@example.com','$2a$12$JZZeyeYy71xijX5IowHhU.GUbCQe.OvmO9g38yyCeM6PlEGC/3Zp2','0912345601','nu','1998-03-12',NULL,'khach_hang','hoat_dong',NULL,'2026-07-25 19:37:56.000000','2026-11-22 19:37:56.704313');
INSERT INTO `nguoi_dung` (`id`, `ho_ten`, `email`, `mat_khau`, `so_dien_thoai`, `gioi_tinh`, `ngay_sinh`, `anh_dai_dien`, `vai_tro`, `trang_thai`, `doi_mat_khau_luc`, `created_at`, `updated_at`) VALUES (4,'Trần Quang Minh','tran.minh@example.com','$2a$12$JZZeyeYy71xijX5IowHhU.GUbCQe.OvmO9g38yyCeM6PlEGC/3Zp2','0912345602','nam','1995-07-08',NULL,'khach_hang','hoat_dong',NULL,'2026-07-27 19:37:56.000000','2026-11-22 19:37:56.704313');
INSERT INTO `nguoi_dung` (`id`, `ho_ten`, `email`, `mat_khau`, `so_dien_thoai`, `gioi_tinh`, `ngay_sinh`, `anh_dai_dien`, `vai_tro`, `trang_thai`, `doi_mat_khau_luc`, `created_at`, `updated_at`) VALUES (5,'Lê Thu Hương','le.huong@example.com','$2a$12$JZZeyeYy71xijX5IowHhU.GUbCQe.OvmO9g38yyCeM6PlEGC/3Zp2','0912345603','nu','2001-11-20',NULL,'khach_hang','hoat_dong',NULL,'2026-07-30 19:37:56.000000','2026-11-22 19:37:56.704313');
INSERT INTO `nguoi_dung` (`id`, `ho_ten`, `email`, `mat_khau`, `so_dien_thoai`, `gioi_tinh`, `ngay_sinh`, `anh_dai_dien`, `vai_tro`, `trang_thai`, `doi_mat_khau_luc`, `created_at`, `updated_at`) VALUES (6,'Phạm Anh Đức','pham.duc@example.com','$2a$12$JZZeyeYy71xijX5IowHhU.GUbCQe.OvmO9g38yyCeM6PlEGC/3Zp2','0912345604','nam','1993-01-30',NULL,'khach_hang','hoat_dong',NULL,'2026-08-02 19:37:56.000000','2026-11-22 19:37:56.704313');
INSERT INTO `nguoi_dung` (`id`, `ho_ten`, `email`, `mat_khau`, `so_dien_thoai`, `gioi_tinh`, `ngay_sinh`, `anh_dai_dien`, `vai_tro`, `trang_thai`, `doi_mat_khau_luc`, `created_at`, `updated_at`) VALUES (7,'Võ Ngọc Mai','vo.mai@example.com','$2a$12$JZZeyeYy71xijX5IowHhU.GUbCQe.OvmO9g38yyCeM6PlEGC/3Zp2','0912345605','nu','2000-05-05',NULL,'khach_hang','hoat_dong',NULL,'2026-08-04 19:37:56.000000','2026-11-22 19:37:56.704313');
INSERT INTO `nguoi_dung` (`id`, `ho_ten`, `email`, `mat_khau`, `so_dien_thoai`, `gioi_tinh`, `ngay_sinh`, `anh_dai_dien`, `vai_tro`, `trang_thai`, `doi_mat_khau_luc`, `created_at`, `updated_at`) VALUES (8,'Đặng Minh Khoa','dang.khoa@example.com','$2a$12$JZZeyeYy71xijX5IowHhU.GUbCQe.OvmO9g38yyCeM6PlEGC/3Zp2','0912345606','nam','1997-09-14',NULL,'khach_hang','hoat_dong',NULL,'2026-08-09 19:37:56.000000','2026-11-22 19:37:56.704313');
INSERT INTO `nguoi_dung` (`id`, `ho_ten`, `email`, `mat_khau`, `so_dien_thoai`, `gioi_tinh`, `ngay_sinh`, `anh_dai_dien`, `vai_tro`, `trang_thai`, `doi_mat_khau_luc`, `created_at`, `updated_at`) VALUES (9,'Bùi Phương Thảo','bui.thao@example.com','$2a$12$JZZeyeYy71xijX5IowHhU.GUbCQe.OvmO9g38yyCeM6PlEGC/3Zp2','0912345607','nu','1999-12-01',NULL,'khach_hang','hoat_dong',NULL,'2026-08-14 19:37:56.000000','2026-11-22 19:37:56.704313');
INSERT INTO `nguoi_dung` (`id`, `ho_ten`, `email`, `mat_khau`, `so_dien_thoai`, `gioi_tinh`, `ngay_sinh`, `anh_dai_dien`, `vai_tro`, `trang_thai`, `doi_mat_khau_luc`, `created_at`, `updated_at`) VALUES (10,'Hoàng Văn Tuấn','hoang.tuan@example.com','$2a$12$JZZeyeYy71xijX5IowHhU.GUbCQe.OvmO9g38yyCeM6PlEGC/3Zp2','0912345608','nam','1990-04-22',NULL,'khach_hang','hoat_dong',NULL,'2026-08-18 19:37:56.000000','2026-11-22 19:37:56.704313');
INSERT INTO `nguoi_dung` (`id`, `ho_ten`, `email`, `mat_khau`, `so_dien_thoai`, `gioi_tinh`, `ngay_sinh`, `anh_dai_dien`, `vai_tro`, `trang_thai`, `doi_mat_khau_luc`, `created_at`, `updated_at`) VALUES (11,'Đỗ Bảo Ngọc','do.ngoc@example.com','$2a$12$JZZeyeYy71xijX5IowHhU.GUbCQe.OvmO9g38yyCeM6PlEGC/3Zp2','0912345609','nu','2002-08-18',NULL,'khach_hang','hoat_dong',NULL,'2026-08-24 19:37:56.000000','2026-11-22 19:37:56.704313');
INSERT INTO `nguoi_dung` (`id`, `ho_ten`, `email`, `mat_khau`, `so_dien_thoai`, `gioi_tinh`, `ngay_sinh`, `anh_dai_dien`, `vai_tro`, `trang_thai`, `doi_mat_khau_luc`, `created_at`, `updated_at`) VALUES (12,'Ngô Thành Long','ngo.long@example.com','$2a$12$JZZeyeYy71xijX5IowHhU.GUbCQe.OvmO9g38yyCeM6PlEGC/3Zp2','0912345610','nam','1996-02-10',NULL,'khach_hang','bi_khoa',NULL,'2026-09-03 19:37:56.000000','2026-10-03 19:37:56.000000');
/*!40000 ALTER TABLE `nguoi_dung` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `san_pham`
--

DROP TABLE IF EXISTS `san_pham`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `san_pham` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `danh_muc_id` bigint NOT NULL,
  `thuong_hieu_id` bigint DEFAULT NULL,
  `ten` varchar(255) NOT NULL,
  `slug` varchar(255) NOT NULL,
  `mo_ta_ngan` varchar(500) DEFAULT NULL,
  `mo_ta` text,
  `gia_ban` bigint NOT NULL DEFAULT '0',
  `gia_goc` bigint DEFAULT NULL,
  `bao_hanh_thang` int NOT NULL DEFAULT '0',
  `noi_bat` tinyint(1) NOT NULL DEFAULT '0',
  `hien_thi` tinyint(1) NOT NULL DEFAULT '1',
  `luot_xem` int NOT NULL DEFAULT '0',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `deleted_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_san_pham_slug` ((if((`deleted_at` is null),`slug`,NULL))),
  KEY `danh_muc_id` (`danh_muc_id`),
  KEY `thuong_hieu_id` (`thuong_hieu_id`),
  KEY `ix_san_pham_gia_ban` (`gia_ban`),
  CONSTRAINT `san_pham_ibfk_1` FOREIGN KEY (`danh_muc_id`) REFERENCES `danh_muc` (`id`),
  CONSTRAINT `san_pham_ibfk_2` FOREIGN KEY (`thuong_hieu_id`) REFERENCES `thuong_hieu` (`id`),
  CONSTRAINT `san_pham_chk_1` CHECK ((`gia_ban` >= 0)),
  CONSTRAINT `san_pham_chk_2` CHECK ((`gia_goc` >= 0)),
  CONSTRAINT `san_pham_chk_3` CHECK ((`bao_hanh_thang` >= 0)),
  CONSTRAINT `san_pham_chk_4` CHECK ((`luot_xem` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `san_pham`
--

LOCK TABLES `san_pham` WRITE;
/*!40000 ALTER TABLE `san_pham` DISABLE KEYS */;
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (1,5,1,'Củ sạc Anker Nano 65W GaN','cu-sac-anker-nano-65w','Sạc nhanh 65W cho laptop, điện thoại; nhỏ gọn nhờ công nghệ GaN.','<p>Củ sạc 1 cổng USB-C 65W, hỗ trợ PD 3.0 và PPS.</p>',790000,990000,18,1,1,320,'2026-10-23 19:37:56.000000','2026-11-22 19:37:56.635633',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (2,5,2,'Củ sạc Apple USB-C 20W','cu-sac-apple-usb-c-20w','Củ sạc chính hãng Apple, sạc nhanh iPhone.','<p>Sạc nhanh iPhone 50% trong khoảng 30 phút.</p>',490000,550000,12,0,1,210,'2026-10-25 19:37:56.000000','2026-11-22 19:37:56.635633',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (3,5,3,'Củ sạc Baseus GaN5 Pro 100W','cu-sac-baseus-gan5-pro-100w','3 cổng (2 USB-C, 1 USB-A), tổng công suất 100W.','<p>Sạc cùng lúc laptop, máy tính bảng và điện thoại.</p>',1090000,NULL,12,1,1,150,'2026-11-02 19:37:56.000000','2026-11-22 19:37:56.635633',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (4,6,1,'Cáp Lightning Anker PowerLine III','cap-lightning-anker-powerline-iii','Chuẩn MFi, độ bền 25.000 lần gập.','<p>Cáp USB-C to Lightning dài 1m.</p>',290000,350000,24,0,1,90,'2026-10-13 19:37:56.000000','2026-11-22 19:37:56.635633',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (5,6,8,'Cáp USB-C to USB-C Ugreen 100W','cap-usb-c-ugreen-100w','Cáp bện dù, hỗ trợ sạc nhanh 100W.','<p>Chip E-Marker, truyền dữ liệu 480Mbps.</p>',159000,NULL,18,1,1,280,'2026-10-28 19:37:56.000000','2026-11-22 19:37:56.635633',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (6,7,1,'Sạc dự phòng Anker PowerCore 10000mAh','sac-du-phong-anker-powercore-10000','Nhỏ gọn, sạc nhanh 20W.','<p>Dung lượng 10.000mAh, 1 cổng USB-C vào/ra và 1 cổng USB-A.</p>',590000,690000,18,1,1,260,'2026-11-04 19:37:56.000000','2026-11-22 19:37:56.635633',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (7,7,9,'Sạc dự phòng Xiaomi 20000mAh 22.5W','sac-du-phong-xiaomi-20000','Dung lượng lớn, sạc nhanh 22.5W.','<p>3 cổng ra, sạc được 3 thiết bị cùng lúc.</p>',650000,NULL,6,0,1,120,'2026-10-18 19:37:56.000000','2026-11-22 19:37:56.635633',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (8,8,2,'Tai nghe Apple AirPods Pro 2','tai-nghe-apple-airpods-pro-2','Chống ồn chủ động, hộp sạc USB-C.','<p>Chip H2, âm thanh không gian cá nhân hóa.</p>',5490000,6190000,12,1,1,610,'2026-11-10 19:37:56.000000','2026-11-22 19:37:56.635633',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (9,8,6,'Tai nghe Samsung Galaxy Buds3','tai-nghe-samsung-galaxy-buds3','Thiết kế mở, âm thanh AKG.','<p>Chống ồn chủ động, pin 30 giờ cùng hộp sạc.</p>',3490000,NULL,12,0,1,180,'2026-11-14 19:37:56.000000','2026-11-22 19:37:56.635633',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (10,8,7,'Tai nghe Sony WH-1000XM5','tai-nghe-sony-wh-1000xm5','Chống ồn chủ động hàng đầu, pin 30 giờ.','<p>8 micro chống ồn, kết nối Bluetooth 5.2 đa điểm.</p>',7990000,8990000,12,1,1,540,'2026-11-12 19:37:56.000000','2026-11-22 19:37:56.635633',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (11,9,4,'Loa JBL Flip 6','loa-jbl-flip-6','Chống nước IP67, pin 12 giờ.','<p>Âm thanh JBL Original Pro Sound, kết nối PartyBoost.</p>',2690000,2990000,12,1,1,390,'2026-11-07 19:37:56.000000','2026-11-22 19:37:56.635633',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (12,9,7,'Loa Sony SRS-XB13','loa-sony-srs-xb13','Nhỏ gọn, chống nước IP67.','<p>Extra Bass, pin 16 giờ.</p>',990000,NULL,12,0,1,75,'2026-10-08 19:37:56.000000','2026-11-22 19:37:56.635633',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (13,10,5,'Chuột Logitech MX Master 3S','chuot-logitech-mx-master-3s','Click yên tĩnh, cảm biến 8000 DPI.','<p>Cuộn MagSpeed, kết nối 3 thiết bị.</p>',2290000,2490000,12,1,1,430,'2026-11-08 19:37:56.000000','2026-11-22 19:37:56.635633',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (14,10,5,'Chuột gaming Logitech G304','chuot-logitech-g304','Không dây Lightspeed, pin 250 giờ.','<p>Cảm biến HERO 12K, nặng 99g.</p>',790000,NULL,24,0,1,260,'2026-10-03 19:37:56.000000','2026-11-22 19:37:56.635633',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (15,11,5,'Bàn phím cơ Logitech MX Mechanical Mini','ban-phim-logitech-mx-mechanical-mini','Bàn phím cơ không dây, đèn nền thông minh.','<p>Bố cục 75%, kết nối Bluetooth và Logi Bolt.</p>',3290000,NULL,12,0,1,140,'2026-11-16 19:37:56.000000','2026-11-22 19:37:56.635633',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (16,12,8,'Ốp lưng trong suốt Ugreen cho iPhone 16 Pro','op-lung-ugreen-iphone-16-pro','Chống ố vàng, viền chống sốc.','<p>Hỗ trợ sạc MagSafe.</p>',190000,NULL,0,0,1,60,'2026-11-19 19:37:56.000000','2026-11-22 19:37:56.635633',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (17,13,3,'Kính cường lực Baseus iPhone 16','kinh-cuong-luc-baseus-iphone-16','Độ cứng 9H, kèm khung dán.','<p>Phủ lớp chống bám vân tay.</p>',150000,NULL,0,0,1,40,'2026-11-20 19:37:56.000000','2026-11-22 19:37:56.635633',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (32,5,6,'Củ sạc nhanh Samsung 25W','cu-sac-samsung-25w','Sạc siêu nhanh 25W cho điện thoại Galaxy.','<p>Chuẩn USB PD 3.0 PPS, 1 cổng USB-C.</p>',390000,450000,12,0,1,170,'2026-08-29 19:37:56.000000','2026-11-22 19:37:56.642747',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (33,5,8,'Tẩu sạc ô tô Ugreen 30W','tau-sac-o-to-ugreen-30w','2 cổng USB-C và USB-A, sạc nhanh trên xe.','<p>Tổng công suất 30W, vỏ hợp kim nhôm tản nhiệt tốt.</p>',290000,NULL,18,0,1,65,'2026-11-18 19:37:56.000000','2026-11-22 19:37:56.642747',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (34,6,3,'Cáp USB-C to USB-C Baseus 60W','cap-usb-c-baseus-60w','Sạc nhanh 60W, vỏ bện chống đứt.','<p>Hỗ trợ USB PD 60W, truyền dữ liệu 480Mbps.</p>',129000,NULL,12,0,1,95,'2026-09-13 19:37:56.000000','2026-11-22 19:37:56.642747',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (35,6,8,'Cáp USB-A to USB-C Ugreen 3A','cap-usb-a-usb-c-ugreen-3a','Dùng với củ sạc USB-A, dòng 3A.','<p>Đầu nối mạ niken chống oxy hóa, dài 1m.</p>',99000,129000,18,0,1,140,'2026-08-19 19:37:56.000000','2026-11-22 19:37:56.642747',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (36,7,6,'Sạc dự phòng Samsung EB-P1100C 10000mAh','sac-du-phong-samsung-eb-p1100c','Mỏng nhẹ, vỏ kim loại, sạc nhanh 15W.','<p>1 cổng USB-C và 1 cổng USB-A, sạc 2 thiết bị cùng lúc.</p>',690000,790000,6,0,1,110,'2026-08-24 19:37:56.000000','2026-11-22 19:37:56.642747',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (37,7,9,'Sạc dự phòng Xiaomi Mi Power Bank Pro 10000mAh','sac-du-phong-xiaomi-pro-10000','Vỏ nhôm nguyên khối, sạc nhanh 18W.','<p>Hỗ trợ Quick Charge 3.0 và USB PD.</p>',550000,NULL,6,0,1,85,'2026-09-18 19:37:56.000000','2026-11-22 19:37:56.642747',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (38,8,6,'Tai nghe Samsung Galaxy Buds2 Pro','tai-nghe-samsung-galaxy-buds2-pro','Chống ồn thông minh, âm thanh 24-bit Hi-Fi.','<p>Chống nước IPX7, kết nối liền mạch với thiết bị Galaxy.</p>',3990000,4490000,12,1,1,350,'2026-09-08 19:37:56.000000','2026-11-22 19:37:56.642747',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (39,8,7,'Tai nghe Sony WH-CH510','tai-nghe-sony-wh-ch510','Nhẹ 132g, pin 35 giờ.','<p>Bluetooth 5.0, sạc USB-C. Đã ngừng kinh doanh.</p>',990000,1290000,12,0,0,230,'2026-08-14 19:37:56.000000','2026-11-22 19:37:56.642747',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (40,9,4,'Loa JBL Charge 4','loa-jbl-charge-4','Pin 20 giờ, kiêm sạc dự phòng.','<p>Chống nước IPX7, công suất 30W.</p>',2490000,2990000,12,0,1,260,'2026-09-03 19:37:56.000000','2026-11-22 19:37:56.642747',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (41,9,4,'Loa JBL Go 2','loa-jbl-go-2','Siêu nhỏ gọn, chống nước IPX7.','<p>Pin 5 giờ, có micro đàm thoại.</p>',690000,790000,6,0,1,190,'2026-09-23 19:37:56.000000','2026-11-22 19:37:56.642747',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (42,10,5,'Chuột không dây Logitech M331 Silent Plus','chuot-logitech-m331-silent-plus','Giảm 90% tiếng click, pin 24 tháng.','<p>Đầu thu USB nano, cảm biến 1000 DPI.</p>',390000,450000,12,0,1,300,'2026-08-26 19:37:56.000000','2026-11-22 19:37:56.642747',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (43,10,5,'Chuột không dây Logitech M705 Marathon','chuot-logitech-m705-marathon','Pin tới 3 năm, cuộn siêu nhanh.','<p>Thiết kế công thái học cho tay phải, 7 nút tùy chỉnh.</p>',990000,NULL,12,0,1,120,'2026-11-17 19:37:56.000000','2026-11-22 19:37:56.642747',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (44,11,10,'Bàn phím cơ Keychron K8','ban-phim-co-keychron-k8','Bố cục TKL 87 phím, Bluetooth 5.1.','<p>Hot-swap, tương thích macOS và Windows.</p>',2190000,NULL,12,1,1,410,'2026-11-21 19:37:56.000000','2026-11-22 19:37:56.642747',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (45,11,5,'Bàn phím Bluetooth Logitech K380','ban-phim-logitech-k380','Kết nối 3 thiết bị, chuyển đổi bằng 1 phím.','<p>Nhỏ gọn, pin 2 năm, hỗ trợ Windows, macOS, iOS, Android.</p>',790000,890000,12,1,1,280,'2026-08-22 19:37:56.000000','2026-11-22 19:37:56.642747',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (46,12,2,'Ốp lưng Silicone MagSafe Apple cho iPhone 17','op-lung-apple-silicone-iphone-17','Chính hãng Apple, lớp lót microfiber.','<p>Nam châm MagSafe tích hợp, ôm sát thân máy.</p>',1190000,1290000,0,0,1,150,'2026-11-15 19:37:56.000000','2026-11-22 19:37:56.642747',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (47,12,6,'Ốp lưng TPU trong suốt Samsung Galaxy S22 Ultra','op-lung-samsung-galaxy-s22-ultra-tpu','Dẻo, mỏng, giữ nguyên thiết kế máy.','<p>Gờ bảo vệ camera và màn hình.</p>',250000,NULL,0,0,1,45,'2026-08-16 19:37:56.000000','2026-11-22 19:37:56.642747',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (48,13,6,'Kính cường lực Samsung Galaxy A04e','kinh-cuong-luc-samsung-galaxy-a04e','Full màn hình, độ cứng 9H.','<p>Viền đen tràn cạnh, kèm khăn lau.</p>',120000,NULL,0,0,1,30,'2026-09-21 19:37:56.000000','2026-11-22 19:37:56.642747',NULL);
INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `mo_ta`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`, `hien_thi`, `luot_xem`, `created_at`, `updated_at`, `deleted_at`) VALUES (49,13,8,'Kính cường lực chống nhìn trộm Ugreen iPhone 16','mieng-dan-chong-nhin-trom-iphone','Góc nhìn 60°, bảo vệ quyền riêng tư.','<p>Độ cứng 9H, phủ lớp chống bám vân tay.</p>',290000,NULL,0,0,1,70,'2026-09-28 19:37:56.000000','2026-11-22 19:37:56.642747',NULL);
/*!40000 ALTER TABLE `san_pham` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thanh_toan`
--

DROP TABLE IF EXISTS `thanh_toan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `thanh_toan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `don_hang_id` bigint NOT NULL,
  `phuong_thuc` varchar(10) NOT NULL,
  `so_tien` bigint NOT NULL,
  `ma_tham_chieu` varchar(50) NOT NULL,
  `ma_giao_dich` varchar(50) DEFAULT NULL,
  `trang_thai` varchar(20) NOT NULL DEFAULT 'cho_xu_ly',
  `du_lieu_phan_hoi` json DEFAULT NULL,
  `thanh_toan_luc` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `ma_tham_chieu` (`ma_tham_chieu`),
  UNIQUE KEY `ma_giao_dich` (`ma_giao_dich`),
  KEY `don_hang_id` (`don_hang_id`),
  CONSTRAINT `thanh_toan_ibfk_1` FOREIGN KEY (`don_hang_id`) REFERENCES `don_hang` (`id`) ON DELETE CASCADE,
  CONSTRAINT `thanh_toan_chk_1` CHECK ((`phuong_thuc` in (_utf8mb4'cod',_utf8mb4'vnpay'))),
  CONSTRAINT `thanh_toan_chk_2` CHECK ((`so_tien` >= 0)),
  CONSTRAINT `thanh_toan_chk_3` CHECK ((`trang_thai` in (_utf8mb4'cho_xu_ly',_utf8mb4'thanh_cong',_utf8mb4'that_bai')))
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thanh_toan`
--

LOCK TABLES `thanh_toan` WRITE;
/*!40000 ALTER TABLE `thanh_toan` DISABLE KEYS */;
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (1,2,'vnpay',710100,'DH2608255JXCR601','14015838','thanh_cong','{\"vnp_Amount\": \"71010000\", \"vnp_TxnRef\": \"DH2608255JXCR601\", \"vnp_PayDate\": \"20260825142056\", \"vnp_BankCode\": \"NCB\", \"vnp_CardType\": \"ATM\", \"vnp_ResponseCode\": \"00\", \"vnp_TransactionNo\": \"14015838\", \"vnp_TransactionStatus\": \"00\"}','2026-08-25 14:20:56.000000','2026-08-25 14:19:56.000000','2026-08-25 14:20:56.000000');
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (2,4,'vnpay',1380000,'DH260828KYDS7L01','14031676','thanh_cong','{\"vnp_Amount\": \"138000000\", \"vnp_TxnRef\": \"DH260828KYDS7L01\", \"vnp_PayDate\": \"20260828114056\", \"vnp_BankCode\": \"NCB\", \"vnp_CardType\": \"ATM\", \"vnp_ResponseCode\": \"00\", \"vnp_TransactionNo\": \"14031676\", \"vnp_TransactionStatus\": \"00\"}','2026-08-28 11:40:56.000000','2026-08-28 11:39:56.000000','2026-08-28 11:40:56.000000');
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (3,8,'vnpay',3990000,'DH260909FU9N3G01','14063352','thanh_cong','{\"vnp_Amount\": \"399000000\", \"vnp_TxnRef\": \"DH260909FU9N3G01\", \"vnp_PayDate\": \"20260909161056\", \"vnp_BankCode\": \"NCB\", \"vnp_CardType\": \"ATM\", \"vnp_ResponseCode\": \"00\", \"vnp_TransactionNo\": \"14063352\", \"vnp_TransactionStatus\": \"00\"}','2026-09-09 16:10:56.000000','2026-09-09 16:09:56.000000','2026-09-09 16:10:56.000000');
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (4,10,'vnpay',708000,'DH260914VAP4HW01','14079190','thanh_cong','{\"vnp_Amount\": \"70800000\", \"vnp_TxnRef\": \"DH260914VAP4HW01\", \"vnp_PayDate\": \"20260914174056\", \"vnp_BankCode\": \"NCB\", \"vnp_CardType\": \"ATM\", \"vnp_ResponseCode\": \"00\", \"vnp_TransactionNo\": \"14079190\", \"vnp_TransactionStatus\": \"00\"}','2026-09-14 17:40:56.000000','2026-09-14 17:39:56.000000','2026-09-14 17:40:56.000000');
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (5,11,'vnpay',550000,'DH2609194HWBQ501',NULL,'cho_xu_ly',NULL,NULL,'2026-09-19 11:19:56.000000','2026-09-19 11:20:56.000000');
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (6,13,'vnpay',4010000,'DH260922JXCR6K01','14102947','thanh_cong','{\"vnp_Amount\": \"401000000\", \"vnp_TxnRef\": \"DH260922JXCR6K01\", \"vnp_PayDate\": \"20260922163056\", \"vnp_BankCode\": \"NCB\", \"vnp_CardType\": \"ATM\", \"vnp_ResponseCode\": \"00\", \"vnp_TransactionNo\": \"14102947\", \"vnp_TransactionStatus\": \"00\"}','2026-09-22 16:30:56.000000','2026-09-22 16:29:56.000000','2026-09-22 16:30:56.000000');
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (7,16,'vnpay',970000,'DH2609297LZET801','14126704','thanh_cong','{\"vnp_Amount\": \"97000000\", \"vnp_TxnRef\": \"DH2609297LZET801\", \"vnp_PayDate\": \"20260929155056\", \"vnp_BankCode\": \"NCB\", \"vnp_CardType\": \"ATM\", \"vnp_ResponseCode\": \"00\", \"vnp_TransactionNo\": \"14126704\", \"vnp_TransactionStatus\": \"00\"}','2026-09-29 15:50:56.000000','2026-09-29 15:49:56.000000','2026-09-29 15:50:56.000000');
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (8,18,'vnpay',790000,'DH261004M2FU9N01','14142542','thanh_cong','{\"vnp_Amount\": \"79000000\", \"vnp_TxnRef\": \"DH261004M2FU9N01\", \"vnp_PayDate\": \"20261004172056\", \"vnp_BankCode\": \"NCB\", \"vnp_CardType\": \"ATM\", \"vnp_ResponseCode\": \"00\", \"vnp_TransactionNo\": \"14142542\", \"vnp_TransactionStatus\": \"00\"}','2026-10-04 17:20:56.000000','2026-10-04 17:19:56.000000','2026-10-04 17:20:56.000000');
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (9,20,'vnpay',790000,'DH2610113GVAP401','14158380','thanh_cong','{\"vnp_Amount\": \"79000000\", \"vnp_TxnRef\": \"DH2610113GVAP401\", \"vnp_PayDate\": \"20261011153056\", \"vnp_BankCode\": \"NCB\", \"vnp_CardType\": \"ATM\", \"vnp_ResponseCode\": \"00\", \"vnp_TransactionNo\": \"14158380\", \"vnp_TransactionStatus\": \"00\"}','2026-10-11 15:30:56.000000','2026-10-11 15:29:56.000000','2026-10-11 15:30:56.000000');
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (10,22,'vnpay',1080000,'DH261017HWBQ5J01','14174218','thanh_cong','{\"vnp_Amount\": \"108000000\", \"vnp_TxnRef\": \"DH261017HWBQ5J01\", \"vnp_PayDate\": \"20261017122056\", \"vnp_BankCode\": \"NCB\", \"vnp_CardType\": \"ATM\", \"vnp_ResponseCode\": \"00\", \"vnp_TransactionNo\": \"14174218\", \"vnp_TransactionStatus\": \"00\"}','2026-10-17 12:20:56.000000','2026-10-17 12:19:56.000000','2026-10-17 12:20:56.000000');
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (11,24,'vnpay',889000,'DH261024XCR6KY01','14190056','thanh_cong','{\"vnp_Amount\": \"88900000\", \"vnp_TxnRef\": \"DH261024XCR6KY01\", \"vnp_PayDate\": \"20261024135056\", \"vnp_BankCode\": \"NCB\", \"vnp_CardType\": \"ATM\", \"vnp_ResponseCode\": \"00\", \"vnp_TransactionNo\": \"14190056\", \"vnp_TransactionStatus\": \"00\"}','2026-10-24 13:50:56.000000','2026-10-24 13:49:56.000000','2026-10-24 13:50:56.000000');
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (12,26,'vnpay',348000,'DH261029DS7LZE01',NULL,'that_bai','{\"vnp_Amount\": \"34800000\", \"vnp_TxnRef\": \"DH261029DS7LZE01\", \"vnp_BankCode\": \"NCB\", \"vnp_ResponseCode\": \"24\", \"vnp_TransactionStatus\": \"02\"}','2026-10-29 14:29:56.000000','2026-10-29 14:28:56.000000','2026-10-29 14:29:56.000000');
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (13,26,'vnpay',348000,'DH261029DS7LZE02','14205894','thanh_cong','{\"vnp_Amount\": \"34800000\", \"vnp_TxnRef\": \"DH261029DS7LZE02\", \"vnp_PayDate\": \"20261029143056\", \"vnp_BankCode\": \"NCB\", \"vnp_CardType\": \"ATM\", \"vnp_ResponseCode\": \"00\", \"vnp_TransactionNo\": \"14205894\", \"vnp_TransactionStatus\": \"00\"}','2026-10-29 14:30:56.000000','2026-10-29 14:29:56.000000','2026-10-29 14:30:56.000000');
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (14,29,'vnpay',1330000,'DH2611052FU9N301','14229651','thanh_cong','{\"vnp_Amount\": \"133000000\", \"vnp_TxnRef\": \"DH2611052FU9N301\", \"vnp_PayDate\": \"20261105131056\", \"vnp_BankCode\": \"NCB\", \"vnp_CardType\": \"ATM\", \"vnp_ResponseCode\": \"00\", \"vnp_TransactionNo\": \"14229651\", \"vnp_TransactionStatus\": \"00\"}','2026-11-05 13:10:56.000000','2026-11-05 13:09:56.000000','2026-11-05 13:10:56.000000');
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (15,31,'vnpay',2290000,'DH261109GVAP4H01','14245489','thanh_cong','{\"vnp_Amount\": \"229000000\", \"vnp_TxnRef\": \"DH261109GVAP4H01\", \"vnp_PayDate\": \"20261109140056\", \"vnp_BankCode\": \"NCB\", \"vnp_CardType\": \"ATM\", \"vnp_ResponseCode\": \"00\", \"vnp_TransactionNo\": \"14245489\", \"vnp_TransactionStatus\": \"00\"}','2026-11-09 14:00:56.000000','2026-11-09 13:59:56.000000','2026-11-09 14:00:56.000000');
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (16,32,'vnpay',5390000,'DH261111P4HWBQ01','14253408','thanh_cong','{\"vnp_Amount\": \"539000000\", \"vnp_TxnRef\": \"DH261111P4HWBQ01\", \"vnp_PayDate\": \"20261111162056\", \"vnp_BankCode\": \"NCB\", \"vnp_CardType\": \"ATM\", \"vnp_ResponseCode\": \"00\", \"vnp_TransactionNo\": \"14253408\", \"vnp_TransactionStatus\": \"00\"}','2026-11-11 16:20:56.000000','2026-11-11 16:19:56.000000','2026-11-11 16:20:56.000000');
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (17,36,'vnpay',3290000,'DH261117KYDS7L01','14285084','thanh_cong','{\"vnp_Amount\": \"329000000\", \"vnp_TxnRef\": \"DH261117KYDS7L01\", \"vnp_PayDate\": \"20261117132056\", \"vnp_BankCode\": \"NCB\", \"vnp_CardType\": \"ATM\", \"vnp_ResponseCode\": \"00\", \"vnp_TransactionNo\": \"14285084\", \"vnp_TransactionStatus\": \"00\"}','2026-11-17 13:20:56.000000','2026-11-17 13:19:56.000000','2026-11-17 13:20:56.000000');
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (18,38,'vnpay',1380000,'DH261120ZET8M201','14300922','thanh_cong','{\"vnp_Amount\": \"138000000\", \"vnp_TxnRef\": \"DH261120ZET8M201\", \"vnp_PayDate\": \"20261120141056\", \"vnp_BankCode\": \"NCB\", \"vnp_CardType\": \"ATM\", \"vnp_ResponseCode\": \"00\", \"vnp_TransactionNo\": \"14300922\", \"vnp_TransactionStatus\": \"00\"}','2026-11-20 14:10:56.000000','2026-11-20 14:09:56.000000','2026-11-20 14:10:56.000000');
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (19,40,'vnpay',2290000,'DH261121FU9N3G01','14316760','thanh_cong','{\"vnp_Amount\": \"229000000\", \"vnp_TxnRef\": \"DH261121FU9N3G01\", \"vnp_PayDate\": \"20261121171056\", \"vnp_BankCode\": \"NCB\", \"vnp_CardType\": \"ATM\", \"vnp_ResponseCode\": \"00\", \"vnp_TransactionNo\": \"14316760\", \"vnp_TransactionStatus\": \"00\"}','2026-11-21 17:10:56.000000','2026-11-21 17:09:56.000000','2026-11-21 17:10:56.000000');
INSERT INTO `thanh_toan` (`id`, `don_hang_id`, `phuong_thuc`, `so_tien`, `ma_tham_chieu`, `ma_giao_dich`, `trang_thai`, `du_lieu_phan_hoi`, `thanh_toan_luc`, `created_at`, `updated_at`) VALUES (20,42,'vnpay',2580000,'DH261122VAP4HW01','14332598','thanh_cong','{\"vnp_Amount\": \"258000000\", \"vnp_TxnRef\": \"DH261122VAP4HW01\", \"vnp_PayDate\": \"20261122184056\", \"vnp_BankCode\": \"NCB\", \"vnp_CardType\": \"ATM\", \"vnp_ResponseCode\": \"00\", \"vnp_TransactionNo\": \"14332598\", \"vnp_TransactionStatus\": \"00\"}','2026-11-22 18:40:56.000000','2026-11-22 18:39:56.000000','2026-11-22 18:40:56.000000');
/*!40000 ALTER TABLE `thanh_toan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thong_so`
--

DROP TABLE IF EXISTS `thong_so`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `thong_so` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `san_pham_id` bigint NOT NULL,
  `ten` varchar(100) NOT NULL,
  `gia_tri` varchar(255) NOT NULL,
  `thu_tu` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `san_pham_id` (`san_pham_id`),
  CONSTRAINT `thong_so_ibfk_1` FOREIGN KEY (`san_pham_id`) REFERENCES `san_pham` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=127 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thong_so`
--

LOCK TABLES `thong_so` WRITE;
/*!40000 ALTER TABLE `thong_so` DISABLE KEYS */;
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (1,1,'Công suất','65W',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (2,1,'Cổng','1 x USB-C',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (3,2,'Công suất','20W',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (4,2,'Cổng','1 x USB-C',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (5,3,'Công suất','100W',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (6,3,'Cổng','2 x USB-C, 1 x USB-A',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (7,4,'Chiều dài','1m',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (8,4,'Chuẩn','MFi',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (9,5,'Công suất','100W',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (10,5,'Chất liệu','Bện dù',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (11,6,'Dung lượng','10.000mAh',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (12,6,'Công suất','20W',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (13,7,'Dung lượng','20.000mAh',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (14,7,'Công suất','22.5W',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (15,8,'Chip','Apple H2',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (16,8,'Thời lượng pin','6 giờ (30 giờ kèm hộp)',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (17,9,'Kết nối','Bluetooth 5.4',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (18,9,'Thời lượng pin','30 giờ kèm hộp',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (19,10,'Kết nối','Bluetooth 5.2',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (20,10,'Thời lượng pin','30 giờ',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (21,11,'Công suất','30W',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (22,11,'Chống nước','IP67',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (23,12,'Thời lượng pin','16 giờ',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (24,12,'Chống nước','IP67',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (25,13,'DPI','8000',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (26,13,'Kết nối','Bluetooth, Logi Bolt',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (27,14,'DPI','12000',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (28,14,'Trọng lượng','99g',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (29,15,'Bố cục','75%',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (30,15,'Thời lượng pin','15 ngày (bật đèn)',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (31,16,'Chất liệu','TPU + PC',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (32,17,'Độ cứng','9H',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (64,32,'Công suất','25W',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (65,32,'Cổng','1 x USB-C',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (66,33,'Công suất','30W',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (67,33,'Cổng','1 x USB-C, 1 x USB-A',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (68,34,'Công suất','60W',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (69,34,'Chất liệu','Bện dù',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (70,35,'Dòng điện','3A',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (71,35,'Chiều dài','1m',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (72,36,'Dung lượng','10.000mAh',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (73,36,'Công suất','15W',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (74,37,'Dung lượng','10.000mAh',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (75,37,'Công suất','18W',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (76,38,'Kết nối','Bluetooth 5.3',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (77,38,'Chống nước','IPX7',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (78,39,'Kết nối','Bluetooth 5.0',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (79,39,'Thời lượng pin','35 giờ',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (80,40,'Công suất','30W',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (81,40,'Thời lượng pin','20 giờ',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (82,41,'Công suất','3W',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (83,41,'Chống nước','IPX7',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (84,42,'DPI','1000',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (85,42,'Kết nối','USB nano 2.4GHz',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (86,43,'DPI','1000',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (87,43,'Thời lượng pin','3 năm',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (88,44,'Bố cục','TKL 87 phím',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (89,44,'Kết nối','Bluetooth 5.1, USB-C',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (90,45,'Kết nối','Bluetooth',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (91,45,'Thời lượng pin','2 năm',2);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (92,46,'Chất liệu','Silicone',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (93,47,'Chất liệu','TPU',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (94,48,'Độ cứng','9H',1);
INSERT INTO `thong_so` (`id`, `san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES (95,49,'Độ cứng','9H',1);
/*!40000 ALTER TABLE `thong_so` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thuong_hieu`
--

DROP TABLE IF EXISTS `thuong_hieu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `thuong_hieu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ten` varchar(100) NOT NULL,
  `slug` varchar(120) NOT NULL,
  `logo` varchar(255) DEFAULT NULL,
  `mo_ta` text,
  `hien_thi` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `deleted_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_thuong_hieu_slug` ((if((`deleted_at` is null),`slug`,NULL)))
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thuong_hieu`
--

LOCK TABLES `thuong_hieu` WRITE;
/*!40000 ALTER TABLE `thuong_hieu` DISABLE KEYS */;
INSERT INTO `thuong_hieu` (`id`, `ten`, `slug`, `logo`, `mo_ta`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (1,'Anker','anker','https://upload.wikimedia.org/wikipedia/commons/a/a4/Anker_Innovations_Co._Ltd._Logo.png',NULL,1,'2026-11-22 19:37:56.635276','2026-11-22 19:37:56.635276',NULL);
INSERT INTO `thuong_hieu` (`id`, `ten`, `slug`, `logo`, `mo_ta`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (2,'Apple','apple','https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fa/Apple_logo_black.svg/960px-Apple_logo_black.svg.png',NULL,1,'2026-11-22 19:37:56.635276','2026-11-22 19:37:56.635276',NULL);
INSERT INTO `thuong_hieu` (`id`, `ten`, `slug`, `logo`, `mo_ta`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (3,'Baseus','baseus',NULL,NULL,1,'2026-11-22 19:37:56.635276','2026-11-22 19:37:56.635276',NULL);
INSERT INTO `thuong_hieu` (`id`, `ten`, `slug`, `logo`, `mo_ta`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (4,'JBL','jbl','https://upload.wikimedia.org/wikipedia/commons/d/d2/Jbl-logo.jpg',NULL,1,'2026-11-22 19:37:56.635276','2026-11-22 19:37:56.635276',NULL);
INSERT INTO `thuong_hieu` (`id`, `ten`, `slug`, `logo`, `mo_ta`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (5,'Logitech','logitech','https://thumb.wikimedia.org/wikipedia/commons/thumb/1/17/Logitech_logo.svg/960px-Logitech_logo.svg.png',NULL,1,'2026-11-22 19:37:56.635276','2026-11-22 19:37:56.635276',NULL);
INSERT INTO `thuong_hieu` (`id`, `ten`, `slug`, `logo`, `mo_ta`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (6,'Samsung','samsung','https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b4/Samsung_wordmark.svg/960px-Samsung_wordmark.svg.png',NULL,1,'2026-11-22 19:37:56.635276','2026-11-22 19:37:56.635276',NULL);
INSERT INTO `thuong_hieu` (`id`, `ten`, `slug`, `logo`, `mo_ta`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (7,'Sony','sony','https://thumb.wikimedia.org/wikipedia/commons/thumb/c/ca/Sony_logo.svg/960px-Sony_logo.svg.png',NULL,1,'2026-11-22 19:37:56.635276','2026-11-22 19:37:56.635276',NULL);
INSERT INTO `thuong_hieu` (`id`, `ten`, `slug`, `logo`, `mo_ta`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (8,'Ugreen','ugreen','https://upload.wikimedia.org/wikipedia/commons/7/75/UGREEN_wordmark.png',NULL,1,'2026-11-22 19:37:56.635276','2026-11-22 19:37:56.635276',NULL);
INSERT INTO `thuong_hieu` (`id`, `ten`, `slug`, `logo`, `mo_ta`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (9,'Xiaomi','xiaomi','https://thumb.wikimedia.org/wikipedia/commons/thumb/a/ae/Xiaomi_logo_%282021-%29.svg/960px-Xiaomi_logo_%282021-%29.svg.png',NULL,1,'2026-11-22 19:37:56.635276','2026-11-22 19:37:56.635276',NULL);
INSERT INTO `thuong_hieu` (`id`, `ten`, `slug`, `logo`, `mo_ta`, `hien_thi`, `created_at`, `updated_at`, `deleted_at`) VALUES (10,'Keychron','keychron','https://thumb.wikimedia.org/wikipedia/commons/thumb/6/69/Keychron_logo.svg/960px-Keychron_logo.svg.png',NULL,1,'2026-11-22 19:37:56.642684','2026-11-22 19:37:56.642684',NULL);
/*!40000 ALTER TABLE `thuong_hieu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tuy_chon`
--

DROP TABLE IF EXISTS `tuy_chon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tuy_chon` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `san_pham_id` bigint NOT NULL,
  `ten` varchar(50) NOT NULL,
  `loai` varchar(20) NOT NULL,
  `thu_tu` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `san_pham_id` (`san_pham_id`,`ten`),
  CONSTRAINT `tuy_chon_ibfk_1` FOREIGN KEY (`san_pham_id`) REFERENCES `san_pham` (`id`) ON DELETE CASCADE,
  CONSTRAINT `tuy_chon_chk_1` CHECK ((`loai` in (_utf8mb4'mau_sac',_utf8mb4'dung_luong',_utf8mb4'phien_ban',_utf8mb4'khac')))
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tuy_chon`
--

LOCK TABLES `tuy_chon` WRITE;
/*!40000 ALTER TABLE `tuy_chon` DISABLE KEYS */;
INSERT INTO `tuy_chon` (`id`, `san_pham_id`, `ten`, `loai`, `thu_tu`) VALUES (1,1,'Màu sắc','mau_sac',0);
INSERT INTO `tuy_chon` (`id`, `san_pham_id`, `ten`, `loai`, `thu_tu`) VALUES (2,3,'Màu sắc','mau_sac',0);
INSERT INTO `tuy_chon` (`id`, `san_pham_id`, `ten`, `loai`, `thu_tu`) VALUES (3,5,'Chiều dài','khac',0);
INSERT INTO `tuy_chon` (`id`, `san_pham_id`, `ten`, `loai`, `thu_tu`) VALUES (4,6,'Màu sắc','mau_sac',0);
INSERT INTO `tuy_chon` (`id`, `san_pham_id`, `ten`, `loai`, `thu_tu`) VALUES (5,9,'Màu sắc','mau_sac',0);
INSERT INTO `tuy_chon` (`id`, `san_pham_id`, `ten`, `loai`, `thu_tu`) VALUES (6,10,'Màu sắc','mau_sac',0);
INSERT INTO `tuy_chon` (`id`, `san_pham_id`, `ten`, `loai`, `thu_tu`) VALUES (7,11,'Màu sắc','mau_sac',0);
INSERT INTO `tuy_chon` (`id`, `san_pham_id`, `ten`, `loai`, `thu_tu`) VALUES (8,13,'Màu sắc','mau_sac',0);
INSERT INTO `tuy_chon` (`id`, `san_pham_id`, `ten`, `loai`, `thu_tu`) VALUES (9,15,'Loại switch','phien_ban',0);
INSERT INTO `tuy_chon` (`id`, `san_pham_id`, `ten`, `loai`, `thu_tu`) VALUES (10,16,'Dòng máy','phien_ban',0);
INSERT INTO `tuy_chon` (`id`, `san_pham_id`, `ten`, `loai`, `thu_tu`) VALUES (16,34,'Chiều dài','khac',0);
INSERT INTO `tuy_chon` (`id`, `san_pham_id`, `ten`, `loai`, `thu_tu`) VALUES (17,38,'Màu sắc','mau_sac',0);
INSERT INTO `tuy_chon` (`id`, `san_pham_id`, `ten`, `loai`, `thu_tu`) VALUES (18,40,'Màu sắc','mau_sac',0);
INSERT INTO `tuy_chon` (`id`, `san_pham_id`, `ten`, `loai`, `thu_tu`) VALUES (19,41,'Màu sắc','mau_sac',0);
INSERT INTO `tuy_chon` (`id`, `san_pham_id`, `ten`, `loai`, `thu_tu`) VALUES (20,42,'Màu sắc','mau_sac',0);
INSERT INTO `tuy_chon` (`id`, `san_pham_id`, `ten`, `loai`, `thu_tu`) VALUES (21,44,'Loại switch','phien_ban',0);
/*!40000 ALTER TABLE `tuy_chon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `yeu_thich`
--

DROP TABLE IF EXISTS `yeu_thich`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `yeu_thich` (
  `nguoi_dung_id` bigint NOT NULL,
  `san_pham_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`nguoi_dung_id`,`san_pham_id`),
  KEY `san_pham_id` (`san_pham_id`),
  CONSTRAINT `yeu_thich_ibfk_1` FOREIGN KEY (`nguoi_dung_id`) REFERENCES `nguoi_dung` (`id`) ON DELETE CASCADE,
  CONSTRAINT `yeu_thich_ibfk_2` FOREIGN KEY (`san_pham_id`) REFERENCES `san_pham` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `yeu_thich`
--

LOCK TABLES `yeu_thich` WRITE;
/*!40000 ALTER TABLE `yeu_thich` DISABLE KEYS */;
INSERT INTO `yeu_thich` (`nguoi_dung_id`, `san_pham_id`, `created_at`) VALUES (2,8,'2026-11-22 19:37:56.832654');
INSERT INTO `yeu_thich` (`nguoi_dung_id`, `san_pham_id`, `created_at`) VALUES (2,10,'2026-11-22 19:37:56.832654');
INSERT INTO `yeu_thich` (`nguoi_dung_id`, `san_pham_id`, `created_at`) VALUES (2,44,'2026-11-22 19:37:56.832654');
/*!40000 ALTER TABLE `yeu_thich` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed
