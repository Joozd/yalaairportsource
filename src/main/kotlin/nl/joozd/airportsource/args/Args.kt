package nl.joozd.airportsource.args

class Args private constructor(
    val outputDir: String,
    val help: Boolean
){
    class Builder {
        var outputDir: String = "./output"
        var help: Boolean = false

        fun build(): Args {
            return Args(outputDir, help)
        }
    }
}