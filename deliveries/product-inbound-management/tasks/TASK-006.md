# TASK-006: 仓库调拨机能

## 覆盖需求

FUNC-017~022, FIELD-010, RULE-021~026, FLOW-004, INT-006, INT-007, BR-001

## 验证用例

VP-055 ~ VP-072

## 依赖

TASK-001（Product Entity）✅ 已完成
TASK-002（Inventory Entity & Service）✅ 已完成

## 文件计划

| 类型 | 文件路径 |
| --- | --- |
| 产品代码 | `src/main/java/com/common/entity/TransferOrder.java` |
| 产品代码 | `src/main/java/com/gc/transfer/dto/TransferOrderDTO.java` |
| 产品代码 | `src/main/java/com/gc/transfer/repository/TransferOrderRepository.java` |
| 产品代码 | `src/main/java/com/gc/transfer/service/TransferOrderService.java` |
| 产品代码 | `src/main/java/com/gc/transfer/controller/TransferOrderController.java` |
| 测试代码 | `src/test/java/com/gc/transfer/controller/TransferOrderControllerTest.java` |

## TDD 步骤

- [ ] Step 1: 编写调拨创建与查询测试
  - 创建调拨单默认 PENDING（VP-055）
  - 按单号查询成功（VP-059）
  - 查询不存在返回 404（VP-060）
- [ ] Step 2: 运行红灯验证
  - 执行 `mvn test -Dtest=TransferOrderControllerTest`，确认编译失败
- [ ] Step 3: 实现最小产品代码
  - 创建 `TransferOrder` Entity（`s105_transfer_order`）
  - 创建 `TransferOrderDTO`
  - 创建 `TransferOrderRepository`
  - 创建 `TransferOrderService`（create, get, approve, ship, receive, cancel）
  - 创建 `TransferOrderController`（POST/GET/PUT approve/PUT ship/PUT receive/PUT cancel）
- [ ] Step 4: 运行绿灯验证
  - 执行 `mvn test -Dtest=TransferOrderControllerTest`，确认创建与查询测试通过
- [ ] Step 5: 补充审批/出库/入库/取消/异常测试
  - 审批→APPROVED + 调出方预占（VP-061）
  - 调拨出库→SHIPPED + 调出方扣减（VP-063）
  - 调拨入库→COMPLETED + 调入方增加（VP-065）
  - 取消→恢复预占（VP-067, VP-068）
  - 调出方=调入方拒绝（VP-056）
  - 库存不足拒绝（VP-057）
  - 重复单号失败（VP-058）
  - 非法状态转换拒绝（VP-062, VP-064, VP-066, VP-069, VP-070）
- [ ] Step 6: 运行完整绿灯验证
  - 执行 `mvn test -Dtest=TransferOrderControllerTest`，确认全部通过

## 结果记录

（执行后填写）
