package pl.training.commons.aop

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class LinkedHashMapCache<K, V>(private val capacity: Int) : Cache<K, V> {

    private val entries = object : LinkedHashMap<K, V>(capacity, 1f, true) {

        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>): Boolean {
            return size > capacity
        }

    }

    private val lock = ReentrantReadWriteLock()

    override fun put(key: K, value: V) {
        lock.write { entries[key] = value }
    }

    override fun get(key: K): V? {
        return lock.read { entries[key] }
    }

}