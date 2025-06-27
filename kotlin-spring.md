# Complete Spring with Kotlin tutorial

## Table of Contents

### 1. [Spring Boot: Dependency Injection, AOP, and Events](#1-spring-boot-dependency-injection-aop-and-events)
- [1.1 Dependency Injection](#11-dependency-injection)
  - [1.1.1 What is Dependency Injection?](#111-what-is-dependency-injection)
  - [1.1.2 Component Scanning and Annotations](#112-component-scanning-and-annotations)
  - [1.1.3 Types of Injection](#113-types-of-injection)
  - [1.1.4 Configuration with @Bean](#114-configuration-with-bean)
  - [1.1.5 Conditional Beans](#115-conditional-beans)
- [1.2 Aspect-Oriented Programming (AOP)](#12-aspect-oriented-programming-aop)
  - [1.2.1 What is AOP?](#121-what-is-aop)
  - [1.2.2 Setting Up AOP](#122-setting-up-aop)
  - [1.2.3 Creating Aspects](#123-creating-aspects)
- [1.3 Spring Events](#13-spring-events)
  - [1.3.1 What are Spring Events?](#131-what-are-spring-events)
  - [1.3.2 Creating and Publishing Events](#132-creating-and-publishing-events)
  - [1.3.3 Event Listeners](#133-event-listeners)
  - [1.3.4 Advanced Event Patterns](#134-advanced-event-patterns)

### 2. [Spring Data JPA](#2-spring-data-jpa)
- [2.1 Introduction and Setup](#21-introduction-and-setup)
- [2.2 Entity Mapping](#22-entity-mapping)
- [2.3 Repository Pattern](#23-repository-pattern)
- [2.4 Query Methods](#24-query-methods)
- [2.5 Custom Queries](#25-custom-queries)
- [2.6 Relationships](#26-relationships)
- [2.7 Pagination and Sorting](#27-pagination-and-sorting)
- [2.8 Specifications and Criteria API](#28-specifications-and-criteria-api)
- [2.9 Transactions](#29-transactions)
  - [2.9.1 Understanding Transactions](#291-understanding-transactions)
  - [2.9.2 Transaction Isolation Levels](#292-transaction-isolation-levels)
  - [2.9.3 Transaction Propagation](#293-transaction-propagation)
  - [2.9.4 Declarative Transactions](#294-declarative-transactions)
  - [2.9.5 Programmatic Transactions](#295-programmatic-transactions)
- [2.10 Auditing](#210-auditing)

### 3. [Spring Web MVC](#3-spring-web-mvc)
- [3.1 Introduction and Setup](#31-introduction-and-setup)
- [3.2 Controllers](#32-controllers)
- [3.3 Request Mapping](#33-request-mapping)
- [3.4 Request and Response Handling](#34-request-and-response-handling)
- [3.5 Data Binding and Validation](#35-data-binding-and-validation)
- [3.6 Exception Handling](#36-exception-handling)
- [3.7 Interceptors](#37-interceptors)
- [3.8 File Upload and Download](#38-file-upload-and-download)
- [3.9 Content Negotiation](#39-content-negotiation)
- [3.10 Testing](#310-testing)

### 4. [Spring Security Basics](#4-spring-security-basics)
- [4.1 Introduction and Setup](#41-introduction-and-setup)
- [4.2 Authentication](#42-authentication)
- [4.3 Authorization](#43-authorization)
- [4.4 Password Encoding](#44-password-encoding)
- [4.5 JWT Authentication](#45-jwt-authentication)
- [4.6 Method-Level Security](#46-method-level-security)
- [4.7 Security Testing](#47-security-testing)
- [4.8 CORS Configuration](#48-cors-configuration)
- [4.9 Security Best Practices](#49-security-best-practices)

### 5. [Best Practices and Common Pitfalls](#5-best-practices-and-common-pitfalls)

---

# 1. Spring Boot: Dependency Injection, AOP, and Events

## 1.1 Dependency Injection

### 1.1.1 What is Dependency Injection?

Dependency Injection (DI) is a fundamental design pattern and a core principle of the Spring Framework that promotes loose coupling between components. Instead of objects creating their own dependencies internally (which leads to tight coupling and difficult testing), DI allows dependencies to be "injected" from external sources, typically by a container like Spring's ApplicationContext.

The main benefits of DI include improved testability (you can easily mock dependencies), better separation of concerns, reduced coupling between classes, and enhanced flexibility in configuration. Spring Boot makes DI even more convenient by providing auto-configuration and component scanning, which automatically discovers and registers beans without extensive XML configuration.

There are three main types of dependency injection: constructor injection (recommended), setter injection, and field injection. Constructor injection is preferred because it ensures dependencies are provided when the object is created, makes dependencies explicit, allows for immutable fields, and helps detect circular dependencies early.

### 1.1.2 Component Scanning and Annotations

```kotlin
// Service class
@Service
class UserService {
    
    fun findById(id: Long): User {
        // Business logic here
        return User(id, "John Doe")
    }
}

// Repository class
@Repository
class UserRepository {
    
    fun save(user: User): User {
        // Database operations
        return user
    }
}

// Controller class
@RestController
class UserController(
    // Constructor injection (recommended)
    private val userService: UserService
) {
    
    @GetMapping("/users/{id}")
    fun getUser(@PathVariable id: Long): User {
        return userService.findById(id)
    }
}
```

### 1.1.3 Types of Injection

**Constructor Injection (Recommended)**
Constructor injection is the preferred method in Spring Boot because it ensures that all required dependencies are provided at object creation time. This approach makes your dependencies explicit and immutable, which leads to more robust and testable code. Constructor injection also helps Spring detect circular dependencies early in the application startup process.

```kotlin
@Service
class OrderService(
    private val paymentService: PaymentService,
    private val emailService: EmailService
) {
    // All dependencies are guaranteed to be available
    // Dependencies are immutable (val in Kotlin)
    // Clear contract - dependencies are explicit
}
```

**Field Injection**
Field injection uses the @Autowired annotation directly on fields. While convenient, it's generally discouraged because it makes dependencies less explicit, complicates testing (you need reflection to set dependencies in tests), and can hide design problems like too many dependencies. It also makes the class harder to instantiate outside of the Spring container.

```kotlin
@Service
class OrderService {
    @Autowired
    private lateinit var paymentService: PaymentService
    
    @Autowired
    private lateinit var emailService: EmailService
    
    // Dependencies are mutable and can be null initially
    // Harder to test and instantiate manually
    // Less explicit about dependencies
}
```

**Setter Injection**
Setter injection provides a middle ground and is useful for optional dependencies or when you need to change dependencies after object creation. It's particularly useful in configuration scenarios where you want to provide default values but allow overriding.

```kotlin
@Service
class OrderService {
    private lateinit var paymentService: PaymentService
    
    @Autowired
    fun setPaymentService(paymentService: PaymentService) {
        this.paymentService = paymentService
    }
    
    // Useful for optional dependencies or late initialization
    // Allows changing dependencies after object creation
}
```

### 1.1.4 Configuration with @Bean

The @Bean annotation allows you to explicitly define beans in configuration classes, giving you full control over bean creation and initialization. This approach is particularly useful when you need to configure third-party libraries, create complex objects with specific initialization logic, or when you can't modify the source code to add Spring annotations.

When multiple beans of the same type exist, Spring needs help determining which one to inject. The @Primary annotation marks a bean as the default choice when multiple candidates exist, while @Qualifier allows you to specify exactly which bean to inject by name.

```kotlin
@Configuration
class AppConfig {
    
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        // Configure BCrypt with specific strength
        return BCryptPasswordEncoder(12)
    }
    
    @Bean
    @Primary // This will be the default DataSource when multiple exist
    fun primaryDataSource(): DataSource {
        return DataSourceBuilder.create()
            .url("jdbc:mysql://localhost:3306/primary")
            .username("primary_user")
            .password("primary_pass")
            .build()
    }
    
    @Bean
    @Qualifier("secondary") // Named bean for specific injection
    fun secondaryDataSource(): DataSource {
        return DataSourceBuilder.create()
            .url("jdbc:mysql://localhost:3306/secondary")
            .username("secondary_user")
            .password("secondary_pass")
            .build()
    }
    
    // Usage with qualifier
    @Service
    class DatabaseService(
        private val primaryDataSource: DataSource, // @Primary bean injected
        @Qualifier("secondary") private val secondaryDataSource: DataSource
    ) {
        // Both data sources available with clear distinction
    }
}
```

### 1.1.5 Conditional Beans

Conditional bean creation is a powerful feature that allows you to create beans only when certain conditions are met. This is particularly useful for feature toggles, environment-specific configurations, or when you want to provide fallback implementations.

Spring Boot provides numerous conditional annotations that check for various conditions at startup time. These conditions are evaluated once during application context initialization, making them efficient for runtime behavior.

```kotlin
@Configuration
class ConditionalConfig {
    
    // Create bean only if property is set to true
    @Bean
    @ConditionalOnProperty(name = ["app.feature.enabled"], havingValue = "true")
    fun featureService(): FeatureService {
        return AdvancedFeatureServiceImpl()
    }
    
    // Create bean only if no other bean of this type exists
    @Bean
    @ConditionalOnMissingBean
    fun defaultService(): DefaultService {
        return DefaultServiceImpl()
    }
    
    // Create bean only if specific class is on classpath
    @Bean
    @ConditionalOnClass(RedisConnectionFactory::class)
    fun redisService(): RedisService {
        return RedisServiceImpl()
    }
    
    // Create bean only in specific profiles
    @Bean
    @Profile("production")
    fun productionService(): ProductionService {
        return ProductionServiceImpl()
    }
    
    // Create bean only if another bean exists
    @Bean
    @ConditionalOnBean(DataSource::class)
    fun databaseService(): DatabaseService {
        return DatabaseServiceImpl()
    }
}
```

These conditional annotations help create flexible, environment-aware applications that can adapt their behavior based on available resources, configuration properties, or runtime conditions.

## 1.2 Aspect-Oriented Programming (AOP)

### 1.2.1 What is AOP?

Aspect-Oriented Programming (AOP) is a programming paradigm that aims to increase modularity by allowing the separation of cross-cutting concerns. Cross-cutting concerns are aspects of a program that affect multiple modules, such as logging, security, transaction management, caching, and performance monitoring.

Traditional object-oriented programming organizes code into classes and methods, but cross-cutting concerns tend to be scattered across multiple classes, leading to code duplication and tight coupling. AOP addresses this by allowing you to define these concerns separately as "aspects" and then weave them into your application at specific points.

In Spring AOP, aspects are applied at runtime using proxy-based approach. This means Spring creates proxy objects that intercept method calls and apply the aspect logic before, after, or around the actual method execution. This approach is less invasive than compile-time weaving and integrates seamlessly with Spring's dependency injection.

### 1.2.2 Setting Up AOP

Add dependency to `build.gradle.kts`:
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-aop")
}
```

Enable AOP in your main class:
```kotlin
@SpringBootApplication
@EnableAspectJAutoProxy
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
```

### 1.2.3 Creating Aspects

**Logging Aspect**

```kotlin
@Aspect
@Component
class LoggingAspect {
    
    companion object {
        private val log = LoggerFactory.getLogger(LoggingAspect::class.java)
    }
    
    // Pointcut for all service methods
    @Pointcut("execution(* com.example.service.*.*(..))")
    fun serviceLayer() {}
    
    // Before advice
    @Before("serviceLayer()")
    fun logBefore(joinPoint: JoinPoint) {
        log.info("Executing method: ${joinPoint.signature.name}")
    }
    
    // After returning advice
    @AfterReturning(pointcut = "serviceLayer()", returning = "result")
    fun logAfterReturning(joinPoint: JoinPoint, result: Any?) {
        log.info("Method ${joinPoint.signature.name} completed successfully")
    }
    
    // Around advice for execution time
    @Around("serviceLayer()")
    fun logExecutionTime(joinPoint: ProceedingJoinPoint): Any? {
        val startTime = System.currentTimeMillis()
        
        return try {
            val result = joinPoint.proceed()
            val endTime = System.currentTimeMillis()
            log.info("Method ${joinPoint.signature.name} executed in ${endTime - startTime} ms")
            result
        } catch (e: Exception) {
            log.error("Exception in method ${joinPoint.signature.name}: ${e.message}")
            throw e
        }
    }
}
```

**Security Aspect**

```kotlin
@Aspect
@Component
class SecurityAspect {
    
    @Around("@annotation(secured)")
    fun checkSecurity(joinPoint: ProceedingJoinPoint, secured: Secured): Any? {
        // Get current user
        val authentication = SecurityContextHolder.getContext().authentication
        
        if (authentication == null || !authentication.isAuthenticated) {
            throw SecurityException("User not authenticated")
        }
        
        // Check roles
        val hasRole = secured.value.any { role ->
            authentication.authorities.any { auth -> auth.authority == role }
        }
        
        if (!hasRole) {
            throw SecurityException("Insufficient permissions")
        }
        
        return joinPoint.proceed()
    }
}

// Custom annotation
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Secured(val value: Array<String>)

// Usage
@Service
class AdminService {
    
    @Secured(["ROLE_ADMIN", "ROLE_SUPER_ADMIN"])
    fun deleteUser(userId: Long) {
        // Delete user logic
    }
}
```

**Caching Aspect**

```kotlin
@Aspect
@Component
class CachingAspect {
    
    companion object {
        private val log = LoggerFactory.getLogger(CachingAspect::class.java)
    }
    
    private val cache = ConcurrentHashMap<String, Any>()
    
    @Around("@annotation(cacheable)")
    fun cache(joinPoint: ProceedingJoinPoint, cacheable: Cacheable): Any? {
        val key = generateKey(joinPoint)
        
        cache[key]?.let {
            log.info("Cache hit for key: $key")
            return it
        }
        
        val result = joinPoint.proceed()
        result?.let {
            cache[key] = it
            log.info("Cached result for key: $key")
        }
        
        return result
    }
    
    private fun generateKey(joinPoint: ProceedingJoinPoint): String {
        return "${joinPoint.signature.name}:${joinPoint.args.contentToString()}"
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cacheable
```

## 1.3 Spring Events

### 1.3.1 What are Spring Events?

Spring Events provide a way to decouple components by allowing them to communicate through events. Publishers fire events, and listeners handle them asynchronously. This pattern promotes loose coupling between application components and enables a more reactive architecture.

### 1.3.2 Creating and Publishing Events

**Custom Event**

```kotlin
// Custom event class
class UserRegisteredEvent(
    source: Any,
    val user: User,
    val registrationMethod: String
) : ApplicationEvent(source)
```

**Publishing Events**

```kotlin
@Service
class UserService(
    private val eventPublisher: ApplicationEventPublisher,
    private val userRepository: UserRepository
) {
    
    fun registerUser(user: User, method: String): User {
        val savedUser = userRepository.save(user)
        
        // Publish event
        val event = UserRegisteredEvent(this, savedUser, method)
        eventPublisher.publishEvent(event)
        
        return savedUser
    }
}
```

### 1.3.3 Event Listeners

```kotlin
@Component
class UserEventListener(
    private val emailService: EmailService,
    private val auditService: AuditService
) {
    
    companion object {
        private val log = LoggerFactory.getLogger(UserEventListener::class.java)
    }
    
    // Synchronous listener
    @EventListener
    fun handleUserRegistered(event: UserRegisteredEvent) {
        log.info("User registered: ${event.user.email}")
        auditService.logUserRegistration(event.user, event.registrationMethod)
    }
    
    // Asynchronous listener
    @EventListener
    @Async
    fun sendWelcomeEmail(event: UserRegisteredEvent) {
        log.info("Sending welcome email to: ${event.user.email}")
        emailService.sendWelcomeEmail(event.user)
    }
    
    // Conditional listener
    @EventListener(condition = "#event.registrationMethod == 'SOCIAL'")
    fun handleSocialRegistration(event: UserRegisteredEvent) {
        log.info("Social registration detected for: ${event.user.email}")
        // Special handling for social registrations
    }
}
```

**Enabling Async Events**

```kotlin
@Configuration
@EnableAsync
class AsyncConfig {
    
    @Bean
    fun taskExecutor(): TaskExecutor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = 4
            maxPoolSize = 8
            queueCapacity = 25
            setThreadNamePrefix("async-event-")
            initialize()
        }
    }
}
```

**Transaction Events**

```kotlin
@Component
class TransactionEventListener {
    
    companion object {
        private val log = LoggerFactory.getLogger(TransactionEventListener::class.java)
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleUserRegisteredAfterCommit(event: UserRegisteredEvent) {
        // This runs only after transaction commits successfully
        log.info("Transaction committed for user: ${event.user.email}")
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    fun handleUserRegisteredAfterRollback(event: UserRegisteredEvent) {
        // This runs only after transaction rolls back
        log.error("Transaction rolled back for user: ${event.user.email}")
    }
}
```

### 1.3.4 Advanced Event Patterns

**Event Ordering**

```kotlin
@Component
class OrderedEventListener {
    
    companion object {
        private val log = LoggerFactory.getLogger(OrderedEventListener::class.java)
    }
    
    @EventListener
    @Order(1)
    fun firstHandler(event: UserRegisteredEvent) {
        log.info("First handler executed")
    }
    
    @EventListener
    @Order(2)
    fun secondHandler(event: UserRegisteredEvent) {
        log.info("Second handler executed")
    }
}
```

**Generic Events**

```kotlin
// Generic event
class EntityEvent<T>(
    source: Any,
    val entity: T,
    val action: String
) : ApplicationEvent(source)

// Publisher
@Service
class GenericService {
    
    @Autowired
    private lateinit var eventPublisher: ApplicationEventPublisher
    
    fun createUser(user: User) {
        // Save user
        eventPublisher.publishEvent(EntityEvent(this, user, "CREATED"))
    }
}

// Listener
@Component
class EntityEventListener {
    
    companion object {
        private val log = LoggerFactory.getLogger(EntityEventListener::class.java)
    }
    
    @EventListener
    fun handleEntityEvent(event: EntityEvent<User>) {
        log.info("User ${event.entity.name} was ${event.action}")
    }
}
```

**Data Classes for Examples**

```kotlin
data class User(
    val id: Long,
    val name: String,
    val email: String = "",
    val status: UserStatus = UserStatus.ACTIVE,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class UserStatus {
    ACTIVE, INACTIVE, SUSPENDED
}
```

---

# 2. Spring Data JPA

## 2.1 Introduction and Setup

Spring Data JPA is a part of the Spring Data project that makes it easy to implement JPA-based repositories. It reduces boilerplate code and provides powerful features for data access, including automatic query generation, custom queries, pagination, and auditing.

### Dependencies

Add to `build.gradle.kts`:
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("mysql:mysql-connector-java")
    testImplementation("com.h2database:h2")
}
```

### Configuration

`application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
    defer-datasource-initialization: true
  
  sql:
    init:
      mode: always
```

### Java Configuration

```kotlin
@Configuration
@EnableJpaRepositories(basePackages = ["com.example.repository"])
@EnableJpaAuditing
class JpaConfig {
    
    @Bean
    fun auditorProvider(): AuditorAware<String> {
        return AuditorAware {
            // Return current user
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication != null && authentication.isAuthenticated) {
                Optional.of(authentication.name)
            } else {
                Optional.of("system")
            }
        }
    }
}
```

## 2.2 Entity Mapping

### Basic Entity

```kotlin
@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false, unique = true, length = 100)
    val email: String,
    
    @Column(nullable = false, length = 50)
    val firstName: String,
    
    @Column(nullable = false, length = 50)
    val lastName: String,
    
    @Column(length = 15)
    val phoneNumber: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: UserStatus = UserStatus.ACTIVE,
    
    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PrePersist
    protected fun onCreate() {
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }
    
    @PreUpdate
    protected fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}

enum class UserStatus {
    ACTIVE, INACTIVE, SUSPENDED
}
```

### Advanced Entity Features

```kotlin
@Entity
@Table(
    name = "products", 
    indexes = [
        Index(name = "idx_product_name", columnList = "name"),
        Index(name = "idx_product_category", columnList = "category_id")
    ]
)
@EntityListeners(AuditingEntityListener::class)
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false, length = 100)
    val name: String,
    
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    
    @Column(precision = 10, scale = 2)
    val price: BigDecimal,
    
    @Column(name = "stock_quantity")
    val stockQuantity: Int = 0,
    
    // Embedded object
    @Embedded
    val metadata: ProductMetadata? = null,
    
    // Audit fields
    @CreatedDate
    var createdAt: LocalDateTime? = null,
    
    @LastModifiedDate
    var updatedAt: LocalDateTime? = null,
    
    @CreatedBy
    var createdBy: String? = null,
    
    @LastModifiedBy
    var updatedBy: String? = null,
    
    @Version
    var version: Long? = null
)

@Embeddable
data class ProductMetadata(
    val brand: String? = null,
    val model: String? = null,
    val color: String? = null,
    val weight: Double? = null
)
```

## 2.3 Repository Pattern

### Basic Repository

```kotlin
@Repository
interface UserRepository : JpaRepository<User, Long> {
    // No need to implement - Spring Data JPA provides implementations
}
```

### Custom Repository Interface

```kotlin
interface CustomUserRepository {
    fun findUsersWithComplexCriteria(criteria: String): List<User>
    fun findActiveUsersWithPagination(pageable: Pageable): Page<User>
}

@Repository
class CustomUserRepositoryImpl(
    @PersistenceContext
    private val entityManager: EntityManager
) : CustomUserRepository {
    
    override fun findUsersWithComplexCriteria(criteria: String): List<User> {
        val cb = entityManager.criteriaBuilder
        val query = cb.createQuery(User::class.java)
        val user = query.from(User::class.java)
        
        // Complex criteria logic
        val predicate = cb.or(
            cb.like(cb.lower(user.get("firstName")), "%${criteria.lowercase()}%"),
            cb.like(cb.lower(user.get("lastName")), "%${criteria.lowercase()}%"),
            cb.like(cb.lower(user.get("email")), "%${criteria.lowercase()}%")
        )
        
        query.select(user).where(predicate)
        return entityManager.createQuery(query).resultList
    }
    
    override fun findActiveUsersWithPagination(pageable: Pageable): Page<User> {
        val cb = entityManager.criteriaBuilder
        val query = cb.createQuery(User::class.java)
        val user = query.from(User::class.java)
        
        query.select(user).where(cb.equal(user.get<UserStatus>("status"), UserStatus.ACTIVE))
        
        val typedQuery = entityManager.createQuery(query)
        typedQuery.firstResult = pageable.offset.toInt()
        typedQuery.maxResults = pageable.pageSize
        
        val users = typedQuery.resultList
        
        // Count query for total elements
        val countQuery = cb.createQuery(Long::class.java)
        val userCount = countQuery.from(User::class.java)
        countQuery.select(cb.count(userCount)).where(cb.equal(userCount.get<UserStatus>("status"), UserStatus.ACTIVE))
        val total = entityManager.createQuery(countQuery).singleResult
        
        return PageImpl(users, pageable, total)
    }
}

// Extended repository interface
interface UserRepository : JpaRepository<User, Long>, CustomUserRepository {
    // Query methods and custom methods are both available
}
```

## 2.4 Query Methods

### Query Method Naming

```kotlin
@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    // Find by single property
    fun findByEmail(email: String): User?
    fun findByFirstName(firstName: String): List<User>
    
    // Find by multiple properties
    fun findByFirstNameAndLastName(firstName: String, lastName: String): List<User>
    fun findByFirstNameOrLastName(firstName: String, lastName: String): List<User>
    
    // Conditional queries
    fun findByFirstNameContaining(name: String): List<User>
    fun findByFirstNameContainingIgnoreCase(name: String): List<User>
    fun findByFirstNameStartingWith(prefix: String): List<User>
    fun findByFirstNameEndingWith(suffix: String): List<User>
    
    // Comparison queries
    fun findByCreatedAtAfter(date: LocalDateTime): List<User>
    fun findByCreatedAtBefore(date: LocalDateTime): List<User>
    fun findByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): List<User>
    
    // Null checks
    fun findByPhoneNumberIsNotNull(): List<User>
    fun findByPhoneNumberIsNull(): List<User>
    
    // Collection queries
    fun findByStatusIn(statuses: Collection<UserStatus>): List<User>
    fun findByStatusNotIn(statuses: Collection<UserStatus>): List<User>
    
    // Ordering and limiting
    fun findByStatusOrderByCreatedAtDesc(status: UserStatus): List<User>
    fun findTop10ByStatusOrderByCreatedAtDesc(status: UserStatus): List<User>
    fun findFirstByStatusOrderByCreatedAtAsc(status: UserStatus): User?
    
    // Boolean queries
    fun existsByEmail(email: String): Boolean
    fun countByStatus(status: UserStatus): Long
    
    // Delete queries
    fun deleteByStatus(status: UserStatus)
    fun deleteByCreatedAtBefore(date: LocalDateTime): Long
}
```

### Advanced Query Methods

```kotlin
@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    
    // Complex property navigation
    fun findByCategoryNameAndPriceBetween(
        categoryName: String, 
        minPrice: BigDecimal, 
        maxPrice: BigDecimal
    ): List<Product>
    
    // Projection queries
    fun findByCategoryName(categoryName: String): List<ProductNameAndPrice>
    
    // Stream results for large datasets
    @Query("SELECT p FROM Product p WHERE p.price > ?1")
    fun findExpensiveProducts(minPrice: BigDecimal): Stream<Product>
    
    // Async queries
    @Async
    fun findByPriceGreaterThan(price: BigDecimal): CompletableFuture<List<Product>>
    
    // Slice for memory-efficient pagination
    fun findByCategoryName(categoryName: String, pageable: Pageable): Slice<Product>
}

// Projection interface
interface ProductNameAndPrice {
    val name: String
    val price: BigDecimal
}
```

## 2.5 Custom Queries

### @Query Annotation

```kotlin
@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    // JPQL queries
    @Query("SELECT u FROM User u WHERE u.firstName = ?1 AND u.lastName = ?2")
    fun findByFullName(firstName: String, lastName: String): List<User>
    
    // Named parameters
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.status = :status")
    fun findByEmailAndStatus(
        @Param("email") email: String, 
        @Param("status") status: UserStatus
    ): User?
    
    // Native SQL queries
    @Query(
        value = "SELECT * FROM users u WHERE u.created_at > :date", 
        nativeQuery = true
    )
    fun findUsersCreatedAfter(@Param("date") date: LocalDateTime): List<User>
    
    // Modifying queries
    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.email = :email")
    fun updateUserStatus(
        @Param("email") email: String, 
        @Param("status") status: UserStatus
    ): Int
    
    @Modifying
    @Query("DELETE FROM User u WHERE u.status = :status AND u.createdAt < :date")
    fun deleteInactiveUsers(
        @Param("status") status: UserStatus, 
        @Param("date") date: LocalDateTime
    ): Int
    
    // Count queries
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    fun countByStatus(@Param("status") status: UserStatus): Long
    
    // Custom result mapping
    @Query("""
        SELECT new com.example.dto.UserSummary(u.id, u.firstName, u.lastName, u.email) 
        FROM User u WHERE u.status = :status
    """)
    fun findUserSummaries(@Param("status") status: UserStatus): List<UserSummary>
}

// DTO class
data class UserSummary(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String
)
```

## 2.6 Relationships

### One-to-Many

```kotlin
@Entity
@Table(name = "categories")
data class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    val name: String,
    
    @OneToMany(mappedBy = "category", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val products: MutableList<Product> = mutableListOf()
) {
    // Helper methods
    fun addProduct(product: Product) {
        products.add(product)
        product.category = this
    }
    
    fun removeProduct(product: Product) {
        products.remove(product)
        product.category = null
    }
}

@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    val name: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: Category? = null
    
    // Other fields...
)
```

### Many-to-Many

```kotlin
@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    val email: String,
    
    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    val roles: MutableSet<Role> = mutableSetOf()
) {
    // Helper methods
    fun addRole(role: Role) {
        roles.add(role)
        role.users.add(this)
    }
    
    fun removeRole(role: Role) {
        roles.remove(role)
        role.users.remove(this)
    }
}

@Entity
@Table(name = "roles")
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    val name: String,
    
    @ManyToMany(mappedBy = "roles")
    val users: MutableSet<User> = mutableSetOf()
)
```

### One-to-One

```kotlin
@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    val email: String,
    
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var profile: UserProfile? = null
) {
    // Helper method
    fun setProfile(profile: UserProfile) {
        this.profile = profile
        profile.user = this
    }
}

@Entity
@Table(name = "user_profiles")
data class UserProfile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    val bio: String? = null,
    val avatarUrl: String? = null,
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null
)
```

## 2.7 Pagination and Sorting

### Basic Pagination

```kotlin
@RestController
class UserController(
    private val userRepository: UserRepository
) {
    
    @GetMapping("/users")
    fun getUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "id") sortBy: String,
        @RequestParam(defaultValue = "asc") sortDir: String
    ): Page<User> {
        val sort = if (sortDir.equals("desc", ignoreCase = true)) 
            Sort.by(sortBy).descending() 
        else 
            Sort.by(sortBy).ascending()
        
        val pageable = PageRequest.of(page, size, sort)
        return userRepository.findAll(pageable)
    }
    
    @GetMapping("/users/active")
    fun getActiveUsers(pageable: Pageable): Page<User> {
        return userRepository.findByStatus(UserStatus.ACTIVE, pageable)
    }
}
```

## 2.8 Specifications and Criteria API

### JPA Specifications

```kotlin
// Enable specifications in repository
interface UserRepository : JpaRepository<User, Long>, JpaSpecificationExecutor<User>

// Specification utility class
object UserSpecifications {
    
    fun hasFirstName(firstName: String?): Specification<User>? {
        return if (firstName == null) null
        else Specification { root, _, criteriaBuilder ->
            criteriaBuilder.equal(root.get<String>("firstName"), firstName)
        }
    }
    
    fun hasLastName(lastName: String?): Specification<User>? {
        return if (lastName == null) null
        else Specification { root, _, criteriaBuilder ->
            criteriaBuilder.equal(root.get<String>("lastName"), lastName)
        }
    }
    
    fun hasEmail(email: String?): Specification<User>? {
        return if (email == null) null
        else Specification { root, _, criteriaBuilder ->
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("email")), 
                "%${email.lowercase()}%"
            )
        }
    }
    
    fun hasStatus(status: UserStatus?): Specification<User>? {
        return if (status == null) null
        else Specification { root, _, criteriaBuilder ->
            criteriaBuilder.equal(root.get<UserStatus>("status"), status)
        }
    }
    
    fun createdBetween(start: LocalDateTime?, end: LocalDateTime?): Specification<User>? {
        return when {
            start == null && end == null -> null
            start == null -> Specification { root, _, criteriaBuilder ->
                criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), end)
            }
            end == null -> Specification { root, _, criteriaBuilder ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), start)
            }
            else -> Specification { root, _, criteriaBuilder ->
                criteriaBuilder.between(root.get("createdAt"), start, end)
            }
        }
    }
}

// Service using specifications
@Service
class UserService(
    private val userRepository: UserRepository
) {
    
    fun findUsers(
        firstName: String?, 
        lastName: String?, 
        email: String?, 
        status: UserStatus?, 
        startDate: LocalDateTime?, 
        endDate: LocalDateTime?, 
        pageable: Pageable
    ): Page<User> {
        
        val spec = Specification.where(UserSpecifications.hasFirstName(firstName))
            .and(UserSpecifications.hasLastName(lastName))
            .and(UserSpecifications.hasEmail(email))
            .and(UserSpecifications.hasStatus(status))
            .and(UserSpecifications.createdBetween(startDate, endDate))
        
        return userRepository.findAll(spec, pageable)
    }
}
```

## 2.9 Transactions

### 2.9.1 Understanding Transactions

Transactions are fundamental to maintaining data consistency and integrity in database operations. A transaction represents a unit of work that must be completed entirely or not at all (atomicity). Spring's transaction management provides a consistent programming model across different transaction APIs (JTA, JDBC, Hibernate, JPA) and supports both declarative and programmatic transaction management.

The key ACID properties of transactions are:
- **Atomicity**: All operations within a transaction succeed or fail together
- **Consistency**: Transactions bring the database from one consistent state to another
- **Isolation**: Concurrent transactions don't interfere with each other
- **Durability**: Committed changes persist even in case of system failure

### 2.9.2 Transaction Isolation Levels

Isolation levels control how transactions interact with each other and what phenomena they prevent. Spring supports all standard SQL isolation levels:

**READ_UNCOMMITTED (Isolation.READ_UNCOMMITTED)**
This is the lowest isolation level where transactions can read uncommitted changes from other transactions. While this offers the best performance, it allows dirty reads, non-repeatable reads, and phantom reads. This level is rarely used in production due to data consistency risks.

**READ_COMMITTED (Isolation.READ_COMMITTED)**
This level prevents dirty reads by ensuring transactions can only read committed data. However, it still allows non-repeatable reads and phantom reads. This is the default isolation level for many databases including PostgreSQL and SQL Server.

**REPEATABLE_READ (Isolation.REPEATABLE_READ)**
This level prevents dirty reads and non-repeatable reads by ensuring that if a transaction reads a row, subsequent reads of the same row within the transaction will return the same data. However, phantom reads are still possible. This is MySQL's default isolation level.

**SERIALIZABLE (Isolation.SERIALIZABLE)**
This is the highest isolation level that prevents all read phenomena (dirty reads, non-repeatable reads, and phantom reads) by making transactions execute as if they were running serially. This provides the strongest consistency guarantees but has the lowest performance due to extensive locking.

### 2.9.3 Transaction Propagation

Propagation defines how transactions behave when a transactional method calls another transactional method. Spring provides several propagation options:

**REQUIRED (Propagation.REQUIRED)** - Default
If a transaction exists, the method runs within it. If no transaction exists, a new one is created. This is the most common propagation type and ensures that all operations run within a transaction.

**REQUIRES_NEW (Propagation.REQUIRES_NEW)**
Always creates a new transaction, suspending any existing transaction. This is useful for operations that must complete regardless of the outcome of the calling transaction, such as audit logging.

**SUPPORTS (Propagation.SUPPORTS)**
Runs within an existing transaction if one exists, otherwise runs without a transaction. This is useful for read-only operations that can benefit from transactional context but don't require it.

**NOT_SUPPORTED (Propagation.NOT_SUPPORTED)**
Always runs without a transaction, suspending any existing transaction. This is rarely used but can be useful for operations that shouldn't be part of a transaction.

**MANDATORY (Propagation.MANDATORY)**
Requires an existing transaction and throws an exception if none exists. This ensures that the method is only called within a transactional context.

**NEVER (Propagation.NEVER)**
Requires that no transaction exists and throws an exception if one is found. This is used for operations that must not run within a transaction.

**NESTED (Propagation.NESTED)**
Creates a nested transaction within an existing transaction. If the nested transaction fails, only the nested portion is rolled back, not the entire transaction. This requires JDBC 3.0+ and is not supported by all databases.

### 2.9.4 Declarative Transactions

Declarative transaction management is Spring's preferred approach, using annotations to define transaction boundaries. This approach separates transaction management concerns from business logic, making code cleaner and more maintainable.

The @Transactional annotation can be applied at class or method level, with method-level annotations overriding class-level settings. When applied at the class level, all public methods become transactional with the specified settings.

#### Rollback Rules
By default, Spring rolls back transactions only for unchecked exceptions (RuntimeException and its subclasses) and errors. Checked exceptions do not trigger rollback unless explicitly configured. You can customize this behavior using rollback rules.

**rollbackFor**: Specifies exception types that should trigger rollback
**noRollbackFor**: Specifies exception types that should NOT trigger rollback
**rollbackForClassName/noRollbackForClassName**: String-based versions for when exception classes aren't available

```kotlin
@Service
@Transactional // Class-level transaction settings apply to all methods
class UserService(
    private val userRepository: UserRepository,
    private val emailService: EmailService,
    private val auditService: AuditService
) {
    
    // Method-level transaction with custom rollback rules
    @Transactional(
        rollbackFor = [Exception::class], // Roll back for any exception
        noRollbackFor = [ValidationException::class], // Don't roll back for validation errors
        timeout = 30 // Transaction timeout in seconds
    )
    fun createUser(user: User): User {
        val savedUser = userRepository.save(user)
        
        // This will rollback the entire transaction if email sending fails
        // because we specified rollbackFor = [Exception::class]
        emailService.sendWelcomeEmail(savedUser)
        
        return savedUser
    }
    
    // Read-only transaction for better performance
    // Read-only transactions can enable optimizations like:
    // - Hibernate flush mode set to MANUAL
    // - Database connection set to read-only
    // - Certain caching optimizations
    @Transactional(readOnly = true)
    fun findActiveUsers(): List<User> {
        return userRepository.findByStatus(UserStatus.ACTIVE)
    }
    
    // Transaction with specific isolation level
    // SERIALIZABLE prevents all read phenomena but has performance impact
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun transferCredits(fromUserId: Long, toUserId: Long, amount: BigDecimal) {
        val fromUser = userRepository.findById(fromUserId)
            .orElseThrow { EntityNotFoundException("Source user not found") }
        val toUser = userRepository.findById(toUserId)
            .orElseThrow { EntityNotFoundException("Target user not found") }
        
        // Critical section - prevent concurrent modifications
        if (fromUser.credits < amount) {
            throw InsufficientFundsException("Insufficient credits")
        }
        
        fromUser.subtractCredits(amount)
        toUser.addCredits(amount)
        
        userRepository.save(fromUser)
        userRepository.save(toUser)
        
        // Audit trail - this will be part of the same transaction
        auditService.logCreditTransfer(fromUserId, toUserId, amount)
    }
    
    // Transaction with timeout
    // Useful for long-running operations to prevent resource exhaustion
    @Transactional(timeout = 60) // 60 seconds timeout
    fun bulkUpdateUsers(updates: List<UserUpdate>) {
        updates.forEach { update ->
            val user = userRepository.findById(update.userId)
                .orElseThrow { EntityNotFoundException("User not found") }
            
            // Apply updates
            user.updateFromRequest(update)
            userRepository.save(user)
            
            // Simulate time-consuming operation
            Thread.sleep(100)
        }
    }
    
    // New transaction propagation - always runs in its own transaction
    // This audit record will be saved even if the calling transaction rolls back
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun auditUserAction(userId: Long, action: String, details: String) {
        val auditRecord = AuditRecord(
            userId = userId,
            action = action,
            details = details,
            timestamp = LocalDateTime.now()
        )
        auditRepository.save(auditRecord)
        
        // This transaction commits independently
        // Even if the calling method's transaction fails, this audit record persists
    }
}
```

### 2.9.5 Programmatic Transactions

While declarative transactions are preferred for most use cases, programmatic transactions provide fine-grained control over transaction boundaries. This approach is useful when you need dynamic transaction management, complex transaction logic, or when working with legacy code that doesn't support annotations.

The TransactionTemplate provides a convenient way to execute code within a transaction while handling the transaction lifecycle automatically. It follows the template method pattern, where you provide the business logic and the template handles transaction management.

```kotlin
@Service
class TransactionalService(
    private val transactionTemplate: TransactionTemplate,
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository
) {
    
    // Basic programmatic transaction
    fun createUserProgrammatically(user: User): User {
        return transactionTemplate.execute { status ->
            try {
                val savedUser = userRepository.save(user)
                
                // Perform additional operations
                initializeUserSettings(savedUser)
                sendWelcomeEmail(savedUser)
                
                savedUser
            } catch (e: Exception) {
                // Manually mark transaction for rollback
                status.setRollbackOnly()
                throw RuntimeException("User creation failed", e)
            }
        } ?: throw RuntimeException("Transaction returned null")
    }
    
    // Transaction without return value
    fun createUserWithoutReturn(user: User) {
        transactionTemplate.executeWithoutResult { status ->
            try {
                userRepository.save(user)
                
                // Perform additional operations
                auditService.logUserCreation(user)
                
            } catch (e: Exception) {
                // Mark for rollback and let exception propagate
                status.setRollbackOnly()
                throw RuntimeException("User creation failed", e)
            }
        }
    }
    
    // Using TransactionTemplate with custom configuration
    fun performCustomTransactionOperation() {
        // Create custom transaction template for this specific operation
        val customTemplate = TransactionTemplate(transactionManager).apply {
            isolationLevel = TransactionDefinition.ISOLATION_SERIALIZABLE
            timeout = 30
            isReadOnly = false
            propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
        }
        
        customTemplate.execute { status ->
            // Operations requiring SERIALIZABLE isolation
            performCriticalDatabaseOperations()
            
            // Check if we need to rollback based on business logic
            if (shouldRollbackBasedOnBusinessRules()) {
                status.setRollbackOnly()
            }
            
            null
        }
    }
}
```

#### Transaction Best Practices

**Keep Transactions Short**: Long-running transactions hold database locks longer, reducing concurrency and increasing the risk of deadlocks. Break large operations into smaller, focused transactions when possible.

**Use Appropriate Isolation Levels**: Choose the lowest isolation level that meets your consistency requirements. Higher isolation levels provide stronger guarantees but reduce performance and concurrency.

**Handle Exceptions Properly**: Understand which exceptions trigger rollbacks and configure rollback rules appropriately. Consider whether business exceptions should cause rollbacks or just be logged.

**Avoid Transactions for Read-Only Operations**: Unless you need consistent reads across multiple queries, avoid wrapping simple read operations in transactions. Use `@Transactional(readOnly = true)` for read-heavy operations.

**Be Careful with Nested Transactions**: Understand how different propagation modes affect transaction boundaries. REQUIRES_NEW creates separate transactions, while NESTED creates savepoints.

**Test Transaction Behavior**: Write integration tests that verify your transaction behavior, especially for complex scenarios involving multiple services or exception handling.

## 2.10 Auditing

### Entity Auditing

```kotlin
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    val email: String,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,
    
    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null,
    
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    var createdBy: String? = null,
    
    @LastModifiedBy
    @Column(name = "updated_by")
    var updatedBy: String? = null,
    
    @Version
    var version: Long? = null
)

// Configuration
@Configuration
@EnableJpaAuditing
class AuditConfig {
    
    @Bean
    fun auditorProvider(): AuditorAware<String> {
        return AuditorAware {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication != null && authentication.isAuthenticated 
                && authentication.name != "anonymousUser") {
                Optional.of(authentication.name)
            } else {
                Optional.of("system")
            }
        }
    }
}
```

---

# 3. Spring Web MVC

## 3.1 Introduction and Setup

Spring Web MVC is a web framework built on the Model-View-Controller pattern that provides a flexible and powerful way to build web applications and RESTful APIs. It's built around a central DispatcherServlet that dispatches requests to handlers, with configurable handler mappings, view resolution, locale and theme resolution.

### Dependencies

Add to `build.gradle.kts`:
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

### Configuration

```kotlin
@Configuration
@EnableWebMvc
class WebConfig : WebMvcConfigurer {
    
    // CORS Configuration
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000", "https://yourdomain.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
    }
    
    // Add Interceptors
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(LoggingInterceptor())
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/public/**")
        
        registry.addInterceptor(AuthenticationInterceptor())
            .addPathPatterns("/api/admin/**")
    }
    
    // Configure Message Converters
    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        val builder = Jackson2ObjectMapperBuilder()
            .indentOutput(true)
            .simpleDateFormat("yyyy-MM-dd HH:mm:ss")
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        
        converters.add(MappingJackson2HttpMessageConverter(builder.build()))
    }
    
    // Configure Content Negotiation
    override fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {
        configurer
            .favorParameter(true)
            .parameterName("mediaType")
            .ignoreAcceptHeader(false)
            .useRegisteredExtensionsOnly(false)
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaType("xml", MediaType.APPLICATION_XML)
            .mediaType("json", MediaType.APPLICATION_JSON)
    }
    
    // Configure Resource Handling
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:/static/")
            .setCachePeriod(3600)
            .resourceChain(true)
            .addResolver(VersionResourceResolver().addContentVersionStrategy("/**"))
    }
}
```

## 3.2 Controllers

### Basic Controller

```kotlin
@RestController
@RequestMapping("/api/users")
@Validated
class UserController(
    private val userService: UserService
) {
    
    companion object {
        private val log = LoggerFactory.getLogger(UserController::class.java)
    }
    
    @GetMapping
    fun getAllUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "id") sortBy: String,
        @RequestParam(defaultValue = "asc") sortDir: String
    ): ResponseEntity<Page<UserDto>> {
        val users = userService.getAllUsers(page, size, sortBy, sortDir)
        return ResponseEntity.ok(users)
    }
    
    @GetMapping("/{id}")
    fun getUserById(@PathVariable @Positive id: Long): ResponseEntity<UserDto> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(user)
    }
    
    @PostMapping
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserDto> {
        val createdUser = userService.createUser(request)
        val location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdUser.id)
            .toUri()
        
        return ResponseEntity.created(location).body(createdUser)
    }
    
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable @Positive id: Long,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserDto> {
        val updatedUser = userService.updateUser(id, request)
        return ResponseEntity.ok(updatedUser)
    }
    
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable @Positive id: Long): ResponseEntity<Void> {
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }
}
```

## 3.3 Request Mapping

### Path Variables and Request Parameters

```kotlin
@RestController
@RequestMapping("/api")
class RequestMappingController {
    
    // Simple path variable
    @GetMapping("/users/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<String> {
        return ResponseEntity.ok("User ID: $id")
    }
    
    // Multiple path variables
    @GetMapping("/users/{userId}/orders/{orderId}")
    fun getUserOrder(
        @PathVariable userId: Long,
        @PathVariable orderId: Long
    ): ResponseEntity<String> {
        return ResponseEntity.ok("User: $userId, Order: $orderId")
    }
    
    // Path variable with regex
    @GetMapping("/products/{code:[A-Z]{2}-\\d{4}}")
    fun getProductByCode(@PathVariable code: String): ResponseEntity<String> {
        return ResponseEntity.ok("Product code: $code")
    }
    
    // Optional path variable
    @GetMapping("/categories", "/categories/{id}")
    fun getCategories(@PathVariable(required = false) id: Long?): ResponseEntity<String> {
        return if (id != null) {
            ResponseEntity.ok("Category ID: $id")
        } else {
            ResponseEntity.ok("All categories")
        }
    }
    
    // Request parameters
    @GetMapping("/search")
    fun search(
        @RequestParam query: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) category: String?,
        @RequestParam allParams: Map<String, String>
    ): ResponseEntity<String> {
        val response = buildString {
            appendLine("Query: $query")
            appendLine("Page: $page")
            appendLine("Size: $size")
            appendLine("Category: $category")
            append("All params: $allParams")
        }
        
        return ResponseEntity.ok(response)
    }
}
```

## 3.4 Request and Response Handling

### Request Body Processing

```kotlin
@RestController
@RequestMapping("/api/requests")
class RequestHandlingController(
    private val userService: UserService,
    private val contactService: ContactService,
    private val fileService: FileService,
    private val webhookService: WebhookService
) {
    
    // JSON request body
    @PostMapping("/users")
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserDto> {
        val user = userService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }
    
    // XML request body
    @PostMapping(value = ["/users/xml"], consumes = [MediaType.APPLICATION_XML_VALUE])
    fun createUserFromXml(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserDto> {
        val user = userService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }
    
    // Form data
    @PostMapping(value = ["/contact"], consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun submitContactForm(
        @RequestParam name: String,
        @RequestParam email: String,
        @RequestParam message: String
    ): ResponseEntity<String> {
        contactService.processContact(name, email, message)
        return ResponseEntity.ok("Contact form submitted successfully")
    }
    
    // Multipart form data
    @PostMapping(value = ["/profile"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateProfile(
        @RequestPart("user") @Valid userRequest: UpdateUserRequest,
        @RequestPart(value = "avatar", required = false) avatar: MultipartFile?
    ): ResponseEntity<String> {
        avatar?.takeIf { !it.isEmpty }?.let { file ->
            val avatarUrl = fileService.uploadAvatar(file)
            userRequest.avatarUrl = avatarUrl
        }
        
        userService.updateProfile(userRequest)
        return ResponseEntity.ok("Profile updated successfully")
    }
    
    // Raw request body
    @PostMapping("/webhook")
    fun handleWebhook(
        @RequestBody rawBody: String,
        @RequestHeader("X-Signature") signature: String,
        request: HttpServletRequest
    ): ResponseEntity<String> {
        if (!webhookService.verifySignature(rawBody, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature")
        }
        
        webhookService.processWebhook(rawBody)
        return ResponseEntity.ok("Webhook processed")
    }
}
```

## 3.5 Data Binding and Validation

Data binding and validation are crucial aspects of web applications that ensure data integrity and provide a good user experience. Spring MVC provides comprehensive support for both through annotations and custom validators.

### Request DTOs with Validation

```kotlin
data class CreateUserRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,
    
    @field:NotBlank(message = "First name is required")
    @field:Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    val firstName: String,
    
    @field:NotBlank(message = "Last name is required")
    @field:Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    val lastName: String,
    
    @field:Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    val phoneNumber: String? = null,
    
    @field:NotNull(message = "Status is required")
    val status: UserStatus = UserStatus.ACTIVE,
    
    @field:Past(message = "Date of birth must be in the past")
    val dateOfBirth: LocalDate? = null,
    
    @field:Valid
    val address: AddressDto? = null
)

data class AddressDto(
    @field:NotBlank(message = "Street is required")
    val street: String,
    
    @field:NotBlank(message = "City is required")
    val city: String,
    
    @field:NotBlank(message = "Country is required")
    val country: String,
    
    @field:Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid postal code format")
    val postalCode: String
)

data class UpdateUserRequest(
    @field:Email(message = "Invalid email format")
    val email: String? = null,
    
    @field:Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    val firstName: String? = null,
    
    @field:Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    val lastName: String? = null,
    
    @field:Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    val phoneNumber: String? = null,
    
    val status: UserStatus? = null,
    
    var avatarUrl: String? = null
)
```

### Custom Validators

Custom validators allow you to implement business-specific validation logic that goes beyond the standard Bean Validation annotations. They're particularly useful for validating data against database constraints or complex business rules.

```kotlin
// Custom validation annotation
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UniqueEmailValidator::class])
annotation class UniqueEmail(
    val message: String = "Email already exists",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

// Validator implementation
@Component
class UniqueEmailValidator(
    private val userRepository: UserRepository
) : ConstraintValidator<UniqueEmail, String> {
    
    override fun isValid(email: String?, context: ConstraintValidatorContext): Boolean {
        return email == null || !userRepository.existsByEmail(email)
    }
}

// Usage in DTO
data class CreateUserRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    @field:UniqueEmail
    val email: String,
    // other fields...
)
```

### Controller with Validation

```kotlin
@RestController
@RequestMapping("/api/users")
@Validated
class UserController(
    private val userService: UserService
) {
    
    @PostMapping
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserDto> {
        val user = userService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }
    
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable @Positive id: Long,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserDto> {
        val user = userService.updateUser(id, request)
        return ResponseEntity.ok(user)
    }
    
    // Validation groups
    @PostMapping("/admin")
    fun createAdminUser(@Validated(AdminCreation::class) @RequestBody request: CreateUserRequest): ResponseEntity<UserDto> {
        val user = userService.createAdminUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }
}

// Validation groups
interface AdminCreation
interface UserUpdate
```

## 3.6 Exception Handling

Exception handling is a critical aspect of web applications that ensures graceful error responses and prevents sensitive information leakage. Spring MVC provides several mechanisms for handling exceptions at different levels.

### Global Exception Handler

Global exception handlers using @RestControllerAdvice provide a centralized way to handle exceptions across your entire application. This approach promotes consistency and reduces code duplication.

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {
    
    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }
    
    // Validation errors
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = mutableMapOf<String, String>()
        
        ex.bindingResult.fieldErrors.forEach { error ->
            errors[error.field] = error.defaultMessage ?: "Invalid value"
        }
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = "Validation failed",
            errors = errors,
            timestamp = LocalDateTime.now()
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    // Constraint violation
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val errors = ex.constraintViolations.associate { violation ->
            violation.propertyPath.toString() to (violation.message ?: "Constraint violation")
        }
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = "Constraint violation",
            errors = errors,
            timestamp = LocalDateTime.now()
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    // Entity not found
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFound(ex: EntityNotFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Entity not found",
            timestamp = LocalDateTime.now()
        )
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }
    
    // Business logic exceptions
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.UNPROCESSABLE_ENTITY.value(),
            message = ex.message ?: "Business logic error",
            code = ex.errorCode,
            timestamp = LocalDateTime.now()
        )
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse)
    }
    
    // Data integrity violation
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): ResponseEntity<ErrorResponse> {
        log.error("Data integrity violation", ex)
        
        val message = when {
            ex.message?.contains("Duplicate entry") == true -> "Resource already exists"
            ex.message?.contains("foreign key constraint") == true -> "Referenced resource not found"
            else -> "Data integrity violation"
        }
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            message = message,
            timestamp = LocalDateTime.now()
        )
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }
    
    // Access denied
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.FORBIDDEN.value(),
            message = "Access denied",
            timestamp = LocalDateTime.now()
        )
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }
    
    // Generic exception
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected error", ex)
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "Internal server error",
            timestamp = LocalDateTime.now()
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}

// Custom exceptions
class BusinessException(
    message: String,
    val errorCode: String? = null
) : RuntimeException(message)

class EntityNotFoundException(message: String) : RuntimeException(message)

// Error response DTO
data class ErrorResponse(
    val status: Int,
    val message: String,
    val code: String? = null,
    val errors: Map<String, String>? = null,
    val timestamp: LocalDateTime,
    val path: String? = null
)
```

## 3.7 Interceptors

Interceptors provide a powerful mechanism to perform cross-cutting operations before and after request processing. They're ideal for logging, authentication, authorization, and performance monitoring.

```kotlin
@Component
class LoggingInterceptor : HandlerInterceptor {
    
    companion object {
        private val log = LoggerFactory.getLogger(LoggingInterceptor::class.java)
    }
    
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val startTime = System.currentTimeMillis()
        request.setAttribute("startTime", startTime)
        
        log.info("Request: ${request.method} ${request.requestURI}")
        return true
    }
    
    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?
    ) {
        log.info("Response status: ${response.status}")
    }
    
    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        val startTime = request.getAttribute("startTime") as Long
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime
        
        log.info("Request completed in ${executionTime}ms")
        
        ex?.let {
            log.error("Request failed with exception", it)
        }
    }
}

@Component
class AuthenticationInterceptor : HandlerInterceptor {
    
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val authHeader = request.getHeader("Authorization")
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("{\"error\":\"Missing or invalid authorization header\"}")
            return false
        }
        
        val token = authHeader.substring(7)
        if (!isValidToken(token)) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("{\"error\":\"Invalid token\"}")
            return false
        }
        
        // Set user context
        request.setAttribute("userId", extractUserIdFromToken(token))
        return true
    }
    
    private fun isValidToken(token: String): Boolean {
        // Token validation logic
        return token.isNotBlank()
    }
    
    private fun extractUserIdFromToken(token: String): String {
        // Extract user ID from token
        return "user123"
    }
}
```

## 3.8 File Upload and Download

File handling is a common requirement in web applications. Spring MVC provides comprehensive support for file uploads and downloads with proper error handling and security considerations.

```kotlin
@RestController
@RequestMapping("/api/files")
class FileController(
    private val fileService: FileService
) {
    
    // Single file upload
    @PostMapping("/upload")
    fun uploadFile(
        @RequestParam("file") file: MultipartFile,
        @RequestParam(required = false) description: String?
    ): ResponseEntity<FileUploadResponse> {
        
        if (file.isEmpty) {
            throw BusinessException("File is empty")
        }
        
        val allowedTypes = setOf("image/jpeg", "image/png", "application/pdf")
        if (file.contentType !in allowedTypes) {
            throw BusinessException("File type not allowed: ${file.contentType}")
        }
        
        val maxSize = 5 * 1024 * 1024 // 5MB
        if (file.size > maxSize) {
            throw BusinessException("File size exceeds maximum allowed size")
        }
        
        val uploadedFile = fileService.uploadFile(file, description)
        
        val response = FileUploadResponse(
            id = uploadedFile.id,
            filename = uploadedFile.filename,
            size = uploadedFile.size,
            contentType = uploadedFile.contentType,
            url = "/api/files/download/${uploadedFile.id}"
        )
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    // Multiple files upload
    @PostMapping("/upload/multiple")
    fun uploadMultipleFiles(
        @RequestParam("files") files: Array<MultipartFile>
    ): ResponseEntity<List<FileUploadResponse>> {
        
        if (files.isEmpty()) {
            throw BusinessException("No files provided")
        }
        
        val responses = files.map { file ->
            val uploadedFile = fileService.uploadFile(file, null)
            FileUploadResponse(
                id = uploadedFile.id,
                filename = uploadedFile.filename,
                size = uploadedFile.size,
                contentType = uploadedFile.contentType,
                url = "/api/files/download/${uploadedFile.id}"
            )
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responses)
    }
    
    // File download
    @GetMapping("/download/{id}")
    fun downloadFile(@PathVariable id: Long): ResponseEntity<Resource> {
        val fileInfo = fileService.getFileInfo(id)
        val resource = fileService.loadFileAsResource(fileInfo.filename)
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${fileInfo.originalFilename}\"")
            .header(HttpHeaders.CONTENT_TYPE, fileInfo.contentType)
            .body(resource)
    }
    
    // Streaming download for large files
    @GetMapping("/stream/{id}")
    fun streamFile(@PathVariable id: Long): ResponseEntity<StreamingResponseBody> {
        val fileInfo = fileService.getFileInfo(id)
        
        val streamingBody = StreamingResponseBody { outputStream ->
            fileService.streamFile(fileInfo.filename, outputStream)
        }
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${fileInfo.originalFilename}\"")
            .header(HttpHeaders.CONTENT_TYPE, fileInfo.contentType)
            .header(HttpHeaders.CONTENT_LENGTH, fileInfo.size.toString())
            .body(streamingBody)
    }
}

// DTOs
data class FileUploadResponse(
    val id: Long,
    val filename: String,
    val size: Long,
    val contentType: String,
    val url: String
)

data class UploadedFileInfo(
    val id: Long,
    val filename: String,
    val originalFilename: String,
    val size: Long,
    val contentType: String,
    val description: String?
)
```

## 3.9 Content Negotiation

Content negotiation allows the same endpoint to return different representations (JSON, XML, CSV) based on client preferences expressed through Accept headers or URL parameters.

```kotlin
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    
    // JSON and XML support
    @GetMapping(
        value = ["/{id}"],
        produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE]
    )
    fun getUser(@PathVariable id: Long): ResponseEntity<UserDto> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(user)
    }
    
    // Different response based on accept header
    @GetMapping("/report/{id}")
    fun getUserReport(@PathVariable id: Long): ResponseEntity<*> {
        return userService.getUserReport(id)
    }
}

@Service
class UserService {
    
    fun getUserReport(id: Long): ResponseEntity<*> {
        val request = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
        val httpRequest = request.request
        val acceptHeader = httpRequest.getHeader("Accept")
        
        val user = getUserById(id)
        
        return when {
            acceptHeader?.contains("application/pdf") == true -> {
                val pdfBytes = generatePdfReport(user)
                ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .body(pdfBytes)
            }
            acceptHeader?.contains("text/csv") == true -> {
                val csvData = generateCsvReport(user)
                ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                    .body(csvData)
            }
            else -> ResponseEntity.ok(user)
        }
    }
}
```

## 3.10 Testing

Testing web controllers is essential for ensuring your API behaves correctly. Spring provides excellent testing support with MockMvc for integration testing.

### Controller Testing

```kotlin
@WebMvcTest(UserController::class)
class UserControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @MockBean
    private lateinit var userService: UserService
    
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    
    @Test
    fun `should create user successfully`() {
        // Given
        val request = CreateUserRequest(
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe"
        )
        
        val expectedUser = UserDto(
            id = 1L,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe"
        )
        
        every { userService.createUser(request) } returns expectedUser
        
        // When & Then
        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpected(jsonPath("$.id").value(1))
            .andExpected(jsonPath("$.email").value("test@example.com"))
            .andExpected(jsonPath("$.firstName").value("John"))
            .andExpected(jsonPath("$.lastName").value("Doe"))
    }
    
    @Test
    fun `should return validation error for invalid request`() {
        // Given
        val invalidRequest = CreateUserRequest(
            email = "invalid-email",
            firstName = "",
            lastName = "Doe"
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpected(status().isBadRequest)
            .andExpected(jsonPath("$.message").value("Validation failed"))
            .andExpected(jsonPath("$.errors.email").exists())
            .andExpected(jsonPath("$.errors.firstName").exists())
    }
    
    @Test
    fun `should get user by id`() {
        // Given
        val userId = 1L
        val expectedUser = UserDto(
            id = userId,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe"
        )
        
        every { userService.getUserById(userId) } returns expectedUser
        
        // When & Then
        mockMvc.perform(get("/api/users/$userId"))
            .andExpected(status().isOk)
            .andExpected(jsonPath("$.id").value(userId))
            .andExpected(jsonPath("$.email").value("test@example.com"))
    }
    
    @Test
    fun `should return 404 when user not found`() {
        // Given
        val userId = 999L
        every { userService.getUserById(userId) } throws EntityNotFoundException("User not found")
        
        // When & Then
        mockMvc.perform(get("/api/users/$userId"))
            .andExpected(status().isNotFound)
            .andExpected(jsonPath("$.message").value("User not found"))
    }
}
```

---

# 4. Spring Security Basics

## 4.1 Introduction and Setup

Spring Security is a powerful and highly customizable authentication and access-control framework for Spring applications. It provides comprehensive security services including authentication, authorization, protection against attacks like session fixation, clickjacking, cross site request forgery, and more.

The framework is built around the concept of authentication (who you are) and authorization (what you're allowed to do). It uses a series of filters to intercept requests and apply security constraints before they reach your application code.

### Dependencies

Add to `build.gradle.kts`:
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.security:spring-security-test")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
}
```

### Basic Security Configuration

```kotlin
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig {
    
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
    
    @Bean
    fun authenticationManager(
        authConfig: AuthenticationConfiguration
    ): AuthenticationManager {
        return authConfig.authenticationManager
    }
    
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { 
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) 
            }
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .httpBasic { }
            .build()
    }
}
```

## 4.2 Authentication

Authentication is the process of verifying the identity of a user. Spring Security supports various authentication mechanisms including form-based authentication, HTTP Basic authentication, and token-based authentication.

### User Entity and Repository

```kotlin
@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(unique = true, nullable = false)
    val username: String,
    
    @Column(unique = true, nullable = false)
    val email: String,
    
    @Column(nullable = false)
    val password: String,
    
    @Column(nullable = false)
    val enabled: Boolean = true,
    
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "role")
    val roles: Set<Role> = setOf(Role.USER)
)

enum class Role {
    USER, ADMIN, MODERATOR
}

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
}
```

### Custom UserDetailsService

The UserDetailsService is a core interface in Spring Security that loads user-specific data. By implementing this interface, you can integrate with any user storage system.

```kotlin
@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")
        
        return CustomUserPrincipal(user)
    }
}

class CustomUserPrincipal(
    private val user: User
) : UserDetails {
    
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return user.roles.map { SimpleGrantedAuthority("ROLE_${it.name}") }
    }
    
    override fun getPassword(): String = user.password
    override fun getUsername(): String = user.username
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = user.enabled
    
    fun getId(): Long = user.id
    fun getEmail(): String = user.email
}
```

### Authentication Controller

```kotlin
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val userService: UserService,
    private val jwtTokenProvider: JwtTokenProvider
) {
    
    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<JwtResponse> {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.username,
                loginRequest.password
            )
        )
        
        SecurityContextHolder.getContext().authentication = authentication
        val userPrincipal = authentication.principal as CustomUserPrincipal
        val jwt = jwtTokenProvider.generateToken(userPrincipal)
        
        return ResponseEntity.ok(
            JwtResponse(
                token = jwt,
                type = "Bearer",
                id = userPrincipal.getId(),
                username = userPrincipal.username,
                email = userPrincipal.getEmail(),
                roles = userPrincipal.authorities.map { it.authority }
            )
        )
    }
    
    @PostMapping("/register")
    fun register(@Valid @RequestBody signUpRequest: SignUpRequest): ResponseEntity<MessageResponse> {
        if (userService.existsByUsername(signUpRequest.username)) {
            return ResponseEntity.badRequest()
                .body(MessageResponse("Error: Username is already taken!"))
        }
        
        if (userService.existsByEmail(signUpRequest.email)) {
            return ResponseEntity.badRequest()
                .body(MessageResponse("Error: Email is already in use!"))
        }
        
        userService.createUser(signUpRequest)
        
        return ResponseEntity.ok(MessageResponse("User registered successfully!"))
    }
    
    @PostMapping("/logout")
    fun logout(): ResponseEntity<MessageResponse> {
        SecurityContextHolder.clearContext()
        return ResponseEntity.ok(MessageResponse("Logged out successfully!"))
    }
}

// DTOs
data class LoginRequest(
    @field:NotBlank
    val username: String,
    
    @field:NotBlank
    val password: String
)

data class SignUpRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 20)
    val username: String,
    
    @field:NotBlank
    @field:Email
    val email: String,
    
    @field:NotBlank
    @field:Size(min = 6, max = 40)
    val password: String,
    
    val roles: Set<String> = setOf("user")
)

data class JwtResponse(
    val token: String,
    val type: String,
    val id: Long,
    val username: String,
    val email: String,
    val roles: List<String>
)

data class MessageResponse(
    val message: String
)
```

## 4.3 Authorization

Authorization determines what an authenticated user is allowed to do. Spring Security provides several mechanisms for authorization including role-based access control, permission-based access control, and method-level security.

### Role-Based Access Control

```kotlin
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    
    // Only authenticated users can access
    @GetMapping("/profile")
    fun getCurrentUserProfile(authentication: Authentication): ResponseEntity<UserProfileDto> {
        val userPrincipal = authentication.principal as CustomUserPrincipal
        val profile = userService.getUserProfile(userPrincipal.getId())
        return ResponseEntity.ok(profile)
    }
    
    // Only admins can access
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers(pageable: Pageable): ResponseEntity<Page<UserDto>> {
        val users = userService.getAllUsers(pageable)
        return ResponseEntity.ok(users)
    }
    
    // Users can only access their own data or admins can access any
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserDto> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(user)
    }
    
    // Only admins can delete users
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }
}
```

## 4.4 Password Encoding

Password encoding is crucial for security. Spring Security provides various password encoders, with BCrypt being the most commonly used due to its adaptive nature and built-in salt generation.

### Password Encoder Configuration

```kotlin
@Configuration
class PasswordConfig {
    
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(12) // Cost factor of 12
    }
    
    // Alternative: Argon2 (more secure but newer)
    @Bean
    fun argon2PasswordEncoder(): PasswordEncoder {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()
    }
    
    // For legacy systems with multiple encoding schemes
    @Bean
    fun delegatingPasswordEncoder(): PasswordEncoder {
        val encoders = mutableMapOf<String, PasswordEncoder>()
        encoders["bcrypt"] = BCryptPasswordEncoder()
        encoders["argon2"] = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()
        encoders["sha256"] = StandardPasswordEncoder() // Deprecated, for legacy support only
        
        val passwordEncoder = DelegatingPasswordEncoder("bcrypt", encoders)
        passwordEncoder.setDefaultPasswordEncoderForMatches(BCryptPasswordEncoder())
        return passwordEncoder
    }
}
```

### Password Change Controller

```kotlin
@RestController
@RequestMapping("/api/auth")
class PasswordController(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder
) {
    
    @PostMapping("/change-password")
    fun changePassword(
        @Valid @RequestBody request: ChangePasswordRequest,
        authentication: Authentication
    ): ResponseEntity<MessageResponse> {
        val userPrincipal = authentication.principal as CustomUserPrincipal
        val user = userService.findById(userPrincipal.getId())
        
        // Verify current password
        if (!passwordEncoder.matches(request.currentPassword, user.password)) {
            return ResponseEntity.badRequest()
                .body(MessageResponse("Current password is incorrect"))
        }
        
        // Update password
        userService.updatePassword(user.id, passwordEncoder.encode(request.newPassword))
        
        return ResponseEntity.ok(MessageResponse("Password changed successfully"))
    }
    
    @PostMapping("/forgot-password")
    fun forgotPassword(@Valid @RequestBody request: ForgotPasswordRequest): ResponseEntity<MessageResponse> {
        val user = userService.findByEmail(request.email)
            ?: return ResponseEntity.badRequest()
                .body(MessageResponse("Email not found"))
        
        val resetToken = userService.generatePasswordResetToken(user.id)
        // Send email with reset token (implementation not shown)
        
        return ResponseEntity.ok(MessageResponse("Password reset email sent"))
    }
    
    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<MessageResponse> {
        if (!userService.validatePasswordResetToken(request.token)) {
            return ResponseEntity.badRequest()
                .body(MessageResponse("Invalid or expired reset token"))
        }
        
        val userId = userService.getUserIdFromResetToken(request.token)
        userService.updatePassword(userId, passwordEncoder.encode(request.newPassword))
        userService.invalidatePasswordResetToken(request.token)
        
        return ResponseEntity.ok(MessageResponse("Password reset successfully"))
    }
}

// DTOs
data class ChangePasswordRequest(
    @field:NotBlank
    val currentPassword: String,
    
    @field:NotBlank
    @field:Size(min = 6, max = 40)
    val newPassword: String
)

data class ForgotPasswordRequest(
    @field:NotBlank
    @field:Email
    val email: String
)

data class ResetPasswordRequest(
    @field:NotBlank
    val token: String,
    
    @field:NotBlank
    @field:Size(min = 6, max = 40)
    val newPassword: String
)
```

## 4.5 JWT Authentication

JSON Web Tokens (JWT) provide a stateless way to handle authentication in modern web applications. JWTs are self-contained tokens that carry all the information needed to authenticate a user.

### JWT Token Provider

```kotlin
@Component
class JwtTokenProvider {
    
    companion object {
        private val log = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    }
    
    @Value("\${app.jwtSecret}")
    private lateinit var jwtSecret: String
    
    @Value("\${app.jwtExpirationInMs}")
    private var jwtExpirationInMs: Int = 86400000 // 1 day
    
    fun generateToken(userPrincipal: CustomUserPrincipal): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationInMs)
        
        return Jwts.builder()
            .setSubject(userPrincipal.getId().toString())
            .claim("username", userPrincipal.username)
            .claim("email", userPrincipal.getEmail())
            .claim("roles", userPrincipal.authorities.map { it.authority })
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact()
    }
    
    fun getUserIdFromJWT(token: String): Long {
        val claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .body
        
        return claims.subject.toLong()
    }
    
    fun validateToken(authToken: String): Boolean {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken)
            return true
        } catch (ex: SignatureException) {
            log.error("Invalid JWT signature")
        } catch (ex: MalformedJwtException) {
            log.error("Invalid JWT token")
        } catch (ex: ExpiredJwtException) {
            log.error("Expired JWT token")
        } catch (ex: UnsupportedJwtException) {
            log.error("Unsupported JWT token")
        } catch (ex: IllegalArgumentException) {
            log.error("JWT claims string is empty")
        }
        return false
    }
    
    fun getClaimsFromJWT(token: String): Claims {
        return Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .body
    }
}
```

### JWT Authentication Filter

```kotlin
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val customUserDetailsService: CustomUserDetailsService
) : OncePerRequestFilter() {
    
    companion object {
        private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
    }
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = getJwtFromRequest(request)
            
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                val userId = jwtTokenProvider.getUserIdFromJWT(jwt)
                val userDetails = customUserDetailsService.loadUserByUsername(userId.toString())
                
                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.authorities
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (ex: Exception) {
            log.error("Could not set user authentication in security context", ex)
        }
        
        filterChain.doFilter(request, response)
    }
    
    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
}
```

## 4.6 Method-Level Security

Method-level security allows you to apply security constraints directly to service methods, providing fine-grained control over authorization.

### Securing Service Methods

```kotlin
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository
) {
    
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers(pageable: Pageable): Page<UserDto> {
        return userRepository.findAll(pageable).map { it.toDto() }
    }
    
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    fun getUserById(id: Long): UserDto {
        val user = userRepository.findById(id)
            .orElseThrow { EntityNotFoundException("User not found") }
        return user.toDto()
    }
    
    @PostAuthorize("hasRole('ADMIN') or returnObject.id == authentication.principal.id")
    fun findUserByEmail(email: String): UserDto {
        val user = userRepository.findByEmail(email)
            ?: throw EntityNotFoundException("User not found")
        return user.toDto()
    }
    
    @PreFilter("hasRole('ADMIN') or filterObject.id == authentication.principal.id")
    fun updateUsers(users: List<UpdateUserRequest>): List<UserDto> {
        return users.map { request ->
            val user = userRepository.findById(request.id)
                .orElseThrow { EntityNotFoundException("User not found") }
            // Update user logic
            user.toDto()
        }
    }
    
    @PostFilter("hasRole('ADMIN') or filterObject.id == authentication.principal.id")
    fun searchUsers(criteria: String): List<UserDto> {
        return userRepository.findByFirstNameContainingIgnoreCase(criteria)
            .map { it.toDto() }
    }
}
```

### Custom Security Expressions

```kotlin
@Component("userSecurity")
class UserSecurityService {
    
    fun isOwnerOrAdmin(userId: Long, authentication: Authentication): Boolean {
        val userPrincipal = authentication.principal as? CustomUserPrincipal ?: return false
        
        return userPrincipal.getId() == userId || 
               userPrincipal.authorities.any { it.authority == "ROLE_ADMIN" }
    }
    
    fun canAccessResource(resourceId: Long, authentication: Authentication): Boolean {
        // Custom logic to check if user can access a specific resource
        val userPrincipal = authentication.principal as? CustomUserPrincipal ?: return false
        
        // Check if user is admin
        if (userPrincipal.authorities.any { it.authority == "ROLE_ADMIN" }) {
            return true
        }
        
        // Check if user owns the resource (implementation depends on your domain)
        return checkResourceOwnership(resourceId, userPrincipal.getId())
    }
    
    private fun checkResourceOwnership(resourceId: Long, userId: Long): Boolean {
        // Implementation depends on your domain model
        return true // Simplified for example
    }
}

// Usage in controller
@RestController
@RequestMapping("/api/resources")
class ResourceController {
    
    @GetMapping("/{id}")
    @PreAuthorize("@userSecurity.canAccessResource(#id, authentication)")
    fun getResource(@PathVariable id: Long): ResponseEntity<ResourceDto> {
        // Method implementation
        return ResponseEntity.ok(ResourceDto())
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("@userSecurity.isOwnerOrAdmin(#id, authentication)")
    fun updateResource(
        @PathVariable id: Long,
        @RequestBody request: UpdateResourceRequest
    ): ResponseEntity<ResourceDto> {
        // Method implementation
        return ResponseEntity.ok(ResourceDto())
    }
}
```

## 4.7 Security Testing

Security testing ensures that your security configurations work as expected and that unauthorized access is properly blocked.

### Security Test Configuration

```kotlin
@TestConfiguration
class TestSecurityConfig {
    
    @Bean
    @Primary
    fun testPasswordEncoder(): PasswordEncoder {
        // Use NoOpPasswordEncoder for tests to avoid BCrypt overhead
        return NoOpPasswordEncoder.getInstance()
    }
}
```

### Controller Security Tests

```kotlin
@WebMvcTest(UserController::class)
@Import(TestSecurityConfig::class)
class UserControllerSecurityTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @MockBean
    private lateinit var userService: UserService
    
    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `admin can access all users`() {
        every { userService.getAllUsers(any()) } returns Page.empty()
        
        mockMvc.perform(get("/api/users/all"))
            .andExpected(status().isOk)
    }
    
    @Test
    @WithMockUser(username = "user", roles = ["USER"])
    fun `regular user cannot access all users`() {
        mockMvc.perform(get("/api/users/all"))
            .andExpected(status().isForbidden)
    }
    
    @Test
    fun `unauthenticated user cannot access protected endpoint`() {
        mockMvc.perform(get("/api/users/profile"))
            .andExpected(status().isUnauthorized)
    }
    
    @Test
    @WithMockUser(username = "user", roles = ["USER"])
    fun `user can access own profile`() {
        val userPrincipal = CustomUserPrincipal(
            User(id = 1L, username = "user", email = "user@example.com", password = "password")
        )
        
        every { userService.getUserProfile(1L) } returns UserProfileDto()
        
        mockMvc.perform(get("/api/users/profile"))
            .andExpected(status().isOk)
    }
}

// Custom security test annotation
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory::class)
annotation class WithMockCustomUser(
    val id: Long = 1L,
    val username: String = "user",
    val email: String = "user@example.com",
    val roles: Array<String> = ["USER"]
)

class WithMockCustomUserSecurityContextFactory : WithSecurityContextFactory<WithMockCustomUser> {
    
    override fun createSecurityContext(annotation: WithMockCustomUser): SecurityContext {
        val user = User(
            id = annotation.id,
            username = annotation.username,
            email = annotation.email,
            password = "password",
            roles = annotation.roles.map { Role.valueOf(it) }.toSet()
        )
        
        val principal = CustomUserPrincipal(user)
        val authorities = annotation.roles.map { SimpleGrantedAuthority("ROLE_$it") }
        val auth = UsernamePasswordAuthenticationToken(principal, null, authorities)
        
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = auth
        return context
    }
}
```

## 4.8 CORS Configuration

### Understanding CORS
Cross-Origin Resource Sharing (CORS) is a security mechanism that allows web applications running on one domain to access resources from another domain. Modern browsers enforce the same-origin policy by default, which blocks cross-origin requests for security reasons. CORS provides a way to relax this restriction in a controlled manner.

When a web application makes a cross-origin request, the browser first sends a preflight request (for certain types of requests) to check if the server allows the cross-origin request. The server responds with appropriate CORS headers indicating what is allowed.

### CORS Configuration in Spring Security

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig {
    
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            // Allow specific origins (production approach)
            allowedOrigins = listOf(
                "https://myapp.com",
                "https://www.myapp.com",
                "http://localhost:3000", // For development
                "http://localhost:8080"
            )
            
            // Allow specific HTTP methods
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            
            // Allow specific headers
            allowedHeaders = listOf(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
            )
            
            // Allow credentials (cookies, authorization headers)
            allowCredentials = true
            
            // How long the browser can cache preflight response
            maxAge = 3600L
            
            // Headers that client can access
            exposedHeaders = listOf("X-Total-Count", "X-Page-Number")
        }
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
    
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            // ... other security configuration
            .build()
    }
}

// Alternative: Controller-level CORS
@RestController
@CrossOrigin(
    origins = ["http://localhost:3000", "https://myapp.com"],
    methods = [RequestMethod.GET, RequestMethod.POST],
    allowedHeaders = ["*"],
    allowCredentials = "true",
    maxAge = 3600
)
class ApiController {
    
    @GetMapping("/data")
    fun getData(): ResponseEntity<List<String>> {
        return ResponseEntity.ok(listOf("data1", "data2"))
    }
    
    // Method-level CORS (overrides class-level)
    @PostMapping("/upload")
    @CrossOrigin(origins = ["https://trusted-uploader.com"])
    fun uploadData(@RequestBody data: String): ResponseEntity<String> {
        return ResponseEntity.ok("uploaded")
    }
}
```

## 4.9 Security Best Practices

### Password Security
**Use Strong Password Encoding**: Always use BCrypt, Argon2, or similar adaptive hashing functions. Never store passwords in plain text or use fast hashing algorithms like MD5 or SHA-1.

**Implement Password Policies**: Enforce minimum password complexity, prevent password reuse, and implement account lockout policies after failed attempts.

**Consider Multi-Factor Authentication**: Add an extra layer of security beyond passwords, especially for sensitive operations.

### Session Management
**Use Stateless Authentication**: For REST APIs, prefer stateless authentication with JWT tokens instead of server-side sessions to improve scalability.

**Secure Session Configuration**: If using sessions, configure secure session cookies, appropriate timeout values, and session fixation protection.

**Token Expiration**: Implement reasonable token expiration times and refresh token mechanisms for long-lived applications.

### Input Validation and Sanitization
**Validate All Input**: Never trust user input. Validate, sanitize, and escape all data coming from external sources.

**Use Parameterized Queries**: Prevent SQL injection by using parameterized queries or ORM frameworks that handle this automatically.

**Implement Rate Limiting**: Protect against brute force attacks and DoS attempts by implementing rate limiting on authentication endpoints.

### Error Handling
**Don't Leak Information**: Avoid exposing sensitive information in error messages. Generic error messages prevent information disclosure attacks.

**Log Security Events**: Implement comprehensive security logging for authentication failures, authorization violations, and suspicious activities.

### HTTPS and Transport Security
**Use HTTPS Everywhere**: Encrypt all communication between client and server, especially for authentication and sensitive data.

**Implement HSTS**: Use HTTP Strict Transport Security headers to prevent protocol downgrade attacks.

**Secure Headers**: Implement security headers like X-Frame-Options, X-Content-Type-Options, and Content-Security-Policy.

### Configuration Security
**Externalize Configuration**: Keep sensitive configuration (database passwords, API keys) in external configuration files or environment variables.

**Use Secrets Management**: For production environments, use dedicated secrets management solutions like HashiCorp Vault or cloud provider secret services.

**Regular Updates**: Keep all dependencies updated, especially security-related libraries, and subscribe to security advisories.

```kotlin
// Example of comprehensive security configuration
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class ProductionSecurityConfig {
    
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() } // Disabled for stateless API
            .headers { headers ->
                headers
                    .frameOptions().deny() // Prevent clickjacking
                    .contentTypeOptions().and() // Prevent MIME sniffing
                    .httpStrictTransportSecurity { hstsConfig ->
                        hstsConfig
                            .maxAgeInSeconds(31536000) // 1 year
                            .includeSubdomains(true)
                    }
            }
            .sessionManagement { 
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) 
            }
            .exceptionHandling { 
                it.authenticationEntryPoint(customAuthenticationEntryPoint())
                it.accessDeniedHandler(customAccessDeniedHandler())
            }
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/public/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(rateLimitingFilter(), JwtAuthenticationFilter::class.java)
            .build()
    }
    
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        // Use BCrypt with high cost factor for production
        return BCryptPasswordEncoder(12)
    }
    
    @Bean
    fun rateLimitingFilter(): RateLimitingFilter {
        return RateLimitingFilter().apply {
            // Allow 5 login attempts per minute per IP
            setLimit("/api/auth/login", 5, Duration.ofMinutes(1))
            // Allow 1000 API calls per hour per user
            setLimit("/api/**", 1000, Duration.ofHours(1))
        }
    }
}
```

This comprehensive approach to Spring Security ensures that your application is protected against common security vulnerabilities while maintaining usability and performance. Remember that security is an ongoing process that requires regular review, updates, and monitoring.

---

# 5. Best Practices and Common Pitfalls

## Spring Boot Best Practices

### Dependency Injection
- **Prefer constructor injection** over field injection for better testability and clearer dependencies
- **Use @Primary and @Qualifier** to resolve ambiguity when multiple beans of the same type exist
- **Keep constructors simple** and avoid heavy operations during bean initialization
- **Use immutable dependencies** when possible by leveraging Kotlin's `val` keyword
- **Leverage Kotlin's constructor parameter properties** for concise code

### AOP
- **Keep aspects focused** on single concerns to maintain clarity and avoid coupling
- **Use appropriate advice types** (@Before, @After, @Around) based on your needs
- **Be careful with @Around advice** to maintain method contracts and handle exceptions properly
- **Test aspects thoroughly** as they can be hard to debug when issues arise
- **Use Kotlin's null safety features** to prevent null pointer exceptions in aspects

### Events
- **Use events for loose coupling** between components rather than direct method calls
- **Make events immutable** by using Kotlin data classes with `val` properties
- **Consider using @Async** for non-critical event handling to improve performance
- **Use @TransactionalEventListener** for database-related events to ensure consistency
- **Keep event handlers lightweight** to avoid blocking the publisher thread

## Spring Data JPA Best Practices

### Entity Design
- **Use appropriate fetch types**: LAZY for collections, EAGER sparingly to avoid N+1 problems
- **Implement proper equals() and hashCode()**: Essential for entity collections and caching
- **Use DTOs for API responses**: Don't expose entity classes directly to prevent data leakage
- **Handle N+1 problems**: Use @BatchSize, @Fetch, or JOIN FETCH queries
- **Use projections** for read-only queries to improve performance with large result sets

### Transaction Management
- **Keep transactions short**: Long-running transactions hold locks and reduce concurrency
- **Use appropriate isolation levels**: Choose the lowest level that meets your consistency requirements
- **Handle exceptions properly**: Configure rollback rules appropriately for business exceptions
- **Use read-only transactions**: For query-heavy operations to enable database optimizations
- **Test transaction behavior**: Verify rollback scenarios and propagation behavior

### Query Optimization
- **Use pagination** for large result sets to prevent memory issues
- **Implement proper exception handling**: Handle DataIntegrityViolationException and OptimisticLockException
- **Monitor query performance**: Enable SQL logging in development and use query analysis tools
- **Use connection pooling**: Configure HikariCP properly for production environments
- **Validate input data**: Use Bean Validation annotations to prevent invalid data

## Spring Web MVC Best Practices

### Controller Design
- **Use appropriate HTTP status codes**: 200, 201, 204, 400, 401, 403, 404, 409, 500
- **Follow RESTful conventions**: Use proper HTTP methods and resource naming
- **Implement proper validation**: Use Bean Validation and custom validators
- **Handle exceptions gracefully**: Use @ControllerAdvice for global exception handling
- **Use DTOs consistently**: Don't expose internal data structures through APIs

### API Design
- **Implement pagination**: For endpoints that return collections
- **Use appropriate response types**: ResponseEntity for fine-grained control
- **Secure your endpoints**: Implement proper authentication and authorization
- **Document your API**: Use OpenAPI/Swagger annotations for clear documentation
- **Version your APIs**: Plan for backward compatibility and deprecation strategies

### Performance
- **Use caching**: Implement appropriate caching strategies for frequently accessed data
- **Optimize database queries**: Use proper indexing and query optimization techniques
- **Implement rate limiting**: Protect against abuse and ensure fair resource usage
- **Monitor application metrics**: Track response times, error rates, and resource usage
- **Use connection pooling**: Configure appropriate pool sizes for database connections

## Spring Security Best Practices

### Authentication
- **Use strong password encoding**: BCrypt or Argon2 with appropriate cost factors
- **Implement account lockout**: Protect against brute force attacks
- **Use secure session management**: Proper session timeout and fixation protection
- **Consider multi-factor authentication**: For sensitive applications and operations
- **Implement proper logout**: Clear security context and invalidate sessions

### Authorization
- **Follow principle of least privilege**: Grant minimum necessary permissions
- **Use role-based access control**: Organize permissions into meaningful roles
- **Implement method-level security**: For fine-grained authorization control
- **Secure sensitive operations**: Require additional authentication for critical actions
- **Audit security events**: Log authentication attempts and authorization decisions

### Configuration
- **Use HTTPS everywhere**: Encrypt all communication, especially authentication
- **Implement security headers**: HSTS, X-Frame-Options, CSP, and others
- **Configure CORS properly**: Allow only necessary origins and methods
- **Externalize secrets**: Use environment variables or secret management systems
- **Keep dependencies updated**: Regularly update security-related libraries

## Common Pitfalls

### Spring Boot Pitfalls
1. **Circular Dependencies**: Avoid circular dependencies in constructor injection
2. **Heavy Event Handlers**: Don't perform heavy operations in synchronous event handlers
3. **Exception Handling in Aspects**: Handle exceptions properly to avoid breaking method contracts
4. **Testing Aspects**: Mock dependencies and events appropriately in tests
5. **Kotlin lateinit Variables**: Be careful with `lateinit` - prefer constructor injection

### Spring Data JPA Pitfalls
1. **LazyInitializationException**: Initialize collections within transaction boundaries
2. **N+1 Query Problem**: Use JOIN FETCH or entity graphs to avoid multiple queries
3. **Incorrect transaction boundaries**: Don't make them too large or too small
4. **Forgetting @Transactional**: Required for write operations in service layer
5. **Using CascadeType.ALL carelessly**: Can lead to unintended deletions
6. **Not handling OptimisticLockException**: Version conflicts in concurrent updates
7. **Kotlin data class issues**: Be careful with equals/hashCode in JPA entities

### Spring Web MVC Pitfalls
1. **Not handling validation errors**: Always provide meaningful error messages to clients
2. **Exposing sensitive data**: Be careful what you include in API responses
3. **Not implementing pagination**: Large result sets can cause performance and memory issues
4. **Ignoring security**: Always validate and sanitize input from external sources
5. **Poor exception handling**: Don't let exceptions bubble up unhandled to clients
6. **Not testing edge cases**: Test validation, security, and error scenarios thoroughly

### Spring Security Pitfalls
1. **Weak password policies**: Implement proper password complexity requirements
2. **Information leakage**: Don't expose sensitive information in error messages
3. **Improper session management**: Configure session timeout and security properly
4. **Missing security headers**: Implement all relevant security headers
5. **Not logging security events**: Monitor and log authentication and authorization events
6. **Hardcoded secrets**: Never commit secrets to version control
7. **Insufficient input validation**: Validate all input to prevent injection attacks

### General Kotlin/Spring Pitfalls
1. **Mixing nullable and non-nullable types**: Be consistent with Kotlin's null safety
2. **Not leveraging Kotlin features**: Use data classes, sealed classes, and extension functions appropriately
3. **Ignoring coroutines**: Consider reactive programming for I/O intensive operations
4. **Poor error handling**: Use Kotlin's Result type or proper exception handling strategies
5. **Not using Kotlin's concise syntax**: Take advantage of Kotlin's expressive language features

## Performance Considerations

### Database Performance
- **Use appropriate indexes** on frequently queried columns
- **Implement query caching** for expensive, frequently-executed queries
- **Monitor slow queries** and optimize them regularly
- **Use read replicas** for read-heavy workloads
- **Implement database connection pooling** with appropriate settings

### Application Performance
- **Use caching strategically** at multiple levels (application, database, HTTP)
- **Implement asynchronous processing** for long-running operations
- **Monitor memory usage** and garbage collection performance
- **Profile your application** regularly to identify bottlenecks
- **Use CDNs** for static content delivery

### Security Performance
- **Cache authentication results** appropriately while maintaining security
- **Use efficient password hashing** with appropriate cost factors
- **Implement rate limiting** to prevent abuse and resource exhaustion
- **Monitor security metrics** and respond to threats quickly
- **Regular security audits** and penetration testing

This comprehensive guide provides a solid foundation for building robust, secure, and performant applications with the Spring Framework using Kotlin. Remember that best practices evolve, so stay updated with the latest Spring and Kotlin developments, and always test your implementations thoroughly.