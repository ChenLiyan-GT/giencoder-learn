# TASK-005: 拣货机能

## 覆盖需求

FUNC-005~008, FIELD-006, RULE-006~010, FLOW-002, INT-003, BR-001

## 验证用例

VP-022 ~ VP-034

## 依赖

TASK-001（Product Entity）✅ 已完成
TASK-002（Inventory）✅ 已完成
TASK-004（OutboundOrder）✅ 已完成

## 文件计划

| 类型 | 文件路径 | 实际路径 |
| --- | --- | --- |
| 产品代码 | `src/main/java/com/example/demo/entity/PickingOrder.java` | `src/main/java/com/common/entity/PickingOrder.java` ✅ |
| 产品代码 | `src/main/java/com/example/demo/dto/PickingOrderDTO.java` | `src/main/java/com/gc/picking/dto/PickingOrderDTO.java` ✅ |
| 产品代码 | `src/main/java/com/example/demo/repository/PickingOrderRepository.java` | `src/main/java/com/gc/picking/repository/PickingOrderRepository.java` ✅ |
| 产品代码 | `src/main/java/com/example/demo/service/PickingOrderService.java` | `src/main/java/com/gc/picking/service/PickingOrderService.java` ✅ |
| 产品代码 | `src/main/java/com/example/demo/controller/PickingOrderController.java` | `src/main/java/com/gc/picking/controller/PickingOrderController.java` ✅ |
| 测试代码 | `src/test/java/com/example/demo/controller/PickingOrderControllerTest.java` | `src/test/java/com/gc/picking/controller/PickingOrderControllerTest.java` ✅ |

## TDD 步骤

- [x] Step 1: 编写拣货创建与查询测试
  - 创建拣货单成功（VP -022）
  - 按单号查询成功（VP-024）
  - 查询不存在返回 404（VP-025）
- [x] Step 2: 运行红灯验证
  - 执行 `mvn test -Dtest=PickingOrderControllerTest`，确认编译失败
- [x] Step 3: 实现最小产品代码
  - 创建 `PickingOrder` Entity（`s102_picking_order`）
  - 创建 `PickingOrderDTO`
  - 创建 `PickingOrderRepository`
  - 创建 `PickingOrderService`（create, get, pick, complete）
  - 创建 `PickingOrderController`（POST/GET/PUT pick/PUT complete）
- [x] Step 4: 运行绿灯验证
  - 执行 `mvn test -Dtest=PickingOrderControllerTest`，确认创建与查询测试通过
- [x] Step 5: 补充拣货执行/完成/异常测试
  - 执行拣货→picked_quantity 累加（VP-026, VP-027）
  - 拣货库存不足拒绝（VP-028）
  - 完成拣货→COMPLETED（VP-029）
  - picked 未达标 complete 被拒绝（VP-030）
  - 重复单号失败（VP-023）
  - 取消拣货（VP-031, VP-032）
- [x] Step 6: 运行完整绿灯验证
  - 执行 `mvn test -Dtest=PickingOrderControllerTest`，确认全部通过

## 结果记录

**Step 4 绿灯验证（2026-07-16）:**
- 测试结果：Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
- 覆盖 VP-022~VP-032（VP-033/VP-034 为代码审查项）
- 验证命令：`mvn test -Dtest=PickingOrderControllerTest`

**VP-033 代码审查：**
- ✅ PickingOrder Entity 含 picking_order_id, picking_order_no, outbound_order_no, product_cd, quantity, picked_quantity, status + 8 审计字段

**VP-034 代码审查：**
- ✅ @Table(name="s102_picking_order", schema="scash")

**TASK-005 完成状态：**
- ✅ 11/11 测试通过
- ✅ VP-022~VP-034 全部覆盖
- ✅ 拣货单创建、查询、执行、完成、取消功能全部实现
