# TASK-005: 拣货机能

## 覆盖需求

FUNC-005~008, FIELD-006, RULE-006~010, FLOW-002, INT-003, BR-001

## 验证用例

VP-022 ~ VP-034

## 依赖

TASK-001（Product Entity）、TASK-002（Inventory）、TASK-004（OutboundOrder）

## 文件计划

| 类型 | 文件路径 |
| --- | --- |
| 产品代码 | `src/main/java/com/example/demo/entity/PickingOrder.java` |
| 产品代码 | `src/main/java/com/example/demo/dto/PickingOrderDTO.java` |
| 产品代码 | `src/main/java/com/example/demo/repository/PickingOrderRepository.java` |
| 产品代码 | `src/main/java/com/example/demo/service/PickingOrderService.java` |
| 产品代码 | `src/main/java/com/example/demo/controller/PickingOrderController.java` |
| 测试代码 | `src/test/java/com/example/demo/controller/PickingOrderControllerTest.java` |

## TDD 步骤

### Step 1: 编写拣货创建与查询测试

- 创建拣货单成功（VP-022）
- 按单号查询成功（VP-024）
- 查询不存在返回 404（VP-025）

### Step 2: 运行红灯验证

- 执行 `mvn test -Dtest=PickingOrderControllerTest`，确认编译失败

### Step 3: 实现最小产品代码

- 创建 `PickingOrder` Entity（`s102_picking_order`）
- 创建 `PickingOrderDTO`
- 创建 `PickingOrderRepository`
- 创建 `PickingOrderService`（create, get, pick, complete）
- 创建 `PickingOrderController`（POST/GET/PUT pick/PUT complete）

### Step 4: 运行绿灯验证

- 执行 `mvn test -Dtest=PickingOrderControllerTest`，确认创建与查询测试通过

### Step 5: 补充拣货执行/完成/异常测试

- 执行拣货→picked_quantity 累加（VP-026, VP-027）
- 拣货库存不足拒绝（VP-028）
- 完成拣货→COMPLETED（VP-029）
- picked 未达标 complete 被拒绝（VP-030）
- 重复单号失败（VP-023）
- 取消拣货（VP-031, VP-032）

### Step 6: 运行完整绿灯验证

- 执行 `mvn test -Dtest=PickingOrderControllerTest`，确认全部通过

## 结果记录

（执行后填写）
