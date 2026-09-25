package nl.joozd.airportsource.args

internal sealed interface ArgumentDescription {
    val name: ArgName
    val shortName: String?
    val helpText: String
    val numberOfArguments: Int
    fun build(iterator: Iterator<String>): Argument = buildArgument(name, iterator, numberOfArguments)

    data object OUTPUT_DIR: ArgumentDescription {
        override val name: ArgName = CliArgNames.OUTPUT_DIR
        override val shortName: String = "-o"
        override val helpText: String = "Specifies the output directory for the application. Usage: --${name.argName} <directory_path>"
        override val numberOfArguments: Int = 1
    }

    data object HELP: ArgumentDescription {
        override val name: ArgName = CliArgNames.HELP
        override val shortName: String = "-h"
        override val helpText: String = "Displays this help message. Usage: --${name.argName}"
        override val numberOfArguments: Int = 0
    }
}

private fun buildArgument(name: ArgName, iterator: Iterator<String>, argsNeeded: Int): Argument {
    val args = buildList {
        repeat(argsNeeded) {
            if (!iterator.hasNext()) throw InvalidArgumentCountException(name.argName)
            add(iterator.next())
        }
    }
    return Argument(name, ArgValues(args))
}