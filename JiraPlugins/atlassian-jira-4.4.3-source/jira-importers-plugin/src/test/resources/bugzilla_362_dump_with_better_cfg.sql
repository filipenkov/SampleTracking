-- MySQL dump 10.13  Distrib 5.1.37, for debian-linux-gnu (i486)
--
-- Host: localhost    Database: bz362
-- ------------------------------------------------------
-- Server version	5.1.37-1ubuntu5.4

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
-- Table structure for table `attach_data`
--

DROP TABLE IF EXISTS `attach_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `attach_data` (
  `id` mediumint(9) NOT NULL,
  `thedata` longblob NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_attach_data_id_attachments_attach_id` FOREIGN KEY (`id`) REFERENCES `attachments` (`attach_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 MAX_ROWS=100000 AVG_ROW_LENGTH=1000000;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attach_data`
--

LOCK TABLES `attach_data` WRITE;
/*!40000 ALTER TABLE `attach_data` DISABLE KEYS */;
INSERT INTO `attach_data` VALUES (1,'###############################################################################\n# Copyright (c) 2007, 2009 David Green and others.\n# All rights reserved. This program and the accompanying materials\n# are made available under the terms of the Eclipse Public License v1.0\n# which accompanies this distribution, and is available at\n# http://www.eclipse.org/legal/epl-v10.html\n#\n# Contributors:\n#     David Green - initial API and implementation\n###############################################################################\nsource.. = src/\noutput.. = bin/\nbin.includes = META-INF/,\\\n               .,\\\n               plugin.xml,\\\n               plugin.properties,\\\n               about.html\n\nsrc.includes=about.html'),(3,''),(4,''),(5,''),(6,'#surefire\n#Mon Nov 08 12:55:20 CET 2010\nuser.dir=/home/wseliga/lab/jira-importers-plugin\nlocalRepository=/home/wseliga/.m2/repository\nbasedir=/home/wseliga/lab/jira-importers-plugin\n');
/*!40000 ALTER TABLE `attach_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `attachments`
--

DROP TABLE IF EXISTS `attachments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `attachments` (
  `attach_id` mediumint(9) NOT NULL AUTO_INCREMENT,
  `bug_id` mediumint(9) NOT NULL,
  `creation_ts` datetime NOT NULL,
  `modification_time` datetime NOT NULL,
  `description` tinytext NOT NULL,
  `mimetype` tinytext NOT NULL,
  `ispatch` tinyint(4) DEFAULT NULL,
  `filename` varchar(100) NOT NULL,
  `submitter_id` mediumint(9) NOT NULL,
  `isobsolete` tinyint(4) NOT NULL DEFAULT '0',
  `isprivate` tinyint(4) NOT NULL DEFAULT '0',
  `isurl` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`attach_id`),
  KEY `attachments_bug_id_idx` (`bug_id`),
  KEY `attachments_creation_ts_idx` (`creation_ts`),
  KEY `attachments_modification_time_idx` (`modification_time`),
  KEY `attachments_submitter_id_idx` (`submitter_id`,`bug_id`),
  CONSTRAINT `fk_attachments_bug_id_bugs_bug_id` FOREIGN KEY (`bug_id`) REFERENCES `bugs` (`bug_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_attachments_submitter_id_profiles_userid` FOREIGN KEY (`submitter_id`) REFERENCES `profiles` (`userid`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attachments`
--

LOCK TABLES `attachments` WRITE;
/*!40000 ALTER TABLE `attachments` DISABLE KEYS */;
INSERT INTO `attachments` VALUES (1,8,'2010-11-02 16:40:26','2010-11-02 16:40:26','The small attachment','text/plain',0,'build.properties',2,0,0,0),(3,8,'2010-11-02 16:45:16','2010-11-02 16:45:16','That\'s a big file','application/x-java-archive',0,'atlassian-jira-plugin-timesheet-1.8.jar',2,0,0,0),(4,8,'2010-11-04 10:19:34','2010-11-04 10:19:34','Very big file','application/x-java-archive',0,'atlassian-universal-plugin-manager-plugin-1.0.1.jar',2,0,0,0),(5,35,'2010-11-09 16:30:27','2010-11-09 16:30:27','afd','text/xml',0,'processed-monitor-log3.5914473331217972108.xml',2,0,0,0),(6,36,'2010-11-09 16:33:43','2010-11-09 16:33:43','Test attachment Tue Nov 09 16:32:49 CET 2010','text/plain',0,'surefire2323378835174320423tmp',2,0,0,0);
/*!40000 ALTER TABLE `attachments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bug_group_map`
--

DROP TABLE IF EXISTS `bug_group_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bug_group_map` (
  `bug_id` mediumint(9) NOT NULL,
  `group_id` mediumint(9) NOT NULL,
  UNIQUE KEY `bug_group_map_bug_id_idx` (`bug_id`,`group_id`),
  KEY `bug_group_map_group_id_idx` (`group_id`),
  CONSTRAINT `fk_bug_group_map_bug_id_bugs_bug_id` FOREIGN KEY (`bug_id`) REFERENCES `bugs` (`bug_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_bug_group_map_group_id_groups_id` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bug_group_map`
--

LOCK TABLES `bug_group_map` WRITE;
/*!40000 ALTER TABLE `bug_group_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `bug_group_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bug_see_also`
--

DROP TABLE IF EXISTS `bug_see_also`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bug_see_also` (
  `bug_id` mediumint(9) NOT NULL,
  `value` varchar(255) NOT NULL,
  UNIQUE KEY `bug_see_also_bug_id_idx` (`bug_id`,`value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bug_see_also`
--

LOCK TABLES `bug_see_also` WRITE;
/*!40000 ALTER TABLE `bug_see_also` DISABLE KEYS */;
/*!40000 ALTER TABLE `bug_see_also` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bug_severity`
--

DROP TABLE IF EXISTS `bug_severity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bug_severity` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `value` varchar(64) NOT NULL,
  `sortkey` smallint(6) NOT NULL DEFAULT '0',
  `isactive` tinyint(4) NOT NULL DEFAULT '1',
  `visibility_value_id` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `bug_severity_value_idx` (`value`),
  KEY `bug_severity_sortkey_idx` (`sortkey`,`value`),
  KEY `bug_severity_visibility_value_id_idx` (`visibility_value_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bug_severity`
--

LOCK TABLES `bug_severity` WRITE;
/*!40000 ALTER TABLE `bug_severity` DISABLE KEYS */;
INSERT INTO `bug_severity` VALUES (1,'blocker',100,1,NULL),(2,'critical',200,1,NULL),(3,'major',300,1,NULL),(4,'normal',400,1,NULL),(5,'minor',500,1,NULL),(6,'trivial',600,1,NULL),(7,'enhancement',700,1,NULL);
/*!40000 ALTER TABLE `bug_severity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bug_status`
--

DROP TABLE IF EXISTS `bug_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bug_status` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `value` varchar(64) NOT NULL,
  `sortkey` smallint(6) NOT NULL DEFAULT '0',
  `isactive` tinyint(4) NOT NULL DEFAULT '1',
  `visibility_value_id` smallint(6) DEFAULT NULL,
  `is_open` tinyint(4) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `bug_status_value_idx` (`value`),
  KEY `bug_status_sortkey_idx` (`sortkey`,`value`),
  KEY `bug_status_visibility_value_id_idx` (`visibility_value_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bug_status`
--

LOCK TABLES `bug_status` WRITE;
/*!40000 ALTER TABLE `bug_status` DISABLE KEYS */;
INSERT INTO `bug_status` VALUES (1,'UNCONFIRMED',100,1,NULL,1),(2,'NEW',200,1,NULL,1),(3,'ASSIGNED',300,1,NULL,1),(4,'REOPENED',400,1,NULL,1),(5,'RESOLVED',500,1,NULL,0),(6,'VERIFIED',600,1,NULL,0),(7,'CLOSED',700,1,NULL,0),(8,'TESTING',333,1,NULL,1),(9,'QA',333,1,NULL,1);
/*!40000 ALTER TABLE `bug_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bugs`
--

DROP TABLE IF EXISTS `bugs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bugs` (
  `bug_id` mediumint(9) NOT NULL AUTO_INCREMENT,
  `assigned_to` mediumint(9) NOT NULL,
  `bug_file_loc` mediumtext,
  `bug_severity` varchar(64) NOT NULL,
  `bug_status` varchar(64) NOT NULL,
  `creation_ts` datetime DEFAULT NULL,
  `delta_ts` datetime NOT NULL,
  `short_desc` varchar(255) NOT NULL,
  `op_sys` varchar(64) NOT NULL,
  `priority` varchar(64) NOT NULL,
  `product_id` smallint(6) NOT NULL,
  `rep_platform` varchar(64) NOT NULL,
  `reporter` mediumint(9) NOT NULL,
  `version` varchar(64) NOT NULL,
  `component_id` smallint(6) NOT NULL,
  `resolution` varchar(64) NOT NULL DEFAULT '',
  `target_milestone` varchar(20) NOT NULL DEFAULT '---',
  `qa_contact` mediumint(9) DEFAULT NULL,
  `status_whiteboard` mediumtext NOT NULL,
  `votes` mediumint(9) NOT NULL DEFAULT '0',
  `keywords` mediumtext NOT NULL,
  `lastdiffed` datetime DEFAULT NULL,
  `everconfirmed` tinyint(4) NOT NULL,
  `reporter_accessible` tinyint(4) NOT NULL DEFAULT '1',
  `cclist_accessible` tinyint(4) NOT NULL DEFAULT '1',
  `estimated_time` decimal(7,2) NOT NULL DEFAULT '0.00',
  `remaining_time` decimal(7,2) NOT NULL DEFAULT '0.00',
  `deadline` datetime DEFAULT NULL,
  `alias` varchar(20) DEFAULT NULL,
  `cf_os` varchar(64) NOT NULL DEFAULT '---',
  PRIMARY KEY (`bug_id`),
  UNIQUE KEY `bugs_alias_idx` (`alias`),
  KEY `bugs_assigned_to_idx` (`assigned_to`),
  KEY `bugs_creation_ts_idx` (`creation_ts`),
  KEY `bugs_delta_ts_idx` (`delta_ts`),
  KEY `bugs_bug_severity_idx` (`bug_severity`),
  KEY `bugs_bug_status_idx` (`bug_status`),
  KEY `bugs_op_sys_idx` (`op_sys`),
  KEY `bugs_priority_idx` (`priority`),
  KEY `bugs_product_id_idx` (`product_id`),
  KEY `bugs_reporter_idx` (`reporter`),
  KEY `bugs_version_idx` (`version`),
  KEY `bugs_component_id_idx` (`component_id`),
  KEY `bugs_resolution_idx` (`resolution`),
  KEY `bugs_target_milestone_idx` (`target_milestone`),
  KEY `bugs_qa_contact_idx` (`qa_contact`),
  KEY `bugs_votes_idx` (`votes`),
  CONSTRAINT `fk_bugs_assigned_to_profiles_userid` FOREIGN KEY (`assigned_to`) REFERENCES `profiles` (`userid`) ON UPDATE CASCADE,
  CONSTRAINT `fk_bugs_component_id_components_id` FOREIGN KEY (`component_id`) REFERENCES `components` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_bugs_product_id_products_id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_bugs_reporter_profiles_userid` FOREIGN KEY (`reporter`) REFERENCES `profiles` (`userid`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bugs`
--

LOCK TABLES `bugs` WRITE;
/*!40000 ALTER TABLE `bugs` DISABLE KEYS */;
INSERT INTO `bugs` VALUES (1,1,'','enhancement','NEW','2010-10-11 15:28:12','2010-11-02 12:05:05','Inital Issue','Linux','---',1,'PC',1,'unspecified',1,'','---',NULL,'',0,'jira, linux','2010-11-02 12:05:05',1,1,1,'0.00','0.00',NULL,NULL,'---'),(2,1,'','critical','NEW','2010-10-11 15:29:03','2010-11-02 13:33:29','UI of BZ is impressive :)','Windows','---',1,'Macintosh',1,'unspecified',1,'','---',NULL,'',0,'','2010-11-02 13:33:29',1,1,1,'40.00','0.00',NULL,NULL,'---'),(3,1,'','enhancement','NEW','2010-10-22 16:10:54','2010-10-27 16:20:29','Wrong bug id that doesn\'t link','Linux','---',1,'PC',2,'unspecified',1,'','---',NULL,'',0,'','2010-10-27 16:20:29',1,1,1,'0.00','0.00',NULL,NULL,'---'),(4,1,'','enhancement','NEW','2010-10-25 11:32:20','2010-10-25 11:39:04','„Slanted quotes in action”','Linux','---',1,'PC',2,'unspecified',1,'','---',NULL,'',0,'','2010-10-25 11:39:04',1,1,1,'0.00','0.00',NULL,NULL,'---'),(5,1,'','enhancement','NEW','2010-10-25 11:39:41','2010-11-08 16:19:04','“Different quotes in title”','Linux','---',1,'PC',2,'unspecified',1,'','---',NULL,'',1,'','2010-11-08 16:19:04',1,1,1,'0.00','0.00',NULL,NULL,'Linux'),(6,1,'','enhancement','RESOLVED','2010-10-29 11:35:54','2010-10-29 11:41:29','Duplicate of 3','Linux','---',1,'PC',2,'unspecified',1,'DUPLICATE','---',NULL,'',0,'','2010-10-29 11:41:29',1,1,1,'0.00','0.00',NULL,NULL,'---'),(7,2,'','enhancement','NEW','2010-11-02 15:12:44','2010-11-09 09:12:17','Issue from invalid component id project','Linux','---',2,'PC',2,'unspecified',2,'','---',NULL,'',0,'','2010-11-09 09:12:17',1,1,1,'0.00','0.00',NULL,NULL,'---'),(8,1,'','enhancement','NEW','2010-11-02 16:39:16','2010-11-04 10:19:34','Very big attachment and a small one','Linux','---',1,'PC',2,'unspecified',1,'','---',NULL,'',0,'','2010-11-04 10:19:34',1,1,1,'0.00','0.00',NULL,NULL,'---'),(9,1,'','enhancement','QA','2010-11-04 13:56:13','2010-11-04 13:56:47','Custom workflow state QA','Linux','---',1,'PC',2,'unspecified',1,'','---',NULL,'',0,'','2010-11-04 13:56:47',1,1,1,'0.00','0.00',NULL,NULL,'---'),(10,1,'','enhancement','TESTING','2010-11-04 13:57:05','2010-11-09 17:42:05','Custom workflow state Testing','Linux','---',1,'PC',2,'unspecified',1,'','---',NULL,'',0,'','2010-11-09 17:42:05',1,1,1,'0.00','0.00',NULL,NULL,'---'),(11,1,'','enhancement','NEW','2010-11-05 11:21:16','2010-11-09 17:42:05','Time tracking','Linux','---',1,'PC',2,'unspecified',1,'','---',NULL,'',0,'','2010-11-09 17:42:05',1,1,1,'340.00','327.00','2011-03-03 00:00:00',NULL,'---'),(12,1,'','normal','NEW','2010-11-08 15:58:34','2010-11-09 17:42:05','wseliga - testing','All','Low',1,'All',2,'1.0',1,'','---',NULL,'',0,'','2010-11-09 17:42:05',1,1,1,'0.00','0.00',NULL,NULL,'---'),(13,1,'','blocker','NEW','2010-11-08 16:03:57','2010-11-09 17:42:05','wseliga - testing 1289228581067','All','High',1,'All',2,'1.0',1,'','M2',NULL,'',0,'','2010-11-09 17:42:06',1,1,1,'0.00','0.00',NULL,NULL,'---'),(14,1,'','blocker','NEW','2010-11-08 16:09:37','2010-11-09 16:05:52','wseliga - testing 1289228921181','All','High',1,'All',2,'1.0',1,'','M2',NULL,'',0,'','2010-11-09 16:05:52',1,1,1,'0.00','0.00',NULL,NULL,'---'),(15,1,'','blocker','NEW','2010-11-08 16:10:48','2010-11-09 17:35:31','wseliga - testing 1289228992756','All','High',1,'All',2,'1.0',1,'','M1',NULL,'',0,'','2010-11-09 17:35:32',1,1,1,'0.00','0.00',NULL,NULL,'---'),(16,1,'','blocker','ASSIGNED','2010-11-08 16:11:45','2010-11-09 16:33:42','wseliga - testing 1289229048910','All','High',1,'All',2,'1.0',1,'','---',NULL,'',0,'','2010-11-09 16:33:42',1,1,1,'0.00','0.00',NULL,NULL,'---'),(17,1,'','blocker','NEW','2010-11-08 16:12:19','2010-11-09 17:35:31','wseliga - testing 1289229082930','All','High',1,'All',2,'1.0',1,'','---',NULL,'',0,'','2010-11-09 17:35:32',1,1,1,'0.00','0.00',NULL,NULL,'---'),(18,2,'','blocker','NEW','2010-11-08 16:17:41','2010-11-08 17:12:37','wseliga - testing 1289229152054','All','High',1,'All',2,'1.0',1,'','---',NULL,'',0,'','2010-11-08 17:12:37',1,1,1,'0.00','0.00',NULL,NULL,'---'),(19,1,'','blocker','NEW','2010-11-08 16:21:06','2010-11-09 16:33:40','wseliga - testing 1289229609501','All','High',1,'All',2,'1.0',1,'','---',NULL,'',0,'','2010-11-09 16:33:42',1,1,1,'0.00','0.00',NULL,NULL,'---'),(20,3,'','blocker','NEW','2010-11-08 16:56:19','2010-11-09 17:42:05','wseliga - testing 1289231723043','All','High',1,'All',2,'1.0',1,'','---',NULL,'',0,'','2010-11-09 17:42:06',1,1,1,'0.00','0.00',NULL,NULL,'---'),(21,3,'','blocker','NEW','2010-11-08 16:59:18','2010-11-08 16:59:18','wseliga - testing 1289231901503','All','High',1,'All',2,'1.0',1,'','---',NULL,'',0,'','2010-11-08 16:59:18',1,1,1,'0.00','0.00',NULL,NULL,'---'),(22,3,'','blocker','ASSIGNED','2010-11-08 17:00:19','2010-11-08 17:01:03','wseliga - testing 1289231962897','All','High',1,'All',2,'1.0',1,'','---',NULL,'',0,'','2010-11-08 17:01:03',1,1,1,'0.00','0.00',NULL,NULL,'---'),(23,3,'','blocker','ASSIGNED','2010-11-08 17:04:35','2010-11-08 17:04:35','wseliga - testing 1289232219344','All','High',1,'All',2,'1.0',1,'','---',NULL,'',0,'','2010-11-08 17:04:35',1,1,1,'0.00','0.00',NULL,NULL,'---'),(24,3,'','blocker','TESTING','2010-11-08 17:05:55','2010-11-08 17:06:23','wseliga - testing 1289232308319','All','High',1,'All',2,'1.0',1,'','---',NULL,'',0,'','2010-11-08 17:06:23',1,1,1,'0.00','0.00',NULL,NULL,'---'),(25,3,'','blocker','ASSIGNED','2010-11-08 17:07:15','2010-11-08 17:07:15','wseliga - testing 1289232376591','All','High',1,'All',2,'1.0',1,'','---',NULL,'',0,'','2010-11-08 17:07:15',1,1,1,'0.00','0.00',NULL,NULL,'---'),(26,3,'','blocker','TESTING','2010-11-08 17:10:27','2010-11-08 17:10:28','wseliga - testing 1289232580547','All','High',1,'All',2,'1.0',1,'','---',NULL,'',0,'','2010-11-08 17:10:28',1,1,1,'0.00','0.00',NULL,NULL,'---'),(27,3,'','blocker','ASSIGNED','2010-11-08 17:16:00','2010-11-08 17:16:00','wseliga - testing 1289232913521','All','High',1,'All',2,'1.0',1,'','---',NULL,'',0,'','2010-11-08 17:16:00',1,1,1,'0.00','0.00',NULL,NULL,'---'),(28,3,'','blocker','TESTING','2010-11-08 17:17:29','2010-11-08 17:17:30','wseliga - testing 1289233002227','All','High',1,'All',2,'1.0',1,'','---',NULL,'',0,'','2010-11-08 17:17:30',1,1,1,'0.00','0.00',NULL,NULL,'---'),(29,3,'','blocker','TESTING','2010-11-08 17:17:57','2010-11-08 17:17:58','wseliga - testing 1289233030490','All','High',1,'All',2,'1.0',1,'','---',NULL,'',0,'','2010-11-08 17:17:58',1,1,1,'0.00','0.00',NULL,NULL,'---'),(30,3,'','blocker','TESTING','2010-11-08 17:21:41','2010-11-08 17:21:42','wseliga - testing 1289233254115','All','High',1,'All',2,'1.0',1,'','---',NULL,'',0,'','2010-11-08 17:21:42',1,1,1,'0.00','0.00',NULL,NULL,'---'),(31,3,'','blocker','TESTING','2010-11-08 17:24:21','2010-11-08 17:24:22','wseliga - testing 1289233414222','All','High',1,'All',2,'1.0',1,'','---',NULL,'',0,'','2010-11-08 17:24:22',1,1,1,'0.00','0.00',NULL,NULL,'---'),(32,3,'','blocker','RESOLVED','2010-11-08 17:24:52','2010-11-08 17:24:55','wseliga - testing 1289233445827','All','High',1,'All',2,'1.0',1,'DUPLICATE','---',NULL,'',0,'','2010-11-08 17:24:55',1,1,1,'0.00','0.00',NULL,NULL,'---'),(33,1,'','enhancement','NEW','2010-11-09 09:12:48','2010-11-09 09:12:48','Issue by disabled user','Linux','---',1,'PC',4,'unspecified',1,'','---',NULL,'',0,'','2010-11-09 09:12:48',1,1,1,'0.00','0.00',NULL,NULL,'---'),(34,3,'','blocker','RESOLVED','2010-11-09 16:08:11','2010-11-09 16:08:13','wseliga - testing 1289315226210','All','High',1,'All',2,'1.0',1,'DUPLICATE','M2',NULL,'',0,'','2010-11-09 16:08:13',1,1,1,'0.00','0.00',NULL,NULL,'---'),(35,3,'','blocker','RESOLVED','2010-11-09 16:29:31','2010-11-09 16:30:27','wseliga - testing 1289316516563','All','High',1,'All',2,'1.0',1,'DUPLICATE','M2',NULL,'',0,'','2010-11-09 16:30:27',1,1,1,'0.00','0.00',NULL,NULL,'---'),(36,3,'','blocker','RESOLVED','2010-11-09 16:33:40','2010-11-09 16:33:43','wseliga - testing 1289316765386','All','High',1,'All',2,'1.0',1,'DUPLICATE','M2',NULL,'',0,'','2010-11-09 16:33:43',1,1,1,'0.00','0.00',NULL,NULL,'---'),(37,7,'','normal','NEW','2010-11-09 17:30:59','2010-11-09 17:30:59','test issue  unassigned ,version: 4.0 ,severity: normal ,priority: Low','All','Low',1,'All',2,'4.0',5,'','---',NULL,'',0,'','2010-11-09 17:30:59',1,1,1,'0.00','0.00',NULL,NULL,'---'),(38,5,'','blocker','NEW','2010-11-09 17:32:55','2010-11-09 17:32:55','test issue  ,without version ,severity: blocker ,priority: Normal','All','Normal',1,'All',2,'unspecified',3,'','---',NULL,'',0,'','2010-11-09 17:32:55',1,1,1,'0.00','0.00',NULL,NULL,'---'),(39,9,'','trivial','NEW','2010-11-09 17:34:10','2010-11-09 17:35:31','test issue  ,assigned to user5@example.com ,version: 2.0 ,severity: trivial ,priority: ---','All','---',1,'All',2,'2.0',4,'','---',NULL,'',0,'','2010-11-09 17:35:31',1,1,1,'0.00','0.00',NULL,NULL,'---'),(40,5,'','critical','ASSIGNED','2010-11-09 17:39:57','2010-11-09 17:39:57','test issue  ,assigned to user1@example.com ,version: 2.0 ,severity: critical ,priority: Lowest','All','Lowest',1,'All',2,'2.0',5,'','---',NULL,'',0,'','2010-11-09 17:39:57',1,1,1,'0.00','0.00',NULL,NULL,'---'),(41,5,'','minor','ASSIGNED','2010-11-09 17:40:15','2010-11-09 17:40:15','test issue  unassigned ,version: 4.0 ,severity: minor ,priority: Normal','All','Normal',1,'All',2,'4.0',3,'','---',NULL,'',0,'','2010-11-09 17:40:15',1,1,1,'0.00','0.00',NULL,NULL,'---'),(42,6,'','normal','NEW','2010-11-09 17:42:05','2010-11-09 17:42:05','test issue  ,assigned to user2@example.com ,version: 1.0 ,severity: normal ,priority: High, depends on: 10, 11, 12, 13, blocks: 20, status: new','All','High',1,'All',2,'1.0',5,'','---',NULL,'',0,'','2010-11-09 17:42:05',1,1,1,'0.00','0.00',NULL,NULL,'---');
/*!40000 ALTER TABLE `bugs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bugs_activity`
--

DROP TABLE IF EXISTS `bugs_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bugs_activity` (
  `bug_id` mediumint(9) NOT NULL,
  `attach_id` mediumint(9) DEFAULT NULL,
  `who` mediumint(9) NOT NULL,
  `bug_when` datetime NOT NULL,
  `fieldid` mediumint(9) NOT NULL,
  `added` varchar(255) DEFAULT NULL,
  `removed` tinytext,
  KEY `bugs_activity_bug_id_idx` (`bug_id`),
  KEY `bugs_activity_who_idx` (`who`),
  KEY `bugs_activity_bug_when_idx` (`bug_when`),
  KEY `bugs_activity_fieldid_idx` (`fieldid`),
  KEY `bugs_activity_added_idx` (`added`),
  KEY `fk_bugs_activity_attach_id_attachments_attach_id` (`attach_id`),
  CONSTRAINT `fk_bugs_activity_attach_id_attachments_attach_id` FOREIGN KEY (`attach_id`) REFERENCES `attachments` (`attach_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_bugs_activity_bug_id_bugs_bug_id` FOREIGN KEY (`bug_id`) REFERENCES `bugs` (`bug_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_bugs_activity_fieldid_fielddefs_id` FOREIGN KEY (`fieldid`) REFERENCES `fielddefs` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_bugs_activity_who_profiles_userid` FOREIGN KEY (`who`) REFERENCES `profiles` (`userid`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bugs_activity`
--

LOCK TABLES `bugs_activity` WRITE;
/*!40000 ALTER TABLE `bugs_activity` DISABLE KEYS */;
INSERT INTO `bugs_activity` VALUES (5,NULL,2,'2010-10-27 14:19:14',20,'pniewiadomski@atlassian.com',''),(3,NULL,2,'2010-10-27 16:20:29',22,'5',''),(2,NULL,2,'2010-10-27 16:20:29',21,'5',''),(5,NULL,2,'2010-10-27 16:20:29',22,'2',''),(5,NULL,2,'2010-10-27 16:20:29',21,'3',''),(5,NULL,2,'2010-10-28 12:21:11',55,'Linux','---'),(6,NULL,2,'2010-10-29 11:41:29',9,'RESOLVED','NEW'),(6,NULL,2,'2010-10-29 11:41:29',12,'DUPLICATE',''),(2,NULL,2,'2010-10-29 11:41:29',20,'pniewiadomski@atlassian.com',''),(1,NULL,2,'2010-11-02 12:05:05',11,'jira, linux',''),(1,NULL,2,'2010-11-02 12:05:05',20,'pniewiadomski@atlassian.com',''),(2,NULL,2,'2010-11-02 12:15:34',11,'jira, linux',''),(2,NULL,2,'2010-11-02 12:51:05',11,'','jira, linux'),(2,NULL,2,'2010-11-02 13:21:31',20,'','pniewiadomski@atlassian.com'),(2,NULL,2,'2010-11-02 13:33:29',47,'4',''),(2,NULL,2,'2010-11-02 13:33:29',20,'pniewiadomski@atlassian.com',''),(2,NULL,2,'2010-11-02 13:33:29',40,'40','0.00'),(9,NULL,2,'2010-11-04 13:56:22',9,'ASSIGNED','NEW'),(9,NULL,2,'2010-11-04 13:56:47',9,'QA','ASSIGNED'),(10,NULL,2,'2010-11-04 13:57:11',9,'ASSIGNED','NEW'),(10,NULL,2,'2010-11-04 13:57:30',9,'TESTING','ASSIGNED'),(11,NULL,2,'2010-11-05 11:21:26',40,'340.0','0.00'),(11,NULL,2,'2010-11-05 11:22:18',47,'4',''),(11,NULL,2,'2010-11-05 11:22:18',42,'2011-03-03',''),(11,NULL,2,'2010-11-05 11:22:18',41,'330','0.00'),(11,NULL,2,'2010-11-05 11:22:58',47,'3',''),(11,NULL,2,'2010-11-05 11:22:58',41,'327','330.00'),(5,NULL,2,'2010-11-08 16:19:04',20,'wseliga@atlassian.com',''),(18,NULL,2,'2010-11-08 16:19:58',20,'piotr.maruszak@spartez.com, pniewiadomski@atlassian.com',''),(18,NULL,2,'2010-11-08 16:24:21',20,'','pniewiadomski@atlassian.com'),(18,NULL,2,'2010-11-08 16:24:26',20,'','wseliga@atlassian.com'),(20,NULL,2,'2010-11-08 16:59:18',22,'21',''),(20,NULL,2,'2010-11-08 17:00:19',22,'22',''),(19,NULL,2,'2010-11-08 17:00:19',21,'22',''),(22,NULL,2,'2010-11-08 17:01:03',9,'ASSIGNED','NEW'),(20,NULL,2,'2010-11-08 17:04:35',22,'23',''),(19,NULL,2,'2010-11-08 17:04:35',21,'23',''),(20,NULL,2,'2010-11-08 17:05:55',22,'24',''),(19,NULL,2,'2010-11-08 17:05:55',21,'24',''),(24,NULL,2,'2010-11-08 17:06:23',9,'TESTING','ASSIGNED'),(20,NULL,2,'2010-11-08 17:07:15',22,'25',''),(19,NULL,2,'2010-11-08 17:07:15',21,'25',''),(20,NULL,2,'2010-11-08 17:10:27',22,'26',''),(19,NULL,2,'2010-11-08 17:10:27',21,'26',''),(26,NULL,2,'2010-11-08 17:10:28',9,'TESTING','ASSIGNED'),(18,NULL,2,'2010-11-08 17:12:06',16,'pniewiadomski@atlassian.com','piotr.maruszak@spartez.com'),(18,NULL,2,'2010-11-08 17:12:37',47,'2',''),(20,NULL,2,'2010-11-08 17:16:00',22,'27',''),(19,NULL,2,'2010-11-08 17:16:00',21,'27',''),(20,NULL,2,'2010-11-08 17:17:29',22,'28',''),(19,NULL,2,'2010-11-08 17:17:29',21,'28',''),(28,NULL,2,'2010-11-08 17:17:30',47,'10',''),(28,NULL,2,'2010-11-08 17:17:30',9,'TESTING','ASSIGNED'),(20,NULL,2,'2010-11-08 17:17:57',22,'29',''),(19,NULL,2,'2010-11-08 17:17:57',21,'29',''),(29,NULL,2,'2010-11-08 17:17:58',47,'10',''),(29,NULL,2,'2010-11-08 17:17:58',9,'TESTING','ASSIGNED'),(20,NULL,2,'2010-11-08 17:21:41',22,'30',''),(19,NULL,2,'2010-11-08 17:21:41',21,'30',''),(30,NULL,2,'2010-11-08 17:21:42',47,'10',''),(30,NULL,2,'2010-11-08 17:21:42',9,'TESTING','ASSIGNED'),(20,NULL,2,'2010-11-08 17:24:21',22,'31',''),(19,NULL,2,'2010-11-08 17:24:21',21,'31',''),(31,NULL,2,'2010-11-08 17:24:22',47,'10',''),(31,NULL,2,'2010-11-08 17:24:22',9,'TESTING','ASSIGNED'),(20,NULL,2,'2010-11-08 17:24:52',22,'32',''),(19,NULL,2,'2010-11-08 17:24:52',21,'32',''),(32,NULL,2,'2010-11-08 17:24:54',47,'10',''),(32,NULL,2,'2010-11-08 17:24:54',9,'TESTING','ASSIGNED'),(32,NULL,2,'2010-11-08 17:24:55',47,'20',''),(32,NULL,2,'2010-11-08 17:24:55',9,'RESOLVED','TESTING'),(32,NULL,2,'2010-11-08 17:24:55',12,'DUPLICATE',''),(7,NULL,4,'2010-11-09 09:12:17',20,'disabled@localhost.localdomain',''),(13,NULL,2,'2010-11-09 16:05:40',30,'M2','---'),(14,NULL,2,'2010-11-09 16:05:52',30,'M2','---'),(15,NULL,2,'2010-11-09 16:06:12',30,'M1','---'),(16,NULL,2,'2010-11-09 16:06:32',9,'ASSIGNED','NEW'),(20,NULL,2,'2010-11-09 16:08:11',22,'34',''),(19,NULL,2,'2010-11-09 16:08:11',21,'34',''),(34,NULL,2,'2010-11-09 16:08:12',47,'10',''),(34,NULL,2,'2010-11-09 16:08:12',9,'TESTING','ASSIGNED'),(34,NULL,2,'2010-11-09 16:08:13',47,'20',''),(34,NULL,2,'2010-11-09 16:08:13',9,'RESOLVED','TESTING'),(34,NULL,2,'2010-11-09 16:08:13',12,'DUPLICATE',''),(34,NULL,2,'2010-11-09 16:08:13',30,'M2','---'),(20,NULL,2,'2010-11-09 16:29:31',22,'35',''),(19,NULL,2,'2010-11-09 16:29:31',21,'35',''),(35,NULL,2,'2010-11-09 16:29:32',47,'10',''),(35,NULL,2,'2010-11-09 16:29:32',9,'TESTING','ASSIGNED'),(35,NULL,2,'2010-11-09 16:29:33',47,'20',''),(35,NULL,2,'2010-11-09 16:29:33',9,'RESOLVED','TESTING'),(35,NULL,2,'2010-11-09 16:29:33',12,'DUPLICATE',''),(35,NULL,2,'2010-11-09 16:29:33',30,'M2','---'),(20,NULL,2,'2010-11-09 16:33:40',22,'36',''),(19,NULL,2,'2010-11-09 16:33:40',21,'36',''),(36,NULL,2,'2010-11-09 16:33:41',47,'10',''),(36,NULL,2,'2010-11-09 16:33:41',9,'TESTING','ASSIGNED'),(36,NULL,2,'2010-11-09 16:33:42',47,'20',''),(36,NULL,2,'2010-11-09 16:33:42',9,'RESOLVED','TESTING'),(36,NULL,2,'2010-11-09 16:33:42',12,'DUPLICATE',''),(36,NULL,2,'2010-11-09 16:33:42',30,'M2','---'),(17,NULL,2,'2010-11-09 17:35:31',22,'39',''),(15,NULL,2,'2010-11-09 17:35:31',22,'39',''),(39,NULL,2,'2010-11-09 17:35:31',21,'17, 15',''),(10,NULL,2,'2010-11-09 17:39:57',22,'40',''),(10,NULL,2,'2010-11-09 17:40:15',22,'41',''),(11,NULL,2,'2010-11-09 17:40:15',22,'41',''),(12,NULL,2,'2010-11-09 17:40:15',22,'41',''),(20,NULL,2,'2010-11-09 17:40:15',21,'41',''),(10,NULL,2,'2010-11-09 17:42:05',22,'42',''),(11,NULL,2,'2010-11-09 17:42:05',22,'42',''),(12,NULL,2,'2010-11-09 17:42:05',22,'42',''),(13,NULL,2,'2010-11-09 17:42:05',22,'42',''),(20,NULL,2,'2010-11-09 17:42:05',21,'42','');
/*!40000 ALTER TABLE `bugs_activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bugs_fulltext`
--

DROP TABLE IF EXISTS `bugs_fulltext`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bugs_fulltext` (
  `bug_id` mediumint(9) NOT NULL,
  `short_desc` varchar(255) NOT NULL,
  `comments` mediumtext,
  `comments_noprivate` mediumtext,
  PRIMARY KEY (`bug_id`),
  FULLTEXT KEY `bugs_fulltext_short_desc_idx` (`short_desc`),
  FULLTEXT KEY `bugs_fulltext_comments_idx` (`comments`),
  FULLTEXT KEY `bugs_fulltext_comments_noprivate_idx` (`comments_noprivate`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bugs_fulltext`
--

LOCK TABLES `bugs_fulltext` WRITE;
/*!40000 ALTER TABLE `bugs_fulltext` DISABLE KEYS */;
INSERT INTO `bugs_fulltext` VALUES (1,'Inital Issue','',''),(2,'UI of BZ is impressive :)','It\'s better to spend some bugs on JIRA :)\n\nChanging estimates','It\'s better to spend some bugs on JIRA :)\n\nChanging estimates'),(3,'Wrong bug id that doesn\'t link','Here\'s a wrong link to a bug.\n\nluck Bug 7777777777777777\n\nHere\'s a good Bug 1','Here\'s a wrong link to a bug.\n\nluck Bug 7777777777777777\n\nHere\'s a good Bug 1'),(4,'„Slanted quotes in action”','Testing „Slanted quotes in action”\n“Different quotes”','Testing „Slanted quotes in action”\n“Different quotes”'),(5,'“Different quotes in title”','“Different quotes in description”','“Different quotes in description”'),(6,'Duplicate of 3','\n','\n'),(7,'Issue from invalid component id project','Let\'s see what happens here\nComment by disabled user','Let\'s see what happens here\nComment by disabled user'),(8,'Very big attachment and a small one','Very big attachment is stored locally in the filesystem.\n\nThe small one is stored in db.\n\n\n','Very big attachment is stored locally in the filesystem.\n\nThe small one is stored in db.\n\n\n'),(9,'Custom workflow state QA','That\'s a custom step','That\'s a custom step'),(10,'Custom workflow state Testing','That\'s a custom step','That\'s a custom step'),(11,'Time tracking','This is an issue having time tracking\nChanging time tracking\nTime tracking','This is an issue having time tracking\nChanging time tracking\nTime tracking'),(12,'wseliga - testing','1289228258556','1289228258556'),(13,'wseliga - testing 1289228581067','1289228581067','1289228581067'),(14,'wseliga - testing 1289228921181','1289228921181\na','1289228921181\na'),(15,'wseliga - testing 1289228992756','1289228992756','1289228992756'),(16,'wseliga - testing 1289229048910','1289229048910\n\n\n\n','1289229048910\n\n\n\n'),(17,'wseliga - testing 1289229082930','1289229082930','1289229082930'),(18,'wseliga - testing 1289229152054','1289229152054\nfsdf','1289229152054\nfsdf'),(19,'wseliga - testing 1289229609501','1289229609501','1289229609501'),(20,'wseliga - testing 1289231723043','1289231723043','1289231723043'),(21,'wseliga - testing 1289231901503','1289231901503','1289231901503'),(22,'wseliga - testing 1289231962897','1289231962897','1289231962897'),(23,'wseliga - testing 1289232219344','1289232219344','1289232219344'),(24,'wseliga - testing 1289232308319','1289232308319','1289232308319'),(25,'wseliga - testing 1289232376591','1289232376591','1289232376591'),(26,'wseliga - testing 1289232580547','1289232580547','1289232580547'),(27,'wseliga - testing 1289232913521','1289232913521','1289232913521'),(28,'wseliga - testing 1289233002227','1289233002227\nmy reason for adding work','1289233002227\nmy reason for adding work'),(29,'wseliga - testing 1289233030490','1289233030490\nmy reason for adding work','1289233030490\nmy reason for adding work'),(30,'wseliga - testing 1289233254115','1289233254115\nmy reason for adding work','1289233254115\nmy reason for adding work'),(31,'wseliga - testing 1289233414222','1289233414222\nmy reason for adding work','1289233414222\nmy reason for adding work'),(32,'wseliga - testing 1289233445827','1289233445827\nmy reason for adding work\nanother reason for adding work','1289233445827\nmy reason for adding work\nanother reason for adding work'),(33,'Issue by disabled user','I\'m disabled','I\'m disabled'),(34,'wseliga - testing 1289315226210','1289315226210\nmy reason for adding work\nanother reason for adding work','1289315226210\nmy reason for adding work\nanother reason for adding work'),(35,'wseliga - testing 1289316516563','1289316516563\nmy reason for adding work\nanother reason for adding work\nabc','1289316516563\nmy reason for adding work\nanother reason for adding work\nabc'),(36,'wseliga - testing 1289316765386','1289316765386\nmy reason for adding work\nanother reason for adding work\nAutomated attachment test','1289316765386\nmy reason for adding work\nanother reason for adding work\nAutomated attachment test'),(37,'test issue  unassigned ,version: 4.0 ,severity: normal ,priority: Low','My descriptionTue Nov 09 17:29:53 CET 2010\ntest issue  unassigned ,version: 4.0 ,severity: normal ,priority: Low','My descriptionTue Nov 09 17:29:53 CET 2010\ntest issue  unassigned ,version: 4.0 ,severity: normal ,priority: Low'),(38,'test issue  ,without version ,severity: blocker ,priority: Normal','My descriptionTue Nov 09 17:31:50 CET 2010\ntest issue  ,without version ,severity: blocker ,priority: Normal','My descriptionTue Nov 09 17:31:50 CET 2010\ntest issue  ,without version ,severity: blocker ,priority: Normal'),(39,'test issue  ,assigned to user5@example.com ,version: 2.0 ,severity: trivial ,priority: ---','My descriptionTue Nov 09 17:33:04 CET 2010\ntest issue  ,assigned to user5@example.com ,version: 2.0 ,severity: trivial ,priority: ---','My descriptionTue Nov 09 17:33:04 CET 2010\ntest issue  ,assigned to user5@example.com ,version: 2.0 ,severity: trivial ,priority: ---'),(40,'test issue  ,assigned to user1@example.com ,version: 2.0 ,severity: critical ,priority: Lowest','My descriptionTue Nov 09 17:38:52 CET 2010\ntest issue  ,assigned to user1@example.com ,version: 2.0 ,severity: critical ,priority: Lowest','My descriptionTue Nov 09 17:38:52 CET 2010\ntest issue  ,assigned to user1@example.com ,version: 2.0 ,severity: critical ,priority: Lowest'),(41,'test issue  unassigned ,version: 4.0 ,severity: minor ,priority: Normal','My descriptionTue Nov 09 17:39:10 CET 2010\ntest issue  unassigned ,version: 4.0 ,severity: minor ,priority: Normal','My descriptionTue Nov 09 17:39:10 CET 2010\ntest issue  unassigned ,version: 4.0 ,severity: minor ,priority: Normal'),(42,'test issue  ,assigned to user2@example.com ,version: 1.0 ,severity: normal ,priority: High, depends on: 10, 11, 12, 13, blocks: 20, status: new','My descriptionTue Nov 09 17:40:58 CET 2010\ntest issue  ,assigned to user2@example.com ,version: 1.0 ,severity: normal ,priority: High, depends on: 10, 11, 12, 13, blocks: 20, status: new','My descriptionTue Nov 09 17:40:58 CET 2010\ntest issue  ,assigned to user2@example.com ,version: 1.0 ,severity: normal ,priority: High, depends on: 10, 11, 12, 13, blocks: 20, status: new');
/*!40000 ALTER TABLE `bugs_fulltext` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bz_schema`
--

DROP TABLE IF EXISTS `bz_schema`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bz_schema` (
  `schema_data` longblob NOT NULL,
  `version` decimal(3,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bz_schema`
--

LOCK TABLES `bz_schema` WRITE;
/*!40000 ALTER TABLE `bz_schema` DISABLE KEYS */;
INSERT INTO `bz_schema` VALUES ('$VAR1 = {\n          \'attach_data\' => {\n                             \'FIELDS\' => [\n                                           \'id\',\n                                           {\n                                             \'NOTNULL\' => 1,\n                                             \'PRIMARYKEY\' => 1,\n                                             \'REFERENCES\' => {\n                                                               \'COLUMN\' => \'attach_id\',\n                                                               \'DELETE\' => \'CASCADE\',\n                                                               \'TABLE\' => \'attachments\'\n                                                             },\n                                             \'TYPE\' => \'INT3\'\n                                           },\n                                           \'thedata\',\n                                           {\n                                             \'NOTNULL\' => 1,\n                                             \'TYPE\' => \'LONGBLOB\'\n                                           }\n                                         ]\n                           },\n          \'attachments\' => {\n                             \'FIELDS\' => [\n                                           \'attach_id\',\n                                           {\n                                             \'NOTNULL\' => 1,\n                                             \'PRIMARYKEY\' => 1,\n                                             \'TYPE\' => \'MEDIUMSERIAL\'\n                                           },\n                                           \'bug_id\',\n                                           {\n                                             \'NOTNULL\' => 1,\n                                             \'REFERENCES\' => {\n                                                               \'COLUMN\' => \'bug_id\',\n                                                               \'DELETE\' => \'CASCADE\',\n                                                               \'TABLE\' => \'bugs\'\n                                                             },\n                                             \'TYPE\' => \'INT3\'\n                                           },\n                                           \'creation_ts\',\n                                           {\n                                             \'NOTNULL\' => 1,\n                                             \'TYPE\' => \'DATETIME\'\n                                           },\n                                           \'modification_time\',\n                                           {\n                                             \'NOTNULL\' => 1,\n                                             \'TYPE\' => \'DATETIME\'\n                                           },\n                                           \'description\',\n                                           {\n                                             \'NOTNULL\' => 1,\n                                             \'TYPE\' => \'TINYTEXT\'\n                                           },\n                                           \'mimetype\',\n                                           {\n                                             \'NOTNULL\' => 1,\n                                             \'TYPE\' => \'TINYTEXT\'\n                                           },\n                                           \'ispatch\',\n                                           {\n                                             \'TYPE\' => \'BOOLEAN\'\n                                           },\n                                           \'filename\',\n                                           {\n                                             \'NOTNULL\' => 1,\n                                             \'TYPE\' => \'varchar(100)\'\n                                           },\n                                           \'submitter_id\',\n                                           {\n                                             \'NOTNULL\' => 1,\n                                             \'REFERENCES\' => {\n                                                               \'COLUMN\' => \'userid\',\n                                                               \'TABLE\' => \'profiles\'\n                                                             },\n                                             \'TYPE\' => \'INT3\'\n                                           },\n                                           \'isobsolete\',\n                                           {\n                                             \'DEFAULT\' => \'FALSE\',\n                                             \'NOTNULL\' => 1,\n                                             \'TYPE\' => \'BOOLEAN\'\n                                           },\n                                           \'isprivate\',\n                                           {\n                                             \'DEFAULT\' => \'FALSE\',\n                                             \'NOTNULL\' => 1,\n                                             \'TYPE\' => \'BOOLEAN\'\n                                           },\n                                           \'isurl\',\n                                           {\n                                             \'DEFAULT\' => \'FALSE\',\n                                             \'NOTNULL\' => 1,\n                                             \'TYPE\' => \'BOOLEAN\'\n                                           }\n                                         ],\n                             \'INDEXES\' => [\n                                            \'attachments_bug_id_idx\',\n                                            [\n                                              \'bug_id\'\n                                            ],\n                                            \'attachments_creation_ts_idx\',\n                                            [\n                                              \'creation_ts\'\n                                            ],\n                                            \'attachments_modification_time_idx\',\n                                            [\n                                              \'modification_time\'\n                                            ],\n                                            \'attachments_submitter_id_idx\',\n                                            [\n                                              \'submitter_id\',\n                                              \'bug_id\'\n                                            ]\n                                          ]\n                           },\n          \'bug_group_map\' => {\n                               \'FIELDS\' => [\n                                             \'bug_id\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'REFERENCES\' => {\n                                                                 \'COLUMN\' => \'bug_id\',\n                                                                 \'DELETE\' => \'CASCADE\',\n                                                                 \'TABLE\' => \'bugs\'\n                                                               },\n                                               \'TYPE\' => \'INT3\'\n                                             },\n                                             \'group_id\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'REFERENCES\' => {\n                                                                 \'COLUMN\' => \'id\',\n                                                                 \'DELETE\' => \'CASCADE\',\n                                                                 \'TABLE\' => \'groups\'\n                                                               },\n                                               \'TYPE\' => \'INT3\'\n                                             }\n                                           ],\n                               \'INDEXES\' => [\n                                              \'bug_group_map_bug_id_idx\',\n                                              {\n                                                \'FIELDS\' => [\n                                                              \'bug_id\',\n                                                              \'group_id\'\n                                                            ],\n                                                \'TYPE\' => \'UNIQUE\'\n                                              },\n                                              \'bug_group_map_group_id_idx\',\n                                              [\n                                                \'group_id\'\n                                              ]\n                                            ]\n                             },\n          \'bug_see_also\' => {\n                              \'FIELDS\' => [\n                                            \'bug_id\',\n                                            {\n                                              \'NOTNULL\' => 1,\n                                              \'TYPE\' => \'INT3\'\n                                            },\n                                            \'value\',\n                                            {\n                                              \'NOTNULL\' => 1,\n                                              \'TYPE\' => \'varchar(255)\'\n                                            }\n                                          ],\n                              \'INDEXES\' => [\n                                             \'bug_see_also_bug_id_idx\',\n                                             {\n                                               \'FIELDS\' => [\n                                                             \'bug_id\',\n                                                             \'value\'\n                                                           ],\n                                               \'TYPE\' => \'UNIQUE\'\n                                             }\n                                           ]\n                            },\n          \'bug_severity\' => {\n                              \'FIELDS\' => [\n                                            \'id\',\n                                            {\n                                              \'NOTNULL\' => 1,\n                                              \'PRIMARYKEY\' => 1,\n                                              \'TYPE\' => \'SMALLSERIAL\'\n                                            },\n                                            \'value\',\n                                            {\n                                              \'NOTNULL\' => 1,\n                                              \'TYPE\' => \'varchar(64)\'\n                                            },\n                                            \'sortkey\',\n                                            {\n                                              \'DEFAULT\' => 0,\n                                              \'NOTNULL\' => 1,\n                                              \'TYPE\' => \'INT2\'\n                                            },\n                                            \'isactive\',\n                                            {\n                                              \'DEFAULT\' => \'TRUE\',\n                                              \'NOTNULL\' => 1,\n                                              \'TYPE\' => \'BOOLEAN\'\n                                            },\n                                            \'visibility_value_id\',\n                                            {\n                                              \'TYPE\' => \'INT2\'\n                                            }\n                                          ],\n                              \'INDEXES\' => [\n                                             \'bug_severity_value_idx\',\n                                             {\n                                               \'FIELDS\' => [\n                                                             \'value\'\n                                                           ],\n                                               \'TYPE\' => \'UNIQUE\'\n                                             },\n                                             \'bug_severity_sortkey_idx\',\n                                             [\n                                               \'sortkey\',\n                                               \'value\'\n                                             ],\n                                             \'bug_severity_visibility_value_id_idx\',\n                                             [\n                                               \'visibility_value_id\'\n                                             ]\n                                           ]\n                            },\n          \'bug_status\' => {\n                            \'FIELDS\' => [\n                                          \'id\',\n                                          {\n                                            \'NOTNULL\' => 1,\n                                            \'PRIMARYKEY\' => 1,\n                                            \'TYPE\' => \'SMALLSERIAL\'\n                                          },\n                                          \'value\',\n                                          {\n                                            \'NOTNULL\' => 1,\n                                            \'TYPE\' => \'varchar(64)\'\n                                          },\n                                          \'sortkey\',\n                                          {\n                                            \'DEFAULT\' => 0,\n                                            \'NOTNULL\' => 1,\n                                            \'TYPE\' => \'INT2\'\n                                          },\n                                          \'isactive\',\n                                          {\n                                            \'DEFAULT\' => \'TRUE\',\n                                            \'NOTNULL\' => 1,\n                                            \'TYPE\' => \'BOOLEAN\'\n                                          },\n                                          \'visibility_value_id\',\n                                          {\n                                            \'TYPE\' => \'INT2\'\n                                          },\n                                          \'is_open\',\n                                          {\n                                            \'DEFAULT\' => \'TRUE\',\n                                            \'NOTNULL\' => 1,\n                                            \'TYPE\' => \'BOOLEAN\'\n                                          }\n                                        ],\n                            \'INDEXES\' => [\n                                           \'bug_status_value_idx\',\n                                           {\n                                             \'FIELDS\' => [\n                                                           \'value\'\n                                                         ],\n                                             \'TYPE\' => \'UNIQUE\'\n                                           },\n                                           \'bug_status_sortkey_idx\',\n                                           [\n                                             \'sortkey\',\n                                             \'value\'\n                                           ],\n                                           \'bug_status_visibility_value_id_idx\',\n                                           [\n                                             \'visibility_value_id\'\n                                           ]\n                                         ]\n                          },\n          \'bugs\' => {\n                      \'FIELDS\' => [\n                                    \'bug_id\',\n                                    {\n                                      \'NOTNULL\' => 1,\n                                      \'PRIMARYKEY\' => 1,\n                                      \'TYPE\' => \'MEDIUMSERIAL\'\n                                    },\n                                    \'assigned_to\',\n                                    {\n                                      \'NOTNULL\' => 1,\n                                      \'REFERENCES\' => {\n                                                        \'COLUMN\' => \'userid\',\n                                                        \'TABLE\' => \'profiles\'\n                                                      },\n                                      \'TYPE\' => \'INT3\'\n                                    },\n                                    \'bug_file_loc\',\n                                    {\n                                      \'TYPE\' => \'MEDIUMTEXT\'\n                                    },\n                                    \'bug_severity\',\n                                    {\n                                      \'NOTNULL\' => 1,\n                                      \'TYPE\' => \'varchar(64)\'\n                                    },\n                                    \'bug_status\',\n                                    {\n                                      \'NOTNULL\' => 1,\n                                      \'TYPE\' => \'varchar(64)\'\n                                    },\n                                    \'creation_ts\',\n                                    {\n                                      \'TYPE\' => \'DATETIME\'\n                                    },\n                                    \'delta_ts\',\n                                    {\n                                      \'NOTNULL\' => 1,\n                                      \'TYPE\' => \'DATETIME\'\n                                    },\n                                    \'short_desc\',\n                                    {\n                                      \'NOTNULL\' => 1,\n                                      \'TYPE\' => \'varchar(255)\'\n                                    },\n                                    \'op_sys\',\n                                    {\n                                      \'NOTNULL\' => 1,\n                                      \'TYPE\' => \'varchar(64)\'\n                                    },\n                                    \'priority\',\n                                    {\n                                      \'NOTNULL\' => 1,\n                                      \'TYPE\' => \'varchar(64)\'\n                                    },\n                                    \'product_id\',\n                                    {\n                                      \'NOTNULL\' => 1,\n                                      \'REFERENCES\' => {\n                                                        \'COLUMN\' => \'id\',\n                                                        \'TABLE\' => \'products\'\n                                                      },\n                                      \'TYPE\' => \'INT2\'\n                                    },\n                                    \'rep_platform\',\n                                    {\n                                      \'NOTNULL\' => 1,\n                                      \'TYPE\' => \'varchar(64)\'\n                                    },\n                                    \'reporter\',\n                                    {\n                                      \'NOTNULL\' => 1,\n                                      \'REFERENCES\' => {\n                                                        \'COLUMN\' => \'userid\',\n                                                        \'TABLE\' => \'profiles\'\n                                                      },\n                                      \'TYPE\' => \'INT3\'\n                                    },\n                                    \'version\',\n                                    {\n                                      \'NOTNULL\' => 1,\n                                      \'TYPE\' => \'varchar(64)\'\n                                    },\n                                    \'component_id\',\n                                    {\n                                      \'NOTNULL\' => 1,\n                                      \'REFERENCES\' => {\n                                                        \'COLUMN\' => \'id\',\n                                                        \'TABLE\' => \'components\'\n                                                      },\n                                      \'TYPE\' => \'INT2\'\n                                    },\n                                    \'resolution\',\n                                    {\n                                      \'DEFAULT\' => \'\\\'\\\'\',\n                                      \'NOTNULL\' => 1,\n                                      \'TYPE\' => \'varchar(64)\'\n                                    },\n                                    \'target_milestone\',\n                                    {\n                                      \'DEFAULT\' => \'\\\'---\\\'\',\n                                      \'NOTNULL\' => 1,\n                                      \'TYPE\' => \'varchar(20)\'\n                                    },\n                                    \'qa_contact\',\n                                    {\n                                      \'REERENCES\' => {\n                                                       \'COLUMN\' => \'userid\',\n                                                       \'TABLE\' => \'profiles\'\n                                                     },\n                                      \'TYPE\' => \'INT3\'\n                                    },\n                                    \'status_whiteboard\',\n                                    {\n                                      \'DEFAULT\' => \'\\\'\\\'\',\n                                      \'NOTNULL\' => 1,\n                                      \'TYPE\' => \'MEDIUMTEXT\'\n                                    },\n                                    \'votes\',\n                                    {\n                                      \'DEFAULT\' => \'0\',\n                                      \'NOTNULL\' => 1,\n                                      \'TYPE\' => \'INT3\'\n                                    },\n                                    \'keywords\',\n                                    {\n                                      \'DEFAULT\' => \'\\\'\\\'\',\n                                      \'NOTNULL\' => 1,\n                                      \'TYPE\' => \'MEDIUMTEXT\'\n                                    },\n                                    \'lastdiffed\',\n                                    {\n                                      \'TYPE\' => \'DATETIME\'\n                                    },\n                                    \'everconfirmed\',\n                                    {\n                                      \'NOTNULL\' => 1,\n                                      \'TYPE\' => \'BOOLEAN\'\n                                    },\n                                    \'reporter_accessible\',\n                                    {\n                                      \'DEFAULT\' => \'TRUE\',\n                                      \'NOTNULL\' => 1,\n                                      \'TYPE\' => \'BOOLEAN\'\n                                    },\n                                    \'cclist_accessible\',\n                                    {\n                                      \'DEFAULT\' => \'TRUE\',\n                                      \'NOTNULL\' => 1,\n                                      \'TYPE\' => \'BOOLEAN\'\n                                    },\n                                    \'estimated_time\',\n                                    {\n                                      \'DEFAULT\' => \'0\',\n                                      \'NOTNULL\' => 1,\n                                      \'TYPE\' => \'decimal(7,2)\'\n                                    },\n                                    \'remaining_time\',\n                                    {\n                                      \'DEFAULT\' => \'0\',\n                                      \'NOTNULL\' => 1,\n                                      \'TYPE\' => \'decimal(7,2)\'\n                                    },\n                                    \'deadline\',\n                                    {\n                                      \'TYPE\' => \'DATETIME\'\n                                    },\n                                    \'alias\',\n                                    {\n                                      \'TYPE\' => \'varchar(20)\'\n                                    },\n                                    \'cf_os\',\n                                    {\n                                      \'DEFAULT\' => \'\\\'---\\\'\',\n                                      \'NOTNULL\' => 1,\n                                      \'TYPE\' => \'varchar(64)\'\n                                    }\n                                  ],\n                      \'INDEXES\' => [\n                                     \'bugs_alias_idx\',\n                                     {\n                                       \'FIELDS\' => [\n                                                     \'alias\'\n                                                   ],\n                                       \'TYPE\' => \'UNIQUE\'\n                                     },\n                                     \'bugs_assigned_to_idx\',\n                                     [\n                                       \'assigned_to\'\n                                     ],\n                                     \'bugs_creation_ts_idx\',\n                                     [\n                                       \'creation_ts\'\n                                     ],\n                                     \'bugs_delta_ts_idx\',\n                                     [\n                                       \'delta_ts\'\n                                     ],\n                                     \'bugs_bug_severity_idx\',\n                                     [\n                                       \'bug_severity\'\n                                     ],\n                                     \'bugs_bug_status_idx\',\n                                     [\n                                       \'bug_status\'\n                                     ],\n                                     \'bugs_op_sys_idx\',\n                                     [\n                                       \'op_sys\'\n                                     ],\n                                     \'bugs_priority_idx\',\n                                     [\n                                       \'priority\'\n                                     ],\n                                     \'bugs_product_id_idx\',\n                                     [\n                                       \'product_id\'\n                                     ],\n                                     \'bugs_reporter_idx\',\n                                     [\n                                       \'reporter\'\n                                     ],\n                                     \'bugs_version_idx\',\n                                     [\n                                       \'version\'\n                                     ],\n                                     \'bugs_component_id_idx\',\n                                     [\n                                       \'component_id\'\n                                     ],\n                                     \'bugs_resolution_idx\',\n                                     [\n                                       \'resolution\'\n                                     ],\n                                     \'bugs_target_milestone_idx\',\n                                     [\n                                       \'target_milestone\'\n                                     ],\n                                     \'bugs_qa_contact_idx\',\n                                     [\n                                       \'qa_contact\'\n                                     ],\n                                     \'bugs_votes_idx\',\n                                     [\n                                       \'votes\'\n                                     ]\n                                   ]\n                    },\n          \'bugs_activity\' => {\n                               \'FIELDS\' => [\n                                             \'bug_id\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'REFERENCES\' => {\n                                                                 \'COLUMN\' => \'bug_id\',\n                                                                 \'DELETE\' => \'CASCADE\',\n                                                                 \'TABLE\' => \'bugs\'\n                                                               },\n                                               \'TYPE\' => \'INT3\'\n                                             },\n                                             \'attach_id\',\n                                             {\n                                               \'REFERENCES\' => {\n                                                                 \'COLUMN\' => \'attach_id\',\n                                                                 \'DELETE\' => \'CASCADE\',\n                                                                 \'TABLE\' => \'attachments\'\n                                                               },\n                                               \'TYPE\' => \'INT3\'\n                                             },\n                                             \'who\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'REFERENCES\' => {\n                                                                 \'COLUMN\' => \'userid\',\n                                                                 \'TABLE\' => \'profiles\'\n                                                               },\n                                               \'TYPE\' => \'INT3\'\n                                             },\n                                             \'bug_when\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'TYPE\' => \'DATETIME\'\n                                             },\n                                             \'fieldid\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'REFERENCES\' => {\n                                                                 \'COLUMN\' => \'id\',\n                                                                 \'TABLE\' => \'fielddefs\'\n                                                               },\n                                               \'TYPE\' => \'INT3\'\n                                             },\n                                             \'added\',\n                                             {\n                                               \'TYPE\' => \'varchar(255)\'\n                                             },\n                                             \'removed\',\n                                             {\n                                               \'TYPE\' => \'TINYTEXT\'\n                                             }\n                                           ],\n                               \'INDEXES\' => [\n                                              \'bugs_activity_bug_id_idx\',\n                                              [\n                                                \'bug_id\'\n                                              ],\n                                              \'bugs_activity_who_idx\',\n                                              [\n                                                \'who\'\n                                              ],\n                                              \'bugs_activity_bug_when_idx\',\n                                              [\n                                                \'bug_when\'\n                                              ],\n                                              \'bugs_activity_fieldid_idx\',\n                                              [\n                                                \'fieldid\'\n                                              ],\n                                              \'bugs_activity_added_idx\',\n                                              [\n                                                \'added\'\n                                              ]\n                                            ]\n                             },\n          \'bugs_fulltext\' => {\n                               \'FIELDS\' => [\n                                             \'bug_id\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'PRIMARYKEY\' => 1,\n                                               \'REFERENCES\' => {\n                                                                 \'COLUMN\' => \'bug_id\',\n                                                                 \'DELETE\' => \'CASCADE\',\n                                                                 \'TABLE\' => \'bugs\'\n                                                               },\n                                               \'TYPE\' => \'INT3\'\n                                             },\n                                             \'short_desc\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'TYPE\' => \'varchar(255)\'\n                                             },\n                                             \'comments\',\n                                             {\n                                               \'TYPE\' => \'LONGTEXT\'\n                                             },\n                                             \'comments_noprivate\',\n                                             {\n                                               \'TYPE\' => \'LONGTEXT\'\n                                             }\n                                           ],\n                               \'INDEXES\' => [\n                                              \'bugs_fulltext_short_desc_idx\',\n                                              {\n                                                \'FIELDS\' => [\n                                                              \'short_desc\'\n                                                            ],\n                                                \'TYPE\' => \'FULLTEXT\'\n                                              },\n                                              \'bugs_fulltext_comments_idx\',\n                                              {\n                                                \'FIELDS\' => [\n                                                              \'comments\'\n                                                            ],\n                                                \'TYPE\' => \'FULLTEXT\'\n                                              },\n                                              \'bugs_fulltext_comments_noprivate_idx\',\n                                              {\n                                                \'FIELDS\' => [\n                                                              \'comments_noprivate\'\n                                                            ],\n                                                \'TYPE\' => \'FULLTEXT\'\n                                              }\n                                            ]\n                             },\n          \'bz_schema\' => {\n                           \'FIELDS\' => [\n                                         \'schema_data\',\n                                         {\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'LONGBLOB\'\n                                         },\n                                         \'version\',\n                                         {\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'decimal(3,2)\'\n                                         }\n                                       ]\n                         },\n          \'category_group_map\' => {\n                                    \'FIELDS\' => [\n                                                  \'category_id\',\n                                                  {\n                                                    \'NOTNULL\' => 1,\n                                                    \'REFERENCES\' => {\n                                                                      \'COLUMN\' => \'id\',\n                                                                      \'DELETE\' => \'CASCADE\',\n                                                                      \'TABLE\' => \'series_categories\'\n                                                                    },\n                                                    \'TYPE\' => \'INT2\'\n                                                  },\n                                                  \'group_id\',\n                                                  {\n                                                    \'NOTNULL\' => 1,\n                                                    \'REFERENCES\' => {\n                                                                      \'COLUMN\' => \'id\',\n                                                                      \'DELETE\' => \'CASCADE\',\n                                                                      \'TABLE\' => \'groups\'\n                                                                    },\n                                                    \'TYPE\' => \'INT3\'\n                                                  }\n                                                ],\n                                    \'INDEXES\' => [\n                                                   \'category_group_map_category_id_idx\',\n                                                   {\n                                                     \'FIELDS\' => [\n                                                                   \'category_id\',\n                                                                   \'group_id\'\n                                                                 ],\n                                                     \'TYPE\' => \'UNIQUE\'\n                                                   }\n                                                 ]\n                                  },\n          \'cc\' => {\n                    \'FIELDS\' => [\n                                  \'bug_id\',\n                                  {\n                                    \'NOTNULL\' => 1,\n                                    \'REFERENCES\' => {\n                                                      \'COLUMN\' => \'bug_id\',\n                                                      \'DELETE\' => \'CASCADE\',\n                                                      \'TABLE\' => \'bugs\'\n                                                    },\n                                    \'TYPE\' => \'INT3\'\n                                  },\n                                  \'who\',\n                                  {\n                                    \'NOTNULL\' => 1,\n                                    \'REFERENCES\' => {\n                                                      \'COLUMN\' => \'userid\',\n                                                      \'DELETE\' => \'CASCADE\',\n                                                      \'TABLE\' => \'profiles\'\n                                                    },\n                                    \'TYPE\' => \'INT3\'\n                                  }\n                                ],\n                    \'INDEXES\' => [\n                                   \'cc_bug_id_idx\',\n                                   {\n                                     \'FIELDS\' => [\n                                                   \'bug_id\',\n                                                   \'who\'\n                                                 ],\n                                     \'TYPE\' => \'UNIQUE\'\n                                   },\n                                   \'cc_who_idx\',\n                                   [\n                                     \'who\'\n                                   ]\n                                 ]\n                  },\n          \'cf_os\' => {\n                       \'FIELDS\' => [\n                                     \'id\',\n                                     {\n                                       \'NOTNULL\' => 1,\n                                       \'PRIMARYKEY\' => 1,\n                                       \'TYPE\' => \'SMALLSERIAL\'\n                                     },\n                                     \'value\',\n                                     {\n                                       \'NOTNULL\' => 1,\n                                       \'TYPE\' => \'varchar(64)\'\n                                     },\n                                     \'sortkey\',\n                                     {\n                                       \'DEFAULT\' => 0,\n                                       \'NOTNULL\' => 1,\n                                       \'TYPE\' => \'INT2\'\n                                     },\n                                     \'isactive\',\n                                     {\n                                       \'DEFAULT\' => \'TRUE\',\n                                       \'NOTNULL\' => 1,\n                                       \'TYPE\' => \'BOOLEAN\'\n                                     },\n                                     \'visibility_value_id\',\n                                     {\n                                       \'TYPE\' => \'INT2\'\n                                     }\n                                   ],\n                       \'INDEXES\' => [\n                                      \'cf_os_sortkey_idx\',\n                                      [\n                                        \'sortkey\',\n                                        \'value\'\n                                      ],\n                                      \'cf_os_visibility_value_id_idx\',\n                                      [\n                                        \'visibility_value_id\'\n                                      ],\n                                      \'cf_os_value_idx\',\n                                      {\n                                        \'FIELDS\' => [\n                                                      \'value\'\n                                                    ],\n                                        \'TYPE\' => \'UNIQUE\'\n                                      }\n                                    ]\n                     },\n          \'classifications\' => {\n                                 \'FIELDS\' => [\n                                               \'id\',\n                                               {\n                                                 \'NOTNULL\' => 1,\n                                                 \'PRIMARYKEY\' => 1,\n                                                 \'TYPE\' => \'SMALLSERIAL\'\n                                               },\n                                               \'name\',\n                                               {\n                                                 \'NOTNULL\' => 1,\n                                                 \'TYPE\' => \'varchar(64)\'\n                                               },\n                                               \'description\',\n                                               {\n                                                 \'TYPE\' => \'MEDIUMTEXT\'\n                                               },\n                                               \'sortkey\',\n                                               {\n                                                 \'DEFAULT\' => \'0\',\n                                                 \'NOTNULL\' => 1,\n                                                 \'TYPE\' => \'INT2\'\n                                               }\n                                             ],\n                                 \'INDEXES\' => [\n                                                \'classifications_name_idx\',\n                                                {\n                                                  \'FIELDS\' => [\n                                                                \'name\'\n                                                              ],\n                                                  \'TYPE\' => \'UNIQUE\'\n                                                }\n                                              ]\n                               },\n          \'component_cc\' => {\n                              \'FIELDS\' => [\n                                            \'user_id\',\n                                            {\n                                              \'NOTNULL\' => 1,\n                                              \'REFERENCES\' => {\n                                                                \'COLUMN\' => \'userid\',\n                                                                \'DELETE\' => \'CASCADE\',\n                                                                \'TABLE\' => \'profiles\'\n                                                              },\n                                              \'TYPE\' => \'INT3\'\n                                            },\n                                            \'component_id\',\n                                            {\n                                              \'NOTNULL\' => 1,\n                                              \'REFERENCES\' => {\n                                                                \'COLUMN\' => \'id\',\n                                                                \'DELETE\' => \'CASCADE\',\n                                                                \'TABLE\' => \'components\'\n                                                              },\n                                              \'TYPE\' => \'INT2\'\n                                            }\n                                          ],\n                              \'INDEXES\' => [\n                                             \'component_cc_user_id_idx\',\n                                             {\n                                               \'FIELDS\' => [\n                                                             \'component_id\',\n                                                             \'user_id\'\n                                                           ],\n                                               \'TYPE\' => \'UNIQUE\'\n                                             }\n                                           ]\n                            },\n          \'components\' => {\n                            \'FIELDS\' => [\n                                          \'id\',\n                                          {\n                                            \'NOTNULL\' => 1,\n                                            \'PRIMARYKEY\' => 1,\n                                            \'TYPE\' => \'SMALLSERIAL\'\n                                          },\n                                          \'name\',\n                                          {\n                                            \'NOTNULL\' => 1,\n                                            \'TYPE\' => \'varchar(64)\'\n                                          },\n                                          \'product_id\',\n                                          {\n                                            \'NOTNULL\' => 1,\n                                            \'REFERENCES\' => {\n                                                              \'COLUMN\' => \'id\',\n                                                              \'DELETE\' => \'CASCADE\',\n                                                              \'TABLE\' => \'products\'\n                                                            },\n                                            \'TYPE\' => \'INT2\'\n                                          },\n                                          \'initialowner\',\n                                          {\n                                            \'NOTNULL\' => 1,\n                                            \'REFERENCES\' => {\n                                                              \'COLUMN\' => \'userid\',\n                                                              \'TABLE\' => \'profiles\'\n                                                            },\n                                            \'TYPE\' => \'INT3\'\n                                          },\n                                          \'initialqacontact\',\n                                          {\n                                            \'REFERENCES\' => {\n                                                              \'COLUMN\' => \'userid\',\n                                                              \'DELETE\' => \'SET NULL\',\n                                                              \'TABLE\' => \'profiles\'\n                                                            },\n                                            \'TYPE\' => \'INT3\'\n                                          },\n                                          \'description\',\n                                          {\n                                            \'NOTNULL\' => 1,\n                                            \'TYPE\' => \'MEDIUMTEXT\'\n                                          }\n                                        ],\n                            \'INDEXES\' => [\n                                           \'components_product_id_idx\',\n                                           {\n                                             \'FIELDS\' => [\n                                                           \'product_id\',\n                                                           \'name\'\n                                                         ],\n                                             \'TYPE\' => \'UNIQUE\'\n                                           },\n                                           \'components_name_idx\',\n                                           [\n                                             \'name\'\n                                           ]\n                                         ]\n                          },\n          \'dependencies\' => {\n                              \'FIELDS\' => [\n                                            \'blocked\',\n                                            {\n                                              \'NOTNULL\' => 1,\n                                              \'REFERENCES\' => {\n                                                                \'COLUMN\' => \'bug_id\',\n                                                                \'DELETE\' => \'CASCADE\',\n                                                                \'TABLE\' => \'bugs\'\n                                                              },\n                                              \'TYPE\' => \'INT3\'\n                                            },\n                                            \'dependson\',\n                                            {\n                                              \'NOTNULL\' => 1,\n                                              \'REFERENCES\' => {\n                                                                \'COLUMN\' => \'bug_id\',\n                                                                \'DELETE\' => \'CASCADE\',\n                                                                \'TABLE\' => \'bugs\'\n                                                              },\n                                              \'TYPE\' => \'INT3\'\n                                            }\n                                          ],\n                              \'INDEXES\' => [\n                                             \'dependencies_blocked_idx\',\n                                             [\n                                               \'blocked\'\n                                             ],\n                                             \'dependencies_dependson_idx\',\n                                             [\n                                               \'dependson\'\n                                             ]\n                                           ]\n                            },\n          \'duplicates\' => {\n                            \'FIELDS\' => [\n                                          \'dupe_of\',\n                                          {\n                                            \'NOTNULL\' => 1,\n                                            \'REFERENCES\' => {\n                                                              \'COLUMN\' => \'bug_id\',\n                                                              \'DELETE\' => \'CASCADE\',\n                                                              \'TABLE\' => \'bugs\'\n                                                            },\n                                            \'TYPE\' => \'INT3\'\n                                          },\n                                          \'dupe\',\n                                          {\n                                            \'NOTNULL\' => 1,\n                                            \'PRIMARYKEY\' => 1,\n                                            \'REFERENCES\' => {\n                                                              \'COLUMN\' => \'bug_id\',\n                                                              \'DELETE\' => \'CASCADE\',\n                                                              \'TABLE\' => \'bugs\'\n                                                            },\n                                            \'TYPE\' => \'INT3\'\n                                          }\n                                        ]\n                          },\n          \'email_setting\' => {\n                               \'FIELDS\' => [\n                                             \'user_id\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'REFERENCES\' => {\n                                                                 \'COLUMN\' => \'userid\',\n                                                                 \'DELETE\' => \'CASCADE\',\n                                                                 \'TABLE\' => \'profiles\'\n                                                               },\n                                               \'TYPE\' => \'INT3\'\n                                             },\n                                             \'relationship\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'TYPE\' => \'INT1\'\n                                             },\n                                             \'event\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'TYPE\' => \'INT1\'\n                                             }\n                                           ],\n                               \'INDEXES\' => [\n                                              \'email_setting_user_id_idx\',\n                                              {\n                                                \'FIELDS\' => [\n                                                              \'user_id\',\n                                                              \'relationship\',\n                                                              \'event\'\n                                                            ],\n                                                \'TYPE\' => \'UNIQUE\'\n                                              }\n                                            ]\n                             },\n          \'fielddefs\' => {\n                           \'FIELDS\' => [\n                                         \'id\',\n                                         {\n                                           \'NOTNULL\' => 1,\n                                           \'PRIMARYKEY\' => 1,\n                                           \'TYPE\' => \'MEDIUMSERIAL\'\n                                         },\n                                         \'name\',\n                                         {\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'varchar(64)\'\n                                         },\n                                         \'type\',\n                                         {\n                                           \'DEFAULT\' => 0,\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'INT2\'\n                                         },\n                                         \'custom\',\n                                         {\n                                           \'DEFAULT\' => \'FALSE\',\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'BOOLEAN\'\n                                         },\n                                         \'description\',\n                                         {\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'TINYTEXT\'\n                                         },\n                                         \'mailhead\',\n                                         {\n                                           \'DEFAULT\' => \'FALSE\',\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'BOOLEAN\'\n                                         },\n                                         \'sortkey\',\n                                         {\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'INT2\'\n                                         },\n                                         \'obsolete\',\n                                         {\n                                           \'DEFAULT\' => \'FALSE\',\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'BOOLEAN\'\n                                         },\n                                         \'enter_bug\',\n                                         {\n                                           \'DEFAULT\' => \'FALSE\',\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'BOOLEAN\'\n                                         },\n                                         \'buglist\',\n                                         {\n                                           \'DEFAULT\' => \'FALSE\',\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'BOOLEAN\'\n                                         },\n                                         \'visibility_field_id\',\n                                         {\n                                           \'REFERENCES\' => {\n                                                             \'COLUMN\' => \'id\',\n                                                             \'TABLE\' => \'fielddefs\'\n                                                           },\n                                           \'TYPE\' => \'INT3\'\n                                         },\n                                         \'visibility_value_id\',\n                                         {\n                                           \'TYPE\' => \'INT2\'\n                                         },\n                                         \'value_field_id\',\n                                         {\n                                           \'REFERENCES\' => {\n                                                             \'COLUMN\' => \'id\',\n                                                             \'TABLE\' => \'fielddefs\'\n                                                           },\n                                           \'TYPE\' => \'INT3\'\n                                         }\n                                       ],\n                           \'INDEXES\' => [\n                                          \'fielddefs_name_idx\',\n                                          {\n                                            \'FIELDS\' => [\n                                                          \'name\'\n                                                        ],\n                                            \'TYPE\' => \'UNIQUE\'\n                                          },\n                                          \'fielddefs_sortkey_idx\',\n                                          [\n                                            \'sortkey\'\n                                          ],\n                                          \'fielddefs_value_field_id_idx\',\n                                          [\n                                            \'value_field_id\'\n                                          ]\n                                        ]\n                         },\n          \'flagexclusions\' => {\n                                \'FIELDS\' => [\n                                              \'type_id\',\n                                              {\n                                                \'NOTNULL\' => 1,\n                                                \'REFERENCES\' => {\n                                                                  \'COLUMN\' => \'id\',\n                                                                  \'DELETE\' => \'CASCADE\',\n                                                                  \'TABLE\' => \'flagtypes\'\n                                                                },\n                                                \'TYPE\' => \'INT2\'\n                                              },\n                                              \'product_id\',\n                                              {\n                                                \'REFERENCES\' => {\n                                                                  \'COLUMN\' => \'id\',\n                                                                  \'DELETE\' => \'CASCADE\',\n                                                                  \'TABLE\' => \'products\'\n                                                                },\n                                                \'TYPE\' => \'INT2\'\n                                              },\n                                              \'component_id\',\n                                              {\n                                                \'REFERENCES\' => {\n                                                                  \'COLUMN\' => \'id\',\n                                                                  \'DELETE\' => \'CASCADE\',\n                                                                  \'TABLE\' => \'components\'\n                                                                },\n                                                \'TYPE\' => \'INT2\'\n                                              }\n                                            ],\n                                \'INDEXES\' => [\n                                               \'flagexclusions_type_id_idx\',\n                                               [\n                                                 \'type_id\',\n                                                 \'product_id\',\n                                                 \'component_id\'\n                                               ]\n                                             ]\n                              },\n          \'flaginclusions\' => {\n                                \'FIELDS\' => [\n                                              \'type_id\',\n                                              {\n                                                \'NOTNULL\' => 1,\n                                                \'REFERENCES\' => {\n                                                                  \'COLUMN\' => \'id\',\n                                                                  \'DELETE\' => \'CASCADE\',\n                                                                  \'TABLE\' => \'flagtypes\'\n                                                                },\n                                                \'TYPE\' => \'INT2\'\n                                              },\n                                              \'product_id\',\n                                              {\n                                                \'REFERENCES\' => {\n                                                                  \'COLUMN\' => \'id\',\n                                                                  \'DELETE\' => \'CASCADE\',\n                                                                  \'TABLE\' => \'products\'\n                                                                },\n                                                \'TYPE\' => \'INT2\'\n                                              },\n                                              \'component_id\',\n                                              {\n                                                \'REFERENCES\' => {\n                                                                  \'COLUMN\' => \'id\',\n                                                                  \'DELETE\' => \'CASCADE\',\n                                                                  \'TABLE\' => \'components\'\n                                                                },\n                                                \'TYPE\' => \'INT2\'\n                                              }\n                                            ],\n                                \'INDEXES\' => [\n                                               \'flaginclusions_type_id_idx\',\n                                               [\n                                                 \'type_id\',\n                                                 \'product_id\',\n                                                 \'component_id\'\n                                               ]\n                                             ]\n                              },\n          \'flags\' => {\n                       \'FIELDS\' => [\n                                     \'id\',\n                                     {\n                                       \'NOTNULL\' => 1,\n                                       \'PRIMARYKEY\' => 1,\n                                       \'TYPE\' => \'MEDIUMSERIAL\'\n                                     },\n                                     \'type_id\',\n                                     {\n                                       \'NOTNULL\' => 1,\n                                       \'REFERENCES\' => {\n                                                         \'COLUMN\' => \'id\',\n                                                         \'DELETE\' => \'CASCADE\',\n                                                         \'TABLE\' => \'flagtypes\'\n                                                       },\n                                       \'TYPE\' => \'INT2\'\n                                     },\n                                     \'status\',\n                                     {\n                                       \'NOTNULL\' => 1,\n                                       \'TYPE\' => \'char(1)\'\n                                     },\n                                     \'bug_id\',\n                                     {\n                                       \'NOTNULL\' => 1,\n                                       \'REFERENCES\' => {\n                                                         \'COLUMN\' => \'bug_id\',\n                                                         \'DELETE\' => \'CASCADE\',\n                                                         \'TABLE\' => \'bugs\'\n                                                       },\n                                       \'TYPE\' => \'INT3\'\n                                     },\n                                     \'attach_id\',\n                                     {\n                                       \'REFERENCES\' => {\n                                                         \'COLUMN\' => \'attach_id\',\n                                                         \'DELETE\' => \'CASCADE\',\n                                                         \'TABLE\' => \'attachments\'\n                                                       },\n                                       \'TYPE\' => \'INT3\'\n                                     },\n                                     \'creation_date\',\n                                     {\n                                       \'NOTNULL\' => 1,\n                                       \'TYPE\' => \'DATETIME\'\n                                     },\n                                     \'modification_date\',\n                                     {\n                                       \'TYPE\' => \'DATETIME\'\n                                     },\n                                     \'setter_id\',\n                                     {\n                                       \'REFERENCES\' => {\n                                                         \'COLUMN\' => \'userid\',\n                                                         \'TABLE\' => \'profiles\'\n                                                       },\n                                       \'TYPE\' => \'INT3\'\n                                     },\n                                     \'requestee_id\',\n                                     {\n                                       \'REFERENCES\' => {\n                                                         \'COLUMN\' => \'userid\',\n                                                         \'TABLE\' => \'profiles\'\n                                                       },\n                                       \'TYPE\' => \'INT3\'\n                                     }\n                                   ],\n                       \'INDEXES\' => [\n                                      \'flags_bug_id_idx\',\n                                      [\n                                        \'bug_id\',\n                                        \'attach_id\'\n                                      ],\n                                      \'flags_setter_id_idx\',\n                                      [\n                                        \'setter_id\'\n                                      ],\n                                      \'flags_requestee_id_idx\',\n                                      [\n                                        \'requestee_id\'\n                                      ],\n                                      \'flags_type_id_idx\',\n                                      [\n                                        \'type_id\'\n                                      ]\n                                    ]\n                     },\n          \'flagtypes\' => {\n                           \'FIELDS\' => [\n                                         \'id\',\n                                         {\n                                           \'NOTNULL\' => 1,\n                                           \'PRIMARYKEY\' => 1,\n                                           \'TYPE\' => \'SMALLSERIAL\'\n                                         },\n                                         \'name\',\n                                         {\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'varchar(50)\'\n                                         },\n                                         \'description\',\n                                         {\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'MEDIUMTEXT\'\n                                         },\n                                         \'cc_list\',\n                                         {\n                                           \'TYPE\' => \'varchar(200)\'\n                                         },\n                                         \'target_type\',\n                                         {\n                                           \'DEFAULT\' => \'\\\'b\\\'\',\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'char(1)\'\n                                         },\n                                         \'is_active\',\n                                         {\n                                           \'DEFAULT\' => \'TRUE\',\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'BOOLEAN\'\n                                         },\n                                         \'is_requestable\',\n                                         {\n                                           \'DEFAULT\' => \'FALSE\',\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'BOOLEAN\'\n                                         },\n                                         \'is_requesteeble\',\n                                         {\n                                           \'DEFAULT\' => \'FALSE\',\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'BOOLEAN\'\n                                         },\n                                         \'is_multiplicable\',\n                                         {\n                                           \'DEFAULT\' => \'FALSE\',\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'BOOLEAN\'\n                                         },\n                                         \'sortkey\',\n                                         {\n                                           \'DEFAULT\' => \'0\',\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'INT2\'\n                                         },\n                                         \'grant_group_id\',\n                                         {\n                                           \'REFERENCES\' => {\n                                                             \'COLUMN\' => \'id\',\n                                                             \'DELETE\' => \'SET NULL\',\n                                                             \'TABLE\' => \'groups\'\n                                                           },\n                                           \'TYPE\' => \'INT3\'\n                                         },\n                                         \'request_group_id\',\n                                         {\n                                           \'REFERENCES\' => {\n                                                             \'COLUMN\' => \'id\',\n                                                             \'DELETE\' => \'SET NULL\',\n                                                             \'TABLE\' => \'groups\'\n                                                           },\n                                           \'TYPE\' => \'INT3\'\n                                         }\n                                       ]\n                         },\n          \'group_control_map\' => {\n                                   \'FIELDS\' => [\n                                                 \'group_id\',\n                                                 {\n                                                   \'NOTNULL\' => 1,\n                                                   \'REFERENCES\' => {\n                                                                     \'COLUMN\' => \'id\',\n                                                                     \'DELETE\' => \'CASCADE\',\n                                                                     \'TABLE\' => \'groups\'\n                                                                   },\n                                                   \'TYPE\' => \'INT3\'\n                                                 },\n                                                 \'product_id\',\n                                                 {\n                                                   \'NOTNULL\' => 1,\n                                                   \'REFERENCES\' => {\n                                                                     \'COLUMN\' => \'id\',\n                                                                     \'DELETE\' => \'CASCADE\',\n                                                                     \'TABLE\' => \'products\'\n                                                                   },\n                                                   \'TYPE\' => \'INT2\'\n                                                 },\n                                                 \'entry\',\n                                                 {\n                                                   \'DEFAULT\' => \'FALSE\',\n                                                   \'NOTNULL\' => 1,\n                                                   \'TYPE\' => \'BOOLEAN\'\n                                                 },\n                                                 \'membercontrol\',\n                                                 {\n                                                   \'NOTNULL\' => 1,\n                                                   \'TYPE\' => \'BOOLEAN\'\n                                                 },\n                                                 \'othercontrol\',\n                                                 {\n                                                   \'NOTNULL\' => 1,\n                                                   \'TYPE\' => \'BOOLEAN\'\n                                                 },\n                                                 \'canedit\',\n                                                 {\n                                                   \'DEFAULT\' => \'FALSE\',\n                                                   \'NOTNULL\' => 1,\n                                                   \'TYPE\' => \'BOOLEAN\'\n                                                 },\n                                                 \'editcomponents\',\n                                                 {\n                                                   \'DEFAULT\' => \'FALSE\',\n                                                   \'NOTNULL\' => 1,\n                                                   \'TYPE\' => \'BOOLEAN\'\n                                                 },\n                                                 \'editbugs\',\n                                                 {\n                                                   \'DEFAULT\' => \'FALSE\',\n                                                   \'NOTNULL\' => 1,\n                                                   \'TYPE\' => \'BOOLEAN\'\n                                                 },\n                                                 \'canconfirm\',\n                                                 {\n                                                   \'DEFAULT\' => \'FALSE\',\n                                                   \'NOTNULL\' => 1,\n                                                   \'TYPE\' => \'BOOLEAN\'\n                                                 }\n                                               ],\n                                   \'INDEXES\' => [\n                                                  \'group_control_map_product_id_idx\',\n                                                  {\n                                                    \'FIELDS\' => [\n                                                                  \'product_id\',\n                                                                  \'group_id\'\n                                                                ],\n                                                    \'TYPE\' => \'UNIQUE\'\n                                                  },\n                                                  \'group_control_map_group_id_idx\',\n                                                  [\n                                                    \'group_id\'\n                                                  ]\n                                                ]\n                                 },\n          \'group_group_map\' => {\n                                 \'FIELDS\' => [\n                                               \'member_id\',\n                                               {\n                                                 \'NOTNULL\' => 1,\n                                                 \'REFERENCES\' => {\n                                                                   \'COLUMN\' => \'id\',\n                                                                   \'DELETE\' => \'CASCADE\',\n                                                                   \'TABLE\' => \'groups\'\n                                                                 },\n                                                 \'TYPE\' => \'INT3\'\n                                               },\n                                               \'grantor_id\',\n                                               {\n                                                 \'NOTNULL\' => 1,\n                                                 \'REFERENCES\' => {\n                                                                   \'COLUMN\' => \'id\',\n                                                                   \'DELETE\' => \'CASCADE\',\n                                                                   \'TABLE\' => \'groups\'\n                                                                 },\n                                                 \'TYPE\' => \'INT3\'\n                                               },\n                                               \'grant_type\',\n                                               {\n                                                 \'DEFAULT\' => \'0\',\n                                                 \'NOTNULL\' => 1,\n                                                 \'TYPE\' => \'INT1\'\n                                               }\n                                             ],\n                                 \'INDEXES\' => [\n                                                \'group_group_map_member_id_idx\',\n                                                {\n                                                  \'FIELDS\' => [\n                                                                \'member_id\',\n                                                                \'grantor_id\',\n                                                                \'grant_type\'\n                                                              ],\n                                                  \'TYPE\' => \'UNIQUE\'\n                                                }\n                                              ]\n                               },\n          \'groups\' => {\n                        \'FIELDS\' => [\n                                      \'id\',\n                                      {\n                                        \'NOTNULL\' => 1,\n                                        \'PRIMARYKEY\' => 1,\n                                        \'TYPE\' => \'MEDIUMSERIAL\'\n                                      },\n                                      \'name\',\n                                      {\n                                        \'NOTNULL\' => 1,\n                                        \'TYPE\' => \'varchar(255)\'\n                                      },\n                                      \'description\',\n                                      {\n                                        \'NOTNULL\' => 1,\n                                        \'TYPE\' => \'MEDIUMTEXT\'\n                                      },\n                                      \'isbuggroup\',\n                                      {\n                                        \'NOTNULL\' => 1,\n                                        \'TYPE\' => \'BOOLEAN\'\n                                      },\n                                      \'userregexp\',\n                                      {\n                                        \'DEFAULT\' => \'\\\'\\\'\',\n                                        \'NOTNULL\' => 1,\n                                        \'TYPE\' => \'TINYTEXT\'\n                                      },\n                                      \'isactive\',\n                                      {\n                                        \'DEFAULT\' => \'TRUE\',\n                                        \'NOTNULL\' => 1,\n                                        \'TYPE\' => \'BOOLEAN\'\n                                      },\n                                      \'icon_url\',\n                                      {\n                                        \'TYPE\' => \'TINYTEXT\'\n                                      }\n                                    ],\n                        \'INDEXES\' => [\n                                       \'groups_name_idx\',\n                                       {\n                                         \'FIELDS\' => [\n                                                       \'name\'\n                                                     ],\n                                         \'TYPE\' => \'UNIQUE\'\n                                       }\n                                     ]\n                      },\n          \'keyworddefs\' => {\n                             \'FIELDS\' => [\n                                           \'id\',\n                                           {\n                                             \'NOTNULL\' => 1,\n                                             \'PRIMARYKEY\' => 1,\n                                             \'TYPE\' => \'SMALLSERIAL\'\n                                           },\n                                           \'name\',\n                                           {\n                                             \'NOTNULL\' => 1,\n                                             \'TYPE\' => \'varchar(64)\'\n                                           },\n                                           \'description\',\n                                           {\n                                             \'TYPE\' => \'MEDIUMTEXT\'\n                                           }\n                                         ],\n                             \'INDEXES\' => [\n                                            \'keyworddefs_name_idx\',\n                                            {\n                                              \'FIELDS\' => [\n                                                            \'name\'\n                                                          ],\n                                              \'TYPE\' => \'UNIQUE\'\n                                            }\n                                          ]\n                           },\n          \'keywords\' => {\n                          \'FIELDS\' => [\n                                        \'bug_id\',\n                                        {\n                                          \'NOTNULL\' => 1,\n                                          \'REFERENCES\' => {\n                                                            \'COLUMN\' => \'bug_id\',\n                                                            \'DELETE\' => \'CASCADE\',\n                                                            \'TABLE\' => \'bugs\'\n                                                          },\n                                          \'TYPE\' => \'INT3\'\n                                        },\n                                        \'keywordid\',\n                                        {\n                                          \'NOTNULL\' => 1,\n                                          \'REFERENCES\' => {\n                                                            \'COLUMN\' => \'id\',\n                                                            \'DELETE\' => \'CASCADE\',\n                                                            \'TABLE\' => \'keyworddefs\'\n                                                          },\n                                          \'TYPE\' => \'INT2\'\n                                        }\n                                      ],\n                          \'INDEXES\' => [\n                                         \'keywords_bug_id_idx\',\n                                         {\n                                           \'FIELDS\' => [\n                                                         \'bug_id\',\n                                                         \'keywordid\'\n                                                       ],\n                                           \'TYPE\' => \'UNIQUE\'\n                                         },\n                                         \'keywords_keywordid_idx\',\n                                         [\n                                           \'keywordid\'\n                                         ]\n                                       ]\n                        },\n          \'login_failure\' => {\n                               \'FIELDS\' => [\n                                             \'user_id\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'REFERENCES\' => {\n                                                                 \'COLUMN\' => \'userid\',\n                                                                 \'DELETE\' => \'CASCADE\',\n                                                                 \'TABLE\' => \'profiles\'\n                                                               },\n                                               \'TYPE\' => \'INT3\'\n                                             },\n                                             \'login_time\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'TYPE\' => \'DATETIME\'\n                                             },\n                                             \'ip_addr\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'TYPE\' => \'varchar(40)\'\n                                             }\n                                           ],\n                               \'INDEXES\' => [\n                                              \'login_failure_user_id_idx\',\n                                              [\n                                                \'user_id\'\n                                              ]\n                                            ]\n                             },\n          \'logincookies\' => {\n                              \'FIELDS\' => [\n                                            \'cookie\',\n                                            {\n                                              \'NOTNULL\' => 1,\n                                              \'PRIMARYKEY\' => 1,\n                                              \'TYPE\' => \'varchar(16)\'\n                                            },\n                                            \'userid\',\n                                            {\n                                              \'NOTNULL\' => 1,\n                                              \'REFERENCES\' => {\n                                                                \'COLUMN\' => \'userid\',\n                                                                \'DELETE\' => \'CASCADE\',\n                                                                \'TABLE\' => \'profiles\'\n                                                              },\n                                              \'TYPE\' => \'INT3\'\n                                            },\n                                            \'ipaddr\',\n                                            {\n                                              \'TYPE\' => \'varchar(40)\'\n                                            },\n                                            \'lastused\',\n                                            {\n                                              \'NOTNULL\' => 1,\n                                              \'TYPE\' => \'DATETIME\'\n                                            }\n                                          ],\n                              \'INDEXES\' => [\n                                             \'logincookies_lastused_idx\',\n                                             [\n                                               \'lastused\'\n                                             ]\n                                           ]\n                            },\n          \'longdescs\' => {\n                           \'FIELDS\' => [\n                                         \'comment_id\',\n                                         {\n                                           \'NOTNULL\' => 1,\n                                           \'PRIMARYKEY\' => 1,\n                                           \'TYPE\' => \'MEDIUMSERIAL\'\n                                         },\n                                         \'bug_id\',\n                                         {\n                                           \'NOTNULL\' => 1,\n                                           \'REFERENCES\' => {\n                                                             \'COLUMN\' => \'bug_id\',\n                                                             \'DELETE\' => \'CASCADE\',\n                                                             \'TABLE\' => \'bugs\'\n                                                           },\n                                           \'TYPE\' => \'INT3\'\n                                         },\n                                         \'who\',\n                                         {\n                                           \'NOTNULL\' => 1,\n                                           \'REFERENCES\' => {\n                                                             \'COLUMN\' => \'userid\',\n                                                             \'TABLE\' => \'profiles\'\n                                                           },\n                                           \'TYPE\' => \'INT3\'\n                                         },\n                                         \'bug_when\',\n                                         {\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'DATETIME\'\n                                         },\n                                         \'work_time\',\n                                         {\n                                           \'DEFAULT\' => \'0\',\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'decimal(7,2)\'\n                                         },\n                                         \'thetext\',\n                                         {\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'LONGTEXT\'\n                                         },\n                                         \'isprivate\',\n                                         {\n                                           \'DEFAULT\' => \'FALSE\',\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'BOOLEAN\'\n                                         },\n                                         \'already_wrapped\',\n                                         {\n                                           \'DEFAULT\' => \'FALSE\',\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'BOOLEAN\'\n                                         },\n                                         \'type\',\n                                         {\n                                           \'DEFAULT\' => \'0\',\n                                           \'NOTNULL\' => 1,\n                                           \'TYPE\' => \'INT2\'\n                                         },\n                                         \'extra_data\',\n                                         {\n                                           \'TYPE\' => \'varchar(255)\'\n                                         }\n                                       ],\n                           \'INDEXES\' => [\n                                          \'longdescs_bug_id_idx\',\n                                          [\n                                            \'bug_id\'\n                                          ],\n                                          \'longdescs_who_idx\',\n                                          [\n                                            \'who\',\n                                            \'bug_id\'\n                                          ],\n                                          \'longdescs_bug_when_idx\',\n                                          [\n                                            \'bug_when\'\n                                          ]\n                                        ]\n                         },\n          \'milestones\' => {\n                            \'FIELDS\' => [\n                                          \'id\',\n                                          {\n                                            \'NOTNULL\' => 1,\n                                            \'PRIMARYKEY\' => 1,\n                                            \'TYPE\' => \'MEDIUMSERIAL\'\n                                          },\n                                          \'product_id\',\n                                          {\n                                            \'NOTNULL\' => 1,\n                                            \'REFERENCES\' => {\n                                                              \'COLUMN\' => \'id\',\n                                                              \'DELETE\' => \'CASCADE\',\n                                                              \'TABLE\' => \'products\'\n                                                            },\n                                            \'TYPE\' => \'INT2\'\n                                          },\n                                          \'value\',\n                                          {\n                                            \'NOTNULL\' => 1,\n                                            \'TYPE\' => \'varchar(20)\'\n                                          },\n                                          \'sortkey\',\n                                          {\n                                            \'DEFAULT\' => 0,\n                                            \'NOTNULL\' => 1,\n                                            \'TYPE\' => \'INT2\'\n                                          }\n                                        ],\n                            \'INDEXES\' => [\n                                           \'milestones_product_id_idx\',\n                                           {\n                                             \'FIELDS\' => [\n                                                           \'product_id\',\n                                                           \'value\'\n                                                         ],\n                                             \'TYPE\' => \'UNIQUE\'\n                                           }\n                                         ]\n                          },\n          \'namedqueries\' => {\n                              \'FIELDS\' => [\n                                            \'id\',\n                                            {\n                                              \'NOTNULL\' => 1,\n                                              \'PRIMARYKEY\' => 1,\n                                              \'TYPE\' => \'MEDIUMSERIAL\'\n                                            },\n                                            \'userid\',\n                                            {\n                                              \'NOTNULL\' => 1,\n                                              \'REFERENCES\' => {\n                                                                \'COLUMN\' => \'userid\',\n                                                                \'DELETE\' => \'CASCADE\',\n                                                                \'TABLE\' => \'profiles\'\n                                                              },\n                                              \'TYPE\' => \'INT3\'\n                                            },\n                                            \'name\',\n                                            {\n                                              \'NOTNULL\' => 1,\n                                              \'TYPE\' => \'varchar(64)\'\n                                            },\n                                            \'query\',\n                                            {\n                                              \'NOTNULL\' => 1,\n                                              \'TYPE\' => \'LONGTEXT\'\n                                            },\n                                            \'query_type\',\n                                            {\n                                              \'DEFAULT\' => 0,\n                                              \'NOTNULL\' => 1,\n                                              \'TYPE\' => \'BOOLEAN\'\n                                            }\n                                          ],\n                              \'INDEXES\' => [\n                                             \'namedqueries_userid_idx\',\n                                             {\n                                               \'FIELDS\' => [\n                                                             \'userid\',\n                                                             \'name\'\n                                                           ],\n                                               \'TYPE\' => \'UNIQUE\'\n                                             }\n                                           ]\n                            },\n          \'namedqueries_link_in_footer\' => {\n                                             \'FIELDS\' => [\n                                                           \'namedquery_id\',\n                                                           {\n                                                             \'NOTNULL\' => 1,\n                                                             \'REFERENCES\' => {\n                                                                               \'COLUMN\' => \'id\',\n                                                                               \'DELETE\' => \'CASCADE\',\n                                                                               \'TABLE\' => \'namedqueries\'\n                                                                             },\n                                                             \'TYPE\' => \'INT3\'\n                                                           },\n                                                           \'user_id\',\n                                                           {\n                                                             \'NOTNULL\' => 1,\n                                                             \'REFERENCES\' => {\n                                                                               \'COLUMN\' => \'userid\',\n                                                                               \'DELETE\' => \'CASCADE\',\n                                                                               \'TABLE\' => \'profiles\'\n                                                                             },\n                                                             \'TYPE\' => \'INT3\'\n                                                           }\n                                                         ],\n                                             \'INDEXES\' => [\n                                                            \'namedqueries_link_in_footer_id_idx\',\n                                                            {\n                                                              \'FIELDS\' => [\n                                                                            \'namedquery_id\',\n                                                                            \'user_id\'\n                                                                          ],\n                                                              \'TYPE\' => \'UNIQUE\'\n                                                            },\n                                                            \'namedqueries_link_in_footer_userid_idx\',\n                                                            [\n                                                              \'user_id\'\n                                                            ]\n                                                          ]\n                                           },\n          \'namedquery_group_map\' => {\n                                      \'FIELDS\' => [\n                                                    \'namedquery_id\',\n                                                    {\n                                                      \'NOTNULL\' => 1,\n                                                      \'REFERENCES\' => {\n                                                                        \'COLUMN\' => \'id\',\n                                                                        \'DELETE\' => \'CASCADE\',\n                                                                        \'TABLE\' => \'namedqueries\'\n                                                                      },\n                                                      \'TYPE\' => \'INT3\'\n                                                    },\n                                                    \'group_id\',\n                                                    {\n                                                      \'NOTNULL\' => 1,\n                                                      \'REFERENCES\' => {\n                                                                        \'COLUMN\' => \'id\',\n                                                                        \'DELETE\' => \'CASCADE\',\n                                                                        \'TABLE\' => \'groups\'\n                                                                      },\n                                                      \'TYPE\' => \'INT3\'\n                                                    }\n                                                  ],\n                                      \'INDEXES\' => [\n                                                     \'namedquery_group_map_namedquery_id_idx\',\n                                                     {\n                                                       \'FIELDS\' => [\n                                                                     \'namedquery_id\'\n                                                                   ],\n                                                       \'TYPE\' => \'UNIQUE\'\n                                                     },\n                                                     \'namedquery_group_map_group_id_idx\',\n                                                     [\n                                                       \'group_id\'\n                                                     ]\n                                                   ]\n                                    },\n          \'op_sys\' => {\n                        \'FIELDS\' => [\n                                      \'id\',\n                                      {\n                                        \'NOTNULL\' => 1,\n                                        \'PRIMARYKEY\' => 1,\n                                        \'TYPE\' => \'SMALLSERIAL\'\n                                      },\n                                      \'value\',\n                                      {\n                                        \'NOTNULL\' => 1,\n                                        \'TYPE\' => \'varchar(64)\'\n                                      },\n                                      \'sortkey\',\n                                      {\n                                        \'DEFAULT\' => 0,\n                                        \'NOTNULL\' => 1,\n                                        \'TYPE\' => \'INT2\'\n                                      },\n                                      \'isactive\',\n                                      {\n                                        \'DEFAULT\' => \'TRUE\',\n                                        \'NOTNULL\' => 1,\n                                        \'TYPE\' => \'BOOLEAN\'\n                                      },\n                                      \'visibility_value_id\',\n                                      {\n                                        \'TYPE\' => \'INT2\'\n                                      }\n                                    ],\n                        \'INDEXES\' => [\n                                       \'op_sys_value_idx\',\n                                       {\n                                         \'FIELDS\' => [\n                                                       \'value\'\n                                                     ],\n                                         \'TYPE\' => \'UNIQUE\'\n                                       },\n                                       \'op_sys_sortkey_idx\',\n                                       [\n                                         \'sortkey\',\n                                         \'value\'\n                                       ],\n                                       \'op_sys_visibility_value_id_idx\',\n                                       [\n                                         \'visibility_value_id\'\n                                       ]\n                                     ]\n                      },\n          \'priority\' => {\n                          \'FIELDS\' => [\n                                        \'id\',\n                                        {\n                                          \'NOTNULL\' => 1,\n                                          \'PRIMARYKEY\' => 1,\n                                          \'TYPE\' => \'SMALLSERIAL\'\n                                        },\n                                        \'value\',\n                                        {\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'varchar(64)\'\n                                        },\n                                        \'sortkey\',\n                                        {\n                                          \'DEFAULT\' => 0,\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'INT2\'\n                                        },\n                                        \'isactive\',\n                                        {\n                                          \'DEFAULT\' => \'TRUE\',\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'BOOLEAN\'\n                                        },\n                                        \'visibility_value_id\',\n                                        {\n                                          \'TYPE\' => \'INT2\'\n                                        }\n                                      ],\n                          \'INDEXES\' => [\n                                         \'priority_value_idx\',\n                                         {\n                                           \'FIELDS\' => [\n                                                         \'value\'\n                                                       ],\n                                           \'TYPE\' => \'UNIQUE\'\n                                         },\n                                         \'priority_sortkey_idx\',\n                                         [\n                                           \'sortkey\',\n                                           \'value\'\n                                         ],\n                                         \'priority_visibility_value_id_idx\',\n                                         [\n                                           \'visibility_value_id\'\n                                         ]\n                                       ]\n                        },\n          \'products\' => {\n                          \'FIELDS\' => [\n                                        \'id\',\n                                        {\n                                          \'NOTNULL\' => 1,\n                                          \'PRIMARYKEY\' => 1,\n                                          \'TYPE\' => \'SMALLSERIAL\'\n                                        },\n                                        \'name\',\n                                        {\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'varchar(64)\'\n                                        },\n                                        \'classification_id\',\n                                        {\n                                          \'DEFAULT\' => \'1\',\n                                          \'NOTNULL\' => 1,\n                                          \'REFERENCES\' => {\n                                                            \'COLUMN\' => \'id\',\n                                                            \'DELETE\' => \'CASCADE\',\n                                                            \'TABLE\' => \'classifications\'\n                                                          },\n                                          \'TYPE\' => \'INT2\'\n                                        },\n                                        \'description\',\n                                        {\n                                          \'TYPE\' => \'MEDIUMTEXT\'\n                                        },\n                                        \'isactive\',\n                                        {\n                                          \'DEFAULT\' => 1,\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'BOOLEAN\'\n                                        },\n                                        \'votesperuser\',\n                                        {\n                                          \'DEFAULT\' => 0,\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'INT2\'\n                                        },\n                                        \'maxvotesperbug\',\n                                        {\n                                          \'DEFAULT\' => \'10000\',\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'INT2\'\n                                        },\n                                        \'votestoconfirm\',\n                                        {\n                                          \'DEFAULT\' => 0,\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'INT2\'\n                                        },\n                                        \'defaultmilestone\',\n                                        {\n                                          \'DEFAULT\' => \'\\\'---\\\'\',\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'varchar(20)\'\n                                        },\n                                        \'allows_unconfirmed\',\n                                        {\n                                          \'DEFAULT\' => \'FALSE\',\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'BOOLEAN\'\n                                        }\n                                      ],\n                          \'INDEXES\' => [\n                                         \'products_name_idx\',\n                                         {\n                                           \'FIELDS\' => [\n                                                         \'name\'\n                                                       ],\n                                           \'TYPE\' => \'UNIQUE\'\n                                         }\n                                       ]\n                        },\n          \'profile_setting\' => {\n                                 \'FIELDS\' => [\n                                               \'user_id\',\n                                               {\n                                                 \'NOTNULL\' => 1,\n                                                 \'REFERENCES\' => {\n                                                                   \'COLUMN\' => \'userid\',\n                                                                   \'DELETE\' => \'CASCADE\',\n                                                                   \'TABLE\' => \'profiles\'\n                                                                 },\n                                                 \'TYPE\' => \'INT3\'\n                                               },\n                                               \'setting_name\',\n                                               {\n                                                 \'NOTNULL\' => 1,\n                                                 \'REFERENCES\' => {\n                                                                   \'COLUMN\' => \'name\',\n                                                                   \'DELETE\' => \'CASCADE\',\n                                                                   \'TABLE\' => \'setting\'\n                                                                 },\n                                                 \'TYPE\' => \'varchar(32)\'\n                                               },\n                                               \'setting_value\',\n                                               {\n                                                 \'NOTNULL\' => 1,\n                                                 \'TYPE\' => \'varchar(32)\'\n                                               }\n                                             ],\n                                 \'INDEXES\' => [\n                                                \'profile_setting_value_unique_idx\',\n                                                {\n                                                  \'FIELDS\' => [\n                                                                \'user_id\',\n                                                                \'setting_name\'\n                                                              ],\n                                                  \'TYPE\' => \'UNIQUE\'\n                                                }\n                                              ]\n                               },\n          \'profiles\' => {\n                          \'FIELDS\' => [\n                                        \'userid\',\n                                        {\n                                          \'NOTNULL\' => 1,\n                                          \'PRIMARYKEY\' => 1,\n                                          \'TYPE\' => \'MEDIUMSERIAL\'\n                                        },\n                                        \'login_name\',\n                                        {\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'varchar(255)\'\n                                        },\n                                        \'cryptpassword\',\n                                        {\n                                          \'TYPE\' => \'varchar(128)\'\n                                        },\n                                        \'realname\',\n                                        {\n                                          \'DEFAULT\' => \'\\\'\\\'\',\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'varchar(255)\'\n                                        },\n                                        \'disabledtext\',\n                                        {\n                                          \'DEFAULT\' => \'\\\'\\\'\',\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'MEDIUMTEXT\'\n                                        },\n                                        \'disable_mail\',\n                                        {\n                                          \'DEFAULT\' => \'FALSE\',\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'BOOLEAN\'\n                                        },\n                                        \'mybugslink\',\n                                        {\n                                          \'DEFAULT\' => \'TRUE\',\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'BOOLEAN\'\n                                        },\n                                        \'extern_id\',\n                                        {\n                                          \'TYPE\' => \'varchar(64)\'\n                                        }\n                                      ],\n                          \'INDEXES\' => [\n                                         \'profiles_login_name_idx\',\n                                         {\n                                           \'FIELDS\' => [\n                                                         \'login_name\'\n                                                       ],\n                                           \'TYPE\' => \'UNIQUE\'\n                                         },\n                                         \'profiles_extern_id_idx\',\n                                         {\n                                           \'FIELDS\' => [\n                                                         \'extern_id\'\n                                                       ],\n                                           \'TYPE\' => \'UNIQUE\'\n                                         }\n                                       ]\n                        },\n          \'profiles_activity\' => {\n                                   \'FIELDS\' => [\n                                                 \'userid\',\n                                                 {\n                                                   \'NOTNULL\' => 1,\n                                                   \'REFERENCES\' => {\n                                                                     \'COLUMN\' => \'userid\',\n                                                                     \'DELETE\' => \'CASCADE\',\n                                                                     \'TABLE\' => \'profiles\'\n                                                                   },\n                                                   \'TYPE\' => \'INT3\'\n                                                 },\n                                                 \'who\',\n                                                 {\n                                                   \'NOTNULL\' => 1,\n                                                   \'REFERENCES\' => {\n                                                                     \'COLUMN\' => \'userid\',\n                                                                     \'TABLE\' => \'profiles\'\n                                                                   },\n                                                   \'TYPE\' => \'INT3\'\n                                                 },\n                                                 \'profiles_when\',\n                                                 {\n                                                   \'NOTNULL\' => 1,\n                                                   \'TYPE\' => \'DATETIME\'\n                                                 },\n                                                 \'fieldid\',\n                                                 {\n                                                   \'NOTNULL\' => 1,\n                                                   \'REFERENCES\' => {\n                                                                     \'COLUMN\' => \'id\',\n                                                                     \'TABLE\' => \'fielddefs\'\n                                                                   },\n                                                   \'TYPE\' => \'INT3\'\n                                                 },\n                                                 \'oldvalue\',\n                                                 {\n                                                   \'TYPE\' => \'TINYTEXT\'\n                                                 },\n                                                 \'newvalue\',\n                                                 {\n                                                   \'TYPE\' => \'TINYTEXT\'\n                                                 }\n                                               ],\n                                   \'INDEXES\' => [\n                                                  \'profiles_activity_userid_idx\',\n                                                  [\n                                                    \'userid\'\n                                                  ],\n                                                  \'profiles_activity_profiles_when_idx\',\n                                                  [\n                                                    \'profiles_when\'\n                                                  ],\n                                                  \'profiles_activity_fieldid_idx\',\n                                                  [\n                                                    \'fieldid\'\n                                                  ]\n                                                ]\n                                 },\n          \'quips\' => {\n                       \'FIELDS\' => [\n                                     \'quipid\',\n                                     {\n                                       \'NOTNULL\' => 1,\n                                       \'PRIMARYKEY\' => 1,\n                                       \'TYPE\' => \'MEDIUMSERIAL\'\n                                     },\n                                     \'userid\',\n                                     {\n                                       \'REFERENCES\' => {\n                                                         \'COLUMN\' => \'userid\',\n                                                         \'DELETE\' => \'SET NULL\',\n                                                         \'TABLE\' => \'profiles\'\n                                                       },\n                                       \'TYPE\' => \'INT3\'\n                                     },\n                                     \'quip\',\n                                     {\n                                       \'NOTNULL\' => 1,\n                                       \'TYPE\' => \'MEDIUMTEXT\'\n                                     },\n                                     \'approved\',\n                                     {\n                                       \'DEFAULT\' => \'TRUE\',\n                                       \'NOTNULL\' => 1,\n                                       \'TYPE\' => \'BOOLEAN\'\n                                     }\n                                   ]\n                     },\n          \'rep_platform\' => {\n                              \'FIELDS\' => [\n                                            \'id\',\n                                            {\n                                              \'NOTNULL\' => 1,\n                                              \'PRIMARYKEY\' => 1,\n                                              \'TYPE\' => \'SMALLSERIAL\'\n                                            },\n                                            \'value\',\n                                            {\n                                              \'NOTNULL\' => 1,\n                                              \'TYPE\' => \'varchar(64)\'\n                                            },\n                                            \'sortkey\',\n                                            {\n                                              \'DEFAULT\' => 0,\n                                              \'NOTNULL\' => 1,\n                                              \'TYPE\' => \'INT2\'\n                                            },\n                                            \'isactive\',\n                                            {\n                                              \'DEFAULT\' => \'TRUE\',\n                                              \'NOTNULL\' => 1,\n                                              \'TYPE\' => \'BOOLEAN\'\n                                            },\n                                            \'visibility_value_id\',\n                                            {\n                                              \'TYPE\' => \'INT2\'\n                                            }\n                                          ],\n                              \'INDEXES\' => [\n                                             \'rep_platform_value_idx\',\n                                             {\n                                               \'FIELDS\' => [\n                                                             \'value\'\n                                                           ],\n                                               \'TYPE\' => \'UNIQUE\'\n                                             },\n                                             \'rep_platform_sortkey_idx\',\n                                             [\n                                               \'sortkey\',\n                                               \'value\'\n                                             ],\n                                             \'rep_platform_visibility_value_id_idx\',\n                                             [\n                                               \'visibility_value_id\'\n                                             ]\n                                           ]\n                            },\n          \'resolution\' => {\n                            \'FIELDS\' => [\n                                          \'id\',\n                                          {\n                                            \'NOTNULL\' => 1,\n                                            \'PRIMARYKEY\' => 1,\n                                            \'TYPE\' => \'SMALLSERIAL\'\n                                          },\n                                          \'value\',\n                                          {\n                                            \'NOTNULL\' => 1,\n                                            \'TYPE\' => \'varchar(64)\'\n                                          },\n                                          \'sortkey\',\n                                          {\n                                            \'DEFAULT\' => 0,\n                                            \'NOTNULL\' => 1,\n                                            \'TYPE\' => \'INT2\'\n                                          },\n                                          \'isactive\',\n                                          {\n                                            \'DEFAULT\' => \'TRUE\',\n                                            \'NOTNULL\' => 1,\n                                            \'TYPE\' => \'BOOLEAN\'\n                                          },\n                                          \'visibility_value_id\',\n                                          {\n                                            \'TYPE\' => \'INT2\'\n                                          }\n                                        ],\n                            \'INDEXES\' => [\n                                           \'resolution_value_idx\',\n                                           {\n                                             \'FIELDS\' => [\n                                                           \'value\'\n                                                         ],\n                                             \'TYPE\' => \'UNIQUE\'\n                                           },\n                                           \'resolution_sortkey_idx\',\n                                           [\n                                             \'sortkey\',\n                                             \'value\'\n                                           ],\n                                           \'resolution_visibility_value_id_idx\',\n                                           [\n                                             \'visibility_value_id\'\n                                           ]\n                                         ]\n                          },\n          \'series\' => {\n                        \'FIELDS\' => [\n                                      \'series_id\',\n                                      {\n                                        \'NOTNULL\' => 1,\n                                        \'PRIMARYKEY\' => 1,\n                                        \'TYPE\' => \'MEDIUMSERIAL\'\n                                      },\n                                      \'creator\',\n                                      {\n                                        \'REFERENCES\' => {\n                                                          \'COLUMN\' => \'userid\',\n                                                          \'DELETE\' => \'CASCADE\',\n                                                          \'TABLE\' => \'profiles\'\n                                                        },\n                                        \'TYPE\' => \'INT3\'\n                                      },\n                                      \'category\',\n                                      {\n                                        \'NOTNULL\' => 1,\n                                        \'REFERENCES\' => {\n                                                          \'COLUMN\' => \'id\',\n                                                          \'DELETE\' => \'CASCADE\',\n                                                          \'TABLE\' => \'series_categories\'\n                                                        },\n                                        \'TYPE\' => \'INT2\'\n                                      },\n                                      \'subcategory\',\n                                      {\n                                        \'NOTNULL\' => 1,\n                                        \'REFERENCES\' => {\n                                                          \'COLUMN\' => \'id\',\n                                                          \'DELETE\' => \'CASCADE\',\n                                                          \'TABLE\' => \'series_categories\'\n                                                        },\n                                        \'TYPE\' => \'INT2\'\n                                      },\n                                      \'name\',\n                                      {\n                                        \'NOTNULL\' => 1,\n                                        \'TYPE\' => \'varchar(64)\'\n                                      },\n                                      \'frequency\',\n                                      {\n                                        \'NOTNULL\' => 1,\n                                        \'TYPE\' => \'INT2\'\n                                      },\n                                      \'query\',\n                                      {\n                                        \'NOTNULL\' => 1,\n                                        \'TYPE\' => \'MEDIUMTEXT\'\n                                      },\n                                      \'is_public\',\n                                      {\n                                        \'DEFAULT\' => \'FALSE\',\n                                        \'NOTNULL\' => 1,\n                                        \'TYPE\' => \'BOOLEAN\'\n                                      }\n                                    ],\n                        \'INDEXES\' => [\n                                       \'series_creator_idx\',\n                                       {\n                                         \'FIELDS\' => [\n                                                       \'creator\',\n                                                       \'category\',\n                                                       \'subcategory\',\n                                                       \'name\'\n                                                     ],\n                                         \'TYPE\' => \'UNIQUE\'\n                                       }\n                                     ]\n                      },\n          \'series_categories\' => {\n                                   \'FIELDS\' => [\n                                                 \'id\',\n                                                 {\n                                                   \'NOTNULL\' => 1,\n                                                   \'PRIMARYKEY\' => 1,\n                                                   \'TYPE\' => \'SMALLSERIAL\'\n                                                 },\n                                                 \'name\',\n                                                 {\n                                                   \'NOTNULL\' => 1,\n                                                   \'TYPE\' => \'varchar(64)\'\n                                                 }\n                                               ],\n                                   \'INDEXES\' => [\n                                                  \'series_categories_name_idx\',\n                                                  {\n                                                    \'FIELDS\' => [\n                                                                  \'name\'\n                                                                ],\n                                                    \'TYPE\' => \'UNIQUE\'\n                                                  }\n                                                ]\n                                 },\n          \'series_data\' => {\n                             \'FIELDS\' => [\n                                           \'series_id\',\n                                           {\n                                             \'NOTNULL\' => 1,\n                                             \'REFERENCES\' => {\n                                                               \'COLUMN\' => \'series_id\',\n                                                               \'DELETE\' => \'CASCADE\',\n                                                               \'TABLE\' => \'series\'\n                                                             },\n                                             \'TYPE\' => \'INT3\'\n                                           },\n                                           \'series_date\',\n                                           {\n                                             \'NOTNULL\' => 1,\n                                             \'TYPE\' => \'DATETIME\'\n                                           },\n                                           \'series_value\',\n                                           {\n                                             \'NOTNULL\' => 1,\n                                             \'TYPE\' => \'INT3\'\n                                           }\n                                         ],\n                             \'INDEXES\' => [\n                                            \'series_data_series_id_idx\',\n                                            {\n                                              \'FIELDS\' => [\n                                                            \'series_id\',\n                                                            \'series_date\'\n                                                          ],\n                                              \'TYPE\' => \'UNIQUE\'\n                                            }\n                                          ]\n                           },\n          \'setting\' => {\n                         \'FIELDS\' => [\n                                       \'name\',\n                                       {\n                                         \'NOTNULL\' => 1,\n                                         \'PRIMARYKEY\' => 1,\n                                         \'TYPE\' => \'varchar(32)\'\n                                       },\n                                       \'default_value\',\n                                       {\n                                         \'NOTNULL\' => 1,\n                                         \'TYPE\' => \'varchar(32)\'\n                                       },\n                                       \'is_enabled\',\n                                       {\n                                         \'DEFAULT\' => \'TRUE\',\n                                         \'NOTNULL\' => 1,\n                                         \'TYPE\' => \'BOOLEAN\'\n                                       },\n                                       \'subclass\',\n                                       {\n                                         \'TYPE\' => \'varchar(32)\'\n                                       }\n                                     ]\n                       },\n          \'setting_value\' => {\n                               \'FIELDS\' => [\n                                             \'name\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'REFERENCES\' => {\n                                                                 \'COLUMN\' => \'name\',\n                                                                 \'DELETE\' => \'CASCADE\',\n                                                                 \'TABLE\' => \'setting\'\n                                                               },\n                                               \'TYPE\' => \'varchar(32)\'\n                                             },\n                                             \'value\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'TYPE\' => \'varchar(32)\'\n                                             },\n                                             \'sortindex\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'TYPE\' => \'INT2\'\n                                             }\n                                           ],\n                               \'INDEXES\' => [\n                                              \'setting_value_nv_unique_idx\',\n                                              {\n                                                \'FIELDS\' => [\n                                                              \'name\',\n                                                              \'value\'\n                                                            ],\n                                                \'TYPE\' => \'UNIQUE\'\n                                              },\n                                              \'setting_value_ns_unique_idx\',\n                                              {\n                                                \'FIELDS\' => [\n                                                              \'name\',\n                                                              \'sortindex\'\n                                                            ],\n                                                \'TYPE\' => \'UNIQUE\'\n                                              }\n                                            ]\n                             },\n          \'status_workflow\' => {\n                                 \'FIELDS\' => [\n                                               \'old_status\',\n                                               {\n                                                 \'REFERENCES\' => {\n                                                                   \'COLUMN\' => \'id\',\n                                                                   \'DELETE\' => \'CASCADE\',\n                                                                   \'TABLE\' => \'bug_status\'\n                                                                 },\n                                                 \'TYPE\' => \'INT2\'\n                                               },\n                                               \'new_status\',\n                                               {\n                                                 \'NOTNULL\' => 1,\n                                                 \'REFERENCES\' => {\n                                                                   \'COLUMN\' => \'id\',\n                                                                   \'DELETE\' => \'CASCADE\',\n                                                                   \'TABLE\' => \'bug_status\'\n                                                                 },\n                                                 \'TYPE\' => \'INT2\'\n                                               },\n                                               \'require_comment\',\n                                               {\n                                                 \'DEFAULT\' => 0,\n                                                 \'NOTNULL\' => 1,\n                                                 \'TYPE\' => \'INT1\'\n                                               }\n                                             ],\n                                 \'INDEXES\' => [\n                                                \'status_workflow_idx\',\n                                                {\n                                                  \'FIELDS\' => [\n                                                                \'old_status\',\n                                                                \'new_status\'\n                                                              ],\n                                                  \'TYPE\' => \'UNIQUE\'\n                                                }\n                                              ]\n                               },\n          \'tokens\' => {\n                        \'FIELDS\' => [\n                                      \'userid\',\n                                      {\n                                        \'REFERENCES\' => {\n                                                          \'COLUMN\' => \'userid\',\n                                                          \'DELETE\' => \'CASCADE\',\n                                                          \'TABLE\' => \'profiles\'\n                                                        },\n                                        \'TYPE\' => \'INT3\'\n                                      },\n                                      \'issuedate\',\n                                      {\n                                        \'NOTNULL\' => 1,\n                                        \'TYPE\' => \'DATETIME\'\n                                      },\n                                      \'token\',\n                                      {\n                                        \'NOTNULL\' => 1,\n                                        \'PRIMARYKEY\' => 1,\n                                        \'TYPE\' => \'varchar(16)\'\n                                      },\n                                      \'tokentype\',\n                                      {\n                                        \'NOTNULL\' => 1,\n                                        \'TYPE\' => \'varchar(8)\'\n                                      },\n                                      \'eventdata\',\n                                      {\n                                        \'TYPE\' => \'TINYTEXT\'\n                                      }\n                                    ],\n                        \'INDEXES\' => [\n                                       \'tokens_userid_idx\',\n                                       [\n                                         \'userid\'\n                                       ]\n                                     ]\n                      },\n          \'ts_error\' => {\n                          \'FIELDS\' => [\n                                        \'error_time\',\n                                        {\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'INT4\'\n                                        },\n                                        \'jobid\',\n                                        {\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'INT4\'\n                                        },\n                                        \'message\',\n                                        {\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'varchar(255)\'\n                                        },\n                                        \'funcid\',\n                                        {\n                                          \'DEFAULT\' => 0,\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'INT4\'\n                                        }\n                                      ],\n                          \'INDEXES\' => [\n                                         \'ts_error_funcid_idx\',\n                                         [\n                                           \'funcid\',\n                                           \'error_time\'\n                                         ],\n                                         \'ts_error_error_time_idx\',\n                                         [\n                                           \'error_time\'\n                                         ],\n                                         \'ts_error_jobid_idx\',\n                                         [\n                                           \'jobid\'\n                                         ]\n                                       ]\n                        },\n          \'ts_exitstatus\' => {\n                               \'FIELDS\' => [\n                                             \'jobid\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'PRIMARYKEY\' => 1,\n                                               \'TYPE\' => \'INTSERIAL\'\n                                             },\n                                             \'funcid\',\n                                             {\n                                               \'DEFAULT\' => 0,\n                                               \'NOTNULL\' => 1,\n                                               \'TYPE\' => \'INT4\'\n                                             },\n                                             \'status\',\n                                             {\n                                               \'TYPE\' => \'INT2\'\n                                             },\n                                             \'completion_time\',\n                                             {\n                                               \'TYPE\' => \'INT4\'\n                                             },\n                                             \'delete_after\',\n                                             {\n                                               \'TYPE\' => \'INT4\'\n                                             }\n                                           ],\n                               \'INDEXES\' => [\n                                              \'ts_exitstatus_funcid_idx\',\n                                              [\n                                                \'funcid\'\n                                              ],\n                                              \'ts_exitstatus_delete_after_idx\',\n                                              [\n                                                \'delete_after\'\n                                              ]\n                                            ]\n                             },\n          \'ts_funcmap\' => {\n                            \'FIELDS\' => [\n                                          \'funcid\',\n                                          {\n                                            \'NOTNULL\' => 1,\n                                            \'PRIMARYKEY\' => 1,\n                                            \'TYPE\' => \'INTSERIAL\'\n                                          },\n                                          \'funcname\',\n                                          {\n                                            \'NOTNULL\' => 1,\n                                            \'TYPE\' => \'varchar(255)\'\n                                          }\n                                        ],\n                            \'INDEXES\' => [\n                                           \'ts_funcmap_funcname_idx\',\n                                           {\n                                             \'FIELDS\' => [\n                                                           \'funcname\'\n                                                         ],\n                                             \'TYPE\' => \'UNIQUE\'\n                                           }\n                                         ]\n                          },\n          \'ts_job\' => {\n                        \'FIELDS\' => [\n                                      \'jobid\',\n                                      {\n                                        \'NOTNULL\' => 1,\n                                        \'PRIMARYKEY\' => 1,\n                                        \'TYPE\' => \'INTSERIAL\'\n                                      },\n                                      \'funcid\',\n                                      {\n                                        \'NOTNULL\' => 1,\n                                        \'TYPE\' => \'INT4\'\n                                      },\n                                      \'arg\',\n                                      {\n                                        \'TYPE\' => \'LONGBLOB\'\n                                      },\n                                      \'uniqkey\',\n                                      {\n                                        \'TYPE\' => \'varchar(255)\'\n                                      },\n                                      \'insert_time\',\n                                      {\n                                        \'TYPE\' => \'INT4\'\n                                      },\n                                      \'run_after\',\n                                      {\n                                        \'NOTNULL\' => 1,\n                                        \'TYPE\' => \'INT4\'\n                                      },\n                                      \'grabbed_until\',\n                                      {\n                                        \'NOTNULL\' => 1,\n                                        \'TYPE\' => \'INT4\'\n                                      },\n                                      \'priority\',\n                                      {\n                                        \'TYPE\' => \'INT2\'\n                                      },\n                                      \'coalesce\',\n                                      {\n                                        \'TYPE\' => \'varchar(255)\'\n                                      }\n                                    ],\n                        \'INDEXES\' => [\n                                       \'ts_job_funcid_idx\',\n                                       {\n                                         \'FIELDS\' => [\n                                                       \'funcid\',\n                                                       \'uniqkey\'\n                                                     ],\n                                         \'TYPE\' => \'UNIQUE\'\n                                       },\n                                       \'ts_job_run_after_idx\',\n                                       [\n                                         \'run_after\',\n                                         \'funcid\'\n                                       ],\n                                       \'ts_job_coalesce_idx\',\n                                       [\n                                         \'coalesce\',\n                                         \'funcid\'\n                                       ]\n                                     ]\n                      },\n          \'ts_note\' => {\n                         \'FIELDS\' => [\n                                       \'jobid\',\n                                       {\n                                         \'NOTNULL\' => 1,\n                                         \'TYPE\' => \'INT4\'\n                                       },\n                                       \'notekey\',\n                                       {\n                                         \'TYPE\' => \'varchar(255)\'\n                                       },\n                                       \'value\',\n                                       {\n                                         \'TYPE\' => \'LONGBLOB\'\n                                       }\n                                     ],\n                         \'INDEXES\' => [\n                                        \'ts_note_jobid_idx\',\n                                        {\n                                          \'FIELDS\' => [\n                                                        \'jobid\',\n                                                        \'notekey\'\n                                                      ],\n                                          \'TYPE\' => \'UNIQUE\'\n                                        }\n                                      ]\n                       },\n          \'user_group_map\' => {\n                                \'FIELDS\' => [\n                                              \'user_id\',\n                                              {\n                                                \'NOTNULL\' => 1,\n                                                \'REFERENCES\' => {\n                                                                  \'COLUMN\' => \'userid\',\n                                                                  \'DELETE\' => \'CASCADE\',\n                                                                  \'TABLE\' => \'profiles\'\n                                                                },\n                                                \'TYPE\' => \'INT3\'\n                                              },\n                                              \'group_id\',\n                                              {\n                                                \'NOTNULL\' => 1,\n                                                \'REFERENCES\' => {\n                                                                  \'COLUMN\' => \'id\',\n                                                                  \'DELETE\' => \'CASCADE\',\n                                                                  \'TABLE\' => \'groups\'\n                                                                },\n                                                \'TYPE\' => \'INT3\'\n                                              },\n                                              \'isbless\',\n                                              {\n                                                \'DEFAULT\' => \'FALSE\',\n                                                \'NOTNULL\' => 1,\n                                                \'TYPE\' => \'BOOLEAN\'\n                                              },\n                                              \'grant_type\',\n                                              {\n                                                \'DEFAULT\' => 0,\n                                                \'NOTNULL\' => 1,\n                                                \'TYPE\' => \'INT1\'\n                                              }\n                                            ],\n                                \'INDEXES\' => [\n                                               \'user_group_map_user_id_idx\',\n                                               {\n                                                 \'FIELDS\' => [\n                                                               \'user_id\',\n                                                               \'group_id\',\n                                                               \'grant_type\',\n                                                               \'isbless\'\n                                                             ],\n                                                 \'TYPE\' => \'UNIQUE\'\n                                               }\n                                             ]\n                              },\n          \'versions\' => {\n                          \'FIELDS\' => [\n                                        \'id\',\n                                        {\n                                          \'NOTNULL\' => 1,\n                                          \'PRIMARYKEY\' => 1,\n                                          \'TYPE\' => \'MEDIUMSERIAL\'\n                                        },\n                                        \'value\',\n                                        {\n                                          \'NOTNULL\' => 1,\n                                          \'TYPE\' => \'varchar(64)\'\n                                        },\n                                        \'product_id\',\n                                        {\n                                          \'NOTNULL\' => 1,\n                                          \'REFERENCES\' => {\n                                                            \'COLUMN\' => \'id\',\n                                                            \'DELETE\' => \'CASCADE\',\n                                                            \'TABLE\' => \'products\'\n                                                          },\n                                          \'TYPE\' => \'INT2\'\n                                        }\n                                      ],\n                          \'INDEXES\' => [\n                                         \'versions_product_id_idx\',\n                                         {\n                                           \'FIELDS\' => [\n                                                         \'product_id\',\n                                                         \'value\'\n                                                       ],\n                                           \'TYPE\' => \'UNIQUE\'\n                                         }\n                                       ]\n                        },\n          \'votes\' => {\n                       \'FIELDS\' => [\n                                     \'who\',\n                                     {\n                                       \'NOTNULL\' => 1,\n                                       \'REFERENCES\' => {\n                                                         \'COLUMN\' => \'userid\',\n                                                         \'DELETE\' => \'CASCADE\',\n                                                         \'TABLE\' => \'profiles\'\n                                                       },\n                                       \'TYPE\' => \'INT3\'\n                                     },\n                                     \'bug_id\',\n                                     {\n                                       \'NOTNULL\' => 1,\n                                       \'REFERENCES\' => {\n                                                         \'COLUMN\' => \'bug_id\',\n                                                         \'DELETE\' => \'CASCADE\',\n                                                         \'TABLE\' => \'bugs\'\n                                                       },\n                                       \'TYPE\' => \'INT3\'\n                                     },\n                                     \'vote_count\',\n                                     {\n                                       \'NOTNULL\' => 1,\n                                       \'TYPE\' => \'INT2\'\n                                     }\n                                   ],\n                       \'INDEXES\' => [\n                                      \'votes_who_idx\',\n                                      [\n                                        \'who\'\n                                      ],\n                                      \'votes_bug_id_idx\',\n                                      [\n                                        \'bug_id\'\n                                      ]\n                                    ]\n                     },\n          \'watch\' => {\n                       \'FIELDS\' => [\n                                     \'watcher\',\n                                     {\n                                       \'NOTNULL\' => 1,\n                                       \'REFERENCES\' => {\n                                                         \'COLUMN\' => \'userid\',\n                                                         \'DELETE\' => \'CASCADE\',\n                                                         \'TABLE\' => \'profiles\'\n                                                       },\n                                       \'TYPE\' => \'INT3\'\n                                     },\n                                     \'watched\',\n                                     {\n                                       \'NOTNULL\' => 1,\n                                       \'REFERENCES\' => {\n                                                         \'COLUMN\' => \'userid\',\n                                                         \'DELETE\' => \'CASCADE\',\n                                                         \'TABLE\' => \'profiles\'\n                                                       },\n                                       \'TYPE\' => \'INT3\'\n                                     }\n                                   ],\n                       \'INDEXES\' => [\n                                      \'watch_watcher_idx\',\n                                      {\n                                        \'FIELDS\' => [\n                                                      \'watcher\',\n                                                      \'watched\'\n                                                    ],\n                                        \'TYPE\' => \'UNIQUE\'\n                                      },\n                                      \'watch_watched_idx\',\n                                      [\n                                        \'watched\'\n                                      ]\n                                    ]\n                     },\n          \'whine_events\' => {\n                              \'FIELDS\' => [\n                                            \'id\',\n                                            {\n                                              \'NOTNULL\' => 1,\n                                              \'PRIMARYKEY\' => 1,\n                                              \'TYPE\' => \'MEDIUMSERIAL\'\n                                            },\n                                            \'owner_userid\',\n                                            {\n                                              \'NOTNULL\' => 1,\n                                              \'REFERENCES\' => {\n                                                                \'COLUMN\' => \'userid\',\n                                                                \'DELETE\' => \'CASCADE\',\n                                                                \'TABLE\' => \'profiles\'\n                                                              },\n                                              \'TYPE\' => \'INT3\'\n                                            },\n                                            \'subject\',\n                                            {\n                                              \'TYPE\' => \'varchar(128)\'\n                                            },\n                                            \'body\',\n                                            {\n                                              \'TYPE\' => \'MEDIUMTEXT\'\n                                            },\n                                            \'mailifnobugs\',\n                                            {\n                                              \'DEFAULT\' => \'FALSE\',\n                                              \'NOTNULL\' => 1,\n                                              \'TYPE\' => \'BOOLEAN\'\n                                            }\n                                          ]\n                            },\n          \'whine_queries\' => {\n                               \'FIELDS\' => [\n                                             \'id\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'PRIMARYKEY\' => 1,\n                                               \'TYPE\' => \'MEDIUMSERIAL\'\n                                             },\n                                             \'eventid\',\n                                             {\n                                               \'NOTNULL\' => 1,\n                                               \'REFERENCES\' => {\n                                                                 \'COLUMN\' => \'id\',\n                                                                 \'DELETE\' => \'CASCADE\',\n                                                                 \'TABLE\' => \'whine_events\'\n                                                               },\n                                               \'TYPE\' => \'INT3\'\n                                             },\n                                             \'query_name\',\n                                             {\n                                               \'DEFAULT\' => \'\\\'\\\'\',\n                                               \'NOTNULL\' => 1,\n                                               \'TYPE\' => \'varchar(64)\'\n                                             },\n                                             \'sortkey\',\n                                             {\n                                               \'DEFAULT\' => \'0\',\n                                               \'NOTNULL\' => 1,\n                                               \'TYPE\' => \'INT2\'\n                                             },\n                                             \'onemailperbug\',\n                                             {\n                                               \'DEFAULT\' => \'FALSE\',\n                                               \'NOTNULL\' => 1,\n                                               \'TYPE\' => \'BOOLEAN\'\n                                             },\n                                             \'title\',\n                                             {\n                                               \'DEFAULT\' => \'\\\'\\\'\',\n                                               \'NOTNULL\' => 1,\n                                               \'TYPE\' => \'varchar(128)\'\n                                             }\n                                           ],\n                               \'INDEXES\' => [\n                                              \'whine_queries_eventid_idx\',\n                                              [\n                                                \'eventid\'\n                                              ]\n                                            ]\n                             },\n          \'whine_schedules\' => {\n                                 \'FIELDS\' => [\n                                               \'id\',\n                                               {\n                                                 \'NOTNULL\' => 1,\n                                                 \'PRIMARYKEY\' => 1,\n                                                 \'TYPE\' => \'MEDIUMSERIAL\'\n                                               },\n                                               \'eventid\',\n                                               {\n                                                 \'NOTNULL\' => 1,\n                                                 \'REFERENCES\' => {\n                                                                   \'COLUMN\' => \'id\',\n                                                                   \'DELETE\' => \'CASCADE\',\n                                                                   \'TABLE\' => \'whine_events\'\n                                                                 },\n                                                 \'TYPE\' => \'INT3\'\n                                               },\n                                               \'run_day\',\n                                               {\n                                                 \'TYPE\' => \'varchar(32)\'\n                                               },\n                                               \'run_time\',\n                                               {\n                                                 \'TYPE\' => \'varchar(32)\'\n                                               },\n                                               \'run_next\',\n                                               {\n                                                 \'TYPE\' => \'DATETIME\'\n                                               },\n                                               \'mailto\',\n                                               {\n                                                 \'NOTNULL\' => 1,\n                                                 \'TYPE\' => \'INT3\'\n                                               },\n                                               \'mailto_type\',\n                                               {\n                                                 \'DEFAULT\' => \'0\',\n                                                 \'NOTNULL\' => 1,\n                                                 \'TYPE\' => \'INT2\'\n                                               }\n                                             ],\n                                 \'INDEXES\' => [\n                                                \'whine_schedules_run_next_idx\',\n                                                [\n                                                  \'run_next\'\n                                                ],\n                                                \'whine_schedules_eventid_idx\',\n                                                [\n                                                  \'eventid\'\n                                                ]\n                                              ]\n                               }\n        };\n','2.00');
/*!40000 ALTER TABLE `bz_schema` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category_group_map`
--

DROP TABLE IF EXISTS `category_group_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `category_group_map` (
  `category_id` smallint(6) NOT NULL,
  `group_id` mediumint(9) NOT NULL,
  UNIQUE KEY `category_group_map_category_id_idx` (`category_id`,`group_id`),
  KEY `fk_category_group_map_group_id_groups_id` (`group_id`),
  CONSTRAINT `fk_category_group_map_category_id_series_categories_id` FOREIGN KEY (`category_id`) REFERENCES `series_categories` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_category_group_map_group_id_groups_id` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category_group_map`
--

LOCK TABLES `category_group_map` WRITE;
/*!40000 ALTER TABLE `category_group_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `category_group_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cc`
--

DROP TABLE IF EXISTS `cc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cc` (
  `bug_id` mediumint(9) NOT NULL,
  `who` mediumint(9) NOT NULL,
  UNIQUE KEY `cc_bug_id_idx` (`bug_id`,`who`),
  KEY `cc_who_idx` (`who`),
  CONSTRAINT `fk_cc_bug_id_bugs_bug_id` FOREIGN KEY (`bug_id`) REFERENCES `bugs` (`bug_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_cc_who_profiles_userid` FOREIGN KEY (`who`) REFERENCES `profiles` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cc`
--

LOCK TABLES `cc` WRITE;
/*!40000 ALTER TABLE `cc` DISABLE KEYS */;
INSERT INTO `cc` VALUES (18,1),(19,1),(20,1),(21,1),(22,1),(23,1),(24,1),(25,1),(26,1),(27,1),(28,1),(29,1),(30,1),(31,1),(32,1),(34,1),(35,1),(36,1),(1,2),(2,2),(5,2),(5,3),(14,3),(15,3),(16,3),(17,3),(19,3),(20,3),(21,3),(22,3),(23,3),(24,3),(25,3),(26,3),(27,3),(28,3),(29,3),(30,3),(31,3),(32,3),(34,3),(35,3),(36,3),(7,4),(39,5),(40,5),(41,5),(42,5),(39,6),(40,6),(41,6),(42,6),(39,7),(40,7),(41,7),(42,7),(39,8),(40,8),(41,8),(42,8),(40,9),(41,9);
/*!40000 ALTER TABLE `cc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cf_os`
--

DROP TABLE IF EXISTS `cf_os`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cf_os` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `value` varchar(64) NOT NULL,
  `sortkey` smallint(6) NOT NULL DEFAULT '0',
  `isactive` tinyint(4) NOT NULL DEFAULT '1',
  `visibility_value_id` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `cf_os_value_idx` (`value`),
  KEY `cf_os_sortkey_idx` (`sortkey`,`value`),
  KEY `cf_os_visibility_value_id_idx` (`visibility_value_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cf_os`
--

LOCK TABLES `cf_os` WRITE;
/*!40000 ALTER TABLE `cf_os` DISABLE KEYS */;
INSERT INTO `cf_os` VALUES (1,'---',0,1,NULL),(2,'Linux',1,1,NULL),(3,'Mac',2,1,NULL);
/*!40000 ALTER TABLE `cf_os` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `classifications`
--

DROP TABLE IF EXISTS `classifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `classifications` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `description` mediumtext,
  `sortkey` smallint(6) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `classifications_name_idx` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `classifications`
--

LOCK TABLES `classifications` WRITE;
/*!40000 ALTER TABLE `classifications` DISABLE KEYS */;
INSERT INTO `classifications` VALUES (1,'Unclassified','Not assigned to any classification',0);
/*!40000 ALTER TABLE `classifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `component_cc`
--

DROP TABLE IF EXISTS `component_cc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `component_cc` (
  `user_id` mediumint(9) NOT NULL,
  `component_id` smallint(6) NOT NULL,
  UNIQUE KEY `component_cc_user_id_idx` (`component_id`,`user_id`),
  KEY `fk_component_cc_user_id_profiles_userid` (`user_id`),
  CONSTRAINT `fk_component_cc_component_id_components_id` FOREIGN KEY (`component_id`) REFERENCES `components` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_component_cc_user_id_profiles_userid` FOREIGN KEY (`user_id`) REFERENCES `profiles` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `component_cc`
--

LOCK TABLES `component_cc` WRITE;
/*!40000 ALTER TABLE `component_cc` DISABLE KEYS */;
/*!40000 ALTER TABLE `component_cc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `components`
--

DROP TABLE IF EXISTS `components`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `components` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `product_id` smallint(6) NOT NULL,
  `initialowner` mediumint(9) NOT NULL,
  `initialqacontact` mediumint(9) DEFAULT NULL,
  `description` mediumtext NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `components_product_id_idx` (`product_id`,`name`),
  KEY `components_name_idx` (`name`),
  KEY `fk_components_initialowner_profiles_userid` (`initialowner`),
  KEY `fk_components_initialqacontact_profiles_userid` (`initialqacontact`),
  CONSTRAINT `fk_components_initialowner_profiles_userid` FOREIGN KEY (`initialowner`) REFERENCES `profiles` (`userid`) ON UPDATE CASCADE,
  CONSTRAINT `fk_components_initialqacontact_profiles_userid` FOREIGN KEY (`initialqacontact`) REFERENCES `profiles` (`userid`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_components_product_id_products_id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `components`
--

LOCK TABLES `components` WRITE;
/*!40000 ALTER TABLE `components` DISABLE KEYS */;
INSERT INTO `components` VALUES (1,'TestComponent',1,1,NULL,'This is a test component in the test product database. This ought to be blown away and replaced with real stuff in a finished installation of Bugzilla.'),(2,'Testing',2,2,NULL,'Testing'),(3,'Component1',1,5,NULL,'abc'),(4,'Component2',1,6,NULL,'abc2'),(5,'Component3',1,7,NULL,'xfds'),(6,'Component4',1,8,NULL,'fsdlj');
/*!40000 ALTER TABLE `components` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dependencies`
--

DROP TABLE IF EXISTS `dependencies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dependencies` (
  `blocked` mediumint(9) NOT NULL,
  `dependson` mediumint(9) NOT NULL,
  KEY `dependencies_blocked_idx` (`blocked`),
  KEY `dependencies_dependson_idx` (`dependson`),
  CONSTRAINT `fk_dependencies_blocked_bugs_bug_id` FOREIGN KEY (`blocked`) REFERENCES `bugs` (`bug_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_dependencies_dependson_bugs_bug_id` FOREIGN KEY (`dependson`) REFERENCES `bugs` (`bug_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dependencies`
--

LOCK TABLES `dependencies` WRITE;
/*!40000 ALTER TABLE `dependencies` DISABLE KEYS */;
INSERT INTO `dependencies` VALUES (5,3),(2,5),(21,20),(22,20),(19,22),(23,20),(19,23),(24,20),(19,24),(25,20),(19,25),(26,20),(19,26),(27,20),(19,27),(28,20),(19,28),(29,20),(19,29),(30,20),(19,30),(31,20),(19,31),(32,20),(19,32),(34,20),(19,34),(35,20),(19,35),(36,20),(19,36),(39,17),(39,15),(40,10),(41,10),(41,11),(41,12),(20,41),(42,10),(42,11),(42,12),(42,13),(20,42);
/*!40000 ALTER TABLE `dependencies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `duplicates`
--

DROP TABLE IF EXISTS `duplicates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `duplicates` (
  `dupe_of` mediumint(9) NOT NULL,
  `dupe` mediumint(9) NOT NULL,
  PRIMARY KEY (`dupe`),
  KEY `fk_duplicates_dupe_of_bugs_bug_id` (`dupe_of`),
  CONSTRAINT `fk_duplicates_dupe_bugs_bug_id` FOREIGN KEY (`dupe`) REFERENCES `bugs` (`bug_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_duplicates_dupe_of_bugs_bug_id` FOREIGN KEY (`dupe_of`) REFERENCES `bugs` (`bug_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `duplicates`
--

LOCK TABLES `duplicates` WRITE;
/*!40000 ALTER TABLE `duplicates` DISABLE KEYS */;
INSERT INTO `duplicates` VALUES (2,6),(16,32),(16,34),(16,35),(16,36);
/*!40000 ALTER TABLE `duplicates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `email_setting`
--

DROP TABLE IF EXISTS `email_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `email_setting` (
  `user_id` mediumint(9) NOT NULL,
  `relationship` tinyint(4) NOT NULL,
  `event` tinyint(4) NOT NULL,
  UNIQUE KEY `email_setting_user_id_idx` (`user_id`,`relationship`,`event`),
  CONSTRAINT `fk_email_setting_user_id_profiles_userid` FOREIGN KEY (`user_id`) REFERENCES `profiles` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `email_setting`
--

LOCK TABLES `email_setting` WRITE;
/*!40000 ALTER TABLE `email_setting` DISABLE KEYS */;
INSERT INTO `email_setting` VALUES (1,0,0),(1,0,1),(1,0,2),(1,0,3),(1,0,4),(1,0,5),(1,0,6),(1,0,7),(1,0,9),(1,0,10),(1,0,50),(1,1,0),(1,1,1),(1,1,2),(1,1,3),(1,1,4),(1,1,5),(1,1,6),(1,1,7),(1,1,9),(1,1,10),(1,1,50),(1,2,0),(1,2,1),(1,2,2),(1,2,3),(1,2,4),(1,2,5),(1,2,6),(1,2,7),(1,2,8),(1,2,9),(1,2,10),(1,2,50),(1,3,0),(1,3,1),(1,3,2),(1,3,3),(1,3,4),(1,3,5),(1,3,6),(1,3,7),(1,3,9),(1,3,10),(1,3,50),(1,4,0),(1,4,1),(1,4,2),(1,4,3),(1,4,4),(1,4,5),(1,4,6),(1,4,7),(1,4,9),(1,4,10),(1,4,50),(1,5,0),(1,5,1),(1,5,2),(1,5,3),(1,5,4),(1,5,5),(1,5,6),(1,5,7),(1,5,9),(1,5,10),(1,5,50),(1,100,100),(1,100,101),(2,0,0),(2,0,1),(2,0,2),(2,0,3),(2,0,4),(2,0,5),(2,0,6),(2,0,7),(2,0,9),(2,0,10),(2,0,50),(2,1,0),(2,1,1),(2,1,2),(2,1,3),(2,1,4),(2,1,5),(2,1,6),(2,1,7),(2,1,9),(2,1,10),(2,1,50),(2,2,0),(2,2,1),(2,2,2),(2,2,3),(2,2,4),(2,2,5),(2,2,6),(2,2,7),(2,2,8),(2,2,9),(2,2,10),(2,2,50),(2,3,0),(2,3,1),(2,3,2),(2,3,3),(2,3,4),(2,3,5),(2,3,6),(2,3,7),(2,3,9),(2,3,10),(2,3,50),(2,4,0),(2,4,1),(2,4,2),(2,4,3),(2,4,4),(2,4,5),(2,4,6),(2,4,7),(2,4,9),(2,4,10),(2,4,50),(2,5,0),(2,5,1),(2,5,2),(2,5,3),(2,5,4),(2,5,5),(2,5,6),(2,5,7),(2,5,9),(2,5,10),(2,5,50),(2,100,100),(2,100,101),(3,0,0),(3,0,1),(3,0,2),(3,0,3),(3,0,4),(3,0,5),(3,0,6),(3,0,7),(3,0,9),(3,0,10),(3,0,50),(3,1,0),(3,1,1),(3,1,2),(3,1,3),(3,1,4),(3,1,5),(3,1,6),(3,1,7),(3,1,9),(3,1,10),(3,1,50),(3,2,0),(3,2,1),(3,2,2),(3,2,3),(3,2,4),(3,2,5),(3,2,6),(3,2,7),(3,2,8),(3,2,9),(3,2,10),(3,2,50),(3,3,0),(3,3,1),(3,3,2),(3,3,3),(3,3,4),(3,3,5),(3,3,6),(3,3,7),(3,3,9),(3,3,10),(3,3,50),(3,4,0),(3,4,1),(3,4,2),(3,4,3),(3,4,4),(3,4,5),(3,4,6),(3,4,7),(3,4,9),(3,4,10),(3,4,50),(3,5,0),(3,5,1),(3,5,2),(3,5,3),(3,5,4),(3,5,5),(3,5,6),(3,5,7),(3,5,9),(3,5,10),(3,5,50),(3,100,100),(3,100,101),(4,0,0),(4,0,1),(4,0,2),(4,0,3),(4,0,4),(4,0,5),(4,0,6),(4,0,7),(4,0,9),(4,0,10),(4,0,50),(4,1,0),(4,1,1),(4,1,2),(4,1,3),(4,1,4),(4,1,5),(4,1,6),(4,1,7),(4,1,9),(4,1,10),(4,1,50),(4,2,0),(4,2,1),(4,2,2),(4,2,3),(4,2,4),(4,2,5),(4,2,6),(4,2,7),(4,2,8),(4,2,9),(4,2,10),(4,2,50),(4,3,0),(4,3,1),(4,3,2),(4,3,3),(4,3,4),(4,3,5),(4,3,6),(4,3,7),(4,3,9),(4,3,10),(4,3,50),(4,4,0),(4,4,1),(4,4,2),(4,4,3),(4,4,4),(4,4,5),(4,4,6),(4,4,7),(4,4,9),(4,4,10),(4,4,50),(4,5,0),(4,5,1),(4,5,2),(4,5,3),(4,5,4),(4,5,5),(4,5,6),(4,5,7),(4,5,9),(4,5,10),(4,5,50),(4,100,100),(4,100,101),(5,0,0),(5,0,1),(5,0,2),(5,0,3),(5,0,4),(5,0,5),(5,0,6),(5,0,7),(5,0,9),(5,0,10),(5,0,50),(5,1,0),(5,1,1),(5,1,2),(5,1,3),(5,1,4),(5,1,5),(5,1,6),(5,1,7),(5,1,9),(5,1,10),(5,1,50),(5,2,0),(5,2,1),(5,2,2),(5,2,3),(5,2,4),(5,2,5),(5,2,6),(5,2,7),(5,2,8),(5,2,9),(5,2,10),(5,2,50),(5,3,0),(5,3,1),(5,3,2),(5,3,3),(5,3,4),(5,3,5),(5,3,6),(5,3,7),(5,3,9),(5,3,10),(5,3,50),(5,4,0),(5,4,1),(5,4,2),(5,4,3),(5,4,4),(5,4,5),(5,4,6),(5,4,7),(5,4,9),(5,4,10),(5,4,50),(5,5,0),(5,5,1),(5,5,2),(5,5,3),(5,5,4),(5,5,5),(5,5,6),(5,5,7),(5,5,9),(5,5,10),(5,5,50),(5,100,100),(5,100,101),(6,0,0),(6,0,1),(6,0,2),(6,0,3),(6,0,4),(6,0,5),(6,0,6),(6,0,7),(6,0,9),(6,0,10),(6,0,50),(6,1,0),(6,1,1),(6,1,2),(6,1,3),(6,1,4),(6,1,5),(6,1,6),(6,1,7),(6,1,9),(6,1,10),(6,1,50),(6,2,0),(6,2,1),(6,2,2),(6,2,3),(6,2,4),(6,2,5),(6,2,6),(6,2,7),(6,2,8),(6,2,9),(6,2,10),(6,2,50),(6,3,0),(6,3,1),(6,3,2),(6,3,3),(6,3,4),(6,3,5),(6,3,6),(6,3,7),(6,3,9),(6,3,10),(6,3,50),(6,4,0),(6,4,1),(6,4,2),(6,4,3),(6,4,4),(6,4,5),(6,4,6),(6,4,7),(6,4,9),(6,4,10),(6,4,50),(6,5,0),(6,5,1),(6,5,2),(6,5,3),(6,5,4),(6,5,5),(6,5,6),(6,5,7),(6,5,9),(6,5,10),(6,5,50),(6,100,100),(6,100,101),(7,0,0),(7,0,1),(7,0,2),(7,0,3),(7,0,4),(7,0,5),(7,0,6),(7,0,7),(7,0,9),(7,0,10),(7,0,50),(7,1,0),(7,1,1),(7,1,2),(7,1,3),(7,1,4),(7,1,5),(7,1,6),(7,1,7),(7,1,9),(7,1,10),(7,1,50),(7,2,0),(7,2,1),(7,2,2),(7,2,3),(7,2,4),(7,2,5),(7,2,6),(7,2,7),(7,2,8),(7,2,9),(7,2,10),(7,2,50),(7,3,0),(7,3,1),(7,3,2),(7,3,3),(7,3,4),(7,3,5),(7,3,6),(7,3,7),(7,3,9),(7,3,10),(7,3,50),(7,4,0),(7,4,1),(7,4,2),(7,4,3),(7,4,4),(7,4,5),(7,4,6),(7,4,7),(7,4,9),(7,4,10),(7,4,50),(7,5,0),(7,5,1),(7,5,2),(7,5,3),(7,5,4),(7,5,5),(7,5,6),(7,5,7),(7,5,9),(7,5,10),(7,5,50),(7,100,100),(7,100,101),(8,0,0),(8,0,1),(8,0,2),(8,0,3),(8,0,4),(8,0,5),(8,0,6),(8,0,7),(8,0,9),(8,0,10),(8,0,50),(8,1,0),(8,1,1),(8,1,2),(8,1,3),(8,1,4),(8,1,5),(8,1,6),(8,1,7),(8,1,9),(8,1,10),(8,1,50),(8,2,0),(8,2,1),(8,2,2),(8,2,3),(8,2,4),(8,2,5),(8,2,6),(8,2,7),(8,2,8),(8,2,9),(8,2,10),(8,2,50),(8,3,0),(8,3,1),(8,3,2),(8,3,3),(8,3,4),(8,3,5),(8,3,6),(8,3,7),(8,3,9),(8,3,10),(8,3,50),(8,4,0),(8,4,1),(8,4,2),(8,4,3),(8,4,4),(8,4,5),(8,4,6),(8,4,7),(8,4,9),(8,4,10),(8,4,50),(8,5,0),(8,5,1),(8,5,2),(8,5,3),(8,5,4),(8,5,5),(8,5,6),(8,5,7),(8,5,9),(8,5,10),(8,5,50),(8,100,100),(8,100,101),(9,0,0),(9,0,1),(9,0,2),(9,0,3),(9,0,4),(9,0,5),(9,0,6),(9,0,7),(9,0,9),(9,0,10),(9,0,50),(9,1,0),(9,1,1),(9,1,2),(9,1,3),(9,1,4),(9,1,5),(9,1,6),(9,1,7),(9,1,9),(9,1,10),(9,1,50),(9,2,0),(9,2,1),(9,2,2),(9,2,3),(9,2,4),(9,2,5),(9,2,6),(9,2,7),(9,2,8),(9,2,9),(9,2,10),(9,2,50),(9,3,0),(9,3,1),(9,3,2),(9,3,3),(9,3,4),(9,3,5),(9,3,6),(9,3,7),(9,3,9),(9,3,10),(9,3,50),(9,4,0),(9,4,1),(9,4,2),(9,4,3),(9,4,4),(9,4,5),(9,4,6),(9,4,7),(9,4,9),(9,4,10),(9,4,50),(9,5,0),(9,5,1),(9,5,2),(9,5,3),(9,5,4),(9,5,5),(9,5,6),(9,5,7),(9,5,9),(9,5,10),(9,5,50),(9,100,100),(9,100,101),(10,0,0),(10,0,1),(10,0,2),(10,0,3),(10,0,4),(10,0,5),(10,0,6),(10,0,7),(10,0,9),(10,0,10),(10,0,50),(10,1,0),(10,1,1),(10,1,2),(10,1,3),(10,1,4),(10,1,5),(10,1,6),(10,1,7),(10,1,9),(10,1,10),(10,1,50),(10,2,0),(10,2,1),(10,2,2),(10,2,3),(10,2,4),(10,2,5),(10,2,6),(10,2,7),(10,2,8),(10,2,9),(10,2,10),(10,2,50),(10,3,0),(10,3,1),(10,3,2),(10,3,3),(10,3,4),(10,3,5),(10,3,6),(10,3,7),(10,3,9),(10,3,10),(10,3,50),(10,4,0),(10,4,1),(10,4,2),(10,4,3),(10,4,4),(10,4,5),(10,4,6),(10,4,7),(10,4,9),(10,4,10),(10,4,50),(10,5,0),(10,5,1),(10,5,2),(10,5,3),(10,5,4),(10,5,5),(10,5,6),(10,5,7),(10,5,9),(10,5,10),(10,5,50),(10,100,100),(10,100,101);
/*!40000 ALTER TABLE `email_setting` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fielddefs`
--

DROP TABLE IF EXISTS `fielddefs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fielddefs` (
  `id` mediumint(9) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `type` smallint(6) NOT NULL DEFAULT '0',
  `custom` tinyint(4) NOT NULL DEFAULT '0',
  `description` tinytext NOT NULL,
  `mailhead` tinyint(4) NOT NULL DEFAULT '0',
  `sortkey` smallint(6) NOT NULL,
  `obsolete` tinyint(4) NOT NULL DEFAULT '0',
  `enter_bug` tinyint(4) NOT NULL DEFAULT '0',
  `buglist` tinyint(4) NOT NULL DEFAULT '0',
  `visibility_field_id` mediumint(9) DEFAULT NULL,
  `visibility_value_id` smallint(6) DEFAULT NULL,
  `value_field_id` mediumint(9) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `fielddefs_name_idx` (`name`),
  KEY `fielddefs_sortkey_idx` (`sortkey`),
  KEY `fielddefs_value_field_id_idx` (`value_field_id`),
  KEY `fk_fielddefs_visibility_field_id_fielddefs_id` (`visibility_field_id`),
  CONSTRAINT `fk_fielddefs_value_field_id_fielddefs_id` FOREIGN KEY (`value_field_id`) REFERENCES `fielddefs` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_fielddefs_visibility_field_id_fielddefs_id` FOREIGN KEY (`visibility_field_id`) REFERENCES `fielddefs` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fielddefs`
--

LOCK TABLES `fielddefs` WRITE;
/*!40000 ALTER TABLE `fielddefs` DISABLE KEYS */;
INSERT INTO `fielddefs` VALUES (1,'bug_id',0,0,'Bug #',1,100,0,0,1,NULL,NULL,NULL),(2,'short_desc',0,0,'Summary',1,200,0,0,1,NULL,NULL,NULL),(3,'classification',0,0,'Classification',1,300,0,0,1,NULL,NULL,NULL),(4,'product',2,0,'Product',1,400,0,0,1,NULL,NULL,NULL),(5,'version',0,0,'Version',1,500,0,0,1,NULL,NULL,NULL),(6,'rep_platform',2,0,'Platform',1,600,0,0,1,NULL,NULL,NULL),(7,'bug_file_loc',0,0,'URL',1,700,0,0,0,NULL,NULL,NULL),(8,'op_sys',2,0,'OS/Version',1,800,0,0,1,NULL,NULL,NULL),(9,'bug_status',2,0,'Status',1,900,0,0,1,NULL,NULL,NULL),(10,'status_whiteboard',0,0,'Status Whiteboard',1,1000,0,0,1,NULL,NULL,NULL),(11,'keywords',0,0,'Keywords',1,1100,0,0,1,NULL,NULL,NULL),(12,'resolution',2,0,'Resolution',0,1200,0,0,1,NULL,NULL,NULL),(13,'bug_severity',2,0,'Severity',1,1300,0,0,1,NULL,NULL,NULL),(14,'priority',2,0,'Priority',1,1400,0,0,1,NULL,NULL,NULL),(15,'component',0,0,'Component',1,1500,0,0,1,NULL,NULL,NULL),(16,'assigned_to',0,0,'AssignedTo',1,1600,0,0,1,NULL,NULL,NULL),(17,'reporter',0,0,'ReportedBy',1,1700,0,0,1,NULL,NULL,NULL),(18,'votes',0,0,'Votes',0,1800,0,0,1,NULL,NULL,NULL),(19,'qa_contact',0,0,'QAContact',1,1900,0,0,1,NULL,NULL,NULL),(20,'cc',0,0,'CC',1,2000,0,0,0,NULL,NULL,NULL),(21,'dependson',0,0,'Depends on',1,2100,0,0,0,NULL,NULL,NULL),(22,'blocked',0,0,'Blocks',1,2200,0,0,0,NULL,NULL,NULL),(23,'attachments.description',0,0,'Attachment description',0,2300,0,0,0,NULL,NULL,NULL),(24,'attachments.filename',0,0,'Attachment filename',0,2400,0,0,0,NULL,NULL,NULL),(25,'attachments.mimetype',0,0,'Attachment mime type',0,2500,0,0,0,NULL,NULL,NULL),(26,'attachments.ispatch',0,0,'Attachment is patch',0,2600,0,0,0,NULL,NULL,NULL),(27,'attachments.isobsolete',0,0,'Attachment is obsolete',0,2700,0,0,0,NULL,NULL,NULL),(28,'attachments.isprivate',0,0,'Attachment is private',0,2800,0,0,0,NULL,NULL,NULL),(29,'attachments.submitter',0,0,'Attachment creator',0,2900,0,0,0,NULL,NULL,NULL),(30,'target_milestone',0,0,'Target Milestone',0,3000,0,0,1,NULL,NULL,NULL),(31,'creation_ts',0,0,'Creation date',1,3100,0,0,1,NULL,NULL,NULL),(32,'delta_ts',0,0,'Last changed date',1,3200,0,0,1,NULL,NULL,NULL),(33,'longdesc',0,0,'Comment',0,3300,0,0,0,NULL,NULL,NULL),(34,'longdescs.isprivate',0,0,'Comment is private',0,3400,0,0,0,NULL,NULL,NULL),(35,'alias',0,0,'Alias',0,3500,0,0,1,NULL,NULL,NULL),(36,'everconfirmed',0,0,'Ever Confirmed',0,3600,0,0,0,NULL,NULL,NULL),(37,'reporter_accessible',0,0,'Reporter Accessible',0,3700,0,0,0,NULL,NULL,NULL),(38,'cclist_accessible',0,0,'CC Accessible',0,3800,0,0,0,NULL,NULL,NULL),(39,'bug_group',0,0,'Group',1,3900,0,0,0,NULL,NULL,NULL),(40,'estimated_time',0,0,'Estimated Hours',1,4000,0,0,1,NULL,NULL,NULL),(41,'remaining_time',0,0,'Remaining Hours',0,4100,0,0,1,NULL,NULL,NULL),(42,'deadline',0,0,'Deadline',1,4200,0,0,1,NULL,NULL,NULL),(43,'commenter',0,0,'Commenter',0,4300,0,0,0,NULL,NULL,NULL),(44,'flagtypes.name',0,0,'Flags',0,4400,0,0,1,NULL,NULL,NULL),(45,'requestees.login_name',0,0,'Flag Requestee',0,4500,0,0,0,NULL,NULL,NULL),(46,'setters.login_name',0,0,'Flag Setter',0,4600,0,0,0,NULL,NULL,NULL),(47,'work_time',0,0,'Hours Worked',0,4700,0,0,1,NULL,NULL,NULL),(48,'percentage_complete',0,0,'Percentage Complete',0,4800,0,0,1,NULL,NULL,NULL),(49,'content',0,0,'Content',0,4900,0,0,0,NULL,NULL,NULL),(50,'attach_data.thedata',0,0,'Attachment data',0,5000,0,0,0,NULL,NULL,NULL),(51,'attachments.isurl',0,0,'Attachment is a URL',0,5100,0,0,0,NULL,NULL,NULL),(52,'owner_idle_time',0,0,'Time Since Assignee Touched',0,5200,0,0,0,NULL,NULL,NULL),(53,'see_also',7,0,'See Also',0,5300,0,0,0,NULL,NULL,NULL),(54,'days_elapsed',0,0,'Days since bug changed',0,5400,0,0,0,NULL,NULL,NULL),(55,'cf_os',2,1,'Operating system',0,5500,0,1,1,NULL,NULL,NULL);
/*!40000 ALTER TABLE `fielddefs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flagexclusions`
--

DROP TABLE IF EXISTS `flagexclusions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `flagexclusions` (
  `type_id` smallint(6) NOT NULL,
  `product_id` smallint(6) DEFAULT NULL,
  `component_id` smallint(6) DEFAULT NULL,
  KEY `flagexclusions_type_id_idx` (`type_id`,`product_id`,`component_id`),
  KEY `fk_flagexclusions_product_id_products_id` (`product_id`),
  KEY `fk_flagexclusions_component_id_components_id` (`component_id`),
  CONSTRAINT `fk_flagexclusions_component_id_components_id` FOREIGN KEY (`component_id`) REFERENCES `components` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_flagexclusions_product_id_products_id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_flagexclusions_type_id_flagtypes_id` FOREIGN KEY (`type_id`) REFERENCES `flagtypes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flagexclusions`
--

LOCK TABLES `flagexclusions` WRITE;
/*!40000 ALTER TABLE `flagexclusions` DISABLE KEYS */;
/*!40000 ALTER TABLE `flagexclusions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flaginclusions`
--

DROP TABLE IF EXISTS `flaginclusions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `flaginclusions` (
  `type_id` smallint(6) NOT NULL,
  `product_id` smallint(6) DEFAULT NULL,
  `component_id` smallint(6) DEFAULT NULL,
  KEY `flaginclusions_type_id_idx` (`type_id`,`product_id`,`component_id`),
  KEY `fk_flaginclusions_product_id_products_id` (`product_id`),
  KEY `fk_flaginclusions_component_id_components_id` (`component_id`),
  CONSTRAINT `fk_flaginclusions_component_id_components_id` FOREIGN KEY (`component_id`) REFERENCES `components` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_flaginclusions_product_id_products_id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_flaginclusions_type_id_flagtypes_id` FOREIGN KEY (`type_id`) REFERENCES `flagtypes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flaginclusions`
--

LOCK TABLES `flaginclusions` WRITE;
/*!40000 ALTER TABLE `flaginclusions` DISABLE KEYS */;
/*!40000 ALTER TABLE `flaginclusions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flags`
--

DROP TABLE IF EXISTS `flags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `flags` (
  `id` mediumint(9) NOT NULL AUTO_INCREMENT,
  `type_id` smallint(6) NOT NULL,
  `status` char(1) NOT NULL,
  `bug_id` mediumint(9) NOT NULL,
  `attach_id` mediumint(9) DEFAULT NULL,
  `creation_date` datetime NOT NULL,
  `modification_date` datetime DEFAULT NULL,
  `setter_id` mediumint(9) DEFAULT NULL,
  `requestee_id` mediumint(9) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `flags_bug_id_idx` (`bug_id`,`attach_id`),
  KEY `flags_setter_id_idx` (`setter_id`),
  KEY `flags_requestee_id_idx` (`requestee_id`),
  KEY `flags_type_id_idx` (`type_id`),
  KEY `fk_flags_attach_id_attachments_attach_id` (`attach_id`),
  CONSTRAINT `fk_flags_attach_id_attachments_attach_id` FOREIGN KEY (`attach_id`) REFERENCES `attachments` (`attach_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_flags_bug_id_bugs_bug_id` FOREIGN KEY (`bug_id`) REFERENCES `bugs` (`bug_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_flags_requestee_id_profiles_userid` FOREIGN KEY (`requestee_id`) REFERENCES `profiles` (`userid`) ON UPDATE CASCADE,
  CONSTRAINT `fk_flags_setter_id_profiles_userid` FOREIGN KEY (`setter_id`) REFERENCES `profiles` (`userid`) ON UPDATE CASCADE,
  CONSTRAINT `fk_flags_type_id_flagtypes_id` FOREIGN KEY (`type_id`) REFERENCES `flagtypes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flags`
--

LOCK TABLES `flags` WRITE;
/*!40000 ALTER TABLE `flags` DISABLE KEYS */;
/*!40000 ALTER TABLE `flags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flagtypes`
--

DROP TABLE IF EXISTS `flagtypes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `flagtypes` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `description` mediumtext NOT NULL,
  `cc_list` varchar(200) DEFAULT NULL,
  `target_type` char(1) NOT NULL DEFAULT 'b',
  `is_active` tinyint(4) NOT NULL DEFAULT '1',
  `is_requestable` tinyint(4) NOT NULL DEFAULT '0',
  `is_requesteeble` tinyint(4) NOT NULL DEFAULT '0',
  `is_multiplicable` tinyint(4) NOT NULL DEFAULT '0',
  `sortkey` smallint(6) NOT NULL DEFAULT '0',
  `grant_group_id` mediumint(9) DEFAULT NULL,
  `request_group_id` mediumint(9) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_flagtypes_grant_group_id_groups_id` (`grant_group_id`),
  KEY `fk_flagtypes_request_group_id_groups_id` (`request_group_id`),
  CONSTRAINT `fk_flagtypes_grant_group_id_groups_id` FOREIGN KEY (`grant_group_id`) REFERENCES `groups` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_flagtypes_request_group_id_groups_id` FOREIGN KEY (`request_group_id`) REFERENCES `groups` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flagtypes`
--

LOCK TABLES `flagtypes` WRITE;
/*!40000 ALTER TABLE `flagtypes` DISABLE KEYS */;
/*!40000 ALTER TABLE `flagtypes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_control_map`
--

DROP TABLE IF EXISTS `group_control_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group_control_map` (
  `group_id` mediumint(9) NOT NULL,
  `product_id` smallint(6) NOT NULL,
  `entry` tinyint(4) NOT NULL DEFAULT '0',
  `membercontrol` tinyint(4) NOT NULL,
  `othercontrol` tinyint(4) NOT NULL,
  `canedit` tinyint(4) NOT NULL DEFAULT '0',
  `editcomponents` tinyint(4) NOT NULL DEFAULT '0',
  `editbugs` tinyint(4) NOT NULL DEFAULT '0',
  `canconfirm` tinyint(4) NOT NULL DEFAULT '0',
  UNIQUE KEY `group_control_map_product_id_idx` (`product_id`,`group_id`),
  KEY `group_control_map_group_id_idx` (`group_id`),
  CONSTRAINT `fk_group_control_map_group_id_groups_id` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_group_control_map_product_id_products_id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_control_map`
--

LOCK TABLES `group_control_map` WRITE;
/*!40000 ALTER TABLE `group_control_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `group_control_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_group_map`
--

DROP TABLE IF EXISTS `group_group_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group_group_map` (
  `member_id` mediumint(9) NOT NULL,
  `grantor_id` mediumint(9) NOT NULL,
  `grant_type` tinyint(4) NOT NULL DEFAULT '0',
  UNIQUE KEY `group_group_map_member_id_idx` (`member_id`,`grantor_id`,`grant_type`),
  KEY `fk_group_group_map_grantor_id_groups_id` (`grantor_id`),
  CONSTRAINT `fk_group_group_map_grantor_id_groups_id` FOREIGN KEY (`grantor_id`) REFERENCES `groups` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_group_group_map_member_id_groups_id` FOREIGN KEY (`member_id`) REFERENCES `groups` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_group_map`
--

LOCK TABLES `group_group_map` WRITE;
/*!40000 ALTER TABLE `group_group_map` DISABLE KEYS */;
INSERT INTO `group_group_map` VALUES (1,1,0),(1,1,1),(1,1,2),(1,2,0),(1,2,1),(1,2,2),(1,3,0),(1,3,1),(1,3,2),(1,4,0),(1,4,1),(1,4,2),(1,5,0),(1,5,1),(1,5,2),(1,6,0),(1,6,1),(1,6,2),(1,7,0),(1,7,1),(1,7,2),(1,8,0),(1,8,1),(1,8,2),(1,9,0),(1,9,1),(1,9,2),(1,10,0),(1,10,1),(1,10,2),(12,10,0),(1,11,0),(1,11,1),(1,11,2),(1,12,0),(1,12,1),(1,12,2),(1,13,0),(1,13,1),(1,13,2),(11,13,0);
/*!40000 ALTER TABLE `group_group_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `groups`
--

DROP TABLE IF EXISTS `groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `groups` (
  `id` mediumint(9) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` mediumtext NOT NULL,
  `isbuggroup` tinyint(4) NOT NULL,
  `userregexp` tinytext NOT NULL,
  `isactive` tinyint(4) NOT NULL DEFAULT '1',
  `icon_url` tinytext,
  PRIMARY KEY (`id`),
  UNIQUE KEY `groups_name_idx` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `groups`
--

LOCK TABLES `groups` WRITE;
/*!40000 ALTER TABLE `groups` DISABLE KEYS */;
INSERT INTO `groups` VALUES (1,'admin','Administrators',0,'',1,NULL),(2,'tweakparams','Can change Parameters',0,'',1,NULL),(3,'editusers','Can edit or disable users',0,'',1,NULL),(4,'creategroups','Can create and destroy groups',0,'',1,NULL),(5,'editclassifications','Can create, destroy, and edit classifications',0,'',1,NULL),(6,'editcomponents','Can create, destroy, and edit components',0,'',1,NULL),(7,'editkeywords','Can create, destroy, and edit keywords',0,'',1,NULL),(8,'editbugs','Can edit all bug fields',0,'.*',1,NULL),(9,'canconfirm','Can confirm a bug or mark it a duplicate',0,'',1,NULL),(10,'bz_canusewhines','User can configure whine reports for self',0,'',1,NULL),(11,'bz_sudoers','Can perform actions as other users',0,'',1,NULL),(12,'bz_canusewhineatothers','Can configure whine reports for other users',0,'',1,NULL),(13,'bz_sudo_protect','Can not be impersonated by other users',0,'',1,NULL);
/*!40000 ALTER TABLE `groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `keyworddefs`
--

DROP TABLE IF EXISTS `keyworddefs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `keyworddefs` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `description` mediumtext,
  PRIMARY KEY (`id`),
  UNIQUE KEY `keyworddefs_name_idx` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `keyworddefs`
--

LOCK TABLES `keyworddefs` WRITE;
/*!40000 ALTER TABLE `keyworddefs` DISABLE KEYS */;
INSERT INTO `keyworddefs` VALUES (1,'keyword1','Testing keywords'),(2,'jira','Jira tag'),(3,'linux','Linux tag');
/*!40000 ALTER TABLE `keyworddefs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `keywords`
--

DROP TABLE IF EXISTS `keywords`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `keywords` (
  `bug_id` mediumint(9) NOT NULL,
  `keywordid` smallint(6) NOT NULL,
  UNIQUE KEY `keywords_bug_id_idx` (`bug_id`,`keywordid`),
  KEY `keywords_keywordid_idx` (`keywordid`),
  CONSTRAINT `fk_keywords_bug_id_bugs_bug_id` FOREIGN KEY (`bug_id`) REFERENCES `bugs` (`bug_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_keywords_keywordid_keyworddefs_id` FOREIGN KEY (`keywordid`) REFERENCES `keyworddefs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `keywords`
--

LOCK TABLES `keywords` WRITE;
/*!40000 ALTER TABLE `keywords` DISABLE KEYS */;
INSERT INTO `keywords` VALUES (1,2),(1,3);
/*!40000 ALTER TABLE `keywords` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `login_failure`
--

DROP TABLE IF EXISTS `login_failure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `login_failure` (
  `user_id` mediumint(9) NOT NULL,
  `login_time` datetime NOT NULL,
  `ip_addr` varchar(40) NOT NULL,
  KEY `login_failure_user_id_idx` (`user_id`),
  CONSTRAINT `fk_login_failure_user_id_profiles_userid` FOREIGN KEY (`user_id`) REFERENCES `profiles` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `login_failure`
--

LOCK TABLES `login_failure` WRITE;
/*!40000 ALTER TABLE `login_failure` DISABLE KEYS */;
/*!40000 ALTER TABLE `login_failure` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `logincookies`
--

DROP TABLE IF EXISTS `logincookies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `logincookies` (
  `cookie` varchar(16) NOT NULL,
  `userid` mediumint(9) NOT NULL,
  `ipaddr` varchar(40) DEFAULT NULL,
  `lastused` datetime NOT NULL,
  PRIMARY KEY (`cookie`),
  KEY `logincookies_lastused_idx` (`lastused`),
  KEY `fk_logincookies_userid_profiles_userid` (`userid`),
  CONSTRAINT `fk_logincookies_userid_profiles_userid` FOREIGN KEY (`userid`) REFERENCES `profiles` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `logincookies`
--

LOCK TABLES `logincookies` WRITE;
/*!40000 ALTER TABLE `logincookies` DISABLE KEYS */;
INSERT INTO `logincookies` VALUES ('1KR3nLKkMY',2,NULL,'2010-11-09 16:33:38'),('1PXhNd0Lrl',2,'192.168.157.47','2010-11-08 09:57:11'),('303Fcs9I0u',2,NULL,'2010-11-09 15:27:34'),('3LKdQygACz',2,NULL,'2010-11-08 16:00:53'),('3vx9hj5wcc',2,NULL,'2010-11-08 16:03:57'),('3zWJ7ecPYY',2,NULL,'2010-11-08 17:24:55'),('5KuCgqI3cF',2,NULL,'2010-11-09 16:30:13'),('7lWwCMKaH9',2,NULL,'2010-11-08 17:17:30'),('7tprjd1LHe',2,NULL,'2010-11-08 17:12:41'),('84aQTGbRiq',2,NULL,'2010-11-09 17:21:27'),('8c2S2jslSg',2,NULL,'2010-11-08 17:21:39'),('8cD3ZDPNLd',2,NULL,'2010-11-09 16:29:29'),('93r18jbPxX',2,NULL,'2010-11-09 17:31:43'),('9YB8ljZ0Mi',2,NULL,'2010-11-09 17:33:57'),('A9eGyD9p0q',2,NULL,'2010-11-08 17:15:59'),('ACHWUqNrAj',2,NULL,'2010-11-08 16:10:48'),('AcRair6XBt',2,NULL,'2010-11-08 17:05:00'),('AeizxvS6Tz',2,NULL,'2010-11-08 17:07:02'),('asGcQoB7IF',2,NULL,'2010-11-08 16:03:55'),('b2R9a89D3N',2,NULL,'2010-11-08 17:07:45'),('bD7HqI7tZ3',2,NULL,'2010-11-08 16:54:21'),('bFXubazgg6',2,NULL,'2010-10-26 11:20:23'),('bjo907zFZo',2,NULL,'2010-11-09 17:34:10'),('BLpQ06mXtm',2,NULL,'2010-11-08 16:58:54'),('bm2hhzXZQj',2,NULL,'2010-11-08 15:56:49'),('BZY3yKAJsc',2,NULL,'2010-11-08 17:12:09'),('c03NuELbKR',2,NULL,'2010-11-08 16:42:18'),('C1wkEx5PrO',2,NULL,'2010-11-08 15:16:48'),('ChhK10e7Qk',2,NULL,'2010-11-08 17:33:05'),('ClaPIh5iVA',2,NULL,'2010-11-09 17:39:56'),('Cr21AlLCcf',2,NULL,'2010-11-09 16:50:43'),('cSazupCswC',2,NULL,'2010-11-08 17:10:28'),('drefy3jdib',2,NULL,'2010-11-09 17:32:55'),('DuovYLXZH6',2,NULL,'2010-11-09 17:26:22'),('ec93LhewZ8',2,NULL,'2010-11-09 15:27:31'),('ECeNjfKA6B',2,NULL,'2010-11-08 17:17:27'),('eGDl3cUPHF',2,NULL,'2010-11-08 17:05:53'),('Eh6dIqmi36',2,NULL,'2010-11-08 16:21:04'),('eLHVvYTClD',2,NULL,'2010-11-08 16:00:56'),('f4k47RXamC',2,NULL,'2010-11-09 17:21:30'),('fb7Oq7kdIN',2,NULL,'2010-11-09 17:28:24'),('fG3EpDsba8',2,NULL,'2010-11-09 17:42:06'),('FNrNMtBzML',2,NULL,'2010-11-09 16:29:33'),('fqJkSpw5yl',2,NULL,'2010-11-08 17:24:19'),('FTfIWY5Kuk',2,NULL,'2010-11-08 17:17:58'),('FVrcuhsu5z',2,NULL,'2010-11-09 17:32:28'),('G8EEZZ6jbV',2,NULL,'2010-11-08 16:42:15'),('GcOHcZAfuy',2,NULL,'2010-11-09 17:42:09'),('GQEz739AZJ',2,NULL,'2010-11-08 15:40:04'),('gXszMsa8R0',2,NULL,'2010-11-08 17:17:56'),('h3ejqNSDP9',2,NULL,'2010-11-08 15:58:33'),('H6olqNqJ2T',2,NULL,'2010-11-09 17:30:59'),('HFMjU0hhYK',2,NULL,'2010-11-08 16:20:02'),('hGJZnOeSyr',2,NULL,'2010-11-08 17:00:17'),('HiGW6xPZJ5',2,NULL,'2010-11-09 17:29:02'),('HjAtmC5OW7',2,NULL,'2010-11-08 16:12:17'),('hjgz7lJpHS',2,NULL,'2010-11-08 16:24:24'),('hQ4V4IwR5y',2,NULL,'2010-11-09 17:42:05'),('hYHLiS7KEc',2,NULL,'2010-11-09 17:34:08'),('hywdapNSuD',2,NULL,'2010-11-09 17:29:04'),('IcoEJ556AE',2,NULL,'2010-11-09 09:13:07'),('iDidYuGBr4',2,NULL,'2010-11-08 15:46:38'),('igDjiosU2d',2,NULL,'2010-11-05 16:52:32'),('iHHFfFNH9G',2,NULL,'2010-11-08 16:59:16'),('iL3vfFiuWP',2,NULL,'2010-11-09 16:33:42'),('IN8jlif8dK',2,NULL,'2010-11-08 15:45:08'),('IwPdZsOuv5',2,NULL,'2010-11-09 16:09:29'),('j2yeW3RtcY',2,NULL,'2010-11-09 17:27:37'),('joJXZWW1Vs',2,NULL,'2010-11-09 17:42:02'),('jzWi3eKXN5',2,NULL,'2010-11-09 17:40:14'),('KmPI74ggUV',2,NULL,'2010-11-08 16:24:29'),('l6jxcC5jC5',2,NULL,'2010-11-08 16:17:41'),('lkmYVXuaWt',2,NULL,'2010-11-08 17:04:35'),('lpKw4i0eFC',2,NULL,'2010-11-08 17:00:19'),('m792G2zYET',1,NULL,'2010-10-11 15:29:03'),('Mb3kH2VPY9',2,NULL,'2010-11-08 16:56:19'),('NbhCqHuRW7',2,NULL,'2010-11-08 17:24:23'),('NO8tChIJB4',2,NULL,'2010-11-09 17:29:19'),('NRaEejwgHI',2,NULL,'2010-11-08 16:21:05'),('ojjvfx76n3',2,NULL,'2010-11-08 15:16:46'),('OL9h9cr7Rw',2,NULL,'2010-11-08 17:16:01'),('PD6cuDVDgn',2,NULL,'2010-11-09 17:26:24'),('PJ4mLJ0ckG',2,NULL,'2010-11-09 17:27:38'),('QU8oAtNLGB',2,NULL,'2010-11-09 17:39:57'),('R1b41vLZcZ',2,NULL,'2010-11-08 17:24:51'),('rBbo1tPazj',2,NULL,'2010-11-09 15:48:41'),('RybhDuQgzf',2,NULL,'2010-11-09 15:48:43'),('sKUmKJBbCl',2,NULL,'2010-11-08 15:19:35'),('sOVcpKhFnD',2,NULL,'2010-11-08 16:21:48'),('svBENwIcLQ',2,NULL,'2010-11-08 15:56:51'),('SZgbC8Govn',2,NULL,'2010-11-09 16:08:09'),('tOkBDm7LIZ',2,NULL,'2010-11-09 17:40:15'),('ttSmrxpV85',2,NULL,'2010-11-09 17:32:54'),('u07FhSkVv6',2,NULL,'2010-11-08 16:10:47'),('UTm2rcm2bK',2,NULL,'2010-11-08 17:11:58'),('vaojQC2AUt',2,NULL,'2010-11-09 16:08:13'),('vC9G5btFSm',2,NULL,'2010-11-08 17:05:56'),('VEUIAkDGDX',2,NULL,'2010-11-09 16:33:43'),('vF9zS4M57P',2,NULL,'2010-11-08 15:44:11'),('Vpc7gTqsse',2,NULL,'2010-11-08 17:04:34'),('VS6TCN9Bzs',2,NULL,'2010-11-08 17:10:26'),('W4gg3Q0LF2',2,NULL,'2010-11-08 16:11:43'),('W5pb8vK9tk',2,NULL,'2010-11-08 15:58:34'),('WA3WkxqmvM',2,NULL,'2010-11-08 16:09:35'),('wH6GtULy4y',2,NULL,'2010-11-08 16:11:45'),('wvKd9M2IkT',2,NULL,'2010-11-08 16:09:37'),('x4gDTSRWf5',2,NULL,'2010-11-08 16:12:19'),('XkWmSPCsxB',2,NULL,'2010-11-09 17:31:41'),('XMfToLvZy7',2,NULL,'2010-11-09 17:28:25'),('XOii4j02To',2,NULL,'2010-11-08 17:21:42'),('xpUovfFAj0',2,NULL,'2010-11-09 17:29:20'),('Xqq7gmK0Qy',2,NULL,'2010-11-08 16:56:18'),('YcSbk7vjmh',2,NULL,'2010-11-08 17:04:58'),('YLpoyS9a1u',2,NULL,'2010-11-08 16:59:18'),('yrQd90Orjz',2,NULL,'2010-11-09 17:33:58'),('yVmYbCVnEp',2,NULL,'2010-11-08 16:21:44'),('Zg4dwC9w6Z',2,NULL,'2010-11-08 15:19:33'),('zKbX6zemT1',2,NULL,'2010-11-09 17:30:57'),('Zp62iUbFxn',2,NULL,'2010-11-08 16:13:26'),('zUsBpXuir5',2,NULL,'2010-11-09 17:32:30');
/*!40000 ALTER TABLE `logincookies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `longdescs`
--

DROP TABLE IF EXISTS `longdescs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `longdescs` (
  `comment_id` mediumint(9) NOT NULL AUTO_INCREMENT,
  `bug_id` mediumint(9) NOT NULL,
  `who` mediumint(9) NOT NULL,
  `bug_when` datetime NOT NULL,
  `work_time` decimal(7,2) NOT NULL DEFAULT '0.00',
  `thetext` mediumtext NOT NULL,
  `isprivate` tinyint(4) NOT NULL DEFAULT '0',
  `already_wrapped` tinyint(4) NOT NULL DEFAULT '0',
  `type` smallint(6) NOT NULL DEFAULT '0',
  `extra_data` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`comment_id`),
  KEY `longdescs_bug_id_idx` (`bug_id`),
  KEY `longdescs_who_idx` (`who`,`bug_id`),
  KEY `longdescs_bug_when_idx` (`bug_when`),
  CONSTRAINT `fk_longdescs_bug_id_bugs_bug_id` FOREIGN KEY (`bug_id`) REFERENCES `bugs` (`bug_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_longdescs_who_profiles_userid` FOREIGN KEY (`who`) REFERENCES `profiles` (`userid`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `longdescs`
--

LOCK TABLES `longdescs` WRITE;
/*!40000 ALTER TABLE `longdescs` DISABLE KEYS */;
INSERT INTO `longdescs` VALUES (1,1,1,'2010-10-11 15:28:12','0.00','',0,0,0,NULL),(2,2,1,'2010-10-11 15:29:03','0.00','It\'s better to spend some bugs on JIRA :)',0,0,0,NULL),(3,3,2,'2010-10-22 16:10:54','0.00','Here\'s a wrong link to a bug.\n\nluck Bug 7777777777777777\n\nHere\'s a good Bug 1',0,0,0,NULL),(4,4,2,'2010-10-25 11:32:20','0.00','Testing „Slanted quotes in action”',0,0,0,NULL),(5,4,2,'2010-10-25 11:39:04','0.00','“Different quotes”',0,0,0,NULL),(6,5,2,'2010-10-25 11:39:41','0.00','“Different quotes in description”',0,0,0,NULL),(7,6,2,'2010-10-29 11:35:54','0.00','',0,0,0,NULL),(8,6,2,'2010-10-29 11:41:29','0.00','',0,0,1,'2'),(9,2,2,'2010-10-29 11:41:29','0.00','',0,0,2,'6'),(10,2,2,'2010-11-02 13:33:29','4.00','Changing estimates',0,0,0,NULL),(11,7,2,'2010-11-02 15:12:44','0.00','Let\'s see what happens here',0,0,0,NULL),(12,8,2,'2010-11-02 16:39:16','0.00','Very big attachment is stored locally in the filesystem.\n\nThe small one is stored in db.',0,0,0,NULL),(13,8,2,'2010-11-02 16:40:26','0.00','',0,0,5,'1'),(14,8,2,'2010-11-02 16:45:16','0.00','',0,0,5,'3'),(15,8,2,'2010-11-04 10:19:34','0.00','',0,0,5,'4'),(16,9,2,'2010-11-04 13:56:13','0.00','That\'s a custom step',0,0,0,NULL),(17,10,2,'2010-11-04 13:57:05','0.00','That\'s a custom step',0,0,0,NULL),(18,11,2,'2010-11-05 11:21:16','0.00','This is an issue having time tracking',0,0,0,NULL),(19,11,2,'2010-11-05 11:22:18','4.00','Changing time tracking',0,0,0,NULL),(20,11,2,'2010-11-05 11:22:58','3.00','Time tracking',0,0,0,NULL),(21,12,2,'2010-11-08 15:58:34','0.00','1289228258556',0,0,0,NULL),(22,13,2,'2010-11-08 16:03:57','0.00','1289228581067',0,0,0,NULL),(23,14,2,'2010-11-08 16:09:37','0.00','1289228921181',0,0,0,NULL),(24,15,2,'2010-11-08 16:10:48','0.00','1289228992756',0,0,0,NULL),(25,16,2,'2010-11-08 16:11:45','0.00','1289229048910',0,0,0,NULL),(26,17,2,'2010-11-08 16:12:19','0.00','1289229082930',0,0,0,NULL),(27,18,2,'2010-11-08 16:17:41','0.00','1289229152054',0,0,0,NULL),(28,19,2,'2010-11-08 16:21:06','0.00','1289229609501',0,0,0,NULL),(29,20,2,'2010-11-08 16:56:19','0.00','1289231723043',0,0,0,NULL),(30,21,2,'2010-11-08 16:59:18','0.00','1289231901503',0,0,0,NULL),(31,22,2,'2010-11-08 17:00:19','0.00','1289231962897',0,0,0,NULL),(32,23,2,'2010-11-08 17:04:35','0.00','1289232219344',0,0,0,NULL),(33,24,2,'2010-11-08 17:05:55','0.00','1289232308319',0,0,0,NULL),(34,25,2,'2010-11-08 17:07:15','0.00','1289232376591',0,0,0,NULL),(35,26,2,'2010-11-08 17:10:27','0.00','1289232580547',0,0,0,NULL),(36,18,2,'2010-11-08 17:12:37','2.00','fsdf',0,0,0,NULL),(37,27,2,'2010-11-08 17:16:00','0.00','1289232913521',0,0,0,NULL),(38,28,2,'2010-11-08 17:17:29','0.00','1289233002227',0,0,0,NULL),(39,28,2,'2010-11-08 17:17:30','10.00','my reason for adding work',0,0,0,NULL),(40,29,2,'2010-11-08 17:17:57','0.00','1289233030490',0,0,0,NULL),(41,29,2,'2010-11-08 17:17:58','10.00','my reason for adding work',0,0,0,NULL),(42,30,2,'2010-11-08 17:21:41','0.00','1289233254115',0,0,0,NULL),(43,30,2,'2010-11-08 17:21:42','10.00','my reason for adding work',0,0,0,NULL),(44,31,2,'2010-11-08 17:24:21','0.00','1289233414222',0,0,0,NULL),(45,31,2,'2010-11-08 17:24:22','10.00','my reason for adding work',0,0,0,NULL),(46,32,2,'2010-11-08 17:24:52','0.00','1289233445827',0,0,0,NULL),(47,32,2,'2010-11-08 17:24:54','10.00','my reason for adding work',0,0,0,NULL),(48,32,2,'2010-11-08 17:24:55','20.00','another reason for adding work',0,0,1,'16'),(49,16,2,'2010-11-08 17:24:55','0.00','',0,0,2,'32'),(50,7,4,'2010-11-09 09:12:17','0.00','Comment by disabled user',0,0,0,NULL),(51,33,4,'2010-11-09 09:12:48','0.00','I\'m disabled',0,0,0,NULL),(52,14,2,'2010-11-09 16:05:52','0.00','a',0,0,0,NULL),(53,34,2,'2010-11-09 16:08:11','0.00','1289315226210',0,0,0,NULL),(54,34,2,'2010-11-09 16:08:12','10.00','my reason for adding work',0,0,0,NULL),(55,34,2,'2010-11-09 16:08:13','20.00','another reason for adding work',0,0,1,'16'),(56,16,2,'2010-11-09 16:08:13','0.00','',0,0,2,'34'),(57,35,2,'2010-11-09 16:29:31','0.00','1289316516563',0,0,0,NULL),(58,35,2,'2010-11-09 16:29:32','10.00','my reason for adding work',0,0,0,NULL),(59,35,2,'2010-11-09 16:29:33','20.00','another reason for adding work',0,0,1,'16'),(60,16,2,'2010-11-09 16:29:33','0.00','',0,0,2,'35'),(61,35,2,'2010-11-09 16:30:27','0.00','abc',0,0,5,'5'),(62,36,2,'2010-11-09 16:33:40','0.00','1289316765386',0,0,0,NULL),(63,36,2,'2010-11-09 16:33:41','10.00','my reason for adding work',0,0,0,NULL),(64,36,2,'2010-11-09 16:33:42','20.00','another reason for adding work',0,0,1,'16'),(65,16,2,'2010-11-09 16:33:42','0.00','',0,0,2,'36'),(66,36,2,'2010-11-09 16:33:43','0.00','Automated attachment test',0,0,5,'6'),(67,37,2,'2010-11-09 17:30:59','0.00','My descriptionTue Nov 09 17:29:53 CET 2010\ntest issue  unassigned ,version: 4.0 ,severity: normal ,priority: Low',0,0,0,NULL),(68,38,2,'2010-11-09 17:32:55','0.00','My descriptionTue Nov 09 17:31:50 CET 2010\ntest issue  ,without version ,severity: blocker ,priority: Normal',0,0,0,NULL),(69,39,2,'2010-11-09 17:34:10','0.00','My descriptionTue Nov 09 17:33:04 CET 2010\ntest issue  ,assigned to user5@example.com ,version: 2.0 ,severity: trivial ,priority: ---',0,0,0,NULL),(70,40,2,'2010-11-09 17:39:57','0.00','My descriptionTue Nov 09 17:38:52 CET 2010\ntest issue  ,assigned to user1@example.com ,version: 2.0 ,severity: critical ,priority: Lowest',0,0,0,NULL),(71,41,2,'2010-11-09 17:40:15','0.00','My descriptionTue Nov 09 17:39:10 CET 2010\ntest issue  unassigned ,version: 4.0 ,severity: minor ,priority: Normal',0,0,0,NULL),(72,42,2,'2010-11-09 17:42:05','0.00','My descriptionTue Nov 09 17:40:58 CET 2010\ntest issue  ,assigned to user2@example.com ,version: 1.0 ,severity: normal ,priority: High, depends on: 10, 11, 12, 13, blocks: 20, status: new',0,0,0,NULL);
/*!40000 ALTER TABLE `longdescs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `milestones`
--

DROP TABLE IF EXISTS `milestones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `milestones` (
  `id` mediumint(9) NOT NULL AUTO_INCREMENT,
  `product_id` smallint(6) NOT NULL,
  `value` varchar(20) NOT NULL,
  `sortkey` smallint(6) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `milestones_product_id_idx` (`product_id`,`value`),
  CONSTRAINT `fk_milestones_product_id_products_id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `milestones`
--

LOCK TABLES `milestones` WRITE;
/*!40000 ALTER TABLE `milestones` DISABLE KEYS */;
INSERT INTO `milestones` VALUES (1,1,'---',0),(2,2,'---',0),(3,1,'M1',0),(4,1,'M2',0),(5,1,'M3',0),(6,1,'M4',0),(7,1,'M5',0);
/*!40000 ALTER TABLE `milestones` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `namedqueries`
--

DROP TABLE IF EXISTS `namedqueries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `namedqueries` (
  `id` mediumint(9) NOT NULL AUTO_INCREMENT,
  `userid` mediumint(9) NOT NULL,
  `name` varchar(64) NOT NULL,
  `query` mediumtext NOT NULL,
  `query_type` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `namedqueries_userid_idx` (`userid`,`name`),
  CONSTRAINT `fk_namedqueries_userid_profiles_userid` FOREIGN KEY (`userid`) REFERENCES `profiles` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `namedqueries`
--

LOCK TABLES `namedqueries` WRITE;
/*!40000 ALTER TABLE `namedqueries` DISABLE KEYS */;
/*!40000 ALTER TABLE `namedqueries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `namedqueries_link_in_footer`
--

DROP TABLE IF EXISTS `namedqueries_link_in_footer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `namedqueries_link_in_footer` (
  `namedquery_id` mediumint(9) NOT NULL,
  `user_id` mediumint(9) NOT NULL,
  UNIQUE KEY `namedqueries_link_in_footer_id_idx` (`namedquery_id`,`user_id`),
  KEY `namedqueries_link_in_footer_userid_idx` (`user_id`),
  CONSTRAINT `fk_namedqueries_link_in_footer_namedquery_id_namedqueries_id` FOREIGN KEY (`namedquery_id`) REFERENCES `namedqueries` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_namedqueries_link_in_footer_user_id_profiles_userid` FOREIGN KEY (`user_id`) REFERENCES `profiles` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `namedqueries_link_in_footer`
--

LOCK TABLES `namedqueries_link_in_footer` WRITE;
/*!40000 ALTER TABLE `namedqueries_link_in_footer` DISABLE KEYS */;
/*!40000 ALTER TABLE `namedqueries_link_in_footer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `namedquery_group_map`
--

DROP TABLE IF EXISTS `namedquery_group_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `namedquery_group_map` (
  `namedquery_id` mediumint(9) NOT NULL,
  `group_id` mediumint(9) NOT NULL,
  UNIQUE KEY `namedquery_group_map_namedquery_id_idx` (`namedquery_id`),
  KEY `namedquery_group_map_group_id_idx` (`group_id`),
  CONSTRAINT `fk_namedquery_group_map_group_id_groups_id` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_namedquery_group_map_namedquery_id_namedqueries_id` FOREIGN KEY (`namedquery_id`) REFERENCES `namedqueries` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `namedquery_group_map`
--

LOCK TABLES `namedquery_group_map` WRITE;
/*!40000 ALTER TABLE `namedquery_group_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `namedquery_group_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `op_sys`
--

DROP TABLE IF EXISTS `op_sys`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `op_sys` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `value` varchar(64) NOT NULL,
  `sortkey` smallint(6) NOT NULL DEFAULT '0',
  `isactive` tinyint(4) NOT NULL DEFAULT '1',
  `visibility_value_id` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `op_sys_value_idx` (`value`),
  KEY `op_sys_sortkey_idx` (`sortkey`,`value`),
  KEY `op_sys_visibility_value_id_idx` (`visibility_value_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `op_sys`
--

LOCK TABLES `op_sys` WRITE;
/*!40000 ALTER TABLE `op_sys` DISABLE KEYS */;
INSERT INTO `op_sys` VALUES (1,'All',100,1,NULL),(2,'Windows',200,1,NULL),(3,'Mac OS',300,1,NULL),(4,'Linux',400,1,NULL),(5,'Other',500,1,NULL);
/*!40000 ALTER TABLE `op_sys` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `priority`
--

DROP TABLE IF EXISTS `priority`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `priority` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `value` varchar(64) NOT NULL,
  `sortkey` smallint(6) NOT NULL DEFAULT '0',
  `isactive` tinyint(4) NOT NULL DEFAULT '1',
  `visibility_value_id` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `priority_value_idx` (`value`),
  KEY `priority_sortkey_idx` (`sortkey`,`value`),
  KEY `priority_visibility_value_id_idx` (`visibility_value_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `priority`
--

LOCK TABLES `priority` WRITE;
/*!40000 ALTER TABLE `priority` DISABLE KEYS */;
INSERT INTO `priority` VALUES (1,'Highest',100,1,NULL),(2,'High',200,1,NULL),(3,'Normal',300,1,NULL),(4,'Low',400,1,NULL),(5,'Lowest',500,1,NULL),(6,'---',600,1,NULL);
/*!40000 ALTER TABLE `priority` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `products` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `classification_id` smallint(6) NOT NULL DEFAULT '1',
  `description` mediumtext,
  `isactive` tinyint(4) NOT NULL DEFAULT '1',
  `votesperuser` smallint(6) NOT NULL DEFAULT '0',
  `maxvotesperbug` smallint(6) NOT NULL DEFAULT '10000',
  `votestoconfirm` smallint(6) NOT NULL DEFAULT '0',
  `defaultmilestone` varchar(20) NOT NULL DEFAULT '---',
  `allows_unconfirmed` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `products_name_idx` (`name`),
  KEY `fk_products_classification_id_classifications_id` (`classification_id`),
  CONSTRAINT `fk_products_classification_id_classifications_id` FOREIGN KEY (`classification_id`) REFERENCES `classifications` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'TestProduct',1,'This is a test product. This ought to be blown away and replaced with real stuff in a finished installation of bugzilla.',1,10000,1,0,'---',0),(2,'Invalid component id',1,'This is a project with an invalid component id',1,0,10000,0,'---',0);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profile_setting`
--

DROP TABLE IF EXISTS `profile_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `profile_setting` (
  `user_id` mediumint(9) NOT NULL,
  `setting_name` varchar(32) NOT NULL,
  `setting_value` varchar(32) NOT NULL,
  UNIQUE KEY `profile_setting_value_unique_idx` (`user_id`,`setting_name`),
  KEY `fk_profile_setting_setting_name_setting_name` (`setting_name`),
  CONSTRAINT `fk_profile_setting_setting_name_setting_name` FOREIGN KEY (`setting_name`) REFERENCES `setting` (`name`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_profile_setting_user_id_profiles_userid` FOREIGN KEY (`user_id`) REFERENCES `profiles` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profile_setting`
--

LOCK TABLES `profile_setting` WRITE;
/*!40000 ALTER TABLE `profile_setting` DISABLE KEYS */;
INSERT INTO `profile_setting` VALUES (1,'lang','en'),(1,'per_bug_queries','on'),(1,'skin','standard'),(1,'timezone','Europe/Warsaw');
/*!40000 ALTER TABLE `profile_setting` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profiles`
--

DROP TABLE IF EXISTS `profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `profiles` (
  `userid` mediumint(9) NOT NULL AUTO_INCREMENT,
  `login_name` varchar(255) NOT NULL,
  `cryptpassword` varchar(128) DEFAULT NULL,
  `realname` varchar(255) NOT NULL DEFAULT '',
  `disabledtext` mediumtext NOT NULL,
  `disable_mail` tinyint(4) NOT NULL DEFAULT '0',
  `mybugslink` tinyint(4) NOT NULL DEFAULT '1',
  `extern_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`userid`),
  UNIQUE KEY `profiles_login_name_idx` (`login_name`),
  UNIQUE KEY `profiles_extern_id_idx` (`extern_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profiles`
--

LOCK TABLES `profiles` WRITE;
/*!40000 ALTER TABLE `profiles` DISABLE KEYS */;
INSERT INTO `profiles` VALUES (1,'piotr.maruszak@spartez.com','ToR3JPdzcSvzt93gzFWzhMaNUfRVNy+/56midZW6UkSxeDWqzG4{SHA-256}','Piotr Maruszak','',0,1,NULL),(2,'pniewiadomski@atlassian.com','2xJwXRe3ingmTcXG50fcjT0A74PrQqEdajvausrQoLyBmXOVvJA{SHA-256}','Paweł Niewiadomski','',0,1,NULL),(3,'wseliga@atlassian.com','Qr4duyPsUAdICNmKAXo+UiCGAbhqQW6yHBXDMYoEsXP+qmen5W4{SHA-256}','Wojciech Seliga','',0,1,NULL),(4,'disabled@localhost.localdomain','LufQynuN1f57LHg81SfcbNNlEmEgoEq678OqhEHW/papmcpeyiM{SHA-256}','disabled','You\'re account was disabled',0,1,NULL),(5,'user1@example.com','GVbXCXyPSgnD8s0mOOFjaIKTsZwXzZHevX8IRCW6+NIeayeuvTM{SHA-256}','User 1','',0,1,NULL),(6,'user2@example.com','BATfHkH8RC3BT8nILSqQHcZTHu3/2ov6824MCZ2Vd6osv6AzAOI{SHA-256}','User 2','',0,1,NULL),(7,'user3@example.com','qkmuHy3HWNYNWYE2aQ04xWaJR3tx8TEOkGITpksz171YfLP6Wkg{SHA-256}','User 3','',0,1,NULL),(8,'user4@example.com','ZVP08UAnpjK9eE2IbuJWhphv96Sw93hgB0t/geSVnjkrj14Ol+c{SHA-256}','User 4','',0,1,NULL),(9,'user5@example.com','XWfRtYTlGe/vLkZKpAayMe28We5GG8BaSIqwxsLLs9GIVUN2a30{SHA-256}','User 5','account disabled',0,1,NULL),(10,'user6@example.com','vEVt1NMZj8NuCkApnxsNn3Ls4rd5O+4H829lWwR+ou6K+P+t58c{SHA-256}','User 6','account disabled too',0,1,NULL);
/*!40000 ALTER TABLE `profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profiles_activity`
--

DROP TABLE IF EXISTS `profiles_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `profiles_activity` (
  `userid` mediumint(9) NOT NULL,
  `who` mediumint(9) NOT NULL,
  `profiles_when` datetime NOT NULL,
  `fieldid` mediumint(9) NOT NULL,
  `oldvalue` tinytext,
  `newvalue` tinytext,
  KEY `profiles_activity_userid_idx` (`userid`),
  KEY `profiles_activity_profiles_when_idx` (`profiles_when`),
  KEY `profiles_activity_fieldid_idx` (`fieldid`),
  KEY `fk_profiles_activity_who_profiles_userid` (`who`),
  CONSTRAINT `fk_profiles_activity_fieldid_fielddefs_id` FOREIGN KEY (`fieldid`) REFERENCES `fielddefs` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_profiles_activity_userid_profiles_userid` FOREIGN KEY (`userid`) REFERENCES `profiles` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_profiles_activity_who_profiles_userid` FOREIGN KEY (`who`) REFERENCES `profiles` (`userid`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profiles_activity`
--

LOCK TABLES `profiles_activity` WRITE;
/*!40000 ALTER TABLE `profiles_activity` DISABLE KEYS */;
INSERT INTO `profiles_activity` VALUES (1,1,'2010-10-11 14:20:30',31,NULL,'2010-10-11 14:20:30'),(2,1,'2010-10-11 15:26:05',31,NULL,'2010-10-11 15:26:05'),(2,1,'2010-10-11 15:26:21',39,'','admin'),(3,1,'2010-10-11 15:27:17',31,NULL,'2010-10-11 15:27:17'),(3,1,'2010-10-11 15:27:23',39,'','admin'),(4,2,'2010-11-09 09:10:45',31,NULL,'2010-11-09 09:10:45'),(5,2,'2010-11-09 16:11:06',31,NULL,'2010-11-09 16:11:06'),(6,2,'2010-11-09 16:11:34',31,NULL,'2010-11-09 16:11:34'),(7,2,'2010-11-09 16:11:47',31,NULL,'2010-11-09 16:11:47'),(8,2,'2010-11-09 16:13:18',31,NULL,'2010-11-09 16:13:18'),(9,2,'2010-11-09 16:13:40',31,NULL,'2010-11-09 16:13:40'),(10,2,'2010-11-09 16:14:07',31,NULL,'2010-11-09 16:14:07');
/*!40000 ALTER TABLE `profiles_activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quips`
--

DROP TABLE IF EXISTS `quips`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `quips` (
  `quipid` mediumint(9) NOT NULL AUTO_INCREMENT,
  `userid` mediumint(9) DEFAULT NULL,
  `quip` mediumtext NOT NULL,
  `approved` tinyint(4) NOT NULL DEFAULT '1',
  PRIMARY KEY (`quipid`),
  KEY `fk_quips_userid_profiles_userid` (`userid`),
  CONSTRAINT `fk_quips_userid_profiles_userid` FOREIGN KEY (`userid`) REFERENCES `profiles` (`userid`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quips`
--

LOCK TABLES `quips` WRITE;
/*!40000 ALTER TABLE `quips` DISABLE KEYS */;
/*!40000 ALTER TABLE `quips` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rep_platform`
--

DROP TABLE IF EXISTS `rep_platform`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rep_platform` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `value` varchar(64) NOT NULL,
  `sortkey` smallint(6) NOT NULL DEFAULT '0',
  `isactive` tinyint(4) NOT NULL DEFAULT '1',
  `visibility_value_id` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `rep_platform_value_idx` (`value`),
  KEY `rep_platform_sortkey_idx` (`sortkey`,`value`),
  KEY `rep_platform_visibility_value_id_idx` (`visibility_value_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rep_platform`
--

LOCK TABLES `rep_platform` WRITE;
/*!40000 ALTER TABLE `rep_platform` DISABLE KEYS */;
INSERT INTO `rep_platform` VALUES (1,'All',100,1,NULL),(2,'PC',200,1,NULL),(3,'Macintosh',300,1,NULL),(4,'Other',400,1,NULL);
/*!40000 ALTER TABLE `rep_platform` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resolution`
--

DROP TABLE IF EXISTS `resolution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `resolution` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `value` varchar(64) NOT NULL,
  `sortkey` smallint(6) NOT NULL DEFAULT '0',
  `isactive` tinyint(4) NOT NULL DEFAULT '1',
  `visibility_value_id` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `resolution_value_idx` (`value`),
  KEY `resolution_sortkey_idx` (`sortkey`,`value`),
  KEY `resolution_visibility_value_id_idx` (`visibility_value_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resolution`
--

LOCK TABLES `resolution` WRITE;
/*!40000 ALTER TABLE `resolution` DISABLE KEYS */;
INSERT INTO `resolution` VALUES (1,'',100,1,NULL),(2,'FIXED',200,1,NULL),(3,'INVALID',300,1,NULL),(4,'WONTFIX',400,1,NULL),(5,'DUPLICATE',500,1,NULL),(6,'WORKSFORME',600,1,NULL),(7,'MOVED',700,1,NULL);
/*!40000 ALTER TABLE `resolution` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `series`
--

DROP TABLE IF EXISTS `series`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `series` (
  `series_id` mediumint(9) NOT NULL AUTO_INCREMENT,
  `creator` mediumint(9) DEFAULT NULL,
  `category` smallint(6) NOT NULL,
  `subcategory` smallint(6) NOT NULL,
  `name` varchar(64) NOT NULL,
  `frequency` smallint(6) NOT NULL,
  `query` mediumtext NOT NULL,
  `is_public` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`series_id`),
  UNIQUE KEY `series_creator_idx` (`creator`,`category`,`subcategory`,`name`),
  KEY `fk_series_category_series_categories_id` (`category`),
  KEY `fk_series_subcategory_series_categories_id` (`subcategory`),
  CONSTRAINT `fk_series_category_series_categories_id` FOREIGN KEY (`category`) REFERENCES `series_categories` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_series_creator_profiles_userid` FOREIGN KEY (`creator`) REFERENCES `profiles` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_series_subcategory_series_categories_id` FOREIGN KEY (`subcategory`) REFERENCES `series_categories` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `series`
--

LOCK TABLES `series` WRITE;
/*!40000 ALTER TABLE `series` DISABLE KEYS */;
INSERT INTO `series` VALUES (1,2,1,2,'All Open',1,'field0-0-0=resolution&type0-0-0=notregexp&value0-0-0=.&product=Invalid%20component%20id&component=Testing',1),(2,2,1,2,'All Closed',1,'field0-0-0=resolution&type0-0-0=regexp&value0-0-0=.&product=Invalid%20component%20id&component=Testing',1),(3,2,3,4,'All Open',1,'field0-0-0=resolution&type0-0-0=notregexp&value0-0-0=.&product=TestProduct&component=Component1',1),(4,2,3,4,'All Closed',1,'field0-0-0=resolution&type0-0-0=regexp&value0-0-0=.&product=TestProduct&component=Component1',1),(5,2,3,5,'All Open',1,'field0-0-0=resolution&type0-0-0=notregexp&value0-0-0=.&product=TestProduct&component=Component2',1),(6,2,3,5,'All Closed',1,'field0-0-0=resolution&type0-0-0=regexp&value0-0-0=.&product=TestProduct&component=Component2',1),(7,2,3,6,'All Open',1,'field0-0-0=resolution&type0-0-0=notregexp&value0-0-0=.&product=TestProduct&component=Component3',1),(8,2,3,6,'All Closed',1,'field0-0-0=resolution&type0-0-0=regexp&value0-0-0=.&product=TestProduct&component=Component3',1),(9,2,3,7,'All Open',1,'field0-0-0=resolution&type0-0-0=notregexp&value0-0-0=.&product=TestProduct&component=Component4',1),(10,2,3,7,'All Closed',1,'field0-0-0=resolution&type0-0-0=regexp&value0-0-0=.&product=TestProduct&component=Component4',1);
/*!40000 ALTER TABLE `series` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `series_categories`
--

DROP TABLE IF EXISTS `series_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `series_categories` (
  `id` smallint(6) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `series_categories_name_idx` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `series_categories`
--

LOCK TABLES `series_categories` WRITE;
/*!40000 ALTER TABLE `series_categories` DISABLE KEYS */;
INSERT INTO `series_categories` VALUES (4,'Component1'),(5,'Component2'),(6,'Component3'),(7,'Component4'),(1,'Invalid component id'),(2,'Testing'),(3,'TestProduct');
/*!40000 ALTER TABLE `series_categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `series_data`
--

DROP TABLE IF EXISTS `series_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `series_data` (
  `series_id` mediumint(9) NOT NULL,
  `series_date` datetime NOT NULL,
  `series_value` mediumint(9) NOT NULL,
  UNIQUE KEY `series_data_series_id_idx` (`series_id`,`series_date`),
  CONSTRAINT `fk_series_data_series_id_series_series_id` FOREIGN KEY (`series_id`) REFERENCES `series` (`series_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `series_data`
--

LOCK TABLES `series_data` WRITE;
/*!40000 ALTER TABLE `series_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `series_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `setting`
--

DROP TABLE IF EXISTS `setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `setting` (
  `name` varchar(32) NOT NULL,
  `default_value` varchar(32) NOT NULL,
  `is_enabled` tinyint(4) NOT NULL DEFAULT '1',
  `subclass` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `setting`
--

LOCK TABLES `setting` WRITE;
/*!40000 ALTER TABLE `setting` DISABLE KEYS */;
INSERT INTO `setting` VALUES ('comment_sort_order','oldest_to_newest',1,NULL),('csv_colsepchar',',',1,NULL),('display_quips','on',1,NULL),('lang','en',1,'Lang'),('per_bug_queries','off',1,NULL),('post_bug_submit_action','next_bug',1,NULL),('quote_replies','quoted_reply',1,NULL),('skin','Dusk',1,'Skin'),('state_addselfcc','cc_unless_role',1,NULL),('timezone','local',1,'Timezone'),('zoom_textareas','on',1,NULL);
/*!40000 ALTER TABLE `setting` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `setting_value`
--

DROP TABLE IF EXISTS `setting_value`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `setting_value` (
  `name` varchar(32) NOT NULL,
  `value` varchar(32) NOT NULL,
  `sortindex` smallint(6) NOT NULL,
  UNIQUE KEY `setting_value_nv_unique_idx` (`name`,`value`),
  UNIQUE KEY `setting_value_ns_unique_idx` (`name`,`sortindex`),
  CONSTRAINT `fk_setting_value_name_setting_name` FOREIGN KEY (`name`) REFERENCES `setting` (`name`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `setting_value`
--

LOCK TABLES `setting_value` WRITE;
/*!40000 ALTER TABLE `setting_value` DISABLE KEYS */;
INSERT INTO `setting_value` VALUES ('comment_sort_order','oldest_to_newest',5),('comment_sort_order','newest_to_oldest',10),('comment_sort_order','newest_to_oldest_desc_first',15),('csv_colsepchar',',',5),('csv_colsepchar',';',10),('display_quips','on',5),('display_quips','off',10),('per_bug_queries','on',5),('per_bug_queries','off',10),('post_bug_submit_action','next_bug',5),('post_bug_submit_action','same_bug',10),('post_bug_submit_action','nothing',15),('quote_replies','quoted_reply',5),('quote_replies','simple_reply',10),('quote_replies','off',15),('state_addselfcc','always',5),('state_addselfcc','never',10),('state_addselfcc','cc_unless_role',15),('zoom_textareas','on',5),('zoom_textareas','off',10);
/*!40000 ALTER TABLE `setting_value` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `status_workflow`
--

DROP TABLE IF EXISTS `status_workflow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `status_workflow` (
  `old_status` smallint(6) DEFAULT NULL,
  `new_status` smallint(6) NOT NULL,
  `require_comment` tinyint(4) NOT NULL DEFAULT '0',
  UNIQUE KEY `status_workflow_idx` (`old_status`,`new_status`),
  KEY `fk_status_workflow_new_status_bug_status_id` (`new_status`),
  CONSTRAINT `fk_status_workflow_new_status_bug_status_id` FOREIGN KEY (`new_status`) REFERENCES `bug_status` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_status_workflow_old_status_bug_status_id` FOREIGN KEY (`old_status`) REFERENCES `bug_status` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `status_workflow`
--

LOCK TABLES `status_workflow` WRITE;
/*!40000 ALTER TABLE `status_workflow` DISABLE KEYS */;
INSERT INTO `status_workflow` VALUES (NULL,1,0),(NULL,2,0),(NULL,3,0),(1,2,0),(1,3,0),(1,5,0),(2,3,0),(2,5,0),(3,2,0),(3,5,0),(4,2,0),(4,3,0),(4,5,0),(5,1,0),(5,4,0),(5,6,0),(5,7,0),(6,1,0),(6,4,0),(6,7,0),(7,1,0),(7,4,0),(6,5,0),(7,5,0),(8,5,0),(9,5,0),(3,9,0),(3,8,0),(9,8,0),(8,9,0);
/*!40000 ALTER TABLE `status_workflow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tokens`
--

DROP TABLE IF EXISTS `tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tokens` (
  `userid` mediumint(9) DEFAULT NULL,
  `issuedate` datetime NOT NULL,
  `token` varchar(16) NOT NULL,
  `tokentype` varchar(8) NOT NULL,
  `eventdata` tinytext,
  PRIMARY KEY (`token`),
  KEY `tokens_userid_idx` (`userid`),
  CONSTRAINT `fk_tokens_userid_profiles_userid` FOREIGN KEY (`userid`) REFERENCES `profiles` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tokens`
--

LOCK TABLES `tokens` WRITE;
/*!40000 ALTER TABLE `tokens` DISABLE KEYS */;
INSERT INTO `tokens` VALUES (2,'2010-11-09 16:14:07','53Cs4vfwyW','session','edit_user'),(2,'2010-11-09 16:13:40','5NnEUvke6U','session','edit_user'),(2,'2010-11-09 16:17:46','9fzV5PU9Ul','session','create_attachment:'),(2,'2010-11-09 17:05:06','Ce7neL5bfT','session','edit_product'),(2,'2010-11-09 16:07:15','dbCGEYV5Ru','session','createbug:'),(2,'2010-11-09 17:11:45','EI00AK652Q','session','edit_product'),(2,'2010-11-09 12:56:02','ErKAVQzJGS','session','add_user'),(2,'2010-11-08 16:07:54','fMUO4JsYee','session','add_user'),(2,'2010-11-08 15:57:52','FN3oH6GmnH','session','edit_product'),(2,'2010-11-09 16:11:34','GaJdcKfDR0','session','edit_user'),(2,'2010-11-09 15:43:44','HfxbWKwiV2','session','edit_product'),(2,'2010-11-09 09:13:07','HM4MHLHJOS','session','edit_user'),(2,'2010-11-09 16:11:18','k77FAIQSbh','session','edit_user'),(2,'2010-11-09 17:08:50','LpjpUKxynD','session','delete_component'),(2,'2010-11-09 16:05:04','oOVHD2jRXb','session','edit_user_prefs'),(2,'2010-11-09 16:30:06','OX3ljXEybN','session','create_attachment:5'),(2,'2010-11-09 17:06:11','Pcs3zgTVje','session','createbug:'),(2,'2010-11-09 17:09:02','qhphN0atyW','session','buglist_mass_change'),(2,'2010-11-09 16:09:11','RCCWDlT5vP','session','edit_milestone'),(4,'2010-11-09 09:12:36','sCVrnZybB9','session','createbug:33'),(2,'2010-11-09 16:13:18','seIENsVTQq','session','edit_user'),(2,'2010-11-09 16:30:36','UMMLhD59cQ','session','create_attachment:'),(2,'2010-11-09 16:05:08','WMRJxsSOKO','session','edit_parameters'),(2,'2010-11-09 17:08:38','WsZwbP76gG','session','delete_component'),(2,'2010-11-09 15:43:02','wzT89nQDhs','session','edit_milestone'),(2,'2010-11-09 16:05:29','xoqvihnmmr','session','edit_parameters'),(2,'2010-11-09 16:11:50','Y7DYhb4Lj9','session','edit_user'),(2,'2010-11-09 16:05:16','YNNme6N8U5','session','edit_parameters');
/*!40000 ALTER TABLE `tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ts_error`
--

DROP TABLE IF EXISTS `ts_error`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ts_error` (
  `error_time` int(11) NOT NULL,
  `jobid` int(11) NOT NULL,
  `message` varchar(255) NOT NULL,
  `funcid` int(11) NOT NULL DEFAULT '0',
  KEY `ts_error_funcid_idx` (`funcid`,`error_time`),
  KEY `ts_error_error_time_idx` (`error_time`),
  KEY `ts_error_jobid_idx` (`jobid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ts_error`
--

LOCK TABLES `ts_error` WRITE;
/*!40000 ALTER TABLE `ts_error` DISABLE KEYS */;
/*!40000 ALTER TABLE `ts_error` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ts_exitstatus`
--

DROP TABLE IF EXISTS `ts_exitstatus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ts_exitstatus` (
  `jobid` int(11) NOT NULL AUTO_INCREMENT,
  `funcid` int(11) NOT NULL DEFAULT '0',
  `status` smallint(6) DEFAULT NULL,
  `completion_time` int(11) DEFAULT NULL,
  `delete_after` int(11) DEFAULT NULL,
  PRIMARY KEY (`jobid`),
  KEY `ts_exitstatus_funcid_idx` (`funcid`),
  KEY `ts_exitstatus_delete_after_idx` (`delete_after`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ts_exitstatus`
--

LOCK TABLES `ts_exitstatus` WRITE;
/*!40000 ALTER TABLE `ts_exitstatus` DISABLE KEYS */;
/*!40000 ALTER TABLE `ts_exitstatus` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ts_funcmap`
--

DROP TABLE IF EXISTS `ts_funcmap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ts_funcmap` (
  `funcid` int(11) NOT NULL AUTO_INCREMENT,
  `funcname` varchar(255) NOT NULL,
  PRIMARY KEY (`funcid`),
  UNIQUE KEY `ts_funcmap_funcname_idx` (`funcname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ts_funcmap`
--

LOCK TABLES `ts_funcmap` WRITE;
/*!40000 ALTER TABLE `ts_funcmap` DISABLE KEYS */;
/*!40000 ALTER TABLE `ts_funcmap` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ts_job`
--

DROP TABLE IF EXISTS `ts_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ts_job` (
  `jobid` int(11) NOT NULL AUTO_INCREMENT,
  `funcid` int(11) NOT NULL,
  `arg` longblob,
  `uniqkey` varchar(255) DEFAULT NULL,
  `insert_time` int(11) DEFAULT NULL,
  `run_after` int(11) NOT NULL,
  `grabbed_until` int(11) NOT NULL,
  `priority` smallint(6) DEFAULT NULL,
  `coalesce` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`jobid`),
  UNIQUE KEY `ts_job_funcid_idx` (`funcid`,`uniqkey`),
  KEY `ts_job_run_after_idx` (`run_after`,`funcid`),
  KEY `ts_job_coalesce_idx` (`coalesce`,`funcid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ts_job`
--

LOCK TABLES `ts_job` WRITE;
/*!40000 ALTER TABLE `ts_job` DISABLE KEYS */;
/*!40000 ALTER TABLE `ts_job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ts_note`
--

DROP TABLE IF EXISTS `ts_note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ts_note` (
  `jobid` int(11) NOT NULL,
  `notekey` varchar(255) DEFAULT NULL,
  `value` longblob,
  UNIQUE KEY `ts_note_jobid_idx` (`jobid`,`notekey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ts_note`
--

LOCK TABLES `ts_note` WRITE;
/*!40000 ALTER TABLE `ts_note` DISABLE KEYS */;
/*!40000 ALTER TABLE `ts_note` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_group_map`
--

DROP TABLE IF EXISTS `user_group_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_group_map` (
  `user_id` mediumint(9) NOT NULL,
  `group_id` mediumint(9) NOT NULL,
  `isbless` tinyint(4) NOT NULL DEFAULT '0',
  `grant_type` tinyint(4) NOT NULL DEFAULT '0',
  UNIQUE KEY `user_group_map_user_id_idx` (`user_id`,`group_id`,`grant_type`,`isbless`),
  KEY `fk_user_group_map_group_id_groups_id` (`group_id`),
  CONSTRAINT `fk_user_group_map_group_id_groups_id` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_user_group_map_user_id_profiles_userid` FOREIGN KEY (`user_id`) REFERENCES `profiles` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_group_map`
--

LOCK TABLES `user_group_map` WRITE;
/*!40000 ALTER TABLE `user_group_map` DISABLE KEYS */;
INSERT INTO `user_group_map` VALUES (1,1,0,0),(1,1,1,0),(2,1,0,0),(2,1,1,0),(3,1,0,0),(3,1,1,0),(1,3,0,0),(1,8,0,2),(2,8,0,2),(3,8,0,2),(4,8,0,2),(5,8,0,2),(6,8,0,2),(7,8,0,2),(8,8,0,2),(9,8,0,2),(10,8,0,2);
/*!40000 ALTER TABLE `user_group_map` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `versions`
--

DROP TABLE IF EXISTS `versions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `versions` (
  `id` mediumint(9) NOT NULL AUTO_INCREMENT,
  `value` varchar(64) NOT NULL,
  `product_id` smallint(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `versions_product_id_idx` (`product_id`,`value`),
  CONSTRAINT `fk_versions_product_id_products_id` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `versions`
--

LOCK TABLES `versions` WRITE;
/*!40000 ALTER TABLE `versions` DISABLE KEYS */;
INSERT INTO `versions` VALUES (3,'1.0',1),(4,'2.0',1),(5,'3.0',1),(6,'4.0',1),(7,'5.0',1),(1,'unspecified',1),(2,'unspecified',2);
/*!40000 ALTER TABLE `versions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `votes`
--

DROP TABLE IF EXISTS `votes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `votes` (
  `who` mediumint(9) NOT NULL,
  `bug_id` mediumint(9) NOT NULL,
  `vote_count` smallint(6) NOT NULL,
  KEY `votes_who_idx` (`who`),
  KEY `votes_bug_id_idx` (`bug_id`),
  CONSTRAINT `fk_votes_bug_id_bugs_bug_id` FOREIGN KEY (`bug_id`) REFERENCES `bugs` (`bug_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_votes_who_profiles_userid` FOREIGN KEY (`who`) REFERENCES `profiles` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `votes`
--

LOCK TABLES `votes` WRITE;
/*!40000 ALTER TABLE `votes` DISABLE KEYS */;
INSERT INTO `votes` VALUES (2,5,1);
/*!40000 ALTER TABLE `votes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `watch`
--

DROP TABLE IF EXISTS `watch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `watch` (
  `watcher` mediumint(9) NOT NULL,
  `watched` mediumint(9) NOT NULL,
  UNIQUE KEY `watch_watcher_idx` (`watcher`,`watched`),
  KEY `watch_watched_idx` (`watched`),
  CONSTRAINT `fk_watch_watched_profiles_userid` FOREIGN KEY (`watched`) REFERENCES `profiles` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_watch_watcher_profiles_userid` FOREIGN KEY (`watcher`) REFERENCES `profiles` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `watch`
--

LOCK TABLES `watch` WRITE;
/*!40000 ALTER TABLE `watch` DISABLE KEYS */;
/*!40000 ALTER TABLE `watch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `whine_events`
--

DROP TABLE IF EXISTS `whine_events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `whine_events` (
  `id` mediumint(9) NOT NULL AUTO_INCREMENT,
  `owner_userid` mediumint(9) NOT NULL,
  `subject` varchar(128) DEFAULT NULL,
  `body` mediumtext,
  `mailifnobugs` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_whine_events_owner_userid_profiles_userid` (`owner_userid`),
  CONSTRAINT `fk_whine_events_owner_userid_profiles_userid` FOREIGN KEY (`owner_userid`) REFERENCES `profiles` (`userid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `whine_events`
--

LOCK TABLES `whine_events` WRITE;
/*!40000 ALTER TABLE `whine_events` DISABLE KEYS */;
/*!40000 ALTER TABLE `whine_events` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `whine_queries`
--

DROP TABLE IF EXISTS `whine_queries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `whine_queries` (
  `id` mediumint(9) NOT NULL AUTO_INCREMENT,
  `eventid` mediumint(9) NOT NULL,
  `query_name` varchar(64) NOT NULL DEFAULT '',
  `sortkey` smallint(6) NOT NULL DEFAULT '0',
  `onemailperbug` tinyint(4) NOT NULL DEFAULT '0',
  `title` varchar(128) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `whine_queries_eventid_idx` (`eventid`),
  CONSTRAINT `fk_whine_queries_eventid_whine_events_id` FOREIGN KEY (`eventid`) REFERENCES `whine_events` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `whine_queries`
--

LOCK TABLES `whine_queries` WRITE;
/*!40000 ALTER TABLE `whine_queries` DISABLE KEYS */;
/*!40000 ALTER TABLE `whine_queries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `whine_schedules`
--

DROP TABLE IF EXISTS `whine_schedules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `whine_schedules` (
  `id` mediumint(9) NOT NULL AUTO_INCREMENT,
  `eventid` mediumint(9) NOT NULL,
  `run_day` varchar(32) DEFAULT NULL,
  `run_time` varchar(32) DEFAULT NULL,
  `run_next` datetime DEFAULT NULL,
  `mailto` mediumint(9) NOT NULL,
  `mailto_type` smallint(6) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `whine_schedules_run_next_idx` (`run_next`),
  KEY `whine_schedules_eventid_idx` (`eventid`),
  CONSTRAINT `fk_whine_schedules_eventid_whine_events_id` FOREIGN KEY (`eventid`) REFERENCES `whine_events` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `whine_schedules`
--

LOCK TABLES `whine_schedules` WRITE;
/*!40000 ALTER TABLE `whine_schedules` DISABLE KEYS */;
/*!40000 ALTER TABLE `whine_schedules` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-11-09 17:49:34
