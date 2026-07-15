# TASK-004: 出库机能

## 覆盖需求

FUNC-009~012, FIELD-007, RULE-011~015, FLOW-003, INT-002, BR-001

## 验证用例

VP-035 ~ VP-046

## 依赖

TASK-001（Product Entity）、TASK-002（Inventory Entity & Service）

## 文件计划

| 类型 | 文件路径 |
| --- | --- |
| 产品代码 | `src/main/java/com/example/demo/entity/OutboundOrder.java` |
| 产品代码 | `src/main/java/com/example/demo/dto/OutboundOrderDTO.java` |
| 产品代码 | `src/main/java/com/example/demo/repository/OutboundOrderRepository.java` |
| 产品代码 | `src/main/java/com/example/demo/service/OutboundOrderService.java` |
| 产品代码 | `src/main/java/com/example/demo/controller/OutboundOrderController.java` |
| 测试代码 | `src/test/java/com/example/demo/controller/OutboundOrderControllerTest.java` |

## TDD 步骤

### Step 1: 编写出库创建与查询测试

- 创建出库单→校验库存并预占（VP-035）
- 按单号查询成功（VP-038）
- 查询不存在返回 404（VP-039）

### Step 2: 运行红灯验证

- 执行 `mvn test -Dtest=OutboundOrderControllerTest`，确认编译失败

### Step 3: 实现最小产品代码

- 创建 `OutboundOrder` Entity（`s103_outbound_order`）
- 创建 `OutboundOrderDTO`
- 创建 `OutboundOrderRepository`
- 创建 `OutboundOrderService`（create, get, ship, cancel）
- 创建 `OutboundOrderController`（POST/GET/PUT ship/PUT cancel）

### Step 4: 运行绿灯验证

- 执行 `mvn test -Dtest=OutboundOrderControllerTest`，确认创建与查询测试通过

### Step 5: 补充发货/取消/库存扣减/异常测试

- 确认出库→库存扣减（VP-040）
- 取消出库→恢复预占（VP-041）
- 库存不足拒绝创建（VP-036）
- 重复单号失败（VP-037）
- 已出库不可取消（VP-042）
- 状态流转异常路径（VP-043, VP-044）

### Step 6: 运行完整绿灯验证

- 执行 `mvn test -Dtest=OutboundOrderControllerTest`，确认全部通过

## 结果记录

（执行后填写）
