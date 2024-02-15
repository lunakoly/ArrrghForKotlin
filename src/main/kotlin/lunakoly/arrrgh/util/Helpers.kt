package lunakoly.arrrgh.util

inline fun <reified E: Enum<E>> namesToValues() = enumValues<E>().associateBy { it.name }

inline fun <reified E: Enum<E>> dashedNamesToValues() = namesToValues<E>().mapKeys { it.key.dashed() }

fun String.dashed() = lowercase().replace("_", "-")
