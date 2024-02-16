package lunakoly.arrrgh

import kotlin.test.Test
import kotlin.test.assertEquals

class ArrrghParserContextTest {
    enum class TestEnum {
        LONG_OPTION, DEFAULT,
    }

    @Test
    fun testGoodParse() {
        val args = listOf(
            "--params a --params b --data c --vata d --rata e --do-check f g",
            "--mode default --rode long-option",
            "--accuracy 0.1"
        ).joinToString(" ").split(" ").toTypedArray()
        val parser = ArrrghParserContext()

        val listParams by parser.list("--params")
        val requiredWithoutDefault by parser.requiredString("--data", null)
        val requiredWithDefault by parser.requiredString("--vata", "empty")
        val optional by parser.optionalString("--rata")
        val flag by parser.boolean("--do-check")
        val requiredEnumWithoutDefault by parser.requiredEnum<TestEnum>("--mode", null)
        val requiredEnumWithDefault by parser.requiredEnum("--rode", TestEnum.DEFAULT)
        val optionalEnum by parser.optionalEnum<TestEnum>("--bode")
        val fraction by parser.requiredDouble("--fraction", 0.5)
        val accuracy by parser.optionalDouble("--accuracy")
        val rest by parser.default()

        val error = parser.parse(args)

        assertEquals(listOf("a", "b"), listParams)
        assertEquals("c", requiredWithoutDefault)
        assertEquals("d", requiredWithDefault)
        assertEquals("e", optional)
        assertEquals(true, flag)
        assertEquals(listOf("f", "g"), rest)
        assertEquals(null, error)
        assertEquals(TestEnum.DEFAULT, requiredEnumWithoutDefault)
        assertEquals(TestEnum.LONG_OPTION, requiredEnumWithDefault)
        assertEquals(null, optionalEnum)
        assertEquals(0.5, fraction)
        assert(accuracy?.let { 0.09 < it && it < 0.11 } == true)
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testBadParse() {
        val args = listOf(
            "--vata d --vata e --do-check --do-check",
            "--rode lolkek --bode default",
            "--fraction test --fraction null",
        ).joinToString(" ").split(" ").toTypedArray()
        val parser = ArrrghParserContext()

        val listParams by parser.list("--params")
        val requiredWithoutDefault by parser.requiredString("--data", null)
        val requiredWithDefault by parser.requiredString("--vata", "empty")
        val optional by parser.optionalString("--rata")
        val flag by parser.boolean("--do-check")
        val requiredEnumWithoutDefault by parser.requiredEnum<TestEnum>("--mode", null)
        val requiredEnumWithDefault by parser.requiredEnum("--rode", TestEnum.DEFAULT)
        val optionalEnum by parser.optionalEnum<TestEnum>("--bode")
        val fraction by parser.requiredDouble("--fraction", 0.5)
        val accuracy by parser.optionalDouble("--accuracy")
        val rest by parser.default()

        val error = parser.parse(args)

        assertEquals(
            listOf(
                "--vata > Duplicate argument `e`, the previous one was `d`",
                "--do-check > The flag was passed twice",
                "--rode > `lolkek` is not a supported value. The supported ones are: `long-option`, `default`",
                "--fraction > `test` is not a valid double",
                "--fraction > `null` is not a valid double",
                "--data > Requires a value",
                "--mode > Requires a value",
            ).joinToString("\n"),
            error?.messages.orEmpty().joinToString("\n"),
        )
    }
}
