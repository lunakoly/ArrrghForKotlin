package lunakoly.arrrgh

interface ArrrghParserContextOwner {
    infix fun <R> String.denotes(processor: Processor<R>): Processor<R>

    fun default(): ListProcessor
}

fun ArrrghParserContextOwner.list(parameterName: String) = parameterName denotes ListProcessor()
fun ArrrghParserContextOwner.boolean(parameterName: String) = parameterName denotes BooleanProcessor()
fun ArrrghParserContextOwner.requiredString(parameterName: String, defaultValue: String? = null) =
    parameterName denotes RequiredStringProcessor(defaultValue)
fun ArrrghParserContextOwner.optionalString(parameterName: String) = parameterName denotes OptionalStringProcessor()
