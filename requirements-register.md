# 拣货机能 需求登记

## 项目信息

- 项目名称：giencoder-learn
- 业务领域：仓储管理 - 拣货作业
- 技术栈：Spring Boot 3.4.4 / Java 25 / PostgreSQL / JPA
- 基线：现有 Company CRUD 作为架构样本

## 需求列表

| 需求 ID | 需求名称 | 优先级 | 类别 | 状态 | 说明 |
| --- | --- | --- | --- | --- | --- |
| REQ-001 | 拣货单数据模型 | 高 | 功能 | approved | 定义拣货单及拣货明细的数据库表结构和实体映射 |
| REQ-002 | 创建拣货单 | 高 | 功能 | approved | 根据出库指示生成拣货单，包含拣货明细行 |
| REQ-003 | 查询拣货单 | 高 | 功能 | approved | 按拣货单号查询拣货单及其明细 |
| REQ-004 | 拣货单状态流转 | 高 | 功能 | approved | 拣货单从"待拣货"→"拣货中"→"拣货完毕"的状态变更 |
| REQ-005 | 更新拣货明细实拣数量 | 高 | 功能 | approved | 拣货作业时录入实际拣货数量 |
| REQ-006 | 删除拣货单 | 中 | 功能 | approved | 仅"待拣货"状态允许删除，其他状态拒绝 |
| REQ-007 | 拣货单号唯一性约束 | 高 | 数据规则 | approved | 拣货单号在系统中必须唯一 |
| REQ-008 | 拣货数量校验 | 高 | 数据规则 | approved | 实拣数量不能为负数；实拣数量超过指示数量时给出警告但不阻断 |
| REQ-009 | 状态变更前置条件 | 高 | 业务规则 | approved | 状态只能按顺序流转，不可跳跃或回退 |
| REQ-010 | 乐观锁并发控制 | 中 | 非功能 | approved | 使用 version 字段防止并发更新冲突 |
| REQ-011 | 逻辑删除 | 中 | 非功能 | approved | 使用 deleted_flag 实现逻辑删除，物理记录保留 |
| REQ-012 | 审计字段自动填充 | 低 | 非功能 | approved | created_ts/updated_ts 等审计字段由系统自动填充 |

## 需求详细说明

### REQ-001 拣货单数据模型

拣货单主表（s102_picking）：
- picking_id: 主键（BIGSERIAL）
- picking_no: 拣货单号（VARCHAR(20), NOT NULL, UNIQUE）
- status: 状态（VARCHAR(20), NOT NULL）— 待拣货 / 拣货中 / 拣货完毕
- warehouse_cd: 仓库代码（VARCHAR(20), NOT NULL）
- picking_date: 拣货日期（DATE, NOT NULL）
- created_ts, created_user_cd, created_program: 创建审计字段
- updated_ts, updated_user_cd, updated_program: 更新审计字段
- version: 乐观锁版本号（INTEGER, NOT NULL, DEFAULT 0）
- deleted_flag: 逻辑删除标识（VARCHAR(1), NOT NULL, DEFAULT '0'）

拣货明细表（s103_picking_detail）：
- detail_id: 主键（BIGSERIAL）
- picking_id: 关联拣货单主键（BIGINT, NOT NULL, FK）
- line_no: 行号（INTEGER, NOT NULL）
- item_cd: 商品代码（VARCHAR(20), NOT NULL）
- item_nm: 商品名称（VARCHAR(40)）
- instructed_qty: 指示数量（INTEGER, NOT NULL）
- picked_qty: 实拣数量（INTEGER, DEFAULT 0）
- lot_no: 批次号（VARCHAR(20)）
- created_ts, created_user_cd, created_program: 创建审计字段
- updated_ts, updated_user_cd, updated_program: 更新审计字段
- version: 乐观锁版本号（INTEGER, NOT NULL, DEFAULT 0）
- deleted_flag: 逻辑删除标识（VARCHAR(1), NOT NULL, DEFAULT '0'）

### REQ-002 创建拣货单

- 接收拣货单号、仓库代码、拣货日期和明细列表
- 自动设置初始状态为"待拣货"
- 自动设置明细的实拣数量默认值为 0
- 保存主表和明细表记录
- 返回创建后的拣货单（含明细）

### REQ-003 查询拣货单

- 按拣货单号查询
- 返回拣货单主信息及所有明细行
- 拣货单号不存在时返回 404

### REQ-004 拣货单状态流转

- 待拣货 → 拣货中：开始拣货作业时
- 拣货中 → 拣货完毕：所有明细行实拣数量 > 0 时
- 不可跳跃（如待拣货直接到拣货完毕）
- 不可回退（如拣货中回到待拣货）
- 非法状态变更返回错误

### REQ-005 更新拣货明细实拣数量

- 指定拣货单号和行号，更新实拣数量
- 实拣数量不能为负数
- 仅"拣货中"状态允许更新实拣数量
- 其他状态时拒绝更新

### REQ-006 删除拣货单

- 仅"待拣货"状态的拣货单允许删除
- 其他状态拒绝删除并返回错误
- 删除时同时删除所有明细行

### REQ-007 拣货单号唯一性约束

- 数据库层面 UNIQUE 约束
- 应用层面创建时校验，重复时返回错误

### REQ-008 拣货数量校验

- 实拣数量 >= 0（不可为负数）
- 实拣数量 > 指示数量时允许但记录警告信息

### REQ-009 状态变更前置条件

- 状态流转必须遵循：待拣货 → 拣货中 → 拣货完毕
- 不允许跳跃或回退
- 违反时返回明确的错误信息

### REQ-010 乐观锁并发控制

- 主表和明细表均使用 version 字段
- 更新时校验 version，不一致时返回冲突错误

### REQ-011 逻辑删除

- 删除操作设置 deleted_flag = '1' 而非物理删除
- 查询时自动过滤已删除记录

### REQ-012 审计字段自动填充

- 创建时自动填充 created_ts、created_user_cd、created_program
- 更新时自动填充 updated_ts、updated_user_cd、updated_program
- 当前阶段使用默认值（mock_user），后续对接认证系统

## 范围说明

- 范围内：拣货单 CRUD、状态流转、数量校验、并发控制、逻辑删除
- 范围外：出库指示集成、库存扣减、用户认证、前端界面、消息通知

---

*最后更新：2026-07-13*
