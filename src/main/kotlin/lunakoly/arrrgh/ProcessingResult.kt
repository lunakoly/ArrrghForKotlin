package lunakoly.arrrgh

sealed class ProcessingResult<out T> {
    data class Value<T>(val value: T) : ProcessingResult<T>()
    data class Error(val messages: List<String>) : ProcessingResult<Nothing>() {
        constructor(message: String) : this(listOf(message))
    }
}

inline fun <T, R : T> ProcessingResult<T>.valueOrOnError(block: (ProcessingResult.Error) -> R): T {
    return when (this) {
        is ProcessingResult.Value<T> -> value
        is ProcessingResult.Error -> block(this)
    }
}

inline fun <T> ProcessingResult<T>.selfOrOnError(block: (ProcessingResult.Error) -> Nothing): ProcessingResult.Value<T> {
    return when (this) {
        is ProcessingResult.Value<T> -> this
        is ProcessingResult.Error -> block(this)
    }
}

fun <T> ProcessingResult<T>.selfIfValueOrNull(): ProcessingResult.Value<T>? {
    return when (this) {
        is ProcessingResult.Value<T> -> this
        is ProcessingResult.Error -> null
    }
}

fun <T> ProcessingResult<T>.selfIfErrorOrNull(): ProcessingResult.Error? {
    return when (this) {
        is ProcessingResult.Value<T> -> null
        is ProcessingResult.Error -> this
    }
}

infix fun ProcessingResult.Error?.combineWith(other: ProcessingResult.Error?): ProcessingResult.Error? {
    val messages = this?.messages.orEmpty() + other?.messages.orEmpty()
    return if (messages.isNotEmpty()) ProcessingResult.Error(messages) else null
}

inline fun ProcessingResult.Error?.transform(transform: (String) -> String): ProcessingResult.Error? {
    val messages = this?.messages?.map(transform).orEmpty()
    return if (messages.isNotEmpty()) ProcessingResult.Error(messages) else null
}

fun ProcessingResult.Error?.withPrefixes(prefix: String) = transform { "$prefix$it" }
