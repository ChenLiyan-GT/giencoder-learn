# Demo 实现说明

## 项目结构

```
src/main/java/com/example/demo/
├── DemoApplication.java          # 主启动类
├── controller/
│   └── CompanyController.java    # Controller 层 - 接收 company_cd 参数
├── service/
│   └── CompanyService.java       # Service 层 - 连接 DB 查询
├── repository/
│   └── CompanyRepository.java    # Repository 层 - 数据访问接口
├── entity/
│   └── Company.java              # Entity 实体类 - 映射 m101_company 表
└── dto/
    └── CompanyDTO.java           # DTO - 封装查询结果
```

## 功能说明

### API 接口

**查询公司信息**
- **URL**: `GET /api/companies/{companyCd}`
- **参数**: `companyCd` - 公司代码（路径参数）
- **返回**: `CompanyDTO` - 公司信息

### 数据流程

```
Controller (接收 company_cd)
    ↓
Service (调用 Repository 查询 DB)
    ↓
Repository (执行 SQL 查询)
    ↓
Entity (映射数据库记录)
    ↓
Service (转换为 DTO)
    ↓
Controller (返回 JSON 响应)
```

## 数据库配置

### PostgreSQL 连接信息
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=1234
spring.datasource.driver-class-name=org.postgresql.Driver
```

### 表结构
```sql
CREATE TABLE scash.m101_company (
  company_id BIGSERIAL NOT NULL,
  company_cd CHARACTER VARYING(20) NOT NULL,
  company_nm_kana CHARACTER VARYING(40),
  company_nm_kanji CHARACTER VARYING(40) NOT NULL,
  company_abbreviation CHARACTER VARYING(20),
  postal_cd CHARACTER VARYING(7),
  area_cd CHARACTER VARYING(5),
  address CHARACTER VARYING(240),
  phone_no CHARACTER VARYING(20),
  fax_no CHARACTER VARYING(20),
  created_ts TIMESTAMP(6) WITH TIME ZONE DEFAULT NOW(),
  created_user_cd CHARACTER VARYING(16),
  created_program CHARACTER VARYING(50),
  updated_ts TIMESTAMP(6) WITH TIME ZONE DEFAULT NOW(),
  updated_user_cd CHARACTER VARYING(16),
  updated_program CHARACTER VARYING(50),
  version INTEGER DEFAULT 0 NOT NULL,
  deleted_flag CHARACTER VARYING(1) DEFAULT '0' NOT NULL,
  PRIMARY KEY (company_id)
);
```

## 使用方法

### 1. 启动应用
```bash
mvn spring-boot:run
```

### 2. 调用 API
```bash
# 查询公司代码为 'COMP001' 的公司信息
curl http://localhost:8080/api/companies/COMP001
```

### 3. 响应示例
```json
{
  "companyId": 1,
  "companyCd": "COMP001",
  "companyNmKana": "カイシャ",
  "companyNmKanji": "株式会社サンプル",
  "companyAbbreviation": "サンプル",
  "postalCd": "100-0001",
  "areaCd": "13101",
  "address": "東京都千代田区",
  "phoneNo": "03-1234-5678",
  "faxNo": "03-1234-5679",
  "createdTs": "2026-07-01T10:00:00Z",
  "version": 0,
  "deletedFlag": "0"
}
```

## 技术要点

### 1. 分层架构
- **Controller 层**: 处理 HTTP 请求/响应
- **Service 层**: 业务逻辑、事务管理
- **Repository 层**: 数据访问（JPA）
- **Entity 层**: 数据库表映射
- **DTO 层**: 数据传输对象

### 2. PostgreSQL 连接共通
- 使用 Spring Data JPA
- HikariCP 连接池
- 自动事务管理

### 3. 查询逻辑
- Service 层通过 Repository 查询数据库
- 查询结果从 Entity 转换为 DTO
- Controller 层返回封装好的 DTO

## 依赖管理

```xml
<!-- Spring Boot Starter Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

## 编译验证

```bash
# 编译项目
mvn clean compile

# 打包项目
mvn clean package

# 运行测试
mvn test
```

---
*最后更新：2026-07-02*
