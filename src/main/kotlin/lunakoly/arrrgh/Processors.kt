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

class BooleanProcessor : SingleValueProcessor<Boolean> {
    override var explicitlySetValue: ProcessingResult<Boolean> = ProcessingResult.Error("Expected a value")
    override val result: ProcessingResult<Boolean> get() = explicitlySetValue.selfIfValueOrNull() ?: ProcessingResult.Value(false)

    override fun processNextValue(args: Iterator<String>) = ProcessingResult.Value(true)

    override fun reportDuplication(previous: ProcessingResult.Value<Boolean>, next: ProcessingResult.Value<Boolean>) =
        ProcessingResult.Error("The flag was passed twice")
}

class RequiredStringProcessor(defaultValue: String? = null) : SingleValueProcessor<String> {
    override var explicitlySetValue: ProcessingResult<String> = ProcessingResult.Error("Expected a value")
    override val result: ProcessingResult<String> get() = explicitlySetValue.selfIfValueOrNull() ?: initialValue

    private val initialValue = when {
        defaultValue != null -> ProcessingResult.Value(defaultValue)
        else -> explicitlySetValue
    }

    override fun processNextValue(args: Iterator<String>) = expectNext(args)
}

class OptionalStringProcessor : SingleValueProcessor<String?> {
    override var explicitlySetValue: ProcessingResult<String?> = ProcessingResult.Error("Expected a value")
    override val result: ProcessingResult<String?> get() = explicitlySetValue.selfIfValueOrNull() ?: ProcessingResult.Value(null)

    override fun processNextValue(args: Iterator<String>) = expectNext(args)
}
