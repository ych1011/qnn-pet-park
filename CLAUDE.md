# QNN的宠物乐园 - 小学课堂积分激励系统

小学课堂积分激励工具：学生通过课堂表现赚取积分，积分喂养电子宠物，宠物随积分升级进化。

## 技术栈

- **后端**: Spring Boot 3.3 + MyBatis-Plus 3.5 + JWT + MySQL 8
- **前端**: Vue 3.4 + Element Plus 2.7 + Vite 5 + Pinia
- **部署**: Docker Compose + Nginx
- **Java**: OpenJDK 21 LTS（预留 Agent 开发功能）

## 开发规范

- 所有开发规范见 `docs/qnn-pet-park-PRD-v1.md`
- 数据模型（DDL）、API接口、权限矩阵、页面路由均以PRD为准
- API统一返回格式：`{ code, message, data }`
- 时间字段统一用 `DATETIME`，时区 `Asia/Shanghai`
- 主键统一用 `BIGINT AUTO_INCREMENT`
- 枚举值统一小写（如 role: admin/teacher, type: add/subtract）

## 关键命令

```bash
# 后端
cd backend
mvn spring-boot:run                    # 启动后端（开发模式）
mvn clean package -DskipTests          # 打包
java -jar target/qnn-pet-0.0.1.jar    # 运行jar

# 前端
cd frontend
npm install                            # 安装依赖
npm run dev                            # 启动开发服务器
npm run build                          # 构建生产版本

# Docker
docker-compose up -d                   # 一键启动所有服务
docker-compose down                    # 停止所有服务
docker-compose logs -f                 # 查看日志
```

## 禁止事项

- 不要引入PRD中未列出的第三方依赖
- 不要直接修改数据库表结构，必须通过Flyway迁移文件
- 不要修改.env中的变量名
- 不要跳过PRD中定义的权限校验逻辑（老师只能操作自己班级的数据）
- 不要在代码中硬编码密码、密钥、JWT Secret
- 关键写入接口（加减分）必须实现幂等性
- 日志中不得输出密码、Token等敏感信息原文
- 所有时间字段统一用 `Asia/Shanghai` 时区，禁止混用UTC和本地时区
- Docker容器内连接其他服务用服务名（如mysql），禁止用localhost
- 前端API请求地址统一从环境变量读取，不得硬编码协议（http/https）
