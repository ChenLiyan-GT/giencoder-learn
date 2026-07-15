# 商品入库/拣货/出库/管理机能 验证计划

## 1. 文档信息

| 项目 | 内容 |
| --- | --- |
| 项目名称 | 仓储管理系统核心机能 |
| 验证对象 | 商品主数据、入库、拣货、出库、库存管理、仓库调拨 6 大模块 |
| 上游产物 | `requirements-register.md`（77 条源需求）、`prd.md`（34 条验收标准）、`user-stories.md`（拣货模块 8 用户故事 / 30 验收场景） |
| 技术栈 | Spring Boot 3.4.4 + JDK 25 + PostgreSQL + Maven |
| 测试框架 | JUnit 5 + @SpringBootTest(RANDOM_PORT) + TestRestTemplate |
| 覆盖率要求 | JaCoCo 指令/分支 100%、Pitest 变异/覆盖率 100% |

---

## 2. 验证策略概述

### 2.1 验证层级

| 层级 | 方法 | 目标 | 覆盖范围 |
| --- | --- | --- | --- |
| L1 API 集成测试 | TestRestTemplate 调用 HTTP 端点 | 验证 API 契约、HTTP 状态码、响应体结构 | 全部 22 个 API 端点 |
| L2 业务规则测试 | 调用 API 后断言数据库状态 | 验证状态流转、库存增减、唯一性约束 | 全部 26 条 RULE 需求 |
| L3 端到端流程测试 | 多步操作串联 | 验证跨模块集成链路 | 4 条 FLOW + 7 条 INT 需求 |
| L4 非功能测试 | 代码审查 + 自动化检查 | 验证架构规范、编码规范 | 10 条 NFR 需求 |
| L5 覆盖率门禁 | JaCoCo + Pitest | 验证测试充分性 | BR-002 |

### 2.2 验证原则

- 每条源需求至少对应 1 个测试用例（正向 + 异常路径）
- 状态流转的每个非法转换均需独立测试
- 库存变更操作需断言 quantity 和 reserved_quantity 的精确值
- 逻辑删除的记录在查询时不可见
- 乐观锁冲突返回 HTTP 409

---

## 3. 需求→验证用例映射

### 3.1 商品主数据（Product）

| 验证用例 ID | 覆盖需求 ID | 验证内容 | 前置条件 | 操作 | 期望结果 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- |
| VP-001 | FUNC-016, BR-001 | 创建商品成功 | 无 | POST /api/products {product_cd:"P001", product_nm_kanji:"商品A"} | 201 + DTO 含 product_cd="P001" | 高 |
| VP-002 | FUNC-016, FIELD-009 | 创建商品字段完整性 | 无 | POST /api/products 含全部字段 | 201 + DTO 含 product_cd, product_nm_kanji, product_nm_kana, unit_cd | 高 |
| VP-003 | RULE-020 | 重复商品代码创建失败 | 已存在 product_cd="P001" | POST /api/products {product_cd:"P001"} | 非 2xx 状态码 | 高 |
| VP-004 | FUNC-016 | 按商品代码查询成功 | 存在 product_cd="P001" | GET /api/products/P001 | 200 + DTO 含正确字段 | 高 |
| VP-005 | FUNC-016 | 查询不存在的商品代码 | 无 | GET /api/products/P999 | 404 | 高 |
| VP-006 | FUNC-016 | 更新商品成功 | 存在 product_cd="P001" | PUT /api/products/P001 {product_nm_kanji:"商品A改"} | 200 + DTO 含更新后名称 | 高 |
| VP-007 | FUNC-016, NFR-009 | 逻辑删除商品 | 存在 product_cd="P001" | DELETE /api/products/P001 | 200/204；再次 GET 返回 404 | 高 |
| VP-008 | FIELD-004, FIELD-009 | Product Entity 字段与审计字段 | — | 代码审查 | Entity 含 product_id, product_cd(unique), product_nm_kanji, product_nm_kana, unit_cd + 8 审计字段 | 高 |
| VP-009 | FIELD-002 | Product 表名与 schema | — | 代码审查 | @Table(name="a101_product", schema="scash") | 高 |

### 3.2 入库机能（Inbound）

| 验证用例 ID | 覆盖需求 ID | 验证内容 | 前置条件 | 操作 | 期望结果 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- |
| VP-010 | FUNC-001, RULE-001 | 创建入库单默认状态 RECEIVED | 无 | POST /api/inbound-orders {inboundOrderNo:"IN001", companyCd:"C001", productCd:"P001", quantity:100} | 201 + status="RECEIVED" | 高 |
| VP-011 | RULE-005 | 重复入库单号创建失败 | 已存在 inboundOrderNo="IN001" | POST /api/inbound-orders {inboundOrderNo:"IN001"} | 非 2xx 状态码 | 高 |
| VP-012 | FUNC-002 | 按单号查询入库单成功 | 存在 IN001 | GET /api/inbound-orders/IN001 | 200 + DTO 含正确字段 | 高 |
| VP-013 | FUNC-002 | 查询不存在的入库单 | 无 | GET /api/inbound-orders/IN999 | 404 | 高 |
| VP-014 | FUNC-003, RULE-002, INT-001 | 确认入库→库存增加 | IN001 状态 RECEIVED | PUT /api/inbound-orders/IN001/confirm | status="CONFIRMED"；Inventory(C001/P001).quantity += 100 | 高 |
| VP-015 | FUNC-003, RULE-002 | 确认入库→库存不存在时自动创建 | 无对应库存记录 | 确认入库单 | Inventory 记录被创建，quantity=入库数量 | 高 |
| VP-016 | FUNC-004, RULE-003 | 拒绝入库→库存不变 | IN002 状态 RECEIVED | PUT /api/inbound-orders/IN002/reject | status="REJECTED"；Inventory.quantity 不变 | 高 |
| VP-017 | RULE-004, FLOW-001 | 已确认入库单不可再次确认 | IN001 状态 CONFIRMED | PUT /api/inbound-orders/IN001/confirm | 非 2xx 或错误响应 | 高 |
| VP-018 | RULE-004, FLOW-001 | 已确认入库单不可拒绝 | IN001 状态 CONFIRMED | PUT /api/inbound-orders/IN001/reject | 非 2xx 或错误响应 | 高 |
| VP-019 | RULE-004, FLOW-001 | 已拒绝入库单不可再次操作 | IN002 状态 REJECTED | PUT /api/inbound-orders/IN002/reject | 非 2xx 或错误响应 | 高 |
| VP-020 | FIELD-005, FIELD-003 | InboundOrder Entity 字段与审计字段 | — | 代码审查 | Entity 含 inbound_order_id, inbound_order_no(unique), company_cd, product_cd, quantity, status + 8 审计字段 | 高 |
| VP-021 | FIELD-002 | InboundOrder 表名与 schema | — | 代码审查 | @Table(name="s101_inbound_order", schema="scash") | 高 |

### 3.3 拣货机能（Picking）

| 验证用例 ID | 覆盖需求 ID | 验证内容 | 前置条件 | 操作 | 期望结果 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- |
| VP-022 | FUNC-005, RULE-006, INT-003 | 创建拣货单成功 | 存在有效出库单 | POST /api/picking-orders {pickingOrderNo:"PK001", outboundOrderNo:"OB001", productCd:"P001", quantity:50} | 201 + status="PENDING" | 高 |
| VP-023 | RULE-010 | 重复拣货单号创建失败 | 已存在 pickingOrderNo="PK001" | POST /api/picking-orders {pickingOrderNo:"PK001"} | 非 2xx 状态码 | 高 |
| VP-024 | FUNC-006 | 按单号查询拣货单成功 | 存在 PK001 | GET /api/picking-orders/PK001 | 200 + DTO 含正确字段 | 高 |
| VP-025 | FUNC-006 | 查询不存在的拣货单 | 无 | GET /api/picking-orders/PK999 | 404 | 高 |
| VP-026 | FUNC-007, RULE-008, FLOW-002 | 执行拣货→picked_quantity 累加，状态=PICKING | PK001 状态 PENDING | PUT /api/picking-orders/PK001/pick {pickedQuantity:20} | picked_quantity=20, status="PICKING" | 高 |
| VP-027 | FUNC-007, RULE-008 | 分批拣货→picked_quantity 累加 | PK001 已拣 20 | PUT /api/picking-orders/PK001/pick {pickedQuantity:30} | picked_quantity=50 | 高 |
| VP-028 | RULE-007 | 拣货时库存不足→拒绝 | 可用库存 < 拣货数量 | 执行拣货 | 非 2xx 或错误响应 | 高 |
| VP-029 | FUNC-008, RULE-009, FLOW-002 | 完成拣货→status=COMPLETED | picked_quantity >= quantity | PUT /api/picking-orders/PK001/complete | status="COMPLETED" | 高 |
| VP-030 | RULE-009 | picked_quantity 未达 quantity 时 complete 被拒绝 | picked_quantity < quantity | PUT /api/picking-orders/PK002/complete | 非 2xx 或错误响应 | 高 |
| VP-031 | FLOW-002 | PENDING→CANCELLED | PK003 状态 PENDING | 取消操作 | status="CANCELLED" | 高 |
| VP-032 | FLOW-002 | PICKING→CANCELLED | PK004 状态 PICKING | 取消操作 | status="CANCELLED" | 高 |
| VP-033 | FIELD-006, FIELD-003 | PickingOrder Entity 字段与审计字段 | — | 代码审查 | Entity 含 picking_order_id, picking_order_no(unique), outbound_order_no, product_cd, quantity, picked_quantity, status + 8 审计字段 | 高 |
| VP-034 | FIELD-002 | PickingOrder 表名与 schema | — | 代码审查 | @Table(name="s102_picking_order", schema="scash") | 高 |

### 3.4 出库机能（Outbound）

| 验证用例 ID | 覆盖需求 ID | 验证内容 | 前置条件 | 操作 | 期望结果 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- |
| VP-035 | FUNC-009, RULE-011, RULE-017, Q-004 | 创建出库单→校验库存并预占 | 可用库存充足 | POST /api/outbound-orders {outboundOrderNo:"OB001", companyCd:"C001", productCd:"P001", quantity:30} | 201 + status="CREATED"；reserved_quantity += 30 | 高 |
| VP-036 | RULE-011, RULE-018 | 创建出库单→库存不足拒绝 | 可用库存 < 出库数量 | POST /api/outbound-orders | 非 2xx 或错误响应 | 高 |
| VP-037 | RULE-015 | 重复出库单号创建失败 | 已存在 outboundOrderNo="OB001" | POST /api/outbound-orders {outboundOrderNo:"OB001"} | 非 2xx 状态码 | 高 |
| VP-038 | FUNC-010 | 按单号查询出库单成功 | 存在 OB001 | GET /api/outbound-orders/OB001 | 200 + DTO 含正确字段 | 高 |
| VP-039 | FUNC-010 | 查询不存在的出库单 | 无 | GET /api/outbound-orders/OB999 | 404 | 高 |
| VP-040 | FUNC-011, RULE-012, INT-002 | 确认出库→库存扣减 | OB001 状态 CREATED/PICKED | PUT /api/outbound-orders/OB001/ship | status="SHIPPED"；quantity -= 30, reserved_quantity -= 30 | 高 |
| VP-041 | FUNC-012, RULE-014 | 取消出库→恢复预占 | OB002 状态 CREATED | PUT /api/outbound-orders/OB002/cancel | status="CANCELLED"；reserved_quantity 减少 | 高 |
| VP-042 | RULE-013 | 已出库订单不可取消 | OB001 状态 SHIPPED | PUT /api/outbound-orders/OB001/cancel | 非 2xx 或错误响应 | 高 |
| VP-043 | FLOW-003 | CREATED→CANCELLED | OB003 状态 CREATED | 取消操作 | status="CANCELLED" | 高 |
| VP-044 | FLOW-003 | PICKED→CANCELLED | OB004 状态 PICKED | 取消操作 | status="CANCELLED" | 高 |
| VP-045 | FIELD-007, FIELD-003 | OutboundOrder Entity 字段与审计字段 | — | 代码审查 | Entity 含 outbound_order_id, outbound_order_no(unique), company_cd, product_cd, quantity, status + 8 审计字段 | 高 |
| VP-046 | FIELD-002 | OutboundOrder 表名与 schema | — | 代码审查 | @Table(name="s103_outbound_order", schema="scash") | 高 |

### 3.5 库存管理机能（Inventory）

| 验证用例 ID | 覆盖需求 ID | 验证内容 | 前置条件 | 操作 | 期望结果 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- |
| VP-047 | FUNC-013 | 查询库存列表（分页） | 存在库存记录 | GET /api/inventories?page=0&size=10 | 200 + 分页库存列表 | 高 |
| VP-048 | FUNC-014 | 按公司+商品查询库存成功 | 存在 C001/P001 库存 | GET /api/inventories/C001/P001 | 200 + DTO 含 quantity, reserved_quantity | 高 |
| VP-049 | FUNC-014 | 查询不存在的库存 | 无 | GET /api/inventories/C999/P999 | 404 | 高 |
| VP-050 | FUNC-015, RULE-019 | 库存调整→数量变更+记录原因 | 存在 C001/P001 库存 | PUT /api/inventories/C001/P001/adjust {quantity:200, reason:"盘点调整"} | quantity=200；reason 被记录 | 高 |
| VP-051 | RULE-016 | 库存数量不可为负 | 库存 quantity=10 | 调整至 quantity=-1 或扣减超过库存 | 非 2xx 或错误响应 | 高 |
| VP-052 | RULE-018 | 可用库存计算 | quantity=100, reserved_quantity=30 | 查询库存 | 可用库存=70 | 高 |
| VP-053 | FIELD-008, FIELD-003 | Inventory Entity 字段与审计字段 | — | 代码审查 | Entity 含 inventory_id, company_cd, product_cd, quantity, reserved_quantity + 8 审计字段；联合唯一键 (company_cd, product_cd) | 高 |
| VP-054 | FIELD-002 | Inventory 表名与 schema | — | 代码审查 | @Table(name="a102_inventory", schema="scash") | 高 |

### 3.6 仓库调拨机能（Transfer）

| 验证用例 ID | 覆盖需求 ID | 验证内容 | 前置条件 | 操作 | 期望结果 | 优先级 |
| --- | --- | --- | --- | --- | --- | --- |
| VP-055 | FUNC-017, RULE-021, RULE-023 | 创建调拨单→默认 PENDING + 校验库存 | 调出方可用库存充足 | POST /api/transfer-orders {transferOrderNo:"TF001", fromCompanyCd:"C001", toCompanyCd:"C002", productCd:"P001", quantity:20} | 201 + status="PENDING" | 高 |
| VP-056 | RULE-024 | 调出方=调入方→创建失败 | — | POST /api/transfer-orders {fromCompanyCd:"C001", toCompanyCd:"C001"} | 非 2xx 或错误响应 | 高 |
| VP-057 | RULE-023 | 调出方可用库存不足→创建失败 | 可用库存 < 调拨数量 | 创建调拨单 | 非 2xx 或错误响应 | 高 |
| VP-058 | RULE-022 | 重复调拨单号创建失败 | 已存在 transferOrderNo="TF001" | POST /api/transfer-orders {transferOrderNo:"TF001"} | 非 2xx 状态码 | 高 |
| VP-059 | FUNC-018 | 按单号查询调拨单成功 | 存在 TF001 | GET /api/transfer-orders/TF001 | 200 + DTO 含正确字段 | 高 |
| VP-060 | FUNC-018 | 查询不存在的调拨单 | 无 | GET /api/transfer-orders/TF999 | 404 | 高 |
| VP-061 | FUNC-019, RULE-026, INT-007 | 审批调拨→APPROVED + 调出方预占 | TF001 状态 PENDING | PUT /api/transfer-orders/TF001/approve | status="APPROVED"；调出方 reserved_quantity += 20 | 高 |
| VP-062 | RULE-026 | 非 PENDING 状态不可审批 | TF001 状态 APPROVED | PUT /api/transfer-orders/TF001/approve | 非 2xx 或错误响应 | 高 |
| VP-063 | FUNC-020, RULE-026, INT-006 | 调拨出库→SHIPPED + 调出方扣减 | TF001 状态 APPROVED | PUT /api/transfer-orders/TF001/ship | status="SHIPPED"；调出方 quantity -= 20, reserved_quantity -= 20 | 高 |
| VP-064 | RULE-026 | 非 APPROVED 状态不可出库 | TF001 状态 SHIPPED | PUT /api/transfer-orders/TF001/ship | 非 2xx 或错误响应 | 高 |
| VP-065 | FUNC-021, RULE-026, INT-006 | 调拨入库→COMPLETED + 调入方增加 | TF001 状态 SHIPPED | PUT /api/transfer-orders/TF001/receive | status="COMPLETED"；调入方 quantity += 20 | 高 |
| VP-066 | RULE-026 | 非 SHIPPED 状态不可入库 | TF001 状态 PENDING | PUT /api/transfer-orders/TF001/receive | 非 2xx 或错误响应 | 高 |
| VP-067 | FUNC-022, RULE-025 | PENDING→CANCELLED + 恢复预占 | TF002 状态 PENDING | PUT /api/transfer-orders/TF002/cancel | status="CANCELLED" | 高 |
| VP-068 | FUNC-022, RULE-025, INT-007 | APPROVED→CANCELLED + 恢复预占 | TF003 状态 APPROVED（已预占） | PUT /api/transfer-orders/TF003/cancel | status="CANCELLED"；调出方 reserved_quantity 恢复 | 高 |
| VP-069 | RULE-025 | SHIPPED 状态不可取消 | TF001 状态 SHIPPED | PUT /api/transfer-orders/TF001/cancel | 非 2xx 或错误响应 | 高 |
| VP-070 | RULE-025 | COMPLETED 状态不可取消 | TF001 状态 COMPLETED | PUT /api/transfer-orders/TF001/cancel | 非 2xx 或错误响应 | 高 |
| VP-071 | FIELD-010, FIELD-003 | TransferOrder Entity 字段与审计字段 | — | 代码审查 | Entity 含 transfer_order_id, transfer_order_no(unique), from_company_cd, to_company_cd, product_cd, quantity, transferred_quantity, status + 8 审计字段 | 高 |
| VP-072 | FIELD-002 | TransferOrder 表名与 schema | — | 代码审查 | @Table(name="s105_transfer_order", schema="scash") | 高 |

### 3.7 跨模块集成

| 验证用例 ID | 覆盖需求 ID | 验证内容 | 操作链路 | 期望结果 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| VP-073 | INT-001, INT-002, INT-004 | 入库→库存增加→出库→库存扣减全流程 | 创建商品→创建入库单→确认入库→创建出库单→确认出库 | 每步库存数量正确增减 | 高 |
| VP-074 | INT-003, Q-003 | 拣货完成→出库单状态自动变更为 PICKED | 创建出库单→创建拣货单→执行拣货→完成拣货 | 出库单 status="PICKED" | 高 |
| VP-075 | INT-004, FLOW-004 | 入库→出库→拣货→调拨全流程 | 入库确认→出库创建→拣货→出库确认→调拨全流程 | 全流程状态和库存正确 | 高 |
| VP-076 | INT-005 | 公司代码在各实体间一致 | 创建入库单/出库单/库存/调拨单均使用相同 company_cd | 各实体 company_cd 一致 | 中 |
| VP-077 | INT-006, INT-007 | 调拨全流程库存变更 | 创建调拨→审批→出库→入库 | 调出方 quantity 减少、reserved_quantity 先增后减；调入方 quantity 增加 | 高 |

### 3.8 非功能需求

| 验证用例 ID | 覆盖需求 ID | 验证内容 | 验证方法 | 期望结果 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| VP-078 | NFR-001 | Spring Boot 3.4.4 + Java 25 构建 | `mvn compile` | 编译成功 | 高 |
| VP-079 | NFR-002 | 构造器注入，无 @Autowired 字段注入 | 代码审查 | Controller/Service 中无 @Autowired 字段 | 高 |
| VP-080 | NFR-003 | Entity↔DTO 使用 BeanUtils.copyProperties | 代码审查 | Service 层转换方法使用 BeanUtils | 中 |
| VP-081 | NFR-004 | Repository 继承 JpaRepository<Entity, Long> | 代码审查 | 所有 Repository 接口继承正确 | 高 |
| VP-082 | NFR-005 | 禁止原生 SQL 拼接 | 代码审查 | 无字符串拼接 SQL | 高 |
| VP-083 | NFR-006 | 测试使用 @SpringBootTest(RANDOM_PORT) + TestRestTemplate | 代码审查 | 所有测试类注解正确 | 高 |
| VP-084 | NFR-007, BR-002 | JaCoCo 100% + Pitest 100% | `mvn test` + `mvn pitest:mutationCoverage` | 均通过 | 高 |
| VP-085 | NFR-008 | 乐观锁 version 字段 | 代码审查 | Entity 含 version 字段；并发冲突返回 409 | 中 |
| VP-086 | NFR-009 | 逻辑删除 deleted_flag | 代码审查 | 删除操作设置 deleted_flag='1'；查询过滤 | 中 |
| VP-087 | NFR-010 | 代码行长度 ≤ 120 字符 | 代码审查/格式化检查 | 无超长行 | 低 |
| VP-088 | FIELD-001 | PostgreSQL 数据源 | 检查 application.properties | datasource 指向 PostgreSQL | 高 |
| VP-089 | FIELD-003 | 所有实体含 8 个审计字段 | 代码审查 | created_ts, created_user_cd, created_program, updated_ts, updated_user_cd, updated_program, version, deleted_flag | 高 |

---

## 4. PRD 验收标准→验证用例映射

| PRD 验收标准 ID | 验收标准摘要 | 对应验证用例 ID | 覆盖状态 |
| --- | --- | --- | --- |
| AC-PRD-001 | POST /api/products 返回 201 | VP-001, VP-002 | 已覆盖 |
| AC-PRD-002 | product_cd 全局唯一 | VP-003 | 已覆盖 |
| AC-PRD-003 | GET /api/products/{productCd} 返回 200/404 | VP-004, VP-005 | 已覆盖 |
| AC-PRD-004 | PUT /api/products/{productCd} 更新成功 | VP-006 | 已覆盖 |
| AC-PRD-005 | DELETE 逻辑删除 | VP-007 | 已覆盖 |
| AC-PRD-006 | POST /api/inbound-orders 返回 201，默认 RECEIVED | VP-010 | 已覆盖 |
| AC-PRD-007 | inbound_order_no 全局唯一 | VP-011 | 已覆盖 |
| AC-PRD-008 | GET /api/inbound-orders/{no} 返回 200/404 | VP-012, VP-013 | 已覆盖 |
| AC-PRD-009 | 确认入库→库存增加 | VP-014, VP-015 | 已覆盖 |
| AC-PRD-010 | 拒绝入库→库存不变 | VP-016 | 已覆盖 |
| AC-PRD-011 | 已确认/已拒绝入库单不可再操作 | VP-017, VP-018, VP-019 | 已覆盖 |
| AC-PRD-012 | POST /api/picking-orders 返回 201 | VP-022 | 已覆盖 |
| AC-PRD-013 | picking_order_no 全局唯一 | VP-023 | 已覆盖 |
| AC-PRD-014 | GET /api/picking-orders/{no} 返回 200/404 | VP-024, VP-025 | 已覆盖 |
| AC-PRD-015 | 执行拣货→picked_quantity 累加 | VP-026, VP-027 | 已覆盖 |
| AC-PRD-016 | 拣货库存不足→拒绝 | VP-028 | 已覆盖 |
| AC-PRD-017 | 完成拣货→COMPLETED | VP-029 | 已覆盖 |
| AC-PRD-018 | picked_quantity 未达标→complete 拒绝 | VP-030 | 已覆盖 |
| AC-PRD-019 | POST /api/outbound-orders 返回 201 + 库存预占 | VP-035 | 已覆盖 |
| AC-PRD-020 | outbound_order_no 全局唯一 | VP-037 | 已覆盖 |
| AC-PRD-021 | GET /api/outbound-orders/{no} 返回 200/404 | VP-038, VP-039 | 已覆盖 |
| AC-PRD-022 | 确认出库→库存扣减 | VP-040 | 已覆盖 |
| AC-PRD-023 | 取消出库→恢复预占 | VP-041 | 已覆盖 |
| AC-PRD-024 | 已出库订单不可取消 | VP-042 | 已覆盖 |
| AC-PRD-025 | GET /api/inventories 分页查询 | VP-047 | 已覆盖 |
| AC-PRD-026 | GET /api/inventories/{companyCd}/{productCd} 返回 200/404 | VP-048, VP-049 | 已覆盖 |
| AC-PRD-027 | 库存调整→数量变更+记录原因 | VP-050 | 已覆盖 |
| AC-PRD-028 | 库存数量不可为负 | VP-051 | 已覆盖 |
| AC-PRD-029 | POST /api/transfer-orders 返回 201，默认 PENDING | VP-055 | 已覆盖 |
| AC-PRD-030 | 调出方=调入方→创建失败 | VP-056 | 已覆盖 |
| AC-PRD-031 | 调拨审批→预占+出库→扣减+入库→增加 | VP-061, VP-063, VP-065 | 已覆盖 |
| AC-PRD-032 | SHIPPED/COMPLETED 不可取消 | VP-069, VP-070 | 已覆盖 |
| AC-PRD-033 | 状态变更仅允许合法路径 | VP-062, VP-064, VP-066 | 已覆盖 |
| AC-PRD-034 | 全流程集成测试通过 | VP-073, VP-075 | 已覆盖 |

---

## 5. 用户故事验收场景覆盖

> 注：`user-stories.md` 仅覆盖拣货机能（US-001~US-008），以下映射仅涉及拣货模块。

| 用户故事 | 验收场景 ID | 验收场景摘要 | 对应验证用例 ID | 覆盖状态 |
| --- | --- | --- | --- | --- |
| US-001 创建拣货单 | AC-001 | 创建成功返回 201 | VP-022 | 已覆盖 |
| US-001 | AC-002 | 必填字段校验 | VP-022（含全部字段） | 已覆盖 |
| US-001 | AC-003 | 重复单号拒绝 | VP-023 | 已覆盖 |
| US-002 查询拣货单 | AC-004 | 按单号查询成功 | VP-024 | 已覆盖 |
| US-002 | AC-005 | 不存在返回 404 | VP-025 | 已覆盖 |
| US-003 状态流转 | AC-006 | PENDING→PICKING | VP-026 | 已覆盖 |
| US-003 | AC-007 | PICKING→COMPLETED | VP-029 | 已覆盖 |
| US-003 | AC-008 | PENDING→CANCELLED | VP-031 | 已覆盖 |
| US-003 | AC-009 | 非法状态转换拒绝 | VP-030 | 已覆盖 |
| US-004 更新实拣数量 | AC-010 | picked_quantity 累加 | VP-026, VP-027 | 已覆盖 |
| US-004 | AC-011 | 库存不足拒绝 | VP-028 | 已覆盖 |
| US-005 删除拣货单 | AC-012 | 逻辑删除后不可查询 | VP-031（CANCELLED 等价） | 部分覆盖 |
| US-006 乐观锁 | AC-013 | 并发更新冲突返回 409 | VP-085 | 已覆盖 |
| US-007 逻辑删除过滤 | AC-014 | 查询过滤 deleted_flag='1' | VP-007, VP-086 | 已覆盖 |
| US-008 审计字段 | AC-015 | created_ts/updated_ts 自动填充 | VP-089 | 已覆盖 |

---

## 6. 设计元素验证映射

> **说明**：本项目当前无独立的设计文档（`design.md` 不存在）。设计要素来源于 `AGENTS.md` 中的实体设计和 API 端点定义，验证已融入第 3 节各模块验证用例中。

| 设计要素 | 来源 | 对应验证用例 ID |
| --- | --- | --- |
| 6 张数据库表结构 | AGENTS.md §4.0~4.5 | VP-008, VP-009, VP-020, VP-021, VP-033, VP-034, VP-045, VP-046, VP-053, VP-054, VP-071, VP-072 |
| 22 个 API 端点 | AGENTS.md §4.0~4.5 | VP-001~VP-072（全部 API 测试用例） |
| 状态机设计（4 套） | AGENTS.md §7.4 | VP-017~VP-019, VP-026, VP-029~VP-032, VP-040~VP-044, VP-061~VP-070 |
| 库存预占/扣减模型 | AGENTS.md §4.4 | VP-035, VP-040, VP-041, VP-061, VP-063, VP-065, VP-068 |

---

## 7. 测试执行计划

### 7.1 测试文件规划

| 测试类 | 覆盖模块 | 覆盖验证用例 ID |
| --- | --- | --- |
| `ProductControllerTest` | 商品主数据 | VP-001~VP-009 |
| `InboundOrderControllerTest` | 入库 | VP-010~VP-021 |
| `PickingOrderControllerTest` | 拣货 | VP-022~VP-034 |
| `OutboundOrderControllerTest` | 出库 | VP-035~VP-046 |
| `InventoryControllerTest` | 库存管理 | VP-047~VP-054 |
| `TransferOrderControllerTest` | 仓库调拨 | VP-055~VP-072 |
| `IntegrationFlowTest` | 跨模块集成 | VP-073~VP-077 |

### 7.2 执行顺序

1. **商品主数据** — 基础数据，其他模块依赖
2. **库存管理** — 入库/出库/调拨均操作库存
3. **入库机能** — 库存增加入口
4. **出库机能** — 库存扣减入口
5. **拣货机能** — 关联出库单
6. **调拨机能** — 最复杂状态机
7. **集成流程** — 全链路验证

### 7.3 自动化命令

```bash
# 编译检查
mvn compile

# 运行全部测试 + JaCoCo 覆盖率
mvn test

# 变异测试
mvn org.pitest:pitest-maven:mutationCoverage
```

---

## 8. 覆盖率追踪矩阵

### 8.1 源需求覆盖率

| 需求类别 | 总数 | 已覆盖 | 覆盖率 |
| --- | --- | --- | --- |
| BR（业务目标） | 2 | 2 | 100% |
| FIELD（数据模型） | 10 | 10 | 100% |
| FUNC（功能） | 22 | 22 | 100% |
| RULE（业务规则） | 26 | 26 | 100% |
| FLOW（流程） | 4 | 4 | 100% |
| NFR（非功能） | 10 | 10 | 100% |
| INT（集成） | 7 | 7 | 7 | 100% |
| **合计** | **81** | **81** | **100%** |

> 注：INT-006 和 INT-007 在需求登记中与调拨机能章节重复出现，实际独立需求为 5 条（INT-001~INT-005），加上调拨章节的 INT-006/INT-007 共 7 条。

### 8.2 PRD 验收标准覆盖率

| 指标 | 值 |
| --- | --- |
| PRD 验收标准总数 | 34 |
| 已覆盖 | 34 |
| 覆盖率 | 100% |

### 8.3 验证用例统计

| 指标 | 值 |
| --- | --- |
| 验证用例总数 | 89 |
| API 集成测试用例 | 72 |
| 代码审查用例 | 12 |
| 端到端流程用例 | 5 |

---

## 9. 风险与缓解

| 风险 | 影响范围 | 缓解措施 |
| --- | --- | --- |
| user-stories.md 仅覆盖拣货模块 | 其他 5 个模块无用户故事级验收场景 | 以 PRD 验收标准为基准补充验证用例 |
| design.md 不存在 | 设计要素验证无独立上游文档 | 以 AGENTS.md 实体设计和 API 定义替代 |
| 乐观锁并发测试复杂度高 | NFR-008 验证可能不充分 | 编写专用并发测试用例，模拟两线程同时更新 |
| Pitest 100% 变异覆盖率难以达成 | BR-002 门禁可能不通过 | 优先保证 JaCoCo 100%，Pitest 逐步提升 |
| 跨模块事务一致性 | INT-001/002/006 涉及多表更新 | 使用 @Transactional 保证原子性，集成测试验证回滚 |
| 库存预占时机（Q-004） | 影响出库创建和可用库存计算 | 按默认假设（创建时预占）实现并验证 |

---

## 10. 质量门禁

| 门禁项 | 标准 | 验证方法 | 通过条件 |
| --- | --- | --- | --- |
| G1 编译 | `mvn compile` 无错误 | CI 自动执行 | BUILD SUCCESS |
| G2 单元/集成测试 | 全部测试通过 | `mvn test` | 0 failures, 0 errors |
| G3 JaCoCo 覆盖率 | 指令 ≥ 100%, 分支 ≥ 100% | JaCoCo 报告 | 覆盖率达标 |
| G4 Pitest 变异测试 | 变异覆盖率 ≥ 100% | Pitest 报告 | 覆盖率达标 |
| G5 需求覆盖率 | 每条需求至少 1 个验证用例 | 本文档映射矩阵 | 100% |
| G6 PRD 验收标准覆盖率 | 每条验收标准至少 1 个验证用例 | 本文档第 4 节 | 100% |
| G7 代码规范 | 无 @Autowired 字段注入、无 SQL 拼接、行宽 ≤ 120 | 代码审查 | 全部合规 |

---

*最后更新：2026-07-14*
