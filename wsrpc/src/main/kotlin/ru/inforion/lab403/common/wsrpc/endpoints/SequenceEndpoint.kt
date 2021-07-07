package ru.inforion.lab403.common.wsrpc.endpoints

import ru.inforion.lab403.common.concurrent.locks.PhonyLock
import ru.inforion.lab403.common.wsrpc.interfaces.WebSocketRpcEndpoint
import ru.inforion.lab403.common.wsrpc.annotations.WebSocketRpcMethod
import ru.inforion.lab403.common.wsrpc.interfaces.Callable
import java.util.concurrent.locks.Lock
import kotlin.concurrent.withLock

class SequenceEndpoint<T>(
    sequence: Sequence<T>,
    private val lock: Lock = PhonyLock,
    override val name: String = "Sequence"
) : WebSocketRpcEndpoint {
    private var state: Sequence<*> = sequence

    @WebSocketRpcMethod
    fun find(predicate: Callable<Boolean>) {
        val result = lock.withLock { state.find { predicate.call(it) } }
        state = sequenceOf(result)
    }

    @WebSocketRpcMethod
    fun filter(predicate: Callable<Boolean>) {
        state = state.filter { predicate.call(it) }
    }

    @WebSocketRpcMethod
    fun <R> map(transform: Callable<R>) {
        state = state.map { transform.call(it) }
    }

    @WebSocketRpcMethod
    fun <R> mapIndexed(transform: Callable<R>) {
        state = state.mapIndexed { index, value -> transform.call(index, value) }
    }

    @WebSocketRpcMethod
    fun <R : Comparable<R>> sortedBy(selector: Callable<R>) {
        state = state.sortedBy { selector.call(it) }
    }

    @WebSocketRpcMethod
    fun <R : Comparable<R>> sorted() {
        @Suppress("UNCHECKED_CAST")
        val sequence = state as Sequence<R>
        state = sequence.sorted()
    }

    @WebSocketRpcMethod
    fun <R> groupBy(selector: Callable<R>) {
        state = lock.withLock { state.groupBy { selector.call(it) } }.asSequence()
    }

    @WebSocketRpcMethod
    fun <K, V, R> mapValues(transform: Callable<R>) {
        state = lock.withLock {
            @Suppress("UNCHECKED_CAST")
            val sequence = state as Sequence<Map.Entry<K, V>>
            sequence.associate {
                val value = transform.call(it.key, it.value)
                it.key to value
            }
        }.asSequence()
    }

    @WebSocketRpcMethod
    fun associate(transform: Callable<Iterable<*>>) {
        state = lock.withLock {
            state.associate {
                val iterator = transform.call(it).iterator()
                iterator.next() to iterator.next()
            }.asSequence()
        }
    }

    @WebSocketRpcMethod
    fun take(n: Int) {
        state = state.take(n)
    }

    @WebSocketRpcMethod
    fun <R> flatMap(transform: Callable<Iterable<R>>) {
        state = state.flatMap { transform.call(it) }
    }

    @WebSocketRpcMethod
    fun <R> distinctBy(transform: Callable<R>) {
        state = state.distinctBy { transform.call(it) }
    }

    @WebSocketRpcMethod
    fun distinct() {
        state = state.distinct()
    }

    @WebSocketRpcMethod(close = true)
    fun first() = lock.withLock { state.first() }

    @WebSocketRpcMethod(close = true)
    fun last() = lock.withLock { state.last() }

    @WebSocketRpcMethod(close = true)
    fun count() = lock.withLock { state.count() }

    @WebSocketRpcMethod(close = true)
    fun collect() = lock.withLock { state.toList() }

    @WebSocketRpcMethod(close = true)
    fun <K, V> collectAsMap() = lock.withLock {
        @Suppress("UNCHECKED_CAST")
        val sequence = state as Sequence<Map.Entry<K, V>>
        sequence.associate { it.key to it.value }
    }
}