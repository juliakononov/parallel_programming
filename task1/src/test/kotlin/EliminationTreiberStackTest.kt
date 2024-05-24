import eliminationStack.EliminationTreiberStack
import org.jetbrains.kotlinx.lincheck.LoggingLevel
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.junit.jupiter.api.Test

class EliminationTreiberStackTest {
    private val stack = EliminationTreiberStack<Int>()

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
            .threads(3)
            .actorsPerThread(3)
            .logLevel(LoggingLevel.INFO)
            .check(this::class)
}