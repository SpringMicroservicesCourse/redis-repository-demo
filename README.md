# redis-repository-demo

> Spring Data Redis Repository with @RedisHash and @Indexed for object-oriented Redis operations

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Redis](https://img.shields.io/badge/Redis-latest-red.svg)](https://redis.io/)
[![Spring Data Redis](https://img.shields.io/badge/Spring%20Data%20Redis-3.4.5-blue.svg)](https://spring.io/projects/spring-data-redis)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A comprehensive demonstration of **Spring Data Redis Repository** with `@RedisHash`, automatic secondary indexes with `@Indexed`, custom Money type converters, and object-oriented Redis operations.

## Features

- Spring Data Redis Repository
- `@RedisHash` for entity mapping
- `@Indexed` for automatic secondary indexes
- Custom Money type converters (Reading/Writing)
- CrudRepository interface for CRUD operations
- Automatic TTL management (timeToLive)
- Database-to-cache synchronization (JPA → Redis)
- Type-safe repository operations
- Method naming convention queries
- Lettuce client (non-blocking I/O)
- Circular dependency resolution (@Lazy)

## Tech Stack

- Spring Boot 3.4.5
- Spring Data Redis 3.4.5 (Lettuce client)
- Spring Data JPA
- Java 21
- H2 Database 2.3.232
- Apache Commons Pool2 (connection pool)
- Joda Money 2.0.2
- Lombok
- Maven 3.8+

## Getting Started

### Prerequisites

- JDK 21 or higher
- Maven 3.8+ (or use included Maven Wrapper)
- Docker (for Redis)

### Quick Start

**Step 1: Start Redis**

```bash
# Using Docker (recommended)
docker run -d \
  --name redis-spring-course \
  -p 6379:6379 \
  redis

# Or using Docker Compose
docker-compose up -d
```

**Step 2: Verify Redis**

```bash
# Test Redis connection
docker exec -it redis-spring-course redis-cli ping
# Expected: PONG
```

**Step 3: Run the application**

```bash
./mvnw spring-boot:run
```

## Configuration

### Application Properties

```properties
# JPA/Hibernate configuration
spring.jpa.hibernate.ddl-auto=none
spring.jpa.properties.hibernate.show_sql=true
spring.jpa.properties.hibernate.format_sql=true

# Actuator endpoints
management.endpoints.web.exposure.include=*

# Redis connection
spring.redis.host=localhost

# Lettuce connection pool
spring.redis.lettuce.pool.maxActive=5
spring.redis.lettuce.pool.maxIdle=5
```

### Docker Compose

**docker-compose.yml:**

```yaml
services:
  redis-spring-course:
    image: redis
    container_name: redis-spring-course
    ports:
      - "6379:6379"
    volumes:
      - ./redis-data:/data
```

## Usage

### Application Flow

```
1. Spring Boot starts
   ↓
2. @EnableRedisRepositories activates Redis Repository
   ↓
3. H2 database initialized with schema.sql
   - Creates t_coffee table
   - Inserts 5 coffee records
   ↓
4. ApplicationRunner executes:
   - Query coffee "mocha" (6 times)
   - First query: Database → Cache to Redis (CoffeeCache entity)
   - Next 5 queries: Read from Redis cache (via secondary index)
```

### Code Example

```java
@Slf4j
@SpringBootApplication
@EnableJpaRepositories
public class SpringBucksApplication implements ApplicationRunner {
    
    @Autowired
    private CoffeeService coffeeService;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Query coffee "mocha" 6 times
        Optional<Coffee> c = coffeeService.findSimpleCoffeeFromCache("mocha");
        log.info("Coffee {}", c);
        
        for (int i = 0; i < 5; i++) {
            c = coffeeService.findSimpleCoffeeFromCache("mocha");
        }
        
        log.info("Value from Redis: {}", c);
    }
}
```

### Sample Output

```
Coffee Found: Optional[Coffee(super=BaseEntity(id=4, createTime=2025-10-16 16:52:02.640075, updateTime=2025-10-16 16:52:02.640075), name=mocha, price=TWD 150.00)]
Save Coffee CoffeeCache(id=4, name=mocha, price=TWD 150.00) to cache.

Coffee CoffeeCache(id=4, name=mocha, price=TWD 150.00) found in cache.
Coffee CoffeeCache(id=4, name=mocha, price=TWD 150.00) found in cache.
Coffee CoffeeCache(id=4, name=mocha, price=TWD 150.00) found in cache.
Coffee CoffeeCache(id=4, name=mocha, price=TWD 150.00) found in cache.
Coffee CoffeeCache(id=4, name=mocha, price=TWD 150.00) found in cache.

Value from Redis: Optional[Coffee(super=BaseEntity(id=null, createTime=null, updateTime=null), name=mocha, price=TWD 150.00)]
```

**Output Analysis:**
- **First query**: Database query → Save to Redis as CoffeeCache
- **Next 5 queries**: Cache hit via `findOneByName()` using secondary index
- **Note**: Cached Coffee has no id/timestamps (only essential fields cached)

## Key Components

### CoffeeCache Entity

```java
/**
 * Redis Repository entity
 * Maps to Redis Hash with automatic TTL and secondary indexes
 */
@RedisHash(value = "springbucks-coffee", timeToLive = 60)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoffeeCache {
    
    @Id
    private Long id;              // Primary key
    
    @Indexed
    private String name;          // Secondary index (auto-created)
    
    private Money price;          // Custom converter required
}
```

**Annotation Explanation:**

- **@RedisHash**:
  - `value = "springbucks-coffee"`: Redis Hash name prefix
  - `timeToLive = 60`: Auto-expire after 60 seconds
  
- **@Id**: Primary key field (generates `springbucks-coffee:4`)

- **@Indexed**: Creates secondary index (`springbucks-coffee:name:mocha`)
  - Enables query by name: `findOneByName()`

### CoffeeCacheRepository

```java
/**
 * Redis Repository interface
 * Extends CrudRepository for basic CRUD operations
 */
public interface CoffeeCacheRepository extends CrudRepository<CoffeeCache, Long> {
    
    /**
     * Find by name using secondary index
     * Spring Data auto-implements this method
     */
    Optional<CoffeeCache> findOneByName(String name);
}
```

**Repository Methods:**
- `save(coffeeCache)`: Save to Redis
- `findById(id)`: Find by primary key
- `findOneByName(name)`: Find by secondary index
- `deleteById(id)`: Delete from Redis
- `findAll()`: Find all entries

### Custom Money Converters

**MoneyToBytesConverter (Writing):**

```java
@WritingConverter
public class MoneyToBytesConverter implements Converter<Money, byte[]> {
    
    @Override
    public byte[] convert(@NonNull Money source) {
        String value = Long.toString(source.getAmountMinorLong());
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
```

**BytesToMoneyConverter (Reading):**

```java
@ReadingConverter
public class BytesToMoneyConverter implements Converter<byte[], Money> {
    
    @Override
    public Money convert(@NonNull byte[] source) {
        String value = new String(source, StandardCharsets.UTF_8);
        return Money.ofMinor(CurrencyUnit.of("TWD"), Long.parseLong(value));
    }
}
```

**Conversion Flow:**

```
Java → Redis:
Money(TWD 150.00) → getAmountMinorLong() → 15000L
                  → Long.toString() → "15000"
                  → getBytes(UTF-8) → byte[]
                  → Redis

Redis → Java:
Redis → byte[] → new String(UTF-8) → "15000"
              → Long.parseLong() → 15000L
              → Money.ofMinor() → Money(TWD 150.00)
```

### RedisConfig

```java
@Configuration
@EnableRedisRepositories(basePackages = "tw.fengqing.spring.springbucks.repository")
public class RedisConfig {
    
    /**
     * Lettuce client configuration
     * ReadFrom.MASTER_PREFERRED: Prefer reading from master
     */
    @Bean
    public LettuceClientConfigurationBuilderCustomizer customizer() {
        return builder -> builder.readFrom(ReadFrom.MASTER_PREFERRED);
    }
    
    /**
     * Register custom Money converters
     */
    @Bean
    public RedisCustomConversions redisCustomConversions() {
        return new RedisCustomConversions(
                Arrays.asList(new MoneyToBytesConverter(), new BytesToMoneyConverter()));
    }
}
```

**Important:**
- `@EnableRedisRepositories`: Enable Redis Repository scanning
- `basePackages`: Specify repository package to avoid conflicts with JPA repositories
- `RedisCustomConversions`: Register custom type converters

### CoffeeService

```java
@Service
public class CoffeeService {
    
    @Autowired
    private CoffeeRepository coffeeRepository;  // JPA repository
    
    @Autowired
    private CoffeeCacheRepository cacheRepository;  // Redis repository
    
    /**
     * Find coffee from cache with automatic fallback
 */
public Optional<Coffee> findSimpleCoffeeFromCache(String name) {
        // 1. Check Redis cache using secondary index
    Optional<CoffeeCache> cached = cacheRepository.findOneByName(name);
    
    if (cached.isPresent()) {
            // Cache hit - Convert to Coffee entity
        CoffeeCache coffeeCache = cached.get();
        Coffee coffee = Coffee.builder()
                .name(coffeeCache.getName())
                .price(coffeeCache.getPrice())
                .build();
        log.info("Coffee {} found in cache.", coffeeCache);
        return Optional.of(coffee);
        }
        
        // 2. Cache miss - Query database
        Optional<Coffee> raw = findOneCoffee(name);
        
        // 3. Update cache
        raw.ifPresent(c -> {
            CoffeeCache coffeeCache = CoffeeCache.builder()
                    .id(c.getId())
                    .name(c.getName())
                    .price(c.getPrice())
                    .build();
            log.info("Save Coffee {} to cache.", coffeeCache);
            cacheRepository.save(coffeeCache);
            // TTL automatically set by @RedisHash(timeToLive = 60)
        });
        
        return raw;
    }
}
```

## Redis Data Structure

### Redis Keys Created by Repository

When saving `CoffeeCache(id=4, name=mocha, price=15000)`, Spring Data Redis automatically creates **4 keys**:

```bash
# Connect to Redis
docker exec -it redis-spring-course redis-cli

# View all keys
127.0.0.1:6379> KEYS *
1) "springbucks-coffee"              # Set: All CoffeeCache IDs
2) "springbucks-coffee:4:idx"        # Set: Index collection for ID 4
3) "springbucks-coffee:name:mocha"   # Set: Secondary index by name
4) "springbucks-coffee:4"            # Hash: Full data for ID 4
```

### Data Structure Details

| Redis Key | Type | Purpose | Content |
|-----------|------|---------|---------|
| `springbucks-coffee` | Set | Main index | All CoffeeCache IDs (e.g., "4") |
| `springbucks-coffee:4` | Hash | Main data | Full data (\_class, id, name, price) |
| `springbucks-coffee:name:mocha` | Set | Secondary index | Find ID by name (@Indexed) |
| `springbucks-coffee:4:idx` | Set | Index collection | All indexes for ID 4 |

### View Cache Data

```bash
# 1. View main Set (all IDs)
127.0.0.1:6379> SMEMBERS springbucks-coffee
1) "4"

127.0.0.1:6379> TYPE springbucks-coffee
set

# 2. View main Hash (full data for ID 4)
127.0.0.1:6379> HGETALL springbucks-coffee:4
1) "_class"
2) "tw.fengqing.spring.springbucks.model.CoffeeCache"
3) "id"
4) "4"
5) "name"
6) "mocha"
7) "price"
8) "15000"

127.0.0.1:6379> TYPE springbucks-coffee:4
hash

# 3. View secondary index (find ID by name)
127.0.0.1:6379> SMEMBERS springbucks-coffee:name:mocha
1) "4"

127.0.0.1:6379> TYPE springbucks-coffee:name:mocha
set

# 4. View index collection (all indexes for ID 4)
127.0.0.1:6379> SMEMBERS springbucks-coffee:4:idx
1) "springbucks-coffee:name:mocha"

127.0.0.1:6379> TYPE springbucks-coffee:4:idx
set

# 5. Check TTL
127.0.0.1:6379> TTL springbucks-coffee:4
(integer) 45  # 45 seconds remaining

127.0.0.1:6379> TTL springbucks-coffee:name:mocha
(integer) 45  # Index expires with main data

# 6. View individual Hash fields
127.0.0.1:6379> HGET springbucks-coffee:4 name
"mocha"

127.0.0.1:6379> HGET springbucks-coffee:4 price
"15000"

# Exit
127.0.0.1:6379> exit
```

## How It Works

### Save Operation

When calling `cacheRepository.save(coffeeCache)`, Spring Data Redis automatically:

```
cacheRepository.save(CoffeeCache(id=4, name=mocha, price=15000))
  ↓
1. Create main Hash: springbucks-coffee:4
   → HSET springbucks-coffee:4 _class "tw.fengqing.spring.springbucks.model.CoffeeCache"
   → HSET springbucks-coffee:4 id "4"
   → HSET springbucks-coffee:4 name "mocha"
   → HSET springbucks-coffee:4 price "15000"
   → EXPIRE springbucks-coffee:4 60
  ↓
2. Add to main Set: springbucks-coffee
   → SADD springbucks-coffee "4"
  ↓
3. Create secondary index: springbucks-coffee:name:mocha (@Indexed)
   → SADD springbucks-coffee:name:mocha "4"
   → EXPIRE springbucks-coffee:name:mocha 60
  ↓
4. Create index collection: springbucks-coffee:4:idx
   → SADD springbucks-coffee:4:idx "springbucks-coffee:name:mocha"
   → EXPIRE springbucks-coffee:4:idx 60
```

### Query Operation

When calling `findOneByName("mocha")`:

```
cacheRepository.findOneByName("mocha")
  ↓
1. Query secondary index: SMEMBERS springbucks-coffee:name:mocha
   → Returns: ["4"]
  ↓
2. Query main Hash: HGETALL springbucks-coffee:4
   → Returns: {_class=..., id=4, name=mocha, price=15000}
  ↓
3. Deserialize to CoffeeCache object
   → Use BytesToMoneyConverter: "15000" → Money(TWD 150.00)
```

### Delete Operation

When calling `cacheRepository.deleteById(4L)`:

```
cacheRepository.deleteById(4L)
  ↓
1. Remove from main Set: SREM springbucks-coffee "4"
  ↓
2. Get index collection: SMEMBERS springbucks-coffee:4:idx
   → Returns: ["springbucks-coffee:name:mocha"]
  ↓
3. Remove from secondary index: SREM springbucks-coffee:name:mocha "4"
  ↓
4. Delete main Hash: DEL springbucks-coffee:4
  ↓
5. Delete index collection: DEL springbucks-coffee:4:idx
```

## @RedisHash Annotations

### @RedisHash

**Purpose:** Define Redis Hash entity

```java
@RedisHash(
    value = "springbucks-coffee",  // Redis Hash name prefix
    timeToLive = 60                 // Auto-expire after 60 seconds
)
public class CoffeeCache {
    // ...
}
```

**Effects:**
- Creates Redis Hash with name `springbucks-coffee:{id}`
- Auto-sets TTL on save
- Manages main Set `springbucks-coffee` with all IDs

### @Id

**Purpose:** Define primary key

```java
@Id
private Long id;
```

**Effects:**
- Generates Redis key: `springbucks-coffee:{id}`
- Used in `findById()` and `deleteById()`

### @Indexed

**Purpose:** Create secondary index

```java
@Indexed
private String name;
```

**Effects:**
- Creates secondary index key: `springbucks-coffee:name:{name}`
- Stores Set of IDs with this name
- Enables `findOneByName()` method
- Index expires with main data (TTL sync)

## Repository Methods

### Built-in Methods (from CrudRepository)

```java
// Save to Redis
CoffeeCache saved = cacheRepository.save(coffeeCache);

// Find by ID
Optional<CoffeeCache> found = cacheRepository.findById(4L);

// Find all
Iterable<CoffeeCache> all = cacheRepository.findAll();

// Delete by ID
cacheRepository.deleteById(4L);

// Delete entity
cacheRepository.delete(coffeeCache);

// Check existence
boolean exists = cacheRepository.existsById(4L);

// Count
long count = cacheRepository.count();
```

### Custom Query Methods

```java
public interface CoffeeCacheRepository extends CrudRepository<CoffeeCache, Long> {
    
    // Find by name using @Indexed secondary index
    Optional<CoffeeCache> findOneByName(String name);
    
    // Can add more query methods:
    // List<CoffeeCache> findByPriceGreaterThan(Money price);
    // List<CoffeeCache> findByNameContaining(String keyword);
}
```

**Naming Convention:**
- `findOneByName`: Find single result by name field
- `findByName`: Find list by name field
- `findByNameAndPrice`: Multiple field query
- `findByPriceGreaterThan`: Comparison query

## Custom Converters

### Why Custom Converters?

Redis Repository doesn't know how to serialize complex types like `Money`. We need custom converters.

### Converter Registration

```java
@Configuration
public class RedisConfig {
    
    @Bean
    public RedisCustomConversions redisCustomConversions() {
        return new RedisCustomConversions(
                Arrays.asList(
                        new MoneyToBytesConverter(),    // Write: Money → byte[]
                        new BytesToMoneyConverter()     // Read: byte[] → Money
                ));
    }
}
```

### MoneyToBytesConverter

```java
@WritingConverter
public class MoneyToBytesConverter implements Converter<Money, byte[]> {
    
    @Override
    public byte[] convert(@NonNull Money source) {
        // Money(TWD 150.00) → "15000" → byte[]
        String value = Long.toString(source.getAmountMinorLong());
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
```

### BytesToMoneyConverter

```java
@ReadingConverter
public class BytesToMoneyConverter implements Converter<byte[], Money> {
    
    @Override
    public Money convert(@NonNull byte[] source) {
        // byte[] → "15000" → Money(TWD 150.00)
        String value = new String(source, StandardCharsets.UTF_8);
        return Money.ofMinor(CurrencyUnit.of("TWD"), Long.parseLong(value));
    }
}
```

## Circular Dependency Resolution

### Problem (Spring Boot 3.x)

```
***************************
APPLICATION FAILED TO START
***************************

Description:

The dependencies of some of the beans in the application context form a cycle:

┌─────┐
|  redisConfig (field private org.springframework.data.redis...)
↑     ↓
|  coffeeService (field private ...CoffeeCacheRepository)
↑     ↓
|  springBucksApplication (field private ...CoffeeService)
└─────┘
```

### Solution: Configuration Separation + @Lazy

**Separate Redis Configuration:**

```java
// Separate Redis configuration class
@Configuration
@EnableRedisRepositories(basePackages = "tw.fengqing.spring.springbucks.repository")
public class RedisConfig {
    // Redis-specific beans
}

// Main application class
@SpringBootApplication
public class SpringBucksApplication {
    // Application logic
}
```

**Use @Lazy for late initialization:**

```java
@SpringBootApplication
public class SpringBucksApplication implements ApplicationRunner {
    
    @Autowired
    @Lazy  // Lazy initialization to avoid circular dependency
    private CoffeeService coffeeService;
    
    // ...
}
```

## Monitoring

### Redis Monitoring Commands

**Real-time Monitor:**

```bash
# Monitor all Redis commands
docker exec -it redis-spring-course redis-cli MONITOR

# After starting app, you'll see:
# 1697012345.123456 [0 127.0.0.1:12345] "SMEMBERS" "springbucks-coffee:name:mocha"
# 1697012345.234567 [0 127.0.0.1:12345] "HGETALL" "springbucks-coffee:4"
```

**Database Statistics:**

```bash
docker exec -it redis-spring-course redis-cli

127.0.0.1:6379> DBSIZE
(integer) 4  # 4 keys for one CoffeeCache entry

127.0.0.1:6379> INFO keyspace
# Keyspace
db0:keys=4,expires=4,avg_ttl=45000
```

### Actuator Endpoints

```bash
# View all endpoints
curl http://localhost:8080/actuator

# Health check
curl http://localhost:8080/actuator/health
```

## Best Practices

### 1. Entity Design

```java
// ✅ Good: Only cache essential fields
@RedisHash(value = "user-cache", timeToLive = 3600)
public class UserCache {
    @Id
    private Long id;
    
    @Indexed
    private String username;    // Create index for queries
    
    @Indexed
    private String email;       // Multiple indexes supported
    
    private String displayName; // No index (not frequently queried)
}

// ❌ Bad: Cache too many fields
@RedisHash(value = "user-cache", timeToLive = 3600)
public class UserCache {
    @Id
    private Long id;
    
    private String password;           // Sensitive data!
    private byte[] profileImage;       // Large data!
    private List<Order> orderHistory;  // Complex relationships!
}
```

### 2. Index Design

```java
// ✅ Use @Indexed for frequently queried fields
@Indexed
private String username;  // findByUsername() is common

// ❌ Don't index every field
@Indexed
private String description;  // Rarely queried, wastes space
```

### 3. TTL Configuration

```java
// ✅ Always set TTL to prevent memory leak
@RedisHash(value = "coffee-cache", timeToLive = 60)

// ✅ Different TTL for different data
@RedisHash(value = "hot-data", timeToLive = 300)     // 5 minutes
@RedisHash(value = "cold-data", timeToLive = 3600)   // 1 hour
@RedisHash(value = "config-data", timeToLive = 86400) // 24 hours

// ❌ Never expire (memory leak!)
@RedisHash(value = "user-cache")  // No timeToLive!
```

### 4. Converter Best Practices

```java
// ✅ Handle null values
@ReadingConverter
public class BytesToMoneyConverter implements Converter<byte[], Money> {
    @Override
    public Money convert(@NonNull byte[] source) {
        if (source == null || source.length == 0) {
            return null;  // Handle null
        }
        String value = new String(source, StandardCharsets.UTF_8);
        return Money.ofMinor(CurrencyUnit.of("TWD"), Long.parseLong(value));
    }
}

// ✅ Add error handling
@WritingConverter
public class MoneyToBytesConverter implements Converter<Money, byte[]> {
    @Override
    public byte[] convert(@NonNull Money source) {
        try {
            String value = Long.toString(source.getAmountMinorLong());
            return value.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Money conversion failed", e);
            return "0".getBytes(StandardCharsets.UTF_8);
        }
    }
}
```

## Common Issues

### Issue 1: Circular Dependency

**Error:**

```
The dependencies of some of the beans form a cycle
```

**Solutions:**

```java
// Solution 1: Use @Lazy
@Autowired
@Lazy
private CoffeeService coffeeService;

// Solution 2: Separate configuration
@Configuration
@EnableRedisRepositories(basePackages = "...")
public class RedisConfig { }
```

### Issue 2: Converter Not Working

**Problem:** Money field not converted properly

**Cause:** Converter not registered

**Solution:**

```java
@Bean
public RedisCustomConversions redisCustomConversions() {
    return new RedisCustomConversions(
            Arrays.asList(
                    new MoneyToBytesConverter(),
                    new BytesToMoneyConverter()
            ));
}
```

### Issue 3: Secondary Index Not Created

**Problem:** `findOneByName()` returns empty

**Cause:** Missing `@Indexed` annotation

**Solution:**

```java
// Add @Indexed to name field
@Indexed
private String name;
```

### Issue 4: Data Not Expiring

**Problem:** Redis memory keeps growing

**Cause:** No TTL configured

**Solution:**

```java
// Add timeToLive to @RedisHash
@RedisHash(value = "springbucks-coffee", timeToLive = 60)
```

## redis-demo vs redis-repository-demo

| Feature | redis-demo | redis-repository-demo |
|---------|------------|----------------------|
| **Approach** | Manual RedisTemplate | Automatic Repository |
| **Code Complexity** | Higher (manual) | Lower (auto) |
| **Redis Keys** | 1 key | 4 keys per entity |
| **Data Structure** | Hash only | Hash + Set |
| **Index Support** | ❌ Manual | ✅ Automatic (@Indexed) |
| **TTL Management** | Manual `expire()` | Auto (@RedisHash) |
| **Serialization** | JDK (binary) | Custom converters |
| **Type Safety** | Manual conversion | Automatic |
| **Flexibility** | ✅ High | ⚠️ Limited |
| **Code Lines** | 10+ lines | 2-3 lines |
| **Use Case** | Full control needed | Complex queries, indexes |

**Selection Guide:**
- **redis-demo**: Use when you need full control over Redis operations
- **redis-repository-demo**: Use when you need secondary indexes and automatic management

## Testing

### Run Tests

```bash
# Run tests
./mvnw test

# Run application
./mvnw spring-boot:run
```

### Clear Redis Cache

```bash
# Connect to Redis
docker exec -it redis-spring-course redis-cli

# Delete all keys
127.0.0.1:6379> FLUSHALL

# Or delete specific pattern
127.0.0.1:6379> EVAL "return redis.call('del', unpack(redis.call('keys', 'springbucks-coffee*')))" 0
```

## Best Practices Demonstrated

1. **Spring Data Redis Repository**: Object-oriented Redis operations
2. **@RedisHash**: Entity mapping with automatic TTL
3. **@Indexed**: Secondary indexes for complex queries
4. **Custom Converters**: Money type serialization
5. **Circular Dependency Resolution**: @Lazy and configuration separation
6. **ReadFrom Strategy**: Data consistency with MASTER_PREFERRED
7. **Type Safety**: Compile-time checks with repository interface

## References

- [Spring Data Redis Documentation](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)
- [Spring Data Redis Repositories](https://docs.spring.io/spring-data/redis/docs/current/reference/html/#redis.repositories)
- [Redis Documentation](https://redis.io/docs/)
- [Redis Hash Commands](https://redis.io/commands/?group=hash)
- [Lettuce Redis Client](https://lettuce.io/)

## License

MIT License - see [LICENSE](LICENSE) file for details.

## About Us

我們主要專注在敏捷專案管理、物聯網（IoT）應用開發和領域驅動設計（DDD）。喜歡把先進技術和實務經驗結合，打造好用又靈活的軟體解決方案。近來也積極結合 AI 技術，推動自動化工作流，讓開發與運維更有效率、更智慧。持續學習與分享，希望能一起推動軟體開發的創新和進步。

## Contact

**風清雲談** - 專注於敏捷專案管理、物聯網（IoT）應用開發和領域驅動設計（DDD）。

- 🌐 官方網站：[風清雲談部落格](https://blog.fengqing.tw/)
- 📘 Facebook：[風清雲談粉絲頁](https://www.facebook.com/profile.php?id=61576838896062)
- 💼 LinkedIn：[Chu Kuo-Lung](https://www.linkedin.com/in/chu-kuo-lung)
- 📺 YouTube：[雲談風清頻道](https://www.youtube.com/channel/UCXDqLTdCMiCJ1j8xGRfwEig)
- 📧 Email：[fengqing.tw@gmail.com](mailto:fengqing.tw@gmail.com)

---

**⭐ 如果這個專案對您有幫助，歡迎給個 Star！**
