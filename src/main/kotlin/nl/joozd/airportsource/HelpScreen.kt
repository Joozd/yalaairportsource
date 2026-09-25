package nl.joozd.airportsource

import nl.joozd.airportsource.args.ArgumentDescription
import nl.joozd.airportsource.args.argumentsMap

private const val HELP_LINE_WIDTH = 160
private const val HELP_INDENT = "  "
private const val HELP_COLUMN_GAP = "  "

/**
 * Prints all available CLI arguments and their help text.
 */
internal fun printHelp() {
    val entries = argumentsMap.values.map(::toHelpEntry)
    val nameColumnWidth = entries.maxOfOrNull { it.name.length } ?: 0
    val helpTextWidth = HELP_LINE_WIDTH -
            HELP_INDENT.length -
            nameColumnWidth -
            HELP_COLUMN_GAP.length

    println("Available arguments:")

    entries.forEach { entry ->
        printHelpEntry(entry, nameColumnWidth, helpTextWidth)
    }
}

/**
 * Converts an argument description to its printable help representation.
 */
private fun toHelpEntry(argument: ArgumentDescription): HelpEntry {
    val names = listOfNotNull(
        argument.shortName,
        "--${argument.name.argName}"
    ).joinToString(", ")

    return HelpEntry(
        name = names,
        helpText = argument.helpText
    )
}

/**
 * Prints a single help entry, wrapping long help text onto aligned continuation lines.
 */
private fun printHelpEntry(
    entry: HelpEntry,
    nameColumnWidth: Int,
    helpTextWidth: Int
) {
    val wrappedHelp = wrapText(entry.helpText, helpTextWidth)
    val continuationIndent =
        HELP_INDENT + " ".repeat(nameColumnWidth) + HELP_COLUMN_GAP

    wrappedHelp.forEachIndexed { index, line ->
        if (index == 0) {
            println(
                HELP_INDENT +
                        entry.name.padEnd(nameColumnWidth) +
                        HELP_COLUMN_GAP +
                        line
            )
        } else {
            println(continuationIndent + line)
        }
    }
}

/**
 * Wraps [text] at word boundaries to lines no longer than [maxWidth].
 *
 * Words longer than [maxWidth] are emitted unbroken.
 */
private fun wrapText(text: String, maxWidth: Int): List<String> {
    if (text.isBlank()) return listOf("")
    if (maxWidth <= 0) return listOf(text)

    return buildList {
        var currentLine = StringBuilder()

        text.split(Regex("\\s+")).forEach { word ->
            val separatorLength = if (currentLine.isEmpty()) 0 else 1

            if (currentLine.length + separatorLength + word.length > maxWidth) {
                if (currentLine.isNotEmpty()) {
                    add(currentLine.toString())
                    currentLine = StringBuilder()
                }
            }

            if (currentLine.isNotEmpty()) {
                currentLine.append(' ')
            }

            currentLine.append(word)
        }

        if (currentLine.isNotEmpty()) {
            add(currentLine.toString())
        }
    }
}

/**
 * Printable representation of a CLI argument in the help output.
 *
 * @property name formatted argument names.
 * @property helpText description shown next to the argument.
 */
private data class HelpEntry(
    val name: String,
    val helpText: String
)