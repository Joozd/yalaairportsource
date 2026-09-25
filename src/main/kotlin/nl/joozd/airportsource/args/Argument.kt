package nl.joozd.airportsource.args

internal class Argument(
    val name: ArgName,
    val values: ArgValues
)

@JvmInline
internal value class ArgName(val argName: String)

@JvmInline
internal value class ArgValues(val values: List<String>){
    val first get() = values[0] // will throw if not present
}