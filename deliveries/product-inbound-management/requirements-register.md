# 商品入库/拣货/出库/管理机能 源需求登记

## 输入来源

| 来源 | 位置 | 说明 |
| --- | --- | --- |
| 用户直接输入 | 对话消息 | "商品入库/拣货/出库/管理机能，业务表以s开头，master表以a开头，数据库是postgre" |
| 用户追加输入 | 对话消息 | "修改需求文档，包括多仓库调拨" |
| 项目入口文档 | `.gientech/AGENTS.md` | 仓储管理核心机能开发指南，含5大机能（商品主数据、入库、拣货、出库、调拨、管理）实体设计、API端点、业务规则 |
| 编码指引 | `CODING_GUIDELINES.md` | 项目编码规范（命名、格式、注释、架构、测试、Git、安全、性能） |
| 现有代码样本 | `src/main/java/com/example/demo/entity/Company.java` 等 | Company CRUD 作为代码风格基线 |
| 已有需求登记 | `requirements-register.md` | 拣货机能历史需求登记（REQ-001 ~ REQ-012） |
| Maven 配置 | `pom.xml` | Spring Boot 3.4.4 / Java 25 / JaCoCo 100% / Pitest 100% |

## 视觉证据登记

| image_id | 图片路径或附件名 | 区域位置 | 视觉观察 | 提取出的需求 ID | 置信度 | 未解决问题 |
| --- | --- | --- | --- | --- | --- | --- |
| （无） | （无） | （无） | 本次输入为纯文本，无图片/截图/设计稿 | — | — | — |

## 需求总览

- **业务目标**：构建仓储管理系统的核心机能，覆盖商品入库登记→库存管理→拣货作业→出库确认→仓库间调拨的完整物流链路。
- **成功标准**：5大机能模块（入库、拣货、出库、库存管理、仓库调拨）各含完整的 CRUD + 状态流转 + 业务规则校验；JaCoCo 指令/分支覆盖率 100%；Pitest 变异覆盖率 100%。
- **范围内**：入库单创建/确认/拒绝、拣货单创建/执行/完成、出库单创建/发货/取消、库存查询/调整、商品主数据管理、仓库间调拨（创建/审批/出库/入库/完成）、数据库表设计（PostgreSQL，业务表 `s` 前缀，主数据表 `a` 前缀）。
- **范围外**：用户认证与授权、前端界面、消息通知、WMS 硬件集成（条码枪/AGV）、批次/序列号追踪（本次不强制）、报表导出。

## 源需求登记

### 一、业务目标与背景

| 需求 ID | 类型 | 来源位置 | 原文摘录或视觉观察 | 结构化表述 | 优先级 | 验收提示 | 当前状态 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| BR-001 | 业务背景 | AGENTS.md §1 | "实现仓储管理的核心机能：商品主数据/入库/拣货/出库/调拨/管理" | 系统需实现入库登记→库存增减→拣货执行→出库确认→仓库调拨的完整仓储作业流程 | 高 | 5大模块各含可工作的 API 端点 | 已登记 |
| BR-002 | 成功标准 | pom.xml | JaCoCo instruction/branch minimum=1.0; Pitest mutation/coverage threshold=100 | 全项目测试指令覆盖率 ≥ 100%、分支覆盖率 ≥ 100%、变异覆盖率 ≥ 100% | 高 | `mvn test` 和 `mvn pitest:mutationCoverage` 均通过 | 已登记 |

### 二、数据库与数据模型

| 需求 ID | 类型 | 来源位置 | 原文摘录或视觉观察 | 结构化表述 | 优先级 | 验收提示 | 当前状态 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| FIELD-001 | 数据库 | 用户输入 | "数据库是postgre" | 项目使用 PostgreSQL 作为关系数据库 | 高 | `application.properties` 中 datasource 指向 PostgreSQL | 已登记 |
| FIELD-002 | 数据规则 | 用户输入 | "业务表以s开头，master表以a开头" | Schema 命名：主数据表 `scash.a<序号>_<表名>`（如 `a101_product`），业务交易表 `scash.s<序号>_<表名>`（如 `s101_inbound_order`） | 高 | Entity `@Table` 注解中 schema=scash，表名符合 s/a 前缀规则 | 已登记 |
| FIELD-003 | 数据规则 | AGENTS.md §7.2 | "必须包含审计字段: created_ts, created_user_cd, updated_ts, updated_user_cd, version, deleted_flag" | 所有实体表必须包含 8 个审计字段（created_ts, created_user_cd, created_program, updated_ts, updated_user_cd, updated_program, version, deleted_flag） | 高 | 每个 Entity 类含对应字段和 `@Column` 映射 | 已登记 |
| FIELD-004 | 字段 | AGENTS.md §4.0~4.4 | 商品/入库/拣货/出库/库存实体示例 | 商品主数据表（`a101_product`）：product_id, product_cd (UNIQUE), product_nm_kanji, product_nm_kana, unit_cd, created/updated 审计字段 | 高 | Product Entity 存在且字段齐全 | 已登记 |
| FIELD-005 | 字段 | AGENTS.md §4.1 | InboundOrder 实体示例 | 入库单表（`s101_inbound_order`）：inbound_order_id, inbound_order_no (UNIQUE), company_cd, product_cd, quantity, status, 审计字段 | 高 | InboundOrder Entity 存在 | 已登记 |
| FIELD-006 | 字段 | AGENTS.md §4.2 | PickingOrder 实体示例 | 拣货单表（`s102_picking_order`）：picking_order_id, picking_order_no (UNIQUE), outbound_order_no, product_cd, quantity, picked_quantity, status, 审计字段 | 高 | PickingOrder Entity 存在 | 已登记 |
| FIELD-007 | 字段 | AGENTS.md §4.3 | OutboundOrder 实体示例 | 出库单表（`s103_outbound_order`）：outbound_order_id, outbound_order_no (UNIQUE), company_cd, product_cd, quantity, status, 审计字段 | 高 | OutboundOrder Entity 存在 | 已登记 |
| FIELD-008 | 字段 | AGENTS.md §4.4 | Inventory 实体示例 | 库存表（`a102_inventory`）：inventory_id, company_cd, product_cd, quantity, reserved_quantity, 审计字段；联合唯一键 (company_cd, product_cd) | 高 | Inventory Entity 存在 | 已登记 |
| FIELD-010 | 字段 | AGENTS.md §4.5 | TransferOrder 实体示例 | 仓库调拨单表（`s105_transfer_order`）：transfer_order_id, transfer_order_no (UNIQUE), from_company_cd, to_company_cd, product_cd, quantity, transferred_quantity, status, 审计字段 | 高 | TransferOrder Entity 存在 | 已登记 |

### 三、入库机能

| 需求 ID | 类型 | 来源位置 | 原文摘录或视觉观察 | 结构化表述 | 优先级 | 验收提示 | 当前状态 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| FUNC-001 | 功能 | AGENTS.md §4.1 | "POST /api/inbound-orders — 创建入库单" | 提供 API 创建入库单，接收入库单号、公司代码、商品代码、数量 | 高 | POST 返回 201 + 入库单 DTO | 已登记 |
| FUNC-002 | 功能 | AGENTS.md §4.1 | "GET /api/inbound-orders/{inboundOrderNo} — 查询入库单" | 按入库单号查询入库单详情 | 高 | GET 返回 200（存在）或 404（不存在） | 已登记 |
| FUNC-003 | 功能 | AGENTS.md §4.1 | "PUT /api/inbound-orders/{inboundOrderNo}/confirm — 确认入库" | 确认入库单，状态变更为 CONFIRMED，同步增加对应商品库存 | 高 | PUT 后入库单状态=CONFIRMED，库存数量增加 | 已登记 |
| FUNC-004 | 功能 | AGENTS.md §4.1 | "PUT /api/inbound-orders/{inboundOrderNo}/reject — 拒绝入库" | 拒绝入库单，状态变更为 REJECTED，不增加库存 | 高 | PUT 后入库单状态=REJECTED，库存不变 | 已登记 |
| RULE-001 | 业务规则 | AGENTS.md §4.1 | "创建入库单时，状态初始为 RECEIVED" | 新建入库单默认状态为 RECEIVED | 高 | 创建后 status = "RECEIVED" | 已登记 |
| RULE-002 | 业务规则 | AGENTS.md §4.1 | "确认入库时，同步增加对应商品的库存数量" | 确认入库操作成功后，Inventory.quantity += InboundOrder.quantity | 高 | 库存表对应记录 quantity 正确增加 | 已登记 |
| RULE-003 | 业务规则 | AGENTS.md §4.1 | "拒绝入库时，状态变更为 REJECTED，不增加库存" | 拒绝入库仅变更状态，不触发库存变更 | 高 | 库存表对应记录 quantity 不变 | 已登记 |
| RULE-004 | 业务规则 | AGENTS.md §4.1 | "已确认或已拒绝的入库单不可再次操作" | CONFIRMED / REJECTED 状态的入库单无法再次确认或拒绝 | 高 | 对已操作入库单执行 confirm/reject 返回错误 | 已登记 |
| FLOW-001 | 流程 | AGENTS.md §4.1 | 状态值: RECEIVED → CONFIRMED / REJECTED | 入库单状态流：RECEIVED → CONFIRMED（确认）或 RECEIVED → REJECTED（拒绝）；终态不可再变更 | 高 | 状态变更仅允许合法路径 | 已登记 |
| RULE-005 | 数据规则 | AGENTS.md §7.2 | "inbound_order_no 设置 unique 约束" | 入库单号全局唯一 | 高 | 重复单号创建失败 | 已登记 |

### 四、拣货机能

| 需求 ID | 类型 | 来源位置 | 原文摘录或视觉观察 | 结构化表述 | 优先级 | 验收提示 | 当前状态 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| FUNC-005 | 功能 | AGENTS.md §4.2 | "POST /api/picking-orders — 创建拣货单" | 提供 API 创建拣货单，接收拣货单号、出库单号、商品代码、数量 | 高 | POST 返回 201 + 拣货单 DTO | 已登记 |
| FUNC-006 | 功能 | AGENTS.md §4.2 | "GET /api/picking-orders/{pickingOrderNo} — 查询拣货单" | 按拣货单号查询拣货单详情 | 高 | GET 返回 200（存在）或 404（不存在） | 已登记 |
| FUNC-007 | 功能 | AGENTS.md §4.2 | "PUT /api/picking-orders/{pickingOrderNo}/pick — 执行拣货" | 执行拣货操作，累加 picked_quantity | 高 | PUT 后 picked_quantity 累加，状态=PICKING | 已登记 |
| FUNC-008 | 功能 | AGENTS.md §4.2 | "PUT /api/picking-orders/{pickingOrderNo}/complete — 完成拣货" | 完成拣货，状态变更为 COMPLETED | 高 | PUT 后状态=COMPLETED | 已登记 |
| RULE-006 | 业务规则 | AGENTS.md §4.2 | "拣货单关联出库单，由出库单驱动生成" | 拣货单必须关联有效的出库单号 | 高 | 创建时 outbound_order_no 不可为空 | 已登记 |
| RULE-007 | 业务规则 | AGENTS.md §4.2 | "拣货时校验库存是否充足" | 执行拣货前校验可用库存（quantity - reserved_quantity）≥ 拣货数量 | 高 | 库存不足时拒绝拣货 | 已登记 |
| RULE-008 | 业务规则 | AGENTS.md §4.2 | "picked_quantity 累加记录，支持分批拣货" | 每次执行拣货，picked_quantity 在原值基础上累加 | 高 | 多次 pick 后 picked_quantity 为各次之和 | 已登记 |
| RULE-009 | 业务规则 | AGENTS.md §4.2 | "拣货完成后 (picked_quantity >= quantity)，状态变更为 COMPLETED" | picked_quantity ≥ quantity 时允许完成拣货，状态变更为 COMPLETED | 高 | picked 未达 quantity 时 complete 被拒绝 | 已登记 |
| FLOW-002 | 流程 | AGENTS.md §4.2 | 状态值: PENDING → PICKING → COMPLETED / CANCELLED | 拣货单状态流：PENDING → PICKING（首次拣货）→ COMPLETED（完成）；PENDING/PICKING → CANCELLED（取消） | 高 | 状态变更仅允许合法路径 | 已登记 |
| RULE-010 | 数据规则 | AGENTS.md §7.2 | "picking_order_no 设置 unique 约束" | 拣货单号全局唯一 | 高 | 重复单号创建失败 | 已登记 |

### 五、出库机能

| 需求 ID | 类型 | 来源位置 | 原文摘录或视觉观察 | 结构化表述 | 优先级 | 验收提示 | 当前状态 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| FUNC-009 | 功能 | AGENTS.md §4.3 | "POST /api/outbound-orders — 创建出库单" | 提供 API 创建出库单，接收出库单号、公司代码、商品代码、数量 | 高 | POST 返回 201 + 出库单 DTO | 已登记 |
| FUNC-010 | 功能 | AGENTS.md §4.3 | "GET /api/outbound-orders/{outboundOrderNo} — 查询出库单" | 按出库单号查询出库单详情 | 高 | GET 返回 200（存在）或 404（不存在） | 已登记 |
| FUNC-011 | 功能 | AGENTS.md §4.3 | "PUT /api/outbound-orders/{outboundOrderNo}/ship — 确认出库" | 确认出库，状态变更为 SHIPPED，同步扣减库存 | 高 | PUT 后状态=SHIPPED，库存扣减 | 已登记 |
| FUNC-012 | 功能 | AGENTS.md §4.3 | "PUT /api/outbound-orders/{outboundOrderNo}/cancel — 取消出库" | 取消出库单，状态变更为 CANCELLED，恢复库存预占 | 高 | PUT 后状态=CANCELLED | 已登记 |
| RULE-011 | 业务规则 | AGENTS.md §4.3 | "创建出库单时校验库存充足性" | 创建出库单时校验可用库存 ≥ 出库数量 | 高 | 库存不足时拒绝创建 | 已登记 |
| RULE-012 | 业务规则 | AGENTS.md §4.3 | "出库确认时，同步扣减对应商品的库存数量" | SHIP 操作成功后，Inventory.quantity -= OutboundOrder.quantity | 高 | 库存表对应记录 quantity 正确扣减 | 已登记 |
| RULE-013 | 业务规则 | AGENTS.md §4.3 | "已出库的订单不可取消" | SHIPPED 状态的出库单无法取消 | 高 | 对 SHIPPED 订单执行 cancel 返回错误 | 已登记 |
| RULE-014 | 业务规则 | AGENTS.md §4.3 | "取消出库时恢复库存预占" | CANCELLED 操作恢复 reserved_quantity | 高 | 取消后 reserved_quantity 减少 | 已登记 |
| FLOW-003 | 流程 | AGENTS.md §4.3 | 状态值: CREATED → PICKED → SHIPPED / CANCELLED | 出库单状态流：CREATED → PICKED（拣货完成）→ SHIPPED（发货）；CREATED/PICKED → CANCELLED（取消） | 高 | 状态变更仅允许合法路径 | 已登记 |
| RULE-015 | 数据规则 | AGENTS.md §7.2 | "outbound_order_no 设置 unique 约束" | 出库单号全局唯一 | 高 | 重复单号创建失败 | 已登记 |

### 六、库存管理机能

| 需求 ID | 类型 | 来源位置 | 原文摘录或视觉观察 | 结构化表述 | 优先级 | 验收提示 | 当前状态 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| FUNC-013 | 功能 | AGENTS.md §4.4 | "GET /api/inventories — 查询库存列表" | 提供分页查询库存列表的 API | 高 | GET 返回分页库存列表 | 已登记 |
| FUNC-014 | 功能 | AGENTS.md §4.4 | "GET /api/inventories/{companyCd}/{productCd} — 查询指定商品库存" | 按公司代码+商品代码查询库存详情 | 高 | GET 返回 200（存在）或 404（不存在） | 已登记 |
| FUNC-015 | 功能 | AGENTS.md §4.4 | "PUT /api/inventories/{companyCd}/{productCd}/adjust — 库存调整" | 提供库存调整 API，支持手动修正库存数量 | 高 | PUT 后库存数量变更 | 已登记 |
| RULE-016 | 业务规则 | AGENTS.md §4.4 | "库存数量 = 实际库存，不可为负数" | Inventory.quantity 始终 ≥ 0 | 高 | 任何操作不得使 quantity < 0 | 已登记 |
| RULE-017 | 业务规则 | AGENTS.md §4.4+§4.5 | "reserved_quantity 记录被出库单/调拨单预占的数量" | 创建出库单/审批调拨单时增加 reserved_quantity；取消/发货/调拨完成后减少 | 高 | 出库创建/调拨审批后 reserved_quantity 增加 | 已登记 |
| RULE-018 | 业务规则 | AGENTS.md §4.4 | "可用库存 = quantity - reserved_quantity" | 所有库存充足性校验使用 (quantity - reserved_quantity) | 高 | 拣货/出库/调拨校验基于可用库存 | 已登记 |
| RULE-019 | 业务规则 | AGENTS.md §4.4 | "库存调整需记录调整原因和调整前后的数量" | 库存调整操作需记录调整原因（reason）、调整前数量（before_qty）、调整后数量（after_qty） | 中 | 调整 API 接收 reason 参数 | 已登记 |

### 七、商品主数据

| 需求 ID | 类型 | 来源位置 | 原文摘录或视觉观察 | 结构化表述 | 优先级 | 验收提示 | 当前状态 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| FUNC-016 | 功能 | AGENTS.md §4.0 | "商品主数据管理机能 (Product)" | 提供商品主数据 CRUD（创建、查询、更新、删除） | 高 | Product Entity + CRUD API | 已登记 |
| FIELD-009 | 字段 | AGENTS.md §4.0 + FIELD-004 | 商品主数据表设计 | 商品表（`a101_product`）字段：product_id (PK), product_cd (UNIQUE, NOT NULL, VARCHAR(20)), product_nm_kanji (NOT NULL, VARCHAR(40)), product_nm_kana (VARCHAR(40)), unit_cd (VARCHAR(10)), 审计字段 | 高 | Product Entity 字段齐全 | 已登记 |
| RULE-020 | 数据规则 | AGENTS.md §4.0 | "product_cd 设置 unique 约束" | 商品代码全局唯一 | 高 | 重复商品代码创建失败 | 已登记 |

### 八、仓库调拨机能

| 需求 ID | 类型 | 来源位置 | 原文摘录或视觉观察 | 结构化表述 | 优先级 | 验收提示 | 当前状态 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| FUNC-017 | 功能 | AGENTS.md §4.5 | "POST /api/transfer-orders — 创建调拨单" | 提供 API 创建调拨单，接收调拨单号、调出公司代码、调入公司代码、商品代码、数量 | 高 | POST 返回 201 + 调拨单 DTO | 已登记 |
| FUNC-018 | 功能 | AGENTS.md §4.5 | "GET /api/transfer-orders/{transferOrderNo} — 查询调拨单" | 按调拨单号查询调拨单详情 | 高 | GET 返回 200（存在）或 404（不存在） | 已登记 |
| FUNC-019 | 功能 | AGENTS.md §4.5 | "PUT /api/transfer-orders/{transferOrderNo}/approve — 审批调拨单" | 审批调拨单，状态变更为 APPROVED，调出方库存预占 | 高 | PUT 后状态=APPROVED，调出方 reserved_quantity 增加 | 已登记 |
| FUNC-020 | 功能 | AGENTS.md §4.5 | "PUT /api/transfer-orders/{transferOrderNo}/ship — 确认调拨出库" | 确认调拨出库，状态变更为 SHIPPED，调出方库存扣减 | 高 | PUT 后状态=SHIPPED，调出方 quantity 扣减 | 已登记 |
| FUNC-021 | 功能 | AGENTS.md §4.5 | "PUT /api/transfer-orders/{transferOrderNo}/receive — 确认调拨入库" | 确认调拨入库，状态变更为 COMPLETED，调入方库存增加 | 高 | PUT 后状态=COMPLETED，调入方 quantity 增加 | 已登记 |
| FUNC-022 | 功能 | AGENTS.md §4.5 | "PUT /api/transfer-orders/{transferOrderNo}/cancel — 取消调拨单" | 取消调拨单，状态变更为 CANCELLED，恢复调出方库存预占 | 高 | PUT 后状态=CANCELLED | 已登记 |
| RULE-021 | 业务规则 | AGENTS.md §4.5 | "初始状态为 PENDING" | 新建调拨单默认状态为 PENDING | 高 | 创建后 status = "PENDING" | 已登记 |
| RULE-022 | 业务规则 | AGENTS.md §4.5 | "transfer_order_no 设置 unique 约束" | 调拨单号全局唯一 | 高 | 重复单号创建失败 | 已登记 |
| RULE-023 | 业务规则 | AGENTS.md §4.5 | "创建时校验调拨单号唯一性、调出方≠调入方、调出方可用库存充足" | 创建调拨单时校验调出方可用库存 ≥ 调拨数量 | 高 | 调出方库存不足时拒绝创建 | 已登记 |
| RULE-024 | 业务规则 | AGENTS.md §4.5 | "创建时校验调拨单号唯一性、调出方≠调入方、调出方可用库存充足" | 调出方和调入方不能为同一公司 | 高 | from_company_cd = to_company_cd 时创建失败 | 已登记 |
| RULE-025 | 业务规则 | AGENTS.md §4.5 | "SHIPPED / COMPLETED 状态的调拨单不可取消" | 已发货（SHIPPED）或已完成（COMPLETED）的调拨单不可取消 | 高 | 对 SHIPPED/COMPLETED 调拨单执行 cancel 返回错误 | 已登记 |
| RULE-026 | 业务规则 | AGENTS.md §4.5 | "仅 PENDING 可审批，仅 APPROVED 可出库，仅 SHIPPED 可入库" | 仅 PENDING 状态的调拨单可审批；仅 APPROVED 状态的调拨单可出库；仅 SHIPPED 状态的调拨单可入库 | 高 | 状态变更仅允许合法路径 | 已登记 |
| FLOW-004 | 流程 | AGENTS.md §4.5 | 状态值: PENDING → APPROVED → SHIPPED → COMPLETED / CANCELLED | 调拨单状态流：PENDING → APPROVED（审批）→ SHIPPED（调出）→ COMPLETED（调入）；PENDING/APPROVED → CANCELLED（取消） | 高 | 状态变更仅允许合法路径 | 已登记 |
| INT-006 | 集成 | AGENTS.md §4.5 | "调拨出库扣减调出方库存，调拨入库增加调入方库存" | 调拨出库需扣减调出方库存，调拨入库需增加调入方库存，涉及两方库存变更 | 高 | 调拨完成后调出方库存减少、调入方库存增加 | 已登记 |
| INT-007 | 集成 | AGENTS.md §4.5 | "审批时调出方 reserved_quantity 增加，取消时恢复" | 调拨审批时需预占调出方库存（reserved_quantity 增加），取消时恢复预占 | 高 | 审批后 reserved_quantity 增加，取消后恢复 | 已登记 |

### 九、架构与非功能需求

| 需求 ID | 类型 | 来源位置 | 原文摘录或视觉观察 | 结构化表述 | 优先级 | 验收提示 | 当前状态 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| NFR-001 | 非功能 | pom.xml | spring-boot-starter-parent 3.4.4, java.version=25 | 项目基于 Spring Boot 3.4.4 + Java 25 构建 | 高 | `mvn compile` 通过 | 已登记 |
| NFR-002 | 非功能 | AGENTS.md §5.1 | "使用构造器注入 (禁止 @Autowired 字段注入)" | Controller/Service 统一使用构造器注入 | 高 | 代码中无 `@Autowired` 字段注入 | 已登记 |
| NFR-003 | 非功能 | AGENTS.md §5.2 | "Entity 与 DTO 之间使用 BeanUtils.copyProperties 转换" | Entity ↔ DTO 转换统一使用 BeanUtils.copyProperties | 中 | Service 层中使用 BeanUtils | 已登记 |
| NFR-004 | 非功能 | AGENTS.md §5.3 | "继承 JpaRepository<Entity, Long>" | Repository 统一继承 JpaRepository<Entity, Long> | 高 | 所有 Repository 接口继承正确 | 已登记 |
| NFR-005 | 非功能 | AGENTS.md §5.3 | "复杂查询使用 @Query (JPQL，禁止原生 SQL 拼接)" | 复杂查询使用 JPQL @Query，禁止原生 SQL 拼接 | 高 | 无 SQL 拼接代码 | 已登记 |
| NFR-006 | 非功能 | AGENTS.md §6.2 | "使用 @SpringBootTest(webEnvironment = RANDOM_PORT) + TestRestTemplate" | 测试统一使用集成测试模式 | 高 | 测试类使用正确注解 | 已登记 |
| NFR-007 | 非功能 | AGENTS.md §6.3 | JaCoCo 指令/分支 100%, Pitest 变异/覆盖率 100% | 全量测试覆盖率和变异测试覆盖率均为 100% | 高 | `mvn test` 和 `mvn pitest:mutationCoverage` 均通过 | 已登记 |
| NFR-008 | 非功能 | AGENTS.md §5.4 | "version 字段" | 使用乐观锁（version 字段）防止并发更新冲突 | 中 | Entity 含 @Version 或手动 version 校验 | 已登记 |
| NFR-009 | 非功能 | AGENTS.md §5.4 | "deleted_flag 字段" | 使用逻辑删除（deleted_flag）而非物理删除 | 中 | 删除操作设置 deleted_flag='1' | 已登记 |
| NFR-010 | 非功能 | CODING_GUIDELINES.md §2.2 | "单行不超过 120 字符" | 代码行长度 ≤ 120 字符 | 低 | 代码格式化检查通过 | 已登记 |

### 十、跨模块集成

| 需求 ID | 类型 | 来源位置 | 原文摘录或视觉观察 | 结构化表述 | 优先级 | 验收提示 | 当前状态 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| INT-001 | 集成 | AGENTS.md §4.1+§4.4 | "确认入库时，同步增加对应商品的库存数量" | 入库确认需调用库存服务，增加 Inventory.quantity | 高 | 入库确认后库存数量正确增加 | 已登记 |
| INT-002 | 集成 | AGENTS.md §4.3+§4.4 | "出库确认时，同步扣减对应商品的库存数量" | 出库确认需调用库存服务，扣减 Inventory.quantity | 高 | 出库确认后库存数量正确扣减 | 已登记 |
| INT-003 | 集成 | AGENTS.md §4.2+§4.3 | "拣货单关联出库单，由出库单驱动生成" | 拣货单创建时需校验出库单存在且状态合法 | 高 | 无效出库单号无法创建拣货单 | 已登记 |
| INT-004 | 集成 | AGENTS.md §9 | "入库→库存→拣货→出库→调拨 全流程验证" | 需支持完整链路：入库→库存增加→创建出库→拣货→出库确认→库存扣减→调拨全流程 | 高 | 端到端集成测试通过 | 已登记 |
| INT-005 | 集成 | AGENTS.md §4.1+§4.5 | "company_cd" | 入库单/出库单/库存/调拨单均关联公司代码，与现有 Company 模块（m101_company）关联 | 中 | 公司代码字段在各实体间一致 | 已登记 |
| INT-006 | 集成 | AGENTS.md §4.5 | "调拨出库扣减调出方库存，调拨入库增加调入方库存" | 调拨出库需扣减调出方库存，调拨入库需增加调入方库存，涉及两方库存变更 | 高 | 调拨完成后调出方库存减少、调入方库存增加 | 已登记 |
| INT-007 | 集成 | AGENTS.md §4.5 | "审批时调出方 reserved_quantity 增加，取消时恢复" | 调拨审批时需预占调出方库存（reserved_quantity 增加），取消时恢复预占 | 高 | 审批后 reserved_quantity 增加，取消后恢复 | 已登记 |

## 澄清问题

| 问题 ID | 关联需求 ID | 问题 | 影响 | 是否阻断后续 PRD | 询问方式 |
| --- | --- | --- | --- | --- | --- |
| Q-001 | FIELD-004, FUNC-016 | 商品主数据表是否需要商品分类、规格、重量、尺寸等扩展字段？ | 影响 Product Entity 字段设计 | 否 | 本次先按最小字段集（product_cd, product_nm_kanji, product_nm_kana, unit_cd）实现，后续可扩展 |
| Q-002 | RULE-019, FUNC-015 | 库存调整是否需要独立的调整日志表（如 `s104_inventory_adjust_log`）来持久化调整历史？ | 影响数据库表数量和调整 API 设计 | 否 | 本次调整原因记录在调整请求中，是否落日志表由 PRD 阶段确认；最小实现可在调整请求体中携带 reason 字段 |
| Q-003 | FLOW-002, FLOW-003 | 拣货完成（COMPLETED）后，出库单状态是否自动从 CREATED 变更为 PICKED？还是需要手动触发？ | 影响拣货与出库模块的耦合方式 | 否 | 默认假设：拣货完成后自动更新关联出库单状态为 PICKED |
| Q-004 | RULE-011, RULE-017 | 创建出库单时，库存预占（reserved_quantity 增加）是在创建出库单时立即执行，还是在拣货开始时执行？ | 影响库存预占时机和可用库存计算 | 否 | 默认假设：创建出库单时立即增加 reserved_quantity |
| Q-005 | FUNC-013 | 库存列表查询需要支持哪些筛选条件？（公司代码、商品代码、库存数量范围、reserved_quantity > 0 等） | 影响查询 API 参数设计 | 否 | 默认假设：支持公司代码和商品代码筛选 + 分页 |
| Q-006 | FIELD-006 | 拣货单是否需要 company_cd 字段？AGENTS.md 示例中拣货单未包含 company_cd，但出库单包含。 | 影响拣货单实体字段 | 否 | 默认假设：拣货单通过 outbound_order_no 间接关联公司，不直接存储 company_cd |
| Q-007 | FLOW-004, RULE-026 | 调拨是否需要审批环节？还是创建后直接执行？ | 影响调拨状态机和 API 设计 | 否 | 默认假设：包含审批环节（PENDING → APPROVED → SHIPPED → COMPLETED） |
| Q-008 | FIELD-010, FUNC-020 | 调拨出库是否需要走拣货流程，还是直接扣减库存？ | 影响调拨与拣货模块的耦合方式 | 否 | 默认假设：调拨出库直接扣减库存，不经过拣货流程 |

## 范围与风险

| 事项 | 关联需求 ID | 说明 | 下一步动作 |
| --- | --- | --- | --- |
| 数据库表前缀变更 | FIELD-002 | 用户明确要求业务表以 `s` 开头、主数据表以 `a` 开头 | PRD 阶段统一使用 s/a 前缀 |
| 拣货机能已有历史登记 | BR-001 | `requirements-register.md` 中已有 REQ-001~REQ-012 拣货相关需求，本次登记覆盖全部 5 大模块 | 以本登记为准，历史登记作为参考 |
| 库存预占时机 | RULE-011, RULE-017, Q-004 | 出库单创建时是否立即预占库存，影响并发场景下的可用库存准确性 | PRD 阶段确认预占时机 |
| 跨模块事务一致性 | INT-001, INT-002, INT-003, INT-006 | 入库确认/出库确认/调拨涉及多表更新，需保证事务一致性 | PRD 阶段明确事务边界 |
| 乐观锁冲突处理 | NFR-008 | version 字段并发冲突时的用户反馈方式未明确 | 默认返回 HTTP 409 Conflict |
| 调拨审批流程 | FLOW-004, Q-007 | 调拨是否需要审批环节，以及审批规则 | 默认假设：包含审批环节 |

---

*最后更新：2026-07-14*
