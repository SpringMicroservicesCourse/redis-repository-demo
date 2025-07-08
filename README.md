# Spring Boot Redis Repository Demo ⚡

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-latest-red.svg)](https://redis.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 專案介紹

本專案是一個基於Spring Boot的Redis Repository示範應用程式，主要展示如何在Spring Boot應用中整合Redis作為快取層，實現高效能的資料存取模式。專案以咖啡訂單系統為例，展示了關聯式資料庫（H2）與NoSQL快取（Redis）的混合使用架構。

### 核心功能
- 🔍 **智慧快取機制**：首次查詢從資料庫獲取，後續查詢直接從Redis快取提取
- 🗄️ **混合資料存儲**：結合JPA（關聯式資料庫）與Redis（NoSQL快取）
- ⚡ **效能最佳化**：透過Redis快取大幅提升查詢效能
- 🔄 **自動資料轉換**：實現JPA實體與Redis快取物件的無縫轉換

### 解決的問題
- 資料庫查詢效能瓶頸
- 高併發場景下的回應時間優化
- 複雜查詢的結果快取策略

> 💡 **為什麼選擇此專案？**
> - 📚 學習Spring Data Redis的最佳實務
> - 🏗️ 了解現代微服務架構中的快取策略
> - 🔧 掌握循環依賴的解決方案

### 🎯 專案特色

- **無循環依賴設計**：採用配置分離與延遲注入，符合Spring Boot 3.x最佳實務
- **型別安全的金額處理**：整合Joda Money進行精確的貨幣計算
- **完整的實體映射**：自定義轉換器處理複雜物件的序列化與反序列化
- **企業級架構模式**：清晰的分層架構與職責分離

## 技術棧

### 核心框架
- **Spring Boot 3.4.5** - 主要應用程式框架，提供自動配置與依賴注入
- **Spring Data JPA** - 資料持久層框架，處理關聯式資料庫操作
- **Spring Data Redis** - Redis整合框架，提供Repository模式的快取操作
- **Hibernate 6.6.13** - JPA實作，負責ORM映射與SQL生成

### 資料庫與快取
- **H2 Database** - 輕量級記憶體資料庫，適合開發與測試環境
- **Redis** - 高效能記憶體快取資料庫，支援多種資料結構
- **Lettuce** - 非同步Redis客戶端，提供連線池與高併發支援

### 開發工具與輔助
- **Lombok** - 減少樣板程式碼，自動生成getter/setter/builder等方法
- **Joda Money** - 專業的貨幣處理函式庫，確保金額計算的精確性
- **Maven** - 專案建構與依賴管理工具
- **Docker** - 容器化Redis服務，簡化開發環境設定

## 專案結構

```
redis-repository-demo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── tw/fengqing/spring/springbucks/
│   │   │       ├── config/          # 配置類別
│   │   │       │   └── RedisConfig.java
│   │   │       ├── converter/       # 自定義轉換器
│   │   │       │   ├── BytesToMoneyConverter.java
│   │   │       │   ├── MoneyConverter.java
│   │   │       │   └── MoneyToBytesConverter.java
│   │   │       ├── model/           # 資料模型
│   │   │       │   ├── BaseEntity.java
│   │   │       │   ├── Coffee.java
│   │   │       │   ├── CoffeeCache.java
│   │   │       │   ├── CoffeeOrder.java
│   │   │       │   └── OrderState.java
│   │   │       ├── repository/      # 資料存取層
│   │   │       │   ├── CoffeeCacheRepository.java
│   │   │       │   ├── CoffeeOrderRepository.java
│   │   │       │   └── CoffeeRepository.java
│   │   │       ├── service/         # 業務邏輯層
│   │   │       │   ├── CoffeeOrderService.java
│   │   │       │   └── CoffeeService.java
│   │   │       └── SpringBucksApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── data.sql            # 初始化資料
│   │       └── schema.sql          # 資料庫架構
│   └── test/
├── pom.xml
└── README.md
```

## 快速開始

### 前置需求
- ☕ **Java 21** 或更高版本
- 🐳 **Docker** (用於執行Redis服務)
- 📦 **Maven 3.6+** (用於專案建構)

### 安裝與執行

1. **克隆此倉庫：**
```bash
git clone https://github.com/SpringMicroservicesCourse/redis-repository-demo.git
```

2. **進入專案目錄：**
```bash
cd redis-repository-demo
```

3. **啟動Redis服務：**
```bash
# 使用Docker啟動Redis
docker run -d --name redis-demo -p 6379:6379 redis:latest

# 驗證Redis服務狀態
docker exec redis-demo redis-cli ping
# 預期回應：PONG
```

4. **編譯專案：**
```bash
mvn clean compile
```

5. **執行應用程式：**
```bash
mvn spring-boot:run
```

### 🎉 執行結果

當應用程式成功啟動後，您會看到如下的快取演示效果：

```
# 第一次查詢 - 從資料庫獲取
Coffee Found: Optional[Coffee(name=mocha, price=TWD 150.00)]
Save Coffee CoffeeCache(id=4, name=mocha, price=TWD 150.00) to cache.

# 後續查詢 - 從Redis快取獲取
Coffee CoffeeCache(id=4, name=mocha, price=TWD 150.00) found in cache.
```

## 進階說明

### 環境變數
```properties
# Redis連線設定
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your-password

# 資料庫設定
H2_DB_PATH=./data/springbucks
```

### 設定檔說明
```properties
# application.properties 主要設定

# JPA/Hibernate設定
spring.jpa.hibernate.ddl-auto=none           # 不自動更新資料庫架構
spring.jpa.properties.hibernate.show_sql=true    # 顯示SQL語句
spring.jpa.properties.hibernate.format_sql=true  # 格式化SQL輸出

# Spring Boot管理端點
management.endpoints.web.exposure.include=*      # 開放所有監控端點

# Redis連線設定
spring.redis.host=localhost                       # Redis伺服器位址
spring.redis.lettuce.pool.maxActive=5           # 連線池最大活躍連線數
spring.redis.lettuce.pool.maxIdle=5             # 連線池最大閒置連線數
```

### 核心架構說明

#### 🔄 快取策略實作

```java
/**
 * 智慧快取查詢方法
 * 1. 先檢查Redis快取
 * 2. 快取未命中時查詢資料庫
 * 3. 將查詢結果存入快取
 */
public Optional<Coffee> findSimpleCoffeeFromCache(String name) {
    // 第一步：嘗試從Redis快取獲取
    Optional<CoffeeCache> cached = cacheRepository.findOneByName(name);
    
    if (cached.isPresent()) {
        // 快取命中：直接返回快取結果
        CoffeeCache coffeeCache = cached.get();
        Coffee coffee = Coffee.builder()
                .name(coffeeCache.getName())
                .price(coffeeCache.getPrice())
                .build();
        log.info("Coffee {} found in cache.", coffeeCache);
        return Optional.of(coffee);
    } else {
        // 快取未命中：查詢資料庫並更新快取
        Optional<Coffee> raw = findOneCoffee(name);
        raw.ifPresent(c -> {
            CoffeeCache coffeeCache = CoffeeCache.builder()
                    .id(c.getId())
                    .name(c.getName())
                    .price(c.getPrice())
                    .build();
            log.info("Save Coffee {} to cache.", coffeeCache);
            cacheRepository.save(coffeeCache);
        });
        return raw;
    }
}
```

#### 🔧 循環依賴解決方案

本專案採用**配置分離 + 延遲注入**的方式解決Spring Boot 3.x的循環依賴問題：

```java
// 獨立的Redis配置類
@Configuration
@EnableRedisRepositories(basePackages = "tw.fengqing.spring.springbucks.repository")
public class RedisConfig {
    // Redis相關配置與Bean定義
}

// 主應用類使用延遲注入
@SpringBootApplication
public class SpringBucksApplication {
    @Autowired
    @Lazy  // 延遲初始化，避免循環依賴
    private CoffeeService coffeeService;
}
```

## 參考資源

- [Spring Data Redis 官方文件](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)
- [Redis 官方文件](https://redis.io/documentation)
- [Spring Boot 3.x 升級指南](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Joda Money 使用指南](https://www.joda.org/joda-money/)

## 注意事項與最佳實踐

### ⚠️ 重要提醒

| 項目 | 說明 | 建議做法 |
|------|------|----------|
| 循環依賴 | Spring Boot 3.x 預設禁止循環依賴 | 使用@Lazy注解或配置分離 |
| 金額精度 | 使用float/double會有精度問題 | 採用Joda Money或BigDecimal |
| 快取一致性 | Redis與資料庫資料可能不一致 | 實作適當的快取更新策略 |
| 連線管理 | Redis連線數限制 | 合理配置連線池參數 |

### 🔒 最佳實踐指南

- **快取策略**：根據業務場景選擇適當的TTL（生存時間）設定
- **錯誤處理**：實作Redis連線失敗時的降級機制
- **監控告警**：建立快取命中率與效能監控指標
- **資料一致性**：考慮使用分散式鎖確保資料一致性
- **安全性**：生產環境務必設定Redis密碼與網路隔離

### 📊 效能優化建議

```java
// 批次操作範例
@Service
public class CoffeeService {
    
    /**
     * 批次查詢快取，減少網路往返次數
     */
    public Map<String, Coffee> batchFindFromCache(List<String> names) {
        // 使用MultiGet減少Redis查詢次數
        List<CoffeeCache> cached = cacheRepository.findByNameIn(names);
        
        // 轉換為Map以提升查詢效率
        return cached.stream()
                .collect(Collectors.toMap(
                    CoffeeCache::getName,
                    this::convertToEntity
                ));
    }
}
```

## 授權說明

本專案採用 MIT 授權條款，詳見 LICENSE 檔案。

## 關於我們

我們主要專注在敏捷專案管理、物聯網（IoT）應用開發和領域驅動設計（DDD）。喜歡把先進技術和實務經驗結合，打造好用又靈活的軟體解決方案。

## 聯繫我們

- **FB 粉絲頁**：[風清雲談 | Facebook](https://www.facebook.com/profile.php?id=61576838896062)
- **LinkedIn**：[linkedin.com/in/chu-kuo-lung](https://www.linkedin.com/in/chu-kuo-lung)
- **YouTube 頻道**：[雲談風清 - YouTube](https://www.youtube.com/channel/UCXDqLTdCMiCJ1j8xGRfwEig)
- **風清雲談 部落格**：[風清雲談](https://blog.fengqing.tw/)
- **電子郵件**：[fengqing.tw@gmail.com](mailto:fengqing.tw@gmail.com)

---

**📅 最後更新：2025年6月30日**  
**👨‍💻 維護者：風清雲談團隊** 