# TASK-002: 库存管理 CRUD

## 覆盖需求

FUNC-013~015, FIELD-008, RULE-016~019, BR-001

## 验证用例

VP-047 ~ VP-054

## 依赖

TASK-001（Product Entity 必须存在）

## 文件计划

| 类型 | 文件路径 |
| --- | --- |
| 产品代码 | `src/main/java/com/example/demo/entity/Inventory.java` |
| 产品代码 | `src/main/java/com/example/demo/dto/InventoryDTO.java` |
| 产品代码 | `src/main/java/com/example/demo/repository/InventoryRepository.java` |
| 产品代码 | `src/main/java/com/example/demo/service/InventoryService.java` |
| 产品代码 | `src/main/java/com/example/demo/controller/InventoryController.java` |
| 测试代码 | `src/test/java/com/example/demo/controller/InventoryControllerTest.java` |

## TDD 步骤

### Step 1: 编写库存查询测试

- 编写 `InventoryControllerTest` 测试分页查询库存列表（VP-047）
- 编写按公司+商品查询库存成功（VP-048）
- 编写查询不存在的库存返回 404（VP-049）

### Step 2: 运行红灯验证

- 执行 `mvn test -Dtest=InventoryControllerTest`，确认编译失败

### Step 3: 实现最小产品代码

- 创建 `Inventory` Entity（`@Table(name="a102_inventory", schema="scash")`，含 inventory_id, company_cd, product_cd, quantity, reserved_quantity + 8 审计字段）
- 创建 `InventoryDTO`
- 创建 `InventoryRepository`（`findByCompanyCdAndProductCd`）
- 创建 `InventoryService`（getList, get, adjust）
- 创建 `InventoryController`（GET list, GET detail, PUT adjust）

### Step 4: 运行绿灯验证

- 执行 `mvn test -Dtest=InventoryControllerTest`，确认查询测试通过

### Step 5: 补充库存调整与边界测试

- 库存调整成功+记录原因（VP-050）
- 库存数量不可为负（VP-051）
- 可用库存计算验证（VP-052）

### Step 6: 运行完整绿灯验证

- 执行 `mvn test -Dtest=InventoryControllerTest`，确认全部通过

## 结果记录

（执行后填写）
