# 拣货机能 产品需求文档（PRD）

## 1. 概述

### 1.1 产品目标

为仓储管理系统提供拣货作业的核心后端 API，支持拣货单的创建、查询、状态流转、实拣数量录入和删除，作为全项目展开的架构样本。

### 1.2 目标用户

- 仓库作业人员：执行拣货作业，录入实拣数量
- 仓库管理员：创建拣货单、监控拣货进度、删除无效拣货单

### 1.3 范围

- 范围内：拣货单 CRUD API、状态流转、数量校验、并发控制、逻辑删除
- 范围外：出库指示集成、库存扣减、用户认证、前端界面、消息通知

## 2. 功能需求

### 2.1 创建拣货单（REQ-002）

**API**: `POST /api/pickings`

**请求体**:
```json
{
  "pickingNo": "PK-20260713-001",
  "warehouseCd": "WH001",
  "pickingDate": "2026-07-13",
  "details": [
    {
      "lineNo": 1,
      "itemCd": "ITEM001",
      "itemNm": "商品A",
      "instructedQty": 10,
      "lotNo": "LOT001"
    }
  ]
}
```

**验收标准**:
- AC-PRD-001: 创建成功时返回 HTTP 201 及完整拣货单数据（含明细）
- AC-PRD-002: 自动设置状态为"待拣货"
- AC-PRD-003: 自动设置明细的实拣数量（pickedQty）默认值为 0
- AC-PRD-004: 拣货单号已存在时返回 HTTP 409 及错误信息
- AC-PRD-005: 必填字段（pickingNo、warehouseCd、pickingDate、details）缺失时返回 HTTP 400
- AC-PRD-006: 明细的 instructedQty <= 0 时返回 HTTP 400
- AC-PRD-007: 审计字段自动填充（created_ts、created_user_cd、created_program）

### 2.2 查询拣货单（REQ-003）

**API**: `GET /api/pickings/{pickingNo}`

**验收标准**:
- AC-PRD-008: 拣货单存在时返回 HTTP 200 及拣货单主信息与所有明细行
- AC-PRD-009: 拣货单不存在时返回 HTTP 404
- AC-PRD-010: 已逻辑删除的拣货单不在查询结果中

### 2.3 拣货单状态流转（REQ-004, REQ-009）

**API**: `PATCH /api/pickings/{pickingNo}/status`

**请求体**:
```json
{
  "status": "拣货中"
}
```

**验收标准**:
- AC-PRD-011: 待拣货 → 拣货中，成功时返回 HTTP 200
- AC-PRD-012: 拣货中 → 拣货完毕，成功时返回 HTTP 200
- AC-PRD-013: 状态跳跃（如待拣货 → 拣货完毕）时返回 HTTP 400 及错误信息
- AC-PRD-014: 状态回退（如拣货中 → 待拣货）时返回 HTTP 400 及错误信息
- AC-PRD-015: 拣货单不存在时返回 HTTP 404
- AC-PRD-016: 拣货完毕 → 拣货中，返回 HTTP 400 及错误信息

### 2.4 更新拣货明细实拣数量（REQ-005, REQ-008）

**API**: `PATCH /api/pickings/{pickingNo}/details/{lineNo}/picked-qty`

**请求体**:
```json
{
  "pickedQty": 8
}
```

**验收标准**:
- AC-PRD-017: 拣货中状态下更新实拣数量成功时返回 HTTP 200
- AC-PRD-018: 实拣数量为负数时返回 HTTP 400
- AC-PRD-019: 非拣货中状态下更新实拣数量时返回 HTTP 400 及错误信息
- AC-PRD-020: 拣货单或明细不存在时返回 HTTP 404
- AC-PRD-021: 实拣数量超过指示数量时允许更新，响应中包含警告信息

### 2.5 删除拣货单（REQ-006, REQ-011）

**API**: `DELETE /api/pickings/{pickingNo}`

**验收标准**:
- AC-PRD-022: 待拣货状态下删除成功时返回 HTTP 204
- AC-PRD-023: 非待拣货状态下删除时返回 HTTP 400 及错误信息
- AC-PRD-024: 拣货单不存在时返回 HTTP 404
- AC-PRD-025: 删除为逻辑删除（deleted_flag = '1'），非物理删除
- AC-PRD-026: 删除主表时同时逻辑删除所有关联明细行

## 3. 非功能需求

### 3.1 并发控制（REQ-010）

**验收标准**:
- AC-PRD-027: 更新时 version 不一致返回 HTTP 409 及错误信息

### 3.2 逻辑删除（REQ-011）

**验收标准**:
- AC-PRD-028: 查询 API 自动过滤 deleted_flag = '1' 的记录

### 3.3 审计字段（REQ-012）

**验收标准**:
- AC-PRD-029: 创建记录时自动填充 created_ts（当前时间）、created_user_cd（mock_user）、created_program
- AC-PRD-030: 更新记录时自动填充 updated_ts（当前时间）、updated_user_cd（mock_user）、updated_program

## 4. 数据模型（REQ-001）

### 4.1 拣货单主表 s102_picking

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| picking_id | BIGSERIAL | PK | 主键 |
| picking_no | VARCHAR(20) | NOT NULL, UNIQUE | 拣货单号 |
| status | VARCHAR(20) | NOT NULL | 状态 |
| warehouse_cd | VARCHAR(20) | NOT NULL | 仓库代码 |
| picking_date | DATE | NOT NULL | 拣货日期 |
| created_ts | TIMESTAMP | | 创建时间 |
| created_user_cd | VARCHAR(16) | | 创建用户 |
| created_program | VARCHAR(50) | | 创建程序 |
| updated_ts | TIMESTAMP | | 更新时间 |
| updated_user_cd | VARCHAR(16) | | 更新用户 |
| updated_program | VARCHAR(50) | | 更新程序 |
| version | INTEGER | NOT NULL DEFAULT 0 | 乐观锁版本 |
| deleted_flag | VARCHAR(1) | NOT NULL DEFAULT '0' | 逻辑删除标识 |

### 4.2 拣货明细表 s103_picking_detail

| 字段 | 类型 | 约束 | 说明 |
| --- | --- | --- | --- |
| detail_id | BIGSERIAL | PK | 主键 |
| picking_id | BIGINT | NOT NULL, FK | 关联拣货单 |
| line_no | INTEGER | NOT NULL | 行号 |
| item_cd | VARCHAR(20) | NOT NULL | 商品代码 |
| item_nm | VARCHAR(40) | | 商品名称 |
| instructed_qty | INTEGER | NOT NULL | 指示数量 |
| picked_qty | INTEGER | DEFAULT 0 | 实拣数量 |
| lot_no | VARCHAR(20) | | 批次号 |
| created_ts | TIMESTAMP | | 创建时间 |
| created_user_cd | VARCHAR(16) | | 创建用户 |
| created_program | VARCHAR(50) | | 创建程序 |
| updated_ts | TIMESTAMP | | 更新时间 |
| updated_user_cd | VARCHAR(16) | | 更新用户 |
| updated_program | VARCHAR(50) | | 更新程序 |
| version | INTEGER | NOT NULL DEFAULT 0 | 乐观锁版本 |
| deleted_flag | VARCHAR(1) | NOT NULL DEFAULT '0' | 逻辑删除标识 |

## 5. 状态机

```
待拣货 ──→ 拣货中 ──→ 拣货完毕

规则：
- 只能正向流转，不可跳跃、不可回退
- 待拣货：初始状态，允许删除
- 拣货中：允许更新实拣数量
- 拣货完毕：终态，不允许任何修改
```

---

*最后更新：2026-07-13*
