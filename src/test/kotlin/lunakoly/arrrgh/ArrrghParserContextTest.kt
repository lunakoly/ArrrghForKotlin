package lunakoly.arrrgh

import kotlin.test.Test
import kotlin.test.assertEquals

class ArrrghParserContextTest {
    @Test
    fun testGoodParse() {
        val args = "--params a --params b --data c --vata d --rata e --do-check f g".split(" ").toTypedArray()
        val parser = ArrrghParserContext()

        val listParams by parser.list("--params")
        val requiredWithoutDefault by parser.requiredString("--data", null)
        val requiredWithDefault by parser.requiredString("--vata", "empty")
        val optional by parser.optionalString("--rata")
        val flag by parser.boolean("--do-check")
        val rest by parser.default()

        val error = parser.parse(args)

        assertEquals(listOf("a", "b"), listParams)
        assertEquals("c", requiredWithoutDefault)
        assertEquals("d", requiredWithDefault)
        assertEquals("e", optional)
        assertEquals(true, flag)
        assertEquals(listOf("f", "g"), rest)
        assertEquals(null, error)
    }

    @Test
    fun testBadParse() {
        val args = "--vata d --vata e --do-check --do-check".split(" ").toTypedArray()
        val parser = ArrrghParserContext()

        val listParams by parser.list("--params")
        val requiredWithoutDefault by parser.requiredString("--data", null)
        val requiredWithDefault by parser.requiredString("--vata", "empty")
        val optional by parser.optionalString("--rata")
        val flag by parser.boolean("--do-check")
        val rest by parser.default()

        val error = parser.parse(args)

        assertEquals(
            listOf(
                "--vata > Duplicate argument `e`, the previous one was `d`",
                "--do-check > The flag was passed twice",
                "--data > Expected a value",
            ),
            error?.messages.orEmpty(),
        )
    }
}
