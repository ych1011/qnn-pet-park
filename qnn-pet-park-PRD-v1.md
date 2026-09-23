# QNN的宠物乐园 — 小学课堂积分激励工具
# 开发规范文档 v1.0

**文档版本**: v1.0
**创建日期**: 2026-09-23
**项目代号**: qnn-pet-park
**状态**: 需求已确认，可直接进入开发
**技术栈**: Spring Boot 3 + Vue 3 + MySQL 8
**PRD Generator版本**: v4.0

> **本文档是开发的唯一真相源。所有开发规范、接口定义、数据模型以此文档为准。**

---

## 1. 项目概述与目标

### 1.1 项目背景

小学1-3年级班主任希望通过"电子宠物养成"的方式激励学生课堂表现。市面上同类产品（OurTeacher、学萌岛、班级宠物园等）要么收费、要么功能过于臃肿。用户（Java开发）决定自己开发一个轻量版给老婆和同事们使用。

### 1.2 V1 核心目标

**一句话定位**：一个让小学低年级老师通过"积分养宠物"来激励学生课堂表现的Web工具。

**量化指标**：
- 支持3-5个老师同时使用，每人管理1个班级（40-50名学生）
- 课堂加减分操作2步完成（点学生 → 选规则）
- 宠物成长5个等级，视觉体验对小学低年级学生有吸引力

### 1.3 目标用户

| 角色 | role字段值 | 特征 | 核心需求 |
|------|-----------|------|---------|
| 超级管理员 | `admin` | 项目拥有者（用户本人），技术背景 | 管理老师账号、系统配置 |
| 老师 | `teacher` | 小学1-3年级班主任，非技术用户 | 课堂积分操作、学生管理、宠物分配 |

### 1.4 架构设计要点

- **单租户架构**：所有老师和学生在同一个数据库中，通过teacher_id做数据隔离
- **无多租户隔离需求**：V1规模小（3-5个老师），不需要按学校隔离
- **数据隔离策略**：每个老师只能查看和操作自己班级的数据，通过JWT中的teacher_id过滤

---

## 2. 技术架构

### 2.1 技术栈

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 前端框架 | Vue 3 | 3.4+ | Composition API |
| UI组件库 | Element Plus | 2.7+ | 管理后台和老师端 |
| 前端构建 | Vite | 5.x | 开发体验和构建速度 |
| HTTP客户端 | Axios | 1.7+ | API请求 |
| 状态管理 | Pinia | 2.x | 轻量状态管理 |
| 路由 | Vue Router | 4.x | 路由守卫 |
| 后端框架 | Spring Boot | 3.3+ | Java 21+（预留 Agent 开发功能） |
| ORM | MyBatis-Plus | 3.5+ | 简化CRUD |
| 认证 | JWT (jjwt) | 0.12+ | Token认证 |
| 数据库 | MySQL | 8.0+ | 关系型数据库 |
| 数据库迁移 | Flyway | 10.x | SQL版本管理 |
| 反向代理 | Nginx | 1.24+ | 静态资源+API转发 |
| 容器化 | Docker + Docker Compose | - | 部署标准化 |
| Java版本 | OpenJDK | 21 LTS | 长期支持版本（预留 Agent 开发功能） |

### 2.2 技术选型理由

| 选择 | 理由 |
|------|------|
| Spring Boot 3 | 用户是Java开发，最熟悉的技术栈，生态成熟 |
| Vue 3 | 用户会Vue，Composition API开发体验好 |
| Element Plus | Vue 3生态最成熟的组件库，表格/表单/对话框开箱即用 |
| MySQL 8 | 用户指定，云服务商都支持，运维简单 |
| MyBatis-Plus | 国内Java开发标配，比JPA更灵活，代码生成方便 |
| JWT | 无状态认证，前后端分离的标准方案 |
| Flyway | SQL迁移工具，版本控制数据库变更 |
| Docker Compose | 一键部署所有服务，环境一致性 |

### 2.3 系统架构图

```
┌─────────────────────────────────────────────────────────┐
│                      用户浏览器                          │
│                                                         │
│  ┌────────────┐  ┌────────────┐  ┌───────────────────┐  │
│  │  管理后台   │  │  老师端     │  │  课堂大屏展示     │  │
│  │  Vue 3     │  │  Vue 3     │  │  Vue 3 (全屏)     │  │
│  └──────┬─────┘  └──────┬─────┘  └────────┬──────────┘  │
│         └───────────────┼─────────────────┘             │
└─────────────────────────┼───────────────────────────────┘
                          │ HTTPS
                    ┌─────┴─────┐
                    │   Nginx   │
                    │  反向代理  │
                    │  静态资源  │
                    └─────┬─────┘
                          │
              ┌───────────┴───────────┐
              │   Spring Boot 后端    │
              │   :8080               │
              │                       │
              │  ├─ AuthController    │
              │  ├─ ClassController   │
              │  ├─ StudentController │
              │  ├─ PetController     │
              │  ├─ ScoreController   │
              │  ├─ RuleController    │
              │  └─ UserController    │
              └───────────┬───────────┘
                          │
                    ┌─────┴─────┐
                    │  MySQL 8  │
                    │  :3306    │
                    └───────────┘
```

### 2.4 有状态/无状态组件

| 组件 | 有状态/无状态 | 说明 |
|------|-------------|------|
| Spring Boot | 无状态 | JWT无状态认证，可水平扩展 |
| MySQL | 有状态 | 数据持久化，需定期备份 |
| Nginx | 无状态 | 仅反向代理和静态资源 |

---

## 3. 权限体系

### 3.1 权限矩阵表

| 功能 | admin（超级管理员） | teacher（老师） |
|------|:---:|:---:|
| 登录系统 | ✅ | ✅ |
| 添加老师账号 | ✅ | ❌ |
| 禁用/启用老师账号 | ✅ | ❌ |
| 重置老师密码 | ✅ | ❌ |
| 查看老师列表 | ✅ | ❌ |
| 创建/编辑班级 | ❌ | ✅（仅自己的班级） |
| 添加/编辑/删除学生 | ❌ | ✅（仅自己班级的学生） |
| 给学生分配宠物 | ❌ | ✅（仅自己班级的学生） |
| 配置积分规则 | ❌ | ✅（仅自己班级的规则） |
| 课堂加分/扣分 | ❌ | ✅（仅自己班级的学生） |
| 随机点名 | ❌ | ✅（仅自己班级） |
| 查看积分排行 | ❌ | ✅（仅自己班级） |
| 查看积分历史 | ❌ | ✅（仅自己班级） |
| 课堂大屏展示 | ❌ | ✅（仅自己班级） |
| 查看其他老师数据 | ❌ | ❌ |

### 3.2 数据过滤规则

```java
// 老师端：所有查询自动附加 teacher_id 过滤
// 通过 JWT Token 中解析出的 userId 进行过滤

// 伪代码：
@Aspect
public class DataScopeAspect {
    // 老师角色：自动在SQL WHERE条件中追加 AND teacher_id = {当前用户ID}
    // 管理员角色：不加任何过滤条件
}
```

### 3.3 前端路由守卫

| 路由前缀 | 允许角色 | 说明 |
|----------|---------|------|
| `/login` | 所有人 | 登录页 |
| `/admin/**` | admin | 管理后台 |
| `/teacher/**` | teacher | 老师端 |
| `/display/**` | teacher | 课堂大屏（全屏展示页） |

未登录访问任何受保护路由 → 跳转登录页
teacher角色访问 `/admin/**` → 跳转403页面
admin角色访问 `/teacher/**` → 跳转403页面

---

## 4. 数据模型

### 4.1 ER关系图

```
sys_user (用户表)
  │
  ├── admin 类型：超级管理员
  │
  └── teacher 类型：老师
        │
        └── class_info (班级表) — 1对1
              │
              ├── score_rule (积分规则表) — 1对多
              │
              └── student (学生表) — 1对多
                    │
                    ├── pet (宠物表) — 1对1
                    │     │
                    │     └── pet_type (宠物类型表) — N对1
                    │
                    └── score_log (积分记录表) — 1对多
```

### 4.2 完整DDL

```sql
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
    name VARCHAR(100) NOT NULL COMMENT '班级名称（如：一年级二班）',
    grade VARCHAR(20) DEFAULT NULL COMMENT '年级（如：一年级）',
    semester VARCHAR(20) DEFAULT NULL COMMENT '学期（如：2026秋季）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_teacher_id (teacher_id),
    CONSTRAINT fk_class_teacher FOREIGN KEY (teacher_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- ========================================
-- 宠物类型表（系统预设，6种宠物 × 5个等级）
-- ========================================
CREATE TABLE pet_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '宠物类型ID',
    code VARCHAR(30) NOT NULL COMMENT '宠物编码（如：cat, dog, rabbit）',
    name VARCHAR(30) NOT NULL COMMENT '宠物名称（如：小猫、小狗）',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物类型表';

-- ========================================
-- 宠物等级配置表（每个宠物类型每个等级的配置）
-- ========================================
CREATE TABLE pet_level_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    pet_type_id BIGINT NOT NULL COMMENT '宠物类型ID',
    level INT NOT NULL COMMENT '等级（1-5）',
    level_name VARCHAR(30) NOT NULL COMMENT '等级名称（如：蛋、幼崽、成长、成熟、传说）',
    required_score INT NOT NULL COMMENT '达到此等级需要的累计积分',
    image_url VARCHAR(500) NOT NULL COMMENT '此等级的宠物图片路径',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_pet_level (pet_type_id, level),
    CONSTRAINT fk_level_pet_type FOREIGN KEY (pet_type_id) REFERENCES pet_type(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物等级配置表';

-- ========================================
-- 积分规则表（每个班级自定义的加减分规则）
-- ========================================
CREATE TABLE score_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '规则ID',
    class_id BIGINT NOT NULL COMMENT '所属班级ID',
    name VARCHAR(100) NOT NULL COMMENT '规则名称（如：举手回答问题）',
    type VARCHAR(10) NOT NULL COMMENT '类型：add（加分）/ subtract（扣分）',
    score INT NOT NULL COMMENT '分值（正数，显示时根据type判断加减）',
    category VARCHAR(30) DEFAULT 'study' COMMENT '分类：study-学习/discipline-纪律/habit-习惯/morality-品德',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
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
    avatar VARCHAR(500) DEFAULT NULL COMMENT '学生头像URL（可选）',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号（可按学号排）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-在读 0-转出',
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
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领养时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_student_id (student_id),
    CONSTRAINT fk_pet_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_pet_type FOREIGN KEY (pet_type_id) REFERENCES pet_type(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物表';

-- ========================================
-- 积分记录表（每次加减分的流水记录）
-- ========================================
CREATE TABLE score_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    class_id BIGINT NOT NULL COMMENT '班级ID（冗余，方便按班级查询）',
    rule_id BIGINT DEFAULT NULL COMMENT '关联的积分规则ID（可为空，自定义加分时为空）',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称快照（记录时的规则名，防止规则改名后历史混乱）',
    type VARCHAR(10) NOT NULL COMMENT '类型：add/subtract',
    score INT NOT NULL COMMENT '分值',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    INDEX idx_student_id (student_id),
    INDEX idx_class_id_time (class_id, created_at),
    CONSTRAINT fk_log_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_log_class FOREIGN KEY (class_id) REFERENCES class_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分记录表';
```

### 4.3 索引说明

| 表 | 索引 | 类型 | 用途 |
|---|------|------|------|
| sys_user | uk_username | UNIQUE | 用户名唯一 |
| class_info | uk_teacher_id | UNIQUE | 一个老师一个班 |
| pet_type | uk_code | UNIQUE | 宠物编码唯一 |
| pet_level_config | uk_pet_level | UNIQUE | 每个宠物每个等级只有一条配置 |
| student | idx_class_id | INDEX | 按班级查学生 |
| pet | uk_student_id | UNIQUE | 一个学生一只宠物 |
| score_log | idx_student_id | INDEX | 查某学生的积分历史 |
| score_log | idx_class_id_time | INDEX | 按班级+时间查积分记录 |

### 4.4 数据库迁移策略

- 使用 **Flyway** 管理SQL版本
- 迁移文件放在 `src/main/resources/db/migration/` 目录
- 命名规范：`V1__create_tables.sql`, `V2__insert_seed_data.sql`
- 禁止直接修改已执行的迁移文件，只能新增迁移文件

### 4.5 数据增长预估

| 表 | 预估月增长量 | 归档策略 |
|---|------------|---------|
| score_log | 约 5000-10000条/月（5个班 × 40人 × 每天3-5次积分） | 学期结束后归档到 score_log_archive 表 |
| 其他表 | 几乎不增长 | 无需归档 |

---

## 5. 功能模块详细说明

### 5.1 用户认证模块

**功能描述**：登录/登出，JWT Token认证。

**业务规则**：
- 密码使用 BCrypt 加密存储（cost factor = 10）
- JWT Token 有效期 24 小时
- Token 通过 HTTP Header `Authorization: Bearer <token>` 传递
- Token 过期后前端跳转登录页

**用户交互流程**：
1. 用户打开系统 → 未登录 → 跳转登录页
2. 输入用户名和密码 → 提交
3. 验证成功 → 返回JWT Token → 前端存储到localStorage
4. 后续请求自动携带Token

**异常处理**：
- 密码错误：提示"用户名或密码错误"（不区分是用户名错还是密码错）
- 账号被禁用：提示"账号已被禁用，请联系管理员"
- Token过期：前端收到401 → 清除Token → 跳转登录页

**数据流向**：
- 登录：前端 POST `/api/auth/login` → AuthController → AuthService → 查 sys_user 表 → 返回Token
- 验证：请求拦截器 → JwtFilter → 解析Token → 设置SecurityContext

**幂等性**：登录接口天然幂等，无需特殊处理。

### 5.2 老师管理模块（管理员用）

**功能描述**：超级管理员添加、禁用、启用、重置老师密码。

**业务规则**：
- 新增老师时：管理员设置用户名、真实姓名、初始密码
- 初始密码格式：系统生成6位随机密码，创建后展示给管理员
- 禁用老师后：该老师无法登录，但班级和学生数据保留
- 重置密码：生成新的6位随机密码，展示给管理员

**用户交互流程**：
1. 管理员登录 → 进入"老师管理"页面
2. 看到老师列表（表格：姓名、用户名、状态、创建时间、操作按钮）
3. 点击"添加老师" → 弹出表单 → 填写 → 保存
4. 点击"禁用" → 二次确认 → 禁用成功
5. 点击"重置密码" → 二次确认 → 显示新密码

**异常处理**：
- 用户名已存在：提示"该用户名已被使用"
- 不能禁用自己：提示"不能禁用自己的账号"

**数据流向**：
- 列表：GET `/api/admin/teachers` → UserController → 查 sys_user WHERE role='teacher'
- 新增：POST `/api/admin/teachers` → UserController → 插入 sys_user
- 禁用/启用：PUT `/api/admin/teachers/{id}/status` → UserController → 更新 sys_user.status
- 重置密码：POST `/api/admin/teachers/{id}/reset-password` → UserController → 更新 sys_user.password

**敏感操作**：
- 禁用老师：需要二次确认弹窗
- 重置密码：需要二次确认弹窗

### 5.3 班级管理模块

**功能描述**：老师创建和编辑自己的班级信息。

**业务规则**：
- 一个老师只能有一个班级（首次登录后引导创建）
- 班级信息包括：班级名称、年级、学期
- 创建班级时自动生成一套默认积分规则（预置模板）

**用户交互流程**：
1. 老师首次登录 → 检测到没有班级 → 引导创建班级
2. 填写班级名称（如"一年级二班"）、年级、学期
3. 创建成功 → 自动跳转到学生管理页面
4. 后续可进入"班级设置"修改班级信息

**异常处理**：
- 班级名称为空：前端校验提示"请输入班级名称"

**数据流向**：
- 创建：POST `/api/teacher/classes` → ClassController → 插入 class_info + 初始化 score_rule 默认规则
- 查询：GET `/api/teacher/classes/current` → ClassController → 查 class_info WHERE teacher_id={当前用户ID}
- 更新：PUT `/api/teacher/classes/{id}` → ClassController → 更新 class_info

### 5.4 学生管理模块

**功能描述**：老师添加、编辑、删除学生。

**业务规则**：
- 每个学生属于一个班级
- 学生姓名必填，最长50个字符
- 学生创建后可以分配宠物（跳转到宠物分配页面）
- 删除学生时：连带删除该学生的宠物和积分记录（级联删除）
- 删除学生需要二次确认

**用户交互流程**：
1. 老师进入"学生管理"页面
2. 看到学生列表（卡片式或表格式，显示姓名、宠物、当前等级、积分）
3. 点击"添加学生" → 输入姓名 → 保存
4. 点击学生卡片 → 进入详情（可编辑姓名、分配/更换宠物、查看积分记录）
5. 长按/右键删除 → 二次确认 → 删除

**异常处理**：
- 学生姓名为空：前端校验提示
- 学生已分配宠物：删除时提示"该学生已有宠物，删除将同时删除宠物和积分记录"

**数据流向**：
- 列表：GET `/api/teacher/students?classId={id}` → StudentController → 查 student LEFT JOIN pet
- 新增：POST `/api/teacher/students` → StudentController → 插入 student
- 编辑：PUT `/api/teacher/students/{id}` → StudentController → 更新 student
- 删除：DELETE `/api/teacher/students/{id}` → StudentController → 级联删除 student + pet + score_log

### 5.5 宠物系统模块

**功能描述**：宠物类型管理、宠物分配、宠物升级。

**业务规则**：
- 系统预设6种宠物类型（种子数据初始化）：

| 编码 | 名称 | 说明 |
|------|------|------|
| cat | 小猫 | 橘猫 |
| dog | 小狗 | 柯基 |
| rabbit | 小兔子 | 垂耳兔 |
| panda | 小熊猫 | 大熊猫 |
| penguin | 小企鹅 | 帝企鹅 |
| dragon | 小龙 | 中国龙 |

- 5个成长等级：

| 等级 | 名称 | 累计积分要求 | 说明 |
|------|------|-------------|------|
| 1 | 蛋 | 0 | 初始状态，一颗蛋 |
| 2 | 幼崽 | 50 | 破壳而出 |
| 3 | 成长 | 150 | 快速长大 |
| 4 | 成熟 | 350 | 体型变大 |
| 5 | 传说 | 600 | 华丽终极形态 |

- 每种宠物 × 5个等级 = 30张图片素材
- 学生领养后学期内不可更换宠物类型
- 学生可以给宠物起自定义名字
- 宠物等级根据当前累计积分自动计算（不需要手动升级）
- 升级判断逻辑：遍历 pet_level_config 表，找到 required_score <= current_score 的最高等级

**用户交互流程**：
1. 老师在学生详情页点击"分配宠物"
2. 展示6种宠物的Lv.1形态图片
3. 选择一种宠物 → 可以给宠物起名字（可选） → 确认
4. 分配成功 → 学生卡片显示宠物
5. 积分增加时 → 自动判断是否升级 → 升级时前端播放升级动画

**异常处理**：
- 学生已有宠物：按钮变为"更换宠物"，点击需二次确认（"更换后当前积分保留，但宠物形态会变回新宠物的蛋"）

**数据流向**：
- 宠物类型列表：GET `/api/teacher/pet-types` → PetController → 查 pet_type + pet_level_config WHERE level=1
- 分配宠物：POST `/api/teacher/pets` → PetController → 插入 pet
- 更换宠物：PUT `/api/teacher/pets/{id}` → PetController → 更新 pet.pet_type_id，重置 current_level=1，保留 current_score
- 获取宠物信息：GET `/api/teacher/students/{id}/pet` → PetController → 查 pet JOIN pet_type JOIN pet_level_config

### 5.6 积分规则配置模块

**功能描述**：老师自定义加减分规则。

**业务规则**：
- 创建班级时自动初始化默认规则（约10条预设）
- 老师可以新增、编辑、禁用、删除规则
- 规则分类：学习(study)、纪律(discipline)、习惯(habit)、品德(morality)
- 删除规则时：已有的积分记录保留（记录中保存了 rule_name 快照）
- 分值必须为正整数，最大99

**默认预设规则**：

| 名称 | 类型 | 分值 | 分类 |
|------|------|------|------|
| 举手回答问题 | add | 2 | study |
| 回答正确 | add | 3 | study |
| 作业优秀 | add | 5 | study |
| 按时交作业 | add | 2 | study |
| 认真听讲 | add | 2 | discipline |
| 坐姿端正 | add | 1 | discipline |
| 主动帮助同学 | add | 3 | morality |
| 拾金不昧 | add | 5 | morality |
| 上课讲话 | subtract | 1 | discipline |
| 没交作业 | subtract | 3 | study |

**数据流向**：
- 规则列表：GET `/api/teacher/rules?classId={id}` → RuleController → 查 score_rule
- 新增规则：POST `/api/teacher/rules` → RuleController → 插入 score_rule
- 编辑规则：PUT `/api/teacher/rules/{id}` → RuleController → 更新 score_rule
- 删除规则：DELETE `/api/teacher/rules/{id}` → RuleController → 删除 score_rule

### 5.7 课堂积分操作模块（核心模块）

**功能描述**：老师在课堂上给学生加减分。

**业务规则**：
- 加分操作2步完成：点学生 → 选加分规则 → 完成
- 扣分操作2步完成：点学生 → 选扣分规则 → 完成
- 每次操作记录到 score_log，同时更新 pet.current_score
- 积分变化后自动判断宠物是否升级
- 支持撤销最近一次操作（10秒内可撤销）

**用户交互流程**：

**加分流程**：
1. 课堂操作页面显示学生列表（每个学生一张卡片，显示宠物+等级+积分）
2. 页面上方显示"加分规则"和"扣分规则"两组按钮
3. 老师先选一条规则（如"举手回答问题 +2"）
4. 然后点击学生卡片 → 弹出确认动画（宠物吃东西的动画） → 完成
5. 底部出现"撤销"按钮，10秒后消失

**扣分流程**：
1. 同上，但选择扣分规则
2. 点击学生 → 弹出扣分动画 → 完成

**异常处理**：
- 网络中断：提示"网络异常，请重试"，操作不生效
- 积分不能为负数：如果扣分后积分为负，则积分为0，宠物等级不降级

**数据流向**：
- 加分/扣分：POST `/api/teacher/scores` → ScoreController → ScoreService：
  1. 插入 score_log
  2. 更新 pet.current_score（加或减）
  3. 重新计算 pet.current_level
  4. 返回最新的积分和等级信息
- 撤销：DELETE `/api/teacher/scores/latest` → ScoreController → 删除最近一条 score_log + 回滚积分

**幂等性设计**：
- 每次加减分请求携带客户端生成的 UUID 作为幂等键
- 后端检查该 UUID 是否已处理过，已处理则直接返回上次结果
- 幂等键存储在 Redis 或内存缓存中，TTL = 5分钟

**并发控制**：
- 同一学生的积分操作不会真正并发（一个老师操作一个班），无需加锁
- 但为安全起见，更新 pet.current_score 时使用数据库层面的原子操作：
  ```sql
  UPDATE pet SET current_score = current_score + #{score} WHERE student_id = #{studentId}
  ```

### 5.8 随机点名模块

**功能描述**：老师点击按钮随机抽取一名学生回答问题。

**业务规则**：
- 从当前班级所有在读学生中随机抽取1人
- 抽取时显示滚动动画（学生名字快速滚动，然后减速停下）
- 动画持续约2-3秒
- 抽中的学生高亮显示

**用户交互流程**：
1. 课堂操作页面有一个"随机点名"按钮
2. 点击 → 学生列表开始滚动动画
3. 2-3秒后停下 → 抽中的学生卡片放大+高亮
4. 老师可以据此给学生加分（点击该学生 → 选规则 → 加分）

**实现方式**：纯前端动画，不需要后端接口。前端从已加载的学生列表中随机选取。

### 5.9 课堂大屏展示模块

**功能描述**：教室大屏幕全屏展示全班宠物状态。

**业务规则**：
- 独立页面，可在新标签页/新窗口打开后全屏
- 展示全班所有学生的宠物卡片（网格布局）
- 每张卡片显示：宠物图片（当前等级）、学生姓名、宠物名字、当前等级、积分
- 页面每隔30秒自动刷新数据（轮询API）
- 有学生升级时，该卡片播放升级动画（闪光/放大效果）
- 适配常见教室屏幕分辨率（1920×1080）

**用户交互流程**：
1. 老师点击"课堂大屏"按钮 → 打开新标签页
2. 新页面全屏展示 → 按F11进入浏览器全屏模式
3. 页面自动轮询刷新，保持数据最新
4. 老师在另一个窗口操作加分 → 大屏自动更新

**数据流向**：
- 全班宠物数据：GET `/api/teacher/display/{classId}` → DisplayController → 查 student + pet + pet_type + pet_level_config
- 自动刷新：前端 setInterval 30秒轮询

### 5.10 积分排行与记录模块

**功能描述**：查看班级积分排行榜和每个学生的积分历史。

**业务规则**：
- 排行榜按累计积分从高到低排列
- 显示排名、学生姓名、宠物（带当前等级图片）、积分
- 积分历史支持按学生筛选
- 积分历史显示：时间、规则名、加/减、分值

**数据流向**：
- 排行榜：GET `/api/teacher/ranking?classId={id}` → ScoreController → 查 student + pet ORDER BY current_score DESC
- 积分历史：GET `/api/teacher/score-logs?studentId={id}&page=1&size=20` → ScoreController → 分页查 score_log

---

## 6. 页面清单与路由

### 6.1 页面路由表

| 页面 | 路由 | 角色 | 说明 |
|------|------|------|------|
| 登录页 | `/login` | 所有人 | 统一登录 |
| **管理后台** | | | |
| 老师管理 | `/admin/teachers` | admin | 增删改查老师账号 |
| **老师端** | | | |
| 首页/仪表盘 | `/teacher/dashboard` | teacher | 班级概览、快速入口 |
| 创建班级 | `/teacher/class/create` | teacher | 首次使用引导创建 |
| 班级设置 | `/teacher/class/settings` | teacher | 编辑班级信息 |
| 学生管理 | `/teacher/students` | teacher | 学生列表、增删改 |
| 学生详情 | `/teacher/students/:id` | teacher | 宠物、积分记录 |
| 积分规则 | `/teacher/rules` | teacher | 加减分规则配置 |
| 课堂操作 | `/teacher/classroom` | teacher | 核心！加减分+随机点名 |
| 课堂大屏 | `/display/:classId` | teacher | 全屏展示页 |
| 积分排行 | `/teacher/ranking` | teacher | 排行榜+历史 |

### 6.2 核心用户流程图

**管理员流程**：
```
登录 → 老师管理页面 → 添加老师（填写信息）→ 告知老师账号密码 → 完成
```

**老师首次使用流程**：
```
登录 → 检测到无班级 → 引导创建班级（填名称/年级/学期）
     → 自动初始化默认积分规则
     → 跳转到学生管理 → 逐个添加学生
     → 给学生分配宠物
     → 开始使用
```

**老师日常课堂流程**：
```
登录 → 进入课堂操作页面 → 选择加分/扣分规则 → 点击学生加分/扣分
     → 偶尔使用随机点名
     → 需要时打开课堂大屏展示
     → 查看积分排行了解班级情况
```

---

## 7. API 接口清单

### 7.1 认证模块

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| POST | `/api/auth/login` | 登录 | 所有人 |
| POST | `/api/auth/logout` | 登出 | 已登录 |
| GET | `/api/auth/me` | 获取当前用户信息 | 已登录 |

**POST `/api/auth/login`**

请求体 Schema：
| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|---------|
| username | string | 是 | 1-50字符 |
| password | string | 是 | 1-100字符 |

响应体 Schema：
| 字段 | 类型 | 说明 |
|------|------|------|
| token | string | JWT Token |
| role | string | 用户角色 |
| realName | string | 真实姓名 |
| hasClass | boolean | 是否已有班级（老师角色用） |

请求示例：
```json
{
  "username": "teacher01",
  "password": "123456"
}
```

响应示例：
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "role": "teacher",
    "realName": "王老师",
    "hasClass": true
  }
}
```

### 7.2 管理员 - 老师管理模块

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET | `/api/admin/teachers` | 老师列表 | admin |
| POST | `/api/admin/teachers` | 添加老师 | admin |
| PUT | `/api/admin/teachers/{id}/status` | 禁用/启用老师 | admin |
| POST | `/api/admin/teachers/{id}/reset-password` | 重置密码 | admin |

**POST `/api/admin/teachers`**

请求体 Schema：
| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|---------|
| username | string | 是 | 3-50字符，字母数字下划线 |
| realName | string | 是 | 1-50字符 |

响应体 Schema：
| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 老师ID |
| username | string | 用户名 |
| realName | string | 真实姓名 |
| initialPassword | string | 初始密码（仅创建时返回） |

响应示例：
```json
{
  "code": 200,
  "data": {
    "id": 2,
    "username": "teacher01",
    "realName": "王老师",
    "initialPassword": "a3K8mN"
  }
}
```

### 7.3 老师端 - 班级管理模块

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET | `/api/teacher/classes/current` | 获取当前老师的班级 | teacher |
| POST | `/api/teacher/classes` | 创建班级 | teacher |
| PUT | `/api/teacher/classes/{id}` | 更新班级信息 | teacher |

### 7.4 老师端 - 学生管理模块

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET | `/api/teacher/students` | 学生列表（含宠物信息） | teacher |
| POST | `/api/teacher/students` | 添加学生 | teacher |
| PUT | `/api/teacher/students/{id}` | 编辑学生 | teacher |
| DELETE | `/api/teacher/students/{id}` | 删除学生 | teacher |

**POST `/api/teacher/students`**

请求体 Schema：
| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|---------|
| name | string | 是 | 1-50字符 |
| sortOrder | int | 否 | 排序序号 |

响应示例：
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "张小明",
    "sortOrder": 1,
    "pet": null
  }
}
```

### 7.5 老师端 - 宠物系统模块

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET | `/api/teacher/pet-types` | 宠物类型列表（含Lv.1图片） | teacher |
| GET | `/api/teacher/pet-types/{id}/levels` | 某宠物的全部等级配置 | teacher |
| POST | `/api/teacher/pets` | 给学生分配宠物 | teacher |
| PUT | `/api/teacher/pets/{id}` | 更换宠物/修改宠物名字 | teacher |

**POST `/api/teacher/pets`**

请求体 Schema：
| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|---------|
| studentId | long | 是 | 必须存在且属于当前老师的班级 |
| petTypeId | long | 是 | 必须存在 |
| customName | string | 否 | 最长50字符 |

### 7.6 老师端 - 积分规则模块

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET | `/api/teacher/rules` | 积分规则列表 | teacher |
| POST | `/api/teacher/rules` | 新增规则 | teacher |
| PUT | `/api/teacher/rules/{id}` | 编辑规则 | teacher |
| DELETE | `/api/teacher/rules/{id}` | 删除规则 | teacher |

### 7.7 老师端 - 积分操作模块

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| POST | `/api/teacher/scores` | 加分/扣分 | teacher |
| POST | `/api/teacher/scores/undo` | 撤销最近一次操作 | teacher |
| GET | `/api/teacher/ranking` | 积分排行榜 | teacher |
| GET | `/api/teacher/score-logs` | 积分记录（分页） | teacher |

**POST `/api/teacher/scores`**

请求体 Schema：
| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|---------|
| studentId | long | 是 | 必须存在且属于当前班级 |
| ruleId | long | 否 | 可选，自定义加分时为空 |
| type | string | 是 | add 或 subtract |
| score | int | 是 | 1-99 |
| ruleName | string | 是 | 1-100字符 |
| remark | string | 否 | 最长255字符 |
| idempotentKey | string | 是 | UUID，防重复提交 |

响应体 Schema：
| 字段 | 类型 | 说明 |
|------|------|------|
| logId | long | 积分记录ID |
| newScore | int | 学生新的累计积分 |
| newLevel | int | 学生宠物新的等级 |
| leveledUp | boolean | 是否触发了升级 |
| oldLevel | int | 升级前的等级（仅升级时有值） |

请求示例：
```json
{
  "studentId": 1,
  "ruleId": 5,
  "type": "add",
  "score": 2,
  "ruleName": "举手回答问题",
  "idempotentKey": "550e8400-e29b-41d4-a716-446655440000"
}
```

响应示例：
```json
{
  "code": 200,
  "data": {
    "logId": 123,
    "newScore": 52,
    "newLevel": 2,
    "leveledUp": true,
    "oldLevel": 1
  }
}
```

### 7.8 课堂大屏模块

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET | `/api/teacher/display/{classId}` | 全班宠物展示数据 | teacher |

**API总数统计**：共 **21** 个API接口。

### 7.9 统一响应格式

所有API统一返回格式：
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

错误响应：
```json
{
  "code": 400,
  "message": "用户名或密码错误",
  "data": null
}
```

**错误码定义**：
| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录/Token过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 数据冲突（如用户名重复） |
| 500 | 服务器内部错误 |

---

## 8. 安全设计

### 8.1 安全措施

| 攻击类型 | 防护措施 |
|---------|---------|
| 密码泄露 | BCrypt加密存储，cost factor=10 |
| SQL注入 | MyBatis-Plus参数化查询，禁止拼接SQL |
| XSS | Vue模板默认转义，CSP头部 |
| CSRF | JWT无状态认证，不依赖Cookie |
| 暴力破解 | 登录失败5次锁定账号30分钟（内存计数） |
| 越权访问 | 所有老师端接口校验数据归属（teacher_id） |
| Token劫持 | HTTPS传输，Token有效期24小时 |

### 8.2 日志规范

- 格式：JSON结构化日志
- 级别：INFO（正常操作）、WARN（可疑行为）、ERROR（异常）
- 脱敏：日志中不记录密码、Token原文
- 关键操作记录：登录、加减分、学生增删改

---

## 9. 部署方案

### 9.1 Docker Compose 配置

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: qnn-pet-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: qnn-pet
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql
    ports:
      - "3306:3306"

  backend:
    build: ./backend
    container_name: qnn-pet-backend
    restart: unless-stopped
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/qnn-pet?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: ${MYSQL_USER}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      mysql:
        condition: service_healthy
    ports:
      - "8080:8080"

  frontend:
    build: ./frontend
    container_name: qnn-pet-frontend
    restart: unless-stopped
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql_data:
```

### 9.2 Nginx 配置要点

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态资源
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;  # Vue Router history模式
    }

    # API反向代理
    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 静态图片资源
    location /images/ {
        root /usr/share/nginx/html;
    }
}
```

### 9.3 服务器配置推荐

| 配置项 | 推荐值 | 说明 |
|--------|-------|------|
| CPU | 2核 | 3-5个老师同时用完全够 |
| 内存 | 2GB | MySQL + Spring Boot |
| 硬盘 | 40GB SSD | 数据库+图片+系统 |
| 带宽 | 3Mbps | 主要传图片和JSON |
| 系统 | Ubuntu 22.04 LTS | 稳定、社区资源多 |
| 预估月费 | 30-50元 | 轻量应用服务器 |

---

## 10. 项目目录结构

```
qnn-pet-park/
├── backend/                          # Spring Boot 后端
│   ├── pom.xml                       # Maven依赖
│   ├── Dockerfile                    # Docker构建文件
│   └── src/main/
│       ├── java/com/qnn-pet/
│       │   ├── QNN的宠物乐园Application.java        # 启动类
│       │   ├── config/
│       │   │   ├── SecurityConfig.java         # Spring Security配置
│       │   │   ├── CorsConfig.java             # CORS跨域配置
│       │   │   └── FlywayConfig.java           # Flyway迁移配置
│       │   ├── security/
│       │   │   ├── JwtTokenProvider.java        # JWT生成/验证
│       │   │   ├── JwtAuthenticationFilter.java # Token过滤器
│       │   │   └── UserDetailsServiceImpl.java  # 用户认证
│       │   ├── controller/
│       │   │   ├── AuthController.java          # 登录/登出
│       │   │   ├── AdminUserController.java     # 管理员-老师管理
│       │   │   ├── ClassController.java         # 班级管理
│       │   │   ├── StudentController.java       # 学生管理
│       │   │   ├── PetController.java           # 宠物系统
│       │   │   ├── RuleController.java          # 积分规则
│       │   │   ├── ScoreController.java         # 积分操作
│       │   │   └── DisplayController.java       # 课堂大屏
│       │   ├── service/
│       │   │   ├── AuthService.java
│       │   │   ├── UserService.java
│       │   │   ├── ClassService.java
│       │   │   ├── StudentService.java
│       │   │   ├── PetService.java
│       │   │   ├── RuleService.java
│       │   │   └── ScoreService.java
│       │   ├── mapper/                          # MyBatis-Plus Mapper
│       │   │   ├── SysUserMapper.java
│       │   │   ├── ClassInfoMapper.java
│       │   │   ├── PetTypeMapper.java
│       │   │   ├── PetLevelConfigMapper.java
│       │   │   ├── ScoreRuleMapper.java
│       │   │   ├── StudentMapper.java
│       │   │   ├── PetMapper.java
│       │   │   └── ScoreLogMapper.java
│       │   ├── entity/                          # 数据库实体
│       │   │   ├── SysUser.java
│       │   │   ├── ClassInfo.java
│       │   │   ├── PetType.java
│       │   │   ├── PetLevelConfig.java
│       │   │   ├── ScoreRule.java
│       │   │   ├── Student.java
│       │   │   ├── Pet.java
│       │   │   └── ScoreLog.java
│       │   ├── dto/                             # 请求/响应DTO
│       │   │   ├── LoginRequest.java
│       │   │   ├── LoginResponse.java
│       │   │   ├── CreateTeacherRequest.java
│       │   │   ├── CreateStudentRequest.java
│       │   │   ├── AssignPetRequest.java
│       │   │   ├── AddScoreRequest.java
│       │   │   └── AddScoreResponse.java
│       │   └── common/
│       │       ├── Result.java                  # 统一返回格式
│       │       ├── ErrorCode.java               # 错误码枚举
│       │       └── BusinessException.java       # 业务异常
│       └── resources/
│           ├── application.yml                  # 应用配置
│           ├── application-prod.yml             # 生产环境配置
│           └── db/migration/                    # Flyway迁移文件
│               ├── V1__create_tables.sql        # 建表
│               └── V2__insert_seed_data.sql       # 种子数据
│
├── frontend/                         # Vue 3 前端
│   ├── package.json                  # 依赖
│   ├── vite.config.js                # Vite配置
│   ├── Dockerfile                    # Docker构建文件
│   ├── nginx.conf                    # Nginx配置
│   └── src/
│       ├── main.js                   # 入口
│       ├── App.vue                   # 根组件
│       ├── router/
│       │   └── index.js              # 路由配置+守卫
│       ├── stores/                   # Pinia状态管理
│       │   ├── auth.js               # 登录状态
│       │   └── class.js              # 班级/学生状态
│       ├── api/                      # API请求封装
│       │   ├── request.js            # Axios实例+拦截器
│       │   ├── auth.js
│       │   ├── teacher.js
│       │   └── admin.js
│       ├── views/
│       │   ├── Login.vue             # 登录页
│       │   ├── admin/
│       │   │   └── TeacherManage.vue # 老师管理
│       │   ├── teacher/
│       │   │   ├── Dashboard.vue     # 仪表盘
│       │   │   ├── ClassCreate.vue   # 创建班级
│       │   │   ├── ClassSettings.vue # 班级设置
│       │   │   ├── StudentList.vue   # 学生管理
│       │   │   ├── StudentDetail.vue # 学生详情
│       │   │   ├── RuleConfig.vue    # 积分规则
│       │   │   ├── Classroom.vue     # 课堂操作（核心）
│       │   │   └── Ranking.vue       # 排行榜
│       │   └── display/
│       │       └── DisplayBoard.vue  # 课堂大屏展示
│       ├── components/
│       │   ├── PetCard.vue           # 宠物卡片组件
│       │   ├── ScoreButton.vue       # 加减分按钮
│       │   ├── RandomPicker.vue      # 随机点名动画
│       │   └── LevelUpAnimation.vue  # 升级动画
│       └── assets/
│           └── images/pets/          # 宠物图片目录
│               ├── cat_lv1.png       # 小猫-蛋
│               ├── cat_lv2.png       # 小猫-幼崽
│               ├── ...               # ...共30张
│               └── dragon_lv5.png    # 小龙-传说
│
├── docker-compose.yml                # Docker编排
├── .env                              # 环境变量（不入Git）
├── .env.example                      # 环境变量模板
├── .gitignore                        # Git忽略规则
└── docs/
    └── qnn-pet-park-PRD-v1.md           # 本文档
```

---

## 11. 环境变量

### .env.example

```bash
# ========== 数据库 ==========
MYSQL_ROOT_PASSWORD=           # MySQL root密码（强密码）
MYSQL_USER=qnn-pet            # 应用数据库用户
MYSQL_PASSWORD=                # 应用数据库密码（强密码）

# ========== JWT ==========
JWT_SECRET=                    # JWT签名密钥（至少32位随机字符串）

# ========== 应用 ==========
SERVER_PORT=8080               # 后端端口
```

### 敏感变量说明

| 变量 | 说明 | 安全要求 |
|------|------|---------|
| MYSQL_ROOT_PASSWORD | 数据库root密码 | 不入Git，至少16位 |
| MYSQL_PASSWORD | 应用数据库密码 | 不入Git，至少16位 |
| JWT_SECRET | JWT签名密钥 | 不入Git，至少32位随机字符串 |

---

## 12. 第三方依赖清单

### 后端 (pom.xml 核心依赖)

| 依赖 | 版本 | 用途 |
|------|------|------|
| spring-boot-starter-web | 3.3.x | Web框架 |
| spring-boot-starter-security | 3.3.x | 安全框架 |
| spring-boot-starter-validation | 3.3.x | 参数校验 |
| mybatis-plus-spring-boot3-starter | 3.5.7 | ORM |
| mysql-connector-j | 8.0.33 | MySQL驱动 |
| flyway-core | 10.10.0 | 数据库迁移 |
| flyway-mysql | 10.10.0 | Flyway MySQL方言 |
| jjwt-api | 0.12.5 | JWT |
| jjwt-impl | 0.12.5 | JWT实现 |
| jjwt-jackson | 0.12.5 | JWT JSON处理 |
| lombok | 1.18.30 | 简化代码 |

### 前端 (package.json 核心依赖)

| 依赖 | 版本 | 用途 |
|------|------|------|
| vue | 3.4.x | 框架 |
| vue-router | 4.3.x | 路由 |
| pinia | 2.1.x | 状态管理 |
| element-plus | 2.7.x | UI组件库 |
| axios | 1.7.x | HTTP客户端 |

---

## 13. 成本估算

| 项目 | 月费用 | 说明 |
|------|--------|------|
| 云服务器 | 30-50元 | 2核2G轻量应用服务器（阿里云/腾讯云） |
| 域名 | ~5元 | .cn域名约35元/年 |
| SSL证书 | 0元 | Let's Encrypt免费证书 |
| 宠物素材 | 0元 | 自行绘制或AI生成 |
| **月度总计** | **约35-55元** | |

**V1总投入**：
- 服务器首年：约 400-600元
- 域名首年：约 35元
- 总计：约 435-635元/年

---

## 14. 开发里程碑

| 阶段 | 时间 | 交付物 |
|------|------|--------|
| **第1周** | 基础搭建 | 项目骨架、数据库、Docker环境、登录认证 |
| **第2周** | 核心功能 | 班级管理、学生管理、宠物系统、积分规则 |
| **第3周** | 课堂操作 | 加减分、随机点名、撤销、课堂大屏 |
| **第4周** | 完善+部署 | 排行榜、积分历史、管理后台、部署上线 |

---

## 15. V2 规划（第一版不做）

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 家长端 | P1 | 家长通过链接/小程序查看孩子宠物和积分 |
| 批量/小组加分 | P1 | 一次给多个学生或一个小组加分 |
| 积分小卖部 | P2 | 积分兑换奖品（免作业卡、小文具等） |
| 详细报表导出 | P2 | 按周/月生成Excel报表 |
| 多班级管理 | P2 | 一个老师管理多个班级 |
| 学期重置 | P2 | 新学期一键重置宠物和积分 |
| 成就勋章 | P2 | 达成特定条件解锁勋章（如连续5天满分） |
| 微信小程序 | P2 | 家长通过小程序查看 |

---

## 16. 种子数据脚本

**V2__insert_seed_data.sql** 需要插入的数据：

1. **超级管理员账号**：username=admin，初始密码需修改
2. **6种宠物类型**：cat, dog, rabbit, panda, penguin, dragon
3. **30条宠物等级配置**：每种宠物5个等级的名称、积分要求、图片路径
4. **默认积分规则模板**：10条预设规则（创建班级时复制）

---

## 17. 宠物图片素材规范

| 项目 | 规范 |
|------|------|
| 数量 | 6种 × 5级 = 30张 |
| 尺寸 | 256×256px（大屏展示用大图） |
| 格式 | PNG（透明背景） |
| 风格 | Q版卡通，圆润可爱，色彩明亮 |
| 命名 | `{pet_code}_lv{level}.png`（如 `cat_lv1.png`） |
| 存放 | `frontend/src/assets/images/pets/` |
| 等级视觉差异 | Lv1蛋→Lv2小幼崽→Lv3长大→Lv4更大更华丽→Lv5传说形态带特效 |

---

---

内容由 AI 生成
