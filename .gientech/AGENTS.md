# AGENTS.md — 商品入库/拣货/出库/调拨/管理机能开发指南

## 1. 项目定位

本项目基于 Spring Boot 3.4.4 + Java 25 + JPA + PostgreSQL，实现仓储管理的核心机能：
- **商品主数据 (Product)**: 商品信息的创建、查询、更新、删除
- **入库 (Inbound)**: 商品入库登记、入库单管理、库存增加
- **拣货 (Picking)**: 拣货单创建、拣货任务分配、拣货确认
- **出库 (Outbound)**: 出库单管理、库存扣减、出库确认
- **调拨 (Transfer)**: 跨仓库调拨单创建、审批、调出、调入、取消
- **管理 (Management)**: 库存查询、库存调整

以现有 `Company` 模块为代码风格样本，全项目统一遵循本指南。

---

## 2. 技术栈

| 项目 | 版本/说明 |
|------|-----------|
| 框架 | Spring Boot 3.4.4 |
| JDK | 25 (Amazon Corretto) |
| 构建工具 | Maven |
| 数据库 | PostgreSQL |
| ORM | Spring Data JPA |
| 测试框架 | JUnit 5 + Spring Boot Test |
| 覆盖率工具 | JaCoCo (指令/分支覆盖率 100%) |
| 变异测试 | Pitest (变异/覆盖率阈值 100%) |

---

## 3. 包结构规范

```
src/main/java/com/example/demo/
├── controller/          # HTTP 请求处理层
├── service/             # 业务逻辑层
├── repository/          # 数据访问层 (JPA Repository)
├── entity/              # JPA 实体类 (对应数据库表)
├── dto/                 # 数据传输对象
└── exception/           # 自定义异常 (可选)
```

### 命名规则

| 层级 | 命名模式 | 示例 |
|------|----------|------|
| Entity | `<业务名词>` | `Product`, `InboundOrder`, `PickingOrder`, `OutboundOrder`, `TransferOrder`, `Inventory` |
| DTO | `<业务名词>DTO` | `ProductDTO`, `InboundOrderDTO`, `TransferOrderDTO` |
| Repository | `<业务名词>Repository` | `ProductRepository`, `InboundOrderRepository` |
| Service | `<业务名词>Service` | `ProductService`, `InboundOrderService` |
| Controller | `<业务名词>Controller` | `ProductController`, `InboundOrderController` |

---

## 4. 各机能模块开发规范

### 4.0 商品主数据管理机能 (Product)

#### 实体设计要点
- 表名: `scash.a101_product`（主数据表，`a` 开头）
- 必须包含审计字段: `created_ts`, `created_user_cd`, `updated_ts`, `updated_user_cd`, `version`, `deleted_flag`
- 主键使用 `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- `product_cd` 设置 `unique` 约束

```java
@Entity
@Table(name = "a101_product", schema = "scash")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_cd", nullable = false, unique = true, length = 20)
    private String productCd;

    @Column(name = "product_nm_kanji", nullable = false, length = 40)
    private String productNmKanji;

    @Column(name = "product_nm_kana", length = 40)
    private String productNmKana;

    @Column(name = "unit_cd", length = 10)
    private String unitCd;

    // 审计字段...
}
```

#### API 端点
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/products` | 创建商品 |
| GET | `/api/products/{productCd}` | 按商品代码查询 |
| PUT | `/api/products/{productCd}` | 更新商品 |
| DELETE | `/api/products/{productCd}` | 删除商品（逻辑删除） |

#### 业务规则
- 创建时 `product_cd` 全局唯一，重复创建失败
- 删除操作使用逻辑删除（`deleted_flag = '1'`）
- 查询时自动过滤已删除的商品

### 4.1 入库机能 (Inbound)

#### 实体设计要点
- 表名: `scash.s101_inbound_order`（交易表，`s` 开头）
- 必须包含审计字段: `created_ts`, `created_user_cd`, `updated_ts`, `updated_user_cd`, `version`, `deleted_flag`
- 主键使用 `@GeneratedValue(strategy = GenerationType.IDENTITY)`

```java
@Entity
@Table(name = "s101_inbound_order", schema = "scash")
public class InboundOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inbound_order_id")
    private Long inboundOrderId;

    @Column(name = "inbound_order_no", nullable = false, unique = true, length = 20)
    private String inboundOrderNo;

    @Column(name = "company_cd", nullable = false, length = 20)
    private String companyCd;

    @Column(name = "product_cd", nullable = false, length = 20)
    private String productCd;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "status", nullable = false, length = 10)
    private String status;  // RECEIVED, CONFIRMED, REJECTED

    // 审计字段...
}
```

#### API 端点
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/inbound-orders` | 创建入库单 |
| GET | `/api/inbound-orders/{inboundOrderNo}` | 查询入库单 |
| PUT | `/api/inbound-orders/{inboundOrderNo}/confirm` | 确认入库 (库存增加) |
| PUT | `/api/inbound-orders/{inboundOrderNo}/reject` | 拒绝入库 |

#### 业务规则
- 创建入库单时，状态初始为 `RECEIVED`
- 确认入库时，同步增加对应商品的库存数量
- 拒绝入库时，状态变更为 `REJECTED`，不增加库存
- 已确认或已拒绝的入库单不可再次操作

### 4.2 拣货机能 (Picking)

#### 实体设计要点

```java
@Entity
@Table(name = "s102_picking_order", schema = "scash")
public class PickingOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "picking_order_id")
    private Long pickingOrderId;

    @Column(name = "picking_order_no", nullable = false, unique = true, length = 20)
    private String pickingOrderNo;

    @Column(name = "outbound_order_no", nullable = false, length = 20)
    private String outboundOrderNo;

    @Column(name = "product_cd", nullable = false, length = 20)
    private String productCd;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "picked_quantity")
    private Integer pickedQuantity;

    @Column(name = "status", nullable = false, length = 10)
    private String status;  // PENDING, PICKING, COMPLETED, CANCELLED

    // 审计字段...
}
```

#### API 端点
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/picking-orders` | 创建拣货单 |
| GET | `/api/picking-orders/{pickingOrderNo}` | 查询拣货单 |
| PUT | `/api/picking-orders/{pickingOrderNo}/pick` | 执行拣货 |
| PUT | `/api/picking-orders/{pickingOrderNo}/complete` | 完成拣货 |

#### 业务规则
- 拣货单关联出库单，由出库单驱动生成
- 拣货时校验库存是否充足
- `picked_quantity` 累加记录，支持分批拣货
- 拣货完成后 (`picked_quantity >= quantity`)，状态变更为 `COMPLETED`

### 4.3 出库机能 (Outbound)

#### 实体设计要点

```java
@Entity
@Table(name = "s103_outbound_order", schema = "scash")
public class OutboundOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbound_order_id")
    private Long outboundOrderId;

    @Column(name = "outbound_order_no", nullable = false, unique = true, length = 20)
    private String outboundOrderNo;

    @Column(name = "company_cd", nullable = false, length = 20)
    private String companyCd;

    @Column(name = "product_cd", nullable = false, length = 20)
    private String productCd;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "status", nullable = false, length = 10)
    private String status;  // CREATED, PICKED, SHIPPED, CANCELLED

    // 审计字段...
}
```

#### API 端点
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/outbound-orders` | 创建出库单 |
| GET | `/api/outbound-orders/{outboundOrderNo}` | 查询出库单 |
| PUT | `/api/outbound-orders/{outboundOrderNo}/ship` | 确认出库 (库存扣减) |
| PUT | `/api/outbound-orders/{outboundOrderNo}/cancel` | 取消出库 |

#### 业务规则
- 创建出库单时校验库存充足性
- 出库确认时，同步扣减对应商品的库存数量
- 已出库的订单不可取消
- 取消出库时恢复库存预占

### 4.4 库存管理机能 (Inventory)

#### 实体设计要点

```java
@Entity
@Table(name = "a102_inventory", schema = "scash")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;

    @Column(name = "company_cd", nullable = false, length = 20)
    private String companyCd;

    @Column(name = "product_cd", nullable = false, length = 20)
    private String productCd;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "reserved_quantity")
    private Integer reservedQuantity;

    // 审计字段...
}
```

#### API 端点
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/inventories` | 查询库存列表 (支持分页) |
| GET | `/api/inventories/{companyCd}/{productCd}` | 查询指定商品库存 |
| PUT | `/api/inventories/{companyCd}/{productCd}/adjust` | 库存调整 |

#### 业务规则
- 库存数量 = 实际库存，不可为负数
- `reserved_quantity` 记录被出库单/调拨单预占的数量
- 可用库存 = `quantity - reserved_quantity`
- 库存调整需记录调整原因和调整前后的数量

### 4.5 仓库调拨机能 (Transfer)

#### 实体设计要点
- 表名: `scash.s105_transfer_order`（交易表，`s` 开头）
- 必须包含审计字段: `created_ts`, `created_user_cd`, `updated_ts`, `updated_user_cd`, `version`, `deleted_flag`
- 主键使用 `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- `transfer_order_no` 设置 `unique` 约束

```java
@Entity
@Table(name = "s105_transfer_order", schema = "scash")
public class TransferOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_order_id")
    private Long transferOrderId;

    @Column(name = "transfer_order_no", nullable = false, unique = true, length = 20)
    private String transferOrderNo;

    @Column(name = "from_company_cd", nullable = false, length = 20)
    private String fromCompanyCd;

    @Column(name = "to_company_cd", nullable = false, length = 20)
    private String toCompanyCd;

    @Column(name = "product_cd", nullable = false, length = 20)
    private String productCd;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "transferred_quantity")
    private Integer transferredQuantity;

    @Column(name = "status", nullable = false, length = 10)
    private String status;  // PENDING, APPROVED, SHIPPED, COMPLETED, CANCELLED

    // 审计字段...
}
```

#### API 端点
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/transfer-orders` | 创建调拨单 |
| GET | `/api/transfer-orders/{transferOrderNo}` | 查询调拨单 |
| PUT | `/api/transfer-orders/{transferOrderNo}/approve` | 审批调拨单 |
| PUT | `/api/transfer-orders/{transferOrderNo}/ship` | 确认调拨出库 |
| PUT | `/api/transfer-orders/{transferOrderNo}/receive` | 确认调拨入库 |
| PUT | `/api/transfer-orders/{transferOrderNo}/cancel` | 取消调拨单 |

#### 业务规则
- 创建时校验调拨单号唯一性、调出方≠调入方、调出方可用库存充足
- 初始状态为 `PENDING`
- 状态流: `PENDING → APPROVED → SHIPPED → COMPLETED`，`PENDING/APPROVED → CANCELLED`
- 审批（APPROVED）时，调出方 `reserved_quantity` 增加（预占）
- 调拨出库（SHIPPED）时，调出方 `quantity` 扣减、`reserved_quantity` 减少
- 调拨入库（COMPLETED）时，调入方 `quantity` 增加
- 取消（CANCELLED）时，调出方 `reserved_quantity` 恢复
- SHIPPED / COMPLETED 状态的调拨单不可取消

---

## 5. 代码编写规范

### 5.1 Controller 层

- 使用构造器注入 (禁止 `@Autowired` 字段注入)
- 仅处理 HTTP 请求/响应，不包含业务逻辑
- 返回统一的 HTTP 状态码

```java
@RestController
@RequestMapping("/api/inbound-orders")
public class InboundOrderController {

    private final InboundOrderService inboundOrderService;

    public InboundOrderController(InboundOrderService inboundOrderService) {
        this.inboundOrderService = inboundOrderService;
    }

    @PostMapping
    public ResponseEntity<InboundOrderDTO> createInboundOrder(@RequestBody InboundOrderDTO dto) {
        InboundOrderDTO created = inboundOrderService.createInboundOrder(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{inboundOrderNo}")
    public ResponseEntity<InboundOrderDTO> getInboundOrder(@PathVariable String inboundOrderNo) {
        InboundOrderDTO result = inboundOrderService.getInboundOrderByNo(inboundOrderNo);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
}
```

### 5.2 Service 层

- 包含核心业务逻辑和事务管理
- Entity 与 DTO 之间使用 `BeanUtils.copyProperties` 转换
- 查询方法添加 `@Transactional(readOnly = true)`

```java
@Service
@Transactional
public class InboundOrderService {

    private final InboundOrderRepository inboundOrderRepository;
    private final InventoryRepository inventoryRepository;

    public InboundOrderService(InboundOrderRepository inboundOrderRepository,
                               InventoryRepository inventoryRepository) {
        this.inboundOrderRepository = inboundOrderRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public InboundOrderDTO getInboundOrderByNo(String inboundOrderNo) {
        Optional<InboundOrder> order = inboundOrderRepository.findByInboundOrderNo(inboundOrderNo);
        return order.map(this::convertToDTO).orElse(null);
    }

    public InboundOrderDTO createInboundOrder(InboundOrderDTO dto) {
        InboundOrder order = new InboundOrder();
        BeanUtils.copyProperties(dto, order);
        if (order.getStatus() == null) {
            order.setStatus("RECEIVED");
        }
        if (order.getDeletedFlag() == null) {
            order.setDeletedFlag("0");
        }
        InboundOrder saved = inboundOrderRepository.save(order);
        return convertToDTO(saved);
    }

    public InboundOrderDTO confirmInboundOrder(String inboundOrderNo) {
        InboundOrder order = inboundOrderRepository.findByInboundOrderNo(inboundOrderNo)
            .orElseThrow(() -> new ResourceNotFoundException("入库单不存在"));

        if (!"RECEIVED".equals(order.getStatus())) {
            throw new IllegalStateException("仅接收状态的入库单可以确认");
        }

        order.setStatus("CONFIRMED");
        InboundOrder saved = inboundOrderRepository.save(order);

        // 增加库存
        Inventory inventory = inventoryRepository.findByCompanyCdAndProductCd(
                order.getCompanyCd(), order.getProductCd())
            .orElseGet(() -> createNewInventory(order));
        inventory.setQuantity(inventory.getQuantity() + order.getQuantity());
        inventoryRepository.save(inventory);

        return convertToDTO(saved);
    }

    private Inventory createNewInventory(InboundOrder order) {
        Inventory inventory = new Inventory();
        inventory.setCompanyCd(order.getCompanyCd());
        inventory.setProductCd(order.getProductCd());
        inventory.setQuantity(0);
        inventory.setReservedQuantity(0);
        return inventory;
    }

    private InboundOrderDTO convertToDTO(InboundOrder entity) {
        InboundOrderDTO dto = new InboundOrderDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
```

### 5.3 Repository 层

- 继承 `JpaRepository<Entity, Long>`
- 使用 Spring Data 方法命名规范定义查询
- 复杂查询使用 `@Query` (JPQL，禁止原生 SQL 拼接)

```java
@Repository
public interface InboundOrderRepository extends JpaRepository<InboundOrder, Long> {
    Optional<InboundOrder> findByInboundOrderNo(String inboundOrderNo);
    List<InboundOrder> findByCompanyCdAndStatus(String companyCd, String status);
}
```

### 5.4 Entity 通用字段模板

所有实体必须包含以下审计字段:

```java
@Column(name = "created_ts")
private Instant createdTs;

@Column(name = "created_user_cd", length = 16)
private String createdUserCd;

@Column(name = "created_program", length = 50)
private String createdProgram;

@Column(name = "updated_ts")
private Instant updatedTs;

@Column(name = "updated_user_cd", length = 16)
private String updatedUserCd;

@Column(name = "updated_program", length = 50)
private String updatedProgram;

@Column(name = "version", nullable = false)
private Integer version;

@Column(name = "deleted_flag", nullable = false, length = 1)
private String deletedFlag;
```

---

## 6. 测试规范

### 6.1 测试结构

```
src/test/java/com/example/demo/
├── controller/
│   ├── ProductControllerTest.java
│   ├── InboundOrderControllerTest.java
│   ├── PickingOrderControllerTest.java
│   ├── OutboundOrderControllerTest.java
│   ├── TransferOrderControllerTest.java
│   └── InventoryControllerTest.java
└── service/
    ├── ProductServiceTest.java
    ├── InboundOrderServiceTest.java
    ├── PickingOrderServiceTest.java
    ├── OutboundOrderServiceTest.java
    ├── TransferOrderServiceTest.java
    └── InventoryServiceTest.java
```

### 6.2 测试编写规范

- 使用 `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate` 进行集成测试
- 使用 `@BeforeEach` 准备测试数据，确保每次测试环境一致
- 测试方法命名: `shouldReturn<状态码>When<场景>`
- 使用 `@DisplayName` 添加中文描述
- 使用 `assertAll` 组合多个断言
- 测试数据清理: 在 `@BeforeEach` 中删除并重建

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InboundOrderControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InboundOrderRepository inboundOrderRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/inbound-orders";
        inboundOrderRepository.deleteAll();
        inventoryRepository.deleteAll();
        // 插入测试数据
    }

    @Test
    @DisplayName("createInboundOrder: 创建入库单成功时返回201")
    void shouldReturn201WhenCreateInboundOrderSucceeds() {
        InboundOrderDTO request = new InboundOrderDTO();
        request.setInboundOrderNo("IN001");
        request.setCompanyCd("C001");
        request.setProductCd("P001");
        request.setQuantity(100);

        ResponseEntity<InboundOrderDTO> response = restTemplate.postForEntity(
            baseUrl, request, InboundOrderDTO.class);

        assertAll(
            () -> assertEquals(HttpStatus.CREATED, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("IN001", response.getBody().getInboundOrderNo()),
            () -> assertEquals("RECEIVED", response.getBody().getStatus())
        );
    }

    @Test
    @DisplayName("confirmInboundOrder: 确认入库成功时库存增加")
    void shouldIncreaseInventoryWhenConfirmInboundOrder() {
        // given: 创建入库单
        InboundOrderDTO request = new InboundOrderDTO();
        request.setInboundOrderNo("IN002");
        request.setCompanyCd("C001");
        request.setProductCd("P001");
        request.setQuantity(50);
        restTemplate.postForEntity(baseUrl, request, InboundOrderDTO.class);

        // when: 确认入库
        HttpEntity<Void> entity = new HttpEntity<>(null);
        restTemplate.exchange(baseUrl + "/IN002/confirm", HttpMethod.PUT, entity, Void.class);

        // then: 库存增加
        Inventory inventory = inventoryRepository
            .findByCompanyCdAndProductCd("C001", "P001").orElse(null);
        assertNotNull(inventory);
        assertEquals(50, inventory.getQuantity());
    }
}
```

### 6.3 覆盖率要求

| 指标 | 阈值 |
|------|------|
| JaCoCo 指令覆盖率 | 100% |
| JaCoCo 分支覆盖率 | 100% |
| Pitest 变异覆盖率 | 100% |
| Pitest 覆盖率阈值 | 100% |

---

## 7. 数据库设计要点

### 7.1 Schema 命名
- Schema: `scash`
- 主数据表: `scash.a<序号>_<表名>`（如 `a101_product`, `a102_inventory`）
- 交易数据表: `scash.s<序号>_<表名>`（如 `s101_inbound_order`, `s102_picking_order`, `s103_outbound_order`, `s105_transfer_order`）

### 7.2 完整表清单

| 序号 | 表名 | 类型 | 说明 |
|------|------|------|------|
| a101 | `a101_product` | 主数据 | 商品主数据 |
| a102 | `a102_inventory` | 主数据 | 库存表 |
| s101 | `s101_inbound_order` | 交易 | 入库单 |
| s102 | `s102_picking_order` | 交易 | 拣货单 |
| s103 | `s103_outbound_order` | 交易 | 出库单 |
| s105 | `s105_transfer_order` | 交易 | 仓库调拨单 |

### 7.3 必填字段
- 所有表必须包含: `created_ts`, `created_user_cd`, `updated_ts`, `updated_user_cd`, `version`, `deleted_flag`
- 业务主键 (如 `inbound_order_no`) 设置 `unique` 约束
- 外键关联字段 (如 `company_cd`, `product_cd`) 设置 `nullable = false`

### 7.4 状态字段枚举

| 模块 | 状态值 |
|------|--------|
| 入库单 | `RECEIVED`, `CONFIRMED`, `REJECTED` |
| 拣货单 | `PENDING`, `PICKING`, `COMPLETED`, `CANCELLED` |
| 出库单 | `CREATED`, `PICKED`, `SHIPPED`, `CANCELLED` |
| 调拨单 | `PENDING`, `APPROVED`, `SHIPPED`, `COMPLETED`, `CANCELLED` |

---

## 8. 开发流程

### 8.1 分支策略
```
develop
  └── feature/product-master          # 商品主数据管理
  └── feature/inbound-function        # 入库机能
  └── feature/picking-function        # 拣货机能
  └── feature/outbound-function       # 出库机能
  └── feature/transfer-function       # 仓库调拨机能
  └── feature/inventory-management    # 库存管理机能
```

### 8.2 Commit 规范
```
feat(product): 添加商品主数据 CRUD 功能
feat(inbound): 添加入库单创建功能
feat(picking): 添加拣货单确认功能
feat(outbound): 添加出库单发货功能
feat(transfer): 添加调拨单审批功能
feat(inventory): 添加库存调整功能
fix(inbound): 修复入库确认时库存计算错误
```

### 8.3 本地验证
```bash
# 编译
mvn compile

# 运行测试 (含 JaCoCo 覆盖率检查)
mvn test

# 变异测试
mvn org.pitest:pitest-maven:mutationCoverage
```

---

## 9. 开发顺序建议

1. **商品主数据** — 建立商品基础信息表
2. **库存管理** — 实现库存的增删查改
3. **入库机能** — 入库单创建 → 确认入库 → 库存增加
4. **出库机能** — 出库单创建 → 库存预占 → 出库确认 → 库存扣减
5. **拣货机能** — 拣货单创建 → 拣货执行 → 拣货完成
6. **调拨机能** — 调拨单创建 → 审批 → 调出 → 调入 → 完成
7. **集成联调** — 入库→库存→拣货→出库→调拨 全流程验证

---

*最后更新: 2026-07-14*
