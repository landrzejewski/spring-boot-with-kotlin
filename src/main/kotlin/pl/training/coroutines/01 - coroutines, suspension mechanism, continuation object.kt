package pl.training.coroutines/*
    Coroutines are components that can be suspended and resumed. Unlike the use of threads, there is no blocking here.
    When a thread is blocked, it still consumes resources and needs to be managed by the operating system.
    When a coroutine is suspended, the only thing that remains is an object that keeps references to local variables
    and the place where this coroutine was suspended. Coroutines are lightweight abstractions that run on top of threads,
    managed by the coroutine library.

    Suspending functions (functions marked with suspend modifier) are functions that can suspend a coroutine. and must be called
    by other suspending functions or by coroutine builders that start coroutines. Suspending functions are not coroutines,
    but they require coroutines.
 */

/*
suspend fun printMessage() {
    // Suspends the coroutine for 1 second
    delay(1000)
    println("Message")
}

// Suspending main is started by Kotlin in a coroutine
suspend fun main() {
    println("Before")
    printMessage() // Called on the same coroutine (serial execution)
    suspendCoroutine<Unit> { continuation -> // The lambda is called before suspension
        println("Before continuation")
        // Old API
        continuation.resume(Unit) // Unit represents a result type
        // continuation.resumeWithException(RuntimeException())

        // New API
        // continuation.resumeWith(Result.success(Unit))
        // continuation.resumeWith(Result.failure(RuntimeException()))
    }
    try {
        val result = suspendCancellableCoroutine<Int> { continuation ->
            // Some work
            continuation.resume(10)
        }
    } catch (e: RuntimeException) {
        println("Exception")
    }
    println("After")
}
*/

/*
    The continuation is an object that stores the state of the coroutine. It must also
    store the local variables and the place where the coroutine was suspended.
 */

/*
suspend fun printMessage() {
    val message = "Message"
    suspendCancellableCoroutine<Unit> { continuation ->
        // It is possible to debug and see what is stored in the continuation object (continuation -> completion)
        continuation.resume(Unit)
    }
    println(message)
}

suspend fun main() {
    val list = listOf(1, 2, 3, 4, 5)
    val text = "Coroutines"
    println(text)
    delay(1000)
    printMessage()
    println(list)
}
*/

/*
    Naive coroutine suspension can be implemented by using an additional thread, but it is very inefficient
    (unnecessary thread creation, resource consumption). A better approach would be schedule a task using
    ScheduledExecutorService (one thread for all coroutines).
*/

/*
suspend fun main() {
    println("Before")
    suspendCancellableCoroutine<Unit> { continuation ->
        thread {
            println("Suspended")
            sleep(1000)
            continuation.resume(Unit)
            println("Resumed")
        }
    }
    println("After")
}
*/

/*
private val executor = Executors.newSingleThreadScheduledExecutor {
    Thread(it, "scheduler").apply { isDaemon = true }
}

// Very similar to old implementation of delay function
suspend fun delay(timeMillis: Long) = suspendCancellableCoroutine { continuation ->
    executor.schedule({ continuation.resume(Unit) }, timeMillis, TimeUnit.MILLISECONDS)
}

suspend fun main() {
    println("Before")
    delay(1000)
    println("After")
}
*/

/*
    Suspending functions are like state machines, with a possible state at the beginning of the function and after each suspending function call.

    Both the number identifying the state and the local data are kept in the continuation object passed as a last argument of a function.

    Continuation of a function decorates a continuation of its caller function; as a result, all these continuations
    represent a call stack that is used when we resume or a resumed function completes.

    So function suspend fun myFunction(): User? becomes fun myFunction(continuation: Continuation<*>): Any? // can return COROUTINE_SUSPENDED or User?

    The next thing is that this function needs its own continuation in order to remember its state.
    Continuations serve as a call stack. Each continuation keeps the state where we suspended (as a label)
    the function’s local variables and parameters (as fields), and the reference to the continuation of the
    function that called this function. One continuation references another, which references another, etc.
    As a result, our continuation is like a huge onion: it keeps everything that is generally kept on the call stack.

    suspend fun myFunction() {
        println("Before")
        delay(1000) // suspending
        println("After")
    }

    A simplified picture of how myFunction looks under the hood. To identify the current state, we use a field called label.
    At the start, it is 0, therefore the function will start from the beginning. However, it is set to the next state
    before each suspension point so that we start from just after the suspension point after a resume.
    When delay is suspended, it returns COROUTINE_SUSPENDED, then myFunction returns COROUTINE_SUSPENDED;
    the same is done by the function that called it, and the function that called this function, and all
    other functions until the top of the call stack. This is how a suspension ends all these functions and leaves the
    thread available for other runnables (including coroutines) to be used (not returning COROUTINE_SUSPENDED would
    cause the execution of next state).

    fun myFunction(continuation: Continuation<Unit>): Any {
        val continuation = continuation as? MyFunctionContinuation ?: MyFunctionContinuation(continuation) // We only need to wrap continuation during first call
        if (continuation.label == 0) {
            println("Before")
            continuation.label = 1
            if (delay(1000, continuation) == COROUTINE_SUSPENDED) {
                return COROUTINE_SUSPENDED
            }
        }
        if (continuation.label == 1) {
            println("After")
            return Unit
        }
        error("Impossible")
    }

    class MyFunctionContinuation(val completion: Continuation<Unit>) : Continuation<Unit> {
        override val context: CoroutineContext
            get() = completion.context
        var label = 0
        var result: Result<Any>? = null
        override fun resumeWith(result: Result<Unit>) {
            this.result = result
            val res = try {
                val r = myFunction(this)
                if (r == COROUTINE_SUSPENDED) return
                Result.success(r as Unit)
            } catch (e: Throwable) {
                Result.failure(e)
            }
            completion.resumeWith(res)
        }
    }

    If function has a state (local variables or parameters) that needs to be restored after suspension.
    Here counter is needed in two states (for a label equal to 0 and 1), so it needs to be kept in the continuation.
    I twill be stored right before suspension. Restoring these kinds of properties happens at the beginning of the function.

    suspend fun myFunction() {
        println("Before")
        var counter = 0
        delay(1000)
        counter++
        println("Counter: $counter")
        println("After")
    }

    fun myFunction(continuation: Continuation<Unit>): Any {
        val continuation = continuation as? MyFunctionContinuation ?: MyFunctionContinuation(continuation)
        var counter = continuation.counter
        if (continuation.label == 0) {
            println("Before")
            counter = 0
            continuation.counter = counter
            continuation.label = 1
            if (delay(1000, continuation) == COROUTINE_SUSPENDED) {
                return COROUTINE_SUSPENDED
            }
        }
        if (continuation.label == 1) {
            counter = (counter as Int) + 1
            println("Counter: $counter")
            println("After")
            return Unit
        }
        error("Impossible")
    }

    class MyFunctionContinuation(val completion: Continuation<Unit>) : Continuation<Unit> {
        override val context: CoroutineContext
            get() = completion.context
        var result: Result<Unit>? = null
        var label = 0
        var counter = 0
        override fun resumeWith(result: Result<Unit>) {
            this.result = result
            val res = try {
                val r = myFunction(this)
                if (r == COROUTINE_SUSPENDED) return
                    Result.success(r as Unit)
            } catch (e: Throwable) {
                Result.failure(e)
            }
            completion.resumeWith(res)
        }
    }

    In case function resumed with value

    suspend fun printUser(token: String) {
        println("Before")
        val userId = getUserId(token) // suspending
        println("Got userId: $userId")
        val userName = getUserName(userId, token) // suspending
        println(User(userId, userName))
        println("After")
    }

    fun printUser(token: String, continuation: Continuation<*>): Any {
        val continuation = continuation as? PrintUserContinuation ?: PrintUserContinuation(
            continuation as Continuation<Unit>,
            token
        )
        var result: Result<Any>? = continuation.result
        var userId: String? = continuation.userId

        val userName: String
        if (continuation.label == 0) {
            println("Before")
            continuation.label = 1
            val res = getUserId(token, continuation)
            if (res == COROUTINE_SUSPENDED) {
                return COROUTINE_SUSPENDED
            }
            result = Result.success(res)
        }
        if (continuation.label == 1) {
            userId = result!!.getOrThrow() as String
            println("Got userId: $userId")
            continuation.label = 2
            continuation.userId = userId
            val res = getUserName(userId, continuation)
            if (res == COROUTINE_SUSPENDED) {
                return COROUTINE_SUSPENDED
            }
            result = Result.success(res)
        }
        if (continuation.label == 2) {
            userName = result!!.getOrThrow() as String
            println(User(userId as String, userName))
            println("After")
            return Unit
        }
        error("Impossible")
    }

    class PrintUserContinuation(val completion: Continuation<Unit>, val token: String) : Continuation<String> {
        override val context: CoroutineContext
            get() = completion.context
        var label = 0
        var result: Result<Any>? = null
        var userId: String? = null
        override fun resumeWith(result: Result<String>) {
            this.result = result
            val res = try {
                val r = printUser(token, this)
                if (r == COROUTINE_SUSPENDED) return
                Result.success(r as Unit)
            } catch (e: Throwable) {
                Result.failure(e)
            }
            completion.resumeWith(res)
        }
    }

    Suspending functions are like state machines, with a possible state at the beginning of the function and after
    each suspending function call.
    Both the label identifying the state and the local data are kept in the continuation object.
    Continuation of one function decorates a continuation of its caller function; as a result, all these continuations
    represent a call stack that is used where resume or a resumed function completes.
 */
