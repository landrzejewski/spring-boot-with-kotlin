# Complete Kotlin Coroutines Tutorial

## Table of Contents
1. [Introduction to Coroutines](#introduction)
2. [Basic Concepts](#basic-concepts)
3. [Creating and Starting Coroutines](#creating-coroutines)
4. [Suspend Functions](#suspend-functions)
5. [Coroutine Builders](#coroutine-builders)
6. [Coroutine Context and Dispatchers](#context-dispatchers)
7. [Structured Concurrency](#structured-concurrency)
8. [Exception Handling](#exception-handling)
9. [Channels and Flow](#channels-flow)
10. [Advanced Patterns](#advanced-patterns)
11. [Testing Coroutines](#testing)
12. [Best Practices](#best-practices)

## 1. Introduction to Coroutines {#introduction}

Kotlin coroutines are a concurrency design pattern that revolutionizes how we handle asynchronous programming. Think of coroutines as lightweight threads that can be suspended and resumed without blocking the underlying thread. Unlike traditional threading models, coroutines allow you to write asynchronous code that reads like synchronous code, eliminating callback hell and making complex async operations much more manageable.

### Understanding the Problem Coroutines Solve

Before coroutines, handling asynchronous operations often led to complex nested callbacks:

```kotlin
// Traditional callback approach (pseudo-code)
fetchUser(userId) { user ->
    fetchPosts(user.id) { posts ->
        fetchComments(posts.first().id) { comments ->
            // Finally do something with the data
            updateUI(user, posts, comments)
        }
    }
}
```

With coroutines, the same operation becomes:

```kotlin
// Coroutine approach
suspend fun loadUserData(userId: String) {
    val user = fetchUser(userId)
    val posts = fetchPosts(user.id)
    val comments = fetchComments(posts.first().id)
    updateUI(user, posts, comments)
}
```

### Key Benefits

- **Lightweight**: You can create thousands of coroutines with minimal memory overhead. A typical coroutine uses only a few KB of memory compared to threads which require 1-2 MB
- **Sequential Code**: Write asynchronous code that looks synchronous, making it easier to read, debug, and maintain
- **Structured Concurrency**: Automatic lifecycle management ensures coroutines don't leak and are properly cancelled when no longer needed
- **Cancellation Support**: Built-in cooperative cancellation mechanism prevents resource leaks and allows for responsive applications
- **Exception Handling**: Unified exception handling model that works consistently across synchronous and asynchronous code

### Coroutines vs Threads

| Aspect | Threads | Coroutines |
|--------|---------|------------|
| Memory overhead | 1-2 MB per thread | Few KB per coroutine |
| Creation cost | Expensive | Very cheap |
| Context switching | Expensive OS operation | Cheap user-space operation |
| Blocking behavior | Blocks entire thread | Suspends only the coroutine |
| Lifecycle management | Manual | Automatic with structured concurrency |

### Setup
Add the following dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // For Android
}
```

## 2. Basic Concepts {#basic-concepts}

Understanding these fundamental concepts is crucial for mastering coroutines.

### Coroutine
A coroutine is a computation that can be suspended and resumed later without blocking the thread it's running on. Think of it as a function that can pause its execution at certain points (suspension points) and resume later, potentially on a different thread. This suspension capability is what makes coroutines so powerful for asynchronous programming.

```kotlin
// This is NOT a coroutine - it blocks the thread
fun blockingFunction() {
    Thread.sleep(1000) // Blocks the entire thread
    println("Done after 1 second")
}

// This IS a coroutine - it suspends without blocking
suspend fun suspendingFunction() {
    delay(1000) // Suspends the coroutine, thread remains free
    println("Done after 1 second")
}
```

### Suspension Point
A suspension point is where a coroutine can pause its execution. These are marked by calls to `suspend` functions. When a coroutine reaches a suspension point, it can be paused, allowing the thread to execute other work. Later, when the suspended operation completes, the coroutine resumes execution.

```kotlin
suspend fun exampleWithSuspensionPoints() {
    println("Starting")
    delay(1000) // Suspension point 1
    println("After first delay")
    
    val result = async { computeSomething() } // Suspension point 2 (when we await)
    val value = result.await() // Suspension point 3
    
    println("Result: $value")
    delay(500) // Suspension point 4
    println("Finished")
}
```

### Coroutine Scope
A CoroutineScope defines the lifetime and context for coroutines. It's a crucial concept for structured concurrency - ensuring that coroutines are properly managed and don't outlive their intended lifecycle. Every coroutine must run within a scope.

Think of a scope as a "container" for coroutines that:
- Provides a context (dispatcher, job, etc.)
- Manages the lifecycle of all coroutines within it
- Ensures proper cleanup when the scope is cancelled

```kotlin
class UserRepository {
    // Create a scope for this repository's coroutines
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    fun fetchUserData(userId: String) {
        // Launch coroutine within the scope
        scope.launch {
            val user = fetchUserFromApi(userId)
            // Process user data
        }
    }
    
    fun cleanup() {
        // Cancel all coroutines in this scope
        scope.cancel()
    }
}
```

### Job
A Job represents the lifecycle of a coroutine. It's a handle that can be used to:
- Check if a coroutine is active, completed, or cancelled
- Cancel a coroutine
- Wait for a coroutine to complete
- Establish parent-child relationships between coroutines

```kotlin
fun main() = runBlocking {
    val job = launch {
        repeat(10) { i ->
            println("Working... $i")
            delay(500)
        }
    }
    
    delay(2000) // Let it work for 2 seconds
    
    println("Job is active: ${job.isActive}")
    job.cancel() // Cancel the job
    job.join() // Wait for cancellation to complete
    println("Job is cancelled: ${job.isCancelled}")
}
```

### Deferred
A Deferred is a special type of Job that represents a coroutine that will produce a result. It's returned by the `async` coroutine builder and allows you to retrieve the result using `await()`.

```kotlin
fun main() = runBlocking {
    val deferred: Deferred<String> = async {
        delay(1000)
        "Hello from async coroutine"
    }
    
    // Do other work while the async operation runs
    println("Doing other work...")
    delay(500)
    
    // Get the result (this suspends until the result is available)
    val result = deferred.await()
    println("Result: $result")
}
```

## 3. Creating and Starting Coroutines {#creating-coroutines}

Let's explore how to create and start coroutines, building from simple examples to more complex scenarios.

### Your First Coroutine

The `runBlocking` builder is often your first introduction to coroutines. It creates a coroutine and blocks the current thread until the coroutine completes. While not suitable for production code (except in main functions and tests), it's perfect for learning and simple scripts.

```kotlin
import kotlinx.coroutines.*

fun main() {
    println("Program starts: ${Thread.currentThread().name}")
    
    // Create a coroutine scope that blocks the main thread
    runBlocking {
        println("Inside runBlocking: ${Thread.currentThread().name}")
        println("Hello")
        delay(1000L) // Non-blocking delay (suspends the coroutine, not the thread)
        println("World!")
    }
    
    println("Program ends: ${Thread.currentThread().name}")
}
```

**Key points about `runBlocking`:**
- It bridges the gap between blocking and non-blocking code
- The main thread is blocked until all coroutines within the block complete
- Use it only in main functions, tests, or when you need to bridge blocking and suspending code

### Understanding delay() vs Thread.sleep()

This is a fundamental difference that new coroutine users often misunderstand:

```kotlin
import kotlinx.coroutines.*

fun demonstrateDelay() = runBlocking {
    println("=== Using delay() ===")
    val startTime = System.currentTimeMillis()
    
    // Launch 3 coroutines that delay for 1 second each
    repeat(3) { i ->
        launch {
            delay(1000) // Suspends the coroutine, doesn't block the thread
            println("Coroutine $i finished after ${System.currentTimeMillis() - startTime}ms")
        }
    }
    
    // All coroutines will finish around the same time (after ~1 second)
}

fun demonstrateThreadSleep() = runBlocking {
    println("=== Using Thread.sleep() ===")
    val startTime = System.currentTimeMillis()
    
    // Launch 3 coroutines that sleep for 1 second each
    repeat(3) { i ->
        launch {
            Thread.sleep(1000) // Blocks the entire thread!
            println("Coroutine $i finished after ${System.currentTimeMillis() - startTime}ms")
        }
    }
    
    // Coroutines will finish sequentially (after ~3 seconds total)
}

fun main() {
    demonstrateDelay()
    Thread.sleep(2000) // Wait for first demo
    demonstrateThreadSleep()
}
```

### Basic Coroutine Example with Detailed Explanation

```kotlin
import kotlinx.coroutines.*

fun main() {
    println("Start: ${Thread.currentThread().name}")
    
    // runBlocking creates a coroutine scope and blocks until completion
    runBlocking {
        println("Inside runBlocking: ${Thread.currentThread().name}")
        
        // launch starts a new coroutine concurrently
        val job = launch {
            println("Inside launch: ${Thread.currentThread().name}")
            delay(1000L) // Suspend for 1 second
            println("Coroutine completed")
        }
        
        // This line executes immediately, not waiting for the launched coroutine
        println("Main thread continues immediately")
        
        // Optional: wait for the launched coroutine to complete
        job.join()
        println("After job.join()")
    }
    
    println("End: ${Thread.currentThread().name}")
}
```

**Execution flow:**
1. "Start" is printed on the main thread
2. runBlocking creates a coroutine scope
3. launch starts a new coroutine but doesn't block
4. "Main thread continues immediately" is printed
5. The launched coroutine starts executing
6. job.join() waits for the launched coroutine to complete
7. "End" is printed after everything completes

### Multiple Coroutines Example

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    println("Starting multiple coroutines")
    
    // Launch multiple coroutines concurrently
    val job1 = launch {
        repeat(3) { i ->
            println("Coroutine 1 - iteration $i")
            delay(500)
        }
    }
    
    val job2 = launch {
        repeat(2) { i ->
            println("Coroutine 2 - iteration $i")
            delay(800)
        }
    }
    
    val job3 = launch {
        delay(1200)
        println("Coroutine 3 - finished")
    }
    
    // Wait for all coroutines to complete
    joinAll(job1, job2, job3)
    println("All coroutines completed")
}
```

This example demonstrates how multiple coroutines run concurrently, interleaving their execution based on their delay patterns.

## 4. Suspend Functions {#suspend-functions}

Suspend functions are the building blocks of coroutines. Understanding them deeply is crucial for effective coroutine programming.

### What Makes a Function "Suspend"?

The `suspend` keyword transforms a regular function into one that can be paused and resumed. This transformation happens at the compiler level - the Kotlin compiler generates a state machine that can save and restore the function's execution state.

```kotlin
// Regular function - runs to completion, cannot be paused
fun regularFunction(): String {
    Thread.sleep(1000) // Blocks the entire thread
    return "Result from regular function"
}

// Suspend function - can be paused and resumed
suspend fun suspendFunction(): String {
    delay(1000) // Suspends the coroutine, thread remains free
    return "Result from suspend function"
}

fun main() = runBlocking {
    println("Calling suspend function...")
    val result = suspendFunction() // This call can be suspended
    println(result)
}
```

### The Continuation Concept

Behind the scenes, suspend functions use a concept called "Continuation." When a suspend function is called, it receives an additional parameter (invisible to you) called a continuation, which represents "what to do after this function completes."

```kotlin
// What you write:
suspend fun fetchData(): String {
    delay(1000)
    return "Data"
}

// What the compiler generates (conceptually):
fun fetchData(continuation: Continuation<String>): Any? {
    // State machine that handles suspension and resumption
    // Returns COROUTINE_SUSPENDED if suspended, or the result if completed
}
```

### Rules for Suspend Functions

1. **Can only be called from coroutines or other suspend functions**
2. **Cannot be called from regular functions directly**
3. **Can call regular functions freely**
4. **Should be main-safe (more on this later)**

```kotlin
suspend fun validSuspendFunction() {
    delay(100) // ✅ Can call other suspend functions
    println("Hello") // ✅ Can call regular functions
    
    val result = anotherSuspendFunction() // ✅ Valid
    regularFunction() // ✅ Valid
}

fun regularFunction() {
    // delay(100) // ❌ Cannot call suspend functions
    // fetchData() // ❌ Compilation error
    
    // To call suspend functions from regular functions, need a coroutine scope:
    runBlocking {
        delay(100) // ✅ Now it's valid
    }
}

suspend fun anotherSuspendFunction(): String {
    delay(500)
    return "Another result"
}
```

### Practical Suspend Function Examples

```kotlin
import kotlinx.coroutines.*
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI

// Simulating network operations
suspend fun fetchUserProfile(userId: String): UserProfile {
    println("Fetching user profile for $userId...")
    delay(1000) // Simulate network delay
    return UserProfile(userId, "John Doe", "john@example.com")
}

suspend fun fetchUserPosts(userId: String): List<Post> {
    println("Fetching posts for $userId...")
    delay(800) // Simulate network delay
    return listOf(
        Post("1", "First post", "Content 1"),
        Post("2", "Second post", "Content 2")
    )
}

suspend fun fetchUserFriends(userId: String): List<String> {
    println("Fetching friends for $userId...")
    delay(600) // Simulate network delay
    return listOf("friend1", "friend2", "friend3")
}

data class UserProfile(val id: String, val name: String, val email: String)
data class Post(val id: String, val title: String, val content: String)

fun main() = runBlocking {
    val userId = "user123"
    
    // Sequential execution - each operation waits for the previous one
    println("=== Sequential Execution ===")
    val startTime = System.currentTimeMillis()
    
    val profile = fetchUserProfile(userId)
    val posts = fetchUserPosts(userId)
    val friends = fetchUserFriends(userId)
    
    val sequentialTime = System.currentTimeMillis() - startTime
    println("Sequential execution took: ${sequentialTime}ms")
    println("Profile: ${profile.name}, Posts: ${posts.size}, Friends: ${friends.size}")
}
```

### Sequential vs Concurrent Execution - Deep Dive

Understanding when to use sequential vs concurrent execution is crucial for performance:

```kotlin
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

suspend fun doSomething(): String {
    delay(1000L) // Simulate some work
    return "Result from doSomething"
}

suspend fun doSomethingElse(): String {
    delay(500L) // Simulate some work
    return "Result from doSomethingElse"
}

suspend fun doThirdThing(): String {
    delay(800L) // Simulate some work
    return "Result from doThirdThing"
}

fun main() = runBlocking {
    // Sequential execution: operations run one after another
    // Total time ≈ 1000 + 500 + 800 = 2300ms
    val sequentialTime = measureTimeMillis {
        val result1 = doSomething()      // Wait 1000ms
        val result2 = doSomethingElse()  // Then wait 500ms
        val result3 = doThirdThing()     // Then wait 800ms
        println("Sequential: $result1, $result2, $result3")
    }
    println("Sequential took $sequentialTime ms")
    
    println("\n" + "=".repeat(50) + "\n")
    
    // Concurrent execution: operations run simultaneously
    // Total time ≈ max(1000, 500, 800) = 1000ms
    val concurrentTime = measureTimeMillis {
        val deferred1 = async { doSomething() }      // Start immediately
        val deferred2 = async { doSomethingElse() }  // Start immediately
        val deferred3 = async { doThirdThing() }     // Start immediately
        
        // Wait for all results
        val result1 = deferred1.await()
        val result2 = deferred2.await()
        val result3 = deferred3.await()
        println("Concurrent: $result1, $result2, $result3")
    }
    println("Concurrent took $concurrentTime ms")
    
    println("\n" + "=".repeat(50) + "\n")
    
    // Mixed execution: some sequential, some concurrent
    val mixedTime = measureTimeMillis {
        // First, fetch user data
        val userData = doSomething() // Must complete first
        
        // Then fetch related data concurrently
        val deferred2 = async { doSomethingElse() }
        val deferred3 = async { doThirdThing() }
        
        val result2 = deferred2.await()
        val result3 = deferred3.await()
        
        println("Mixed: $userData, $result2, $result3")
    }
    println("Mixed took $mixedTime ms")
}
```

### When to Use Sequential vs Concurrent Execution

**Use Sequential when:**
- Operations depend on each other (result of first operation needed for second)
- You want to control resource usage (avoid overwhelming a server)
- Operations should happen in a specific order

**Use Concurrent when:**
- Operations are independent
- You want maximum performance
- Operations can safely run in parallel

```kotlin
// Example: User dashboard data loading
suspend fun loadUserDashboard(userId: String) {
    // Sequential: Must get user first to determine permissions
    val user = fetchUser(userId)
    
    if (user.hasPermission("VIEW_ANALYTICS")) {
        // Concurrent: These can load simultaneously
        val analyticsDeferred = async { fetchAnalytics(userId) }
        val reportsDeferred = async { fetchReports(userId) }
        val notificationsDeferred = async { fetchNotifications(userId) }
        
        val analytics = analyticsDeferred.await()
        val reports = reportsDeferred.await()
        val notifications = notificationsDeferred.await()
        
        // Use the data...
    }
}
```
```

## 5. Coroutine Builders {#coroutine-builders}

Coroutine builders are functions that create and start coroutines. Each builder serves a specific purpose and understanding when to use each one is crucial for effective coroutine programming.

### runBlocking - Bridging Blocking and Non-blocking Worlds

`runBlocking` is unique among coroutine builders because it blocks the current thread until all coroutines within it complete. It's primarily used in main functions, tests, and when you need to call suspend functions from regular blocking code.

```kotlin
import kotlinx.coroutines.*

fun main() {
    println("Before runBlocking: ${Thread.currentThread().name}")
    
    // runBlocking creates a coroutine and blocks the main thread
    runBlocking {
        println("Inside runBlocking: ${Thread.currentThread().name}")
        
        launch {
            delay(1000L)
            println("Launch completed: ${Thread.currentThread().name}")
        }
        
        delay(500L)
        println("runBlocking continues: ${Thread.currentThread().name}")
    } // This point is reached only after ALL coroutines inside complete
    
    println("After runBlocking: ${Thread.currentThread().name}")
}
```

**Key characteristics of runBlocking:**
- Blocks the calling thread until completion
- Creates a new coroutine scope
- Returns the result of the last expression
- Should NOT be used in production code except for main functions and tests
- Can be used to call suspend functions from regular functions

```kotlin
// Example: Using runBlocking to call suspend functions from regular code
class DataRepository {
    suspend fun fetchData(): String {
        delay(1000)
        return "Important data"
    }
}

// Regular function that needs to call suspend function
fun processDataSynchronously(): String {
    val repository = DataRepository()
    
    // Use runBlocking to bridge to suspend world
    return runBlocking {
        repository.fetchData()
    }
}
```

### launch - Fire and Forget Coroutines

`launch` starts a coroutine that doesn't return a result. It's perfect for fire-and-forget operations like logging, analytics, or side effects. It returns a `Job` that can be used to control the coroutine's lifecycle.

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    println("Starting launch examples")
    
    // Basic launch - fire and forget
    launch {
        repeat(3) { i ->
            println("Background task iteration $i")
            delay(500L)
        }
    }
    
    // Launch with job control
    val job = launch {
        repeat(10) { i ->
            println("Cancellable task iteration $i")
            delay(300L)
        }
    }
    
    delay(1000L) // Let it run for a bit
    println("Cancelling the second job")
    job.cancel() // Cancel the job
    job.join() // Wait for cancellation to complete
    
    delay(2000L) // Wait for first launch to complete
    println("All done")
}
```

**Advanced launch example with error handling:**

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    // Launch with exception handler
    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        println("Caught exception: ${exception.message}")
    }
    
    val job1 = launch(exceptionHandler) {
        delay(1000L)
        throw RuntimeException("Something went wrong in job1!")
    }
    
    val job2 = launch {
        try {
            delay(2000L)
            println("Job2 completed successfully")
        } catch (e: CancellationException) {
            println("Job2 was cancelled")
            throw e // Re-throw cancellation exception
        }
    }
    
    // Wait for jobs with different strategies
    try {
        job1.join() // Wait for completion (exception is handled by handler)
    } catch (e: Exception) {
        println("This won't be reached due to exception handler")
    }
    
    job2.join()
    println("Both jobs handled")
}
```

### async - Concurrent Computations with Results

`async` is used when you need to run coroutines concurrently and collect their results. It returns a `Deferred<T>` which is a non-blocking future that represents a promise of a result.

```kotlin
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

suspend fun calculateSum(x: Int, y: Int): Int {
    delay(1000L) // Simulate complex calculation
    println("Calculated $x + $y on ${Thread.currentThread().name}")
    return x + y
}

suspend fun calculateProduct(x: Int, y: Int): Int {
    delay(1200L) // Simulate complex calculation
    println("Calculated $x * $y on ${Thread.currentThread().name}")
    return x * y
}

fun main() = runBlocking {
    println("=== Sequential Execution ===")
    val sequentialTime = measureTimeMillis {
        val sum = calculateSum(2, 3)
        val product = calculateProduct(4, 5)
        println("Sum: $sum, Product: $product")
    }
    println("Sequential took: ${sequentialTime}ms\n")
    
    println("=== Concurrent Execution with async ===")
    val concurrentTime = measureTimeMillis {
        // Start both calculations concurrently
        val sumDeferred = async { calculateSum(2, 3) }
        val productDeferred = async { calculateProduct(4, 5) }
        
        // Wait for results (this is where suspension happens if needed)
        val sum = sumDeferred.await()
        val product = productDeferred.await()
        println("Sum: $sum, Product: $product")
    }
    println("Concurrent took: ${concurrentTime}ms")
}
```

**Important async patterns:**

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    // Pattern 1: Multiple independent async operations
    val deferredResults = List(5) { index ->
        async {
            delay(1000L + index * 100L) // Varying delays
            "Result $index"
        }
    }
    
    // Collect all results
    val results = deferredResults.awaitAll()
    println("All results: $results")
    
    println("\n" + "=".repeat(40) + "\n")
    
    // Pattern 2: async with exception handling
    val riskyAsync = async {
        delay(500L)
        if (Math.random() > 0.5) {
            throw RuntimeException("Random failure!")
        }
        "Success result"
    }
    
    try {
        val result = riskyAsync.await()
        println("Got result: $result")
    } catch (e: Exception) {
        println("Async operation failed: ${e.message}")
    }
    
    println("\n" + "=".repeat(40) + "\n")
    
    // Pattern 3: Lazy async (doesn't start until await() is called)
    val lazyAsync = async(start = CoroutineStart.LAZY) {
        println("Lazy async is finally starting!")
        delay(1000L)
        "Lazy result"
    }
    
    println("Lazy async created but not started yet")
    delay(500L)
    println("Now starting lazy async...")
    val lazyResult = lazyAsync.await() // This starts the coroutine
    println("Lazy result: $lazyResult")
}
```

### Comparing Coroutine Builders

| Builder | Returns | Purpose | Blocking | Exception Handling |
|---------|---------|---------|----------|-------------------|
| `runBlocking` | T | Bridge blocking/non-blocking | Yes | Propagates |
| `launch` | Job | Fire-and-forget | No | CoroutineExceptionHandler |
| `async` | Deferred<T> | Concurrent computation | No | Exception on await() |

### Real-world Example: Data Loading Pipeline

```kotlin
import kotlinx.coroutines.*

data class User(val id: String, val name: String)
data class Profile(val userId: String, val bio: String)
data class Settings(val userId: String, val theme: String)

class UserService {
    suspend fun fetchUser(id: String): User {
        delay(800L) // Simulate API call
        return User(id, "User $id")
    }
    
    suspend fun fetchProfile(userId: String): Profile {
        delay(600L) // Simulate API call
        return Profile(userId, "Bio for $userId")
    }
    
    suspend fun fetchSettings(userId: String): Settings {
        delay(400L) // Simulate API call
        return Settings(userId, "dark")
    }
    
    suspend fun logUserAccess(userId: String) {
        delay(100L) // Simulate logging
        println("Logged access for user: $userId")
    }
}

fun main() = runBlocking {
    val userService = UserService()
    val userId = "123"
    
    println("Loading user data...")
    
    // First, fetch the user (required for other operations)
    val user = userService.fetchUser(userId)
    println("User loaded: ${user.name}")
    
    // Then fetch profile and settings concurrently
    val profileDeferred = async { userService.fetchProfile(user.id) }
    val settingsDeferred = async { userService.fetchSettings(user.id) }
    
    // Fire-and-forget logging (doesn't block other operations)
    launch { userService.logUserAccess(user.id) }
    
    // Wait for concurrent operations
    val profile = profileDeferred.await()
    val settings = settingsDeferred.await()
    
    println("Profile: ${profile.bio}")
    println("Settings: ${settings.theme}")
    println("User data loading complete!")
}
```

This example demonstrates:
- Sequential execution where needed (user must be fetched first)
- Concurrent execution for independent operations (profile and settings)
- Fire-and-forget operations (logging) that don't block the main flow
```

## 6. Coroutine Context and Dispatchers {#context-dispatchers}

Understanding Coroutine Context and Dispatchers is crucial for controlling where and how your coroutines execute. This directly impacts performance, thread safety, and resource utilization.

### Understanding Coroutine Context

A CoroutineContext is a set of elements that define the behavior and environment of a coroutine. Think of it as the "configuration" for how a coroutine should run. It's an indexed set of Element instances, where each element has a unique key.

Key elements in a CoroutineContext:
- **Job**: Controls the lifecycle
- **Dispatcher**: Determines the thread(s) for execution
- **CoroutineName**: For debugging and logging
- **CoroutineExceptionHandler**: For handling uncaught exceptions

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    println("Main context: ${coroutineContext}")
    println("Main job: ${coroutineContext[Job]}")
    println("Main dispatcher: ${coroutineContext[ContinuationInterceptor]}")
    
    launch(Dispatchers.IO + CoroutineName("IOCoroutine") + Job()) {
        println("\nIO context: ${coroutineContext}")
        println("IO job: ${coroutineContext[Job]}")
        println("IO dispatcher: ${coroutineContext[ContinuationInterceptor]}")
        println("IO name: ${coroutineContext[CoroutineName]}")
        println("Thread: ${Thread.currentThread().name}")
    }
    
    delay(100L) // Wait for the coroutine to complete
}
```

### Context Inheritance and Combination

Child coroutines inherit the context from their parent, but you can override specific elements:

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking(CoroutineName("MainCoroutine")) {
    println("Parent context: ${coroutineContext[CoroutineName]}")
    
    // Child inherits parent context
    launch {
        println("Child 1 inherited name: ${coroutineContext[CoroutineName]}")
        println("Child 1 thread: ${Thread.currentThread().name}")
    }
    
    // Child overrides specific context elements
    launch(Dispatchers.IO + CoroutineName("CustomChild")) {
        println("Child 2 custom name: ${coroutineContext[CoroutineName]}")
        println("Child 2 thread: ${Thread.currentThread().name}")
    }
    
    // Combining multiple context elements
    val customContext = Dispatchers.Default + 
                       CoroutineName("WorkerCoroutine") + 
                       CoroutineExceptionHandler { _, ex ->
                           println("Caught: ${ex.message}")
                       }
    
    launch(customContext) {
        println("Custom context name: ${coroutineContext[CoroutineName]}")
        println("Custom context thread: ${Thread.currentThread().name}")
        throw RuntimeException("Test exception")
    }
    
    delay(500L)
}
```

### Dispatchers - Controlling Thread Execution

Dispatchers determine which thread or thread pool your coroutine runs on. Choosing the right dispatcher is crucial for performance and correctness.

#### Dispatchers.Default - CPU-Intensive Work

Best for computationally intensive tasks that don't block on I/O. Uses a shared pool of background threads (typically equal to the number of CPU cores).

```kotlin
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

fun cpuIntensiveTask(n: Int): Long {
    var result = 0L
    for (i in 1..n) {
        result += i * i
    }
    return result
}

fun main() = runBlocking {
    println("Available processors: ${Runtime.getRuntime().availableProcessors()}")
    
    val time = measureTimeMillis {
        // Run CPU-intensive tasks concurrently
        val deferredResults = List(4) { index ->
            async(Dispatchers.Default) {
                println("Task $index running on: ${Thread.currentThread().name}")
                cpuIntensiveTask(10_000_000)
            }
        }
        
        val results = deferredResults.awaitAll()
        println("Results: $results")
    }
    
    println("Parallel execution took: ${time}ms")
}
```

#### Dispatchers.IO - I/O Operations

Optimized for I/O operations that block threads (network calls, file operations, database queries). Uses a larger shared pool that can grow as needed.

```kotlin
import kotlinx.coroutines.*
import java.io.File
import java.net.URL

suspend fun readFileContent(filename: String): String = withContext(Dispatchers.IO) {
    println("Reading file on: ${Thread.currentThread().name}")
    // Simulate file reading
    delay(1000L) // In real code, this would be actual file I/O
    "Content of $filename"
}

suspend fun fetchFromNetwork(url: String): String = withContext(Dispatchers.IO) {
    println("Network call on: ${Thread.currentThread().name}")
    // Simulate network call
    delay(1500L) // In real code, this would be actual network I/O
    "Data from $url"
}

fun main() = runBlocking {
    println("Starting I/O operations...")
    
    val startTime = System.currentTimeMillis()
    
    // Concurrent I/O operations
    val fileDeferred = async { readFileContent("data.txt") }
    val networkDeferred = async { fetchFromNetwork("https://api.example.com") }
    val dbDeferred = async(Dispatchers.IO) { 
        println("DB query on: ${Thread.currentThread().name}")
        delay(800L) // Simulate database query
        "Database result"
    }
    
    val fileContent = fileDeferred.await()
    val networkData = networkDeferred.await()
    val dbResult = dbDeferred.await()
    
    val totalTime = System.currentTimeMillis() - startTime
    
    println("File: $fileContent")
    println("Network: $networkData")
    println("Database: $dbResult")
    println("Total time: ${totalTime}ms")
}
```

#### Dispatchers.Main - UI Thread Operations

Used for UI updates in Android and JavaFX applications. Ensures operations run on the main/UI thread.

```kotlin
// Android example (conceptual)
class MainActivity : AppCompatActivity() {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    fun loadUserData() {
        scope.launch {
            // Show loading indicator on UI thread
            progressBar.visibility = View.VISIBLE
            
            try {
                // Network call on IO thread
                val userData = withContext(Dispatchers.IO) {
                    fetchUserFromApi()
                }
                
                // Update UI on Main thread (automatically back to Main dispatcher)
                userNameText.text = userData.name
                userEmailText.text = userData.email
                
            } catch (e: Exception) {
                // Handle error on UI thread
                showErrorDialog(e.message)
            } finally {
                // Hide loading indicator on UI thread
                progressBar.visibility = View.GONE
            }
        }
    }
}
```

#### Dispatchers.Unconfined - Advanced Use Case

Starts in the caller thread but resumes in whatever thread the suspending function resumes in. Generally not recommended for regular use.

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    println("Main thread: ${Thread.currentThread().name}")
    
    launch(Dispatchers.Unconfined) {
        println("Unconfined 1: ${Thread.currentThread().name}")
        delay(100L)
        println("Unconfined 2: ${Thread.currentThread().name}") // May be different thread
    }
    
    launch(Dispatchers.Default) {
        println("Default 1: ${Thread.currentThread().name}")
        delay(100L)
        println("Default 2: ${Thread.currentThread().name}") // Same thread pool
    }
    
    delay(200L)
}
```

### Custom Dispatchers

For specialized use cases, you can create custom dispatchers:

```kotlin
import kotlinx.coroutines.*
import java.util.concurrent.Executors

fun main() = runBlocking {
    // Single-threaded executor
    val singleThreadDispatcher = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "CustomSingleThread").apply {
            isDaemon = true
        }
    }.asCoroutineDispatcher()
    
    // Fixed thread pool
    val fixedThreadPoolDispatcher = Executors.newFixedThreadPool(3) { runnable ->
        Thread(runnable, "CustomPoolThread").apply {
            isDaemon = true
        }
    }.asCoroutineDispatcher()
    
    // Using custom dispatchers
    launch(singleThreadDispatcher) {
        repeat(3) { i ->
            println("Single thread task $i: ${Thread.currentThread().name}")
            delay(500L)
        }
    }
    
    repeat(5) { taskId ->
        launch(fixedThreadPoolDispatcher) {
            println("Pool task $taskId: ${Thread.currentThread().name}")
            delay(1000L)
        }
    }
    
    delay(3000L)
    
    // Clean up custom dispatchers
    singleThreadDispatcher.close()
    fixedThreadPoolDispatcher.close()
}
```

### withContext - Switching Dispatchers

`withContext` allows you to switch dispatchers for specific operations:

```kotlin
import kotlinx.coroutines.*

class DataProcessor {
    suspend fun processLargeDataset(data: List<Int>): List<Int> {
        // Switch to Default dispatcher for CPU-intensive work
        return withContext(Dispatchers.Default) {
            println("Processing on: ${Thread.currentThread().name}")
            data.map { it * it }.sorted() // CPU-intensive operations
        }
    }
    
    suspend fun saveToDatabase(data: List<Int>) {
        // Switch to IO dispatcher for database operations
        withContext(Dispatchers.IO) {
            println("Saving to DB on: ${Thread.currentThread().name}")
            delay(1000L) // Simulate database save
        }
    }
    
    suspend fun updateUI(message: String) {
        // Switch to Main dispatcher for UI updates (in Android/JavaFX)
        withContext(Dispatchers.Main) {
            println("Updating UI on: ${Thread.currentThread().name}")
            // Update UI components
        }
    }
}

fun main() = runBlocking(Dispatchers.Main) {
    val processor = DataProcessor()
    val data = (1..1000).toList()
    
    println("Starting on: ${Thread.currentThread().name}")
    
    // Each operation automatically switches to the appropriate dispatcher
    val processedData = processor.processLargeDataset(data)
    processor.saveToDatabase(processedData)
    processor.updateUI("Data processing complete!")
    
    println("Back to: ${Thread.currentThread().name}")
}
```

### Performance Considerations

**Dispatcher Selection Guidelines:**

1. **Dispatchers.Main**: UI updates only
2. **Dispatchers.IO**: File I/O, network calls, database operations
3. **Dispatchers.Default**: CPU-intensive computations, data processing
4. **Custom dispatchers**: Specialized requirements (single-threaded operations, specific thread pools)

**Common Anti-patterns:**

```kotlin
// ❌ Wrong: Using Default for I/O operations
launch(Dispatchers.Default) {
    val data = readFileFromDisk() // Blocks a limited thread pool
}

// ✅ Correct: Using IO for I/O operations
launch(Dispatchers.IO) {
    val data = readFileFromDisk() // Uses expandable thread pool
}

// ❌ Wrong: Using IO for CPU-intensive work
launch(Dispatchers.IO) {
    val result = heavyComputation() // Wastes I/O threads
}

// ✅ Correct: Using Default for CPU-intensive work
launch(Dispatchers.Default) {
    val result = heavyComputation() // Uses optimized thread pool
}
```
```

## 7. Structured Concurrency {#structured-concurrency}

Structured concurrency ensures that coroutines are properly managed and cancelled when their scope ends.

### Coroutine Scope

```kotlin
import kotlinx.coroutines.*

class MyRepository {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    fun fetchData() {
        scope.launch {
            // This coroutine is bound to the scope
            val data = performNetworkCall()
            println("Data received: $data")
        }
    }
    
    private suspend fun performNetworkCall(): String {
        delay(2000L)
        return "Network data"
    }
    
    fun cleanup() {
        scope.cancel() // Cancels all coroutines in this scope
    }
}

fun main() = runBlocking {
    val repository = MyRepository()
    repository.fetchData()
    
    delay(1000L)
    repository.cleanup()
    delay(2000L) // Wait to see if the network call completes
}
```

### Parent-Child Relationship

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    val parentJob = launch {
        println("Parent starts")
        
        val child1 = launch {
            try {
                delay(2000L)
                println("Child 1 completes")
            } catch (e: CancellationException) {
                println("Child 1 cancelled")
            }
        }
        
        val child2 = launch {
            try {
                delay(3000L)
                println("Child 2 completes")
            } catch (e: CancellationException) {
                println("Child 2 cancelled")
            }
        }
        
        delay(1000L)
        println("Parent work done")
    }
    
    delay(1500L)
    parentJob.cancel() // Cancels parent and all children
    parentJob.join()
    println("All done")
}
```

## 8. Exception Handling {#exception-handling}

### Basic Exception Handling

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    val job = launch {
        try {
            delay(1000L)
            throw RuntimeException("Something went wrong!")
        } catch (e: Exception) {
            println("Caught exception: ${e.message}")
        }
    }
    
    job.join()
    println("Job completed")
}
```

### Exception Propagation with async

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    val deferred = async {
        delay(1000L)
        throw RuntimeException("Async exception!")
    }
    
    try {
        deferred.await() // Exception is thrown here
    } catch (e: Exception) {
        println("Caught async exception: ${e.message}")
    }
}
```

### CoroutineExceptionHandler

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        println("Handled exception: ${exception.message}")
    }
    
    val job = launch(handler) {
        delay(1000L)
        throw RuntimeException("Unhandled exception!")
    }
    
    job.join()
    println("Program continues")
}
```

### SupervisorJob
Prevents child failures from cancelling siblings.

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    val supervisor = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.Default + supervisor)
    
    val job1 = scope.launch {
        delay(1000L)
        throw RuntimeException("Job 1 failed!")
    }
    
    val job2 = scope.launch {
        try {
            delay(2000L)
            println("Job 2 completed successfully")
        } catch (e: CancellationException) {
            println("Job 2 was cancelled")
        }
    }
    
    delay(3000L)
    supervisor.cancel()
}
```

## 9. Channels and Flow {#channels-flow}

### Channels
Channels provide a way to communicate between coroutines.

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

fun main() = runBlocking {
    val channel = Channel<Int>()
    
    // Producer
    launch {
        for (x in 1..5) {
            channel.send(x * x)
            delay(100L)
        }
        channel.close()
    }
    
    // Consumer
    for (y in channel) {
        println("Received: $y")
    }
}
```

### Buffered Channels

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

fun main() = runBlocking {
    val channel = Channel<Int>(capacity = 4) // Buffer size of 4
    
    launch {
        repeat(10) {
            channel.send(it)
            println("Sent: $it")
        }
        channel.close()
    }
    
    delay(1000L) // Let producer run first
    
    for (value in channel) {
        println("Received: $value")
        delay(200L)
    }
}
```

### Flow
Flow is a stream of values that are computed asynchronously.

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun simpleFlow() = flow<Int> {
    for (i in 1..3) {
        delay(1000L)
        emit(i)
    }
}

fun main() = runBlocking {
    println("Collecting flow:")
    simpleFlow().collect { value ->
        println("Collected: $value")
    }
}
```

### Flow Operators

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun numberFlow() = flow {
    for (i in 1..10) {
        emit(i)
        delay(100L)
    }
}

fun main() = runBlocking {
    numberFlow()
        .filter { it % 2 == 0 } // Even numbers only
        .map { it * it } // Square them
        .take(3) // Take first 3
        .collect { println("Result: $it") }
}
```

### StateFlow and SharedFlow

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class Counter {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()
    
    fun increment() {
        _count.value++
    }
}

fun main() = runBlocking {
    val counter = Counter()
    
    // Collector 1
    launch {
        counter.count.collect { count ->
            println("Collector 1: $count")
        }
    }
    
    // Collector 2
    launch {
        counter.count.collect { count ->
            println("Collector 2: $count")
        }
    }
    
    delay(100L)
    repeat(5) {
        counter.increment()
        delay(500L)
    }
}
```

## 10. Advanced Patterns {#advanced-patterns}

### Producer-Consumer Pattern

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

fun CoroutineScope.produceNumbers() = produce<Int> {
    var x = 1
    while (true) {
        send(x++)
        delay(100L)
    }
}

fun CoroutineScope.consumeNumbers(channel: ReceiveChannel<Int>) = launch {
    for (number in channel) {
        println("Consumed: $number")
        if (number >= 10) break
    }
}

fun main() = runBlocking {
    val numbers = produceNumbers()
    consumeNumbers(numbers)
    delay(2000L)
    numbers.cancel()
}
```

### Fan-out Pattern

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

fun CoroutineScope.produceNumbers() = produce<Int> {
    var x = 1
    while (true) {
        send(x++)
        delay(100L)
    }
}

fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<Int>) = launch {
    for (msg in channel) {
        println("Processor #$id received $msg")
        delay(300L) // Simulate processing time
    }
}

fun main() = runBlocking {
    val producer = produceNumbers()
    repeat(3) { id ->
        launchProcessor(id, producer)
    }
    delay(2000L)
    producer.cancel()
}
```

### Select Expression

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.selects.*

fun CoroutineScope.fizz() = produce<String> {
    while (true) {
        delay(300L)
        send("Fizz")
    }
}

fun CoroutineScope.buzz() = produce<String> {
    while (true) {
        delay(500L)
        send("Buzz!")
    }
}

suspend fun selectAorB(a: ReceiveChannel<String>, b: ReceiveChannel<String>): String =
    select<String> {
        a.onReceive { value -> "a -> '$value'" }
        b.onReceive { value -> "b -> '$value'" }
    }

fun main() = runBlocking {
    val a = fizz()
    val b = buzz()
    repeat(7) {
        println(selectAorB(a, b))
    }
    coroutineContext.cancelChildren()
}
```

## 11. Testing Coroutines {#testing}

### Setup for Testing
Add test dependency:
```kotlin
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
```

### Basic Testing

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test
import kotlin.test.assertEquals

class CoroutineTest {
    
    @Test
    fun testSuspendFunction() = runTest {
        val result = performAsyncOperation()
        assertEquals("Result", result)
    }
    
    private suspend fun performAsyncOperation(): String {
        delay(1000L)
        return "Result"
    }
    
    @Test
    fun testWithVirtualTime() = runTest {
        val startTime = currentTime
        
        launch {
            delay(1000L)
            println("Coroutine completed")
        }
        
        advanceTimeBy(1000L)
        
        val endTime = currentTime
        assertEquals(1000L, endTime - startTime)
    }
}
```

### Testing with TestDispatchers

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test

class ServiceTest {
    
    @Test
    fun testService() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val service = MyService(testDispatcher)
        
        val result = service.fetchData()
        assertEquals("Test data", result)
    }
}

class MyService(private val dispatcher: CoroutineDispatcher) {
    suspend fun fetchData(): String = withContext(dispatcher) {
        delay(1000L)
        "Test data"
    }
}
```

## 12. Best Practices {#best-practices}

### 1. Use Structured Concurrency
Always launch coroutines within a proper scope.

```kotlin
// Good
class ViewModel : CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext = Dispatchers.Main + job
    
    fun loadData() {
        launch {
            // Coroutine code
        }
    }
    
    fun onCleared() {
        job.cancel()
    }
}

// Avoid
fun loadData() {
    GlobalScope.launch { // Don't use GlobalScope
        // This coroutine might outlive its intended scope
    }
}
```

### 2. Choose the Right Dispatcher

```kotlin
// CPU-intensive work
launch(Dispatchers.Default) {
    // Heavy computation
}

// I/O operations
launch(Dispatchers.IO) {
    // Network calls, file operations
}

// UI updates (Android/JavaFX)
launch(Dispatchers.Main) {
    // Update UI elements
}
```

### 3. Handle Exceptions Properly

```kotlin
// Use try-catch for local handling
launch {
    try {
        riskyOperation()
    } catch (e: Exception) {
        handleError(e)
    }
}

// Use CoroutineExceptionHandler for global handling
val handler = CoroutineExceptionHandler { _, exception ->
    logError(exception)
}

launch(handler) {
    riskyOperation()
}
```

### 4. Prefer Flow over Channels

```kotlin
// Good - Use Flow for streams of data
fun dataStream(): Flow<Data> = flow {
    // Emit data
}

// Use Channels only for communication between coroutines
fun processData() {
    val channel = Channel<Data>()
    // Producer-consumer pattern
}
```

### 5. Make Suspend Functions Main-Safe

```kotlin
// Good - Main-safe
suspend fun fetchUser(): User = withContext(Dispatchers.IO) {
    // Network call
    api.getUser()
}

// Caller doesn't need to worry about threading
launch {
    val user = fetchUser() // Can be called from any dispatcher
}
```

### 6. Use Cancellation Cooperatively

```kotlin
suspend fun longRunningTask() {
    repeat(1000) { i ->
        ensureActive() // Check for cancellation
        // Do work
        delay(10L) // Automatically checks for cancellation
    }
}
```

### 7. Avoid Blocking Operations

```kotlin
// Bad
launch {
    Thread.sleep(1000L) // Blocks the thread
}

// Good
launch {
    delay(1000L) // Suspends the coroutine
}
```

### 8. Use SupervisorJob for Independent Children

```kotlin
val supervisor = SupervisorJob()
val scope = CoroutineScope(Dispatchers.Default + supervisor)

scope.launch {
    // If this fails, it won't cancel siblings
}

scope.launch {
    // This continues even if the above fails
}
```

### Common Pitfalls to Avoid

1. **Using runBlocking in production code**: Only use in main functions and tests
2. **Not handling cancellation**: Always make your suspend functions cancellation-aware
3. **Ignoring exceptions**: Unhandled exceptions can crash your application
4. **Using GlobalScope**: Prefer structured concurrency with proper scopes
5. **Blocking the main thread**: Use appropriate dispatchers for different types of work

This tutorial provides a comprehensive foundation for working with Kotlin coroutines. Start with the basics and gradually work your way through the advanced concepts as you become more comfortable with coroutines.