# TASK-004: 出库机能

## 覆盖需求

FUNC-009~012, FIELD-007, RULE-011~015, FLOW-003, INT-002, BR-001

## 验证用例

VP-035 ~ VP-046

## 依赖

TASK-001（Product Entity）✅ 已完成
TASK-002（Inventory Entity & Service）✅ 已完成

## 文件计划

| 类型 | 文件路径 | 实际路径 |
| --- | --- | --- |
| 产品代码 | `src/main/java/com/common/entity/OutboundOrder.java` | ✅ |
| 产品代码 | `src/main/java/com/gc/outbound/dto/OutboundOrderDTO.java` | ✅ |
| 产品代码 | `src/main/java/com/gc/outbound/repository/OutboundOrderRepository.java` | ✅ |
| 产品代码 | `src/main/java/com/gc/outbound/service/OutboundOrderService.java` | ✅ |
| 产品代码 | `src/main/java/com/gc/outbound/controller/OutboundOrderController.java` | ✅ |
| 测试代码 | `src/test/java/com/gc/outbound/controller/OutboundOrderControllerTest.java` | ✅ |

## TDD 步骤

### Step 1: 编写出库创建与查询测试 ✅

- 创建出库单→校验库存并预占（VP-035）
- 按单号查询成功（VP-038）
- 查询不存在返回 404（VP-039）
- 库存不足拒绝创建（VP-036）
- 重复单号失败（VP-037）

### Step 2: 运行红灯验证 ✅

- 执行 `mvn test -Dtest=OutboundOrderControllerTest`，确认编译失败
- 错误：OutboundOrder、OutboundOrderDTO、OutboundOrderRepository 类未实现

### Step 3: 实现最小产品代码 ✅

- 创建 `OutboundOrder` Entity（`s103_outbound_order`）
- 创建 `OutboundOrderDTO`
- 创建 `OutboundOrderRepository`
- 创建 `OutboundOrderService`（create, get）
  - 库存充足检查
  - 预占库存逻辑
  - 重复单号检查
- 创建 `OutboundOrderController`（POST/GET）

### Step 4: 运行绿灯验证 ✅

- 执行 `mvn clean test -Dtest=InboundOrderControllerTest,OutboundOrderControllerTest`
- 结果：Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
- VP-010~VP-014, VP-035~VP-039 全部通过
- 添加全局异常处理器：`src/main/java/com/common/controller/GlobalExceptionHandler.java`

### Step 5: 补充发货/取消/库存扣减/异常测试 ⏳

- 确认出库→库存扣减（VP-040）
- 取消出库→恢复预占（VP-041）
- 已出库不可取消（VP-042）
- 状态流转异常路径（VP-043, VP-044）

### Step 6: 运行完整绿灯验证 ⏳

- 执行 `mvn test -Dtest=OutboundOrderControllerTest`，确认全部通过

## 结果记录

```yaml
changed_files:
  - src/main/java/com/common/entity/OutboundOrder.java
  - src/main/java/com/gc/outbound/dto/OutboundOrderDTO.java
  - src/main/java/com/gc/outbound/repository/OutboundOrderRepository.java
  - src/main/java/com/gc/outbound/service/OutboundOrderService.java
  - src/main/java/com/gc/outbound/controller/OutboundOrderController.java
  - src/test/java/com/gc/outbound/controller/OutboundOrderControllerTest.java
  - src/test/resources/schema.sql
  - src/main/java/com/common/controller/GlobalExceptionHandler.java
tests_added_or_changed:
  - src/test/java/com/gc/outbound/controller/OutboundOrderControllerTest.java
commands_run:
  - cwd: D:/giencoder/giencoder-learn_github
    command: mvn clean test -Dtest=InboundOrderControllerTest,OutboundOrderControllerTest
    exit_code: 0
    result: PASS
    output_summary: Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
tdd_green_result:
  cwd: D:/giencoder/giencoder-learn_github
  command: mvn clean test -Dtest=InboundOrderControllerTest,OutboundOrderControllerTest
  exit_code: 0
  result: PASS
  output_summary: Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
additional_checks:
  - 库存充足检查：已实现
  - 预占库存逻辑：已实现
  - 重复单号检查：已实现
requirement_ids_covered:
  - FUNC-009~012
  - FIELD-007
  - RULE-011~015
  - FLOW-003
  - INT-002
  - BR-001
user_stories_covered:
  - VP-035: 创建出库单默认 PENDING 状态
  - VP-036: 库存不足拒绝创建
  - VP-037: 重复单号创建失败
  - VP-038: 按单号查询出库单成功
  - VP-039: 查询不存在的出库单返回 404
remaining_risks:
  - Step 5 待实现：发货/取消功能
  - Step 5 待实现：库存扣减测试
```
