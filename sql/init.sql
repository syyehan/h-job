CREATE database if NOT EXISTS `h_job` default character set utf8mb4 collate utf8mb4_unicode_ci;
use `h_job`;

SET NAMES utf8mb4;


CREATE TABLE `h_job_user`
(
    `id`          int(20)       NOT NULL AUTO_INCREMENT,
    `user_name`   varchar(50)   NOT NULL COMMENT '账号',
    `password`    varchar(50)   NOT NULL COMMENT '密码',
    `role`        tinyint(4)    NOT NULL COMMENT '角色：0-普通用户、1-管理员',
    `permission`  varchar(255)           DEFAULT NULL COMMENT '权限：执行器ID列表，多个逗号分割',
    `job_token`   varchar(1000) NOT NULL DEFAULT '0' COMMENT 'token',
    `create_time` datetime               DEFAULT NULL,
    `update_time` datetime               DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `i_username` (`user_name`) USING BTREE
) ENGINE = InnoDB COMMENT ='角色表';



CREATE TABLE `h_job_server_address`
(
    `id`              int(20)      NOT NULL AUTO_INCREMENT,
    `service_name`    varchar(64)  NOT NULL COMMENT '服务名称(nacos中的服务名),支持非nacos服务',
    `title`           varchar(12)  NOT NULL COMMENT '业务名称',
    `address_type`    tinyint(4)   NOT NULL DEFAULT '0' COMMENT '服务地址类型：0=自动注册(nacos)、1=手动录入(支持IP)',
    `nacos_namespace` varchar(128) NOT NULL DEFAULT 'public' COMMENT 'nacos-namespace',
    `nacos_group`     varchar(128) NOT NULL DEFAULT 'DEFAULT_GROUP' COMMENT 'nacos-group',
    `address_list`    text COMMENT '服务地址列表，多地址逗号分隔',
    `create_time`     datetime              DEFAULT NULL,
    `update_time`     datetime              DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT ='服务地址信息表';



CREATE TABLE `h_job_info`
(
    `id`                        int(20)      NOT NULL AUTO_INCREMENT,
    `job_server_id`             int(20)      NOT NULL COMMENT 'h_job_server_address主键ID',
    `job_desc`                  varchar(255) NOT NULL COMMENT '任务描述',
    `create_user`               varchar(64)           DEFAULT NULL COMMENT '创建人',
    `cron`                      varchar(128)          DEFAULT NULL COMMENT 'cron表达式',
    `route_strategy`            varchar(50)           DEFAULT NULL COMMENT '路由策略',
    `path`                      varchar(255)          DEFAULT NULL COMMENT '调用路径',
    `param`                     varchar(512)          DEFAULT NULL COMMENT '调用参数',
    `method`                    varchar(10)  NOT NULL DEFAULT 'GET' COMMENT '调用方式 POST/GET',
    `executor_timeout`          int(11)      NOT NULL DEFAULT '0' COMMENT '任务执行超时时间，单位秒',
    `executor_fail_retry_count` int(11)      NOT NULL DEFAULT '0' COMMENT '失败重试次数',
    `job_status`                tinyint(4)   NOT NULL DEFAULT '0' COMMENT '调度状态：0-停止，1-运行',
    `job_last_time`             bigint(13)   NOT NULL DEFAULT '0' COMMENT '上次调度时间',
    `job_next_time`             bigint(13)   NOT NULL DEFAULT '0' COMMENT '下次调度时间',
    `create_time`               datetime              DEFAULT NULL,
    `update_time`               datetime              DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT ='任务配置表';


CREATE TABLE `h_job_log`
(
    `id`               bigint(20) NOT NULL AUTO_INCREMENT,
    `job_server_id`    int(11)    NOT NULL COMMENT 'h_job_server_address-主键ID',
    `job_id`           int(11)    NOT NULL COMMENT 'h_job_info-主键ID',
    `address`          varchar(255) DEFAULT NULL COMMENT '本次执行-地址',
    `param`            varchar(512) DEFAULT NULL COMMENT '本次执行-参数',
    `sharding`         varchar(20)  DEFAULT NULL COMMENT '本次执行-分片参数，格式如 1-2-3',
    `fail_retry_count` int(11)      DEFAULT '0' COMMENT '失败重试次数',
    `result_code`      int(11)      DEFAULT '0' COMMENT '返回-code',
    `result_msg`       text COMMENT '返回-信息',
    `executor_date`    datetime     DEFAULT NULL COMMENT '执行-时间',
    `executor_code`    int(11)    NOT NULL COMMENT '执行-状态，0-成功、1-进行中、2-失败',
    `fail_retry_flag`  int(1)       DEFAULT '0' COMMENT '重试-状态，0-未重试、1-已经重试',
    `create_time`      datetime     DEFAULT NULL,
    `update_time`      datetime     DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_executor_date` (`executor_date`),
    KEY `idx_job_id` (`job_id`)
) ENGINE = InnoDB COMMENT ='调度日志';


INSERT INTO `h-job`.`h_job_server_address` (`service_name`, `title`, `address_type`, `nacos_namespace`, `nacos_group`,
                                            `address_list`, `create_time`)
VALUES ('consumer', 'consumer', 0, 'uat', 'uat', null, NOW());

INSERT INTO `h-job`.`h_job_info` (`job_server_id`, `job_desc`, `method`, `create_user`, `cron`, `route_strategy`,
                                  `path`, `param`,
                                  `executor_timeout`, `executor_fail_retry_count`, `job_status`, `job_last_time`,
                                  `job_next_time`, `create_time`, `update_time`)
VALUES (1, 'consumer', 'GET', 'hy', '*/5 * * * * ?', 'first', '/dev/test', NULL, 5000, 0, 1, 0, 0, NOW(), NOW());

INSERT INTO `h-job`.`h_job_user` (`user_name`, `password`, `role`, `permission`, `create_time`, `update_time`)
VALUES ('admin', '123456', 1, NULL, NOW(), NOW());

