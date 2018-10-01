DROP TABLE IF EXISTS `opa_output_fields`;
DROP TABLE IF EXISTS `accounts_exports_items`;
DROP TABLE IF EXISTS `accounts_exports`;

CREATE TABLE `opa_output_fields` (
  `field_id` int(11) NOT NULL auto_increment, 
  `result_type` varchar(150) NOT NULL,  
  `field_name` varchar(150) NOT NULL,  
  `field_source` varchar(150) NULL,
  `order_number` int(11) NOT NULL,  
  `field_label` varchar(150) NOT NULL,
  `order_number_brief` int(11) NULL,  
  `field_label_brief` varchar(150) NULL,
  `section` varchar(300) NULL, 
  `field_value_instruction` varchar(200) NOT NULL,  
  `field_type` varchar(30) NOT NULL,  
  PRIMARY KEY  (`result_type`, `field_name`),
  KEY `field_id` (`field_id`),
  KEY `field_name` (`field_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='OPA Annotations - OPA Output Fields';

CREATE TABLE `accounts_exports` (
  `export_id` int(11) NOT NULL auto_increment,
  `account_id` int(11) default NULL, 
  `export_name` varchar(100) default NULL,
  `export_type` varchar(10) default NULL, 
  `bulk_export` tinyint(1) NOT NULL default 0, 
  `bulk_export_content` varchar(50) default NULL, 
  `include_thumbnails` tinyint(1) NOT NULL default 0, 
  `include_embedded_thumbnails` tinyint(1) NOT NULL default 0, 
  `include_comments` tinyint(1) NOT NULL default 0, 
  `include_content` tinyint(1) NOT NULL default 0,
  `include_metadata` tinyint(1) NOT NULL default 0,   
  `include_tags` tinyint(1) NOT NULL default 0, 
  `include_transcriptions` tinyint(1) NOT NULL default 0, 
  `include_translations` tinyint(1) NOT NULL default 0, 
  `export_format` varchar(10) default NULL, 
  `url` varchar(2000) default NULL, 
  `query_parameters` text(65535) NOT NULL, 
  `rows` int(11) default NULL, 
  `offset` int(11) default NULL, 
  `sort` varchar(250) default NULL, 
  `request_status` varchar(20) default NULL,
  `error_message` varchar(400) default NULL,
  `total_recs_processed` int(11) default NULL, 
  `total_recs_to_be_processed` int(11) default NULL, 
  `file_size` int(14) default NULL, 
  `processing_hint` varchar(10) default NULL, 
  `spring_job_execution_id` bigint(20) default NULL, 
  `server_instance_id` int(11) default NULL, 
  `request_ts` timestamp NULL, 
  `completed_ts` timestamp NULL,
  `last_action_ts` timestamp NULL,
  `expires_ts` timestamp NULL,
  PRIMARY KEY  (`export_id`),
  KEY `account_id` (`account_id`),
  KEY `export_type` (`export_type`),
  KEY `include_thumbnails` (`include_thumbnails`),
  KEY `include_comments` (`include_comments`),
  KEY `include_tags` (`include_tags`),
  KEY `include_transcriptions` (`include_transcriptions`),
  KEY `include_translations` (`include_translations`),
  KEY `export_format` (`export_format`),
  KEY `request_status` (`request_status`),
  KEY `request_ts` (`request_ts`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='OPA Annotations - Accounts Exports Table';

 
CREATE TABLE `accounts_exports_items` (
  `export_item_id` int(11) NOT NULL auto_increment,
  `export_id` int(11) NOT NULL, 
  `na_id` varchar(50) default NULL, 
  `object_id` varchar(50) default NULL, 
  `opa_id` varchar(100) default NULL, 
  `item_ts` timestamp NULL, 
  PRIMARY KEY  (`export_item_id`),
  KEY `export_id` (`export_id`),
  KEY `na_id` (`na_id`), 
  KEY `object_id` (`object_id`), 
  KEY `opa_id` (`opa_id`),
  KEY `item_ts` (`item_ts`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='OPA Annotations - Accounts Exports Items Table';