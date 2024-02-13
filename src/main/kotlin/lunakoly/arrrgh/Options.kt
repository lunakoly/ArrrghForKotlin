package lunakoly.arrrgh

open class Options private constructor(
    internal val parsingContext: ArrrghParserContext,
) : ArrrghParserContextOwner by parsingContext {
    constructor() : this(ArrrghParserContext())
}

fun <T : Options> T.fillFrom(args: Array<String>): T? {
    return when (val messages = parsingContext.parse(args)?.messages) {
        null -> this
        else -> null.also { messages.forEach { println("Error > $it") } }
    }
}
