package eliminationStack;

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicStampedReference

class Exchanger<T> {
    companion object {
        const val EMPTY = 0
        const val WAITING = 1
        const val BUSY = 2
    }

    private val slot = AtomicStampedReference<T?>(null, EMPTY)

    @Throws(TimeoutException::class)
    fun exchange(myItem: T, timeout: Long, unit: TimeUnit): T? {
        val nanos = unit.toNanos(timeout)
        val timeBound = System.nanoTime() + nanos
        val stampHolder = intArrayOf(EMPTY)

        while (true) {
            if (System.nanoTime() > timeBound)
                throw TimeoutException()

            val yrItem = slot.get(stampHolder)
            val stamp = stampHolder[0]

            when (stamp) {
                EMPTY -> {
                    if (slot.compareAndSet(yrItem, myItem, EMPTY, WAITING)) {
                        while (System.nanoTime() < timeBound) {
                            val newtItem = slot.get(stampHolder)
                            if (stampHolder[0] == BUSY) {
                                slot.set(null, EMPTY)
                                return newtItem
                            }
                        }
                        if (slot.compareAndSet(myItem, null, WAITING, EMPTY)) {
                            throw TimeoutException()
                        } else {
                            val newtItem = slot.get(stampHolder)
                            slot.set(null, EMPTY)
                            return newtItem
                        }
                    }
                }
                WAITING -> {
                    if (slot.compareAndSet(yrItem, myItem, WAITING, BUSY)) {
                        return yrItem
                    }
                }
                BUSY -> {
                    // Do nothing, just loop again
                }
                else -> throw IllegalStateException("Impossible state")
            }
        }
    }
}