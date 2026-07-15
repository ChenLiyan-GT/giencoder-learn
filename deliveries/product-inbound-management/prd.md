# 商品入库/拣货/出库/管理机能 PRD

## 1. 背景与目标

- **业务背景**：仓储管理系统需覆盖商品从入库登记到出库确认的完整物流链路，并支持仓库间的商品调拨。当前系统已有 Company 模块作为基础，需要新增入库、拣货、出库、库存管理、仓库调拨 5 大核心机能模块，形成完整的仓储作业闭环。（BR-001, BR-002）
- **目标**：
  1. 实现入库登记→库存增减→拣货执行→出库确认→仓库调拨的完整业务流程
  2. 建立商品主数据与库存管理基础
  3. 各模块具备完整的 CRUD 操作与状态流转能力
  4. 全项目测试指令/分支覆盖率 100%，变异覆盖率 100%（BR-002）
- **成功标准**：
  - 5 大机能模块各含可工作的 API 端点（BR-001）
  - `mvn test` 和 `mvn pitest:mutationCoverage` 均通过，无覆盖率不达标项（BR-002）
  - 入库→库存→拣货→出库端到端流程可完整执行（INT-004）
  - 跨仓库调拨全流程可完整执行：创建→审批→调出→调入→完成（INT-006, INT-007）
- **关联需求 ID**：BR-001, BR-002

## 2. 用户角色与场景

| 角色 | 场景 | 目标 | 关联需求 ID |
| --- | --- | --- | --- |
| 入库操作员 | 收到供应商送货，创建入库单登记商品信息 | 登记入库单，状态初始为 RECEIVED | FUNC-001, RULE-001 |
| 入库操作员 | 验收商品合格后确认入库 | 入库单状态变更为 CONFIRMED，库存数量增加 | FUNC-003, RULE-002 |
| 入库操作员 | 验收商品不合格，拒绝入库 | 入库单状态变更为 REJECTED，库存不变 | FUNC-004, RULE-003 |
| 出库操作员 | 收到出库指示，创建出库单 | 校验库存充足后创建出库单，预占库存 | FUNC-009, RULE-011, RULE-017 |
| 拣货员 | 根据出库单创建拣货单，开始拣货作业 | 创建拣货单，执行分批拣货 | FUNC-005, FUNC-007, RULE-008 |
| 拣货员 | 拣货完成后确认拣货完毕 | 拣货单状态变更为 COMPLETED，关联出库单状态变更为 PICKED | FUNC-008, FLOW-002, Q-003 |
| 出库操作员 | 拣货完成后确认发货 | 出库单状态变更为 SHIPPED，库存数量扣减 | FUNC-011, RULE-012 |
| 库存管理员 | 查询当前库存状况 | 按公司/商品筛选，查看库存列表 | FUNC-013, FUNC-014, Q-005 |
| 库存管理员 | 盘点后发现差异，手动调整库存 | 记录调整原因，修正库存数量 | FUNC-015, RULE-019 |
| 数据管理员 | 维护商品主数据 | 创建、查询、更新、删除商品信息 | FUNC-016, FIELD-004, FIELD-009 |
| 调拨操作员 | 需要将商品从 A 仓库调拨到 B 仓库 | 创建调拨单，指定调出/调入方及商品数量 | FUNC-017, RULE-021, RULE-023 |
| 调拨审批员 | 审批调拨申请 | 审批通过后调出方库存预占 | FUNC-019, INT-007 |
| 调拨操作员 | 调出方确认发货 | 调拨单状态变更为 SHIPPED，调出方库存扣减 | FUNC-020, INT-006 |
| 调入方操作员 | 调入方确认收货 | 调拨单状态变更为 COMPLETED，调入方库存增加 | FUNC-021, INT-006 |
| 调拨审批员 | 取消调拨单 | 调拨单状态变更为 CANCELLED，恢复调出方库存预占 | FUNC-022, INT-007 |

## 3. 功能范围

### 范围内

| 功能 | 说明 | 关联需求 ID |
| --- | --- | --- |
| 商品主数据管理 | 商品信息的创建、查询、更新、删除 | FUNC-016, FIELD-004, FIELD-009, RULE-020 |
| 入库单创建 | 登记入库单号、公司代码、商品代码、数量，初始状态 RECEIVED | FUNC-001, RULE-001, RULE-005 |
| 入库单查询 | 按入库单号查询入库单详情 | FUNC-002 |
| 入库确认 | 确认入库单，状态变更为 CONFIRMED，同步增加库存 | FUNC-003, RULE-002, INT-001 |
| 入库拒绝 | 拒绝入库单，状态变更为 REJECTED，库存不变 | FUNC-004, RULE-003 |
| 出库单创建 | 校验可用库存后创建出库单，预占库存 | FUNC-009, RULE-011, RULE-015, RULE-017 |
| 出库单查询 | 按出库单号查询出库单详情 | FUNC-010 |
| 出库确认（发货） | 确认发货，状态变更为 SHIPPED，扣减库存 | FUNC-011, RULE-012, INT-002 |
| 出库取消 | 取消出库单，状态变更为 CANCELLED，恢复库存预占 | FUNC-012, RULE-013, RULE-014 |
| 拣货单创建 | 关联出库单创建拣货单，初始状态 PENDING | FUNC-005, RULE-006, RULE-010 |
| 拣货单查询 | 按拣货单号查询拣货单详情 | FUNC-006 |
| 执行拣货 | 累加记录实拣数量，状态变更为 PICKING | FUNC-007, RULE-007, RULE-008 |
| 完成拣货 | 校验实拣数量达标后完成拣货，状态变更为 COMPLETED | FUNC-008, RULE-009 |
| 库存列表查询 | 分页查询库存列表，支持公司代码和商品代码筛选 | FUNC-013, Q-005 |
| 库存详情查询 | 按公司代码+商品代码查询库存详情 | FUNC-014 |
| 库存调整 | 手动调整库存数量，记录调整原因及前后数量 | FUNC-015, RULE-019, Q-002 |
| 调拨单创建 | 登记调拨单号、调出/调入公司代码、商品代码、数量，初始状态 PENDING | FUNC-017, RULE-021, RULE-022, RULE-023, RULE-024 |
| 调拨单查询 | 按调拨单号查询调拨单详情 | FUNC-018 |
| 调拨审批 | 审批调拨单，状态变更为 APPROVED，调出方库存预占 | FUNC-019, RULE-026, INT-007 |
| 调拨出库 | 确认调拨出库，状态变更为 SHIPPED，调出方库存扣减 | FUNC-020, RULE-026, INT-006 |
| 调拨入库 | 确认调拨入库，状态变更为 COMPLETED，调入方库存增加 | FUNC-021, RULE-026, INT-006 |
| 调拨取消 | 取消调拨单，状态变更为 CANCELLED，恢复调出方库存预占 | FUNC-022, RULE-025, INT-007 |
| 端到端流程验证 | 入库→库存增加→出库→拣货→出库确认→库存扣减全流程 | INT-004 |
| 调拨端到端流程 | 调拨创建→审批→调出→调入→完成，全流程执行成功 | INT-006, INT-007 |

### 范围外

| 项目 | 原因 | 关联需求 ID |
| --- | --- | --- |
| 用户认证与授权 | 本次使用默认用户（mock_user），后续对接认证系统 | — |
| 前端界面 | 本次仅实现后端 API，无前端页面 | — |
| 消息通知 | 不在本期需求范围内 | — |
| WMS 硬件集成（条码枪/AGV） | 不在本期需求范围内 | — |
| 批次/序列号追踪 | 本次不强制，后续可扩展 | Q-001 |
| 报表导出 | 不在本期需求范围内 | — |
| 库存调整日志表 | 本次最小实现，调整原因在请求体中携带 | Q-002 |

## 4. 业务流程

### 4.1 商品管理流程

**主流程**：
1. 数据管理员创建商品信息（商品代码、商品名称汉字、商品名称假名、单位代码）
2. 系统校验商品代码唯一性，创建成功
3. 按商品代码查询商品详情
4. 更新商品信息
5. 删除商品（逻辑删除，deleted_flag='1'）

**异常流程**：
- 商品代码重复 → 创建失败
- 商品代码不存在 → 查询/更新/删除返回 404
- 已删除商品 → 查询时自动过滤

**关联需求 ID**：FUNC-016, FIELD-004, FIELD-009, RULE-020

### 4.2 入库流程

**主流程**：
1. 入库操作员创建入库单（入库单号、公司代码、商品代码、数量）
2. 系统校验入库单号唯一性，设置初始状态为 RECEIVED
3. 入库操作员验收商品
4. 验收合格 → 确认入库 → 状态变更为 CONFIRMED → 库存数量增加
5. 验收不合格 → 拒绝入库 → 状态变更为 REJECTED → 库存不变

**异常流程**：
- 入库单号重复 → 创建失败
- 已确认或已拒绝的入库单再次操作 → 返回错误
- 入库单不存在 → 查询返回 404

**边界流程**：
- 入库的商品在当前库存表中不存在 → 自动创建库存记录（初始 quantity=0，再增加）

**关联需求 ID**：FUNC-001~FUNC-004, RULE-001~RULE-005, FLOW-001, INT-001

### 4.3 出库流程

**主流程**：
1. 出库操作员创建出库单（出库单号、公司代码、商品代码、数量）
2. 系统校验可用库存（quantity - reserved_quantity）≥ 出库数量
3. 校验通过 → 创建出库单，状态为 CREATED，reserved_quantity 增加
4. 拣货员完成拣货后，出库单状态变更为 PICKED
5. 出库操作员确认发货 → 状态变更为 SHIPPED → quantity 扣减，reserved_quantity 减少

**异常流程**：
- 可用库存不足 → 创建出库单失败
- 出库单号重复 → 创建失败
- 已发货（SHIPPED）的出库单取消 → 返回错误
- 出库单不存在 → 查询返回 404

**边界流程**：
- 取消出库单（CREATED/PICKED 状态）→ 状态变更为 CANCELLED → reserved_quantity 恢复
- 出库商品库存记录不存在 → 创建失败

**关联需求 ID**：FUNC-009~FUNC-012, RULE-011~RULE-015, FLOW-003, INT-002

### 4.4 拣货流程

**主流程**：
1. 拣货员根据出库单创建拣货单（拣货单号、出库单号、商品代码、数量）
2. 系统校验出库单存在且状态合法，设置初始状态为 PENDING
3. 拣货员执行拣货 → picked_quantity 累加 → 状态变更为 PICKING
4. 拣货员可分批拣货，多次累加 picked_quantity
5. picked_quantity ≥ quantity → 完成拣货 → 状态变更为 COMPLETED → 出库单状态变更为 PICKED

**异常流程**：
- 拣货单号重复 → 创建失败
- 出库单号无效 → 创建拣货单失败
- 可用库存不足 → 拣货被拒绝
- picked_quantity < quantity 时完成拣货 → 被拒绝
- 非法状态流转（跳跃或回退）→ 返回错误

**边界流程**：
- 拣货数量超过指示数量 → 允许但记录警告
- 分批拣货：多次 pick 操作累加 picked_quantity

**关联需求 ID**：FUNC-005~FUNC-008, RULE-006~RULE-010, FLOW-002, INT-003

### 4.5 库存管理流程

**主流程**：
1. 库存管理员查询库存列表（支持公司代码、商品代码筛选 + 分页）
2. 查询指定商品的库存详情（公司代码 + 商品代码）
3. 库存管理员执行库存调整 → 记录调整原因、调整前后数量 → 更新库存

**异常流程**：
- 调整后的库存数量为负数 → 调整失败
- 公司代码 + 商品代码组合不存在 → 查询返回 404

**关联需求 ID**：FUNC-013~FUNC-015, RULE-016~RULE-019

### 4.6 仓库调拨流程

**主流程**：
1. 调拨操作员创建调拨单（调拨单号、调出公司代码、调入公司代码、商品代码、数量）
2. 系统校验调拨单号唯一性、调出方≠调入方、调出方可用库存 ≥ 调拨数量
3. 校验通过 → 创建调拨单，状态为 PENDING
4. 调拨审批员审批 → 状态变更为 APPROVED → 调出方 reserved_quantity 增加（预占）
5. 调拨操作员确认出库 → 状态变更为 SHIPPED → 调出方 quantity 扣减，reserved_quantity 减少
6. 调入方操作员确认入库 → 状态变更为 COMPLETED → 调入方 quantity 增加

**异常流程**：
- 调拨单号重复 → 创建失败
- 调出方与调入方相同 → 创建失败
- 调出方可用库存不足 → 创建失败
- 非法状态流转（跳跃或回退）→ 返回错误
- SHIPPED / COMPLETED 状态的调拨单取消 → 返回错误
- 调拨单不存在 → 查询返回 404

**边界流程**：
- 取消调拨单（PENDING/APPROVED 状态）→ 状态变更为 CANCELLED → 调出方 reserved_quantity 恢复
- 调入方库存记录不存在 → 自动创建（初始 quantity=0，再增加）

**关联需求 ID**：FUNC-017~FUNC-022, RULE-021~RULE-026, FLOW-004, INT-006, INT-007

### 4.7 端到端集成流程

**完整链路**：
1. 创建商品主数据 → 创建入库单 → 确认入库 → 库存增加
2. 创建出库单（库存预占）→ 创建拣货单 → 执行拣货 → 完成拣货 → 出库单状态变更为 PICKED
3. 确认出库 → 库存扣减 → 完成全流程
4. 创建调拨单 → 审批 → 调出（调出方库存扣减）→ 调入（调入方库存增加）→ 完成调拨

**关联需求 ID**：INT-001~INT-007

## 5. 业务规则

| 规则 | 说明 | 关联需求 ID |
| --- | --- | --- |
| 入库单号全局唯一 | 重复入库单号无法创建 | RULE-005 |
| 入库单初始状态 | 新建入库单状态为 RECEIVED | RULE-001 |
| 入库确认增加库存 | 确认入库后，Inventory.quantity += InboundOrder.quantity | RULE-002 |
| 入库拒绝不增库存 | 拒绝入库仅变更状态，不触发库存变更 | RULE-003 |
| 入库单终态不可操作 | CONFIRMED / REJECTED 状态不可再次确认或拒绝 | RULE-004 |
| 入库单状态流 | RECEIVED → CONFIRMED 或 RECEIVED → REJECTED；终态不可再变更 | FLOW-001 |
| 出库单号全局唯一 | 重复出库单号无法创建 | RULE-015 |
| 出库前库存充足性校验 | 可用库存（quantity - reserved_quantity）≥ 出库数量 | RULE-011 |
| 出库确认扣减库存 | SHIP 操作后，Inventory.quantity -= OutboundOrder.quantity | RULE-012 |
| 已出库订单不可取消 | SHIPPED 状态的出库单无法取消 | RULE-013 |
| 取消出库恢复预占 | CANCELLED 操作恢复 reserved_quantity | RULE-014 |
| 出库单状态流 | CREATED → PICKED → SHIPPED；CREATED/PICKED → CANCELLED | FLOW-003 |
| 拣货单号全局唯一 | 重复拣货单号无法创建 | RULE-010 |
| 拣货单关联出库单 | 拣货单必须关联有效的出库单号 | RULE-006 |
| 拣货前库存充足性校验 | 可用库存（quantity - reserved_quantity）≥ 本次拣货数量 | RULE-007 |
| 拣货数量累加 | picked_quantity 在原值基础上累加，支持分批拣货 | RULE-008 |
| 拣货完成条件 | picked_quantity ≥ quantity 时允许完成拣货 | RULE-009 |
| 拣货单状态流 | PENDING → PICKING → COMPLETED；PENDING/PICKING → CANCELLED | FLOW-002 |
| 库存数量不可为负 | Inventory.quantity 始终 ≥ 0 | RULE-016 |
| 库存预占机制 | 创建出库单/审批调拨单时增加 reserved_quantity；取消/发货/调拨完成后减少 | RULE-017 |
| 可用库存计算 | 出库/拣货/调拨校验基于 (quantity - reserved_quantity) | RULE-018 |
| 库存调整记录 | 调整操作需记录原因（reason）、调整前数量（before_qty）、调整后数量（after_qty） | RULE-019 |
| 商品代码全局唯一 | 重复商品代码无法创建 | RULE-020 |
| 调拨单号全局唯一 | 重复调拨单号无法创建 | RULE-022 |
| 调拨单初始状态 | 新建调拨单状态为 PENDING | RULE-021 |
| 调拨前库存充足性校验 | 调出方可用库存（quantity - reserved_quantity）≥ 调拨数量 | RULE-023 |
| 调出方与调入方不可相同 | from_company_cd ≠ to_company_cd | RULE-024 |
| 已发货/已完成调拨单不可取消 | SHIPPED / COMPLETED 状态的调拨单无法取消 | RULE-025 |
| 调拨单状态流 | PENDING → APPROVED → SHIPPED → COMPLETED；PENDING/APPROVED → CANCELLED | FLOW-004 |
| 调拨状态变更限制 | 仅 PENDING 可审批，仅 APPROVED 可出库，仅 SHIPPED 可入库 | RULE-026 |

## 6. 字段、数据与状态口径

### 6.1 数据库命名规则

| 项目 | 口径 | 来源 | 关联需求 ID |
| --- | --- | --- | --- |
| 数据库 | PostgreSQL | 用户输入 | FIELD-001 |
| Schema | `scash` | 项目配置 | FIELD-001 |
| 主数据表命名 | `scash.a<序号>_<表名>`（如 `a101_product`, `a102_inventory`） | 用户输入 | FIELD-002 |
| 业务交易表命名 | `scash.s<序号>_<表名>`（如 `s101_inbound_order`, `s102_picking_order`, `s103_outbound_order`, `s105_transfer_order`） | 用户输入 | FIELD-002 |

### 6.2 表字段口径

**商品主数据表（`a101_product`）**

| 字段 | 类型 | 约束 | 说明 | 关联需求 ID |
| --- | --- | --- | --- | --- |
| product_id | BIGINT | PK, 自增 | 主键 | FIELD-004, FIELD-009 |
| product_cd | VARCHAR(20) | NOT NULL, UNIQUE | 商品代码 | FIELD-009, RULE-020 |
| product_nm_kanji | VARCHAR(40) | NOT NULL | 商品名称（汉字） | FIELD-009 |
| product_nm_kana | VARCHAR(40) | | 商品名称（假名） | FIELD-009 |
| unit_cd | VARCHAR(10) | | 单位代码 | FIELD-009 |
| 审计字段 | — | — | 8 个审计字段 | FIELD-003 |

**入库单表（`s101_inbound_order`）**

| 字段 | 类型 | 约束 | 说明 | 关联需求 ID |
| --- | --- | --- | --- | --- |
| inbound_order_id | BIGINT | PK, 自增 | 主键 | FIELD-005 |
| inbound_order_no | VARCHAR(20) | NOT NULL, UNIQUE | 入库单号 | FIELD-005, RULE-005 |
| company_cd | VARCHAR(20) | NOT NULL | 公司代码 | FIELD-005 |
| product_cd | VARCHAR(20) | NOT NULL | 商品代码 | FIELD-005 |
| quantity | INTEGER | NOT NULL | 入库数量 | FIELD-005 |
| status | VARCHAR(10) | NOT NULL | 状态（RECEIVED/CONFIRMED/REJECTED） | FIELD-005 |
| 审计字段 | — | — | 8 个审计字段 | FIELD-003 |

**拣货单表（`s102_picking_order`）**

| 字段 | 类型 | 约束 | 说明 | 关联需求 ID |
| --- | --- | --- | --- | --- |
| picking_order_id | BIGINT | PK, 自增 | 主键 | FIELD-006 |
| picking_order_no | VARCHAR(20) | NOT NULL, UNIQUE | 拣货单号 | FIELD-006, RULE-010 |
| outbound_order_no | VARCHAR(20) | NOT NULL | 出库单号 | FIELD-006, RULE-006 |
| product_cd | VARCHAR(20) | NOT NULL | 商品代码 | FIELD-006 |
| quantity | INTEGER | NOT NULL | 指示数量 | FIELD-006 |
| picked_quantity | INTEGER | DEFAULT 0 | 实拣数量 | FIELD-006, RULE-008 |
| status | VARCHAR(10) | NOT NULL | 状态（PENDING/PICKING/COMPLETED/CANCELLED） | FIELD-006 |
| 审计字段 | — | — | 8 个审计字段 | FIELD-003 |

**出库单表（`s103_outbound_order`）**

| 字段 | 类型 | 约束 | 说明 | 关联需求 ID |
| --- | --- | --- | --- | --- |
| outbound_order_id | BIGINT | PK, 自增 | 主键 | FIELD-007 |
| outbound_order_no | VARCHAR(20) | NOT NULL, UNIQUE | 出库单号 | FIELD-007, RULE-015 |
| company_cd | VARCHAR(20) | NOT NULL | 公司代码 | FIELD-007 |
| product_cd | VARCHAR(20) | NOT NULL | 商品代码 | FIELD-007 |
| quantity | INTEGER | NOT NULL | 出库数量 | FIELD-007 |
| status | VARCHAR(10) | NOT NULL | 状态（CREATED/PICKED/SHIPPED/CANCELLED） | FIELD-007 |
| 审计字段 | — | — | 8 个审计字段 | FIELD-003 |

**库存表（`a102_inventory`）**

| 字段 | 类型 | 约束 | 说明 | 关联需求 ID |
| --- | --- | --- | --- | --- |
| inventory_id | BIGINT | PK, 自增 | 主键 | FIELD-008 |
| company_cd | VARCHAR(20) | NOT NULL | 公司代码（联合唯一键） | FIELD-008 |
| product_cd | VARCHAR(20) | NOT NULL | 商品代码（联合唯一键） | FIELD-008 |
| quantity | INTEGER | NOT NULL | 实际库存数量 | FIELD-008, RULE-016 |
| reserved_quantity | INTEGER | DEFAULT 0 | 预占数量 | FIELD-008, RULE-017 |
| 审计字段 | — | — | 8 个审计字段 | FIELD-003 |

**仓库调拨单表（`s105_transfer_order`）**

| 字段 | 类型 | 约束 | 说明 | 关联需求 ID |
| --- | --- | --- | --- | --- |
| transfer_order_id | BIGINT | PK, 自增 | 主键 | FIELD-010 |
| transfer_order_no | VARCHAR(20) | NOT NULL, UNIQUE | 调拨单号 | FIELD-010, RULE-022 |
| from_company_cd | VARCHAR(20) | NOT NULL | 调出方公司代码 | FIELD-010 |
| to_company_cd | VARCHAR(20) | NOT NULL | 调入方公司代码 | FIELD-010 |
| product_cd | VARCHAR(20) | NOT NULL | 商品代码 | FIELD-010 |
| quantity | INTEGER | NOT NULL | 调拨数量 | FIELD-010 |
| transferred_quantity | INTEGER | DEFAULT 0 | 已调拨数量 | FIELD-010 |
| status | VARCHAR(10) | NOT NULL | 状态（PENDING/APPROVED/SHIPPED/COMPLETED/CANCELLED） | FIELD-010 |
| 审计字段 | — | — | 8 个审计字段 | FIELD-003 |

### 6.3 通用审计字段

所有表必须包含以下 8 个审计字段（FIELD-003）：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| created_ts | TIMESTAMP | 创建时间 |
| created_user_cd | VARCHAR(16) | 创建用户代码 |
| created_program | VARCHAR(50) | 创建程序名 |
| updated_ts | TIMESTAMP | 更新时间 |
| updated_user_cd | VARCHAR(16) | 更新用户代码 |
| updated_program | VARCHAR(50) | 更新程序名 |
| version | INTEGER | 乐观锁版本号 |
| deleted_flag | VARCHAR(1) | 逻辑删除标识（'0'=未删除，'1'=已删除） |

### 6.4 状态口径

| 模块 | 状态值 | 含义 | 关联需求 ID |
| --- | --- | --- | --- |
| 入库单 | RECEIVED | 已接收，待确认 | FLOW-001 |
| 入库单 | CONFIRMED | 已确认入库（终态） | FLOW-001 |
| 入库单 | REJECTED | 已拒绝入库（终态） | FLOW-001 |
| 拣货单 | PENDING | 待拣货 | FLOW-002 |
| 拣货单 | PICKING | 拣货中 | FLOW-002 |
| 拣货单 | COMPLETED | 拣货完毕（终态） | FLOW-002 |
| 拣货单 | CANCELLED | 已取消（终态） | FLOW-002 |
| 出库单 | CREATED | 已创建 | FLOW-003 |
| 出库单 | PICKED | 拣货完成 | FLOW-003 |
| 出库单 | SHIPPED | 已发货（终态） | FLOW-003 |
| 出库单 | CANCELLED | 已取消（终态） | FLOW-003 |
| 调拨单 | PENDING | 待审批 | FLOW-004 |
| 调拨单 | APPROVED | 已审批，待调出 | FLOW-004 |
| 调拨单 | SHIPPED | 已调出，待调入 | FLOW-004 |
| 调拨单 | COMPLETED | 已完成（终态） | FLOW-004 |
| 调拨单 | CANCELLED | 已取消（终态） | FLOW-004 |

## 7. 系统边界与外部依赖

| 系统或依赖 | 关系 | 输入/输出 | 风险 | 关联需求 ID |
| --- | --- | --- | --- | --- |
| PostgreSQL 数据库 | 底层存储 | 所有实体数据的增删改查 | 数据库不可用时系统不可用 | FIELD-001 |
| Company 模块（m101_company） | 关联数据 | 入库单/出库单/库存/调拨单通过 company_cd 关联公司 | 公司代码不一致导致关联失败 | INT-005 |
| 认证系统（未来） | 外部依赖 | created_user_cd/updated_user_cd 由认证系统提供 | 本次使用默认值（mock_user），后续需对接 | — |

## 8. 非功能要求

| 类型 | 要求 | 验收提示 | 关联需求 ID |
| --- | --- | --- | --- |
| 技术栈 | Spring Boot 3.4.4 + Java 25 | `mvn compile` 通过 | NFR-001 |
| 依赖注入 | Controller/Service 统一使用构造器注入，禁止 `@Autowired` 字段注入 | 代码中无 `@Autowired` 字段注入 | NFR-002 |
| 数据转换 | Entity ↔ DTO 统一使用 `BeanUtils.copyProperties` | Service 层中使用 BeanUtils | NFR-003 |
| 数据访问 | Repository 统一继承 `JpaRepository<Entity, Long>` | 所有 Repository 接口继承正确 | NFR-004 |
| 查询安全 | 复杂查询使用 JPQL `@Query`，禁止原生 SQL 拼接 | 无 SQL 拼接代码 | NFR-005 |
| 测试方式 | 统一使用 `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate` | 测试类使用正确注解 | NFR-006 |
| 代码覆盖率 | JaCoCo 指令覆盖率 ≥ 100%，分支覆盖率 ≥ 100% | `mvn test` 通过，含覆盖率检查 | NFR-007 |
| 变异测试 | Pitest 变异覆盖率 ≥ 100%，覆盖率阈值 ≥ 100% | `mvn pitest:mutationCoverage` 通过 | NFR-007 |
| 并发控制 | 使用 version 字段实现乐观锁，冲突时返回 HTTP 409 | Entity 含 version 字段 | NFR-008 |
| 逻辑删除 | 使用 deleted_flag 实现逻辑删除，不物理删除记录 | 删除操作设置 deleted_flag='1' | NFR-009 |
| 代码格式 | 单行不超过 120 字符 | 代码格式化检查通过 | NFR-010 |

## 9. 验收标准

| 验收项 | 通过标准 | 关联需求 ID |
| --- | --- | --- |
| 商品主数据 CRUD | 可创建、查询、更新、删除商品；重复商品代码创建失败 | FUNC-016, FIELD-004, FIELD-009, RULE-020 |
| 入库单创建 | POST 返回 201，入库单状态为 RECEIVED，重复单号创建失败 | FUNC-001, RULE-001, RULE-005 |
| 入库单查询 | 按单号查询返回 200（存在）或 404（不存在） | FUNC-002 |
| 入库确认 | PUT 后入库单状态=CONFIRMED，对应库存 quantity 增加 | FUNC-003, RULE-002, INT-001 |
| 入库拒绝 | PUT 后入库单状态=REJECTED，库存 quantity 不变 | FUNC-004, RULE-003 |
| 入库单终态不可操作 | 对 CONFIRMED/REJECTED 状态执行 confirm/reject 返回错误 | RULE-004, FLOW-001 |
| 出库单创建 | POST 返回 201，状态=CREATED，reserved_quantity 增加；库存不足时创建失败 | FUNC-009, RULE-011, RULE-015, RULE-017 |
| 出库单查询 | 按单号查询返回 200（存在）或 404（不存在） | FUNC-010 |
| 出库确认（发货） | PUT 后出库单状态=SHIPPED，库存 quantity 扣减，reserved_quantity 减少 | FUNC-011, RULE-012, INT-002 |
| 出库取消 | PUT 后出库单状态=CANCELLED，reserved_quantity 恢复；SHIPPED 状态不可取消 | FUNC-012, RULE-013, RULE-014, FLOW-003 |
| 拣货单创建 | POST 返回 201，状态=PENDING，关联出库单有效；重复单号创建失败 | FUNC-005, RULE-006, RULE-010 |
| 拣货单查询 | 按单号查询返回 200（存在）或 404（不存在） | FUNC-006 |
| 执行拣货 | PUT 后 picked_quantity 累加，状态变更为 PICKING；库存不足时拒绝 | FUNC-007, RULE-007, RULE-008 |
| 完成拣货 | picked_quantity ≥ quantity 时 PUT 后状态=COMPLETED；不足时被拒绝；拣货完成后出库单状态变更为 PICKED | FUNC-008, RULE-009, FLOW-002, Q-003 |
| 拣货单状态流 | 仅允许 PENDING → PICKING → COMPLETED 和 PENDING/PICKING → CANCELLED；跳跃或回退返回错误 | FLOW-002 |
| 库存列表查询 | 分页返回库存列表，支持公司代码和商品代码筛选 | FUNC-013, Q-005 |
| 库存详情查询 | 按公司代码+商品代码查询返回 200（存在）或 404（不存在） | FUNC-014 |
| 库存调整 | PUT 后库存数量变更，返回调整原因、调整前后数量；调整后数量为负时失败 | FUNC-015, RULE-019, Q-002 |
| 库存不可为负 | 任何操作（入库、出库、调整、调拨）不得使 Inventory.quantity < 0 | RULE-016 |
| 可用库存计算 | 出库/拣货/调拨校验基于 (quantity - reserved_quantity) | RULE-018 |
| 端到端流程 | 入库→库存增加→创建出库（预占）→拣货→完成拣货→出库确认→库存扣减，全流程执行成功 | INT-004 |
| 公司代码关联一致性 | 入库单/出库单/库存/调拨单的 company_cd 与 Company 模块一致 | INT-005 |
| 调拨单创建 | POST 返回 201，状态=PENDING；调出方=调入方时创建失败；调出方库存不足时创建失败；重复单号创建失败 | FUNC-017, RULE-021, RULE-022, RULE-023, RULE-024 |
| 调拨单查询 | 按单号查询返回 200（存在）或 404（不存在） | FUNC-018 |
| 调拨审批 | PUT 后调拨单状态=APPROVED，调出方 reserved_quantity 增加；非 PENDING 状态审批失败 | FUNC-019, RULE-026, INT-007 |
| 调拨出库 | PUT 后调拨单状态=SHIPPED，调出方 quantity 扣减，reserved_quantity 减少；非 APPROVED 状态出库失败 | FUNC-020, RULE-026, INT-006 |
| 调拨入库 | PUT 后调拨单状态=COMPLETED，调入方 quantity 增加；非 SHIPPED 状态入库失败 | FUNC-021, RULE-026, INT-006 |
| 调拨取消 | PUT 后调拨单状态=CANCELLED，调出方 reserved_quantity 恢复；SHIPPED/COMPLETED 状态不可取消 | FUNC-022, RULE-025, INT-007 |
| 调拨单状态流 | 仅允许 PENDING → APPROVED → SHIPPED → COMPLETED 和 PENDING/APPROVED → CANCELLED；跳跃或回退返回错误 | FLOW-004 |
| 调拨端到端流程 | 创建调拨→审批→调出（调出方库存扣减）→调入（调入方库存增加）→完成，全流程执行成功 | INT-006, INT-007 |
| 审计字段完整性 | 所有实体包含 8 个审计字段，创建/更新时正确填充 | FIELD-003 |
| 乐观锁并发控制 | 并发更新时 version 冲突返回 HTTP 409 | NFR-008 |
| 逻辑删除 | 删除操作设置 deleted_flag='1'，查询自动过滤已删除记录 | NFR-009 |
| 测试覆盖率 | JaCoCo 指令/分支覆盖率 100%，Pitest 变异覆盖率 100% | BR-002, NFR-007 |

## 10. 待确认问题

| 问题 | 关联需求 ID | 是否阻断后续阶段 | 下一步动作 |
| --- | --- | --- | --- |
| 商品主数据是否需要商品分类、规格、重量、尺寸等扩展字段？ | Q-001, FIELD-004, FUNC-016 | 否 | 本次按最小字段集实现，后续可扩展 |
| 库存调整是否需要独立的调整日志表？ | Q-002, RULE-019, FUNC-015 | 否 | 本次最小实现：调整原因在请求体中携带 |
| 拣货完成后出库单状态是否自动变更为 PICKED？ | Q-003, FLOW-002, FLOW-003 | 否 | 默认假设：自动更新 |
| 库存预占时机：创建出库单时还是拣货开始时？ | Q-004, RULE-011, RULE-017 | 否 | 默认假设：创建出库单时立即预占 |
| 库存列表查询需要支持哪些筛选条件？ | Q-005, FUNC-013 | 否 | 默认假设：公司代码 + 商品代码 + 分页 |
| 拣货单是否需要 company_cd 字段？ | Q-006, FIELD-006 | 否 | 默认假设：通过 outbound_order_no 间接关联 |
| 调拨是否需要审批环节？ | Q-007, FLOW-004, RULE-026 | 否 | 默认假设：包含审批环节（PENDING → APPROVED → SHIPPED → COMPLETED） |
| 调拨出库是否需要走拣货流程？ | Q-008, FIELD-010, FUNC-020 | 否 | 默认假设：调拨出库直接扣减库存，不经过拣货流程 |

---

*最后更新：2026-07-14*
