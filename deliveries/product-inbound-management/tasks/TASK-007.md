# TASK-007: 跨模块集成流程

## 覆盖需求

INT-001~005, FLOW-001~004, BR-001

## 验证用例

VP-073 ~ VP-077

## 依赖

TASK-001 ~ TASK-006（所有模块必须完成）

## 文件计划

| 类型 | 文件路径 |
| --- | --- |
| 测试代码 | `src/test/java/com/example/demo/controller/IntegrationFlowTest.java` |

## TDD 步骤

### Step 1: 编写入库→出库全流程集成测试

- 创建商品→创建入库单→确认入库→创建出库单→确认出库（VP-073）
- 每步验证库存数量正确增减

### Step 2: 运行红灯验证

- 执行 `mvn test -Dtest=IntegrationFlowTest`，确认测试失败（功能未集成或集成链路不完整）

### Step 3: 修复集成链路问题

- 检查各 Service 间调用是否正确
- 确保事务边界正确
- 确保库存预占/扣减逻辑在跨模块场景下正确

### Step 4: 运行绿灯验证

- 执行 `mvn test -Dtest=IntegrationFlowTest`，确认全流程测试通过

### Step 5: 补充拣货→出库状态联动 + 调拨全流程测试

- 拣货完成→出库单状态自动变更为 PICKED（VP-074）
- 调拨全流程：创建→审批→出库→入库（VP-075, VP-077）
- 公司代码一致性验证（VP-076）

### Step 6: 运行完整绿灯验证

- 执行 `mvn test -Dtest=IntegrationFlowTest`，确认全部通过

## 结果记录

（执行后填写）
