import kotlinx.atomicfu.*


class TreiberStack<T> : Stack<T> {
    private val head = atomic<Node<T>?>(null)

    override fun push(value: T) {
        while (true) {
            val curHead = head.value
            val newHead = Node(value, curHead)
            if (head.compareAndSet(curHead, newHead)){
                return
            }
        }
    }

    override fun pop(): T? {
        while (true) {
            val curHead = head.value
            if (head.compareAndSet(curHead, curHead?.next)){
                return curHead?.value
            }
        }
    }

    override fun top(): T? {
        return head.value?.value
    }
}