interface Stack<T> {
    fun push(value: T)

    fun pop() : T?

    fun top() : T?
}