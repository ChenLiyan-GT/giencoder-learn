# TASK-006: 仓库调拨机能

## 覆盖需求

FUNC-017~022, FIELD-010, RULE-021~026, FLOW-004, INT-006, INT-007, BR-001

## 验证用例

VP-055 ~ VP-072

## 依赖

TASK-001（Product Entity）✅ 已完成
TASK-002（Inventory Entity & Service）✅ 已完成

## 文件计划

| 类型 | 文件路径 | 实际路径 |
| --- | --- | --- |
| 产品代码 | `src/main/java/com/common/entity/TransferOrder.java` | `src/main/java/com/common/entity/TransferOrder.java` ✅ |
| 产品代码 | `src/main/java/com/gc/transfer/dto/TransferOrderDTO.java` | `src/main/java/com/gc/transfer/dto/TransferOrderDTO.java` ✅ |
| 产品代码 | `src/main/java/com/gc/transfer/repository/TransferOrderRepository.java` | `src/main/java/com/gc/transfer/repository/TransferOrderRepository.java` ✅ |
| 产品代码 | `src/main/java/com/gc/transfer/service/TransferOrderService.java` | `src/main/java/com/gc/transfer/service/TransferOrderService.java` ✅ |
| 产品代码 | `src/main/java/com/gc/transfer/controller/TransferOrderController.java` | `src/main/java/com/gc/transfer/controller/TransferOrderController.java` ✅ |
| 测试代码 | `src/test/java/com/gc/transfer/controller/TransferOrderControllerTest.java` | `src/test/java/com/gc/transfer/controller/TransferOrderControllerTest.java` ✅ |

## TDD 步骤

- [x] Step 1: 编写调拨创建与查询测试
  - 创建调拨单默认 PENDING（VP-055）
  - 按单号查询成功（VP-059）
  - 查询不存在返回 404（VP-060）
- [x] Step 2: 运行红灯验证
  - 执行 `mvn test -Dtest=TransferOrderControllerTest`，确认编译失败
- [x] Step 3: 实现最小产品代码
  - 创建 `TransferOrder` Entity（`s105_transfer_order`）
  - 创建 `TransferOrderDTO`
  - 创建 `TransferOrderRepository`
  - 创建 `TransferOrderService`（create, get, approve, ship, receive, cancel）
  - 创建 `TransferOrderController`（POST/GET/PUT approve/PUT ship/PUT receive/PUT cancel）
- [x] Step 4: 运行绿灯验证
  - 执行 `mvn test -Dtest=TransferOrderControllerTest`，确认创建与查询测试通过
- [x] Step 5: 补充审批/出库/入库/取消/异常测试
  - 审批→APPROVED + 调出方预占（VP-061）
  - 调拨出库→SHIPPED + 调出方扣减（VP-063）
  - 调拨入库→COMPLETED + 调入方增加（VP-065）
  - 取消→恢复预占（VP-067, VP-068）
  - 调出方=调入方拒绝（VP-056）
  - 库存不足拒绝（VP-057）
  - 重复单号失败（VP-058）
  - 非法状态转换拒绝（VP-062, VP-064, VP-066, VP-069, VP-070）
- [x] Step 6: 运行完整绿灯验证
  - 执行 `mvn test -Dtest=TransferOrderControllerTest`，确认全部通过

## 结果记录

**Step 6 绿灯验证（2026-07-16）:**
- 测试结果：Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
- 覆盖 VP-055~VP-070（VP-071/VP-072 为代码审查项）
- 验证命令：`mvn test -Dtest=TransferOrderControllerTest`

**VP-071 代码审查：**
- ✅ TransferOrder Entity 含 transfer_order_id, transfer_order_no, from_company_cd, to_company_cd, product_cd, quantity, transferred_quantity, status + 8 审计字段

**VP-072 代码审查：**
- ✅ @Table(name="s105_transfer_order", schema="scash")

**TASK-006 完成状态：**
- ✅ 16/16 测试通过
- ✅ VP-055~VP-072 全部覆盖
- ✅ 调拨单创建、查询、审批、出库、入库、取消功能全部实现
