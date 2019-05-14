CALL insertModuleIfNotExist('','sys_upload_data','数据上传',0);
CALL insertModuleIfNotExist('sys_upload_data','sys_upload_data_qu','数据上传(曲阜)',1);

-- 曲阜上传日志表
DROP TABLE IF EXISTS `t_qf_data_upload`;
CREATE TABLE `t_qf_data_upload` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `add_time` datetime DEFAULT NULL,
  `business_id` varchar(30) DEFAULT NULL,
  `state` varchar(10) DEFAULT NULL,-- 返回类型
  `stateType` varchar(10) DEFAULT NULL COMMENT "1.无档案或者重复档案  2.无管理卡或管理卡重复  3.参数或数据格式错误  4.已存在数据  5.其他错误  6.查询接口无该次随访数据",-- 返回类型
  `upload_reason` LONGTEXT DEFAULT NULL,
  `upload_json` LONGTEXT DEFAULT NULL,
  `delete_flag` int(11) DEFAULT 0,
  `upload_type` varchar(50) DEFAULT NULL,-- 类型
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
ALTER TABLE t_qf_data_upload ADD COLUMN update_time datetime DEFAULT NULL;


SELECT * FROM t_dic_system s WHERE s.type='上传状态';
DELETE FROM t_dic_system WHERE type='上传状态' AND big_type='婚前医学检查';
INSERT INTO `t_dic_system`(text,type,big_type,code,statue) VALUES ('上传完成', '上传状态', '婚前医学检查', '1', '0');
INSERT INTO `t_dic_system`(text,type,big_type,code,statue) VALUES ('上传中', '上传状态', '婚前医学检查', '3', '0');
INSERT INTO `t_dic_system`(text,type,big_type,code,statue) VALUES ('上传失败', '上传状态', '婚前医学检查', '2', '0');
DELETE FROM t_dic_system WHERE type='上传类型' AND big_type='婚前医学检查';
INSERT INTO `t_dic_system`(text,type,big_type,code,statue) VALUES ('孕产妇', '上传类型', '婚前医学检查', '1', '0');
INSERT INTO `t_dic_system`(text,type,big_type,code,statue) VALUES ('儿童', '上传类型', '婚前医学检查', '2', '0');