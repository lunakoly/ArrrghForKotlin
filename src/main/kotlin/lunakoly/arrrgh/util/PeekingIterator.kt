package lunakoly.arrrgh.util

class PeekingIterator<T>(val original: Iterator<T>) : Iterator<T> {
    private sealed class Optional<out T> {
        data class Some<T>(val value: T) : Optional<T>()
        data object None : Optional<Nothing>()
    }

    private var cached: Optional<T> = Optional.None

    override fun hasNext() = if (cached == Optional.None) original.hasNext() else true

    override fun next() = when (val it = cached) {
        is Optional.Some -> it.value.also { cached = Optional.None }
        else -> original.next()
    }

    fun peek(): T {
        val it = cached

        return when {
            it is Optional.Some -> it.value
            original.hasNext() -> original.next().also { cached = Optional.Some(it) }
            else -> error("No elements left")
        }
    }
}

fun <T> Iterator<T>.withPeek(): PeekingIterator<T> = PeekingIterator(this)
