package pl.training.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

/*
    Hot data streams are eager, produce elements independently of their consumption, and store the elements.
    Cold data streams are lazy, perform their operations on-demand, and store nothing.
    We can observe these differences when we use lists (hot) and sequences (cold).
    Builders and operations on hot data streams start immediately. On cold data
    streams, they are not started until the elements are needed
 */

/*
fun main() {
    val l = buildList {
        repeat(3) {
            add("User$it")
            println("L: Added User")
        }
    }
    val l2 = l.map {
        println("L: Processing")
        "Processed $it"
    }
    val s = sequence {
        repeat(3) {
            yield("User$it")
            println("S: Added User")
        }
    }
    val s2 = s.map {
        println("S: Processing")
        "Processed $it"
    }
}
*/

/*
    As a result, cold data streams (like Sequence, Stream or Flow):
        can be infinite;
        do a minimal number of operations;
        use less memory (no need to allocate all the intermediate collections).

    Sequence processing does fewer operations because it processes elements lazily.
    The way it works is very simple. Each intermediate operation (like map or filter)
    just decorates the previous sequence with a new operation. The terminal operation does all the work.
    This means that a list is a collection of elements, but a sequence is just a definition
    of how these elements should be calculated.

    Hot data streams:
        are always ready to be used (each operation can be a terminal operation);
        do not need to recalculate the result when used multiple times.
*/

/*
fun m(i: Int): Int {
    print("m$i ")
    return i * i
}
fun main() {
    val l = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        .map { m(it) } // m1 m2 m3 m4 m5 m6 m7 m8 m9 m10
    println(l) // [1, 4, 9, 16, 25, 36, 49, 64, 81, 100]
    println(l.find { it > 10 }) // 16
    println(l.find { it > 10 }) // 16
    println(l.find { it > 10 }) // 16
    val s = sequenceOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        .map { m(it) }
    println(s.toList())
// [1, 4, 9, 16, 25, 36, 49, 64, 81, 100]
    println(s.find { it > 10 }) // m1 m2 m3 m4 16
    println(s.find { it > 10 }) // m1 m2 m3 m4 16
    println(s.find { it > 10 }) // m1 m2 m3 m4 16
}
*/

/*
    The most typical way to create a flow is by using a
    builder, which is similar to the produce function. It is called flow.
 */

/*
val channel = produce {
    while (true) {
        val x = computeNextValue()
        send(x)
    }
}
val flow = flow {
    while (true) {
        val x = computeNextValue()
        emit(x)
    }
}
*/

/*
    These builders are conceptually equivalent, but since the behavior of channel and
    flow is very different, there are also important differences between these two
    functions. Channels are hot, so they immediately start calculating the values.
    This calculation starts in a separate coroutine.
    This is why produce needs to be a coroutine builder that is defined as an extension
    function on CoroutineScope. The calculation starts immediately, but since the
    default buffer size is 0 (rendezvous) it will soon be suspended until the receiver
    is ready in the example below. Note that there is a difference between stopping
    production when there is no receiver and producing on-demand. Channels, as hot
    data streams, produce elements independently of their consumption and then
    keep them. They do not care how many receivers there are. Since each element
    can be received only once, after the first receiver consumes all the elements, the
    second one will find a channel that is empty and closed already. This is why it will receive no elements at all.
 */

/*
private fun CoroutineScope.makeChannel() = produce {
    println("Channel started")
    for (i in 1..3) {
        delay(1000)
        send(i)
    }
}
suspend fun main() = coroutineScope {
    val channel = makeChannel()
    delay(1000)
    println("Calling channel...")
    for (value in channel) {
        println(value)
    }
    println("Consuming again...")
    for (value in channel) {
        println(value)
    }
}
*/

/*
    The same processing using Flow is very different. Since it is a cold data source,
    the production happens on demand. This means that flow is not a builder and
    does no processing. It is only a definition of how elements should be produced
    that will be used when a terminal operation (like collect) is used. This is why
    the flow builder does not need a CoroutineScope. It will run in the scope from
    the terminal operation that executed it (it takes the scope from the suspending function’s
    continuation, just like coroutineScope and other coroutine scope
    functions). Each terminal operation on a flow starts processing from scratch.
    Compare the examples above and below because they show the key differences
    between Channel and Flow.
 */

/*
private fun makeFlow() = flow {
    println("Flow started")
    for (i in 1..3) {
        delay(1000)
        emit(i)
    }
}
suspend fun main() = coroutineScope {
    val flow = makeFlow()
    delay(1000)
    println("Calling flow...")
    flow.collect { value -> println(value) }
    println("Consuming again...")
    flow.collect { value -> println(value) }
}
*/

/*
    Hot data sources are eager. They produce elements as soon as possible and store them. They create elements independently of their consumption.
    These are collections (List, Set) and Channel.

    Cold data sources are lazy. They process elements on-demand on the terminal operation. All intermediate functions just define what should be
    done (most often using the Decorator pattern). They generally do not store elements and create them on demand. They do the minimal number of
    operations and can be infinite. Their creation and processing of elements is typically the same process as consumption. These elements are Sequence,
    Java Stream, Flow and RxJava streams (Observable, Single, etc).
*/

