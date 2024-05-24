package eliminationStack

import Node
import Stack
import kotlinx.atomicfu.*
import java.util.concurrent.TimeoutException

class EliminationTreiberStack<T> : Stack<T>{
    companion object {
        const val CAPACITY = 4
    }
    private val head = atomic<Node<T>?>(null)
    private val eliminationArray = EliminationArray<T>(CAPACITY)

    override fun push(value: T) {
        while (true) {
            val curHead = head.value
            val newHead = Node(value, curHead)
            if (head.compareAndSet(curHead, newHead)) {
                return
            }

           try {
               eliminationArray.visit(value) ?: return
           }
           catch (ex: TimeoutException) {
               continue
           }
        }
    }

    override fun pop(): T? {
        while (true) {
            val curHead = head.value
            if (head.compareAndSet(curHead, curHead?.next)) {
                return curHead?.value
            }

            try {
                val otherValue = eliminationArray.visit(null)
                if (otherValue != null) {
                    return otherValue
                }
            }
            catch (ex: TimeoutException) {
                continue
            }
        }
    }

    override fun top(): T? {
        return head.value?.value
    }
}