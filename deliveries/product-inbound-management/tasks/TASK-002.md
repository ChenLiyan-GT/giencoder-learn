# TASK-002: 库存管理 CRUD

## 覆盖需求

FUNC-013~015, FIELD-008, RULE-016~019, BR-001

## 验证用例

VP-047 ~ VP-052

## 依赖

TASK-001（Product Entity 必须存在）✅ 已完成

## 文件计划

| 类型 | 文件路径 | 实际路径 |
| --- | --- | --- |
| 产品代码 | `src/main/java/com/example/demo/entity/Inventory.java` | `src/main/java/com/common/entity/Inventory.java` |
| 产品代码 | `src/main/java/com/example/demo/dto/InventoryDTO.java` | `src/main/java/com/gc/inventory/dto/InventoryDTO.java` |
| 产品代码 | `src/main/java/com/example/demo/repository/InventoryRepository.java` | `src/main/java/com/gc/inventory/repository/InventoryRepository.java` |
| 产品代码 | `src/main/java/com/example/demo/service/InventoryService.java` | `src/main/java/com/gc/inventory/service/InventoryService.java` |
| 产品代码 | `src/main/java/com/example/demo/controller/InventoryController.java` | `src/main/java/com/gc/inventory/controller/InventoryController.java` |
| 测试代码 | `src/test/java/com/example/demo/controller/InventoryControllerTest.java` | `src/test/java/com/gc/inventory/controller/InventoryControllerTest.java` |

## TDD 步骤

- [x] Step 1: 编写库存查询测试
  - 证据：已创建 InventoryControllerTest，包含 VP-047、VP-048、VP-049 测试用例
- [x] Step 2: 运行红灯验证
  - 证据：执行 `mvn test -Dtest=InventoryControllerTest` 确认编译失败（类不存在）
- [x] Step 3: 实现最小产品代码
  - 证据：已创建 Inventory Entity (com.common.entity.Inventory)、InventoryDTO、InventoryRepository、InventoryService、InventoryController
- [x] Step 4: 运行绿灯验证
  - 证据：执行 `mvn test -Dtest=InventoryControllerTest` 确认查询测试通过
- [x] Step 5: 补充库存调整与边界测试
  - 证据：已添加 VP-050（库存调整成功）、VP-051（数量不可为负）、VP-052（可用库存计算）测试用例
- [x] Step 6: 运行完整绿灯验证
  - 证据：执行 `mvn test -Dtest=InventoryControllerTest` 确认全部通过（6/6）

## 结果记录

```yaml
changed_files:
  - src/main/java/com/common/entity/Inventory.java
  - src/main/java/com/gc/inventory/dto/InventoryDTO.java
  - src/main/java/com/gc/inventory/repository/InventoryRepository.java
  - src/main/java/com/gc/inventory/service/InventoryService.java
  - src/main/java/com/gc/inventory/controller/InventoryController.java
  - src/test/java/com/gc/inventory/controller/InventoryControllerTest.java
  - src/test/resources/schema.sql
tests_added_or_changed:
  - src/test/java/com/gc/inventory/controller/InventoryControllerTest.java
commands_run:
  - cwd: D:/giencoder/giencoder-learn_github
    command: mvn clean test -Dtest=InventoryControllerTest
    exit_code: 0
    result: passed
    output_summary: Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
tdd_red_result:
  cwd: D:/giencoder/giencoder-learn_github
  command: mvn test -Dtest=InventoryControllerTest
  exit_code: 1
  result: failed
  output_summary: 编译失败，Inventory 相关类不存在
  expected_failure_reason: Inventory Entity、Repository、Service、Controller 类未实现
tdd_green_result:
  cwd: D:/giencoder/giencoder-learn_github
  command: mvn test -Dtest=InventoryControllerTest
  exit_code: 0
  result: passed
  output_summary: Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
additional_checks:
  - mvn clean compile: BUILD SUCCESS
  - schema.sql: 添加 a102_inventory 表定义
requirement_ids_covered:
  - FUNC-013
  - FUNC-014
  - FUNC-015
  - FIELD-008
  - RULE-016
  - RULE-017
  - RULE-018
  - RULE-019
  - BR-001
user_stories_covered:
  - US-004 (库存管理)
remaining_risks: []
```

## 完成状态

✅ **TASK-002 已完成**
- 所有 TDD 步骤已完成并有证据
- 所有测试用例通过（6/6）
- 代码已实现并验证
- API 端点：
  - `GET /api/inventory?page=0&size=10` - 分页查询库存列表
  - `GET /api/inventory/{companyCd}/{productCd}` - 查询指定公司商品的库存
  - `PUT /api/inventory/{companyCd}/{productCd}/adjust` - 调整库存数量
