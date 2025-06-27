# Kotlin-Java Interoperability: Complete Tutorial

## Table of Contents
1. [Introduction](#introduction)
2. [Calling Java from Kotlin](#calling-java-from-kotlin)
3. [Calling Kotlin from Java](#calling-kotlin-from-java)
4. [Properties and Fields](#properties-and-fields)
5. [Static Members](#static-members)
6. [Exceptions](#exceptions)
7. [Generics](#generics)
8. [Collections](#collections)
9. [Annotations](#annotations)
10. [Default Parameters and Named Arguments](#default-parameters-and-named-arguments)
11. [Extension Functions](#extension-functions)
12. [Null Safety](#null-safety)
13. [SAM Conversions](#sam-conversions)
14. [Type Mapping](#type-mapping)
15. [Best Practices](#best-practices)

## Introduction

Kotlin is designed with Java interoperability as a first-class feature. This means you can:
- Use existing Java libraries and frameworks in Kotlin projects
- Gradually migrate Java codebases to Kotlin
- Call Kotlin code from Java applications
- Mix Kotlin and Java source files in the same project

The interoperability is so seamless that you can have Java and Kotlin classes in the same package, inherit from each other, and call each other's methods without any special syntax or wrappers. This tutorial explores all aspects of this interoperability with detailed explanations and practical examples.

## Calling Java from Kotlin

### Basic Java Class Usage

When you use Java classes in Kotlin, the experience is almost identical to using Kotlin classes. However, Kotlin adds some syntactic sugar to make the code more idiomatic.

**Java code:**
```java
// Customer.java
public class Customer {
    private String name;
    private int age;
    
    public Customer(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public void printInfo() {
        System.out.println("Customer: " + name + ", Age: " + age);
    }
}
```

**Kotlin usage:**
```kotlin
fun main() {
    // Creating Java objects works exactly like Kotlin objects
    val customer = Customer("John", 30)
    
    // Property syntax: Kotlin automatically converts getters/setters to properties
    println(customer.name) // Actually calls getName()
    customer.age = 31      // Actually calls setAge(31)
    
    // You can still call getters/setters directly if needed
    customer.setName("Jane")
    println(customer.getName())
    
    // Regular method calls work as expected
    customer.printInfo()
}
```

**Key Points:**
- Kotlin automatically recognizes Java getter/setter patterns and allows property-style access
- The pattern must follow Java conventions: `getXxx()`/`setXxx()` for regular properties, `isXxx()`/`setXxx()` for boolean properties
- This conversion happens at compile time with no runtime overhead
- You can still call the actual getter/setter methods if needed

### Java Static Methods

Static methods in Java are called directly on the class, just like in Java. Kotlin doesn't have static methods (it uses companion objects instead), but it can call Java static methods seamlessly.

**Java code:**
```java
// Utils.java
public class Utils {
    public static String formatName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }
    
    public static final String DEFAULT_SEPARATOR = ", ";
    
    public static int MAX_LENGTH = 100;
    
    private Utils() {} // Prevent instantiation
}
```

**Kotlin usage:**
```kotlin
fun main() {
    // Calling static methods - no special syntax needed
    val fullName = Utils.formatName("John", "Doe")
    println(fullName)
    
    // Accessing static final fields
    println(Utils.DEFAULT_SEPARATOR)
    
    // Accessing static non-final fields
    Utils.MAX_LENGTH = 200
    println(Utils.MAX_LENGTH)
    
    // Note: You cannot use companion object syntax with Java classes
    // This won't work: Utils.Companion.formatName()
}
```

### Java Varargs

Java's varargs are represented as arrays in Kotlin, but Kotlin provides the spread operator (`*`) to make working with them more convenient.

**Java code:**
```java
// Logger.java
public class Logger {
    public static void log(String message, Object... args) {
        System.out.printf(message + "%n", args);
    }
    
    public static int sum(int... numbers) {
        int total = 0;
        for (int n : numbers) {
            total += n;
        }
        return total;
    }
}
```

**Kotlin usage:**
```kotlin
fun main() {
    // Direct varargs call
    Logger.log("User %s logged in at %s", "John", "10:30 AM")
    
    // Using spread operator for arrays
    val args = arrayOf("Jane", "11:00 AM")
    Logger.log("User %s logged in at %s", *args)
    
    // With primitive arrays
    val numbers = intArrayOf(1, 2, 3, 4, 5)
    val total = Logger.sum(*numbers)
    println("Sum: $total")
    
    // Mixing direct arguments with spread
    val moreNumbers = intArrayOf(6, 7, 8)
    val grandTotal = Logger.sum(1, 2, 3, *moreNumbers, 9, 10)
    println("Grand Total: $grandTotal")
}
```

**Important Notes:**
- The spread operator `*` is required when passing an array to a varargs parameter
- You can mix regular arguments with spread arrays
- For primitive types, use the appropriate primitive array (`intArrayOf`, `doubleArrayOf`, etc.)

### Java Inheritance and Interfaces

Kotlin classes can extend Java classes and implement Java interfaces naturally.

**Java code:**
```java
// Vehicle.java
public abstract class Vehicle {
    protected String brand;
    
    public Vehicle(String brand) {
        this.brand = brand;
    }
    
    public abstract void start();
    
    public void honk() {
        System.out.println("Beep!");
    }
}

// Driveable.java
public interface Driveable {
    void drive(int distance);
    
    default void park() {
        System.out.println("Parking...");
    }
}
```

**Kotlin implementation:**
```kotlin
// Extending Java class and implementing Java interface
class Car(brand: String, val model: String) : Vehicle(brand), Driveable {
    
    // Overriding abstract method
    override fun start() {
        println("$brand $model is starting...")
    }
    
    // Overriding interface method
    override fun drive(distance: Int) {
        println("Driving $distance km")
    }
    
    // Default methods are inherited but can be overridden
    override fun park() {
        println("$brand $model is parking carefully")
    }
}

fun main() {
    val car = Car("Toyota", "Camry")
    car.start()
    car.honk()  // Inherited from Java class
    car.drive(100)
    car.park()
}
```

## Calling Kotlin from Java

### Basic Kotlin Class

When Java code uses Kotlin classes, some Kotlin features are adapted to work with Java's constraints.

**Kotlin code:**
```kotlin
// Person.kt
class Person(val name: String, var age: Int) {
    // Secondary constructor
    constructor(name: String) : this(name, 0)
    
    // Properties with custom accessors
    val isAdult: Boolean
        get() = age >= 18
    
    var email: String = ""
        set(value) {
            if (value.contains("@")) {
                field = value
            } else {
                throw IllegalArgumentException("Invalid email")
            }
        }
    
    // Methods
    fun greet() = "Hello, I'm $name"
    
    fun updateAge(newAge: Int) {
        if (newAge > 0) {
            age = newAge
        }
    }
    
    // Method with default parameter
    fun describe(detailed: Boolean = false): String {
        return if (detailed) {
            "Name: $name, Age: $age, Email: $email"
        } else {
            "$name ($age)"
        }
    }
}
```

**Java usage:**
```java
public class Main {
    public static void main(String[] args) {
        // Using primary constructor
        Person person = new Person("Alice", 25);
        
        // Using secondary constructor
        Person baby = new Person("Bob");
        
        // Accessing properties through getters/setters
        System.out.println(person.getName());  // val property - only getter
        person.setAge(26);                     // var property - getter and setter
        System.out.println(person.getAge());
        
        // Custom property accessors work as expected
        System.out.println(person.isAdult());  // returns boolean
        
        try {
            person.setEmail("alice@example.com");  // Custom setter
            person.setEmail("invalid-email");      // Throws exception
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        // Calling methods
        System.out.println(person.greet());
        person.updateAge(27);
        
        // Methods with default parameters - only sees method without defaults
        System.out.println(person.describe(true));
        // person.describe(); // This won't compile without @JvmOverloads
    }
}
```

### Top-Level Functions

Kotlin allows functions to be declared at the top level of a file, outside any class. When called from Java, these functions are compiled as static methods in a class named after the Kotlin file.

**Kotlin code:**
```kotlin
// StringUtils.kt
package com.example.utils

// Top-level properties
val EMPTY_STRING = ""
var debugMode = false

// Top-level functions
fun capitalize(str: String): String = 
    str.lowercase().replaceFirstChar { it.uppercase() }

fun joinStrings(vararg strings: String, separator: String = ", "): String = 
    strings.joinToString(separator)

// Top-level extension function
fun String.reverse(): String = this.reversed()

private fun internalHelper() = "Hidden from Java"
```

**Java usage:**
```java
import com.example.utils.StringUtilsKt;

public class Main {
    public static void main(String[] args) {
        // Accessing top-level functions as static methods
        String result = StringUtilsKt.capitalize("hello world");
        System.out.println(result); // "Hello world"
        
        // Varargs work naturally
        String joined = StringUtilsKt.joinStrings("a", "b", "c");
        System.out.println(joined); // "a, b, c"
        
        // Default parameters require all arguments in Java
        String custom = StringUtilsKt.joinStrings(
            new String[]{"x", "y", "z"}, " | "
        );
        
        // Top-level properties become static getters/setters
        System.out.println(StringUtilsKt.getEMPTY_STRING());
        StringUtilsKt.setDebugMode(true);
        System.out.println(StringUtilsKt.getDebugMode());
        
        // Extension functions become static methods
        String reversed = StringUtilsKt.reverse("hello");
        
        // Private functions are not accessible
        // StringUtilsKt.internalHelper(); // Compilation error
    }
}
```

### Changing File Name with @JvmName

The default class name (FileNameKt) might not be ideal. You can customize it using `@JvmName`.

**Kotlin code:**
```kotlin
// StringHelpers.kt
@file:JvmName("StringHelpers")
@file:JvmMultifileClass // Optional: merge multiple files into one class
package com.example.utils

fun reverse(str: String): String = str.reversed()
fun isEmpty(str: String?): Boolean = str.isNullOrEmpty()
```

**Another file in the same package:**
```kotlin
// MoreStringHelpers.kt
@file:JvmName("StringHelpers")
@file:JvmMultifileClass
package com.example.utils

fun truncate(str: String, length: Int): String = 
    if (str.length <= length) str else str.substring(0, length) + "..."
```

**Java usage:**
```java
import com.example.utils.StringHelpers;

// All functions from both files are in StringHelpers class
String reversed = StringHelpers.reverse("hello");
boolean empty = StringHelpers.isEmpty("");
String truncated = StringHelpers.truncate("Long string", 5);
```

## Properties and Fields

### Understanding Property Access

Kotlin properties are more than just fields - they can have custom getters and setters. Understanding how they're exposed to Java is crucial.

**Kotlin code:**
```kotlin
class Temperature {
    // Simple property - generates getter and setter
    var celsius: Double = 0.0
    
    // Read-only property - generates only getter
    val fahrenheit: Double
        get() = celsius * 9/5 + 32
    
    // Property with custom setter
    var kelvin: Double
        get() = celsius + 273.15
        set(value) {
            celsius = value - 273.15
        }
    
    // Property with backing field and validation
    var minimum: Double = -273.15
        set(value) {
            if (value >= -273.15) {
                field = value
            }
        }
    
    // Computed property (no backing field)
    val isFreezingOrBelow: Boolean
        get() = celsius <= 0
}
```

**Java usage:**
```java
Temperature temp = new Temperature();

// Simple property access
temp.setCelsius(25.0);
System.out.println(temp.getCelsius());

// Read-only property
System.out.println(temp.getFahrenheit()); // 77.0
// temp.setFahrenheit(100); // No setter available

// Custom getter/setter
temp.setKelvin(300.0);
System.out.println(temp.getKelvin());
System.out.println(temp.getCelsius()); // Changed by setKelvin

// Validation in setter
temp.setMinimum(-300); // Ignored due to validation
System.out.println(temp.getMinimum()); // Still -273.15
```

### Exposing Fields with @JvmField

Sometimes you want direct field access instead of getters/setters, especially for performance-critical code or when following Java conventions.

**Kotlin code:**
```kotlin
class Configuration {
    // Exposed as public field
    @JvmField
    val DEBUG = true
    
    // Mutable field
    @JvmField
    var timeout = 5000
    
    // Cannot use @JvmField with private or custom accessors
    // @JvmField
    // private val secret = "hidden" // Error
    
    // Regular property for comparison
    var maxRetries = 3
    
    companion object {
        // Static field in Java
        @JvmField
        val DEFAULT_TIMEOUT = 3000
        
        // Const is automatically a field (compile-time constant)
        const val VERSION = "1.0"
    }
}
```

**Java usage:**
```java
Configuration config = new Configuration();

// Direct field access - no method call
if (config.DEBUG) {
    config.timeout = 10000; // Direct assignment
    System.out.println("Timeout: " + config.timeout);
}

// Regular property requires getter/setter
config.setMaxRetries(5);
System.out.println(config.getMaxRetries());

// Static fields
int defaultTimeout = Configuration.DEFAULT_TIMEOUT;
String version = Configuration.VERSION;
```

**When to use @JvmField:**
- Android view bindings (e.g., `@JvmField @BindView`)
- Configuration constants
- Performance-critical code where method calls matter
- When matching existing Java API conventions

### Lateinit Properties

Kotlin's `lateinit` allows non-null properties to be initialized after construction, useful for dependency injection and initialization frameworks.

**Kotlin code:**
```kotlin
class Service {
    // Lateinit for non-null types only
    lateinit var repository: Repository
    lateinit var logger: Logger
    
    // Cannot use lateinit with primitives or nullable types
    // lateinit var count: Int // Error
    // lateinit var optional: String? // Error
    
    fun initialize() {
        repository = Repository()
        logger = Logger()
    }
    
    fun doWork() {
        // Check if initialized (Kotlin only)
        if (::repository.isInitialized) {
            repository.fetchData()
        }
        
        // Will throw UninitializedPropertyAccessException if not initialized
        logger.log("Working...")
    }
}

class Repository {
    fun fetchData() = "Data"
}

class Logger {
    fun log(message: String) = println(message)
}
```

**Java usage:**
```java
Service service = new Service();

// Accessing before initialization throws exception
try {
    service.getRepository().fetchData(); // UninitializedPropertyAccessException
} catch (Exception e) {
    System.out.println("Not initialized: " + e.getMessage());
}

// Proper initialization
service.initialize();
service.getRepository().fetchData(); // Now works
service.doWork();

// Cannot check initialization state from Java
// No equivalent to ::property.isInitialized
```

## Static Members

### Companion Objects

Kotlin doesn't have static members; instead, it uses companion objects. Understanding how these map to Java is essential.

**Kotlin code:**
```kotlin
class Factory {
    companion object {
        // Not static by default - accessed via Companion instance
        fun createDefault(): Factory = Factory()
        
        // @JvmStatic makes it a real static method
        @JvmStatic
        fun create(type: String): Factory = Factory()
        
        // Properties
        val defaultName = "Default" // Not static
        
        @JvmField
        val DEFAULT_TYPE = "Standard" // Static field
        
        @JvmStatic
        val instanceCount: Int = 0 // Static getter
        
        // Const is always static
        const val VERSION = "1.0"
    }
}
```

**Java usage:**
```java
// Without @JvmStatic - through Companion instance
Factory f1 = Factory.Companion.createDefault();
String name = Factory.Companion.getDefaultName();

// With @JvmStatic - direct static access
Factory f2 = Factory.create("Premium");
String type = Factory.DEFAULT_TYPE; // Field access
int count = Factory.getInstanceCount(); // Getter access
String version = Factory.VERSION; // Const access

// The Companion object itself is accessible
Factory.Companion companion = Factory.Companion;
```

### Named Companion Objects

You can name companion objects for better Java interop.

**Kotlin code:**
```kotlin
class Database {
    companion object Connection {
        @JvmStatic
        fun connect(url: String) = Database()
        
        fun disconnect() = println("Disconnected")
    }
}
```

**Java usage:**
```java
// Using the named companion
Database db = Database.connect("jdbc://...");
Database.Connection.disconnect();

// The name is also accessible
Database.Connection connection = Database.Connection;
```

### Object Declarations (Singletons)

Kotlin's `object` creates a singleton pattern that's thread-safe and lazy-initialized.

**Kotlin code:**
```kotlin
object Singleton {
    init {
        println("Singleton initialized")
    }
    
    var counter = 0
    
    fun doWork() {
        counter++
        println("Working... Count: $counter")
    }
    
    @JvmStatic
    fun staticMethod() {
        println("Static method called")
    }
}

// Object with inheritance
object DefaultLogger : Logger() {
    override fun log(message: String) {
        println("[DEFAULT] $message")
    }
}

abstract class Logger {
    abstract fun log(message: String)
}
```

**Java usage:**
```java
// Accessing the singleton instance
Singleton.INSTANCE.doWork();
Singleton.INSTANCE.doWork();
System.out.println(Singleton.INSTANCE.getCounter()); // 2

// Static method access
Singleton.staticMethod();

// Using object with inheritance
Logger logger = DefaultLogger.INSTANCE;
logger.log("Hello from Java");
```

## Exceptions

### Understanding Exception Handling

Kotlin doesn't distinguish between checked and unchecked exceptions - all exceptions are unchecked. However, Java does make this distinction, so special handling is needed.

**Kotlin code without @Throws:**
```kotlin
class FileProcessor {
    // This throws IOException but doesn't declare it
    fun readFile(path: String): String {
        if (!File(path).exists()) {
            throw IOException("File not found: $path")
        }
        return File(path).readText()
    }
    
    // Multiple exceptions
    fun processData(data: String) {
        if (data.isEmpty()) {
            throw IllegalArgumentException("Data is empty")
        }
        if (data.length > 1000) {
            throw IllegalStateException("Data too large")
        }
        // Process data...
    }
}
```

**Java usage problem:**
```java
FileProcessor processor = new FileProcessor();

// This compiles but might crash at runtime
String content = processor.readFile("data.txt"); // IOException not caught!

// Runtime exceptions work normally
try {
    processor.processData("");
} catch (IllegalArgumentException e) {
    // This works because it's unchecked
}
```

### Using @Throws Annotation

The `@Throws` annotation tells Java that a method can throw checked exceptions.

**Kotlin code with @Throws:**
```kotlin
import java.io.*
import java.sql.SQLException

class DataService {
    @Throws(IOException::class)
    fun readFromFile(filename: String): String {
        return File(filename).readText()
    }
    
    @Throws(IOException::class, SQLException::class)
    fun readFromDatabase(query: String): String {
        // Might throw either exception
        if (query.isEmpty()) {
            throw SQLException("Empty query")
        }
        return "Result"
    }
    
    // Shorthand for multiple exceptions
    @Throws(IOException::class, SQLException::class, IllegalStateException::class)
    fun complexOperation() {
        // Implementation
    }
}
```

**Java usage with proper exception handling:**
```java
DataService service = new DataService();

// Now Java knows about the checked exception
try {
    String data = service.readFromFile("config.txt");
} catch (IOException e) {
    e.printStackTrace();
}

// Multiple exceptions
try {
    String result = service.readFromDatabase("SELECT * FROM users");
} catch (IOException | SQLException e) {
    System.err.println("Operation failed: " + e.getMessage());
}
```

### Exception Type Hierarchies

Understanding how Kotlin and Java exception hierarchies interact:

**Kotlin code:**
```kotlin
// Custom exception hierarchy
open class BusinessException(message: String) : Exception(message)
class ValidationException(message: String) : BusinessException(message)
class ProcessingException(message: String, cause: Throwable?) : 
    BusinessException(message), java.io.Serializable {
    constructor(message: String) : this(message, null)
}

class BusinessService {
    @Throws(BusinessException::class)
    fun validateAndProcess(data: String) {
        if (data.isEmpty()) {
            throw ValidationException("Data cannot be empty")
        }
        try {
            // Some processing
        } catch (e: Exception) {
            throw ProcessingException("Processing failed", e)
        }
    }
}
```

**Java usage:**
```java
BusinessService service = new BusinessService();

try {
    service.validateAndProcess("");
} catch (ValidationException e) {
    // Specific exception handling
    System.err.println("Validation error: " + e.getMessage());
} catch (ProcessingException e) {
    // Handle with cause
    System.err.println("Processing error: " + e.getMessage());
    if (e.getCause() != null) {
        e.getCause().printStackTrace();
    }
} catch (BusinessException e) {
    // General business exception
    System.err.println("Business error: " + e.getMessage());
}
```

## Generics

### Basic Generic Interoperability

Kotlin and Java handle generics similarly at the basic level, but Kotlin adds variance annotations and has different nullability rules.

**Kotlin generic classes:**
```kotlin
// Simple generic class
class Box<T>(val value: T) {
    fun get(): T = value
}

// Multiple type parameters
class Pair<A, B>(val first: A, val second: B) {
    fun swap(): Pair<B, A> = Pair(second, first)
}

// Generic constraints
class NumberBox<T : Number>(val number: T) {
    fun toDouble(): Double = number.toDouble()
}

// Multiple constraints
class ConstrainedBox<T>(val value: T) 
    where T : CharSequence, T : Comparable<T> {
    fun isLongerThan(other: T): Boolean = value.length > other.length
}
```

**Java usage:**
```java
// Basic usage
Box<String> stringBox = new Box<>("Hello");
String value = stringBox.get();

// Multiple type parameters
Pair<Integer, String> pair = new Pair<>(1, "One");
Pair<String, Integer> swapped = pair.swap();

// With constraints
NumberBox<Double> numberBox = new NumberBox<>(3.14);
double d = numberBox.toDouble();

// Multiple constraints work the same
ConstrainedBox<String> constrained = new ConstrainedBox<>("Hello");
boolean longer = constrained.isLongerThan("Hi");
```

### Declaration-Site Variance

Kotlin supports declaration-site variance (defining variance at the class level), which is different from Java's use-site variance.

**Kotlin code with variance:**
```kotlin
// Covariant (producer) - can only output T
class Producer<out T>(private val value: T) {
    fun get(): T = value
    // fun set(value: T) {} // Error: T is in 'in' position
}

// Contravariant (consumer) - can only input T
class Consumer<in T> {
    fun consume(item: T) {
        println("Consuming $item")
    }
    // fun get(): T = ... // Error: T is in 'out' position
}

// Invariant (default) - can both input and output T
class Container<T>(private var value: T) {
    fun get(): T = value
    fun set(newValue: T) {
        value = newValue
    }
}
```

**Java usage and variance behavior:**
```java
// Covariance in action
Producer<String> stringProducer = new Producer<>("Hello");
Producer<? extends CharSequence> charProducer = stringProducer; // OK
CharSequence value = charProducer.get();

// Contravariance in action
Consumer<Number> numberConsumer = new Consumer<>();
Consumer<? super Integer> intConsumer = numberConsumer; // OK
intConsumer.consume(42);

// Invariance
Container<String> stringContainer = new Container<>("Hello");
// Container<CharSequence> charContainer = stringContainer; // Error
```

### Type Projections and Wildcards

Kotlin's type projections map to Java's wildcards, but the syntax is different.

**Kotlin code:**
```kotlin
class GenericProcessor {
    // Star projection - any type
    fun processAny(list: List<*>) {
        println("Size: ${list.size}")
        // Can only read Any? from the list
        val item: Any? = list.firstOrNull()
    }
    
    // Out projection - covariant
    fun processProducer(list: List<out Number>) {
        val sum = list.sumOf { it.toDouble() }
        println("Sum: $sum")
    }
    
    // In projection - contravariant
    fun fillList(list: MutableList<in Int>) {
        list.add(1)
        list.add(2)
        list.add(3)
    }
}
```

**Equivalent Java code:**
```java
public class JavaGenericProcessor {
    // Wildcard - any type
    public void processAny(List<?> list) {
        System.out.println("Size: " + list.size());
        Object item = list.isEmpty() ? null : list.get(0);
    }
    
    // Upper bound wildcard - covariant
    public void processProducer(List<? extends Number> list) {
        double sum = list.stream()
            .mapToDouble(Number::doubleValue)
            .sum();
        System.out.println("Sum: " + sum);
    }
    
    // Lower bound wildcard - contravariant
    public void fillList(List<? super Integer> list) {
        list.add(1);
        list.add(2);
        list.add(3);
    }
}
```

### Reified Type Parameters

Kotlin's `reified` type parameters are not directly accessible from Java but understanding them helps with interop design.

**Kotlin code with reified types:**
```kotlin
// Inline function with reified type
inline fun <reified T> isInstance(value: Any): Boolean {
    return value is T
}

// Class using reified types internally
class TypeChecker {
    // This is accessible from Java
    fun <T> checkType(value: Any, clazz: Class<T>): Boolean {
        return clazz.isInstance(value)
    }
    
    // This uses reified internally but provides Java-friendly API
    inline fun <reified T> check(value: Any): Boolean {
        return checkType(value, T::class.java)
    }
}
```

**Java usage:**
```java
TypeChecker checker = new TypeChecker();

// Using the Java-friendly method
boolean isString = checker.checkType("Hello", String.class);
boolean isNumber = checker.checkType("Hello", Number.class);

// Cannot use the reified version directly
// checker.check("Hello"); // Not accessible
```

## Collections

### Collection Type Mapping

Understanding how Kotlin and Java collections map to each other is crucial for smooth interoperability.

**Kotlin collection interfaces and their Java equivalents:**
```kotlin
fun demonstrateCollections() {
    // Kotlin -> Java mapping
    val list: List<String> = listOf("a", "b")           // java.util.List
    val mutableList: MutableList<String> = mutableListOf("x", "y") // java.util.List
    val set: Set<Int> = setOf(1, 2, 3)                  // java.util.Set
    val map: Map<String, Int> = mapOf("one" to 1)       // java.util.Map
}
```

### Kotlin Collections in Java

Kotlin distinguishes between read-only and mutable collections at the type system level, but this distinction doesn't exist in Java.

**Kotlin code exposing collections:**
```kotlin
class CollectionProvider {
    // Read-only list
    fun getImmutableList(): List<String> = listOf("a", "b", "c")
    
    // Mutable list
    fun getMutableList(): MutableList<String> = mutableListOf("x", "y", "z")
    
    // Actually mutable but exposed as read-only
    private val internalList = mutableListOf("hidden")
    fun getReadOnlyView(): List<String> = internalList
    
    // Set operations
    fun getNumberSet(): Set<Int> = setOf(1, 2, 3, 4, 5)
    
    // Map operations
    fun getConfiguration(): Map<String, Any> = mapOf(
        "debug" to true,
        "timeout" to 5000,
        "name" to "MyApp"
    )
}
```

**Java usage and gotchas:**
```java
CollectionProvider provider = new CollectionProvider();

// Read-only list in Kotlin, but Java sees it as List
List<String> immutable = provider.getImmutableList();
// This compiles but throws UnsupportedOperationException at runtime!
try {
    immutable.add("d"); // Runtime exception
} catch (UnsupportedOperationException e) {
    System.out.println("Cannot modify immutable list");
}

// Mutable list works as expected
List<String> mutable = provider.getMutableList();
mutable.add("w"); // Works fine

// Read-only view can be dangerous
List<String> view = provider.getReadOnlyView();
// view.add("visible"); // Still throws exception

// Safe way to handle Kotlin collections in Java
List<String> safeCopy = new ArrayList<>(provider.getImmutableList());
safeCopy.add("d"); // Now safe to modify
```

### Java Collections in Kotlin

When Kotlin receives Java collections, it treats them as platform types that could be either read-only or mutable.

**Java code providing collections:**
```java
// JavaCollectionProvider.java
import java.util.*;

public class JavaCollectionProvider {
    public List<String> getModifiableList() {
        return Arrays.asList("a", "b", "c"); // Fixed-size list
    }
    
    public List<String> getTrulyMutableList() {
        return new ArrayList<>(Arrays.asList("x", "y", "z"));
    }
    
    public List<String> getImmutableList() {
        return Collections.unmodifiableList(
            Arrays.asList("immutable", "list")
        );
    }
    
    public Set<Integer> getNumberSet() {
        return new HashSet<>(Arrays.asList(1, 2, 3));
    }
    
    public Map<String, Object> getProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("version", "1.0");
        props.put("count", 42);
        return props;
    }
}
```

**Kotlin handling of Java collections:**
```kotlin
fun handleJavaCollections() {
    val provider = JavaCollectionProvider()
    
    // Platform type - can be treated as List or MutableList
    val list1: List<String> = provider.modifiableList // Safe, read-only
    val list2: MutableList<String> = provider.modifiableList // Risky!
    
    // This might fail at runtime
    try {
        list2.add("d") // UnsupportedOperationException
    } catch (e: UnsupportedOperationException) {
        println("Cannot modify fixed-size list")
    }
    
    // Safe handling with explicit types
    val mutableList = provider.trulyMutableList
    mutableList.add("w") // Safe
    
    // Defensive copying
    val safeMutable = provider.immutableList.toMutableList()
    safeMutable.add("now mutable")
    
    // Working with maps
    val props = provider.properties
    props.forEach { (key, value) ->
        when (value) {
            is String -> println("String property: $key = $value")
            is Int -> println("Int property: $key = $value")
            else -> println("Other property: $key = $value")
        }
    }
}
```

### Collection Operations Interop

Kotlin provides many collection operations that aren't available in Java 8's Stream API.

**Kotlin code with collection operations:**
```kotlin
class DataProcessor {
    fun processNumbers(numbers: List<Int>): ProcessingResult {
        return ProcessingResult(
            sum = numbers.sum(),
            average = numbers.average(),
            evenNumbers = numbers.filter { it % 2 == 0 },
            doubled = numbers.map { it * 2 },
            grouped = numbers.groupBy { it % 3 },
            sorted = numbers.sorted()
        )
    }
    
    // Return type visible to Java
    data class ProcessingResult(
        val sum: Int,
        val average: Double,
        val evenNumbers: List<Int>,
        val doubled: List<Int>,
        val grouped: Map<Int, List<Int>>,
        val sorted: List<Int>
    )
}
```

**Java usage:**
```java
DataProcessor processor = new DataProcessor();
List<Integer> numbers = Arrays.asList(5, 2, 8, 1, 9, 3, 7, 4, 6);

DataProcessor.ProcessingResult result = processor.processNumbers(numbers);

System.out.println("Sum: " + result.getSum());
System.out.println("Average: " + result.getAverage());
System.out.println("Even numbers: " + result.getEvenNumbers());
System.out.println("Grouped by mod 3: " + result.getGrouped());
```

## Annotations

### @JvmOverloads for Default Parameters

Kotlin's default parameters don't automatically generate overloaded methods for Java. The `@JvmOverloads` annotation fixes this.

**Kotlin code without @JvmOverloads:**
```kotlin
class MessageDialog(
    val title: String,
    val message: String = "",
    val cancelable: Boolean = true
) {
    fun show(duration: Int = 3000, animated: Boolean = true) {
        println("Showing dialog: $title")
        println("Duration: $duration, Animated: $animated")
    }
}
```

**Java problem:**
```java
// Only the full constructor is available
MessageDialog dialog = new MessageDialog("Title", "Message", false);
// These don't compile:
// MessageDialog dialog2 = new MessageDialog("Title");
// MessageDialog dialog3 = new MessageDialog("Title", "Message");

// Only the full method signature is available
dialog.show(5000, false);
// dialog.show(); // Doesn't compile
```

**Kotlin code with @JvmOverloads:**
```kotlin
class BetterDialog @JvmOverloads constructor(
    val title: String,
    val message: String = "",
    val cancelable: Boolean = true,
    val theme: String = "default"
) {
    @JvmOverloads
    fun show(
        duration: Int = 3000,
        animated: Boolean = true,
        sound: Boolean = false
    ) {
        println("Showing dialog: $title")
        println("Duration: $duration, Animated: $animated, Sound: $sound")
    }
    
    // Generates: show(), show(int), show(int, boolean), show(int, boolean, boolean)
}
```

**Java with overloads:**
```java
// All constructor overloads are available
BetterDialog d1 = new BetterDialog("Title");
BetterDialog d2 = new BetterDialog("Title", "Message");
BetterDialog d3 = new BetterDialog("Title", "Message", false);
BetterDialog d4 = new BetterDialog("Title", "Message", false, "dark");

// All method overloads are available
d1.show();
d1.show(5000);
d1.show(5000, false);
d1.show(5000, false, true);
```

### @JvmName for Methods

The `@JvmName` annotation allows you to provide different names for Java, useful for avoiding conflicts or providing more Java-idiomatic names.

**Kotlin code with naming conflicts:**
```kotlin
class Processor {
    // These would have the same JVM signature due to type erasure
    @JvmName("processIntList")
    fun process(list: List<Int>): String {
        return "Processed ${list.size} integers"
    }
    
    @JvmName("processStringList")
    fun process(list: List<String>): String {
        return "Processed ${list.size} strings"
    }
    
    // Renaming for better Java API
    @JvmName("getFormattedText")
    val formattedText: String
        get() = "Formatted"
    
    // Different property and method names
    val items = listOf("a", "b", "c")
    
    @JvmName("getItemCount")
    fun items(): Int = items.size
}
```

**Java usage:**
```java
Processor processor = new Processor();

// Using renamed methods
String result1 = processor.processIntList(Arrays.asList(1, 2, 3));
String result2 = processor.processStringList(Arrays.asList("a", "b"));

// Using renamed property getter
String formatted = processor.getFormattedText();

// Both property and method are accessible
List<String> items = processor.getItems();
int count = processor.getItemCount();
```

### @JvmStatic and @JvmField in Annotations

When creating custom annotations, these annotations are particularly important.

**Kotlin annotation with constants:**
```kotlin
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TestAnnotation(
    val name: String,
    val priority: Int = 0
) {
    companion object {
        @JvmField
        val DEFAULT_NAME = "test"
        
        @JvmStatic
        val PRIORITIES = intArrayOf(0, 1, 2, 3)
        
        const val HIGH_PRIORITY = 3
        const val LOW_PRIORITY = 0
    }
}
```

**Java usage:**
```java
@TestAnnotation(name = TestAnnotation.DEFAULT_NAME, 
                priority = TestAnnotation.HIGH_PRIORITY)
public class TestClass {
    
    @TestAnnotation(name = "method test")
    public void testMethod() {
        // Access companion constants
        int[] priorities = TestAnnotation.getPRIORITIES();
    }
}
```

### @JvmSuppressWildcards and @JvmWildcard

These annotations control how Kotlin's declaration-site variance is translated to Java's wildcards.

**Kotlin code:**
```kotlin
class WildcardExample {
    // Normally generates List<? extends String> in Java
    fun getProducerList(): List<String> = listOf("a", "b")
    
    // Suppress wildcards for Java interop
    @JvmSuppressWildcards
    fun processExactList(list: List<String>): List<String> {
        return list.map { it.uppercase() }
    }
    
    // Force wildcards
    fun processAnyList(@JvmWildcard list: List<String>) {
        list.forEach { println(it) }
    }
}
```

## Extension Functions

### Understanding Extension Functions

Extension functions are compiled to static methods, making them usable from Java with a different syntax.

**Kotlin extensions:**
```kotlin
// StringExtensions.kt
package com.example.extensions

// Simple extension function
fun String.isPalindrome(): Boolean {
    return this == this.reversed()
}

// Extension with parameters
fun String.repeat(times: Int, separator: String = ""): String {
    return (1..times).map { this }.joinToString(separator)
}

// Extension property
val String.lastChar: Char?
    get() = if (isEmpty()) null else this[length - 1]

// Generic extension
fun <T> List<T>.secondOrNull(): T? = if (size >= 2) this[1] else null

// Extension on nullable type
fun String?.isNullOrEmpty(): Boolean {
    return this == null || this.isEmpty()
}

// Member extension (inside a class)
class StringFormatter {
    fun String.format(): String {
        return "[$this]"
    }
    
    fun processString(str: String): String {
        return str.format() // Can use extension here
    }
}
```

**Java usage:**
```java
import static com.example.extensions.StringExtensionsKt.*;

public class Main {
    public static void main(String[] args) {
        // Extension functions become static methods
        String text = "radar";
        boolean isPalin = isPalindrome(text);
        System.out.println(text + " is palindrome: " + isPalin);
        
        // With parameters
        String repeated = repeat("Hi", 3, ", ");
        System.out.println(repeated); // "Hi, Hi, Hi"
        
        // Extension properties become static methods
        Character last = getLastChar("Hello");
        System.out.println("Last char: " + last);
        
        // Generic extensions
        List<String> list = Arrays.asList("a", "b", "c");
        String second = secondOrNull(list);
        
        // Nullable extensions
        String nullStr = null;
        boolean empty = isNullOrEmpty(nullStr);
        
        // Member extensions are not accessible
        StringFormatter formatter = new StringFormatter();
        // formatter.format("test"); // Doesn't compile
        String result = formatter.processString("test"); // "[test]"
    }
}
```

### Extension Function Scope

Understanding extension function visibility and scope is important for Java interop.

**Kotlin code with scoped extensions:**
```kotlin
// TopLevel.kt
package com.example

// Public extension - visible to Java
fun String.publicExtension() = uppercase()

// Internal extension - not visible to Java
internal fun String.internalExtension() = lowercase()

// Private extension - not visible to Java
private fun String.privateExtension() = trim()

class Container {
    // Member extension - not directly accessible from Java
    fun String.memberExtension() = "[$this]"
    
    // Public method using member extension
    fun format(text: String): String = text.memberExtension()
}

// Extension with receiver and parameter of same type
fun String.concat(other: String, separator: String = " "): String {
    return this + separator + other
}
```

## Null Safety

### Platform Types

When Java code returns values to Kotlin, they're treated as platform types (denoted with `!` in the IDE, like `String!`), which can be treated as either nullable or non-nullable.

**Java code with various nullability scenarios:**
```java
// UserService.java
public class UserService {
    private Map<String, User> users = new HashMap<>();
    
    // Might return null
    public User findUser(String id) {
        return users.get(id);
    }
    
    // Never returns null (but Kotlin doesn't know this)
    public String getServiceName() {
        return "UserService";
    }
    
    // Sometimes returns null
    public String getUserEmail(String userId) {
        User user = findUser(userId);
        return user != null ? user.getEmail() : null;
    }
    
    // Array that might contain nulls
    public String[] getAllEmails() {
        return users.values().stream()
            .map(user -> user.getEmail()) // Some emails might be null
            .toArray(String[]::new);
    }
    
    // Collection that won't contain nulls but might be null itself
    public List<String> getActiveUserIds() {
        if (users.isEmpty()) {
            return null;
        }
        return users.keySet().stream()
            .filter(id -> users.get(id).isActive())
            .collect(Collectors.toList());
    }
}

class User {
    private String email;
    private boolean active;
    
    public String getEmail() { return email; }
    public boolean isActive() { return active; }
}
```

**Kotlin handling platform types:**
```kotlin
fun handlePlatformTypes() {
    val service = UserService()
    
    // Platform type - risky assignment
    val name1: String = service.serviceName  // Assumes non-null
    // val user1: User = service.findUser("123") // Might crash!
    
    // Safe assignment
    val name2: String? = service.serviceName  // Safe but unnecessary
    val user2: User? = service.findUser("123") // Safe and necessary
    
    // Defensive programming
    val email = service.getUserEmail("123")?.trim() ?: "No email"
    
    // Handling arrays with potential nulls
    val emails: Array<String?> = service.allEmails
    emails.forEach { email ->
        email?.let { println("Email: $it") }
    }
    
    // Collections
    val activeIds: List<String>? = service.activeUserIds
    activeIds?.forEach { id ->
        println("Active user: $id")
    }
    
    // Or with default
    val ids = service.activeUserIds ?: emptyList()
}
```

### Nullability Annotations

Using nullability annotations helps Kotlin understand Java code's null-safety guarantees.

**Java code with annotations:**
```java
import org.jetbrains.annotations.*;
// Or: import javax.annotation.*;
// Or: import androidx.annotation.*;

public class AnnotatedService {
    @NotNull
    public String getRequiredValue() {
        return "Never null";
    }
    
    @Nullable
    public String getOptionalValue(boolean returnNull) {
        return returnNull ? null : "Sometimes null";
    }
    
    public void processData(@NotNull String required,
                           @Nullable String optional) {
        System.out.println("Required: " + required);
        if (optional != null) {
            System.out.println("Optional: " + optional);
        }
    }
    
    @NotNull
    public List<@NotNull String> getValidatedList() {
        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");
        return list;
    }
    
    // Method parameters with nullability
    public void complexMethod(@Nullable List<@NotNull String> list) {
        if (list != null) {
            for (String item : list) {
                // item is guaranteed non-null
                System.out.println(item.toUpperCase());
            }
        }
    }
}
```

**Kotlin usage with annotations:**
```kotlin
fun useAnnotatedService() {
    val service = AnnotatedService()
    
    // Kotlin knows this is non-null
    val required: String = service.requiredValue
    println(required.length) // Safe
    
    // Kotlin knows this is nullable
    val optional: String? = service.getOptionalValue(true)
    println(optional?.length) // Must use safe call
    
    // Method parameters
    service.processData("Required", null) // OK
    // service.processData(null, "Optional") // Compile error!
    
    // Generic nullability
    val list: List<String> = service.validatedList
    list.forEach { item ->
        println(item.uppercase()) // item is non-null
    }
    
    // Complex nullability
    service.complexMethod(null) // OK
    service.complexMethod(listOf("a", "b")) // OK
    // service.complexMethod(listOf("a", null)) // Error if Kotlin checks
}
```

### Defensive Programming Patterns

Best practices for handling Java interop with uncertain nullability:

**Kotlin defensive patterns:**
```kotlin
class DefensiveWrapper {
    private val javaService = LegacyJavaService()
    
    // Wrap with safe API
    fun getUserNameSafely(id: String): String {
        return javaService.getUserName(id) ?: "Unknown User"
    }
    
    // Validate and transform
    fun getValidatedEmails(): List<String> {
        return javaService.getAllUserEmails()
            ?.filterNotNull()
            ?.filter { it.isNotBlank() }
            ?.map { it.trim().lowercase() }
            ?: emptyList()
    }
    
    // Contract-based safety
    fun processUser(userId: String) {
        val user = javaService.getUser(userId)
        requireNotNull(user) { "User not found: $userId" }
        
        check(user.isValid) { "Invalid user: $userId" }
        
        // Now user is smart-cast to non-null
        println("Processing user: ${user.name}")
    }
    
    // Extension for Java classes
    fun User?.orDefault(): User {
        return this ?: User("default", "default@example.com")
    }
}

// Legacy Java service (without annotations)
class LegacyJavaService {
    fun getUserName(id: String): String? = TODO()
    fun getAllUserEmails(): List<String?>? = TODO()
    fun getUser(id: String): User? = TODO()
}
```

## SAM Conversions

### Single Abstract Method Interfaces

SAM (Single Abstract Method) conversion allows Kotlin lambdas to be automatically converted to Java interfaces with a single abstract method.

**Java interfaces:**
```java
// Runnable.java (from Java standard library)
public interface Runnable {
    void run();
}

// Custom SAM interfaces
public interface ClickListener {
    void onClick(View view);
}

public interface Predicate<T> {
    boolean test(T value);
}

public interface Transformer<T, R> {
    R transform(T input);
}

// Not a SAM interface (multiple abstract methods)
public interface MultiMethod {
    void method1();
    void method2();
}

// SAM interface with default methods (still SAM)
public interface EnhancedProcessor<T> {
    T process(T input);
    
    default T preProcess(T input) {
        return input;
    }
    
    default void postProcess(T result) {
        System.out.println("Processed: " + result);
    }
}
```

**Kotlin using SAM conversion:**
```kotlin
fun demonstrateSAMConversion() {
    // Standard library SAM
    val runnable: Runnable = Runnable { println("Running!") }
    
    // Even simpler - SAM conversion
    val runnable2 = Runnable { println("Running again!") }
    
    // Custom SAM interfaces
    val clickListener = ClickListener { view ->
        println("Clicked on $view")
    }
    
    // Generic SAM
    val stringPredicate = Predicate<String> { str ->
        str.length > 5
    }
    
    val transformer = Transformer<String, Int> { input ->
        input.length
    }
    
    // SAM with default methods
    val processor = EnhancedProcessor<String> { input ->
        input.uppercase()
    }
    
    // Using in higher-order functions
    performAction { println("Action performed") }
    
    // Method reference as SAM
    val methodRef = Transformer<String, Int>(String::length)
}

// Function accepting SAM interface
fun performAction(action: Runnable) {
    println("Before action")
    action.run()
    println("After action")
}
```

### Java APIs Using SAM

Many Java APIs use SAM interfaces, making Kotlin interop very smooth:

**Java code with SAM parameters:**
```java
public class EventManager {
    private List<ClickListener> listeners = new ArrayList<>();
    
    public void addClickListener(ClickListener listener) {
        listeners.add(listener);
    }
    
    public void removeClickListener(ClickListener listener) {
        listeners.remove(listener);
    }
    
    // Method accepting multiple SAM interfaces
    public <T> T processWithCallback(
            T data,
            Predicate<T> validator,
            Transformer<T, T> transformer,
            Consumer<T> callback) {
        
        if (validator.test(data)) {
            T result = transformer.transform(data);
            callback.accept(result);
            return result;
        }
        return data;
    }
    
    // Executor-style API
    public void execute(Runnable task) {
        System.out.println("Executing task...");
        task.run();
    }
}

interface Consumer<T> {
    void accept(T value);
}
```

**Kotlin using Java SAM APIs:**
```kotlin
fun useJavaSAMAPIs() {
    val manager = EventManager()
    
    // Lambda as SAM
    manager.addClickListener { view ->
        println("Lambda click: $view")
    }
    
    // Store reference for removal
    val listener = ClickListener { println("Stored listener") }
    manager.addClickListener(listener)
    manager.removeClickListener(listener)
    
    // Multiple SAM parameters
    val result = manager.processWithCallback(
        "hello world",
        { it.isNotEmpty() },           // Predicate
        { it.uppercase() },             // Transformer
        { println("Result: $it") }      // Consumer
    )
    
    // Method references
    manager.execute(::doWork)
    
    // Anonymous function syntax
    manager.addClickListener(fun(view) {
        println("Anonymous function click")
    })
}

fun doWork() {
    println("Working...")
}
```

### Kotlin Functional Interfaces

Kotlin 1.4+ introduced functional interfaces (fun interface) for better SAM support:

**Kotlin functional interfaces:**
```kotlin
// Kotlin functional interface
fun interface Calculator {
    fun calculate(a: Int, b: Int): Int
}

fun interface StringProcessor {
    fun process(input: String): String
    
    // Can have default methods
    fun preProcess(input: String): String = input.trim()
}

// Using in Kotlin
class MathOperations {
    fun performCalculation(a: Int, b: Int, calc: Calculator): Int {
        return calc.calculate(a, b)
    }
    
    fun processText(text: String, processor: StringProcessor): String {
        val preprocessed = processor.preProcess(text)
        return processor.process(preprocessed)
    }
}
```

**Java using Kotlin functional interfaces:**
```java
public class JavaCalculator {
    public static void main(String[] args) {
        MathOperations ops = new MathOperations();
        
        // Lambda expression
        int sum = ops.performCalculation(5, 3, (a, b) -> a + b);
        int product = ops.performCalculation(5, 3, (a, b) -> a * b);
        
        // Anonymous class (always works)
        int difference = ops.performCalculation(5, 3, new Calculator() {
            @Override
            public int calculate(int a, int b) {
                return a - b;
            }
        });
        
        // With default methods
        String result = ops.processText("  HELLO  ", 
            input -> input.toLowerCase());
    }
}
```

### SAM Conversion Limitations

Understanding when SAM conversion doesn't work:

**Kotlin code showing limitations:**
```kotlin
// Abstract class - no SAM conversion
abstract class AbstractProcessor {
    abstract fun process(data: String): String
}

// Interface with multiple methods - no SAM conversion
interface MultiMethodInterface {
    fun method1()
    fun method2()
}

// Kotlin interface - no automatic SAM conversion in Kotlin
interface KotlinInterface {
    fun doWork()
}

fun demonstrateLimitations() {
    // These don't work with SAM conversion:
    
    // val processor = AbstractProcessor { it.uppercase() } // Error
    
    // Must use object expression
    val processor = object : AbstractProcessor() {
        override fun process(data: String) = data.uppercase()
    }
    
    // Multiple methods need object expression
    val multi = object : MultiMethodInterface {
        override fun method1() { println("Method 1") }
        override fun method2() { println("Method 2") }
    }
    
    // Kotlin interfaces need explicit conversion
    val kotlinInterface = object : KotlinInterface {
        override fun doWork() { println("Working") }
    }
}

// But you can create adapters
fun KotlinInterface(block: () -> Unit): KotlinInterface {
    return object : KotlinInterface {
        override fun doWork() = block()
    }
}

// Now this works
val adapted = KotlinInterface { println("Adapted!") }
```

## Type Mapping

### Primitive Types

Understanding how Kotlin and Java primitive types map to each other:

**Kotlin primitive type mapping:**
```kotlin
class TypeMappingDemo {
    // Kotlin primitives map to Java primitives when possible
    fun primitiveExamples() {
        val byte: Byte = 1         // byte
        val short: Short = 1       // short
        val int: Int = 1           // int
        val long: Long = 1L        // long
        val float: Float = 1.0f    // float
        val double: Double = 1.0   // double
        val boolean: Boolean = true // boolean
        val char: Char = 'A'       // char
    }
    
    // Nullable primitives map to Java wrapper classes
    fun nullablePrimitives() {
        val nullableByte: Byte? = null      // Byte
        val nullableInt: Int? = null        // Integer
        val nullableLong: Long? = null      // Long
        val nullableBoolean: Boolean? = null // Boolean
    }
    
    // In collections, primitives are always boxed
    fun collectionsWithPrimitives(): List<Int> {
        return listOf(1, 2, 3) // List<Integer> in Java
    }
    
    // Primitive arrays have special types
    fun primitiveArrays() {
        val intArray: IntArray = intArrayOf(1, 2, 3)      // int[]
        val doubleArray: DoubleArray = doubleArrayOf(1.0) // double[]
        val booleanArray: BooleanArray = booleanArrayOf(true) // boolean[]
        
        // Array<Int> is different - uses Integer[]
        val boxedArray: Array<Int> = arrayOf(1, 2, 3)     // Integer[]
    }
}
```

**Java seeing Kotlin types:**
```java
TypeMappingDemo demo = new TypeMappingDemo();

// Primitive arrays are real Java primitive arrays
int[] ints = demo.primitiveArrays();

// Collections use wrapper types
List<Integer> list = demo.collectionsWithPrimitives();

// Methods with primitive parameters/returns
public void processPrimitives(int value, boolean flag) {
    // Kotlin Int becomes Java int
    // Kotlin Boolean becomes Java boolean
}
```

### Special Types

Some Kotlin types have special handling in Java:

**Kotlin special types:**
```kotlin
// Unit type
fun performAction(): Unit {
    println("Action performed")
    // return Unit is implicit
}

// Nothing type
fun fail(message: String): Nothing {
    throw IllegalStateException(message)
}

// Any type
fun processAny(value: Any) {
    when (value) {
        is String -> println("String: $value")
        is Int -> println("Int: $value")
        else -> println("Other: $value")
    }
}

// Function types
val function: (String) -> Int = { it.length }
val biFunction: (String, String) -> String = { a, b -> a + b }

// Type aliases
typealias StringMap = Map<String, String>
typealias ClickHandler = (View) -> Unit
```

**Java representation:**
```java
// Unit becomes void
demo.performAction(); // void method

// Nothing is not directly representable
try {
    demo.fail("Error"); // Returns void, always throws
} catch (IllegalStateException e) {
    // Handle exception
}

// Any becomes Object
demo.processAny("String");
demo.processAny(42);
demo.processAny(new Object());

// Function types become FunctionN interfaces
Function1<String, Integer> func = demo.getFunction();
Integer length = func.invoke("Hello");

// Type aliases are expanded
Map<String, String> map = demo.getStringMap();
```

## Best Practices

### 1. Null Safety Best Practices

**Design principles for null-safe interop:**

```kotlin
// Kotlin API design for Java consumption
class UserService {
    // Use annotations for clarity
    @JvmField
    val INSTANCE = UserService()
    
    // Clear nullability in API
    fun findUser(id: String): User? = database.find(id)
    
    // Provide non-null alternatives
    fun getUser(id: String): User = 
        findUser(id) ?: throw UserNotFoundException(id)
    
    // Use default parameters wisely
    @JvmOverloads
    fun createUser(
        name: String,
        email: String? = null,
        age: Int = 0
    ): User {
        return User(name, email ?: "$name@example.com", age)
    }
}

// Java-friendly data class
data class User @JvmOverloads constructor(
    val name: String,
    val email: String,
    val age: Int = 0
) {
    // Provide Java-friendly validation
    init {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(email.contains("@")) { "Invalid email format" }
        require(age >= 0) { "Age cannot be negative" }
    }
    
    companion object {
        @JvmStatic
        fun createDefault() = User("Guest", "guest@example.com", 0)
    }
}
```

### 2. Naming Conventions

**Follow Java conventions when designing for interop:**

```kotlin
// Good naming for Java interop
class OrderService {
    // Use Java-style naming for better consistency
    @JvmName("getInstance")
    fun instance(): OrderService = this
    
    // Avoid Kotlin-specific naming patterns in public APIs
    fun getOrderById(id: String): Order? = null // Good
    // fun orderById(id: String): Order? = null  // Less Java-friendly
    
    // Property naming
    val isReady: Boolean = true  // Generates isReady() in Java
    val hasErrors: Boolean = false // Generates getHasErrors() in Java
    
    // Use @JvmName to fix awkward names
    @get:JvmName("hasActiveOrders")
    val hasActiveOrders: Boolean = true
}
```

### 3. Collections Best Practices

**Design collection APIs carefully:**

```kotlin
class CollectionBestPractices {
    // Return specific types for Java clarity
    fun getNames(): List<String> = listOf("Alice", "Bob")
    
    // Avoid exposing mutable collections unnecessarily
    private val _items = mutableListOf<String>()
    val items: List<String> get() = _items.toList() // Defensive copy
    
    // Provide Java-friendly modification methods
    fun addItem(item: String) {
        _items.add(item)
    }
    
    // Use arrays for performance-critical code
    fun processLargeDataSet(data: IntArray): IntArray {
        return data.map { it * 2 }.toIntArray()
    }
    
    // Document mutability expectations
    /**
     * Returns a mutable copy of the current configuration.
     * Changes to the returned map do not affect the original.
     */
    fun getConfigurationCopy(): MutableMap<String, String> {
        return HashMap(configuration)
    }
    
    private val configuration = mapOf("key" to "value")
}
```

### 4. Static Members Best Practices

**Organize static members effectively:**

```kotlin
// Use companion object with @JvmStatic judiciously
class Logger private constructor(private val name: String) {
    
    fun log(message: String) {
        println("[$name] $message")
    }
    
    companion object {
        @JvmStatic
        private val instances = mutableMapOf<String, Logger>()
        
        @JvmStatic
        fun getLogger(name: String): Logger {
            return instances.getOrPut(name) { Logger(name) }
        }
        
        // Constants
        const val DEFAULT_LEVEL = "INFO"
        
        @JvmField
        val LEVELS = arrayOf("DEBUG", "INFO", "WARN", "ERROR")
    }
}

// For utility functions, consider object or top-level functions
object StringUtils {
    @JvmStatic
    fun isBlank(str: String?): Boolean = str.isNullOrBlank()
    
    @JvmStatic
    fun capitalize(str: String): String = 
        str.replaceFirstChar { it.uppercase() }
}
```

### 5. Exception Handling Best Practices

**Design exception handling for both languages:**

```kotlin
class FileService {
    // Always use @Throws for checked exceptions
    @Throws(IOException::class)
    fun readFile(path: String): String {
        return File(path).readText()
    }
    
    // Document all possible exceptions
    /**
     * Parses the configuration file.
     * @throws IOException if the file cannot be read
     * @throws ParseException if the file format is invalid
     * @throws IllegalArgumentException if the path is null or empty
     */
    @Throws(IOException::class, ParseException::class)
    fun parseConfig(path: String): Config {
        require(path.isNotEmpty()) { "Path cannot be empty" }
        val content = readFile(path)
        return parseConfigContent(content)
    }
    
    // Provide safe alternatives
    fun readFileSafely(path: String): Result<String> {
        return try {
            Result.success(readFile(path))
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
}

class ParseException(message: String) : Exception(message)
class Config(val settings: Map<String, String>)
```

### 6. Generics Best Practices

**Design generic APIs with both languages in mind:**

```kotlin
// Use variance carefully
class Container<T> {
    private val items = mutableListOf<T>()
    
    // Covariant return type
    fun getAll(): List<out T> = items.toList()
    
    // Contravariant parameter
    fun addAll(elements: List<in T>) {
        items.addAll(elements as List<T>)
    }
    
    // Use @JvmSuppressWildcards when needed
    @JvmSuppressWildcards
    fun process(processor: (T) -> T): List<T> {
        return items.map(processor)
    }
}

// Type constraints that work well in both languages
class Cache<K : Any, V : Any> {
    private val map = mutableMapOf<K, V>()
    
    operator fun get(key: K): V? = map[key]
    
    operator fun set(key: K, value: V) {
        map[key] = value
    }
}
```

### 7. Performance Considerations

**Optimize for cross-language performance:**

```kotlin
class PerformanceOptimized {
    // Use primitive arrays for large datasets
    fun sumLargeArray(values: IntArray): Long {
        var sum = 0L
        for (value in values) {
            sum += value
        }
        return sum
    }
    
    // Avoid boxing with @JvmField for constants
    companion object {
        @JvmField
        val MAX_SIZE = 1000
        
        @JvmField
        val EMPTY_INT_ARRAY = intArrayOf()
    }
    
    // Inline functions are not accessible from Java
    // Provide non-inline alternatives for Java
    inline fun <T> measureTime(block: () -> T): Pair<T, Long> {
        val start = System.currentTimeMillis()
        val result = block()
        val time = System.currentTimeMillis() - start
        return result to time
    }
    
    // Java-friendly version
    fun <T> measureTimeJava(block: Supplier<T>): Pair<T, Long> {
        val start = System.currentTimeMillis()
        val result = block.get()
        val time = System.currentTimeMillis() - start
        return result to time
    }
}

// Java functional interface for better interop
fun interface Supplier<T> {
    fun get(): T
}
```

### 8. API Design Guidelines

**Design APIs that work naturally in both languages:**

```kotlin
// Builder pattern for complex objects
class RequestBuilder {
    private var url: String = ""
    private var method: String = "GET"
    private var headers = mutableMapOf<String, String>()
    private var body: String? = null
    
    fun url(url: String) = apply { this.url = url }
    fun method(method: String) = apply { this.method = method }
    fun header(key: String, value: String) = apply { 
        headers[key] = value 
    }
    fun body(body: String?) = apply { this.body = body }
    
    fun build(): Request {
        require(url.isNotEmpty()) { "URL is required" }
        return Request(url, method, headers.toMap(), body)
    }
    
    companion object {
        @JvmStatic
        fun newBuilder() = RequestBuilder()
    }
}

data class Request(
    val url: String,
    val method: String,
    val headers: Map<String, String>,
    val body: String?
)

// Extension functions with Java alternatives
fun Request.isGet(): Boolean = method == "GET"

// Also provide as regular method for Java
class RequestUtils {
    companion object {
        @JvmStatic
        fun isGet(request: Request): Boolean = request.method == "GET"
    }
}
```

### 9. Documentation for Interop

**Document interop behavior clearly:**

```kotlin
/**
 * Service for managing user sessions.
 * 
 * This class is designed for Java interoperability:
 * - All methods properly handle null values
 * - Checked exceptions are declared with @Throws
 * - Default parameters are available via @JvmOverloads
 * 
 * Java usage example:
 * ```java
 * SessionService service = SessionService.getInstance();
 * Session session = service.createSession("user123");
 * ```
 */
class SessionService {
    /**
     * Creates a new session for the given user.
     * 
     * @param userId The user ID (must not be null)
     * @param duration Session duration in seconds (default: 3600)
     * @return A new Session object
     * @throws IllegalArgumentException if userId is blank
     * @throws SessionException if session creation fails
     */
    @JvmOverloads
    @Throws(SessionException::class)
    fun createSession(
        userId: String,
        duration: Int = 3600
    ): Session {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        // Implementation
        return Session(userId, duration)
    }
    
    companion object {
        @JvmStatic
        val instance = SessionService()
    }
}

class Session(val userId: String, val duration: Int)
class SessionException(message: String) : Exception(message)
```

### 10. Testing Interoperability

**Test your code from both languages:**

```kotlin
// Kotlin tests
class InteropTest {
    @Test
    fun testKotlinUsage() {
        val service = MyService()
        val result = service.process("data")
        assertEquals("PROCESSED: data", result)
    }
}
```

```java
// Java tests
public class InteropJavaTest {
    @Test
    public void testJavaUsage() {
        MyService service = new MyService();
        String result = service.process("data");
        assertEquals("PROCESSED: data", result);
    }
    
    @Test
    public void testNullHandling() {
        MyService service = new MyService();
        // Test that null handling works as expected
        String result = service.processNullable(null);
        assertEquals("default", result);
    }
}
```

## Common Pitfalls and Solutions

### 1. Platform Type Null Pointer Exceptions

**Problem:**
```kotlin
// Dangerous code
val javaList = JavaClass.getList() // Platform type List<String>!
val first = javaList.first() // Might throw NPE
```

**Solution:**
```kotlin
// Safe handling
val javaList = JavaClass.getList()
val first = javaList?.firstOrNull() ?: "default"

// Or with explicit type
val safeList: List<String>? = JavaClass.getList()
```

### 2. Immutable Collection Modifications

**Problem:**
```java
// Java code trying to modify Kotlin immutable collection
List<String> list = KotlinClass.getImmutableList();
list.add("new item"); // UnsupportedOperationException
```

**Solution:**
```kotlin
// Document clearly and provide mutable alternatives
class KotlinClass {
    /**
     * Returns an immutable list. 
     * For a mutable copy, use getMutableListCopy()
     */
    fun getImmutableList(): List<String> = listOf("a", "b")
    
    fun getMutableListCopy(): MutableList<String> = 
        getImmutableList().toMutableList()
}
```

### 3. Missing Method Overloads

**Problem:**
```java
// Java cannot use Kotlin default parameters
// dialog.show(); // Doesn't compile without @JvmOverloads
dialog.show(3000, true, false); // Must provide all parameters
```

**Solution:**
```kotlin
class Dialog {
    @JvmOverloads
    fun show(
        duration: Int = 3000,
        animated: Boolean = true,
        modal: Boolean = false
    ) {
        // Implementation
    }
}
```

### 4. Type Erasure Issues

**Problem:**
```kotlin
// These have the same JVM signature
fun process(list: List<String>) { }
fun process(list: List<Int>) { } // Error: Platform clash
```

**Solution:**
```kotlin
// Use @JvmName to distinguish
@JvmName("processStrings")
fun process(list: List<String>) { }

@JvmName("processIntegers")
fun process(list: List<Int>) { }
```

### 5. Companion Object Access

**Problem:**
```java
// Verbose companion access from Java
MyClass.Companion.doSomething();
```

**Solution:**
```kotlin
class MyClass {
    companion object {
        @JvmStatic
        fun doSomething() { } // Now: MyClass.doSomething()
    }
}
```

## Conclusion

Kotlin-Java interoperability is a powerful feature that enables:
- Gradual migration of Java codebases to Kotlin
- Use of existing Java libraries in Kotlin projects
- Mixed-language development teams
- Leveraging the best features of both languages

Key takeaways:
1. **Understand platform types** and handle nullability defensively
2. **Use annotations** (`@JvmStatic`, `@JvmOverloads`, `@JvmName`, etc.) to improve Java API
3. **Design with both languages in mind** when creating public APIs
4. **Test from both languages** to ensure smooth interoperability
5. **Document interop behavior** clearly for API consumers
6. **Be aware of collection mutability** differences
7. **Handle exceptions properly** with `@Throws` annotation
8. **Consider performance implications** of boxing and method calls

By following these guidelines and understanding the mechanics of interoperability, you can create robust applications that seamlessly combine Kotlin and Java code, taking advantage of the strengths of both languages while maintaining a clean and maintainable codebase.