package lunakoly.arrrgh

import kotlin.reflect.KProperty

interface Processor<R> {
    fun processNext(args: Iterator<String>): ProcessingResult.Error?

    val result: ProcessingResult<R>

    operator fun getValue(self: Any?, property: KProperty<*>): R {
        return when (val it = result) {
            is ProcessingResult.Value<R> -> it.value
            // Properties' delegates must exist regardless the processing result.
            // Thus, they must throw
            is ProcessingResult.Error -> error("Should not be here: ${it.messages}")
        }
    }
}

fun expectNext(args: Iterator<String>): ProcessingResult<String> = when {
    args.hasNext() -> ProcessingResult.Value(args.next())
    else -> ProcessingResult.Error("Expecting an argument")
}

class ListProcessor : Processor<List<String>> {
    private val value = mutableListOf<String>()
    override val result = ProcessingResult.Value(value)

    override fun processNext(args: Iterator<String>): ProcessingResult.Error? {
        value += expectNext(args).valueOrOnError { return it }
        return null
    }
}

interface SingleValueProcessor<T> : Processor<T> {
    /**
     * By convention, `Value` if a value has been supplied explicitly.
     */
    var explicitlySetValue: ProcessingResult<T>

    override val result: ProcessingResult<T> get() = explicitlySetValue

    fun reportDuplication(previous: ProcessingResult.Value<T>, next: ProcessingResult.Value<T>): ProcessingResult.Error =
        ProcessingResult.Error("Duplicate argument `${next.value}`, the previous one was `${previous.value}`")

    override fun processNext(args: Iterator<String>): ProcessingResult.Error? {
        val next = processNextValue(args).selfOrOnError { return it }

        when (val that = explicitlySetValue) {
            is ProcessingResult.Error -> explicitlySetValue = next
            is ProcessingResult.Value -> return reportDuplication(that, next)
        }

        return null
    }

    fun processNextValue(args: Iterator<String>): ProcessingResult<T>
}

interface OptionalSingleValueProcessor<R> : SingleValueProcessor<R?> {
    override val result: ProcessingResult<R?> get() = explicitlySetValue.selfIfValueOrNull() ?: ProcessingResult.Value(null)
}

interface RequiredSingleValueProcessor<R> : SingleValueProcessor<R> {
    val delegateOptionalProcessor: SingleValueProcessor<R?>

    override val result: ProcessingResult<R> get() = explicitlySetValue.selfIfValueOrNull() ?: initialValue

    val defaultValue: R?

    private val initialValue get() = when (val default = defaultValue) {
        null -> explicitlySetValue
        else -> ProcessingResult.Value(default)
    }

    override fun processNextValue(args: Iterator<String>): ProcessingResult<R> {
        val optional = delegateOptionalProcessor.processNextValue(args).valueOrOnError { return it }

        return when (optional) {
            null -> ProcessingResult.Error("Expecting a non-null value")
            else -> ProcessingResult.Value(optional)
        }
    }
}

fun <T> OptionalSingleValueProcessor<T?>.toRequired(defaultValue: T? = null): RequiredSingleValueProcessor<T> {
    return object : RequiredSingleValueProcessor<T> {
        override val defaultValue: T? = defaultValue
        override var explicitlySetValue: ProcessingResult<T> = ProcessingResult.NO_VALUE_ERROR
        override val delegateOptionalProcessor = this@toRequired
    }
}

class BooleanProcessor : SingleValueProcessor<Boolean> {
    override var explicitlySetValue: ProcessingResult<Boolean> = ProcessingResult.NO_VALUE_ERROR
    override val result: ProcessingResult<Boolean> get() = explicitlySetValue.selfIfValueOrNull() ?: ProcessingResult.Value(false)

    override fun processNextValue(args: Iterator<String>) = ProcessingResult.Value(true)

    override fun reportDuplication(previous: ProcessingResult.Value<Boolean>, next: ProcessingResult.Value<Boolean>) =
        ProcessingResult.Error("The flag was passed twice")
}

class OptionalStringProcessor : OptionalSingleValueProcessor<String?> {
    override var explicitlySetValue: ProcessingResult<String?> = ProcessingResult.NO_VALUE_ERROR

    override fun processNextValue(args: Iterator<String>) = expectNext(args)
}

class OptionalEnumProcessor<E : Enum<E>>(
    private val namesToValues: Map<String, E>,
) : OptionalSingleValueProcessor<E?> {
    override var explicitlySetValue: ProcessingResult<E?> = ProcessingResult.NO_VALUE_ERROR

    private val supportedValues: String get() = namesToValues.keys.joinToString(", ") { "`$it`" }

    override fun processNextValue(args: Iterator<String>): ProcessingResult<E> {
        val string = expectNext(args).valueOrOnError { return it }

        return namesToValues[string]?.let { ProcessingResult.Value(it) }
            ?: ProcessingResult.Error("`$string` is not a supported value. The supported ones are: $supportedValues")
    }
}

class OptionalDoubleProcessor : OptionalSingleValueProcessor<Double?> {
    override var explicitlySetValue: ProcessingResult<Double?> = ProcessingResult.NO_VALUE_ERROR

    override fun processNextValue(args: Iterator<String>): ProcessingResult<Double?> {
        val string = expectNext(args).valueOrOnError { return it }

        return string.toDoubleOrNull()?.let { ProcessingResult.Value(it) }
            ?: ProcessingResult.Error("`$string` is not a valid double")
    }
}
