package nl.joozd.airportsource.args

internal fun buildArgsFromCli(args: Array<String>): Args {
    return Args.Builder().apply {
        val argsMap = argsToMap(args)
        argsMap[CliArgNames.OUTPUT_DIR]?.let { argument -> outputDir = argument.values.first }
        argsMap[CliArgNames.HELP]?.let { help = true }
    }.build()
}

