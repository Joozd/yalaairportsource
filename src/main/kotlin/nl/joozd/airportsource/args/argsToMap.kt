package nl.joozd.airportsource.args

/**
 * Converts CLI arguments into a map of parsed arguments.
 *
 * Long argument names must start with `--`. Short argument names start with `-`
 * and are resolved to their corresponding long argument name.
 *
 * Each argument consumes the number of values defined by its argument description,
 * and an argument may only be supplied once.
 *
 * @throws InvalidArgumentCountException if an argument does not have enough values.
 * @throws NotAnArgumentNameException if an argument name does not start with `-`.
 * @throws UnknownArgumentNameException if an argument name is not recognized.
 * @throws DuplicateArgumentException if an argument is supplied more than once.
 */
internal fun argsToMap(args: Array<String>): Map<ArgName, Argument> {
    return buildMap {
        with(args.iterator()) {
            while (hasNext()) {
                val argumentName = parseArgumentName(next())
                if(argumentName in this@buildMap) throw DuplicateArgumentException(argumentName.argName)
                val argument = argumentsMap[argumentName]?.build(this) ?: throw UnknownArgumentNameException(argumentName.argName)
                put(argumentName, argument)
            }
        }
    }
}

/**
 * Validates and normalizes a CLI argument name to its long form.
 *
 * @throws NotAnArgumentNameException if [name] does not start with `-`.
 * @throws UnknownArgumentNameException if [name] is not recognized.
 */
private fun parseArgumentName(name: String): ArgName {
    val lowercaseName = name.lowercase()
    if (!lowercaseName.startsWith("-")) {
        throw NotAnArgumentNameException(lowercaseName)
    }

    return if (lowercaseName.startsWith("--")) {
        ArgName(lowercaseName.drop(2))
            .also {
                if (it !in argumentsMap.keys) {
                    throw UnknownArgumentNameException(name)
                }
            }
    } else {
        shortCliArgToLong(lowercaseName)
            ?: throw UnknownArgumentNameException(name)
    }
}

