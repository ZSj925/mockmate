-- =============================================================================
-- mockmate 建表脚本
-- MySQL 8.0+ ；执行：mysql -uroot -p < docs/schema.sql
-- =============================================================================

create database if not exists mockmate default charset utf8mb4 collate utf8mb4_0900_ai_ci;
use mockmate;

DROP TABLE IF EXISTS `ai_call_log`;
CREATE TABLE `ai_call_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned DEFAULT NULL,
  `session_id` bigint unsigned DEFAULT NULL,
  `scene` varchar(30) NOT NULL COMMENT 'RESUME_PARSE/PREPARE/ANSWER/REPORT',
  `model` varchar(60) NOT NULL,
  `prompt_tokens` int NOT NULL DEFAULT '0',
  `completion_tokens` int NOT NULL DEFAULT '0',
  `latency_ms` int NOT NULL DEFAULT '0',
  `success` tinyint NOT NULL DEFAULT '1',
  `error_code` varchar(50) DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_log_scene` (`scene`,`created_at`),
  KEY `idx_log_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 调用明细';

DROP TABLE IF EXISTS `interview_message`;
CREATE TABLE `interview_message` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `session_id` bigint unsigned NOT NULL,
  `round_no` int NOT NULL,
  `role` varchar(20) NOT NULL COMMENT 'INTERVIEWER/CANDIDATE',
  `content` mediumtext NOT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_msg_session` (`session_id`,`id`)
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对话消息';

DROP TABLE IF EXISTS `interview_report`;
CREATE TABLE `interview_report` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `session_id` bigint unsigned NOT NULL,
  `total_score` decimal(5,2) NOT NULL COMMENT '0-5',
  `level_label` varchar(20) NOT NULL,
  `items_json` json NOT NULL COMMENT '[{roundNo,score,comment,suggestion}]',
  `dimensions_json` json NOT NULL COMMENT '[{dimension,score}]',
  `strengths_json` json DEFAULT NULL,
  `suggestions_json` json DEFAULT NULL,
  `summary` text,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='面试报告';

DROP TABLE IF EXISTS `interview_session`;
CREATE TABLE `interview_session` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `resume_id` bigint unsigned DEFAULT NULL,
  `job_type` varchar(30) NOT NULL COMMENT 'JAVA_BACKEND/FRONTEND/QA',
  `status` varchar(20) NOT NULL DEFAULT 'ONGOING' COMMENT 'ONGOING/FINISHED/ABORTED',
  `question_count` int NOT NULL DEFAULT '5',
  `current_round` int NOT NULL DEFAULT '1',
  `plan_json` json DEFAULT NULL COMMENT 'InterviewPlan.questions',
  `total_score` decimal(5,2) DEFAULT NULL,
  `level_label` varchar(20) DEFAULT NULL COMMENT '优秀/良好/待提升',
  `started_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `finished_at` datetime(3) DEFAULT NULL,
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_session_user` (`user_id`,`started_at`),
  KEY `idx_session_status` (`status`,`updated_at`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='面试会话';

DROP TABLE IF EXISTS `resume`;
CREATE TABLE `resume` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint unsigned NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_type` varchar(10) NOT NULL COMMENT 'TXT/PDF/DOCX',
  `file_size` int unsigned NOT NULL,
  `raw_text` mediumtext NOT NULL COMMENT '抽取的纯文本（脱敏后）',
  `summary_text` mediumtext COMMENT 'LLM 生成的面世摘要（面试时注入 system）',
  `profile_json` json DEFAULT NULL COMMENT 'ResumeProfile',
  `parse_status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PARSING/READY/FAILED',
  `parsed_at` datetime(3) DEFAULT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_resume_user` (`user_id`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='简历';

DROP TABLE IF EXISTS `user_account`;
CREATE TABLE `user_account` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(100) NOT NULL COMMENT 'bcrypt',
  `nickname` varchar(50) NOT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='账号';
