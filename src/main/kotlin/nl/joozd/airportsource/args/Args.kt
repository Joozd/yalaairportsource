package nl.joozd.airportsource.args

import java.nio.file.Path
import kotlin.io.path.Path

class Args private constructor(
    val outputDir: Path,
    val source: Sources,
    val maxDeltas: Int,
    val help: Boolean
){
    class Builder {
        var outputDir: String = "output"
        var source = Sources.OUR_AIRPORTS

        var maxDeltas: Int = 30

        var help: Boolean = false

        fun build(): Args {
            return Args(
                outputDir = Path(outputDir),
                source,
                maxDeltas,
                help
            )
        }
    }
}