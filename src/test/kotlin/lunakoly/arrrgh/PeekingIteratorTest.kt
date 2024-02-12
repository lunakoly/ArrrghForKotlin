package lunakoly.arrrgh

import kotlin.test.Test
import kotlin.test.assertEquals

class PeekingIteratorTest {
    private val lists = listOf(
        listOf(),
        listOf("a"),
        listOf("a", "b", "c"),
    )

    private fun <T> Iterator<T>.collect(): List<T> {
        val result = mutableListOf<T>()

        while (hasNext()) {
            result += next()
        }

        return result
    }

    @Test
    fun testUsualIteratorBehavior() = lists.forEach{ list ->
        assertEquals(list, list.iterator().collect())
    }

    private fun <T> PeekingIterator<T>.collectWithPeek(): List<T> {
        val result = mutableListOf<T>()

        while (hasNext()) {
            peek()
            result += next()
        }

        return result
    }

    @Test
    fun testIteratorBehaviorWithPeek() = lists.forEach{ list ->
        assertEquals(list, list.iterator().withPeek().collectWithPeek())
    }
}