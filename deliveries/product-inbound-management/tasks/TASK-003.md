# TASK-003: 入库机能

## 覆盖需求

FUNC-001~004, FIELD-005, RULE-001~005, FLOW-001, INT-001, BR-001

## 验证用例

VP-010 ~ VP-021

## 依赖

TASK-001（Product Entity）✅ 已完成
TASK-002（Inventory Entity & Service）✅ 已完成

## 文件计划

| 类型 | 文件路径 |
| --- | --- |
| 产品代码 | `src/main/java/com/common/entity/InboundOrder.java` |
| 产品代码 | `src/main/java/com/gc/inbound/dto/InboundOrderDTO.java` |
| 产品代码 | `src/main/java/com/gc/inbound/repository/InboundOrderRepository.java` |
| 产品代码 | `src/main/java/com/gc/inbound/service/InboundOrderService.java` |
| 产品代码 | `src/main/java/com/gc/inbound/controller/InboundOrderController.java` |
| 测试代码 | `src/test/java/com/gc/inbound/controller/InboundOrderControllerTest.java` |

## TDD 步骤

- [ ] Step 1: 编写入库创建与查询测试
  - 创建入库单默认 RECEIVED（VP-010）
  - 按单号查询成功（VP-012）
  - 查询不存在返回 404（VP-013）
- [ ] Step 2: 运行红灯验证
  - 执行 `mvn test -Dtest=InboundOrderControllerTest`，确认编译失败
- [ ] Step 3: 实现最小产品代码
  - 创建 `InboundOrder` Entity（`s101_inbound_order`）
  - 创建 `InboundOrderDTO`
  - 创建 `InboundOrderRepository`
  - 创建 `InboundOrderService`（create, get, confirm, reject）
  - 创建 `InboundOrderController`（POST/GET/PUT confirm/PUT reject）
- [ ] Step 4: 运行绿灯验证
  - 执行 `mvn test -Dtest=InboundOrderControllerTest`，确认创建与查询测试通过
- [ ] Step 5: 补充确认/拒绝/库存集成/异常测试
  - 确认入库→库存增加（VP-014, VP-015）
  - 拒绝入库→库存不变（VP-016）
  - 重复单号失败（VP-011）
  - 已确认/已拒绝不可再操作（VP-017~019）
- [ ] Step 6: 运行完整绿灯验证
  - 执行 `mvn test -Dtest=InboundOrderControllerTest`，确认全部通过

## 结果记录

（执行后填写）
