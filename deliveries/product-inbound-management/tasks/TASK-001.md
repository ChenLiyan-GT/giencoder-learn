# TASK-001: 商品主数据 CRUD

## 覆盖需求

FUNC-016, FIELD-004, FIELD-009, RULE-020, NFR-009, BR-001

## 验证用例

VP-001 ~ VP-009

## 文件计划

| 类型 | 文件路径 |
| --- | --- |
| 产品代码 | `src/main/java/com/example/demo/entity/Product.java` |
| 产品代码 | `src/main/java/com/example/demo/dto/ProductDTO.java` |
| 产品代码 | `src/main/java/com/example/demo/repository/ProductRepository.java` |
| 产品代码 | `src/main/java/com/example/demo/service/ProductService.java` |
| 产品代码 | `src/main/java/com/example/demo/controller/ProductController.java` |
| 测试代码 | `src/test/java/com/example/demo/controller/ProductControllerTest.java` |

## TDD 步骤

### Step 1: 编写 Product Entity + Repository + DTO 的测试

- 编写 `ProductControllerTest` 测试创建商品成功（VP-001）
- 编写查询商品成功（VP-004）
- 编写查询不存在商品返回 404（VP-005）

### Step 2: 运行红灯验证

- 执行 `mvn test -Dtest=ProductControllerTest`，确认编译失败（Entity/Repository/Service/Controller 不存在）

### Step 3: 实现最小产品代码

- 创建 `Product` Entity（`@Table(name="a101_product", schema="scash")`，含 product_id, product_cd(unique), product_nm_kanji, product_nm_kana, unit_cd + 8 审计字段）
- 创建 `ProductDTO`
- 创建 `ProductRepository`（`findByProductCd`）
- 创建 `ProductService`（create, get, update, delete）
- 创建 `ProductController`（POST/GET/PUT/DELETE）

### Step 4: 运行绿灯验证

- 执行 `mvn test -Dtest=ProductControllerTest`，确认全部通过

### Step 5: 补充异常场景测试

- 重复商品代码创建失败（VP-003）
- 更新商品成功（VP-006）
- 逻辑删除商品（VP-007）

### Step 6: 运行完整绿灯验证

- 执行 `mvn test -Dtest=ProductControllerTest`，确认全部通过

## 结果记录

（执行后填写）
