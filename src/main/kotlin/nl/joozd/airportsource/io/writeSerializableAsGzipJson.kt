package nl.joozd.airportsource.io

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.nio.file.Path
import java.util.zip.GZIPOutputStream
import kotlin.io.path.outputStream
import kotlin.io.path.writeText

internal class Writer {
    /**
     * Serializes [value] as JSON, compresses it with GZIP, and writes it to [target].
     */
    @kotlinx.serialization.ExperimentalSerializationApi
    inline fun <reified T> write(value: T, target: Path) {
        GZIPOutputStream(target.outputStream()).use { output ->
            Json.encodeToStream(value, output)
        }
    }

    companion object{
        internal const val JSON_EXTENSION = ".gz"
    }
}