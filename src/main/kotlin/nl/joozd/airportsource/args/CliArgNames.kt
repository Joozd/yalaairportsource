package nl.joozd.airportsource.args

/**
 * cli arguments that start with "--" (long arguments)
 * These must all be lowercase in here. Not case-sensitive on CLI.
 */
internal object CliArgNames {
    val OUTPUT_DIR = ArgName("output-dir")
    val HELP = ArgName("help")
}

/**
 * Helper to convert short cli arguments (like "-o") to long cli arguments (like "--outputDir")
 */
internal fun shortCliArgToLong(arg: String): ArgName? = when (arg) {
    "-o" -> CliArgNames.OUTPUT_DIR
    "-h" -> CliArgNames.HELP
    else -> null
}