-- 种子数据（幂等：主键/唯一键冲突自动跳过，可重复执行）
-- 密码均为 BCrypt 密文：admin 的密码为 admin123，其余账号密码为 123456

INSERT IGNORE INTO sys_user (id, username, password, real_name, role, phone, status) VALUES
(1, 'admin', '$2a$10$MAzhE7nwcEqAPf/H2l4DeOWcnhoNEjfjkccFWaLqxahwOIqzVC0Jy', '系统管理员', 1, NULL, 1),
(2, 'T01',   '$2a$10$XmiS5AJVzRPEABm3CL/1G.OVjyyek/bHlpi1sckdHaZzgI/uOP8fe', '张教授', 2, NULL, 1),
(3, 'T02',   '$2a$10$XmiS5AJVzRPEABm3CL/1G.OVjyyek/bHlpi1sckdHaZzgI/uOP8fe', '李教授', 2, NULL, 1),
(4, 'S01',   '$2a$10$XmiS5AJVzRPEABm3CL/1G.OVjyyek/bHlpi1sckdHaZzgI/uOP8fe', '王小明', 3, NULL, 1),
(5, 'S02',   '$2a$10$XmiS5AJVzRPEABm3CL/1G.OVjyyek/bHlpi1sckdHaZzgI/uOP8fe', '李华',   3, NULL, 1),
(6, 'S03',   '$2a$10$XmiS5AJVzRPEABm3CL/1G.OVjyyek/bHlpi1sckdHaZzgI/uOP8fe', '陈晨',   3, NULL, 1),
(7, 'S04',   '$2a$10$XmiS5AJVzRPEABm3CL/1G.OVjyyek/bHlpi1sckdHaZzgI/uOP8fe', '赵蕾',   3, NULL, 1);

INSERT IGNORE INTO mentor_info (id, user_id, job_title, department, research_area, max_quota, intro) VALUES
(1, 2, '教授',   '计算机学院', '软件工程、分布式系统', 2, '主持多项国家级课题，长期指导本科毕业设计。'),
(2, 3, '副教授', '软件学院',   '数据挖掘、大数据分析', 2, '近年指导学生竞赛获奖多项，欢迎有意向的同学选择。');

INSERT IGNORE INTO student_info (id, user_id, student_no, major, class_name) VALUES
(1, 4, 'S01', '计算机科学与技术', '计科2201'),
(2, 5, 'S02', '软件工程',         '软工2202'),
(3, 6, 'S03', '计算机科学与技术', '计科2202'),
(4, 7, 'S04', '软件工程',         '软工2201');

INSERT IGNORE INTO project (id, mentor_id, title, description, status) VALUES
(1, 2, '基于 Spring Boot 的图书管理系统', '实现图书借阅、归还、库存管理与统计，完成需求分析、设计、编码与测试全流程。', 1),
(2, 3, '校园活动报名平台', '实现活动发布、在线报名、签到与数据统计，完成后撰写设计与测试文档。', 1);

INSERT IGNORE INTO course (id, mentor_id, course_name) VALUES
(1, 2, 'Java 程序设计');

-- 双选时间窗：默认不限制（管理员可在界面启用）
INSERT IGNORE INTO selection_window (id, enabled) VALUES
(1, 0);

INSERT IGNORE INTO student_grade (id, course_id, student_no, student_name, score) VALUES
(1, 1, 'S01', '王小明', 90.0),
(2, 1, 'S02', '李华',   85.0),
(3, 1, 'S03', '陈晨',   78.0),
(4, 1, 'S04', '赵蕾',   92.0),
(5, 1, 'S05', '孙倩',   66.0);
