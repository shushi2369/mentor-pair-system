-- 师生导师双选信息管理系统 数据库脚本（MySQL 8.0，utf8mb4，InnoDB）
-- 库名 mentor_pair 由数据源连接串自动创建（createDatabaseIfNotExist=true）
-- 本脚本可重复执行（CREATE TABLE IF NOT EXISTS）

CREATE TABLE IF NOT EXISTS sys_user (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  username    VARCHAR(50)  NOT NULL COMMENT '登录名：管理员/导师自定义，学员=学号',
  password    VARCHAR(100) NOT NULL COMMENT 'BCrypt 密文',
  real_name   VARCHAR(50)  NOT NULL COMMENT '姓名',
  role        TINYINT      NOT NULL COMMENT '1=管理员 2=导师 3=学员',
  phone       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
  status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1=启用 0=停用',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS mentor_info (
  id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id       BIGINT        NOT NULL COMMENT 'sys_user.id',
  job_title     VARCHAR(50)   DEFAULT NULL COMMENT '职称',
  department    VARCHAR(100)  DEFAULT NULL COMMENT '院系',
  research_area VARCHAR(200)  DEFAULT NULL COMMENT '研究方向',
  max_quota     INT           NOT NULL DEFAULT 0 COMMENT '可带学员名额上限',
  intro         VARCHAR(1000) DEFAULT NULL COMMENT '个人简介',
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time   DATETIME      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_mentor_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导师信息表';

CREATE TABLE IF NOT EXISTS student_info (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id     BIGINT       NOT NULL COMMENT 'sys_user.id',
  student_no  VARCHAR(50)  NOT NULL COMMENT '学号（登录名=学号）',
  major       VARCHAR(100) DEFAULT NULL COMMENT '专业',
  class_name  VARCHAR(100) DEFAULT NULL COMMENT '班级',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_student_user (user_id),
  UNIQUE KEY uk_student_no (student_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学员信息表';

CREATE TABLE IF NOT EXISTS project (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  mentor_id   BIGINT       NOT NULL COMMENT 'sys_user.id（导师）',
  title       VARCHAR(100) NOT NULL COMMENT '项目名称',
  description TEXT         NULL COMMENT '项目说明与任务要求',
  status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1=开放 0=下架',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_project_mentor (mentor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

CREATE TABLE IF NOT EXISTS selection (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  student_id  BIGINT       NOT NULL COMMENT 'sys_user.id（学员）',
  mentor_id   BIGINT       NOT NULL COMMENT 'sys_user.id（导师）',
  project_id  BIGINT       DEFAULT NULL COMMENT 'project.id，可空',
  status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=待处理 1=已接受 2=已拒绝',
  remark      VARCHAR(200) DEFAULT NULL COMMENT '导师回复',
  apply_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  handle_time DATETIME     DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (id),
  KEY idx_selection_mentor (mentor_id, status),
  KEY idx_selection_student (student_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='双选申请表';

CREATE TABLE IF NOT EXISTS submission (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  selection_id BIGINT       NOT NULL COMMENT 'selection.id',
  title        VARCHAR(100) NOT NULL COMMENT '提交标题',
  description  VARCHAR(500) DEFAULT NULL COMMENT '补充说明',
  file_name    VARCHAR(200) NOT NULL COMMENT '原始文件名',
  file_path    VARCHAR(500) NOT NULL COMMENT '磁盘存储相对路径',
  file_size    BIGINT       NOT NULL COMMENT '字节数',
  submit_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (id),
  KEY idx_submission_selection (selection_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目提交表';

CREATE TABLE IF NOT EXISTS guidance (
  id            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  submission_id BIGINT   NOT NULL COMMENT 'submission.id',
  mentor_id     BIGINT   NOT NULL COMMENT 'sys_user.id（导师）',
  content       TEXT     NOT NULL COMMENT '指导意见',
  guidance_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '指导时间',
  PRIMARY KEY (id),
  KEY idx_guidance_submission (submission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指导记录表';

CREATE TABLE IF NOT EXISTS course (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  mentor_id   BIGINT       NOT NULL COMMENT 'sys_user.id（导入导师）',
  course_name VARCHAR(100) NOT NULL COMMENT '课程名称，同导师下唯一',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_mentor_course (mentor_id, course_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表（成绩单导入）';

CREATE TABLE IF NOT EXISTS student_grade (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  course_id    BIGINT       NOT NULL COMMENT 'course.id',
  student_no   VARCHAR(50)  NOT NULL COMMENT '学号（按学号匹配学员）',
  student_name VARCHAR(50)  DEFAULT NULL COMMENT '姓名',
  score        DECIMAL(5,1) NOT NULL COMMENT '成绩 0-100',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_course_student (course_id, student_no),
  KEY idx_grade_student_no (student_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表（成绩单导入）';

CREATE TABLE IF NOT EXISTS selection_window (
  id          BIGINT   NOT NULL COMMENT '固定为 1',
  enabled     TINYINT  NOT NULL DEFAULT 0 COMMENT '1=启用时间窗 0=不限制',
  start_time  DATETIME DEFAULT NULL COMMENT '开始时间',
  end_time    DATETIME DEFAULT NULL COMMENT '截止时间',
  update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='双选时间窗配置';
