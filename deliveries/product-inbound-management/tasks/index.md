# 任务索引

## 任务总览

| 任务 ID | 任务名 | 依赖 | 状态 | 覆盖需求 |
| --- | --- | --- | --- | --- |
| TASK-001 | 商品主数据 CRUD | 无 | ✅ 已完成 | FUNC-016, FIELD-004, FIELD-009, RULE-020, NFR-009 |
| TASK-002 | 库存管理 CRUD | TASK-001 | ✅ 已完成 | FUNC-013~015, FIELD-008, RULE-016~019 |
| TASK-003 | 入库机能 | TASK-001, TASK-002 | 待执行 | FUNC-001~004, FIELD-005, RULE-001~005, FLOW-001, INT-001 |
| TASK-004 | 出库机能 | TASK-001, TASK-002 | 待执行 | FUNC-009~012, FIELD-007, RULE-011~015, FLOW-003, INT-002 |
| TASK-005 | 拣货机能 | TASK-001, TASK-002, TASK-004 | 待执行 | FUNC-005~008, FIELD-006, RULE-006~010, FLOW-002, INT-003 |
| TASK-006 | 仓库调拨机能 | TASK-001, TASK-002 | 待执行 | FUNC-017~022, FIELD-010, RULE-021~026, FLOW-004, INT-006, INT-007 |
| TASK-007 | 跨模块集成流程 | TASK-001~006 | 待执行 | INT-004, INT-005, FLOW-001~004 |

## 执行顺序

1. ✅ TASK-001 (已完成) → 2. ✅ TASK-002 (已完成) → 3. TASK-003 → 4. TASK-004 → 5. TASK-005 → 6. TASK-006 → 7. TASK-007

## 代码结构约定

实际代码包路径遵循以下规范：

### Entity 层
- 所有 Entity 统一放在 `com.common.entity.*` 包下
- 例如：
  - `src/main/java/com/common/entity/Product.java`
  - `src/main/java/com/common/entity/Inventory.java`
  - `src/main/java/com/common/entity/InboundOrder.java`
  - `src/main/java/com/common/entity/OutboundOrder.java`
  - `src/main/java/com/common/entity/PickingOrder.java`
  - `src/main/java/com/common/entity/TransferOrder.java`

### 各功能模块
- 各机能模块代码放在 `com.gc.{机能名}.*` 包下
- 每个模块包含：dto/repository/service/controller 四个子包
- 例如：
  - 商品主数据：`com.gc.product.*`
  - 库存管理：`com.gc.inventory.*`
  - 入库机能：`com.gc.inbound.*`
  - 出库机能：`com.gc.outbound.*`
  - 拣货机能：`com.gc.picking.*`
  - 仓库调拨：`com.gc.transfer.*`
  - 集成测试：`com.gc.integration.*`

### 测试代码
- 测试代码放在 `src/test/java/com/gc/{机能名}/controller/` 下
- 例如：
  - `src/test/java/com/gc/product/controller/ProductControllerTest.java`
  - `src/test/java/com/gc/inventory/controller/InventoryControllerTest.java`
  - `src/test/java/com/gc/inbound/controller/InboundOrderControllerTest.java`
