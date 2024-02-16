package lunakoly.arrrgh

import lunakoly.arrrgh.util.dashedNamesToValues

interface ArrrghParserContextOwner {
    infix fun <R> String.denotes(processor: Processor<R>): Processor<R>

    fun default(): ListProcessor
}

fun ArrrghParserContextOwner.list(parameterName: String) = parameterName denotes ListProcessor()
fun ArrrghParserContextOwner.boolean(parameterName: String) = parameterName denotes BooleanProcessor()
fun ArrrghParserContextOwner.optionalString(parameterName: String) = parameterName denotes OptionalStringProcessor()
fun ArrrghParserContextOwner.requiredString(parameterName: String, defaultValue: String? = null) =
    parameterName denotes OptionalStringProcessor().toRequired(defaultValue)
inline fun <reified E: Enum<E>> ArrrghParserContextOwner.optionalEnum(parameterName: String) =
    parameterName denotes OptionalEnumProcessor(dashedNamesToValues<E>())
inline fun <reified E: Enum<E>> ArrrghParserContextOwner.requiredEnum(parameterName: String, defaultValue: E? = null) =
    parameterName denotes OptionalEnumProcessor(dashedNamesToValues<E>()).toRequired(defaultValue)
fun ArrrghParserContextOwner.optionalDouble(parameterName: String) = parameterName denotes OptionalDoubleProcessor()
fun ArrrghParserContextOwner.requiredDouble(parameterName: String, defaultValue: Double? = null) =
    parameterName denotes OptionalDoubleProcessor().toRequired(defaultValue)
