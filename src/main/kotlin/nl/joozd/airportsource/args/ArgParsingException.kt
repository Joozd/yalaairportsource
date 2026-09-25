@file:Suppress("CanBeParameter")

package nl.joozd.airportsource.args

/**
 * Base class for errors encountered while parsing command-line arguments.
 *
 * Specific parsing failures are represented by subclasses such as
 * [NotAnArgumentNameException], [UnknownArgumentNameException],
 * [DuplicateArgumentException], and [InvalidArgumentCountException].
 *
 * Catching this type allows callers to handle all argument parsing errors
 * through a single exception type while retaining access to the specific cause.
 */
sealed class ArgParsingException(message: String) : IllegalArgumentException(message)

/**
 * Thrown when an argument name does not start with `-` or `--`.
 *
 * @property name invalid argument name.
 */
class NotAnArgumentNameException(
    val name: String
) : ArgParsingException(
    "Argument name must start with '-' or '--'. Found '$name' which does not."
)

/**
 * Thrown when an unknown argument name is encountered.
 *
 * @property name unknown argument name.
 */
class UnknownArgumentNameException(
    val name: String
) : ArgParsingException(
    "Unknown argument name '$name'."
)

/**
 * Thrown when an argument is supplied more than once.
 *
 * @property name duplicated argument name, normalized to its long form.
 */
class DuplicateArgumentException(
    val name: String
) : ArgParsingException(
    "Argument '$name' was supplied more than once."
)

/**
 * Thrown when an Argument's builder wants more values than were provided in the CLI arguments.
 *
 * @property argumentName The name of the argument that expected more values than were provided.
 */
class InvalidArgumentCountException(
    val argumentName: String
) : ArgParsingException(
    "Argument '$argumentName' requires a value that is not provided."
)