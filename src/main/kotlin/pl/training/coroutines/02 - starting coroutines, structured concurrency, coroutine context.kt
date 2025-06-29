package pl.training.coroutines/*
    Starting coroutines is possible using:
        Asynchronous coroutine builders (launch and async), which start an asynchronous coroutine
        Blocking coroutine builders (runBlocking and runTest), which start a coroutine on the current thread and block it until the coroutine is done
        Coroutine scope functions, which create a synchronous coroutine (suspend the current coroutine until the new one is completed)
    If we call await on an already completed Deferred, then there is no suspending
*/

/*
suspend fun main() {
    val job1 = GlobalScope.launch {
        delay(1000L)
        println("World!")
        // returns a Unit
    }
    val job2 =  GlobalScope.async() {
        delay(2000L)
        println("World!")
        1 // returns a result
    }
    println("Hello,")
    job1.join()
    val result = job2.await()
    // val result2 = job2.await()
    println("Result: $result")
    runBlocking {
        delay(1000L)
        println("Blocking Hello World!")
    }
}
*/

/*
    Structured Concurrency
    When a coroutine is started on a scope, it becomes a child of this scope.
    The parent-child relationship has a couple of important consequences:
        Children inherit context from their parent (but they can also overwrite it)
        A parent cannot complete until all its children have completed
        When the parent is cancelled, its child coroutines are cancelled too
        When a child completes with an exception, this exception is passed to the parent

    GlobalScope is literally an empty scope that configures nothing and builds no relationship with coroutines started
    on it, therefore it is considered bad practice to use it because it can easily break our relationships and
    it cannot be used to control coroutines started on it

    Coroutine scope functions are suspending functions that start a synchronous coroutine - a new coroutine but suspend the current
    one until the new one is completed. They behave a lot like runBlocking, but instead of blocking the thread they suspend the coroutine

    Coroutine scope functions are suspending functions that do not require a scope, while coroutine builders are
    regular functions that do require a scope

    In practice, coroutine scope functions are used to create a scope for asynchronous coroutines

    coroutineScope is the most basic coroutine scope function
    withContext behaves just like coroutineScope but can change the context of the coroutine
    supervisorScope behaves just like coroutineScope but ignores its children’s exceptions
    withTimeout behaves just like coroutineScope but cancels the coroutine after a timeout
*/

/*
suspend fun main() {
    coroutineScope {
        delay(1000L)
        println("World!")
    }
    coroutineScope {
        delay(1000L)
        println("World!")
    }
    val result = coroutineScope {
        delay(1000L)
        println("World!")
        1
    }
    println("Hello,")
    println("Result: $result")
}
*/

/*
    CoroutineContext is an interface that represents an element or a collection of elements and implements the Composite design pattern.
    It is an indexed set of element instances like Job, CoroutineName, CouroutineDispatcher, etc.

    val ctx: CoroutineContext = CoroutineName("A name")
    val coroutineName: CoroutineName? = ctx[CoroutineName]
    // or ctx.get(CoroutineName)
    println(coroutineName?.name) // A name
    val job: Job? = ctx[Job] // or ctx.get(Job)

    To find a CoroutineName, we use just CoroutineName. This is not a type or a class, it is a companion object
    What makes CoroutineContext truly useful is its ability to merge two contexts together

    What makes CoroutineContext truly useful is its ability to merge two contexts
    together.When two elements with different keys are added, the resulting context responds to both keys.
    Just like in a map, the new element replaces the previous one when another element with the same key is added.

    val ctx1: CoroutineContext = CoroutineName("Name1")
    println(ctx1[CoroutineName]?.name) // Name1
    println(ctx1[Job]?.isActive) // null
    val ctx2: CoroutineContext = Job()
    println(ctx2[CoroutineName]?.name) // null
    println(ctx2[Job]?.isActive) // true, because "Active"
    // is the default state of a job created this way
    val ctx3 = ctx1 + ctx2
    println(ctx3[CoroutineName]?.name) // Name1
    println(ctx3[Job]?.isActive) // true

    Since CoroutineContext is like a collection, we also have an empty context. Such
    a context by itself returns no elements;

    val empty: CoroutineContext = EmptyCoroutineContext
    println(empty[CoroutineName]) // null
    println(empty[Job]) // null
    val ctxName = empty + CoroutineName("Name1") + empty
    println(ctxName[CoroutineName]) // CoroutineName(Name1)

    Elements can also be removed from a context by their key using the minusKey unction.

    val ctx = CoroutineName("Name1") + Job()
    println(ctx[CoroutineName]?.name) // Name1
    println(ctx[Job]?.isActive) // true
    val ctx2 = ctx.minusKey(CoroutineName)
    println(ctx2[CoroutineName]?.name) // null
    println(ctx2[Job]?.isActive) // true
    val ctx3 = (ctx + CoroutineName("Name2")).minusKey(CoroutineName)
    println(ctx3[CoroutineName]?.name) // null
    println(ctx3[Job]?.isActive) // true

    If we need to do something for each element in a context, we can use the fold
    method, which is similar to fold for other collections.

    val ctx = CoroutineName("Name1") + Job()
    ctx.fold("") { acc, element -> "$acc$element " }.also(::println)
    // CoroutineName(Name1) JobImpl{Active}@dbab622e
    val empty = emptyList<CoroutineContext>()
    ctx.fold(empty) { acc, element -> acc + element }
    .joinToString()
    .also(::println)

    So, CoroutineContext is just a way to hold and pass data. By default, the parent
    passes its context to the child, which is one of the effects of the parent-child
    relationship. We say that a child inherits its context from its parent.
    Each child might have a specific context defined in the argument. This context
    overrides the one from the parent.

    There is one special context called Job, which keeps the state of the current
    coroutine. It is the only context that is not inherited because each coroutine must
    have its own job.

    If you want to modify a context for a suspend function, you can use withContext,
    which behaves like coroutineScope but changes the context for the coroutine
    it creates. It is a suspending function, so it can be used in other suspending
    functions.

    suspend fun readSave() = withContext(Dispatchers.IO) {
        val file = File("save.txt")
        file.readText()
    }

    withContext is often used to define a context that should be specific to a given
    operation. Remember that context is propagated automatically, so whatever is set
    in the parent coroutine will be available in the child coroutine.

    Kotlin Coroutines are not associated with concrete threads. A coroutine might
    start on one thread, get suspended, and then be resumed on another thread.
    This fact generates a problem for Java tools that associate data with threads,
    like ThreadLocal in Java stdlib, or SecurityContext in Spring Boot. If you use
    such tools, Kotlin Coroutines offer a special kind of context. All contexts that
    extend ThreadContextElement are installed into the thread context every time the
    coroutine with this element in the context is resumed on a thread.
*/
