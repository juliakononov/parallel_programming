import eliminationStack.EliminationTreiberStack
import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class PerformanceTest {
    private fun dataFun(stack: Stack<Int>, cond: Int) {
        when (cond) {
            0 -> stack.push(Random.nextInt(100))
            1 -> stack.pop()
            else -> stack.top()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    fun runTest(stack: Stack<Int>, threads: Int, iterations: Int, special: Boolean): Long {
        val jobs = mutableListOf<Job>()
        return measureTimeMillis {
            runBlocking {
                repeat(threads) { i ->
                    jobs.add(launch(newSingleThreadContext(i.toString())) {
                        repeat(iterations) {
                            if (special) {
                                dataFun(stack, (i%2))
                            }
                            else {
                                dataFun(stack, ((i + Random.nextInt(1_000))%3))
                            }
                        }
                    })
                }
                jobs.joinAll()
            }
        }
    }

    @Test
    fun performanceTest() {
        val iterations = 1_000_000
        println("N, eliminationStackTime, stackTime")
        for (threads in mutableListOf(1, 2, 4, 8, 12, 16)) {
            var eliminationStackTime = 0L
            var stackTime = 0L

            repeat(20) {
                val stack = TreiberStack<Int>()
                val eliminationStack = EliminationTreiberStack<Int>()
                eliminationStackTime += runTest(eliminationStack, threads, iterations, false)
                stackTime += runTest(stack, threads, iterations, false)
            }
            eliminationStackTime /= 20
            stackTime /= 20
            println("$threads, $eliminationStackTime, $stackTime")
        }
    }
}

//N, eliminationStackTime, stackTime
//1, 23, 28
//2, 55, 116
//4, 146, 491
//8, 565, 1204
//12, 1012, 1883
//16, 1271, 2747


//N, eliminationStackTime, stackTime
//1, 23, 21
//2, 59, 110
//4, 139, 346
//8, 588, 778
//12, 900, 1251
//16, 1048, 1737
