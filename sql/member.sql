DROP TABLE IF EXISTS `member`;
create table `member`(
    `id` bigint not null comment 'id',
        `mobile` varchar(11) comment '手机号',
        PRIMARY KEY(`id`),
        UNIQUE KEY `mobile_unique` (`mobile`)
) engine=INNODB default charset=utf8mb4 comment='会员'