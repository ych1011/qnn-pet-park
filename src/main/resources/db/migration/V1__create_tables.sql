-- ========================================
-- 用户表（管理员 + 老师）
-- ========================================
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '登录用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    role VARCHAR(20) NOT NULL DEFAULT 'teacher' COMMENT '角色：admin/teacher',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常 0-禁用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：1-已删除 0-正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ========================================
-- 班级表（每个老师一个班级）
-- ========================================
CREATE TABLE class_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '班级ID',
    teacher_id BIGINT NOT NULL COMMENT '所属老师ID',
    name VARCHAR(100) NOT NULL COMMENT '班级名称',
    grade VARCHAR(20) DEFAULT NULL COMMENT '年级',
    semester VARCHAR(20) DEFAULT NULL COMMENT '学期',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：1-已删除 0-正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_teacher_id (teacher_id),
    CONSTRAINT fk_class_teacher FOREIGN KEY (teacher_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- ========================================
-- 宠物类型表（系统预设，6种宠物 x 5个等级）
-- ========================================
CREATE TABLE pet_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '宠物类型ID',
    code VARCHAR(30) NOT NULL COMMENT '宠物编码',
    name VARCHAR(30) NOT NULL COMMENT '宠物名称',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：1-已删除 0-正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物类型表';

-- ========================================
-- 宠物等级配置表
-- ========================================
CREATE TABLE pet_level_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    pet_type_id BIGINT NOT NULL COMMENT '宠物类型ID',
    level INT NOT NULL COMMENT '等级（1-5）',
    level_name VARCHAR(30) NOT NULL COMMENT '等级名称',
    required_score INT NOT NULL COMMENT '达到此等级需要的累计积分',
    image_url VARCHAR(500) NOT NULL COMMENT '此等级的宠物图片路径',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：1-已删除 0-正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_pet_level (pet_type_id, level),
    CONSTRAINT fk_level_pet_type FOREIGN KEY (pet_type_id) REFERENCES pet_type(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物等级配置表';

-- ========================================
-- 积分规则表
-- ========================================
CREATE TABLE score_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '规则ID',
    class_id BIGINT NOT NULL COMMENT '所属班级ID',
    name VARCHAR(100) NOT NULL COMMENT '规则名称',
    type VARCHAR(10) NOT NULL COMMENT '类型：add/subtract',
    score INT NOT NULL COMMENT '分值（正数）',
    category VARCHAR(30) DEFAULT 'study' COMMENT '分类',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：1-已删除 0-正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_rule_class FOREIGN KEY (class_id) REFERENCES class_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分规则表';

-- ========================================
-- 学生表
-- ========================================
CREATE TABLE student (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '学生ID',
    class_id BIGINT NOT NULL COMMENT '所属班级ID',
    name VARCHAR(50) NOT NULL COMMENT '学生姓名',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '学生头像URL',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-在读 0-转出',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：1-已删除 0-正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_class_id (class_id),
    CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES class_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- ========================================
-- 宠物表（每个学生一只宠物）
-- ========================================
CREATE TABLE pet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '宠物ID',
    student_id BIGINT NOT NULL COMMENT '所属学生ID',
    pet_type_id BIGINT NOT NULL COMMENT '宠物类型ID',
    custom_name VARCHAR(50) DEFAULT NULL COMMENT '学生给宠物起的名字',
    current_level INT NOT NULL DEFAULT 1 COMMENT '当前等级（1-5）',
    current_score INT NOT NULL DEFAULT 0 COMMENT '当前累计积分',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：1-已删除 0-正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领养时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_student_id (student_id),
    CONSTRAINT fk_pet_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_pet_type FOREIGN KEY (pet_type_id) REFERENCES pet_type(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物表';

-- ========================================
-- 积分记录表
-- ========================================
CREATE TABLE score_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    class_id BIGINT NOT NULL COMMENT '班级ID（冗余）',
    rule_id BIGINT DEFAULT NULL COMMENT '关联的积分规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称快照',
    type VARCHAR(10) NOT NULL COMMENT '类型：add/subtract',
    score INT NOT NULL COMMENT '分值',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：1-已删除 0-正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    INDEX idx_student_id (student_id),
    INDEX idx_class_id_time (class_id, created_at),
    CONSTRAINT fk_log_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_log_class FOREIGN KEY (class_id) REFERENCES class_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分记录表';