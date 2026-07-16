# TASK-003: 入库机能

## 覆盖需求

FUNC-001~004, FIELD-005, RULE-001~005, FLOW-001, INT-001, BR-001

## 验证用例

VP-010 ~ VP-021

## 依赖

TASK-001（Product Entity）✅ 已完成
TASK-002（Inventory Entity & Service）✅ 已完成

## 文件计划

| 类型 | 文件路径 | 实际路径 |
| --- | --- | --- |
| 产品代码 | `src/main/java/com/common/entity/InboundOrder.java` | ✅ |
| 产品代码 | `src/main/java/com/gc/inbound/dto/InboundOrderDTO.java` | ✅ |
| 产品代码 | `src/main/java/com/gc/inbound/repository/InboundOrderRepository.java` | ✅ |
| 产品代码 | `src/main/java/com/gc/inbound/service/InboundOrderService.java` | ✅ |
| 产品代码 | `src/main/java/com/gc/inbound/controller/InboundOrderController.java` | ✅ |
| 测试代码 | `src/test/java/com/gc/inbound/controller/InboundOrderControllerTest.java` | ✅ |

## TDD 步骤

### Step 1: 编写入库创建与查询测试 ✅

- 创建入库单默认 RECEIVED（VP-010）
- 按单号查询成功（VP-012）
- 查询不存在返回 404（VP-013）
- 重复单号失败（VP-011）
- 删除的商品不能入库（VP-014）

### Step 2: 运行红灯验证 ✅

- 执行 `mvn test -Dtest=InboundOrderControllerTest`，确认编译失败
- 错误：InboundOrder、InboundOrderDTO、InboundOrderRepository 类未实现

### Step 3: 实现最小产品代码 ✅

- 创建 `InboundOrder` Entity（`s101_inbound_order`）
- 创建 `InboundOrderDTO`
- 创建 `InboundOrderRepository`
- 创建 `InboundOrderService`（create, get）
  - 商品存在性检查
  - 删除商品检查
  - 数量合法性检查
  - 重复单号检查
- 创建 `InboundOrderController`（POST/GET）

### Step 4: 运行绿灯验证 ✅

- 执行 `mvn clean test -Dtest=InboundOrderControllerTest,OutboundOrderControllerTest`
- 结果：Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
- VP-010~VP-014, VP-035~VP-039 全部通过
- 添加全局异常处理器：`src/main/java/com/common/controller/GlobalExceptionHandler.java`

### Step 5: 补充确认/拒绝/库存集成测试 ✅

- 确认入库→库存增加（VP-015, VP-016）
- 拒绝入库→库存不变（VP-017）
- 已确认/已拒绝不可再操作（VP-018~020）
- 异常场景测试（VP-021）

### Step 6: 运行完整绿灯验证 ✅

- 执行 `mvn test -Dtest=InboundOrderControllerTest`，确认全部通过
- 结果：Tests run: 12, Failures: 0, Errors: 0, Skipped: 0

## 结果记录

```yaml
changed_files:
  - src/main/java/com/common/entity/InboundOrder.java
  - src/main/java/com/gc/inbound/dto/InboundOrderDTO.java
  - src/main/java/com/gc/inbound/repository/InboundOrderRepository.java
  - src/main/java/com/gc/inbound/service/InboundOrderService.java
  - src/main/java/com/gc/inbound/controller/InboundOrderController.java
  - src/test/java/com/gc/inbound/controller/InboundOrderControllerTest.java
  - src/test/resources/schema.sql
  - src/main/java/com/common/controller/GlobalExceptionHandler.java
tests_added_or_changed:
  - src/test/java/com/gc/inbound/controller/InboundOrderControllerTest.java
commands_run:
  - cwd: D:/giencoder/giencoder-learn_github
    command: mvn test -Dtest=InboundOrderControllerTest
    exit_code: 0
    result: PASS
    output_summary: Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
tdd_green_result:
  cwd: D:/giencoder/giencoder-learn_github
  command: mvn test -Dtest=InboundOrderControllerTest
  exit_code: 0
  result: PASS
  output_summary: Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
additional_checks:
  - 商品存在性检查：已实现
  - 删除商品检查：已实现
  - 数量合法性校验：数量必须>0
  - 重复单号检查：已实现
  - 确认入库功能：已实现（调用 InventoryService.addQuantity）
  - 拒绝入库功能：已实现（状态变更为 REJECTED）
  - 状态校验逻辑：已实现（仅 RECEIVED 状态可确认/拒绝）
requirement_ids_covered:
  - FUNC-001~004
  - FIELD-005
  - RULE-001~005
  - FLOW-001
  - INT-001
  - BR-001
user_stories_covered:
  - VP-010: 创建入库单默认 RECEIVED 状态
  - VP-011: 重复单号创建失败
  - VP-012: 按单号查询入库单成功
  - VP-013: 查询不存在的入库单返回 404
  - VP-014: 删除的商品不能入库
  - VP-015: 确认入库→库存增加
  - VP-016: 确认入库→状态变更为 CONFIRMED
  - VP-017: 拒绝入库→库存不变
  - VP-018: 已确认的入库单不可再确认
  - VP-019: 已拒绝的入库单不可确认
  - VP-020: 已确认的入库单不可拒绝
  - VP-021: 不存在的入库单确认失败
remaining_risks:
  - 无
```

## 完成状态

✅ **TASK-003 已完成**（12/12 测试通过）
