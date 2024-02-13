package lunakoly.arrrgh

internal class ArrrghParserContext : ArrrghParserContextOwner {
    private val errors = mutableListOf<String>()
    private val processors = mutableMapOf<String, Processor<*>>()
    private val defaultProcessor = ListProcessor()

    override fun default() = defaultProcessor

    override infix fun <R> String.denotes(processor: Processor<R>) = processor.also {
        if (processors[this] != null) {
            errors.add("Duplicate processors registered for `$this`")
        }

        processors[this] = processor
    }

    private fun pickProcessorFor(iterator: PeekingIterator<String>): Pair<String?, Processor<*>> {
        val option = iterator.peek()

        processors[option]?.let {
            iterator.next()
            return option to it
        }

        return null to defaultProcessor
    }

    fun parse(args: Array<String>): ProcessingResult.Error? {
        val iterator = args.iterator().withPeek()
        var error: ProcessingResult.Error? = null

        while (iterator.hasNext()) {
            val (option, processor) = pickProcessorFor(iterator)
            error = error combineWith processor.processNext(iterator).withPrefixes(option.orEmpty() + " > ")
        }

        for ((option, processor) in processors) {
            error = error combineWith processor.result.selfIfErrorOrNull().withPrefixes("$option > ")
        }

        return error combineWith defaultProcessor.result.selfIfErrorOrNull()
    }
}
