import org.jetbrains.kotlinx.lincheck.annotations.*
import org.jetbrains.kotlinx.lincheck.*
import org.jetbrains.kotlinx.lincheck.strategy.stress.*
import org.junit.jupiter.api.Test

class TreiberStackTest {
    private val stack = TreiberStack<Int>()

    @Operation
    fun push(value: Int) = stack.push(value)

    @Operation
    fun pop(): Int? = stack.pop()

    @Operation
    fun top(): Int? = stack.top()

    @Test
    fun stressTest() = StressOptions()
            .iterations(40)
            .invocationsPerIteration(200)
            .threads(4)
            .actorsPerThread(3)
            .logLevel(LoggingLevel.INFO)
            .check(this::class)
}

